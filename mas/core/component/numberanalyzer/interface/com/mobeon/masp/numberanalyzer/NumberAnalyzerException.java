/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

/**
 * Exception class that is thrown when some error occurred during the number analysis
 *
 * @author ermmaha
 */
public class NumberAnalyzerException extends Exception {

    public static final String NORULE = "NORULE";
    public static final String NOMATCH = "NOMATCH";
    public static final String BLOCKED = "BLOCKED";

    private String reason = null;

    /**
     * Constructor
     *
     * @param msg detailed message about the error
     */
    public NumberAnalyzerException(String msg) {
        super(msg);
    }

    /**
     * Constructor
     *
     * @param msg    detailed message about the error
     * @param reason One of the strings NORULE, NOMATCH, BLOCKED
     */
    public NumberAnalyzerException(String msg, String reason) {
        super(msg);
        this.reason = reason;
    }

    /**
     * Return the reason string if set
     *
     * @return reason string, nulll if not set
     */
    public String getReason() {
        return reason;
    }
}
