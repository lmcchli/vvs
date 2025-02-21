/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.provisionmanager.cai;

/**
 * Abstract base class for CAI Commands.
 * The toCommandString method must be implemented by deriving classes.
 *
 * @author ermmaha
 */
public abstract class CAICommand {
    static final String MOIPSUB = "MOIPSUB";
    static final String SEMICOLON = ";";
    static final String COLON = ":";
    static final String COMMA = ",";

    /**
     * @return a CAI protocol command string
     */
    public abstract String toCommandString();
}
