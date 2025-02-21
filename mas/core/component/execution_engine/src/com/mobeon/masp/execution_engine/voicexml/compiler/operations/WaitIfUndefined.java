/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class WaitIfUndefined extends VXMLOperationBase {
    String itemName;
    private static ILogger logger = ILoggerFactory.getILogger(WaitIfUndefined.class);

    public WaitIfUndefined(String name) {
        super();
        itemName = name;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        Scope scope = ex.getCurrentScope();
        ValueStack value_stack = ex.getValueStack();
        if (scope.evaluate(itemName)== scope.getUndefined()) {
            if(logger.isDebugEnabled())logger.debug("Setting engine state to "+ExecutionResult.EVENT_WAIT);
            ex.waitForEvents();
        } else {
            if(logger.isDebugEnabled())logger.debug("Setting engine state to "+ExecutionResult.DEFAULT);            
            ex.setExecutionResult(ExecutionResult.DEFAULT);
        }
        ex.getEventProcessor().setEnabled(true);        
    }

    public String arguments() {
        return itemName;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
