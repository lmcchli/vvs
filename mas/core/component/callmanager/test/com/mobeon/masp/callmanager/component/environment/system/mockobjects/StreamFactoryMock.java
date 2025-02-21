/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.system.mockobjects;

import com.mobeon.masp.stream.IStreamFactory;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.StackException;

/**
 * A mock of IStreamFactory used for testing.
 * This class is immutable.
 */
public class StreamFactoryMock implements IStreamFactory {
    private final IOutboundMediaStream outboundMediaStream;
    private final IInboundMediaStream inboundMediaStream;

    public StreamFactoryMock(IOutboundMediaStream outboundMediaStream,
                             IInboundMediaStream inboundMediaStream) {
        this.outboundMediaStream = outboundMediaStream;
        this.inboundMediaStream = inboundMediaStream;
    }

    public IOutboundMediaStream getOutboundMediaStream() {
        return outboundMediaStream;
    }

    public IInboundMediaStream getInboundMediaStream() {
        return inboundMediaStream;
    }
    
    public void init() throws StackException {
    }
}
