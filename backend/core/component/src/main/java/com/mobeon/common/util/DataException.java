package com.mobeon.common.util;


/**
 * Exception to indicate that the given data is not correct. 
 * 
 * 
 * @author lmchemc
 */
public class DataException extends Exception {
    
    public DataException(String msg) {
        super(msg);
    }
}