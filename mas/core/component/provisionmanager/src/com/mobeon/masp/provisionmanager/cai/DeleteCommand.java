/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.provisionmanager.cai;

/**
 * The DELETE CAI command
 *
 * @author ermmaha
 */
public class DeleteCommand extends CAICommand {
    private static final String DELETE = "DELETE";
    
    private String telephoneNumber;

    public DeleteCommand(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String toCommandString() {
        StringBuffer buf = new StringBuffer();
        buf.append(DELETE);
        buf.append(COLON);
        buf.append(MOIPSUB);
        buf.append(COLON);
        buf.append(CAISchema.TELEPHONENUMBER);
        buf.append(COMMA);
        buf.append(telephoneNumber);
        buf.append(SEMICOLON);

        return buf.toString();
    }
}
