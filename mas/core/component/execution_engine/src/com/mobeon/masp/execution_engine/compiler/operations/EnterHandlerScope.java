/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

/**
 * @author David Looberger
 */
public class EnterHandlerScope extends OperationBase {
    public String arguments() {
        return "";
    }

    public void execute(ExecutionContext context) throws InterruptedException {
        context.getHandlerLocator().enteredScope(null);
    }
}
