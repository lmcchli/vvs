/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.products;

import com.mobeon.masp.execution_engine.WaitSet;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.ccxml.runtime.Entry;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.rule.ClassRule;
import com.mobeon.masp.execution_engine.runtime.event.rule.SimpleEventRule;

import java.util.ArrayList;
import java.util.List;

public class WaitSetProduct extends ProductImpl {
    protected boolean forAll;
    protected boolean stopWaitingOnMatch = true;
    private boolean injectOnMatch = true;

    protected List<Entry> entries = new ArrayList<Entry>(4);
    protected List<Entry> terminateEntriess = new ArrayList<Entry>(4);

    public WaitSetProduct(Product parent, String localName, DebugInfo debugInfo) {
        super(parent, localName, debugInfo);
    }

    public WaitSetProduct(Product parent, DebugInfo debugInfo) {
        super(parent, debugInfo);
    }

    public void addWaitFor(String eventNames) {
        SimpleEventRule nameAnalyzer = new SimpleEventRule(false,eventNames);
        entries.add(new Entry(1,nameAnalyzer));
    }

    public void addWaitFor(boolean prefixMatch, String eventNames) {
        SimpleEventRule nameAnalyzer = new SimpleEventRule(prefixMatch, eventNames);
        entries.add(new Entry(1,nameAnalyzer));
    }

    public void addWaitFor(Class ... eventClasses) {
        ClassRule classAnalyzer = new ClassRule(eventClasses);
    }

    public void waitForAll(boolean forAll) {
        this.forAll = forAll;
    }

    public void stopWaitingOnMatch(boolean stopWaitingOnMatch) {
        this.stopWaitingOnMatch = stopWaitingOnMatch;
    }

    public void injectOnMatch(boolean injectOnMatch) {
        this.injectOnMatch = injectOnMatch;
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        if(startWaiting(ex)){

            //Start waiting
            ex.getWaitSet().waitFor(forAll, entries,terminateEntriess,ex,this, stopWaitingOnMatch, injectOnMatch);
        }
    }

    /**
     * Override by subclasses
     * @param ex
     * @return true if waiting should start
     */
    protected boolean startWaiting(ExecutionContext ex){
        return true;
    }

    /**
     * May be overridden
     * @param ex
     * @param event
     */
    public void realExecute(ExecutionContext ex, Event event) {
        try {
            super.execute(ex);
        } catch (InterruptedException e) {
            ex.wasInterrupted();
        }
    }

    public void addTerminateOn(String eventName) {
        SimpleEventRule nameAnalyzer = new SimpleEventRule(false,eventName);
        terminateEntriess.add(new Entry(1,nameAnalyzer));

    }

    public Class getCanonicalClass() {
        return WaitSetProduct.class;
    }
}
