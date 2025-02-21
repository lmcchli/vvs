/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sdp.SdpConstants;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.sdp.SdpParseException;
import com.mobeon.sdp.Origin;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SdpException;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This class represents the content of an Origin field (i.e. the 'o' field)
 * of an SDP.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class SdpOrigin {

    private static final ILogger LOG = ILoggerFactory.getILogger(SdpOrigin.class);

    private final String address;
    private final long sessionId;
    private final long sessionVersion;
    private final String userName;

    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();

    public SdpOrigin(String userName,
                     long sessionId,
                     long sessionVersion,
                     String address) {
        this.address = address;
        this.sessionId = sessionId;
        this.sessionVersion = sessionVersion;
        this.userName = userName;
    }

    public String getAddress() {
        return address;
    }

    public long getSessionId() {
        return sessionId;
    }

    public long getSessionVersion() {
        return sessionVersion;
    }

    public String getUserName() {
        return userName;
    }

    public String getAddressType() {
        return SdpConstants.ADDRESS_TYPE_IP4;
    }

    public String getNetworkType() {
        return SdpConstants.NETWORK_TYPE_IN;
    }

    public String toString() {
        String representation = stringRepresentation.get();

        if (representation == null) {
            representation = "<User name = " + userName +
                    ">, <SessionId = " + sessionId +
                    ">, <Session Version = " + sessionVersion +
                    ">, <Address = " + address + ">";
            stringRepresentation.set(representation);
        }

        return representation;
    }

    public Origin encodeToStackFormat(SdpFactory sdpFactory) throws SdpException {
        // "o=mas 0 0 IN IP4 <host for inbound Media Connection>"
        return sdpFactory.createOrigin(
                getUserName(),
                getSessionId(), getSessionVersion(),
                getNetworkType(),
                getAddressType(),
                getAddress());
    }

    /**
     * This method parses an <param>origin</param> field from an SDP
     * and returns a parsed representation of the origin field as an
     * <@link SdpOrigin>.
     * <p>
     * If the <param>origin</param> field could not be parsed an
     * {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.SD_PARAMETER_NOT_UNDERSTOOD}.
     *
     * @param   origin          An origin field retrieved from an SDP.
     * @return                  A parsed SdpOrigin. Null is returned if
     *                          <param>origin</param> is null.
     * @throws SdpNotSupportedException
     *                          If the Origin field could not be parsed.
     */
    public static SdpOrigin parseOrigin(Origin origin)
            throws SdpNotSupportedException {
        SdpOrigin sdpOrigin = null;

        if (LOG.isDebugEnabled())
            LOG.debug("Parsing origin: " + origin);

        if (origin != null) {

            try {
                sdpOrigin = new SdpOrigin(
                        origin.getUsername(), origin.getSessionId(),
                        origin.getSessionVersion(), origin.getAddress());

            } catch (SdpParseException e) {
                String message =
                        "Could not parse Origin field <" + origin +
                        "> received in remote SDP. The call will not be setup.";
                LOG.warn(message, e);
                throw new SdpNotSupportedException(
                        SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, message);
            }

        }

        return sdpOrigin;
    }


}
