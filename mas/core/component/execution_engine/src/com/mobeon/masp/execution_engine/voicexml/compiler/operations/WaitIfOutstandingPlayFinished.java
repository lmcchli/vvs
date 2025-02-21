package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Aug 22, 2006
 * Time: 10:35:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class WaitIfOutstandingPlayFinished extends VXMLOperationBase {

    static ILogger logger = ILoggerFactory.getILogger(WaitIfOutstandingPlayFinished.class);
    
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if(! ex.getCall().hasReceivedPlayEvent()){
            if(logger.isDebugEnabled())logger.debug("We have not received play event yet, wait for it");
            ex.waitForEvents();
        } else {
            if(logger.isDebugEnabled())logger.debug("We have received play event, continue execution");
        }
    }

    public String arguments() {
        return "";
    }
}
