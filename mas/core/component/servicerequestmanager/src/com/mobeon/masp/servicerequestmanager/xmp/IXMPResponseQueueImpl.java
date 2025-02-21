/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.xmp;

import com.mobeon.common.xmp.server.IXmpAnswer;
import com.mobeon.common.xmp.server.XmpResponseQueue;

/**
 * Implemetation of the XMP response queue interface. Wrapper for
 * foundation Java XmpResponseQueue.
 *
 * @author mmawi
 */
public class IXMPResponseQueueImpl implements IXMPResponseQueue {
    XmpResponseQueue responseQueue;

    public IXMPResponseQueueImpl(XmpResponseQueue responseQueue) {
        this.responseQueue = responseQueue;
    }

    public String getClientId() {
        return responseQueue.getClientId();
    }

    public void addResponse(IXmpAnswer answer) {
        responseQueue.addResponse(answer);
    }
}
