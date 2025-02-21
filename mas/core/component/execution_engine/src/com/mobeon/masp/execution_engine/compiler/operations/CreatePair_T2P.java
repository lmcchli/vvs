/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.values.Pair;

public class CreatePair_T2P extends OperationBase {
    public String arguments() {
        return "";
    }

    public void execute(ExecutionContext context) throws InterruptedException {
        ValueStack stack = context.getValueStack();
        String message = stack.pop().toString(context);
        String name = stack.pop().toString(context);
        Pair pair = new Pair(name, message);
        stack.pushScriptValue(pair);
    }
}
