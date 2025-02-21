/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.condition;

/**
 * A <code>Condition</code> represents a condition for a specific
 * message (i.e. <code>MessageInstance</code>) to be played.
 * <p/>
 *
 *
 * @author Mats Egland
 */
public class Condition {
    /**
     * The string representation of the condition.
     */
    private String condition;

    /**
     * Creates a <code>Condition</code> with the specified
     * condition as a String.
     *
     * @param cond The string representation of the condition.
     */
    public Condition(String cond) {
        this.condition = cond;
    }

    /**
     * Returns the condition as a String.
     *
     * @return The condition.
     */
    public String getCondition() {
        return condition;
    }
}
