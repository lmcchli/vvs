package com.mobeon.smsc.smpp.out;

import com.mobeon.smsc.interfaces.TrafficCentral;
import com.mobeon.smsc.interfaces.SmppConstants;
import com.mobeon.smsc.smpp.SMSComException;
import com.mobeon.smsc.smpp.SMPP_PDU;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * SubmitSMSResponsePDU implements an SMPP submit response bind PDU for sending to the ESME.
 */
public class BindResponsePDU extends SMPP_PDU implements SmppConstants {

    /* Constructor**/
    public BindResponsePDU(int commandId) {	
	this.commandId = commandId;
	commandStatus = 0;         
    }
    
     /****************************************************************
     * getBuffer returns a byte array with a raw SMPP submit sm message response in the
     * form used for communicating with the ESME.
     * @param int the sequenceNumber sent in the received SMS.
     * @param String The message_id, used by the ESME to query the status of a message. This is not implemented
     * always set "Not Implemented".
     * @return the created buffer, or null (if there is not enough
     * information in the PDU yet to create a buffer).
     * @throws SMSComException if writing the bytes through a ByteArray stream
     * causes an IOException, if that could ever happen.
     */
    public byte[] getBuffer(String system_type, int sequenceNumber, int command_status) throws SMSComException{
        this.sequenceNumber = sequenceNumber;                
        this.commandStatus = command_status;
	ByteArrayOutputStream bos= new ByteArrayOutputStream();
	DataOutputStream dos= new DataOutputStream(bos);
	try {
	    writeHeader(dos);
	    writeNTS(dos, system_type);
	} catch (IOException e) {
	    //Since we write to a buffer with known size, this should never happen
	    return null;
	}
	byte[] buf= bos.toByteArray();
	putInt(buf, 0, 4, buf.length);
	return buf;
    }    
}
