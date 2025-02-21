/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt.fallback;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryHandling;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackEvent.FallbackNotificationTypes;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.Constants.triState;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.NotificationHandler;
import com.mobeon.ntf.NtfMain;
import com.mobeon.ntf.mail.IMessageDepositInfo;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.out.FeedbackHandlerImpl;
import com.mobeon.ntf.out.outdial.OutdialNotificationOut;
import com.mobeon.ntf.out.sip.SIPOut;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.OdlFilterInfo;
import com.mobeon.ntf.userinfo.SIPFilterInfo;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.threads.NtfThread;



/**
 * Fall back worker thread
 * 
 */
public class FallbackWorker extends NtfThread {

    private static LogAgent log = NtfCmnLogger.getLogAgent(FallbackWorker.class);
    private ManagedArrayBlockingQueue<Object> queue;
    private FallbackEventHandler fallbackEventHandler;
    private FallbackHandler fallbackHandler;

    private static int countersfFallbackToSms = 0;
    private static int countersfFallbackToOutdial = 0;
    private static int countersfFallbackToSipMwi = 0;

    /**
     * Constructor
     * @param queue Working queue where work items are found
     * @param threadName Thread name
     * @param fallbackHandler instance
     */
    public FallbackWorker(ManagedArrayBlockingQueue<Object> queue, String threadName, FallbackHandler fallbackHandler)
    {
        super(threadName);
        this.queue = queue;
        this.fallbackHandler = fallbackHandler;
        this.fallbackEventHandler = (FallbackEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.FALLBACK_L3.getName());
    }

    /**
     * Process one work item from queue.
     * @return False if the work should continue, true if the worker wants to stop.
     */
    public boolean ntfRun()
    {
        boolean exit = false;

        // Get an event from the working queue
        Object obj = queue.poll(10, TimeUnit.SECONDS);
        
        if (obj != null) { 
            if (!(obj instanceof FallbackEvent)) {
                log.error("Fallback Worker: Invalid object received: " + obj.getClass().getName());
            } else {
                FallbackEvent fallbackEvent = (FallbackEvent) obj;

                log.debug(fallbackEvent + " (" + this.getName() + ")");

                synchronized (fallbackEvent) {
                    String fallbackReferenceId = null;
                    try {
                        log.debug(fallbackEvent.toString());

                        fallbackReferenceId = fallbackEvent.getReferenceId();

                        if (fallbackEvent.getCurrentEvent() == FallbackEvent.FALLBACK_EVENT_PROCESSING) {
                            log.debug("Initiate fallback processing");

                            // Invoke the fall-back mechanism
                            if (fallback(fallbackEvent)) {
                                // Cancel fall-back event since the fall-back mode just scheduled it's own retry or failed.
                                fallbackEventHandler.cancelEvent(fallbackReferenceId);
                            }
                        } else if (fallbackEvent.getCurrentEvent() == FallbackEvent.FALLBACK_EVENT_SCHEDULER_RETRY) {

                            log.debug("Fallback retry invoked, will re-inject the event in the fallback processing: " +  fallbackEvent.toString());

                            if (fallback(fallbackEvent)) {
                                // Cancel fall-back event since the fall-back mode just scheduled it's own retry or failed.
                                fallbackEventHandler.cancelEvent(fallbackReferenceId);
                            }         
                        } else {
                            log.error("Invalid Event " + fallbackEvent.getCurrentEvent() + " received");
                            fallbackEventHandler.cancelEvent(fallbackReferenceId);
                        }


                    } catch (OutOfMemoryError me) {
                        try {
                            ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                            log.error("NTF out of memory, shutting down... ", me);
                        } catch (OutOfMemoryError me2) {;} //ignore second exception
                        return true; //exit.
                    } catch (Exception e) {
                        log.error("Exception in " + getName() + " for " + fallbackEvent + ", will retry ", e);
                    }
                }          
            }
        }
        return exit;
    }

    /**
     * Fall-back
     * @param fallbackEvent FallbackEvent
     * returns true if should cancel the retry event (Failed or OK cases).
     */
    public boolean fallback(FallbackEvent fallbackEvent) throws MsgStoreException {

        FallbackNotificationTypes fn = null;
        String subscriber = fallbackEvent.getSubscriberNumber();

        // Check if the current notification is already in fallback mode
        if (fallbackEvent.isFallback()) {
            log.debug(Constants.notifTypeStrings[fallbackEvent.getFallbackOriginalNotificationType()] + " is the fallback notification.  Only one fallback level is supported.  Subscriber " + subscriber);
            return true;
        }

        if (!Config.getFallbackWhenRoaming()) {
            if (fallbackEvent.isRoaming()) {
                log.debug(Constants.notifTypeStrings[fallbackEvent.getFallbackOriginalNotificationType()] +
                        " is for a roaming subscriber.  Fallback is not supported for a roaming subscriber.  Subscriber " +
                        subscriber);
                return true;
            }
        }

        // Check if a fall back notification is configured for the given notification
        fn =  FallbackUtil.getFallbackNotificationType(fallbackEvent.getFallbackOriginalNotificationType());
        if (fn == null || fn.equals(FallbackNotificationTypes.FALLBACK_NONE)) {
            log.debug("No fallback configured for notification " + Constants.notifTypeStrings[fallbackEvent.getFallbackOriginalNotificationType()]);
            return true;
        } else {
            log.debug(fn.getName() + " found for notification " + Constants.notifTypeStrings[fallbackEvent.getFallbackOriginalNotificationType()]);
        }

        // Check if the fallback notification type is currently implemented
        if (!fn.isImplemented()) {
            log.error("Fallback notification " + fn.getName() + " is not supported for notification " + Constants.notifTypeStrings[fallbackEvent.getFallbackOriginalNotificationType()]);
            return true;
        }

        UserInfo user =  UserFactory.findUserByTelephoneNumber(subscriber);

        if (user != null && user.getFilter() != null) {
            NotificationEmail email = new NotificationEmail(fallbackEvent);

            /**
             * Regardless the msgInfo that might have been received (could be null or not),
             * find the first unread message for the given subscriber
             */
            UserMailbox userMailbox = email.getUserMailbox(fallbackEvent.getSubscriberNumber());
            MessageInfo messageInfo = userMailbox.getFirstNewMessageInfo();
            if (messageInfo != null) {
                fallbackEvent.setMsgInfo(messageInfo);
                email.initFallback();
            } else {
                log.info("Subscriber " + subscriber + " has no new messages; will not trigger fallback to " + fn.getName());
                return true;
            }

            // No fallback invoked when subscriber explicitly desactivates all notifications
            if (user.getFilter().isNotifDisabled()) {
                log.debug("Subscriber " + subscriber + " has all notifications disabled; will not trigger fallback to " + fn.getName());
                return true;
            }


            FeedbackHandlerImpl fh = new FeedbackHandlerImpl();
            //check if this (fall-back) type of notification was already sent i.e existed on filter at time of notification.
            boolean sendFallback = !isFallbackNtfTypeAlreadyTriggered(fn, user.getFilter(), email, fallbackEvent,fh );
            switch(fh.getStatus()) {
                case Constants.FEEDBACK_STATUS_OK: //normal case, go forward. deliberate fall through
                case Constants.FEEDBACK_STATUS_EXPIRED: //should never happen assume ok.
                    //allow fall back if roaming check fails, let the notifier check..
                    break;

                case Constants.FEEDBACK_STATUS_UNKNOWN: //allow a retry.
                case Constants.FEEDBACK_STATUS_RETRY: 
                    return false;
                case Constants.FEEDBACK_STATUS_FAILED:
                    return true; //cancel the event and fail..
                default:
                    log.warn("Unknown status when checking filter - continue anyway.");
                    break;
            }
            if (sendFallback) {
                // The event of the notification is now in fall back mode             
                fallbackEvent.setFallback();
                switch(fn){
                    case FALLBACK_SMS:
                        if (Constants.NTF_ODL == fallbackEvent.getFallbackOriginalNotificationType()) {
                            // Sending the SMS replacing Out dial MDR
                            // This is a legacy MDR that we have to send.
                            // The MRD should be re-factored to send all the fallback cases, not just one.
                            MerAgent.get(Config.getInstanceComponentName()).replacementSMSSent(fallbackEvent.getRecipient());
                        }
                        return (fallbackToSms(user, email, fallbackEvent));
                    case FALLBACK_FLS:
                        if (Constants.NTF_ODL == fallbackEvent.getFallbackOriginalNotificationType()) {
                            // Sending the SMS replacing Out dial MDR
                            // This is a legacy MDR that we have to send.
                            // The MRD should be re-factored to send all the fallback cases, not just one.
                            MerAgent.get(Config.getInstanceComponentName()).replacementSMSSent(fallbackEvent.getRecipient());
                        }
                        return (fallbackToFls(user, email, fallbackEvent));
                    case FALLBACK_FLSSMS:
                        if (Constants.NTF_ODL == fallbackEvent.getFallbackOriginalNotificationType()) {
                            // Sending the SMS replacing Out dial MDR
                            // This is a legacy MDR that we have to send.
                            // The MRD should be re-factored to send all the fall back cases, not just one.
                            MerAgent.get(Config.getInstanceComponentName()).replacementSMSSent(fallbackEvent.getRecipient());
                        }
                        return (fallbackToSmsFls(user, email, fallbackEvent));
                    case FALLBACK_OUTDIAL:                       
                        return(fallbackToOutdial(user, email, fallbackEvent));
                    case FALLBACK_SIPMWI:
                        return (fallbackToSipMwi(user, email, fallbackEvent));
                }
            }

        } else {
            log.error("Subscriber " + subscriber + " not found (filter/profile), fallback " + fn.getName() + " will not be invoked.");
            return true;
        }
        return true;
    }

    private triState removeIfDisabled(int type, UserInfo user, List<String> numbers, IMessageDepositInfo email) {
        //Check if operator wants fall back to respect notifications disabled
        if  (Config.getFallbackUseMoipUserNtd() == false) {
            return triState.FALSE;
        }

        /* if null we do not check roaming/home disable just fully disabled, so we do not need to
         * iterate over the numbers.
         * NOTE: if empty list of numbers we end up returning disabled, this makes logic easier for some callers.
         * We treat an empty list as disabled.
         */
        if(numbers == null) {
            if (user.getFilter().handleDisablings(type, email, null, null)) {
                return triState.TRUE;
            } else {
                return triState.FALSE;
            }
        }    

        Iterator<String> iter = numbers.iterator();
        FeedbackHandlerImpl fh = new FeedbackHandlerImpl();
        while (iter.hasNext()) {
            String number = iter.next();
            // No fall-back invoked if subscriber has disabled this notification type
            if (user.getFilter().handleDisablings(type, email, fh, number)) {
                log.debug("isDisabled() " + number + " is disabled " + Constants.notifTypeStrings[type] + " dropping from list." );
            }
            //this should never happen as we are not checking home/roaming disabled at this point as no number is given to handle disable.
            switch(fh.getStatus()) {
                case Constants.FEEDBACK_STATUS_OK: //normal case, go forward. deliberate fall through
                case Constants.FEEDBACK_STATUS_UNKNOWN: //allow to try deliberate fall through                         
                case Constants.FEEDBACK_STATUS_EXPIRED: //should never happen - deliberate fall through
                    //leave in the list not disabled.
                    break;
                case Constants.FEEDBACK_STATUS_RETRY: //allow to try type, will check if disabled.. deliberate fall through
                    log.warn("isDisabled() " + number + "Unable to obtain roaming state for number, will retry later.");
                    return triState.ERROR; //false will not cancel and allow a retry.

                case Constants.FEEDBACK_STATUS_FAILED:
                    log.warn("isDisabled() " + number + "Unable to obtain roaming state for number, discarding due to Fail Action.");
                    iter.remove();
                default:
                    log.warn("isDisabled() Unknown status when checking disabled - continue anyway.");
                    break;
            }
        }

        if (numbers.size() > 0) {
            return triState.FALSE; //at least one number not disabled..
        } else
        {
            return triState.TRUE;
        }
    }

    /**
     * Fall back to both Flash and SMS - respecting disabled types if configured to do so.
     * @param user - the user profile.
     * @param email - the subscribers email (event) to fall back from.
     * @param fallbackEvent - the fall back event to notify
     * @return true if notified (cancel retry)
     */
    private boolean fallbackToSmsFls(UserInfo user, NotificationEmail email, FallbackEvent fallbackEvent) {
        if  (fallbackEvent.getFallbackOriginalNotificationType() == Constants.NTF_FLS || fallbackEvent.getFallbackOriginalNotificationType() == Constants.NTF_SMS) {
            log.error("Fallback to the same notification type not allowed.  Extend the retry schema of " + Constants.notifTypeStrings[fallbackEvent.getFallbackOriginalNotificationType()] + " instead.");
            return true;
        }


        boolean flsDisabled = false;

        if (Config.getFallbackFlashUrgentOnly() == true) {
            if ( !(email.isUrgent() || FallbackUtil.areUgentMails(user)) ) {
                //There are no unread urgent mails, drop notifications.
                log.debug("Subscriber " + email.getReceiver() + " has no urgent messages, dropping FLS notification.");
                flsDisabled=true;
            }
        }

        triState result;
        ArrayList<String> flsList = null; 
        if (!flsDisabled) { //if not already disabled due to non-urgent..
            flsList = user.getFilter().getNotifNumbersList(FallbackNotificationTypes.FALLBACK_FLS.getProfile(),Constants.TRANSPORT_MOBILE, email);

            result = removeIfDisabled(Constants.NTF_FLS,user,flsList,email);

            switch (result) {
                case ERROR:
                    return false;
                case FALSE:
                    break;
                case TRUE:
                    flsDisabled=true;
            }
        }
        
        boolean smsDisabled = false;
        ArrayList<String> smsList = null;
       
        if (flsDisabled==false && (Config.getFallbackFlashUrgentOnly() == true) && (Config.getFallbackSmsOnUrgentIfFlsSent() == false)) {
          //disable SMS due to sending FLS for urgent and configured NOT to Send SMS in case of FLS sent.
          //this is for the case operator wants a flash for urgent to replace a regular SMS and is the default case.
            smsDisabled=true; 
        }

        if ( !smsDisabled ) {
            smsList = user.getFilter().getNotifNumbersList(FallbackNotificationTypes.FALLBACK_SMS.getProfile(), Constants.TRANSPORT_MOBILE, email);
            result = removeIfDisabled(Constants.NTF_SMS,user,smsList,email);

            switch (result) {
                case ERROR:
                    return false;
                case FALSE:
                    break;
                case TRUE:
                    flsDisabled=true;
            }
        }


        if (smsDisabled && flsDisabled) {
            log.debug("Subscriber " + email.getReceiver() + " has disabled FLS and SMS  notifications; will not trigger fallback.");
            return true;
        } 
        FallbackNotificationTypes fn = FallbackNotificationTypes.FALLBACK_FLSSMS; //assume both.
        if (flsDisabled) {
            fn = FallbackNotificationTypes.FALLBACK_SMS; //only do SMS notification. For lvl 2 retry..
        } else if (smsDisabled) {
            fn = FallbackNotificationTypes.FALLBACK_FLS; //only do FLS notification For lvl 2 retry.
        }         

        //setup the notification group feedback.
        email.setNtfEvent(fallbackEvent); //set the feedback event..
        NotificationGroup ng = new NotificationGroup(NtfMain.getEventHandler(), email, log, MerAgent.get(Config.getInstanceComponentName()));
        ng.addUser(user); //add this user to the ng, only one user to be notified in this case..

        if (!flsDisabled && flsList != null) {
            Properties props = new Properties();

            email.setEmailType(Constants.NTF_VOICE);
            props.setProperty("FLS", getFallbackContent(fallbackEvent,email,Constants.NTF_FLS));
            
            
            String flsNumbers[] = new String[flsList.size()];
            flsNumbers = flsList.toArray(flsNumbers);

            SmsFilterInfo filterInfo = new SmsFilterInfo(props, null, null, flsNumbers);

            SMSAddress source = NotificationHandler.getSourceAddressEmail(email, user.getCosName());

            email.setNtfEvent(fallbackEvent);

            int count = SMSOut.get().handleSMS(user, filterInfo, ng, email, email.getUserMailbox(), source, user.getNotifExpTime(), 0);
            ng.setOutCount(user, count);

            countersfFallbackToSms++;
            profileCounters(fallbackEvent.getFallbackOriginalNotificationType(), Constants.NTF_FLS);
            fallbackHandler.profilerAgentCheckPoint("NTF.Fallback.5.Out (total handover)");
        }


        if (!smsDisabled && smsList != null) {
            Properties props = new Properties();

            email.setEmailType(Constants.NTF_VOICE);
            props.setProperty("SMS", getFallbackContent(fallbackEvent,email,Constants.NTF_SMS));
            
            String smsNumbers[] = new String[smsList.size()];
            smsNumbers = smsList.toArray(smsNumbers);

            SmsFilterInfo filterInfo = new SmsFilterInfo(props, smsNumbers, null, null);

            SMSAddress source = NotificationHandler.getSourceAddressEmail(email, user.getCosName());

            email.setNtfEvent(fallbackEvent);

            int count = SMSOut.get().handleSMS(user, filterInfo, ng, email, email.getUserMailbox(), source, user.getNotifExpTime(), 0);
            ng.setOutCount(user, count);

            countersfFallbackToSms++;
        }

        /**
         * SMS level-2 scheduling - specific case.
         * When fall back  to SMS, a level-2 scheduling must be performed since this notification type
         * is still handled at this level (however, FLSSMS should be an independent notification at level-3).
         * In fall back mode, SMS is the only notification that SHALL be invoked.
         * Therefore, the level-2 fall back must be limited to FLS and NOT the other notification types
         * that the subscriber may have.
         */
        NtfRetryHandling handler = NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.DEFAULT_NTF.getName());
        fallbackEvent.setSentListener(NtfEventHandlerRegistry.getEventSentListener(NtfEventTypes.DEFAULT_NTF.getName()));
        fallbackEvent.setFallBackName(fn.getConfiguredName());
        if (handler != null) {
            String backupId = handler.scheduleEvent(fallbackEvent);
            fallbackEvent.keepReferenceID(backupId);
            log.debug(fn.getName() + " scheduled for subscriber " + email.getReceiver() + ", eventId: " + backupId);
        }
        
        ng.noMoreUsers();


        profileCounters(fallbackEvent.getFallbackOriginalNotificationType(), Constants.NTF_FLSSMS);
        fallbackHandler.profilerAgentCheckPoint("NTF.Fallback.5.Out (total handover)");
        return true;
    }

    private boolean fallbackToFls(UserInfo user, NotificationEmail email, FallbackEvent fallbackEvent) {
        if  (fallbackEvent.getFallbackOriginalNotificationType() == Constants.NTF_FLS || fallbackEvent.getFallbackOriginalNotificationType() == Constants.NTF_SMS) {
            log.error("fallbackToFls() Fallback to the same notification type not allowed.  Extend the retry schema of " + Constants.notifTypeStrings[fallbackEvent.getFallbackOriginalNotificationType()] + " instead.");
            return true;
        }

        if (Config.getFallbackFlashUrgentOnly() == true) {
            if ( !(email.isUrgent() || FallbackUtil.areUgentMails(user)) ) {
                //There are no unread urgent mails, drop notifications.
                log.debug("fallbackToFls() Subscriber " + email.getReceiver() + " has no urgent messages, dropping FLS notification.");
                return true;
            }
        }

        ArrayList<String> flsList = user.getFilter().getNotifNumbersList(FallbackNotificationTypes.FALLBACK_FLS.getProfile(), Constants.TRANSPORT_MOBILE, email);
        triState result = removeIfDisabled(Constants.NTF_SMS,user,flsList,email);
        switch (result) {
            case ERROR:
                return false;
            case FALSE:
                break;
            case TRUE:
                //no numbers to notify.
                log.debug("fallbackToFls() no numbers to notify(disabled), cancel fallback " + email.getReceiver());
                return true;
        }

        Properties props = new Properties();

        email.setEmailType(Constants.NTF_VOICE);
        props.setProperty("FLS", getFallbackContent(fallbackEvent,email,Constants.NTF_FLS));
        
        String flsNumbers[] = new String[flsList.size()];
        flsNumbers = flsList.toArray(flsNumbers);

        SmsFilterInfo filterInfo = new SmsFilterInfo(props, null, null, flsNumbers);

        NotificationGroup ng = new NotificationGroup(NtfMain.getEventHandler(), email, log, MerAgent.get(Config.getInstanceComponentName()));
        ng.addUser(user);

        SMSAddress source = NotificationHandler.getSourceAddressEmail(email, user.getCosName());

        /**
         * SMS level-2 scheduling - specific case.
         * When fall back  to SMS, a level-2 scheduling must be performed since this notification type
         * is still handled at this level (however, SMS should be an independent notification at level-3).
         * In fall back mode, SMS is the only notification that SHALL be invoked.
         * Therefore, the level-2 fall back must be limited to FLS and NOT the other notification types
         * that the subscriber may have.
         */
        NtfRetryHandling handler = NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.DEFAULT_NTF.getName());
        fallbackEvent.setFallBackName(FallbackNotificationTypes.FALLBACK_FLS.getConfiguredName());
        fallbackEvent.setSentListener(NtfEventHandlerRegistry.getEventSentListener(NtfEventTypes.DEFAULT_NTF.getName()));
        if (handler != null) {
            String backupId = handler.scheduleEvent(fallbackEvent);
            fallbackEvent.keepReferenceID(backupId);
            log.debug("FLS scheduled for subscriber " + email.getReceiver() + ", eventId: " + backupId);
        }

        email.setNtfEvent(fallbackEvent);

        int count = SMSOut.get().handleSMS(user, filterInfo, ng, email, email.getUserMailbox(), source, user.getNotifExpTime(), 0);
        ng.setOutCount(user, count);
        ng.noMoreUsers();

        countersfFallbackToSms++;
        profileCounters(fallbackEvent.getFallbackOriginalNotificationType(), Constants.NTF_FLS);
        fallbackHandler.profilerAgentCheckPoint("NTF.Fallback.5.Out (total handover)");
        return true; 
    }

//    /**
//     * Method to retrieve the fallback notification based on the original notification type from the configuration.
//     * @param originalNotificationType originalNotificationType
//     * @return FallbackNotificationTypes
//     */
//    private FallbackNotificationTypes getFallbackNotificationType(int originalNotificationType) {
//
//        return (FallbackUtil.getFallbackNotificationType(originalNotificationType));
//    }

    /**
     * Method to find out if the given fallback notification has already been triggered for the subscriber for the original notification.
     * If so, then there is no reason to trigger the fallback mechanism.
     * @param fn FallbackNotificationTypes
     * @param filter NotificationFilter
     * @param email NotificationEmail
     * @return boolean fallback notification already triggered or not
     */
    private boolean isFallbackNtfTypeAlreadyTriggered(FallbackNotificationTypes fn, NotificationFilter filter, NotificationEmail email, FallbackEvent fallbackEvent, FeedbackHandler fh){
        boolean alreadyTriggered = false;

        /**
         * If the fallback notification type is in the subscriber's filter, the fallback notification was already triggered in the original process.
         *
         * The following attributes are checked when checking the filter.
         * - notifDisabled          subscriber level        Notification: Notification deactivated
         * - filter(part).active    subscriber/cos level    Filter (Active*:)
         * - match(part).notify     subscriber/cos level    Filter (Notify*:)
         * - userNtd                subscriber/cos level    Notification: Notification disabled         MoipUserNTD (including roaming disabled).
         */
        GregorianCalendar receivedDate = new GregorianCalendar();
        Object filterInfoForOriginalNotification = null;
        //NOTE FallBackFLSSMS
        switch(fn){
            case FALLBACK_SMS:
                SmsFilterInfo smsFiltInfo = filter.getSmsFilterInfo(email, receivedDate, fh);
                if (smsFiltInfo != null) { 
                    if (smsFiltInfo.isSms()) {
                        //SMS filter info can be a SMS/MWI or both in this case.
                        //We only care if SMS.
                        filterInfoForOriginalNotification=smsFiltInfo;
                    } else 
                    {
                        /* check special case for fall-back from SIPMWI.
                         * legacy fall-back code interpreted an SMS MWI as an SMS even though a fall-back SMS
                         * does not even send an MWI on as part of the SMS message.
                         * 
                         * TTCN test cases fail due to this. Is this a bug? probably but for legacy reasons we
                         * will interpret an SMS MWI as an SMS by default for fall-back purposes.
                         */
                        if (Config.getFallbackSmsWhenMwiSmsOnly()) {
                            if (smsFiltInfo.isMwi()) {
                                //if not null then below will interpret as already sent.
                                filterInfoForOriginalNotification=smsFiltInfo;
                            }
                        }
                    }
                }           
                break;
            case FALLBACK_FLS:
                filterInfoForOriginalNotification = filter.getFlashFilterInfo(email, receivedDate, fh);          
                break; 
            case FALLBACK_OUTDIAL:
                filterInfoForOriginalNotification = filter.getOdlFilterInfo(email, receivedDate, fh);
                break;
            case FALLBACK_SIPMWI:
                filterInfoForOriginalNotification = filter.getSIPFilterInfo(email, receivedDate, fh);
                break;
            case FALLBACK_FLSSMS:
                SmsFilterInfo flsFilt = filter.getFlashFilterInfo(email, receivedDate, fh);

                SmsFilterInfo smsFilt = filter.getSmsFilterInfo(email, receivedDate, fh);
                if (!smsFilt.isSms())
                {
                    smsFilt=null;
                }
                if (smsFilt != null && flsFilt != null) //if both were sent then stop now unless maybe roaming later..
                {
                    filterInfoForOriginalNotification=1;
                }           
                break;
            default:
                log.warn("isFallbackNtfTypeAlreadyTriggered() Subscriber " + email.getReceiver() + "Unknown fallback Type [" + fn + "], assuming not sent. ");
                return true;
        }

        if (filterInfoForOriginalNotification !=null ) {
            //allow fall-back unless the original type was disabled due to changing to roaming, chances are the subscriber switched from say
            //out-dial to SMS, so SMS would be allowed when roaming but the original SMS was never sent.
            //worse case we send an extra notification but subscriber still gets notified if not already sent.
            boolean result = filter.isNotifTypeDisabledDueToRoaming(fallbackEvent.getFallbackOriginalNotificationType(),email.getReceiverPhoneNumber(),fh);
            if (result == true) {
                //The fall-back type has been disabled due to a change to roaming state, so probably the original (now fall-back) notification
                //type was not sent, in this case we make this assumption and send the fall back-anyway..
                return false; 
            }
        }

        if (filterInfoForOriginalNotification != null) {
            log.debug("Subscriber " + email.getReceiver() + " already had " + Constants.notifTypeStrings[fallbackEvent.getFallbackOriginalNotificationType()] + " notification type triggered.");
            alreadyTriggered = true;
        }
        return alreadyTriggered;
    }

    /**
     * @param user - the user profile.
     * @param email - the subscribers email (event) to fall back from.
     * @param fallbackEvent - the fall back event to notify
     * @return true if notified (cancel retry)
     */
    private boolean fallbackToSms(UserInfo user, NotificationEmail email, FallbackEvent fallbackEvent) {
        if  (fallbackEvent.getFallbackOriginalNotificationType() == Constants.NTF_SMS) {
            log.error("Fallback to the same notification type not allowed.  Extend the retry schema of " + Constants.notifTypeStrings[fallbackEvent.getFallbackOriginalNotificationType()] + " instead.");
            return true;
        }

        ArrayList<String> smsList = user.getFilter().getNotifNumbersList(FallbackNotificationTypes.FALLBACK_SMS.getProfile(), Constants.TRANSPORT_MOBILE, email);
        triState result = removeIfDisabled(Constants.NTF_SMS,user,smsList,email);
        switch (result) {
            case ERROR:
                return false;
            case FALSE:
                break;
            case TRUE:
                //no numbers to notify.
                log.debug("fallbackToSms() no numbers to notify(disabled), cancel fallback " + email.getReceiver());
                return true;
        }

        Properties props = new Properties();

        email.setEmailType(Constants.NTF_VOICE);
        props.setProperty("SMS", getFallbackContent(fallbackEvent,email,Constants.NTF_SMS));
        
     // toArray copies content into other array
        String smsNumbers[] = new String[smsList.size()];
        smsNumbers = smsList.toArray(smsNumbers);

        SmsFilterInfo filterInfo = new SmsFilterInfo(props, smsNumbers, null, null);

        NotificationGroup ng = new NotificationGroup(NtfMain.getEventHandler(), email, log, MerAgent.get(Config.getInstanceComponentName()));
        ng.addUser(user);

        SMSAddress source = NotificationHandler.getSourceAddressEmail(email, user.getCosName());

        /**
         * SMS level-2 scheduling - specific case.
         * When fall back to SMS, a level-2 scheduling must be performed since this notification type
         * is still handled at this level (however, SMS should be an independent notification at level-3).
         * In fall back mode, SMS is the only notification that SHALL be invoked.
         * Therefore, the level-2 fall back must be limited to SMS and NOT the other notification types
         * that the subscriber may have.
         */
        NtfRetryHandling handler = NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.DEFAULT_NTF.getName());
        fallbackEvent.setSentListener(NtfEventHandlerRegistry.getEventSentListener(NtfEventTypes.DEFAULT_NTF.getName()));
        fallbackEvent.setFallBackName(FallbackNotificationTypes.FALLBACK_SMS.getConfiguredName());
        if (handler != null) {
            String backupId = handler.scheduleEvent(fallbackEvent);
            fallbackEvent.keepReferenceID(backupId);
            log.debug("SMS scheduled for subscriber " + email.getReceiver() + ", eventId: " + backupId);
        }



        email.setNtfEvent(fallbackEvent);

        int count = SMSOut.get().handleSMS(user, filterInfo, ng, email, email.getUserMailbox(), source, user.getNotifExpTime(), 0);
        ng.setOutCount(user, count);
        ng.noMoreUsers();

        countersfFallbackToSms++;
        profileCounters(fallbackEvent.getFallbackOriginalNotificationType(), Constants.NTF_SMS);
        fallbackHandler.profilerAgentCheckPoint("NTF.Fallback.5.Out (total handover)");

        return true;
    }

    /**
     * @param user - the user profile.
     * @param email - the subscribers email (event) to fall back from.
     * @param fallbackEvent - the fall back event to notify
     * @return true if notified (cancel retry)
     */
    private boolean fallbackToOutdial(UserInfo user, NotificationEmail email, FallbackEvent fallbackEvent) {
        if  (fallbackEvent.getFallbackOriginalNotificationType() == Constants.NTF_ODL) {
            log.error("Fallback to the same notification type not allowed.  Extend the retry schema of " + Constants.notifTypeStrings[fallbackEvent.getFallbackOriginalNotificationType()] + " instead.");
            return true;
        }

        ArrayList<String> odlList = user.getFilter().getNotifNumbersList(FallbackNotificationTypes.FALLBACK_OUTDIAL.getProfile(), Constants.TRANSPORT_MOBILE, email);
        triState result = removeIfDisabled(Constants.NTF_ODL,user,odlList,email);
        switch (result) {
            case ERROR:
                return false;
            case FALSE:
                break;
            case TRUE:
                //no numbers to notify.
                log.debug("fallbackToOutdial() no numbers to notify(disabled), cancel fallback " + email.getReceiver());
                return true;
        }

        Properties p = new Properties();
        
        String odlNumbers[] = new String[odlList.size()];
        odlNumbers = odlList.toArray(odlNumbers);
        
        OdlFilterInfo filterInfo = new OdlFilterInfo(p, odlNumbers);
        OutdialNotificationOut.get().notify(user, filterInfo, fallbackEvent.getSubscriberNumber(), fallbackEvent);

        countersfFallbackToOutdial++;
        profileCounters(fallbackEvent.getFallbackOriginalNotificationType(), Constants.NTF_ODL);
        fallbackHandler.profilerAgentCheckPoint("NTF.Fallback.5.Out (total handover)");
        return true;

    }

    /**
     * @param user - the user profile.
     * @param email - the subscribers email (event) to fall back from.
     * @param fallbackEvent - the fall back event to notify
       @return true if notified or finished (cancel retry) - this particular type does not get error for roaming checks so never retries (true)
     */
    private boolean fallbackToSipMwi(UserInfo user, NotificationEmail email, FallbackEvent fallbackEvent) {
        if  (fallbackEvent.getFallbackOriginalNotificationType() == Constants.NTF_MWI) {
            log.error("fallbackToSipMwi() Fallback to the same notification type not allowed.  Extend the retry schema of " + Constants.notifTypeStrings[fallbackEvent.getFallbackOriginalNotificationType()] + " instead.");            
            return true;
        }

        String[] numbers = user.getFilter().getMatchingDeliveryProfileNumbers(FallbackNotificationTypes.FALLBACK_SIPMWI.getProfile(), Constants.TRANSPORT_IP);

        if (numbers != null) {
            if (Config.getFallbackUseMoipUserNtd() == true && user.getFilter().handleDisablings(Constants.NTF_MWI, email, null, null)) {
                log.info("fallbackToSipMwi() MWI is disabled for subscriber " + email.getReceiver());
                return true;
            }

            SIPFilterInfo sipFilterInfo = new SIPFilterInfo(numbers);
            SIPOut.get().handleMWI(user, sipFilterInfo, email.getUserMailbox(), fallbackEvent.getSubscriberNumber(), fallbackEvent.getMsgInfo(), fallbackEvent);

            countersfFallbackToSipMwi++;
            profileCounters(fallbackEvent.getFallbackOriginalNotificationType(), Constants.NTF_SIPMWI);
            fallbackHandler.profilerAgentCheckPoint("NTF.Fallback.5.Out (total handover)");
        } else {
            log.error("fallbackToSipMwi() Delivery numbers not found for subscriber " + email.getReceiver() + "; fallback " + FallbackNotificationTypes.FALLBACK_SIPMWI.getName() + " will not be invoked.");
            return true;
        }
        return true;
    }

    private String getFallbackContent(FallbackEvent fallbackEvent,NotificationEmail email,int fallbackType) {
        FallbackInfo info = fallbackEvent.getFallbackInfo();
        if (info != null) {
            return info.getContent();
        }
        //else use default
        return FallbackUtil.getFallbackContent(fallbackEvent.getFallbackOriginalNotificationType(),email.isReminderNotification(),fallbackType);
    }

    private void profileCounters(int originalNotificationType, int fallbackNotificationType) {
        fallbackHandler.profilerAgentCheckPoint("NTF.Fallback.2.From"+Constants.notifTypeStrings[originalNotificationType]+"To"+Constants.notifTypeStrings[fallbackNotificationType]);
    }

    /**
     * Test purposes
     * @return countersfFallbackToSms
     */
    public static int getCountersfFallbackToSms() {
        return countersfFallbackToSms;
    }

    /**
     * Test purposes
     * @return countersfFallbackToOutdial
     */
    public static int getCountersfFallbackToOutdial() {
        return countersfFallbackToOutdial;
    }

    /**
     * Test purposes
     * @return countersfFallbackToSipMwi
     */
    public static int getCountersfFallbackToSipMwi() {
        return countersfFallbackToSipMwi;
    }

    @Override
    /**
     * The shutdown loop stops after the ntfRun method is finished.
     *
     * @return true always (i.e. this thread has not shutdown activity)
     */
    public boolean shutdown() {
        if (isInterrupted()) {
            return true;
        } //exit immediately if interrupted..

        if (queue.size() == 0)
        {
                //give a short time for new items to be queued in workers, to allow other threads to empty there queues.
                if (queue.isIdle(2,TimeUnit.SECONDS)) {
                    return true;
                }
                else
                {
                    if (queue.waitNotEmpty(2, TimeUnit.SECONDS)) {
                        return(ntfRun());
                    } else
                    {
                        return true;
                    }

                }
        } else {
            return(ntfRun());
        }
    }
}

