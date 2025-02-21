package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.values.Visitors;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class ChangeExecutionResult_T extends VXMLOperationBase {
    private static final ILogger logger = ILoggerFactory.getILogger(ChangeExecutionResult_T.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        Value value = ex.getValueStack().pop();
        Boolean val = ((Boolean) value.accept(ex, Visitors.getAsBooleanVisitor()));
        ExecutionResult state;
        if (val == Boolean.TRUE) {
            state = ExecutionResult.EVENT_WAIT;
        } else {
            state = ExecutionResult.DEFAULT;
        }

        if (logger.isDebugEnabled()) logger.debug("Setting state to " + state);
        // Verify that no events have arived that needs attention
        if (state == ExecutionResult.EVENT_WAIT &&
            ex.getEventProcessor().hasEventsInQ()) {
            if (logger.isDebugEnabled())
                logger.debug("There are unhandled events in the event queue, setting state to " + ExecutionResult.DEFAULT);
            ex.setExecutionResult(ExecutionResult.DEFAULT);
        } else {
            ex.setExecutionResult(state);
        }

    }

    public String arguments() {
        return "";
    }
}
