/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.criteria;


/**
 * Base class for citerias matching a value.
 * @author qhast
 */
public abstract class ValueCriteria<T,V extends CriteriaVisitor> extends Criteria<V> {

    /**
     * Matched value.
     */
    T value;

    /**
     * Criteria name.
     */
    private String name;


    /**
     * Contructs with matched value and name.
     * If name is null criteria name will be set to class name.
     * @param name
     * @param value
     */
    protected ValueCriteria(String name, T value) {
        this.value = value;
        this.name = name!=null?name:this.getClass().getName();
    }

    /**
     * Contructs with matched value.
     * Criteria name will be set to class name.
     * @param value
     */
    protected ValueCriteria(T value) {
        this(null, value);
    }

    /**
     * Gets the matched value.
     * @return value.
     */
    public T getValue() {
        return value;
    }

    /**
     * Gets the criteria name.
     * @return critera name
     */
    public String getName() {
        return name;
    }

    /**
     * Compares this object against the specified object.
     * The result is true if and only if the argument is not null and is an
     * object of the same class that has exactly the same property value.
     *
     * @param obj
     * @return true if obj is equal to this object.
     */
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(this.getClass())) {
            ValueCriteria other = (ValueCriteria) obj;
            if(this.name.equals(other.name)) {
                return this.value.equals(other.value);
            }
        }
        return false;
    }

    protected int generateHashCode() {
        return value.hashCode();
    }


    /**
     * Tests if a value matches the criteria.
     * @param value
     * @return true if value matches the criteria.
     */    
    public boolean matchValue(T value) {
        if (this.value == null && value == null) {
            return true;
        } else if (this.value != null && value != null) {
            return this.value.equals(value);
        } else {
            return false;
        }
    }

    /**
     * Return a type string that decribes the criteria
     * For example <code>age=23</code>
     *
     * @return Criteria type and instance
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(20);
        sb.append(name);
        sb.append("=");
        sb.append(value);
        return sb.toString();
    }
}
