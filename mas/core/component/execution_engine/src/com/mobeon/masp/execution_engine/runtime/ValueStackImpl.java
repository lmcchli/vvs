/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.values.*;
import com.mobeon.masp.execution_engine.util.Stack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mikael Andersson
 */
public class ValueStackImpl implements ValueStack {
    final List<Value> valueList = new ArrayList<Value>();
    final Mark mark = new Mark();
    final Stack<Value> stack = new Stack<Value>();

    public Value peek() {
        return stack.peek();
    }

    public Value pop() {
        return stack.pop();
    }

    public List<Value> popToMark() {

        valueList.clear();
        Value v = pop();
        while(v != null && !(v.getKind() == Value.Kind.MARK)){
            valueList.add(v);
            v = pop();
        }
        return valueList;
    }

    public void push(Value entry) {
        if(entry == null){
            throw new NullPointerException("Value may not be null");
        }
        stack.push(entry);
    }

    public void push(String text) {
        push(new TextValue(text));
    }

    public void pushMark() {
        push(mark);
    }

    public void pushScriptValue(Object o) {
        push(new ECMAObjectValue(o));
    }

    public void push(Object o) {
        push(new ObjectValue(o)); 
    }

    public void push(Event e) {
        push(new EventValue(e));
    }

    public void push(String[] text) {
        push(new TextArrayValue(text));
    }

    public void prune(int newSize) {
        stack.prune(newSize);
    }

    public int size() {
        return stack.size();
    }

    public String popAsString(ExecutionContext ex) {
        Value v = pop();
        if(v != null)
            return v.toString(ex);
        else
            return null;
    }

    public List<Value> toList() {
        return stack.toList();
    }
}

