/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.provisionmanager.cai;

/**
 * The LOGOUT CAI command
 *
 * @author ermmaha
 */
public class LogoutCommand extends CAICommand {
    private static final String LOGOUT = "LOGOUT";

    static final LogoutCommand LOGOUTCOMMAND = new LogoutCommand();

    public String toCommandString() {
        StringBuffer buf = new StringBuffer();
        buf.append(LOGOUT);
        buf.append(SEMICOLON);

        return buf.toString();
    }
}
