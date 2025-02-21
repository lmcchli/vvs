package com.mobeon.common.smscom.smpp;

import com.abcxyz.messaging.oe.common.perfmgt.PerformanceData;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-mar-27
 * Time: 16:26:04
 */
public class CancelSmPDU extends SMPP_PDU {
    
    /**
     * *************************************************************
     * Constructor.
     *
     * @param conn - the connection that uses this PDU instance.
     */
    public CancelSmPDU(SMPPCom conn) {
        super(conn, "cancel_sm_pdu");
    }
    
    /**
     * getBuffer returns a byte array with a raw SMPP cancel sm message in the
     * form used for communicating with the SMSC.
     * @param from The destination address.
     * @param to The source address.
     * @param serviceType optional system type to cancel.
     * @return the read or created buffer, or null (if there is not enough
     * information in the PDU yet to create a buffer).
     */
    public byte[] getBuffer(SMSAddress to, SMSAddress from, String serviceType ) {
        ByteArrayOutputStream bos= new ByteArrayOutputStream();
        DataOutputStream dos= new DataOutputStream(bos);
        try {
            commandId= SMPPCMD_CANCEL_SM;
            commandStatus= 0;
            sequenceNumber= conn.nextSequenceNumber();
            writeHeader(dos);
            writeNTS(dos, serviceType);
            // empty messageid
            writeNTS(dos, null);
            if (from != null) {
                dos.writeByte(from.getTON());
                dos.writeByte(from.getNPI());
                writeNTS(dos, from.getNumber());
            } else {
                dos.writeByte(0);
                dos.writeByte(0);
                writeNTS(dos, "");
            }
            dos.writeByte(to.getTON());
            dos.writeByte(to.getNPI());
            writeNTS(dos, to.getNumber());


        } catch (IOException e) {
            ; //Should not happen on a ByteArrayOutputStream
        }

        byte[] buf= bos.toByteArray();
        //Must insert the correct length
        putInt(buf, 0, 4, buf.length);
        return buf;


    }
}
