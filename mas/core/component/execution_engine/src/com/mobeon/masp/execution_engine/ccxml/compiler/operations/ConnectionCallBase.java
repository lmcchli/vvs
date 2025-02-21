/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.Callable;

public abstract class ConnectionCallBase extends CCXMLOperationBase {

    public void execute(CCXMLExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();
        String connectionID = stack.popAsString(ex);
        Connection conn = ex.getEventSourceManager().findConnection(connectionID);
        if (conn != null) {
            callOn(ex, stack, conn);
        } else {
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC, "Could not find connection corresponding to connectionID " + connectionID, DebugInfo.getInstance());
        }
    }

    public abstract void callOn(CCXMLExecutionContext ex, ValueStack stack, Connection conn);

    public String arguments() {
        return "";
    }

}
