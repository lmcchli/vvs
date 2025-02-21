/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.attributes;

import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sdp.SdpConstants;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.sdp.Attribute;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SdpException;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This class represents the content of a media attribute "fmtp" of an SDP.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class SdpFmtp {

    private static final ILogger LOG = ILoggerFactory.getILogger(SdpFmtp.class);

    private static final String PARSE_ERROR =
            "Could not parse fmtp attribute value received in remote SDP. " +
                    "The call will not be setup.";

    private static final SdpNotSupportedException PARSE_EXCEPTION =
            new SdpNotSupportedException(
                    SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, PARSE_ERROR);

    public static final String MODE_SET = "mode-set=";

    private final int format;
    private final String parameters;

    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();

    public SdpFmtp(int payloadType, String parameters) {
        this.format = payloadType;
        this.parameters = parameters;
    }

    public int getFormat() {
        return format;
    }

    public String getParameters() {
        return parameters;
    }

    /**
     * Encodes the fmtp into an SDP stack format using the
     * <param>sdpFactory</param>.
     * An {@link Attribute} is returned with "fmtp" as name and
     * format specific parameters as value.
     * @param   sdpFactory
     * @return  An fmtp attribute.
     * @throws  SdpException if the stack format could not be created.
     */
    public Attribute encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException {

        return sdpFactory.createAttribute(
                SdpConstants.ATTRIBUTE_FMTP, toString());
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
     * This method parses an <param>fmtp</param> media attribute value from an
     * SDP and returns a parsed representation of the field as an
     * <@link SdpFmtp>.
     * <p>
     * The format of the fmtp attribute value is:
     * <format> <format specific parameters>
     * where format is an integer.
     * <p>
     * If the <param>fmtp</param> attribute value could not be parsed an
     * {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.ATTRIBUTE_NOT_UNDERSTOOD}.
     *
     * @param   fmtp            The value of the fmtp attribute.
     * @return                  A parsed SdpFmtp. Null is returned if
     *                          <param>fmtp</param> is null.
     * @throws SdpNotSupportedException
     *                          If the fmtp value does not consist of an integer
     *                          value format and format specific parameter.
     */
    public static SdpFmtp parseFmtpAttribute(String fmtp)
            throws SdpNotSupportedException {
        SdpFmtp sdpFmtp = null;

        if (LOG.isDebugEnabled())
            LOG.debug("Parsing fmtp attribute value: " + fmtp);

        if (fmtp != null) {

            // Find the separation between format and parameters
            int index = fmtp.indexOf(' ');
            if ((index <= 0) || (index >= (fmtp.length() - 1))) {
                LOG.warn("Illegal amount of parts in fmtp: " + fmtp +
                        ". The call will not be setup.");
                throw PARSE_EXCEPTION;
            }

            // Retrieve the integer format
            int format;
            try {
                format = Integer.valueOf(fmtp.substring(0, index));
            } catch (NumberFormatException e) {
                LOG.warn("Illegal format in fmtp: " + fmtp +
                        ". The call will not be setup.");
                throw PARSE_EXCEPTION;
            }

            sdpFmtp = new SdpFmtp(format, fmtp.substring(index + 1));
        }

        return sdpFmtp;
    }

}
