/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.stream.ConnectionProperties;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.masp.callmanager.sdp.attributes.SdpAttributes;
import com.mobeon.masp.callmanager.sdp.fields.*;
import com.mobeon.masp.callmanager.sessionestablishment.PreconditionStatusTable;
import com.mobeon.masp.callmanager.CallProperties.CallType;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.sdp.SdpException;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SessionDescription;
import com.mobeon.sdp.SdpParseException;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.activation.MimeType;

/**
 * This session description factory is used to parse a received SDP and return
 * a parsed {@link SdpSessionDescription} or to create an SDP offer or answer
 * (according to RFC 3264) to use in media negotiation over SIP.
 *
 * @author Malin Nyfeldt
 */
public class SdpSessionDescriptionFactory {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private AtomicReference<SdpFactory> sdpFactory =
            new AtomicReference<SdpFactory>();

    /**
     * Initializes the Session Description factory.
     * The SDP stack implementation is initialized.
     * @throws SdpInternalErrorException if the SDP stack could not be initialized.
     */
    public void init() throws SdpInternalErrorException {
        try {
            SdpFactory.setPathName("gov.nist");
            sdpFactory.set(SdpFactory.getInstance());
        } catch (SdpException e) {
            throw new SdpInternalErrorException(
                    "Could not initialize SDP factory.", e);
        }
    }

    /**
     * This method is used to parse a remote SDP and returns a parsed
     * {@link SdpSessionDescription}.
     * <p>
     * First the stack representation of the SDP is created and then it is
     * parsed to create the parsed session description.
     * <p>
     * If the stack representation could not be created or if the sdp could
     * not be parsed an {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.SD_PARAMETER_NOT_UNDERSTOOD}.
     *
     * @param   sdp
     * @return  A parsed representation of the sdp.
     * @throws  SdpNotSupportedException if the sdp could not be parsed.
     */
    public SdpSessionDescription parseRemoteSdp(String sdp)
            throws SdpNotSupportedException {

        SdpSessionDescription sd = null;

        if (log.isDebugEnabled())
            log.debug("Parsing remote SDP: " + sdp);

        if (sdp != null) {

            SessionDescription stackSD;
            try {
                stackSD = sdpFactory.get().createSessionDescription(sdp);
            } catch (SdpParseException e) {
                String message =
                        "Could not parse remote SDP. The call will not be setup.";
                log.warn(message, e);
                throw new SdpNotSupportedException(
                        SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, message);
            }

            sd = SdpSessionDescriptionImpl.parseSessionDescription(stackSD);

            if (sd == null) {
                String message = "Could not retrieve Session Description " +
                        "from remote SDP. The call will not be setup.";
                log.warn(message);
                throw new SdpNotSupportedException(
                        SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, message);
            }

        }

        if (log.isDebugEnabled())
            log.debug("Parsed session description: " + sd);

        return sd;
    }


    /**
     * This method is used to create an SDP answer based on an SDP intersection
     * found during media negotiation and the media types and connection
     * properties to use for the inbound media streams.
     *
     * @param   sdpIntersection         The media intersection found between the
     *                                  SDP offer and requirements for the
     *                                  current call. MUST NOT be null.
     * @param   audioMimeType           Audio encoding for inbound stream.
     *                                  MUST NOT be null.
     * @param   videoMimeType           Video encoding for inbound stream.
     *                                  MUST NOT be null. Only used for video
     *                                  calls, ignored otherwise.
     * @param   connectionProperties    Connection properties to use for the
     *                                  inbound streams. MUST NOT be null.
     * @param   origin                  User to set in origin field of SDP.
     *                                  MUST NOT be null.
     * @param   localStatusTable        The precondition local status table.
     *                                  MAY be null if precondition are not applied.
     * @return  An SDP answer.
     * @throws  NullPointerException    if any parameter is null.
     * @throws  SdpInternalErrorException
     *          if <param>audioMimeType</param> or <param>videoMimeType</param>
     *          is an unsupported MIME type.
     */
    public String createSdpAnswer(SdpIntersection sdpIntersection,
                                  MimeType audioMimeType,
                                  MimeType videoMimeType,
                                  ConnectionProperties connectionProperties,
                                  String origin,
                                  PreconditionStatusTable localStatusTable)
            throws NullPointerException, SdpInternalErrorException {

        if ((sdpIntersection == null) || (audioMimeType == null) ||
                (videoMimeType == null) || (connectionProperties == null) ||
                (origin == null))
            throw new NullPointerException("Could not create SDP answer. " +
                    "Required parameter is null. <SdpIntersection = " +
                    sdpIntersection + ">, <Audio Mime Type = " + audioMimeType +
                    ">, <Video Mime Type = " + videoMimeType +
                    ">, <Connection Properties = " + connectionProperties +
                    ">, <Origin = " + origin + ">");

        SdpSessionDescriptionImpl localSD = new SdpSessionDescriptionImpl();

        // Create connection
        SdpConnection remoteConnection = sdpIntersection.getSessionDescription().getConnection();
        String host;
        
        if ((remoteConnection != null) && 
                (remoteConnection.isConnectionAddressZero())) {
            host = SdpConnection.HOLD_IP_ADDRESS;

        } else {
            host = connectionProperties.getAudioHost();
        }

        localSD.setConnection(new SdpConnection(host));

        // Create origin
        localSD.setOrigin(new SdpOrigin(
                origin, 0, 0, connectionProperties.getAudioHost()));

        // Create attributes
        localSD.setAttributes(new SdpAttributes());

        // Create media descriptions
        localSD.setMediaDescriptions(
                createMediaDescriptionsFromIntersection(
                        sdpIntersection, audioMimeType, videoMimeType,
                        connectionProperties, localSD, localStatusTable));

        return localSD.encodeToSdp(sdpFactory.get());
    }


    /**
     * This method is used to create an SDP offer based on the call type and
     * the media types and connection properties to use for the inbound media
     * streams.
     * @param callType                  Call type for which to create an offer.
     *                                  MUST NOT be null.
     * @param   audioMimeType           Audio encoding for inbound stream.
     *                                  MUST NOT be null.
     * @param   videoMimeType           Video encoding for inbound stream.
     *                                  MUST NOT be null for video calls.
     * @param   connectionProperties    Connection properties to use for the
     *                                  inbound streams. MUST NOT be null.
     * @param   origin                  User to set in origin field of SDP.
     *                                  MUST NOT be null.
     * @return  An SDP offer.
     * @throws  NullPointerException    if a parameter that must not be null
     *                                  is null anyway.
     * @throws  SdpInternalErrorException
     *          if <param>audioMimeType</param> or <param>videoMimeType</param>
     *          is an unsupported MIME type.
     */
    public String createSdpOffer(CallType callType,
                                 MimeType audioMimeType,
                                 MimeType videoMimeType,
                                 ConnectionProperties connectionProperties,
                                 String origin)
            throws NullPointerException, SdpInternalErrorException {

        if ((callType == null) || (audioMimeType == null) ||
                (connectionProperties == null) || (origin == null))
            throw new NullPointerException("Could not create SDP offer. " +
                    "Required parameter is null. " +
                    "<Call Type = " + callType +
                    ">, <Audio Mime Type = " + audioMimeType +
                    ">, <Connection Properties = " + connectionProperties +
                    ">, <Origin = " + origin + ">");

        if ((callType == CallType.VIDEO) && (videoMimeType == null))
            throw new NullPointerException("Could not create SDP offer. " +
                    "Required video mime type is null.");

        SdpSessionDescriptionImpl localSD = new SdpSessionDescriptionImpl();

        // Create connection
        localSD.setConnection(
                new SdpConnection(connectionProperties.getAudioHost()));

        // Create origin
        localSD.setOrigin(new SdpOrigin(
                origin, 0, 0, connectionProperties.getAudioHost()));

        // Create attributes
        localSD.setAttributes(new SdpAttributes());

        // Create media descriptions
        List<SdpMediaDescription> localMDs =
                new ArrayList<SdpMediaDescription>();
        localMDs.add(createMediaDescription(
                SdpMediaType.AUDIO, audioMimeType,
                connectionProperties, localSD, null, null, null, null, null));

        if (callType == CallType.VIDEO)
            localMDs.add(createMediaDescription(
                    SdpMediaType.VIDEO, videoMimeType,
                    connectionProperties, localSD, null, null, null, null, null));

        localSD.setMediaDescriptions(localMDs);

        return localSD.encodeToSdp(sdpFactory.get());
    }


    //=========================== Private methods =========================

    /**
     * Creates a media description with the given <param>mediaType</param>,
     * encoding (<param>mimeType</param>), <param>connectionProperties</param>.
     * <p>
     * These parameters MUST NOT be null. Remote Media and attributes were added
     * for being able to answer/offer RTCP feedback (RTP/AVPF) SDP requests
     *
     * @param   mediaType                   Media type is audio or video.
     * @param   mimeType                    Encoding to use for media.
     * @param   inboundConnectionProperties Rtp connection properties.
     * @param   sd                          Session description to which the
     *                                      created media description will belong.
     * @param remoteMedia                   Remote SdpMedia, might be null
     * @param remoteAttributes              Remove media attributes, might be null
     * @param localStatusTable              The precondition local status table, might be null
     * @return                              An {@link SdpMediaDescription}.
     * @throws  SdpInternalErrorException   if <param>mimeType</param> is an
     *                                      unsupported MIME type.
     */
    private SdpMediaDescription createMediaDescription(
            SdpMediaType mediaType,
            MimeType mimeType,
            ConnectionProperties inboundConnectionProperties,
            SdpSessionDescription sd,
            Collection<RTPPayload> supportedPayloads,
            SdpMedia remoteMedia,
            SdpAttributes remoteAttributes, 
            SdpConnection remoteConnection,
            PreconditionStatusTable localStatusTable)
            throws SdpInternalErrorException {
        SdpMediaDescription localMD;

        RTPPayload rtpPayload = null;
        RTPPayload dtmfPayload = null;

        SdpMediaTransport remoteMediaTransport = null;
        if (remoteMedia != null) {
            remoteMediaTransport = remoteMedia.getTransport();
        }

        // Check for dynamic payload type
        if (supportedPayloads != null) {
            for (RTPPayload dynamicPayload : supportedPayloads) {
                if (dynamicPayload.getMimeType().match(mimeType)) {
                    rtpPayload = dynamicPayload;
                    break;
                }
            }
        }

        // Check for dynamic dtmf payload
        if (supportedPayloads != null) {
            for (RTPPayload dynamicPayload : supportedPayloads) {
                if (dynamicPayload.getMimeType().match(RTPPayload.AUDIO_DTMF)) {
                    dtmfPayload = dynamicPayload;
                    break;
                }
            }
        }



        // Use static payload type
        if (rtpPayload == null) {
            rtpPayload = RTPPayload.get(mimeType);
        }

        if (rtpPayload != null) {

            String host;
            int port;
            Integer pTime = null;
            Integer maxPTime = null;
            if (mediaType == SdpMediaType.AUDIO) {
                host = inboundConnectionProperties.getAudioHost();
                port = inboundConnectionProperties.getAudioPort();
                if (dtmfPayload==null) {
                    dtmfPayload = RTPPayload.get(RTPPayload.AUDIO_DTMF);
                }
                pTime = inboundConnectionProperties.getPTime();
                maxPTime = inboundConnectionProperties.getMaxPTime();
            } else {
                host = inboundConnectionProperties.getVideoHost();
                port = inboundConnectionProperties.getVideoPort();
            }
            
            if ((remoteConnection != null) && 
                    (remoteConnection.isConnectionAddressZero())) {
                host = SdpConnection.HOLD_IP_ADDRESS;
            }

            localMD = SdpMediaDescriptionImpl.createMediaDescription(
                    mediaType, rtpPayload, dtmfPayload, host, port,
                    pTime, maxPTime, sd, remoteMediaTransport, remoteAttributes, localStatusTable);

        } else {
            throw new SdpInternalErrorException(
                    "RTPPayload was not found for: " + mimeType);
        }

        return localMD;
    }


    /**
     * This method is used to create the media descriptions for an SDP answer
     * based on an SDP intersection found during media negotiation and the
     * media types and connection properties to use for the inbound media streams.
     * <p>
     * The parameters MUST NOT be null.
     *
     * @param   sdpIntersection         The media intersection found between the
     *                                  SDP offer and requirements for the
     *                                  current call. MUST NOT be null.
     * @param   audioMimeType           Audio encoding for inbound stream.
     *                                  MUST NOT be null.
     * @param   videoMimeType           Video encoding for inbound stream.
     *                                  MUST NOT be null. Only used for video
     *                                  calls, ignored otherwise.
     * @param   connectionProperties    Connection properties to use for the
     *                                  inbound streams. MUST NOT be null.
     * @param   sd                      Session description to which the
     *                                  created media description will belong.
     * @param   localStatusTable        The precondition local status table.
     *                                  MAY be null if precondition are not applied.
     * @return                          An {@link SdpMediaDescription}.
     * @throws  SdpInternalErrorException
     *          if <param>audioMimeType</param> or <param>videoMimeType</param>
     *          is an unsupported MIME type.
     */
    private List<SdpMediaDescription> createMediaDescriptionsFromIntersection(
            SdpIntersection sdpIntersection,
            MimeType audioMimeType, MimeType videoMimeType,
            ConnectionProperties connectionProperties,
            SdpSessionDescriptionImpl sd,
            PreconditionStatusTable localStatusTable)
            throws SdpInternalErrorException {

        List<SdpMediaDescription> localMDs = new ArrayList<SdpMediaDescription>();

        List<SdpMediaDescription> remoteMDs =
                sdpIntersection.getSessionDescription().getMediaDescriptions();

        Integer audioIndex = sdpIntersection.getAudioIndex();
        Integer videoIndex = sdpIntersection.getVideoIndex();
        Integer videoFeedbackIndex = sdpIntersection.getVideoFeedbackIndex();

        for (int i = 0; i < remoteMDs.size(); i++) {

            SdpMediaDescription remoteMD = remoteMDs.get(i);

            SdpMediaDescription localMD;

            if ((audioIndex != null) && (i == audioIndex))
                localMD = createMediaDescription(
                        SdpMediaType.AUDIO, audioMimeType,
                        connectionProperties, sd,
                        sdpIntersection.getSupportedRtpPayloads(),
                        remoteMD.getMedia(),
                        remoteMD.getAttributes(), 
                        remoteMD.getConnection(),
                        localStatusTable);

            else if ((videoIndex != null) && (i == videoIndex)) {
                localMD = createMediaDescription(
                        SdpMediaType.VIDEO, videoMimeType,
                        connectionProperties, sd,
                        sdpIntersection.getSupportedRtpPayloads(),
                        remoteMD.getMedia(),
                        remoteMD.getAttributes(), 
                        remoteMD.getConnection(),
                        localStatusTable);
            }
            else if ((videoFeedbackIndex != null) && (i == videoFeedbackIndex)) {
                localMD = createMediaDescription(
                        SdpMediaType.VIDEO, videoMimeType,
                        connectionProperties, sd,
                        sdpIntersection.getSupportedRtpPayloads(),
                        remoteMD.getMedia(),
                        remoteMD.getAttributes(), 
                        remoteMD.getConnection(),
                        localStatusTable);
            }
            else
                localMD = remoteMD.createUnusedMediaDescriptionCopy(sd);

            localMDs.add(localMD);
        }
        return localMDs;
    }

}

