/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration.states;

import com.mobeon.masp.callmanager.registration.SspInstance;

/**
 * The registration state Unregistered.
 * @author Malin Flodin
 */
public class UnregisteredState extends RegistrationState {

    private final SspInstance sspInstance;

    public UnregisteredState(SspInstance sspInstance) {
        this.sspInstance = sspInstance;
    }

    public void doRegister() {
        if (log.isDebugEnabled())
            log.debug("doRegister received. Registration is started. " +
                    sspInstance.getDebugInfo());
        try {
            sspInstance.sendRegister();
        } catch (Exception e) {
            log.notAvailable(
                    PROTOCOL,
                    sspInstance.getAddress().getHost(),
                    sspInstance.getAddress().getPort(),
                    "Exception occurred when registering. " +
                    "The backoff timer is scheduled for a REGISTER retry later. " +
                    sspInstance.getDebugInfo());
            sspInstance.startBackoffTimer();
        }
        sspInstance.setRegisteringState();
    }

    public void doUnregister() {
        if (log.isDebugEnabled())
            log.debug("doUnregister received. It is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public void processSipOkResponse(int reregisterTime) {
        if (log.isDebugEnabled())
            log.debug("SIP Ok response received. It is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public void processSipErrorResponse(int sipResponseCode) {
        if (log.isDebugEnabled())
            log.debug("SIP Error response " + sipResponseCode +
                    " received. It is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public void processSipTimeout() {
        if (log.isDebugEnabled())
            log.debug("SIP Timeout or transaction error received. " +
                    "It is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public void handleReRegisterTimeout() {
        if (log.isDebugEnabled())
            log.debug("Re-register Timeout. It is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public void handleBackoffTimeout() {
        if (log.isDebugEnabled())
            log.debug("Backoff Timeout. It is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public String toString() {
        return "Unregistered";
    }
}
