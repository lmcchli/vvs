/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.states;

import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.MrcpSession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * This is the Speaking state in the MRCP state machine.
 * In the Speaking state, the MRCP state machine, is pending for a SPEAK-COMPLETE event. Any other
 * event is considered as a failure.
 * IdleState is a Singleton.
 */
public class SpeakingState extends MrcpState {
    private static ILogger logger = ILoggerFactory.getILogger(SpeakingState.class);
    static private MrcpState singletonInstance = new SpeakingState();

    /**
     * Singleton getter.
     * @return the singleton instance of SpeakingState.
     */
    public static MrcpState getSingleton() {
        return singletonInstance;
    }

    /**
     * Default constructor.
     * The constructor is private according to the Singleton pattern.
     */
    private SpeakingState() {}


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
     * Issues MRCP PAUSE.
     * The MRCP PAUSE is issued. A transition to the PAUSED state is taken.
     * @param session the current MRCP session (the state machine).
     */
    public void pause(MrcpSession session) {
        if (logger.isDebugEnabled()) logger.debug("pause()");
        if (logger.isDebugEnabled()) logger.debug("Issuing MRCP PAUSE from SpeakingState");
        RtspRequest request = new PauseRequest();
        request.setHeaderField("Session", session.getSessionId());
        RtspResponse response = session.send(request);
        if (isOk(response)) {
            session.transition(PausedState.getSingleton());
        } else {
            logger.error("Failed to PAUSE speech in SPEAKING state, RTSP header: [" + response.getHeader() + "]");
            session.transition(IdleState.getSingleton());
        }
    }

    /**
     * Issues MRCP RESUME.
     * The MRCP RESUME is issued. A transition from PAUSED to SPEAKING state is taken.
     * @param session the current MRCP session (the state machine).
     */
     public void resume(MrcpSession session) {
        if (logger.isDebugEnabled()) logger.debug("Error: Resume is illegal in the Speaking state!");
    }

    /**
     * Handles messages sent from the MRCP server.
     * @param session the current MRCP session (the state machine).
     * @param message the received RTSP message (the MRCP message is attached, if any).
     */
    public void handleMessage(MrcpSession session, RtspMessage message) {
        MrcpMessage mrcp = message.getMrcpMessage();
        if (mrcp != null) {
            if (logger.isDebugEnabled()) logger.debug("SpeakingState got MRCP message: " + mrcp.getName());
            if (mrcp.getName().equals("SPEAK-COMPLETE")) {
                session.transition(IdleState.getSingleton());
                session.notify(MrcpEventId.SPEAK_COMPLETE, "");
            } else {
                logger.warn("Unhandled MRCP message in SPEAKING state: [" + mrcp.getHeader() + "]");
            }
        } else {
            logger.warn("Null MRCP message in SPEAKING state: [" + message.getHeader() + "]");
        }
    }

    /**
     * Getter for the state name/ID.
     * @return the state ID.
     */
    public StateName getId() {
        return StateName.SPEAKING;
    }
}
