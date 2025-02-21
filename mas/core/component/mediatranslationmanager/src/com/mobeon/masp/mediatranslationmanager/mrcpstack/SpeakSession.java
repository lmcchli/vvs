/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack;

import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.RtspRequest;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.RtspResponse;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.SetupRequest;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * This is the MRCP text to speech session (synthesizer).
 */
public class SpeakSession extends MrcpSession {
    private static ILogger logger = ILoggerFactory.getILogger(SpeakSession.class);

    /**
     * The constructor.
     * @param rtspSession an RTSP session.
     * @param rtpPort the client RTP port.
     */
    public SpeakSession(RtspSession rtspSession, int rtpPort) {
        super(rtspSession, rtpPort, false);
    }

    /**
     * Setup/intialization of the speech session.
     */
    public void setup() {
        if (logger.isDebugEnabled()) logger.debug("--> setup()");
        RtspRequest request = new SetupRequest(false, getRtpClientPort());
        RtspResponse response = getMessageHandler().send(request);

        // Assume set-up failure
        isOk = false;

        // Ensure that response is not null
        if (response == null) {
            logger.error("SET-UP response is null");
            return;
        }

        if (logger.isDebugEnabled()) logger.debug("Response: " + response.getStatusCode() + ":" +
                response.getStatusText());

        // Check if response is status OK
        if (response.getStatusCode() != RtspSession.STATUS_OK) {
            logger.error("Invalid SET-UP response: [" + response.getHeader() + "]");
            return;
        }

        // Ensure that we have a session ID
        if (response.getHeaderField("Session") != null) {
            setSessionId(response.getHeaderField("Session"));
            isOk = true;
            if (logger.isInfoEnabled()) logger.info("Success: TTS RTSP session SET-UP");
        } else {
            // This is an error. We can not continue without
            // a session ID.
            logger.error("Session ID is missing in SET-UP response.");
        }
        if (logger.isDebugEnabled()) logger.debug("<-- setup()");
    }

    /**
     * Issuing the MRCP SPEAK request and entering the Speaking state.
     * @param mimeType the mime type of the text to be spoken.
     * @param text the text to be spoken.
     */
    public void speak(String mimeType, String text) {
        if (logger.isDebugEnabled()) logger.debug("speak()");
        currentState.speak(this, mimeType, text);
    }

    /**
     * Issuing the MRCP PAUSE request and entering the Paused state.
     */
    public void pause() {
        if (logger.isDebugEnabled()) logger.debug("pause()");
        currentState.pause(this);
    }

    /**
     * Issuing the MRCP RESUME request and entering the Speaking state.
     */
    public void resume() {
        if (logger.isDebugEnabled()) logger.debug("resume()");
        currentState.resume(this);
    }
}
