/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.InputAggregator;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * Remove one DTMF token from the DTMF queue
 * @author David Looberger
 */
public class PopDtmfUtterance extends VXMLOperationBase {
    public String arguments() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        InputAggregator inputAggregator = ex.getInputAggregator();
        inputAggregator.getControlToken();
    }
}
