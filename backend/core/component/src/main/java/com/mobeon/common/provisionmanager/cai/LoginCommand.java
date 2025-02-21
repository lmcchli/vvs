/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.provisionmanager.cai;

/**
 * The LOGIN CAI command
 *
 * @author ermmaha
 */
public class LoginCommand extends CAICommand {
    private static final String LOGIN = "LOGIN";

    private String uid;
    private String pwd;

    /**
     * Constructor.
     *
     * @param uid
     * @param pwd
     */
    public LoginCommand(String uid, String pwd) {
        this.uid = uid;
        this.pwd = pwd;
    }

    public String toCommandString() {
        StringBuffer buf = new StringBuffer();
        buf.append(LOGIN);
        buf.append(COLON);
        buf.append(uid);
        buf.append(COLON);
        buf.append(pwd);
        buf.append(SEMICOLON);

        return buf.toString();
    }
}
