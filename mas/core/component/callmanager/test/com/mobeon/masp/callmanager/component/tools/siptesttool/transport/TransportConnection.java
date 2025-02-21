/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tools.siptesttool.transport;

/**
 * TODO: Document
 * @author Malin Nyfeldt
 */
public interface TransportConnection {

    public String waitForSipMessage() throws InterruptedException;    

}
