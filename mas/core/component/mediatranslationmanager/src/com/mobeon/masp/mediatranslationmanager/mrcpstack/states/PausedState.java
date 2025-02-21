/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.states;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.MrcpSession;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;

/**
 * This is the Paused state in the MRCP state machine.
 * Pause can only occur during a speech.
 * PausedState is a singleton.
 */
public class PausedState extends MrcpState {
    static private MrcpState singletonInstance = new PausedState();
    private static ILogger logger = ILoggerFactory.getILogger(SpeakingState.class);

    /**
     * Singleton getter.
     * @return the singleton instance of PausedState.
     */
    static MrcpState getSingleton() {
        return singletonInstance;
    }

    /**
     * Default constructor.
     * The constructor is private according to the Singleton pattern.
     */
    private PausedState() {}

    /**
     * Issues MRCP RESUME.
     * The MRCP RESUME is issued. A transition from PAUSED to SPEAKING state is taken.
     * @param session the current MRCP session (the state machine).
     */
    public void resume(MrcpSession session) {
        if (logger.isDebugEnabled()) logger.debug("resume()");
        if (logger.isDebugEnabled()) logger.debug("Issuing RESUME SPEAK from PausedState");
        RtspRequest request = new ResumeRequest();
        request.setHeaderField("Session", session.getSessionId());
        RtspResponse response = session.send(request);
        if (isOk(response)) {
            session.transition(SpeakingState.getSingleton());
        } else {
            logger.error("Failed to RESUME speech in PAUSED state, RTSP header: [" + response.getHeader() + "]");
            session.transition(IdleState.getSingleton());
        }
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
        logger.warn("MRCP stack received an unhandled message when in PAUSED state.");
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
        return StateName.PAUSED;
    }

}
