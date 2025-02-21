package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.execution_engine.runtime.event.rule.EventRuleBase;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRule;
import com.mobeon.common.eventnotifier.Event;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jul 5, 2006
 * Time: 10:25:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class Entry {
    private int count;

    /**
     * The initialCount is used to be able to reset count. count can be decremented by when using this class
     */
    private int initialCount;
    private EventRule rule;
    private Event event;

    public Entry(int count, EventRule nameAnalyzer) {
        this.count = count;
        this.initialCount = count;
        this.rule = nameAnalyzer;
    }

    public Entry(int count, EventRule nameAnalyzer, Event event) {
        this(count, nameAnalyzer);
        this.event = event;
    }

    public Entry(Entry entry) {
        this.count = entry.count;
        this.initialCount = entry.initialCount;
        this.rule = entry.rule;
        this.event = entry.event;
    }

    public Entry clone() {
        return new Entry(this);
    }

    public Event getEvent(){
        return event;
    }

    public String toString(){
        return rule+":"+event;
    }

    public void decrementCount(){
        count--;
    }

    public int getCount(){
        return count;
    }

    public void resetCount(){
        count = initialCount;
    }

    public EventRule getRule() {
        return rule;
    }

}
