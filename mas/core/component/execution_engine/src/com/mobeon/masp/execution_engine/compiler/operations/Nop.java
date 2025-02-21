/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

/**
 * @author Mikael Andersson
 */
public class Nop extends OperationBase {
    public void execute(ExecutionContext ex) throws InterruptedException {
    }

    public String arguments() {
        return "";
    }
}
