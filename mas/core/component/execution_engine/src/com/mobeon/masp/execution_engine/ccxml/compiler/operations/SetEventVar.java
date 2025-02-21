/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;

public class SetEventVar extends CCXMLOperationBase {
    final private String name;

    public SetEventVar(String name) {
        this.name = name;
    }

    public String arguments() {
        return textArgument(name);
    }

    public void execute(CCXMLExecutionContext ex) throws InterruptedException {
        ex.setEventVarName(name);
    }
}
