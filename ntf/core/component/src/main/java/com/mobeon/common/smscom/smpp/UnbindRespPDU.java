/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

/****************************************************************
 * UnbindRespPDU implements an SMPP unbind response PDU for
 * <i>receiving</i>.
 ****************************************************************/
public class UnbindRespPDU extends SMPP_PDU {
    
    /****************************************************************
     * Constructor.
     * @param conn the connection that uses this PDU instance.
     */
    UnbindRespPDU(SMPPCom conn) {
	super(conn, "unbind_resp_pdu", true);
    }
    
    
    /****************************************************************
     * getBuffer creates a buffer with an enquire link PDU.
     * @return the created buffer
     */
    public byte[] getBuffer() {
	commandLength= 16;
	commandId= SMPPCMD_UNBIND_RESP;
	commandStatus= 0x0;
	//sequenceNumber= whatever the caller has already set
	//Header-only PDU, so we can work with the header buffer
	putHeader(header);
	return header;
    }
}
