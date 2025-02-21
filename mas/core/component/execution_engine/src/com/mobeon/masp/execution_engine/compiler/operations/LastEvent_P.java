package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

/**
 * @author Mikael Andersson
 */
public class LastEvent_P extends OperationBase {
    public String arguments() {
        return "";
    }

    public void execute(ExecutionContext context) throws InterruptedException {
        if(context.getEventEntry() != null) {
            context.getValueStack().pushScriptValue(context.getEventEntry());
        }
    }
}
