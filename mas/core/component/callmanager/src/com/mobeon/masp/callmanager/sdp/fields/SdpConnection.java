/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.fields;

import com.mobeon.sdp.Connection;
import com.mobeon.sdp.SdpParseException;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SdpException;

import com.mobeon.masp.callmanager.sdp.SdpConstants;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * This class represents the content of a Connection field (i.e. the 'c' field)
 * of an SDP.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class SdpConnection {

    public static final String HOLD_IP_ADDRESS = "0.0.0.0";

    private static final ILogger LOG = ILoggerFactory.getILogger(SdpConnection.class);

    private final String address;

    public SdpConnection(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public String getAddressType() {
        return SdpConstants.ADDRESS_TYPE_IP4;
    }

    public String getNetworkType() {
        return SdpConstants.NETWORK_TYPE_IN;
    }

    public boolean isConnectionAddressZero() {
        boolean result = false;
        if (!getAddress().equals(null) && (getAddress().length() > 0) && 
                (getAddress().startsWith(HOLD_IP_ADDRESS))) 
            result = true;
        return result;
    }
    
    public String toString() {
        return address;
    }

    /**
     * Encodes the connection into an SDP stack format using the
     * <param>sdpFactory</param>.
     * A {@link Connection} is returned with the network type set to "IN", the
     * address type set to "IP4" and the connection address..
     * @param   sdpFactory
     * @return  A stack formatted connection.
     * @throws  SdpException if the stack connection could not be created.
     */
    public Connection encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException {
        // "c=IN IP4 <address>"
        return sdpFactory.createConnection(
                getNetworkType(),
                getAddressType(),
                getAddress());
    }

    /**
     * This method parses a <param>connection</param> field from an SDP
     * and returns a parsed representation of the connection field as an
     * <@link SdpConnection>.
     * <p>
     * The network type is validated using
     * {@link #validateNetworkType(Connection)},
     * the address type is validated using
     * {@link #validateAddressType(Connection)},
     * and the address is validated using
     * {@link #validateAddress(Connection)}.
     * If not supported, an
     * {@link SdpNotSupportedException} is thrown as described for respective
     * method.
     * <p>
     * If the <param>connection</param> field could not be parsed an
     * {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.SD_PARAMETER_NOT_UNDERSTOOD}.
     *
     * @param   connection      A connection field retrieved from an SDP.
     * @return                  A parsed SdpConnection. Null is returned if
     *                          <param>connection</param> is null.
     * @throws SdpNotSupportedException
     *                          If the Connection field could not be parsed or
     *                          the network type, address type or address is
     *                          unsupported (i.e. was not validated ok).
     */
    public static SdpConnection parseConnection(Connection connection)
            throws SdpNotSupportedException {
        SdpConnection sdpConnection = null;

        if (LOG.isDebugEnabled())
            LOG.debug("Parsing connection: " + connection);

        if (connection != null) {

            try {
                validateNetworkType(connection);
                validateAddressType(connection);
                validateAddress(connection);

                sdpConnection = new SdpConnection(connection.getAddress());

            } catch (SdpParseException e) {
                String message =
                        "Could not parse Connection field <" + connection +
                        "> received in remote SDP. The call will not be setup.";
                LOG.warn(message, e);
                throw new SdpNotSupportedException(
                        SipWarning.SD_PARAMETER_NOT_UNDERSTOOD, message);
            }

        }

        if (LOG.isDebugEnabled())
            if (sdpConnection != null)
                LOG.debug("Parsed connection address: " + sdpConnection.getAddress());

        return sdpConnection;
    }


    //======================== Private Methods ======================

    /**
     * Validates the address of the <param>connection</param>.
     * <p>
     * A unicast address looks like this:   126.16.64.4
     * A multicast address looks like this: 224.2.1.1/<ttl>/<number of addresses>
     * Only unicast addresses are supported, i.e. the address MUST NOT contain
     * any TTL or number of addresses.
     * <p>
     * If the <param>connection</param> address is a multicast address
     * an {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.MULTICAST_NOT_AVAILABLE}.
     * <p>
     * If the <param>connection</param> could not be parsed, an
     * {@link SdpParseException} is thrown.
     *
     * @param   connection                  MUST NOT be null.
     * @throws  SdpParseException           If the connection could not be parsed.
     * @throws  SdpNotSupportedException    If the address is not supported,
     *                                      i.e. is a multicast address.
     */
    private static void validateAddress(Connection connection)
            throws SdpParseException, SdpNotSupportedException {

        int ttl = connection.getAddressTtl();
        int count = connection.getAddressCount();

        if (LOG.isDebugEnabled())
            LOG.debug("Connection TTL = " + ttl +
                    ", Number of addresses = " + count);

        if ((ttl > -1) || (count > 1)) {
            String message = "Address received in remote SDP is not " +
                    "supported since it is a multicast address. Only " +
                    "unicast is supported. The call will not be setup.";
            LOG.warn(message);
            throw new SdpNotSupportedException(
                    SipWarning.MULTICAST_NOT_AVAILABLE, message);
        }
    }

    /**
     * Validates the address type of the <param>connection</param>.
     * <p>
     * Only the address type "IP4" is supported.
     * If the <param>connection</param> contains an unsupported address type,
     * an {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.INCOMPATIBLE_NETWORK_ADDRESS_FORMAT}.
     * <p>
     * If the <param>connection</param> could not be parsed, an
     * {@link SdpParseException} is thrown.
     *
     * @param   connection                  MUST NOT be null.
     * @throws  SdpParseException           If the connection could not be parsed.
     * @throws  SdpNotSupportedException    If the address type is not supported,
     *                                      i.e. differs from "IP4".
     */
    private static void validateAddressType(Connection connection)
            throws SdpParseException, SdpNotSupportedException {

        String addressType = connection.getAddressType();

        if ((addressType == null) ||
                (!addressType.equals(SdpConstants.ADDRESS_TYPE_IP4))) {
            String message =
                    "Address type \"" + addressType +
                    "\" received in remote SDP is not supported. " +
                    "Only \"" + SdpConstants.ADDRESS_TYPE_IP4 +
                            "\" is supported. The call will not be setup.";
            LOG.warn(message);
            throw new SdpNotSupportedException(
                    SipWarning.INCOMPATIBLE_NETWORK_ADDRESS_FORMAT, message);
        }
    }

    /**
     * Validates the network type of the <param>connection</param>.
     * <p>
     * Only the network type "IN" is supported. If the <param>connection</param>
     * contains an unsupported network type, an {@link SdpNotSupportedException}
     * is thrown with {@link SipWarning.INCOMPATIBLE_NETWORK_PROTOCOL}.
     * <p>
     * If the <param>connection</param> could not be parsed, an
     * {@link SdpParseException} is thrown.
     *
     * @param   connection                  MUST NOT be null.
     * @throws  SdpParseException           If the connection could not be parsed.
     * @throws  SdpNotSupportedException    If the network type is not supported,
     *                                      i.e. differs from "IN".
     */
    private static void validateNetworkType(Connection connection)
            throws SdpParseException, SdpNotSupportedException {

        String networkType = connection.getNetworkType();

        if ((networkType == null) ||
                (!networkType.equals(SdpConstants.NETWORK_TYPE_IN))) {
            String message =
                    "Network type \"" + networkType +
                    "\" received in remote SDP is not supported. " +
                    "Only \"" + SdpConstants.NETWORK_TYPE_IN +
                            "\" is supported. The call will not be setup.";
            LOG.warn(message);
            throw new SdpNotSupportedException(
                    SipWarning.INCOMPATIBLE_NETWORK_PROTOCOL, message);
        }
    }

}
