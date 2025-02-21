package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Disconnecter;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ValueStack;

/**
 * @author Mikael Andersson
 */
public class ConnectionUnjoin_T2 extends CCXMLOperationBase {
    private DebugInfo debugInfo;
    private String messageForFiredEvent = "Expected event for unjoin did not arrive in time";
    private String[] eventNames = {Constants.Event.CONFERENCE_UNJOINED,
            Constants.Event.ERROR_CONFERENCE_UNJOIN,
            Constants.Event.CONNECTION_DISCONNECTED,
            Constants.Event.CONNECTION_DISCONNECT_HANGUP,
            Constants.Event.ERROR_CONNECTION,
            Constants.Event.CONNECTION_FAILED,
            Constants.Event.ERROR_NOTALLOWED};
    private String errorConnection = Constants.Event.ERROR_CONNECTION;


    public ConnectionUnjoin_T2(DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
    }

    public String arguments() {
        return "";
    }

    public void execute(CCXMLExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();
        String id2 = stack.popAsString(ex);
        String id1 = stack.popAsString(ex);

        Connection conn1 = ex.getEventSourceManager().findConnection(id1);
        Connection conn2 = ex.getEventSourceManager().findConnection(id2);


        if (conn1 != null && conn2 != null) {
            superviseEvents(ex, id1, id2);
            if(!conn1.unjoin(conn2)) {
                fail("Unable to unjoin "+id1+" and "+id2+" !",ex);
            }
        } else {
            if (conn1 == null && conn2 == null) {
                fail("Both connection id's " + id1 + " and " + id2 + " are invalid !", ex);
                return;
            }
            if (conn1 == null) {
                fail("Connection id " + id1 + " is invalid !", ex);
            }
            if (conn2 == null) {
                fail("Connection id " + id2 + " is invalid !", ex);
            }
        }
    }

    private void superviseEvents(CCXMLExecutionContext ex, String id1, String id2) {
        Connection c = giveMeAConnection(ex, id1, id2);
        if(c != null){
            ex.waitForEvent(errorConnection, messageForFiredEvent, c.getCallManagerWaitTimeout(), new Disconnecter(c), c, eventNames);
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
        ex.getEventHub().fireContextEvent(Constants.Event.ERROR_CONFERENCE_JOIN, message, debugInfo);
    }
}
