/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.event.OdlEvent;
import com.mobeon.ntf.out.outdial.OutdialNotificationOut;

/**
 * Class handling Wait retries
 */
public class OdlEventHandlerWait extends OdlEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(OdlEventHandlerWait.class);

    public OdlEventHandlerWait() {
        // No schema to initialise since this class uses the scheduleEvent(when) method
    }

	public String getEventServiceName() {
		return NtfEventTypes.EVENT_TYPE_ODL_WAIT.getName();
	}

}
