/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

/****************************************************************
 * SubmitSmRespPDU implements an SMPP submit sm response PDU for receiveing from
 * the SMS-C.
 ****************************************************************/
public class SubmitSmRespPDU extends SMPP_PDU {
    String messageId= null;
    

    /****************************************************************
     * Constructor.
     * @param conn the connection that uses this PDU instance.
     */
    SubmitSmRespPDU(SMPPCom conn) {
	super(conn, "submit_sm_resp_pdu", true);
	commandId= SMPPCMD_SUBMIT_SM_RESP;
    }


    /****************************************************************
     * Read reads a submit sm response PDU from the supplied input stream
     * and parses all parameters.
     */
    public void parseBody() {
	pos= 4*4;

	messageId= getNTS();
    }
    
    public String getMessageId() {return messageId;}
}

