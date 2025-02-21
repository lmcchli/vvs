/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging;

/**
 * Abstract class for representing a log context.
 * <p>
 * All logcontext have a name (whose interpretation depends on the subclass),
 * as well as abstract functions for defining the semantics of the particular LogContext subclass.
 * <p>
 * An important method that must be implemented by each subclass is the implies method to
 * compare log context. Basically, "logContext c1 implies logContext c2" means that if
 * one has been notified logContext c1, one has naturally been notified logContext c2.
 * Thus, this is not an equality test, but rather more of a subset test.
 * <p>
 * The implies method is used by the RepetitiveLoggingFilter to determine
 * whether or not a log context in a requested LogJustOnceMessage is implied by another LogJustOnceMessage that is
 * already registred in the current RepetitiveLoggingFilter.
 * <p>
 * LogContext objects are immutable once they have been created.
 * Subclasses should not provide methods that can change the state of a log context once it has been created.
 *
 * @see LogJustOnceMessage
 * @see RepetitiveLoggingFilter
 * @author Håkan Stolt
 */
public abstract class LogContext {

    /**
     * The log context name.
     */
    private final String name;

    /**
     * Constructs with log context name.
     * The name cannot be null or an empty string.
     * @param name log context name.
     */
    protected LogContext(String name) {
        if(name == null || name.length()<=0) throw new IllegalArgumentException("name cannot be null or empty string");
        this.name = name;
    }


    /**
     * Checks if the specified log context is "implied by" this log context.
     * This must be implemented by subclasses of LogContext,
     * as they are the only ones that can impose semantics on a LogContext object.
     * @param logContext specified log context.
     * @return true if this log context implies the specified log context.
     */
    public abstract boolean implies(LogContext logContext);

    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Another object is considered equal to this if it is a derived class
     * of LogContext and has the same name.
     * @param o another object
     * @return true if this log context is equal to the specified object.
     */
    public boolean equals(Object o) {
        if(o.getClass().equals(this.getClass())) {
            LogContext other = (LogContext) o;
            return this.name.equals(other.name);
        } else {
            return false;
        }

    }

    /**
     * Creates a string representation of the log context.
     * @return the log context name.
     */
    public String toString() {
        return name;
    }

    /**
     * Get this log context's name.
     * @return the name.
     */
    public String getName() {
        return name;
    }

}
