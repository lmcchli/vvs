/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.servicerequestmanager.xmp;

import com.mobeon.common.xmp.client.XmpResult;
import com.mobeon.common.xmp.client.XmpResultHandler;

/**
 * Date: 2006-feb-07
 *
 * @author ermmaha
 */
public class XMPResultHandler implements XmpResultHandler {
    private XmpResult xmpResult;
    private boolean resultRetrived = false;

    public XMPResultHandler() {

    }

    public synchronized void waitForResult(int milliseconds) {
        try {
            if (xmpResult == null) {
                wait(milliseconds);
            }
            resultRetrived = true;
        } catch (InterruptedException e) {
            //ignore
        }
    }

    public synchronized void handleResult(XmpResult xmpResult) {
        if (resultRetrived) return;

        this.xmpResult = xmpResult;
        notify();
    }

    public XmpResult getXmpResult() {
        return xmpResult;
    }
}
