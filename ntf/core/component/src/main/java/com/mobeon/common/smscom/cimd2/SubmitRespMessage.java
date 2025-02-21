/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import com.mobeon.common.smscom.Logger;
import java.util.*;

/**
 * SubmitRespMessage knows the format of a CIMD2 submit response message. This
 * class can only be used for <i>receiving</i> from the SMS-C, sending is not
 * supported.<P>
 */
public class SubmitRespMessage extends CIMD2RespMessage {

    String destinationAddress = null;
    String serviceCenterTimeStamp = null;


    /**
     * Constructor.
     * @param conn The CIMD2Com used for this message.
     */
    SubmitRespMessage(CIMD2Com conn) {
        super(conn);
        operationCode = CIMD2_SUBMIT_MESSAGE_RESP;
    }


    /**
     * parseBody parses the body of a bind transmitter response MESSAGE from the
     * buffer read from the SMS-C.
     */
    public void parseBody() {
        int par;

        pos = HEADER_SIZE;
        _errorCode = CIMD2_NO_ERROR;
        _errorText = null;
        destinationAddress = null;
        serviceCenterTimeStamp = null;

        while (buffer[pos] != CIMD2_ETX) {
            par = getInt(CIMD2_COLON);
            switch (par) {
            case CIMD2_ERROR_CODE:
                _errorCode = getInt(CIMD2_TAB);
                break;
            case CIMD2_ERROR_TEXT:
                _errorText = getTTS();
                break;
            case CIMD2_DESTINATION_ADDRESS:
                destinationAddress = getTTS();
                break;
            case CIMD2_SERVICE_CENTRE_TIME_STAMP:
                serviceCenterTimeStamp = getTTS();
                break;
            default:
                if (conn.willLog(Logger.LOG_DEBUG)) {
                    conn.getLogger().logString("CIMD2: unexpected parameter in submit response: " + par,
                                               Logger.LOG_DEBUG);
                }
                skipPast(CIMD2_TAB);
            }
        }
    }
}
