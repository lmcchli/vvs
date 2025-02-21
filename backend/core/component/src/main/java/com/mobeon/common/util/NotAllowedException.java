package com.mobeon.common.util;


/**
 * Exception to indicate something is not allowed, i.e. state change is not allowed, 
 * request prevented, etc.  
 * 
 * 
 * @author lmchemc
 */
public class NotAllowedException extends Exception {
    
    public NotAllowedException(String msg) {
        super(msg);
    }
}