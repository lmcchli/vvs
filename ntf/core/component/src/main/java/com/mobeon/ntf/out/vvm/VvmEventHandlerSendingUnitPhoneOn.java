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
public class VvmEventHandlerSendingUnitPhoneOn extends VvmEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(VvmEventHandlerSendingUnitPhoneOn.class);
    private VvmEventHandler vvmEventHandler;
    
    public VvmEventHandlerSendingUnitPhoneOn() {
        RetryEventInfo info = new RetryEventInfo(getEventServiceName());
        info.setEventRetrySchema(Config.getVvmSimSwapSendingUnitPhoneOnRetrySchema());
        info.setExpireTimeInMinute(Config.getVvmSimSwapSendingUnitPhoneOnExpireTimeInMin());
        info.setExpireRetryTimerInMinute(Config.getVvmExpiryIntervalInMin());        
        info.setMaxExpireTries(Config.getVvmExpiryRetries());
        super.init(info);
        
        vvmEventHandler = (VvmEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.VVM_L3.getName());
    }
    
    public String getEventServiceName() {
        return NtfEventTypes.VVM_SENDING_PHONEON.getName();
    }
    
    /**
     * Schedule an event for SendingUnit.
     * This timer is used to retry if a temporary error occurs internal to NTF (because of a SMSUnit queue full for example)
     * @param vvmEvent VvmEvent
     * @return boolean True if the SendingUnit event has been successfully scheduled and stored persistently
     */
    public boolean scheduleSendingUnit(VvmEvent vvmEvent) {
        boolean result = true;
        String sendingUnitEventId = vvmEvent.getSchedulerIds().getSendingUnitPhoneOnEventId();
        AppliEventInfo eventInfo = null;

        if (sendingUnitEventId == null || sendingUnitEventId.isEmpty()) {
            
            Properties props = vvmEvent.getEventProperties();
            props.put(VvmEvent.SERVICE_TYPE_KEY, NtfEventTypes.VVM_SENDING_PHONEON.getName());//TODO: move this in vvmEvent.getEventProperties(); based on event state
            
            eventInfo = eventHandler.scheduleEvent(vvmEvent.getSubscriberNumber() + NtfEvent.getUniqueId(), NtfEventTypes.VVM_SENDING_PHONEON.getName(), props);

            log.debug("ScheduleSendingUnit: scheduled event: " + eventInfo.getEventId());
            vvmEvent.getSchedulerIds().setSendingUnitPhoneOnEventId(eventInfo.getEventId());

            // Store the eventId
            boolean successfullyCancelled = vvmEventHandler.cancelSmsInfoEvent(vvmEvent, false);
            if (!successfullyCancelled) {
                /**
                 * If the update of the persistent storage is not successful after starting a sendingUnit event,
                 * cancel the sendingUnit event and return false.
                 */
                vvmEventHandler.cancelEvent(eventInfo.getEventId());
                result = false;
            }
        } else {
            log.debug("ScheduleSendingUnit: event already scheduled: " + sendingUnitEventId);
            
            /**
             * If a sendingUnitEventId is already scheduled, still the SMS-Info eventId must be cancelled
             *  
             * Canceling the SMS-Info timer will update the persistent file
             * (with the new eventInfo just scheduled)
             * 
             * Force the cancellation even if there is an error writing on disk since there is already a SendingUnit scheduled. 
             */
            //TODO: PHASE3 - During notif aggregation, NTF could be returning in this state after the WAITING_PHONE timer expire for NOTIF(1). 
            //      In that case, NTF shouldn't cancel a SmsInfo event for Notif(2). This must be fixed if notification aggregation can be done during the PhoneON check
            vvmEventHandler.cancelSmsInfoEvent(vvmEvent, true); 
        }

        return result;
    }
}
