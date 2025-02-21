/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/****************************************************************
 * LogoutMessage knows the format of a CIMD2 logout message. This class can only
 * be used for <i>sending</i> to the SMS-C, receiving is not supported.
 ****************************************************************/
public class LogoutMessage extends CIMD2Message {

    /****************************************************************
     * Constructor.
     * @param conn The CIMD2Com used for this message.
     */
    LogoutMessage(CIMD2Com conn) {
	super(conn);
    }
    
    
    /****************************************************************
     * getBuffer creates a buffer with a logout message.
     * @return the created buffer
     */
    public byte[] getBuffer() {
	operationCode= CIMD2_LOGOUT;
	packetNumber= conn.nextPacketNumber();
	
	ByteArrayOutputStream bos= new ByteArrayOutputStream(100);
	DataOutputStream dos= new DataOutputStream(bos);
	try {
	    writeHeader(dos);
	    writeTrailer(dos);
	} catch (IOException e) {
	    //Since we write to a buffer with known size, this should never happen
	    return null;
	}

	return bos.toByteArray();
    }
}
