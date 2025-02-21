/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * BindPDU is an abstract class that knows about SMPP binding in general.
 */
public abstract class BindPDU extends SMPP_PDU {

    /**
     * Constructor.
     *@param conn - the SMPPConnection where this PDU shall be sent.
     */
    BindPDU(SMPPCom conn) {
        super(conn);
        commandStatus = 0;
    }

    /**
     * Constructor.
     * @param conn - the SMPPConnection where this PDU shall be sent.
     * @param eventName - the base name of the counter.
     */
    BindPDU(SMPPCom conn, String eventName) {
        super(conn, eventName);
        commandStatus = 0;
    }
    

    /**
     * getBuffer creates a buffer with a bind transmitter PDU.
     *@return the created buffer
     */
    public byte[] getBuffer() {
        commandLength =
            HEADER_SIZE
            + conn.getUserName().length()
            + conn.getPassword().length()
            + (conn.getSystemType() != null ? conn.getSystemType().length(): 0)
            + 1 // interface_version
            + 1 // addr ton
            + 1 // addr_npi
            + conn.getAddressRange().getNumber().length()
            + 4; // null terminators (username, password, systemtype and addressrange)
        sequenceNumber = conn.nextSequenceNumber();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(commandLength);
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            writeHeader(dos);
            writeNTS(dos, conn.getUserName());
            writeNTS(dos, conn.getPassword());
            writeNTS(dos, conn.getSystemType());
            dos.writeByte(conn.getVersion());
            dos.writeByte(conn.getAddressRange().getTON());
            dos.writeByte(conn.getAddressRange().getNPI());
            writeNTS(dos, conn.getAddressRange().getNumber());
        } catch (IOException e) {
            //Since we write to a buffer with known size, this should never happen
            return null;
        }
        return bos.toByteArray();
    }
}
