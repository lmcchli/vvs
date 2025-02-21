/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc.smpp.in;

import com.mobeon.smsc.smpp.SMPP_PDU;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;

/**
 * ReadCancelRequestPDU knows about the format of an SMPP cancel 
 * reqeust PDU. It stores the message buffer and the values parsed from it.
 */
public class ReadCancelRequestPDU extends SMPP_PDU {
            
    /* SMPP PDU parameters used to print the cancel PDU request
     * See <I>Short Message Peer to Peer Protocol Specification</I> v3.4 for 
     * details.
     **/
    private String serviceType= null;
    private String messageId = null;
    private int sourceAddrTon = -1;
    private int sourceAddrNpi = -1;
    private String sourceAddr = null;
    private int destAddrTon = -1;
    private int destAddrNpi = -1;
    private String destinationAddr = null;
    
    /**Used to log cancel PDU request*/
    Logger log = null;    

    /**
     * Constructor.
     */
    public ReadCancelRequestPDU() {
	commandId = SMPPCMD_CANCEL_SM;                   
    }

    /*Read the inputstream from the cancel PDU request. Then parse the content and log the 
     * PDU data in a log file.**/
    public boolean read(InputStream is) throws IOException {
        boolean status = super.read(is);
        parseBody();        
        return status;
    }
    
    /**
     * parseBody parses the body of a cancel request PDU
     */
    public Object parseBody() {
	pos= 4*4;
	
	serviceType = getNTS();
        messageId = getNTS();
	sourceAddrTon = getInt(1);
	sourceAddrNpi = getInt(1);
        sourceAddr = getNTS();
	destAddrTon = getInt(1);
	destAddrNpi = getInt(1);
        destinationAddr = getNTS();

	getOptionalParameters();

        return null;
    }

    /*Parses the PDU content to human readable plain text**/
    public String printRequest(){
        StringBuffer cancel = new StringBuffer(1000);        
        cancel.append("Command length: " + commandLength + "\n");
        cancel.append("Command id: " + Integer.toHexString(commandId) + "\n");
        cancel.append("Command status: " + nullValue(commandStatus) + "\n");
        cancel.append("Sequence number: " + sequenceNumber + "\n");
        cancel.append("Service Type: " + serviceType + "\n");
        cancel.append("Message ID: " + messageId + "\n");
        cancel.append("Source Address TON : " + sourceAddrTon + "(" + getTonValue(sourceAddrTon) + ")\n");
        cancel.append("Source Address NPI: " +  sourceAddrNpi + "(" + getNpiValue(sourceAddrNpi) + "\n");
        cancel.append("Source Address: " + nullValue(sourceAddr) + "\n");
        cancel.append("Destination Address TON : " + destAddrTon + "(" + getTonValue(destAddrTon) + ")\n");
        cancel.append("Destination Address NPI: " +  destAddrNpi + "(" + getNpiValue(destAddrNpi) + "\n");
        cancel.append("Destination Address: " + nullValue(destinationAddr) + "\n");
        return cancel.toString();
    }        
    private String nullValue(int i){ return (i==0)?null:""+i; }    
    private String nullValue(String i){ return (i.length()==0)?null:""+i; }
    
          
    private final int UNKNOWN =           0;
    
    /** TON */
    private final int TON_INTERNATIONAL = 1;
    private final int TON_NATIONAL =      2;
    private final int NETWORK_SPECIFIC =  3;
    private final int SUBSCRIBER_NUMBER = 4;
    private final int ALPHANUMERIC =      5;
    private final int ABBREVIATED =       6;    
    private String getTonValue(int ton){
        switch(ton){
            case UNKNOWN:
                return "Unknown";
            case TON_INTERNATIONAL:
                return "International";
            case TON_NATIONAL:
                return "National";
            case NETWORK_SPECIFIC:
                return "Network specific";
            case SUBSCRIBER_NUMBER:
                return "Subscriber number";
            case ALPHANUMERIC:
                return "Alphanumeric";
            case ABBREVIATED:
                return "Abbreviated";
            default:
                return "Not defined";
        }
    }
    
    /** NPI*/
    private final int ISDN =              1;
    private final int DATA =              3;
    private final int TELEX =             4;
    private final int LAND_MOBILE =       6;
    private final int NPI_NATIONAL =      8;
    private final int PRIVATE =           9;
    private final int ERMES =             10; 
    private final int INTERNET =          14; 
    private final int WAP =               18;
    private String getNpiValue(int npi){            
        switch(npi){
            case UNKNOWN:
                return "Unknown";
            case ISDN:
                return "Isdn";
            case DATA:
                return "Data";
            case TELEX:
                return "Telex";
            case LAND_MOBILE:
                return "Land mobile";
            case NPI_NATIONAL:
                return "National";
            case PRIVATE:
                return "Private";
            case ERMES:
                return "ERMES";                            
            case INTERNET:
                return "Internet";
            case WAP:
                return "Wap";
            default:
                return "Not defined";                
        }
    }
    /**
     * Get the serviceType .
     *@return the serviceType
     */
    public String getServiceType() { return serviceType; }

    /**
     * Get the messageId .
     *@return the messageId
     */
    public String getMessageId() { return messageId; }

    /**
     * Get the sourceAddrTon .
     *@return the sourceAddrTon
     */
    public int getSourceAddrTon() { return sourceAddrTon; }

    /**
     * Get the sourceAddrNpi .
     *@return the sourceAddrNpi
     */
    public int getSourceAddrNpi() { return sourceAddrNpi; }

    /**
     * Get the sourceAddr .
     *@return the sourceAddr
     */
    public String getSourceAddr() { return sourceAddr; }

    /**
     * Get the destAddrTon .
     *@return the destAddrTon
     */
    public int getDestAddrTon() { return destAddrTon; }

    /**
     * Get the destAddrNpi .
     *@return the destAddrNpi
     */
    public int getDestAddrNpi() { return destAddrNpi; }

    /**
     * Get the destinationAddr .
     *@return the destinationAddr
     */
    public String getDestinationAddr() { return destinationAddr; }
}
