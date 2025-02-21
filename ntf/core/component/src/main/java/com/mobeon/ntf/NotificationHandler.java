/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.util.DesignSequenceDiagram;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackHandler;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackUtil;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.coremgmt.reminder.SmsReminder;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.provisioning.businessrule.ProvisioningConstants;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.trafficeventsender.MfsClient;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.mail.EmailGenerator;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.SystemNotificationFilterInfo;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.cancel.CancelSmsHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.out.FeedbackHandlerImpl;
import com.mobeon.ntf.out.autounlockpin.AutoUnlockPin;
import com.mobeon.ntf.out.autounlockpin.AutoUnlockPinUtil;
import com.mobeon.ntf.out.delayedevent.DelayedEventHandler;
import com.mobeon.ntf.out.email.EmailOut;
import com.mobeon.ntf.out.fax.FaxPrintOut;
import com.mobeon.ntf.out.mms.MMSOut;
import com.mobeon.ntf.out.outdial.OutdialNotificationOut;
import com.mobeon.ntf.out.pager.PagOut;
import com.mobeon.ntf.out.sip.SIPOut;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.out.vvm.VVMSystemDeactivator;
import com.mobeon.ntf.out.vvm.VvmEvent.VvmEventTypes;
import com.mobeon.ntf.out.vvm.VvmHandler;
import com.mobeon.ntf.out.wap.WapOut;
import com.mobeon.ntf.out.wireline.CmwOut;
import com.mobeon.ntf.out.wireline.WmwOut;
import com.mobeon.ntf.slamdown.SlamdownList;
import com.mobeon.ntf.slamdown.SlamdownListHandler;
import com.mobeon.ntf.text.TemplateMessageGenerationException;
import com.mobeon.ntf.userinfo.CmwFilterInfo;
import com.mobeon.ntf.userinfo.EmailFilterInfo;
import com.mobeon.ntf.userinfo.MmsFilterInfo;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.OdlFilterInfo;
import com.mobeon.ntf.userinfo.PagFilterInfo;
import com.mobeon.ntf.userinfo.SIPFilterInfo;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.WapFilterInfo;
import com.mobeon.ntf.userinfo.WmwFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo.NotifState;
import com.mobeon.ntf.userinfo.mcd.McdUserInfo;
import com.mobeon.ntf.util.threads.NtfThread;
import com.mobeon.ntf.util.time.NtfTime;

/**
 * NotificationHandler controls the flow of notifications from the in interfaces to the out interfaces. It reads user data and
 * invokes filter processing. There may be many instances, each running its own thread.
 */
public class NotificationHandler extends NtfThread implements Constants {

    private static final int REPORTINTERVAL = 30; // how often to report counters when idle;
    private static LogAgent log =  NtfCmnLogger.getLogAgent(NotificationHandler.class);
    /** Used to delete mails when notification is completed */
    private static NtfEventHandler eventHandler;
    /** The input interfaces stores incoming notification in this queue */
    private static EmailGenerator inQueue;
    /** Interface to SNMP subagent */
    // private NotifSNMP snmp;
    /** Interface to MER */
    private static MerAgent mer;
    /** Finds and creates users */
    /** SMS output interface */
    private static SMSOut smsOut;
    /** Email output interface */
    private static EmailOut emailOut;
    /** MMS output interface */
    private static MMSOut mmsOut;
    private static WapOut wapOut;
    /** Outdial output interface */
    private static OutdialNotificationOut odlOut;
    /** Wireline MWI output interface */
    private static WmwOut wmwOut;
    /** pager output interface */
    private static PagOut pagOut;
    /** call Mwi output interface */
    private static CmwOut cmwOut;
    /** sip mwi output interface */
    private static SIPOut sipOut;
    /** handler for accumulated slamdown information */
    private static SlamdownListHandler slamdownHandler;
    private static CancelSmsHandler CancelSms;

    /** handler for fax print */
    private static FaxPrintOut faxPrintOut;

    /** handler for VVM information */
    private VvmHandler vvmHandler;
    private static AtomicInteger nextReport = new AtomicInteger(NtfTime.now+30);
    private static final Lock reportLock = new ReentrantLock();
    /**
     * Create vvmSystemDeactivator object. This is needed so legacy vvmTimeout event (before MiO 3.2) are processed and their files cleaned up.
     */
    private VVMSystemDeactivator vvmSystemDeactivator = new VVMSystemDeactivator(0);
    private DelayedEventHandler delayedeventHandler;
   

    private static IMfsEventManager mfsEventManager = MfsEventFactory.getMfsEvenManager();

    private static AtomicInteger numberOfNotificationMwiOff = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationSlamdown = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationMcn = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationQuota = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationSMS = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationOutdial = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationMMS = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationSipMwi = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationSipMwiUpdate = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationEmail = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationVvmDeposit = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationVvmGreeting = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationVvmExpiry = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationAppleVvmLogout = new AtomicInteger(0);

    private static AtomicInteger numberOfNotificationAppleVvmDeposit = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationAppleVvmGreeting = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationAppleVvmExpiry = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationVvmLogout = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationException = new AtomicInteger(0);
    private static AtomicInteger numberOfFaxPrint = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationDelayedEvent = new AtomicInteger(0);

    private static boolean appleServiceExist = false;

    /**
     * This is the only NotificationHandler constructor.
     *
     * @param id
     *        identifies this NotificationHandler instance in log messages etc.
     * @param userFactory
     *        finds and creates users.
     * @param mer
     *        creates MER interface.
     * @param smsOut
     *        delivery interface for SMS.
     * @param mmsOut
     *        delivery interface for MMS.
     * @param wapOut
     *        delivery interface for wap push.
     * @param odlOut
     *        delivery interface for outdial.
     * @param wmwOut
     *        delivery interface for wireline MWI.
     * @param pagOut
     *        delivery interface for pagers.
     * @param cmwOut
     *        delivery interface for call MWI.
     * @param sipOut
     *        delivery interface for Sip MWI.
     * @param emailOut
     *        delivery interface for e-mail.
     * @param slamdownHandler
     *        - handler for Slamdown/Mcn.
     * @param vvmHandler
     *        - handler for VVM.
     * @param delayedeventHandler
     * @param cancelSmsHandler 
     */
    public NotificationHandler(int id, EmailGenerator emailGenerator, NtfEventHandler eventHandler, UserFactory userFactory,
            MerAgent mer, SMSOut smsOut, MMSOut mmsOut, WapOut wapOut, OutdialNotificationOut odlOut, WmwOut wmwOut, PagOut pagOut,
            CmwOut cmwOut, SIPOut sipOut, EmailOut emailOut, SlamdownListHandler slamdownHandler, FaxPrintOut faxPrintOut,
            VvmHandler vvmHandler, DelayedEventHandler delayedeventHandler, CancelSmsHandler cancelSmsHandler) {

        super("NotificationHandler-" + id);
        inQueue = emailGenerator;
        this.eventHandler = eventHandler;
        this.mer = mer;
        this.smsOut = smsOut;
        this.mmsOut = mmsOut;
        this.wapOut = wapOut;
        this.emailOut = emailOut;
        this.odlOut = odlOut;
        this.wmwOut = wmwOut;
        this.pagOut = pagOut;
        this.cmwOut = cmwOut;
        this.sipOut = sipOut;
        this.slamdownHandler = slamdownHandler;
        this.faxPrintOut = faxPrintOut;
        this.vvmHandler = vvmHandler;
        this.delayedeventHandler = delayedeventHandler;
        this.CancelSms = cancelSmsHandler;

        DesignSequenceDiagram.printFullSequence();
    }

    /**
     * Only used for JUnit test
     */
    public NotificationHandler(int id) {
        super("NotificationHandler-" + id);
        DesignSequenceDiagram.printFullSequence();
    }
    
    /**
     * ntfRun processes a single NotificationEmail and returns to check management state.
     *
     * @return true when the job of this thread is completed, i.e. never for this class.
     *
     */
    public boolean ntfRun() {
        NotificationEmail email = null;
        DesignSequenceDiagram.printFullSequence();


        email = inQueue.getNextEmail(10); // wait no more than 10 seconds.
        
        if(NtfTime.now > nextReport.get()) {logCounters();}
                
        if (email == null) {
            //Probably a timeout or interrupt, go back to run loop to check management state.            
            return false;
        }
        
        try {
            if(log.isDebugEnabled()){
                log.debug("Notification summary: " + email.summary());
                log.debug("Notification taken off queue; queue size now: " + inQueue.size());
            }
            email.init();

            // This if distributes requests to methods with different ways to handle users
            if (email.isMwiOff()) {
                
                handleMwiOff(email);

            } else if (email.isMailboxUpdate()) {
                handleMailboxUpdate(email);
            } else {
                handleSendToSubscriber(email);
            }
        } catch (Exception e) {

            Object perf = null;
            try {
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    perf = CommonOamManager.profilerAgent.enterCheckpoint("NTF.NH.10.Exception");
                }
                numberOfNotificationException.incrementAndGet();

                log.error("NotificationHandler exception: ", e);
                // Notify MRD to cancel future retry
                eventHandler.notifFailed(email.getNtfEvent());

                // Delete the pending <subscriber>/events/slamdowninformation_|mcn_<date> file.
                if (email.getNtfEvent().isEventServiceType(NtfEventTypes.SLAMDOWN.getName())
                        || email.getNtfEvent().isEventServiceType(NtfEventTypes.MCN.getName())) {
                    deleteFile(email.getNtfEvent());
                }
            } catch (OutOfMemoryError me) {
                try {
                    ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                    log.error("NTF out of memory, shutting down... ", me);
                } catch (OutOfMemoryError me2) {;} //ignore second exception
                return true; //exit.
            }
            catch (Exception e2) {
                log.error("NotificationHandler exception: ", e); //log first exception.
            } finally {
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    CommonOamManager.profilerAgent.exitCheckpoint(perf);
                }
            }
        }

        return false;
    }

    static public void deleteFile(NtfEvent ntfEvent) {
        DesignSequenceDiagram.printFullSequence();
        MfsEventManager mfsEventManager = MfsEventFactory.getMfsEvenManager();
        String filename = null;

        if (ntfEvent.isEventServiceType(NtfEventTypes.SLAMDOWN.getName())) {
            filename = ntfEvent.getEventProperties().getProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY);
        } else if (ntfEvent.isEventServiceType(NtfEventTypes.MCN.getName())) {
            filename = ntfEvent.getEventProperties().getProperty(MoipMessageEntities.MCN_EVENT_FILE_PROPERTY);
        }

        try {
            mfsEventManager.removeFile(ntfEvent.getRecipient(), filename,
                    ntfEvent.isEventServiceType(NtfEventTypes.SLAMDOWN.getName()));
        } catch (TrafficEventSenderException tee) {
            log.debug("NotificationHandler deleteFile exception: " + tee);
        }
        return;
    }

    static public void deleteFiles(String notificationNumber, String pattern) {
        DesignSequenceDiagram.printFullSequence();
        String slamdownFileNames[] = null;
        String mcnFileNames[] = null;

        if (pattern == null || pattern.isEmpty()) { return; }

        // Slamdown case
        if (pattern.equalsIgnoreCase(SlamdownList.PATTERN_FILE_ALL)) {
            /**
             * This filter is needed since deleting files (both pending "slamdowninformation_*" and final "slamdowninformationf_*"
             * does not mean delete the "slamdowninformation" that MAS is potentially currently aggregating.
             */
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().startsWith(MfsClient.EVENT_SLAMDOWNINFORMATION + SlamdownList.PATTERN_FILE_FINAL) ||
                           file.getName().startsWith(MfsClient.EVENT_SLAMDOWNINFORMATION + SlamdownList.PATTERN_FILE_PENDING);
                }
            };
            slamdownFileNames = mfsEventManager.getEventFileNames(notificationNumber, filter, true);
        } else {
            slamdownFileNames = mfsEventManager.getFilePathsNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION + pattern);
        }

        if (slamdownFileNames != null && slamdownFileNames.length > 0) {
            for (String slamdownFile : slamdownFileNames) {
                try {
                    log.debug("Deleting Slamdown file: " + slamdownFile);
                    mfsEventManager.removeFile(slamdownFile);
                } catch (TrafficEventSenderException tee) {
                    log.debug("Failed to delete Slamdown file: " + slamdownFile);
                }
            }
        }

        // Mcn case
        if (pattern.equalsIgnoreCase(SlamdownList.PATTERN_FILE_ALL)) {
            /**
             * This filter is needed since deleting files (both pending "missedcallnotification_*" and final "missedcallnotificationf_*"
             * does not mean delete the "missedcallnotification" that MAS is potentially currently aggregating.
             */
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().startsWith(MfsClient.EVENT_MISSEDCALLNOTIFICATION + SlamdownList.PATTERN_FILE_FINAL) ||
                           file.getName().startsWith(MfsClient.EVENT_MISSEDCALLNOTIFICATION + SlamdownList.PATTERN_FILE_PENDING);
                }
            };
            mcnFileNames = mfsEventManager.getEventFileNames(notificationNumber, filter, true);
        } else {
            mcnFileNames = mfsEventManager.getFilePathsNameStartingWith(notificationNumber, MfsClient.EVENT_MISSEDCALLNOTIFICATION + pattern);
        }

        if (mcnFileNames != null && mcnFileNames.length > 0) {
            for (String mcnFile : mcnFileNames) {
                try {
                    log.debug("Deleting Mcn file: " + mcnFile);
                    mfsEventManager.removeFile(mcnFile);
                } catch (TrafficEventSenderException tee) {
                    log.debug("Failed to delete Mcn file: " + mcnFile);
                }
            }
        }
    }

    /**
     * The shutdown loop tries to drain the queue until forced to shutdown
     * A small guard time is included to allow other threads to drain into this one.
     * @return true when exit
     **/
    public boolean shutdown() {
        if (isInterrupted()) {
            return true;
        } //exit immediately if interrupted..

        if (inQueue.size() == 0)
        {
                //give a short time for new items to be queued in workers, to allow other threads to empty there queues.
                if (inQueue.isIdle(2,TimeUnit.SECONDS)) {
                    DesignSequenceDiagram.printFullSequence();
                    return true;
                }
                else
                {
                    if (inQueue.waitNotEmpty(2, TimeUnit.SECONDS)) {
                        if (ntfRun() == true ) {
                            DesignSequenceDiagram.printFullSequence();
                            return true;
                         } else
                         {
                             return false;
                         }
                    } else
                    {
                        DesignSequenceDiagram.printFullSequence();
                        return true;
                    }
                }
        } else {
            if (ntfRun() == true ) {
                    DesignSequenceDiagram.printFullSequence();
                    return true;
            } else
            {
                return false;
            }
        }
    }

    /**
     * Calculate the expiry time and say if the mail has expired.
     *
     * @param timeToLive
     *        how many hours before a notification expires.
     * @param email
     *        The email containing the notification.
     * @return true iff the email has expired.
     */
    public static boolean hasExpired(int timeToLive, NotificationEmail email) {
        DesignSequenceDiagram.printFullSequence();
        GregorianCalendar calendar;

        if (timeToLive < 0) {
            return false;
        }

        calendar = new GregorianCalendar();
        calendar.setTime(email.getMessageReceivedDate());
        calendar.add(Calendar.HOUR_OF_DAY, timeToLive);
        return (new Date().after(calendar.getTime()));
    }

    /**
     * handleSendToSubscriber notifies all receivers in the mail (TO, CC, BCC) according to their filter settings.
     *
     * @param email
     *        the new mail to notify about.
     */
    private void handleSendToSubscriber(NotificationEmail email) throws InterruptedException{
        DesignSequenceDiagram.printFullSequence();

        NotificationGroup ng = new NotificationGroup(eventHandler, email, log, mer);
        UserInfo user = null;

        if (email.isMcn()) {
            user = UserFactory.findUserByTelephoneNumber(email.getReceiver());
            if (user != null) {
                if (user.hasMOIPCnService()) {
                    if (user.hasMcnSubscribedService() && Config.isMcnSubscribedEnabled()) {
                        // McnSubscribed
                        log.debug("MIO VVS MCNSubscribed subscriber " + email.getReceiver() + " found for MCNSubscribed notification");
                    } else {
                        // Mcn internal
                        log.debug("MIO VVS subscriber " + email.getReceiver() + " found for MCN notification");
                    }
                } else {
                    // Mcn internal
                    log.debug("MIO non-VVS subscriber " + email.getReceiver() + " found for MCN notification");
                }
            } else {
                // MCN external
                // Non-subscriber case, create an empty McdUserInfo.
                user = new McdUserInfo(email.getReceiver(), Config.getMcnLanguage());
                log.debug("Non-MIO subscriber " + email.getReceiver() + " for MCN notification");
            }

            ng.addUser(user);
            handleSingleRecipient(user, ng);
        } else {
            // Subscriber case
            user = UserFactory.findUserByTelephoneNumber(email.getReceiver());
            if (user != null) {
                if (!user.hasMOIPCnService()) {
                    log.error("Subscriber " + email.getReceiver() + " has VVS service disabled");
                    ng.setResult(Constants.FEEDBACK_STATUS_DISABLED);
                } else {
                    ng.addUser(user);
                    handleSingleRecipient(user, ng);
                }
            } else {
                //
                log.error("Subscriber " + email.getReceiver() + " not found, will retry later");
                ng.setResult(Constants.FEEDBACK_STATUS_RETRY);
            }
        }
        ng.noMoreUsers();
    }

    /**
     * handleQuotaExceeded handles special notification behaviour when the users inbox is full.
     *
     * @param user
     *        information about the receiver.
     * @param ng
     *        collects results for all notifications from this mail.
     * @param inbox
     *        information from the users inbox.
     * @return int number of requests recorded with the SMS-c
     */
    private int handleQuotaExceeded(UserInfo user, NotificationGroup ng, UserMailbox inbox, NotificationEmail email) {
        DesignSequenceDiagram.printFullSequence();
        int count = 0;
        int delay = 1;
        String template = Config.getQuotaTemplate();
        if (user.isQuotaPerType()) {
            template = Config.getQuotaPerTypeTemplate();
        }

        String[] numbers = user.getFilter().getNotifNumbers("SMS", TRANSPORT_MOBILE, email);
        if (numbers == null || numbers.length == 0) {
            return 0;
        }

        count = handleSmsOut(user, new SystemNotificationFilterInfo(template, numbers), ng, ng.getEmail(), inbox,
                Config.getSourceAddress(NotificationConfigConstants.MAIL_QUOTA_EXCEEDED, user.getCosName()),
                user.getValidity_mailQuotaExceeded(), delay);

        return count;
    }

    /**
     * handleQuotaExceeded handles special notification behaviour when the users inbox is full.
     *
     * @param user
     *        information about the receiver.
     * @param ng
     *        collects results for all notifications from this mail.
     * @param inbox
     *        information from the users inbox.
     * @return int number of requests recorded with the SMS-c
     */
    private int handleQuotaAlmostExceeded(UserInfo user, NotificationGroup ng, UserMailbox inbox, NotificationEmail email) {
        DesignSequenceDiagram.printFullSequence();
        int count = 0;
        int delay = 1;
        String template = Config.getHighQuotaTemplate();
        if (user.isQuotaPerType()) {
            template = Config.getHighQuotaPerTypeTemplate();
        }

        String[] numbers = user.getFilter().getNotifNumbers("SMS", TRANSPORT_MOBILE, email);
        if (numbers == null || numbers.length == 0) {
            return 0;
        }

        count = handleSmsOut(user, new SystemNotificationFilterInfo(template, numbers), ng, ng.getEmail(), inbox,
                Config.getSourceAddress(NotificationConfigConstants.MAIL_QUOTA_HIGH_LEVEL_EXCEEDED, user.getCosName()),
                user.getValidity_mailQuotaHighLevelExceeded(), delay);
        return count;
    }

    /**
     * sendToOutInterfaces gives all output interfaces the opportunity to send the notification.
     */
    private void sendToOutInterfaces(int deliveryCount, UserInfo user, NotificationGroup ng, UserMailbox inbox, int delay,
            StringBuffer logText) throws InterruptedException {
        DesignSequenceDiagram.printFullSequence();
        GregorianCalendar receivedDate = new GregorianCalendar();
        int flashCount = 0;
        int tempCount = 0;
        NotificationEmail email = ng.getEmail();
        NotificationFilter filter = user.getFilter();

        flashCount = handleSmsOut(user, filter.getFlashFilterInfo(email, receivedDate, ng), ng, email, inbox,
                Config.getSourceAddress("flash", user.getCosName()), user.getValidity_flash(), delay);
        if (flashCount > 0) {
            logText.append(flashCount + " SmsFlash ");
        }

        deliveryCount += flashCount;
        tempCount = deliveryCount;

        deliveryCount += handleSmsOut(user, filter.getSmsFilterInfo(email, receivedDate, ng), ng, email, inbox,
                getSourceAddressEmail(email, user.getCosName()),
                (email.isSlamdown() ? user.getValidity_slamdown() : user.getNotifExpTime()), (flashCount > 0 ? delay + 5 : delay));
        if (tempCount < deliveryCount) {
            logText.append(deliveryCount - tempCount + " SMS ");
            tempCount = deliveryCount;
        }

        deliveryCount += handleOdlOut(user, filter.getOdlFilterInfo(email, receivedDate, ng), ng, email, inbox);
        if (tempCount < deliveryCount) {
            logText.append(deliveryCount - tempCount + " Outdial ");
            tempCount = deliveryCount;
        }

        deliveryCount += handleMmsOut(user, filter.getMmsFilterInfo(email, receivedDate, ng), ng, email, inbox);
        if (tempCount < deliveryCount) {
            logText.append(deliveryCount - tempCount + " MMS ");
            tempCount = deliveryCount;
        }

        deliveryCount += handleWmwOut(user, filter.getWmwFilterInfo(email, receivedDate, ng), ng, email, inbox);
        if (tempCount < deliveryCount) {
            logText.append(deliveryCount - tempCount + " Wmw ");
            tempCount = deliveryCount;
        }

        deliveryCount += handlePagOut(user, filter.getPagFilterInfo(email, receivedDate, ng), ng, email);
        if (tempCount < deliveryCount) {
            logText.append(deliveryCount - tempCount + " PagerOut ");
            tempCount = deliveryCount;
        }

        deliveryCount += handleCmwOut(user, filter.getCmwFilterInfo(email, receivedDate, ng), ng, email);
        if (tempCount < deliveryCount) {
            logText.append(deliveryCount - tempCount + " Cmw ");
            tempCount = deliveryCount;
        }
        deliveryCount += handleWapOut(user, filter.getWapFilterInfo(email, receivedDate, ng), ng, email, inbox);
        if (tempCount < deliveryCount) {
            logText.append(deliveryCount - tempCount + " WAP ");
            tempCount = deliveryCount;
        }

        deliveryCount += handleSIPOut(user, filter.getSIPFilterInfo(email, receivedDate, ng), ng, email, inbox);
        if (tempCount < deliveryCount) {
            logText.append(deliveryCount - tempCount + " SIPMWIOut ");
            tempCount = deliveryCount;
        }

        deliveryCount += handleEmailOut(user, filter.getEmailFilterInfo(email, receivedDate, ng), ng, email, inbox,
                user.getNotifExpTime());
        if (tempCount < deliveryCount) {
            logText.append(deliveryCount - tempCount + " Email ");
            tempCount = deliveryCount;
        }

        // This call MUST be last as it needs to know whether other notifs have been sent
        deliveryCount += handleVvmDepositOut(user, ng, inbox, deliveryCount > 0);
        if (tempCount < deliveryCount) {
            logText.append(deliveryCount - tempCount + " VvmDeposit ");
            tempCount = deliveryCount;
        }

        log.debug("Delivery count: " + deliveryCount);
        log.debug("Expecting " + logText.toString() + "from various interfaces for subscriber " + user.getTelephoneNumber());

        ng.setOutCount(user, deliveryCount);
    }

    /**
     * handleSingleRecipient notifies a user to his selected output interfaces.
     *
     * @param user
     *        the user information.
     * @param ng
     *        collects results for all notifications from this mail.
     */
    private void handleSingleRecipient(UserInfo user, NotificationGroup ng) throws InterruptedException{
        DesignSequenceDiagram.printFullSequence();
        int delay = 0;
        NotificationEmail email = ng.getEmail();
        depositType depType= email.getDepositType();
        int deliveryCount = 0;
        StringBuffer logText = new StringBuffer();
        UserMailbox inbox = null;

        if (!email.isMcn()) {
            inbox = email.getUserMailbox();
        }

        if (email.isSlamdown()) {
            log.debug("Slamdown for subscriber " + email.getReceiver());
            deliveryCount = handleSlamdown(user, ng);
            ng.setOutCount(user, deliveryCount);
        } else if (email.isFaxPrint()) {
            log.debug("Fax Print for subscriber " + email.getReceiver());
            deliveryCount = handleFaxPrint(user, ng);
            ng.setOutCount(user, deliveryCount);
        } else if (email.isFaxDeliveryReceipt()) {
            log.debug("Fax Print receipt for subscriber " + email.getReceiver());
            deliveryCount = handleFaxPrintReceipt(user, ng);
            ng.setOutCount(user, deliveryCount);
        } else if (email.isMcn()) {
            log.debug("Mcn for non-subscriber " + email.getReceiver());
            deliveryCount = handleMcn(user, ng);
            ng.setOutCount(user, deliveryCount);
        } else if (email.isVvmGreeting()) {
            log.debug("VVM Greeting for subscriber " + email.getReceiver());
            deliveryCount = handleVvmGreeting(user, ng, inbox);
            ng.setOutCount(user, deliveryCount);
        } else if (email.isVvmExpiry()) {
            log.debug("VVM Expiry for subscriber " + email.getReceiver());
            deliveryCount = handleVvmExpiry(user, ng, inbox);
            ng.setOutCount(user, deliveryCount);
        } else if (email.isVvaSms()) {
            log.debug("handleSingleRecipient:handling VvaSms for  " + email.getReceiver());
            deliveryCount = handleVvaSmsOut(user, ng);
        } else if (email.isAutoUnlockPin()) {
            log.debug("handleSingleRecipient:handling AutoUnlockPin for  " + email.getReceiver());
            AutoUnlockPin.handleAutoUnlockPin(email, user);
        } else if (email.getNtfEvent().isEventServiceName(NtfEventTypes.AUTO_UNLOCK_PIN_L2.getName())) {
            log.debug("handleSingleRecipient:handling AutoUnlockPin (level-2) for  " + email.getReceiver());
            if(AutoUnlockPinUtil.isUnlockNeeded(email.getNtfEvent())) {
                AutoUnlockPin.handleAutoUnlockPinUnlock(email, user);
            } else {
                deliveryCount = handleAutoUnlockPinSms(email, user, ng);
                ng.setOutCount(user, deliveryCount);
            }

        } else if (email.getNtfEvent().isFallback()) {
            // Send only the fallback notification type (and not all notification types in the user's filter).
            FallbackUtil.doLevelTwoRetryScheduledFallback(user, ng);
        } else if (email.getNtfEvent().isEventType(NtfEventTypes.EVENT_TYPE_NOTIF.getName())
                && email.getNtfEvent().isEventServiceType(NtfEventTypes.SUBSCRIBER_VVM_SYSTEM_DEACTIVATED.getName())) {
            // Case subscriber has been deactivated by Provisioning.
            log.debug("handleSingleRecipient:handling: Case subscriber has been deactivated by Provisioning");
            deliveryCount = sendVVMSystemDeactivatedWarningMessage(user, ng, email);
            ng.setOutCount(user, deliveryCount);
        } else if (email.getNtfEvent().isEventType(NtfEventTypes.EVENT_TYPE_NOTIF.getName())
                && email.getNtfEvent().isEventServiceType(NtfEventTypes.SUBSCRIBER_VVM_SYSTEM_ACTIVITY_DETECTED.getName())) {
            // Case subscriber has activity detected.
            log.debug("handleSingleRecipient:handling: Case subscriber has activity detected");
            deliveryCount = handleVvmActivityDetected(user, ng, inbox);
            ng.setOutCount(user, deliveryCount);
        } else if (email.getNtfEvent().isEventType(NtfEventTypes.EVENT_TYPE_NOTIF.getName())
                && email.getNtfEvent().isEventServiceType(NtfEventTypes.SUBSCRIBER_VVM_IMAP_FIRST_DETECTED.getName())) {
            // Case subscriber has IMAP activity detected from the Gateway.
            log.debug("handleSingleRecipient:handling: Case subscriber has IMAP activity detected from Gateway");
            deliveryCount = sendVVMImapFirstDetectedNotification(user, ng, email);
            ng.setOutCount(user, deliveryCount);
        } else if(email.getNtfEvent().isReminder()){
            if(email.getNtfEvent().isEventServiceName(NtfEventTypes.SMS_REMINDER.getName())){
                log.debug("handleSingleRecipient: triggering SMS reminder notification for " + email.getReceiver());
                SmsReminder.triggerReminderNotification(user, ng);
            }
            else{
                log.debug("handleSingleRecipient: sending SMS reminder notification retry for " + email.getReceiver());
                SmsReminder.sendReminderNotificationRetry(user, ng);
            }
        } else if (email.isRoamSms()) {
            GregorianCalendar receivedDate = new GregorianCalendar();
            NotificationFilter filter = user.getFilter();
            deliveryCount = handleSmsOut(user, filter.getSmsFilterInfo(email, receivedDate, ng), ng, email, inbox,
                    getSourceAddressEmail(email, user.getCosName()),
                    user.getNotifExpTime(), delay);
            ng.setOutCount(user, deliveryCount);
            if ( deliveryCount > 0 ) {
                log.debug("Sending roaming sms for Subscriber " + email.getReceiver());
            }
        }
        else if(email.getNtfEvent().isEventServiceType(NtfEventTypes.DELAYED_EVENT.getName())) {
            log.debug("handleSingleRecipient:handling delayedevent for  " + email.getReceiver());
            deliveryCount = handleDelayedEvent(user, ng);
            ng.setOutCount(user, deliveryCount);
        }
        else {

            if (!user.hasDeposiType(depType)) {
                // The user does not have this mail type, and should not be notified about it. It is unusual but not an error.
                log.debug("User " + user.getMail() + " does not have " + depType + ", stopping notification");
                ng.setOutCount(user, 0);
                return;
            }

            // perform mailbox inventory if quota check is needed
            if (Config.isCheckQuota()) {
                UserMailbox mailbox = email.getUserMailbox();

                int quotaWrnLvl = user.getQuotaWarnLvl();
                if (!user.isQuotaPerType()) {
                    int totalcount = mailbox.getAllTotalCount();
                    int noOfMailQuota = user.getNoOfMailQuota();
                    log.debug("Quota totalcount: " + totalcount + " noOfMailQuota: " + noOfMailQuota + " quotaWrnLvl: "
                            + quotaWrnLvl);
                    // Store quotaExceeded to minimize the I/Os (this value will be re-use during template generation phase)
                    email.setQuotaExceeded(isQuotaExceed(noOfMailQuota, totalcount));
                    email.setQuotaAlmostExceeded(isQuotaWarningLevelExceed(noOfMailQuota, totalcount, quotaWrnLvl));
                } else {

                    if (user.hasDeposiType(depositType.VOICE)) {
                        int totalcount = mailbox.getVoiceTotalCount();
                        int noOfMailQuota = user.getNoOfVoiceMailQuota();
                        log.debug("Quota VOICE totalcount: " + totalcount + " noOfMailQuota: " + noOfMailQuota + " quotaWrnLvl: "
                                + quotaWrnLvl);
                        // Store quotaExceeded to minimize the I/Os (this value will be re-use during template generation phase)
                        email.setVoiceQuotaExceeded(isQuotaExceed(noOfMailQuota, totalcount));
                        email.setVoiceQuotaAlmostExceeded(isQuotaWarningLevelExceed(noOfMailQuota, totalcount, quotaWrnLvl));

                    }
                    if (user.hasDeposiType(depositType.VIDEO)) {
                        int totalcount = mailbox.getVideoTotalCount();
                        int noOfMailQuota = user.getNoOfVideoMailQuota();
                        log.debug("Quota VIDEO totalcount: " + totalcount + " noOfMailQuota: " + noOfMailQuota + " quotaWrnLvl: "
                                + quotaWrnLvl);
                        ;
                        // Store quotaExceeded to minimize the I/Os (this value will be re-use during template generation phase)
                        email.setVideoQuotaExceeded(isQuotaExceed(noOfMailQuota, totalcount));
                        email.setVideoQuotaAlmostExceeded(isQuotaWarningLevelExceed(noOfMailQuota, totalcount, quotaWrnLvl));

                    }
                    if (user.hasDeposiType(depositType.FAX)) {
                        int totalcount = mailbox.getFaxTotalCount();
                        int noOfMailQuota = user.getNoOfFaxMailQuota();
                        log.debug("Quota FAX totalcount: " + totalcount + " noOfMailQuota: " + noOfMailQuota + " quotaWrnLvl: "
                                + quotaWrnLvl);
                        ;
                        // Store quotaExceeded to minimize the I/Os (this value will be re-use during template generation phase)
                        email.setFaxQuotaExceeded(isQuotaExceed(noOfMailQuota, totalcount));
                        email.setFaxQuotaAlmostExceeded(isQuotaWarningLevelExceed(noOfMailQuota, totalcount, quotaWrnLvl));
                    }
                }

            }

            /*
             * Quota validation cases. Config.isDiscardWhenQuota() Config.isWarnWhenQuota() 1) NotifyAndWarn: Notification & quota
             * warning 0 1 2) Discard: No notification & no quota warning 1 0 3) Notify: Notification & no quota warning 0 0 4)
             * warn: No notification & quota warning 1 1
             */

            if (Config.isCheckQuota() && (Config.isDiscardWhenQuota() || Config.isWarnWhenQuota())) {
                String receiver = email.getNtfEvent().getRecipient();

                if (email.isQuotaExceededForMsgType() || email.isQuotaAlmostExceededForMsgType()) {

                    Object perf = null;
                    try {
                        if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                            perf = CommonOamManager.profilerAgent.enterCheckpoint("NTF.NH.4.Quota");
                        }

                        // Send a SMS Quota notification even if the subscriber is not profiled to.
                        if (Config.isWarnWhenQuota()) {
                            log.debug("Quota exceeded for subscriber " + receiver);
                            int count = 0;
                            if (email.isQuotaExceededForMsgType()) {
                                count = handleQuotaExceeded(user, ng, inbox, email);
                            } else if (email.isQuotaAlmostExceededForMsgType()) {
                                count = handleQuotaAlmostExceeded(user, ng, inbox, email);
                            }
                            if (count > 0) {
                                numberOfNotificationQuota.incrementAndGet();
                            }
                            logText.append("1 Quota-SMS ");
                            deliveryCount += count;
                        } else {
                            log.debug("Quota: Subscriber " + receiver
                                    + " will not be informed of this quota exceeded (Config.isWarnWhenQuota() is false)");
                        }

                        // If the real notification must be discard, we stop the process here and return
                        if ((Config.isDiscardWhenQuota() && email.isQuotaExceededForMsgType()) || Config.isWarnOnlyWhenQuota()) {
                            log.debug("Quota: Subscriber " + receiver
                                    + " will not receive the notification (Config.isDiscardWhenQuota() is true)");
                            ng.setOutCount(user, deliveryCount);
                            return;
                        }

                    } finally {
                        if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                            CommonOamManager.profilerAgent.exitCheckpoint(perf);
                        }
                    }
                }
            }

            sendToOutInterfaces(deliveryCount, user, ng, inbox, delay, logText);
        }

    }

    private int handleDelayedEvent(UserInfo user, NotificationGroup ng) {
        DesignSequenceDiagram.printFullSequence();
        int count = 0;
        count += delayedeventHandler.handleDelayedEvent(user, ng);
        if (count > 0) {
            eventHandler.profilerAgentCheckPoint("NTF.NH.17.DelayedEvent.Received");
            numberOfNotificationDelayedEvent.incrementAndGet();
        }
        return count;
    }

    private int handleFaxPrintReceipt(UserInfo user, NotificationGroup ng) {
        DesignSequenceDiagram.printFullSequence();
        String[] smsNumbers = user.getFilter().getNotifNumbers("SMS", 0, ng.getEmail());
        Properties props = new Properties();
        String content = ng.getEmail().getNtfEvent().getEventProperties().getProperty("contenttype");

        log.debug("handleFaxPrintReceipt  smsNumbers: " + smsNumbers + " content: " + content);

        int count = 0;
        if (content != null) {
            props.setProperty("SMS", content);
            SmsFilterInfo filterInfo = new SmsFilterInfo(props, smsNumbers, null, null);
            SMSAddress source = NotificationHandler.getSourceAddressEmail(ng.getEmail(), user.getCosName());
            try {
                count = SMSOut.get().handleSendSMS(user, filterInfo, ng, ng.getEmail(), ng.getEmail().getUserMailbox(), source,
                        user.getNotifExpTime(), 0, Constants.NTF_FAX_RECEIPT_SMS);
            } catch (TemplateMessageGenerationException e) {
                log.error("TemplateMessageGenerationException received in handleFaxPrintReceipt.");
            }
        }
        log.debug("handleFaxPrintReceipt  count: " + count);

        return count;
    }

    private boolean isQuotaExceed(int mailQuota, int mailboxSize) {
        DesignSequenceDiagram.printFullSequence();
        if (mailQuota != -1 && mailboxSize > 0 && mailboxSize >= mailQuota) {
            return true;
        }
        return false;
    }

    private boolean isQuotaWarningLevelExceed(int mailQuota, int mailboxSize, int quotalevel) {
        DesignSequenceDiagram.printFullSequence();
        if (mailQuota != -1 && mailboxSize > 0 && quotalevel != -1) {
            int noOFMessagesAtwarningLevel = (mailQuota * quotalevel) / 100 + ((mailQuota * quotalevel) % 100 > 0 ? 1 : 0);
            if (noOFMessagesAtwarningLevel > 0 && (mailboxSize >= noOFMessagesAtwarningLevel))
                return true;
        }
        return false;

    }

    /**
     * handleVvaSmsOut looks if there should be an VVA based SMS notification and sends it to the SMS interface.
     *
     * @TODO Look into whether we should support Roaming Filter for VVASMS feature.
     * @param user
     *        the user information.
     * @param ng
     *        collects results for all notifications from this mail.
     * @return no of notifications sent
     */
    private int handleVvaSmsOut(UserInfo user, NotificationGroup ng) {
        DesignSequenceDiagram.printFullSequence();

        NotificationEmail email = ng.getEmail();
        NtfEvent event = email.getNtfEvent();

        //Jeffrey XIE, 2015-04-27, fix TR HT64948: VVASMS notification to wrong number
        //This is a system message, so it should be sent regardless of the settings for MOIPuserNTD and filters in user profile
        String[] smsNumbers = user.getFilter().getMatchingDeliveryProfileNumbers("SMS", 0, false);
        if(smsNumbers == null){
            String notifNumber = user.getNotifNumber();
            if(notifNumber != null && !notifNumber.isEmpty()){
                smsNumbers = new String[]{notifNumber};
            }
            else{
                smsNumbers = new String[]{email.getReceiverPhoneNumber()};
            }
        }

        Properties filterProp = new Properties();

        filterProp.put("SMS", getVvaSmsContentString(event, event.getEventServiceTypeKey()));

        SmsFilterInfo filterInfo = new SmsFilterInfo(filterProp, smsNumbers, null);
        SMSAddress src = getSourceAddressEmail(email, user.getCosName());

        int deliveryCount = 0;
        try {
            deliveryCount += smsOut.handleSendSMS(user, filterInfo, ng, email, email.getUserMailbox(), src, user.getNotifExpTime(), DEFAULT_DELAY, NTF_SMS);

        } catch (TemplateMessageGenerationException e) {
            log.error("TemplateMessageGenerationException received in handleVvaSmsOut.");
        }
        if (deliveryCount > 0) {
            eventHandler.profilerAgentCheckPoint("NTF.NH.1.Sms");
            numberOfNotificationSMS.addAndGet(deliveryCount);
        }

        return deliveryCount;
    }

    private String getVvaSmsContentString(NtfEvent event, String defaultValue) {
        DesignSequenceDiagram.printFullSequence();
        String content = defaultValue;
        Properties props = event.getEventProperties();

        if (props.keySet() != null) {
            Iterator<Object> iter = props.keySet().iterator();
            if (log.isDebugEnabled()) {
                log.debug("getVvaSmsContentString() Getting the property set");
            }

            boolean found = false;
            while (iter.hasNext() && !found) {
                // Object oval = super.get(key);
                // String sval = (oval instanceof String) ? (String)oval : null;

                String key = (String) iter.next();

                if (key != null) {
                    String value = props.getProperty(key);
                    if (log.isDebugEnabled()) {
                        log.debug("Property :" + key + " is :" + value);
                    }

                    if (key.contains("ntf_content")) {
                        found = true;
                        content = value;
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("getVvaSmsContentString() - returning content tag [" + content + "]");
        }
        return content;
    }

    /**
     * Handle AutoUnlockPin SMS notification
     *
     * @param email
     *        the NotificationEmail for this event
     * @param user
     *        the user information.
     * @param ng
     *        collects results for all notifications from this mail.
     * @return no of notifications sent
     */
    private int handleAutoUnlockPinSms(NotificationEmail email, UserInfo user, NotificationGroup ng) {

        DesignSequenceDiagram.printFullSequence();

        int validity = Config.getValidity_autoUnlockPin();


        String[] numbers = user.getFilter().getNotifNumbers("SMS", TRANSPORT_MOBILE, email);
        if (numbers == null || numbers.length == 0) {
            return 0;
        }
        SMSAddress smsAddressFrom = Config.getSourceAddress(NotificationConfigConstants.AUTO_UNLOCK_PIN, user.getCosName());

        Properties props = new Properties();
        props.setProperty("SMS", "autounlockpinsms"); // set SMS template for Auto Unlock Pin
        SmsFilterInfo filterInfo = new SmsFilterInfo(props, numbers, null, null);

        int count = 0;

        try {
            count = SMSOut.get().handleSendSMS(user, filterInfo, ng, email, email.getUserMailbox(), smsAddressFrom, validity, 0, NTF_SMS);
        } catch (TemplateMessageGenerationException e) {
            log.error("TemplateMessageGenerationException received in handleAutoUnlockPinSms.");
        }

        return count;
    }
    
    
    /**
     * handleSmsOut looks if there should be an SMS notification and sends it to the SMS interface.
     *
     * @param user
     *        the user information.
     * @param info
     *        SMS-specific information about this notification from the users filter. Null if there should be no SMS notification.
     * @param ng
     *        collects results for all notifications from this mail.
     * @param inbox
     *        info from the users mailbox.
     * @return no of notifications sent
     */
    private int handleSmsOut(UserInfo user, SmsFilterInfo info, FeedbackHandler ng, NotificationEmail email, UserMailbox inbox,
            SMSAddress source, int validity, int delay) {
        DesignSequenceDiagram.printFullSequence();
        if (info == null) {
            return 0;
        }

        int count = 0;
        count += smsOut.handleSMS(user, info, ng, email, inbox, source, validity, delay);

        if (count > 0) {
            eventHandler.profilerAgentCheckPoint("NTF.NH.1.Sms");
            numberOfNotificationSMS.addAndGet(count);
        }

        return count;
    }

    /**
     * Handle Slamdown notifications
     *
     * @return count
     */
    private int handleSlamdown(UserInfo user, NotificationGroup ng) {
        DesignSequenceDiagram.printFullSequence();
        int count = 0;
        NtfEvent event = ng.getEmail().getNtfEvent();
        log.debug("handleSlamdown event: " + event.getEventProperties().toString());
        log.debug("handleSlamdown recipient: " + event.getRecipient());
        count += slamdownHandler.handleSlamdown(user, ng, SlamdownList.NOTIFICATION_TYPE_SLAMDOWN);
        if (count > 0) {
            eventHandler.profilerAgentCheckPoint("NTF.NH.2.Slamdown.Received");
            numberOfNotificationSlamdown.addAndGet(count);
        }
        return count;
    }

    /**
     * Handle MCN notifications
     *
     * @return count
     */
    private int handleMcn(UserInfo user, NotificationGroup ng) {
        DesignSequenceDiagram.printFullSequence();
        int count = 0;
        int notificationType=SlamdownList.NOTIFICATION_TYPE_MCN_EXTERNAL ;
        NtfEvent event = ng.getEmail().getNtfEvent();
        log.debug("handleMcn event: " + event.getEventProperties().toString());
        log.debug("handleMcn recipient: " + event.getRecipient());
        if(mfsEventManager.isInternal(ng.getEmail().getNtfEvent().getRecipient())) {
            notificationType=SlamdownList.NOTIFICATION_TYPE_MCN_INTERNAL;
        }
        count += slamdownHandler.handleSlamdown(user, ng, notificationType);
        if (count > 0) {
            eventHandler.profilerAgentCheckPoint("NTF.NH.3.Mcn.Received");
            numberOfNotificationMcn.addAndGet(count);;
        }
        return count;
    }

    /**
     * Handle MCN notifications
     *
     * @return count
     */
    private int handleFaxPrint(UserInfo user, NotificationGroup ng) {
        DesignSequenceDiagram.printFullSequence();
        int count = 0;
        count += faxPrintOut.handleFaxPrint(user, ng);
        if (count > 0) {
            eventHandler.profilerAgentCheckPoint("NTF.NH.16.FaxPrint");
            numberOfFaxPrint.addAndGet(count);;
        }
        ng.ok(user, NTF_FAX_PRINT_NOTIF, false);
        return count;
    }

    /**
     * Handle VVM Deposit notifications
     *
     * @param otherNotificationsWereSent
     *        true if any other notifications have been sent for this event
     * @return Number of notifications sent out
     */
    private int handleVvmDepositOut(UserInfo user, NotificationGroup ng, UserMailbox inbox, boolean otherNotificationsWereSent) {
    	DesignSequenceDiagram.printFullSequence();
    	int count = 0;

    	// Check if the sub has VVMActivated on and VVM service in his CoS, return if not.
    	if (!(user.isVVMActivated() && user.hasVvmService())) {
    		log.debug("NotificationHandler.handleVvmDepositOut() - VVM-Deposit notification will not be sent, Subscriber has not VVM activated.");
    		return count;
    	}

    	if (ng.getEmail().getDepositType()== depositType.VOICE) {
    		//Only checking if the system is activated here, the roaming will be check inside the worker thread
    		if (user.isVVMSystemActivated()|| !vvmHandler.isSimSwapConfigActive()) {
    			log.debug("NotificationHandler.handleVvmDepositOut() - Subscriber " + ng.getEmail().getReceiver()
    					+ " will receive a VVM-Deposit notification");

    			boolean hasApple = isAppleClient(user, ng.getEmail().getReceiver());

    			if (hasApple) {
    				count += vvmHandler.handleVvm(user, ng, inbox, VvmEventTypes.APPLEVVM_DEPOSIT, otherNotificationsWereSent);

    				if (count > 0) {
    					appleServiceExist = true;
    					eventHandler.profilerAgentCheckPoint("NTF.NH.10.AppleVvmDeposit");
    					numberOfNotificationAppleVvmDeposit.incrementAndGet();
    				}
    			} else {
    				count += vvmHandler.handleVvm(user, ng, inbox, VvmEventTypes.VVM_DEPOSIT, otherNotificationsWereSent);

    				if (count > 0) {
    					eventHandler.profilerAgentCheckPoint("NTF.NH.10.VvmDeposit");
                        numberOfNotificationVvmDeposit.incrementAndGet();
    				}
    			}

            } else {
                log.debug("NotificationHandler.handleVvmDepositOut() - VVM-Deposit notification will not be sent because VVM notifications are not allowed");

    			if (!otherNotificationsWereSent && user.isVVMActivated() && !user.isVVMSystemActivated()) {
    				log.debug("NotificationHandler.handleVvmDepositOut() - No other notification were sent ");
    				//FallbackUtil.doLevelTwoScheduledFallback(user, ng); replace with proper fallback..
    				FallbackHandler.get().fallback(Constants.NTF_VVM, ng.getEmail().getNtfEvent());
    			}
    		}
    	} else {
    		log.debug("NotificationHandler.handleVvmDepositOut() - VVM-Deposit notification will not be sent because message was not of type voice");
    	}
    	return count;
            }


    /**
     * Send an SMS notification to the subscriber indicating that his VVM has been successfully
     * activated. Also send a Cai3g update to set MOIPVvmFirstTimeActivated. 
     *
     * @return int number of requests recorded with the SMS-c
     */
    private int sendVVMImapFirstDetectedNotification(UserInfo user, NotificationGroup ng, NotificationEmail email) {
        DesignSequenceDiagram.printFullSequence();

        int count = 0;
        if (Config.getSendVVMImapFirstDetectedSMS()) {
            int delay = 1;
            String template = Config.getVVMImapFirstDetectedTemplate();

            String[] numbers = user.getFilter().getNotifNumbers("SMS", TRANSPORT_MOBILE, email);

            if (numbers == null || numbers.length == 0) {
                return 0;
            }

            SMSAddress smsSourceAddress = Config.getSourceAddress(NotificationConfigConstants.VVM, user.getCosName());
            // Use the same validity period as for the VVM System Deactivated message. It's all the same family of messages.
            int validityPeriod = user.getValidity_vvmSystemDeactivated();

            try {
                count += smsOut.handleSendSMS(user, new SystemNotificationFilterInfo(template, numbers), ng, email, email.getUserMailbox(), smsSourceAddress, validityPeriod, delay, NTF_SMS);
                vvmHandler.handleVvmImapFirstDetected(user);
            } catch (TemplateMessageGenerationException e) {
                log.error("TemplateMessageGenerationException received in sendVVMImapFirstDetectedNotification.");
            }
        }
        return count;
    }

    
    /**
     * Send an SMS warning message to the subscriber, stating that his VVM service has been deactivated by the system.
     *
     * @return int number of requests recorded with the SMS-c
     */
    private int sendVVMSystemDeactivatedWarningMessage(UserInfo user, NotificationGroup ng, NotificationEmail email) {
        DesignSequenceDiagram.printFullSequence();

        int count = 0;
        //CR of MRD 105, add a configuration parameter to send or not that SMS
        if(Config.getSendVVMSystemDeactivatedSMS()){
            int delay = 1;
            String template = Config.getVVMSystemDeactivatedTemplate();

            String[] numbers = user.getFilter().getNotifNumbers("SMS", TRANSPORT_MOBILE, email);

            if (numbers == null || numbers.length == 0) {
                return 0;
            }

            SMSAddress smsSourceAddress = Config.getSourceAddress(NotificationConfigConstants.VVM, user.getCosName());
            int validityPeriod = user.getValidity_vvmSystemDeactivated();

            try {
                count += smsOut.handleSendSMS(user, new SystemNotificationFilterInfo(template, numbers), ng, email, email.getUserMailbox(),
                        smsSourceAddress, validityPeriod, delay, NTF_SMS);
            } catch (TemplateMessageGenerationException e) {
                log.error("TemplateMessageGenerationException received in sendVVMSystemDeactivatedWarningMessage.");
            }
        }
        return count;
    }
    
    
    /**
     * Look if the client is an Apple client or another type if client.
     */
    private boolean isAppleClient(UserInfo user, String subscriber) {
        DesignSequenceDiagram.printFullSequence();
        String clientTypes = user.getVvmClientType().toLowerCase();
        if (clientTypes.equalsIgnoreCase(ProvisioningConstants.APPLEVVM_CLIENTTYPE.toLowerCase())) {
            log.debug("isAppleClient: Subscriber " + subscriber + " has a MOIPVvmClientType that indicates an Apple client");
            return true;
        }
        log.debug("isAppleClient: Subscriber " + subscriber + " has a MOIPVvmClientType that indicates an OMTP client");
        return false;
    }

    /**
     * Handle VVM Greeting notifications
     *
     * @return count
     */
    private int handleVvmGreeting(UserInfo user, NotificationGroup ng, UserMailbox inbox) {
        DesignSequenceDiagram.printFullSequence();
        int count = 0;

        // Check if the sub has VVMActivated on and VVM service in his CoS, return if not.
        if (!(user.isVVMActivated() && user.hasVvmService())) {
            log.debug("NotificationHandler.handleVvmGreeting() - VVM-Greeting notification will not be sent, Subscriber has not VVM activated.");
            return count;
        }

        if (user.isVVMSystemActivated() || !vvmHandler.isSimSwapConfigActive()) {
            log.debug("handleVvmGreeting: Subscriber " + ng.getEmail().getReceiver()
                    + " will receive a VVM-Greeting modification notification");
            boolean hasApple = isAppleClient(user, ng.getEmail().getReceiver());
            if (!hasApple) {
                count += vvmHandler.handleVvm(user, ng, inbox, VvmEventTypes.VVM_GREETING, true);

                if (count > 0) {
                    eventHandler.profilerAgentCheckPoint("NTF.NH.11.VvmGreeting");
                    numberOfNotificationVvmGreeting.incrementAndGet();
                }
            } else {
                count += vvmHandler.handleVvm(user, ng, inbox, VvmEventTypes.APPLEVVM_GREETING, true);

                if (count > 0) {
                    appleServiceExist = true;
                    eventHandler.profilerAgentCheckPoint("NTF.NH.11.AppleVvmGreeting");
                    numberOfNotificationAppleVvmGreeting.incrementAndGet();
                }
            }
        }
        return count;
    }

    /**
     * Handle VVM Expiry notifications
     *
     * @return count
     */
    private int handleVvmExpiry(UserInfo user, NotificationGroup ng, UserMailbox inbox) {
        DesignSequenceDiagram.printFullSequence();
        int count = 0;

        // Check if the sub has VVMActivated on and VVM service in his CoS, return if not.
        if (!(user.isVVMActivated() && user.hasVvmService())) {
            log.debug("NotificationHandler.handleVvmExpiry() - VVM-Expiry notification will not be sent, Subscriber has not VVM activated.");
            return count;
        }

        if (user.isVVMSystemActivated()|| !vvmHandler.isSimSwapConfigActive()) {
            log.debug("handleVvmExpiry: Subscriber " + ng.getEmail().getReceiver()
                    + " will receive a VVM-Expiry modification notification");
            boolean hasApple = isAppleClient(user, ng.getEmail().getReceiver());
            if (!hasApple) {
                count += vvmHandler.handleVvm(user, ng, inbox, VvmEventTypes.VVM_EXPIRY, true);

                if (count > 0) {
                    eventHandler.profilerAgentCheckPoint("NTF.NH.12.VvmExpiry");
                    numberOfNotificationVvmExpiry.incrementAndGet();
                }
            } else {
                count += vvmHandler.handleVvm(user, ng, inbox, VvmEventTypes.APPLEVVM_EXPIRY, true);

                if (count > 0) {
                    appleServiceExist = true;
                    eventHandler.profilerAgentCheckPoint("NTF.NH.12.AppleVvmExpiry");
                    numberOfNotificationAppleVvmExpiry.incrementAndGet();
                }
            }
        }
        return count;
    }

    /**
     * Handle VVM Logout notifications
     *
     * @return count
     */
    private int handleVvmLogout(UserInfo user, NotificationGroup ng, UserMailbox inbox) {
        DesignSequenceDiagram.printFullSequence();
        int count = 0;

        // Check if the sub has VVMActivated on and VVM service in his CoS, return if not.
        if (!(user.isVVMActivated() && user.hasVvmService())) {
            log.debug("NotificationHandler.handleVvmLogout() - VVM-Logout notification will not be sent, Subscriber has not VVM activated.");
            return count;
        }

        if (user.isVVMSystemActivated()|| !vvmHandler.isSimSwapConfigActive()) {
            log.debug("handleVvmLogout: Subscriber " + ng.getEmail().getReceiver()
                    + " will receive a VVM-Logout modification notification");
            boolean hasApple = isAppleClient(user, ng.getEmail().getReceiver());
            // Only send the event if the client is an OMTP client.
            if (!hasApple) {
                count += vvmHandler.handleVvm(user, ng, inbox, VvmEventTypes.VVM_LOGOUT, true);

                if (count > 0) {
                    eventHandler.profilerAgentCheckPoint("NTF.NH.13.VvmLogout");
                    numberOfNotificationVvmLogout.incrementAndGet();
                }
            } else {
                count += vvmHandler.handleVvm(user, ng, inbox, VvmEventTypes.APPLEVVM_LOGOUT, true);

                if (count > 0) {
                    appleServiceExist = true;
                    eventHandler.profilerAgentCheckPoint("NTF.NH.13.AppleVvmLogout");
                    numberOfNotificationAppleVvmLogout.incrementAndGet();
                }
            }
        }

        return count;
    }

    /**
     * Handle VVM Activity Detected events
     *
     * @return count
     */
    private int handleVvmActivityDetected(UserInfo user, NotificationGroup ng, UserMailbox inbox) {
        DesignSequenceDiagram.printFullSequence();
        int count = 0;

        // Check if the sub has VVMActivated on and VVM service in his CoS, return if not.
        if (!(user.isVVMActivated() && user.hasVvmService())) {
            log.debug("NotificationHandler.handleVvmActivityDetected() - vvmActivityDetected: VVM-Activity Detected will not be processed, Subscriber has not VVM activated.");
            return count;
        }

        log.debug("handleVvmActivityDetected: Subscriber " + ng.getEmail().getReceiver()
                + " will have any pending SimSwap system de-activation canceled");

        count += vvmHandler.handleVvmActivityDetected(user, ng, inbox);

        return count;
    }

    /**
     * handleEmailOut looks if there should be an Email notification and sends it to the Email interface.
     *
     * @param user
     *        the user information.
     * @param info
     *        Email-specific information about this notification from the users filter. Null if there should be no Email
     *        notification.
     * @param ng
     *        collects results for all notifications from this mail.
     * @param inbox
     *        info from the users mailbox.
     * @return no of notifications sent
     */
    private int handleEmailOut(UserInfo user, EmailFilterInfo info, NotificationGroup ng, NotificationEmail email,
            UserMailbox inbox, int validity) throws InterruptedException{
        DesignSequenceDiagram.printFullSequence();
        if (info == null || email == null) {
            return 0;
        }
        if(info.isFwdMsg() && !user.hasForwardToEmailService())
        {
            return 0;
        }
        int count = 0;
        try {
            count += emailOut.handleEmail(user, info, ng, email, inbox, validity);
        } catch (TemplateMessageGenerationException e) {
            log.error("TemplateMessageGenerationException received in handleEmailOut.");
        }
        if (count > 0) {
            eventHandler.profilerAgentCheckPoint("NTF.NH.9.Email");
            numberOfNotificationEmail.incrementAndGet();
        }
        return count;
    }

    /**
     * @param user
     *        the user information.
     * @param info
     *        Outdial-specific information about this notification from the users filter. Null if there should be no outdial
     *        notification.
     * @param ng
     *        collects results for all notifications from this mail.
     * @param inbox
     *        info from the users inbox.
     * @return no of notifications sent
     */
    private int handleOdlOut(UserInfo user, OdlFilterInfo info, NotificationGroup ng, NotificationEmail email, UserMailbox inbox) {
        DesignSequenceDiagram.printFullSequence();
        if (info == null || email == null) {
            return 0;
        }

        int count = odlOut.notify(user, info, user.getTelephoneNumber(), email.getNtfEvent(), ng);
        if (count > 0) {
            eventHandler.profilerAgentCheckPoint("NTF.NH.7.Outdial");
            numberOfNotificationOutdial.incrementAndGet();
        }

        return count;
    }

    /**
     * @param user
     *        the user information.
     * @param info
     *        Outdial-specific information about this notification from the users filter. Null if there should be no outdial
     *        notification.
     * @param ng
     *        collects results for all notifications from this mail.
     * @param inbox
     *        info from the users inbox.
     * @return no of notifications sent
     */
    private int handleSIPOut(UserInfo user, SIPFilterInfo info, NotificationGroup ng, NotificationEmail email, UserMailbox inbox) {
        DesignSequenceDiagram.printFullSequence();
        if (info == null) {
            return 0;
        }

        int count = sipOut.handleMWI(user, info, user.getTelephoneNumber(), email.getNtfEvent(), ng);
        if (count > 0) {
            eventHandler.profilerAgentCheckPoint("NTF.NH.6.SipMwi");
            numberOfNotificationSipMwi.incrementAndGet();
        }

        return count;
    }

    /**
     * @param user
     *        the user information.
     * @param info
     *        Wireline MWI-specific information about this notification from the users filter. Null if there should be no wireline
     *        MWI notification.
     * @param ng
     *        collects results for all notifications from this mail.
     * @param inbox
     *        info from the users inbox.
     * @return no of notifications sent
     */
    private int handleWmwOut(UserInfo user, WmwFilterInfo info, NotificationGroup ng, NotificationEmail email, UserMailbox inbox) {
        DesignSequenceDiagram.printFullSequence();
        if (info == null) {
            return 0;
        }
        int count = 0;

        count += wmwOut.sendNotification(user, info, ng, email, inbox);

        return count;
    }

    /**
     * @param user
     *        the user information.
     * @param info
     *        Pager-specific information about this notification from the users filter. Null if there should be no pager
     *        notification.
     * @param ng
     *        collects results for all notifications from this mail.
     * @return no of notifications sent
     */
    private int handlePagOut(UserInfo user, PagFilterInfo info, NotificationGroup ng, NotificationEmail email) {
        DesignSequenceDiagram.printFullSequence();
        if (info == null) {
            return 0;
        }

        int count = 0;

        count += pagOut.sendNotification(user, info, ng, email);

        return count;
    }

    /**
     * @param user
     *        - the user information.
     * @param info
     *        - call-MWI-specific information about this notification from the users filter. Null if there should be no call-MWI
     *        notification.
     * @param ng
     *        - collects results for all notifications from this mail.
     * @return no of notifications sent
     */
    private int handleCmwOut(UserInfo user, CmwFilterInfo info, NotificationGroup ng, NotificationEmail email) {
        DesignSequenceDiagram.printFullSequence();
        if (info == null) {
            return 0;
        }

        int count = 0;

        count += cmwOut.sendNotification(user, info, ng, email);

        return count;
    }

    /**
     * @param user
     *        the user information.
     * @param info
     *        MMS-specific information about this notification from the users filter. Null if there should be no MMS notification.
     * @param ng
     *        collects results for all notifications from this mail.
     * @return no of notifications sent
     */
    private int handleMmsOut(UserInfo user, MmsFilterInfo info, NotificationGroup ng, NotificationEmail email, UserMailbox inbox) {
        DesignSequenceDiagram.printFullSequence();
        if (info == null) {
            return 0;
        }

        int count = 0;
        count += mmsOut.sendNotification(user, inbox, info, ng, email);
        if (count > 0) {
            eventHandler.profilerAgentCheckPoint("NTF.NH.8.MMS");
            numberOfNotificationMMS.incrementAndGet();
        }
        return count;
    }

    /**
     * @param user
     *        the user information.
     * @param info
     *        Wap-push-specific information about this notification from the users filter. Null if there should be no Wap-push
     *        notification.
     * @param ng
     *        collects results for all notifications from this mail.
     * @return no of notifications sent
     */
    private int handleWapOut(UserInfo user, WapFilterInfo info, NotificationGroup ng, NotificationEmail email, UserMailbox inbox) {
        DesignSequenceDiagram.printFullSequence();
        if (info == null) {
            return 0;
        }

        int count = 0;

        count += wapOut.sendNotification(user, info, ng, email, inbox);

        return count;
    }

    /**
     * Handles actions that shall be done when the subscriber mailbox has been updated. (This should be triggered when subscriber
     * has modififed the mailbox content during retrieval session)
     *
     * @param email
     *        the mail with the MWI disable event details.
     */
    private void handleMailboxUpdate(NotificationEmail email) {
        DesignSequenceDiagram.printFullSequence();
        NotificationGroup ng = new NotificationGroup(eventHandler, email, log, mer);
        String to = email.getMailboxUpdateSubscriberUID();

        if (to != null) {
            UserInfo user = UserFactory.findUserByTelephoneNumber(to);
            if (user != null) {
                UserMailbox mailbox = email.getUserMailbox();
                
                //cancel at mailbox update if enabled.
                if (CancelSmsHandler.isCancelEnabledForMailBoxUpdate()) {
                    CancelSms.handleSendCancelSms(user, email, mailbox, ng);
                }
                                                   
                // Here include business logic
                handleSIPNotify(user, ng, email);

                if (!email.isForcedUpdate()) {
                    if (user.hasSpecialSMSMessageIndicationService()) {
                        // Received a mailbox update
                        GregorianCalendar receivedDate = new GregorianCalendar();
                        NotificationFilter filter = user.getFilter();
                        int deliveryCount = handleSmsOut(user, filter.getSmsFilterInfo(email, receivedDate, ng), ng, email,
                                mailbox, getSourceAddressEmail(email, user.getCosName()),
                                (email.isSlamdown() ? user.getValidity_slamdown() : user.getNotifExpTime()), 0);

                        ng.setOutCount(user, deliveryCount);
                    }

                    // Call the VVM Logout to send the MailBoxUpdate.
                    // might need to change this name eventually
                    // Do NOT send VVM Logout if MailBoxUpdate coming from GW ( isVvmNotify == false)
                    if(email.isVvmNotify()){
                        log.debug("Mailbox Update received. Try to notify VVM.");
                        int deliveryCount = handleVvmLogout(user, ng, mailbox);
                        ng.setOutCount(user, deliveryCount);
                    } else {
                        log.debug("Skip vvm notification since mailboxupdate comes from GW.");
                    }
                }
            } else {
                log.debug("Could not find user " + to);
            }
        } else {
            log.debug("Mailbox Update without receiver, skipping it");
        }
        ng.noMoreUsers();
    }

    /**
     * Send SIP Notify for mailbox updates
     */   
    private void handleSIPNotify(UserInfo user, NotificationGroup ng, NotificationEmail email) {
        DesignSequenceDiagram.printFullSequence();
        log.debug("handleSIPNotify for " + user.getMail());
        // ng.noMerEvent(); TODO check if it's ok to send MDR
        ng.addUser(user);
        int count = 0;

        if (email.isForcedUpdate()) {
            String[] numbers = { email.getUserAgentNumber() };
            SIPFilterInfo info = new SIPFilterInfo(numbers);
            count = sipOut.handleMWI(user, info, email.getReceiver(), email.getNtfEvent(), ng);
        } else if (user.isMwiUser() && user.getFilter().hasNotifType("MWI") && user.getFilter().isNotifTypeDisabledOnUser(NTF_MWI,email.getReceiver()) == NotifState.ENABLED) {
            String[] numbers = user.getFilter().getMatchingDeliveryProfileNumbers("MWI", TRANSPORT_IP);
            if (numbers != null && numbers.length > 0) {
                if ( email.isMwiOffUnsubscribed()) {
                    //we need to check if the numbers are the one sent in the unsubscribe, i.e. if only a particular number was removed.
                    String[] mwiUnsubscribedNumbers = email.getNtfEvent().getMultilineProperty(MoipMessageEntities.SERVICE_TYPE_MWI_OFF_UNSUBSCRIBED_NUMBERS);
                    if (mwiUnsubscribedNumbers != null && mwiUnsubscribedNumbers.length > 0) {
                       ArrayList<String> tmpNums = new ArrayList<String>(mwiUnsubscribedNumbers.length);
                       for (String num:numbers) {
                           if (num != null) {
                               for (String num2:mwiUnsubscribedNumbers) {
                                   if (num.equals(num2)) {
                                       tmpNums.add(num);
                                       break;
                                   }
                               }
                           }
                       }

                       if (tmpNums.size() == 0) {
                           return;
                       }
                        numbers = tmpNums.toArray(numbers);
                    }
                }
                SIPFilterInfo info = new SIPFilterInfo(numbers);
                count = sipOut.handleMWI(user, info, email.getReceiver(), email.getNtfEvent(), ng);
            }
        }

        if (count > 0) {
            eventHandler.profilerAgentCheckPoint("NTF.NH.15.SipMwiUpdate");
            numberOfNotificationSipMwiUpdate.incrementAndGet();
        }

        ng.setOutCount(user, count);
    }


    /**
     * Handles actions that shall be done when the subscriber disables MWI or MWI off
     * sent from call flow.
     * 
     * This can come from many places,  EMG, provisioning or VVA call flow.
     *
     * @param email
     *        the mail with the MWI disable event details.
     */
    private void handleMwiOff(NotificationEmail email) {
        DesignSequenceDiagram.printFullSequence();
        NotificationGroup ng = new NotificationGroup(eventHandler, email, log, mer);
        String to = email.getMWISubscriberUID();
        
        if (to != null) {
            UserInfo user = null;

            if (email.isMwiOffUnsubscribed()) {
                user = UserFactory.getUnsubscribedUser(email);
            } else {
                user = UserFactory.findUserByTelephoneNumber(to);
            }

            if (user != null) {
                UserMailbox mailbox = email.getUserMailbox();
                
                //cancel at mwi off if configured,.
                if (CancelSmsHandler.isCancelEnabledForMwiOff()) {
                    //We don't care if subscriber has MWI service, a cancel is sent for many types
                    //including mwi off. This is just the initiator of the cancel.
                    CancelSms.handleSendCancelSms(user, email, mailbox, ng);
                }
                
                if (!user.isMwiUser()) {
                    log.debug("MWI not enabled in cos, no mwi off for:" + to);
                    return;
                }
                
                boolean forced = false;
                NotificationFilter filter = user.getFilter();
                FeedbackHandlerImpl fh = new FeedbackHandlerImpl();
                SmsFilterInfo filterInfo = filter.getSmsFilterInfo(email, new GregorianCalendar(), fh); 
                if (filterInfo != null) {
                    if (fh.getStatus() != Constants.FEEDBACK_STATUS_OK) {
                        log.debug("Unable to determine if MWI has been disabled, assuming it has, forced mwi off for " + to);
                        forced=true;
                    } else {
                        forced=!filterInfo.isMwi();
                    }
                    turnSmsMwiOff(user, ng, email, forced , null, mailbox, filterInfo);
                }
                
                handleSIPNotify(user, ng, email);
                
            } else {
                log.error("Subscriber not found " + to + " for handleMwiDisabled notification");
            }
        } else {
            log.debug("MWI disabled without receiver, skipping it");
        }
        ng.noMoreUsers();
    }

    public static void turnMwiOff(UserInfo user, NotificationGroup ng, NotificationEmail email, boolean forced, String[] someNumbers,
            UserMailbox inbox) {
        
        turnSmsMwiOff(user, ng, email, forced, someNumbers, inbox, null);
    }

    /**
     * Turns MWI off at the available output interfaces
     */
    public static void turnSmsMwiOff(UserInfo user, NotificationGroup ng, NotificationEmail email, boolean forced, String[] someNumbers,
            UserMailbox inbox, SmsFilterInfo filterInfo) {
        DesignSequenceDiagram.printFullSequence();
        log.debug("turnMwiOff for " + user.getMail());
        ng.noMerEvent();

        ng.addUser(user);
        int count = 0;

        //if MWI is allowed globally in the COS, this may cause a problem if operator turned off the feature completely
        //in the COS, not just disabled or removed from filters but so far no TR's
        if (user.isMwiUser() && user.getFilter().hasNotifType("MWI")) {
            if (Config.isBearingNetworkGsm() || Config.isBearingNetworkCdma2000()) {
                try {                       
                    count += smsOut.handleMWIOff(user, ng, email, forced, someNumbers, inbox, filterInfo);
                } catch (TemplateMessageGenerationException e) {
                    log.error("TemplateMessageGenerationException received in turnMwiOff.");
                }
            }

            if (Config.isBearingNetworkPstn()) {
                count += wmwOut.handleMWIOff(user, ng, ng.getEmail());
            }
            
        }

        if (count > 0) {
            ng.setOutCount(user, count);  
            eventHandler.profilerAgentCheckPoint("NTF.NH.5.SmsMwiOff");
            numberOfNotificationMwiOff.incrementAndGet();
        }

              
    }

    /**
     * Gets sourceaddress based on mailtype.
     *
     * @param mail
     *        - the email to look up.
     * @param cosName
     *        - the cosname to get source address from (null allowed)
     * @return sourceaddress to use.
     */
    public static SMSAddress getSourceAddressEmail(NotificationEmail mail, String cosName) {
        return getSourceAddressEmail(mail.getDepositType(), null, cosName, mail.getSenderPhoneNumber(), mail.getSenderVisibile());
    }

    public static SMSAddress getSourceAddressEmail(depositType type, String genericMailType, String cosName, String sender, boolean isVisible) {
        DesignSequenceDiagram.printFullSequence();
        SMSAddress source = null;
        if(genericMailType == null) {
            source = Config.getSourceAddress(type.source());
            if (source == null) {
                source = Config.getSourceAddress(type.source(), null); //check for non COS specific.
            }
        } else {
            source = Config.getSourceAddress(genericMailType, cosName);
            if (source == null) {
                source = Config.getSourceAddress(genericMailType, null); //check for non COS specific.
            }
        }

        /* Something else, use smesourceaddress */
        if (source == null) {
            source = Config.getSmeSourceAddress();
        }

        /* Check if source is callers_number */
        if ("callers_number".equals(source.getNumber())) {
            if (sender != null) {
                sender = sender.trim();
            }
            if (sender != null && sender.length() > 0) {
                if (isVisible) {
                    source = new SMSAddress(source.getTON(), source.getNPI(), sender);
                    log.debug("sourceAddress set to: " + source + " (visible)");
                } else {
                    source = Config.getSmeSourceAddress();
                    log.debug("sourceAddress set to Config.smeSourceAddress: " + source + " (not visible)");
                }
            } else {
                log.debug("sourceAddress set to Config.smeSourceAddress: " + sender + " (sender was not found)");
                source = Config.getSmeSourceAddress();
            }
        }

        log.debug("sourceAddress set to: " + source);

        return source;
    }

    /**
     * Only used for JUnit test
     */
    public SMSAddress testGetSourceAdress(NotificationEmail mail) {
        DesignSequenceDiagram.printFullSequence();
        return getSourceAddressEmail(mail, null);
    }

    /**
     * Logs for debugging purposes
     */
    private void logCounters() {
        
        boolean acquiredLock=false;
        try {
            //NOTE we use a Reentrant lock here to prevent multiple threads from printing the log.
            //If it is already locked just return as not to hold up the thread.
            if (!reportLock.tryLock()) { 
                return;
            }
            acquiredLock=true;
            if(NtfTime.now > nextReport.get()   ) {
                nextReport.set(NtfTime.now+REPORTINTERVAL); //reset next idle report.
            } else {
                return;
            }
        }
        finally {
            if (acquiredLock) { //to prevent an exception..
                reportLock.unlock(); //make sure to unlock it.
            }
        }
        
        log.info("Ntf Counters (NH): " + "SMS&SMS-MWI: "
                + numberOfNotificationSMS.get()
                + ", Slam: "
                + numberOfNotificationSlamdown.get()
                + ", Mcn: "
                + numberOfNotificationMcn.get()
                + ", Quota: "
                + numberOfNotificationQuota.get()
                + ", SmsMwiOff: "
                + numberOfNotificationMwiOff.get()
                + ", SipMwi: "
                + numberOfNotificationSipMwi.get()
                + ", SipMwiUpdate: "
                + numberOfNotificationSipMwiUpdate.get()
                + ", Outdial: "
                + numberOfNotificationOutdial.get()
                + ", MMS: "
                + numberOfNotificationMMS.get()
                + ", VvmDeposit: "
                + numberOfNotificationVvmDeposit.get()
                + ", VvmGreeting: "
                + numberOfNotificationVvmGreeting.get()
                + ", VvmExpiry: "
                + numberOfNotificationVvmExpiry.get()
                + ", VvmLogout: "
                + numberOfNotificationVvmLogout.get()
                + (appleServiceExist ? (", AppleVvmDeposit: " + numberOfNotificationAppleVvmDeposit.get() + ", AppleVvmGreeting: "
                        + numberOfNotificationAppleVvmGreeting.get() + ", AppleVvmExpiry: " + numberOfNotificationAppleVvmExpiry.get()
                        + ", AppleVvmLogout: " + numberOfNotificationAppleVvmLogout.get()) : "")
                + ", FaxPrint: " + numberOfFaxPrint.get()
                + ", Excep: " + numberOfNotificationException.get());
    }
}
