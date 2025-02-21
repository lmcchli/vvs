/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.xmp;

import com.mobeon.common.xmp.server.IXmpAnswer;

/**
 * Interface for a XMP response queue.
 *
 * @author mmawi
 */
public interface IXMPResponseQueue {
    public String getClientId();
    public void addResponse(IXmpAnswer answer);
}
