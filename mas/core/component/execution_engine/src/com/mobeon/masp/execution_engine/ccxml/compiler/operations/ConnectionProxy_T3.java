/**
 * COPYRIGHT (c) Abcxyz Canada Inc., 2010.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Abcxyz Canada Inc.  The program(s) may be used and/or copied only with the
 * written permission from Abcxyz Canada Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Disconnecter;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ValueStack;

/**
 * Proxy an inbound call on the specified {@link Connection}.
 * <p>
 * <b>Expected stack:</b>
 * <ol>
 * <li>
 *  <em>String</em>, Connection id of the inbound connection to accept
 * </li>
 * <li>
 *  <em>String</em>, Server to which to proxy
 * </li>
 * <li>
 *  <em>String</em>, Port on which to proxy
 * </li>
 * </ol>
 */
public class ConnectionProxy_T3 extends ConnectionCallBase{

    private String messageForFiredEvent = "Expected event for proxy did not arrive in time";
    private String[] eventNames = {
    		Constants.Event.CONNECTION_PROXIED,
            Constants.Event.CONNECTION_DISCONNECTED,
            Constants.Event.ERROR_CONNECTION,
            Constants.Event.CONNECTION_FAILED,
            Constants.Event.ERROR_NOTALLOWED};
    private String errorConnection = Constants.Event.ERROR_CONNECTION;
    
	@Override
	public void callOn(CCXMLExecutionContext ex, ValueStack stack, Connection conn){
        String proxyServer = stack.popAsString(ex);
        String port = stack.popAsString(ex);
        try{
        	int proxyPort = Integer.parseInt(port);
        	ex.waitForEvent(errorConnection, messageForFiredEvent, conn.getCallManagerWaitTimeout(), 
        			new Disconnecter(conn), conn, eventNames);
        	conn.proxy(proxyServer, proxyPort);
        }
        catch (NumberFormatException e){
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                    Constants.CCXML.PORT + " had invalid value " + port, DebugInfo.getInstance());
        }
        
	}

	@Override
	public String arguments() {
		return "";
	}

}
