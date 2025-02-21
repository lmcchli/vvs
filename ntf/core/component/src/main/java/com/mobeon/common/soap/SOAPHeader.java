/*
 * SOAPHeader.java
 * Created on March 26, 2004, 3:27 PM
 */
package com.mobeon.common.soap;

/**
 * Models The SOAP Header element
 * A header entry is identified by it's fully qualified element name which contains
 * of the namespace URI and the local name.
 * @author  ermmaha
 */
public class SOAPHeader {
    
    /** Fully qualified element name */
    private SOAPQTag _qtag;
    
    public SOAPHeader(String name, String nameSpaceUri, String transactionID) {
        _qtag = new SOAPQTag(name, nameSpaceUri, transactionID);
    }
    
    public String getTransactionID() {
        return _qtag.getValue();
    }
    
    public String toString() {
        SOAPTag headerTag = new SOAPTag("env:Header", _qtag.toString());
        return headerTag.toString();
    }
}
