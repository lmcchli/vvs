/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration.states;

import com.mobeon.masp.callmanager.registration.SspInstance;

/**
 * The registration state Registered.
 *
 * @author Malin Flodin
 */
public class RegisteredState extends RegistrationState {

    private final SspInstance sspInstance;

    public RegisteredState(SspInstance sspInstance) {
        this.sspInstance = sspInstance;
    }

    public void doRegister() {
        if (log.isDebugEnabled())
            log.debug("doRegister received. It is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public void doUnregister() {
        if (log.isDebugEnabled())
            log.debug("doUnregister received. Unregistration is started. " +
                    sspInstance.getDebugInfo());

        sspInstance.cancelRegisterTimer();

        try {
            sspInstance.sendUnregister();
            sspInstance.setUnregisteringState();

        } catch (Exception e) {
            log.notAvailable(
                    PROTOCOL,
                    sspInstance.getAddress().getHost(),
                    sspInstance.getAddress().getPort(),
                    "Exception occurred when unregistering. " +
                    "The state is considered unregistered immediately." +
                    sspInstance.getDebugInfo());

            sspInstance.setUnregisteredState();
        }
    }

    public void processSipOkResponse(int reregisterTime) {
        if (log.isDebugEnabled())
            log.debug("SIP Ok response received. It suggests an " +
                    "implementation error and is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public void processSipErrorResponse(int sipResponseCode) {
        if (log.isDebugEnabled())
            log.debug("SIP Error response " + sipResponseCode +
                    " received. It suggests an implementation error " +
                    "and is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public void processSipTimeout() {
        if (log.isDebugEnabled())
            log.debug("SIP Timeout or transaction error received. " +
                    "It suggests an implementation error and is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public void handleReRegisterTimeout() {
        if (log.isDebugEnabled())
            log.debug("Re-register Timeout. Re-registration is started. " +
                    sspInstance.getDebugInfo());

        sspInstance.setRegisteringState();

        try {
            sspInstance.sendRegister();
        } catch (Exception e) {
            sspInstance.startBackoffTimer();
        }
    }

    public void handleBackoffTimeout() {
        if (log.isDebugEnabled())
            log.debug("Backoff Timeout. It suggests an implementation error " +
                    "and is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public String toString() {
        return "Registered";
    }
}
