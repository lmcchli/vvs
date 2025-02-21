/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

import java.util.*;

/**
 * BindTransceiverPDU knows about the format of an SMPP bind transceiver PDU.
 * This class can only be used for <i>sending</i> to the SMS-C, receiving is not
 * supported.
 */
public class BindTransceiverPDU extends BindPDU {

    /**
     * Constructor.
     *@param conn - the SMPPConnection where this PDU shall be sent.
     */
    BindTransceiverPDU(SMPPCom conn) {
        super(conn, "bind_transceiver_pdu");
        commandId = SMPPCMD_BIND_TRANSCEIVER;
    }
}
