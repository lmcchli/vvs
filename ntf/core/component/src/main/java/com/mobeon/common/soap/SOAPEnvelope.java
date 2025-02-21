/*
 * SOAPEnvelope.java
 * Created on March 26, 2004, 3:26 PM
 */
package com.mobeon.common.soap;

/**
 * Represents a SOAP Envelope, which contains a SOAP Header and a SOAP Body
 * @author  ermmaha
 */
public class SOAPEnvelope {
    //What is this variable called ?
    private static String SCHEMA_URL_STRING =
        "xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\"";
    
    private SOAPHeader _header;
    private SOAPBody _body;
    
    /**
     * Constructor
     */
    public SOAPEnvelope() {
    }
    
    /**
     * Prints out the complete envelope (with header and body) on the SOAP format
     * @return String
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        if(_header != null) {
            buf.append(_header.toString());
        }
        if(_body != null) {
            buf.append(_body.toString());
        }
        
        SOAPTag envTag = new SOAPTag("env:Envelope", buf.toString());
        envTag.addAttribute(SCHEMA_URL_STRING);
        
        return "<?xml version=\"1.0\" ?>" + envTag.toString();
    }
    
    /**
     * Sets a SOAPHeader object
     * @param SOAPHeader
     */
    public void setHeader(SOAPHeader header) {
        _header = header;
    }
    
    /**
     * @return the SOAPHeader
     */
    public SOAPHeader getHeader() {
        return _header;
    }
    
    /**
     * Sets a SOAPBody object
     * @param SOAPBody
     */
    public void setBody(SOAPBody body) {
        _body = body;
    }
    
    /**
     * @return the SOAPBody
     */
    public SOAPBody getBody() {
        return _body;
    }
}
