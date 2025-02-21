/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.masp.mediaobject.MediaProperties;

/**
 * Factory for creation of Media Streams. Note that no connections are
 * established when the stream objects are created. Use the streams
 * <code>create</code>-method to establish a connection.
 *
 * @author Jörgen Terner
 */
public interface IStreamFactory {
    // Neither the static Factory pattern nor the Singleton pattern has been
    // used. This is because Spring should be able to handle instantiation
    // and injection of factories.

    /**
     * Creates an outbound media stream instance without establishing a
     * connection to the endpoint. Use the streams <code>create</code>-
     * method to establish a connection.
     * 
     * @return An outbound media stream instance.
     * 
     * @throws IllegalStateException If method {@link #init} has not 
     *         been called.
     */
    public IOutboundMediaStream getOutboundMediaStream();

    /**
     * Creates an inbound media stream instance without establishing a
     * connection to the endpoint. Use the streams <code>create</code>-
     * method to establish a connection.
     *
     * @return An inbound media stream instance.
     * 
     * @throws IllegalStateException If method {@link #init} has not 
     *         been called.
     */
    public IInboundMediaStream getInboundMediaStream();
    
    /**
     * Initiates the stream component. Must be called before any
     * other methods are called.
     * 
     * @throws StackException If an error occured.
     */
    public void init() throws StackException;
}