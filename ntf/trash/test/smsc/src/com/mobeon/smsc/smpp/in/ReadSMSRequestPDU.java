/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc.smpp.in;

import com.mobeon.smsc.smpp.SMSComException;
import com.mobeon.smsc.smpp.util.SMSPdu;
import com.mobeon.smsc.interfaces.TrafficCentral;
import com.mobeon.smsc.smpp.SMPP_PDU;

import java.io.InputStream;
import java.io.IOException;

import java.util.*;

/**
 * ReadSMSRequestPDU knows about the format of an SMPP submit transmitter
 * response PDU. It stores the message buffer and the values parsed from it.
 */
public class ReadSMSRequestPDU extends SMPP_PDU {
    
    /**
     * Constructor.
     */
    public ReadSMSRequestPDU() {
	commandId= SMPPCMD_SUBMIT_SM;
    }

    /*Reads the PDU data from the inputstream**/
    public boolean read(InputStream is) throws IOException {
        boolean status = super.read(is);
        parseBody();        
        return status;
    }
    
    /**
     * Reads a submit sm request PDU from the supplied input stream
     * and parses all parameters and saves them in a SMSPdu object 
     * wich is contained in the trafficInfo class.
     */
    public Object parseBody() {
	pos= 4*4;
	SMSPdu sms = new SMSPdu();
        sms.setCommandLength(commandLength);
        sms.setCommandId(commandId);
        sms.setCommandStatus(commandStatus);
        sms.setSequenceNumber(sequenceNumber);
        sms.setServiceType(getNTS());
        sms.setSourceAddrTon(getInt(1));
        sms.setSourceAddrNpi(getInt(1));
        sms.setSourceAddr(getNTS());
        sms.setDestAddrTon(getInt(1));
        sms.setDestAddrNpi(getInt(1));
        sms.setDestinationAddr(getNTS());
        sms.setEsmClass(getInt(1));
        sms.setProtocolId(getInt(1));
        sms.setPriorityFlag(getInt(1));
        sms.setSheduleDeliveryTime(getNTS());
        sms.setValidityPeriod(getNTS());
        sms.setRegisteredDelivery(getInt(1));
        sms.setReplaceIfPresentFlag(getInt(1));
        sms.setDataCoding(getInt(1));
        sms.setSmDefaultMsgId(getInt(1));
        sms.setSmLength(getInt(1));
	sms.setShortMessage(getString(sms.getSmLength()));
	getOptionalParameters();
        sms.setOptionalParameters(optionalParameters);  
        return sms;
    }    
    
    public String getMessageId() {return "Not Implemented";}
}

