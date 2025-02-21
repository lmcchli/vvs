/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

/**
 * BindTransmitterRespPDU knows about the format of an SMPP bind transmitter
 * response PDU. It stores the message buffer and the values parsed from it.
 * This class can only be used for <i>receiving</i> from the SMS-C, sending is
 * not supported.<P>
 */
public class BindTransmitterRespPDU extends BindRespPDU {

    /**
     * Constructor.
     *@param conn - the SMPPConnection where this PDU shall be sent.
     */
    BindTransmitterRespPDU(SMPPCom conn) {
        super(conn, "bind_transmitter_resp_pdu");
        commandId = SMPPCMD_BIND_TRANSMITTER_RESP;
    }
}
