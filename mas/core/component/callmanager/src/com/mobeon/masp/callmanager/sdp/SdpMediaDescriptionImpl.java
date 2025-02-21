/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.sdp.*;

import jakarta.activation.MimeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import com.mobeon.masp.callmanager.sdp.attributes.*;
import com.mobeon.masp.callmanager.sdp.fields.SdpMedia;
import com.mobeon.masp.callmanager.sdp.fields.SdpConnection;
import com.mobeon.masp.callmanager.sdp.fields.SdpMediaType;
import com.mobeon.masp.callmanager.sdp.fields.SdpMediaTransport;
import com.mobeon.masp.callmanager.sdp.fields.SdpMediaFormats;
import com.mobeon.masp.callmanager.sdp.fields.SdpBandwidth;
import com.mobeon.masp.callmanager.sessionestablishment.PreconditionStatusTable;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.masp.stream.RTPPayload.RtpValidationResult;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * This class represents the content of a Media Description of an SDP.
 * <p>
 * This class is immutable.
 *
 * @author Malin Nyfeldt
 */
public class SdpMediaDescriptionImpl implements SdpMediaDescription {

    private static final ILogger LOG =
            ILoggerFactory.getILogger(SdpMediaDescriptionImpl.class);

    private final SdpMedia media;
    private final SdpConnection connection;
    private final HashMap<String, SdpBandwidth> bandwidths;
    private final SdpAttributes attributes;
    private final SdpAttributes mergedAttributes;
    private final SdpSessionDescription sessionDescription;

    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();

    /**
     * Creates a media description with the parts:
     * with a <param>media</param> field,
     * a <param>connection</param> field, a <param>bandwidth</param> field
     * and <param>attributes</param>.
     * <p>
     * The <param>sessionDescription</param> to which this media description
     * belongs is also given. This session description is used to retrieve
     * the session level values of the connection field, bandwidth field and
     * attributes.
     *
     * @param   media                   MUST NOT be null.
     * @param   connection
     * @param   bandwidths              MUST NOT be null.
     * @param   attributes              MUST NOT be null.
     * @param   sessionDescription      MUST NOT be null.
     * @throws  NullPointerException    If a parameter is null that should not be.
     */
    public SdpMediaDescriptionImpl(
            SdpMedia media,
            SdpConnection connection,
            HashMap<String, SdpBandwidth> bandwidths,
            SdpAttributes attributes,
            SdpSessionDescription sessionDescription) throws NullPointerException {

        if ((media == null) || (bandwidths == null) || (attributes == null) ||
                (sessionDescription == null))
            throw new NullPointerException("Media description cannot be " +
                    "created with null parameter. " +
                    "<Media = " + media +
                    ">, <Bandwidths = " + bandwidths +
                    ">, <Attributes = " + attributes +
                    ">, <Session Description = " + sessionDescription + ">");

        this.media = media;
        this.connection = connection;
        this.bandwidths = bandwidths;
        this.attributes = attributes;
        this.mergedAttributes = SdpAttributes.mergeAttributes(
                sessionDescription.getAttributes(), attributes);
        this.sessionDescription = sessionDescription;
    }

    public SdpMedia getMedia() {
        return media;
    }

    public SdpConnection getMediaConnection() {
        return connection;
    }

    public SdpConnection getConnection() {
        SdpConnection result = connection;

        if (result == null)
            result = sessionDescription.getConnection();

        return result;
    }

    public SdpBandwidth getBandwidth(String type) {
        SdpBandwidth result = null;
        if (bandwidths != null)
            result = bandwidths.get(type);

        if (result == null)
            result = sessionDescription.getBandwidth(type);

        return result;
    }

    public HashMap<String, SdpBandwidth> getBandwidths() {
        return bandwidths;
    }

    public SdpAttributes getMediaAttributes() {
        return attributes;
    }

    public SdpAttributes getAttributes() {
        return mergedAttributes;
    }

    public String toString() {
        String representation = stringRepresentation.get();

        if (representation == null) {
            representation = "Media Description: <Media = " + media +
                    ">, <Connection = " + connection +
                    ">, <Bandwidths = " + bandwidths.values() +
                    ">, <Attributes = " + attributes + ">";
            stringRepresentation.set(representation);
        }

        return representation;
    }

    public boolean compareWith(SdpMediaDescription sdpMediaDescription, boolean excludeTransmissionMode) {
        // Following the constructor implementation, only the connection member can be null
        boolean mediaResult = media.toString().equals(sdpMediaDescription.getMedia().toString());
        boolean connectionResult = true;
        boolean bandwidthsResult = bandwidths.values().toString().equals(sdpMediaDescription.getBandwidths().values().toString());
        boolean attributesResult = attributes.compareWith(sdpMediaDescription.getMediaAttributes(), excludeTransmissionMode);

        if (connection != null && sdpMediaDescription.getMediaConnection() != null) {
            connectionResult = connection.toString().equals(sdpMediaDescription.getMediaConnection().toString());
        } else if (connection != null || sdpMediaDescription.getMediaConnection() != null) {
            connectionResult = false;
        }

        return mediaResult && connectionResult && bandwidthsResult && attributesResult;
    }

//=========================== Package private Methods =========================

    /**
     * Creates a media description.
     * The <param>type</param> gives the media type of the media field.
     * {@link SdpMediaType.AUDIO} or {@link SdpMediaType.VIDEO} are possible
     * values.
     * <p>
     * The <param>mediaPayload</param> gives the media format to include in the
     * media field and in one rtpmap attribute.
     * <p>
     * The <param>dtmfPayload</param> (if not null) gives the media format
     * for DTMF to include in the media field and in one rtpmap attribute.
     * If this parameter is not null, the format specific attributes for dtmf
     * is also set in an fmtp attributes.
     * <p>
     * The <param>host</param> is used in the Connection field.
     * <p>
     * The <param>port</param> is used in the Media field.
     * <p>
     * The <param>pTime</param> if not null is added to the list of attributes.
     * <p>
     * The <param>maxPTime</param> if not null is added to the list of attributes.
     * <p>
     * The <param>sessionDescription</param> to which this media description
     * belongs is also given. This session description is used to retrieve
     * the session level values of the connection field and attributes.
     *
     * <param>remoteMediaTransport</param> is used for adding RTP/AVPF support, in
     * addition to RTP/AVP, and
     * <param>remoteAttributes</param> are needed for specific RTP/AVPF attributes
     *
     * @param   type                    MUST NOT be null.
     * @param   mediaPayload            MUST NOT be null.
     * @param   dtmfPayload
     * @param   host                    MUST NOT be null.
     * @param   port
     * @param   pTime
     * @param   maxPTime
     * @param   sessionDescription      MUST NOT be null.
     * @param   remoteMediaTransport    Remote requested media transport, MAY be null
     * @param   remoteAttributes        Remote attributes, MAY be null
     * @param   localStatusTable        The precondition local status table, MAY be null
     * @return  A new media description.
     * @throws  NullPointerException    if any parameter that must not be null
     *                                  is null anyway.
     */
    static SdpMediaDescription createMediaDescription(
            SdpMediaType type,
            RTPPayload mediaPayload,
            RTPPayload dtmfPayload,
            String host, int port,
            Integer pTime,
            Integer maxPTime,
            SdpSessionDescription sessionDescription,
            SdpMediaTransport remoteMediaTransport,
            SdpAttributes remoteAttributes,
            PreconditionStatusTable localStatusTable) throws NullPointerException {

        if ((type == null) || (mediaPayload == null) || (host == null) ||
                (sessionDescription == null))
            throw new NullPointerException("Media description cannot be " +
                    "created with null parameter. " +
                    "<Type = " + type + ">, <Media Payload = " + mediaPayload +
                    ">, <Host = " + host +
                    ">, <Session Description = " + sessionDescription + ">");

        // Create Connection field
        SdpConnection sdpConnection = new SdpConnection(host);

        // Use the remote media transport, or RTP_AVP as default
        SdpMediaTransport mediaTransport = null;
        if (remoteMediaTransport == null) {
                mediaTransport = SdpMediaTransport.RTP_AVP;
        }
        else {
            mediaTransport = remoteMediaTransport;
        }

        // Create Media field
        Vector<Integer> formats = new Vector<Integer>();
        formats.add(mediaPayload.getPayloadType());
        if (dtmfPayload != null) formats.add(dtmfPayload.getPayloadType());
        SdpMedia sdpMedia = new SdpMedia(
                type, port, 0,
                mediaTransport,
                new SdpMediaFormats(formats));

        // Create Bandwidth field
        HashMap<String, SdpBandwidth> bandwidths =
                new HashMap<String, SdpBandwidth>();

        // Retrieve the bandwidth information from the configured RTP payload
        // information.
        //RTPPayload rtpPayload = RTPPayload.get(mediaPayload.getMimeType());
        RTPPayload rtpPayload = mediaPayload;
        if (rtpPayload != null) {
            int bandwidth = rtpPayload.getBandwidth();
            if (bandwidth > 0) {
                bandwidths.put(SdpConstants.BW_TYPE_AS,
                        new SdpBandwidth(SdpConstants.BW_TYPE_AS, bandwidth));
            }
            int rr = rtpPayload.getBwReceiver();
            if( rr >= 0 ) {
                bandwidths.put(
                        SdpConstants.BW_TYPE_RR,
                        new SdpBandwidth(SdpConstants.BW_TYPE_RR, rr));
            }
            int rs = rtpPayload.getBwSender();
            if( rs >= 0 ) {
                bandwidths.put(
                        SdpConstants.BW_TYPE_RS,
                        new SdpBandwidth(SdpConstants.BW_TYPE_RS, rs));
            }
        }


        // Create Attributes
        SdpAttributes attributes = new SdpAttributes();

        if (pTime != null) attributes.setPTime(new SdpPTime(pTime));
        if (maxPTime != null) attributes.setMaxPTime(new SdpMaxPTime(maxPTime));

        attributes.addRtpMap(new SdpRtpMap(
                mediaPayload.getPayloadType(), mediaPayload.getEncoding(),
                mediaPayload.getClockRate(), mediaPayload.getChannels()));

        // If there are format specific parameters, add them to the fmtp attribute
        if (mediaPayload.getMediaFormatParameters() != null &&
        	mediaPayload.getMediaFormatParameters().length() > 0) {
            attributes.addFmtp(new SdpFmtp(mediaPayload.getPayloadType(),
        	    mediaPayload.getMediaFormatParameters()));
        }

        if (dtmfPayload != null) {
            attributes.addRtpMap(new SdpRtpMap(
                    dtmfPayload.getPayloadType(), dtmfPayload.getEncoding(),
                    dtmfPayload.getClockRate(), dtmfPayload.getChannels()));
            attributes.addFmtp(new SdpFmtp(
                    dtmfPayload.getPayloadType(), SdpConstants.FMTP_DTMF));
        }

        // Only the "a=rtcp-fb: <fmt> ccm fir" RTCP feedback is supported
        if (mediaTransport.equals(SdpMediaTransport.RTP_AVPF) && remoteAttributes != null) {
            for (SdpRTCPFeedback value : remoteAttributes.getRTCPFeedback()) {
                String rtcbFbType = value.getParameters();
                if (rtcbFbType.equalsIgnoreCase(SdpConstants.RTCP_FB_FIR)) {
                    attributes.addRTCPFeedback(value);
                }
            }
        }

        // Add preconditions 
        if(localStatusTable != null && localStatusTable.isPreconditionRequire()) {
            attributes.addCurrentPrecondition(localStatusTable.getLocalCurrentStatus());
            attributes.addCurrentPrecondition(localStatusTable.getRemoteCurrentStatus());
            attributes.addDesiredPrecondition(localStatusTable.getLocalDesiredStatus());
            attributes.addDesiredPrecondition(localStatusTable.getRemoteDesiredStatus());
            
            if( localStatusTable.getRemoteCurrentStatus().getDirectionTag() != SdpPrecondition.DirectionTag.SENDRECV ) {
                attributes.addConfirmPrecondition(
                        new SdpPreconditionConf(SdpPrecondition.PRECONDITION_TYPE_QOS, SdpPrecondition.StatusType.REMOTE, SdpPrecondition.DirectionTag.SENDRECV));
            }
        }

        // Add unicast (transmission mode) if incoming value is 'inactive'
        if (remoteAttributes != null && remoteAttributes.getTransmissionMode() == SdpTransmissionMode.INACTIVE) {
            attributes.setTransmissionMode(SdpTransmissionMode.INACTIVE);
        }

        return new SdpMediaDescriptionImpl(sdpMedia, sdpConnection, bandwidths, attributes, sessionDescription);
    }


    /**
     * This method parses a media description from an SDP in stack format (i.e.
     * <param>stackMD</param>) and returns a parsed representation as an
     * <@link SdpMediaDescription>.
     * <p>
     * The media field of a media description is parsed using
     * {@link #parseMedia(com.mobeon.sdp.Media)}.
     * The connection field of a media description is parsed using
     * {@link SdpConnection#parseConnection(com.mobeon.sdp.Connection)}.
     * The bandwidth fields of a media description are parsed using
     * {@link SdpBandwidth#parseBandwidth(java.util.Vector<com.mobeon.sdp.BandWidth>)}.
     * The attributes of a media description are parsed using
     * {@link SdpAttributes#parseAttributes(Vector<com.mobeon.sdp.Attribute>)}.
     * <p>
     * The parsed session description (i.e. <param>parsedSd</param>)
     * to which this media description belongs is also given.
     * This session description is used to retrieve
     * the session level values of the connection field and attributes.
     * <p>
     * If neither the parsed media description nor the parsed session description
     * contained a connection field an {@link SdpNotSupportedException} is
     * thrown with {@link SipWarning.SD_PARAMETER_NOT_UNDERSTOOD}.
     *
     * @param  mediaDescription
     * @param  parsedSd         MUST NOT be null.
     * @return                  A parsed SdpMediaDescription.
     *                          Null is returned if
     *                          <param>mediaDescription</param> is null.
     * @throws SdpNotSupportedException
     *                          If the media description is invalid as described
     *                          in the methods (listed above) used to parse
     *                          the media description or if no connection field
     *                          was found.
     * @throws NullPointerException
     *                          If the session description is null.
     */
    static SdpMediaDescription parseMediaDescription(
            MediaDescription mediaDescription, SdpSessionDescription parsedSd)
            throws SdpNotSupportedException, NullPointerException {
        SdpMediaDescriptionImpl sdpMd = null;

        if (parsedSd == null)
            throw new NullPointerException("Media description cannot be " +
                    "parsed with null session description.");

        if (mediaDescription != null) {
            // Parse Media field
            SdpMedia media = parseMedia(mediaDescription.getMedia());

            // Parse Connection field
            SdpConnection sdpConnection =
                    SdpConnection.parseConnection(mediaDescription.getConnection());

            // Parse Bandwidth fields
            HashMap<String, SdpBandwidth> bandwidths =
                    SdpBandwidth.parseBandwidth(
                            mediaDescription.getBandwidths(true));

            // Parse Attributes field
            SdpAttributes attributes =
                    SdpAttributes.parseAttributes(
                            mediaDescription.getAttributes(false));

            // Verify that Media Description is not encrypted
            assertNoEncryption(mediaDescription);

            sdpMd = new SdpMediaDescriptionImpl(
                    media, sdpConnection, bandwidths, attributes, parsedSd);

            if (sdpMd.getConnection() == null) {
                String message = "No media level or session level connection " +
                        "is given in remote SDP. The call will not be setup.";
                LOG.warn(message);
                throw new SdpNotSupportedException(
                        SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, message);
            }
        }

        return sdpMd;
    }

    //====================== SdpMediaDescription Methods ====================

    public SdpMediaDescription createUnusedMediaDescriptionCopy(
            SdpSessionDescription sessionDescription)
            throws NullPointerException {

        if (sessionDescription == null)
            throw new NullPointerException("Unused Media description cannot be " +
                    "created with null session description.");

        SdpMedia sdpMedia = new SdpMedia(
                media.getType(), 0,  media.getPortCount(),
                media.getTransport(), media.getFormats());

        return new SdpMediaDescriptionImpl(
                sdpMedia, connection, bandwidths, attributes, sessionDescription);
    }

    public MediaDescription encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException {

        // Encode Media field
        Vector<Integer> mediaFormats = getMedia().getFormats().getFormats();
        int[] intFormats = new int[mediaFormats.size()];
        int i = 0;
        for (Integer format : mediaFormats) {
            intFormats[i++] = format;
        }

        MediaDescription stackMD = sdpFactory.createMediaDescription(
                getMedia().getType().toString(),
                getMedia().getPort(),
                getMedia().getPortCount(),
                getMedia().getTransport().toString(),
                intFormats);


        // Encode Connection field
        if (connection != null)
            stackMD.setConnection(connection.encodeToStackFormat(sdpFactory));

        // Encode Bandwidth fields
        for (SdpBandwidth b : bandwidths.values()) {
            stackMD.setBandwidth(b.getType(), b.getValue());
        }

        // Encode Attribute fields
        stackMD.setAttributes(attributes.encodeToStackFormat(sdpFactory));

        return stackMD;
    }

    public boolean areEncodingsSupported(Collection<MimeType> mimeTypes) {
        boolean supported = true;

        for (MimeType mimeType : mimeTypes) {
            // First check if the encoding is supported
        	ArrayList<Integer> supportedPayloads=getSupportedRtpPayload(mimeType);
            if (supportedPayloads.isEmpty()) {
                supported = false;
                if (LOG.isDebugEnabled())
                    LOG.debug("Mime Type <" + mimeType.toString() +
                            "> is not supported by Media Description: " + this);
                break;
            }
//            boolean clockRateSupported=false;
//            Iterator<Integer> payLoadIter = supportedPayloads.iterator();
//			while ( payLoadIter.hasNext() ) {
//				//FIXME move this down stack.
//				SdpRtpMap map = getAttributes().getRtpMaps().get(payLoadIter.next()); 
//                int payLoadClockRate = map.getClockRate();    
//                if(!isClockRateSupported(mimeType,payLoadClockRate)) {
//                	payLoadIter.remove(); //prune any not supported.
//                } else {
//                	clockRateSupported=true; //supported by one at least
//                }
//            }
//			
//            if(!clockRateSupported) {
//            	supported = false;
//                LOG.debug("Mime Type <" + mimeType.toString() +
//                        "> is not supported by Media Description " +
//                        "due to Clock Rate/Sample Rate difference: " + this);
//                break;
//            }

            // Check if the bandwidth value (if specified) is sufficient
            // for the encoding
            if (!isBandwidthSupported(mimeType)) {
                supported = false;
                if (LOG.isDebugEnabled())
                    LOG.debug("Mime Type <" + mimeType.toString() +
                            "> is not supported by Media Description " +
                            "due to bandwidth settings. MD: " + this);
                break;
            }            

        }
        return supported;
    }

    private boolean isClockRateSupported(MimeType mimeType,int clockRate) {
    	boolean supported = true;

        RTPPayload rtpPayload = RTPPayload.get(mimeType);
            if (rtpPayload == null) {
                supported = false;
                if (LOG.isDebugEnabled()) LOG.debug(
                        "Could not retrieve local bandwidth information " +
                                "for mimetype: " + mimeType);
                return supported;
            }
        supported = (rtpPayload.getClockRate() == clockRate);
        return supported;
    }

	public ArrayList<Integer> getSupportedRtpPayload(MimeType mimeType) {
        // For Dynamic mapping, we are trying to find the MIME type in the SDP rtpmap
        // Note: this could assign also static rtp payload if it has an rtpmap associated
           ArrayList<Integer> supportedPaytloadTypes = new ArrayList<Integer>(); 
           getSupportedRtpPayloadByDynamicMapping(mimeType,supportedPaytloadTypes);

        // TODO: Static mapping should only take care of rtp payload less than 96. We are mixing concepts of dynamic RTP payload and payload type configured in the system
        // For static mapping it is not mandatory to have an rtp map associated with the payload type.
        // In this case we are take the payload type from our configuration.
        if (supportedPaytloadTypes.isEmpty())
            getSupportedRtpPayloadByStaticMapping(mimeType,supportedPaytloadTypes);

        return supportedPaytloadTypes;
    }

    public boolean isSdpMediaDescriptionOnHold() {

        boolean onHold = false;
        SdpConnection sdpConn = this.getConnection();

        if (sdpConn != null) {
            onHold = sdpConn.isConnectionAddressZero();
        }

        return onHold;
    }

    //=========================== Private Methods =========================

    /**
     * Returns whether the bandwidth for the <param>mimeType</param> is
     * supported by the bandwidth in the media description.
     * <p>
     * The following rules applies:
     * <ul>
     * <li>
     * If the "AS" bandwidth information is not set in the media description,
     * the bandwidth of the <param>mimeType</param> is automatically supported.
     * </li>
     * <li>
     * If the "AS" bandwidth is set in the media description (or in the session
     * description), the bandwidth of the <param>mimeType</param> is supported
     * if it is smaller than or equal to the bandwidth of the media description.
     * </li>
     * </ul>
     * @param   mimeType
     * @return  Returns if the bandwidth for the <param>mimeType</param>
     *          is supported or not according to the rules described above.
     */
    protected boolean isBandwidthSupported(MimeType mimeType) {
        boolean supported = true;

        RTPPayload rtpPayload = RTPPayload.get(mimeType);
            if (rtpPayload == null) {
                supported = false;
                if (LOG.isDebugEnabled()) LOG.debug(
                        "Could not retrieve local bandwidth information " +
                                "for mimetype: " + mimeType);
                return supported;
            }

        SdpBandwidth mdBandwidth = getBandwidth(SdpConstants.BW_TYPE_AS);
        if (mdBandwidth != null) {
            if (rtpPayload.getBandwidth() > mdBandwidth.getValue()) {
                supported = false;
                if (LOG.isDebugEnabled()) LOG.debug(
                        "Media description bandwidth (" + mdBandwidth.getValue() +
                                ") is NOT sufficient for bandwidth " +
                                "requirements (" + rtpPayload.getBandwidth() +
                                ") of mimetype=" + mimeType);
            } else {
                if (LOG.isDebugEnabled()) LOG.debug(
                        "Media description bandwidth (" + mdBandwidth.getValue() +
                                ") is sufficient for bandwidth " +
                                "requirements (" + rtpPayload.getBandwidth() +
                                ") of mimetype=" + mimeType);
            }
        }
        SdpBandwidth rrBandwidth = getBandwidth(SdpConstants.BW_TYPE_RR);
        if (rrBandwidth != null &&
                (rtpPayload.getMaxReceiver() != -1 ||  rtpPayload.getMinReceiver() != -1)) {
            if (rtpPayload.getMaxReceiver() < rrBandwidth.getValue() ||
                    rtpPayload.getMinReceiver() > rrBandwidth.getValue()) {
                supported = false;
                if (LOG.isDebugEnabled()) LOG.debug(
                        "Media description RR bandwidth (" + rrBandwidth.getValue() +
                                ") is NOT sufficient for RR bandwidth " +
                                "requirements (" + rtpPayload.getMinReceiver() +
                                "-" + rtpPayload.getMaxReceiver() +
                                ") of mimetype=" + mimeType);
            } else {
                if (LOG.isDebugEnabled()) LOG.debug(
                        "Media description RR bandwidth (" + rrBandwidth.getValue() +
                                ") is sufficient for RR bandwidth " +
                                "requirements (" + rtpPayload.getMinReceiver() +
                                "-" + rtpPayload.getMaxReceiver() +
                                ") of mimetype=" + mimeType);
            }
        }
        SdpBandwidth rsBandwidth = getBandwidth(SdpConstants.BW_TYPE_RS);
        if (rsBandwidth != null &&
                (rtpPayload.getMaxSender() != -1 ||  rtpPayload.getMinSender() != -1)) {
            if (rtpPayload.getMaxSender() < rsBandwidth.getValue() ||
                    rtpPayload.getMinSender() > rsBandwidth.getValue()) {
                supported = false;
                if (LOG.isDebugEnabled()) LOG.debug(
                        "Media description RS bandwidth (" + rsBandwidth.getValue() +
                                ") is NOT sufficient for RS bandwidth " +
                                "requirements (" + rtpPayload.getMinSender() +
                                "-" + rtpPayload.getMaxSender() +
                                ") of mimetype=" + mimeType);
            } else {
                if (LOG.isDebugEnabled()) LOG.debug(
                        "Media description RS bandwidth (" + rsBandwidth.getValue() +
                                ") is sufficient for RS bandwidth " +
                                "requirements (" + rtpPayload.getMinSender() +
                                "-" + rtpPayload.getMaxSender() +
                                ") of mimetype=" + mimeType);
            }
        }
        // check if RR+RS is greater then total bandwidth. total bakdwidth is in kilo bits and RR,RS in bits
        if( rsBandwidth != null && rrBandwidth != null && mdBandwidth != null ) {
            if( rsBandwidth.getValue() + rrBandwidth.getValue() > mdBandwidth.getValue()*1000) {
                supported = false;
                if (LOG.isDebugEnabled()) LOG.debug(
                        "RR bandwidth + RS bandwidth is greater than total bandwidth and is rejected");
            }
        }
        return supported;
    }



    /**
     * Check if the format specific parameters are valid.
     *
     * @param mimeType The relevant MIME type
     * @return returns true if the format specific parameters
     * is acceptable and false otherwise.
     * @throws SdpInternalErrorException
     */
    protected RtpValidationResult isFormatSpecificParametersSupported(MimeType mimeType,Integer payloadType) {

        // Fetch RTP payload, for the given MIME type from configuration
        // If can not be found then we return false.
        RTPPayload rtpPayload = RTPPayload.get(mimeType);
        if (rtpPayload == null) {
            return RtpValidationResult.NO_MATCH;
        }

        // Fetch fmtp corresponding to payload type
        HashMap<Integer,SdpFmtp> fmtps = attributes.getFmtps();
        SdpFmtp fmtp = fmtps.get(payloadType);

        // No format specific parameters given. That is always accepted.
        if (fmtp == null) {
            if (LOG.isDebugEnabled())
            LOG.debug("No format specific parameters present");
            return RtpValidationResult.EXCACT_MATCH;
        }

        // Validate the found format specific parameters against what is
        // configured for MIME type (by using the static RTPPayload)
        RtpValidationResult result =RTPPayload.validateFormatSpecificParameters(
                mimeType, fmtp.getParameters());
        if (!result.equals(RtpValidationResult.NO_MATCH)) {
            if (LOG.isDebugEnabled())
            LOG.debug("The format specific parameters <" + fmtp.getParameters() +
                "> was validated OK for mime type <" + mimeType + ">");

            return result;
        } else {
            if (LOG.isDebugEnabled())
            LOG.debug("The format specific parameters <" + fmtp.getParameters() +
                "> was validated NOT OK for mime type <" + mimeType + ">");

            return RtpValidationResult.NO_MATCH;
        }

    }


    /**
     * @param   mimeType
     * @return  Returns whether the encoding given by the <param>mimeType</param>
     *          is supported by the dynamic RTP maps in the attribute list of
     *          this media description.
     */
    private void getSupportedRtpPayloadByDynamicMapping(MimeType mimeType,ArrayList<Integer> supportedPaytloadTypes) {
        Integer payloadType = null;
    	RTPPayload streamPayLoad = RTPPayload.get(mimeType);
    	if (streamPayLoad == null) {
    		//This should never happen as it is being called from the supported types.
    		//however just to be safe.
            LOG.warn("NO Match Found for Mime Type <" + mimeType + "> during dynamic mapping in the media in RTPPayload" +
                    "description: " + this + " was this called in correct context?");
    		return;
    	}
        for (SdpRtpMap aMapping : getAttributes().getRtpMaps().values()) {
        
        	int streamClockRate=streamPayLoad.getClockRate();
            if (aMapping.isEquivalent(mimeType,streamClockRate)) {
                payloadType = aMapping.getPayloadType();
                if (payloadType != null &&
                        getMedia().getFormats().isFormatSupported(payloadType)) {
                    RtpValidationResult result = isFormatSpecificParametersSupported( mimeType,payloadType);
                    if (result.equals(RtpValidationResult.EXCACT_MATCH)) {
                        supportedPaytloadTypes.add(0, payloadType);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Found Exact match for Mime Type <" + mimeType + "> during dynamic mapping in the media " +
                                    "description: " + this);
                        }
                    }
                    else if(result.equals(RtpValidationResult.PARTIAL_MATCH))
                    {
                        supportedPaytloadTypes.add(payloadType);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Found Partial match for Mime Type <" + mimeType + "> during dynamic mapping in the media " +
                                    "description: " + this);
                        }
                    }

                }
            }
        }
        if (LOG.isDebugEnabled()) {
            if(supportedPaytloadTypes.isEmpty())
            {
                LOG.debug("NO Match Found for Mime Type <" + mimeType + "> during dynamic mapping in the media " +
                        "description: " + this);

            }
        }
    }

/*    
    private void getSupportedRtpPayloadByDynamicMapping(MimeType mimeType,ArrayList<Integer> supportedPaytloadTypes) {
        Integer payloadType = null;
        for (SdpRtpMap aMapping : getAttributes().getRtpMaps().values()) {
            if (aMapping.isEquivalent(mimeType)) {
                payloadType = aMapping.getPayloadType();
                if (payloadType != null &&
                        getMedia().getFormats().isFormatSupported(payloadType)) {
                    RtpValidationResult result = isFormatSpecificParametersSupported( mimeType,payloadType);
                    if (result.equals(RtpValidationResult.EXCACT_MATCH)) {
                        supportedPaytloadTypes.add(0, payloadType);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Found Exact match for Mime Type <" + mimeType + "> during dynamic mapping in the media " +
                                    "description: " + this);
                        }
                    }
                    else if(result.equals(RtpValidationResult.PARTIAL_MATCH))
                    {
                        supportedPaytloadTypes.add(payloadType);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Found Partial match for Mime Type <" + mimeType + "> during dynamic mapping in the media " +
                                    "description: " + this);
                        }
                    }

                }
            }
        }
        if (LOG.isDebugEnabled()) {
            if(supportedPaytloadTypes.isEmpty())
            {
                LOG.debug("NO Match Found for Mime Type <" + mimeType + "> during dynamic mapping in the media " +
                        "description: " + this);

            }
        }
    }
 */
    
    /**
     * @param   mimeType
     * @return  Returns whether the encoding given by the <param>mimeType</param>
     *          is supported by statically mapped RTP encodings and is included
     *          in the list of media formats of this media description.
     */
    private void getSupportedRtpPayloadByStaticMapping(MimeType mimeType,ArrayList<Integer> supportedPaytloadTypes) {
        Integer payloadTypeInt=null;
        // First, find a static RTP payload mapping for the mime type
        RTPPayload rtpPayload = RTPPayload.get(mimeType);
        if (rtpPayload != null) {
            // There is a static RTP payload type for this mime type
            int payloadType = rtpPayload.getPayloadType();

            //Static payload type are less than 96
            if(payloadType<RTPPayload.RTP_PAYLOAD_TYPE_DYNAMIC_START)
            {
                // Check if that payload type is among the media formats
                if(media.getFormats().isFormatSupported(payloadType))
                {
                    payloadTypeInt = new Integer(payloadType);
                    supportedPaytloadTypes.add(payloadTypeInt);
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            String message = "Mime Type <" + mimeType + "> is ";
            if (payloadTypeInt==null)
                message += "NOT ";
            message += "supported by the statically mapped formats " +
                    "in the media description: " + this;
            LOG.debug(message);
        }
    }

    /**
     * Parses the <param>media</param> field.
     * <p>
     * The media field is parsed using {@link SdpMedia#parseMedia(Media)}.
     * If the parsing returned a null result,
     * an {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.SD_PARAMETER_NOT_UNDERSTOOD}.
     *
     * @param   media                       MUST NOT be null.
     * @throws  SdpNotSupportedException    If the parsed media field is null.
     */
    private static SdpMedia parseMedia(Media media)
            throws SdpNotSupportedException {

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving media field from <" + media + ">");

        SdpMedia sdpMedia = SdpMedia.parseMedia(media);

        if (sdpMedia == null) {
            String message = "Media field cannot be retrieved from remote SDP. " +
                    "The call will not be setup.";
            LOG.warn(message);
            throw new SdpNotSupportedException(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, message);
        }

        return sdpMedia;
    }

    /**
     * Checks to see if the given media description (<param>mediaDescription</param>)
     * contains encryption keys.
     * <p>
     * Currently no encryption of Media Description is supported.
     * Therefore, if no encryption keys is set, this method simply
     * returns. Otherwise, an {@link SdpNotSupportedException} is thrown
     * with {@link SipWarning.ENCRYPTION_NOT_SUPPORTED}.
     *
     * @param   mediaDescription
     * @throws  SdpNotSupportedException    if media description contains
     *                                      encryption key.
     */
    private static void assertNoEncryption(MediaDescription mediaDescription)
            throws SdpNotSupportedException {
        Key key = mediaDescription.getKey();

        if (LOG.isDebugEnabled())
            LOG.debug("Parsing key: " + key);

        if (key != null) {
            String message = "The remote Media Description contains encryption keys. " +
                    "Since no encryption of SDP is supported, the call will " +
                    "not be setup.";
            LOG.warn(message);
            throw new SdpNotSupportedException(
                    SipWarning.ENCRYPTION_NOT_SUPPORTED, message);
        }
    }
}
