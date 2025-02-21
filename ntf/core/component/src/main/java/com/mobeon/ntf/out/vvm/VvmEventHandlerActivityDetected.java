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
public class VvmEventHandlerActivityDetected extends VvmEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(VvmEventHandlerActivityDetected.class);
    
    public VvmEventHandlerActivityDetected() {
        RetryEventInfo info = new RetryEventInfo(getEventServiceName());
        info.setEventRetrySchema(Config.getNotifRetrySchema());
        info.setExpireTimeInMinute(Config.getNotifExpireTimeInMin());
        info.setExpireRetryTimerInMinute(Config.getVvmExpiryIntervalInMin());        
        info.setMaxExpireTries(Config.getVvmExpiryRetries());
        super.init(info);
    }
    
    public String getEventServiceName() {
        return NtfEventTypes.VVM_ACTIVATOR.getName();
    }

    public boolean scheduleActivityDetected(VvmEvent vvmEvent) {
        boolean result = true;
        AppliEventInfo eventInfo = null;

        Properties props = vvmEvent.getEventProperties();
        props.put(VvmEvent.SERVICE_TYPE_KEY, NtfEventTypes.VVM_ACTIVATOR.getName());
        
        eventInfo = eventHandler.scheduleEvent(vvmEvent.getSubscriberNumber() + NtfEvent.getUniqueId(), NtfEventTypes.VVM_ACTIVATOR.getName(), props);

        log.debug("ScheduleActivityDetected: scheduled event: " + eventInfo.getEventId());

        vvmEvent.getSchedulerIds().setActivatorEventId(eventInfo.getEventId());

        return result;
    }
}
