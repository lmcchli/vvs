package com.mobeon.common.smscom.smpp;


/**
 * BindReceiverRespPDU knows about the format of an SMPP bind receiver
 * response PDU. It stores the message buffer and the values parsed from it.
 * This class can only be used for <i>receiving</i> from the SMS-C, sending is
 * not supported.<P>
 */
public class BindReceiverRespPDU extends BindRespPDU {

    BindReceiverRespPDU(SMPPCom conn) {
        super(conn, "bind_receiver_resp_pdu");
        commandId = SMPPCMD_BIND_RECEIVER_RESP;
    }

}
