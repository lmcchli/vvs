/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;


import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.IMediaStream.SkewMethod;
import com.mobeon.masp.stream.IOutboundMediaStream.PlayOption;

/**
 * Implementation of the RTPSession interface that uses ccRTP.
 */

// Please note that the locks here in (especially the locks concerning play)
// are found to cause a deadlock when stream is deleted during a multiple
// media object play. The locks involved in play are removed. But some further
// redesing should applied to the whole shit.
    
final class CCRTPSession implements RTPSession {

    private static enum SessionType {
        OUTBOUND, INBOUND
    }

    /**
     * Handle to the native instance. Note that no synchronization is
     * done and thus, no sync with main memory. Therefore this member
     * is declared as volatile.
     */
    private volatile long nativeHandle = -1;

    /**
     * <code>true</code> if method <code>create</code> has been called.
     * Note that no synchronization is done and thus, no sync with main
     * memory. Therefore this member is declared as volatile.
     */
    private volatile boolean isCreated;

    /** Defines it this is an inbound or an outbound session. */
    private SessionType mSessionType;

   /**  native libraries to load **/
    private static final String CCRTP_LIBRARY_NAME = "ccrtp";
    private static final String ADAPTER_LIBRARY_NAME = "ccrtpadapter";

    static {
        String libname = ADAPTER_LIBRARY_NAME;

        try {
                System.loadLibrary(libname);
        }
        catch (Exception e) {
            System.out.println("Exception caught when loading library " + libname + e);
        }
    }

    /**
     * Gets a session for an inbound media stream.
     * 
     * @return An inbound session.
     */
    public static RTPSession getInbound() {
        return new CCRTPSession(SessionType.INBOUND);
    }
    
    /**
     * Gets a session for an outbound media stream.
     * 
     * @return An outbound session.
     */
    public static RTPSession getOutbound() {
        return new CCRTPSession(SessionType.OUTBOUND);
    }
    
    /**
     * Creates the session.
     *
     * @param type Defines if this is an inbound or an outbound session.
     */
    private CCRTPSession(SessionType type) {
        mSessionType = type;
    }

    /**
     * Creates the native main class instance. Note that this method 
     * <strong>MUST</strong> be called before the session is used.
     * 
     * @param stream The Java stream instance that owns the stack instance.
     *               This reference is used as information in some stack
     *               events.
     */
    public void init(IMediaStream stream) {
        if (mSessionType == SessionType.INBOUND) {
            nativeHandle = createInboundSession(stream);
        }
        else { // OUTBOUND
            nativeHandle = createOutboundSession(stream);
        }
    }

    /**
     * Creates the native main class instance for an inbound session.
     *
     * @param stream The Java stream instance that owns the stack instance.
     *               This reference is used as information in some stack
     *               events.
     *
     * @return Reference to the native main class instance.
     */
    private native long createInboundSession(IMediaStream stream);

    /**
     * Creates the native main class instance for an outbound session.
     *
     * @param stream The Java stream instance that owns the stack instance.
     *               This reference is used as information in some stack
     *               events.
     *
     * @return Reference to the native main class instance.
     */
    private native long createOutboundSession(IMediaStream stream);

    /**
     * Sets the stacks initial configuration. Must be called once before any
     * stack instance is used.
     *
     * @param configuration The initial configuration.
     */
    /* package */
    static native void initConfiguration(StreamConfiguration configuration);

    /**
     * Updates the stacks configuration.
     *
     * @param configuration The new configuration.
     */
    /* package */
    static native void updateConfiguration(StreamConfiguration configuration);

    /* Javadoc in interface. */
    public void play(int requestId, IMediaObject mediaObject, PlayOption playOption,
                     long cursor) throws StackException {
        if (mediaObject == null) {
            throw new IllegalArgumentException("MediaObject may not be null.");
        }
        if (playOption == null) {
            throw new IllegalArgumentException("PlayOption may not be null.");
        }
        if (cursor < 0) {
            throw new IllegalArgumentException("Cursor must be >= 0.");
        }
        if (!mediaObject.isImmutable()) {
            throw new IllegalArgumentException(
                    "Cannot play a mutable MediaObject");
        }

        // Check session
        if (!isCreated) {
            throw new IllegalStateException("Method create has not been called");
        }

        if (nativeHandle == -1) {
            throw new IllegalStateException("This stream has been deleted.");
        }

        try {
            play(requestId, mediaObject, playOption.decimalDef(),
                    cursor, nativeHandle);
        }
        catch (Throwable t) {
            handleThrowable(t, "play: ");
        }
    }

    /**
     * Native method implementation for play.
     *
     * @param requestId      Identifies this call. Used when sending events
     *                    originating from this call.
     * @param mediaObject Media source.
     * @param playOption  Tells how the playing shall be done.
     * @param cursor      Start location in milliseconds in the media object
     *                    for playing.
     * @param handle      Handle to native instance.
     * @throws UnsupportedOperationException If a new play is issued when the
     *                                       stream is already playing.
     * @throws StackException                If some other error occured.
     */
    /*
    private native void play(Object callId, IMediaObject mediaObject, int playOption,
                             long cursor, long handle) throws StackException;
     */
    private native void play(int requestId, IMediaObject mediaObject, int playOption,
                             long cursor, long handle) throws StackException;

    /* Javadoc in interface. */
    public void record(Object callId,
                       IMediaObject playMediaObject,
                       IOutboundMediaStream outboundStream,
                       IMediaObject recordMediaObject,
                       RecordingProperties properties) throws StackException {

        if (callId == null) {
            throw new IllegalArgumentException("CallId may not be null.");
        }
        if (recordMediaObject == null) {
            throw new IllegalArgumentException("MediaObject may not be null.");
        }
        if (properties == null) {
            throw new IllegalArgumentException(
                    "RecordingProperties may not be null.");
        }

        if (recordMediaObject.isImmutable()) {
            throw new IllegalArgumentException(
                    "Cannot record to an immutable MediaObject.");
        }
        if (playMediaObject != null) {
            if (!playMediaObject.isImmutable()) {
                throw new IllegalArgumentException(
                        "Cannot play a mutable media object.");
            }
            if (outboundStream == null) {
                throw new IllegalArgumentException(
                        "If a play mediaobject is passed as parameter, " +
                                "a non-null outbound stream must be given as well.");
            }
        }

            if (!isCreated) {
                throw new IllegalStateException(
                        "record: Method create has not been called");
            }

            if (nativeHandle == -1) {
                throw new IllegalStateException(
                        "record: This stream has been deleted.");
            }

            try {
                long outboundStreamHandle = -1;
                if (outboundStream != null) {
                    RTPSession outboundSession =
                            ((MediaStreamSupport) outboundStream).getSession();
                    outboundStreamHandle =
                            ((CCRTPSession) outboundSession).nativeHandle;
                }
                record(callId, playMediaObject, outboundStreamHandle,
                        recordMediaObject, properties, nativeHandle);
            }
            catch (Throwable t) {
                handleThrowable(t, "record: ");
            }
    }

    /**
     * Native method implementation for record.
     *
     * @param callId               Identifies this call. Used when sending
     *                             events originating from this method call.
     * @param playMediaObject      If not <code>null</code>, this mediaobject
     *                             will be played on the given outbound stream
     *                             until the recording starts.
     * @param outboundStreamHandle Handle to native outbound stream instance.
     * @param recordMediaObject    Media destination.
     * @param properties           Tells how the recording shall be done.
     * @param handle               Handle to native instance.
     * @throws UnsupportedOperationException If a new record is issued when the
     *                                       stream is already recording.
     * @throws StackException                If some other error occured.
     */
    private native void record(Object callId,
                               IMediaObject playMediaObject,
                               long outboundStreamHandle,
                               IMediaObject recordMediaObject,
                               RecordingProperties properties, long handle) throws StackException;


    /**
     * ReNegotiatedSDP.  These(this) are/is the new type as provided
     * the remote party at outdial in the SDP of 200 OK.
     *
     * @param dtmfPayLoad      DTMF payload type.
     *                                             
     * @throws StackException                If some other error occured.
     */
                       
    public void reNegotiatedSdp(RTPPayload dtmfPayLoad) throws StackException {
    
            if (nativeHandle == -1) {
                throw new IllegalStateException(
                        "record: This stream has been deleted.");
            }

            try {
                reNegotiatedSdp(dtmfPayLoad, nativeHandle);
            }
            catch (Throwable t) {
                handleThrowable(t, "reNegotiatedSdp: ");
            }
    }
    
    private native void reNegotiatedSdp(RTPPayload dtmfPayLoad, long handle) throws StackException;
    


    /* Javadoc in interface. */
    public void create(StreamContentInfo contentInfo,
                       ConnectionProperties connectionProperties,
                       StackEventNotifier eventNotifier,
                       int localAudioPort, int localVideoPort,
                       IInboundMediaStream inboundStream) throws StackException {

        // A little performance might be gained if the read-lock where used
        // first and the write-lock after the initial tests, but 
        // performance would only be gained when the tests fails, which
        // should not be the main concern.
        try {
            if (nativeHandle == -1) {
                throw new IllegalStateException(
                        "This stream has been deleted.");
            }

            if (isCreated) {
                throw new IllegalStateException(
                        "Method create already called. May only be called once.");
            }

            if (connectionProperties == null) {
                create(contentInfo, eventNotifier,
                        localAudioPort, localVideoPort, nativeHandle);
            } else {

                    long inboundStreamHandle = -1;
                    if (inboundStream != null) {
                        RTPSession inboundSession =
                             ((MediaStreamSupport) inboundStream).getSession();
                        inboundStreamHandle =
                            ((CCRTPSession) inboundSession).nativeHandle;
                    }

                    create(contentInfo, eventNotifier,
                        localAudioPort, localVideoPort,
                        connectionProperties.getAudioHost(),
                        connectionProperties.getAudioPort(),
                        connectionProperties.getVideoHost(),
                        connectionProperties.getVideoPort(),
                        connectionProperties.getMaximumTransmissionUnit(),
                        nativeHandle, inboundStreamHandle);
            }

            isCreated = true;
        }
        catch (Throwable t) {
            handleThrowable(t, "create: ");
        }
    }

    /**
     * Native method implementation.
     *
     * @param contentInfo     Information about payload for the media of
     *                        the stream. For inbound streams this also
     *                        includes information needed when saving
     *                        media to a MediaObject.
     * @param eventNotifier   Used to send events back to Java-space.
     * @param localAudioPort  Local port for RTP-traffic over an audio
     *                        session. The portnumber
     *                        <code>localAudioPort+1</code> will be used for
     *                        RTCP-traffic.
     * @param localVideoPort  Local port for RTP-traffic over a video
     *                        session. The portnumber
     *                        <code>localVideoPort+1</code> will be used for
     *                        RTCP-traffic.
     * @param audioHost       Host for the audio session.
     * @param remoteAudioPort Remote port for RTP-traffic over an audio
     *                        session. The portnumber
     *                        <code>remoteAudioPort+1</code> will be used for
     *                        RTCP-traffic.
     * @param videoHost       Host for the video session.
     * @param remoteVideoPort Remote port for RTP-traffic over a video
     *                        session. The portnumber
     *                        <code>remoteVideoPort+1</code> will be used for
     *                        RTCP-traffic.
     * @param mtu             Maximum transmission unit for this stream.
     * @param handle          Handle to native instance.
     */
    private native void create(StreamContentInfo contentInfo,
                               StackEventNotifier eventNotifier,
                               int localAudioPort, int localVideoPort,
                               String audioHost, int remoteAudioPort,
                               String videoHost, int remoteVideoPort,
                               int mtu, long handle, long inboundStream) throws StackException;

    /**
     * Native method implementation of create.
     *
     * @param contentInfo    Information about payload for the media of
     *                       the stream. For inbound streams this also
     *                       includes information needed when saving
     *                       media to a MediaObject.
     * @param eventNotifier  Used to send events back to Java-space.
     * @param localAudioPort Local port for RTP-traffic over an audio
     *                       session. The portnumber
     *                       <code>localAudioPort+1</code> will be used for
     *                       RTCP-traffic.
     * @param localVideoPort Local port for RTP-traffic over a video
     *                       session. The portnumber
     *                       <code>localVideoPort+1</code> will be used for
     *                       RTCP-traffic.
     * @param handle         Handle to native instance.
     */
    private native void create(StreamContentInfo contentInfo,
                               StackEventNotifier eventNotifier,
                               int localAudioPort, int localVideoPort,
                               long handle) throws StackException;

    /* Javadoc in interface. */
    public void delete(int requestId) {
        // Locks the read lock first. This implementation supports
        // multiple calls to delete so a little bit of performance
        // is gained for each extra call to delete.
        try {
            // Test again to cover the possibility that some thread
            // sneeked between the readLock().unlock and the writeLock().lock.
            if (nativeHandle != -1) {
                delete(nativeHandle, requestId);
                nativeHandle = -1;

                // To get better error messages if calls are made on a deleted
                // stream, isCreated is not modified here.
                //isCreated = false;
            }
        } catch (Throwable t) {
            //LOGGER.warn("Unexpected exception from delete", t);
        }
    }

    /**
     * Native method implementation of delete.
     *
     * @param nativeHandle Handle to native instance.
     */
    private native void delete(long nativeHandle, int requestId);

    /* Javadoc in interface. */
    public long stop(Object callId) throws StackException {
        if (callId == null) {
            throw new IllegalArgumentException("CallId may not be null.");
        }

        try {
            if (!isCreated) {
                throw new IllegalStateException(
                        "stop: Method create has not been called");
            }

            if (nativeHandle == -1) {
                throw new IllegalStateException(
                        "stop: This stream has been deleted.");
            }

            return stop(callId, nativeHandle);
        }
        catch (Throwable t) {
            handleThrowable(t, "stop: ");

            // To make the compiler happy, this line is never reached.
            return 0;
        }
    }

    /**
     * Native method implementation for stop.
     *
     * @param callId Identifies the call that initiated the operation that
     *               shall be stopped. May not be <code>null</code>.
     * @param handle Handle to native instance.
     * @throws StackException If some error occured.
     */
    private native long stop(Object callId, long handle) throws StackException;

    /* Javadoc in interface. */
    public void setSkew(SkewMethod method, long skew) throws StackException {
        try {
            if (nativeHandle == -1) {
                throw new IllegalStateException(
                        "setSkew: This stream has been deleted.");
            }

            setSkew(method.decimalDef(), skew, nativeHandle);
        }
        catch (Throwable t) {
            handleThrowable(t, "setSkew: ");
        }
    }
    
    /**
     * Native method implementation for cancel.
     *
     * @param method Decimal representation of {@link IMediaStream.SkewMethod}.
     * @param skew   Skew in milliseconds.
     * @param nativeHandle Handle to native instance.
     * 
     * @throws StackException If some error occured.
     */
    private native void setSkew(int method, long skew, long nativeHandle)
        throws StackException;
    
    /* Javadoc in interface. */
    public void cancel() throws StackException {
        try {
            if (!isCreated) {
                throw new IllegalStateException(
                        "cancel: Method create has not been called");
            }

            if (nativeHandle == -1) {
                throw new IllegalStateException(
                        "cancel: This stream has been deleted.");
            }

            cancel(nativeHandle);
        }
        catch (Throwable t) {
            handleThrowable(t, "cancel: ");
        }
    }

    /**
     * Native method implementation for cancel.
     *
     * @param handle Handle to native instance.
     * @throws StackException If some error occured.
     */
    private native void cancel(long handle) throws StackException;

    /* Javadoc in interface. */
    public int getCumulativePacketLost() throws StackException {
        try {
            if (!isCreated) {
                throw new IllegalStateException("Method create has not been called");
            }

            if (nativeHandle == -1) {
                throw new IllegalStateException("This stream has been deleted.");
            }

            return getCumulativePacketLost(nativeHandle);
        }
        catch (Throwable t) {
            handleThrowable(t, "getCumulativePacketLost: ");

            // To make the compiler happy, this line is never reached.
            return 0;
        }
    }

    /**
     * Native implementation for getCumulativePacketLost.
     *
     * @param handle Handle to native instance.
     * @return Current number of lost packets.
     * @throws StackException If some error occured.
     */
    private native int getCumulativePacketLost(long handle)
            throws StackException;

    /* Javadoc in interface. */
    public short getFractionLost() throws StackException {
        try {
            if (!isCreated) {
                throw new IllegalStateException("Method create has not been called");
            }

            if (nativeHandle == -1) {
                throw new IllegalStateException("This stream has been deleted.");
            }

            return getFractionLost(nativeHandle);
        }
        catch (Throwable t) {
            handleThrowable(t, "getFractionLost: ");

            // To make the compiler happy, this line is never reached.
            return 0;
        }
    }

    /**
     * Native implementation of getFractionLost().
     *
     * @param handle Handle to native instance.
     * @return Current loss fraction.
     * @throws StackException If some error occured.
     */
    public native short getFractionLost(long handle) throws StackException;

    /* Javadoc in interface. */
    public void send(ControlToken[] tokens) throws StackException {
        try {
            if (!isCreated) {
                throw new IllegalStateException("Method create has not been called");
            }

            if (nativeHandle == -1) {
                throw new IllegalStateException("This stream has been deleted.");
            }

            send(tokens, tokens.length, nativeHandle);
        }
        catch (Throwable t) {
            handleThrowable(t, "send: ");
        }
    }

    /**
     * Native implementation of send(IControlToken[]).
     *
     * @param tokens Tokens to send.
     * @param length Number of tokens to send.
     * @param handle Handle to native instance.
     */
    public native void send(ControlToken[] tokens, int length, long handle);

    /**
     * Logs an exception at level <code>DEBUG</code> before doing as follows:
     * <ul>
     * <li>UnsupportedOperationException is rethrown</li>
     * <li>StackException is rethrown</li>
     * <li>IllegalStateException is rethrown</li>
     * <li>All other exceptions is wrapped in a StackException which is
     * thrown</li>
     * </ul>
     *
     * @param t       The exception that has occured.
     * @param message Log message
     * @throws StackException and RuntimeException the resulting exceptions.
     */
    private void handleThrowable(Throwable t, String message)
            throws StackException, RuntimeException {
        if (t instanceof UnsupportedOperationException) {
            //LOGGER.debug(message, t);
            throw (UnsupportedOperationException) t;
        }
        if (t instanceof StackException) {
           // LOGGER.debug(message, t);
            throw (StackException) t;
        }
        if (t instanceof IllegalStateException) {
           // LOGGER.debug(message, t);
            throw (IllegalStateException) t;
        }
        //LOGGER.debug(message + "Unexpected exception", t);
        throw new StackException(message + "Unexpected exception", t);
    }

    /**
     * Connects an inbound stream to the given outbound stream.
     *
     * @param handleDtmfAtInbound
     * @param outboundStream The outbound stream to connect this inbound
     *                       stream to.
     * @param forwardDtmfToOutbound
     * @throws IllegalStateException    If one of the streams are
     *                                  already connected.
     * @throws IllegalArgumentException If <code>callId</code>,
     *                                  <code>outboundStream</code>, or
     *                                  <code>inboundStream</code> is
     *                                  <code>null</code>.
     * @throws StackException           If some other error occured.
     */
    public void join(boolean handleDtmfAtInbound,
                     IOutboundMediaStream outboundStream,
                     boolean forwardDtmfToOutbound)
            throws StackException {
        if (outboundStream == null) {
            throw new
                    IllegalArgumentException("You can not connect null streams.");
        }

        if (!isCreated) {
            throw new
                    IllegalStateException("join: Method create not been called");
        }

        if (nativeHandle == -1) {
            throw new
                    IllegalStateException("join: This stream has been deleted.");
        }

        try {
            RTPSession outboundSession =
                    ((MediaStreamSupport) outboundStream).getSession();

            if (outboundSession == null) {
                throw new IllegalStateException("join: outbound stream is not initialized.");
            }
            long outboundStreamHandle =
                    ((CCRTPSession) outboundSession).nativeHandle;
            join(outboundStreamHandle,
                 nativeHandle,
                 handleDtmfAtInbound,
                 forwardDtmfToOutbound);
        } catch (Throwable t) {
            handleThrowable(t, "join: ");
        }
    }

    /**
     * Native method implementation for join.
     *
     * @param handleDtmfAtInbound
     * @param outboundStreamHandle Handle to native outbound stream instance.
     * @param forwardDtmfToOutbound
     * @param inboundStreamHandle  Handle to native inbound stream instance.
     * @throws IllegalStateException If joining an already joined stream.
     * @throws StackException        If some other error occured.
     */
    private native void join(long outboundStreamHandle,
                             long inboundStreamHandle,
                             boolean handleDtmfAtInbound, 
                             boolean forwardDtmfToOutbound) throws StackException;

    /**
     * Unjoins an outbound stream from this inbound stream.
     *
     * @param outboundStream The outbound stream to disconnect from this
     *                       inbound stream.
     *
     * @throws IllegalStateException    If one of the streams are
     *                                  already connected.
     * @throws IllegalArgumentException If <code>callId</code>,
     *                                  <code>outboundStream</code>, or
     *                                  <code>inboundStream</code> is
     *                                  <code>null</code>.
     * @throws StackException           If some other error occured.
     */
    public void unjoin(IOutboundMediaStream outboundStream)
            throws StackException {
        if (outboundStream == null) {
            throw new IllegalArgumentException(
                    "You can not connect null streams.");
        }

        if (!isCreated) {
            throw new IllegalStateException(
                    "unjoin: Method create not called");
        }

        if (nativeHandle == -1) {
            throw new IllegalStateException(
                    "unjoin: This stream is deleted.");
        }

        try {
            RTPSession outboundSession =
                    ((MediaStreamSupport) outboundStream).getSession();
            long outboundStreamHandle =
                    ((CCRTPSession) outboundSession).nativeHandle;
            unjoin(outboundStreamHandle, nativeHandle);
        } catch (Throwable t) {
            handleThrowable(t, "unjoin: ");
        }
    }

    /**
     * Native method implementation for unjoin.
     *
     * @param outboundStreamHandle Handle to native outbound stream instance.
     * @param inboundStreamHandle  Handle to native inbound stream instance.
     * 
     * @throws IllegalStateException If unjoining streams which are
     *                               not joined.
     * @throws StackException        If some other error occured.
     */
    private native void unjoin(long outboundStreamHandle,
                               long inboundStreamHandle) throws StackException;


    /* Javadoc in interface. */
    public int getSenderSSRC(){
        return getSenderSSRC(nativeHandle);
    }

    /**
     * Native method implementation for getSenderSSRC
     *
     * @param handle Handle to native inbound video stream
     * @return
     */
    private native int getSenderSSRC(long handle);

    /** Javadoc in interface */
    public void sendPictureFastUpdate(int ssrc) {
        sendPictureFastUpdate(nativeHandle, ssrc);
    }

    /**
     * Native method implementation for sendPictureFastUpdate
     *
     * @param handle Handle to outbound stream
     * @param ssrc SSRC of media sender
     */
    private native void sendPictureFastUpdate(long handle, int ssrc);

}
