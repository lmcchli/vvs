/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt;

import com.abcxyz.services.moip.ntf.event.NtfEventTypes;

/**
 * Class handling Call retries
 */
public class OdlEventHandlerCall extends OdlEventHandler {

    public OdlEventHandlerCall() {
        /*
        RetryEventInfo callRetryInfo = new RetryEventInfo(getEventServiceName(), Config.getOutdialCallRetrySchema(), Config.getOutdialCallExpireTimeInMin());
        callScheduler = new AppliEventHandler(callRetryInfo, this);*/
    }

	public String getEventServiceName() {
		return NtfEventTypes.EVENT_TYPE_ODL_CALL.getName();
	}

    /**
     * Schedule the next Call retry
     * @param odlEvent OdlEvent
     */
/*	public AppliEventInfo scheduleCallRetry(OdlEvent odlEvent) {
        String key = odlEvent.getOdlEventKey();
        Properties props = odlEvent.getEventProperties();
        AppliEventInfo eventInfo = callScheduler.scheduleEvent(key + NtfEvent.getUniqueId(), NtfEventTypes.EVENT_TYPE_ODL_CALL.getName(), props);
        odlEvent.keepReferenceID(eventInfo.getEventId());

        log.debug("OdlEventHandlerCall scheduled event: " + eventInfo.getEventId());

        return eventInfo;
    }*/
}
