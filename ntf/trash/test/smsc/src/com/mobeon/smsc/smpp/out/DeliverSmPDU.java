/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc.smpp.out;

import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.smsc.smpp.SMPP_PDU;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * DeliverSmPDU implements an SMPP submit sm PDU for sending to the SMS-C.
 */
public class DeliverSmPDU extends SMPP_PDU {
    
    private static int seq = 1;
    /**
     * Constructor.
     */
    public DeliverSmPDU() {
        commandId= SMPPCMD_DELIVER_SM;
        commandStatus= 0;
    }

    /**
     * getBuffer returns a byte array with a raw SMPP submit sm message in the
     * form used for communicating with the SMSC.
     * @param adr The destination address.
     * @param msg The message.
     * @return the read or created buffer, or null (if there is not enough
     * information in the PDU yet to create a buffer).
     * @throws SMSComException if writing the bytes through a ByteArray stream
     * causes an IOException, if that could ever happen.
     */
    public byte[] getBuffer(SMSAddress adr, SMSAddress org, SMSMessage msg) throws Exception {
        ByteArrayOutputStream bos= new ByteArrayOutputStream();
        DataOutputStream dos= new DataOutputStream(bos);
        try {
            commandId= SMPPCMD_DELIVER_SM;
            commandStatus= 0;
            sequenceNumber= seq++;
            writeHeader(dos);
            writeNTS(dos, msg.getServiceType());
            if (org != null) {
                dos.writeByte(org.getTON());
                dos.writeByte(org.getNPI());
                writeNTS(dos, org.getNumber());
            } else {
                dos.writeByte(0);
                dos.writeByte(0);
                writeNTS(dos, "");
            }
            dos.writeByte(adr.getTON());
            dos.writeByte(adr.getNPI());
            writeNTS(dos, adr.getNumber());
            dos.writeByte(0x4);
            dos.writeByte(0);
            dos.writeByte(0); //Priority
            writeNTS(dos, ""); //Scheduled NA
            writeNTS(dos, ""); //Validity NA
            dos.writeByte(0); //Not Registered
            dos.writeByte(0); //Replace NA
            dos.writeByte(0); //Default DCS
            dos.writeByte(0); //No canned default message
            dos.writeByte(0); //No message
            //dos.write(msg.getText(), msg.getPosition(), msg.getLength());


            /*==== Optional parameters ================================*/
            //user_message_reference
            //source_port
            //source_addr_subunit
            //destination_port
            //dest_addr_subunit
            //sar_msg_ref_num
            //sar_total_segments
            //sar_segment_seqnum
            //more_messages_to_send
            //payload_type
            //message_payload
            //privacy_indicator
            //callback_num
            //callback_num_pres_ind
            //callback_num_atag
            //source_subaddress
            //dest_subaddress
            //user_response_code
            //display_time
            //sms_signal
            //signal
            //ms_validity
            //ms_msg_wait_facilities
            //number_of_messages
            //alert_on_msg_delivery
            //      Not supported by Abcxyz SMS-C sms_signal used instead
            //language_indicator
            //its_reply_type
            //its_session_info
            //ussd_service_op
        } catch (IOException e) {
            throw new Exception("SMPPConnection IOException when making submit sm buffer: " + e.getMessage());
        }

        byte[] buf= bos.toByteArray();
        //Must insert the correct length
        putInt(buf, 0, 4, buf.length);
        return buf;

    }
}

