/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/** Sets the state (executionResult) of the ExecutionContext to the specified state, 
 * e.g. in order to wait for an event to arrive, or to set the state to default once the event
 * has arrived.
 *
 * @author David Looberger
 */
public class ChangeExecutionResult extends VXMLOperationBase  {
    private static final ILogger logger = ILoggerFactory.getILogger(ChangeExecutionResult.class);
    private ExecutionResult state;


    public ChangeExecutionResult(ExecutionResult state) {
        this.state = state;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
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
        return state.toString();

    }
}
