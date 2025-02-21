/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.ContentTypeMapper;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.MediaProperties;

import java.util.LinkedList;
import java.util.Collection;

/**
 * Used to create and control inbound media streams.
 *
 * @author Jörgen Terner
 */
public final class InboundMediaStreamImpl extends MediaStreamSupport
        implements IInboundMediaStream {

    enum RecordState {
        VOID,
        IDLE,
        RECORDING,
        JOINED,
        STOPPED,
        DELETED
    }

    private static final ILogger LOGGER =
            ILoggerFactory.getILogger(InboundMediaStreamImpl.class);

    /**
     * Used to map MIME-types to content type and file extension.
     */
    private ContentTypeMapper mContentTypeMapper;

    /**
     * Used to request an I-frame.
     */
    VideoFastUpdater mVideoFastUpdater;

    /**
     * Record of streams which are joined to this stream.
     */
    private final LinkedList<IOutboundMediaStream> joinedStreams
            = new LinkedList<IOutboundMediaStream>();


    private RecordState recordState = RecordState.VOID;

    /**
     * The constructor.
     *
     * @param inboundRTPSession the RTP session which is used by this stream.
     */
    InboundMediaStreamImpl(RTPSession inboundRTPSession) {
        super(inboundRTPSession);
        getSession().init(this);
    }

    public void finalize() throws Throwable {
        if (recordState == RecordState.VOID) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("Garbing a stream which is not created");
        } else if (recordState != RecordState.DELETED) {
            LOGGER.warn("Garbing a stream which is not deleted");
        }
        super.finalize();
    }


    /* Javadoc in interface */
    public void create(Collection<RTPPayload> rtpPayloads) throws StackException {
       synchronized (this) {
            if (recordState != RecordState.VOID) {
                LOGGER.warn("Multiple create calls");
                throw new IllegalStateException("Multiple calls to create.");
            }
        }

        StreamContentInfo info = StreamContentInfo.getInbound(
        	mContentTypeMapper, rtpPayloads);
        info.setPTime(StreamConfiguration.getInstance().getDefaultPTime());
        info.setMaxPTime(StreamConfiguration.getInstance().getDefaultMaxPTime());
        super.create(info, null, null);
        synchronized (this) {
            recordState = RecordState.IDLE;
        }
        
    }

    /* Javadoc in interface */
    public void create(VideoFastUpdater videoFastUpdater, Collection<RTPPayload> rtpPayloads) throws StackException {
        synchronized (this) {
            if (recordState != RecordState.VOID) {
                LOGGER.warn("Multiple create calls");
                throw new IllegalStateException("Multiple calls to create.");
            }
        }

        StreamContentInfo info = StreamContentInfo.getInbound(mContentTypeMapper,
                rtpPayloads);
        info.setPTime(StreamConfiguration.getInstance().getDefaultPTime());
        info.setMaxPTime(StreamConfiguration.getInstance().getDefaultMaxPTime());
        super.create(info, null, null);
        mVideoFastUpdater = videoFastUpdater;
        synchronized (this) {
            recordState = RecordState.IDLE;
        }

    }

    /* Javadoc in interface */
    public void create(MediaMimeTypes mediaMimeTypes) throws StackException {
        synchronized (this) {
            if (recordState != RecordState.VOID) {
                LOGGER.warn("Multiple create calls");
                throw new IllegalStateException("Multiple calls to create.");
            }
        }

        StreamContentInfo info = StreamContentInfo.getInbound(mContentTypeMapper,
                mediaMimeTypes);
        info.setPTime(StreamConfiguration.getInstance().getDefaultPTime());
        info.setMaxPTime(StreamConfiguration.getInstance().getDefaultMaxPTime());
        super.create(info, null, null);
        synchronized (this) {
            recordState = RecordState.IDLE;
        }
    }

    /* Javadoc in interface */
    public void create(VideoFastUpdater videoFastUpdater,
                       MediaMimeTypes mediaMimeTypes) throws StackException {
        synchronized (this) {
            if (recordState != RecordState.VOID) {
                LOGGER.warn("Multiple create calls");
                throw new IllegalStateException("Multiple calls to create.");
            }
        }

        StreamContentInfo info = StreamContentInfo.getInbound(mContentTypeMapper,
                mediaMimeTypes);
        info.setPTime(StreamConfiguration.getInstance().getDefaultPTime());
        info.setMaxPTime(StreamConfiguration.getInstance().getDefaultMaxPTime());
        super.create(info, null, null);
        mVideoFastUpdater = videoFastUpdater;
        synchronized (this) {
            recordState = RecordState.IDLE;
        }
    }

    /**
     *
     */
    public void delete() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Deleting inbound stream");
        synchronized (this) {
            if (recordState == RecordState.VOID) {
                throw new IllegalStateException("The session is not, yet, created.");
            } else if (recordState == RecordState.DELETED) {
                throw new IllegalStateException("The session is already deleted.");
            }
            recordState = RecordState.DELETED;
        }
        while (joinedStreams.size() > 0) {
            Exception exception = null;
            if (LOGGER.isInfoEnabled()) LOGGER.info("Delete induced implicit unjoin");
            try {
                synchronized (joinedStreams) {
                    for (IOutboundMediaStream outboundStream : joinedStreams) {
                        getSession().unjoin(outboundStream);
                        outboundStream.unjoined();
                    }
                    joinedStreams.clear();
                }
            } catch (StackException e) {
                exception = e;
            } catch (Exception e) {
                exception = e;
            }
            if (exception != null)
                if (LOGGER.isInfoEnabled()) LOGGER.info("Exception during delete", exception);
        }
        super.delete();
    }

    /* Javadoc in interface. */
    public int getPTime() {
        StreamContentInfo info = getContentInfo();
        if (info == null) {
            throw new IllegalStateException(
                    "Create must be called before getPTime().");
        }
        return info.getPTime();
    }

    /* Javadoc in interface. */
    public int getMaxPTime() {
        StreamContentInfo info = getContentInfo();
        if (info == null) {
            throw new IllegalStateException(
                    "Create must be called before getMaxPTime().");
        }
        return info.getMaxPTime();
    }

    /* Javadoc in interface. */
    public String getHost() {
        return StreamConfiguration.getInstance().getLocalHostName();
    }

    /* Javadoc in interface. */
    public void join(IOutboundMediaStream outboundStream)
            throws StackException, IllegalStateException {
        // Bridged DTMF is default ...
        join(true, outboundStream, true);
    }

    public void join(boolean handleDtmfAtInbound,
                     IOutboundMediaStream outboundStream,
                     boolean forwardDtmfToOutbound)
            throws StackException, IllegalStateException {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("--> join()");
        if (outboundStream == null) {
            LOGGER.warn("Parameter outboundStream may not be null");
            throw new IllegalArgumentException(
                    "Parameter outboundStream may not be null");
        }

        synchronized (this) {
            if (recordState == RecordState.VOID) {
                throw new IllegalStateException("The session is not, yet, created.");
            } else if (recordState == RecordState.DELETED) {
                throw new IllegalStateException("The session is already deleted.");
            }
        }

        if (getContentInfo() == null) {
            LOGGER.warn("This stream is not created.");
            throw new IllegalStateException("This stream is not created.");
        }
        if (!isCompatible(outboundStream)) {
            LOGGER.warn("Joining incompatible streams");
            throw new IllegalArgumentException(
                    "Joining incompatible streams");
        }

        try {
            // Makes the state-test, the join-operation and the,
            // modification of state an atomic operation.
            synchronized (outboundStream) {
                if (outboundStream.isJoined()) {
                    throw new IllegalStateException(
                            "Cannot join a joined stream.");
                }
                if (LOGGER.isInfoEnabled()) LOGGER.info("Issuing join through JNI ");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Input audio content type: " + getContentInfo().getAudioPayload());
                    LOGGER.debug("Input video content type: " + getContentInfo().getVideoPayload());
                    for (RTPPayload payload : outboundStream.getSupportedPayloads()) {
                        LOGGER.debug("Output content type: " + payload.toString());
                    }
                }
                getSession().join(handleDtmfAtInbound,
                        outboundStream, forwardDtmfToOutbound);
                outboundStream.joined(this);
                joinedStreams.add(outboundStream);
            }
        }
        catch (IllegalStateException e) {
            LOGGER.debug(e);
            throw e;
        }
        catch (StackException e) {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("join: Unexpected exception", e);
            throw e;
        }
        catch (RuntimeException e) {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("join: Unexpected exception", e);
            throw e;
        }
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("<-- join()");
    }

    /* Javadoc in interface. */
    public void unjoin(IOutboundMediaStream outboundStream)
            throws StackException {
        if (outboundStream == null) {
            throw new IllegalArgumentException(
                    "Parameter outboundStream may not be null");
        }

        synchronized (this) {
            if (recordState == RecordState.VOID) {
                throw new IllegalStateException("The session is not, yet, created.");
            } else if (recordState == RecordState.DELETED) {
                throw new IllegalStateException("The session is already deleted.");
            }
        }

        try {
            synchronized (outboundStream) {
                // This if-clause "fixed" TR 31298. This was run twice for
                // some reason, and the unjoin() call crashed in the C++ parts,
                // because of a NULL handle/pointer (probably already deleted).
                if (joinedStreams.remove(outboundStream)) {
                    getSession().unjoin(outboundStream);
                }
                outboundStream.unjoined();
            }
        }
        catch (StackException e) {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("unjoin: Unexpected exception", e);
            throw e;
        }
        catch (RuntimeException e) {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("unjoin: Unexpected exception", e);
            throw e;
        }
    }

    /* Javadoc in interface. */
    public void record(Object callId, IMediaObject mediaObject,
                       RecordingProperties properties) throws StackException {

        record(callId, null, null, mediaObject, properties);
    }

    /* Javadoc in interface. */
    public void record(Object callId, IMediaObject playMediaObject,
                       IOutboundMediaStream outboundStream,
                       IMediaObject recordMediaObject,
                       RecordingProperties properties) throws StackException {
        CallSessionMapper.getInstance().putSession(callId, this, getCallSession());
        // Update current state
	// The case where recordState == RECORDING will not generete an exception. The reason
	// for this is that this object will never be notified that the recording has
	// finished if the recording stops itself, max duration exceeded or if silence detection cuts the recording.
	// Instead this case will be handled by a RecordFailed event from the C++ code.
        synchronized (this) {
            if (recordState == RecordState.IDLE) {
		recordState = RecordState.RECORDING;
	    } else if (recordState != RecordState.RECORDING) {
		throw new IllegalStateException("Record is not allowed in this state: " + recordState);
            }
        }
	
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

        StackEventNotifier notifier = getEventNotifier();
        if (notifier == null) {
            throw new IllegalStateException("Must call create before record.");
        }
        notifier.initCall(callId);

        MediaProperties mediaProperties = recordMediaObject.getMediaProperties();
        if (mediaProperties.getContentType() == null) {
            mediaProperties.setContentType(getContentInfo().getContentType());
        }

        // Checking the RecordingProperties in order to
        // determine the file format of the recording.
        // By default the file format type is determined by the RTP
        // session (in C++, InboundSession) based upon the payload types.
        if (properties.getRecordingType() == RecordingProperties.RecordingType.AUDIO) {
            // TODO: wav and 3gpp is hardcoded here depends upon codec
            if (mediaProperties.getContentType().getSubType().equals("3gpp")) {
            	String codec= mediaProperties.getContentType().getParameter("codec");
            	if (codec == null) {codec="samr";}
            	else {
            		codec=codec.toLowerCase();
            	}
            	MediaMimeTypes mimeTypes;
            	if (codec.contains("sawb")) {
            		mimeTypes = new MediaMimeTypes(RTPPayload.AUDIO_AMRWB); 
            		//FIXME not sure this is really necessary, maybe should just set it directly
            		//to the mimetype from the inbound.
                    mediaProperties.setContentType(mContentTypeMapper.mapToContentType(mimeTypes));
                    //NOTE: for call flow we use 3gpw but for user files/gateway/body we use the
                    //real extension which is 3gp.
                    mediaProperties.setFileExtension("3gp");
            		//amr-wb
            	} else if (codec.contains("samr")) {
					//amr-nb
            		mimeTypes = new MediaMimeTypes(RTPPayload.AUDIO_AMR); 
                    mediaProperties.setContentType(mContentTypeMapper.mapToContentType(mimeTypes));
                    mediaProperties.setFileExtension("3gp"); 
				} else {
					//unknown codec type.
					  throw new IllegalArgumentException(
			                    "Unknown codec type for audio/3gpp " + codec );
				}
                
            } else {
                mediaProperties.setContentType(mContentTypeMapper.mapToContentType("wav"));
                mediaProperties.setFileExtension("wav");
            }            
        }

        try {
            getSession().record(callId, playMediaObject, outboundStream,
                    recordMediaObject, properties);

            if (properties.isWaitForRecordToFinish()) {
                getEventNotifier().waitForCallToFinish(callId);
                synchronized (this) {
                    recordState = RecordState.IDLE;
                }
            }
        }
        catch (UnsupportedOperationException e) {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("record:", e);
            notifier.abortCall(callId);
            throw e;
        }
        catch (StackException e) {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("record: Unexpected exception", e);
            notifier.abortCall(callId);
            throw e;
        }
        catch (RuntimeException e) {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("record: Unexpected exception", e);
            notifier.abortCall(callId);
            throw e;
        }
    }
    
    /* Javadoc in interface. */
    public void reNegotiatedSdp(RTPPayload dtmfPayLoad) throws StackException {
    
        try {
            getSession().reNegotiatedSdp(dtmfPayLoad);
        }
        catch (StackException e) {
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("reNegotiatedSdp: Unexpected exception", e);
                //Call should probably be aborted (.. notifier.abortCall(callId); ...)
            throw e;
        }
    }

    public long stop(Object sessionId) throws StackException {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID is null");
        }
        synchronized (this) {
            if (recordState == RecordState.RECORDING) recordState = RecordState.IDLE;
            else if (recordState != RecordState.IDLE) {
                throw new IllegalStateException("Stop is not allowed in this state: " + recordState);
            }
        }

        CallSessionMapper.getInstance().putSession(sessionId, this, getCallSession());
        return super.stop(sessionId);
    }

    /* Javadoc in interface. */
    public int getCumulativePacketLost() throws StackException {
        synchronized (this) {
            if (recordState == RecordState.VOID) {
                throw new IllegalStateException("The session is not, yet, created.");
            } else if (recordState == RecordState.DELETED) {
                throw new IllegalStateException("The session is already deleted.");
            }
        }
        return getSession().getCumulativePacketLost();
    }

    /* Javadoc in interface. */
    public short getFractionLost() throws StackException {
        synchronized (this) {
            if (recordState == RecordState.VOID) {
                throw new IllegalStateException("The session is not, yet, created.");
            } else if (recordState == RecordState.DELETED) {
                throw new IllegalStateException("The session is already deleted.");
            }
        }
        return getSession().getFractionLost();
    }

    /**
     * Sets the mapper used to map MIME-types to content type and file
     * extension.
     *
     * @param mapper May not be <code>null</code>.
     * @throws IllegalArgumentException If <code>mapper</code> is
     *                                  <code>null</code>.
     */
    public void setContentTypeMapper(ContentTypeMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException(
                    "Parameter mapper may not be null");
        }
        mContentTypeMapper = mapper;
    }

    /**
     * Determining if an OutboundMediaStream is compatible to this stream.
     * The compatiblity is determined by comparing the payload types of the
     * two streams. The streams are compatible if the set of payload types
     * of the inbound (this) stream.
     *
     * @param outboundStream
     * @return <code>true</code> if the streams are compatible and
     *         <code>false</code> if not.
     */
    private boolean isCompatible(IOutboundMediaStream outboundStream) {
        boolean audioCompatible = false;
        boolean videoCompatible = false;
        if (getContentInfo().getAudioPayload() == null) {
            throw new IllegalStateException("No audio payload, stream is not initialized");
        }
        // Audio must match (nes pas?)
        for (RTPPayload payload : outboundStream.getSupportedPayloads()) {
            if (getContentInfo().getAudioPayload().equals(payload)) {
                audioCompatible = true;
                break;
            }
        }

        // The video should match if present
        // Video is present if the video payload is defined for
        // the inbound stream and the video port is defined for the outbound
        // stream.
        if (getContentInfo().getVideoPayload() != null &&
                outboundStream.getVideoPort() != -1) {
            for (RTPPayload payload : outboundStream.getSupportedPayloads()) {
                if (getContentInfo().getVideoPayload().equals(payload)) {
                    // Found a match!
                    videoCompatible = true;
                    break;
                }
            }
        } else {
            // No video, still compatible, if audio is
            videoCompatible = true;
        }
        // Streams are compatible if both video and audio are ...
        return audioCompatible && videoCompatible;
    }

    /* Javadoc in interface. */
    public void sendPictureFastUpdateRequest() {
        if (mVideoFastUpdater != null) {
            mVideoFastUpdater.sendPictureFastUpdateRequest();
        }
    }

    /* Javadoc in interface. */
    public int getInboundBitRate() {
        int bitrate = 0;
        RTPPayload audioPayload = getContentInfo().getAudioPayload();
        if (audioPayload != null) {
            bitrate += RTPPayload.get(audioPayload.getMimeType()).getBitrate();
        }

        RTPPayload videoPayload = getContentInfo().getVideoPayload();
        if (videoPayload != null) {
            bitrate += RTPPayload.get(videoPayload.getMimeType()).getBitrate();            
        }

        return bitrate;
    }
}
