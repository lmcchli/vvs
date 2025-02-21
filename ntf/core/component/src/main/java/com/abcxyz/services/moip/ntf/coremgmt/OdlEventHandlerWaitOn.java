/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.event.OdlEvent;

/**
 * Class handling startup retries
 */
public class OdlEventHandlerWaitOn extends OdlEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(OdlEventHandlerWaitOn.class);
    private static final int DEFAULT_WAITON_TIMEOUT = 15 * 60000;
    private int waitOnTimeOut = DEFAULT_WAITON_TIMEOUT;

    public OdlEventHandlerWaitOn() {
        // No schema to initialise since this class uses the scheduleEvent(when) method
    }

	public String getEventServiceName() {
		return NtfEventTypes.EVENT_TYPE_ODL_WAITON.getName();
	}

	
	/*
   @Override
    public long getNextRetryTime(AppliEventInfo eventInfo)
    {
	    if (eventInfo.getEventType().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_WAITON.getName())) {
            return waitOnTimeOut + System.currentTimeMillis();
        } else {
            //here it's possible to stop next retry.
        }
        return EventHandleResult.OK; //default schema will be used for next retry event
    }
*/
}
