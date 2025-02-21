/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.provisionmanager.cai;

/**
 * The GET CAI command
 *
 * @author ermmaha
 */
public class GetCommand extends CAICommand {
    private static final String GET = "GET";

    private String attrName;
    private String attrValue;

    public GetCommand(String attrName, String attrValue) {
        this.attrName = attrName;
        this.attrValue = attrValue;
    }

    public String toCommandString() {
        StringBuffer buf = new StringBuffer();
        buf.append(GET);
        buf.append(COLON);
        buf.append(MOIPSUB);
        buf.append(COLON);
        buf.append(attrName);
        buf.append(COMMA);
        buf.append(attrValue);
        buf.append(SEMICOLON);

        return buf.toString();
    }
}
