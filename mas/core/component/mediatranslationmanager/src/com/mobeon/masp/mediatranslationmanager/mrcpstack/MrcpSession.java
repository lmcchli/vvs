/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.states.IdleState;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.states.MrcpState;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.states.StateName;

/**
 * This is the base class for an MRCP session.
 * The MrcpSession is associated to an {@link RtspSession} as an
 * {@link RtspMessageReceiver}. The MrcpSession is implemented as a state machine.
 * The MRCP states are based upon {@link MrcpState}.
 * The actual behaviour of the MrcpSession is implmented in the different states.
 * At startup {@link IdleState} is the default/initial state.
*/
public abstract class MrcpSession implements RtspMessageReceiver {
    private static ILogger logger = ILoggerFactory.getILogger(MrcpSession.class);
    // The current state of the session.
    protected MrcpState currentState = null;
    // Flag indicating if the last request turned out ok.
    protected boolean isOk = false;
    // The RTSP session associated with this MRCP session.
    protected RtspSession rtspSession;
    // The MRCP event listener attached to this session.
    protected MrcpEventListener mrcpEventListener = null;
    // The client RTP port.
    private int rtpClientPort = -1;
    // The server RTP port.
    private int rtpServerPort = -1;
    // The MRCP session ID.
    private String sessionId = "";
    // A boolean flag indicating if session is recognize or synthesize.
    private boolean isRecognizeSession;
    // The value of the last received response status code.
    protected int lastStatusCode = -1;
    // The value of the last received response status text.
    protected String lastStatusText = "";

    /**
     * The constructor.
     * Initializes the session.
     * @param rtspSession the RTSP session utilized by this session.
     * @param rtpPort the client RTP port.
     * @param isReconizeSession the recognize/sythesize flag.
     */
    public MrcpSession(RtspSession rtspSession, int rtpPort, boolean isReconizeSession) {
        if (logger.isDebugEnabled()) logger.debug("--> MrcpSession()");
        this.rtspSession = rtspSession;
        this.rtpClientPort = rtpPort;
        this.isRecognizeSession = isReconizeSession;
        currentState = IdleState.getSingleton();
        rtspSession.attach(this);
        if (logger.isDebugEnabled()) logger.debug("<-- MrcpSession()");
    }

    /**
     * Session setup/initialize.
     */
    abstract public void setup();

    /**
     * Attaching an {@link MrcpEventListener} to this MrcpSession.
     * @param mrcpEventListener
     */
    public void attachMrcpEventListener(MrcpEventListener mrcpEventListener) {
        this.mrcpEventListener = mrcpEventListener;
    }

    /**
     * Performing transition from the current state to another {@link MrcpState}.
     * @param to the target state of the transition.
     */
    public void transition(MrcpState to) {
        if (logger.isDebugEnabled()) logger.debug("--> transition()");
        if (logger.isDebugEnabled()) logger.debug("  From: " + currentState.getId() + " To: " + to.getId());
        currentState = to;
        if (logger.isDebugEnabled()) logger.debug("<-- transition()");
    }

    /**
     * The implementation of the {@link RtspMessageReceiver} receive method.
     * @param message the recived message.
     */
    public void receive(RtspMessage message) {
        if (logger.isDebugEnabled()) logger.debug("--> receive()");
        // If the message is a response the result and response is saved.
        // TODO: remove this since it is stupid only events and requests
        //       will be received here and they are handled by the state.
//        if (message.getMrcpMessage() != null
//                && message.getMrcpMessage().getMessageType() == MessageType.MRCP_RESPONSE) {
//            MrcpResponse response = (MrcpResponse)message.getMrcpMessage();
//            lastStatusCode = response.getStatusCode();
//            lastStatusText = response.getRequestState();
//        }
        // The current state will decide in what manner to react upon the
        // message.
        currentState.handleMessage(this, message);
        if (logger.isDebugEnabled()) logger.debug("<-- receive()");
    }

    /**
     * Sends an {@link RtspRequest} and retuns an {@link RtspResponse}.
     * This method blocks until a response i received (or send fails).
     * @param request the request to be sent.
     * @return the response to the sent request.
     */
    public RtspResponse send(RtspRequest request) {
        return rtspSession.send(request);
    }

    /**
     * Getter for the MRCP session ID.
     * @return the MRCP session ID.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Setter for the MRCP session ID.
     * @param sessionId the session ID.
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Getter for the client RTP port.
     * @return the RTP port number.
     */
    public int getRtpClientPort() {
        return rtpClientPort;
    }

    /**
     * Setter for the client RTP port.
     * @param rtpClientPort the RTP port number.
     */
    public void setRtpClientPort(int rtpClientPort) {
        this.rtpClientPort = rtpClientPort;
    }

    /**
     * Getter for the server RTP port.
     * @return the server RTP port number.
     */
    public int getRtpServerPort() {
        return rtpServerPort;
    }

    /**
     * Setter for the server RTP port.
     * @param rtpServerPort
     */
    public void setRtpServerPort(int rtpServerPort) {
        this.rtpServerPort = rtpServerPort;
    }

    /**
     * Getter for the recognize/synthesize session mode flag.
     * @return true for recognize and false for synthesize.
     */
    public boolean isRecognizeSession() {
        return isRecognizeSession;
    }

    /**
     * Getter for the session ok flag.
     * @return true for ok and false otherwise.
     */
    public boolean isOk() {
        return isOk;
    }

    /**
     * Returns true if the current state is the idle state.
     * @return if current state is idle state and false otherwise.
     */
    public boolean isIdle() {
        return currentState.getClass().getSimpleName().equals("IdleState");
    }

    /**
     * Getter for the RTSP session.
     * @return the assocoiated RTSP session.
     */
    public RtspSession getMessageHandler() {
        return rtspSession;
    }

    /**
     * Tear down the current MRCP session.
     * @return true if the session teardown was successful.
     */
    public boolean teardown() {
        if (logger.isDebugEnabled()) logger.debug("--> teardown()");
        // Sending a RTSP TEARDOWN request.
        RtspRequest request = new RtspRequest("TEARDOWN", isRecognizeSession);
        request.setHeaderField("Session", getSessionId());
        RtspResponse response = send(request);
        // TODO: redefine isOk semantics!
        // Determine if teardown was successful or not.
        isOk = response != null && response.getStatusCode() == RtspSession.STATUS_OK;

        if (!isOk) if (logger.isInfoEnabled()) logger.info("TEARDOWN failed");
        // Entering idle state
        currentState = IdleState.getSingleton();
        // Stopping the RTSP session thread.
        getMessageHandler().stop();
        if (logger.isDebugEnabled()) logger.debug("<-- teardown()");
        return isOk;
    }

    /**
     * Issuing the MRCP STOP request.
     * @return true upon success and false otherwise.
     */
    public boolean stop() {
        return currentState.stop(this);
    }

    /**
     * Getter for the name of the current state.
     * @return the name of the current state.
     */
    public StateName getState() {
        return currentState.getId();
    }

    /**
     * Getter for the current state.
     * @return the current state.
     */
    public MrcpState getCurrentState() {
        return currentState;
    }

    /**
     * {@link MrcpEventListener} nofification.
     * @param event the event type ID.
     * @param reason the reason for the event.
     */
    public void notify(MrcpEventId event, String reason) {
        if (mrcpEventListener != null) mrcpEventListener.handleMrcpEvent(event, reason);
    }

    /**
     * Getter for the last RTSP response status code.
     * @return the status code.
     */
    public int getLastRtspStatusCode() {
        return rtspSession.getLastStatusCode();
    }

    /**
     * Getter for the last RTSP status text.
     * @return the status text.
     */
    public String getLastRtspStatusText() {
        return rtspSession.getLastStatusText();
    }

    /**
     * Getter for the last MRCP response status code.
     * @return the status code.
     */
    public int getLastMrcpStatusCode() {
        return lastStatusCode;
    }

    /**
     * Getter for the last MRCP status text.
     * @return the status text.
     */
    public String getLastMrcpStatusText() {
        return lastStatusText;
    }

}
