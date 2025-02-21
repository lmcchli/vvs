/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

/**
 * Single value data container with visitor pattern for data conversion.
 * Value accepts a {@link ValueVisitor} and the accept method can then
 * return an appropriate converted value.
 * <p/>
 * Used in {@link ExecutionContextBase} and {@link ValueStack}.
 *
 * @author Mikael Andersson
 */
public interface Value {

    enum Kind {
        MARK,
        TEXT,
        TEXT_ARRAY,
        EVENT,
        ECMA,
        OBJECT;
    }

    /**
     * Accepts a visitor and then calls the appropriate appropriate visit method on the visitor.
     *
     * @param ex      Current {@link ExecutionContext}
     * @param visitor The visitor which this method call will visit
     * @return The value the submitted visitors visit... method returns
     */
    Object accept(ExecutionContext ex, ValueVisitor visitor);

    /**
     * Accepts a visitor and then calls the appropriate appropriate visit method on the visitor.
     *
     * @param visitor The visitor which this method call will visit
     * @return The value the submitted visitors visit... method returns
     */
    Object accept(ValueVisitor visitor);

    /**
     * Returns the {@link Kind} of this Value.
     * There may be several classes implementing the same Kind.
     *
     * @return The Kind of this class
     */
    public Value.Kind getKind();

    /**
     * Returns the actual value stored in its native form.
     *
     * @return The stored value
     */
    public Object getValue();


    /**
     * Converts the stored value to a string representation.
     * <p/>
     * <em>Some values can't be converted and vill return <b>null</b>
     * , or the empty string.</em>
     *
     * @param ex Current {@link ExecutionContext}
     * @return The converted value, or <code>null</code>
     */
    public String toString(ExecutionContext ex);

    /**
     * Converts the stored value to a string representation.
     * <p/>
     * <em>Some values can't be converted and vill return <b>null</b>
     * , or the empty string. For such values try calling the variety of this
     * method accepting an {@link ExecutionContext} as paramater </em>
     *
     * @return The converted value, or <code>null</code>
     */
    public String toString();

    Object toECMA(ExecutionContext ex);
    Object toObject(ExecutionContext ex);

}
