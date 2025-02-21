package com.mobeon.smsc.smpp.out;

import com.mobeon.smsc.interfaces.TrafficCentral;
import com.mobeon.smsc.interfaces.SmppConstants;
import com.mobeon.smsc.smpp.SMSComException;
import com.mobeon.smsc.smpp.SMPP_PDU;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Implements an SMPP cancel response PDU for sending to the ESME.
 */
public class CancelResponsePDU extends SMPP_PDU implements SmppConstants {

    /* Constructor**/
    public CancelResponsePDU() {	
	commandId = SMPPCMD_CANCEL_SM_RESP;
	commandStatus = 0;         
    }
    
     /****************************************************************
     * getBuffer returns a byte array with a cancel response PDU.
     * @return the created buffer, or null (if there is not enough
     * information in the PDU yet to create a buffer).
     * @throws SMSComException if writing the bytes through a ByteArray stream
     * causes an IOException, if that could ever happen.
     */
    public byte[] getBuffer() {
	commandLength= 16;
	//sequenceNumber= whatever the caller has already set
	//Header-only PDU, so we can work with the header buffer
	putHeader(header);
	return header;
    }
}
