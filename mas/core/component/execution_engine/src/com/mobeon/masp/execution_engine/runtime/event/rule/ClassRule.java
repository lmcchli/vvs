/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ClassRule extends EventRuleBase {
    private final Set<Class> classes = new HashSet<Class>();

    public ClassRule(Class ... targetClasses) {
        this.classes.addAll(Arrays.asList(targetClasses));
    }

    public boolean isValid(Event e) {
        return logIfValid(classes.contains(e.getClass()), e);
    }

    public String toString() {
        return "isClassIn("+classes.toString()+")";
    }
}
