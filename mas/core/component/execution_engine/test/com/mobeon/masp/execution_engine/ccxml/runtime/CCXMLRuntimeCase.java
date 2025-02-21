/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.InboundCall;
import com.mobeon.masp.callmanager.RedirectingParty;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.ConnectionImpl;
import com.mobeon.masp.execution_engine.ccxml.EventSourceManager;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.dummies.*;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.RuntimeCase;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.operateandmaintainmanager.SessionInfo;
import com.mobeon.masp.operateandmaintainmanager.SessionInfoFactory;
import org.jmock.Mock;
import org.jmock.builder.ArgumentsMatchBuilder;
import org.jmock.builder.NameMatchBuilder;
import org.mozilla.javascript.ScriptableObject;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Mikael Andersson
 */
public abstract class CCXMLRuntimeCase extends RuntimeCase implements CCXMLRuntimeData{


    public CCXMLRuntimeCase(String name) {
        super(name);
    }

    protected Call createCall(Class<? extends Call> aClass) {
        Call call = new CallDummy((DefaultExpectTarget) getExpectTarget(aClass));
        getMocks().put(call, getExpectTargetMock(aClass));
        return call;
    }

    public void setUp() throws Exception {
        super.setUp();
        createConnection();
    }

    protected ExecutionContext createExecutionContext() {
        mockExecutionContext = mock(CCXMLExecutionContext.class);

        Mock sessionInfoMock = new Mock(SessionInfo.class);
        sessionInfoMock.stubs().method("setConnetionState");
        sessionInfoMock.stubs().method("setConnetionType");
        sessionInfoMock.stubs().method("setSessionInitiator");
        sessionInfoMock.stubs().method("setService");
        sessionInfoMock.stubs().method("setDirection");
        sessionInfoMock.stubs().method("setANI");
        sessionInfoMock.stubs().method("setDNIS");
        sessionInfoMock.stubs().method("setRDNIS");
        sessionInfoMock.stubs().method("setOutboundActivity");
        sessionInfoMock.stubs().method("setInboundActivity");

        Mock sessionFactoryMock = new Mock(SessionInfoFactory.class);
        sessionFactoryMock.stubs().method("getSessionInstance").will(returnValue(sessionInfoMock.proxy()));
        sessionFactoryMock.stubs().method("returnSessionInstance");

        Mock sessionMock = new Mock(ISession.class);
        sessionMock.stubs().method("getUnprefixedId").will(returnValue("123"));
        sessionMock.stubs().method("getIdentity").will(returnValue(new IdGeneratorImpl.IdImpl<ISession>(IdGeneratorImpl.SESSION_GENERATOR, 123)));
        sessionMock.stubs().method("getData").with(eq(ISession.SERVICE_NAME)).will(returnValue("default"));
        sessionMock.stubs().method("getData").with(eq(ISession.SESSION_INITIATOR)).will(returnValue("d123"));
        sessionMock.stubs().method("registerSessionInLogger");

        Module module = null;  //TODO may use mock instead
        try {
            module = new Module(new URI("file//:a.ccxml"));
        } catch (URISyntaxException e) {
            assertTrue(false);
        }

        mockExecutionContext.stubs().method("getSessionInfoFactory").will(returnValue(sessionFactoryMock.proxy()));
        mockExecutionContext.stubs().method("getSession").will(returnValue(sessionMock.proxy()));
        mockExecutionContext.stubs().method("getConfigurationManager").will(returnValue(configurationManager));
        mockExecutionContext.stubs().method("waitForEvent");
        mockExecutionContext.stubs().method("getExecutingModule").will(returnValue(module));
        mockExecutionContext.stubs().method("isInterrupted").will(returnValue(false));

        mockExecutionContext.stubs().method("getEventHub").will(returnValue(mockEventHub.proxy()));
        mockExecutionContext.stubs().method("getEventSourceManager").will(returnValue(mockEventSourceManager.proxy()));
        mockExecutionContext.stubs().method("getValueStack").will(returnValue(mockValueStack.proxy()));
        mockExecutionContext.stubs().method("getContextId").will(returnValue("number_22"));
        mockExecutionContext.stubs().method("getContextType").will(returnValue("CCXMLContext"));
        mockExecutionContext.stubs().method("getSessionId").will(returnValue("session_5353563"));


        // TODO returning implementation class here is not the best, but atm EventStream
        // is not an interface and making an interface was not th easiest...
        mockExecutionContext.stubs().method("getEventStream").will(returnValue(new EventStream()));


        return (ExecutionContext) mockExecutionContext.proxy();
    }

    protected CCXMLEvent createEvent(ExecutionContext executionContext) {
        Connection connection = new ConnectionImpl(executionContext);
        CCXMLEvent event = new CCXMLEvent(null);
        event.defineProperty(Constants.Prefix.CONNECTION, connection, ScriptableObject.READONLY);
        return event;
    }



    protected void receive_ExecutionContext_fetchDialogEvent(NameMatchBuilder self, DialogStartEvent event, String dialogId) {
        self.method("fetchDialogEvent").with(eq(dialogId)).will(returnValue(event));
    }

    protected void expect_ExecutionContext_fetchDialogEvent(DialogStartEvent event, String dialogId) {
        receive_ExecutionContext_fetchDialogEvent(mockExecutionContext.expects(once()), event, dialogId);
    }

    protected void receive_ExecutionContext_bindStateTo(NameMatchBuilder self, String varname) {
        self.method("bindStateTo").with(eq(varname));
    }

    protected void expect_ExecutionContext_bindStateTo(String varname) {
        receive_ExecutionContext_bindStateTo(mockExecutionContext.expects(once()), varname);
    }

    protected void receive_ExecutionContext_getConnectionManager(NameMatchBuilder self, EventSourceManager manager) {
        self.method("getConnectionManager").withNoArguments().will(returnValue(manager));
    }

    protected void expect_ExecutionContext_getConnectionManager(EventSourceManager manager) {
        receive_ExecutionContext_getConnectionManager(mockExecutionContext.expects(once()), manager);
    }

    protected void receive_ExecutionContext_getSessionId(NameMatchBuilder self, String sessionId) {
        self.method("getSessionId").withNoArguments().will(returnValue(sessionId));
    }

    protected void expect_ExecutionContext_getSessionId(String sessionId) {
        receive_ExecutionContext_getSessionId(mockExecutionContext.expects(once()), sessionId);
    }

    protected void receive_ExecutionContext_getContextType(NameMatchBuilder self, String contextType) {
        self.method("getContextType").withNoArguments().will(returnValue(contextType));
    }

    protected void expect_ExecutionContext_getContextType(String contextType) {
        receive_ExecutionContext_getContextType(mockExecutionContext.expects(once()), contextType);
    }

    protected void receive_ExecutionContext_getContextId(NameMatchBuilder self, String contextId) {
        self.method("getContextId").withNoArguments().will(returnValue(contextId));
    }

    protected void expect_ExecutionContext_getContextId(String contextId) {
        receive_ExecutionContext_getContextId(mockExecutionContext.expects(once()), contextId);
    }

    protected void expect_Call_accept(Object expectedInstance) throws Exception {
        Mock mock = getExpectTargetMock(InboundCall.class);
        ArgumentsMatchBuilder argumentsMatchBuilder = findMockFor(expectedInstance,InboundCall.class).expects(once()).method("Call_accept");
        argumentsMatchBuilder.with(
                isSameObject(expectedInstance, mock));
    }

    protected void expect_Call_proxy(Object expectedInstance) throws Exception {
        Mock mock = getExpectTargetMock(InboundCall.class);
        ArgumentsMatchBuilder argumentsMatchBuilder = findMockFor(expectedInstance,InboundCall.class).expects(once()).method("Call_proxy");
        argumentsMatchBuilder.with(
                isSameObject(expectedInstance, mock));
    }

    protected void expect_Call_disconnect(Object expectedInstance) throws Exception {
        Mock mock = getExpectTargetMock(InboundCall.class);
        ArgumentsMatchBuilder argumentsMatchBuilder = findMockFor(expectedInstance,InboundCall.class).expects(once()).method("Call_disconnect");
        argumentsMatchBuilder.with(
                isSameObject(expectedInstance, mock));
    }

    protected void expect_Call_getRedirectingParty(RedirectingParty r, Object expectedInstance) {
        findMockFor(expectedInstance, Call.class).expects(once()).method("getRedirectingParty").
                withNoArguments().will(returnValue(r));
    }

    protected void expect_Call_getCallingParty(CallingParty c, Object expectedInstance) {
        findMockFor(expectedInstance, Call.class).expects(once()).method("getCallingParty").
                withNoArguments().will(returnValue(c));
    }

    protected void expect_Connection_accept() {
        getExpectTargetMock(Connection.class).expects(once()).method("Connection_accept").withNoArguments();

    }

    protected void receive_ConnectionManager_findConnection(NameMatchBuilder self, String id, Connection conn) {
        self.method("findConnection").with(eq(id)).will(returnValue(conn));
    }

    protected void expect_ConnectionManager_findConnection(String id, Connection conn) {
        receive_ConnectionManager_findConnection(mockEventSourceManager.expects(once()), id, conn);
    }

    protected void expect_ConnectionManager_createDialog(Dialog d) {
        mockEventSourceManager.expects(once()).method("createDialog").will(returnValue(d));
    }

    protected void receive_ExecutionContext_getEventVar(NameMatchBuilder self, CCXMLEvent event) {
        self.method("getEventVar").withNoArguments().will(returnValue(event));
    }

    protected void expect_ExecutionContext_getEventVar(CCXMLEvent event) {
        receive_ExecutionContext_getEventVar(mockExecutionContext.expects(once()), event);
    }

    protected void receive_ExecutionContext_setEventVarName(NameMatchBuilder self, String name) {
        self.method("setEventVarName").with(eq(name));
    }

    protected void expect_ExecutionContext_setEventVarName(String name) {
        receive_ExecutionContext_setEventVarName(mockExecutionContext.expects(once()), name);
    }

    protected void receive_ValueStack_peek(NameMatchBuilder self, Value onStack) {
        self.method("peek").withNoArguments().will(returnValue(onStack));
    }

    protected void expect_ValueStack_peek(Value onStack) {
        receive_ValueStack_peek(mockValueStack.expects(once()), onStack);
    }

    public EventSourceManager getConnectionManager() {
        return (EventSourceManager) mockEventSourceManager.proxy();
    }

    public CCXMLExecutionContext getExecutionContext() {
        return (CCXMLExecutionContext)super.getExecutionContext();
    }

}
