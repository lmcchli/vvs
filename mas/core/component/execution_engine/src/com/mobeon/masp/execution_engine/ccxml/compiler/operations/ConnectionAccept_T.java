/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Disconnecter;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.compiler.Constants;

/**
 * Accept a inbound call on the specified {@link Connection}.
 * <p>
 * <b>Expected stack:</b>
 * <ol>
 * <li>
 *  <em>String</em>, Connection id of the inbound connection to accept
 * </li
 * </ol>
 *
 * @author Mikael Andersson
 */
public class ConnectionAccept_T extends ConnectionCallBase {

    private String messageForFiredEvent = "Expected event for accept did not arrive in time";
    private String[] eventNames = {Constants.Event.CONNECTION_CONNECTED,
            Constants.Event.CONNECTION_DISCONNECTED,
            Constants.Event.CONNECTION_DISCONNECT_HANGUP,
            Constants.Event.ERROR_CONNECTION,
            Constants.Event.CONNECTION_FAILED,
            Constants.Event.ERROR_NOTALLOWED};
    private String errorConnection = Constants.Event.ERROR_CONNECTION;

    public ConnectionAccept_T(){
    }
    public void callOn(CCXMLExecutionContext ex, ValueStack stack, Connection conn) {
        // Register the events we expect. If we do not get them we will generate error.connection
        ex.waitForEvent(errorConnection, messageForFiredEvent, conn.getCallManagerWaitTimeout(), new Disconnecter(conn), conn, eventNames);
        conn.accept();
    }
}
