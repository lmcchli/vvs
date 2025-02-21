package com.mobeon.common.smscom.smpp;

/**
 * BindReceiverPDU knows about the format of an SMPP bind receiver PDU.
 * This class can only be used for <i>sending</i> to the SMS-C, receiving is not
 * supported.
 */
public class BindReceiverPDU extends BindPDU{

    /**
     * Constructor.
     * @param conn - the SMPPConnection where this PDU shall be sent.
     */
    BindReceiverPDU(SMPPCom conn) {
        super(conn, "bind_receiver_pdu");
        commandId = SMPPCMD_BIND_RECEIVER;
    }
}
