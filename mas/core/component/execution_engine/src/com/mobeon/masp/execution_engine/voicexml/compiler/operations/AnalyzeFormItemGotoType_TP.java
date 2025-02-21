package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.products.FormPredicate;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.values.Visitors;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jan 14, 2006
 * Time: 3:27:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnalyzeFormItemGotoType_TP extends VXMLOperationBase  {

    static ILogger logger = ILoggerFactory.getILogger(AnalyzeFormItemGotoType_TP.class);

    public void execute(VXMLExecutionContext context) throws InterruptedException {
        Value v = context.getValueStack().pop();
        String formItemName = (String) v.accept(context, Visitors.getAsStringVisitor());
        Product executingForm = context.getExecutingForm();
        if(executingForm == null || (! (executingForm instanceof FormPredicate))){
            badfetch(context, formItemName);
            return;
        }
        FormPredicate form = (FormPredicate) executingForm;

       // Product unwindPoint = form.getFormItemProduct();
        Product unwindPoint = form;
        FIAState state = context.getFIAState();
        state.setNextItem(formItemName);

        // Jus to check if the nexitem exists
        if (!state.hasItem(formItemName)) {
            badfetch(context, formItemName);
            return;
        }

        context.getValueStack().pushScriptValue(unwindPoint);
        context.getValueStack().pushMark();
    }

    public String arguments() {
        return "";
    }

    private void badfetch(ExecutionContext context,
                          String formItemName) {
        String msg = "Goto from " + context.getExecutingModule().getDocumentURI()
                     + " to " + formItemName + " is invalid";
        //TODO: make sure a DebugInfo is supplied.
        if (logger.isDebugEnabled()) logger.debug(msg);
        context.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH,
                msg, null);
    }
}
