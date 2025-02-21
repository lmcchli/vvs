/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.provisionmanager.cai;

/**
 * Exception class for the CAI API
 *
 * @author ermmaha
 */
public class CAIException extends Exception {

    private int errorCode;

    /**
     * Constructor.
     *
     * @param msg
     */
    public CAIException(String msg) {
        super(msg);
    }

    /**
     * Constructor.
     *
     * @param msg
     * @param errorCode
     */
    public CAIException(String msg, int errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    /**
     * Retrieves the error code
     *
     * @return message
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Retrieves a message about the error
     *
     * @return message
     */
    public String getMessage() {
        if (errorCode == 0) {
            return super.getMessage();
        }
        String s = super.getMessage();
        StringBuffer buf = new StringBuffer(s != null ? s : "");
        buf.append(" (code=");
        buf.append(errorCode);
        buf.append(")");
        return buf.toString();
    }
}
