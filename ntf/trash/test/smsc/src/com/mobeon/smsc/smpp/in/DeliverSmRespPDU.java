/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc.smpp.in;

import com.mobeon.smsc.smpp.SMPP_PDU;
import com.mobeon.smsc.smpp.SMPPCom;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * DeliverSmRespPDU implements an SMPP delivery response PDU for 
 * receiving.
 */
public class DeliverSmRespPDU extends SMPP_PDU {

    /**
     * Constructor.
     * @param conn The SMPPConnection where this PDU shall be sent.
     */
    public DeliverSmRespPDU() {
	commandId = SMPPCMD_DELIVER_SM_RESP;
	commandLength = HEADER_SIZE + 1;
	commandStatus = 0;
    }
    /**
     * Constructor.
     */
    public DeliverSmRespPDU(int commandId) {
	this.commandId = commandId;                   
    }

    /**
     * Read the inputstream from the response. Then parse the content and log the 
     * PDU data in a log file.**/
    public boolean read(InputStream is) throws IOException {
        boolean status = super.read(is);
        parseBody();        
        return status;
    }
    
    /**
     * parseBody parses the body of a deliver SM response PDU from the buffer
     * read from the SMS-C.
     */
    public Object parseBody() {
	pos= 4*4;
	
	getNTS();//Ignore message ID
	getOptionalParameters();
        return null;
    }
}

