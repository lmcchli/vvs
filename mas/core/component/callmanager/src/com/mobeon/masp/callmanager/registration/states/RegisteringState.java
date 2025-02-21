/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration.states;

import com.mobeon.masp.callmanager.registration.SspInstance;

/**
 * The registration state Registering.
 * @author Malin Flodin
 */
public class RegisteringState extends RegistrationState {

    private final SspInstance sspInstance;

    public RegisteringState(SspInstance sspInstance) {
        this.sspInstance = sspInstance;
    }

    public void doRegister() {
        if (sspInstance.isUnregisterPending()) {
            if (log.isDebugEnabled())
                log.debug("doRegister received. " +
                        "Pending unregister was canceled. " +
                        sspInstance.getDebugInfo());
            sspInstance.setPendingUnregister(false);
        } else {
            if (log.isDebugEnabled())
                log.debug("doRegister received. It is ignored since " +
                    "the register procedure already is ongoing. " +
                        sspInstance.getDebugInfo());
        }
    }

    public void doUnregister() {
        if (log.isDebugEnabled())
            log.debug("doUnregister received. The unregistration will be " +
                    "performed as soon as the ongoing register procedure has " +
                    "completed. " +
                    sspInstance.getDebugInfo());
        sspInstance.setPendingUnregister(true);
    }

    public void processSipOkResponse(int reregisterTime) {

        if (sspInstance.isUnregisterPending()) {

            // An unregister request has been received. Unregister now!
            if (log.isDebugEnabled())
                log.debug("SIP Ok response received while there is a " +
                        "pending unregister request. " +
                        "Unregistration is started. " +
                        sspInstance.getDebugInfo());

            sspInstance.setPendingUnregister(false);

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

        } else if (reregisterTime <= 0) {

            // There was a problem with the OK response. The re-register timer
            // was not set correctly. Backoff and retry later.
            if (log.isDebugEnabled())
                log.debug("SIP Ok response received but the register " +
                        "timer is missing or zero. The backoff timer is " +
                        "scheduled for a REGISTER retry later. " +
                        sspInstance.getDebugInfo());
            sspInstance.startBackoffTimer();
            sspInstance.markAsUnregistered();

        } else {

            // The register is completed and re-register timer is scheduled.
            if (log.isDebugEnabled())
                log.debug("SIP Ok response received. The registration " +
                        "has completed OK. " +
                        sspInstance.getDebugInfo());
            sspInstance.startRetryTimer(reregisterTime);

            log.available(
                    PROTOCOL,
                    sspInstance.getAddress().getHost(),
                    sspInstance.getAddress().getPort());

            sspInstance.setRegisteredState();
        }
    }

    public void processSipErrorResponse(int sipResponseCode) {
        if (sspInstance.isUnregisterPending()) {
            // An unregister request has been received. Unregister now!
            if (log.isDebugEnabled())
                log.debug("SIP Error response " + sipResponseCode +
                        " received while there is a pending unregister request. " +
                        "Since registration failed, unregistration is not necessary. " +
                        sspInstance.getDebugInfo());
            sspInstance.setPendingUnregister(false);
            sspInstance.setUnregisteredState();

        } else {
            if (log.isDebugEnabled())
                log.debug("SIP Error response " + sipResponseCode +
                        " received. The backoff timer is scheduled for " +
                        "a REGISTER retry later. " +
                        sspInstance.getDebugInfo());
            sspInstance.startBackoffTimer();
            sspInstance.markAsUnregistered();
        }
    }

    public void processSipTimeout() {
        if (sspInstance.isUnregisterPending()) {

            // An unregister request has been received. Unregister now!

            log.notResponding(
                    PROTOCOL,
                    sspInstance.getAddress().getHost(),
                    sspInstance.getAddress().getPort(),
                    "SIP Timeout or transaction error received " +
                        "while there is a pending unregister request. " +
                        "Unregistration is started. " +
                        sspInstance.getDebugInfo());

            sspInstance.setPendingUnregister(false);

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

        } else {
            log.notResponding(
                    PROTOCOL,
                    sspInstance.getAddress().getHost(),
                    sspInstance.getAddress().getPort(),
                    "SIP Timeout or transaction error received. " +
                            "The backoff timer is scheduled for a REGISTER retry later. " +
                            sspInstance.getDebugInfo());
            sspInstance.startBackoffTimer();
            sspInstance.markAsUnregistered();
        }
    }

    public void handleReRegisterTimeout() {
        if (log.isDebugEnabled())
            log.debug("Re-register Timeout. It is " +
                    "ignored since the register procedure already is ongoing. " +
                    sspInstance.getDebugInfo());
    }

    public void handleBackoffTimeout() {
        if (log.isDebugEnabled())
            log.debug("Backoff Timeout. Registration is started. " +
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
    }

    public String toString() {
        return "Registering";
    }
}
