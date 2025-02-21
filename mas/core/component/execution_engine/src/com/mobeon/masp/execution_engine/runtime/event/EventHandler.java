package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.masp.execution_engine.compiler.Predicate;


/**
 * @author Mikael Andersson
 */
public class EventHandler {
    private Predicate predicate;

    public EventHandler(Predicate predicate) {
        this.predicate = predicate;
    }

    public Predicate getPredicate() {
        return predicate;

    }

    public String toString() {
        return "EventHandler{" +
                "predicate=" + predicate +
                '}';
    }
}
