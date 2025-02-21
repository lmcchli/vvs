/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.*;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.scheduler.handling.AppliEventHandler;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.coremgmt.reminder.ReminderUtil;
import com.abcxyz.services.moip.ntf.event.InvalidOdlEventException;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.event.OdlEvent;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.out.outdial.IEventStore;
import com.mobeon.ntf.out.outdial.OdlConstants;
import com.mobeon.ntf.out.outdial.OutdialNotificationOut;
import com.mobeon.ntf.userinfo.OdlFilterInfo;

/**
 * Class handling Outdial event
 */
public class OdlEventHandler extends NtfRetryEventHandler {

    private static LogAgent logger = NtfCmnLogger.getLogAgent(OdlEventHandler.class);
    private ManagedArrayBlockingQueue<Object> queue = null;
    private IEventStore eventStore;

    private static final int DEFAULT_WAITON_TIMEOUT = 15 * 60000;

    private int waitOnTimeOut = DEFAULT_WAITON_TIMEOUT;

    private AppliEventHandler startScheduler;
    private AppliEventHandler loginScheduler;
    private AppliEventHandler waitOnScheduler;
    private AppliEventHandler waitScheduler;
    private AppliEventHandler callScheduler;

    public OdlEventHandler() {
        // Default odl scheduler
        RetryEventInfo initialNotifRetryinfo = new RetryEventInfo(getEventServiceName());
        initialNotifRetryinfo.setEventRetrySchema("60 CONTINUE"); //schema for login check, hard coded as previous code
        initialNotifRetryinfo.setRequireRetryTime(true); //for allowing state machine dynamic time changes
        initialNotifRetryinfo.setExpireRetryTimerInMinute(Config.getOutdialExpiryIntervalInMin());
        initialNotifRetryinfo.setMaxExpireTries(Config.getOutdialExpiryRetries());

        // Reminder scheduler
        RetryEventInfo reminderRetryInfo = new RetryEventInfo(NtfEventTypes.EVENT_TYPE_ODL_REMINDER.getName());
        reminderRetryInfo.setEventRetrySchema(Config.getOutdialReminderIntervalInMin() + " CONTINUE");
        reminderRetryInfo.setExpireTimeInMinute(Config.getOutdialReminderExpireInMin());
        reminderRetryInfo.setExpireRetryTimerInMinute(Config.getOutdialExpiryIntervalInMin());
        reminderRetryInfo.setMaxExpireTries(Config.getOutdialExpiryRetries());

        // Initialise both Initial & Reminder retry handler
        init(initialNotifRetryinfo, reminderRetryInfo);

        // Start scheduler
        RetryEventInfo startRetryInfo = new RetryEventInfo(NtfEventTypes.EVENT_TYPE_ODL_START.getName());
        startRetryInfo.setEventRetrySchema(Config.getOutdialStartRetrySchema());
        startRetryInfo.setExpireTimeInMinute(Config.getOutdialStartExpireTimeInMin());
        startRetryInfo.setExpireRetryTimerInMinute(Config.getOutdialExpiryIntervalInMin());
        startRetryInfo.setMaxExpireTries(Config.getOutdialExpiryRetries());
        startScheduler = new AppliEventHandler(startRetryInfo, this);

        // Login scheduler
        RetryEventInfo loginRetryInfo = new RetryEventInfo(NtfEventTypes.EVENT_TYPE_ODL_LOGIN.getName());
        loginRetryInfo.setEventRetrySchema(Config.getOutdialLoginRetrySchema());
        loginRetryInfo.setExpireTimeInMinute(Config.getOutdialLoginExpireTimeInMin());
        loginRetryInfo.setExpireRetryTimerInMinute(Config.getOutdialExpiryIntervalInMin());
        loginRetryInfo.setMaxExpireTries(Config.getOutdialExpiryRetries());
        loginScheduler = new AppliEventHandler(loginRetryInfo, this);

        // WaitOn scheduler
        RetryEventInfo waitOnRetryinfo = new RetryEventInfo(NtfEventTypes.EVENT_TYPE_ODL_WAITON.getName());
        waitOnRetryinfo.setEventRetrySchema("60 CONTINUE");
        waitOnRetryinfo.setRequireRetryTime(true);
        waitOnRetryinfo.setExpireRetryTimerInMinute(Config.getOutdialExpiryIntervalInMin());
        waitOnRetryinfo.setMaxExpireTries(Config.getOutdialExpiryRetries());
        waitOnScheduler = new AppliEventHandler(waitOnRetryinfo, this);

        // Wait scheduler
        RetryEventInfo waitRetryinfo = new RetryEventInfo(NtfEventTypes.EVENT_TYPE_ODL_WAIT.getName());
        waitRetryinfo.setEventRetrySchema("60 CONTINUE");
        waitRetryinfo.setRequireRetryTime(true);
        waitRetryinfo.setExpireRetryTimerInMinute(Config.getOutdialExpiryIntervalInMin());
        waitRetryinfo.setMaxExpireTries(Config.getOutdialExpiryRetries());
        waitScheduler = new AppliEventHandler(waitRetryinfo, this);

        // Call scheduler
        RetryEventInfo callRetryInfo = new RetryEventInfo(NtfEventTypes.EVENT_TYPE_ODL_CALL.getName());
        callRetryInfo.setEventRetrySchema(Config.getOutdialCallRetrySchema());
        callRetryInfo.setExpireTimeInMinute(Config.getOutdialCallExpireTimeInMin());
        callRetryInfo.setExpireRetryTimerInMinute(Config.getOutdialExpiryIntervalInMin());
        callRetryInfo.setMaxExpireTries(Config.getOutdialExpiryRetries());
        callScheduler = new AppliEventHandler(callRetryInfo, this);
    }

	public String getEventServiceName() {
		return NtfEventTypes.OUTDIAL.getName();
	}

	public void keepOdlQueue(ManagedArrayBlockingQueue<Object> queue) {
		this.queue = queue;
	}

	public void keepEventStore(IEventStore eventStore) {
		this.eventStore = eventStore;
	}

	public void reset() {
		super.numOfCancelledEvent = new AtomicLong(0);
		super.numOfFiredExpireEvent = new AtomicLong(0);
		super.numOfFiredNotifEvent = new AtomicLong(0);
		super.numOfScheduledEvent = new AtomicLong(0);
	}

	/**
	 * Sets the time out for wait on requests.
	 * @param timeout Duration in milliseconds.
	 */
	public void setWaitOnTimeout(int timeout) {
		waitOnTimeOut = timeout;
	}

    /**
     * Schedule the next startup retry
     * @param odlEvent OdlEvent
     * @return AppliEventInfo
     */
    public AppliEventInfo scheduleStartRetry(OdlEvent odlEvent) {
        String key = odlEvent.getOdlEventKey();
        Properties props = odlEvent.getEventProperties();
        AppliEventInfo eventInfo = startScheduler.scheduleEvent(key + NtfEvent.getUniqueId(), NtfEventTypes.EVENT_TYPE_ODL_START.getName(), props);
        odlEvent.keepReferenceID(eventInfo.getEventId());

        logger.debug("Scheduled Start event: " + eventInfo.getEventId());

        return eventInfo;
    }

    /**
     * ReSchedule the next startup retry
     * @param odlEvent OdlEvent
     * @return AppliEventInfo
     */
    public AppliEventInfo rescheduleStartRetry(OdlEvent odlEvent) {
        String key = odlEvent.getOdlEventKey();
        Properties props = odlEvent.getEventProperties();
        String previousSchedulerIdStart = odlEvent.getReferenceId();

        AppliEventInfo eventInfo = startScheduler.scheduleEvent(key + NtfEvent.getUniqueId(), NtfEventTypes.EVENT_TYPE_ODL_START.getName(), props);
        odlEvent.keepReferenceID(eventInfo.getEventId());
        logger.debug("Scheduled Start event: " + eventInfo.getEventId());

        cancelEvent(previousSchedulerIdStart);

        return eventInfo;
    }

    /**
     * Schedule the next logged-in retry
     * @param odlEvent OdlEvent
     * @return AppliEventInfo
     */
    public AppliEventInfo scheduleLoginRetry(OdlEvent odlEvent) {
        String key = odlEvent.getOdlEventKey();
        Properties props = odlEvent.getEventProperties();
        AppliEventInfo eventInfo = loginScheduler.scheduleEvent(key + NtfEvent.getUniqueId(), NtfEventTypes.EVENT_TYPE_ODL_LOGIN.getName(), props);
        odlEvent.keepReferenceID(eventInfo.getEventId());

        logger.debug("Scheduled Login event: " + eventInfo.getEventId());

        return eventInfo;
    }

    /**
     * Schedule the next WaitOn retry
     * @param odlEvent OdlEvent
     * @return AppliEventInfo
     */
    public AppliEventInfo scheduleWaitOnRetry(OdlEvent odlEvent, long retryPeriod, long timeOutExpiry) {
        String key = odlEvent.getOdlEventKey();
        Properties props = odlEvent.getEventProperties();

        // only use retry period for sms type 0 case
        long when;
        if(Config.getPhoneOnMethod().equalsIgnoreCase(Config.PHONE_ON_SMS_TYPE_0)) {
            long nextRetryWhen = retryPeriod + System.currentTimeMillis();
            long expiryWhen = timeOutExpiry + odlEvent.getStartTime();
            when = Math.min(nextRetryWhen, expiryWhen);
            logger.debug("scheduleWaitOnRetry:: sys type 0 case; nextRetryWhen: " + nextRetryWhen + " expiryWhen : " + expiryWhen + " when : " + when);
        } else {
            when = timeOutExpiry + System.currentTimeMillis();
            logger.debug("scheduleWaitOnRetry:: Alert sc case; when : " + when);
        }

        AppliEventInfo eventInfo = waitOnScheduler.scheduleEvent(when, key  + NtfEvent.getUniqueId(), NtfEventTypes.EVENT_TYPE_ODL_WAITON.getName(), props);
        odlEvent.keepReferenceID(eventInfo.getEventId());

        logger.debug("Scheduled WaitOn event: " + eventInfo.getEventId());

        return eventInfo;
    }

    /**
     * Schedule the next logged-in retry
     * @param odlEvent OdlEvent
     * @return AppliEventInfo
     */
    public AppliEventInfo scheduleWaitRetry(OdlEvent odlEvent, long waitTimeInSecond) {
        String key = odlEvent.getOdlEventKey();

        Properties props = odlEvent.getEventProperties();
        long when;
        if (waitTimeInSecond < 60) {
            when = (120 + waitTimeInSecond) * 1000 + System.currentTimeMillis();
            try {
                odlEvent.startNtfTimerTask(OutdialNotificationOut.get().getQueue(), waitTimeInSecond);
                logger.debug("OdlEventHandlerWait scheduled event: " + waitTimeInSecond);
            } catch (IllegalStateException e) {
                logger.error("NtfTimerTask fails: " + odlEvent);
            }
        } else {
            when = waitTimeInSecond * 1000 + System.currentTimeMillis();
        }

        AppliEventInfo eventInfo = waitScheduler.scheduleEvent(when, key + NtfEvent.getUniqueId(), NtfEventTypes.EVENT_TYPE_ODL_WAIT.getName(), props);
        odlEvent.keepReferenceID(eventInfo.getEventId());

        logger.debug("Scheduled Wait event: " + eventInfo.getEventId());

        return eventInfo;
    }

    /**
     * Schedule the next Call retry
     * @param odlEvent OdlEvent
     */
	public AppliEventInfo scheduleCallRetry(OdlEvent odlEvent) {
	    String key = odlEvent.getOdlEventKey();
        Properties props = odlEvent.getEventProperties();
        AppliEventInfo eventInfo = callScheduler.scheduleEvent(key + NtfEvent.getUniqueId(), NtfEventTypes.EVENT_TYPE_ODL_CALL.getName(), props);
        odlEvent.keepReferenceID(eventInfo.getEventId());

        logger.debug("Scheduled Call event: " + eventInfo.getEventId());

        return eventInfo;
    }

	@Override
	public long getNextRetryTime(AppliEventInfo eventInfo) {
	    if (eventInfo.getEventType().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_WAITON.getName())) {
	        return waitOnTimeOut + System.currentTimeMillis();
	    } else {
	        //here it's possible to stop next retry.
	    }
	    return EventHandleResult.OK; //default schema will be used for next retry event
	}

	@Override
	public int eventFired(AppliEventInfo eventInfo) {
	    int result = EventHandleResult.OK;

	    try {
	        numOfFiredNotifEvent.incrementAndGet();

	        eventStore = OutdialNotificationOut.get().getEventStore();
	        queue = OutdialNotificationOut.get().getQueue();

	        // Retrieve the EventId (for the serviceName)
	        EventID eventId = null;
	        try {
	            if (eventInfo.getEventId() != null && eventInfo.getEventId().length() > 0) {
	                eventId = new EventID(eventInfo.getEventId());
	            }
	            if (eventId == null) {
	                logger.error("Invalid EventId for event " + eventInfo);
	                return EventHandleResult.STOP_RETRIES;
	            }
	        } catch (InvalidEventIDException ite) {
	            logger.error("Invalid EventId for event " + eventInfo, ite);
	            return EventHandleResult.STOP_RETRIES;
	        }

	        logger.debug("OdlEventHandler event fired: " + eventInfo.getEventId() + " total: " + numOfFiredNotifEvent);

	        Properties props = eventInfo.getEventProperties();
	        OdlEvent event = null;

	        try {
	            event = new OdlEvent(props);
	        } catch (InvalidOdlEventException e) {
	            logger.error("Invalid event: " + eventInfo.getEventId());
	            return EventHandleResult.STOP_RETRIES;
	        }

	        if (!OutdialNotificationOut.get().isStarted()) {
	            logger.error("Received Outdial event but service is not available, will retry");
	            return EventHandleResult.OK;
	        }

	        // Validate if the subscriber's storage is READ-ONLY (using the notification number)
	        if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(event.getTelNumber())) {
	            logger.warn("Storage currently not available to process Outdial event for " + event.getRecipentId() + " : " + event.getTelNumber() + ", will retry");
	            return EventHandleResult.OK;
	        }

	        OdlEvent odlEvent = eventStore.get(event.getRecipentId(), event.getTelNumber());
	        if (odlEvent == null) {
	            logger.warn("No persistent storage found for event: " + eventInfo.getEventId() + " for " + event.getRecipentId() + " : " + event.getTelNumber());
	            return EventHandleResult.STOP_RETRIES;
	        }

	        /**
	         * When retrieving eventIds, the validity period of the file is not considered for fired events.
	         * (as opposed to new notification) since there is payload information to handle, inject the event.
	         */

	        /**
	         * Validate that the eventId stored in storage and that the fired eventId match.
	         * If it's not the case, that means that an NTF from an other site already handled the outdial.
	         * In that case, just cancel the scheduler.
	         */
	        if (!isSchedulerEventValid(eventInfo, odlEvent)) {
	            return EventHandleResult.STOP_RETRIES;
	        }

	        // Validate if its an expiry event notification for level-3 scheduling
	        if (eventInfo.getNextEventInfo() == null || eventInfo.isExpire() || eventInfo.isLastExpire()) {
	            logger.debug("EventFired: Expiry event: " + eventInfo.getEventId());
	            odlEvent.setExpiry();

	            /**
	             * Even if the case of an expired event, EventHandleResult.OK MUST be returned (so that the Scheduler will retry
	             * this expired event) instead of EventHandleResult.STOP_RETRIES since the worker thread that will process this
	             * event might die/not process it. If the worker thread process the event successfully, then it MUST cancel the next
	             * eventId.
	             */
	            result = EventHandleResult.OK;
	        }

	        odlEvent.setFromNotify(false);
	        odlEvent.setEventServiceName(eventId.getServiceName());
	        odlEvent.setEventServiceTypeKey(eventInfo.getEventType());

	        logger.debug("OdlEventHandler event, getEventId: " + eventInfo.getEventId() +
	                " getEventKey: " + eventInfo.getEventKey() +
	                " getEventType: " + eventInfo.getEventType() +
	                " for subscriber " + odlEvent.getIdentity());

	        boolean checkReadStatus = false;
	        if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_START.getName())) {
	            odlEvent.setOdlTrigger(OdlConstants.EVENT_OUTDIAL_START);
	            checkReadStatus = true;
	        } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_LOGIN.getName())) {
	            odlEvent.setFromLogin(true);
	            checkReadStatus = true;
	        } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_WAIT.getName())) {
	            eventInfo.setNextEventInfo(null);
	            result = EventHandleResult.STOP_RETRIES;
	            //should not have any trigger
	            odlEvent.setOdlTrigger(null);
	        } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_WAITON.getName())) {
	            // Do nothing, let the OdlWorker handling this case
	        } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_CALL.getName())) {
	            // Do nothing, let the OdlWorker handling this case
	        } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_REMINDER.getName())) {

	            if (!Config.isOutdialReminderEnabled()) {
	                logger.info("Outdial Reminder feature not enabled anymore for " + odlEvent.getIdentity());

	                // Get the next eventInfo.eventId
	                updateScheduledEventsIds(eventInfo, eventId, odlEvent);

	                // Cancel schedulerIdReminder timer
	                if (odlEvent.getSchedulerIdReminder() != null) {
	                    logger.debug("Cancelling eventId: " + odlEvent.getSchedulerIdReminder());
	                    cancelReminderTriggerEvent(odlEvent.getSchedulerIdReminder());
	                }

	                // Remove from persistent storage
	                odlEvent.setSchedulerIdReminder(null);
	                odlEvent.notifyObservers();

	                // Force Scheduler to stop retries since OutdialReminder is not enabled anymore.
	                return EventHandleResult.STOP_RETRIES;
	            }

	            checkReadStatus = true;
	            odlEvent.setReminder();

	            if (!odlEvent.isExpiry()) {
	                // If there is a PhoneOn pending, no need to try Reminder notification
	                if (isWaitingPhoneOn(odlEvent)) {
	                    // Update the next Reminder trigger timer
	                    updateScheduledEventsIds(eventInfo, eventId, odlEvent);
	                    logger.debug("Subscriber " + odlEvent.getIdentity() + " already waiting for PhoneOn, the reminder will retry later " + odlEvent.getSchedulerIdReminder());
	                    odlEvent.notifyObservers();
	                    return result;
	                } else {
	                    // No PhoneOn waiting while receiving a reminder retry, remind the subscriber.
	                    odlEvent.setOdlTrigger(OdlConstants.EVENT_OUTDIAL_START);
	                    logger.debug("Subscriber " + odlEvent.getIdentity() + " to receive a Reminder retry");
	                }
	            } else {
	                // No validation is performed if the event is an expiry, the OdlWorker will handle it by cancelling
	            }
	        }

	        if (checkReadStatus && !odlEvent.isExpiry()) {
	            NotificationEmail email = new NotificationEmail(odlEvent);
	            try {
	                email.init();
	            } catch (MsgStoreException mse) {
	                logger.debug("Unable to initialize NotificationEmail properly, could be change in mailbox content (deleted voice mail etc), continuing to check state.");
	                logger.debug("msgcoreException : ", mse);
	            } catch (Exception e) {
	                logger.error("Unexpected Exception while init NotificationEmail, continuing to check state anyway: ", e);
	            }

	            OdlFilterInfo filterInfo = ReminderUtil.getReminderOdlFilterInfo(email, null);
	            if (filterInfo != null) {
	                logger.debug("Subscriber's mailbox " + odlEvent.getIdentity() + " still contains new messages.");
	            } else {
	                logger.debug("Subscriber " + odlEvent.getIdentity() + " either does not have new messages or Outdial is not a preferred notification type anymore), stop notification.");

	                // Cancel schedulerIdReminder timer
	                cancelReminderTriggerEvent(odlEvent.getSchedulerIdReminder());

	                // Cancel referenceId timer
	                cancelEvent(odlEvent.getReferenceId());

	                // Remove event from persistent storage
	                eventStore.remove(odlEvent);

	                return EventHandleResult.STOP_RETRIES;
	            }
	        }

	        // Update the next retry (schedulerId or schedulerIdReminder)
	        updateScheduledEventsIds(eventInfo, eventId, odlEvent);
	        odlEvent.notifyObservers();

	        //send to worker
	        try {
	            queue.put(odlEvent);
	        } catch (Throwable t) {
	            return EventHandleResult.ERR_QUEUE_FULL;
	        }

	    } catch (Exception e) {
	        String message = "Event fired exception for " + eventInfo.getEventId();
	        if (eventInfo.getNextEventInfo() != null) {
	            logger.warn(message + ", will retry. ", e);
	        } else {
	            logger.error(message + ", will not retry. ", e);
	        }
	    }
	    return result;
	}

    /**
     * Compare the received eventId with the persistent eventId stored for the given subscriber. If the two eventIds match, this
     * means that the event fired is the one NTF is expecting. If not, it means that this NTF received an old eventId.
     *
     * @param firedEventInfo AppliEventInfo
     * @param odlEvent OdlEvent
     * @return true if the event should be processed, false otherwise.
     */
	private boolean isSchedulerEventValid(AppliEventInfo firedEventInfo, OdlEvent odlEvent) {
        EventID eventId = null;
        try {
            if (firedEventInfo.getEventId() != null && firedEventInfo.getEventId().length() > 0) {
                eventId = new EventID(firedEventInfo.getEventId());
            }

            if (eventId == null) {
                logger.warn("Invalid EventId for event " + firedEventInfo);
                return false;
            }

            boolean shouldProcessFiredEvent = false;
            String storedEvent = null;
            if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_REMINDER.getName())) {
                // REMINDER case
                storedEvent = odlEvent.getSchedulerIdReminder();
            } else {
                // START, LOGIN, WAIT, WAITON, CALL cases
                storedEvent = odlEvent.getReferenceId();
            }

            shouldProcessFiredEvent = CommonMessagingAccess.getInstance().compareEventIds(firedEventInfo, storedEvent);
            if (!shouldProcessFiredEvent) {
                logger.info("EventFired: EventIds not matching: firedEvent: " + eventId + ", storedEvent: " + storedEvent + ", stop retry");
            }

            return shouldProcessFiredEvent;

        } catch (InvalidEventIDException e) {
            logger.error("Invalid EventId for event " + firedEventInfo);
            return false;
        }
    }

    private void updateScheduledEventsIds(AppliEventInfo eventInfo, EventID eventId, OdlEvent odlEvent) {
	    // Update the next retry (schedulerId or schedulerIdReminder)
	    String nextEventId = null;
	    if (eventInfo.getNextEventInfo() != null) {
	        nextEventId = eventInfo.getNextEventInfo().getEventId();
	        logger.debug("OdlEventHandler: Next retry event is " + eventInfo.getNextEventInfo().getEventId());
	    }

	    if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_REMINDER.getName())) {
	        // REMINDER case
	        odlEvent.setSchedulerIdReminder(nextEventId);
	    } else {
	        // START, LOGIN, WAIT, WAITON, CALL cases
	        odlEvent.keepReferenceID(nextEventId);
	    }
	}

	private boolean isWaitingPhoneOn(OdlEvent odlEvent) {
	    boolean waitingPhoneOn = false;
	    try {
	        EventID schedulerId = null;
	        if (odlEvent.getReferenceId() != null && odlEvent.getReferenceId().length() > 0) {
	            schedulerId = new EventID(odlEvent.getReferenceId());
	            if (schedulerId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_WAITON.getName())) {
	                logger.debug("Subscriber " + odlEvent.getIdentity() + " is already waiting for PhoneOn: " + schedulerId);
	                waitingPhoneOn = true;
	            }
	        }
	    } catch (InvalidEventIDException ieie) {
	        logger.error("InvalidEventIDException: ", ieie);
	    }
	    return waitingPhoneOn;
	}
}
