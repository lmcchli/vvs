/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml;

import com.mobeon.masp.callmanager.InboundCall;
import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.execution_engine.EventSourceManagerImpl;
import com.mobeon.masp.execution_engine.SessionFactory;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLRuntimeCase;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

/**
 * @author Mikael Andersson
 */
public class EventSourceManagerTest extends CCXMLRuntimeCase {

    public EventSourceManagerTest(String event) {
        super(event);
        ILoggerFactory.configureAndWatch(EXECUTION_ENGINE_TEST_LOG_XML);
    }

    public static Test suite() {
        return new TestSuite(EventSourceManagerTest.class);
    }


    /**
     * Verifies that a connection will only be created when all
     * necessary data is available, and that it will be created
     * when such data exists.
     *
     * @throws Exception
     */
    public void testCreateConnection() throws Exception {

        EventSourceManagerImpl connectionManager = new EventSourceManagerImpl();
        connectionManager.setCallManager(getCallManager());

        mockExecutionContext.stubs().method("getEventSourceManager").will(returnValue(connectionManager));
        CCXMLExecutionContext executionContext = getExecutionContext();
        connectionManager.setOwner(executionContext);
        Connection connection = connectionManager.createConnection(null, false);

        if (connection != null)
            die("Connection was created even though the call was null, this is an error");

        Mock callMock = new Mock(InboundCall.class);
        callMock.stubs().method("getCallType").will(returnValue(CallProperties.CallType.VOICE));
        callMock.stubs().method("getCallingParty").will(returnValue(null));
        callMock.stubs().method("getCalledParty").will(returnValue(null));
        callMock.stubs().method("getRedirectingParty").will(returnValue(null));

        connection = connectionManager.createConnection((Call)callMock.proxy(), false);
        if (connection == null)
            die("createConnection returned null even though preconditions " +
                    "should be sufficient");
    }

    /**
     * Verifies that we can find a connection we have created by using
     * it's connectionId.
     *
     * @throws Exception
     */
    public void testFindConnection() throws Exception {

        EventSourceManagerImpl connectionManager = new EventSourceManagerImpl();
        connectionManager.setCallManager(getCallManager());
        mockExecutionContext.stubs().method("getEventSourceManager").will(returnValue(connectionManager));
        CCXMLExecutionContext executionContext = getExecutionContext();
        connectionManager.setOwner(executionContext);
        ISession session = new SessionFactory().create();
        connectionManager.setSession(session);

        Connection connection = connectionManager.createConnection(createCall(InboundCall.class), false);

        Connection foundConnection = connectionManager.findConnection(connection.getBridgePartyId());
        if (connection != foundConnection)
            die("Didn't find the same connection as the one we created");
    }

}