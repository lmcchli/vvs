/**
 * Copyright (c) 2010 Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt.fallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackEvent.FallbackNotificationTypes;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.Constants.depositType;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.NotificationHandler;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;

public class FallbackUtil {

    private static LogAgent log = NtfCmnLogger.getLogAgent(FallbackUtil.class);
    
    public static final int[] NotificationsWithAllowedFallback = {
        Constants.NTF_SMS,
        Constants.NTF_SIPMWI,
        Constants.NTF_FLS,
        Constants.NTF_ODL,
        Constants.NTF_VVM,
        Constants.NTF_EML,
        Constants.NTF_MMS
        };

    /**
     * Method to retrieve the fall back notification based on the original notification type from the configuration.
     * @param originalNotificationType originalNotificationType
     * @return FallbackNotificationTypes
     */
    public static FallbackNotificationTypes getFallbackNotificationType(int originalNotificationType) {

        String configuredFallbackName = null;

        switch (originalNotificationType) {
            case Constants.NTF_SMS:
                configuredFallbackName = Config.getFallbackSms();
                break;
            case Constants.NTF_FLS:
                //for now this is considered an SMS - but maybe allow fall-back to SMS in the future.
                configuredFallbackName = Config.getFallbackSms();
            case Constants.NTF_FLSSMS:
                //this is considered an SMS.
                configuredFallbackName = Config.getFallbackSms();
            case Constants.NTF_MMS:
                configuredFallbackName = Config.getFallbackMms();
                break;
            case Constants.NTF_ODL:
                configuredFallbackName = Config.getFallbackOutdial();
                break;
            case Constants.NTF_SIPMWI:
                configuredFallbackName = Config.getFallbackSipMwi();
                break;
            case Constants.NTF_VVM:
                configuredFallbackName = Config.getFallbackVvm();
                break;
            default:
                log.error("Notification type " + Constants.notifTypeStrings[originalNotificationType] + " not supported by fallback.");
                return FallbackNotificationTypes.FALLBACK_NONE;
        }

        return getfallbackType(configuredFallbackName);

    }

    public static FallbackNotificationTypes getfallbackType(String fallBackName) {
        FallbackNotificationTypes fallbackNotification = FallbackNotificationTypes.FALLBACK_NONE;

        Iterator<FallbackNotificationTypes> it = Arrays.asList(FallbackNotificationTypes.values()).iterator();
        while (it.hasNext()) {
            FallbackNotificationTypes currentFallbackNotificationType = it.next();
            if (currentFallbackNotificationType.getConfiguredName().equalsIgnoreCase(fallBackName)) {
                fallbackNotification = currentFallbackNotificationType;
                break;
            }
        }
        return fallbackNotification;
    }
    
    private static volatile FallbackNotificationTypes smsorFlsFallbackUsed = null;
    private static volatile ArrayList<String> usedSMSContentTypes = null;
    private static volatile ArrayList<String> usedFLSContentTypes = null;
    
    /*
     * returns the type(s) of SMS fall-backs currently used in the system. 
     * can be SMS,FLS,or FLSSMS (both).
     * Other types are not considered SMS types.
     * This is only read at start and configuration refresh for efficiency.
     * configured to send an SMS type.
     */
    public static synchronized FallbackNotificationTypes getUsedSMSFallbackTypes () {
        
        if (smsorFlsFallbackUsed != null) {
            return smsorFlsFallbackUsed;
        }
               
        for (int i=0; i < NotificationsWithAllowedFallback.length; i++) {
            FallbackNotificationTypes fallbackType=getFallbackNotificationType(NotificationsWithAllowedFallback[i]);
            switch (fallbackType) {
                case FALLBACK_SMS:
                    if (smsorFlsFallbackUsed == null) {
                        smsorFlsFallbackUsed = fallbackType;
                    } else {
                        if (smsorFlsFallbackUsed == FallbackNotificationTypes.FALLBACK_FLS) {
                            smsorFlsFallbackUsed = FallbackNotificationTypes.FALLBACK_FLSSMS;
                            return (smsorFlsFallbackUsed);
                        }
                    }
                    break;
                case FALLBACK_FLS:
                    if (smsorFlsFallbackUsed == null) {
                        smsorFlsFallbackUsed = fallbackType;
                    } else {
                        if (smsorFlsFallbackUsed == FallbackNotificationTypes.FALLBACK_SMS) {
                            smsorFlsFallbackUsed = FallbackNotificationTypes.FALLBACK_FLSSMS;
                            return (smsorFlsFallbackUsed);
                        }
                    }
                    break;
                case FALLBACK_FLSSMS:
                    smsorFlsFallbackUsed = fallbackType;
                    return fallbackType;
            }
        }
        //did not find any set to false and return.
        smsorFlsFallbackUsed = FallbackNotificationTypes.FALLBACK_NONE;
        return FallbackNotificationTypes.FALLBACK_NONE;        
    }
    
    
    public static synchronized ArrayList<String> getAllSmsContentTypes () {
        
        if (usedSMSContentTypes != null) {
            return usedSMSContentTypes;
        }
        
        HashSet<String> content = new HashSet<String>();
               
        for (int i=0; i < NotificationsWithAllowedFallback.length; i++) {
            FallbackNotificationTypes fallbackType=getFallbackNotificationType(NotificationsWithAllowedFallback[i]);
            switch (fallbackType) {
                case FALLBACK_SMS:
                case FALLBACK_FLSSMS:
                    content.addAll(getContent(NotificationsWithAllowedFallback[i],FallbackNotificationTypes.FALLBACK_SMS));
                break;
                default:
            }
        }
        usedSMSContentTypes = new ArrayList<String>(content);
        return usedSMSContentTypes;        
    }
    
    
    public static synchronized ArrayList<String> getAllFlsContentTypes() {
        
        if (usedFLSContentTypes != null) {
            return usedFLSContentTypes;
        }
        
        HashSet<String> content = new HashSet<String>();
               
        for (int i=0; i < NotificationsWithAllowedFallback.length; i++) {
            FallbackNotificationTypes fallbackType=getFallbackNotificationType(NotificationsWithAllowedFallback[i]);
            switch (fallbackType) {
                case FALLBACK_FLS:
                case FALLBACK_FLSSMS:
                    content.addAll(getContent(NotificationsWithAllowedFallback[i],FallbackNotificationTypes.FALLBACK_FLS));
                break;
                default:
            }
        }
        usedFLSContentTypes = new ArrayList<String>(content);
        return usedFLSContentTypes;        
    }
    
    
    
    private static Collection<String> getContent(int originalNotificationType,FallbackNotificationTypes type) {

        HashSet<String> content = new HashSet<String>();
          
        switch (originalNotificationType) {
            case Constants.NTF_SMS:
                //no fall-back from SMS to FLS.
                break;
            case Constants.NTF_FLS:
                //for now no fall-back to an SMS for now
                break;
            case Constants.NTF_FLSSMS:
                //cannot fall-back to self.
                break;
            case Constants.NTF_MMS:
              if (type == FallbackNotificationTypes.FALLBACK_SMS) { //if fallback sms enabled..
              //note: these are set by the MMS level 3 handler to fall-back to a particular content for specific error types.
                  content.add("mms-msg-too-big");
                  content.add("mms-msg-error");
              }
              break;
            case Constants.NTF_ODL:
                if (Config.getDoOutdial()) {
                    switch (type) {
                        case FALLBACK_FLS:
                            content.add(getFallbackContent(originalNotificationType, false ,Constants.NTF_FLS));
                            break;
                        case FALLBACK_SMS:
                            content.add(getFallbackContent(originalNotificationType, false ,Constants.NTF_SMS));
                            break;
                        case FALLBACK_FLSSMS:
                            content.add(getFallbackContent(originalNotificationType, false ,Constants.NTF_SMS));
                            content.add(getFallbackContent(originalNotificationType, false ,Constants.NTF_FLS));
                            break;
                    }   
                }
                break;
            case Constants.NTF_MWI:
                if (type == FallbackNotificationTypes.FALLBACK_SMS) {
                    content.add(getFallbackContent(originalNotificationType, false ,Constants.NTF_SMS));
                }
                break;
            case Constants.NTF_VVM:
                if (type == FallbackNotificationTypes.FALLBACK_SMS) {
                    content.add(getFallbackContent(originalNotificationType, false ,Constants.NTF_SMS));
                }
                break;
            default:
                
        }

        return content;

    }

    /**
     * Performs the fall back retry for notification type that have level-2 scheduling.
     * For now, only fall back to SMS/FLS is handled.
     * (Ideally, all notification types would be independent and be scheduled at level-3.
     * An improvement might be to have the fallbackHandler schedule a retry instead of coming through here from the
     * level 2 event handler.)
     * You should never call this to start an SMS/FLS fall back (use the FallBackHandler class.
     * If your type does not exist, add it..
     * 
     * @param user the user information
     * @param ng collects results for all notifications from this mail.
     */
    public static void doLevelTwoRetryScheduledFallback(UserInfo user, NotificationGroup ng) { 

        NotificationEmail email = ng.getEmail();
        NtfEvent event = email.getNtfEvent();

        if (!event.isFallback()) {
            log.error("doLevelTwoScheduledFallback() Invalid fallback, event is not a fallback: " + email.getNtfEvent().toString());
            return;
        }
        
        /**
         * Regardless the msgInfo that might have been received (could be null or not),
         * find the first unread message for the given subscriber
         */
        UserMailbox userMailbox = email.getUserMailbox(event.getRecipient());
        MessageInfo messageInfo = userMailbox.getFirstNewMessageInfo();
        if (messageInfo != null) {
            event.setMsgInfo(messageInfo);
        } else {
            log.info("Subscriber " + event.getRecipient() + " has no new messages when retry");
            return;
        }

   
        FallbackNotificationTypes fn = FallbackNotificationTypes.FALLBACK_NONE;

        String fallBackName=event.getFallBackName();

        if (fallBackName != null) {
            fn = getfallbackType(fallBackName);
        } else {
            /* Fetch from configuration.
             * This should only happen for old fall-backs before upgrade after VFENL fall back improvements.
             * Normal case is to use the properly set by fallbackType in FallBackHandler/Worker.
             */
            fn = getFallbackNotificationType(event.getFallbackOriginalNotificationType());
        }       
        switch(fn) {
            case FALLBACK_SMS:
                fallbackToSms(user, email, event,ng);
                break;
            case FALLBACK_FLS:
                fallbackToFls(user, email, event,ng);
                break;
            case FALLBACK_FLSSMS:
                //This is just to cover old events before upgrade to new capabilities.
                //we don't really know what it was so assume old SMS
                fallbackToSmsFls(user, email, event,ng);
                break;
            default:
                //we should never get here as these type of events are not lvl2 events.
                log.info("Invalid level 2 retry event recieved, maybe pre-upgrade, assuming was an SMS fallback: " + event.toString());
                fallbackToSms(user, email, event, ng);                                  
        }

        ng.noMoreUsers();
        return;

    }


    /**
     * Returns the SMS content to be used for fall back.
     * @param originalNotificationType notification type which failed leading to the current fall back
     * @return the SMS content
     */
    public static String getFallbackContent(int originalNotificationType,boolean isReminder, int fallbackType) {
        String content = null;
        switch (originalNotificationType) {
            case Constants.NTF_ODL:
                if (fallbackType == Constants.NTF_FLS) {
                    content = Config.getFallbackOutdialToFlsContent();
                }
                else {
                    content = Config.getFallbackOutdialToSmsContent();
                }
                break;
            case Constants.NTF_SIPMWI:
                content = Config.getFallbackSipMwiToSmsContent();
                break;
            case Constants.NTF_VVM:
                content = Config.getFallbackVvmToSmsContent();
                break;
            default:
                log.warn("No SMS content configured for notification type " + Constants.notifTypeStrings[originalNotificationType] + ", template 'c' will be used.");
                content = "c";
                break;
        }
        return content;
    }

    private static boolean fallbackToFls(UserInfo user, NotificationEmail email, NtfEvent event ,NotificationGroup ng) {
        
        if (Config.getFallbackFlashUrgentOnly() == true && !(email.isUrgent() || FallbackUtil.areUgentMails(user))) {
            log.debug("fallbackToFls() Subscriber " + email.getReceiver() + " has no urgent messages, dropping FLS notification.");
            ng.increaseTempCount(user, 1);
            ng.ok(user,Constants.NTF_FLS); ///don't send but indicate OK so scheduler will get cancelled
            return false;
        }
        
        String[] flsNumbers = user.getFilter().getNotifNumbers(FallbackNotificationTypes.FALLBACK_FLS.getProfile(), 0, email);

        if (flsNumbers != null) {
            Properties props = new Properties();

            props.setProperty("FLS", getFallbackContent(event.getFallbackOriginalNotificationType(),email.isReminderNotification(),Constants.NTF_FLS));

            SmsFilterInfo filterInfo = new SmsFilterInfo(props, null, null, flsNumbers);

            SMSAddress source = NotificationHandler.getSourceAddressEmail(email, user.getCosName());

            int count = SMSOut.get().handleSMS(user, filterInfo, ng, email, email.getUserMailbox(), source, user.getNotifExpTime(), 0);
            ng.setOutCount(user, count);
            return true;

        } else {
            log.error("Delivery numbers not found for subscriber " + email.getReceiver() + "; fallback " + FallbackNotificationTypes.FALLBACK_FLS.getName() + " will not be invoked.");
            return false;
        }        
    }

    private static void fallbackToSms(UserInfo user, NotificationEmail email, NtfEvent event, NotificationGroup ng) {
        String[] smsNumbers = user.getFilter().getNotifNumbers(FallbackNotificationTypes.FALLBACK_SMS.getProfile(), 0, email);

        if (smsNumbers != null) {
            Properties props = new Properties();

            props.setProperty("SMS", getFallbackContent(event.getFallbackOriginalNotificationType(),email.isReminderNotification(),Constants.NTF_SMS));

            SmsFilterInfo filterInfo = new SmsFilterInfo(props, smsNumbers, null, null);

            SMSAddress source = NotificationHandler.getSourceAddressEmail(email, user.getCosName());

            int count = SMSOut.get().handleSMS(user, filterInfo, ng, email, email.getUserMailbox(), source, user.getNotifExpTime(), 0);
            ng.setOutCount(user, count);

        } else {
            log.error("Delivery numbers not found for subscriber " + email.getReceiver() + "; fallback " + FallbackNotificationTypes.FALLBACK_SMS.getName() + " will not be invoked.");
        }
    }


    private static void fallbackToSmsFls(UserInfo user, NotificationEmail email, NtfEvent event, NotificationGroup ng) {
        boolean result = fallbackToFls(user,email,event,ng);

        if (result == true && (Config.getFallbackFlashUrgentOnly() == true) && (Config.getFallbackSmsOnUrgentIfFlsSent() == false))
        {
            //In this case we drop the SMS just send a flash only.
            //we are unlikely to get here though as this most likely has been decided at the initial try unless a change in config.
            log.debug("fallbackToSmsFls() Subscriber " + email.getReceiver() + " sent an FLS for urgent, and configured not to send SMS, dropping SMS");
            return;
        } else {
            fallbackToSms(user,email,event,ng);
        }
    }


    public static boolean areUgentMails(UserInfo userInfo) {
        String msid = userInfo.getMsid();
        MSA msa;
        if(msid != null) {
            msa = new MSA(msid);
        } else {
            return false;
        }

        UserMailbox inbox = new UserMailbox(msa,
                userInfo.hasDeposiType(depositType.EMAIL),
                userInfo.hasDeposiType(depositType.FAX),
                userInfo.hasDeposiType(depositType.VOICE),
                userInfo.hasDeposiType(depositType.VIDEO));
        return (inbox.getNewUrgentTotalCount() > 0);
    }

    public static void refreshConfig() {
        smsorFlsFallbackUsed = null; 
        usedSMSContentTypes = null;
        usedFLSContentTypes  = null;
        getUsedSMSFallbackTypes(); //refresh the value..
        getAllSmsContentTypes(); //refresh the value.
        getAllFlsContentTypes(); //refresh the vaslue
    }
}
