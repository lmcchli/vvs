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
 * This class represents the content of a media attribute "ptime" of an SDP.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class SdpPTime {

    private static final ILogger LOG = ILoggerFactory.getILogger(SdpPTime.class);

    private static final String PARSE_ERROR =
            "Could not parse ptime attribute value received in remote SDP. " +
                    "The call will not be setup.";

    private static final SdpNotSupportedException PARSE_EXCEPTION =
            new SdpNotSupportedException(
                    SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, PARSE_ERROR);

    private final int pTime;

    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();

    public SdpPTime(int pTime) {
        this.pTime = pTime;
    }

    public int getpTime() {
        return pTime;
    }

    /**
     * Encodes the pTime into an SDP stack format using the
     * <param>sdpFactory</param>.
     * An {@link Attribute} is returned with "ptime" as name and
     * the value in milliseconds.
     * @param   sdpFactory
     * @return  A ptime attribute. 
     * @throws  SdpException if the stack format could not be created.
     */
    public Attribute encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException {
        return sdpFactory.createAttribute(
                SdpConstants.ATTRIBUTE_PTIME, toString());
    }

    public String toString() {
        String representation = stringRepresentation.get();

        if (representation == null) {
            representation = String.valueOf(pTime);
            stringRepresentation.set(representation);
        }

        return representation;
    }

    /**
     * This method parses a <param>ptime</param> media attribute value from an
     * SDP and returns a parsed representation of the field as an
     * <@link SdpPTime>.
     * <p>
     * The format of the ptime attribute value is:
     * <packet time>
     * where the packet time is an integer indicating packet time in milliseconds.
     * <p>
     * If the <param>ptime</param> attribute value could not be parsed an
     * {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.ATTRIBUTE_NOT_UNDERSTOOD}.
     *
     * @param   ptime           The value of the ptime attribute.
     * @return                  A parsed SdpPTime. Null is returned if
     *                          <param>ptime</param> is null.
     * @throws SdpNotSupportedException
     *                          If the ptime value does not consist of an
     *                          integer value.
     */
    public static SdpPTime parsePTimeAttribute(String ptime)
            throws SdpNotSupportedException {

        SdpPTime sdpPtime = null;

        if (LOG.isDebugEnabled())
                LOG.debug("Parsing ptime attribute value: " + ptime);

        if (ptime != null) {
            // Retrieve the packet time as an integer
            int pt;
            try {
                pt = Integer.valueOf(ptime);
            } catch (NumberFormatException e) {
                LOG.warn("Illegal format of ptime: " + ptime +
                        ". The call will not be setup.");
                throw PARSE_EXCEPTION;
            }

            sdpPtime = new SdpPTime(pt);
        }

        return sdpPtime;
    }

}
