/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.event.OdlEvent;
import com.mobeon.ntf.Config;

/**
 * Class handling startup retries
 */
public class OdlEventHandlerStart extends OdlEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(OdlEventHandlerStart.class);

    public OdlEventHandlerStart() {
/*        RetryEventInfo info = new RetryEventInfo(getEventServiceName());
        info.setEventRetrySchema(Config.getOutdialStartRetrySchema());
        info.setExpireTimeInMinute(Config.getOutdialStartExpireTimeInMin());
        super.init(info);*/
    }

    public String getEventServiceName() {
        return NtfEventTypes.EVENT_TYPE_ODL_START.getName();
    }


    /**
     * ReSchedule the next startup retry
     * @param odlEvent OdlEvent
     * @return AppliEventInfo
     */
/*
    public AppliEventInfo rescheduleStartRetry(OdlEvent odlEvent) {
        String key = odlEvent.getOdlEventKey();
        Properties props = odlEvent.getEventProperties();
        String previousSchedulerIdStart = odlEvent.getReferenceId();

        AppliEventInfo eventInfo = eventHandler.scheduleEvent(key + NtfEvent.getUniqueId(), NtfEventTypes.EVENT_TYPE_ODL_START.getName(), props);
        odlEvent.keepReferenceID(eventInfo.getEventId());
        log.debug("Scheduled event: " + eventInfo.getEventId());

        cancelEvent(previousSchedulerIdStart);

        return eventInfo;
    }*/
}
