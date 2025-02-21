/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains the address of a remote party, i.e. its host and port.
 * <p>
 * Overrides the equals method. Two remote party addresses are equals if the
 * hosts are equal and the ports are equal.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class RemotePartyAddress {

    private final String host;
    private final int port;
    private AtomicInteger hashCode = new AtomicInteger(0);

    public RemotePartyAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof RemotePartyAddress))
            return false;
        RemotePartyAddress address = (RemotePartyAddress)obj;
        return address.host.equals(host) && address.port == port;
    }

    public int hashCode() {
        if (hashCode.get() == 0) {
            int result = 17;
            result = 37 * result + host.hashCode();
            result = 37 * result + port;
            hashCode.set(result);
        }
        return hashCode.get();
    }

    public String toString() {
        return host + ":" + port;
    }
}
