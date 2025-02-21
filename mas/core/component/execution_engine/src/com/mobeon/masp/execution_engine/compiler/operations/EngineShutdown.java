/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;


public class EngineShutdown extends OperationBase {
    private boolean recursive;

    public EngineShutdown(boolean recursive) {
        this.recursive = recursive;
    }

    public void execute(ExecutionContext context) throws InterruptedException {
        context.shutdown(recursive);
    }
    public String arguments() {
        return String.valueOf(recursive);
    }
}
