/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sdp.SdpConstants;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.sdp.SdpException;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.BandWidth;
import com.mobeon.sdp.SdpParseException;

import java.util.HashMap;
import java.util.Vector;

/**
 * This class represents the content of a Bandwidth field (i.e. the 'b' field)
 * of an SDP.
 * <p>
 * This class is immutable.
 *
 * @author Malin Nyfeldt
 */
public class SdpBandwidth {
    private static final ILogger LOG =
            ILoggerFactory.getILogger(SdpBandwidth.class);

    // The type of bandwidth information. In SDP RFC 4566 two types are
    // specified: AS and CT. Only AS is handled by Call Manager. Other
    // bandwidth types are ignored.
    private final String type;

    // The bandwidth value given in kilobits/second
    private final int value;

    private final String stringRepresentation;

    /**
     * Creates an SDP bandwidth of type AS with the given bandwidth
     * <param>value</param>.
     * @param value     Bandwidth value given in kilobits/second.
     */
    public SdpBandwidth(int value) {
        this.value = value;
        this.type = SdpConstants.BW_TYPE_AS;
        stringRepresentation = type + ":" + value;
    }

    /**
     * Creates an SDP bandwidth of the given <param>type</param> with the given
     * bandwidth <param>value</param>.
     * @param value     Bandwidth value given in kilobits/second.
     */
    public SdpBandwidth(String type, int value) {
        this.value = value;
        this.type = type;
        stringRepresentation = type + ":" + value;
    }

    /**
     * @return  Returns the bandwidth type.
     *          In RFC 3261, bandwidth types AS and CT are defined.
     */
    public String getType() {
        return type;
    }

    /**
     * @return  Returns the bandwidth value in kilobits/second.
     */
    public int getValue() {
        return value;
    }

    /**
     * @return  Returns the string presentation of an SDP bandwidth.
     *          The format is type:value
     */
    public String toString() {
        return stringRepresentation;
    }

    /**
     * Encodes the bandwidth into an SDP stack format using the
     * <param>sdpFactory</param>.
     * A {@link BandWidth} is returned with the bandwidth type and the
     * bandwidth value set.
     * @param   sdpFactory  A factory needed to create SDP fields.
     * @return  A stack formatted bandwidth field.
     * @throws  SdpException if the stack bandwidth field could not be created.
     */
    public BandWidth encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException {
        // "b=<type>:<value>"
        return sdpFactory.createBandwidth(getType(), getValue());
    }

    /**
     * This method parses the bandwidth fields from an SDP
     * and returns a parsed representation of the fields as a
     * <@link HashMap>.
     * <p>
     * If any of the bandwidth fields could not be parsed (e.g. a
     * bandwidth value is not an integer) an
     * {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.SD_PARAMETER_NOT_UNDERSTOOD}.
     *
     * @param   bandwidths      A vector of bandwidth fields retrieved from an
     *                          SDP.
     * @return                  A hash map of the parsed bandwidth fields.
     *                          An empty hashmap is returned if
     *                          <param>bandwidths</param> is null or empty.
     *                          Null is never returned.
     * @throws SdpNotSupportedException
     *                          An SdpNotSupportedException is thrown if
     *                          the bandwidth fields could not be parsed or
     *                          a bandwidth value is not an integer.
     */
    public static HashMap<String, SdpBandwidth> parseBandwidth(
            Vector<BandWidth> bandwidths)
            throws SdpNotSupportedException {

        HashMap<String, SdpBandwidth> sdpBandwidths =
                new HashMap<String, SdpBandwidth>();

        if (LOG.isDebugEnabled())
            LOG.debug("Parsing bandwidth: " + bandwidths);

        if ((bandwidths != null) && (!bandwidths.isEmpty())) {

            for (int i = 0; i < bandwidths.size(); i++) {
                BandWidth bandwidth = bandwidths.elementAt(i);

                try {
                    String bwType = bandwidth.getType();
                    if (bwType == null) {
                        String message = "Could not retrieve bandwidth type from " +
                                "field <" + bandwidth + "> received in remote " +
                                "SDP. The call will not be setup.";
                        throw new SdpNotSupportedException(
                                SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, message);
                    }

                    SdpBandwidth sdpBandwidth =
                            new SdpBandwidth(bwType, bandwidth.getValue());
                    sdpBandwidths.put(bwType, sdpBandwidth);
                } catch (SdpParseException e) {
                    String message =
                            "Could not parse Bandwidth field <" + bandwidth +
                                    "> received in remote SDP. The call will not be setup.";
                    LOG.warn(message, e);
                    throw new SdpNotSupportedException(
                            SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, message);
                }
            }
        }

        return sdpBandwidths;
    }

}
