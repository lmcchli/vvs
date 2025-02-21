package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.InputAggregator;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: May 22, 2006
 * Time: 2:02:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class AssignIfBufferedDTMF extends VXMLOperationBase {

    static ILogger logger = ILoggerFactory.getILogger(AssignIfBufferedDTMF.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if(InputAggregator(ex).hasControlToken()){

            ex.getFIAState().collectDTMFUtterance(ex, false, true, true);
            String utterance = ex.getUtterance() ;

            if (utterance != null && ex.getFIAState().matchedGrammar()){
                Scope scope = ex.getCurrentScope();
                FIAState.NextItem nextItem = ex.getFIAState().getNextItem();
                scope.setValue(nextItem.name, utterance);
                if (logger.isDebugEnabled()) logger.debug("There was buffered DTMF and a match. Setting the value " + utterance + " to " + nextItem.name);
            } else {
                if (logger.isDebugEnabled()) logger.debug("There was buffered DTMF but no match");
            }
        } else {
            if (logger.isDebugEnabled()) logger.debug("No buffered DTMF");
        }
    }

    public String arguments() {
        return "";
    }
}
