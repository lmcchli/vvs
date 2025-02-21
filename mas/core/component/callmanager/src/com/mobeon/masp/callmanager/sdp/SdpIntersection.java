/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.callmanager.sdp.attributes.SdpRtpMap;
import com.mobeon.masp.callmanager.sdp.fields.SdpBandwidth;
import com.mobeon.masp.callmanager.sdp.attributes.SdpRTCPFeedback;
import com.mobeon.masp.callmanager.sdp.attributes.SdpFmtp;
import com.mobeon.masp.callmanager.CallMediaTypes.CallMediaType;
import com.mobeon.masp.callmanager.CallProperties.CallType;
import com.mobeon.masp.stream.ConnectionProperties;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.masp.stream.RTCPFeedback;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import jakarta.activation.MimeType;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class represents an SDP intersection between a peer UA SDP,
 * the media types mandatory for the Call Manager (configurable), and the
 * {@link CallMediaTypes} requested by the call client.
 * <p>
 * The intersection contains the following information:
 * <ul>
 * <li>The {@link SdpSessionDescription} that represents the original remote SDP.
 * This session description contains a list of all media descriptions supported
 * by the SDP.</li>
 * <li> An index pointing out which media description (in the list of media
 *      descriptions in the session description) that shall be used for audio.</li>
 * <li> An index pointing out which media description (in the list of media
 *      descriptions in the session description) that shall be used for video.
 *      This index could be null, which indicates that no video intersection is
 *      found.</li>
 * <li> The first {@link CallMediaTypes} (from the list requested by the client)
 *      that matched the peer SDP.</li>
 * <li> Mandatory audio mime types that all are supported by the SDP.</li>
 * <li> Mandatory video mime types that all are supported by the SDP.</li>
 * <li> The default pTime that shall be used for an audio media if the remote
 *      SDP did not contain a pTime indication.</li>
 * </ul>
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class SdpIntersection {

    private final SdpSessionDescription sdpSessionDescription;
    private final Integer audioIndex;
    private final Integer videoIndex;
    private final Integer videoFeedbackIndex;
    private final CallMediaTypes callMediaTypes;
    private final Map<String, MimeType> mandatoryAudioMimeTypes =
            new HashMap<String, MimeType>();
    private final Map<String, MimeType> mandatoryVideoMimeTypes =
        new HashMap<String, MimeType>();
    private AtomicReference<ConnectionProperties> connectionProperties =
            new AtomicReference<ConnectionProperties>();
    private static final ILogger LOG =
            ILoggerFactory.getILogger(SdpIntersection.class);
    private Map<String, RTPPayload> supportedRtpPayloadMap = new HashMap<String, RTPPayload>();


    /**
     * Creates an SdpIntersection.
     * <p>
     * The following parameters MUST NOT be null: sessionDescription,
     * audioIndex, mandatoryAudioMimeTypes and mandatoryVideoMimeTypes.
     * <p>
     * The following rules apply:
     * <ul>
     * <li>callMediaTypes MUST contain a MimeType for audio.</li>
     * <li>If videoIndex is not null, callMediaTypes MUST contain a MimeType
     * for video.</li>
     * <li>A mandatory mime type in either mandatoryAudioMimeTypes or
     * mandatoryVideoMimeTypes MUST NOT be null.</li>
     * </ul>
     * <p>
     * All mandatory audio and video mime types and the mime types in the
     * callMediaTypes (if not null) is added to
     * {@link SdpIntersection#supportedMimeTypes}.
     *
     * @param sdpSessionDescription
     * @param audioIndex
     * @param videoIndex
     * @param videoFeedbackIndex
     * @param callMediaTypes
     * @param mandatoryAudioMimeTypes
     * @param mandatoryVideoMimeTypes
     * @throws IllegalArgumentException if audioIndex, mandatoryAudioMimeTypes
     * or mandatoryVideoMimeTypes is null. Or, if any of the rules described
     * above is violated.
     */
    public SdpIntersection(SdpSessionDescription sdpSessionDescription,
                           Integer audioIndex,
                           Integer videoIndex,
                           Integer videoFeedbackIndex,
                           CallMediaTypes callMediaTypes,
                           Collection<MimeType> mandatoryAudioMimeTypes,
                           Collection<MimeType> mandatoryVideoMimeTypes) throws SdpInternalErrorException {
        if ((sdpSessionDescription == null) || (mandatoryAudioMimeTypes == null) ||
                (mandatoryVideoMimeTypes == null) || (audioIndex == null)) {
            throw new IllegalArgumentException("Mandatory parameter is null. " +
                    "<Session Description = " + sdpSessionDescription +
                    ">, <Mandatory Audio Mime Types = " +
                    mandatoryAudioMimeTypes +
                    ">, <Mandatory Video Mime Types = " +
                    mandatoryVideoMimeTypes +
                    ">, <Audio Index = " + audioIndex + ">");
        }

        this.sdpSessionDescription = sdpSessionDescription;
        this.audioIndex = audioIndex;
        this.videoIndex = videoIndex;
        this.videoFeedbackIndex = videoFeedbackIndex;
        this.callMediaTypes = callMediaTypes;

        addMandatoryTypes(mandatoryAudioMimeTypes,CallMediaType.AUDIO);
        addCallMediaType(CallMediaType.AUDIO);

        if (videoIndex != null) {
            addMandatoryTypes(mandatoryVideoMimeTypes,CallMediaType.VIDEO);
            addCallMediaType(CallMediaType.VIDEO);
        }
       
        addSupportedRtpPayload();
              
    }


    /**
     * Creates and returns the supported RTPPayload's from the supported
     * mime types. The Map contains an {@link com.mobeon.masp.stream.RTPPayload}
     * for each supported mime type regardless of if the payload type is static
     * or dynamic (see RFC 3551). If a payload type is static and not modified
     * it is retrieved from {@link com.mobeon.masp.stream.RTPPayload}.
     * @return A map of the supported RTP payloads, indexed with mime type.
     * @throws SdpInternalErrorException
     */
    public Collection<RTPPayload> getSupportedRtpPayloads() {

        return supportedRtpPayloadMap.values();
    }

    public CallMediaTypes getCallMediaTypes() {
        return callMediaTypes;
    }

    /**
     * Returns the call type of the intersection. If the SdpIntersection was
     * created without a videoIndex (i.e. null) the intersection is considered a
     * {@link CallType.VOICE} call, otherwise the intersection is considered a
     * {@link CallType.VIDEO} call.
     * @return {@link CallType.VOICE} or {@link CallType.VIDEO}.
     */
    public CallType getCallType() {
        CallType callType = CallType.VOICE;
        if (videoIndex != null) {
            callType = CallType.VIDEO;
        }
        return callType;
    }

    public SdpSessionDescription getSessionDescription() {
        return sdpSessionDescription;
    }


    public Integer getAudioIndex() {
        return audioIndex;
    }

    public Integer getVideoIndex() {
        return videoIndex;
    }

    public Integer getVideoFeedbackIndex() {
        return videoFeedbackIndex;
    }
    /**
     * Creates connection properties based on the SDP intersection.
     * The audio host, audio port and ptime is retrieved from the
     * {@link SdpMediaDescription} (in the {@link SdpSessionDescription}) at index
     * audioIndex. If the ptime was not set in the media description, the
     * configured value for ptime is used instead.
     * The video host and video port is retrieved from the
     * {@link SdpMediaDescription} (in the {@link SdpSessionDescription}) at index
     * videoIndex.
     * @return the connection properties that describes the SDP intersection.
     */
    public ConnectionProperties getConnectionProperties() {
        if (connectionProperties.get() == null) {
            ConnectionProperties cp = new ConnectionProperties();

            SdpMediaDescription audioMD =
                    sdpSessionDescription.getMediaDescription(audioIndex);
            cp.setAudioHost(audioMD.getConnection().getAddress());
            cp.setAudioPort(audioMD.getMedia().getPort());

            if (audioMD.getAttributes().getPTime() != null) {
                cp.setPTime(audioMD.getAttributes().getPTime().getpTime());
            } else {
                cp.setPTime(ConnectionProperties.getDefaultPTime());
            }

            if (audioMD.getAttributes().getMaxPTime() != null) {
                cp.setMaxPTime(audioMD.getAttributes().getMaxPTime().getValue());
            } else {
                cp.setMaxPTime(ConnectionProperties.getDefaultMaxPTime());
            }

            if (videoIndex != null) {
                SdpMediaDescription videoMD =
                        sdpSessionDescription.getMediaDescription(videoIndex);
                cp.setVideoHost(videoMD.getConnection().getAddress());
                cp.setVideoPort(videoMD.getMedia().getPort());
            }

            connectionProperties.set(cp);
        }
        return connectionProperties.get();
    }


    /**
     * Creates and returns the supported RTCP feedback (see draft-ietf-avt-avtf-ccm)
     * At time of writing, only Full Intra-frame Request (FIR, "ccm fir") is supported.
     * @return RTCPFeedback object with supported RTCP feedback attribute values in
     */
    private void addSupportedRtpPayload() throws SdpInternalErrorException{

        //Add audio
        for (MimeType mimeType : mandatoryAudioMimeTypes.values()) {

            SdpMediaDescription audioMD = sdpSessionDescription.getMediaDescription(audioIndex);
            RTPPayload rtpPayload = getRtpPayloads(audioMD,mimeType);
            if (rtpPayload != null) {
                supportedRtpPayloadMap.put(mimeType.getBaseType().toLowerCase(), rtpPayload);
            }
        }

        //Add video
        for (MimeType mimeType : mandatoryVideoMimeTypes.values()) {

            if (videoIndex != null) {
                SdpMediaDescription videoMD = sdpSessionDescription.getMediaDescription(videoIndex);
                RTPPayload rtpPayload = getRtpPayloads(videoMD,mimeType);
                // to be consistent with the audio part above, we should only add this payload
                // if the clockRate matches - but the configured clock rate was really
                // introduced for audio payloads, not even sure it applies to video the same
                // way.  So for now, not modifying the existing behavior for video
                // payload selection.
                if (rtpPayload != null ) {
                	supportedRtpPayloadMap.put(mimeType.toString(), rtpPayload);
                }
            }
       }
    }


    /**
     * Creates and returns the supported RTCP feedback (see draft-ietf-avt-avtf-ccm)
     * At time of writing, only Full Intra-frame Request (FIR, "ccm fir") is supported.
     * @return RTCPFeedback object with supported RTCP feedback attribute values in
     */
    public RTCPFeedback getRTCPFeedback() {
        Collection<SdpMediaDescription> mds = getSessionDescription().getMediaDescriptions();
        RTCPFeedback rtcpFeedback = new RTCPFeedback();
        for (SdpMediaDescription md : mds) {
            Vector<SdpRTCPFeedback> values = md.getAttributes().getRTCPFeedback();
            for (SdpRTCPFeedback value : values) {
                String rtcbFbType = value.getParameters();
                if (rtcbFbType.equalsIgnoreCase(SdpConstants.RTCP_FB_FIR)) {
                    rtcpFeedback.addFIRValue(rtcbFbType);
                }
            }
        }
        return rtcpFeedback;
    }

    //=========================== Private Methods =========================

    /**
     * Retrieves the mime type for the given call type from the
     * {@link SdpIntersection#callMediaTypes}
     * @param type call media type
     * @throws IllegalArgumentException if the
     * {@link SdpIntersection#callMediaTypes} does not contain a mime type for
     * the given call type.
     */
    private void addCallMediaType(CallMediaType type) {
        if (callMediaTypes != null) {
            MimeType mimeType = callMediaTypes.getMimeType(type);
            if (mimeType == null) {
                throw new IllegalArgumentException(
                        "Call media types should contain a mime type for " +
                                type);
            } else {
                if(type.equals(CallMediaType.AUDIO))
                {
                    mandatoryAudioMimeTypes.put(mimeType.toString(), mimeType);

                }
                else if(type.equals(CallMediaType.VIDEO))
                {
                    mandatoryVideoMimeTypes.put(mimeType.toString(), mimeType);

                }
            }
        }
    }

    /**
     * Adds all mime types from the collection to the
     * @param mandatoryMimeTypes Mimetype needed
     * @throws IllegalArgumentException if the mandatoryMimeTypes contains
     * a mime type that is null.
     */
    private void addMandatoryTypes(Collection<MimeType> mandatoryMimeTypes,CallMediaType type) {
        for (MimeType mimeType : mandatoryMimeTypes) {
            if (mimeType == null) {
                throw new IllegalArgumentException(
                        "Mandatory mime type must not be null.");
            } else {
                if(type.equals(CallMediaType.AUDIO))
                {
                    mandatoryAudioMimeTypes.put(mimeType.toString(), mimeType);
                }
                else if(type.equals(CallMediaType.VIDEO))
                {
                    mandatoryVideoMimeTypes.put(mimeType.toString(), mimeType);
                }
            }
        }
    }


    private RTPPayload getRtpPayloads(SdpMediaDescription md, MimeType mimeType)throws SdpInternalErrorException {

        ArrayList<Integer> rtpPayloads =md.getSupportedRtpPayload(mimeType);
        if(!rtpPayloads.isEmpty())
        {
            Integer payloadType = rtpPayloads.get(0);//Get prefered one
            SdpRtpMap rtpMap =  md.getAttributes().getRtpMaps().get(payloadType);

            RTPPayload configuredPayload = RTPPayload.get(mimeType);
            if(configuredPayload==null) {
                throw new SdpInternalErrorException("MimeType "+mimeType.toString() +" is not configured in system configuration");
            }

            if (rtpMap==null) {
                if(payloadType<RTPPayload.RTP_PAYLOAD_TYPE_DYNAMIC_START) {
                    return configuredPayload;
                } else {
                    throw new SdpInternalErrorException("Unable to find "+mimeType.toString() + " in Sdp Media Description "+md);
                }
            }

            SdpBandwidth asBandwidth = md.getBandwidth("AS");
            int bandwidth = 0;
            if (asBandwidth != null) {
                bandwidth = asBandwidth.getValue();
            } else {
                bandwidth = configuredPayload.getBandwidth();
            }

            // Bandwidth is in kbps while the bitrate is in bps
            int bitrate = 0;
            bitrate = bandwidth * 1000;

            SdpBandwidth rsBandwidth = md.getBandwidth("RS");
            int rs = -1;
            if (rsBandwidth != null) {
                rs = rsBandwidth.getValue();
            } else {
                rs = configuredPayload.getBwSender();
            }

            SdpBandwidth rrBandwidth = md.getBandwidth("RR");
            int rr = -1;
            if (rrBandwidth != null) {
                rr= rrBandwidth.getValue();
            } else {
                rr = configuredPayload.getBwReceiver();
            }

            int channels = 1;
            if (rtpMap.getChannels() != null)
                channels = rtpMap.getChannels();

            SdpFmtp fmtp = null;
            if (md.getAttributes().getFmtps() != null)
                fmtp = md.getAttributes().getFmtps().get(rtpMap.getPayloadType());

            String formatParameters = null;
            if (fmtp != null)
                formatParameters = fmtp.getParameters();

            /**
             * RFC 4348 (VMR-WB RTP Payload Format), 9.3 Offer-Answer Model Considerations states:
             * The mode-set parameter is declarative, and only operating modes that have been indicated
             * to be supported by both ends SHALL be used.
             * 
             * RFC 3267 (Real-Time Transport Protocol for the Adaptive Multi-Rate (AMR)
             * and Adaptive Multi-Rate Wideband (AMR-WB) Audio Codecs)
             * does not precise how to handle mode-set.
             * 
             * Therefore, RFC 4348 is followed.
             */
            // Retrieve the localModeSet configuration
            String localModeSet = null;
            String localMediaFormatParameters = configuredPayload.getMediaFormatParameters();

            if (localMediaFormatParameters != null) {
                String[] mediaFormatParameters = localMediaFormatParameters.split(";");
                boolean remoteModeSetExists = false;

                // Loop through the configured mediaFormatParameters of this mimeType to find the mode-set (if configured)
                for (String mediaFormatParameter : mediaFormatParameters) {

                    // Find the mode-set in the local mediaFormatParameters
                    if (mediaFormatParameter.startsWith(SdpFmtp.MODE_SET)) {

                        // The mode-set answer is solely what is configured and not the intersection (RFC 4348)
                        localModeSet = mediaFormatParameter.substring(SdpFmtp.MODE_SET.length());

                        // Update the SDP answer by replacing the mode-set from the SDP offer value to the local configured value.
                        if (formatParameters != null) {
                            String[] params = formatParameters.split(";");

                            for (String param : params) {
                                // Find the mode-set in the remote mediaFormatParameters
                                if (param.startsWith(SdpFmtp.MODE_SET)) {
                                    int indexFirst = formatParameters.indexOf(SdpFmtp.MODE_SET);
                                    StringBuffer newFormatParameters = new StringBuffer(formatParameters);
                                    newFormatParameters.replace(indexFirst + SdpFmtp.MODE_SET.length(), indexFirst + param.length(), localModeSet);
                                    formatParameters = newFormatParameters.toString();
                                    remoteModeSetExists = true;
                                    break;
                                }
                            }

                            // If no mode-set is present in the remote fmtp, it must be prepended to the one defined
                            if (!remoteModeSetExists) {
                                formatParameters = SdpFmtp.MODE_SET + localModeSet + ";" + formatParameters;
                            }
                        } else {
                            // If no fmtp present at all, it must created to include a mode-set
                            formatParameters = SdpFmtp.MODE_SET + localModeSet;
                        }
                        break;
                    }
                }
            }

            RTPPayload rtpPayload = new RTPPayload(
                    rtpMap.getPayloadType(), mimeType,
                    rtpMap.getEncodingName(),
                    rtpMap.getClockRate(), channels, bitrate, formatParameters, rs, 0, 0, rr, 0, 0);

            return rtpPayload;
        } else {
            throw new SdpInternalErrorException("Unable to find "+mimeType.toString() + " in Sdp Media Description "+md);
        }
    }

}
