/**
 * Copyright (c) 2012 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.vvm;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.EventRetryTimerSchema;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.ntf.Config;

/**
 * This class extends the VvmEventHandler because the various vvm events
 * have their respective scheduler-schema and a handler can only support one schema.
 */
public class VvmEventHandlerWaitingPhoneOn extends VvmEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(VvmEventHandlerWaitingPhoneOn.class);
    private VvmEventHandler vvmEventHandler;
            
    public VvmEventHandlerWaitingPhoneOn() {
        RetryEventInfo info = new RetryEventInfo(getEventServiceName());
        info.setEventRetrySchema(Config.getVvmSimSwapWaitingPhoneOnRetrySchema());
        info.setExpireTimeInMinute(Config.getVvmSimSwapWaitingPhoneOnExpireTimeInMin());
        info.setExpireRetryTimerInMinute(Config.getVvmExpiryIntervalInMin());        
        info.setMaxExpireTries(Config.getVvmExpiryRetries());
        super.init(info);
        
        vvmEventHandler = (VvmEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.VVM_L3.getName());
    }
    
    public String getEventServiceName() {
        return NtfEventTypes.VVM_WAIT_PHONEON.getName();
    }

    /**
     * Schedule an event for Waiting-PhoneOn
     * @param vvmEvent VvmEvent
     * @return boolean True if the Waiting-PhoneOn as been scheduled and stored successfully, false otherwise. 
     */
    public boolean scheduleWaitingPhoneOn(VvmEvent vvmEvent) {
        boolean result = true;
        String waitingPhoneOnEventId = vvmEvent.getSchedulerIds().getWaitingPhoneOnEventId();
        AppliEventInfo eventInfo = null;

        /**
         * In the case of a validity timer retrying (for example 3 X 24h), in order to keep the retry count,
         * NTF shall not re-schedule another Waiting-PhoneOn but keep the current one if present.  
         */
        if (waitingPhoneOnEventId == null || waitingPhoneOnEventId.isEmpty()) {
            EventRetryTimerSchema retrySchema = new EventRetryTimerSchema(Config.getVvmSimSwapWaitingPhoneOnRetrySchema());
            long when = retrySchema.getNextRetryTime(0);
            
            Properties props = vvmEvent.getEventProperties();
            props.put(VvmEvent.SERVICE_TYPE_KEY, NtfEventTypes.VVM_WAIT_PHONEON.getName()); //TODO: move this in vvmEvent.getEventProperties(); based on event state

            /**
             * Scheduler limitation.
             * An event which has a backup event (scheduled by the Scheduler) cannot be cancelled and re-scheduled.
             * Scheduler will not consider the re-scheduled one.
             * This particular case might happen here since an SMSUnit can either be successful or not depending
             * of the response type from the NTF SMS-Client.
             * To avoid this possibility, a 2 second delay (2000 ms) is introduced in order to make sure the Scheduler
             * will handle this new schedule-event which will have a slightly different ID.
             */
            eventInfo = eventHandler.scheduleEvent(
                    when + 2000,
                    vvmEvent.getSubscriberNumber() + NtfEvent.getUniqueId(),
                    NtfEventTypes.VVM_WAIT_PHONEON.getName(),
                    props);

            log.debug("ScheduleWaitingPhoneOn: scheduled event: " + eventInfo.getEventId());
            vvmEvent.getSchedulerIds().setWaitingPhoneOnEventId(eventInfo.getEventId());

            /**
             * As a PhoneOn event is now scheduled (either SMS-Type-0/SMSc or AlertSc/HLR),
             * the Sending-Unit timer must be cancelled.  In order to perform IO-write only once,
             * both Sending-Unit and Waiting-PhoneOn eventIds will be updated in 1 IO operation..
             * 
             * Canceling the Sending-Unit timer will update the persistent file with the new eventInfo just scheduled.
             */
            boolean successfullyCancelled = vvmEventHandler.cancelSendingUnitPhoneOnEvent(vvmEvent, false);
            if (!successfullyCancelled) {
                /**
                 * If the update of the persistent storage is not successful while trying to cancel SmsUnit,
                 * it means that the storage of the Waiting-PhoneOn scheduled event as not been stored persistently either,
                 * Cancel the Waiting-PhoneOn event and let the SendingUnit retry.
                 */
                vvmEventHandler.cancelEvent(eventInfo.getEventId());
                result = false;
            }

        } else {
            log.debug("ScheduleWaitingPhoneOn: event already scheduled: " + waitingPhoneOnEventId);

            /**
             * If a waitingPhoneOnEventId is already scheduled, still the Sending-Unit eventId must be cancelled
             *  
             * Canceling the Sending-Unit timer will update the persistent file
             * (with the new eventInfo just scheduled)
             * 
             * Force the cancellation even if there is an error writing on disk since there is already a Waiting-PhoneOn scheduled. 
             */
            vvmEventHandler.cancelSendingUnitPhoneOnEvent(vvmEvent, true);
        }

        return result;
    }
}
