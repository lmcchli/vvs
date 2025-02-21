/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.frontend.rtp;

import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-jan-17
 * Time: 11:31:34
 * To change this template use File | Settings | File Templates.
 */
public class PortFactory {
    private static final PortFactory instance = new PortFactory();
    protected  Object mutex = new Object();
    protected  int PORTBASE = 22220;
    protected  int nextAllocatedPort = PORTBASE;
    private Logger logger = Logger.getLogger("com.mobeon");

    protected PortFactory() {
    }

    public static PortFactory createInstance() {
        return instance;
    }

    // TODO: Keep track of all allocated portnumbers in a collection,
    // and reuse portnumbers when thay become free.
    public int allocatePortNumber() {
        synchronized(mutex) {
            int ret = this.nextAllocatedPort;
            this.nextAllocatedPort += 2;
            logger.debug("Allocating port " + ret);
            return ret;
        }
    }
}
