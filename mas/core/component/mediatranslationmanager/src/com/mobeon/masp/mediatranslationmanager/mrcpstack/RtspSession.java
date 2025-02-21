/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.mrcpstack;

import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;
import com.mobeon.masp.util.executor.ExecutorServiceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for the network layer in the communication with the TTS/ASR engine.
 * RtspSession is the tcp/ip socket based implementation of the RTSP communication.
 * The interface towards the application is implemented in the base class {@link RtspSession}.
 * This class handles creating the socket and transfer of RTSP messages.
 */
public class RtspSession  implements Runnable {
    protected static ILogger logger = ILoggerFactory.getILogger(RtspSession.class);
    private Socket socket = null;            /* Server socket */
    private boolean isActive = true;  /* indicating if session is active or not */
    private RtspConnection rtspConnection = null;
    // The amount of milliseconds we will wait for an MRCP response
    private static final int RECEIVE_TIMEOUT = 10000;
    private ISession session = null;
    // The receiver of incoming RTSP messages/requests.
    protected RtspMessageReceiver messageReceiver = null;
    // The status code of the last received message.
    protected int lastStatusCode = -1;
    // The status text of the last received message.
    protected String lastStatusText = "";
    // The RTSP message input.
    protected InputStream input;
    // A queue to which incoming responses are posted.
    protected SynchronousQueue<RtspResponse> responseQueue =
            new SynchronousQueue<RtspResponse>();
    // The RTSP message output.
    protected OutputStream output;
    // The OK status code
    public static final int STATUS_OK = 200;
    public static final String STATUS_OK_TEXT = "OK";

    public RtspSession(RtspConnection rtspConnection) {
        this.rtspConnection = rtspConnection;
        rtspConnection.open();
        input = rtspConnection.getInputStream();
        output = rtspConnection.getOutputStream();
        ExecutorService service =
                ExecutorServiceManager.getInstance().getExecutorService(RtspSession.class);
        service.execute(this);
    }

    /**
     * Stops/terminates the session.
     */
    public void stop() {
        if (logger.isDebugEnabled()) logger.debug("--> Stop()");
        isActive = false;
        try {
            // Closing the I/O will do the trick.
            if (socket != null) socket.close();
            if (rtspConnection != null) rtspConnection.close();
        } catch (IOException e) {
            logger.warn("Caught IOException during socket close");
        }
        if (logger.isDebugEnabled()) logger.debug("<-- Stop()");
    }

    /**
     * This is the main loop of the session thread.
     */
    public void run() {
        if (session != null) {
            session.registerSessionInLogger();
        } else {
            if (logger.isInfoEnabled()) logger.info("ISession is null. Can not log session information ..");
        }
        if (logger.isDebugEnabled()) logger.debug("--> Entering RTSP session loop");
        try {
            // Perform as long as session is active ...
            while (isActive) {
                if (logger.isDebugEnabled()) logger.debug("Pending on RTSP message ...");
                // The sessaion handler is pending incoming messages
                // (outgoing messages are handled in the base class).
                RtspMessage message = MessageParser.parse(input);
                // Handling of incoming message.
                if (message != null) {
                    if (logger.isDebugEnabled()) logger.debug("Got RTSP message: " + message.getMessageType());
                    // Handling the distinction between response to a previously transmitted
                    // outgoing request and an incoming request.
                    switch (message.getMessageType()) {
                        case RTSP_RESPONSE:
                            // Make the response available to the application.
                            responseQueue.offer((RtspResponse)message);
                            if (logger.isDebugEnabled()) logger.debug("Response: [" + message.getMessage() + "]");
                            break;
                        case RTSP_REQUEST:
                            if (logger.isDebugEnabled()) logger.debug("Request: [" + message.getMessage() + "]");
                            // TODO: verify that this is ok
                            RtspResponse response = new RtspResponse(STATUS_OK, STATUS_OK_TEXT);
                            output.write(response.getMessage().getBytes());
                            // Nofify the application that an incomming request/message has arrived.
                            if (messageReceiver != null) messageReceiver.receive(message);
                            break;
                        default:
                            break;
                    }
                } else {
                    if (isActive) {
                        logger.error("Received null pointer RTSP message, terminating session.");
                        stop();
                    }
                    break;
                }
            }
            if (logger.isDebugEnabled()) logger.debug("<-- Leaving RTSP session loop");
        } catch (Exception exception) {
            logger.error("Caught exception in RTSP Session:", exception);
            stop();
        }
    }

    public void setSession(ISession session) {
        this.session = session;
    }

    /**
     * Server port getter.
     * @return the server port number.
     */
    public int getServerPort() {
        return rtspConnection.getPortNumber();
    }

    /**
     * Hostname getter.
     * @return the RTSP hostname.
     */
    public String getHostname() {
        return rtspConnection.getHostName();
    }

    /**
     * Status code getter.
     * @return the status code of the last received RTSP message.
     */
    public int getLastStatusCode() {
        return lastStatusCode;
    }

    /**
     * Status text getter.
     * @return the status text of the last received RTSP message.
     */
    public String getLastStatusText() {
        return lastStatusText;
    }

    /**
     * Attaching an {@link com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.RtspMessageReceiver} to the RTSP session.
     * @param messageReceiver
     */
    public void attach(RtspMessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    /**
     * Send method for RTSP requests.
     * This method will issue an RTSP request and pend upon an RTSP response.
     * Hence this method is blocking until a response is received (or something goes
     * wrong).
     * @param request an RTSP request.
     * @return the RTSP response.
     */
    public RtspResponse send (RtspRequest request) {
        if (logger.isDebugEnabled()) logger.debug("--> send()");
        RtspResponse response = null;
        try {
            // The service URL is updated with host and port.
            request.setUrl(getHostname(), getServerPort());
            if (logger.isDebugEnabled()) logger.debug("Request: [" + request.getMessage() + "]");
            // The request is sent to the output.
            output.write(request.getMessage().getBytes());
            if (logger.isDebugEnabled()) logger.debug("Pending on response ...");
            response = responseQueue.poll(RECEIVE_TIMEOUT, TimeUnit.MILLISECONDS);
            if (logger.isDebugEnabled()) logger.debug("Got response ...");
        } catch (Exception exception) {
            logger.info("Exception [" + exception.getStackTrace() + "]");
        }
        // Handling (saving) the status of the message.
        if (response != null) {
            lastStatusCode = response.getStatusCode();
            lastStatusText = response.getStatusText();
            if (logger.isDebugEnabled()) logger.debug("Response: " + response);
        } else {
            logger.error("Received a null pointer RTSP response.");
        }
        if (logger.isDebugEnabled()) logger.debug("<-- send()");
        return response;
    }
}
