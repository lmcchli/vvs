/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import com.mobeon.common.smscom.SMSAddress;
import java.util.*;


/**
 * DeliverMessage implements a CIMD2 deliver message received from the SMS-C.
 */
public class DeliverMessage extends CIMD2Message {

    private SMSAddress _destinationAddress;
    private SMSAddress _originatorAddress;
    private String _userData;
    private int _protocolIdentifier;
    private int _dataCodingScheme;

    /**
     * Constructor.
     * @param conn the CIMD2Com used for this message.
     */
    public DeliverMessage(CIMD2Com conn) {
        super(conn);
    }

    /**
     * parseBody parses the body of a deliver message from the buffer read from
     * the SMS-C.
     */
    public void parseBody() {
        int par;
        String dummy;

        pos = HEADER_SIZE;
        _destinationAddress = null;
        _originatorAddress = null;
        _userData = null;
        _protocolIdentifier = -1;
        _dataCodingScheme = -1;

        while (buffer[pos] != CIMD2_ETX) {
            par = getInt(CIMD2_COLON);
            switch (par) {
            case CIMD2_DESTINATION_ADDRESS:
                _destinationAddress = parseCimd2Address(getTTS());
                break;
            case CIMD2_ORIGINATING_ADDRESS:
                _originatorAddress = parseCimd2Address(getTTS());
                break;
            case CIMD2_SERVICE_CENTRE_TIME_STAMP:
                dummy = getTTS();
                break;
            case CIMD2_USER_DATA_HEADER:
                dummy = getTTS();
                break;
            case CIMD2_USER_DATA:
                _userData = getTTS();
                break;
            case CIMD2_USER_DATA_BINARY:
                dummy = getTTS();
                break;
            case CIMD2_PROTOCOL_IDENTIFIER:
                _protocolIdentifier = getInt(CIMD2_TAB);
                break;
            case CIMD2_DATA_CODING_SCHEME:
                _dataCodingScheme = getInt(CIMD2_TAB);
                break;
            case CIMD2_ORIGINATING_IMSI:
                dummy = getTTS();
                break;
            case CIMD2_ORIGINATED_VISITED_MSC_ADDRESS:
                dummy = getTTS();
                break;
            default:
                if (conn.willLog(conn.getLogger().LOG_DEBUG)) {
                    conn.getLogger().logString("CIMD2: unexpected parameter in submit response: "
                                               + par, conn.getLogger().LOG_DEBUG);
                }
                skipPast(CIMD2_TAB);
            }
        }
    }

    /**
     * Get the destinationAddress .
     *@return the destinationAddress
     */
    public SMSAddress getDestinationAddress() { return _destinationAddress; }

    /**
     * Get the originatorAddress .
     *@return the originatorAddress
     */
    public SMSAddress getOriginatorAddress() { return _originatorAddress; }

    /**
     * Get the userData .
     *@return the userData
     */
    public String getUserData() { return _userData; }

    /**
     * Get the protocolIdentifier .
     *@return the protocolIdentifier
     */
    public int getProtocolIdentifier() { return _protocolIdentifier; }

    /**
     * Get the dataCodingScheme .
     *@return the dataCodingScheme
     */
    public int getDataCodingScheme() { return _dataCodingScheme; }

    public String toString() {
        return "{DeliverMessage:"
            + " to=" + _destinationAddress
            + " from=" + _originatorAddress
            + " msg=\"" + _userData + "\""
            + " pid=" + _protocolIdentifier
            + " dcs=" + _dataCodingScheme
            + "}";

    }
}
