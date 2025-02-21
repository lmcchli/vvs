package com.mobeon.masp.stream;

import com.mobeon.masp.stream.jni.NativeStreamHandling;

/**
 * This is a factory for creating ccRTP sessions (interfaced/proxied through JNI).
 */
public class CCRTPSessionFactory implements RTPSessionFactory {
    public void init() {
        NativeStreamHandling.initialize();
    }
    /**
     * Creates an inbound RTP Session
     *
     * @return a new inbound RTP session
     */
    public RTPSession createInboundRTPSession() {
        return CCRTPSession.getInbound();
    }

    /**
     * Creates an outbound RTP Session
     *
     * @return a new outbound RTP session
     */
    public RTPSession createOutboundRTPSession() {
        return CCRTPSession.getOutbound();
    }
}
