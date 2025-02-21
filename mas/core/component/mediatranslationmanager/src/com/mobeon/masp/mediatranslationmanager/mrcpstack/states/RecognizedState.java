/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.states;

import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.MrcpSession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * This is the Recognized state in the MRCP state machine.
 * This state is entered after a successful recognition.
 * RecognizedState is a singleton.
 */
public class RecognizedState extends MrcpState {
    private static ILogger logger = ILoggerFactory.getILogger(RecognizedState.class);
    static private MrcpState singletonInstance = new RecognizedState();

    /**
     * Singleton getter.
     * @return the singleton instance of RecognizedState.
     */
    public static MrcpState getSingleton() {
        return singletonInstance;
    }

    /**
     * Default constructor.
     * The constructor is private according to the Singleton pattern.
     */
    private RecognizedState() {}

    /**
     * Issues MRCP RECOGNIZE.
     * The MRCP RECOGNIZE request is issued. If the request is successful a transition to
     * the MRCP RECOGNIZING state is taken.
     * @param session the current MRCP session (the state machine).
     * @param grammarIds the ID of a previously defined grammar.
     * @return true upon success (if entering RECOGNIZING) false otherwise.
     */
    public boolean recognize(MrcpSession session, String ... grammarIds) {
        logger.debug("recognize()");
        logger.debug("Issuing MRCP RECOGNIZE from RecognizedState");
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
        if (logger.isDebugEnabled()) logger.debug("Issuing MRCP RECOGNIZE from RecognizedState");
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
        if (logger.isDebugEnabled()) logger.debug("defineGrammar()");
        if (logger.isDebugEnabled()) logger.debug("Issuing MRCP DEFINE-GRAMMAR from RecognizedState");
        boolean result = false;
        RtspRequest request = new DefineGrammarRequest(mimeType, grammar, grammarId);
        request.setHeaderField("Session", session.getSessionId());
        if (isOk(session.send(request))) {
            result = true;
//            session.transition(IdleState.getSingleton());
        }
        return result;
    }

    /**
     * Issues MRCP STOP.
     * The MRCP STOP is issued. A transition to the Idle state is taken.
     * @param session the current MRCP session (the state machine).
     * @return false upon success and false otherwise.
     */
    public boolean stop(MrcpSession session) {
        return super.stop(session);
    }

    /**
     * Handles messages sent from the MRCP server.
     * @param session the current MRCP session (the state machine).
     * @param message the received RTSP message (the MRCP message is attached, if any).
     */
    public void handleMessage(MrcpSession session, RtspMessage message) {
        // This should never happen
        logger.warn("MRCP stack received an unhandled message when in RECOGNIZED state.");
        if (message != null) {
            logger.warn("RTSP header is: [" + message.getHeader() + "]");
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
        return StateName.RECOGNIZED;
    }
}
