/**
 * Copyright (c) 2012 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.vvm;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.ntf.Config;

/**
 * This class extends the VvmEventHandler because the various vvm events
 * have their respective scheduler-schema and a handler can only support one schema.
 */
public class VvmEventHandlerSmsInfo extends VvmEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(VvmEventHandlerSmsInfo.class);
            
    public VvmEventHandlerSmsInfo() {
        RetryEventInfo info = new RetryEventInfo(getEventServiceName());
        info.setEventRetrySchema(Config.getVvmSmsUnitRetrySchema());
        info.setExpireTimeInMinute(Config.getVvmSmsUnitExpireTimeInMin());
        info.setExpireRetryTimerInMinute(Config.getVvmExpiryIntervalInMin());        
        info.setMaxExpireTries(Config.getVvmExpiryRetries());
        super.init(info);
    }
    
    public String getEventServiceName() {
        return NtfEventTypes.VVM_SMS_INFO.getName();
    }
    
    /**
     * 
     * @param vvmEvent The event to schedule
     * @return If the event was scheduled successfully
     */
    public boolean scheduleSmsInfo(VvmEvent vvmEvent) {
        boolean result = true;
        String smsUnitEventId = vvmEvent.getSchedulerIds().getSmsInfoEventId();
        AppliEventInfo eventInfo = null;

        if (smsUnitEventId == null || smsUnitEventId.isEmpty()) {
            
            Properties props = vvmEvent.getEventProperties();
            props.put(VvmEvent.SERVICE_TYPE_KEY, NtfEventTypes.VVM_SMS_INFO.getName());//TODO: move this in vvmEvent.getEventProperties(); based on event state
            eventInfo = eventHandler.scheduleEvent( vvmEvent.getSubscriberNumber() + NtfEvent.getUniqueId(), NtfEventTypes.VVM_SMS_INFO.getName(), props);

            log.debug("ScheduleSmsUnit: scheduled event: " + eventInfo.getEventId());
            vvmEvent.getSchedulerIds().setSmsInfoEventId(eventInfo.getEventId());

            // Store the eventId
            boolean successfullyUpdated = vvmEvent.updateEventIdsPersistent();
            if (!successfullyUpdated) {
                /**
                 * If the update of the persistent storage is not successful after starting a SmsInfo event,
                 * cancel the SmsInfo event and return false.
                 */
                eventHandler.cancelEvent(eventInfo);
                result = false;
            }
        } else {
            log.debug("ScheduleSmsUnit: event already scheduled: " + smsUnitEventId);
        }

        return result;
    }
}
