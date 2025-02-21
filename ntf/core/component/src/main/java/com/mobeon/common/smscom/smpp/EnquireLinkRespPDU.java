/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

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
    EnquireLinkRespPDU(SMPPCom conn) {
	super(conn, "enquire_link_resp_pdu", true);
    }
    
    
    /****************************************************************
     * getBuffer creates a buffer with an enquire link PDU.
     * @return the created buffer
     */
    public byte[] getBuffer() {
	commandLength= 16;
	commandId=SMPPCMD_ENQUIRE_LINK_RESP;
	commandStatus= 0x0;
	//sequenceNumber= whatever the caller has already set
	//Header-only PDU, so we can work with the header buffer
	putHeader(header);
	return header;
    }
}
