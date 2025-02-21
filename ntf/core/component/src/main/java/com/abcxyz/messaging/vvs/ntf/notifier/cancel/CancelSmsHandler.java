/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2015.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.abcxyz.messaging.vvs.ntf.notifier.cancel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.CancelFeedBack;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.CancelInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.CancelInfo.CancelType;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.SMSAddressInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.IntfServiceTypePidLookup;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackUtil;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackEvent.FallbackNotificationTypes;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.coremgmt.reminder.ReminderUtil;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.NotificationHandler;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.out.FeedbackHandlerImpl;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.out.sms.ServiceTypePidLookupImpl;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * 
 * Utility Class to handle 
 * sending of Cancel SMS if configured to do so. 
 * 
 * Cancel SMS will cancel SMS queued in the SMSC if any given 
 * the criteria of source address,Destination address and ServiceType.
 * 
 * Depending on how specific the cancel needs to be the criteria are
 * configurable.
 * 
 * @author lmcmajo
 */
public class CancelSmsHandler implements Constants {
    
    private static LogAgent log =  NtfCmnLogger.getLogAgent(CancelSmsHandler.class);
       
    /** SMS output interface */
    private static SMSOut smsOut=SMSOut.get();
    //The ServiceType interface.
    private static IntfServiceTypePidLookup serviceTypeLookup = ServiceTypePidLookupImpl.get();
    /** Interface to MER/MDR */
    private static MerAgent mer = MerAgent.get();
    private static boolean allowCancelbySrcAddress=true;
    private static boolean allowCancelbyServiceType=false;
    private static boolean debug=Config.isCancelSmsDebug();
    private static String smeServiceType=Config.getSmeServiceType();
    private static SMSAddress smeSourceAddress = Config.getSmeSourceAddress();
    private static boolean cancelEnabled = false;
    
    private static CancelSmsHandler cancelSmsManager = null; //single instance of this class.

    //Mwi specific
    private static String MwiOnserviceType;  //the current setting of mwi on service Type from replace Table or more general if not set in table.
    private static String MwiOffserviceType; //the current setting of mwi off service Type from replace Table or more general if not set in table.
    private static String MwiGeneralServiceType; //the mwi service type if configured, used by both mwi on and off if not overridden by replaceTable.

    private static boolean MwiOnOffServiceTypeMatch;
    private static boolean GeneralSmsServiceTypeWillCancelAllMwi;

    private static boolean mwiOnUsingDefaultServiceType; //indicate that MwiOn is using default ServiceType.

    private static CancelSmsEnabledForEvent cancelSmsEnabledForEvent; //indicates which type of traffic event initiates a cancel if any.

    private static CancelSmsPluginDistributor pluginDistributor; //the distributor of cancel to the notifier plug-ins if configured.
    
    
    static {
        refreshConfig();
    }

    /**
     * CancelManager sends cancel requests to the SMS part, but most of all
     * remembers what has already been cancelled and skips duplicates.
     */
    private class CancelManager {
        HashSet<String> c = new HashSet<String>(); //Combinations already cancelled

        public void cancel(SMSAddress from,
                SMSAddress to,
                boolean cancelByReplacePosition,
                String notifContent) {

            //Already cancelled - this more general than service_type/replace pos so would have already been cancelled.
            if (c.contains(from.toString() + to.toString())) { return; } //Already cancelled

            if (!cancelByReplacePosition || notifContent == null) {
                c.add(from.toString() + to.toString());
            } else {    
                if (c.contains(from.toString() + to.toString() + serviceTypeLookup.getServiceType(notifContent))) { return;} //Already cancelled
                c.add(from.toString() + to.toString() + serviceTypeLookup.getServiceType(notifContent));
            }
            smsOut.sendCancel(from, to, cancelByReplacePosition, notifContent);
        }

        public boolean didCancel() {
            return !c.isEmpty();
        }

        public void cancel(SMSAddress from, SMSAddress to, String serviceType) {
            
            
            //Already cancelled - this more general than service_type so would have already been cancelled.
            if (c.contains(from.toString() + to.toString())) { return; } 
            
            if (serviceType == null) {
                cancel(from,to,false,null);
            } else {
                if (c.contains(from.toString() + to.toString() + serviceType)) { return; } //Already cancelled
                c.add(from.toString() + to.toString() + serviceType);
                smsOut.sendCancelbyServiceType(from, to, serviceType);
            }
        }
        
        public String toString() {
            return c.toString();
        }

        public int size() {
            return c.size();
        }

        public void cancel(SMSAddress from, SMSAddress to, int cancelPosition) {
            //get the servie type from the table dependent on position.
            //if it's -1 it will be null, which is handled by cancel call.
            String serviceType = serviceTypeLookup.getServiceType(cancelPosition);
            cancel(from,to,serviceType);            
        }

    }

    
    public static CancelSmsHandler get() {
        if (cancelSmsManager  == null) {
         cancelSmsManager = new CancelSmsHandler();
        }
        return cancelSmsManager;
    }
    
    public static boolean isCancelEnabledForMailBoxUpdate() {
        return cancelSmsEnabledForEvent == CancelSmsEnabledForEvent.mailboxupdate;
    }
    
    public static boolean isCancelEnabledForMwiOff() {
        return cancelSmsEnabledForEvent == CancelSmsEnabledForEvent.mwioff;
    }

    private CancelSmsHandler() {        
    }
    
    /**
     * Cancel pending SMS/FLS notifications if the user has retrieved all new messages.
     * This uses the cancel SM message sent to the SMSC.  We can cancel by service_type read (content/phrase via the 
     * ReplaceNotifications.List)
     * 
     * The sender number which can be configured per message type.
     * 
     * The subscriber notification number(s) 
     * 
     * We cancel, SMS reminders, SMS fall-back and all configured SMS notification types (SMS MWI, count, subject ,header, flash etc).
     * Cancel does not consider if a notification type is disabled or in the Subscribers filter, just if those types are allowed and 
     * has configured numbers for those notification types.  This is because the subscriber may have changed there filter and disabled
     * settings when the logged into the mailbox and cancelling a non-existent SMS is just ignored by the SMSC/Terminal anyway.
     * 
     * Cancel does not deliberately cancel other SMS types like VVASMS (greeting and pin reminder), system notifications etc, only
     * notification types.  However if using too general configuration for source address and or service Type, it it highly likely
     * these other notifications will also get cancelled, it may even cancel other messages for the user from another system.
     * 
     * It is therefore better to configure all types (content) the operator may cancel in the replace.table and or the source 
     * address table.  However this will often result in substantial SMS traffic.  Perhaps the best way is to use one source
     * address for all notifications that operator wants to cancel, and another for anything else.
     * 
     * It should also be noted that Cancel will not work when using the senders number with voice mail notifications, unless
     * the source address is always empty and you use service type instead.
     * 
     * A future improvement may be to allow the operator to set specific Service type in the replace table for all types they
     * wish to cancel, however this will not work well with replace as the wrong messages may be replaced.
     * 
     *@param user information about the receiver.
     *@param email information about the logout circumstances.
     *@param inbox the users mailbox.
     *@param ng - the notification group used in case MWI off is sent.
     */    
    public void handleSendCancelSms(UserInfo user, NotificationEmail email, UserMailbox inbox, NotificationGroup ng)  {
        
        if (!cancelEnabled) { return; }

        /* NOTE: in MoIP Solaris cancel was only discarded for MWI off, but in MIO due to time critical and large Parallel NTF and Threads
         * it is not advisable to cancel any SMS unless the mail box is really empty at the point of cancel.
         * so if a new message has been deposited while listening to older VM or the subscriber did not read all VM then don't
         * attempt to cancel.  This will not cover all cases, but should cover 99.9% of cancelling unread VM notifications.
         * 
         * Replace Notification should be used instead in these cases, if configured.
         */
        
        
        if (debug) {
            log.info("handleCancelSmsMwiAtUpdate: Preparing cancel for subscriber: " +  email.getReceiverPhoneNumber());
        }
        
        if (inbox.getNewTotalCount() > 0) {            
            log.debug("handleCancelSmsMwiAtUpdate: User still has new messages, dropping cancel SMS for subscriber: " + email.getReceiverPhoneNumber());
            return;
        }
            
        
        if (debug) {
            log.info("handleCancelSmsMwiAtUpdate: allow cancel by source address  : " + allowCancelbySrcAddress + " by service type/content: " +  allowCancelbyServiceType);
        }

        boolean doGeneralSMS = false;
        boolean doGeneralFLS = false;
        boolean cancelBySourceWouldCanelAllMWI = false;
        boolean sentFLSCancel = false;
        CancelManager cm = new CancelManager();
        NotificationFilter filter = user.getFilter();
        boolean hasFLS = filter.hasNotifType("FLS");
        boolean hasForcedFlsReminder = false; 
        boolean hasForcedSmsReminder = false;
        boolean hasSMS = filter.hasNotifType("SMS");
        
        if (debug) {
            log.info("handleCancelSmsMwiAtUpdate: will Cancel Flash: " + hasFLS + " Will cancel SMS types: " + hasSMS  );
        }
        
        //Determine if operator has forced reminders so if SMS or FLS notifications are not
        //in filter it will still cancel the reminders.
        //This still depends on delivery profile having SMS/FLS numbers.
        hasForcedFlsReminder = ReminderUtil.isForcedFLSReminder(); 
        hasForcedSmsReminder = ReminderUtil.isForcedSMSReminder();
        
        if (debug) {
            log.info("handleCancelSmsMwiAtUpdate: will Cancel forced Flash reminder : " + hasForcedFlsReminder + " Will cancel forced SMS reminder: " + hasForcedSmsReminder  );
        }


        FallbackNotificationTypes fallbackTypes = FallbackUtil.getUsedSMSFallbackTypes();

        
        String []smsNumbers = null; 
        if (hasSMS || hasForcedSmsReminder) {
            smsNumbers = filter.getNotifNumbers("SMS",email); 
            if (debug) {
                log.info("handleCancelSmsMwiAtUpdate: to the following SMS notifications numbers : " + Arrays.toString(smsNumbers));
            }
        }
        String []flsNumbers = null; 
        if (hasFLS || hasForcedFlsReminder) {
            flsNumbers = filter.getNotifNumbers("FLS",email); 
            if (debug) {
                log.info("handleCancelSmsMwiAtUpdate: to the following Flash notifications numbers : " + Arrays.toString(flsNumbers));
            }
        }


        for( int i=0;i<flsSmsCancelDepositTypes.length;i++ ) {
            if (debug) {
                log.info("handleCancelSmsMwiAtUpdate: checking Deposit Type " + flsSmsCancelDepositTypes[i]);
            }
            //Fall-back SMS uses the same source address as standard SMS depending on depositType so it is also handled here.
            if( userHasService(user, flsSmsCancelDepositTypes[i], "SMS")  || userHasService(user, flsSmsCancelDepositTypes[i], "FLS" ) ) {
                if( hasSMS || hasForcedSmsReminder) {
                    doGeneralSMS = cancelSms(filter,fallbackTypes,user,flsSmsCancelDepositTypes[i],smsNumbers,cm);
                } 

                if ( (hasFLS || hasForcedFlsReminder) && !sentFLSCancel) {

                    if (debug) {
                        log.info("handleCancelSmsMwiAtUpdate: has depositType " + flsSmsCancelDepositTypes[i] + " for FLASH");
                    }

                   doGeneralFLS = cancelFls(filter,fallbackTypes,user,flsSmsCancelDepositTypes[i],flsNumbers,cm);  
                   sentFLSCancel = true; //we only need to cancel flash types once as only one source address.
                }
            } else
            {
                if (debug) {
                    log.info("handleCancelSmsMwiAtUpdate: deposit Type not used by Subscriber: " +  flsSmsCancelDepositTypes[i]);
                }
            }
        }

        
        if (debug) {
            log.info("handleCancelSmsMwiAtUpdate: checking Cancel for MWI on." );
        }
       
        // cancel MWI if subscriber has service.
        if( filter.hasNotifType("MWI") && !user.isNotifTypeUnused("MWI") && user.isMwiUser() ) {
            
            cancelBySourceWouldCanelAllMWI = cancelMwi(user,cm,email);
            
        } else if (debug) {
                log.info("handleCancelSmsMwiAtUpdate: no MWI for subscriber, will not cancel. " );

        }

        if( doGeneralSMS && smsNumbers != null ) {
            if (debug) {
                log.info("handleCancelSmsMwiAtUpdate: perform a general cancel for SMS as using default service number and content source address for at least one deposit type." );
            }
            for( int n=0;n<smsNumbers.length;n++ ) {
                SMSAddress dest = new SMSAddress(user.getTypeOfNumber(), user.getNumberingPlan(), smsNumbers[n]);
                cm.cancel(smeSourceAddress, dest, false, null);
            }
        }
        if( doGeneralFLS && flsNumbers != null ) {
            if (debug) {
                log.info("handleCancelSmsMwiAtUpdate: perform a general cancel for FLS as using default service number and content source address for at least one deposit type." );
            }
            for( int n=0;n<flsNumbers.length;n++ ) {
                SMSAddress dest = new SMSAddress(user.getTypeOfNumber(), user.getNumberingPlan(), flsNumbers[n]);
                cm.cancel(smeSourceAddress, dest, false, null);
            }
        }
        
        if ( (cancelBySourceWouldCanelAllMWI && GeneralSmsServiceTypeWillCancelAllMwi) && ((doGeneralSMS && smsNumbers != null) || (doGeneralFLS && flsNumbers != null)) ) {
            
            log.debug("handleCancelSmsAtUpdate: sending mwi off with cancel as mwi off has been canceled by a more general cancel.");

            /* To be sure also send an MWI off as MWI on may have already got to the phone
             * in the case of using default service type and source address, we have probably
             * Cancelled MWI off, so send another one.
             * 
             * Typically MWI off and update are send by the call flow at the same time
             * since MWI is simpler than cancel, it will most probably go out before the 
             * cancel and hence be cancelled by this cancel.
             */
            FeedbackHandlerImpl fh = new FeedbackHandlerImpl();
            SmsFilterInfo filterInfo = filter.getSmsFilterInfo(email, new GregorianCalendar(), fh); 
            //if failed to check or subscriber would normally receive an MWI
            if (fh.getStatus() != Constants.FEEDBACK_STATUS_OK || filterInfo.isMwi()) {
                NotificationHandler.turnMwiOff(user, ng, email, false, null, inbox);
            }
        }
        
        //Determine if any notification plug-ins wish to send cancel for this subscriber.
        cancelFromPlugIn(email,cm); 
                
        if (debug) {
            if (cm.didCancel()) {               
                log.info("handleCancelSmsMwiAtUpdate: the following cancel sms were sent after filtering total [" + cm.size() + "] in form (from to [serviceType]): ");
                log.info("handleCancelSmsMwiAtUpdate: " + cm.toString());
            }
        }

        if( cm.didCancel() ) {
            log.debug("cancel sent for subscriber " + email.getReceiverPhoneNumber());
            mer.cancelSmsSent(email.getReceiverPhoneNumber());
        } else {
            log.debug("cancel not sent for subscriber " + email.getReceiverPhoneNumber());
        }
    }

    
    /* Cancel MWI and check if a generalCancel by source address only would cancel MWI off.
     * return true if general cancel by source only will cancel MWI off.
     */
    private boolean cancelMwi(UserInfo user,CancelManager cm, NotificationEmail email) {
        boolean sourceMwiOnlyWillCancelAllMWI = false;
        boolean mwiOnCancelbySourceWillCancelAllMwi = false;
        String []mwiNumbers = user.getFilter().getNotifNumbers("MWI",email); //note this is SMS MWI not SIP MWI (transport Mobile by default)
        if (mwiNumbers != null) {
            
            if (debug) {
                log.info("cancelMwi: will send MWI ON cancel for " + Arrays.toString(mwiNumbers) );
            }

            SMSAddress mwiOnSource;
            SMSAddress mwiOffSource;

            if (allowCancelbySrcAddress) {                    
                mwiOnSource = Config.getSourceAddress("mwion", user.getCosName());
                if (mwiOnSource.equals(smeSourceAddress)) {
                    if (debug) {
                        log.info("cancelMwi: mwi on using default source adrress for mwion source address as not in source Adrress table: " + smeServiceType );
                    }
                } else {
                    if (debug) {
                        log.info("cancelMwi: mwi on using source address from source address table: " + smeServiceType );
                    }
                }
                
                mwiOffSource = Config.getSourceAddress("mwioff", user.getCosName());
                if (mwiOffSource.equals(smeSourceAddress)) {
                    if (debug) {
                        log.info("cancelMwi: mwi off using default source adrress for mwion source address as not in source Adrress table: " + smeServiceType );
                    }
                } else {
                    if (debug) {
                        log.info("cancelMwi: mwi off using source address from source address table: " + smeServiceType );
                    }
                }
                
                if (mwiOffSource.equals(mwiOnSource)) {
                   mwiOnCancelbySourceWillCancelAllMwi = true;
                }
               
            } else {
                mwiOnSource=smeSourceAddress; //use default.
                mwiOffSource=smeSourceAddress; //use default.
                mwiOnCancelbySourceWillCancelAllMwi = true;
                if (debug) {
                    log.info("cancelMwi: MwiON using default source address for mwi as specified not to use source address for cancel " + smeServiceType );
                }
            }
            
            //figure out if a general SMS cancel for source only will also most likely cancel an MWI
            if (mwiOffSource.equals(smeSourceAddress)) {
                if (smeSourceAddress.getNumber().isEmpty() ) {
                    //this means we are sending to an empty number
                    sourceMwiOnlyWillCancelAllMWI=true;
                    if (debug) {
                        log.info("An SMS cancel or general cancel if only cancel by source address would also cancel an mwi off as using default source address and source address is empty.");
                    }
                }                    
            }
            
            
            if ( mwiOnCancelbySourceWillCancelAllMwi && MwiOnOffServiceTypeMatch) {
                log.info("cancelMwi, will not try to Cancel MWIOn a cancel would also cancel MWIOff and may leave the subscriber with mwiON still if already sent to phone before cancel.");
                
            } else {
                for( int n=0;n<mwiNumbers.length;n++) {
                    SMSAddress dest = new SMSAddress(user.getTypeOfNumber(), user.getNumberingPlan(), mwiNumbers[n]);
                    if (allowCancelbyServiceType && !mwiOnUsingDefaultServiceType) { 
                        if (debug) {
                            log.info("cancelMwi: send MWI cancel for source [" + mwiOnSource + "] dest [" + dest + "] serviceType: [" + MwiOnserviceType + "]");
                        }
                        cm.cancel(mwiOnSource, dest, MwiOnserviceType);
                    } else {
                        if (debug) {
                            log.info("cancelMwi: send MWI cancel for source [" + mwiOnSource + "] dest [" + dest + "]");
                        }
                        cm.cancel(mwiOnSource, dest, false, null);
                    }
                }
            }
        }
        
        return sourceMwiOnlyWillCancelAllMWI;
        
    }

    /*
     * Cancel flash messages.
     * Return true if should send a general Cancel as specific in at least one case is not configured.
     */
    private boolean cancelFls(NotificationFilter filter, FallbackNotificationTypes fallbackTypes, UserInfo user, depositType type, String[] flsNumbers, CancelManager cm) {
        
        SMSAddress source;
        boolean doGeneralFLS = false;
        boolean cancelByServiceType=true;
        
        if (allowCancelbySrcAddress) {
            source = Config.getSourceAddress(depositType.FLASH.source(), user.getCosName());
            if (debug) {
                log.info("cancelFls: using flash source address: " + source );
            }    
        } else {
            source = smeSourceAddress; //just use default.
            if (debug) {
                log.info("cancelFls: using default source address as configured to not cancel by source address: " + source );
            }  
        }

        HashSet<String> content = new HashSet<String>();
        if (allowCancelbyServiceType) { //only populate the content array if doing by serviceType as serviceType is populated based on content.
            if (debug) {
                log.info("cancelFls: gathering content for serviceType lookup for flash.");
            } 
            content.addAll(filter.getTemplatesForType(type, "FLS"));
            if (debug) {
                log.info("cancelFls: content found for FLS filter: " + content.toString() + " for deposit type: " + type);
            } 
            if (fallbackTypes == FallbackNotificationTypes.FALLBACK_FLS || fallbackTypes == FallbackNotificationTypes.FALLBACK_FLSSMS) {
                ArrayList<String> fallbackContent = FallbackUtil.getAllFlsContentTypes();
                content.addAll(fallbackContent);
                if (debug) {
                    log.info("cancelFls: content found for configured fallback types(flash): " + fallbackContent.toString() + " for deposit type: " + type);
                } 
            }
            Collection<String> reminderContent = ReminderUtil.getAllFlsReminderContent();
            if (debug) {
                log.info("cancelFls: content found for configured reminder types(fls) " +  reminderContent.toString() + " for deposit type: " + type);
            } 
            content.addAll(reminderContent); //gets all currently set reminder content in order to check service type.                          

        } else if (debug) {
            log.info("cancelFls: not configured to use service Type/content, continuing");
        }

        //service type is used to know which Specific "type" of message to Cancel, similar to replace notification.
        //so we re-use the same table. If they are defined then we can be more specific with our cancel types.
        String serviceType[] = new String[content.size()];
        String[] contentA = content.toArray (new String[content.size()]);
        for (int stl= 0;stl<serviceType.length;stl++) {
            serviceType[stl] = serviceTypeLookup.getServiceType(contentA[stl]);
            if (!serviceType.equals(smeServiceType)) {
                cancelByServiceType=true;
            }
        }

        if (debug && content.size() > 0) {
            log.info("cancelFls: Default ServiceType=[" + smeServiceType + "]");
            log.info("cancelFls: table of content to Service type for deposit type(FLS): " + type + ":" );
            for (int j=0;j<content.size();j++) {
                log.info("cancelFls: " + contentA[j] +  "\t" + serviceType[j]);
            }       
        }

        if( source.equals(smeSourceAddress) && cancelByServiceType == false) {
            if (debug) {
                log.info("cancelFls: A general cancel for FLS will be sent as both source address and service type are default." +  type);
            }
            doGeneralFLS = true;
        } else if( flsNumbers != null ) {
            for( int n=0;n<flsNumbers.length;n++) {
                SMSAddress dest = new SMSAddress(user.getTypeOfNumber(), user.getNumberingPlan(), flsNumbers[n]);
                if (content.size() > 0) {
                    for (int j=0;j<contentA.length;j++) {
                        if (serviceType[j].equals(smeServiceType)) {
                            if (debug) {
                                log.info("cancelFls: send FLS cancel for source [" + source + "] dest [" + dest + "]");
                            }
                            cm.cancel(source, dest, false, null);
                        } else {
                            if (debug) {
                                log.info("cancelFls: send cancel for source [" + source + "] dest [" + dest +  "] for content [" +  contentA[j]  + "] service Type [" + serviceType[j] + "]");
                            }
                            cm.cancel(source, dest, true, contentA[j]);
                        }
                    }
                } else {
                    cm.cancel(source, dest, false, null);
                }
            }
            if (debug) {
                log.info("FLash SMS cancel done, other deposit Types will not be checked for Flash" );
            }
        } 
        return doGeneralFLS;
    }

    /*
     * Cancel SMS notification messages.
     * Return true if should send a general Cancel as specific in at least one case is not configured.
     */
    private boolean cancelSms(NotificationFilter filter, FallbackNotificationTypes fallbackTypes, UserInfo user, depositType type, String[] smsNumbers, CancelManager cm) {
        
        boolean doGeneralSMS = false;
        boolean cancelByServiceType=false;
        
        if (debug) {
            log.info("cancelSms: has depositType " + type + " for SMS");
        }
        SMSAddress source;
        if (allowCancelbySrcAddress) {
            source = Config.getSourceAddress(type.source(), user.getCosName());

            if (debug) {
                log.info("cancelSms: using source number from table or default: " + source + " for depositType " + type);
            }     
        } else {
            source = smeSourceAddress; //just use default.
            if (debug) {
                log.info("cancelSms: using default source number: " + source + " for depositType " + type);
            }
        }
        HashSet<String> content = new HashSet<String>();
        if (allowCancelbyServiceType) { //only populate the content array if doing by serviceType as serviceType is populated based on content.
            if (debug) {
                log.info("cancelSms: gathering content for serviceType lookup for depositType " + type);
            } 
            content.addAll(filter.getTemplatesForType(type, "SMS"));
            if (debug) {
                log.info("cancelSms: content found for SMS filter: " + content.toString() + " for deposit type: " + type);
            } 
            if (content.isEmpty()) {content.add("s");} //default subject.
            if (fallbackTypes == FallbackNotificationTypes.FALLBACK_SMS || fallbackTypes == FallbackNotificationTypes.FALLBACK_FLSSMS) {
                ArrayList<String> fallbackContent = FallbackUtil.getAllSmsContentTypes();
                content.addAll(FallbackUtil.getAllSmsContentTypes());
                if (debug) {
                    log.info("cancelSms: content found for configured fallback types(sms): " + fallbackContent.toString() + " for deposit type: " + type);
                } 

            }
            Collection<String> reminderContent = ReminderUtil.getAllSmsReminderContent();
            if (debug) {
                log.info("cancelSms: content found for configured reminder types(sms) " +  reminderContent.toString() + " for deposit type: " + type);
            } 
            content.addAll(reminderContent); //gets all currently set reminder content in order to check service type.
        } else if (debug) {
            log.info("cancelSms: not configured to use service Type/content, continuing");
        }

        String serviceType[] = new String[content.size()];
        String[] contentA = content.toArray (new String[content.size()]);

        for (int stl= 0;stl<serviceType.length;stl++) {

            serviceType[stl] = serviceTypeLookup.getServiceType(contentA[stl]);

            if (!serviceType[stl].equals(smeServiceType)) {
                cancelByServiceType=true;
            }
        }

        if (debug && content.size() > 0) {
            log.info("cancelSms: default ServiceType=[" + smeServiceType + "]");
            log.info("cancelSms: table of content for SMS to Service type for deposit type: " + type + ":" );
            for (int j=0;j<content.size();j++) {
                log.info("cancelSms: " + contentA[j] +  "\t" + serviceType[j]);
            }       
        }


        if (debug) {
            log.info("cancelSms: sending cancel for all smsNumbers for all content/serviceType if configured from source address for deposit Type " +  type);
        }
        if( source.equals(smeSourceAddress) && cancelByServiceType == false) {
            if (debug) {
                log.info("cancelSms: A general cancel for SMS will be sent as both source address and service type are default." + type);
            }
            doGeneralSMS = true;
        } else if( smsNumbers != null ) {
            for( int n=0;n<smsNumbers.length;n++) {
                SMSAddress dest = new SMSAddress(user.getTypeOfNumber(), user.getNumberingPlan(), smsNumbers[n]);
                if (content.size() > 0) {
                    for (int j=0;j<content.size();j++) {
                        if (serviceType[j].equals(smeServiceType)) {
                            if (debug) {
                                log.info("cancelSms: send cancel for source [" + source + "] dest [" + dest + "]");
                            }
                            cm.cancel(source, dest, false, null);
                        } else {
                            if (debug) {
                                log.info("cancelSms: send cancel for source [" + source + "] dest [" + dest + "] for content " +  contentA[j]  + " service Type " + serviceType[j] );
                            }
                            cm.cancel(source, dest, true, contentA[j]);
                        }
                    }
                } else {
                    if (debug) {
                        log.info("cancelSms: send cancel for source [" + source + "] dest [" + dest + "]");
                    }
                    cm.cancel(source, dest, false, null);
                }
            }
        }
        
       return doGeneralSMS; 
    }
    
  //Fetch a list of Cancel SMS to send from  notifier plug-ins and process them via the cancel Manager
    private void cancelFromPlugIn(NotificationEmail email,CancelManager cm) {
        
        if (!pluginDistributor.isActive()) {
            return; //if not is use just return.
        }
        
        CancelRequestImpl request = new CancelRequestImpl(email.getReceiverPhoneNumber());
        CancelFeedBack cancelFeedBack = pluginDistributor.distributeCancel(request);              
        cancelAll(cancelFeedBack.getAll(),cm);
    }

    //Cancel a collection of cancelInfo through a cancel Manager.
    private void cancelAll(Collection<CancelInfo> cancelCollection,CancelManager cm) {
        Iterator<CancelInfo> iter = cancelCollection.iterator();
        while (iter.hasNext()) {
            SMSAddress source = smeSourceAddress;
            SMSAddress dest = null;
            CancelType cancelType;
            
            CancelInfo info = iter.next();
            dest = new SMSAddress(info.getDestination());
            cancelType = info.getCancelType();
            if (cancelType == CancelType.SOURCEONLY && allowCancelbySrcAddress == false) {
                log.error("doPluginCancel: unable to perform cancel from plug-in as plug-in wants Source Only cancel and NTF configured not to use Source for cancel.");
                log.error("doPluginCancel: " + info);
                continue;                
            } 
            if (allowCancelbySrcAddress) {
                if (debug) {
                    log.info("doPluginCancel: NTF configured for source address cancel, setting source from plug-in info:");
                }
                SMSAddressInfo src = info.getSource();
                if (src != null) {
                    source = new SMSAddress(src);
                } else {
                    source = smeSourceAddress;
                }
                    
            } else {
                source = smeSourceAddress;
            }
            
            if (debug) {
                log.info("doPluginCancel: source set to:" + source);
            }
            
            dest = new SMSAddress(info.getDestination());
            
            if (debug) {
                log.info("doPluginCancel: plug-in requesting cancel by: [" + cancelType.toString() + "]");
            }
            
            if (allowCancelbyServiceType) {
                if (debug) {
                    log.info("doPluginCancel: Check plug-in for service Type if configured, (NTF configured to use serviceType cancel)");
                }
                switch (cancelType) {
                    case CONTENT:
                        String content = info.getContent(); 
                        if (debug) {
                            log.info("doPluginCancel(serviceType): send SMS cancel for source [" + source + "] dest [" + dest + "] "+ "content: [" + content + "]");
                        }
                        cm.cancel(source, dest, true, content);
                        break;
                    case POSITION:
                        int cancelPosition=info.getCancelPosition();
                        if (debug) {
                            log.info("doPluginCancel(serviceType): send SMS cancel for source [" + source + "] dest [" + dest + "] "+ "CancelPosition: [" + cancelPosition + "]");
                        }
                        cm.cancel(source, dest, cancelPosition);
                        break;
                    case SERVICETYPE:
                        String serviceType=info.getSmppServiceType();
                        if (serviceType != null && serviceType.equals(smeServiceType)) {
                            if (debug) {
                                log.info("doPluginCancel(serviceType): send SMS cancel for source [" + source + "] dest [" + dest + "]");
                            }
                            cm.cancel(source, dest,-1);
                        } else {
                            if (debug) {
                                log.info("doPluginCancel(serviceType): send cancel for source [" + source + "] dest [" + dest + "] service Type [" + serviceType + "]");
                            }
                            cm.cancel(source, dest, serviceType);
                        }
                        break;
                    case SOURCEONLY:
                        if (debug) {
                            log.info("doPluginCancel(serviceType): send SMS cancel for source only source [" + source + "] dest [" + dest + "]");
                        }
                        cm.cancel(source, dest,-1);
                        break;

                    default:
                        log.error("doPluginCancel: Unknown CancelType: [" + cancelType +"] for " + dest );
                }
            } else {
                if (debug) {
                    log.info("doPluginCancel(NTF set no serviceType): send SMS cancel for source only source [" + source + "] dest [" + dest + "]");
                }
                cm.cancel(source, dest,-1);
            }            
        }        
    }

    private boolean userHasService(UserInfo user, depositType canceltype, String notifType) {
        
        switch(canceltype) {
            case EMAIL:
            case VIDEO:
            case VOICE:
            case FAX:
                return user.hasDeposiType(canceltype) && 
                       user.getFilter().filterMatchesMailType(canceltype,notifType);
            default:
                return false;
        }         
    }
    
    public static void refreshConfig() {
        
        
        cancelSmsEnabledForEvent = Config.getCancelSmsEnabledForEvent();
        cancelEnabled = (cancelSmsEnabledForEvent != CancelSmsEnabledForEvent.none);
        
        if (!cancelEnabled) {return;} //don;t bother loading everything if cancel not enabled.
        
        pluginDistributor = CancelSmsPluginDistributor.get();
        
        debug = Config.isCancelSmsDebug();
        
        CancelSmsMethod cancelMethod = Config.getCancelSmsMethod();
        smeServiceType=Config.getSmeServiceType();
        smeSourceAddress = Config.getSmeSourceAddress();

        switch (cancelMethod) {
            case both:
                allowCancelbySrcAddress=true;
                allowCancelbyServiceType=true;           
                break;
            case serviceTypeContent:
                allowCancelbySrcAddress=false;
                allowCancelbyServiceType=true;  
                break;
            case sourceAddress:
                allowCancelbySrcAddress=true;
                allowCancelbyServiceType=false;  
                break;
            default:
                //should never happen, assume default.
                log.warn("Unknown type in parameter Cm.cancelSmsMethod: [" + cancelMethod + "] assuming default: serviceTypeContent.");
                allowCancelbySrcAddress=true;
                allowCancelbyServiceType=false; 
        }
        
        getMwiSpecificParameters();
    }

    
    /*
     * This function pre-loads all the mwi specific parameters to save time at run-time.
     * We need to know if sending a cancel for mwi on will also cancel an mwi off
     * 
     * This will happen if we send a more general cancel where MWI off and on both
     * have the same Service Type and also the same or more general source address
     *
     * Service type for mwi can be pre-loaded as it is fixed, but for source address
     * can be COS specific so needs to be loaded at run time according to the user
     * profile.
     * 
     */
    private static void getMwiSpecificParameters() {
        
        MwiGeneralServiceType = Config.getSmeServiceTypeForMwi();
        mwiOnUsingDefaultServiceType=false;
        MwiOnOffServiceTypeMatch = false;
        GeneralSmsServiceTypeWillCancelAllMwi=false;
        
        if (allowCancelbyServiceType == true) {
            //first we figure out which mwiOn service type we will use.
            if (debug) {
                log.info("getMwiSpecificParameters: checking service table for mwiontext specific content" );
            }
            String content = "mwiontext"; //for now this can only be one.
            MwiOnserviceType = serviceTypeLookup.getServiceType(content); //this returns smeServiceType if not in table.
            if (MwiOnserviceType.equals(smeServiceType)) {
                if (debug) {
                    log.info("getMwiSpecificParameters: mwi on not using service type from replace table, checking parameter smeServiceTypeForMwi." );
                }
                //if not specific for on then get general type for MWI
                MwiOnserviceType = MwiGeneralServiceType;
                if (MwiOnserviceType.equals(smeServiceType)) {
                    if (debug) {
                        log.info("getMwiSpecificParameters: mwi on using default servicetype, as no specific type set: " + smeServiceType );
                    }
                    mwiOnUsingDefaultServiceType=true;
                   
                } else if (debug) {
                    log.info("getMwiSpecificParameters: mwi on using serviceType for general MWI (on and off): " + MwiOnserviceType );
                }
            } else if (debug) {
                log.info("getMwiSpecificParameters: mwi on using serviceType specific from replace table: " + MwiOnserviceType );
            }
            
            //now we need to figure out mwiOff serviceType in order to determine if mwiOff will be cancelled by an mwiOn serviceType.
            content = "mwiofftext"; //for now this can only be one.
            MwiOffserviceType = serviceTypeLookup.getServiceType(content); //this returns smeServiceType if not in table.
            if (MwiOffserviceType.equals(smeServiceType)) {
                if (debug) {
                    log.info("getMwiSpecificParameters:  mwi off not using service type from replace table, checking parameter smeServiceTypeForMwi." );
                }
                //if not specific for on then get general type for MWI
                MwiOffserviceType = MwiGeneralServiceType;
                if (MwiOffserviceType.equals(smeServiceType)) {
                    if (debug) {
                        log.info("getMwiSpecificParameters: mwi off using default servicetype, as no specific type set: " + smeServiceType );
                    }
                } else if (debug) {
                    log.info("getMwiSpecificParameters: mwi off using serviceType for general MWI (on and off): " + MwiOffserviceType );
                }
            } else if (debug) {
                log.info("getMwiSpecificParameters: mwi off using serviceType specific from replace table:: " + MwiOffserviceType );
            }
            
        } else {
            if (debug) {
                log.info("getMwiSpecificParameters: mwi off using default servicetype, as specified not to cancel by service Type: " + smeServiceType );
            }
            MwiOffserviceType=smeServiceType;
            MwiOnserviceType=smeServiceType;
            mwiOnUsingDefaultServiceType=true;
        }
        
        if (MwiOffserviceType.equals(MwiOnserviceType)) {
            MwiOnOffServiceTypeMatch=true; //this means cancelling an MWI specifically with just serviceType would cancel both MWI off and on.
            if (debug) {
                log.info("getMwiSpecificParameters: Both Mwi On and off use same service type.");
            }
            if (MwiOnserviceType.equals(smeServiceType)) {
                if (smeServiceType.isEmpty()) {
                    //indicates if just using ServiceType cancel or a general cancel will also cancel MWI.
                    //basically if mwiOn is the same as MWI off and both are null a general cancel using
                    //service type will cancel both.
                    GeneralSmsServiceTypeWillCancelAllMwi=true;
                }
            }            
        }
              
    }
    
    //
    /**
     * Allow another interface to Cancel an SMS using the Handlers utilities.
     * 
     * This allows a notifier plug-in to handle it's own cancel rather than
     * using NTF's cancel mechanism, if for example the plug-in overrides MWI and
     * update events.
     * 
     * This flavour used for a single cancel.
     * 
     * @param info the CancelInfo about what specifically to cancel.
     */
    public void cancel(CancelInfo info) {      
        if (info == null) {return;}
        //cancelManager is used to filter duplicates and send cancel to SMSC.
        //in this case it's redundant but part of the interface..
        CancelManager cm = new CancelManager();
        Vector<CancelInfo> cancelCollection = new Vector<CancelInfo>(1);
        cancelCollection.add(info);
        cancelAll(cancelCollection,cm);
    }

    /**
     * Allow another interface to Cancel an SMS using the Handlers utilities.
     * 
     * This allows a notifier plug-in to handle it's own cancel rather than
     * using NTF's cancel mechanism, if for example the plug-in overrides MWI and
     * update events.
     * 
     * This flavour to send a combined cancel, not sending duplicates if any.
     * 
     * @param infoSet the CancelInfo collection about what specifically to cancel.
     */
    public void cancel(Collection<CancelInfo> infoSet) {
        if (infoSet.isEmpty()) {return;}
        //cancelManager is used to filter duplicates and send cancel to SMSC.
        CancelManager cm = new CancelManager();
        cancelAll(infoSet,cm);
    }
}
