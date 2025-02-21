package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class InitiatedDisconnect extends VXMLOperationBase {
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.setInitatedDisconnect();
    }

    public String arguments() {
        return "";
    }
}
