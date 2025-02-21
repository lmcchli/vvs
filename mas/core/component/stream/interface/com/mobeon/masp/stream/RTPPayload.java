/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Maps MIME types to RTP payloads.
 *
 * @author Jörgen Terner
 */
public final class RTPPayload {

    public static final MimeType AUDIO_ANY; // audio/*
    public static final MimeType AUDIO_DTMF; // RFC2833
    public static final MimeType AUDIO_CN;   // RFC3389
    public static final MimeType AUDIO_PCMU;
    public static final MimeType AUDIO_PCMA;
    public static final MimeType AUDIO_AMR;  // RFC3267
    public static final MimeType AUDIO_AMRWB; //RFC3267
    public static final MimeType VIDEO_H263; // RFC2190


    public static final int RTP_PAYLOAD_TYPE_DYNAMIC_START = 96;  // RFC3267

    public enum RtpValidationResult {
        EXCACT_MATCH, PARTIAL_MATCH, NO_MATCH
    }

    private static final ILogger LOGGER =
    	ILoggerFactory.getILogger(RTPPayload.class);

    private static final Map<String, RTPPayload> MAPPING = new HashMap<String, RTPPayload>();

    /** For thread-safe access to the definition Map. */
    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    private static final String VIDEO = "video";
    private static final String AUDIO = "audio";
    private static final String TELEPHONE_EVENT = "telephone-event";

    static {
        try {
            AUDIO_ANY = new MimeType("audio", "*");
            AUDIO_DTMF = new MimeType("audio", "telephone-event");
            AUDIO_CN = new MimeType("audio", "cn");
            AUDIO_PCMU = new MimeType("audio", "pcmu");
            AUDIO_PCMA = new MimeType("audio", "pcma");
            AUDIO_AMR = new MimeType("audio", "amr");
            AUDIO_AMRWB = new MimeType("audio", "amr-wb");
            VIDEO_H263 = new MimeType("video", "h263");
        } catch (MimeTypeParseException e) {
            // Should not be possible when the arguments are constant strings.
            LOGGER.error("Failed to create MimeType", e);
            throw new IllegalStateException("Failed to create MimeType", e);
        }

       // MAPPING.put(AUDIO_AMR.toString().toLowerCase(),
       //         new RTPPayload(97, AUDIO_AMR, "AMR", 8000, 1)); // RFC3267
    }

    /** Payloadtype defined in RFC3551. */
    private int mPayloadType;

    /** MIME type ("audio/pcmu" for example). */
    private MimeType mMimeType;

    /** Case-sensitive encoding. */
    private String mEncoding;

    /** Clockrate in Hz. */
    private int mClockRate;

    /** Number of channels. Not supported yet*/
    private int mChannels = 1;

    /** Bitrate **/
    private int mBitrate = 0;
	private int mBwSender = -1;
	private int mBwReceiver = -1;
	private int mMinSender = -1;
	private int mMaxSender = -1;
	private int mMinReceiver = -1;
	private int mMaxReceiver = -1;

    /** Media format specific parameters **/
    private String mMediaFormatParameters;

    /** Create a validator for format specific parameters **/
    private FormatSpecificParametersValidator mFormatSpecificParametersValidator;

    /**
     * Creates a new RTPPayload istance.
     *
     * @param payloadType RTP Payload defined in RFC3551.
     * @param mimeType    MIME type that should be mapped to the given
     *                    <code>payloadType</code>.
     * @param encoding    Encoding as defined by IANA (case-sensitive).
     * @param clockRate   Clockrate in Hz.
     * @param channels    Number of channels.
     * @param bitrate     Bitrate in bits per second (bps)
     * @param mediaFormatParameters  Formats specific parameters
     */
    public RTPPayload(int payloadType, MimeType mimeType,
    		String encoding, int clockRate, int channels,
    		int bitrate, String mediaFormatParameters) {
    	mPayloadType = payloadType;
    	mMimeType = mimeType;
    	mClockRate = clockRate;
    	mChannels = channels;
    	mEncoding = encoding;
    	mBitrate = bitrate;
    	mMediaFormatParameters = mediaFormatParameters;
    	mFormatSpecificParametersValidator = null;
    	//new FormatSpecificParametersValidator(mediaFormatParameters);
    }

    /**
     * Creates a new RTPPayload instance with bandwidth modifiers.
     *
     * @param payloadType RTP Payload defined in RFC3551.
     * @param mimeType    MIME type that should be mapped to the given
     *                    <code>payloadType</code>.
     * @param encoding    Encoding as defined by IANA (case-sensitive).
     * @param clockRate   Clockrate in Hz.
     * @param channels    Number of channels.
     * @param bandwidth	  Total bandwidth
     * @param bwSender	  RTCP bandwidth allocated to active data senders
     * @param minSender	  Minimum allowed value of bwSender
     * @param maxSender   Maximum allowed value of bwSender
     * @param bwReceiver  RTCP bandwidth allocated to other participants
     * @param minReceiver Minimum allowed value of bwReceiver
     * @param maxReceiver Maximum allowed value of bwReceiver
     */
    public RTPPayload(int payloadType, MimeType mimeType,
    		String encoding, int clockRate, int channels, int bitrate,
    		String mediaFormatParameters, int bwSender, int minSender, int maxSender,
    		int bwReceiver,  int minReceiver, int maxReceiver) {
    	mPayloadType = payloadType;
    	mMimeType = mimeType;
    	mClockRate = clockRate;
    	mChannels = channels;
    	mEncoding = encoding;
    	mBitrate= bitrate;
    	mMediaFormatParameters = mediaFormatParameters;
    	mBwSender = bwSender;
    	mMinSender = minSender;
    	mMaxSender = maxSender;
    	mBwReceiver = bwReceiver;
    	mMinReceiver = minReceiver;
    	mMaxReceiver = maxReceiver;
    }

    /**
     * Update RTP payload definitions. This method should be called
     * whenever the configuration is updated.
     */
    public static void updateDefs(List<RTPPayload> defs) {
    	LOCK.writeLock().lock();
    	try {
    		MAPPING.clear();
    		for (RTPPayload def : defs) {
    			def.mFormatSpecificParametersValidator =
    				new FormatSpecificParametersValidator(def.mMediaFormatParameters);
    			MAPPING.put(def.getMimeType().toString().toLowerCase(), def);
    		}
    	}
    	finally {
    		LOCK.writeLock().unlock();
    	}
    }

    /**
     * Returns the RTP Payload that corresponds to the given MIME type.
     *
     * @param mimeType MIME type.
     *
     * @return The corresponding RTP Payload or <code>null</code>, if no
     *         mapping is known.
     */
    public static RTPPayload get(MimeType mimeType) {
    	if (mimeType == null) {
    		LOGGER.debug("null sent as MimeType to RTPPayload.get(MimeType)");
    		return null;
    	}
    	LOCK.readLock().lock();
    	try {
    		return MAPPING.get(mimeType.getBaseType().toLowerCase());
    	}
    	finally {
    		LOCK.readLock().unlock();
    	}
    }


    /**
     * Validate that the given format specific parameters is compatible with the
     * configured format specific parameters for the given mime type.
     *
     * @param mimeType The mime type to test against.
     * @param fmtp The format specific parameters to validate.
     * @return true if fmtp was validated ok, false otherwise.
     */
    public static RtpValidationResult validateFormatSpecificParameters(
    		MimeType mimeType, String fmtp) {

    	RTPPayload rtpPayload = get(mimeType);
    	return rtpPayload.validateFormatSpecificParameters(fmtp);
    }

    /**
     * Validate that the given format specific parameters is compatible with the
     * configured format specific parameters for this RTPPayload.
     *
     * @param fmtp The format specific parameters to validate.
     * @return true if fmtp was validated ok, false otherwise.
     */
    private RtpValidationResult validateFormatSpecificParameters(String fmtp) {
    	if (mFormatSpecificParametersValidator != null)
    		return mFormatSpecificParametersValidator.validateFormatParameters(fmtp);
    	else
    		return RtpValidationResult.NO_MATCH;
    }


    public int getClockRate() {
        return mClockRate;
    }

    public MimeType getMimeType() {
        return mMimeType;
    }

    public String getContentType() {
        return mMimeType.getPrimaryType();
    }

    public String getSubType() {
        return mMimeType.getSubType();
    }

    /**
     * Case-sensitive encoding name. The MIME type is in general in lower-case
     * so if case is important, this encoding can be used.
     *
     * @return Encoding in correct case according to IANA.
     */
    public String getEncoding() {
        return mEncoding;
    }

    public int getPayloadType() {
        return mPayloadType;
    }

    public int getChannels() {
        return mChannels;
    }

    public int getBitrate() {
        return mBitrate;
    }

    public int getBandwidth() {
        return (int)Math.ceil((double)mBitrate / 1000.0);
    }

    public String getMediaFormatParameters() {
        return mMediaFormatParameters;
    }

    public boolean isAudio() {

        return AUDIO.equals(getContentType());
    }

    public boolean isDTMF() {
        return TELEPHONE_EVENT.equals(getSubType());
    }

    public boolean isVideo() {
        return VIDEO.equals(getContentType());
    }

    public String toString() {
        return getContentType() + "/" + getSubType() +
                ":" + mEncoding + "," +
                "PT:" + mPayloadType + "," +
                "CR:" + mClockRate + "," +
                "BW:" + mBitrate + "," +
                "C:" + mChannels + "," +
                "RS:" + mBwSender + "," +
                "minRS:" + mMinSender + "," +
                "maxRS:" + mMaxSender + "," +
                "RR:" + mBwReceiver + "," +
                "minRR:" + mMinReceiver + "," +
                "maxRR:" + mMaxReceiver;
    }

    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(this.getClass())))
        {
            RTPPayload p = (RTPPayload)obj;
            // Check if we are dealing with dynamic payload types (see RFC 3551)
            // payload types 96 to 127 are dynamic. 
            // We cannot simply compare dynamic payload types to asses equality.
            if ((p.mPayloadType < RTP_PAYLOAD_TYPE_DYNAMIC_START) &&  
                    (mPayloadType < RTP_PAYLOAD_TYPE_DYNAMIC_START)) {
                return p.mPayloadType == mPayloadType;
            } else { 
                // we are dealing with one or two dynamic payload types
                // not sure if we should compare all attributes or only a subset to determine equality
                // for the moment let only use the encoding (i.e. AMR) and the clock rate.
                return ((p.mEncoding != null && p.mEncoding.equalsIgnoreCase(mEncoding)) &&
                        (p.mClockRate == mClockRate));
            }
        }                 
        return false;        
    }
        

    public int hashCode() {
        return mPayloadType;
    }

	public int getBwSender() {
		return mBwSender;
	}

	public int getBwReceiver() {
		return mBwReceiver;
	}

	public int getMinSender() {
		return mMinSender;
	}

	public int getMaxSender() {
		return mMaxSender;
	}

	public int getMinReceiver() {
		return mMinReceiver;
	}

	public int getMaxReceiver() {
		return mMaxReceiver;
	}
}
