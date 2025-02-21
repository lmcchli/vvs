/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.values.Visitors;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class UnwindOrContinue_TP   extends VXMLOperationBase {
    private Product unwindPoint;

    public UnwindOrContinue_TP(Product unwindPoint) {
        this.unwindPoint = unwindPoint;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        Value v = ex.getValueStack().pop();
        if ((Boolean) v.accept(ex, Visitors.getAsBooleanVisitor())){
            // Unwind to the specified Product
            ex.getEngine().unwind(unwindPoint, false);
            ex.getValueStack().pushScriptValue(Boolean.FALSE);
        }
        else {
          ex.getValueStack().pushScriptValue(Boolean.TRUE);
        }
    }

    public String arguments() {
        return unwindPoint.identity();
    }
}
