/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.runtime.event.EventHandlerDeclaration;

import java.util.ArrayList;
import java.util.List;

public class State implements Cloneable {

    List<EventHandlerDeclaration> activeHandlers;

    public State() {
        
    }
    public State(State state) {
        this.activeHandlers = new ArrayList<EventHandlerDeclaration>(activeHandlers);
    }

    public State clone() {
        return new State(this);
    }
}
