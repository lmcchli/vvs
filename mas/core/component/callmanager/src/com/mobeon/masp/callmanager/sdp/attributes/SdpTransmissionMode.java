/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.attributes;

import com.mobeon.sdp.Attribute;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SdpException;

/**
 * This class represents the transmission mode of a media in an SDP.
 * The transmission mode is given by one of the following SDP attributes:
 * <ul>
 * <li>recvonly (defined in RFC 2327)</li>
 * <li>sendrecv (defined in RFC 2327)</li>
 * <li>sendonly (defined in RFC 2327)</li>
 * <li>inactive (defined in RFC 3108)</li>
 * </ul>
 *
 * @author Malin Flodin
 */
public enum SdpTransmissionMode {
    INACTIVE ("inactive"),
    RECVONLY ("recvonly"),
    SENDONLY ("sendonly"),
    SENDRECV ("sendrecv");

    private final String name;

    SdpTransmissionMode(String name) {
       this.name = name;
   }

    /**
     * Encodes the transmission mode into an SDP stack format using the
     * <param>sdpFactory</param>.
     * An {@link Attribute} is returned with the transmission mode as name and
     * null as value.
     * @param   sdpFactory
     * @return  A transmission mode attribute with null value. 
     * @throws  SdpException if the stack format could not be created.
     */
    public Attribute encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException {
        return sdpFactory.createAttribute(name, null);
    }

    public String toString() {
        return name;
    }
}
