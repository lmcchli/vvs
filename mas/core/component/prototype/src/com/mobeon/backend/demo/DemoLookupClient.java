/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.backend.demo;

import com.mobeon.backend.LookupClient;

import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-16
 * Time: 12:46:25
 * To change this template use File | Settings | File Templates.
 */
public class DemoLookupClient implements LookupClient {
    public DemoLookupClient() {
    }
    /** Returns the session variables for the requested client if such exist, otherwise null
     *
     * @param cnum Identifies the local user to search for
     * @return Returns the session variables for the requested user if such exist, otherwise null
     */
    public Hashtable getSubInfo(String cnum ) {
        Hashtable ret = new Hashtable();
        ret.put("HOST","mail.host.com");
        ret.put("EMAIL","demo@host.com");
        ret.put("PASSWORD","dummy");
        ret.put("COS","max");
        return ret;
    }
}
