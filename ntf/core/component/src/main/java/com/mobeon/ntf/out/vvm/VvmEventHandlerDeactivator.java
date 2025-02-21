/**
 * Copyright (c) 2012 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.vvm;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
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
public class VvmEventHandlerDeactivator extends VvmEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(VvmEventHandlerDeactivator.class);

    private static final String DEFAULT_SIM_SWAP_TIMEOUT = "5"; 
    private VvmEventHandler vvmEventHandler;

    public VvmEventHandlerDeactivator() {

        String simSwapTimeout = Config.getSimSwapTimeout();
        if (simSwapTimeout != null) {
            simSwapTimeout = simSwapTimeout.trim();
        }

        if (simSwapTimeout == null || simSwapTimeout.isEmpty() || simSwapTimeout.equals("0")) {
            simSwapTimeout = DEFAULT_SIM_SWAP_TIMEOUT;
            log.debug("VvmEventHandlerDeactivator: back to default: " + simSwapTimeout);
        }

        RetryEventInfo info = new RetryEventInfo(getEventServiceName());
        info.setEventRetrySchema(simSwapTimeout); 
        info.setExpireTimeInMinute(Config.getSimSwapTimeoutExpireTimeInMin());
        info.setExpireRetryTimerInMinute(Config.getVvmExpiryIntervalInMin());        
        info.setMaxExpireTries(Config.getVvmExpiryRetries());
        super.init(info);

        vvmEventHandler = (VvmEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.VVM_L3.getName());
    }

    public String getEventServiceName() {
        return NtfEventTypes.VVM_DEACTIVATOR.getName();
    }

    public boolean scheduleDeactivator(VvmEvent vvmEvent) {
        boolean result = true;
        String deactivatorEventId = vvmEvent.getSchedulerIds().getDeactivatorEventId();
        AppliEventInfo eventInfo = null;

        if (deactivatorEventId == null || deactivatorEventId.isEmpty()) {
            
            Properties props = vvmEvent.getEventProperties();
            props.put(VvmEvent.SERVICE_TYPE_KEY, NtfEventTypes.VVM_DEACTIVATOR.getName());//TODO: move this in vvmEvent.getEventProperties(); based on event state
            
            eventInfo = eventHandler.scheduleEvent(vvmEvent.getSubscriberNumber() + NtfEvent.getUniqueId(), NtfEventTypes.VVM_DEACTIVATOR.getName(), props);

            log.debug("ScheduleDeactivator: scheduled event: " + eventInfo.getEventId());
            vvmEvent.getSchedulerIds().setDeactivatorEventId(eventInfo.getEventId());

            // Store the deactivator eventId and cancel SendingPhoneOn and WaitingPhoneOn in one IO operation
            boolean successfullyCancelled = vvmEventHandler.cancelWaitingPhoneOn(vvmEvent, false); 
            if (!successfullyCancelled) {
                /**
                 * If the update of the persistent storage is not successful after starting a deactivator event,
                 * cancel the deactivator event and return false.
                 */
                vvmEventHandler.cancelEvent(eventInfo.getEventId());
                result = false;
            }
        } else {
            log.debug("ScheduleDeactivator: event already scheduled: " + deactivatorEventId); //TODO: this should never happen?
            
            //TODO: update this comment
            /**
             * If a sendingUnitEventId is already scheduled, still the SMS-Info eventId must be cancelled
             *  
             * Canceling the SMS-Info timer will update the persistent file
             * (with the new eventInfo just scheduled)
             * 
             * Force the cancellation even if there is an error writing on disk since there is already a SendingUnit scheduled. 
             */
            vvmEventHandler.cancelWaitingPhoneOn(vvmEvent, true); 
        }

        return result;
    }
}
