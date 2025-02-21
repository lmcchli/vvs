/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.system.mockobjects;

import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.masp.stream.StackException;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.RecordingProperties;
import com.mobeon.masp.stream.VideoFastUpdater;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.IMediaObject;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A mock of IInboundMediaStream used for testing.
 * This class is thread-safe.
 */
public class InboundMediaStreamMock extends MediaStreamMock
        implements IInboundMediaStream {

    private AtomicBoolean throwException = new AtomicBoolean(false);
    private AtomicReference<String> localHost = new AtomicReference<String>();
    private AtomicReference<VideoFastUpdater> videoFastUpdater = new AtomicReference<VideoFastUpdater>();
    private AtomicReference<Boolean> recordDone = new AtomicReference<Boolean>();
    private AtomicReference<Boolean> recordStopDone = new AtomicReference<Boolean>();

    public void clear() {
        throwException.set(false);
        videoFastUpdater = null;
        recordDone = null;
        recordStopDone = null;
    }

    public void create(MediaMimeTypes mediaMimeTypes) throws StackException {
        if (throwException.getAndSet(false)) throw new StackException("Stack ERROR");
    }

    public void create(
            VideoFastUpdater videoFastUpdater, MediaMimeTypes mediaMimeTypes)
            throws StackException {
        this.videoFastUpdater.set(videoFastUpdater);
        if (throwException.getAndSet(false)) throw new StackException("Stack ERROR");
    }

    public void create(Collection<RTPPayload> rtpPayloads)
    	throws StackException {
        if (throwException.getAndSet(false)) throw new StackException("Stack ERROR");
    }

    public void create(VideoFastUpdater videoFastUpdater,
	    Collection<RTPPayload> rtpPayloads) throws StackException {
        this.videoFastUpdater.set(videoFastUpdater);
        if (throwException.getAndSet(false)) throw new StackException("Stack ERROR");
    }

    public void join(IOutboundMediaStream outboundStream) throws StackException {
	if (throwException.getAndSet(false)) throw new StackException("Stack ERROR");
    }

    public void join(boolean handleDtmfAtInbound,
                     IOutboundMediaStream iOutboundMediaStream,
                     boolean forwardDtmfToOutbound) throws StackException {
        if (throwException.getAndSet(false)) throw new StackException("Stack ERROR");
    }

    public void unjoin(IOutboundMediaStream outboundStream) throws StackException {
        if (throwException.getAndSet(false)) throw new StackException("Stack ERROR");
    }

    public void record(Object callId, IMediaObject mediaObject,
                       RecordingProperties properties) throws StackException {
        recordDone.set(true);
    }

    public void record(Object callId, IMediaObject playMediaObject,
                       IOutboundMediaStream s, IMediaObject mediaObject,
                       RecordingProperties properties) throws StackException {
        recordDone.set(true);
    }

    public long stop(Object callId) throws StackException {
        recordStopDone.set(true);
        return 0;
    }

    public int getCumulativePacketLost() throws StackException {
        return 10;
    }

    public short getFractionLost() throws StackException {
        return 0;
    }

    public int getPTime() {
	// We use a bit odd values here so that it differs from any default value
        return 15;	 
    }

    public int getMaxPTime() {
	// We use a bit odd values here so that it differs from any default value
	return 30;
    }

    public String getHost() {
        return localHost.get();
    }

    public void throwExceptionNextMethod() {
        throwException.set(true);
    }

    public void setHost(String host) {
        this.localHost.set(host);
    }

    public void initiateVideoFastUpdate() {
        videoFastUpdater.get().sendPictureFastUpdateRequest();
    }

    public void sendPictureFastUpdateRequest() {}

    public int getInboundBitRate() {
        return 12000;
    }

    public void waitForRecord(long timeoutInMilliSeconds) {
        waitForInput(timeoutInMilliSeconds, recordDone);
    }

    public void waitForStopRecord(long timeoutInMilliSeconds) {
        waitForInput(timeoutInMilliSeconds, recordStopDone);
    }

	public int getSenderSSRC() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void sendPictureFastUpdate(int ssrc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reNegotiatedSdp(RTPPayload dtmfPayLoad) throws StackException {
		// TODO Auto-generated method stub
		
	}
}
