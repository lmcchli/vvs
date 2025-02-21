/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationChanged;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.stream.jni.AbstractCallbackReceiver;
import com.mobeon.masp.stream.jni.CallbackDispatcher;
import com.mobeon.masp.stream.jni.Callback;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Support class for media stream classes. This class is responsible for
 * holding a reference to the stack implementation.
 */
public abstract class MediaStreamSupport implements IMediaStream, AbstractCallbackReceiver {
    public static final int UNDEFINED_PORTNUMBER = -1;

    private static final ILogger LOGGER = ILoggerFactory.getILogger(MediaStreamSupport.class);

    /** Local portnumber for RTP traffic over the audio stream. */
    private AtomicInteger mAudioPort = new AtomicInteger(UNDEFINED_PORTNUMBER);

    /**
     * Local portnumber for RTP-traffic over the video stream.
     * If only audio is sent/received over a stream, this port is always
     * undefined.
     */
    private AtomicInteger mVideoPort = new AtomicInteger(UNDEFINED_PORTNUMBER);


    /** The stack implementation. */
    private RTPSession mRTPSession;

    /**
     * Information about the media that shall be sent/received
     * on this stream.
     */
    private AtomicReference<StreamContentInfo> mContentInfo =
        new AtomicReference<StreamContentInfo>();

    /**
     * Callback-object. Used to implement sychronous calls and to
     * handle stack events.
     */
    private AtomicReference<StackEventNotifier> mEventNotifier =
        new AtomicReference<StackEventNotifier>();

    /** Used to send events to other components. */
    private AtomicReference<IEventDispatcher> mEventDispatcher =
        new AtomicReference<IEventDispatcher>();

    /**
     * Used as identifier in the RTP packets sent by this stream.
     * See RFC 1550 for more information about CNAME.
     */
    private AtomicReference<String> mCNAME = new AtomicReference<String>();

    /**
     * This is the call session information.
     * The session information should be propagated to all the threads
     * involved in the streaming.
     */
    private ISession callSession;

    /* package */ MediaStreamSupport(RTPSession session) {
        super();
        mRTPSession = session;
    }

    /**
     * Initiates the stream component.
     */
    static void init() {
        // TODO: replace with generic init method ...
        CCRTPSession.initConfiguration(StreamConfiguration.getInstance());
    }

    /**
     * Propagates the new configuration to the stack implementation.
     */
    static void updateConfiguration() {
        // TODO: replace with generic init method ...
        CCRTPSession.updateConfiguration(StreamConfiguration.getInstance());
    }

    protected final RTPSession getSession() {
        return mRTPSession;
    }

    /* Javadoc in interface */
    public void setSkew(SkewMethod method, long skew) {
        try {
            mRTPSession.setSkew(method, skew);
        }
        catch (Throwable t) {
            LOGGER.debug("setSkew: Unexpected exception.", t);
            // No action is taken if this request fails.
        }
    }

    /**
     * Creates and connects a stream to its endpoint.
     *
     * @param contentInfo           Describes the media of the stream.
     * @param connectionProperties  Properties necessary to establish a
     *                              connection to the endpoint.
     *
     * @throws IllegalArgumentException If <code>contentInfo</code> is
     *                                  <code>null</code>.
     * @throws IllegalStateException    If this method has already been called
     *                                  for this stream instance.
     * @throws CreateSessionException   If the local session could not be
     *                                  created.
     * @throws StackException           If some other error occured.
     */
    public void createOld(StreamContentInfo contentInfo,
                       ConnectionProperties connectionProperties,
                       IInboundMediaStream inboundStream) throws StackException
    {
        if (contentInfo == null) {
            throw new IllegalArgumentException("contentInfo may not be null");
        }
        contentInfo.setCNAME(mCNAME.get());

        if (mEventDispatcher.get() == null) {
            throw new IllegalStateException("No event dispatcher " +
                    "specified. Call the method setEventDispatcher before " +
                    "create.");
        }

//        if (callSession == null) {
//            throw new IllegalStateException("No call session " +
//                    "specified. Call the method setCallSession before " +
//                    "create.");
//        }
//
        StackEventNotifier notifier =
                StackEventHandler.getEventNotifier(mEventDispatcher.get());
        int audioPort = allocatePortPair();
        int videoPort = UNDEFINED_PORTNUMBER;
        if (contentInfo.isVideo()) {
            videoPort = allocatePortPair();
        }
        try {
            // rtpStack.create will handle the cases when create
            // it already called or delete has been called.
            mRTPSession.create(contentInfo,
                    connectionProperties, notifier,
                    audioPort, videoPort, inboundStream);
            mContentInfo.set(contentInfo);
            mEventNotifier.set(notifier);
            mAudioPort.set(audioPort);
            mVideoPort.set(videoPort);
        }
        catch (CreateSessionException e) {
            // This is probably because the allocated ports were
            // occupied, try to allocate a new pair and try once more.
            // To avoid the risk of an eternal loop, a loop is not used here.
            LOGGER.info("create: failed to create local sessions with " +
                    "portnumbers audio:" + audioPort + ", video: " +
                    videoPort + ". Will make another try.");

            releasePorts();
            audioPort = allocatePortPair();
            videoPort = UNDEFINED_PORTNUMBER;
            if (contentInfo.isVideo()) {
                videoPort = allocatePortPair();
            }
            try {
                mRTPSession.create(contentInfo,
                        connectionProperties, notifier,
                        audioPort, videoPort, inboundStream);
                mContentInfo.set(contentInfo);
                mEventNotifier.set(notifier);
                mAudioPort.set(audioPort);
                mVideoPort.set(videoPort);
            }
            catch (StackException e1) {
                releasePorts();
                // Failed a second time, log message and rethrow
                LOGGER.warn("create: failed to create local sessions with " +
                        "portnumbers audio:" + audioPort + ", video: " +
                        videoPort + " Giving up.");
                throw e1;
            }
        }
        catch (StackException e) {
            releasePorts();
            LOGGER.warn("create: Unexpected exception while creating " +
                    "local sessions with portnumbers audio:" +
                    audioPort + ", video: " + videoPort, e);
            throw e;
        }
    }

    /**
     * Creates and connects a stream to its endpoint.
     *
     * @param contentInfo           Describes the media of the stream.
     * @param connectionProperties  Properties necessary to establish a
     *                              connection to the endpoint.
     *
     * @throws IllegalArgumentException If <code>contentInfo</code> is
     *                                  <code>null</code>.
     * @throws IllegalStateException    If this method has already been called
     *                                  for this stream instance.
     * @throws CreateSessionException   If the local session could not be
     *                                  created.
     * @throws StackException           If some other error occured.
     */
    public void create(StreamContentInfo contentInfo, ConnectionProperties connectionProperties,
                       IInboundMediaStream inboundStream) throws StackException
    {
        if (contentInfo == null) {
            throw new IllegalArgumentException("contentInfo may not be null");
        }
        contentInfo.setCNAME(mCNAME.get());

        if (mEventDispatcher.get() == null) {
            throw new IllegalStateException("No event dispatcher specified. Call the method setEventDispatcher before create.");
        }

        StackEventNotifier notifier = StackEventHandler.getEventNotifier(mEventDispatcher.get());

        // Allocate ports and create the RTP session
        try {
        	allocatePorts(contentInfo.isVideo(), inboundStream);

            // rtpStack.create will handle the cases when create
            // it already called or delete has been called.
        	createRTPSession(contentInfo,
                    connectionProperties, notifier,
                    mAudioPort.get(), mVideoPort.get(), inboundStream);
        }
        catch (OutOfPortsException outOfPorts) {
        	// When failing to allocate the required ports due to
        	// that there were no ports available we make a second attempt
        	// hoping that an other call has released its ports.

            LOGGER.info("create: failed to create local sessions with " +
                    "portnumbers audio:" + mAudioPort.get() + ", video: " +
                    mVideoPort.get() + ". Will make another try.");

            // In case we got some ports we must ensure that they are released.
            releasePorts();
            try {
            	// This is the second attempt
            	allocatePorts(contentInfo.isVideo(), inboundStream);
           	    createRTPSession(contentInfo,
                        connectionProperties, notifier,
                        mAudioPort.get(), mVideoPort.get(), inboundStream);
            }
            catch (StackException stackException) {
            	// The second attempt failed, due to out of ports, or something else
                releasePorts();
                // Failed a second time, log message and re-throw

                LOGGER.warn("create: failed to create local sessions with " +
                        "portnumbers audio:" + mAudioPort.get() + ", video: " +
                        mVideoPort.get() + " Giving up.", stackException);
                throw stackException;
            }
        }
        catch (StackException e) {
        	// For some reason we failed to create the RTP session
        	// Release ports and re-throw the exception
            releasePorts();
            LOGGER.warn("create: Unexpected exception while creating " +
                    "local sessions with portnumbers audio:" +
                    mAudioPort.get() + ", video: " + mVideoPort.get(), e);
            throw e;
        }

    }



    /**
     * This is a convenience method for creating an outbound stream
     * @param contentInfo
     * @param connectionProperties
     * @param notifier
     * @param localAudioPort
     * @param localVideoPort
     * @param inboundStream
     * @throws StackException
     * @throws OutOfPortsException
     */
    private void createRTPSession(StreamContentInfo contentInfo,
            					  ConnectionProperties connectionProperties, StackEventNotifier notifier,
            					  int localAudioPort, int localVideoPort,
                                  IInboundMediaStream inboundStream) throws StackException, OutOfPortsException {
    	LOGGER.debug("MediaStreamSupport.createRTPSession with local audio port: " + localAudioPort);
        mRTPSession.create(contentInfo,
                connectionProperties, notifier,
                localAudioPort, localVideoPort, inboundStream);
        mContentInfo.set(contentInfo);
        mEventNotifier.set(notifier);
    }

    /**
     * This is a convenience method that either allocates free ports or retrieves port numbers from
     * the inbound stream (if present).
     * @param isVideoCall
     * @param inboundStream
     * @throws OutOfPortsException
     */
    private void allocatePorts(boolean isVideoCall, IInboundMediaStream inboundStream) throws OutOfPortsException {
    	int localAudioPort = UNDEFINED_PORTNUMBER;
       	int localVideoPort = UNDEFINED_PORTNUMBER;
        // TODO: ensure that this assumtion holds for all created outbound streams
       	if (inboundStream == null) {
       		// If this stream is not paired with an InboundStream the port numbers
       		// is allocated from the free port pool
       		localAudioPort = allocatePortPair();
       		LOGGER.debug("MediaStreamSupport.allocatePorts: inbound stream is null, allocate new ports: " + localAudioPort);
       		if (isVideoCall) {
       			localVideoPort = allocatePortPair();
       		}
       	} else {
            // For all calls all inbound and outbound streams are paired (in call manager)
            // Due to a requirement where MAS shall support RFC 4961 the outbound stream
            // must use the inbound stream ports as local port number.
      		localAudioPort = inboundStream.getAudioPort();
      		LOGGER.debug("MediaStreamSupport.allocatePorts: inbound stream is not null, re-using audioPort: " + localAudioPort);
       		if (isVideoCall) {
       			localVideoPort = inboundStream.getVideoPort();
       		}
       	}
        mAudioPort.set(localAudioPort);
        mVideoPort.set(localVideoPort);

    }



    /**
     * Allocates ports for a session.
     *
     * @return The first of two allocated portnumbers for a session.
     *
     * @throws CreateSessionException If no local ports were available.
     */
    private int allocatePortPair() throws OutOfPortsException {
    	int port = FreePortHandler.getInstance().lockPair();
        if (port == -1) {
            final String msg = "allocatePorts: No local ports were available";
            LOGGER.warn(msg);
            throw new OutOfPortsException(msg);
        }
        return port;
    }

    /* Javadoc in interface */
    public void delete() {
        // Lock removed since there is only one user of this object
        if (this instanceof OutboundMediaStreamImpl) {
            int requestId = CallbackDispatcher.getSingleton().addRequest(null, this);
            if (LOGGER.isDebugEnabled()) LOGGER.debug("Deleting Outbound: " +
                    mAudioPort.get() + ":" + mVideoPort.get() + ":" + requestId);
            // NB! The allocated ports, if any, are released by the C++ code callback.
            // All in order to ensure that we are not releasing ports (in Java)
            // which still are in use by the C++ code (ccRTP).
            mRTPSession.delete(requestId);
        } else {
            if (LOGGER.isDebugEnabled()) LOGGER.debug("Deleting Inbound: "+
                    mAudioPort.get() + ":" + mVideoPort.get());
            // NB! The allocated ports, if any, are released by the C++ code.
            // All in order to ensure that we are not releasing ports (in Java)
            // which still are in use by the C++ code (ccRTP).
            mRTPSession.delete(-1);
        }

        // Esuring that the reference cycle is broken.
        mRTPSession = null;
        CallSessionMapper.getInstance().popSession(this);
    }

    /* Javadoc in interface */
    public long stop(Object callId) throws StackException {
        CallSessionMapper.getInstance().putSession(callId, this, callSession);
        return mRTPSession.stop(callId);
   }

    /* Javadoc in interface */
    public int getAudioPort() {
        return mAudioPort.get();
    }

    /* Javadoc in interface */
    public int getAudioControlPort() {
        int audioPort = mAudioPort.get();
        return audioPort == UNDEFINED_PORTNUMBER ? UNDEFINED_PORTNUMBER : audioPort + 1;
    }

    /* Javadoc in interface */
    public int getVideoPort() {
        return mVideoPort.get();
    }

    /* Javadoc in interface */
    public int getVideoControlPort() {
        int videoPort = mVideoPort.get();
        return videoPort == UNDEFINED_PORTNUMBER ? UNDEFINED_PORTNUMBER : videoPort + 1;
    }

    /* Javadoc in interface */
    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        if (eventDispatcher == null) {
            throw new IllegalArgumentException(
                    "The event dispatcher may not be null!");
        }
        mEventDispatcher.set(eventDispatcher);
    }

    /* Javadoc in interface */
    public IEventDispatcher getEventDispatcher() {
        return mEventDispatcher.get();
    }

    /**
     * Gets the event notifier that should be used in this stream instance.
     *
     * @return The event notifier. Will be <code>null</code> if
     *         <code>create</code> has not been called.
     */
    // Package-declared so that test-classes may get access to the event
    // notifier
    /* package */ final StackEventNotifier getEventNotifier() {
        return mEventNotifier.get();
    }

    /**
     * Gets the content information (RTP payload etc) for media on this stream.
     *
     * @return Content information. Will be <code>null</code> if
     *         <code>create</code> has not been called.
     */
    /* package */ final StreamContentInfo getContentInfo() {
        return mContentInfo.get();
    }

    /* Javadoc in interface */
    public void setCNAME(String name) {
        mCNAME.set(name);
    }

    /* Javadoc in interface */
    public void setCallSession(ISession callSession) {
        this.callSession = callSession;
    }

    /**
     * Getter for the call session information.
     * @return call session information.
     */
    public ISession getCallSession() {
        return callSession;
    }

    /**
     * Getter for the call session ID.
     * Returns the call session ID, if the call session is not set the
     * string "undefined" is returned as session ID.
     * @return the call session ID
     */
    public String getCallSessionId() {
        if (callSession == null) {
            return "";
        }
        return callSession.getId();
    }

    /**
     * @return The event receiver that will handle
     *         "configuration has changed"-events.
     */
    /* package */ static IEventReceiver getEventReceiver() {
        return new ConfigurationEventReceiver();
    }

    /**
     * This class receives the "configuration has changed"-event.
     */
    public static class ConfigurationEventReceiver implements IEventReceiver {

        public void doEvent(Event event) {
            if (event instanceof ConfigurationChanged ) {
                try {
                    StreamConfiguration.getInstance().update();
                    MediaStreamSupport.updateConfiguration();
                }
                catch (Exception e) {
                    LOGGER.warn("Failed to update configuration.", e);
                }
            }
            // else ignore
        }

        public void doGlobalEvent(Event event) {
            if (event instanceof ConfigurationChanged ) {
                try {
                    StreamConfiguration.getInstance().update();
                    MediaStreamSupport.updateConfiguration();
                }
                catch (Exception e) {
                    LOGGER.warn("Failed to update configuration.", e);
                }
            }
            // else ignore
        }
    }

    public void releasePorts() {
        if (LOGGER.isDebugEnabled()) {
            if (this instanceof OutboundMediaStreamImpl) {
                LOGGER.debug("Releasing Outbound: " +
                        mAudioPort.get() + ":" + mVideoPort.get());
            } else {
                LOGGER.debug("Releasing Inbound: " +
                        mAudioPort.get() + ":" + mVideoPort.get());
            }
        }
        releasePort(mAudioPort);
        releasePort(mVideoPort);
    }

    private static void releasePort(AtomicInteger portAtomic) {
        int port = portAtomic.get();
        if(port != UNDEFINED_PORTNUMBER) {
            if(portAtomic.compareAndSet(port,UNDEFINED_PORTNUMBER)) {
                FreePortHandler.getInstance().releasePair(port);
            }
        }
    }

    public void finalize() throws Throwable {
        if (mAudioPort.get() != UNDEFINED_PORTNUMBER || mVideoPort.get() != UNDEFINED_PORTNUMBER) {
            LOGGER.warn("Garbing a stream for which the ports were not released: " +
            		"audio port=" + mAudioPort.get() + ", video port=" + mVideoPort.get());
            releasePorts();
        }
        super.finalize();
    }



    public void notify(Object requestId, Callback callback) {
        switch (callback.command) {
            case Callback.DELETE_COMMAND:
                if (LOGGER.isDebugEnabled()) LOGGER.debug("notify: DELETE");
                switch (callback.status) {
                    case Callback.OK:
                        releasePorts();
                        break;

                    default:
                        LOGGER.error("Unhandled callback event status!");
                        break;
                }
                break;

            default:
                LOGGER.error("Unhandled callback event!");
                break;
        }
    }

    /** Javadoc in interface */
    public void sendPictureFastUpdate(int ssrc) {
        mRTPSession.sendPictureFastUpdate(ssrc);
    }

    /** Javadoc in interface */
    public int getSenderSSRC() {
        return mRTPSession.getSenderSSRC();
    }
}

