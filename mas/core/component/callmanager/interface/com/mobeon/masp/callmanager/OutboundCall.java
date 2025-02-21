/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import com.mobeon.masp.stream.ControlToken;

/**
 * Provides call management related to an active outbound call.
 * <p>
 * When an outbound call has been created, if the load situation allows a new
 * outbound call the call setup will start. During the call setup zero or more
 * {@link com.mobeon.masp.callmanager.events.ProgressingEvent} can be
 * generated. If early media is available, this is indicated in the event.
 * <p>
 * When the call setup is complete, a
 * {@link com.mobeon.masp.callmanager.events.ConnectedEvent} is
 * generated.
 *
 * @author Malin Flodin
 */
public interface OutboundCall extends Call {

    /**
     * This method is used to disconnect the call.
     */
    public void disconnect();

    /**
     * This method is used to send control tokens over the outbound call.
     *
     * @param tokens
     */
    public void sendToken(ControlToken[] tokens);
}
