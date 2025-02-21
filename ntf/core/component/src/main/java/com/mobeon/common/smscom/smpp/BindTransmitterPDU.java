/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

import java.util.*;

/**
 * BindTransmitterPDU knows about the format of an SMPP bind transmitter PDU.
 * This class can only be used for <i>sending</i> to the SMS-C, receiving is not
 * supported.
 */
public class BindTransmitterPDU extends BindPDU {

    /**
     * Constructor.
     *@param conn - the SMPPConnection where this PDU shall be sent.
     */
    BindTransmitterPDU(SMPPCom conn) {
        super(conn, "bind_transmitter_pdu");
        commandId = SMPPCMD_BIND_TRANSMITTER;
    }
}
