/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.system.mockobjects;

import com.mobeon.masp.stream.IMediaStream;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A mock of IMediaStream used for testing.
 * This class is thread-safe.
 */
public abstract class MediaStreamMock implements IMediaStream {
    private static final int AUDIO_PORT = 22220;
    private static final int AUDIO_CONTROL_PORT = 22221;
    private static final int VIDEO_PORT = 22222;
    private static final int VIDEO_CONTROL_PORT = 22223;

    protected final ILogger log = ILoggerFactory.getILogger(getClass());

    public void setSkew(SkewMethod method, long skew) {
    }

    public void delete() {
    }

    public int getAudioPort() {
        return AUDIO_PORT;
    }

    public int getAudioControlPort() {
        return AUDIO_CONTROL_PORT;
    }

    public int getVideoPort() {
        return VIDEO_PORT;
    }

    public int getVideoControlPort() {
        return VIDEO_CONTROL_PORT;
    }

    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
    }

    public void setCallSession(ISession callSession) {
    }

    public void setCNAME(String name) {
    }

    public IEventDispatcher getEventDispatcher() { return null; }

    public void waitForInput(long timeoutInMilliSeconds, AtomicReference input) {
        long startTime = System.currentTimeMillis();

        while (input.get() == null &&
                (System.currentTimeMillis() < startTime + timeoutInMilliSeconds)) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.debug("Interrupted while waiting for input.");
                break;
            }
        }
    }
}
