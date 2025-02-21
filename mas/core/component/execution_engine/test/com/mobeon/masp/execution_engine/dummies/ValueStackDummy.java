package com.mobeon.masp.execution_engine.dummies;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.mock.MockAction;
import static com.mobeon.masp.execution_engine.mock.MockAction.Action.DELEGATE;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.ValueStack;

import java.util.List;

/**
 * @author Mikael Andersson
 */
public class ValueStackDummy implements ValueStack {

    private DefaultExpectTarget expectTarget;
    private RuntimeData data;

    public ValueStackDummy(DefaultExpectTarget expectTarget,RuntimeData data) {
        this.expectTarget = expectTarget;
        this.data = data;
    }

    public List<Value> popToMark() {
        return null;
    }

    public void push(String text) {
    }

    public void pushMark() {
    }

    public void push(Event e) {
    }

    public void pushScriptValue(Object o) {
    }

    public void push(String[] text) {
    }

    public int size() {
        return 0;
    }

    public void push(Object o) {
    }

    public void push(Value entry) {
    }

    public Value pop() {
        return null;
    }

    public Value peek() {
        return null;
    }

    public void prune(int newSize) {
    }

    // @MockAction(DELEGATE)
    public String popAsString(ExecutionContext ex) {
        return expectTarget.ValueStack_popAsString(ex);
    }

    public List<Value> toList() {
        return null;
    }
}

