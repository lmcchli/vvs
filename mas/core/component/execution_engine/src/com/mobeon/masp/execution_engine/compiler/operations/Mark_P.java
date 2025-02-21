/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

/**
 * @author Mikael Andersson
 */
public class Mark_P extends OperationBase {
    public void execute(ExecutionContext ex) throws InterruptedException {
        ex.getValueStack().pushMark();
    }

    public String arguments() {
        return "";
    }
}
