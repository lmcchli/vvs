package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.values.Visitors;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jan 2, 2006
 * Time: 6:08:21 PM
 * To change this template use File | Settings | File Templates.
 * <p/>
 * This class unwinds the engine stack to a certain point and then calls
 * products. Both the unwind point and the products-to-be-called are
 * expected to be found on the value stack.
 */

public class UnwindAndCall_TM_T extends OperationBase {
    private static final ILogger log = ILoggerFactory.getILogger(UnwindAndCall_TM_T.class);

    private boolean unwindSuppliedProduct;

    /**
     * Creates an UnwindAndCall_TM_T
     *
     * @param unwindSuppliedProduct if true, the engine stack is unwound
     *                              up to AND INCLUDING the unwindPoint. Otherwise just up to.
     */
    public UnwindAndCall_TM_T(boolean unwindSuppliedProduct) {
        this.unwindSuppliedProduct = unwindSuppliedProduct;
    }

    /**
     * @param ex
     * @throws InterruptedException
     * @logs.error "Unwinding failed <message>" - Unwinding an internal data structure failed. <message> should give more information about the problem.
     */
    public void execute(ExecutionContext ex) throws InterruptedException {

        List<Executable> ops = new ArrayList<Executable>();

        // On top of stack we find the Products to be called. There may
        // be no such products.

        List<Value> values = ex.getValueStack().popToMark();
        for (int i = values.size() - 1; i >= 0; i--) {
            Value v = values.get(i);
            Executable productToBeCalled = (Executable)v.toObject(ex);
            ops.add(productToBeCalled);
        }

        // Then on stack we find the unwind target.

        Value v = ex.getValueStack().pop();
        Product unwindPoint =
                (Product) v.accept(ex, Visitors.getAsObjectVisitor());

        if (! ex.getEngine().unwind(unwindPoint, unwindSuppliedProduct)) {
            logUnwindError(ex, unwindPoint);
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                    "Unwinding failed.", DebugInfo.getInstance());
        } else {
            if (ops.size() > 0) {
                ex.call(ops, unwindPoint);
            }
        }
    }

    private void logUnwindError(ExecutionContext ex, Product unwindPoint) {
        Product p = ex.getExecutingModule().getProduct();
        DebugInfo debugInfo = p.getDebugInfo();
        String tagName = p.getDebugInfo().getTagName();
        Object location = debugInfo.getLocation();
        log.error("Unwinding failed. " + ex.getExecutingModule().getDocumentURI() + ", " + tagName + ", " + location +
                ", unwindPoint: " + unwindPoint.getDebugInfo());
    }

    public String arguments() {
        return "";
    }
}
