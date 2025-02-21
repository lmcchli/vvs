/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

public class Reference extends OperationBase  {
    private Product reference;

    public Reference(Product filledItems) {
        this.reference = filledItems;
    }

    public String arguments() {
        return reference.toString();
    }

    public void execute(ExecutionContext context) throws InterruptedException {
        reference.execute(context);
    }
}
