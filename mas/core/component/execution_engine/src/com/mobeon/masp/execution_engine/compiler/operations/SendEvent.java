/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

/**
 * @author Mikael Andersson
 */
public class SendEvent extends OperationBase {

    final private String event;
    private DebugInfo debugInfo;
    private String message;

    public SendEvent(String event, String message, DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
        this.event = event;
        this.message = message;
    }

    public void execute(ExecutionContext ex) throws InterruptedException {

        ex.getEventHub().fireContextEvent(event, message, debugInfo);
    }

    public String arguments() {
        return textArgument(event);
    }
}
