package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueStack;

/**
 * @author Mikael Andersson
 */
public class SendEvent_T2 extends OperationBase {
    private DebugInfo debugInfo;

    public SendEvent_T2(DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();
        String message = stack.popAsString(ex);
        String event = stack.popAsString(ex);
        ex.getEventHub().fireContextEvent(event, message, debugInfo);
    }

    public String arguments() {
        return "";
    }
}
