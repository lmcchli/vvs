/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc.smpp.out;

import com.mobeon.smsc.smpp.SMSComException;
import com.mobeon.smsc.config.Config;

import com.mobeon.smsc.smpp.SMPP_PDU;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * SubmitSMSResponsePDU implements an SMPP submit response sm PDU for sending to the ESME.
 */
public class SubmitSMSResponsePDU extends SMPP_PDU {   

    private static Random rand = new Random();
    /**
     * Constructor.
     */
    public SubmitSMSResponsePDU() {
	commandId= SMPPCMD_SUBMIT_SM_RESP;
	commandStatus= 0;
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
    public byte[] getBuffer(int sequenceNumber, String message_id) throws SMSComException {
	ByteArrayOutputStream bos= new ByteArrayOutputStream();
	DataOutputStream dos= new DataOutputStream(bos);
	this.sequenceNumber = sequenceNumber;
        if( commandStatus == 0 && rand.nextInt(100) < Config.getPercentError() ) {
            commandStatus = 0x14;
        }
	try {            
	    writeHeader(dos);
            writeNTS(dos, message_id);
	} catch (IOException e) {
	    throw new SMSComException("SMPPConnection IOException when making submit sm buffer: " + e.getMessage());
	}	
	byte[] buf= bos.toByteArray();
	putInt(buf, 0, 4, buf.length);
	return buf;
    }
}

