/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework;

/**
 * The SMPPException class signals that an error has occurred in retrieving the SMPP parameter values. 
 */
public class SMPPException extends Exception {
    
    /**
     * Constructs an SMPPException with the specified detail message.
     * @param message the message containing the details of the error
     */
    public SMPPException(String message) {
        super(message);
    }
}
