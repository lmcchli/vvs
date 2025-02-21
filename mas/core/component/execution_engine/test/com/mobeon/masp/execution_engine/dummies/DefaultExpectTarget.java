package com.mobeon.masp.execution_engine.dummies;

import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;

/**
 */
public interface DefaultExpectTarget {

    public void Call_accept(Call self);

    public void Call_proxy(Call self);
    
    public void Call_disconnect(Call self);
    
    public void Connection_accept();

    public Scope ExecutionContext_getCurrentScope();

    public String ValueStack_popAsString(ExecutionContext ex);

    public void EventHub_fireContextEvent(String event, DebugInfo debugInfo);
}
