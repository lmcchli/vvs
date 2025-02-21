/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import java.util.*;

/**
 * LogoutRespMessage knows the format of a CIMD2 logout response message. This
 * class can only be used for <i>receiving</i> from the SMS-C, sending is not
 * supported.<P>
 */
public class LogoutRespMessage extends CIMD2RespMessage {

    /**
     * Constructor.
     * @param conn The CIMD2Com used for this message.
     */
    LogoutRespMessage(CIMD2Com conn) {
        super(conn);
        operationCode = CIMD2_LOGOUT_RESP;
    }
}
