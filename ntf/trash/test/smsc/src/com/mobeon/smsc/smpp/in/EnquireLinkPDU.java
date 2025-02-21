/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc.smpp.in;

import com.mobeon.smsc.smpp.SMPP_PDU;
import com.mobeon.smsc.smpp.SMPPCom;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/****************************************************************
 * EnquireLinkPDU implements an SMPP enquire link PDU for sending and
 * receiving.
 ****************************************************************/
public class EnquireLinkPDU extends SMPP_PDU {

    /****************************************************************
     * Constructor.
     * @param conn The SMPPConnection where this PDU shall be sent.
     */
    public EnquireLinkPDU(SMPPCom conn) {
        //	super(conn);
	commandId= SMPPCMD_ENQUIRE_LINK;
	commandLength= HEADER_SIZE;
	commandStatus= 0;
    }


    /****************************************************************
     * getBuffer creates a buffer with an enquire link PDU.
     * @return the created buffer
     */
    public byte[] getBuffer() {
        //	sequenceNumber= conn.nextSequenceNumber();
	
	//Header-only PDU, so we can work with the header buffer
	putHeader(header);
	return header;
    }
}

