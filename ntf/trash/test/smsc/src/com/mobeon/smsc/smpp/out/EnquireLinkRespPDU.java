/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc.smpp.out;

import com.mobeon.smsc.smpp.SMPP_PDU;
import com.mobeon.smsc.smpp.SMPPCom;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/****************************************************************
 * EnquireLingRespPDU implements an SMPP enquire link repsonse PDU for sending
 * and receiving. It is possible to call getBuffer on the same instance over and
 * over to generate new messages, or a new instance can be created for each
 * message.
 ****************************************************************/
public class EnquireLinkRespPDU extends SMPP_PDU {
	

    /****************************************************************
     * Constructor.
     * @param conn the connection that uses this PDU instance.
     */
    public EnquireLinkRespPDU(SMPPCom conn) {
        //	super(conn);
    }
    
    
    /****************************************************************
     * getBuffer creates a buffer with an enquire link PDU.
     * @return the created buffer
     */
    public byte[] getBuffer() {
	commandLength= 16;
	commandId=SMPPCMD_ENQUIRE_LINK_RESP;
	//sequenceNumber= whatever the caller has already set
	//Header-only PDU, so we can work with the header buffer
	putHeader(header);
	return header;
    }
}
