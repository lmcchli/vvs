/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.values.ECMAObjectValue;
import com.mobeon.masp.execution_engine.runtime.values.Visitors;

/** Take the top value of the value stack and inverse its boolean representation
 *
 * @author David Looberger
 */
public class Not_TP extends OperationBase {
    public String arguments() {
        return classToMnemonic(getClass());
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        Value value = ex.getValueStack().pop();
        Boolean inverseBoolVal = !((Boolean) value.accept(ex, Visitors.getAsBooleanVisitor()));
        ex.getValueStack().push(new ECMAObjectValue(inverseBoolVal));

    }
}
