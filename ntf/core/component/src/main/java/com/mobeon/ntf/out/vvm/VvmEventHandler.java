/**
 * Copyright (c) 2010 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.vvm;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * This class handles VVM events
 */
public class VvmEventHandler extends NtfRetryEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(VvmEventHandler.class);
    private MerAgent mer; 

    public VvmEventHandler() {
        RetryEventInfo info = new RetryEventInfo(getEventServiceName());
        info.setEventRetrySchema(Config.getVvmSmsUnitRetrySchema());        //RetrySchema kept for backward compatibility with legacy VVM_L3 event
        info.setExpireTimeInMinute(Config.getVvmSmsUnitExpireTimeInMin());  //ExpireTimeInMinute kept for backward compatibility with legacy VVM_L3 event
        super.init(info);
        this.mer = MerAgent.get();
        
    }

    public String getEventServiceName() {
        return NtfEventTypes.VVM_L3.getName();
    }

    public void reset() {
    	super.numOfCancelledEvent.set(0);
    	super.numOfFiredExpireEvent.set(0);
    	super.numOfFiredNotifEvent.set(0);
    	super.numOfScheduledEvent.set(0);
    }


    @Override
    public int eventFired(AppliEventInfo eventInfo) {

        int result = EventHandleResult.OK;

        try {
            VvmEvent vvmEvent = null;
            boolean storedInQueue = false;
            
            numOfFiredNotifEvent.incrementAndGet();

            if (!VvmHandler.get().isStarted()) {
                log.error("Received vvm event but service is not available, will retry");
                return EventHandleResult.OK;
            }
            
            
            if( eventInfo.getEventType().equalsIgnoreCase(NtfEventTypes.VVM_L3.getName()) /*&& 
                props.getProperty(VvmEvent.SERVICE_TYPE_KEY) == null*/) {
                /**
                 *  A scheduled VVM Event with type VVM_L3 is assumed to be a legacy event (before MiO 3.2) since events scheduled
                 *  with current code would have event type VVM_SMS_INFO, VVM_SENDING_PHONEON, VVM_WAIT_PHONEON or VVM_DEACTIVATOR.
                 *  
                 *  Legacy event is canceled and a new one is scheduled to ensure backward compatibility.
                 *  
                 */
                if(handleLegacyVvmNotif(eventInfo)) {
                    return EventHandleResult.STOP_RETRIES;
                } else {
                    return EventHandleResult.OK;
                }
            }
                
            Properties props = eventInfo.getEventProperties();
            vvmEvent = new VvmEvent(props);
            String subscriberNumber = vvmEvent.getSubscriberNumber();
            
            log.debug("EventFired: " + subscriberNumber + " " + eventInfo.getEventType());
            
            // Validate if the subscriber's storage is READ-ONLY (using the notification number)
            if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(vvmEvent.getSubscriberNumber())) {
                log.warn("Storage currently not available to process Vvm event for " + vvmEvent.getSubscriberNumber() + ", will retry");
                return EventHandleResult.OK;
            }
            
            if( isEventTypeActivityDetected(eventInfo) ) {
                /**
                 * For Vvm Activity Detected event, NTF MUST NOT affect the persistent storage of VVM notification since this event type
                 * does not use a status file.
                 */
                
                vvmEvent.setCurrentState(VvmEvent.STATE_ACTIVATOR);
                
                /**
                 * Update the next eventId: - even though it could be null, the persistent storage must be updated accordingly; -
                 * Worker thread might cancel future schedules, therefore, the latest values must be stored.
                 */
                if (eventInfo.getNextEventInfo() != null) {
                    vvmEvent.getSchedulerIds().setActivatorEventId(eventInfo.getNextEventInfo().getEventId());
                }
             
                if (eventInfo.getNextEventInfo() == null || eventInfo.isExpire() || eventInfo.isLastExpire()) {
                    /**
                     * Even if the case of an expired event, EventHandleResult.OK MUST be returned (so that the Scheduler will retry
                     * this expired event) instead of EventHandleResult.STOP_RETRIES since the worker thread that will process this
                     * event might die/not process it. If the worker thread process the event successfully, then it MUST cancel the next
                     * eventId.
                     */
                    log.debug("EventFired: Expiry event: " + eventInfo.getEventId());
                    vvmEvent.setCurrentEvent(VvmEvent.VVM_EVENT_SCHEDULER_EXPIRY);
                } else {
                    vvmEvent.setCurrentEvent(VvmEvent.VVM_EVENT_SCHEDULER_RETRY);
                }
                                
                // Validate if the subscriber still has VVM
                UserInfo userInfo = UserFactory.findUserByTelephoneNumber(subscriberNumber);
                if (userInfo == null || !userInfo.isVVMActivated() || !userInfo.hasVvmService()) {
                    log.debug("Subscriber does not have VVM service enabled, pending VVM Activity Detected event will be canceled");
                    
                    return EventHandleResult.STOP_RETRIES;
                }

                // Initialize userInfo, userMailbox and notificationEmail
                vvmEvent.initUser(userInfo);
                
            } else {
                
                vvmEvent.retrieveSchedulerEventIdsPersistent();
                if (!shouldProcessFiredEvent(eventInfo, vvmEvent)) {
                    return EventHandleResult.STOP_RETRIES;
                }
                
                /**
                 * Update the next eventId: - even though it could be null, the persistent storage must be updated accordingly; -
                 * Worker thread might cancel future schedules, therefore, the latest values must be stored.
                 */
                vvmEvent.updateScheduledEventsIds(eventInfo);
                    
                boolean successfullyUpdated = vvmEvent.updateEventIdsPersistent();
                if (!successfullyUpdated) {
                    log.warn("Unable to update persistent storage with new eventId for " + vvmEvent.getSubscriberNumber() + " : " + vvmEvent.getSubscriberNumber() + ", will retry");
                    return EventHandleResult.OK;
                }
             
                if (eventInfo.getNextEventInfo() == null || eventInfo.isExpire() || eventInfo.isLastExpire()) {
                    /**
                     * Even if the case of an expired event, EventHandleResult.OK MUST be returned (so that the Scheduler will retry
                     * this expired event) instead of EventHandleResult.STOP_RETRIES since the worker thread that will process this
                     * event might die/not process it. If the worker thread process the event successfully, then it MUST cancel the next
                     * eventId.
                     */
                    log.debug("EventFired: Expiry event: " + eventInfo.getEventId());
                    vvmEvent.setCurrentEvent(VvmEvent.VVM_EVENT_SCHEDULER_EXPIRY);
                } else {
                    vvmEvent.setCurrentEvent(VvmEvent.VVM_EVENT_SCHEDULER_RETRY);
                }
                
                
                
                // Validate if the subscriber still has VVM
                UserInfo userInfo = UserFactory.findUserByTelephoneNumber(subscriberNumber);
                if (!isSubscriberVvmActive(userInfo)) {
                    log.debug("Subscriber does not have VVM service enabled, pending VVM event will be canceled");
                    if(!cancelAllEvents(vvmEvent, false)) {
                        log.warn("Unable to cancel all events and update persistent storage for " + subscriberNumber + ", will retry");
                    }
                    
                    return EventHandleResult.OK;
                }

                // Initialize userInfo, userMailbox and notificationEmail
                vvmEvent.initUser(userInfo);
            }          
            
            storedInQueue = VvmHandler.get().getWorkingQueue().offer(vvmEvent);
            if (storedInQueue) {
                log.debug("EventFired: Stored in workingQueue : " + vvmEvent.getSubscriberNumber());
            } else {
                log.warn("EventFired: Not stored in workingQueue (full), will retry : " + vvmEvent.getSubscriberNumber());
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
     * @param vvmEvent VvmEvent
     * @return true if the event should be processed, false otherwise.
     */
    private boolean shouldProcessFiredEvent(AppliEventInfo firedEventInfo, VvmEvent vvmEvent) {

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

            if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.VVM_SMS_INFO.getName())) {
                storedEvent = vvmEvent.getSchedulerIds().getSmsInfoEventId();
                vvmEvent.setCurrentState(VvmEvent.STATE_SENDING_INFO);
            } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.VVM_SENDING_PHONEON.getName())) {
                storedEvent = vvmEvent.getSchedulerIds().getSendingUnitPhoneOnEventId();
                vvmEvent.setCurrentState(VvmEvent.STATE_SENDING_PHONE_ON);
            } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.VVM_WAIT_PHONEON.getName())) {
                storedEvent = vvmEvent.getSchedulerIds().getWaitingPhoneOnEventId();
                vvmEvent.setCurrentState(VvmEvent.STATE_WAITING_PHONE_ON);
            } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.VVM_DEACTIVATOR.getName())) {
                storedEvent = vvmEvent.getSchedulerIds().getDeactivatorEventId();
                vvmEvent.setCurrentState(VvmEvent.STATE_DEACTIVATOR);
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
     * In the particular case of canceling all the events, all the clients SHOULD try to cancel the eventIds anyway
     * since this method is usually used at the end of a Vvm notification process or when a faulty operation
     * has been encountered.
     *  
     * @param vvmEvent VvmEvent
     * @param tryToCancelAnyway True if the cancel operation SHOULD be tried even if the persistent storage update is not successful 
     * @return True if the cancel operation is successful, false otherwise
     */
    public boolean cancelAllEvents(VvmEvent vvmEvent, boolean tryToCancelAnyway) {
        boolean result = true;
        String smsInfoEventId = vvmEvent.getSchedulerIds().getSmsInfoEventId();
        String sendingPhoneOnEventId = vvmEvent.getSchedulerIds().getSendingUnitPhoneOnEventId();
        String waitingPhoneOnEventId = vvmEvent.getSchedulerIds().getWaitingPhoneOnEventId();
        String deactivatorEventId = vvmEvent.getSchedulerIds().getDeactivatorEventId();

        // First step is to nullify the values on disk
        vvmEvent.getSchedulerIds().nullify();
        boolean successfullyUpdated = vvmEvent.updateEventIdsPersistent();

        /**
         * If the update of the persistent storage is successful, cancel the events.
         * If not, do not cancel and let the scheduler retry later.
         */
        if (successfullyUpdated || tryToCancelAnyway) {
            // Second step is to cancel the scheduled events
            if (smsInfoEventId != null && !smsInfoEventId.isEmpty()) {
                log.debug("VvmEventHandler cancel SMS-Info event: " + smsInfoEventId);
                super.cancelEvent(smsInfoEventId);
            }

            if (sendingPhoneOnEventId != null && !sendingPhoneOnEventId.isEmpty()) {
                log.debug("VvmEventHandler cancel Sending-Phone-On event: " + sendingPhoneOnEventId);
                super.cancelEvent(sendingPhoneOnEventId);
            }
            
            if (waitingPhoneOnEventId != null && !waitingPhoneOnEventId.isEmpty()) {
                log.debug("VvmEventHandler cancel Waiting-Phone-On event: " + waitingPhoneOnEventId);
                super.cancelEvent(waitingPhoneOnEventId);
            }

            if (deactivatorEventId != null && !deactivatorEventId.isEmpty()) {
                log.debug("VvmEventHandler cancel Deactivator event: " + deactivatorEventId);
                super.cancelEvent(deactivatorEventId);
            }
        } else {
            log.warn("Unable to cancel all events and update persistent storage for " + vvmEvent.getSubscriberNumber());

            // Revert back to the original eventIds
            vvmEvent.getSchedulerIds().setSmsInfoEventId(smsInfoEventId);
            vvmEvent.getSchedulerIds().setSendingUnitPhoneOnEventId(sendingPhoneOnEventId);
            vvmEvent.getSchedulerIds().setWaitingPhoneOnEventId(waitingPhoneOnEventId);
            vvmEvent.getSchedulerIds().setDeactivatorEventId(deactivatorEventId);

            result = false;
        }
        return result;        
    }

    public boolean cancelSmsInfoEvent(VvmEvent vvmEvent, boolean tryToCancelAnyway) {
        boolean result = true;
        String smsInfoEventId = vvmEvent.getSchedulerIds().getSmsInfoEventId();
        //String sendingUnitEventId = vvmEvent.getSchedulerIds().getSendingUnitPhoneOnEventId();
        
        // First step is to nullify the values on disk
        vvmEvent.getSchedulerIds().setSmsInfoEventId(null);
        boolean successfullyUpdated = vvmEvent.updateEventIdsPersistent();
        
        /**
         * If the update of the persistent storage is successful, cancel the events.
         * If not, do not cancel and let the scheduler retry later.
         */
        if (successfullyUpdated || tryToCancelAnyway) {
            // Second step is to cancel the scheduled events
            if (smsInfoEventId != null && !smsInfoEventId.isEmpty()) {
                log.debug("VvmEventHandler cancel SMS-Info event: " + smsInfoEventId);
                super.cancelEvent(smsInfoEventId);
            }
        } else {
            log.warn("Unable to cancel SMS-Info event and update persistent storage for " + vvmEvent.getSubscriberNumber());

            // Revert back to the original eventIds
            vvmEvent.getSchedulerIds().setSmsInfoEventId(smsInfoEventId);

            result = false;
        }
        return result;
    }
    
    public boolean cancelSendingUnitPhoneOnEvent(VvmEvent vvmEvent, boolean tryToCancelAnyway) {
        boolean result = true;
        String sendingUnitPhoneOnEventId = vvmEvent.getSchedulerIds().getSendingUnitPhoneOnEventId();
        
        // First step is to nullify the values on disk
        vvmEvent.getSchedulerIds().setSendingUnitPhoneOnEventId(null);
        boolean successfullyUpdated = vvmEvent.updateEventIdsPersistent();
    
        /**
         * If the update of the persistent storage is successful, cancel the events.
         * If not, do not cancel and let the scheduler retry later.
         */
        if (successfullyUpdated || tryToCancelAnyway) {
            // Second step is to cancel the scheduled events
            if (sendingUnitPhoneOnEventId != null && !sendingUnitPhoneOnEventId.isEmpty()) {
                log.debug("VvmEventHandler cancel Sending-Unit-PhoneOn event: " + sendingUnitPhoneOnEventId);
                super.cancelEvent(sendingUnitPhoneOnEventId);
            }
        } else {
            log.warn("Unable to cancel Sending-Unit-PhoneOn event and update persistent storage for " + vvmEvent.getSubscriberNumber());

            // Revert back to the original eventIds
            vvmEvent.getSchedulerIds().setSendingUnitPhoneOnEventId(sendingUnitPhoneOnEventId);

            result = false;
        }
    
        return result;
    }
    
    public boolean cancelWaitingPhoneOn(VvmEvent vvmEvent, boolean tryToCancelAnyway) {
        boolean result = true;
        String sendingUnitPhoneOnEventId = vvmEvent.getSchedulerIds().getSendingUnitPhoneOnEventId();
        String waitingPhoneOnEventId = vvmEvent.getSchedulerIds().getWaitingPhoneOnEventId();
        
        // First step is to nullify the values on disk
        vvmEvent.getSchedulerIds().setSendingUnitPhoneOnEventId(null);
        vvmEvent.getSchedulerIds().setWaitingPhoneOnEventId(null);
        boolean successfullyUpdated = vvmEvent.updateEventIdsPersistent();
    
        /**
         * If the update of the persistent storage is successful, cancel the events.
         * If not, do not cancel and let the scheduler retry later.
         */
        if (successfullyUpdated || tryToCancelAnyway) {
            // Second step is to cancel the scheduled events
            if (sendingUnitPhoneOnEventId != null && !sendingUnitPhoneOnEventId.isEmpty()) {
                log.debug("VvmEventHandler cancel Sending-Unit-PhoneOn event: " + sendingUnitPhoneOnEventId);
                super.cancelEvent(sendingUnitPhoneOnEventId);
            }

            if (waitingPhoneOnEventId != null && !waitingPhoneOnEventId.isEmpty()) {
                log.debug("VvmEventHandler cancel Waiting-PhoneOn event: " + waitingPhoneOnEventId);
                super.cancelEvent(waitingPhoneOnEventId);
            }
        } else {
            log.warn("Unable to cancel SmsType0 event and update persistent storage for " + vvmEvent.getSubscriberNumber());

            // Revert back to the original eventIds
            vvmEvent.getSchedulerIds().setSendingUnitPhoneOnEventId(sendingUnitPhoneOnEventId);
            vvmEvent.getSchedulerIds().setWaitingPhoneOnEventId(waitingPhoneOnEventId);

            result = false;
        }
    
        return result;
    }
    
    /**
     * 
     * @param userInfo UserInfo
     * @return true if subscriber for this vvmEvent still has VVM service enabled
     */
    private boolean isSubscriberVvmActive(UserInfo userInfo) {
        if(userInfo == null) {
            return false;
        }
        if (VvmHandler.get().isSimSwapConfigActive()){
            //sim swap is on
            return userInfo.isVVMSystemActivated() && userInfo.isVVMActivated() && userInfo.hasVvmService();
        } else {
            //sim swap is off
            return userInfo.isVVMActivated() && userInfo.hasVvmService();
        }
    }
    
    private boolean isEventTypeActivityDetected(AppliEventInfo firedEventInfo) {

        EventID eventId = null;
        try {
            if (firedEventInfo.getEventId() != null && firedEventInfo.getEventId().length() > 0) {
                eventId = new EventID(firedEventInfo.getEventId());
            }

            if (eventId == null) {
                log.warn("Invalid EventId for event " + firedEventInfo);
                return false;
            }

            return eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.VVM_ACTIVATOR.getName());

        } catch (InvalidEventIDException e) {
            log.error("Invalid EventId for event " + firedEventInfo);
            return false;
        }
    }
    
    /**
     * This method tries to schedule a new event and returns true if the level-3 legacy event (scheduled before MiO 3.2) can be canceled
     *
     * @param eventInfo AppliEventInfo
     * @return true if the legacy event was processed correctly and it can be canceled
     */
    private boolean handleLegacyVvmNotif(AppliEventInfo eventInfo) {
        
        try {
            // Must re-construct the vvmEvent object
            Properties props = eventInfo.getEventProperties();
            
            // Extract subscriber number for legacy vvmEvent Event Key
            String subscriberNumber = eventInfo.getEventKey();
            String callerNumber = props.getProperty(VvmEvent.CALLER);
            
            String[] keys = subscriberNumber.split("-");
            if (keys.length > 0) {
                subscriberNumber = keys[0];
            }
            
            // Validate if the subscriber's storage is READ-ONLY (using the notification number)
            if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(subscriberNumber)) {
                log.warn("Storage currently not available to process Vvm event for " + subscriberNumber + ", will retry");
                return false;
            }
            
            // Validate if it is last event notification (expire event) for level-3 scheduling
            if (eventInfo.isExpire()) {
                log.debug("EventFired: Expiry legacy Vvm event for " + subscriberNumber);

                // Generate MDR
                mer.vvmTimeout(subscriberNumber, callerNumber);

                return true;
            }
            
            UserInfo userInfo = UserFactory.findUserByTelephoneNumber(subscriberNumber);
            if (userInfo == null) {
                //Generate MDR
                mer.vvmFailed(subscriberNumber, callerNumber);
                
                log.debug("EventFired for " + subscriberNumber + ", but subscriber not found, legacy VVM notification dropped");
                return true;
            }
            log.debug("Event Fired: legacy Vvm notification detected for " + subscriberNumber);
            
            VvmEvent vvmEvent = new VvmEvent( subscriberNumber,
                                     null,
                                     null,
                                     null,
                                     VvmEvent.getNotificationType(props),
                                     callerNumber,
                                     false);
            vvmEvent.setEventServiceTypeKey(NtfEventTypes.VVM_L3.getName());
            vvmEvent.initUser(userInfo);
            
            
            // A new VVM notification has been scheduled, retry later (level-3)
            vvmEvent.retrieveSchedulerEventIdsPersistent();
            if (!vvmEvent.getSchedulerIds().isEmtpy()) {
                log.info("Event Fired: Attempting to handle a legacy Vvm notif while a Vvm notif is already scheduled for " 
                        + subscriberNumber + ", will retry later (level-3)");
                return false;
            }
            
            // Schedule new level-3 event and sends VVM notification (skips worker queue)
            boolean successfullyProcessed = VvmHandler.get().handleSmsUnit(vvmEvent);
            if (successfullyProcessed) {
                return true;                    
            } else {
                return false;
            }
            
        } catch (Exception e) {
            String message = "Event fired exception for " + eventInfo.getEventId();
            if (eventInfo.getNextEventInfo() != null) {
                log.warn(message + ", will retry. ", e);
            } else {
                log.error(message + ", will not retry. ", e);
            }
        }
        
        return false;
    }
}
