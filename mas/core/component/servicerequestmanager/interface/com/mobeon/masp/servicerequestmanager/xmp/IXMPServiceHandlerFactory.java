/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.xmp;

/**
 * Interface for a factory creating <code>IXMPServiceHandler</code>s.
 *
 * @author mmawi
 */
public interface IXMPServiceHandlerFactory {
    /**
     * Create a new XMP service handler.
     *
     * @return A new XMP service handler
     */
    IXMPServiceHandler create();
}
