package com.abcxyz.services.moip.ntf.coremgmt.reminder;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryHandling;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.Constants.depositType;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.NotificationHandler;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.out.FeedbackHandlerImpl;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * Initiates and handles SMS reminder notifications as a part of the Notification Reminder feature. 
 * The Notification Reminder feature sends a notification at regular intervals to remind the subscriber that there are still new messages in his mailbox. 
 */
public class SmsReminder {
    
    //Constants used for persistence
    protected static final String SMS_STATUS_FILE = "sms.status";
    protected static final String REMINDER_TRIGGER_EVENT_ID = "remindertriggereventid";
    protected static final String REMINDER_NOTIFICATION_EVENT_ID = "remindernotifeventid";

    private static LogAgent log = NtfCmnLogger.getLogAgent(SmsReminder.class);
    
    //First call to this class is from NtfRetryEventHandler constructor; 
    //so, initialise retryHandler later when NtfRetryEventHandler has been created and is in the NtfEventHandlerRegistry.
    protected static NtfRetryHandling retryHandler = null; 
    private static IMfsEventManager mfsEventManager = MfsEventFactory.getMfsEvenManager();
    
    
    /**
     * Creates the RetryEventInfo for reminder triggers according to the configuration settings.
     * @return RetryEventInfo  retry event info for reminder trigger
     */
    public static RetryEventInfo getReminderTriggerRetryEventInfo(){
        //In the case SMS reminder feature was on then turned off, we might still get previously scheduled reminders when the feature is off.  
        //So for robustness, always instantiate reminder event handler so that any reminders can be cancelled even if the feature is off.
        long reminderIntervalInMinute = Config.getSmsReminderIntervalInMin();
        String reminderRetrySchema = reminderIntervalInMinute + " CONTINUE";
        long reminderExpiryInMinute = Config.getSmsReminderExpireInMin();
        RetryEventInfo reminderRetryInfo =  new RetryEventInfo(NtfEventTypes.SMS_REMINDER.getName(), reminderRetrySchema, reminderExpiryInMinute);
        return reminderRetryInfo;
    }

    public static boolean shouldProcessFiredReminderEvent(NtfEvent ntfEvent, AppliEventInfo eventInfo) {
        boolean shouldProcess = true;
        if (ntfEvent == null || eventInfo == null) {
            shouldProcess = false;
        } else {
            String persistedEventId = null;
            if (ntfEvent.isEventServiceName(NtfEventTypes.SMS_REMINDER.getName())) {
                persistedEventId = retrievePersistentProperty(ntfEvent.getRecipient(), REMINDER_TRIGGER_EVENT_ID);
            } else {
                persistedEventId = retrievePersistentProperty(ntfEvent.getRecipient(), REMINDER_NOTIFICATION_EVENT_ID);
            }

            shouldProcess = CommonMessagingAccess.getInstance().compareEventIds(eventInfo, persistedEventId);
            if (!shouldProcess) {
                log.info("EventFired: EventIds not matching: firedEvent: " + eventInfo.getEventId() + ", storedEvent: " + persistedEventId + ", stop retry");
            }
        }
        return shouldProcess;
    }

    /**
     * Triggers the SMS reminder notification.
     * @param user the user information
     * @param ng collects results for all notifications resulting from this trigger.
     */
    public static void triggerReminderNotification(UserInfo user, NotificationGroup ng) { 
        log.debug("triggerReminderNotification: Reminder, after configured delay.");
        if(retryHandler == null){
            retryHandler = NtfEventHandlerRegistry.getEventHandler();
        }
        NotificationEmail email = ng.getEmail();
        String mailboxTel = email.getReceiver();

        if(Config.isSmsReminderEnabled() && user.hasUnreadMessageReminder()) { 
            UserMailbox mailbox = email.getUserMailbox();
            
            if (mailbox.getNewTotalCount() == 0) {
                log.debug("triggerReminderNotification: No need for SMS reminder notification because no more unread messages for " + mailboxTel);
                cancelReminderTrigger(email);
                return;
            }
                        
            //Determine what types are allowed.
            String ReminderType = Config.getSmsReminderAllowedType();
            boolean allowSMS = false;
            boolean allowFLS = false;
            if (ReminderType.equalsIgnoreCase(Constants.notifTypeAbbrev[Constants.NTF_FLSSMS])) {
                allowSMS=true;
                allowFLS=true;
            } else if (ReminderType.equalsIgnoreCase(Constants.notifTypeAbbrev[Constants.NTF_SMS])) {
                allowSMS=true;
            } else { //for now only other type allowed.
                allowFLS=true;
            }

            String existingBackupReminderNotifEventId = null;
            String newBackupReminderNotifEventId = null;
            
            SmsFilterInfo smsFilterInfo = null;
            SmsFilterInfo flsFilterInfo = null;

            if (Config.getSmsReminderIgnoreFilters()) {

                // Send SMS and/or FLS reminder(s), regardless of provisioning filters or disablings
                if (allowSMS) {
                    String[] smsNumbers = user.getFilter().getMatchingDeliveryProfileNumbers("SMS", 0, false);
                    if (smsNumbers == null) {
                        String notifNumber = user.getNotifNumber();
                        if (notifNumber != null && !notifNumber.isEmpty())
                            smsNumbers = new String[] { notifNumber };
                        else
                            smsNumbers = new String[] { email.getReceiverPhoneNumber() };
                    }

                    Properties filterProp = new Properties();
                    filterProp.put("SMS", "");

                    smsFilterInfo = new SmsFilterInfo(filterProp, smsNumbers, null, null);
                    log.debug("triggerReminderNotification: smsReminderIgnoreFilters is set; SMS reminder notification will be sent");
                }
                
                if (allowFLS) {
                    String[] flashNumbers = user.getFilter().getMatchingDeliveryProfileNumbers("FLS", 0, false);
                    if( flashNumbers == null ) {
                        String notifNumber = user.getNotifNumber();
                        if ( notifNumber != null  && !notifNumber.isEmpty() )
                            flashNumbers = new String[] { notifNumber };
                        else
                            flashNumbers = new String[] {email.getReceiverPhoneNumber()};
                    }
                    
                    Properties filterProp = new Properties();
                    filterProp.put("FLS", "");

                    flsFilterInfo = new SmsFilterInfo(filterProp, null, null, flashNumbers);
                    log.debug("triggerReminderNotification: smsReminderIgnoreFilters is set; FLS reminder notification will be sent");
                }

            } else {

                boolean hasNewUrgent = (mailbox.getNewUrgentVoiceCount() == 0? false : true);
                
                if (hasNewUrgent) {
                    email.setUrgentReminder();
                } 
                
                FeedbackHandlerImpl fh = new FeedbackHandlerImpl();
                if (  allowFLS == true ) {
                    flsFilterInfo = ReminderUtil.getReminderFlashFilterInfo(user, email, fh);
                    if (flsFilterInfo == null && fh.getStatus() == Constants.FEEDBACK_STATUS_OK) {
                        if (Config.getSmsReminderTrySmsOnFLSDisabled()) {
                           log.debug("triggerReminderNotification: FLS is disabled, will try SMS as configured to:  " + NotificationConfigConstants.SMS_REMINDER_TRY_SMS_ON_FLS_DISABLED);
                           allowSMS = true;
                        }
                    }
                }
                
                if (fh.getStatus() == Constants.FEEDBACK_STATUS_OK && allowSMS == true) {      
                    smsFilterInfo = ReminderUtil.getReminderSmsFilterInfo(user, email, fh); 
                }
                switch (fh.getStatus()) { //deal with problems relating to roaming checks if set.
                    case Constants.FEEDBACK_STATUS_OK:
                        break;
                    case Constants.FEEDBACK_STATUS_FAILED:
                        log.warn("triggerReminderNotification: failed due to roaming check failure, backup already scheduled, failed for this reminder: " + mailboxTel);
                        ng.increaseTempCount(user, 1);
                        //We do retry here and let the next reminder kick in, if we fail it will kill all reminders going forward
                        ng.retry(user, Constants.NTF_SMS, "This reminder, will remind again on Reminder interval unless expired.");
                        return; // let the next reminder try if scheduled.
                    case Constants.FEEDBACK_STATUS_RETRY:
                    case Constants.FEEDBACK_STATUS_UNKNOWN:
                        //In case there is still a previous reminder notification retry scheduled from the last reminder trigger event fired,
                        //cancel it after scheduling new reminder notification.
                        existingBackupReminderNotifEventId = retrievePersistentProperty(mailboxTel, REMINDER_NOTIFICATION_EVENT_ID);            
                        newBackupReminderNotifEventId = retryHandler.scheduleEvent(email.getNtfEvent());
                        updatePersistentProperty(mailboxTel, REMINDER_NOTIFICATION_EVENT_ID, newBackupReminderNotifEventId);
                        email.getNtfEvent().keepReferenceID(newBackupReminderNotifEventId);            
                        log.debug("triggerReminderNotification: Retry due to roaming error for: " + mailboxTel);
                        retryHandler.cancelEvent(existingBackupReminderNotifEventId);
                        ng.increaseTempCount(user, 1);
                        ng.retry(user,Constants.NTF_SMS, "Reminder retry due to roaming check error.");
                        return;
                  default:
                        log.debug("triggerReminderNotification: Unexpected feedback type, assuming ok: " + fh.getStatus() );         
                      break;
                }

            }
            
            
            if (smsFilterInfo != null || flsFilterInfo != null) {
                updatePersistentProperty(mailboxTel, REMINDER_TRIGGER_EVENT_ID, email.getNtfEvent().getReferenceId());
                //In case there is still a previous reminder notification retry scheduled from the last reminder trigger event fired,
                //cancel it after scheduling new reminder notification.
                existingBackupReminderNotifEventId = retrievePersistentProperty(mailboxTel, REMINDER_NOTIFICATION_EVENT_ID);            
                newBackupReminderNotifEventId = retryHandler.scheduleEvent(email.getNtfEvent());
                updatePersistentProperty(mailboxTel, REMINDER_NOTIFICATION_EVENT_ID, newBackupReminderNotifEventId);
                email.getNtfEvent().keepReferenceID(newBackupReminderNotifEventId);            
                log.debug("triggerReminderNotification: Created backup for current reminder notification: " + newBackupReminderNotifEventId);

                retryHandler.cancelEvent(existingBackupReminderNotifEventId);
                log.debug("triggerReminderNotification: Cancelled reminder notification backup from previous reminder trigger for " + mailboxTel + ": " + existingBackupReminderNotifEventId);

                if (smsFilterInfo != null) {
                    sendSMS(user, ng, smsFilterInfo);
                    log.debug("triggerReminderNotification: SMS reminder notification initiated for " + mailboxTel);
                }

                if (flsFilterInfo != null) {
                    sendSMS(user, ng, flsFilterInfo);
                    log.debug("triggerReminderNotification: FLASH SMS reminder notification initiated for " + mailboxTel);
                }
                
            } else {
                log.debug("triggerReminderNotification: No need for SMS reminder notification because SMS and FLASH SMS are not the notification type configured for new messages, or currently disabled, cancelling SMS reminder for " + mailboxTel);
                cancelReminderTrigger(email);  
            }
        }
        else {
            log.debug("triggerReminderNotification: SMS Notification Reminder feature now disabled; cancelling previously scheduled SMS reminder for " + mailboxTel);
            cancelReminderTrigger(email);              
        }
    }

    /**
     * Sends the SMS reminder notification retry.
     * @param user the user information
     * @param ng collects results for all notifications resulting from this trigger.
     */
    public static void sendReminderNotificationRetry(UserInfo user, NotificationGroup ng) { 
        log.debug("sendReminderNotificationRetry: retry of a reminder");
        if(retryHandler == null){
            retryHandler = NtfEventHandlerRegistry.getEventHandler();
        }
        NotificationEmail email = ng.getEmail();
        String mailboxTel = email.getReceiver();
            
           
        if(Config.isSmsReminderEnabled() && user.hasUnreadMessageReminder()) { 
            UserMailbox mailbox = email.getUserMailbox();
            if (mailbox.getNewTotalCount() == 0) {
                log.debug("sendReminderNotificationRetry: No need for SMS reminder notification because no more unread messages for " + mailboxTel);
                cancelReminderTrigger(email);
                return;
            }
                     
            //Determine what types are allowed.
            String ReminderType = Config.getSmsReminderAllowedType();
            boolean allowSMS = false;
            boolean allowFLS = false;
            if (ReminderType.equalsIgnoreCase(Constants.notifTypeAbbrev[Constants.NTF_FLSSMS])) {
                allowSMS=true;
                allowFLS=true;
            } else if (ReminderType.equalsIgnoreCase(Constants.notifTypeAbbrev[Constants.NTF_SMS])) {
                allowSMS=true;
            } else { //for now only other type allowed.
                allowFLS=true;
            }
                     
            SmsFilterInfo smsFilterInfo = null;
            SmsFilterInfo flsFilterInfo = null;
            
            if (ReminderUtil.isSmsReminderIgnoreFilters()) {
                
                // Send SMS and/or FLS reminder(s), regardless of provisioning filters or disablings
                if (allowSMS) {
                    String[] smsNumbers = user.getFilter().getMatchingDeliveryProfileNumbers("SMS", 0, false);
                    if (smsNumbers == null) {
                        String notifNumber = user.getNotifNumber();
                        if (notifNumber != null && !notifNumber.isEmpty())
                            smsNumbers = new String[] { notifNumber };
                        else
                            smsNumbers = new String[] { email.getReceiverPhoneNumber() };
                    }

                    Properties filterProp = new Properties();
                    filterProp.put("SMS", "");

                    smsFilterInfo = new SmsFilterInfo(filterProp, smsNumbers, null, null);
                    log.debug("sendReminderNotificationRetry: smsReminderIgnoreFilters is set; SMS reminder notification will be sent");
                }
                
                if (allowFLS) {
                    String[] flashNumbers = user.getFilter().getMatchingDeliveryProfileNumbers("FLS", 0, false);
                    if( flashNumbers == null ) {
                        String notifNumber = user.getNotifNumber();
                        if ( notifNumber != null  && !notifNumber.isEmpty() )
                            flashNumbers = new String[] { notifNumber };
                        else
                            flashNumbers = new String[] {email.getReceiverPhoneNumber()};
                    }
                    
                    Properties filterProp = new Properties();
                    filterProp.put("FLS", "");

                    flsFilterInfo = new SmsFilterInfo(filterProp, null, null, flashNumbers);
                    log.debug("sendReminderNotificationRetry: smsReminderIgnoreFilters is set; FLS reminder notification will be sent");
                }

            } else {

                boolean hasNewUrgent = (mailbox.getNewUrgentVoiceCount() == 0? false : true);
                if (hasNewUrgent) {
                    email.setUrgentReminder();
                } 
               
                FeedbackHandlerImpl fh = new FeedbackHandlerImpl();
                if (  allowFLS == true ) {
                    flsFilterInfo = ReminderUtil.getReminderFlashFilterInfo(user, email, fh);
                    if (flsFilterInfo == null && fh.getStatus() == Constants.FEEDBACK_STATUS_OK) {
                        if (Config.getSmsReminderTrySmsOnFLSDisabled()) {
                            log.debug("sendReminderNotificationRetry, FLS is disabled will try SMS as configured to:  " + NotificationConfigConstants.SMS_REMINDER_TRY_SMS_ON_FLS_DISABLED);
                            allowSMS = true;
                         }
                    }
                }
                if (fh.getStatus() == Constants.FEEDBACK_STATUS_OK && allowSMS == true) {      
                    smsFilterInfo = ReminderUtil.getReminderSmsFilterInfo(user, email, fh); 
                }
                switch (fh.getStatus()) { //deal with problems relating to roaming checks if set.
                    case Constants.FEEDBACK_STATUS_OK:
                        break;
                    case Constants.FEEDBACK_STATUS_FAILED:
                        log.warn("sendReminderNotificationRetry: failed for: " + mailboxTel);
                        ng.increaseTempCount(user, 1);
                        ng.failed(user, Constants.NTF_SMS, "Reminder failed due to roaming check error.");
                        return;
                    case Constants.FEEDBACK_STATUS_RETRY:
                    case Constants.FEEDBACK_STATUS_UNKNOWN:
                        ng.increaseTempCount(user, 1);
                        ng.retry(user, Constants.NTF_SMS, "retry reminder"); //level 2 retry again..
                        log.debug("sendReminderNotificationRetry: retry again for: " + mailboxTel);
                       return;
                  default:
                        log.warn("sendReminderNotificationRetry: Unexpected feedback type, asuming ok: " + fh.getStatus() );         
                      break;
                }
                
            }
            
            if (smsFilterInfo != null || flsFilterInfo != null) {
                updatePersistentProperty(mailboxTel, REMINDER_NOTIFICATION_EVENT_ID, email.getNtfEvent().getReferenceId());
                if (smsFilterInfo != null) {
                    sendSMS(user, ng, smsFilterInfo);
                    log.debug("sendReminderNotificationRetry: SMS reminder notification initiated for " + mailboxTel);
                }
                if (flsFilterInfo != null) {
                    sendSMS(user, ng, flsFilterInfo);
                    log.debug("sendReminderNotificationRetry: FLASH SMS reminder notification initiated for " + mailboxTel);
                }

            } else {
                log.debug("sendReminderNotificationRetry: No need for SMS reminder notification either because not in subscribers filters, or not configured to allow type - cancelling SMS/FLS reminder for " + mailboxTel);
                cancelReminderTrigger(email);  
            }
        }
        else{
            log.debug("sendReminderNotificationRetry: SMS Notification Reminder feature now disabled; cancelling previously scheduled SMS reminder for " + mailboxTel);
            cancelReminderTrigger(email);              
        }
    }
    
    /**
     * Starts the next SMS reminder trigger.
     * @param email the notification email
     * @param feedbackStatus the status returned from the delivery interface
     */
    public static void startNextReminderTrigger(UserInfo user, NotificationEmail email, int feedbackStatus){
        try{
            log.debug("Entered startNextReminderTrigger");
            if(retryHandler == null){
                retryHandler = NtfEventHandlerRegistry.getEventHandler();
            }
            NtfEvent event = email.getNtfEvent();
            String mailboxTel = event.getRecipient();
            if(Config.isSmsReminderEnabled() && user.hasUnreadMessageReminder()){
                if(!event.isFallback()){
                    switch(feedbackStatus){
                        case Constants.FEEDBACK_STATUS_OK:
                            if(!event.isReminder()){
                                log.debug("startNextReminderTrigger: new message notification for " + mailboxTel + " result status is OK; scheduling new SMS reminder trigger.");
                                startReminderTriggerOnNewMessageDeposit(event);

                                //The user already got a notification for the new deposit; cancel any pending reminder notification retries.
                                String notificationRetryEventId = retrievePersistentProperty(mailboxTel, REMINDER_NOTIFICATION_EVENT_ID);
                                if(notificationRetryEventId != null){
                                    updatePersistentProperty(mailboxTel, REMINDER_NOTIFICATION_EVENT_ID, null);
                                    retryHandler.cancelEvent(notificationRetryEventId);
                                    log.debug("startReminderTriggerOnNewMessageDeposit: Since sms status ok for new deposit notification, cancelled pending reminder notification retry for " + mailboxTel);
                                }                        
                            }
                            else {
                                log.debug("startNextReminderTrigger: reminder notification for " + mailboxTel + " result status is OK; cancelling reminder notification retry and rescheduling reminder trigger retry.");
                                //There is a call to cancelEvent later in NtfRetryEventHandler but since we are handling the persistence of reminders here, cancel here and update persistence.
                                //(Should not just update persistence and leave cancellation for later in case ntf goes down.)
                                updatePersistentProperty(mailboxTel, REMINDER_NOTIFICATION_EVENT_ID, null);
                                retryHandler.cancelEvent(event.getReferenceId());
                                event.keepReferenceID(null);

                                //In case the current reminder notification took a while to succeed, always reschedule the next reminder trigger retry event to (current time + reminder interval). 
                                String triggerEventId = retrievePersistentProperty(mailboxTel, REMINDER_TRIGGER_EVENT_ID);
                                String newTriggerEventId = retryHandler.rescheduleReminderTriggerEvent(triggerEventId);
                                if(!newTriggerEventId.equalsIgnoreCase(triggerEventId)){
                                    updatePersistentProperty(mailboxTel, REMINDER_TRIGGER_EVENT_ID, newTriggerEventId);
                                }
                            }
                            break;
                        case Constants.FEEDBACK_STATUS_FAILED:   
                            if(!event.isReminder()){
                                log.debug("startNextReminderTrigger: new message notification for " + mailboxTel + " result status is FAILED; scheduling new SMS reminder trigger.");
                                fallback(email);
                                startReminderTriggerOnNewMessageDeposit(event);
                            }
                            else {
                                //There is already a reminder trigger retry scheduled.  Only need to cancel reminder notification retry event.
                                //There is a call to cancelEvent later in NtfRetryEventHandler but since we are handling the persistence of reminders here, cancel here and update persistence.
                                //(Should not just update persistence and leave cancellation for later in case ntf goes down.)
                                log.debug("startNextReminderTrigger: reminder notification for " + mailboxTel + " result status is FAILED; reminder trigger retry already scheduled; cancelling reminder notification retry.");
                                updatePersistentProperty(mailboxTel, REMINDER_NOTIFICATION_EVENT_ID, null);
                                retryHandler.cancelEvent(event.getReferenceId());
                                fallback(email);
                                event.keepReferenceID(null);
                            }
                            break;
                        case Constants.FEEDBACK_STATUS_EXPIRED:
                            if(!event.isReminder()){
                                log.debug("startNextReminderTrigger: new message notification for " + mailboxTel + " result status is EXPIRED; scheduling new SMS reminder trigger.");
                                startReminderTriggerOnNewMessageDeposit(event);
                                fallback(email);
                            }
                            else {
                                log.debug("startNextReminderTrigger: reminder notification for " + mailboxTel + " result status is EXPIRED; reminder trigger retry already scheduled; removing reminder notification retry from persistence.");
                                updatePersistentProperty(mailboxTel, REMINDER_NOTIFICATION_EVENT_ID, null);
                                fallback(email);
                            }
                            break;
                        case Constants.FEEDBACK_STATUS_RETRY:
                            if(!event.isReminder()){
                                log.debug("startNextReminderTrigger: new message notification for " + mailboxTel + " result status is RETRY; no need to schedule new SMS reminder trigger at this time.");
                            }
                            else{
                                log.debug("startNextReminderTrigger: reminder notification for " + mailboxTel + " result status is RETRY; no need to reschedule SMS reminder trigger at this time.");
                            }
                            break;
                        default:
                            log.error("startNextReminderTrigger: unknown result status");
                    }
                }
                else{
                    log.debug("startNextReminderTrigger: Current SMS notification is a fallback; will not start SMS reminder trigger for " + mailboxTel);
                }
            }
            else{
                log.debug("startNextReminderTrigger: SMS Notification Reminder feature disabled; will not start SMS reminder trigger for " + mailboxTel);
            }
        } catch(Throwable t){
            //General catch statement to allow the new message notification to complete even though starting the reminder trigger failed.
            log.error("startNextReminderTrigger: Error starting next reminder trigger.", t);
        }
    }

    private static void fallback(NotificationEmail email) {
        String ReminderType = Config.getSmsReminderAllowedType();
        if (ReminderType.equalsIgnoreCase(Constants.notifTypeAbbrev[Constants.NTF_FLSSMS])) {
            FallbackHandler.get().fallback(Constants.NTF_FLSSMS, email.getNtfEvent());
        } else if (ReminderType.equalsIgnoreCase(Constants.notifTypeAbbrev[Constants.NTF_SMS])) {
            FallbackHandler.get().fallback(Constants.NTF_SMS, email.getNtfEvent());
        } else { //for now only other type allowed.
            FallbackHandler.get().fallback(Constants.NTF_FLS, email.getNtfEvent());
        }
    }

    /**
     * Handles the expiration of a reminder trigger.
     * @param email the notification email
     */
    public static void handleReminderTriggerExpiry(NotificationEmail email){
        NtfEvent event = email.getNtfEvent();
        //updatePersistentProperty(event.getRecipient(), REMINDER_TRIGGER_EVENT_ID, null);
        if(retryHandler == null){
            retryHandler = NtfEventHandlerRegistry.getEventHandler();
        }
        retryHandler.cancelReminderTriggerEvent(event.getReferenceId());
        event.keepReferenceID(null);
        //updatePersistentProperty(event.getRecipient(), REMINDER_NOTIFICATION_EVENT_ID, null);

        // We're finished with the file - instead of updating it twice with it being deleted the 2nd time,
        // delete it right away.
        try {
            mfsEventManager.removeFile(event.getRecipient(), SMS_STATUS_FILE);
            log.debug("handleReminderTriggerExpiry: " + SMS_STATUS_FILE + " file for " + event.getRecipient() + " is now empty; file deleted.");
            }
        catch (TrafficEventSenderException e) {
        log.error("handleReminderTriggerExpiry: Exception occured removing file: ", e);
        }
        
    }

    /**
     * Calls the SMS interface.
     * @param user the user information
     * @param ng collects results for all notifications resulting from this trigger.
     * @param filterInfo the SMS filter info for the user
     */
    protected static void sendSMS(UserInfo user, NotificationGroup ng, SmsFilterInfo filterInfo){
        NotificationEmail email = ng.getEmail();        
        //To get the mailbox total new message count in SMSOut.handleSMS(), the email type needs to be voice or video; so, set email type to voice.
        email.setDepositType(depositType.VOICE);

        SMSAddress source = null;
        
        //Chose content (template) based on normal sms/flash sms
        if(filterInfo.isFlash()) {
            filterInfo.setFlashContent( Config.getFlsReminderContent() );
            source = Config.getSourceAddress("flash", user.getCosName());        
        } else {
            filterInfo.setNotifContent( Config.getSmsReminderContent() );
            source = NotificationHandler.getSourceAddressEmail(email, user.getCosName());
        }

        filterInfo.setRoaming(false); // force it not to use roaming template for reminder..
        int count = SMSOut.get().handleSMS(user, filterInfo, ng, email, email.getUserMailbox(), source, user.getNotifExpTime(), 0);
        ng.setOutCount(user, count);
    }
    
    /**
     * Starts the next reminder trigger after a new message deposit.
     * @param event the notification event resulting from the new message deposit
     */
    private static void startReminderTriggerOnNewMessageDeposit(NtfEvent event){
        //If there is already an existing reminder trigger, cancel it and start a new one because the existing reminder
        //trigger might have an expiration that will occur before the expiration of the new message deposited 
        //(since the existing reminder trigger was scheduled for an earlier message deposit).
        
        String mailboxTel = event.getRecipient();
        String newTriggerEventId = retryHandler.scheduleReminderTriggerEvent(event);
        if(newTriggerEventId != null){
            String oldTriggerEventId = retrievePersistentProperty(mailboxTel, REMINDER_TRIGGER_EVENT_ID);
            updatePersistentProperty(mailboxTel, REMINDER_TRIGGER_EVENT_ID, newTriggerEventId);
            retryHandler.cancelReminderTriggerEvent(oldTriggerEventId); 
            log.debug("startReminderTriggerOnNewMessageDeposit: Succeeded in scheduling new reminder trigger event for " + mailboxTel);
        }
        else{
            log.error("startReminderTriggerOnNewMessageDeposit: Failed to schedule new reminder trigger event for " + mailboxTel + ".  The previously scheduled reminder trigger event, if any, will continue.");
        }        
    }
    
    /**
     * Cancels the reminder trigger and reminder notification.
     * @param email the notification email
     */
    private static void cancelReminderTrigger(NotificationEmail email){
        try {
            String mailboxTel = email.getReceiver();
            Properties properties = mfsEventManager.getProperties(mailboxTel, SMS_STATUS_FILE);
            if (properties != null) {
                String reminderNotificationEventId = properties.getProperty(REMINDER_NOTIFICATION_EVENT_ID);  
                String reminderTriggerEventId = properties.getProperty(REMINDER_TRIGGER_EVENT_ID);
                mfsEventManager.removeFile(mailboxTel, SMS_STATUS_FILE);
                
                retryHandler.cancelEvent(reminderNotificationEventId);                
                retryHandler.cancelReminderTriggerEvent(reminderTriggerEventId);                
                log.debug("cancelReminderTrigger: Cancelled all reminder backup events for " + mailboxTel + ": " + properties.toString());
            }
            else {
                NtfEvent event = email.getNtfEvent();
                String reminderEventId = event.getReferenceId();
                if(email.getNtfEvent().isEventServiceName(NtfEventTypes.SMS_REMINDER.getName())){
                    retryHandler.cancelReminderTriggerEvent(reminderEventId);
                }
                else {
                    retryHandler.cancelEvent(reminderEventId);
                }
                log.debug("cancelReminderTrigger: Expected persistent reminder event id(s) not found for  " + mailboxTel 
                        + ".  Cancelled backup event id for current reminder event: " + reminderEventId);
            }

        } catch (TrafficEventSenderException e) {
            log.error("cancelCurrentReminder: Exception occured: ", e);
        }
    }

    /**
     * Retrieves a property value from the SMS status file of the given phone number.
     * @param mailboxTel a tel identity (without the "tel:" URI) for the mailbox
     * @param property the property to update
     * @return the value of the property
     */
    protected static String retrievePersistentProperty(String mailboxTel, String property){
        String value = null;
        Properties properties = mfsEventManager.getProperties(mailboxTel, SMS_STATUS_FILE);
        log.debug("retrievePersistentProperty: Read " + SMS_STATUS_FILE + " file for " + mailboxTel + ": " +  properties);
        if (properties != null) {
            value = properties.getProperty(property);                
            log.debug("retrievePersistentProperty: Retrieved from " + SMS_STATUS_FILE + " file for " + mailboxTel + ": " +  property + "=" + value);
        }
        else{
            log.debug("retrievePersistentProperty: " + SMS_STATUS_FILE + " file for " + mailboxTel + " currently does not exist; no on-going reminders.  Return " + property + "=" + value);
        }
        return value;
    }

    /**
     * Public method to allow an external Class (like OdlWorker) to be able to 'update Persistent Properties"
     * of a Sms Reminder that it created (Outdial Notification, SMS reminder notification).
     * The event is created and scheduled using NtfRetryEventHandler but it MUST be 'stored persistently'
     * by SmsReminder in order to be correctly processed when triggered.  In addition, this method makes sure
     * that if there already was an Sms Reminder scheduled, it's properly canceled - and potentially its
     * scheduled retry also. 
     * @param mailboxTel    a tel identity (without the "tel:" URI) for the mailbox
     * @param value         the new value of the property to update
     */
    public static void updateExternallyScheduledReminderTrigger(String mailboxTel, String value){
        log.debug("updateExternallyScheduledReminderTrigger: scheduling new SMS reminder trigger for: " + mailboxTel);

        //cancel already scheduled reminder, if any
        String oldTriggerEventId = retrievePersistentProperty(mailboxTel, REMINDER_TRIGGER_EVENT_ID);
        updatePersistentProperty( mailboxTel, REMINDER_TRIGGER_EVENT_ID, value);
        if(oldTriggerEventId != null){
            retryHandler.cancelReminderTriggerEvent(oldTriggerEventId);
            log.debug("updateExternallyScheduledReminderTrigger: cancelled existing reminder notification for " + mailboxTel);
        }
        
        //cancel any pending reminder notification retries.
        String notificationRetryEventId = retrievePersistentProperty(mailboxTel, REMINDER_NOTIFICATION_EVENT_ID);
        if(notificationRetryEventId != null){
            updatePersistentProperty(mailboxTel, REMINDER_NOTIFICATION_EVENT_ID, null);
            retryHandler.cancelEvent(notificationRetryEventId);
            log.debug("updateExternallyScheduledReminderTrigger: cancelled pending reminder notification retry for " + mailboxTel);
        }                        
        
    }
    
    /**
     * Stores a property value to the SMS status file of the given phone number.
     * @param mailboxTel a tel identity (without the "tel:" URI) for the mailbox
     * @param property the property to update
     * @param value the new value of the property to update
     */
    protected static void updatePersistentProperty(String mailboxTel, String property, String value){
        try {
            if(property != null){
                Properties properties = mfsEventManager.getProperties(mailboxTel, SMS_STATUS_FILE);
                log.debug("updatePersistentProperty: Read " + SMS_STATUS_FILE + " file for " + mailboxTel + ": " +  properties);
                
                if(value == null){
                    //request to nullify property value: remove property from status file
                    if(properties != null){
                        properties.remove(property);
                        log.debug("updatePersistentProperty: " + SMS_STATUS_FILE + " file for " + mailboxTel + " updated: removed " +  property);
                        if(properties.isEmpty()){
                            mfsEventManager.removeFile(mailboxTel, SMS_STATUS_FILE);
                            log.debug("updatePersistentProperty: " + SMS_STATUS_FILE + " file for " + mailboxTel + " is now empty; file deleted.");
                        }
                        else{
                            mfsEventManager.storeProperties(mailboxTel, SMS_STATUS_FILE, properties);
                        }
                    }
                }
                else{
                    if(properties == null) {
                        properties = new Properties();                
                    }
                    properties.setProperty(property, value);
                    log.debug("updatePersistentProperty: " + SMS_STATUS_FILE + " file for " + mailboxTel + " updated with " +  property + "=" + value);
                    mfsEventManager.storeProperties(mailboxTel, SMS_STATUS_FILE, properties);
                }                
            }
        } catch (TrafficEventSenderException e) {
            log.error("updatePersistentProperty: Exception occured: ", e);
        }        
    }    
}
