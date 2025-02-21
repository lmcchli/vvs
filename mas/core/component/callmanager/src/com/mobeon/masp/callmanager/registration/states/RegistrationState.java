/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration.states;

import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Interface for a registration state, i.e. the state of the registration towards
 * a specific SSP. Could be either Unregistered, Registered, Unregistering or
 * Registering.
 *
 * @author Malin Flodin
 */
public abstract class RegistrationState {

    protected final HostedServiceLogger log = new HostedServiceLogger(
            ILoggerFactory.getILogger(getClass()));

    protected static final String PROTOCOL = "sip";

    /**
     * Event that informs the state to start a SIP REGISTER procedure.
     */
    public abstract void doRegister();

    /**
     * Event that informs the state to start a SIP UNREGISTER procedure.
     */
    public abstract void doUnregister();

    /**
     * Event that informs the state that a SIP OK response has been received
     * for the ongoing REGISTER or UNREGISTER procedure.
     * @param reregisterTime
     */
    public abstract void processSipOkResponse(int reregisterTime);

    /**
     * Event that informs the state that a SIP Error response has been received
     * for the ongoing REGISTER or UNREGISTER procedure.
     * @param sipResponseCode
     */
    public abstract void processSipErrorResponse(int sipResponseCode);

    /**
     * Event that informs the state that a SIP timeout or transaction error has
     * occurred for the ongoing REGISTER or UNREGISTER procedure.
     */
    public abstract void processSipTimeout();

    /**
     * Event that informs the state that the Re-register Timer has expired and
     * that a SIP REGISTER procedure should be re-initiated.
     */
    public abstract void handleReRegisterTimeout();

    /**
     * Event that informs the state that the Backoff timer has expired and that
     * it is ok to try a REGISTER or UNREGISTER procedure again.
     */
    public abstract void handleBackoffTimeout();
}
