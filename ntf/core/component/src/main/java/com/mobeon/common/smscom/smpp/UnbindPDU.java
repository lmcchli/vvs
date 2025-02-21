/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/****************************************************************
 * UnbindPDU implements an SMPP unbind PDU for <i>sending</i>.
 ****************************************************************/
public class UnbindPDU extends SMPP_PDU {

    /****************************************************************
     * Constructor.
     * @param conn The SMPPConnection where this PDU shall be sent.
     */
    UnbindPDU(SMPPCom conn) {
	super(conn, "unbind_pdu");
	commandId= SMPPCMD_UNBIND;
	commandLength= HEADER_SIZE;
	commandStatus= 0;
    }


    /****************************************************************
     * getBuffer creates a buffer with an unbind PDU.
     * @return the created buffer.
     */
    public byte[] getBuffer() {
	sequenceNumber= conn.nextSequenceNumber();

	//Header-only PDU, so we can owrk with the header buffer
	putHeader(header);
	return header;
    }
}

