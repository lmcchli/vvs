/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import java.io.InputStream;
import java.util.*;


/****************************************************************
 * NackMessage knows the format of a CIMD2 NACK message. This class can only be
 * used for <i>receiving</i> from the SMS-C, sending is not supported.<P>
 ****************************************************************/
public class NackMessage extends CIMD2Message {


    /****************************************************************
     * Constructor.
     * @param conn The CIMD2Connection where this message shall be sent.
     */
    NackMessage(CIMD2Com conn) {
	super(conn);
	operationCode= CIMD2_NACK;
    }


    /****************************************************************
     * parseBody parses the body of a NACK message from the buffer read from the
     * SMS-C.
     */
    public void parseBody() {
	int par;

	pos= HEADER_SIZE;
	
	while (buffer[pos] != CIMD2_ETX) {
	    par= getInt(CIMD2_COLON);
	    switch (par) {
	    default:
		skipPast(CIMD2_TAB);
	    }
	}
    }
}
