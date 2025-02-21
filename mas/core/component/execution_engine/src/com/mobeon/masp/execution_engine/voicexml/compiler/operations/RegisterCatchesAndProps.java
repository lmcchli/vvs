package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.InputItemImpl;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class RegisterCatchesAndProps extends VXMLOperationBase {
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        Product item = ex.getFIAState() .getNextItem().product;
        if (item instanceof InputItemImpl) {
            InputItemImpl inputItem = (InputItemImpl) item;
            Product catches = inputItem.getCatches();
            ex.getEngine().call(catches);
        }
    }

    public String arguments() {
        return "";
    }
}
