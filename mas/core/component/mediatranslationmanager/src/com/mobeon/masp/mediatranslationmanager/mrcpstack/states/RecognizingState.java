/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.mrcpstack.states;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.MrcpSession;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;

/**
 * This is the Recognizing state in the MRCP state machine.
 * In the Recognizing state, the MRCP state machine, is pending for a RECOGNITION-COMPLETE event. Any other
 * event is considered as a failure. Any other completion cause than "000 success" is considered as failure.
 * Upon success the Recognized state is entered otherwise the Idle state is entered.
 * NB! If Recognize fails due to timeout the RTSP session is teared down by the MRCP server.
 * IdleState is a Singleton.
 */
public class RecognizingState extends MrcpState {
    private static ILogger logger = ILoggerFactory.getILogger(RecognizingState.class);
    static private MrcpState singletonInstance = new RecognizingState();

    /**
     * Singleton getter.
     * @return the singleton instance of RecognizingState.
     */
    public static MrcpState getSingleton() {
        return singletonInstance;
    }

    /**
     * Default constructor.
     * The constructor is private according to the Singleton pattern.
     */
    private RecognizingState() {}

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
        if (logger.isDebugEnabled()) logger.debug("--> handleMessage() [" + message.getMessageType() + "]");
        MrcpMessage mrcpMessage = message.getMrcpMessage();
        if (mrcpMessage == null) {
            logger.error("In SPEAKING state: Can't handle null pointer MRCP message [" + message.getHeader() + "]");
            throw new IllegalStateException("Mrcp message is null [" + message.getHeader() + "]");
        }
        switch (mrcpMessage.getMessageType()) {
            case MRCP_EVENT:
                MrcpEvent event = (MrcpEvent)mrcpMessage;
                // TODO: handle timeout, failures ...
                if (event.getName().equals("RECOGNITION-COMPLETE")) {
                    // TODO: must handle timeout ...
                    String result = event.getHeaderField("Completion-Cause");
                    if (logger.isDebugEnabled()) logger.debug("Completion-Cause: " + result);
                    int statusCode = Integer.parseInt(result.substring(0,3));
                    if (logger.isDebugEnabled()) logger.debug("Completion-Cause code: " + statusCode);
                    switch (statusCode) {
                        case MrcpMessage.SUCCESS:
                            // This should be a valid recignize, hence the MRCP content is NLSML
                            // TODO: String type = event.getHeaderField("type")
                            // TODO: verify that this is NLSML
                            result = mrcpMessage.getContent();
                            session.transition(RecognizedState.getSingleton());
                            if (logger.isDebugEnabled()) logger.debug("Content: [" + result + "]");
                            session.notify(MrcpEventId.RECOGNITION_COMPLETE_SUCCESS, result);
                            break;

                        case MrcpMessage.NO_INPUT_TIMEOUT:
                            session.transition(IdleState.getSingleton());
                            session.notify(MrcpEventId.RECOGNITION_COMPLETE_NO_INPUT, result);
                            break;

                        case MrcpMessage.NO_MATCH:
                            session.transition(IdleState.getSingleton());
                            session.notify(MrcpEventId.RECOGNITION_COMPLETE_NO_MATCH, result);
                            break;

                        default:
                            session.transition(IdleState.getSingleton());
                            session.notify(MrcpEventId.RECOGNITION_COMPLETE_FAIL, result);
                            break;
                    }
                } else {
                    logger.warn("Unhandled message: [" + mrcpMessage.getHeader() + "]");
                }
                break;
            default:
                logger.error("Unhandled message: [" + mrcpMessage.getHeader() + "]");
                break;
        }
        if (logger.isDebugEnabled()) logger.debug("<-- handleMessage()");
    }

    /**
     * Getter for the state name/ID.
     * @return the state ID.
     */
    public StateName getId() {
        return StateName.RECOGNIZING;
    }
}
