/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

/****************************************************************
 * BindRespPDU knows about the format of the SMPP bind responses in general.
 ****************************************************************/
public abstract class BindRespPDU extends SMPP_PDU {

    private String systemId= null;
    private int interfaceVersion= 0;

    /****************************************************************
     * Constructor.
     * @param conn The SMPPConnection where this PDU shall be sent.
     * @param eventName - the base name of the counter.
     */
    BindRespPDU(SMPPCom conn, String eventName) {
        super(conn, eventName, true);
    }

    
    /****************************************************************
     * parseBody parses the body of a bind transmitter response PDU from the
     * buffer read from the SMS-C.
     */
    public void parseBody() {
	interfaceVersion= -1;
	pos= 4*4;
	
	systemId= getNTS();
	getOptionalParameters();
	if (optionalParameters != null) {
	    Integer i= (Integer)(optionalParameters.get(new Integer(SMPPTAG_SC_INTERFACE_VERSION)));
	    if (i != null) {
		interfaceVersion= i.intValue();
	    }
	}
    }


    /**Gets the system id of the connected SMS-C. @return the system id.*/
    public String getSystemId() {return systemId;}

    /**Gets the SMPP version supported by the SMS-C. @return the SMPP version or -1 if the SMS-C did not tell.*/
    public int getInterfaceVersion() {return interfaceVersion;}
}
