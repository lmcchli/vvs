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
 * ReadBindRequestPDU knows about the format of an SMPP bind transmitter
 * response PDU. It stores the message buffer and the values parsed from it.
 */
public class ReadBindRequestPDU extends SMPP_PDU {
            
    /* SMPP PDU parameters used to print the bind PDU request
     * See <I>Short Message Peer to Peer Protocol Specification</I> v3.4 for 
     * details.
     **/
    private String systemId= null;
    private String password = null;
    private String system_type = null;
    private int inteface_version = -1;
    private int addr_ton = -1;
    private int addr_npi = -1;
    private String address_range = null;
    private int interfaceVersion= 0;
    
    /**Used to log bind PDU request*/
    Logger log = null;    

    /**
     * Constructor.
     */
    public ReadBindRequestPDU(int commandId) {
	this.commandId = commandId;                   
    }

    /*Read the inputstream from the bind PDU request. Then parse the content and log the 
     * PDU data in a log file.**/
    public boolean read(InputStream is) throws IOException {
        boolean status = super.read(is);
        parseBody();        
        return status;
    }
    
    /**
     * parseBody parses the body of a bind transmitter response PDU from the
     * buffer read from the SMS-C.
     */
    public Object parseBody() {
	interfaceVersion= -1;
	pos= 4*4;
	
	systemId = getNTS();//uid
        password = getNTS();//pwd
        system_type = getNTS();//type
        interfaceVersion = getInt(1);//version
        addr_ton = getInt(1);//interface
        addr_npi = getInt(1);//addr_ton
        address_range = getNTS();//range

	getOptionalParameters();
	if (optionalParameters != null) {
	    Integer i= (Integer)(optionalParameters.get(new Integer(SMPPTAG_SC_INTERFACE_VERSION)));
	    if (i != null) {
		interfaceVersion= i.intValue();
	    }
	}
        
        if( log == null){ log = Logger.getLogger(system_type); }
        if (log.isLoggable(Level.FINE)) {            
            log.fine("ESME bindrequest: " + printRequest());                                         
        }
        return null;
    }

    /*Parses the PDU content to human readable plain text**/
    public String printRequest(){
        StringBuffer bind = new StringBuffer(1000);        
        bind.append("Command length: " + commandLength + "\n");
        bind.append("Command id: " + Integer.toHexString(commandId) + "\n");
        bind.append("Command status: " + nullValue(commandStatus) + "\n");
        bind.append("Sequence number: " + sequenceNumber + "\n");
        bind.append("System id: " + systemId + "\n");
        bind.append("Password: " + password + "\n");
        bind.append("System type: " + nullValue(system_type) + "\n");
        bind.append("Interface version: " + Integer.toHexString(inteface_version) + "\n");
        bind.append("Addr ton: " + addr_ton + "(" + getTonValue(addr_ton) + ")\n");
        bind.append("Addr npi: " +  addr_npi + "(" + getNpiValue(addr_npi) + "\n");
        bind.append("Address range: " + nullValue(address_range) + "\n");
        return bind.toString();
    }        
    private String nullValue(int i){ return (i==0)?null:""+i; }    
    private String nullValue(String i){ return (i.length()==0)?null:""+i; }
    
    /**Gets the system id of the connected ESME. 
     * @return the system id.
     */
    public String getSystemId() {return systemId;}

    /**Gets the SMPP version supported by the ESME. 
     *@return the SMPP version or -1 if the SMS-C did not tell.
     */
    public String getInterfaceVersion() {return "0x"+Integer.toHexString(interfaceVersion);}
    
    /*Gets the system_type of the connected ESME.
     *@return the system_type
     **/
    public String getSystemType() {return system_type;}
    
    /**
     *Gets the password of the connected ESME. 
     *@return the password
     */
    public String getPassword() {return password;}
          
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
}
