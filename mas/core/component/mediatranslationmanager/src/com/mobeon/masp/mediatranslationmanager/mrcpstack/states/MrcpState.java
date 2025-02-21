/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.states;

import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.MrcpSession;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspSession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * This is a base class for the MRCP states in the MRCP state machine (see {@link MrcpSession}).
 */
public abstract class MrcpState {
    private static ILogger logger = ILoggerFactory.getILogger(MrcpState.class);

    /**
     * Issues MRCP SPEAK (implemented in sub-class).
     * The MRCP SPEAK request is issued. If the request is successful a transition to
     * the MRCP SPEAKING state is taken.
     * @param session the current MRCP session (the state machine).
     * @param mimeType the mime type of the text to be spoken.
     * @param text the speech text.
     * @return true upon success (if entering SPEAKING) false otherwise.
     */
    public boolean speak(MrcpSession session, String mimeType, String text) {
        if (logger.isDebugEnabled()) logger.debug("Speak is not implemented for this state!");
        return false;
    }
    /**
     * Issues MRCP RECOGNIZE (implemented in sub-class).
     * The MRCP RECOGNIZE request is issued. If the request is successful a transition to
     * the MRCP RECOGNIZING state is taken.
     * @param session the current MRCP session (the state machine).
     * @param grammarIds the ID of a previously defined grammar.
     * @return true upon success (if entering RECOGNIZING) false otherwise.
     */
    public boolean recognize(MrcpSession session, String ... grammarIds) {
        if (logger.isDebugEnabled()) logger.debug("Recognize is not implemented for this state!");
        return false;
    }

    /**
     * Issues MRCP RECOGNIZE (implemented in sub-class).
     * The MRCP RECOGNIZE request is issued. If the request is successful a transition to
     * the MRCP RECOGNIZING state is taken.
     * @param session the current MRCP session (the state machine).
     * @param mimeType grammar text mime type.
     * @param grammar grammar text.
     * @param grammarId the ID of the grammar (a unique string).
     * @return true upon success (if entering RECOGNIZING) false otherwise.
     */
    public boolean recognize(MrcpSession session, String mimeType, String grammar, String grammarId) {
        if (logger.isDebugEnabled()) logger.debug("Recognize is not implemented for this state!");
        return false;
    }

    /**
     * Issues MRCP DEFINE-GRAMMAR (implemented in sub-class).
     * The MRCP DEFINE-GRAMMAR is issued. No transition is taken.
     * @param session the current MRCP session (the state machine).
     * @param mimeType grammar text mime type.
     * @param grammar grammar text.
     * @param grammarId grammarId the ID of the grammar (a unique string).
     * @return true upon success, false otherwise.
     */
    public boolean defineGrammar(MrcpSession session, String mimeType, String grammar, String grammarId) {
        if (logger.isDebugEnabled()) logger.debug("Define Grammar is not implemented for this state!");
        return false;
    }

    /**
     * Issues MRCP STOP.
     * The MRCP STOP is issued. A transition to the Idle state is taken.
     * @param session the current MRCP session (the state machine).
     * @return false upon success and false otherwise.
     */
    public boolean stop(MrcpSession session) {
        if (logger.isDebugEnabled()) logger.debug("stop()");
        if (logger.isDebugEnabled()) logger.debug("Issuing STOP");
        boolean result = false;
        RtspRequest request = new StopRequest(session.isRecognizeSession());
        request.setHeaderField("Session", session.getSessionId());
        RtspResponse response = session.send(request);
        if (isOk(response)) {
            session.transition(IdleState.getSingleton());
            result = true;
        } else {
            logger.warn("STOP request failed");
            if (response == null) {
                logger.warn("RTSP response is null");
            } else {
                logger.warn("RTSP response header : [" + response.getHeader() + "]");
            }
            session.transition(IdleState.getSingleton());
        }
        return result;
    }

    /**
     * Issues MRCP PAUSE (implemented in sub-class).
     * The MRCP PAUSE is issued. A transition to the PAUSED state is taken.
     * @param session the current MRCP session (the state machine).
     */
    public void pause(MrcpSession session) {
        if (logger.isDebugEnabled()) logger.debug("Pause is not implemented for this state!");
    }

    /**
     * Issues MRCP RESUME (implemented in sub-class).
     * The MRCP RESUME is issued. A transition from PAUSED to SPEAKING state is taken.
     * @param session the current MRCP session (the state machine).
     */
    public void resume(MrcpSession session) {
        if (logger.isDebugEnabled()) logger.debug("Resume is not implemented for this state!");
    }

    /**
     * Handles messages sent from the MRCP server.
     * @param session the current MRCP session (the state machine).
     * @param message the received RTSP message (the MRCP message is attached, if any).
     */
    public abstract void handleMessage(MrcpSession session, RtspMessage message);

    /**
     * Getter for the state name/ID.
     * @return the state ID.
     */
    public abstract StateName getId();

    /**
     * Checks if an RTSP response correspond to a successful MRCP request.
     * @param response an RTSP response.
     * @return true upon success and false otherwise.
     */
    public boolean isOk(RtspResponse response) {
        return isOk(response, "COMPLETE");
    }

    /**
     * Checks if an RTSP response correspond to a successful MRCP request.
     * @param response an RTSP response.
     * @return true upon success and false otherwise.
     */
    public boolean isOk(RtspResponse response, String mrcpName) {
        if (response == null) {
            if (logger.isInfoEnabled()) logger.info("Got a null response");
            return false;
        }
        if (logger.isDebugEnabled()) logger.debug("RTSP Response: " + response.getStatusText());
        MrcpMessage mrcp = response.getMrcpMessage();
        boolean mrcpOk = false;
        if (mrcp == null) {
            logger.error("Expected MRCP [" + mrcpName + 
                    "] got null pointer MRCP message in RTSP response: [" + response.getHeader() + "]");
            return false;
        }

        if (!(mrcp instanceof MrcpResponse)) {
            logger.error("Expected MRCP Response [" + mrcpName +
                    "] got MRCP message: [" + mrcp.getMessage() + "]");
            return false;
        }

        if (logger.isDebugEnabled()) logger.debug("MRCP Response: " + mrcp.getName());
        if (logger.isDebugEnabled()) logger.debug("MRCP Response status code: " + ((MrcpResponse)mrcp).getStatusCode());
        // The status code of success is always 200 all other values are some kind of failure
        if (((MrcpResponse)mrcp).getStatusCode() != 200) {
            logger.warn("MRCP Response status code not ok: " + ((MrcpResponse)mrcp).getStatusCode());
            // Determine cause of non-success
            if ("COMPLETE".equals(mrcpName)) {
                String completionCause = mrcp.getHeaderField("Completion-Cause");
                if (completionCause != null) {
                    String id = completionCause.substring(0, 3);
                    String text = completionCause.substring(4);
                    if (logger.isDebugEnabled()) logger.debug("Completion-Cause: [" + id + "][" + text +"]");
                }
                logger.warn("MRCP Completion-Cause: [" + completionCause + "]");
            }
        }
        return response.getStatusCode() == RtspSession.STATUS_OK;
    }
}
