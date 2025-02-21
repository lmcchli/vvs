package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes the Product found in executionContext, or the "dialog product"
 */

public class ExecuteDialogTrampoline extends OperationBase {

    public String arguments() {
        return "";
    }

    public void execute(ExecutionContext context) throws InterruptedException {
        Product p = context.getAndResetProductToBeExecuted();
        if(p != null){
            context.getEngine().call(p);
        } else {
            context.getEngine().call(context.getExecutingModule().getSpecialProduct(Module.DIALOG_PRODUCT));
        }
    }
}
