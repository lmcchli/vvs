/**
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.attributes;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sdp.SdpConstants;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.sdp.Attribute;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SdpException;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This class represents rtcp-fb attributes in media descriptions
 *
 * @author Stefan Berglund
 */
public class SdpRTCPFeedback {
    private static final ILogger LOG = ILoggerFactory.getILogger(SdpFmtp.class);

    private static final String PARSE_ERROR =
            "Could not parse fmtp attribute value received in remote SDP. " +
                    "The call will not be setup.";

    private static final SdpNotSupportedException PARSE_EXCEPTION =
            new SdpNotSupportedException(
                    SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, PARSE_ERROR);

    /** Payloadtype, or "*" */
    private final String format;
    private final String parameters;

    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();

    public SdpRTCPFeedback(String format, String parameters) {
        this.format = format;
        this.parameters = parameters;
    }

    public String getFormat() {
        return format;
    }

    public String getParameters() {
        return parameters;
    }

    /**
     * Encodes the RTCP feedback into an SDP stack format using the
     * <param>sdpFactory</param>.
     * An {@link com.mobeon.sdp.Attribute} is returned with "rtcp-fb" as name and
     * format specific parameters as value.
     * @param   sdpFactory
     * @return  An rtcp-fb attribute.
     * @throws  com.mobeon.sdp.SdpException if the stack format could not be created.
     */
    public Attribute encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException {

        return sdpFactory.createAttribute(
                SdpConstants.ATTRIBUTE_RTCP_FB, toString());
    }

    public String toString() {
        String representation = stringRepresentation.get();

        if (representation == null) {
            representation = format + " " + parameters;
            stringRepresentation.set(representation);
        }

        return representation;
    }

    /**
     * This method parses an <param>rtcp-fb</param> media attribute value from an
     * SDP and returns a parsed representation of the field as an
     * <@link SdpRTCPFeedback>.
     * <p>
     * The format of the rtcp-fb attribute value is:
     * <format> <feedback specific parameters>
     * where format is an integer.
     * <p>
     * If the <param>rtcp-fb</param> attribute value could not be parsed an
     * {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.ATTRIBUTE_NOT_UNDERSTOOD}.
     *
     * @param   rtcpfb            The value of the rtcp-fb attribute.
     * @return                  A parsed SdpRTCPFeedback. Null is returned if
     *                          <param>rtcpfb</param> is null.
     * @throws SdpNotSupportedException
     *                          If the rtcpfb value does not consist of an integer
     *                          value format and format specific parameter.
     */
    public static SdpRTCPFeedback parseAttribute(String rtcpfb)
            throws SdpNotSupportedException {
        SdpRTCPFeedback sdpRtcpFb = null;

        if (LOG.isDebugEnabled())
            LOG.debug("Parsing rtcpfb attribute value: " + rtcpfb);

        if (rtcpfb != null) {

            // Find the separation between format and parameters
            int index = rtcpfb.indexOf(' ');
            if ((index <= 0) || (index >= (rtcpfb.length() - 1))) {
                LOG.warn("Illegal amount of parts in rtcpfb: " + rtcpfb +
                        ". The call will not be setup.");
                throw PARSE_EXCEPTION;
            }

            // Retrieve the integer format
            String format;
            format = rtcpfb.substring(0, index);

            sdpRtcpFb = new SdpRTCPFeedback(format, rtcpfb.substring(index + 1));
        }

        return sdpRtcpFb;
    }

}
