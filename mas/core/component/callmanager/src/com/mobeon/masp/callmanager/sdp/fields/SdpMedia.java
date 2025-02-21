/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sdp.SdpConstants;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.sdp.Media;
import com.mobeon.sdp.SdpParseException;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class represents the content of a Media field (i.e. the 'm' field)
 * of an SDP.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class SdpMedia {

    private static final ILogger LOG = ILoggerFactory.getILogger(SdpMedia.class);

    private final SdpMediaType type;
    private final int port;
    private final int portCount;
    private final SdpMediaTransport transport;
    private final SdpMediaFormats formats;

    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();

    public SdpMedia(SdpMediaType type,
                    int port, int portCount,
                    SdpMediaTransport transport, SdpMediaFormats formats) {
        this.type = type;
        this.port = port;
        this.portCount = portCount;
        this.transport = transport;
        this.formats = formats;
    }

    public SdpMediaType getType() {
        return type;
    }

    public int getPort() {
        return port;
    }

    public int getPortCount() {
        return portCount;
    }

    public SdpMediaTransport getTransport() {
        return transport;
    }

    public SdpMediaFormats getFormats() {
        return formats;
    }

    public String toString() {
        String representation = stringRepresentation.get();

        if (representation == null) {
            representation = "<Type = " + type +
                    ">, <Port = " + port +
                    ">, <Port Count = " + portCount +
                    ">, <Transport = " + transport +
                    ">, <Formats = " + formats + ">";
            stringRepresentation.set(representation);
        }

        return representation;
    }

    /**
     * This method parses a <param>media</param> field from an SDP
     * and returns a parsed representation of the media field as an
     * <@link SdpMedia>.
     * <p>
     * The media type is retrieved using
     * {@link #retrieveMediaType(com.mobeon.sdp.Media)},
     * the media port is retrieved using
     * {@link #retrieveMediaPort(Media)},
     * the media port count is retrieved using
     * {@link #retrieveMediaPortCount(Media)},
     * the media transport is retrieved using
     * {@link #retrieveMediaTransport(Media)},
     * and the media formats is retrieved using
     * {@link #retrieveMediaFormats(Media)}.
     * If there is something that is not supported, an
     * {@link SdpNotSupportedException} is thrown as described for respective
     * method.
     * <p>
     * If the <param>media</param> field could not be parsed an
     * {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.SD_PARAMETER_NOT_UNDERSTOOD}.
     *
     * @param   media           A media field retrieved from an SDP.
     * @return                  A parsed SdpMedia. Null is returned if
     *                          <param>media</param> is null.
     * @throws SdpNotSupportedException
     *                          If the Media field could not be parsed or
     *                          the parts of the media field is
     *                          unsupported (i.e. was not retrieved ok).
     */
    public static SdpMedia parseMedia(Media media)
            throws SdpNotSupportedException {
        SdpMedia sdpMedia = null;

        if (LOG.isDebugEnabled())
            LOG.debug("Parsing media: " + media);

        if (media != null) {

            try {
                // Retrieve Media Type
                SdpMediaType type = retrieveMediaType(media);

                // Parse Media Port and port count
                int port = retrieveMediaPort(media);
                int portCount = retrieveMediaPortCount(media);

                // Retrieve Media Transport
                SdpMediaTransport transport = retrieveMediaTransport(media);

                // Retrieve Media Formats
                SdpMediaFormats formats = retrieveMediaFormats(media);

                sdpMedia = new SdpMedia(type, port, portCount, transport, formats);

            } catch (SdpParseException e) {
                String message =
                        "Could not parse Media field <" + media +
                        "> received in remote SDP. The call will not be setup.";
                LOG.warn(message, e);
                throw new SdpNotSupportedException(
                        SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, message);
            }

        }

        return sdpMedia;
    }

    //======================== Private Methods ======================

    /**
     * Retrieves the media formats from the <param>media</param> field.
     * <p>
     * If the <param>media</param> field contains a format that is not an
     * integer, an {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.INCOMPATIBLE_MEDIA_FORMAT}.
     * <p>
     * If the <param>media</param> field could not be parsed, an
     * {@link SdpParseException} is thrown.
     *
     * @param   media                       MUST NOT be null.
     * @throws  SdpParseException           If the media could not be parsed.
     * @throws  SdpNotSupportedException    If the media format is not an integer.
     */
    private static SdpMediaFormats retrieveMediaFormats(Media media)
            throws SdpParseException, SdpNotSupportedException {

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving media formats from " + media);

        Vector<String> formats = media.getMediaFormats(true);
        SdpMediaFormats sdpMediaFormats =
                SdpMediaFormats.parseMediaFormats(formats);

        if (sdpMediaFormats == null) {
            String message = "Media formats cannot be retrieved from " +
                    "Media field \"" + media + "\" of remote SDP. " +
                    "The call will not be setup.";
            LOG.warn(message);
            throw new SdpNotSupportedException(
                    SipWarning.INCOMPATIBLE_MEDIA_FORMAT, message);
        }

        return sdpMediaFormats;
    }

    /**
     * Retrieves the media port from the <param>media</param> field.
     * <p>
     * If the <param>media</param> field could not be parsed, an
     * {@link SdpParseException} is thrown.
     *
     * @param   media                       MUST NOT be null.
     * @throws  SdpParseException           If the media could not be parsed.
     */
    private static int retrieveMediaPort(Media media) throws SdpParseException {

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving media port from " + media);

        return media.getMediaPort();
    }

    /**
     * Retrieves the media port count from the <param>media</param> field.
     * <p>
     * If the <param>media</param> field contains a port count larger than one,
     * an {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.PORT_COUNT_NOT_ALLOWED}.
     * <p>
     * If the <param>media</param> field could not be parsed, an
     * {@link SdpParseException} is thrown.
     *
     * @param   media                       MUST NOT be null.
     * @throws  SdpParseException           If the media could not be parsed.
     * @throws  SdpNotSupportedException    If the media port count is larger
     *                                      than zero.
     */
    private static int retrieveMediaPortCount(Media media)
            throws SdpParseException, SdpNotSupportedException {

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving media port count from " + media);

        int portCount = media.getPortCount();
        if (portCount > 1) {
            String message =
                    "Port count larger than one in Media field <" + media +
                            "> of remote SDP. It is not supported. " +
                            "The call will not be setup.";
            LOG.warn(message);
            throw new SdpNotSupportedException(
                    SipWarning.PORT_COUNT_NOT_ALLOWED, message);
        }
        return portCount;
    }

    /**
     * Retrieves the media type from the <param>media</param> field.
     * <p>
     * Only the media transport "RTP/AVP" is supported.
     * If the <param>media</param> field contains an unsupported transport type,
     * an {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.INCOMPATIBLE_TRANSPORT_PROTOCOL}.
     * <p>
     * If the <param>media</param> field could not be parsed, an
     * {@link SdpParseException} is thrown.
     *
     * @param   media                       MUST NOT be null.
     * @throws  SdpParseException           If the media could not be parsed.
     * @throws  SdpNotSupportedException    If the media transport is not
     *                                      supported, i.e. differs from "RTP/AVP".
     */
    private static SdpMediaTransport retrieveMediaTransport(Media media)
            throws SdpParseException, SdpNotSupportedException {

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving media transport from " + media);

        String transport = media.getProtocol();
        SdpMediaTransport sdpMediaTransport =
                SdpMediaTransport.parseMediaTranport(transport);

        if (sdpMediaTransport == null) {
            String message = "Media transport \"" + transport +
                    "\" received in remote SDP is not supported. " +
                    "Only \"" + SdpConstants.TRANSPORT_RTP_AVP +
                    "\" is supported. The call will not be setup.";
            LOG.warn(message);
            throw new SdpNotSupportedException(
                    SipWarning.INCOMPATIBLE_TRANSPORT_PROTOCOL, message);
        }

        return sdpMediaTransport;
    }

    /**
     * Retrieves the media type from the <param>media</param> field.
     * <p>
     * Only the media types "audio" and "video" are supported.
     * If the <param>media</param> field contains an unsupported media type,
     * an {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.MEDIA_TYPE_NOT_AVAILABLE}.
     * <p>
     * If the <param>media</param> field could not be parsed, an
     * {@link SdpParseException} is thrown.
     *
     * @param   media                       MUST NOT be null.
     * @throws  SdpParseException           If the media could not be parsed.
     * @throws  SdpNotSupportedException    If the media type is not supported,
     *                                      i.e. differs from "audio" or "video".
     */
    private static SdpMediaType retrieveMediaType(Media media)
            throws SdpParseException, SdpNotSupportedException {

        if (LOG.isDebugEnabled())
            LOG.debug("Retrieving media type from " + media);

        String mt = media.getMediaType();
        SdpMediaType sdpMediaType = SdpMediaType.parseMediaType(mt);

        if (sdpMediaType == null) {
            String message = "Media type \"" + mt +
                    "\" received in remote SDP is not supported. " +
                    "Only \"" + SdpConstants.MEDIA_TYPE_AUDIO +
                    "\" and \"" + SdpConstants.MEDIA_TYPE_VIDEO +
                    "\" are supported. The call will not be setup.";
            LOG.warn(message);
            throw new SdpNotSupportedException(
                    SipWarning.MEDIA_TYPE_NOT_AVAILABLE, message);
        }
        return sdpMediaType;
    }


}
