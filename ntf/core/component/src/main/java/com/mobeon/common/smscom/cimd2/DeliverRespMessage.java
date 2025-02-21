/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import java.util.*;

/**
 * DeliverRespMessage knows the format of a CIMD2 deliver response message.
 */
public class DeliverRespMessage extends CIMD2RespMessage {

    /**
     * Constructor.
     *@param conn The CIMD2Com used for this message.
     */
    DeliverRespMessage(CIMD2Com conn) {
        super(conn);
        operationCode = CIMD2_DELIVER_MESSAGE_RESP;
    }
}
