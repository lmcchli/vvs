/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Disconnecter;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.compiler.Constants;

/**
 * Reject an inbound call on the specified {@link Connection}.
 * <p>
 * <b>Expected stack:</b>
 * <ol>
 * <li>
 *  <em>String</em>, Connection id of the inbound connection to accept
 * </li>
 * <li>
 *  <em>String</em>, Reason for the rejection
 * </li>
 * <li>
 *  <em>String</em>, Hints for the rejection
 * </li>
 * </ol>
 *
 * @author Mikael Andersson
 */
public class ConnectionReject_T3 extends ConnectionCallBase {

    private String messageForFiredEvent = "Expected event for reject did not arrive in time";
    private String[] eventNames = {
            Constants.Event.CONNECTION_DISCONNECTED,
            Constants.Event.CONNECTION_DISCONNECT_HANGUP,
            Constants.Event.ERROR_CONNECTION,
            Constants.Event.CONNECTION_FAILED,
            Constants.Event.ERROR_NOTALLOWED};

    public void callOn(CCXMLExecutionContext ex, ValueStack stack, Connection conn) {
        String reason = stack.popAsString(ex);
        String hints = stack.popAsString(ex);
        
        String rejectEventType = null;
        if(hints != null && hints.length() > 0){
            String hintsRejectEventType = hints + "." + Constants.CCXML.REJECT_EVENT_TYPE;
            Scope currentScope = ex.getCurrentScope();
            Object hintsValue = currentScope.evaluate(hintsRejectEventType);
            // If we fail to evaluate, "rejecteventtype" was not defined by the application, this is ok
            if(! currentScope.lastEvaluationFailed() && hintsValue != currentScope.getUndefined()){
                if(hintsValue != null){
                	rejectEventType = hintsValue.toString();
                }
            }
        }
        
        ex.waitForEvent(Constants.Event.ERROR_CONNECTION, messageForFiredEvent, conn.getCallManagerWaitTimeout(), new Disconnecter(conn), conn, eventNames);
        conn.reject(rejectEventType, reason);
    }
}
