/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class SetRecordingStopped extends VXMLOperationBase {
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.getCall().setIsRecording(false);
    }

    public String arguments() {
        return "";
    }
}
