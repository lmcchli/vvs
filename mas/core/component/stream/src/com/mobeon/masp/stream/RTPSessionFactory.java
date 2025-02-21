package com.mobeon.masp.stream;

/**
 * The RTPSessionFactory provide an interface for creating RTP Sessions.
 */
public interface RTPSessionFactory {
    public void init();
    
    /**
     * Creates an inbound RTP Session
     * @return a new inbound RTP session
     */
    public RTPSession createInboundRTPSession();

    /**
     * Creates an outbound RTP Session
     * @return a new outbound RTP session
     */
    public RTPSession createOutboundRTPSession();
}
