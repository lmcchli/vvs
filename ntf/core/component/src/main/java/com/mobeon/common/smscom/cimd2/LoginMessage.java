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
 * LoginMessage knows the format of a CIMD2 login message. This class can only
 * be used for <i>sending</i> to the SMS-C, receiving is not supported.
 ****************************************************************/
public class LoginMessage extends CIMD2Message {

    /****************************************************************
     * Constructor.
     * @param conn The CIMD2Com used for this message.
     */
    LoginMessage(CIMD2Com conn) {
	super(conn);
    }
    
    
    /****************************************************************
     * getBuffer creates a buffer with a login message.
     * @return the created buffer
     */
    public byte[] getBuffer() {
	operationCode= CIMD2_LOGIN;
	packetNumber= conn.nextPacketNumber();
	
	ByteArrayOutputStream bos= new ByteArrayOutputStream(100);
	DataOutputStream dos= new DataOutputStream(bos);
	try {
	    writeHeader(dos);
	    writeInt(dos, 3, CIMD2_USER_IDENTITY, CIMD2_COLON);
	    writeTTS(dos, conn.getUserName());
	    writeInt(dos, 3, CIMD2_PASSWORD, CIMD2_COLON);
	    writeTTS(dos, conn.getPassword());
	    writeTrailer(dos);
	} catch (IOException e) {
	    //Since we write to a buffer with known size, this should never happen
	    return null;
	}

	return bos.toByteArray();
    }
}
