/*
 * Copyright (c) 2010 Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

import java.net.InetSocketAddress;

import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;

/**
 * A proxy event contains all information regarding proxying inbound call.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 * <p>
 * This class is immutable.
 */
public class ProxyEvent extends CallCommandEvent {

    private RemotePartyAddress uas = null;

    public ProxyEvent(RemotePartyAddress uas) {
        this.uas = uas;
    }

    public RemotePartyAddress getUas() {
        return uas;
    }

    public String toString() {
        return "ProxyEvent (uas = " + uas + ")";
    }
}
