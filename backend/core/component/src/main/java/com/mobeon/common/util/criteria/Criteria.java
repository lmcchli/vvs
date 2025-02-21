/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.criteria;


/**
 * Base implementation for criterias.
 * A criteria can be a composite of other criterias.
 * Each criteria acts like a node in a criteria tree.
 * <pre>
 * criteria A
 *  |
 *  +-- criteria B
 *  |    |
 *  |    +-- criteria C
 *  |
 *  +-- criteria D
 *       |
 *       +-- criteria E
 *       |
 *       +-- criteria F
 * </pre>
 * <p/>
 * When declared, Criteria should be typed with which visitor it accepts.
 * <p/>
 * Cloning. Implentors of subclasses should make sure that a deep copy
 * of the instance is returned when {@link #clone()} is called.
 * The returned object must be of the same class as the declaring class.
 *
 * @author qhast
 */
public abstract class Criteria<V extends CriteriaVisitor> {


    /**
     * Used to save hashCode calculations.
     */
    Integer hashCodeCache;

    /**
     * Reference to the parent node of this criteria.
     * Is null if this is the top node.
     */
    private Object parent;

    /**
     * Get the parent criteria node.
     *
     * @return parent criteria, null if this is top node.
     */
    public Object getParent() {
        return parent;
    }

    /**
     * Sets the parent node for this node.
     * Parent can onbly be set once.
     *
     * @param parent the criteria having this as child.
     * @throws IllegalStateException if parent already is set.
     */
    void setParent(Object parent) {
        if (this.parent == null) {
            this.parent = parent;
        } else {
            throw new IllegalStateException("This criteria already has a parent!");
        }
    }

    /**
     * returns hashCode for this object. (May be cached.)
     */
    public int hashCode() {
        if (hashCodeCache == null) {
            hashCodeCache = generateHashCode();
        }
        return hashCodeCache;
    }

    /**
     * Compute the hashCode for this object.
     */
    protected abstract int generateHashCode();

    /**
     * Accepts a visitor. See {@link CriteriaVisitor} for more information.
     *
     * @param visitor
     * @see CriteriaVisitor
     */
    public abstract void accept(V visitor);


    /**
     * Return a type string that decribes the criteria
     * For example <code>AND(age=23,weight=55)</code>
     *
     * @return Criteria type and instance
     */
    public abstract String toString();


    /**
     * Implentors should make sure that a deep copy of
     * the instance is returned.
     *
     * @return a deep copy of instance
     */
    public abstract Criteria<V> clone();

}

