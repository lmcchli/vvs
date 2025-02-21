/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import com.mobeon.common.smscom.SMSAddress;
import java.io.IOException;
import java.util.*;


/**
 * DeliverStatusReportMessage implements a CIMD2 deliver status report message
 * received from the SMS-C.
 */
public class DeliverStatusReportMessage extends CIMD2Message {

    private SMSAddress _destinationAddress;
    private int _statusCode;
    private int _statusErrorCode;

    /**
     * Constructor.
     * @param conn the CIMD2Com used for this message.
     */
    public DeliverStatusReportMessage(CIMD2Com conn) {
        super(conn);
    }

    /**
     * parseBody parses the body of a deliverStatusReport message from the buffer read from
     * the SMS-C.
     */
    public void parseBody() {
        int par;
        String dummy;

        pos = HEADER_SIZE;
        _destinationAddress = null;
        _statusCode = -1;
        _statusErrorCode = -1;

        while (buffer[pos] != CIMD2_ETX) {
            par = getInt(CIMD2_COLON);
            switch (par) {
            case CIMD2_DESTINATION_ADDRESS:
                _destinationAddress = parseCimd2Address(getTTS());
                break;
            case CIMD2_SERVICE_CENTRE_TIME_STAMP:
                dummy = getTTS();
                break;
            case CIMD2_STATUS_CODE:
                _statusCode = getInt(CIMD2_TAB);
                break;
            case CIMD2_STATUS_ERROR_CODE:
                _statusErrorCode = getInt(CIMD2_TAB);
                break;
            case CIMD2_DISCHARGE_TIME:
                dummy = getTTS();
                break;
            default:
                if (conn.willLog(conn.getLogger().LOG_DEBUG)) {
                    conn.getLogger().logString("CIMD2: unexpected parameter in deliver status report response: "
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
     * Get the statusCode .
     *@return the statusCode
     */
    public int getStatusCode() { return _statusCode; }

    /**
     * Get the statusErrorCode .
     *@return the statusErrorCode
     */
    public int getStatusErrorCode() { return _statusErrorCode; }

    public String toString() {
        return "{DeliverStatusReportMessage:"
            + " to=" + _destinationAddress
            + " status=\"" + _statusCode + "\""
            + " status error=" + _statusErrorCode
            + "}";
        
    }
}
