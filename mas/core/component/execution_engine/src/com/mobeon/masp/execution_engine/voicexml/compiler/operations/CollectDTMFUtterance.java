/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Collect DTMF utterance
 *
 * @author David Looberger
 */
public class CollectDTMFUtterance extends VXMLOperationBase {
    String formItemName;
    private static ILogger logger = ILoggerFactory.getILogger(CollectDTMFUtterance.class);
    private boolean justMatch = false;
    private boolean sendDTMFWakeup = false;

    public CollectDTMFUtterance() {
        this.formItemName = null;
    }

    public CollectDTMFUtterance(String formItemName, boolean justMatch, boolean sendDTMFWakeup) {
        this.formItemName = formItemName;
        this.justMatch = justMatch;
        this.sendDTMFWakeup = sendDTMFWakeup;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.getFIAState().collectDTMFUtterance(ex, justMatch, true, sendDTMFWakeup);
    }

    public String arguments() {
        return "";
    }
}
