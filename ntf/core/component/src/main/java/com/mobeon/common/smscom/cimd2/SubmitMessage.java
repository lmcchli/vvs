/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * SubmitMessage implements an CIMD2 submit message for sending to the SMS-C.
 */
public class SubmitMessage extends CIMD2Message {

    private SimpleDateFormat cimd2Date;

    /**
     * Constructor.
     *@param conn the CIMD2Com used for this message.
     */
    public SubmitMessage(CIMD2Com conn) {
        super(conn);
        cimd2Date = new SimpleDateFormat ("yyMMddHHmmss");
        cimd2Date.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    /**
     *
     */
    private String cimd2RelDate(int seconds) {
        StringBuffer sb = new StringBuffer();
        
        //Hours (valid value 1-255)
        int hours = seconds / 3600;
        if (hours >= 255) { 
            sb.append("255");
        } 
        else if (hours < 1){
            sb.append("0");
        }
        else {
            sb.append(hours);
        }
        return sb.toString();
    }

    /**
     * getBuffer returns a byte array with a raw CIMD2 submit message in the
     * form used for communicating with the SMSC.
     *@param adr The destination address.
     *@param org The source address.
     *@param msg The message.
     *@return the read or created buffer, or null (if there is not enough
     * information in the message yet to create a buffer).
     */
    public byte[] getBuffer(SMSAddress adr, SMSAddress org, SMSMessage msg) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            operationCode = CIMD2_SUBMIT_MESSAGE;
            packetNumber = conn.nextPacketNumber();
            writeHeader(dos);
            writeAddress(dos, adr, org);

            /* data coding scheme (30) */
            writeInt(dos, 3, CIMD2_DATA_CODING_SCHEME, CIMD2_COLON);
            writeInt(dos, 3, msg.getDCS(), CIMD2_TAB);

            /* validity period relative & absolute (50&51) in hours*/
            if (msg.getExpiryTimeAbsolute() != null) {
                writeInt(dos, 3, CIMD2_VALIDITY_PERIOD_ABSOLUTE, CIMD2_COLON);
                writeTTS(dos, cimd2Date.format(msg.getExpiryTimeAbsolute()));
            } else if (msg.getExpiryTimeRelative() > 0) {
                writeInt(dos, 3, CIMD2_VALIDITY_PERIOD_RELATIVE, CIMD2_COLON);
                writeTTS(dos, "" + msg.getExpiryTimeRelative());
            } else {
                ;
            }
            
            /* protocol identifier (52) */
            writeInt(dos, 3, CIMD2_PROTOCOL_IDENTIFIER, CIMD2_COLON);
            writeInt(dos, 3, msg.getPID(), CIMD2_TAB);
            
            /* first delivery time relative (53) in seconds */
            if (msg.getScheduledDeliveryTime() > 0) {
            	writeInt(dos, 3, CIMD2_FIRST_DELIVERY_TIME_RELATIVE, CIMD2_COLON);
                writeTTS(dos, cimd2RelDate(msg.getScheduledDeliveryTime()));
            } 

            /* user data (33)*/
            writeInt(dos, 3, CIMD2_USER_DATA, CIMD2_COLON);
            dos.write(msg.getText(), msg.getPosition(), msg.getLength());
            dos.write(CIMD2_TAB);

            /* replay path (55)*/
            if (msg.getUseReplyPath()) {
                writeInt(dos, 3, CIMD2_REPLY_PATH, CIMD2_COLON);
                writeInt(dos, 1, 1, CIMD2_TAB);
            }

            /* status report request (56)*/
            if (msg.getDeliveryReceipt()) {
                writeInt(dos, 3, CIMD2_STATUS_REPORT_REQUEST, CIMD2_COLON);
                writeInt(dos, 2, 2 | 4 | 8 | 32, CIMD2_TAB);
            }

            /* send as cancel enabled (58) unless phone on */
            if( msg.getPID() != 0x40 ) {
                writeInt(dos, 3, CIMD2_CANCEL_ENABLED, CIMD2_COLON);
                writeInt(dos, 1, 1, CIMD2_TAB);
            }

            /* cimd2 priority (67)*/
            if( msg.getPriority() >= 1 && msg.getPriority() <= 9 ) {
                writeInt(dos, 3, CIMD2_PRIORITY, CIMD2_COLON);
                writeInt(dos, 1, msg.getPriority(), CIMD2_TAB);
            }

            writeTrailer(dos);
        } catch (IOException e) {
            ; //Should not happen on a ByteArrayOutputStream
        }

        return bos.toByteArray();
    }
}

