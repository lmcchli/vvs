/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;

public class UseStateVariable extends CCXMLOperationBase {
    final private String varname;

    public UseStateVariable(String varname) {
        this.varname = varname;
    }

    public String arguments() {
        return textArgument(varname);
    }

    public void execute(CCXMLExecutionContext ex) throws InterruptedException {
        ex.bindStateTo(varname);
    }
}
