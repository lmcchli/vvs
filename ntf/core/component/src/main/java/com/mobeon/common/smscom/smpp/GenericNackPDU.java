/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

import java.util.*;

/****************************************************************
 * GenericNackPDU implements an SMPP generic nack PDU for sending and
 * receiving.
 ****************************************************************/
public class GenericNackPDU extends SMPP_PDU {

    /****************************************************************
     * Constructor.
     *@param conn - the SMPPConnection where this PDU shall be sent.
     */
    GenericNackPDU(SMPPCom conn) {
        super(conn, "generic_nack_pdu");
        commandId = SMPPCMD_GENERIC_NACK;
        commandLength = HEADER_SIZE;
        commandStatus = 0;
    }


    /****************************************************************
     * getBuffer creates a buffer with an enquire link PDU.
     * Sequence number and command status must be set before getting
     * the buffer.
     *@return the created buffer
     */
    public byte[] getBuffer() {
        //Header-only PDU, so we can work with the header buffer
        putHeader(header);
        return header;
    }
}

