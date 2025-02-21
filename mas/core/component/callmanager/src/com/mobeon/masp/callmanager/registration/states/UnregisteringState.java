/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration.states;

import com.mobeon.masp.callmanager.registration.SspInstance;

/**
 * The registration state Unregistering.
 *
 * @author Malin Flodin
 */
public class UnregisteringState extends RegistrationState {

    private final SspInstance sspInstance;

    public UnregisteringState(SspInstance sspInstance) {
        this.sspInstance = sspInstance;
    }

    public void doRegister() {
        if (log.isDebugEnabled())
            log.debug("doRegister received. The registration will be " +
                    "performed as soon as the ongoing unregister procedure has " +
                    "completed. " +
                    sspInstance.getDebugInfo());
        sspInstance.setPendingRegister(true);
    }

    public void doUnregister() {
        if (sspInstance.isRegisterPending()) {
            if (log.isDebugEnabled())
                log.debug("doUnregister received. Pending register was " +
                        "canceled. " +
                        sspInstance.getDebugInfo());
            sspInstance.setPendingRegister(false);
        } else {
            if (log.isDebugEnabled())
                log.debug("doUnregister received. It is ignored since " +
                        "the unregister procedure already is ongoing. " +
                        sspInstance.getDebugInfo());
        }
    }

    public void processSipOkResponse(int reregisterTime) {
        if (sspInstance.isRegisterPending()) {

            // A register request has been received. Register now!
            if (log.isDebugEnabled())
                log.debug("SIP Ok response received while there is a " +
                        "pending register request. Registration is started. " +
                        sspInstance.getDebugInfo());

            sspInstance.setPendingRegister(false);
            sspInstance.setRegisteringState();

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

        } else {

            // The unregister is completed.
            if (log.isDebugEnabled())
                log.debug("SIP Ok response received. The unregistration " +
                        "has completed OK. " +
                        sspInstance.getDebugInfo());
            log.available(
                    PROTOCOL,
                    sspInstance.getAddress().getHost(),
                    sspInstance.getAddress().getPort());
            sspInstance.setUnregisteredState();
        }
    }

    public void processSipErrorResponse(int sipResponseCode) {
        if (sspInstance.isRegisterPending()) {

            // A register request has been received. Register now!
            if (log.isDebugEnabled())
                log.debug("SIP Error response " + sipResponseCode +
                        " received while there is a pending register request. " +
                        "Registration is started. " +
                        sspInstance.getDebugInfo());

            sspInstance.setPendingRegister(false);
            sspInstance.setRegisteringState();

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

        } else {

            // The unregister is considered completed.
            if (log.isDebugEnabled())
                log.debug("SIP Error response " + sipResponseCode +
                        " received. The unregister procedure is " +
                        "considered completed anyway. " +
                        sspInstance.getDebugInfo());
            log.available(
                    PROTOCOL,
                    sspInstance.getAddress().getHost(),
                    sspInstance.getAddress().getPort());
            sspInstance.setUnregisteredState();

        }
    }

    public void processSipTimeout() {
        if (sspInstance.isRegisterPending()) {

            // A register request has been received. Register now!

            log.notResponding(
                    PROTOCOL,
                    sspInstance.getAddress().getHost(),
                    sspInstance.getAddress().getPort(),
                    "SIP Timeout or transaction error received " +
                        "while there is a pending unregister request. " +
                        "Registration is started. " +
                        sspInstance.getDebugInfo());

            sspInstance.setPendingRegister(false);
            sspInstance.setRegisteringState();

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

        } else {

            // The unregister is considered completed.

            log.notResponding(
                    PROTOCOL,
                    sspInstance.getAddress().getHost(),
                    sspInstance.getAddress().getPort(),
                    "SIP Timeout or transaction error received. " +
                        "The unregister procedure is considered completed anyway. " +
                        sspInstance.getDebugInfo());

            sspInstance.setUnregisteredState();
        }
    }

    public void handleReRegisterTimeout() {
        if (log.isDebugEnabled())
            log.debug("Re-register Timeout. It suggests " +
                    "an implementation error and is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public void handleBackoffTimeout() {
        if (log.isDebugEnabled())
            log.debug("Backoff Timeout. It suggests " +
                    "an implementation error and is ignored. " +
                    sspInstance.getDebugInfo());
    }

    public String toString() {
        return "Unregistering";
    }
}
