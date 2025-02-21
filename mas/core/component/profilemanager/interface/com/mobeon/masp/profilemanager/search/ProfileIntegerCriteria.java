/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.search;

import com.mobeon.masp.util.criteria.ValueCriteria;

/**
 * Criteria for a profile attribute to a integer attribute value
 *
 * @author mande
 * @since <pre>11/17/2005</pre>
 * @version 1.0
 */
public class ProfileIntegerCriteria extends ValueCriteria<Integer, ProfileCriteriaVisitor> {

    /**
     * Contruct a criteria with matched name and value.
     * If name is null criteria name will be set to class name.
     *
     * @param name
     * @param value
     */
    public ProfileIntegerCriteria(String name, int value) {
        super(name, value);
    }

    /**
     * Accepts a visitor. See {@link com.mobeon.masp.util.criteria.CriteriaVisitor} for more information.
     *
     * @param visitor
     * @see com.mobeon.masp.util.criteria.CriteriaVisitor
     */
    public void accept(ProfileCriteriaVisitor visitor) {
        visitor.visitProfileIntegerCriteria(this);
    }

    /**
     * Implementors should make sure that a deep copy of
     * the instance is returned.
     *
     * @return a deep copy of instance
     */
    public ProfileIntegerCriteria clone() {
        return new ProfileIntegerCriteria(getName(), getValue());
    }
}
