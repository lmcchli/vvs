/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Disconnecter;
import com.mobeon.masp.execution_engine.compiler.Constants;

public class Disconnect_T extends CCXMLOperationBase {

    private String messageForFiredEvent = "Expected event for disconnect did not arrive in time";
    private String[] eventNames = {
            Constants.Event.CONNECTION_DISCONNECTED,
            Constants.Event.CONNECTION_DISCONNECT_HANGUP,
            Constants.Event.ERROR_CONNECTION,
            Constants.Event.CONNECTION_FAILED,
            Constants.Event.ERROR_NOTALLOWED};

    public void execute(CCXMLExecutionContext ex) throws InterruptedException {
        String connectionID = ex.getValueStack().pop().toString(ex);
        Connection conn = ex.getEventSourceManager().findConnection(connectionID);
        if (conn != null) {
            ex.waitForEvent(Constants.Event.ERROR_CONNECTION, messageForFiredEvent, conn.getCallManagerWaitTimeout(), new Disconnecter(conn), conn, eventNames);
            conn.disconnect();
        }
    }

    public String arguments() {
        return "";
    }
}
