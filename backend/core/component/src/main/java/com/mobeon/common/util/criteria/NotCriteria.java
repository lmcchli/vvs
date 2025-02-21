/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.criteria;

/**
 * Represents a criteria of logical negation of a criteria.
 *
 * @author qhast
 * @see OrCriteria
 * @see AndCriteria
 */
public abstract class NotCriteria<V extends CriteriaVisitor> extends Criteria<V> {

    /**
     * Negated criteria.
     */
    private Criteria<V> criteria;

    /**
     * Create a NotCriteria with other criteria.
     *
     * @param criteria the criteria to be negated.
     * @throws IllegalArgumentException if null is passed.
     */
    public NotCriteria(Criteria<V> criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("You must provide a non-null criteria to the NOT criteria!");
        }
        if (criteria.getParent() == null) {
            this.criteria = criteria;
        } else {
            this.criteria = criteria.clone(); //If criteria already has parent make a clone.
        }
        this.criteria.setParent(this);
    }

    /**
     * Gets the negated criteria.
     *
     * @return negated criteria.
     */
    public Criteria<V> getCriteria() {
        return criteria;
    }

    /**
     * Compares this object against the specified object.
     * The result is true if and only if the argument is not null and is a
     * NotCriteria object that has exactly the same negated criteria.
     *
     * @param obj
     * @return true if obj is equal to this object.
     */
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(this.getClass())) {
            return ((NotCriteria) obj).getCriteria().equals(this.criteria);
        }
        return false;
    }

    /**
     * Accepts a visitor. See {@link CriteriaVisitor} for more information.
     *
     * @param visitor
     * @see CriteriaVisitor
     */
    public final void accept(V visitor) {
        criteria.accept(visitor);
        visitor.visitNotCriteria(this);
    }

    protected int generateHashCode() {
        return criteria.hashCode() + 1;
    }

    /**
     * Return a type string that decribes the criteria
     * For example <code>NOT(age=23,weight=55)</code>
     *
     * @return Criteria type and instance
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(20);
        sb.append("NOT(");
        sb.append(criteria.toString());
        sb.append(")");
        return sb.toString();
    }

}
