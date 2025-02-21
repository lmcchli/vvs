/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.coremgmt.reminder.ReminderUtil;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.event.SipMwiEvent;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.out.sip.SIPOut;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.SIPFilterInfo;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * This class handles SIP MWI events
 */
public class SipMwiEventHandler extends NtfRetryEventHandler {

    private static LogAgent logger = NtfCmnLogger.getLogAgent(SipMwiEventHandler.class);
    private ManagedArrayBlockingQueue<Object> queue;
    private IMfsEventManager mfsEventManager = MfsEventFactory.getMfsEvenManager();

    public static final String SIPMWI_STATUS_FILE = "sipmwi.status";
    public static final String SCHEDULER_ID = "sid";
    public static final String REMINDER_SCHEDULER_ID = "rsid";
    private static final String ENCODED_DOT = "*dot*";
    private static final String DOT = "\\.";

    public SipMwiEventHandler() {
        RetryEventInfo retryInfo = new RetryEventInfo(getEventServiceName());
        retryInfo.setEventRetrySchema(Config.getSipMwiNotifRetrySchema());
        retryInfo.setExpireTimeInMinute(Config.getSipMwiNotifExpireTimeInMin());
        retryInfo.setExpireRetryTimerInMinute(Config.getSipMwiExpiryIntervalInMin());
        retryInfo.setMaxExpireTries(Config.getSipMwiExpiryRetries());

        RetryEventInfo reminderRetryInfo = new RetryEventInfo(getReminderEventServiceName());
        reminderRetryInfo.setEventRetrySchema(String.valueOf(Config.getSipMwiReminderIntervalInMin())+"m CONTINUE");
        reminderRetryInfo.setExpireTimeInMinute(Config.getSipMwiReminderExpireInMin());
        reminderRetryInfo.setExpireRetryTimerInMinute(Config.getSipMwiExpiryIntervalInMin());
        reminderRetryInfo.setMaxExpireTries(Config.getSipMwiExpiryRetries());

        super.init(retryInfo, reminderRetryInfo);
    }

	public String getEventServiceName() {
		return NtfEventTypes.SIPMWI.getName();
	}

    public String getReminderEventServiceName() {
        return NtfEventTypes.SIPMWI_REMINDER.getName();
    }

	public void setWorkingQueue(ManagedArrayBlockingQueue<Object> queue) {
		this.queue = queue;
	}

	public void reset() {
		super.numOfCancelledEvent = new AtomicLong(0);
		super.numOfFiredExpireEvent = new AtomicLong(0);
		super.numOfFiredNotifEvent = new AtomicLong(0);
		super.numOfScheduledEvent = new AtomicLong(0);
	}

	/**
	 * Schedule new event
	 * @param sipMwiEvent The SipMwi event to schedule
     * @return True if the schedule operation is successful, false otherwise
	 */
    public boolean scheduleBackup(SipMwiEvent sipMwiEvent) {
        boolean result = true;

        // Retrieve from the persistent storage any pending SipMwi notification (retry and reminder)
        retrieveSchedulerEventIdsPersistent(sipMwiEvent);

        String previousEventId = null;
        if (!sipMwiEvent.isSchedulerIdEmpty()) {
            // There is a SipMwi notification retry pending so cancel the old event from Scheduler
            // The latest is the greatest.
            previousEventId = sipMwiEvent.getReferenceId();
        }

        // Schedule the new SipMwi notification retry
        AppliEventInfo eventInfo = eventHandler.scheduleEvent(sipMwiEvent.getNotificationNumber().replaceAll(DOT, ENCODED_DOT) + NtfEvent.getUniqueId(), NtfEventTypes.EVENT_TYPE_NOTIF.getName(), sipMwiEvent.getEventProperties());
        sipMwiEvent.keepReferenceID(eventInfo.getEventId());

        // First step is to update the persistent storage with the new value
        boolean successfullyUpdated = updateEventIdsPersistent(sipMwiEvent);
        if (successfullyUpdated) {
            // Second step is to update the sipMwiEvent and cancel the previous eventId
            logger.debug("SipMwiEventHandler scheduled a new SipMwi notification retry " + eventInfo.getEventId());

            if (previousEventId != null) {
                logger.debug("SipMwiEventHandler canceling old SipMwi notification " + previousEventId);
                super.cancelEvent(previousEventId);
            }
        } else {
            /**
             * If the update of the persistent storage is not successful, cancel the just-scheduled eventInfo
             * and keep the previousEventId to retry
             */
            super.cancelEvent(eventInfo.getEventId());
            sipMwiEvent.keepReferenceID(previousEventId);
            result = false;
        }

        return result;
    }

    /**
     * Schedule new reminder trigger event
     * @param sipMwiEvent The SipMwi event to schedule
     * @return True if the schedule operation is successful, false otherwise
     */
    public boolean scheduleReminderTriggerBackup(SipMwiEvent sipMwiEvent) {
        boolean result = true;

        if (Config.isSipMwiReminderEnabled() && !sipMwiEvent.isFallback()) {
            // Retrieve from the persistent storage any pending SipMwi notification (retry and reminder)
            retrieveSchedulerEventIdsPersistent(sipMwiEvent);

            String previousReminderEventId = null;
            if (!sipMwiEvent.isReminderSchedulerIdEmpty()) {
                // There is a SipMwi reminder trigger notification retry pending so cancel the old event from Scheduler
                previousReminderEventId = sipMwiEvent.getReminderTriggerReferenceId();
            }

            // Schedule the new SipMwi reminder retry
            String eventId = this.scheduleReminderTriggerEvent(sipMwiEvent);
            sipMwiEvent.setReminderTriggerReferenceId(eventId);

            // First step is to update the persistent storage with the new value
            boolean successfullyUpdated = updateEventIdsPersistent(sipMwiEvent);
            if (successfullyUpdated) {
                // Second step is to update the sipMwiEvent and cancel the previous eventId
                logger.debug("SipMwiEventHandler scheduled a new SipMwi reminder " + eventId);

                if (previousReminderEventId != null) {
                    logger.debug("SipMwiEventHandler canceling old reminder SipMwi notification " + previousReminderEventId);
                    super.cancelReminderTriggerEvent(previousReminderEventId);
                }
            } else {
                /**
                 * If the update of the persistent storage is not successful, cancel the just-scheduled eventInfo
                 * and keep the previousEventId to retry
                 */
                super.cancelReminderTriggerEvent(eventId);
                sipMwiEvent.keepReferenceID(previousReminderEventId);
                result = false;
            }
        } else {
            if (!Config.isSipMwiReminderEnabled()) {
                logger.debug("SipMwiEventHandler notification reminder is off, ignoring scheduling new SipMwi reminder trigger event: " + sipMwiEvent);
            }
            if (Config.isSipMwiReminderEnabled() && !sipMwiEvent.isFallback()) {
                logger.debug("SipMwiEventHandler notification reminder is in fallback mode, ignoring scheduling new SipMwi reminder trigger event: " + sipMwiEvent);
            }
        }

        return result;
    }

    /**
     *
     * @param sipMwiEvent The SipMwi event to re-schedule
     * @return True if the update operation is successful, false otherwise
     */
    public boolean reScheduleReminderTriggerBackup(SipMwiEvent sipMwiEvent) {
        if (Config.isSipMwiReminderEnabled() && !sipMwiEvent.isFallback()) {
            String newEventId = this.rescheduleReminderTriggerEvent(sipMwiEvent.getReminderTriggerReferenceId());
            logger.debug("SipMwiEventHandler re-scheduled reminder trigger event: " + newEventId);

            if (!sipMwiEvent.getReminderTriggerReferenceId().equalsIgnoreCase(newEventId)) {
                sipMwiEvent.setReminderTriggerReferenceId(newEventId);
                boolean successfullyUpdated = updateEventIdsPersistent(sipMwiEvent);
                if (!successfullyUpdated) {
                    return false;
                }
            }
        } else {
            if (!Config.isSipMwiReminderEnabled()) {
                logger.debug("SipMwiEventHandler notification reminder is off, ignoring re-scheduling SipMwi reminder trigger event: " + sipMwiEvent);
            }
            if (Config.isSipMwiReminderEnabled() && !sipMwiEvent.isFallback()) {
                logger.debug("SipMwiEventHandler notification reminder is in fallback mode, ignoring re-scheduling SipMwi reminder trigger event: " + sipMwiEvent);
            }
        }
        return true;
    }

	/**
	 *
	 * @param sipMwiEvent The SipMwi event to re-schedule
	 * @param retryTime Indicates when the event is to be fired.
     * @return True if the schedule operation is successful, false otherwise
	 */
	public boolean scheduleRetryTimeSpecific(SipMwiEvent sipMwiEvent, long retryTime) {
	    boolean result = true;

	    if (!Config.isSipMwiReminderEnabled() && sipMwiEvent.isReminder()) {
	        logger.debug("SipMwiEventHandler notification reminder is off, ignoring scheduling SipMwi reminder event at specific time: " + sipMwiEvent);
	    } else {
	        String previousEventId = sipMwiEvent.getReferenceId();

	        long when = retryTime * 1000 + System.currentTimeMillis();
	        AppliEventInfo eventInfo = eventHandler.scheduleEvent(when, sipMwiEvent.getNotificationNumber().replaceAll(DOT,ENCODED_DOT) + NtfEvent.getUniqueId(), NtfEventTypes.EVENT_TYPE_NOTIF.getName(), sipMwiEvent.getEventProperties());
            sipMwiEvent.keepReferenceID(eventInfo.getEventId());

	        // First step is to update the persistent storage with the new value
            boolean successfullyUpdated = updateEventIdsPersistent(sipMwiEvent);
            if (successfullyUpdated) {
                // Second step is to update the sipMwiEvent and cancel the previous eventId
                logger.debug("SipMwiEventHandler new eventId scheduled: " + eventInfo.getEventId());

                logger.debug("SipMwiEventHandler cancel previous event: " + previousEventId);
                super.cancelEvent(previousEventId);
            } else {
                /**
                 * If the update of the persistent storage is not successful, cancel the just-scheduled eventInfo
                 * and keep the previousEventId to retry
                 */
                super.cancelEvent(eventInfo.getEventId());
                sipMwiEvent.keepReferenceID(previousEventId);
                result = false;
            }
	    }
        return result;
	}

	private boolean shouldProcessEventFired(SipMwiEvent sipMwiEvent, AppliEventInfo eventInfo) {
	    boolean shouldProcess = true;
        if (sipMwiEvent == null || eventInfo == null) {
            shouldProcess = false;
        } else {
            String persistedEventId = null;
            if (sipMwiEvent.isEventServiceType(NtfEventTypes.SIPMWI.getName())) {
                persistedEventId = sipMwiEvent.getReferenceId();
            } else if (sipMwiEvent.isEventServiceType(NtfEventTypes.SIPMWI_REMINDER.getName())) {
                persistedEventId = sipMwiEvent.getReminderTriggerReferenceId();
            } else {
                logger.info("EventFired: Invalid eventServiceType: " + sipMwiEvent.getEventServiceTypeKey() + ", stop retry");
                return false;
            }

            shouldProcess = CommonMessagingAccess.getInstance().compareEventIds(eventInfo, persistedEventId);
            if (!shouldProcess) {
                logger.info("EventFired: EventIds not matching: firedEvent: " + eventInfo.getEventId() + ", storedEvent: " + persistedEventId + ", stop retry");
            }
        }
	    return shouldProcess;
	}

	@Override
	public int eventFired(AppliEventInfo eventInfo) {

        if (eventInfo == null || eventInfo.getEventId() == null) {
            logger.error("Invalid eventId for event " + eventInfo);
            return EventHandleResult.STOP_RETRIES;
        }

        try {
            SipMwiEvent sipMwiEvent = null;
            if (!SIPOut.get().isStarted()) { // Configuration issue or exception
                logger.error("Received SipMWi event but service is not available, will retry");
                return EventHandleResult.OK;
            }

            logger.debug("SipMwiEventHandler event fired: " + eventInfo.getEventId());
            numOfFiredNotifEvent.incrementAndGet();

            sipMwiEvent = new SipMwiEvent(eventInfo.getEventId());
            sipMwiEvent.setCurrentEvent(SipMwiEvent.SIPMWI_EVENT_NOTIFICATION);

            // Validate if the subscriber's storage is READ-ONLY (using the notification number)
            if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(sipMwiEvent.getNotificationNumber())) {
                logger.warn("Storage currently not available to process SipMwi event for " + sipMwiEvent.getSubscriberNumber() + " : " + sipMwiEvent.getNotificationNumber() + ", will retry");
                return EventHandleResult.OK;
            }

            // Retrieve from the persistent storage any pending SipMwi notification (retry and reminder)
            retrieveSchedulerEventIdsPersistent(sipMwiEvent);
            if (!shouldProcessEventFired(sipMwiEvent, eventInfo)) {
                return EventHandleResult.STOP_RETRIES;
            }

            // Validate if its an expiry notification
            if (eventInfo.getNextEventInfo() == null || eventInfo.isExpire() || eventInfo.isLastExpire()) {
                logger.debug("SipMwiEventHandler event found to be expired: " + sipMwiEvent.getReferenceId());
                sipMwiEvent.setCurrentEvent(SipMwiEvent.SIPMWI_EVENT_EXPIRED);
            }

            if (sipMwiEvent.isEventServiceType(NtfEventTypes.SIPMWI.getName())) {

                if (sipMwiEvent.isReminder()) {
                    // This event is a reminder notification
                    logger.debug("SipMwiEventHandler SipMwi reminder notification retry event: " + sipMwiEvent.getReferenceId());
                } else {
                    logger.debug("SipMwiEventHandler SipMwi notification retry event: " + sipMwiEvent.getReferenceId());
                }

                updateScheduledEventsIds(eventInfo, sipMwiEvent);
                if (!updateEventIdsPersistent(sipMwiEvent)) {
                    String message = "Unable to update persistent storage with next eventId for " + sipMwiEvent.getIdentity();
                    if (eventInfo.getNextEventInfo() != null) {
                        logger.warn(message + ", will retry");
                    } else {
                        logger.error(message + ", will not retry");
                    }
                    return EventHandleResult.OK;
                }

            } else if (sipMwiEvent.isEventServiceType(NtfEventTypes.SIPMWI_REMINDER.getName())) {
                /**
                 * This event is a reminder trigger notification, it triggers a new
                 * notification reminder event (NtfEventTypes.SIPMWI with isReminder() true)
                 * The reminder trigger notification event is not injected in the queue to be processed by the workers.
                 */
                logger.debug("SipMwiEventHandler SipMwi reminder trigger notification retry event: " + sipMwiEvent.getReferenceId());

                if (!Config.isSipMwiReminderEnabled()) {
                    logger.info("SipMwi Reminder feature not enabled anymore for " + sipMwiEvent.getSubscriberNumber());
                    if (this.cancelAllEvents(sipMwiEvent, false)) {
                        return EventHandleResult.STOP_RETRIES;
                    } else {
                        logger.warn("Unable to cancelEvents for " + sipMwiEvent.getSubscriberNumber() + ", will retry");
                        return EventHandleResult.OK;
                    }
                }

                boolean notificationNumberFoundInDeliveryProfile = false;
                UserInfo userInfo = UserFactory.findUserByTelephoneNumber(sipMwiEvent.getSubscriberNumber());
                if (userInfo != null) {
                    SIPFilterInfo sipFilterInfo = this.getSIPFilterInfo(userInfo, sipMwiEvent);
                    if (sipFilterInfo != null) {
                        for (String number: sipFilterInfo.getNumbers()) {
                            if (sipMwiEvent.getNotificationNumber().equals(number)) {
                                notificationNumberFoundInDeliveryProfile = true;
                                break;
                            }
                        }
                    }
                }

                if (!notificationNumberFoundInDeliveryProfile) {
                    logger.warn("Subscriber " + sipMwiEvent.getSubscriberNumber() + " or notification number not found.");
                    boolean successfullyCancelled = this.cancelAllEvents(sipMwiEvent, false);
                    if (successfullyCancelled) {
                        return EventHandleResult.STOP_RETRIES;
                    } else {
                        logger.warn("Unable to cancelEvents for " + sipMwiEvent.getSubscriberNumber() + ", will retry");
                        return EventHandleResult.OK;
                    }
                }

                // Set the next Reminder trigger
                updateScheduledEventsIds(eventInfo, sipMwiEvent);
                if (!updateEventIdsPersistent(sipMwiEvent)) {
                    String message = "Unable to update persistent storage with next eventId for " + sipMwiEvent.getIdentity();
                    if (eventInfo.getNextEventInfo() != null) {
                        logger.warn(message + ", will retry");
                    } else {
                        logger.error(message + ", will not retry");
                    }
                    return EventHandleResult.OK;
                }

            } else {
                logger.error("Unknown fired event type: : " + sipMwiEvent.getEventTypeKey());
                this.cancelAllEvents(sipMwiEvent, true);
                return EventHandleResult.STOP_RETRIES;
            }

            // Add notification reminder to the working queue for processing
            boolean storedInQueue = queue.offer(sipMwiEvent);
            if (storedInQueue) {
                logger.debug("EventFired: Stored in workingQueue : " + sipMwiEvent);
            } else {
                logger.warn("EventFired: Not stored in workingQueue (full), will retry : " + sipMwiEvent);
            }
        } catch (Exception e) {
            String message = "Event fired exception for " + eventInfo.getEventId();
            if (eventInfo.getNextEventInfo() != null) {
                logger.warn(message + ", will retry. ", e);
            } else {
                logger.error(message + ", will not retry. ", e);
            }
        }
		return EventHandleResult.OK;
	}

	private void updateScheduledEventsIds(AppliEventInfo eventInfo, SipMwiEvent sipMwiEvent) {
        // Update the next retry (sipmwi or sipmwi reminder)
        String nextEventId = null;

        if (eventInfo.getNextEventInfo() != null) {
            nextEventId = eventInfo.getNextEventInfo().getEventId();
        }
        logger.debug("SipMwiEventHandler: Next retry event is " + nextEventId);

        if (sipMwiEvent.isEventServiceType(NtfEventTypes.SIPMWI.getName())) {
            sipMwiEvent.keepReferenceID(nextEventId);
        } else if (sipMwiEvent.isEventServiceType(NtfEventTypes.SIPMWI_REMINDER.getName())) {
            sipMwiEvent.setReminderTriggerReferenceId(nextEventId);
        } else {
            logger.error("Invalid eventId type: " + eventInfo.getEventId());
        }
    }

	/**
     * This method TRIES to cancel the reminderEventId by updating the persistent storage and invoking the scheduler.cancel.
     * In the plausible case of being unable to update the persistent storage (because of an I/O Exception for example),
     * the method will return false - which would lead the clients of this method not to go forward with their next
     * operation and, instead, wait for a retry of this eventId (by the scheduler).
     *
     * @param sipMwiEvent SipMwiEvent
     * @return True if the cancel operation is successful, false otherwise
     */
    public boolean cancelReminderEvent(SipMwiEvent sipMwiEvent) {
        boolean result = true;
        String reminderRefrenceId = sipMwiEvent.getReminderTriggerReferenceId();

        // First step is to nullify the value on disk
        sipMwiEvent.setReminderTriggerReferenceId("");
        boolean successfullyUpdated = updateEventIdsPersistent(sipMwiEvent);

        /**
         * If the update of the persistent storage is successful, cancel the event.
         * If not, do not cancel and let the scheduler retry later.
         */
        if (successfullyUpdated) {
            if (reminderRefrenceId != null && reminderRefrenceId.length() > 0) {
                logger.debug("SipMwiEventHandler cancel reminder trigger event: " + reminderRefrenceId);
                super.cancelReminderTriggerEvent(reminderRefrenceId);
            }
        } else {
            // Revert back to the original eventId
            sipMwiEvent.setReminderTriggerReferenceId(reminderRefrenceId);
            result = false;
        }
        return result;
    }

    /**
     * This method TRIES to cancel the eventId by updating the persistent storage and invoking the scheduler.cancel.
     * In the plausible case of being unable to update the persistent storage (because of an I/O Exception for example),
     * the method will return false - which would lead the clients of this method not to go forward with their next
     * operation and, instead, wait for a retry of this eventId (by the scheduler).
     * 
     * In the case of a client already backed-up by another eventId type, the client COULD decide to cancel the
     * eventId anyway regardless if the persistent storage operation is successful or not.
     * This operation MUST be considered as a 'best effort' since the eventId COULD be stored on another site
     * (in a Geo-Distributed solution) and therefore, not be cancelled successfully.
     *  
     * @param sipMwiEvent SipMwiEvent
     * @return True if the cancel operation is successful, false otherwise
     */
    public boolean cancelEvent(SipMwiEvent sipMwiEvent) {
        return cancelEvent(sipMwiEvent, false);
    }

    public boolean cancelEvent(SipMwiEvent sipMwiEvent, boolean tryToCancelAnyway) {
        boolean result = true;
        String referenceId = sipMwiEvent.getReferenceId();

        // First step is to nullify the value on disk
        sipMwiEvent.keepReferenceID("");
        boolean successfullyUpdated = updateEventIdsPersistent(sipMwiEvent);

        /**
         * If the update of the persistent storage is successful, cancel the event.
         * If not, do not cancel and let the scheduler retry later.
         */
        if (successfullyUpdated || tryToCancelAnyway) {
            // Second step is to cancel the scheduled events
            if (referenceId != null && referenceId.length() > 0) {
                logger.debug("SipMwiEventHandler cancel event: " + referenceId);
                super.cancelEvent(referenceId);
            }
        } else {
            // Revert back to the original eventIds
            sipMwiEvent.keepReferenceID(referenceId);
            result = false;
        }
        return result;
    }

    /**
     * This method TRIES to cancel the eventIds by updating the persistent storage and invoking the scheduler.cancel.
     * In the plausible case of being unable to update the persistent storage (because of an I/O Exception for example),
     * the method will return false - which would lead the clients of this method not to go forward with their next
     * operation and, instead, wait for a retry of this eventId (by the scheduler).
     * 
     * In the case of a client already backed-up by another eventId type, the client COULD decide to cancel the
     * eventIds anyway regardless if the persistent storage operation is successful or not.
     * This operation MUST be considered as a 'best effort' since the eventId COULD be stored on another site
     * (in a Geo-Distributed solution) and therefore, not be cancelled successfully.
     *  
     * In the particular case of cancelling all the events, all the clients SHOULD try to cancel the eventIds anyway
     * since this method is usually used at the end of a SIP MWI notification process or when a faulty operation
     * has been encountered.
     *  
     * @param sipMwiEvent SipMwiEvent
     * @param tryToCancelAnyway True if the cancel operation SHOULD be tried even if the persistent storage update is not successful 
     * @return True if the cancel operation is successful, false otherwise
     */
    public boolean cancelAllEvents(SipMwiEvent sipMwiEvent, boolean tryToCancelAnyway) {
        boolean result = true;
        String referenceId = sipMwiEvent.getReferenceId();
        String reminderRefrenceId = sipMwiEvent.getReminderTriggerReferenceId();

        // First step is to nullify the values on disk
        sipMwiEvent.keepReferenceID("");
        sipMwiEvent.setReminderTriggerReferenceId("");
        boolean successfullyUpdated = updateEventIdsPersistent(sipMwiEvent);

        /**
         * If the update of the persistent storage is successful, cancel the events.
         * If not, do not cancel and let the scheduler retry later.
         */
        if (successfullyUpdated || tryToCancelAnyway) {
            // Second step is to cancel the scheduled events
            if (referenceId != null && referenceId.length() > 0) {
                logger.debug("SipMwiEventHandler cancel event: " + referenceId);
                super.cancelEvent(referenceId);
            }

            if (reminderRefrenceId != null && reminderRefrenceId.length() > 0) {
                logger.debug("SipMwiEventHandler cancel reminder trigger event: " + reminderRefrenceId);
                super.cancelReminderTriggerEvent(reminderRefrenceId);
            }
        } else {
            // Revert back to the original eventIds
            sipMwiEvent.keepReferenceID(referenceId);
            sipMwiEvent.setReminderTriggerReferenceId(reminderRefrenceId);

            result = false;
        }
        return result;
    }

    /**
     * Retrieves the persistent scheduler-id values from the subscriber (or non subscriber) private/events directory.
     * Values are stored in the SchedulerIds private member.
     * @param sipMwiEvent SipMwiEvent
     */
    public void retrieveSchedulerEventIdsPersistent(SipMwiEvent sipMwiEvent) {
        if (mfsEventManager == null) {
            mfsEventManager = MfsEventFactory.getMfsEvenManager();
        }

        Properties properties = mfsEventManager.getProperties(sipMwiEvent.getNotificationNumber(), SIPMWI_STATUS_FILE);
        if (properties != null) {
            sipMwiEvent.keepReferenceID(properties.getProperty(SCHEDULER_ID));
            sipMwiEvent.setReminderTriggerReferenceId(properties.getProperty(REMINDER_SCHEDULER_ID));
            logger.debug("Read the " + SIPMWI_STATUS_FILE + " file for " + sipMwiEvent +
                    " and retrieved referenceId: " + sipMwiEvent.getReferenceId() +
                    " and reminderReferenceId: " + sipMwiEvent.getReminderTriggerReferenceId());
        }
    }

    /**
     * Update the scheduler-id values for a subscriber (or non subscriber) in the private/events directory.
     * When the SipMwiEvent is for a reminder notification, then the reminder-scheduler-id is updated.
     * Values stored are taken from the SchedulerIds private member.
     * @param sipMwiEvent SipMwiEvent
     * @return boolean True if the update is successful, false otherwise
     */
    public boolean updateEventIdsPersistent(SipMwiEvent sipMwiEvent) {
        boolean result = true;

        if (mfsEventManager == null) {
            mfsEventManager = MfsEventFactory.getMfsEvenManager();
        }

        try {
            if (sipMwiEvent.isSchedulerIdEmpty() && sipMwiEvent.isReminderSchedulerIdEmpty()) {
                mfsEventManager.removeFile(sipMwiEvent.getNotificationNumber(), SIPMWI_STATUS_FILE);
                logger.debug("Removed the " + SIPMWI_STATUS_FILE + " file for " + sipMwiEvent + " (if it existed)");
            } else {
                String schedulerId = sipMwiEvent.getReferenceId();
                String reminderSchedulerId = sipMwiEvent.getReminderTriggerReferenceId();
                
                Properties properties = new Properties();
                properties.setProperty(SCHEDULER_ID, schedulerId != null ? schedulerId : "" );
                properties.setProperty(REMINDER_SCHEDULER_ID, reminderSchedulerId != null ? reminderSchedulerId : "" );

                logger.debug("Storing the new schedulerIds values for " + sipMwiEvent + "\nschedulerId: "+schedulerId + "\nreminderSchedulerId: "+reminderSchedulerId);
                mfsEventManager.storeProperties(sipMwiEvent.getNotificationNumber(), SIPMWI_STATUS_FILE, properties);
            }
        } catch (TrafficEventSenderException tese) {
            logger.error("Exception while SipMwiEventHandler.updateEventIdsPersistent", tese);
            result = false;
        }

        return result;
    }

    /**
     * Removes the SIPMWI_STATUS_FILE for the given notification number. 
     * @param sipMwiEvent SipMwiEvent
     */
    public void removeSchedulerIdsPersistent(SipMwiEvent sipMwiEvent) {
        if (mfsEventManager == null) {
            mfsEventManager = MfsEventFactory.getMfsEvenManager();
        }

        try {
            mfsEventManager.removeFile(sipMwiEvent.getNotificationNumber(), SIPMWI_STATUS_FILE);
            logger.debug("Removed the " + SIPMWI_STATUS_FILE + " file for " + sipMwiEvent + " (if it existed)");
        } catch (TrafficEventSenderException tese) {
            logger.error("Exception while SipMwiEventHandler.removeSchedulerIdsPersistent", tese);
        }
    }
    
    public boolean isSubscriberHasNewMessage(UserInfo userInfo, SipMwiEvent sipMwiEvent) {
        UserMailbox inbox = new UserMailbox(sipMwiEvent.getMsgInfo().rmsa,
                userInfo.hasMailType(Constants.NTF_EMAIL),
                userInfo.hasMailType(Constants.NTF_FAX),
                userInfo.hasMailType(Constants.NTF_VOICE),
                userInfo.hasMailType(Constants.NTF_VIDEO));

        if( inbox.getNewVoiceCount() > 0 ||
            inbox.getNewUrgentVoiceCount() > 0 || 
            inbox.getNewVideoCount() > 0 ||
            inbox.getNewUrgentVideoCount() > 0 ||
            inbox.getNewFaxCount() > 0 ||
            inbox.getNewUrgentFaxCount() > 0 ||
            inbox.getNewEmailCount() > 0 ||
            inbox.getNewUrgentEmailCount() > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public SIPFilterInfo getSIPFilterInfo(UserInfo userInfo, SipMwiEvent sipMwiEvent) {
        UserMailbox inbox = new UserMailbox(sipMwiEvent.getMsgInfo().rmsa,
                userInfo.hasMailType(Constants.NTF_EMAIL),
                userInfo.hasMailType(Constants.NTF_FAX),
                userInfo.hasMailType(Constants.NTF_VOICE),
                userInfo.hasMailType(Constants.NTF_VIDEO));

        NotificationFilter notifFilter = userInfo.getFilter();
        
        SIPFilterInfo sipFilterInfo = null;
        
        if ( sipMwiEvent.isFallback() ) {
            String[] numbers = notifFilter.getMatchingDeliveryProfileNumbers("MWI", Constants.TRANSPORT_IP);
            if( numbers != null ) {
                sipFilterInfo = new SIPFilterInfo( numbers );
            }
            
        } else {
            Object sipFilterInfoObject = ReminderUtil.getReminderFilterInfo(Constants.NTF_SIPMWI, new NotificationEmail(sipMwiEvent), inbox, notifFilter, null);
        
            if(sipFilterInfoObject instanceof SIPFilterInfo){
                sipFilterInfo = (SIPFilterInfo)sipFilterInfoObject;
            }
        }
        return sipFilterInfo;
    }

}

