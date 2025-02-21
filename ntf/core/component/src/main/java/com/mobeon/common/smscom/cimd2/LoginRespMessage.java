/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import java.io.InputStream;
import java.util.*;

/****************************************************************
 * LoginRespMessage knows the format of a CIMD2 login response message. This
 * class can only be used for <i>receiving</i> from the SMS-C, sending is not
 * supported.<P>
 ****************************************************************/
public class LoginRespMessage extends CIMD2RespMessage {


    /****************************************************************
     * Constructor.
     * @param conn The CIMD2Com used for this message.
     */
    LoginRespMessage(CIMD2Com conn) {
	super(conn);
	operationCode= CIMD2_LOGIN_RESP;
    }
}
