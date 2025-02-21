/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.ContentTypeMapper;
import com.mobeon.masp.mediaobject.MediaMimeTypes;

import jakarta.activation.MimeType;
import java.util.Collection;
import java.util.List;

/**
 * Contains information about the media that shall be received
 * on an inbound stream / sent on an outbound stream.
 * <p> 
 * This information is needed to specify the correct RTP payload type 
 * and to save received media in a MediaObject.
 * <p>
 * The following information is needed:
 * <ul>
 * <li>RTP payload types for audio (required), DTMF (required) 
 *     and video (optional).</li>
 * <li>Content type (required for inbound streams).</li>
 * <li>File extension (required for inbound streams).</li>
 * <li>CNAME that shall be specified in all outgoing packets from
 *     this stream.</li>
 * </ul>
 * <p>
 * Future work: Make this class cache StreamContentInfo instances.
 * 
 * @author Jörgen Terner
 */
public final class StreamContentInfo {
    private static final ILogger LOGGER = 
        ILoggerFactory.getILogger(StreamContentInfo.class);
    
    /** Payload type for audio. Only set for Inbound streams. */
    private RTPPayload mAudioPayload;
    /** Payload type for video. Only set for Inbound streams. */
    private RTPPayload mVideoPayload;
    /** Only set for inbound streams. */
    private MimeType mContentType;
    /** Only set for inbound streams. */
    private String mFileExtension; // "WAV" for example

    private RTPPayload mDTMFPayload;
    private RTPPayload mCNPayload;
    
    private boolean mIsVideo = false;
    
    private String mCNAME;

    /** 
     * Recommended length of time in milliseconds represented by the media in 
     * a packet.
     */
    private int mPTime = 40;

    /**
     * Maximum length of time in milliseconds represented by the media in
     * a packet.
     */
    private int mMaxPTime = 40;

    /** 
     * Payload types for all supported codecs. Only set for
     * Outbound streams. Stored in an array to simplify retreival
     * of elements from native code.
     */
    RTPPayload[] mPayloads;
        
    /**
     * Maps the given MIME-type information (codec information) to
     * payload types, content type and file extension. This method
     * should be used from all inbound streams.
     * 
     * @param mapper         Used to map to content type and file extension.
     * @param mediaMimeTypes Key.
     * 
     * @throws IllegalArgumentException If <code>mapper</code> or 
     *         <code>mediaMimeTypes</code> is <code>null</code> or if
     *         <code>mediaMimeTypes</code> is empty.
     * @throws IllegalStateException    If <code>mediaMimeTypes</code>
     *         could not be mapped to the required information:
     *         content type, file extension and audio RTP payload.
     */
    public static StreamContentInfo getInbound(ContentTypeMapper mapper,
            MediaMimeTypes mediaMimeTypes) {
        return new StreamContentInfo(mapper, mediaMimeTypes);
    }

    /**
     * Maps the given MIME-type information (codec information) to
     * payload types, content type and file extension. This method
     * should be used from all inbound streams.
     *
     * @param mapper         Used to map to content type and file extension.
     * @param payloads RTP payloads for the session.
     *
     * @throws IllegalArgumentException If <code>mapper</code> or
     *         <code>payloads</code> is <code>null</code> or if
     *         <code>payloads</code> is empty.
     * @throws IllegalStateException    If <code>payloads</code>
     *         could not be mapped to the required information:
     *         content type, file extension and audio RTP payload.
     */
    public static StreamContentInfo getInbound(ContentTypeMapper mapper,
            Collection<RTPPayload> payloads) {
        return new StreamContentInfo(mapper, payloads);
    }

    /**
     * Maps the given MIME-type information (codec information) to
     * payload types. If <code>payloads</code> does not contain a required
     * mapping, a default mapping is used. This method should be used from 
     * all outbound streams.
     * 
     * @param payloads Mappings of MIME-types to payloads.
     * 
     * @throws IllegalArgumentException If <code>payloads</code> 
     *         is <code>null</code>.
     */
    public static StreamContentInfo getOutbound(
            Collection<RTPPayload> payloads){
        return new StreamContentInfo(payloads);
    }

    /**
     * Maps the given MIME-type information (codec information) to
     * payload types, content type and file extension. This method
     * should be used from all inbound streams.
     * 
     * @param mapper         Used to map to content type and file extension.
     * @param mediaMimeTypes Keys.
     * 
     * @throws IllegalArgumentException If <code>mapper</code> or 
     *         <code>mediaMimeTypes</code> is <code>null</code> or if
     *         <code>mediaMimeTypes</code> is empty.
     * @throws IllegalStateException    If <code>mediaMimeTypes</code>
     *         could not be mapped to the required information:
     *         content type, file extension and audio RTP payload.
     */
    private StreamContentInfo(ContentTypeMapper mapper, 
            MediaMimeTypes mediaMimeTypes) {
        if (mapper == null) {
            throw new IllegalArgumentException(
                "Parameter mapper may not be null");
        }
        if (mediaMimeTypes == null) {
            throw new IllegalArgumentException(
                    "Parameter mediaMimeTypes may not be null");
        }
        if (mediaMimeTypes.getNumberOfMimeTypes() == 0) {
            throw new IllegalArgumentException("Parameter mediaMimeTypes " +
                    "must contain at least one MIME-type");
        }
        mContentType = mapper.mapToContentType(mediaMimeTypes);
        if (mContentType == null) {
            throw new IllegalStateException("The given MediaMimeTypes " +
                    "could not be mapped to a content type");
        }
        mFileExtension = mapper.mapToFileExtension(mediaMimeTypes);
        if (mFileExtension == null) {
            throw new IllegalStateException("The given MediaMimeTypes " +
                    "could not be mapped to a file extension");
        }

        List<MimeType> types = mediaMimeTypes.getAllMimeTypes();
        for (MimeType mimeType : types) {
            RTPPayload payload = RTPPayload.get(mimeType);
            if (payload != null) {
                if (payload.isAudio()) {
                    mAudioPayload = payload;
                }
                else if (payload.isVideo()) {
                    mVideoPayload = payload;
                    mIsVideo = true;
                }
            }
            else {
                LOGGER.debug("Could not map media type to RTP payload: " + 
                        mimeType);
            }
        }
        if (mAudioPayload == null) {
            throw new IllegalStateException("The given MediaMimeTypes " +
                "could not be mapped to an audio RTP payload");
        }
        mDTMFPayload = RTPPayload.get(RTPPayload.AUDIO_DTMF);
        mCNPayload = RTPPayload.get(RTPPayload.AUDIO_CN);
        if (mDTMFPayload == null) {
            throw new IllegalStateException(
                    "No RTP payload mapping exists for DTMF. " +
                    "A default mapping should always exists in the " +
                    "configuration.");
        }
        if (mCNPayload == null) {
            throw new IllegalStateException(
                    "No RTP payload mapping exists for CN (Comfort noise). " +
                    "A default mapping should always exists in the " +
                    "configuration.");
        }
    }



    /**
     * Maps the given MIME-type information (codec information) to
     * payload types, content type and file extension. This method
     * should be used from all inbound streams.
     *
     * @param mapper         Used to map to content type and file extension.
     * @param payloads RTP payloads in the session.
     *
     * @throws IllegalArgumentException If <code>mapper</code> or
     *         <code>payloads</code> is <code>null</code> or if
     *         <code>payloads</code> is empty.
     * @throws IllegalStateException    If <code>mediaMimeTypes</code>
     *         could not be mapped to the required information:
     *         content type, file extension and audio RTP payload.
     */
    private StreamContentInfo(ContentTypeMapper mapper,
            Collection<RTPPayload> payloads) {
        if (mapper == null) {
            throw new IllegalArgumentException(
                "Parameter mapper may not be null");
        }
        if (payloads == null) {
            throw new IllegalArgumentException(
                    "Parameter mediaMimeTypes may not be null");
        }
        if (payloads.isEmpty()) {
            throw new IllegalArgumentException("Parameter mediaMimeTypes " +
                    "must contain at least one RTP payload");
        }

        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();
        for (RTPPayload payload : payloads) {
            if (payload.getMimeType().toString().equals(RTPPayload.AUDIO_DTMF.toString())) {
                mDTMFPayload = payload;
            } else if (!payload.getMimeType().toString().equals(RTPPayload.AUDIO_CN.toString())) {
                mediaMimeTypes.addMimeType(payload.getMimeType());
                if (payload.isAudio()) {
                        mAudioPayload = payload;
                } else if (payload.isVideo()) {
                    mVideoPayload = payload;
                    mIsVideo = true;
                }
            }
        }

        mContentType = mapper.mapToContentType(mediaMimeTypes);
        if (mContentType == null) {
            throw new IllegalStateException("The given MediaMimeTypes " +
                    "could not be mapped to a content type");
        }
        
        mFileExtension = mapper.mapToFileExtension(mediaMimeTypes);
        if (mFileExtension == null) {
            throw new IllegalStateException("The given MediaMimeTypes " +
                    "could not be mapped to a file extension");
        }

        if (mAudioPayload == null) {
            throw new IllegalStateException("The given MediaMimeTypes " +
                "could not be mapped to an audio RTP payload");
        }

        if (mDTMFPayload == null)
            mDTMFPayload = RTPPayload.get(RTPPayload.AUDIO_DTMF);

        if (mDTMFPayload == null) {
            throw new IllegalStateException(
                    "No RTP payload mapping exists for DTMF. " +
                    "A default mapping should always exists in the " +
                    "configuration.");
        }

        mCNPayload = RTPPayload.get(RTPPayload.AUDIO_CN);

        if (mCNPayload == null) {
            throw new IllegalStateException(
                    "No RTP payload mapping exists for CN (Comfort noise). " +
                    "A default mapping should always exists in the " +
                    "configuration.");
        }
    }

    /**
     * Maps the given MIME-type information (codec information) to
     * payload types. If <code>payloads</code> does not contain a required
     * mapping, a default mapping is used. This method should be used from 
     * all outbound streams.
     * 
     * @param payloads Mappings of MIME-types to payloads.
     * 
     * @throws IllegalArgumentException If <code>payloads</code> 
     *         is <code>null</code> or empty.
     * @throws IllegalStateException    If <code>payloads</code>
     *         does not contain the required audio RTP payload mapping.
     */
    private StreamContentInfo(Collection<RTPPayload> payloads) {
        if (payloads == null) {
            throw new IllegalArgumentException(
                    "Parameter payloads may not be null");
        }

        int numberOfCodecs = payloads.size();
        // Cannot call get because equals is not implemented in 
        // class MimeType
        mPayloads = new RTPPayload[numberOfCodecs];
        mPayloads = payloads.toArray(mPayloads);
//        for (RTPPayload payload : mPayloads) {
//            payload.toString();
//        }
        for (RTPPayload mPayload : mPayloads) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Payload: " + mPayload.toString());
                LOGGER.debug("Payload MimeType: " + mPayload.getMimeType());
            }
            if (mPayload.getMimeType().toString().equals(RTPPayload.AUDIO_DTMF.toString())) {
                mDTMFPayload = mPayload;
            } else if (mPayload.getMimeType().toString().equals(RTPPayload.AUDIO_CN.toString())) {
                mCNPayload = mPayload;
            } else if (mPayload.isAudio()) {
                mAudioPayload = mPayload;
            } else if (mPayload.isVideo()) {
                mVideoPayload = mPayload;
                mIsVideo = true;
            }
        }
        if (mDTMFPayload == null) {
            // If DTMF was not among the specified mappings, the default
            // mapping is used.
            mDTMFPayload = RTPPayload.get(RTPPayload.AUDIO_DTMF);
        }
        if (mCNPayload == null) {
            // If CN was not among the specified mappings, the default
            // mapping is used.
            mCNPayload = RTPPayload.get(RTPPayload.AUDIO_CN);
        }
        if (mDTMFPayload == null) {
            throw new IllegalStateException(
                    "No RTP payload mapping exists for DTMF. " +
                    "A default mapping should always exists in the " +
                    "configuration.");
        }
        if (mCNPayload == null) {
            throw new IllegalStateException(
                    "No RTP payload mapping exists for CN (Comfort noise). " +
                    "A default mapping should always exists in the " +
                    "configuration.");
        }
    }
    
    /**
     * @return The content type that should be used when saving media
     *         received with this payload type. Is <code>null</code> for
     *         outbound streams, can never be <code>null</code> for inbound
     *         streams.
     */
    MimeType getContentType() {
        return mContentType;
    }

    /**
     * @return The file extension that should be used when saving media
     *         received with this payload type. Is <code>null</code> for
     *         outbound streams, can never be <code>null</code> for inbound
     *         streams.
     */
    public String getFileExtension() {
        return mFileExtension;
    }
    
    /**
     * @return The audio payload, Is <code>null</code> for
     *         outbound streams, can never be <code>null</code> for inbound
     *         streams.
     */
    public RTPPayload getAudioPayload() {
        return mAudioPayload;
    }

    /**
     * @return The DTMF payload, can never be <code>null</code>.
     */
    public RTPPayload getDTMFPayload() {
        return mDTMFPayload;
    }

    /**
     * @return The CN (Comfort Noise) payload, can never be <code>null</code>.
     */
    public RTPPayload getCNPayload() {
        return mCNPayload;
    }

    /**
     * @return The video payload, <code>null</code> if only audio
     *         is sent/received on this stream. Is <code>null</code> for
     *         outbound streams, can never be <code>null</code> for inbound
     *         streams.
     */
    public RTPPayload getVideoPayload() {
        return mVideoPayload;
    }
    
    /**
     * @return All supported payloads. Is <code>null</code> for
     *         inbound streams, can never be <code>null</code> for outbound
     *         streams.
     */
    public RTPPayload[] getPayloads() {
        return mPayloads;
    }
    
    /**
     * @return <code>true</code> if the stream content is video, 
     *         <code>false</code> if audio only.
     */
    public boolean isVideo() {
        return mIsVideo;
    }
    
    /**
     * pTime is the recommended length of time in milliseconds represented 
     * by media in a packet.
     * 
     * @return pTime. 
     */
    public int getPTime() {
        return mPTime;
    }
    
    /**
     * Sets the recommended length of time in milliseconds represented 
     * by media in a packet.
     * 
     * @param pTime pTime.
     */
    void setPTime(int pTime) {
        mPTime = pTime;
    }

    /**
     * maxPTime is the maximum length of time in milliseconds represented
     * by media in a packet.
     *
     * @return mMaxPTime.
     */
    public int getMaxPTime() {
        return mMaxPTime;
    }

    /**
     * Sets the maxmimum length of time in milliseconds represented
     * by media in a packet.
     *
     * @param maxPTime maxPTime.
     */
    void setMaxPTime(int maxPTime) {
        mMaxPTime = maxPTime;
    }
    
    /**
     * @param name Used as identifier in the RTP packets
     *             sent by this stream. See RFC 1550.
     */
    public void setCNAME(String name) {
        mCNAME = name;
    }
    
    /**
     * @return The name used as identifier in the RTP packets
     *         sent by this stream. See RFC 1550.
     */
    public String getCNAME() {
        return mCNAME;
    }
}
