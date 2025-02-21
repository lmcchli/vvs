package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.masp.execution_engine.compiler.Predicate;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeChangedSubscriber;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistry;
import com.mobeon.masp.execution_engine.util.ListUnionIterator;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class HandlerLocator implements ScopeChangedSubscriber {

    private Map<String, HandlerTable> handlerByState = new HashMap<String, HandlerTable>();
    private HandlerTable global = new HandlerTable();
    private Selector factory;
    private int depth = 0;
    private static final ILogger log = ILoggerFactory.getILogger(HandlerLocator.class);

    public HandlerLocator(Selector factory) {
        this.factory = factory;
    }

    public void addEventHandler(Predicate predicate, String event) {
        addEventHandler(predicate, factory.create(event));
    }

    public void addEventHandler(Predicate predicate, String handlerRe, String state) {
        addEventHandler(predicate, factory.create(handlerRe), state);
    }

    public void addEventHandler(Predicate predicate, Selector sel) {
        addEventHandler(predicate, sel, null);
    }

    public void enteredScope(Scope newScope) {
        for (HandlerTable ht : handlerByState.values()) {
            ht.addLevel();
        }
        global.addLevel();
        depth++;
        if (log.isDebugEnabled()) log.debug("Depth is " + depth);
    }

    public void leftScope(Scope oldScope) {
        for (HandlerTable ht : handlerByState.values()) {
            ht.removeLevel();
        }
        global.removeLevel();
        depth--;
        if (log.isDebugEnabled()) log.debug("Depth is " + depth);
    }

    public void addEventHandler(Predicate predicate, Selector sel, String state) {
        if (log.isDebugEnabled()) log.debug("Adding handler at depth " + depth);
        HandlerTable es = getHandlerTable(state);
        EventHandler eh = new EventHandler(predicate);
        if (state != null)
            es.addHandler(sel, eh);
        else
            global.addHandler(sel, eh);
    }


    private EventHandler findCandidate(String event, HandlerTable handlerTable, ExecutionContext ex, EventHandler result) {
        ListUnionIterator<EventHandlerDeclaration> iterator = handlerTable.getIterator();
        while (iterator.hasNext()) {
            EventHandlerDeclaration ps = iterator.next();
            if (ps.getSelector().match(event)) {
                if (ps.getPredicate().eval(ex)) {
                    result = ps.getRef();
                    break;
                }
            }
        }
        return result;
    }

    public EventHandler locateEventHandler(String state, String event, ExecutionContext ex) {
        EventHandler result = null;
        HandlerTable handlerTable = handlerByState.get(state);
        if (handlerTable != null) {
            result = findCandidate(event, handlerTable, ex, result);
        }
        if (result == null) {
            result = findCandidate(event, global, ex, result);
        }
        return result;
    }

    private HandlerTable getHandlerTable(String state) {
        HandlerTable es = handlerByState.get(state);
        if (es == null && state != null) {
            es = new HandlerTable();
            handlerByState.put(state, es);
        }
        return es;
    }

    public Map<String, HandlerTable> getHandlerByState() {
        return handlerByState;
    }

    public HandlerTable getGlobal() {
        return global;
    }

    public void setScopeRegistry(ScopeRegistry scopeRegistry) {
        scopeRegistry.addScopeChangedSubscriber(this);
    }
}


