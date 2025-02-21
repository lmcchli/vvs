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
 * This class represents the content of a media attribute "maxptime" of an SDP.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class SdpMaxPTime {

    private static final ILogger LOG = ILoggerFactory.getILogger(SdpMaxPTime.class);

    private static final String PARSE_ERROR =
            "Could not parse maxptime attribute value received in remote SDP. " +
                    "The call will not be setup.";

    private static final SdpNotSupportedException PARSE_EXCEPTION =
            new SdpNotSupportedException(
                    SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, SdpMaxPTime.PARSE_ERROR);

    private final int maxPTime;

    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();

    public SdpMaxPTime(int maxPTime) {
        this.maxPTime = maxPTime;
    }

    public int getValue() {
        return maxPTime;
    }

    /**
     * Encodes the maxPTime into an SDP stack format using the
     * <param>sdpFactory</param>.
     * An {@link com.mobeon.sdp.Attribute} is returned with "maxptime" as name and
     * the value in milliseconds.
     * @param   sdpFactory
     * @return  A maxptime attribute.
     * @throws  com.mobeon.sdp.SdpException if the stack format could not be created.
     */
    public Attribute encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException {
        return sdpFactory.createAttribute(
                SdpConstants.ATTRIBUTE_MAXPTIME, toString());
    }

    public String toString() {
        String representation = stringRepresentation.get();

        if (representation == null) {
            representation = String.valueOf(maxPTime);
            stringRepresentation.set(representation);
        }

        return representation;
    }

    /**
     * This method parses a <param>maxptime</param> media attribute value from an
     * SDP and returns a parsed representation of the field as an
     * <@link SdpMaxPTime>.
     * <p>
     * The format of the maxptime attribute value is:
     * <packet time>
     * where the packet time is an integer indicating packet time in milliseconds.
     * <p>
     * If the <param>maxptime</param> attribute value could not be parsed an
     * {@link com.mobeon.masp.callmanager.sdp.SdpNotSupportedException} is thrown with
     * {@link com.mobeon.masp.callmanager.sip.header.SipWarning.ATTRIBUTE_NOT_UNDERSTOOD}.
     *
     * @param   maxPTime           The value of the maxPTime attribute.
     * @return                  A parsed SdpMaxPTime. Null is returned if
     *                          <param>maxptime</param> is null.
     * @throws com.mobeon.masp.callmanager.sdp.SdpNotSupportedException
     *                          If the maxPTime value does not consist of an
     *                          integer value.
     */
    public static SdpMaxPTime parseMaxPTimeAttribute(String maxPTime)
            throws SdpNotSupportedException {

        SdpMaxPTime sdpMaxPTime = null;

        if (SdpMaxPTime.LOG.isDebugEnabled())
                SdpMaxPTime.LOG.debug("Parsing maxPTime attribute value: " + maxPTime);

        if (maxPTime != null) {
            // Retrieve the packet time as an integer
            int pt;
            try {
                pt = Integer.valueOf(maxPTime);
            } catch (NumberFormatException e) {
                SdpMaxPTime.LOG.warn("Illegal format of maxPTime: " + maxPTime +
                        ". The call will not be setup.");
                throw SdpMaxPTime.PARSE_EXCEPTION;
            }

            sdpMaxPTime = new SdpMaxPTime(pt);
        }

        return sdpMaxPTime;
    }

}
