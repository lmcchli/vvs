/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.values.Pair;

public class Pair_TP extends OperationBase {
    private String name;

    public Pair_TP(String name) { this.name = name;}

    public String arguments() {
        return textArgument(name);
    }

    public void execute(ExecutionContext context) throws InterruptedException {
        ValueStack valueStack = context.getValueStack();
        Object value = valueStack.pop().getValue();
        valueStack.pushScriptValue(new Pair(name,value));
    }

}
