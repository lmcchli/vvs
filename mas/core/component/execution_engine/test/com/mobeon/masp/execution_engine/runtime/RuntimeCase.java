/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.execution_engine.Case;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.EventSourceManager;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.ExecutableBase;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.operations.Log;
import com.mobeon.masp.execution_engine.dummies.*;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.mock.DelegatingMock;
import com.mobeon.masp.execution_engine.mock.DelegatingProxy;
import com.mobeon.masp.execution_engine.runtime.event.EventHub;
import com.mobeon.masp.execution_engine.runtime.event.HandlerLocator;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistry;
import com.mobeon.masp.execution_engine.runtime.values.ECMAObjectValue;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.util.test.MASTestSwitches;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.masp.operateandmaintainmanager.SessionInfoFactory;
import com.mobeon.masp.operateandmaintainmanager.SessionInfo;
import junit.framework.Assert;
import org.jmock.Mock;
import org.jmock.builder.NameMatchBuilder;
import org.jmock.core.Constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RuntimeCase extends Case implements RuntimeData {

    protected Mock mockExecutionContext;
    protected Mock mockScopeContext;
    protected Executable op;
    protected ILogger logger;
    protected Mock mockLogger;
    protected Mock mockValueStack;
    protected Mock mockEventHub;
    protected Mock mockEventSourceManager;
    protected Mock mockEventProcessor;
    protected Mock mockMediaObjectFactory;
    protected Mock mockScopeRegistry;
    private Mock callManagerMock;
    protected IConfigurationManager configurationManager;

    public RuntimeCase(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        MASTestSwitches.enableUnitTesting();
        super.setUp();
        setupConfigurationManager();
        createEventProcessor();
        createEventHub();
        createConnectionManager();
        createValueStack();
        createExecutionContext();
        createScope();
        createScopeRegistry();
        createCallManager();
        createScope();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    private String name(Class<?> aClass, String s) {
        return Tools.classToMnemonic(aClass) + "_" + s;
    }


    protected Scope createScope() {
        mockScopeContext = new Mock(Scope.class);
        return (Scope) mockScopeContext.proxy();
    }

    protected Connection createConnection() {
        return new ConnectionDummy((DefaultExpectTarget) getExpectTarget(Connection.class),this);
    }

    protected ExecutionContext createExecutionContext() {
        mockExecutionContext = mock(ExecutionContext.class);
        mockExecutionContext.stubs().method("getEventHub").will(returnValue(mockEventHub.proxy()));
        mockExecutionContext.stubs().method("getValueStack").will(returnValue(mockValueStack.proxy()));
        return (ExecutionContext) mockExecutionContext.proxy();
    }

    protected ValueStack createValueStack() {
        mockValueStack = new Mock(ValueStack.class);
        return (ValueStack) mockValueStack.proxy();
    }

    protected EventHub createEventHub() {
        mockEventHub = mock(EventHub.class);
        return (EventHub) mockEventHub.proxy();
    }

    protected Object getEventHubDelegate() {
       return new EventHubDummy((DefaultExpectTarget) getExpectTarget(EventHub.class), this);
    }

    protected ScopeRegistry createScopeRegistry() {
        mockScopeRegistry = new Mock(ScopeRegistry.class);
        return (ScopeRegistry) mockScopeRegistry.proxy();
    }

    public Mock defaultExpectTarget = new Mock(DefaultExpectTarget.class);
    public Map<Class, Mock> classToTarget = new HashMap<Class, Mock>();
    private Map<Object, Mock> mockMap = new HashMap<Object, Mock>();

    protected Map<Object, Mock> getMocks() {
        return mockMap;
    }

    public Mock getExpectTargetMock(Class aClass) {
        Mock result = classToTarget.get(aClass);
        if (result == null)
            return defaultExpectTarget;
        else
            return result;
    }

    public Object getExpectTarget(Class aClass) {
        Mock result = classToTarget.get(aClass);
        if (result == null)
            return defaultExpectTarget.proxy();
        else
            return result.proxy();
    }


    protected void validateMnemonic(String expectedMnemonic) {
        if (op.toMnemonic() == null)
            Assert.fail("toMnemonic() cannot return null");
        String actualMnemonic = op.toMnemonic();
        if (!expectedMnemonic.equals(actualMnemonic))
            Assert.fail("toMnemonic() return wrong mnemonic, was " + actualMnemonic + " should be " + expectedMnemonic);
        ExecutableBase.StringAccumulator expectedIndent = new ExecutableBase.StringAccumulator();
        Log.indent(expectedIndent, 1);
        expectedIndent.append(expectedMnemonic);
        if (!expectedIndent.toString().equals(op.toMnemonic(1)))
            Assert.fail("toMnemonic(int) doesn't indent correctly");
    }


    protected void setupLogger() {
        mockLogger = mock(ILogger.class);
        logger = (ILogger) mockLogger.proxy();

    }

    protected void expect_ScopeRegistry_createNewScope() {
        NameMatchBuilder self = mockScopeRegistry.expects(once());
        self.method("createNewScope");
    }

    protected void expect_ECMAExecutor_deleteMostRecentScope() {
        NameMatchBuilder self = mockScopeRegistry.expects(once());
        self.method("deleteMostRecentScope");
    }

    protected void expect_ILogger_info(String testString) {
        receive_ILogger_info(mockLogger.expects(once()), testString);

    }

    private void receive_ILogger_info(NameMatchBuilder self, String testString) {
        self.method("info").with(eq(testString));

    }

    protected void setupConfigurationManager() {
        Mock groupMock = new Mock(IGroup.class);
        groupMock.stubs().method("getString").with(eq(RuntimeConstants.CONFIG.GENERATE_OPS)).will(returnValue("false"));
        groupMock.stubs().method("getString").with(eq(RuntimeConstants.CONFIG.ALWAYS_COMPILE)).will(returnValue("false"));
        groupMock.stubs().method("getInteger").with(eq(RuntimeConstants.CONFIG.ACCEPT_TIMEOUT)).will(returnValue(60000));
        groupMock.stubs().method("getInteger").with(eq(RuntimeConstants.CONFIG.CREATECALL_ADDITIONAL_TIMEOUT)).will(returnValue(60000));
        groupMock.stubs().method("getInteger").with(eq(RuntimeConstants.CONFIG.CALL_MANAGER_WAIT_TIME)).will(returnValue(60000));


        Mock configurationMock = new Mock(IConfiguration.class);
        configurationMock.stubs().method("getGroup").will(returnValue(groupMock.proxy()));

        Mock configurationManagerMock = new Mock(IConfigurationManager.class);
        configurationManagerMock.stubs().method("getConfiguration").will(returnValue(configurationMock.proxy()));
        configurationManager = (IConfigurationManager) configurationManagerMock.proxy();
    }

    protected EventSourceManager createConnectionManager() {
        mockEventSourceManager = new Mock(EventSourceManager.class);
        mockEventSourceManager.stubs().method("getInitiatingConnection").will(returnValue(createConnection()));
        return (EventSourceManager) mockEventSourceManager.proxy();
    }

    public class ValueStackExpect {
        private Mock myMock() {
            return findMockFor(null, myClass());
        }

        private Class<ValueStack> myClass() {
            return ValueStack.class;
        }

        public void popAsString(ExecutionContext ec, String onStack) {
            myMock().expects(once()).method(name(myClass(), "popAsString")).with(isSameObject(ec, myMock())).will(returnValue(onStack));
        }
    }

    public class EventHubExpect {
        private Mock myMock() {
            return findMockFor(null, myClass());
        }

        public void fireContextEvent(String message, DebugInfo debugInfo) {
            myMock().expects(once()).method(name(myClass(), "fireContextEvent")).with(eq(message), isA(DebugInfo.class));
        }

        private Class<EventHub> myClass() {
            return EventHub.class;
        }
    }


    public final EventHubExpect EventHub = new EventHubExpect();
    public final ValueStackExpect ValueStack = new ValueStackExpect();


    protected void expect_ValueStack_push(Object value) {
        receive_ValueStack_push(mockValueStack.expects(once()), value);
    }

    protected void expect_ValueStack_push_ECMAValue() {
        // We expect "push" to be called with an ECMAObjectValue, that's the "isA"
        // call below.
        mockValueStack.expects(once()).method("push").with(isA(ECMAObjectValue.class));
    }

    protected void receive_ValueStack_push(NameMatchBuilder self, Object value) {
        self.method("push").with(stringEq(value));
    }


    protected void receive_ValueStack_pop(NameMatchBuilder self, Value onStack) {
        self.method("pop").withNoArguments().will(returnValue(onStack));
    }


    protected void expect_ValueStack_pop(Value onStack) {
        receive_ValueStack_pop(mockValueStack.expects(once()), onStack);
    }

    protected void expect_ValueStack_popAsString(String onStack) {
        receive_ValueStack_popAsString(mockValueStack.expects(once()), onStack);
    }

    protected void receive_ValueStack_popAsString(NameMatchBuilder self, String onStack) {
        self.method("popAsString").withAnyArguments().will(returnValue(onStack));
    }

    protected void receive_ValueStack_popToMark(NameMatchBuilder self, List<Value> onStack) {
        self.method("popToMark").withNoArguments().will(returnValue(onStack));
    }

    protected void expect_ValueStack_popToMark(List<Value> onStack) {
        receive_ValueStack_popToMark(mockValueStack.expects(once()), onStack);
    }


    protected void receive_ScopeContext_evaluate(NameMatchBuilder self, String name, Object value) {
        self.method("evaluate").with(eq(name)).will(returnValue(value));
    }

    protected void receive_ScopeContext_getValue(NameMatchBuilder self, String name, Object value) {
        self.method("getValue").with(eq(name)).will(returnValue(value));
    }

    protected void expect_ScopeContext_getValue(String name, Object value) {
        receive_ScopeContext_getValue(mockScopeContext.expects(once()), name, value);
    }

    protected void receive_ScopeContext_leaveCurrentScope(NameMatchBuilder self) {
        self.method("leaveCurrentScope").withNoArguments();
    }

    protected void expect_ScopeContext_leaveCurrentScope() {
        receive_ScopeContext_leaveCurrentScope(mockScopeContext.expects(once()));
    }

    protected void receive_ScopeContext_isDeclaredInExactlyThisScope(NameMatchBuilder self, String name, boolean result) {
        self.method("isDeclaredInExactlyThisScope").with(eq(name)).will(returnValue(result));
    }

    protected void receive_ScopeContext_isDeclaredInAnyScope(NameMatchBuilder self, String name, boolean result) {
        self.method("isDeclaredInAnyScope").with(eq(name)).will(returnValue(result));
    }

    /**
     * Sets up the following Jmock expectation:
     * <p/>
     * scopeContext.isDeclaredInExactlyThisScope() will be called once.
     *
     * @param name   expected parameter to scopeContext.isDeclaredInExactlyThisScope().
     * @param result return value of scopeContext.isDeclaredInExactlyThisScope().
     */
    protected void expect_ScopeContext_isDeclaredInExactlyThisScope(String name, boolean result) {
        receive_ScopeContext_isDeclaredInExactlyThisScope(mockScopeContext.expects(once()), name, result);
    }

    protected void expect_ScopeContext_lastEvaluationFailed(boolean returnValue) {
        mockScopeContext.expects(once()).method("lastEvaluationFailed").will(returnValue(returnValue));
    }

    protected void expect_ScopeContext_isDeclaredInAnyScope(String name, boolean result) {
        receive_ScopeContext_isDeclaredInAnyScope(mockScopeContext.expects(once()), name, result);
    }

    protected void receive_ScopeContext_setValue(NameMatchBuilder self, String name, Object object) {
        self.method("setValue").with(eq(name), eq(object));
    }

    protected void expect_ScopeContext_setValue(String name, Object object) {
        mockScopeContext.expects(once()).method("setValue").withAnyArguments();
    }

    protected void receive_EventHub_fireEvent(NameMatchBuilder self, Event event) {
        self.method("fireEvent").with(eq(event));
    }

    protected void receive_EventHub_fireEvent(NameMatchBuilder self, String event, DebugInfo info) {
        self.method("fireEvent").with(stringEq(event), stringEq(info));
    }

    protected void receive_EventHub_fireEvent(NameMatchBuilder self, String event) {
        self.method("fireEvent").with(simpleEventEquality(event));
    }

    protected void receive_EventHub_fireContextEvent(NameMatchBuilder self, String event) {
        self.method("fireContextEvent").with(simpleEventEquality(event));
    }

    private Constraint simpleEventEquality(final String eventName) {
        return new Constraint() {

            public StringBuffer describeTo(StringBuffer stringBuffer) {
                stringBuffer.append("SimpleEvent with name ");
                stringBuffer.append(eventName);
                return stringBuffer;
            }

            public boolean eval(Object object) {
                if (object instanceof SimpleEvent) {
                    SimpleEvent event = (SimpleEvent) object;
                    if (event.getEvent().equals(eventName))
                        return true;
                }
                return false;
            }
        };
    }

    protected void expect_EventHub_fireEvent(Event event) {
        receive_EventHub_fireEvent(mockEventHub.expects(once()), event);
    }

    protected void expect_EventHub_fireEvent(String event) {
        receive_EventHub_fireEvent(mockEventHub.expects(once()), event);
    }

    protected void expect_EventHub_fireContextEvent(String event) {
        receive_EventHub_fireContextEvent(mockEventHub.expects(once()), event);
    }

    protected void expect_EventHub_fireContextEvent(String event, String message, DebugInfo debugInfo) {
        mockEventHub.expects(once()).method("fireContextEvent").with(stringEq(event), isA(String.class), isA(DebugInfo.class));
    }

    protected void expect_EventHub_fireEvent(String event, DebugInfo info) {
        receive_EventHub_fireEvent(mockEventHub.expects(once()), event, info);
    }


    protected void receive_ValueStack_pushMark(NameMatchBuilder self) {
        self.method("pushMark").withNoArguments();
    }

    protected void expect_ValueStack_pushMark() {
        receive_ValueStack_pushMark(mockValueStack.expects(once()));
    }

    protected void receive_ScopeContext_hasPrefix(NameMatchBuilder self, String varName, boolean result) {
        self.method("hasPrefix").with(eq(varName)).will(returnValue(result));
    }

    protected void expect_ScopeContext_hasPrefix(String varName, boolean result) {
        receive_ScopeContext_hasPrefix(mockScopeContext.expects(once()), varName, result);
    }

    protected void receive_ScopeContext_getUndefined(NameMatchBuilder self, Object instance) {
        self.method("getUndefined").withNoArguments().will(returnValue(instance));
    }

    protected void expect_ScopeContext_getUndefined(Object instance) {
        receive_ScopeContext_getUndefined(mockScopeContext.expects(once()), instance);
    }


    protected void receive_EventProcessor_setEnabled(NameMatchBuilder self, boolean enabled) {
        self.method("setEnabled").with(eq(enabled));
    }

    protected void expect_EventProcessor_setEnabled(boolean enabled) {
        receive_EventProcessor_setEnabled(mockEventProcessor.expects(once()), enabled);
    }

    protected void expect_EventProcessor_isEnabled(boolean returnValue) {
        mockEventProcessor.expects(once()).method("isEnabled").will(returnValue(returnValue));
    }

    private EventProcessor createEventProcessor() {
        mockEventProcessor = mock(EventProcessor.class);
        return (EventProcessor) mockEventProcessor.proxy();
    }

    protected void receive_ExecutionContext_executeAtomic(NameMatchBuilder self, List<Executable> ops) {
        self.method("executeAtomic").with(eq(ops));
    }

    protected void expect_ExecutionContext_executeAtomic(List<Executable> ops) {
        receive_ExecutionContext_executeAtomic(mockExecutionContext.expects(once()), ops);
    }

    protected void receive_ExecutionContext_call(NameMatchBuilder self, List<Executable> ops, Product product) {
        self.method("call").with(eq(ops), eq(product));
    }

    protected void expect_ExecutionContext_call(List<Executable> ops, Product product) {
        receive_ExecutionContext_call(mockExecutionContext.expects(once()), ops, product);
    }


    protected void expect_MediaObjectFactory_create(Object o) {
        mockMediaObjectFactory.expects(once()).method("create").
                withNoArguments().will(returnValue(o));
    }

    protected void expect_ScopeContext_addAliasPrefix(String alias) {
        mockScopeContext.expects(once()).method("addAliasPrefix").with(eq(alias));
    }

    protected void expect_ScopeContext_javaToJS(Object o) {
        mockScopeContext.expects(once()).method("javaToJS").will(returnValue(o));
    }

    protected void expect_ScopeContext_evaluateAndDeclareVariable(String name, String value) {
        mockScopeContext.expects(once()).method("evaluateAndDeclareVariable").with(eq(name), eq(value));
    }

    protected void expect_ScopeContext_evaluate(String ecmaScript, Object ecmaResult) {
        mockScopeContext.expects(once()).method("evaluate").with(eq(ecmaScript)).will(returnValue(ecmaResult));
    }

    protected void expect_ScopeContext_toBoolean(Boolean value) {
        mockScopeContext.expects(once()).method("toBoolean").withAnyArguments().will(returnValue(value));
    }

    public ExecutionContext getExecutionContext() {
        return (ExecutionContext) mockExecutionContext.proxy();
    }

    public Scope getCurrentScope() {
        return (Scope) mockScopeContext.proxy();
    }

    public ValueStack getValueStack() {
        return (ValueStack) mockValueStack.proxy();
    }

    public ScopeRegistry getScopeRegistry() {
        return (ScopeRegistry) mockScopeRegistry.proxy();
    }

    public EventProcessor getEventProcessor() {
        return (EventProcessor) mockEventProcessor.proxy();
    }

    public EventHub getEventHub() {
        return (EventHub) mockEventHub.proxy();
    }

    public HandlerLocator getHandlerLocator() {
        return null;
    }

    public Engine getEngine() {
        return null;
    }

    private CallManager createCallManager() {
        callManagerMock = mock(CallManager.class);
        return (CallManager) callManagerMock.proxy();
    }

    protected CallManager getCallManager() {
        return (CallManager) callManagerMock.proxy();
    }

    protected Mock findMockFor(Object expectedInstance, Class<?> aClass) {
        Mock result = getMocks().get(expectedInstance);
        if (result == null)
            return getExpectTargetMock(aClass);
        else
            return result;
    }

    protected Constraint isSameObject(Object expectedInstance, Mock mock) {
        Constraint eq = eq(expectedInstance);
        if (expectedInstance instanceof DelegatingProxy) {
            eq = ((DelegatingProxy)expectedInstance).isSame();
        }
        return eq;
    }
    /*
    protected void receive_ExecutionContext_getLogger(NameMatchBuilder self, Class logClass) {
        self.method("getLogger").with(eq(logClass)).will(returnValue(logger));
    }

    protected void expect_ExecutionContext_getLogger(Class logClass) {
        receive_ExecutionContext_getLogger(mockExecutionContext.expects(once()), logClass);
    }

    protected void expect_ExecutionContext_getScopeRegistry(ScopeRegistry scopeRegistry) {
        NameMatchBuilder self = mockExecutionContext.expects(once());
        self.method("getScopeRegistry").will(returnValue(scopeRegistry));
    }

    protected void receive_ExecutionContext_getValueStack(NameMatchBuilder self, ValueStack stack) {
        self.method("getValueStack").withNoArguments().will(returnValue(stack));
    }

    protected void expect_ExecutionContext_getValueStack(ValueStack stack) {
        receive_ExecutionContext_getValueStack(mockExecutionContext.expects(once()), stack);
    }


    protected void receive_ExecutionContext_getEventHub(NameMatchBuilder self, EventHub eventHub) {
        self.method("getEventHub").withNoArguments().will(returnValue(eventHub));
    }

    protected void expect_ExecutionContext_getEventHub(EventHub eventHub) {
        receive_ExecutionContext_getEventHub(mockExecutionContext.expects(once()), eventHub);
    }
    protected void receive_ExecutionContext_getEventProcessor(NameMatchBuilder self, EventProcessor processor) {
        self.method("getEventProcessor").withNoArguments().will(returnValue(processor));
    }

    protected void expect_ExecutionContext_getEventProcessor(EventProcessor processor) {
        receive_ExecutionContext_getEventProcessor(mockExecutionContext.expects(once()), processor);
    }
    protected void expect_ExecutionContext_getMediaObjectFactory(IMediaObjectFactory mof) {
        NameMatchBuilder self = mockExecutionContext.expects(once());
        self.method("getMediaObjectFactory").
                withNoArguments().will(returnValue(mof));
    }
    protected void receive_ExecutionContext_getCurrentScope(NameMatchBuilder self, Scope scoping) {
        self.method("ExecutionContext_getCurrentScope").withNoArguments().will(returnValue(scoping));
    }

    protected void expect_ExecutionContext_getCurrentScope(Scope scoping) {
        receive_ExecutionContext_getCurrentScope(findMockFor(null, ExecutionContext.class).expects(once()), scoping);
    }


    */
}
