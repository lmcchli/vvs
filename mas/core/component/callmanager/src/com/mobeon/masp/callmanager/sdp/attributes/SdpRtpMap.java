/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.attributes;

import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sdp.SdpConstants;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.sdp.Attribute;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SdpException;

import jakarta.activation.MimeType;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class represents the content of a media attribute "rtpmap" of an SDP.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class SdpRtpMap {

    private static final ILogger LOG = ILoggerFactory.getILogger(SdpRtpMap.class);

    private static final String PARSE_ERROR =
            "Could not parse rtpmap attribute value received in remote SDP. " +
                    "The call will not be setup.";

    private static final SdpNotSupportedException PARSE_EXCEPTION =
            new SdpNotSupportedException(
                    SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, PARSE_ERROR);

    private final int payloadType;
    private final String encodingName;
    private final int clockRate;
    private final Integer channels;

    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();

    /**
     * Creates a new SdpRtpMap with the given <param>payloadType</param> and
     * encoding details. The encoding details are <param>encodingName</param>,
     * <param>clockRate</param> and <param>channels</param>.
     * The <param>channels</param> parameter is optional and shall be set to
     * null if not present.
     * @param payloadType
     * @param encodingName      MUST NOT be null.
     * @param clockRate
     * @param channels
     * @throws IllegalArgumentException if  <param>encodingName</param> is null.
     */
    public SdpRtpMap(int payloadType, String encodingName,
                     int clockRate, Integer channels)
            throws IllegalArgumentException {

        if (encodingName == null)
                throw new IllegalArgumentException(
                        "Encoding name must not be null.");

        this.payloadType = payloadType;
        this.encodingName = encodingName;
        this.clockRate = clockRate;
        this.channels = channels;
    }

    public Integer getChannels() {
        return channels;
    }

    public int getClockRate() {
        return clockRate;
    }

    public String getEncodingName() {
        return encodingName;
    }

    public int getPayloadType() {
        return payloadType;
    }

    /**
     * Encodes the rtpmap into an SDP stack format using the
     * <param>sdpFactory</param>.
     * An {@link Attribute} is returned with "rtpmap" as name and
     * the mapping as value.
     * @param   sdpFactory
     * @return  An rtpmap attribute.
     * @throws  SdpException if the stack format could not be created.
     */
    public Attribute encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException {

        return sdpFactory.createAttribute(
                SdpConstants.ATTRIBUTE_RTPMAP, toString());
    }

    /**
     * This method is used to find out if a given <param>mimeType</param> is
     * equivalent to this rtpmap with regards to encoding name and other
     * encoding details.
     * For example:
     * "audio/pcmu" is equivalent to the following rtpmap:8 PCMU/8000.
     * <p>
     * Currently, this method compares the base Mine Type like audio/amr
     * and the clock rate to the passed in clock rate.
     * If you wish not to check the clock rate, i.e maybe sip signaling, pass -1)
     *
     * @param   mimeType        A valid content type, e.g. "audio/pcmu".
     * @param  clockrate to compare with that in the stream config (RTPPayload), pass -1 to skip comparison
     * @return  Returns true if the given <param>mimeType</param> is equivalent
     *          to the encoding properties specified by this rtpmap.
     *          Otherwise, false is returned.
     */
    public boolean isEquivalent(MimeType mimeType,int clockRate) {
        boolean equivalent = false;

        if (mimeType != null) {
        	if (getEncodingName().equalsIgnoreCase(mimeType.getSubType()))
        	{

        		if (clockRate > 0 && this.clockRate > 0) {
        			equivalent = (this.clockRate == clockRate);
        			if (LOG.isDebugEnabled() && !equivalent) {
        				LOG.debug(
        						"ClockRate does not match for " + mimeType);
        			} else {
        				equivalent = true;
        			}
        		}
        	}
        }
        if (LOG.isDebugEnabled()) {
            String message = "MimeType <" + mimeType + "> is ";
            if (!equivalent)
                message += "NOT ";
            message += "handled by rtpmap:" + this;
            LOG.debug(message);
        }
        

        return equivalent;
    }
    
    public String toString() {
        String representation = stringRepresentation.get();

        if (representation == null) {

            representation = payloadType + " " + encodingName + "/" + clockRate;
            if (channels != null)
                representation += "/" + channels;

            stringRepresentation.set(representation);
        }

        return representation;
    }

    /**
     * This method parses an <param>rtpmap</param> media attribute value from an
     * SDP and returns a parsed representation of the field as an
     * <@link SdpRtpMap>.
     * <p>
     * The format of the rtpmap attribute value is:
     * <payload type> <encoding name>/<clock rate>[/<encoding parameters>]
     * where payload type and clockrate is given as integers and the encoding
     * parameters (if present) indicates number of channels as an integer for
     * audio media.
     * <p>
     * If the <param>rtpmap</param> attribute value could not be parsed an
     * {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.ATTRIBUTE_NOT_UNDERSTOOD}.
     *
     * @param   rtpmap          The value of the rtpmap attribute.
     * @return                  A parsed SdpRtpMap. Null is returned if
     *                          <param>rtpmap</param> is null.
     * @throws SdpNotSupportedException
     *                          If the rtpmap value does not consist of all the
     *                          parts it should as described above.
     */
    public static SdpRtpMap parseRtpMapAttribute(String rtpmap)
            throws SdpNotSupportedException {

        SdpRtpMap sdpRtpMap = null;

        if (LOG.isDebugEnabled())
                LOG.debug("Parsing rtpmap attribute value: " + rtpmap);

        if (rtpmap != null) {
            // Find the separation between payload type and encoding details
            int index = rtpmap.indexOf(' ');
            if (index <= 0) {
                LOG.warn("Illegal amount of parts in rtpmap: " + rtpmap +
                        ". The call will not be setup.");
                throw PARSE_EXCEPTION;
            }

            // Retrieve the payload type as an integer
            int payload;
            try {
                payload = Integer.valueOf(rtpmap.substring(0, index));
            } catch (NumberFormatException e) {
                LOG.warn("Illegal payload type in rtpmap: " + rtpmap +
                        ". The call will not be setup.");
                throw PARSE_EXCEPTION;
            }

            // Split the encoding details into encoding name, clock rate and
            // encoding parameters
            String[] encodingDetails = (rtpmap.substring(index + 1)).split("/");
            if (encodingDetails.length < 2) {
                LOG.warn("Illegal amount of encoding details in rtpmap: " + rtpmap +
                        ". The call will not be setup.");
                throw PARSE_EXCEPTION;
            }

            // Retrieve the encoding name
            String encodingName = encodingDetails[0];

            // Retrieve the clock rate
            int clockRate;
            try {
                clockRate = Integer.valueOf(encodingDetails[1]);
            } catch (NumberFormatException e) {
                LOG.warn("Illegal clock rate in rtpmap: " + rtpmap +
                        ". The call will not be setup.");
                throw PARSE_EXCEPTION;
            }

            Integer channels = null;
            if (encodingDetails.length > 2) {
                try {
                    channels = Integer.valueOf(encodingDetails[2]);
                } catch (NumberFormatException e) {
                    LOG.warn("Illegal channels in rtpmap: " + rtpmap +
                            ". The call will not be setup.");
                    throw PARSE_EXCEPTION;
                }
            }

            try {
                sdpRtpMap = new SdpRtpMap(
                        payload, encodingName, clockRate, channels);
            } catch (IllegalArgumentException e) {
                LOG.warn(PARSE_ERROR);
                throw PARSE_EXCEPTION;
            }
        }

        return sdpRtpMap;
    }
}
