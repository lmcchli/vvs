/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.mrcpstack.states;

import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.MrcpSession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * This is the Idle state in the MRCP state machine.
 * In the Idle state the MRCP state machine is pending for either SPEAK or RECOGNIZE requests.
 * There can also be configuration requests such as DEFINE-GRAMMAR. But only SPEAK and RECOGNIZE will
 * result in a state transition.
 * IdleState is a Singleton.
 */
public class IdleState extends MrcpState {
    static private MrcpState singletonInstance = new IdleState();
    private static ILogger logger = ILoggerFactory.getILogger(IdleState.class);

    /**
     * Singleton getter.
     * @return the singleton instance of IdleState.
     */
    public static MrcpState getSingleton() {
        return singletonInstance;
    }

    /**
     * Default constructor.
     * The constructor is private according to the Singleton pattern.
     */
    private IdleState() {}

    /**
     * Issues MRCP SPEAK.
     * The MRCP SPEAK request is issued. If the request is successful a transition to
     * the MRCP SPEAKING state is taken.
     * @param session the current MRCP session (the state machine).
     * @param mimeType the mime type of the text to be spoken.
     * @param text the speech text.
     * @return true upon success (if entering SPEAKING) false otherwise.
     */
    public boolean speak(MrcpSession session, String mimeType, String text) {
        if (logger.isDebugEnabled()) logger.debug("speak()");
        if (logger.isDebugEnabled()) logger.debug("Issuing MRCP SPEAK from IdleState");
        boolean result = false;
        RtspRequest request = new SpeakRequest(mimeType, text);
        request.setHeaderField("Session", session.getSessionId());
        if (isOk(session.send(request), "IN-PROGRESS")) {
            session.transition(SpeakingState.getSingleton());
            result = true;
        }
        return result;
    }

    /**
     * Issues MRCP RECOGNIZE.
     * The MRCP RECOGNIZE request is issued. If the request is successful a transition to
     * the MRCP RECOGNIZING state is taken.
     * @param session the current MRCP session (the state machine).
     * @param grammarIds the IDs of a previously defined grammars.
     * @return true upon success (if entering RECOGNIZING) false otherwise.
     */
    public boolean recognize(MrcpSession session, String ... grammarIds) {
        if (logger.isDebugEnabled()) logger.debug("recognize()");
        if (logger.isDebugEnabled()) logger.debug("Issuing MRCP RECOGNIZE from IdleState");
        boolean result = false;
        RtspRequest request = new RecognizeRequest(grammarIds);
        request.setHeaderField("Session", session.getSessionId());
        if (isOk(session.send(request), "IN-PROGRESS")) {
            session.transition(RecognizingState.getSingleton());
            result = true;
        }
        return result;
    }

    /**
     * Issues MRCP RECOGNIZE.
     * The MRCP RECOGNIZE request is issued. If the request is successful a transition to
     * the MRCP RECOGNIZING state is taken.
     * @param session the current MRCP session (the state machine).
     * @param mimeType grammar text mime type.
     * @param grammar grammar text.
     * @param grammarId the ID of the grammar (a unique string).
     * @return true upon success (if entering RECOGNIZING) false otherwise.
     */
    public boolean recognize(MrcpSession session, String mimeType, String grammar, String grammarId) {
        if (logger.isDebugEnabled()) logger.debug("recognize()");
        if (logger.isDebugEnabled()) logger.debug("Issuing MRCP RECOGNIZE from IdleState");
        boolean result = false;
        RtspRequest request = new RecognizeRequest(mimeType, grammar, grammarId);
        request.setHeaderField("Session", session.getSessionId());
        if (isOk(session.send(request), "IN-PROGRESS")) {
            session.transition(RecognizingState.getSingleton());
            result = true;
        }
        return result;
    }

    /**
     * Issues MRCP DEFINE-GRAMMAR.
     * The MRCP DEFINE-GRAMMAR is issued. No transition is taken.
     * @param session the current MRCP session (the state machine).
     * @param mimeType grammar text mime type.
     * @param grammar grammar text.
     * @param grammarId grammarId the ID of the grammar (a unique string).
     * @return true upon success, false otherwise.
     */
    public boolean defineGrammar(MrcpSession session, String mimeType, String grammar, String grammarId) {
        if (logger.isDebugEnabled()) logger.debug("--> defineGrammar()");
        if (logger.isDebugEnabled()) logger.debug("Issuing MRCP DEFINE-GRAMMAR from IdleState");
        boolean result = false;
        RtspRequest request = new DefineGrammarRequest(mimeType, grammar, grammarId);
        request.setHeaderField("Session", session.getSessionId());
        if (logger.isDebugEnabled()) logger.debug("request: " + request);
        if (isOk(session.send(request))) {
            result = true;
        }
        if (logger.isDebugEnabled()) logger.debug("<-- defineGrammar()");
        return result;
    }

    /**
     * Stop is not implemented in the Idle state.
     * @param session the current MRCP session (the state machine).
     * @return false.
     */
    public boolean stop(MrcpSession session) {
        return false;
    }

    /**
     * Handles messages sent from the MRCP server.
     * There should not be any messages received in the Idle state.
     * @param session the current MRCP session (the state machine).
     * @param message the received RTSP message (the MRCP message is attached, if any).
     */
    public void handleMessage(MrcpSession session, RtspMessage message) {
        // This should never happen
        logger.warn("MRCP stack received an unhandled message when in IDLE state.");
        if (message != null) {
            logger.warn("RTSP header is: [" + message.getHeader() + "]");
            logger.warn("RTSP message is: [" + message.getMessage() + "]");
            MrcpMessage mrcp = message.getMrcpMessage();
            if (mrcp != null) {
                logger.warn("MRCP header is: [" + mrcp.getHeader() + "]");
            }
        }
    }

    /**
     * Getter for the state name/ID.
     * @return the state ID.
     */
    public StateName getId() {
        return StateName.IDLE;
    }

}
