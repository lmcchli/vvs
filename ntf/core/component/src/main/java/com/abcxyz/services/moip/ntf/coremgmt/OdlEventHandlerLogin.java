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
 * Class handling logged-in retries
 */
public class OdlEventHandlerLogin extends OdlEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(OdlEventHandlerLogin.class);

    public OdlEventHandlerLogin() {
/*        RetryEventInfo info = new RetryEventInfo(getEventServiceName());
        info.setEventRetrySchema(Config.getOutdialLoginRetrySchema());
        info.setExpireTimeInMinute(Config.getOutdialLoginExpireTimeInMin());
        super.init(info);*/
    }

    public String getEventServiceName() {
        return NtfEventTypes.EVENT_TYPE_ODL_LOGIN.getName();
    }

}
