package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.stream.*;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventDispatcher;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An abstract mock object for the media streams.
 */
abstract class MediaStreamSupportMock extends BaseMock implements IMediaStream {
    public static final int UNDEFINED_PORTNUMBER = -1;

    /** Local portnumber for RTP traffic over the audio stream. */
    private volatile int mAudioPort = UNDEFINED_PORTNUMBER;

    /**
     * Local portnumber for RTP-traffic over the video stream.
     * If only audio is sent/received over a stream, this port is always
     * undefined.
     */
    private volatile int mVideoPort = UNDEFINED_PORTNUMBER;

    /** The stack implementation. */
    /* private IRTPStack mRtpStack; */

    /**
     * Information about the media that shall be received
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

    /** Protects state-members. */
    private final ReentrantReadWriteLock STATE_LOCK =
        new ReentrantReadWriteLock();

    MediaStreamSupportMock() {
        super();
        // This is a mock object, no stack needed !
        // mRtpStack = new CCRTPStack(this);
    }

    /**
     * Returns with the eventdispatcher
     *
     * @return A dispatcher.
     */
    public IEventDispatcher getEventDispatcher ()
    {
        return mEventDispatcher.get();
    }

    /**
     * Initiates the stream component.
     */
    static void init() {
        // Not needed, this is a mock
        // CCRTPStack.initConfiguration(StreamConfiguration.getInstance());
    }

    /**
     * Propagates the new configuration to the stack implementation.
     */
    static void updateConfiguration() {
        // Not needed, this is a mock
        // CCRTPStack.updateConfiguration(StreamConfiguration.getInstance());
    }

    /*
    protected final IRTPStack getStack() {
        return mRtpStack;
    }
    */

    /* Javadoc in interface */
    public void setSkew(SkewMethod method, long skew) {
        // TODO: Add somethig here
        log.info ("MOCK: MediaStreamSupportMock.setSkew is unimplemented");
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
    public void create(StreamContentInfo contentInfo,
                       ConnectionProperties connectionProperties) throws StackException
    {
        if (contentInfo == null) {
            throw new IllegalArgumentException("contentInfo may not be null");
        }
        STATE_LOCK.writeLock().lock();
        try {
            if (mEventDispatcher.get() == null) {
                throw new IllegalStateException("No event dispatcher " +
                    "specified. Call the method setEventDispatcher before " +
                    "create.");
            }

            /* StackEventNotifier notifier =
                StackEventHandler.getEventNotifier(mEventDispatcher.get()); */
            int audioPort = allocatePortPair();
            int videoPort = UNDEFINED_PORTNUMBER;// XXX allocatePortPair();
            mContentInfo.set(contentInfo);
            /* mEventNotifier.set(notifier); */
            mAudioPort = audioPort;
            mVideoPort = videoPort;
        }
        finally {
            STATE_LOCK.writeLock().unlock();
        }
    }

    /**
     * Allocates ports for a session.
     *
     * @return The first of two allocated portnumbers for a session.
     *
     * @throws CreateSessionException If no local ports were available.
     */
    private int allocatePortPair() throws CreateSessionException {
        int port = FreePortHandler.getInstance().lockPair();
        if (port == -1) {
            final String msg = "allocatePorts: No local ports were available";
            log.debug(msg);
            throw new CreateSessionException(msg);
        }
        return port;
    }

    /**
     * Frees the allocated ports.
     */
    private void deallocatePortPair(int audio, int video) {
        if (audio != UNDEFINED_PORTNUMBER) {
            FreePortHandler.getInstance().releasePair(audio);
        }
        if (video != UNDEFINED_PORTNUMBER) {
            FreePortHandler.getInstance().releasePair(video);
        }
    }

    /* Javadoc in interface */
    public void delete() {
        STATE_LOCK.writeLock().lock();
        try {
            /* mRtpStack.delete(); */
            deallocatePortPair(mAudioPort, mVideoPort);
            mAudioPort = UNDEFINED_PORTNUMBER;
            mVideoPort = UNDEFINED_PORTNUMBER;
        } finally {
            STATE_LOCK.writeLock().unlock();
        }
    }

    /* Javadoc in interface */
    public int getAudioPort() {
        return mAudioPort;
    }

    /* Javadoc in interface */
    public int getAudioControlPort() {
        return (mAudioPort != UNDEFINED_PORTNUMBER) ?
            mAudioPort + 1 : UNDEFINED_PORTNUMBER;
    }

    /* Javadoc in interface */
    public int getVideoPort() {
        return mVideoPort;
    }

    /* Javadoc in interface */
    public int getVideoControlPort() {
        return (mVideoPort != UNDEFINED_PORTNUMBER) ?
            mVideoPort + 1 : UNDEFINED_PORTNUMBER;
    }

    /* Javadoc in interface */
    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        if (eventDispatcher == null) {
            throw new IllegalArgumentException(
                    "The event dispatcher may not be null!");
        }
        mEventDispatcher.set(eventDispatcher);
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
    // Package-declared so that test-classes may get access to the payload type
    /* package */
    //It wasn't declared so in the interface thou..
    public final StreamContentInfo getContentInfo() {
        return mContentInfo.get();
    }
}