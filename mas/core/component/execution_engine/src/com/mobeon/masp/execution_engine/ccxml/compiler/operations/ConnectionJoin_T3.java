package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.BridgeParty;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Disconnecter;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ValueStack;

/**
 * @author Mikael Andersson
 */
public class ConnectionJoin_T3 extends CCXMLOperationBase {
    private DebugInfo debugInfo;
    private String messageForFiredEvent = "Expected event for join did not arrive in time";
    private String[] eventNames = {Constants.Event.CONFERENCE_JOINED,
            Constants.Event.ERROR_CONFERENCE_JOIN,
            Constants.Event.CONNECTION_DISCONNECTED,
            Constants.Event.CONNECTION_DISCONNECT_HANGUP,
            Constants.Event.ERROR_CONNECTION,
            Constants.Event.CONNECTION_FAILED,
            Constants.Event.ERROR_NOTALLOWED};

    public ConnectionJoin_T3(DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
    }

    public void execute(CCXMLExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();
        String duplexValue = stack.popAsString(ex);
        String id2 = stack.popAsString(ex);
        String id1 = stack.popAsString(ex);

        // We do not yet know if it is connections or dialogs we are
        // about to join, try both.
        BridgeParty party1 = ex.getEventSourceManager().findConnection(id1);
        BridgeParty party2 = ex.getEventSourceManager().findConnection(id2);

        if(party1 == null){
            party1 = ex.getEventSourceManager().findDialog(id1);
        }
        if(party2 == null){
            party2 = ex.getEventSourceManager().findDialog(id2);
        }

        if(party1 != null && party2 != null) {
            boolean fullDuplex = true;
            if(duplexValue != null) {
                if(duplexValue.equalsIgnoreCase("half")) {
                   fullDuplex = false;
                } else if(duplexValue.equalsIgnoreCase("full")) {
                    fullDuplex = true;
                } else {
                    fail("Invalid value "+duplexValue+" for duplex attribute of <join>", ex);
                }
                superviseEvents(ex, id1, id2);
                if(!party1.join(party2,fullDuplex, false)) {
                    fail("Unable to join "+id2+" and "+id1,ex);
                }
            }

        } else {
            if(party1 == null && party2 == null) {
                fail("Both connection id's "+id1+" and "+id2+" are invalid !",ex);
                return;
            }
            if(party1 == null) {
                fail("Connection id "+id1+" is invalid !",ex);
            }
            if(party2 == null) {
                fail("Connection id "+id2+" is invalid !",ex);
            }
        }
    }

    private void superviseEvents(CCXMLExecutionContext ex, String id1, String id2) {
        Connection c = giveMeAConnection(ex, id1, id2);
        if(c != null){
            ex.waitForEvent(Constants.Event.ERROR_CONNECTION, messageForFiredEvent, c.getCallManagerWaitTimeout(), new Disconnecter(c), c, eventNames);
        }
    }

    private Connection giveMeAConnection(CCXMLExecutionContext ex, String id1, String id2){
        Connection c = ex.getEventSourceManager().findConnection(id1);
        if(c != null){
            return c;
        }
        return ex.getEventSourceManager().findConnection(id2);
    }

    private void fail(String message, CCXMLExecutionContext ex) {
        ex.getEventHub().fireContextEvent(Constants.Event.ERROR_CONFERENCE_JOIN,message,debugInfo);
    }

    public String arguments() {
        return "";
    }
}
