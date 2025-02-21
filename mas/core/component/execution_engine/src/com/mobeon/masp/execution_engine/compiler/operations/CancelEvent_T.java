package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Operation;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

/**
 * @author Mikael Anderson
 */
public class CancelEvent_T extends OperationBase implements Operation  {
    public String arguments() {
        return "";
    }

    public void execute(ExecutionContext context) throws InterruptedException {
        String sendid = context.getValueStack().popAsString(context);
        context.getEventHub().cancel(sendid, null);
    }
}
