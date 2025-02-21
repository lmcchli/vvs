/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistry;

public class NewScope extends OperationBase {

    String name;

    public NewScope(String name) {
        assert(name != null);
        this.name = name;
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        ScopeRegistry registry = ex.getScopeRegistry();
        registry.createNewScope(name);
    }

    public String arguments() {
        return "[" + name + "]";
    }
}
