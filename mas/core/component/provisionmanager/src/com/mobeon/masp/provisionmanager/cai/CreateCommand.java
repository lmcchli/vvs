/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.provisionmanager.cai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The CREATE CAI command
 *
 * @author ermmaha
 */
public class CreateCommand extends CAICommand {
    private static final String CREATE = "CREATE";

    private String telephoneNumber;
    private Map<String, String> attributes = new HashMap<String, String>();

    public CreateCommand(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public void addAttribute(String name, String value) {
        attributes.put(name, value);
    }

    public String toCommandString() {
        StringBuffer buf = new StringBuffer();
        buf.append(CREATE);
        buf.append(COLON);
        buf.append(MOIPSUB);
        buf.append(COLON);
        buf.append(CAISchema.TELEPHONENUMBER);
        buf.append(COMMA);
        buf.append(telephoneNumber);
        if (!attributes.isEmpty()) {
            addAttributesToCommand(buf);
        }
        buf.append(SEMICOLON);

        return buf.toString();
    }

    private void addAttributesToCommand(StringBuffer buf) {
        Iterator<String> it = attributes.keySet().iterator();
        while (it.hasNext()) {
            buf.append(COLON);
            String name = it.next();
            String value = attributes.get(name);
            buf.append(name);
            buf.append(COMMA);
            buf.append(value);
        }
    }
}
