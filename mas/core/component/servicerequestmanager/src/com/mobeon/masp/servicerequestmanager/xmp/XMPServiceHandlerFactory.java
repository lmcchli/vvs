/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.xmp;

/**
 * Implementation of the <code>IXMPServiceHandlerFactory</code> interface.
 * 
 * @author mmawi
 */
public class XMPServiceHandlerFactory implements IXMPServiceHandlerFactory {
    public IXMPServiceHandler create() {
        return new XMPServiceHandler();
    }
}
