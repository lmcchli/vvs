/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.values.Pair;

public class Pair_P extends OperationBase {
    private Pair pair;

    public Pair_P(String name, String value) {
        pair = new Pair(name,value);
    }


    public String arguments() {
        return textArgument(pair.getName())+", "+textArgument(pair.getValue().toString());
    }

    public void execute(ExecutionContext context) throws InterruptedException {
        context.getValueStack().pushScriptValue(pair);
    }
}
