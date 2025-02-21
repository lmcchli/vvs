/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.provisionmanager.cai;

/**
 * Interface used to debug the CAI traffic sent between the CAIConnection and the CAI server
 *
 * @author ermmaha
 */
public interface CAICommSpy {
    /**
     * @param line data that is sent to the server.
     */
    public void println(String line);

    /**
     * @param line data that is read from the server.
     */
    public void readLine(String line);

    /**
     * @param msg general debug message
     */
    public void debug(String msg);
}
