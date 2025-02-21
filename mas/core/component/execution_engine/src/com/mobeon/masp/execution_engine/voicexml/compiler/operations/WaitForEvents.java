package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Aug 29, 2006
 * Time: 5:55:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class WaitForEvents extends VXMLOperationBase {

    static ILogger logger = ILoggerFactory.getILogger(WaitForEvents.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {

        // Continue execution iff we have received playFinished and are not waiting for more input

        if(! ex.getCall().hasReceivedPlayEvent()){
            if(logger.isDebugEnabled())logger.debug("We have not received play event yet, wait for it");
            ex.waitForEvents();
        } else {
            if(ex.getFIAState().hasReceivedSomeInputAndWantsToMatchMore()){
                if (logger.isDebugEnabled()) logger.debug("Will wait for more input");
                ex.waitForEvents();
            } else {
                if (logger.isDebugEnabled()) logger.debug("Continue executing");
                ex.setExecutionResult(ExecutionResult.DEFAULT);
            }
        }
    }

    public String arguments() {
        return "";
    }
}
