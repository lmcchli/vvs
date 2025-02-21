package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.masp.execution_engine.compiler.Predicate;

/**
 * User: QMIAN
 * Date: 2006-mar-01
 * Time: 10:41:33
 */
public class EventHandlerDeclaration {
    Selector selector;
    Predicate predicate;
    EventHandler ref;

    public Selector getSelector() {
        return selector;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public EventHandler getRef() {
        return ref;
    }

    public EventHandlerDeclaration(Selector selector, Predicate predicate, EventHandler ref) {
        this.predicate = predicate;
        this.selector = selector;
        this.ref = ref;
    }
}
