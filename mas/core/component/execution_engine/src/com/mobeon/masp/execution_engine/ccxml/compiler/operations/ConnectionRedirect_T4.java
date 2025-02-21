package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;

import com.mobeon.masp.callmanager.InboundCall.RedirectStatusCode;
import com.mobeon.masp.callmanager.RedirectDestination;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Disconnecter;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;

/**
 * Executes the <redirect connectionid="evt.connectionid" dest="MoipSystem@localhost" reason="" hints=""/>
 * @author lmcraby
 * @version MIO 2.0.1
 */
public class ConnectionRedirect_T4 extends ConnectionCallBase {
    
    private String messageForFiredEvent = "Expected event for redirect did not arrive in time";
    private String[] eventNames = {Constants.Event.CONNECTION_REDIRECTED,
            Constants.Event.CONNECTION_DISCONNECTED,
            Constants.Event.CONNECTION_DISCONNECT_HANGUP,
            Constants.Event.ERROR_CONNECTION,
            Constants.Event.CONNECTION_FAILED,
            Constants.Event.ERROR_NOTALLOWED};

    @Override
    public void callOn(CCXMLExecutionContext ex, ValueStack stack, Connection conn) {
        String sDestination = stack.popAsString(ex);
        String reason = stack.popAsString(ex);
        String hints = stack.popAsString(ex);
        
      
        RedirectStatusCode redirectCode = RedirectStatusCode._302_MOVED_TEMPORARILY;
        RedirectDestination destination =null;
        try {
            destination = RedirectDestination.parseRedirectDestination(sDestination);
            //Obtain the SIP redirect code from reason
           try {
               redirectCode = RedirectStatusCode.valueOf(reason);
               //Parse the hints but ignore content for now
               if(hints != null && hints.length() > 0){
                   
               }
               
               //Wait for event connection.redirected        
               ex.waitForEvent(Constants.Event.ERROR_CONNECTION, messageForFiredEvent, conn.getCallManagerWaitTimeout(), new Disconnecter(conn), conn, eventNames);
               conn.redirect(destination,redirectCode);
               
           } catch (Exception e ){
               handleInvalidValue(ex, Constants.CCXML.REASON, reason, " must be one of " +  Arrays.asList(RedirectStatusCode.values()) .toString());
               reason = null;            
            }
        } catch (URISyntaxException e) {
            handleInvalidValue(ex,Constants.CCXML.DESTINATION ,sDestination, e.getMessage());
        }
    }

    private void handleInvalidValue(CCXMLExecutionContext ex, String field , String specifiedValue, String message) {
        
        String msg = field + " has invalid value ( " + specifiedValue + " ) : " + message;        
        ex.getEventHub().fireContextEvent( Constants.Event.ERROR_SEMANTIC, msg , DebugInfo.getInstance());
    }

}
