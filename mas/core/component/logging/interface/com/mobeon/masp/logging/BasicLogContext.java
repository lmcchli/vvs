/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging;

/**
 * The BasicLogContext class extends the LogContext class,
 * and can be used as the base class for log contexts that want
 * to follow the same naming convention as BasicLogContext.
 * The name for a BasicLogContext is the name of the given log context
 * (for example, "availability","availability.responding", etc).
 * The naming convention follows the hierarchical property naming convention.
 * An asterisk may appear by itself, or if immediately preceded by
 * a "." may appear at the end of the name, to signify a wildcard match.
 * For example, "*" and "java.*" are valid, while "*java", "a*b", and "java*"
 * are not valid.
 * @author Håkan Stolt
 */
public class BasicLogContext extends LogContext {

    public BasicLogContext(String name) {
        super(name);
    }

    /**
     * Checks if the specified log context is "implied by" this log context.
     * More specifically, this method returns true if:
     * <ul>
     * <li>logContext's class is the same as this object's class, and</li>
     * <li>logContext's name equals or (in the case of wildcards) is implied by
     * this object's name. For example, "a.b.*" implies "a.b.c".</li>
     * </ul>
     * @param logContext specified log context.
     * @return true if this log context implies the specified log context.
     */
    public boolean implies(LogContext logContext) {
        if(logContext.getClass().equals(this.getClass())) {
            if(this.getName().equals(logContext.getName())) {
                return true;
            } else if(this.getName().matches("[^*]*[*]")) {
                String beforeWildcard = this.getName().substring(0,this.getName().length()-1);
                return logContext.getName().startsWith(beforeWildcard);
            }
        }
        return false;
    }





}
