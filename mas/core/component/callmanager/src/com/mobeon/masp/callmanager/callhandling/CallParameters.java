/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.RedirectingParty;

/**
 * A container of call parameters.
 * As a container, it provides only setters and getters.
 * All getters return null or zero (for return type int) if values have not
 * been previously set.
 *
 * This class is thread-safe.
 * 
 * @author Malin Flodin
 */
public class CallParameters {
    private CallingParty callingParty;
    private CalledParty calledParty;
    private RedirectingParty redirectingParty;

    public synchronized CallingParty getCallingParty() {
        return callingParty;
    }

    public synchronized void setCallingParty(CallingParty callingParty) {
        this.callingParty = callingParty;
    }

    public synchronized CalledParty getCalledParty() {
        return calledParty;
    }

    public synchronized void setCalledParty(CalledParty calledParty) {
        this.calledParty = calledParty;
    }

    public synchronized RedirectingParty getRedirectingParty() {
        return redirectingParty;
    }

    public synchronized void setRedirectingParty(RedirectingParty redirectingParty) {
        this.redirectingParty = redirectingParty;
    }
}
