/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

/**
 * @author Mikael Andersson
 */
public class TextArray_P extends OperationBase {
    private String[] text;

    public TextArray_P(String[] text) {
        this.text = text;
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        ex.getValueStack().push(text);
    }

    public String arguments() {
        return arrayToString(text);
    }
}
