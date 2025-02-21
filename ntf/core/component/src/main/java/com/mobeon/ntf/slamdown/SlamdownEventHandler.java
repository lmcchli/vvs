/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.slamdown;

import java.util.concurrent.atomic.AtomicLong;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.ntf.slamdown.event.SlamdownEvent;

/**
 * This class handles Slamdown events
 */
public class SlamdownEventHandler extends NtfRetryEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(SlamdownEventHandler.class);

    public SlamdownEventHandler() {
        RetryEventInfo info = new RetryEventInfo(getEventServiceName());
        super.init(info);
    }

    public String getEventServiceName() {
        return NtfEventTypes.SLAMDOWN_L3.getName();
    }

    public void reset() {
        super.numOfCancelledEvent = new AtomicLong(0);
        super.numOfFiredExpireEvent = new AtomicLong(0);
        super.numOfFiredNotifEvent = new AtomicLong(0);
        super.numOfScheduledEvent = new AtomicLong(0);
    }

    /**
     * Schedule an event for aggregation
     * 
     * @param slamdownList
     *        SlamdownEvent
     */
    public AppliEventInfo scheduleAggregate(SlamdownList slamdownList) {
        AppliEventInfo eventInfo = eventHandler.scheduleEvent(slamdownList.getNotificationNumber() + NtfEvent.getUniqueId(),
                NtfEventTypes.SLAMDOWN_AGGREGATION.getName(), slamdownList.getEventProperties());
        log.debug("Scheduled Aggregate event: " + eventInfo.getEventId());
        return eventInfo;
    }

    @Override
    public int eventFired(AppliEventInfo eventInfo) {
        int result = EventHandleResult.OK;

        try {
            SlamdownEvent slamdownEvent = null;
            boolean storedInQueue = false;
            slamdownEvent = new SlamdownEvent(eventInfo.getEventProperties());

            log.debug("EventFired: " + slamdownEvent.getSubscriberNumber() + " : " + slamdownEvent.getNotificationNumber() + " " + eventInfo.getEventType());
            numOfFiredNotifEvent.incrementAndGet();

            if (!SlamdownListHandler.get().isStarted()) {
                log.error("Received slamdown event but service is not available");
                return EventHandleResult.OK;
            }

            // Validate if the subscriber's storage is READ-ONLY (using the notification number)
            if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(slamdownEvent.getNotificationNumber())) {
                log.warn("Storage currently not available to process Slamdown event for " + slamdownEvent.getSubscriberNumber() + " : " + slamdownEvent.getNotificationNumber() + ", will retry");
                return EventHandleResult.OK;
            }

            SlamdownList[] slamdownList = SlamdownList.recreateSlamdownList(slamdownEvent.getNotificationNumber());
            if (slamdownList != null && slamdownList.length > 0) {

                log.debug("EventFired: Event found on disk: " + eventInfo.getEventId());

                for (SlamdownList sl : slamdownList) {
                    /**
                     * When retrieving eventIds, the validity period of the file is not considered for fired events.
                     * (as opposed to new notification) since there is payload information to handle, inject the event.
                     */
                    sl.retrieveSchedulerEventIdsPersistent();

                    if (!shouldProcessFiredEvent(eventInfo, sl)) {
                        return EventHandleResult.STOP_RETRIES;
                    }

                    /**
                     * Update the next eventId: - even though it could be null, the persistent storage must be updated accordingly; -
                     * Worker thread might cancel future schedules, therefore, the latest values must be stored.
                     */
                    sl.updateScheduledEventsIds(eventInfo);
                }

                /**
                 * In the case of having 2 slamdownLists, there is no reason to update the persistent file twice with the same
                 * information, just perform the operation using the first slamdownList.
                 */
                boolean successfullyUpdated = slamdownList[0].updateEventIdsPersistent();
                if (!successfullyUpdated) {
                    log.warn("Unable to update persistent storage with new eventId for " + slamdownEvent.getSubscriberNumber() + " : " + slamdownEvent.getNotificationNumber() + ", will retry");
                    return EventHandleResult.OK;
                }

                for (SlamdownList sl : slamdownList) {
                    if (eventInfo.getNextEventInfo() == null || eventInfo.isExpire() || eventInfo.isLastExpire()) {
                        /**
                         * Even if the case of an expired event, EventHandleResult.OK MUST be returned (so that the Scheduler will retry
                         * this expired event) instead of EventHandleResult.STOP_RETRIES since the worker thread that will process this
                         * event might die/not process it. If the worker thread process the event successfully, then it MUST cancel the next
                         * eventId.
                         */
                        log.debug("EventFired: Expiry event: " + eventInfo.getEventId());
                        sl.setCurrentEvent(SlamdownList.EVENT_SCHEDULER_EXPIRY);
                    } else {
                        sl.setCurrentEvent(SlamdownList.EVENT_SCHEDULER_RETRY);
                    }

                    storedInQueue = SlamdownListHandler.get().getWorkingQueue().offer(sl);
                    if (storedInQueue) {
                        log.debug("EventFired: Stored in workingQueue : " + sl.getNotificationNumber());
                    } else {
                        log.warn("EventFired: Not stored in workingQueue (full), will retry : " + sl.getNotificationNumber());
                    }
                }

            } else {
                log.debug("EventFired: Event not found on disk (another NTF instance proccessed the notification) " + eventInfo.getEventId());

                // Remove the persistent scheduler ids (if it exists)
                SlamdownList.removeSchedulerIdsPersistent(slamdownEvent.getNotificationNumber());

                if (eventInfo.getNextEventInfo() != null) {
                    // Cancel this scheduler event
                    super.cancelEvent(eventInfo.getNextEventInfo().getEventId());
                }

                result = EventHandleResult.STOP_RETRIES;
            }
        } catch (Exception e) {
            String message = "Event fired exception for " + eventInfo.getEventId();
            if (eventInfo.getNextEventInfo() != null) {
                log.warn(message + ", will retry. ", e);
            } else {
                log.error(message + ", will not retry. ", e);
            }
        }
        return result;
    }

    /**
     * Compare the received eventId with the persistent eventId stored for the given subscriber. If the two eventIds match, this
     * means that the event fired is the one NTF is expecting. If not, it means that this NTF received an old eventId.
     * 
     * @param firedEventInfo AppliEventInfo
     * @param slamdownList SlamdownList
     * @return true if the event should be processed, false otherwise.
     */
    private boolean shouldProcessFiredEvent(AppliEventInfo firedEventInfo, SlamdownList slamdownList) {

        EventID eventId = null;
        try {
            if (firedEventInfo.getEventId() != null && firedEventInfo.getEventId().length() > 0) {
                eventId = new EventID(firedEventInfo.getEventId());
            }

            if (eventId == null) {
                log.warn("Invalid EventId for event " + firedEventInfo);
                return false;
            }

            boolean shouldProcessFiredEvent = false;
            String storedEvent = null;
            if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.SLAMDOWN_SMS_UNIT.getName())) {
                storedEvent = slamdownList.getSchedulerIds().getSmsUnitEventId();
                slamdownList.setCurrentState(SlamdownList.STATE_SENDING_UNIT);
            } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.SLAMDOWN_SMS_TYPE_0.getName())) {
                storedEvent = slamdownList.getSchedulerIds().getSmsType0EventId();
                slamdownList.setCurrentState(SlamdownList.STATE_WAITING_PHONE_ON);
            } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.SLAMDOWN_SMS_INFO.getName())) {
                storedEvent = slamdownList.getSchedulerIds().getSmsInfoEventId();
                slamdownList.setCurrentState(SlamdownList.STATE_SENDING_INFO);
            } else {
                log.debug("Invalid eventInfo received: " + eventId);
            }

            shouldProcessFiredEvent = CommonMessagingAccess.getInstance().compareEventIds(firedEventInfo, storedEvent);
            if (!shouldProcessFiredEvent) {
                log.info("EventFired: EventIds not matching: firedEvent: " + eventId + ", storedEvent: " + storedEvent + ", stop retry");
            }

            return shouldProcessFiredEvent;

        } catch (InvalidEventIDException e) {
            log.error("Invalid EventId for event " + firedEventInfo);
            return false;
        }
    }

    /**
     * This method TRIES to cancel the SmsUnit eventId by updating the persistent storage and invoking the scheduler.cancel.
     * In the plausible case of being unable to update the persistent storage (because of an I/O Exception for example),
     * the method will return false - which would lead the clients of this method not to go forward with their next
     * operation and, instead, wait for a retry of this eventId (by the scheduler).
     * 
     * In the case of a client already backed-up by another eventId type, the client COULD decide to cancel the SmsUnit
     * eventId anyway regardless if the persistent storage operation is successful or not.
     * This operation MUST be considered as a 'best effort' since the eventId COULD be stored on another site
     * (in a Geo-Distributed solution) and therefore, not be cancelled successfully.
     *  
     * @param slamdownList SlamdownList
     * @param tryToCancelAnyway True if the cancel operation SHOULD be tried even if the persistent storage update is not successful 
     * @return True if the cancel operation is successful, false otherwise
     */
    public boolean cancelSmsUnitEvent(SlamdownList slamdownList, boolean tryToCancelAnyway) {
        boolean result = true;
        String smsUnitEventId = slamdownList.getSchedulerIds().getSmsUnitEventId();

        // First step is to nullify the value on disk
        slamdownList.getSchedulerIds().setSmsUnitEventId(null);
        boolean successfullyUpdated = slamdownList.updateEventIdsPersistent();

        /**
         * If the update of the persistent storage is successful, cancel the event.
         * If not, do not cancel it and let the scheduler retry later.
         */
        if (successfullyUpdated || tryToCancelAnyway) {
            // Second step is to cancel the scheduled event
            log.debug("SlamdownEventHandler cancel SmsUnit event: " + smsUnitEventId);
            super.cancelEvent(smsUnitEventId);
        } else {
            log.warn("Unable to cancel SmsUnit event and update persistent storage for " + slamdownList.getSubscriberNumber());

            // Revert back to the original eventIds
            slamdownList.getSchedulerIds().setSmsUnitEventId(smsUnitEventId);

            result = false;
        }
        return result;
    }

    /**
     * This method TRIES to cancel the SmsType0 eventId by updating the persistent storage and invoking the scheduler.cancel.
     * In the plausible case of being unable to update the persistent storage (because of an I/O Exception for example),
     * the method will return false - which would lead the clients of this method not to go forward with their next
     * operation and, instead, wait for a retry of this eventId (by the scheduler).
     * 
     * In the case of a client already backed-up by another eventId type, the client COULD decide to cancel the SmsType0
     * eventId anyway regardless if the persistent storage operation is successful or not.
     * This operation MUST be considered as a 'best effort' since the eventId COULD be stored on another site
     * (in a Geo-Distributed solution) and therefore, not be cancelled successfully.
     *  
     * @param slamdownList SlamdownList
     * @param tryToCancelAnyway True if the cancel operation SHOULD be tried even if the persistent storage update is not successful 
     * @return True if the cancel operation is successful, false otherwise
     */
    public boolean cancelSmsType0Event(SlamdownList slamdownList, boolean tryToCancelAnyway) {
        boolean result = true;
        String smsUnitEventId = slamdownList.getSchedulerIds().getSmsUnitEventId();
        String smsType0EventId = slamdownList.getSchedulerIds().getSmsType0EventId();

        // First step is to nullify the values on disk
        slamdownList.getSchedulerIds().setSmsUnitEventId(null);
        slamdownList.getSchedulerIds().setSmsType0EventId(null);
        boolean successfullyUpdated = slamdownList.updateEventIdsPersistent();

        /**
         * If the update of the persistent storage is successful, cancel the events.
         * If not, do not cancel and let the scheduler retry later.
         */
        if (successfullyUpdated || tryToCancelAnyway) {
            // Second step is to cancel the scheduled events
            if (smsUnitEventId != null && !smsUnitEventId.isEmpty()) {
                log.debug("SlamdownEventHandler cancel SMS-Unit event: " + smsUnitEventId);
                super.cancelEvent(smsUnitEventId);
            }

            if (smsType0EventId != null && !smsType0EventId.isEmpty()) {
                log.debug("SlamdownEventHandler cancel SMS-Type-0 event: " + smsType0EventId);
                super.cancelEvent(smsType0EventId);
            }
        } else {
            log.warn("Unable to cancel SmsType0 event and update persistent storage for " + slamdownList.getSubscriberNumber());

            // Revert back to the original eventIds
            slamdownList.getSchedulerIds().setSmsUnitEventId(smsUnitEventId);
            slamdownList.getSchedulerIds().setSmsType0EventId(smsType0EventId);

            result = false;
        }
        return result;
    }

    /**
     * This method TRIES to cancel the SmsInfo eventId by updating the persistent storage and invoking the scheduler.cancel.
     * In the plausible case of being unable to update the persistent storage (because of an I/O Exception for example),
     * the method will return false - which would lead the clients of this method not to go forward with their next
     * operation and, instead, wait for a retry of this eventId (by the scheduler).
     * 
     * In the case of a client already backed-up by another eventId type, the client COULD decide to cancel the SmsInfo
     * eventId anyway regardless if the persistent storage operation is successful or not.
     * This operation MUST be considered as a 'best effort' since the eventId COULD be stored on another site
     * (in a Geo-Distributed solution) and therefore, not be cancelled successfully.
     *  
     * In the particular case of cancelling all the events, all the clients SHOULD try to cancel the eventIds anyway
     * since this method is usually used at the end of a Slamdown/Mcn notification process or when a faulty operation
     * has been encountered.
     *  
     * @param slamdownList SlamdownList
     * @param tryToCancelAnyway True if the cancel operation SHOULD be tried even if the persistent storage update is not successful 
     * @return True if the cancel operation is successful, false otherwise
     */
    public boolean cancelAllEvents(SlamdownList slamdownList, boolean tryToCancelAnyway) {
        boolean result = true;
        String smsUnitEventId = slamdownList.getSchedulerIds().getSmsUnitEventId();
        String smsType0EventId = slamdownList.getSchedulerIds().getSmsType0EventId();
        String smsInfoEventId = slamdownList.getSchedulerIds().getSmsInfoEventId();

        // First step is to nullify the values on disk
        slamdownList.getSchedulerIds().nullify();
        boolean successfullyUpdated = slamdownList.updateEventIdsPersistent();

        /**
         * If the update of the persistent storage is successful, cancel the events.
         * If not, do not cancel and let the scheduler retry later.
         */
        if (successfullyUpdated || tryToCancelAnyway) {
            // Second step is to cancel the scheduled events
            if (smsUnitEventId != null && !smsUnitEventId.isEmpty()) {
                log.debug("SlamdownEventHandler cancel SMS-Unit event: " + smsUnitEventId);
                super.cancelEvent(smsUnitEventId);
            }

            if (smsType0EventId != null && !smsType0EventId.isEmpty()) {
                log.debug("SlamdownEventHandler cancel SMS-Type-0 event: " + smsType0EventId);
                super.cancelEvent(smsType0EventId);
            }

            if (smsInfoEventId != null && !smsInfoEventId.isEmpty()) {
                log.debug("SlamdownEventHandler cancel SMS-Info event: " + smsInfoEventId);
                super.cancelEvent(smsInfoEventId);
            }
        } else {
            log.warn("Unable to cancel all events and update persistent storage for " + slamdownList.getSubscriberNumber());

            // Revert back to the original eventIds
            slamdownList.getSchedulerIds().setSmsUnitEventId(smsUnitEventId);
            slamdownList.getSchedulerIds().setSmsType0EventId(smsType0EventId);
            slamdownList.getSchedulerIds().setSmsInfoEventId(smsInfoEventId);

            result = false;
        }
        return result;
    }
}
