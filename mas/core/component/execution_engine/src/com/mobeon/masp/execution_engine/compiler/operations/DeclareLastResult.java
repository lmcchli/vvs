/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author David Looberger
 */
public class DeclareLastResult extends VXMLOperationBase {
    private String name;

    public DeclareLastResult(String name) {
        this.name = name;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        Scope scope = ex.getCurrentScope();
        ScriptableObject lastResult = ex.getLastResult();
        scope.declareReadOnlyVariable(name, lastResult);
    }

    public String arguments() {
        return name;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
