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
 * AliveMessage knows the format of a CIMD2 alive message.
 ****************************************************************/
public class AliveMessage extends CIMD2Message {

    /****************************************************************
     * Constructor.
     * @param conn The CIMD2Com used for this message.
     */
    AliveMessage(CIMD2Com conn) {
	super(conn);
    }
    
    
    /****************************************************************
     * getBuffer creates a buffer with an alive message.
     * @return the created buffer
     */
    public byte[] getBuffer() {
	operationCode= CIMD2_ALIVE;
	packetNumber= conn.nextPacketNumber();
	
	ByteArrayOutputStream bos= new ByteArrayOutputStream(10);
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
