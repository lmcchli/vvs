package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;

/**
 * @author Mikael Andersson
 */
public interface EventRule {
    enum Category {TRUE,FALSE,INVALID}

    int BUFFER_CAPACITY = 100;
    Category categoryOf(Event e);
    boolean isValid(Event e);

    String toString();

}
