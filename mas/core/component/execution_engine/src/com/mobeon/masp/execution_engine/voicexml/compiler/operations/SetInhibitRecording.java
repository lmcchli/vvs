/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class SetInhibitRecording extends VXMLOperationBase {
    private boolean inhibit;

    public SetInhibitRecording(boolean inhibit) {
        this.inhibit = inhibit;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.getFIAState().setInhibitRecording(inhibit);
    }

    public String arguments() {
        return inhibit ? "true" :"false";
    }
}
