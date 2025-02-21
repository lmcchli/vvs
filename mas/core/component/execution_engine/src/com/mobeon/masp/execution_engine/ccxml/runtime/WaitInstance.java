package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.compiler.products.WaitSetProduct;
import com.mobeon.common.eventnotifier.Event;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jul 5, 2006
 * Time: 10:23:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class WaitInstance {

    protected boolean forAll;
    protected ExecutionContext ec;
    protected WaitSetProduct wsp;
    protected List<Entry> entries;
    protected List<Entry> terminateEntries;
    protected boolean stopWaitingOnMatch;
    public boolean isInjectOnMatch = true;

    public List<Entry> getEntries() {
        return entries;
    }

    public WaitInstance(
            boolean forAll, List<Entry> entries, List<Entry> terminateEntries, ExecutionContext ec, WaitSetProduct e, boolean stopWaitingOnMatch, boolean isInjectOnMatch) {
        this.forAll = forAll;
        this.ec = ec;
        this.wsp = e;
        this.entries = new ArrayList<Entry>(entries.size());
        this.terminateEntries= new ArrayList<Entry>(entries.size());
        this.stopWaitingOnMatch = stopWaitingOnMatch;
        this.isInjectOnMatch = isInjectOnMatch;

        for (Entry entry : entries) {
            this.entries.add(entry.clone());
        }
        for (Entry entry : terminateEntries) {
            this.terminateEntries.add(entry.clone());
        }
    }

    public boolean shouldTerminate(Event e) {
        for (Entry entry : terminateEntries) {
            if (entry.getRule().isValid(e)) {
                return true;
            }
        }
        return false;
    }

    public boolean tryMatch(Event e) {
        boolean allMatch = true;
        for (Entry entry : entries) {
            if (entry.getRule().isValid(e)) {
                entry.decrementCount();
                if(! forAll){
                    // we are happy with one match
                    allMatch = true;
                    break;
                }
            }
            if (entry.getCount() > 0) allMatch = false;
            if (allMatch && !forAll) break;
        }
        // Now reset all counts and return.
        resetCounts();
        return allMatch;

    }

    private void resetCounts() {
        for (Entry entry : entries) {
            entry.resetCount();
        }
    }

    public ExecutionContext getExecutionContext() {
        return ec;
    }

    public String toString(){
        String entriesStr = "";
        for (Entry entry : entries) {
            entriesStr += entry;
        }
        return forAll+":"+entriesStr+":"+terminateEntries+":"+stopWaitingOnMatch;
    }

    public WaitSetProduct getWaitSetProduct() {
        return wsp;
    }

    public boolean isStopWaitingOnMatch() {
        return stopWaitingOnMatch;
    }
}
