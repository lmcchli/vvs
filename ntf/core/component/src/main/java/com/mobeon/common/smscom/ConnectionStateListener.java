/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom;


/**
 * ConnectionStateListener specifies an interface for clients that want to know
 * the state of an SMSC connection.
 * <P>
 * There are two states; up and down. Up means that the connection is fully
 * functional and down means that either login or connecting has failed.
 */
public interface ConnectionStateListener {
    /**
     * Tells that the connection is OK.
     * @param name - the name of the connection.
     */
    public void connectionUp(String name);

    /**
     * Tells that the connection can not be used.
     * @param name - the name of the connection
     */
    public void connectionDown(String name);

    /**
     * The party on the other end of the connection is insane, so the connection
     * has been closed and can not be used. The error was on a higher protocol
     * level, so there was nothing wrong with the connection itself. Retrying
     * the request on a new connection may work.
     *
     * This method is called e.g. if the other end does not reply to requests,
     * or replies to the wrong request.
     */
    public void connectionReset(String name);
    
    /**
     * Indicates that the connection should be made temporarily unavailable for new requests.
     */
    public void connectionTemporaryUnavailableForNewRequests();
}
