/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueStack;

/**
 * @author Mikael Andersson
 */
public class Text_P extends OperationBase {

    final String text;

    public Text_P(String text) {
        this.text = text;
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        ValueStack vs = ex.getValueStack();
        vs.push(text);
    }

    public String arguments() {
        return textArgument(text);
    }
}
