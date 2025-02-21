/*
 * SOAPParseException.java
 * Created on March 29, 2004, 5:52 AM
 */
package com.mobeon.common.soap;

/**
 * Exception thrown when some eccor occurred when parsing a SOAP message
 * @author  ermmaha
 */
public class SOAPParseException extends Exception {
    
    /** Creates a new instance of SOAPParseException */
    public SOAPParseException(String message) {
        super(message);
    }
}
