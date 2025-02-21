package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.products.FormPredicate;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jan 30, 2006
 * Time: 10:38:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class CatchUnwind extends VXMLOperationBase {

    static ILogger logger = ILoggerFactory.getILogger(CatchUnwind.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        FIAState fiaState = ex.getFIAState();
        FIAState.Phase phase = fiaState.getPhase();
        if(phase == FIAState.Phase.Initialization ||
                phase == FIAState.Phase.Select){
            fiaState.errorOccured(true);
            fiaState.setFIADone();
        }
        Product executingForm = ex.getExecutingForm();
        if(executingForm == null || (! (executingForm instanceof FormPredicate))){
            badfetch(ex);
            return;
        }
        FormPredicate form = (FormPredicate) executingForm;
        Product unwindPoint = form.getFormItemProduct();
        boolean unwindSuppliedProduct = false;
        ex.getEngine().unwind(unwindPoint, unwindSuppliedProduct);
    }


    private void badfetch(ExecutionContext context) {
        String msg = "The unwind point was null or of unexpected type";
        if (logger.isDebugEnabled()) logger.debug(msg);
        context.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH,
                msg, null);
    }

    public String arguments() {
        return "";
    }
}
