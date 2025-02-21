/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;

/**
 * Assigns a value from the stack (using peek) to a name in the
 * current context.
 *
 * @author Patrick Zeits
 */
public class AssignECMAVar extends OperationBase {
    String formItemName;
    public AssignECMAVar(String name) {
        super();
        formItemName = name;
    }

    public String arguments() {
        return formItemName;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
      ValueStack stack = ex.getValueStack();
        Object ecmaValue = stack.peek().toECMA(ex);
        Scope scope = ex.getCurrentScope();
        if(! scope.isDeclaredInAnyScope(formItemName)) {
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                    formItemName + " is not declared",
                    DebugInfo.getInstance());
            return;
        }
        Object wrappedEcmaObject = scope.javaToJS(ecmaValue);
        scope.setValue(formItemName, wrappedEcmaObject);    }
}
