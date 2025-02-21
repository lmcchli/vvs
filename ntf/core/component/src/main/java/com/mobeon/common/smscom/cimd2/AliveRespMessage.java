/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import java.util.*;

/**
 * AliveRespMessage knows the format of a CIMD2 alive response message.
 */
public class AliveRespMessage extends CIMD2RespMessage {


    /**
     * Constructor.
     *@param conn The CIMD2Com used for this message.
     */
    AliveRespMessage(CIMD2Com conn) {
        super(conn);
        operationCode = CIMD2_ALIVE_RESP;
    }
}
