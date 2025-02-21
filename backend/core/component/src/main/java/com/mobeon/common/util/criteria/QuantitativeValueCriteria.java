/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.criteria;

/**
 * Base class for value citerias that has a quantitative value.
 *
 * @author qhast
 */
public abstract class QuantitativeValueCriteria<T extends Comparable<T>,V extends CriteriaVisitor>
        extends ValueCriteria<T, V> {

    /**
     * A value comparison. The comparison of a comparison criteria can one of the following:
     * <ul>
     * <li>{@link #EQ}</li>
     * <li>{@link #GE}</li>
     * <li>{@link #GT}</li>
     * <li>{@link #LE}</li>
     * <li>{@link #LT}</li>
     * <li>{@link #NE}</li>
     * </ul>
     */
    public enum Comparison {
        /**
         * Equal.
         */
        EQ,

        /**
         * Greater than or equal.
         */
        GE,

        /**
         * Greater than.
         */
        GT,

        /**
         * Lesser than or equal.
         */
        LE,

        /**
         * Lesser than.
         */
        LT,

        /**
         * Not equal.
         */
        NE
    }


    /**
     * The comparison used when matching the value.
     */
    Comparison comparison;

    /**
     * Create a comparison criteria with value and comparison.
     *
     * @param value
     * @param c     which comparsion that should be applied.
     */
    protected QuantitativeValueCriteria(String name, T value, Comparison c) {
        super(name, value);
        this.comparison = c == null ? Comparison.EQ : c;
    }

    protected QuantitativeValueCriteria(String name, T value) {
        this(name, value, null);
    }

    protected QuantitativeValueCriteria(T value, Comparison c) {
        this(null, value, c);
    }

    protected QuantitativeValueCriteria(T value) {
        this(null, value, null);
    }

    /**
     * Gets the comparison used when matching the value.
     *
     * @return used comparision.
     */
    public Comparison getComparison() {
        return comparison;
    }


    /**
     * Compares this object against the specified object.
     * The result is true if and only if the argument is not null and is a
     * object of that has exactly the same value and comparision.
     *
     * @param obj
     * @return true if obj is equal to this object.
     */
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            QuantitativeValueCriteria other = (QuantitativeValueCriteria) obj;
            return other.comparison.equals(this.comparison);
        }
        return false;
    }


    protected int generateHashCode() {
        return super.generateHashCode() + comparison.toString().hashCode();
    }

    /**
     * Tests if a value matches the criteria.
     *
     * @param value
     * @return true if value matches the criteria.
     */
    public boolean matchValue(T value) {
        switch (comparison) {
            case GE:
                return this.value.compareTo(value) <= 0;
            case GT:
                return this.value.compareTo(value) < 0;
            case LE:
                return this.value.compareTo(value) >= 0;
            case LT:
                return this.value.compareTo(value) > 0;
            case NE:
                return !super.matchValue(value);
            default:
                return super.matchValue(value);
        }
    }

    /**
     * Return a type string that decribes the criteria
     * For example <code>age>=23</code>
     *
     * @return Criteria type and instance
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(20);
        sb.append(getName());
        switch (comparison) {
            case GE:
                sb.append(">=");
                break;
            case GT:
                sb.append(">");
                break;
            case LE:
                sb.append("<=");
                break;
            case LT:
                sb.append("<");
                break;
            case NE:
                sb.append("!=");
                break;
            default:
                sb.append("=");
        }
        sb.append(getValue().toString());
        return sb.toString();
    }


}
