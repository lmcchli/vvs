/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.mock;

import org.jmock.core.Constraint;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Constraint that checks for a specific LogLevel. This also exists in trafficeventsender.radius.LocalHostTest.
 * Todo: Move this to an external test util package
 */
public class LogLevelConstraint implements Constraint {
    Level level;

    public LogLevelConstraint(Level level) {
        this.level = level;
    }

    public boolean eval(Object o) {
        return o instanceof LoggingEvent && ((LoggingEvent)o).getLevel().equals(level);
    }

    public StringBuffer describeTo(StringBuffer buffer) {
        return buffer.append("a ").append(level.toString()).append(" log");
    }
}
