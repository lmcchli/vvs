/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

import java.util.*;

/**
 * DeliverSmRespPDU implements an SMPP deliver sm repsonse PDU for sending.
 * It is possible to call getBuffer on the same instance over and
 * over to generate new messages, or a new instance can be created for each
 * message.
 */
public class DeliverSmRespPDU extends SMPP_PDU {

    /**
     * Constructor.
     *@param conn - the connection that uses this PDU instance.
     */
    DeliverSmRespPDU(SMPPCom conn) {
        super(conn, "deliver_sm_resp_pdu", true);
    }
    
    
    /**
     * getBuffer creates a buffer with an deliver sm PDU.
     *@return the created buffer
     */
    public byte[] getBuffer() {
        byte[] buf = new byte[HEADER_SIZE + 1];
        commandLength = HEADER_SIZE + 1;
        commandId = SMPPCMD_DELIVER_SM_RESP;
        commandStatus = 0x0;
        //sequenceNumber = whatever the caller has already set
        putHeader(buf);
        putInt(buf, HEADER_SIZE, 1, 0);
        return buf;
    }
}
