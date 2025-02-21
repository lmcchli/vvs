package com.abcxyz.services.moip.ntf.coremgmt.reminder;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.Constants.depositType;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.OdlFilterInfo;
import com.mobeon.ntf.userinfo.SIPFilterInfo;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;


/**
 * Utility methods used for the Notification Reminder feature.
 * 
 * The Notification Reminder feature sends a notification at regular intervals
 * to remind the subscriber that there are still new messages in his mailbox. 
 */
public class ReminderUtil {

    private static LogAgent log = NtfCmnLogger.getLogAgent(ReminderUtil.class);
    private static HashSet<String> allowedSmsRemindercontent =  null;
    private static HashSet<String> allowedFlsRemindercontent = null;
    //indicates that the operator has selected to force a reminder regardless of the users filter settings
    private static boolean smsReminderIgnoreFilters = false; 
    
    
    /**
     * @return true if operator has overridden filters to force a reminder.
     */
    public static boolean isSmsReminderIgnoreFilters() {
        return smsReminderIgnoreFilters;
    }

    /**
     * Retrieves the SMS filter info based on the subscriber's mailbox inventory and notification settings.
     * @param email the reminder notification email
     * @param fh feedback Handler
     * @return the notification filter info or null if no reminder SMS should be sent
     */
    public static SmsFilterInfo getReminderSmsFilterInfo(UserInfo user, NotificationEmail email, FeedbackHandler fh){
        SmsFilterInfo smsFilterInfo = null;
        Object filterInfoObject = getReminderFilterInfo(Constants.NTF_SMS, email, email.getUserMailbox(), user.getFilter(), fh);        
        if(filterInfoObject instanceof SmsFilterInfo){
            smsFilterInfo = (SmsFilterInfo)filterInfoObject;
        }
        return smsFilterInfo;
    }

    /**
     * Retrieves the FLS filter info based on the subscriber's mailbox inventory and notification settings.
     * @param email the reminder notification email
     * @return the notification filter info or null if no reminder FLS should be sent
     */
    public static SmsFilterInfo getReminderFlashFilterInfo(UserInfo user, NotificationEmail email,FeedbackHandler fh){
        SmsFilterInfo smsFilterInfo = null;
        Object filterInfoObject = getReminderFilterInfo(Constants.NTF_FLS, email, email.getUserMailbox(), user.getFilter(), fh);        
        if(filterInfoObject instanceof SmsFilterInfo){
            smsFilterInfo = (SmsFilterInfo)filterInfoObject;
        }
        return smsFilterInfo;
    }

    /**
     * Retrieves the ODL filter info based on the subscriber's mailbox inventory and notification settings.
     * @param email the reminder notification email
     * @return the notification filter info or null if no reminder ODL should be sent
     */
    public static OdlFilterInfo getReminderOdlFilterInfo(NotificationEmail email,FeedbackHandler fh){
        OdlFilterInfo odlFilterInfo = null;
        Object filterInfoObject = getReminderFilterInfo(Constants.NTF_ODL, email, fh);        
        if(filterInfoObject instanceof OdlFilterInfo){
            odlFilterInfo = (OdlFilterInfo)filterInfoObject;
        }
        return odlFilterInfo;
    }

    /**
     * Retrieves the SIP filter info based on the subscriber's mailbox inventory and notification settings.
     * @param email the reminder notification email
     * @return the notification filter info or null if no reminder SIP MWI should be sent
     */
    public static SIPFilterInfo getReminderSIPFilterInfo(NotificationEmail email,FeedbackHandler fh){
        SIPFilterInfo sipFilterInfo = null;
        Object filterInfoObject = getReminderFilterInfo(Constants.NTF_SIPMWI, email, fh);        
        if(filterInfoObject instanceof SIPFilterInfo){ 
            sipFilterInfo = (SIPFilterInfo)filterInfoObject;
        }
        return sipFilterInfo;
    }
    
    /**
     * Retrieves the filter info for a specific notification type based on the subscriber's mailbox inventory and notification settings.
     * @param notifType the reminder notification type
     * @param email the reminder notification email
     * @return the notification filter info or null if the notification type should not be sent
     */
    public static Object getReminderFilterInfo(int notifType, NotificationEmail email,FeedbackHandler fh){
        NtfEvent reminderEvent = email.getNtfEvent();
        UserInfo userInfo =  UserFactory.findUserByTelephoneNumber(reminderEvent.getRecipient());
        if (userInfo != null) {
            return getReminderFilterInfo(notifType, email, email.getUserMailbox(), userInfo.getFilter(), fh);
        } else {
            return null;
        }
    }

    /**
     * Retrieves the filter info for a specific notification type based on the subscriber's mailbox inventory and notification settings.
     * @param notifType the reminder notification type
     * @param email the reminder notification email
     * @param inbox the subscriber's mailbox
     * @param notifFilter the subscriber's notification filter
     * @param fh feedbackHandler
     * @return the notification filter info or null if the notification type should not be sent
     */
    public static Object getReminderFilterInfo(int notifType, NotificationEmail email, UserMailbox inbox, NotificationFilter notifFilter, FeedbackHandler fh){
        Object filterInfo = null;
        if(inbox.getNewTotalCount() < 1){
            log.debug("ReminderUtil.getReminderFilterInfo: No new messages in subscriber mailbox:" + email.getReceiver());
        } else {
            GregorianCalendar receivedDate = new GregorianCalendar();

            if(inbox.getNewVoiceCount() > 0){
                email.setDepositType(depositType.VOICE);
                log.debug("ReminderUtil.getReminderFilterInfo: Try to get filter info for subscriber:" + email.getReceiver() + " notifType:" + notifType
                        + " messageType:voice");
                filterInfo = getReminderFilterInfo(notifType, email, notifFilter, receivedDate, fh);
            }

            if(filterInfo == null){
                if(inbox.getNewEmailCount() > 0){
                    email.setDepositType(depositType.EMAIL);
                    log.debug("ReminderUtil.getReminderFilterInfo: Try to get filter info for subscriber:" + email.getReceiver() + " notifType:" + notifType
                            + " messageType:email");
                    filterInfo = getReminderFilterInfo(notifType, email, notifFilter, receivedDate, fh);
                }

                if(filterInfo == null){
                    if(inbox.getNewFaxCount() > 0){
                        email.setDepositType(depositType.FAX);
                        log.debug("ReminderUtil.getReminderFilterInfo: Try to get filter info for subscriber:" + email.getReceiver() + " notifType:" + notifType
                                + " messageType:fax");
                        filterInfo = getReminderFilterInfo(notifType, email, notifFilter, receivedDate, fh);
                    }

                    if(filterInfo == null){
                        if(inbox.getNewVideoCount() > 0){
                            email.setDepositType(depositType.VIDEO);
                            log.debug("ReminderUtil.getReminderFilterInfo: Try to get filter info for subscriber:" + email.getReceiver() + " notifType:" + notifType
                                    + " messageType:video");
                            filterInfo = getReminderFilterInfo(notifType, email, notifFilter, receivedDate, fh);
                        }
                    }
                }
            }
            
            if(filterInfo == null){
                log.debug("ReminderUtil.getReminderFilterInfo: No filter info found for subscriber:" + email.getReceiver() + " notifType:" + notifType);
            }
            else{
                log.debug("ReminderUtil.getReminderFilterInfo: Filter info found for subscriber:" + email.getReceiver() + " notifType:" + notifType);
            }
        }

        return filterInfo;
    }
    
    private static Object getReminderFilterInfo(int notifType, NotificationEmail email, NotificationFilter notifFilter, Calendar receivedDate,FeedbackHandler fh){
        Object filterInfo = null;
        switch(notifType){
            case Constants.NTF_SMS:
	    case Constants.NTF_MWI:
                filterInfo = notifFilter.getSmsFilterInfo(email, receivedDate, fh);
                break;
            case Constants.NTF_FLS:
                filterInfo = notifFilter.getFlashFilterInfo(email, receivedDate, fh);
                break;
            case Constants.NTF_MMS:
                filterInfo = notifFilter.getMmsFilterInfo(email, receivedDate, fh);
                break;
            case Constants.NTF_EML:
                filterInfo = notifFilter.getEmailFilterInfo(email, receivedDate, fh);
                break;
            case Constants.NTF_SIPMWI:
                filterInfo = notifFilter.getSIPFilterInfo(email, receivedDate, fh);
                break;
            case Constants.NTF_ODL:
                filterInfo = notifFilter.getOdlFilterInfo(email, receivedDate, fh);
                break;
        }
        
        return filterInfo;
    }
    
    /**
     * Returns the SMS content to be used for all configured SMS Reminders.
     * @return the SMS content
     */
    public static Collection<String> getAllSmsReminderContent() { 
       
        if (allowedSmsRemindercontent != null) {
            return allowedSmsRemindercontent;
        }
        
        allowedSmsRemindercontent = new HashSet<String>();
        if (!(Config.isSmsReminderEnabled() || (Config.isOutdialReminderEnabled() && Config.getOutdialReminderType().equals("flssms"))))
        {
            //switched off so return an empty set.
            allowedSmsRemindercontent = new HashSet<String>();
            return allowedSmsRemindercontent; 
        }
        
        //depending on allowed type return a set of content that could be sent. 
        String AllowedTypes = Config.getSmsReminderAllowedType();
        
        if (AllowedTypes.equalsIgnoreCase(Constants.notifTypeAbbrev[Constants.NTF_FLSSMS])) {
            allowedSmsRemindercontent.add(Config.getSmsReminderContent());
        } else if (AllowedTypes.equalsIgnoreCase(Constants.notifTypeAbbrev[Constants.NTF_SMS])) {
            allowedSmsRemindercontent.add(Config.getSmsReminderContent());
        } else {
            //ignore
        }
        
      return allowedSmsRemindercontent;
    }
    
    /**
     * Returns the SMS content to be used for all configured SMS Reminders.
     * @return the SMS content
     */
    public static Collection<String> getAllFlsReminderContent() { 
       
        if (allowedFlsRemindercontent != null) {
            return allowedFlsRemindercontent;
        }
        
        allowedFlsRemindercontent = new HashSet<String>();
        if (!(Config.isSmsReminderEnabled() || (Config.isOutdialReminderEnabled() && Config.getOutdialReminderType().equals("flssms"))))
        {
            //switched off so return an empty set.
            allowedFlsRemindercontent = new HashSet<String>();
            return allowedFlsRemindercontent; 
        }
        
        //depending on allowed type return a set of content that could be sent. 
        String AllowedTypes = Config.getSmsReminderAllowedType();
        
        if (AllowedTypes.equalsIgnoreCase(Constants.notifTypeAbbrev[Constants.NTF_FLSSMS])) {
            allowedFlsRemindercontent.add(Config.getFlsReminderContent());
        } else if (AllowedTypes.equalsIgnoreCase(Constants.notifTypeAbbrev[Constants.NTF_FLS])) {
            allowedFlsRemindercontent.add(Config.getFlsReminderContent());
        } else {
            //ignore
        }
        
      return allowedFlsRemindercontent;
    }
    
    public static void refreshConfig() {
        allowedSmsRemindercontent = null; 
        allowedFlsRemindercontent = null;
        getAllSmsReminderContent(); //refresh the value..
        getAllFlsReminderContent(); //refresh the value..
        smsReminderIgnoreFilters = Config.getSmsReminderIgnoreFilters();
    }

    /**
     * @return true if operator has overridden filters and FLS reminders are configured.
     */
    public static boolean isForcedFLSReminder() {
        return (smsReminderIgnoreFilters && !allowedFlsRemindercontent.isEmpty());
    }

    /**
     * @return true if operator has overridden filters and SMS reminders are configured.
     */
    public static boolean isForcedSMSReminder() {
        return (smsReminderIgnoreFilters && !allowedSmsRemindercontent.isEmpty());
    }
}
