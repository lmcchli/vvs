/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.callhandling.CallImpl;
import com.mobeon.masp.callmanager.registration.SspInstance;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.notification.OutboundNotification;

import javax.sip.message.Request;

/**
 * This class is responsible for dispatching a SIP tiemout event to a
 * corresponding call or active REGISTER transaction.
 *
 * @author Malin Flodin
 */
public class SipTimeoutDispatcher {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    public SipTimeoutDispatcher() {
    }

    public void dispatchSipTimeoutEvent(SipTimeoutEvent sipTimeoutEvent) {

        SspInstance sspInstance = null;
        OutboundNotification notification = null;
        CallImpl call = null;

        if (sipTimeoutEvent.getMethod().equals(Request.REGISTER)) {
            sspInstance =
                    CMUtils.getInstance().getRegistrationDispatcher().
                            getSspInstance(sipTimeoutEvent);

        } else if (sipTimeoutEvent.getMethod().equals(Request.NOTIFY)){
            notification = CMUtils.getInstance().getNotificationDispatcher().
                    getNotification(sipTimeoutEvent);

        } else {
            call = (CallImpl) CMUtils.getInstance().getCallDispatcher().getCall(
                    sipTimeoutEvent);

        }

        if (sspInstance != null) {
            if (log.isDebugEnabled())
                log.debug("SIP timeout for ongoing REGISTER is queued in " +
                        "sspInstance: " + sspInstance);
            sspInstance.queueEvent(sipTimeoutEvent);

        } else if (notification != null) {
            if (log.isDebugEnabled())
                log.debug("SIP timeout for ongoing NOTIFY is queued in " +
                        "OutboundNotification: " + notification);
            notification.queueEvent(sipTimeoutEvent);

        } else if (call != null) {
            if (log.isDebugEnabled())
                log.debug("SIP timeout is queued in the active call: " + call);
            call.queueEvent(sipTimeoutEvent);

        } else {
            if (log.isInfoEnabled()) log.info("The timeout for the SIP " + sipTimeoutEvent.getMethod() +
                                              " request did not match any ongoing register procedure or " +
                                              "active call. The SIP timeout is ignored.");

        }

    }
}
