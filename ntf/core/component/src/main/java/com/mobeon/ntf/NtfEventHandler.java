/**
 * Copyright (c) 2009 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf;

import java.util.concurrent.atomic.AtomicInteger;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.EventSentListener;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventReceiver;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.coremgmt.reminder.SmsReminder;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.time.NtfTime;
import com.mobeon.ntf.event.EventRouter;
import com.mobeon.ntf.event.PhoneOnEvent;
import com.mobeon.ntf.mail.EmailStore;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.out.autounlockpin.AutoUnlockPin;
import com.mobeon.ntf.out.autounlockpin.AutoUnlockPinUtil;

/**
 * NtfEventHandler gets notified by NtfSchedulerEventHandler to handle new notifications
 */
public class NtfEventHandler implements NtfEventReceiver, NtfCompletedListener {

    /** Log agent */
    private static LogAgent log = NtfCmnLogger.getLogAgent(NtfEventHandler.class);

    /** The next step in the flow of processing notifications */
    private EmailStore nextNotifProcessor;

    private int lastReportTime = 0;

    private static final CommonMessagingAccess commonMessagingAccess = CommonMessagingAccess.getInstance();
    private static IMfsEventManager mfsEventManager = MfsEventFactory.getMfsEvenManager();

    private static AtomicInteger numberOfNotificationCompleted = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationRetry = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationRetryQueueFull = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationFailed= new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationHoldback = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationAlreadyRead = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationMfsException = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationException = new AtomicInteger(0);
    private static AtomicInteger numberOfNotificationCurrent = new AtomicInteger(0);

    /**
     * Constructor
     */
    public NtfEventHandler(EmailStore notifProcessor) {

        nextNotifProcessor = notifProcessor;

        /** Report */
        lastReportTime = NtfTime.now;
        //TODO: ManagementInfo.get().setMailstorePoller( this ); // ManagementInfo must be updated
        //TODO: notifyStatus(); // ManagementInfo must be updated
    }

    /**
     * NtfEventReceiver methods
     */
    public void sendEvent(NtfEvent event) {

        NotificationEmail email = null;
        String to = event.getRecipient();

        try {
            boolean internalUser = false;
            boolean subscriberLoggedIn = false;
            email = new NotificationEmail(event);

            /**
             * Validate if the subscriber's storage is READ-ONLY.
             * Even if the event is expiry, let the scheduler retry so that the persistent file can be deleted properly.
             * For now, only SMS_Reminder has persistent storage stored at level-2
             * Notification types that are handled at level-3 (with a READ-ONLY subscriber/notification number) will be caught
             * at level-3 since here, at this state, level-2 is not aware of the notification number(s) which could be different
             * from the subscriber number and therefore, be in READ-ONLY mode while the subscriber number still be fully accessible.
             */
            if (event.isEventServiceName(NtfEventTypes.SMS_REMINDER.getName())) {
                if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(event.getRecipient())) {
                    log.warn("Storage currently not available for SMS Reminder notification for " + event.getRecipient() + ", will retry");
                    numberOfNotificationRetry.incrementAndGet();
                    return;
                }
            }

            /** Validate if its last event notification (expire event) for level-2 scheduling */
            if (event.isExpiry()) {
                email.init();
                //If the SMS reminder trigger expired, do not fallback.
                if(event.isEventServiceName(NtfEventTypes.SMS_REMINDER.getName())){
                    log.debug("SMS reminder trigger expired for subscriber " + email.getReceiver());
                    SmsReminder.handleReminderTriggerExpiry(email);
                    return;
                }

                //handle AutoUnlockPin expiry here to generate specific MDR
                if(event.isEventServiceName(NtfEventTypes.AUTO_UNLOCK_PIN_L2.getName()) &&
                   AutoUnlockPinUtil.isUnlockNeeded(event)){
                    log.debug("Auto Pin Unlock level-2 expired for subscriber " + email.getReceiver());
                    AutoUnlockPin.handleAutoUnlockPinExpiry(email);
                    return;
                }

                /** Inject an expired event to the Notification group in order to generate MDRs and invoke fallback mechanism. */
                NotificationGroup ng = new NotificationGroup(this, email, log, MerAgent.get(Config.getInstanceComponentName()));
                UserInfo user = UserFactory.findUserByTelephoneNumber(email.getReceiver());
                if (user == null) {
                    log.error("Subscriber " + email.getReceiver() + " not found, neither MDR nor fallback mechanism will be generated/triggered.");
                    return;
                }
                ng.addUser(user);
                ng.noMoreUsers();

                numberOfNotificationCurrent.incrementAndGet();
                ng.expired(user, Constants.NTF_SMS, "SMS notification expired");
                return;
            }

            if(event.isEventServiceType(NtfEventTypes.ALERT_SC.getName())){
        		String address = event.getRecipient();
            	log.debug("NtfEventHandler.sendEvent: Got an alert sc for " + address);
        		PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this, address, PhoneOnEvent.PHONEON_OK, "Received alert sc event");
        		EventRouter.get().phoneOn(phoneOnEvent);

            	event.getSentListener().sendStatus(event, EventSentListener.SendStatus.OK);
            	return;
            }

            /**
             * Validation to check if the subscriber is logged-in should be performed at all time except for:
             * 1) MissedCallNotification (MCN) cases (because the user is not a MIO subscriber);
             * 2) Holdback on vvasms events (cliviasms, welcomesms) should not be held back either;
             * 3) MWI OFF for unsubscribed users because we already know that they cannot login;
             * 4) and delayedevent, which should be scheduled now even if the subscriber is logged in.
             */
            if (!(event.isEventServiceType(NtfEventTypes.MCN.getName()) || event.isEventServiceType(NtfEventTypes.VVA_SMS.getName())
                    || event.isEventServiceType(NtfEventTypes.MWI_OFF_UNSUBSCRIBED.getName()) || event.isEventServiceType(NtfEventTypes.DELAYED_EVENT.getName()))) {
                subscriberLoggedIn = isSubscriberLoggedIn(event);
                internalUser = true;
            }

            log.debug("EventServiceType: " + event.getEventServiceTypeKey() + ", EventType: " + event.getEventTypeKey() + ", EventServiceName: " + event.getEventServiceName());

            // Skip the notification if the subscriber is logged-in, scheduler will retry later
            // If the subscriber is logged-in and it's a slam down notification, go forward with notification
            if (subscriberLoggedIn && !(event.isEventServiceType(NtfEventTypes.SLAMDOWN.getName())|| event.isEventServiceType(NtfEventTypes.FAX_PRINT.getName()))) {
                log.debug("Subscriber " + to + " is currently logged-in, notification will retry later");
                event.getSentListener().sendStatus(event, EventSentListener.SendStatus.NORMAL_RETRY);
                profilerAgentCheckPoint("NTF.NEH.6.Holdback");
                numberOfNotificationHoldback.incrementAndGet();
                return;
            } else {
                if (internalUser) {
                    log.debug("Subscriber " + to
                            + (subscriberLoggedIn ? " is logged-in (slamdown notification)," : " is not logged-in," )
                            + " go forward with notification");
                }
            }

            // Specific validation for DefaultNotifcation case only
            if ((event.isEventServiceType(NtfEventTypes.DEFAULT_NTF.getName()) || event.isEventServiceType(NtfEventTypes.ROAMING.getName()))
            		&& event.isEventType(NtfEventTypes.EVENT_TYPE_NOTIF.getName()) && !event.isReminder() ) {
                // Do not notify if the message is already read (in retry cases only)
                if (event.getNumberOfTried() > 0 && !isMessageNew(event)) {
                    log.debug("Subscriber " + to + " already read message " + event.getMessageId() + ". Retries will be cancelled");
                    event.getSentListener().sendStatus(event, EventSentListener.SendStatus.OK);
                    profilerAgentCheckPoint("NTF.NEH.7.AlreadyReadMessage");
                    numberOfNotificationAlreadyRead.incrementAndGet();
                    return;
                }
            }

            // Inject the NotificationEmail in the level-2 processing queue
            boolean storedInQueue = false;
            if (log.isDebugEnabled()) {
                log.debug("Trying to add to queue which already has size: " + nextNotifProcessor.getSize());
            }
            storedInQueue = nextNotifProcessor.putEmailCheckSize(email);

            if (storedInQueue) {
                profilerAgentCheckPoint("NTF.NEH.1.CurrentNotif");
                numberOfNotificationCurrent.incrementAndGet();
            } else {
                log.debug("Subscriber " + to + " cannot be notified for now, queue is full, notification will retry later");
                event.getSentListener().sendStatus(event, EventSentListener.SendStatus.NORMAL_RETRY);

                profilerAgentCheckPoint("NTF.NEH.4.RetryQueueFull");
                numberOfNotificationRetryQueueFull.incrementAndGet();
            }

        } catch (MsgStoreException me) {
            // No retry should be perform, handled as a debug information since plausible not to find state file (message already deleted)
            log.debug("Message(state file) not found for eventId " + event.getReferenceId() + " (Subscriber: " + to + "). No retry will be performed.");
            event.getSentListener().sendStatus(event, EventSentListener.SendStatus.PERMANENT_ERROR);

            // Specific Slamdown and Mcn cases
            if (event.isEventServiceType(NtfEventTypes.SLAMDOWN.getName()) || event.isEventServiceType(NtfEventTypes.MCN.getName())) {
                NotificationHandler.deleteFile(event);
            }

            profilerAgentCheckPoint("NTF.NEH.8.MsgStoreException");
            numberOfNotificationMfsException.incrementAndGet();
            return;
        } catch (Exception e) {
            // No retry should be perform
            log.error("Exception " + e + ". No retry will be performed.");
            event.getSentListener().sendStatus(event, EventSentListener.SendStatus.PERMANENT_ERROR);

            // Specific Slamdown and Mcn cases
            if (event.isEventServiceType(NtfEventTypes.SLAMDOWN.getName()) || event.isEventServiceType(NtfEventTypes.MCN.getName())) {
                NotificationHandler.deleteFile(event);
            }

            profilerAgentCheckPoint("NTF.NEH.9.Exception");
            numberOfNotificationException.incrementAndGet();
            return;
        } finally {
            logCounters();
        }
    }

    /**
     * NtfCompletedListener methods
     */
    public void notifCompleted(NtfEvent event) {
        // Notify MRD to cancel future retry via NtfEvent
        boolean notificationProcessedAtNtfLevel3 =
            event.isEventServiceType(NtfEventTypes.SLAMDOWN.getName()) ||
            event.isEventServiceType(NtfEventTypes.MCN.getName()) ||
            event.isEventServiceType(NtfEventTypes.OUTDIAL.getName()) ||
            event.isEventServiceType(NtfEventTypes.DELAYED_EVENT.getName());

        if (!notificationProcessedAtNtfLevel3) {
            log.debug("Notification completed (level-2, between MAS and NTF), canceling retry event (" + event.getReferenceId() + ")");
        }

        event.getSentListener().sendStatus(event, EventSentListener.SendStatus.OK);
        numberOfNotificationCompleted.incrementAndGet();
        profilerAgentCheckPoint("NTF.NEH.2.Completed");
        numberOfNotificationCurrent.decrementAndGet();
    }

    public void notifRetry(NtfEvent event) {
        // Notify NtfRetryEventHandler (which will NOT inform scheduler, the retry timer will kick-in later)
        log.debug("Notification retry, will retry (" + event.getReferenceId() + ")");
        event.getSentListener().sendStatus(event, EventSentListener.SendStatus.NORMAL_RETRY);
        profilerAgentCheckPoint("NTF.NEH.3.Retry");
        numberOfNotificationRetry.incrementAndGet();
        numberOfNotificationCurrent.decrementAndGet();
    }

    public void notifFailed(NtfEvent event) {
        // Notify MRD to cancel future retry via NtfEvent since FAILED
        log.debug("Notification failed, (level-2, between MAS and NTF), canceling retry event (" + event.getReferenceId() + ")");
        event.getSentListener().sendStatus(event, EventSentListener.SendStatus.PERMANENT_ERROR);
        profilerAgentCheckPoint("NTF.NEH.5.Failed");
        numberOfNotificationFailed.incrementAndGet();
        numberOfNotificationCurrent.decrementAndGet();
    }

    public int getTimeSinceLastReport() {
        return NtfTime.now - lastReportTime;
    }

    public synchronized void notifyStatus() {

        //boolean down = true;
        //TODO: Get Status from MFS
/*
        if( down ) {
            ManagementStatus mStatus = ManagementInfo.get().getStatus("Storage", Config.getImapHost() );
            if( mStatus != null ) {
                mStatus.setHostName(Config.getImapHost());
                mStatus.setPort(Config.getImapPort());
                mStatus.setZone(Config.getLogicalZone());
                mStatus.down();
            }
        } else {
            ManagementStatus mStatus = ManagementInfo.get().getStatus("Storage", Config.getImapHost() );
            if(mStatus != null ) {
                mStatus.up();
            }

        }
*/
    }

    public static void resetNumberOfNotification() {
        numberOfNotificationCompleted = new AtomicInteger(0);
        numberOfNotificationRetry = new AtomicInteger(0);
        numberOfNotificationFailed= new AtomicInteger(0);
        numberOfNotificationHoldback = new AtomicInteger(0);
        numberOfNotificationAlreadyRead = new AtomicInteger(0);
    }

    public static int getNumberOfNotificationCompleted() {
        return numberOfNotificationCompleted.get();
    }

    public static int getNumberOfNotificationRetry() {
        return numberOfNotificationRetry.get();
    }

    public static int getNumberOfNotificationFailed() {
        return numberOfNotificationFailed.get();
    }

    public static int getNumberOfNotificationHoldback() {
        return numberOfNotificationHoldback.get();
    }

    public static int getNumberOfNotificationAlreadyRead() {
        return numberOfNotificationAlreadyRead.get();
    }

    public static int getNumberOfNotificationMfsException() {
        return numberOfNotificationMfsException.get();
    }

    public static int getNumberOfNotificationException() {
        return numberOfNotificationException.get();
    }

    public static int getNumberOfNotificationCurrent() {
        return numberOfNotificationCurrent.get();
    }

    public void decreaseNumberOfNotificationCurrent() {
        numberOfNotificationCurrent.decrementAndGet();
    }

    /**
     * Check if the subscriber's private directory contains the .login file
     * @param event NtfEvent
     * @return boolean
     */
    private boolean isSubscriberLoggedIn(NtfEvent event) {
        int validityPeriod = Config.getLoginFileValidityPeriod();
        return mfsEventManager.loginFileExistsAndValidDate(event.getRecipient(), validityPeriod);
    }

    /**
     * Check if the MFS message is in the new state
     * @param event NtfEvent
     * @return boolean
     * @throws MsgStoreException exception
     */
    private boolean isMessageNew(NtfEvent event) throws MsgStoreException {
        StateFile stateFile = commonMessagingAccess.getStateFile(event.getMsgInfo());
        String msgState = stateFile.getAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY);
        return msgState.equalsIgnoreCase(MoipMessageEntities.MESSAGE_NEW);
    }

    /**
     * Logs for debugging purposes
     */
    private void logCounters() {
        log.info("Ntf Counters (NEH): " +
                "Current: " + numberOfNotificationCurrent.get() +
                ", Completed: " + numberOfNotificationCompleted.get() +
                ", Retry: " + numberOfNotificationRetry.get() +
                ", RetryQueueFull: " + numberOfNotificationRetryQueueFull.get() +
                ", Failed: " + numberOfNotificationFailed.get() +
                ", Holdback: " + numberOfNotificationHoldback.get() +
                ", AlreadyRead: " + numberOfNotificationAlreadyRead.get() +
                ", MfsExcep: " + numberOfNotificationMfsException.get() +
                ", Excep: " + numberOfNotificationException.get());
    }

    /**
     * Profiling method
     * @param checkPoint String
     */
    public void profilerAgentCheckPoint(String checkPoint) {
        Object perf = null;
        if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
            try {
                perf = CommonOamManager.profilerAgent.enterCheckpoint(checkPoint);
            } finally {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
        }
    }

}

