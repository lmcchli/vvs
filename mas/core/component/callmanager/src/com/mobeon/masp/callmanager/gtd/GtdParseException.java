package com.mobeon.masp.callmanager.gtd;

/**
 * Class for exception related to errors when parsing GTDs.
 */
public class GtdParseException extends Exception {

    public GtdParseException(String message, Throwable cause){
        super(message, cause);
    }
}
