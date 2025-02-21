package com.mobeon.masp.stream.mock;

import com.mobeon.masp.stream.RTPSessionFactory;
import com.mobeon.masp.stream.RTPSession;

public class MockRTPSessionFactory implements RTPSessionFactory {
    public void init() {
    }

    /**
     * Creates an inbound RTP Session
     *
     * @return a new inbound RTP session
     */
    public RTPSession createInboundRTPSession() {
        return new MockRTPSession();
    }

    /**
     * Creates an outbound RTP Session
     *
     * @return a new outbound RTP session
     */
    public RTPSession createOutboundRTPSession() {
        return new MockRTPSession();
    }
}
