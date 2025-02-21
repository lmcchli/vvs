/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.masp.callmanager.sdp.fields.SdpConnection;
import com.mobeon.masp.callmanager.sdp.fields.SdpMedia;
import com.mobeon.masp.callmanager.sdp.fields.SdpMediaType;
import com.mobeon.masp.callmanager.sdp.fields.SdpOrigin;
import com.mobeon.masp.callmanager.sdp.fields.SdpBandwidth;
import com.mobeon.masp.callmanager.sdp.attributes.SdpAttributes;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.mediacontentmanager.IMediaContentResourceProperties;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import com.mobeon.sdp.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is a parsed representation of a session description contained in
 * a remote SDP.
 * It contains the session description level connection, origin and attributes.
 * It also contains a list of media descriptions.
 * <p>
 * This class is thread-safe through the use of atomic members.
 *
 * @author Malin Nyfeldt
 */
public class SdpSessionDescriptionImpl implements SdpSessionDescription {

    private static final ILogger LOG =
            ILoggerFactory.getILogger(SdpSessionDescriptionImpl.class);

    private AtomicReference<SdpConnection> connection =
            new AtomicReference<SdpConnection>();
    private AtomicReference<HashMap<String, SdpBandwidth>> bandwidths =
            new AtomicReference<HashMap<String, SdpBandwidth>>();
    private AtomicReference<SdpOrigin> origin = new AtomicReference<SdpOrigin>();
    private AtomicReference<SdpAttributes> attributes =
            new AtomicReference<SdpAttributes>();
    private AtomicReference<List<SdpMediaDescription>> mediaDescriptions =
            new AtomicReference<List<SdpMediaDescription>>();

    public SdpSessionDescriptionImpl() {
        setAttributes(new SdpAttributes());
        setBandwidths(new HashMap<String, SdpBandwidth>());
    }

    // Getters
    public SdpAttributes getAttributes() {
        return attributes.get();
    }

    public SdpBandwidth getBandwidth(String type) {
        SdpBandwidth result = null;
        HashMap<String, SdpBandwidth> bws = bandwidths.get();
        if (bws != null)
            result = bws.get(type);
        return result;
    }

    public HashMap<String, SdpBandwidth> getBandwidths() {
        return bandwidths.get();
    }

    public SdpConnection getConnection() {
        return connection.get();
    }

    public SdpOrigin getOrigin() {
        return origin.get();
    }

    public SdpMediaDescription getMedia(SdpMedia sdpMedia) {
        SdpMediaDescription sdpMediaDescriptionFound = null;
        for (SdpMediaDescription sdpMediaDescription : getMediaDescriptions()) {
//DEBUGDEBUG            if (sdpMediaDescription.getMedia().toString().equals(sdpMedia.toString())) {
                sdpMediaDescriptionFound = sdpMediaDescription;
                break;
//DEBUGDEBUG            }
        }
        return sdpMediaDescriptionFound;
    }

    public SdpMediaDescription getMediaDescription(int index) throws IndexOutOfBoundsException {
        return mediaDescriptions.get().get(index);
    }

    public List<SdpMediaDescription> getMediaDescriptions() {
        return mediaDescriptions.get();
    }

    // Setters
    public void setAttributes(SdpAttributes attributes) {
        this.attributes.set(attributes);
    }

    public void setBandwidths(HashMap<String, SdpBandwidth> bandwidths) {
        this.bandwidths.set(bandwidths);
    }

    public void setConnection(SdpConnection connection) {
            this.connection.set(connection);
    }

    public void setOrigin(SdpOrigin origin) {
            this.origin.set(origin);
    }

    public void setMediaDescriptions(
            List<SdpMediaDescription> mediaDescriptions) {
        this.mediaDescriptions.set(mediaDescriptions);
    }


    public boolean containsVideo() {
        boolean containsVideo = false;

        for (SdpMediaDescription md : getMediaDescriptions()) {
            if (md.getMedia().getType() == SdpMediaType.VIDEO) {
                containsVideo = true;
                break;
            }
        }

        return containsVideo;
    }

    public String toString() {
        return "Session Description: " +
                "<Origin = " + getOrigin() +
                ">, <Connection = " + getConnection() +
                ">, <Bandwidths = " + getBandwidths() +
                ">, <Attributes = " + getAttributes() +
                ">, <Media Descriptions = " + getMediaDescriptions() + ">";
    }

    public String toString2() {
        return "Session Description: " +
                "<Connection = " + getConnection() +
                ">, <Bandwidths = " + getBandwidths() +
                ">, <Attributes = " + getAttributes() +
                ">, <Media Descriptions = " + getMediaDescriptions() + ">";
    }

    /**
     * In this method, {@link SdpOrigin} is not compared for backward compatibility reason.
     * @param sdpSessionDescription SdpSessionDescription
     * @param excludeTransmissionMode true if transmissionMode must be exclude out of the comparison, false otherwise
     * return true if both objects are equal, false otherwise
     */
    public boolean compareWith(SdpSessionDescription sdpSessionDescription, boolean excludeTransmissionMode) {
        boolean connection = true;
        boolean bandwidths = true;
        boolean attributes = true;

        if (getConnection() != null && sdpSessionDescription.getConnection() != null) {
            connection = getConnection().toString().equals(sdpSessionDescription.getConnection().toString());
        } else if (getConnection() != null || sdpSessionDescription.getConnection() != null) {
            return false;
        }

        if (getBandwidths() != null && sdpSessionDescription.getBandwidths() != null) {
            bandwidths = getBandwidths().toString().equals(sdpSessionDescription.getBandwidths().toString());
        } else if (getBandwidths() != null || sdpSessionDescription.getBandwidths() != null) {
            return false;
        }

        if (getAttributes() != null && sdpSessionDescription.getAttributes() != null) {
            attributes = getAttributes().compareWith(sdpSessionDescription.getAttributes(), false);
        } else if (getAttributes() != null || sdpSessionDescription.getAttributes() != null) {
            return false;
        }

        if (getMediaDescriptions().size() != sdpSessionDescription.getMediaDescriptions().size()) {
            return false;
        }

        int index = 0;
        for (SdpMediaDescription thisSdpMediaDescription : getMediaDescriptions()) {
            SdpMediaDescription otherSdpMediaDescription = sdpSessionDescription.getMediaDescription(index++);
            if (!thisSdpMediaDescription.compareWith(otherSdpMediaDescription, excludeTransmissionMode)) {
                return false;
            }
        }
        return connection && bandwidths && attributes;
    }

    public String encodeToSdp(SdpFactory sdpFactory) throws SdpInternalErrorException {

        SessionDescription stackSdp;
        try {
            stackSdp = sdpFactory.createSessionDescription();

            // Encode version
            stackSdp.setVersion(encodeVersion(sdpFactory));

            // Encode origin
            stackSdp.setOrigin(getOrigin().encodeToStackFormat(sdpFactory));

            // Encode session name
            stackSdp.setSessionName(encodeSessionName(sdpFactory));

            // Encode connection
            SdpConnection connection = getConnection();
            if (connection != null)
                stackSdp.setConnection(connection.encodeToStackFormat(sdpFactory));

            // Encode Bandwidth fields
            for (SdpBandwidth b : getBandwidths().values()) {
                stackSdp.setBandwidth(b.getType(), b.getValue());
            }

            // Encode attributes
            stackSdp.setAttributes(getAttributes().encodeToStackFormat(sdpFactory));

            // Encode time descriptions
            stackSdp.setTimeDescriptions(encodeTimeDescription(sdpFactory));

            // Encode media descriptions
            Vector<MediaDescription> stackMDs = new Vector<MediaDescription>();
            for (SdpMediaDescription md : mediaDescriptions.get())
                stackMDs.add(md.encodeToStackFormat(sdpFactory));
            stackSdp.setMediaDescriptions(stackMDs);

        } catch (SdpException e) {
            throw new SdpInternalErrorException(e.getMessage(), e);
        }

        return stackSdp.toString();
    }

    public boolean isSdpMediaDescriptionOnHold() {
    	
        boolean onHold = false;

        for (SdpMediaDescription md : getMediaDescriptions()) {
        	if (md.isSdpMediaDescriptionOnHold()) {
            	onHold = true;
        	} else {
        		onHold = false;
        		break;
        	}
        }

        return onHold;
    }

    //======================== Static Methods ======================

    /**
     * Parses the <param>stackSd</param> and returns a parsed variant of type
     * {@link SdpSessionDescription}.
     * <p>
     * The connection of the session description is parsed using
     * {@link SdpConnection#parseConnection(Connection)},
     * the attributes of the session description are parsed using
     * {@link SdpAttributes#parseAttributes(Vector<Attribute>)} and
     * the media descriptions are parsed using
     * {@link #retrieveMediaDescriptions(
     * SessionDescription, SdpSessionDescriptionImpl)}.
     * <p>
     * It is also verified that no encryption is used for the SDP and
     * that the charset is UTF-8.
     *
     * @param   stackSd     A session description as received from the SIP stack.
     * @return              A parsed variant of the session description.
     * @throws  SdpNotSupportedException
     *                      If the session description contains unsupported
     *                      features (e.g. is encrypted or contains an unsupported
     *                      charset) or could not be parsed. See also the methods
     *                      used for parsing the session description for details.
     */
    static SdpSessionDescription parseSessionDescription(
            SessionDescription stackSd) throws SdpNotSupportedException {

        SdpSessionDescriptionImpl parsedSd = null;

        if (stackSd != null) {

            parsedSd = new SdpSessionDescriptionImpl();

            // Retrieve session level Connection
            parsedSd.setConnection(
                    SdpConnection.parseConnection(stackSd.getConnection()));

            // Parse Bandwidth fields
            parsedSd.setBandwidths(SdpBandwidth.parseBandwidth(
                    stackSd.getBandwidths(true)));

            // Retrieve origin
            parsedSd.setOrigin(SdpOrigin.parseOrigin(stackSd.getOrigin()));

            // Verify that SDP is not encrypted
            assertNoEncryption(stackSd);

            // Retrieve session level attributes
            parsedSd.setAttributes(
                    SdpAttributes.parseAttributes(stackSd.getAttributes(false)));

            // Verify that charset is supported
            assertCharsetSupported(parsedSd);

            // Verify that version is supported
            assertVersionSupported(stackSd);

            // Retrieve media descriptions
            parsedSd.setMediaDescriptions(
                    retrieveMediaDescriptions(stackSd, parsedSd));
        }

        return parsedSd;
    }

    /**
     * Retrieves media descriptions from the <param>stackSd</param>
     * and parses them using
     * {@link SdpMediaDescriptionImpl#parseMediaDescription(
     * MediaDescription, SdpSessionDescription)}.
     * The parsed variant of the media descriptions are added to the
     * <param>parsedSd</param>.
     * <p>
     * If the media descriptions could not be retrieved, an
     * {@link SdpNotSupportedException} is thrown indicating
     * {@link SipWarning.SD_PARAMETER_NOT_UNDERSTOOD}.
     * <p>
     * If there is an error parsing the media description an
     * {@link SdpNotSupportedException} is thrown as described in
     * {@link SdpMediaDescriptionImpl#parseMediaDescription(
     * MediaDescription, SdpSessionDescription)}.
     * <p>
     * If {@link SdpMediaDescriptionImpl#parseMediaDescription(
     * MediaDescription, SdpSessionDescription)} returns null
     * an {@link SdpNotSupportedException} is
     * thrown with {@link SipWarning.SD_PARAMETER_NOT_UNDERSTOOD}.
     *
     * @param   stackSd     A session description as received from the SIP stack.
     * @param   parsedSd    The parsed session description.
     * @throws  SdpNotSupportedException
     *                      If the media descriptions could not be retrieved or
     *                      parsed.
     */
    private static List<SdpMediaDescription> retrieveMediaDescriptions(
            SessionDescription stackSd, SdpSessionDescriptionImpl parsedSd)
            throws SdpNotSupportedException {

        Vector<MediaDescription> mediaDescriptions;
        try {
            mediaDescriptions = stackSd.getMediaDescriptions(false);
        } catch (SdpException e) {
            String message = "Could not retrieve Media Description fields " +
                    "from remote SDP. The call will not be setup.";
            LOG.warn(message, e);
            throw new SdpNotSupportedException(
                    SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, message);
        }

        List<SdpMediaDescription> parsedMDs = new ArrayList<SdpMediaDescription>();

        if (mediaDescriptions != null) {
            for (MediaDescription m : mediaDescriptions) {
                SdpMediaDescription md =
                        SdpMediaDescriptionImpl.parseMediaDescription(m, parsedSd);

                if (md == null) {
                    String message = "Could not retrieve Media Description " +
                            "from remote SDP. The call will not be setup.";
                    LOG.warn(message);
                    throw new SdpNotSupportedException(
                            SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, message);
                }

                parsedMDs.add(md);
            }
        }

        return parsedMDs;
    }

    /**
     * Verifies if the charset of the parsed session description
     * (<param>parsedSd</param>) is supported.
     * Currently only UTF-8 is supported which is also the default charset of
     * an SDP if none is specified.
     * <p>
     * If the SDP has a charset specified that is not UTF-8 an
     * {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.CHARSET_NOT_SUPPORTED}.
     *
     * @param   parsedSd
     * @throws  SdpNotSupportedException    for unsupported charset.
     */
    private static void assertCharsetSupported(SdpSessionDescriptionImpl parsedSd)
            throws SdpNotSupportedException {

        String charset = parsedSd.getAttributes().getCharset();

        if ((charset != null) && !(charset.equalsIgnoreCase(SdpConstants.UTF_8))) {
            String message =
                    "The remote SDP contains the unsupported charset <" +
                            charset + ">. " +
                            "Since only UTF-8 is supported, the call will " +
                            "not be setup.";
            LOG.warn(message);
            throw new SdpNotSupportedException(
                    SipWarning.CHARSET_NOT_SUPPORTED, message);
        }
    }

    /**
     * Checks to see if the given session description (<param>stackSd</param>)
     * contains encryption keys.
     * <p>
     * Currently no encryption of SDP is supported.
     * Therefore, if no encryption keys is set in the SDP this method simply
     * returns. Otherwise, an {@link SdpNotSupportedException} is thrown
     * with {@link SipWarning.ENCRYPTION_NOT_SUPPORTED}.
     *
     * @param   stackSd
     * @throws  SdpNotSupportedException    if session description contains
     *                                      encryption key.
     */
    private static void assertNoEncryption(SessionDescription stackSd)
            throws SdpNotSupportedException {

        Key key = stackSd.getKey();

        if (LOG.isDebugEnabled())
            LOG.debug("Parsing key: " + key);

        if (key != null) {
            String message = "The remote SDP contains encryption keys. " +
                    "Since no encryption of SDP is supported, the call will " +
                    "not be setup.";
            LOG.warn(message);
            throw new SdpNotSupportedException(
                    SipWarning.ENCRYPTION_NOT_SUPPORTED, message);
        }
    }

    private static void assertVersionSupported(SessionDescription stackSd)
            throws SdpNotSupportedException {
        Version version = stackSd.getVersion();

        if (LOG.isDebugEnabled())
            LOG.debug("Parsing version: " + version);

        if (version != null) {
            try {
                if (version.getVersion() != 0) {
                    String message = "The remote SDP contains an unsupported version: " +
                            version.getVersion() + ". Only version 0 is supported, " +
                            "the call will not be setup.";
                    LOG.warn(message);
                    throw new SdpNotSupportedException(
                            SipWarning.VERSION_NOT_SUPPORTED, message);
                }
            } catch (SdpParseException e) {
                String message = "Unable to get version from the remote SDP." +
                        " Version 0 is required, the call will not be setup.";
                LOG.warn(message);
                throw new SdpNotSupportedException(
                        SipWarning.VERSION_NOT_SUPPORTED, message);
            }
        }
    }

    //======================== Private Methods ======================

    private SessionName encodeSessionName(SdpFactory sdpFactory)
            throws SdpException {
        // "s=-"
        return sdpFactory.createSessionName("-");
    }

    private Vector<TimeDescription> encodeTimeDescription(SdpFactory sdpFactory)
            throws SdpException {
        // "t=0 0"
        TimeDescription t = sdpFactory.createTimeDescription();
        Vector<TimeDescription> timeDescs = new Vector<TimeDescription>();
        timeDescs.add(t);
        return timeDescs;
    }

    private Version encodeVersion(SdpFactory sdpFactory) throws SdpException {
        // "v=0"
        return sdpFactory.createVersion(0);
    }
}
