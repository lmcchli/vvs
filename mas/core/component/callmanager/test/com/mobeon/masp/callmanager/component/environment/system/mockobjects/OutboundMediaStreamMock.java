/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.system.mockobjects;

import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.ConnectionProperties;
import com.mobeon.masp.stream.StackException;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.masp.stream.RTCPFeedback;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.mediaobject.IMediaObject;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * A mock of IOutboundMediaStream used for testing.
 * This class is thread-safe.
 */
public class OutboundMediaStreamMock extends MediaStreamMock
        implements IOutboundMediaStream {

    private AtomicBoolean throwException = new AtomicBoolean(false);
    private AtomicReference<ControlToken[]> activeTokens =
            new AtomicReference<ControlToken[]>();
    private AtomicReference<Boolean> playDone = new AtomicReference<Boolean>();
    private AtomicReference<Boolean> playStopDone = new AtomicReference<Boolean>();


    public void clear() {
        throwException.set(false);
        activeTokens.set(null);
        playDone.set(null);
        playStopDone.set(null);
    }

    public void create(Collection<RTPPayload> payloads,
                       ConnectionProperties connectionProperties,
                       RTCPFeedback rtcpFeedback)
            throws StackException {
        if (throwException.getAndSet(false)) throw new StackException("Stack ERROR");
    }


    public void create(Collection<RTPPayload> payloads,
                       ConnectionProperties connectionProperties,
                       IInboundMediaStream inboundStream)
            throws StackException {
        if (throwException.getAndSet(false)) throw new StackException("Stack ERROR");
    }

    public void joined(IInboundMediaStream inboundStream) {        
    }

    public void unjoined() {
    }

    public boolean isJoined() {
        return false;
    }

    public long stop(Object callId) throws StackException {
        playStopDone.set(true);
        return 0;
    }

    public void translationDone(IMediaObject mediaObject) {
    }

    public void translationFailed(String cause) {
    }

    public void translationDone() {
    }

    public void send(ControlToken[] tokens) {
        activeTokens.set(tokens);
    }

    public void play(Object callId, IMediaObject mediaObject,
                     PlayOption playOption, long cursor) throws StackException {
        playDone.set(true);
    }

    public void play(Object callId, IMediaObject mediaObjects[],
                     PlayOption playOption, long cursor) throws StackException {
        playDone.set(true);
    }

    public void cancel() {
    }

    public void joined() {
    }

    public RTPPayload[] getSupportedPayloads() {
        return new RTPPayload[0];
    }

    public void throwExceptionNextCreate() {
        throwException.set(true);
    }

    public void waitForOutboundTokens(long timeoutInMilliSeconds) {
        waitForInput(timeoutInMilliSeconds, activeTokens);
    }

    public void waitForPlay(long timeoutInMilliSeconds) {
        waitForInput(timeoutInMilliSeconds, playDone);
    }

    public void waitForStopPlay(long timeoutInMilliSeconds) {
        waitForInput(timeoutInMilliSeconds, playStopDone);
    }

	public void create(Collection<RTPPayload> payloads,
			ConnectionProperties connectionProperties) throws StackException {
        if (throwException.getAndSet(false)) throw new StackException("Stack ERROR");	
		
	}

	public void create(Collection<RTPPayload> payloads,
			ConnectionProperties connectionProperties,
			RTCPFeedback rtcpFeedback, IInboundMediaStream inboundStream)
			throws StackException {
        if (throwException.getAndSet(false)) throw new StackException("Stack ERROR");	
		
	}

	public boolean usesRTCPPictureFastUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getSenderSSRC() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void sendPictureFastUpdate(int ssrc) {
		// TODO Auto-generated method stub
		
	}
}
