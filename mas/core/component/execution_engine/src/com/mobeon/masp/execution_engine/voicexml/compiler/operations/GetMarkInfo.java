/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * Operation used to propagate the last known markname an marktime to the ECMA environment as entities
 * on the application.lastresult$ object.
 * @author David Looberger
 */
public class GetMarkInfo extends VXMLOperationBase {
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.getFIAState().getMarkInfo(ex);
    }

    public String arguments() {
        return "";
    }
}
