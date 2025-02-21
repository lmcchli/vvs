package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class SetIsExiting extends VXMLOperationBase {
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.getFIAState().setIsExiting();
    }

    public String arguments() {
        return "";
    }
}
