/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.mediatranslationmanager.TextToSpeech;
import com.mobeon.masp.stream.jni.CallbackDispatcher;

import jakarta.activation.MimeType;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Used to create and control outbound media streams.
 *
 * @author Jörgen Terner
 */
public final class OutboundMediaStreamImpl extends MediaStreamSupport
        implements IOutboundMediaStream {
    // The main purpose of the PlayState is to handle play of multi media
    // objects. In order to avoid locks and such ugliness we use this state
    // variable to tell the multi play wether or not it is allowed to
    // play another media object.
    // The multi play is handled by the class MediaObjectPlaySequence.
    // The multi play is driven by play finished events received from the
    // C++ domain (through JNI).
    // This is also the concurrency problem. Stream is accessed by, at least,
    // threads. The call thread (Java domain) and the JNI (C++ domain) thread.
    // Now when the usage of Stream is more known than it was in the early stage
    // of the design some effort should be spent in redesign focused upon
    // concurrency issues.
    enum PlayState {
        VOID,
        IDLE,
        PLAYING,
        MULTI_PLAYING,
        STOPPED,
        DELETED
    }

    /** RTCP feedback that this stream should use */
    private AtomicReference<RTCPFeedback> rtcpFeedback = null;

    private static final ILogger LOGGER =
            ILoggerFactory.getILogger(OutboundMediaStreamImpl.class);

    /**
     * Used to convert media objects.
     */
    private AtomicReference<MediaTranslationManager> mMTM =
            new AtomicReference<MediaTranslationManager>();
    TextToSpeech translator = null;

    private PlayState playState = PlayState.VOID;

    // Here are some fields related to play the purpose is to store
    // play values so that they can be used in play after translate
    // see {@link #play} and {@link #translated}.
    private Object requestId = null;
    private PlayOption playOption = null;
    private long cursor = -1;
    private IInboundMediaStream inboundStream = null;
    private boolean isJoined = false;

    private static int countNotSupported = 0;
    private static int countNotMedia = 0;
        
    OutboundMediaStreamImpl(RTPSession outboundRTPSession) {
        super(outboundRTPSession);
        getSession().init(this);
    }

    public void finalize() throws Throwable {
        super.finalize();

        // Just want to be informed if the stream is in a state where
        // delete is not quite proper.
        if (playState == PlayState.VOID) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("Garbing a stream which is not created");
        } else if (playState != PlayState.DELETED) {
            LOGGER.warn("Garbing a stream which is not deleted");
        }
    }

    /* For backwards compatibility. */
    public void create(Collection<RTPPayload> payloads,
                       ConnectionProperties connectionProperties)
    throws StackException {
        this.create(payloads, connectionProperties, null, null);
    }

    /* Javadoc in interface. */
    public void create(Collection<RTPPayload> payloads,
                       ConnectionProperties connectionProperties, RTCPFeedback rtcpFeedback,
                       IInboundMediaStream inboundStream)
            throws StackException {
        synchronized (this) {
            if (playState != PlayState.VOID) {
                LOGGER.warn("Multiple create calls");
                throw new IllegalStateException("Multiple calls to create.");
            }
        }

        if (payloads == null) {
            throw new IllegalArgumentException("Must specify the RTP " +
                    "payload mappings when creating a stream.");
        }
        if (connectionProperties == null) {
            throw new IllegalArgumentException("connectionProperties may not be null");
        }
        if ((connectionProperties.getAudioHost() == null) ||
                (connectionProperties.getAudioPort() == UNDEFINED_PORTNUMBER)) {
            throw new IllegalArgumentException("connectionProperties must " +
                    "contain at least an audiohost and audioport.");
        }
        if ((connectionProperties.getVideoHost() != null) &&
                (connectionProperties.getVideoPort() == UNDEFINED_PORTNUMBER)) {
            throw new IllegalArgumentException("If a videohost is specified " +
                    " a videoport is needed as well.");
        }
        if (connectionProperties.getPTime() <= 0) {
            throw new IllegalArgumentException("PTime must be specified in" +
                    "connection properties for an outbound stream.");
        }
        StreamContentInfo info = StreamContentInfo.getOutbound(payloads);

        if (connectionProperties.getMaxPTime() < connectionProperties.getPTime()) {
            throw new IllegalArgumentException("MaxPTime must be greater or equal to pTime");
        }

        info.setPTime(connectionProperties.getPTime());
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("PTime is set to " + connectionProperties.getPTime() +
                    " in ConnectionProperties");

        if ((connectionProperties.getMaxPTime() % connectionProperties.getPTime()) != 0) {
            int myMaxPTime = ( (connectionProperties.getMaxPTime() / connectionProperties.getPTime()) * connectionProperties.getPTime() );
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Requested MaxPTime " + connectionProperties.getMaxPTime() +
                                " is NOT a multiple of PTime; instead, setting MaxPTime to " + myMaxPTime +
                                " in OutboundMediaStream ConnectionProperties");
            info.setMaxPTime(myMaxPTime);
        } else {
            info.setMaxPTime(connectionProperties.getMaxPTime());
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("MaxPTime is set to " + connectionProperties.getMaxPTime() +
                        " in ConnectionProperties");
        }

	super.create(info, connectionProperties, inboundStream);
        synchronized (this) {
            playState = PlayState.IDLE;
        }

        this.rtcpFeedback = new AtomicReference<RTCPFeedback>(rtcpFeedback);
    }

    /* Javadoc in interface. */
    public void joined(IInboundMediaStream inboundStream) {
        this.inboundStream = inboundStream;
        synchronized (this) {
            isJoined = true;
        }
    }

    public boolean isJoined() {
        return isJoined;
    }

    /* Javadoc in interface. */
    public void unjoined() {
        synchronized (this) {
            isJoined = false;
        }
        // Assuming that unjoin is handled elsewhere ...
        inboundStream = null;
    }

    public void delete() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Attempting to delete outbound stream");
        // TODO: which is the best way to handle this?
        synchronized (this) {
            if (playState == PlayState.VOID) {
                LOGGER.warn("Deleting a stream which is not, yet, created");
                throw new IllegalStateException("Delete before create");
            } else if (playState == PlayState.DELETED) {
                LOGGER.warn("Deleting a stream which is already deleted");
                throw new IllegalStateException("Stream already deleted");
            } else {
                playState = PlayState.DELETED;
            }
        }
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("Delete outbound stream");
        if (inboundStream != null) {
            if (LOGGER.isInfoEnabled()) LOGGER.info("Delete induced implicit unjoin");
            try {
                inboundStream.unjoin(this);
                // The inbound stream is supposed to call the unjoined() method ...
            } catch (StackException e) {
                LOGGER.warn("Implicit unjoin failed", e);
            }
        }
        super.delete();
        if (requestId != null && requestId instanceof MediaObjectPlaySequence) {
            requestId = null;
        }
    }

    public boolean canStop() {
        synchronized (this) {
            return playState != PlayState.VOID;
        }
    }


    public boolean canPlay() {
        synchronized (this) {
            return playState == PlayState.IDLE && !isJoined;
        }
    }

    public boolean isMultiPlay() {
        synchronized (this) {
            return playState == PlayState.MULTI_PLAYING;
        }
    }

    /* Javadoc in interface. */
    public void play(Object requestId, IMediaObject mediaObject,
                     PlayOption playOption, long cursor) throws StackException {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Single media object play is called");
        if (!(requestId instanceof MediaObjectPlaySequence)) {
            CallSessionMapper.getInstance().putSession(requestId, this, getCallSession());
        }
        // Update current state
        synchronized (this) {
            if (canPlay()) playState = PlayState.PLAYING;
            else if (isJoined) {
                throw new IllegalStateException("Cannot play while the stream is joined");
            } else if (playState == PlayState.DELETED) {
                throw new IllegalStateException("Play is not allowed since stream is deleted");
            } else if (playState == PlayState.STOPPED) {
                throw new IllegalStateException("Play is not allowed since stream is stopped");
            }
            StackEventNotifier notifier = getEventNotifier();
            if (notifier == null) {
                throw new IllegalStateException("Must call create before play.");
            }
            if (mediaObject == null) {
                throw new IllegalArgumentException(
                        "Cannot play a null MediaObject.");
            }
            if (!mediaObject.isImmutable()) {
                throw new IllegalArgumentException(
                        "Cannot play a mutable MediaObject.");
            }
            if (requestId == null) {
                throw new IllegalArgumentException("Request ID may not be null.");
            }
            if (playOption == null) {
                throw new IllegalArgumentException("PlayOption may not be null.");
            }
            if (cursor < 0) {
                throw new IllegalArgumentException("Cursor must be >= 0.");
            }

            // Check if translation is needed ...
            // 1) is this stream compatible with the media object with
            //    respect to the media type?
            // 2) is the media primary type "text"?
            // 3) if neither #1 or #2, then we have a problem. Is it up to
            //    to stream or MTM?
            // Saving play parameters which will be used when playing the translated
            // media object.
            setRequestId(requestId);
            setPlayOption(playOption);
            setCursor(cursor);

            MimeType contentType =
                    mediaObject.getMediaProperties().getContentType();

            if (contentType == null) {
                throw new IllegalArgumentException("No content type in MO");
            }
            
            if (isSupported(contentType)) {
                // This media can be handled by stream directly

                if (LOGGER.isDebugEnabled()) {
                    RTPPayload[] payloads = getSupportedPayloads();
                    LOGGER.debug("About to play media object with content type " +
                            contentType.getBaseType());
                    for (RTPPayload p : payloads) {
                        LOGGER.debug("Supported content type (sent to create): " +
                                p.getMimeType().getBaseType());
                    }
                }

                if (!checkCursorAndInitiatePlay(requestId, mediaObject)) {
                    return;
                }
                try {
                    int reqId = CallbackDispatcher.getSingleton().addRequest(requestId, notifier);
                    getSession().play(reqId, mediaObject, playOption, cursor);

                    // This would make the call synchronous
                    // getEventNotifier().waitForCallToFinish(requestId);
                }
                catch (UnsupportedOperationException e) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("play:", e);
                    notifier.abortCall(requestId);
                    throw e;
                }
                catch (StackException e) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("play: Unexpected exception", e);
                    notifier.abortCall(requestId);
                    throw e;
                }
                catch (RuntimeException e) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("play: Unexpected exception", e);
                    notifier.abortCall(requestId);
                    throw e;
                }
            } else {
                
                countNotSupported++;
                LOGGER.error("PlayOutbound: NotSupported (" + countNotSupported + ") ContentType: " + contentType);
                
                // This media has to be translated
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Cannot handle contentType " +
                            contentType.toString() + ", sending media object " +
                            "to MTM for translation.");
                }
                countNotMedia++;
                LOGGER.error("PlayOutbound: NotMediaTranslation (" + countNotMedia + ") ContentType: " + contentType);

                String msg =
                            "No media translation manager set in stream, " +
                                    "no conversions can be made";
                if (LOGGER.isDebugEnabled())
                        LOGGER.debug(msg);
                    
                if (requestId instanceof MediaObjectPlaySequence) {
                   ((MediaObjectPlaySequence) requestId).requestFailed(msg);
                } else {
                   this.getEventNotifier().playFailed(requestId, msg);
                }
            }
        }
    }

    /* Javadoc in interface. */
    public void play(Object requestId, IMediaObject[] mediaObjects,
                     PlayOption playOption, long cursor) throws StackException {
        CallSessionMapper.getInstance().putSession(requestId, this, getCallSession());
        // Update current state
        synchronized (this) {
            if (canPlay()) playState = PlayState.MULTI_PLAYING;
            else if (isJoined) {
                throw new IllegalStateException(
                        "Cannot play while the stream is joined");
            } else if (playState == PlayState.DELETED) {
                throw new IllegalStateException("Play is not allowed since stream is deleted");
            } else if (playState == PlayState.STOPPED) {
                throw new IllegalStateException("Play is not allowed since stream is stopped");
            }
        }

        StackEventNotifier notifier = getEventNotifier();
        if (notifier == null) {
            throw new IllegalStateException("Must call create before play.");
        }
        if (mediaObjects == null) {
            throw new IllegalArgumentException(
                    "Cannot play a null sequence of MediaObjects.");
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Multiple media object play is called: " + mediaObjects.length);

        if ((cursor > 0) && (mediaObjects.length > 1)) {
            if (!cursorCanBeUsed(mediaObjects, cursor)) {
                cursor = 0;
            }
        }

        MediaObjectPlaySequence seq =
                new MediaObjectPlaySequence(this, requestId, mediaObjects,
                        playOption, cursor);
        notifier.initCall(requestId);
        try {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Initiate sequenced play");
            seq.playNext();
        }
        catch (UnsupportedOperationException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("play:", e);
            notifier.abortCall(requestId);
            throw e;
        }
        catch (StackException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("play: Unexpected exception", e);
            notifier.abortCall(requestId);
            throw e;
        }
        catch (RuntimeException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("play: Unexpected exception", e);
            notifier.abortCall(requestId);
            throw e;
        }
    }

    /* Javadoc in interface. */
    public void cancel() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Cancelling outbound stream");
        synchronized (this) {
            if (playState == PlayState.VOID) {
                throw new IllegalStateException(
                        "cancel: Session is not yet created/initilaized.");
            } else if (playState == PlayState.DELETED) {
                throw new IllegalStateException(
                        "cancel: Session is deleted.");
            } else if (playState == PlayState.STOPPED) return;
            playState = PlayState.STOPPED;
        }
        try {
            getSession().cancel();
        }
        catch (StackException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("cancel: Unexpected exception", e);
        }
        catch (RuntimeException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("cancel: Unexpected exception", e);
            throw e;
        }
        synchronized (this) {
            playState = PlayState.IDLE;
        }
    }

    public long stop(Object callId) throws StackException {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Stopping outbound stream");
        CallSessionMapper.getInstance().putSession(callId, this, getCallSession());
        synchronized (this) {
            if (playState == PlayState.VOID) {
                throw new IllegalStateException(
                        "cancel: Session is not yet created/initilaized.");
            } else if (playState == PlayState.DELETED) {
                throw new IllegalStateException(
                        "cancel: Session is deleted.");
            }
            playState = PlayState.STOPPED;
        }
        long cursor = super.stop(callId);
        synchronized (this) {
            playState = PlayState.IDLE;
        }
        return cursor;
    }

    /* Javadoc in interface. */
    public void translationDone(IMediaObject mediaObject) {
        StackEventNotifier notifier = getEventNotifier();
        try {
            if (notifier == null) {
                throw new IllegalStateException(
                        "The stream is not created.");
            }

            if (getRequestId() == null) throw new
                    IllegalStateException("No call id defined.");
            if (getPlayOption() == null) throw new
                    IllegalStateException("No play option defined");
            if (getCursor() < 0) throw new
                    IllegalStateException("No cursor defined.");

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Translated media object arrived.");

            if (!checkCursorAndInitiatePlay(requestId, mediaObject)) {
                return;
            }
            int reqId = CallbackDispatcher.getSingleton().addRequest(getRequestId(), notifier);
            getSession().play(reqId, mediaObject,
                    getPlayOption(), getCursor());
        } catch (Throwable t) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Exception from play (translated media):", t);
            if (notifier != null) {
                notifier.abortCall(requestId);
                Object requestId = getRequestId();
                if (requestId instanceof MediaObjectPlaySequence) {
                    ((MediaObjectPlaySequence) requestId).requestFailed(
                            t.getMessage());
                } else {
                    synchronized (this) {
                        playState = PlayState.IDLE;
                    }
                    notifier.playFailed(requestId, t.getMessage());
                }
            }
        } finally {
            setRequestId(null);
            setPlayOption(null);
            setCursor(-1);
        }
    }

    /* Javadoc in interface. */
    public void translationFailed(String cause) {
        Object requestId = getRequestId();
        if (translator != null) translator.close();
        if (requestId instanceof MediaObjectPlaySequence) {
            ((MediaObjectPlaySequence) requestId).requestFailed(cause);
        } else {
            synchronized (this) {
                playState = PlayState.IDLE;
            }
            getEventNotifier().playFailed(requestId, cause);
        }
    }

    /* Javadoc in interface. */
    public void translationDone() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Translation done.");
        if (translator != null) translator.close();
        if (requestId instanceof MediaObjectPlaySequence) {
            ((MediaObjectPlaySequence) requestId).requestFinished(PlayFinishedEvent.CAUSE.PLAY_FINISHED, 0);
        } else {
            synchronized (this) {
                playState = PlayState.IDLE;
            }
            this.getEventNotifier().playFinished(requestId,
                    PlayFinishedEvent.CAUSE.PLAY_FINISHED,
                    0);
        }
    }

    /* Javadoc in interface. */
    public void send(ControlToken[] tokens) throws StackException {
        try {
            getSession().send(tokens);
        }
        catch (StackException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("send: Unexpected exception", e);
            throw e;
        }
        catch (RuntimeException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("send: Unexpected exception", e);
            throw e;
        }
    }


    /**
     * Checks if a media object should be skipped because the cursor
     * is past all media data. If not, the request is initiated.
     *
     * @param requestId   Identifies this request.
     * @param mediaObject The media.
     * @return <code>true</code> if the media object shall be played,
     *         <code>false</code> if is shall be skipped.
     */
    private boolean checkCursorAndInitiatePlay(Object requestId,
                                               IMediaObject mediaObject) {
        // Check if the cursor is past the end of this media object
        // This implementation could be modified to that length is
        // only read if the cursor is > 0. For now, however, this 
        // implementation is used to reveal callers that does not
        // set the media length.
        try {
        	
        	// mettre un log mario
        	 if (LOGGER.isDebugEnabled()) {
        		// StackEventNotifier notifier = getEventNotifier();
        		 LOGGER.debug("checkCursorAndInitiatePlay: called ");
        	 }
            long lengthMs = mediaObject.getMediaProperties().getLengthInUnit(
                    MediaLength.LengthUnit.MILLISECONDS);
            
            
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Length in milliseconds=" + lengthMs);

            if (cursor > lengthMs) {
                // This media object should not be played because the cursor
                // is past the end of the media.
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Cursor is longer than Media Object");
                if (requestId instanceof MediaObjectPlaySequence) {
                    ((MediaObjectPlaySequence) requestId).skippedMedia(lengthMs);
                } else {
                    this.getEventNotifier().playFinished(requestId,
                            PlayFinishedEvent.CAUSE.PLAY_FINISHED, lengthMs);
                }
                return false;
            }
            if (requestId instanceof MediaObjectPlaySequence) {
                ((MediaObjectPlaySequence) requestId).skippedPartOfMedia();
            } else {
                getEventNotifier().initCall(requestId);
            }
            return true;
        }
        catch (IllegalArgumentException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("No length set in media object (millisec). " +
                    "The cursor might be past end of media object");

            if (!(requestId instanceof MediaObjectPlaySequence)) {
                getEventNotifier().initCall(requestId);
            }
            return true;
        }
    }

    /**
     * Checks if the cursor can be used while playing an array of media objects.
     * <p/>
     * The cursor can be used if all media objects with a supported content type
     * (no translation is needed) have a length in milliseconds set.
     *
     * @param mediaObjects Media objects.
     * @param cursor       Used as info in log messages.
     * @return <code>true</code> if the cursor information can be used.
     */
    private boolean cursorCanBeUsed(IMediaObject[] mediaObjects, long cursor) {
        for (int i = 0; i < mediaObjects.length; i++) {
            if (isSupported(mediaObjects[i].getMediaProperties().getContentType())) {
                try {
                    mediaObjects[i].getMediaProperties().getLengthInUnit(
                            MediaLength.LengthUnit.MILLISECONDS);
                }
                catch (IllegalArgumentException e) {
                    LOGGER.error(
                            "No length in milliseconds set in mediaobject " +
                                    "(with index " + i +
                                    ") sent to play. The cursor (" + cursor + ") is ignored");
                    return false;
                }
            }
        }
        return true;
    }

    /* Javadoc in interface. */
    public RTPPayload[] getSupportedPayloads() {
        if (getContentInfo() == null) {
            LOGGER.error("no playload supported");
            return new RTPPayload[0];
        }
        return getContentInfo().getPayloads();
    }

    /**
     * Checks if a content-type is supported by stream according
     * to the configuration.
     *
     * @param contentType The requested content-type.
     * @return <code>true</code> if the given type is supported,
     *         <code>false</code> otherwise.
     */
    private boolean isSupported(MimeType contentType) {
        List<MimeType> supportedTypes =
                StreamConfiguration.getInstance().getSupportedContentTypes();
        final boolean debugEnabled = LOGGER.isDebugEnabled();
        if (debugEnabled)
            LOGGER.debug("Searching for content type " + contentType.getBaseType());
        for (MimeType type : supportedTypes) {
            if (debugEnabled)
                LOGGER.debug("Matching against type from configuration: " + type.getBaseType());
            if (type.match(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Getter for the request id.
     * The main purpose is to be able to access these fields from the test programs.
     *
     * @return The request id.
     */
    public Object getRequestId() {
        return requestId;
    }

    /**
     * Setter for the request id.
     * The main purpose is to be able to access these fields from the test programs.
     *
     * @param requestId Identifies the latest request.
     */
    public void setRequestId(Object requestId) {
        this.requestId = requestId;
    }

    /**
     * Getter for the the play option.
     * The main purpose is to be able to access these fields from the test programs.
     *
     * @return the play option.
     */
    public PlayOption getPlayOption() {
        return playOption;
    }

    /**
     * Setter for the play option.
     * The main purpose is to be able to access these fields from the test programs.
     *
     * @param playOption a play option
     */
    public void setPlayOption(PlayOption playOption) {
        this.playOption = playOption;
    }

    /**
     * Getter for the play cursor.
     * The main purpose is to be able to access these fields from the test programs.
     *
     * @return the cursor.
     */
    public long getCursor() {
        return cursor;
    }

    /**
     * Setter for the play cursor.
     * The main purpose is to be able to access these fields from the test programs.
     *
     * @param cursor a cursor.
     */
    public void setCursor(long cursor) {
        this.cursor = cursor;
    }

    /**
     * Sets the MTM that shall be used for media conversions.
     *
     * @param mtm MTM, may not be <code>null</code>.
     */
    public void setMTM(MediaTranslationManager mtm) {
        if (mtm == null) {
            throw new IllegalArgumentException(
                    "The MTM may not be null!");
        }
        mMTM.set(mtm);
    }

    /** Javadoc in interface */
    public boolean usesRTCPPictureFastUpdate() {
        if (rtcpFeedback == null) {
            return false;
        }
        return rtcpFeedback.get().hasFIR();
    }
}
