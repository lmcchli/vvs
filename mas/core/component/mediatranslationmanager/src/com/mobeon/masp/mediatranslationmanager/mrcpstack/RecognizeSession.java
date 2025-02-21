/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack;

import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.RtspRequest;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.SetupRequest;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.RtspResponse;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import com.mobeon.sdp.MediaDescription;
import java.util.Vector;

/**
 * This is the MRCP speech recognize session (recognizer).
 */
public class RecognizeSession extends MrcpSession {
    private static ILogger logger = ILoggerFactory.getILogger(RecognizeSession.class);

    /**
     * The constructor.
     * @param rtspSession an RTSP session.
     * @param rtpPort the client RTP port.
     */
    public RecognizeSession(RtspSession rtspSession, int rtpPort) {
        super(rtspSession, rtpPort, true);
    }

    /**
     * Setup/intialization of the recognize session.
     */
    public void setup() {
        if (logger.isDebugEnabled()) logger.debug("--> setup()");
        // Issue an RTSP SET-UP request
        RtspRequest request = new SetupRequest(true, getRtpClientPort());
        RtspResponse response = getMessageHandler().send(request);

        isOk = false;

        // Ensuring that response is not null
        if (response == null) {
            logger.error("ASR setup response is null");
            return;
        }

        if (logger.isDebugEnabled()) logger.debug("Response: " + response.getStatusCode() + ":" +
                response.getStatusText());

        // Checking if status code is OK
        if (response.getStatusCode() != RtspSession.STATUS_OK) {
            logger.error("Invalid SET-UP response: [" + response.getHeader() + "]");
            return;
        }

        // Handling the response
        if (response.getHeaderField("Session") != null) {
            setSessionId(response.getHeaderField("Session"));
        } else {
            logger.error("Session ID is missing in SET-UP response.");
            return;
        }

        // Retrieve the port number
        try {
            // Get the SDP data from the response.
            Vector mdVec = response.getSDP().getMediaDescriptions(true);
            if (mdVec.size() > 0) {
                MediaDescription md = (MediaDescription)mdVec.get(0);
                // Get the RTP server port (to wich the speech is sent).
                setRtpServerPort(md.getMedia().getMediaPort());
                isOk = true;
            } else {
                logger.error("Could not retrieve server RTP port number in SET-UP response");
            }
        } catch (Exception e) {
            logger.error("SDP parse exception", e);
        }

        // Should be ok ...
        if (isOk) if (logger.isInfoEnabled()) logger.info("Success: ASR RTSP session SET-UP");
        if (logger.isDebugEnabled()) logger.debug("<-- setup()");
    }

    /**
     * Issuing the MRCP DEFINE-GRAMMAR request.
     * @param mimeType the mime type of the grammar text.
     * @param grammar the gramamr text.
     * @param grammarId the ID of the grammar.
     * @return true if the request was successful and false otherwise.
     */
    public boolean defineGrammar(String mimeType, String grammar, String grammarId) {
        return currentState.defineGrammar(this, mimeType, grammar, grammarId);
    }

    /**
     * Issuing the MRCP RECOGNIZE request.
     * This request refere to a previously defined grammar.
     * @param grammarIds the ID of the grammar.
     * @return true if the request was successful and false otherwise.
     */
    public boolean recognize(String[] grammarIds) {
        if (logger.isDebugEnabled()) logger.debug("recognize()");
        return currentState.recognize(this, grammarIds);
    }

    /**
     * Issuing the MRCP RECOGNIZE request.
     * This request include defining a grammar.
     * @param mimeType the mime type of the grammar text.
     * @param grammar the grammar text.
     * @param grammarId the ID of the grammar.
     * @return true if the request was successful and false otherwise.
     */
    public boolean recognize(String mimeType, String grammar, String grammarId) {
        if (logger.isDebugEnabled()) logger.debug("recognize()");
        return currentState.recognize(this, mimeType, grammar, grammarId);
    }

}
