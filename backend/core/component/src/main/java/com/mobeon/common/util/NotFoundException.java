package com.mobeon.common.util;


/**
 * Exception to indicate something could not be found, i.e. the name of the data element, 
 * attribute, key, value, etc.  
 * 
 * 
 * @author lmchemc
 */
public class NotFoundException extends Exception {
    
    public NotFoundException(String msg) {
        super(msg);
    }
}