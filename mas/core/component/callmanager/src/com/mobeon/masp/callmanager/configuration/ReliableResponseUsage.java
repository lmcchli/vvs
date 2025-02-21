/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

/**
 * Enumreation indicating the usage of reliable responses.
 * Provisional responses shall always be sent reliably if the value is
 * {@link ReliableResponseUsage.YES}.
 * Provisional responses shall never be sent reliably if the value is
 * {@link ReliableResponseUsage.NO}.
 * If the value is {@link ReliableResponseUsage.SDPONLY}, all provisional
 * responses carrying an SDP shall be sent reliably.
 *
 * @author Malin Nyfeldt
 */
public enum ReliableResponseUsage {
    YES,
    NO,
    SDPONLY;

    /**
     * Parses the configuration for reliable response usage and returns a
     * {@link ReliableResponseUsage}.
     * @param usage
     * @return Returns a {@link ReliableResponseUsage} indicating when
     * reliable responses shall be sent reliably. 
     */
    public static ReliableResponseUsage parseReliableResponseUsage(String usage) {
        ReliableResponseUsage result = ReliableResponseUsage.SDPONLY;
        if (usage != null) {
            if (usage.equals("yes")) result = ReliableResponseUsage.YES;
            else if (usage.equals("no")) result = ReliableResponseUsage.NO;
        }
        return result;
    }
}
