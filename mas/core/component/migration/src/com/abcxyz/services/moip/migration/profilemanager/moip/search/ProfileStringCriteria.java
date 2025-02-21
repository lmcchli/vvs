/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip.search;

import com.mobeon.common.util.criteria.ValueCriteria;

/**
 * Criteria for a profile attribute to a string attribute value
 *
 * @author mande
 * @version 1.0
 * @since <pre>11/17/2005</pre>
 */
public class ProfileStringCriteria extends ValueCriteria<String, ProfileCriteriaVisitor> {

    /**
     * Contruct a criteria with matched name and value.
     * If name is null criteria name will be set to class name.
     *
     * @param name
     * @param value
     */
    public ProfileStringCriteria(String name, String value) {
        super(name, value);
    }

    /**
     * Accepts a visitor. See {@link com.mobeon.common.util.criteria.CriteriaVisitor} for more information.
     *
     * @param visitor
     * @see com.mobeon.common.util.criteria.CriteriaVisitor
     */
    public void accept(ProfileCriteriaVisitor visitor) {
        visitor.visitProfileStringCriteria(this);
    }

    /**
     * Implentors should make sure that a deep copy of
     * the instance is returned.
     *
     * @return a deep copy of instance
     */
    public ProfileStringCriteria clone() {
        return new ProfileStringCriteria(getName(), getValue());
    }
}
