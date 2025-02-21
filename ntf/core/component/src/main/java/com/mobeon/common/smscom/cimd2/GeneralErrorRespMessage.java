/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import java.io.InputStream;
import java.util.*;


/****************************************************************
 * GeneralErrorRespMessage knows the format of a CIMD2 general error response
 * message. This class can only be used for <i>receiving</i> from the SMS-C,
 * sending is not supported.<P>
 ****************************************************************/
public class GeneralErrorRespMessage extends CIMD2RespMessage {


    /****************************************************************
     * Constructor.
     * @param conn The CIMD2Com used for this message.
     */
    GeneralErrorRespMessage(CIMD2Com conn) {
	super(conn);
	operationCode= CIMD2_GENERAL_ERROR_RESP;
    }
}
