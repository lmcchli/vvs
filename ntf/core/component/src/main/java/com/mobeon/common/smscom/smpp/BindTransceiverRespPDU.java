/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

import java.util.*;

/**
 * BindTransceiverRespPDU knows about the format of an SMPP bind transceiver
 * response PDU. It stores the message buffer and the values parsed from it.
 * This class can only be used for <i>receiving</i> from the SMS-C, sending is
 * not supported.<P>
 */
public class BindTransceiverRespPDU extends BindRespPDU {

    /**
     * Constructor.
     *@param conn The SMPPConnection where this PDU shall be sent.
     */
    BindTransceiverRespPDU(SMPPCom conn) {
        super(conn, "bind_transceiver_resp_pdu");
        commandId = SMPPCMD_BIND_TRANSCEIVER_RESP;
    }
}
