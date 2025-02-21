package com.mobeon.masp.execution_engine.dummies;

import com.mobeon.masp.execution_engine.runtime.Engine;
import com.mobeon.masp.execution_engine.runtime.EventProcessor;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.event.EventHub;
import com.mobeon.masp.execution_engine.runtime.event.HandlerLocator;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistry;

/**
 * @author Mikael Andersson
 */
public interface RuntimeData {
    
    public Scope getCurrentScope();

    public ValueStack getValueStack();

    public EventHub getEventHub();

    public EventProcessor getEventProcessor();

    public HandlerLocator getHandlerLocator();

    public Engine getEngine();

    public ScopeRegistry getScopeRegistry();

    public ExecutionContext getExecutionContext();
}
