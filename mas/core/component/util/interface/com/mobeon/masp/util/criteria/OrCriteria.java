/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.criteria;

import java.util.Arrays;


/**
 * Represents a criteria of logical disjunction between of a collection of criterias.
 *
 * @author qhast
 * @see AndCriteria
 * @see NotCriteria
 */
public abstract class OrCriteria<V extends CriteriaVisitor> extends Criteria<V> {

    /**
     * Disjunctioned criterias.
     */
    private Criteria<V>[] criterias;

    /**
     * Create an AndCriteria with other criterias.
     * 
     * @param criterias the criterias to be disjunctioned.
     * @throws IllegalArgumentException if less than two arguments is passed, or if null is passed.
     */
    public OrCriteria(Criteria<V>... criterias) {
        if(criterias.length < 2) {
            throw new IllegalArgumentException("You must provide at least two criterias to the OR criteria!");
        }
        this.criterias = criterias;
        Criteria<V> criteria;
        for (int i=0; i<this.criterias.length; i++) {
            criteria = this.criterias[i];
            if(criteria == null) {
                throw new IllegalArgumentException("You must provide a non-null criterias to the OR criteria!");
            }
            if(criteria.getParent() != null) {
                this.criterias[i] = criteria.clone();
            }
            this.criterias[i].setParent(this);
        }
    }

    /**
     * Gets the disjunctioned criterias.
     *
     * @return disjunctioned criterias.
     */
    public Criteria<V>[] getCriterias() {
        return criterias;
    }

    /**
     * Compares this object against the specified object.
     * The result is true if and only if the argument is not null and is a
     * AndCriteria object that has exactly the same collection of
     * disjunctioned criterias.
     *
     * @param obj
     * @return true if obj is equal to this object.
     */
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(this.getClass())) {
            OrCriteria other = (OrCriteria) obj;
            if (other.criterias.length == this.criterias.length) {
                return Arrays.asList(other.criterias).containsAll(Arrays.asList(this.criterias));
            }
        }
        return false;
    }

     /**
     * Accepts a visitor. See {@link CriteriaVisitor} for more information.
     * @param visitor
     * @see CriteriaVisitor
     */
    public final void accept(V visitor) {
        for (Criteria<V> c : criterias) {
            c.accept(visitor);
        }
        visitor.visitOrCriteria(this);
    }

    protected int generateHashCode() {
        int result = 0;
        for (Criteria c : criterias) {
            result += c.hashCode();
        }
        return result;
    }

    /**
     * Return a type string that decribes the criteria
     * For example <code>OR(age=23,weight=55)</code>
     *
     * @return Criteria type and instance
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(criterias.length*20);
        sb.append("OR(");
        for(int i=0;i<criterias.length; i++) {
            sb.append(criterias[i].toString());
            if(i<criterias.length-1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }


}
