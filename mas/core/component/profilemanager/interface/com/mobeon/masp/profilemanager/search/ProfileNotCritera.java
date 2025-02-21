/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.search;

import com.mobeon.masp.util.criteria.Criteria;
import com.mobeon.masp.util.criteria.NotCriteria;

/**
 * Documentation
 *
 * @author mande
 */
public class ProfileNotCritera extends NotCriteria<ProfileCriteriaVisitor> {
    
    /**
     * Create a NotCriteria with other criteria.
     *
     * @param criteria the criteria to be negated.
     * @throws IllegalArgumentException if null is passed.
     */
    public ProfileNotCritera(Criteria<ProfileCriteriaVisitor> criteria) {
        super(criteria);
    }

    /**
     * Implentors should make sure that a deep copy of
     * the instance is returned.
     *
     * @return a deep copy of instance
     */
    public Criteria<ProfileCriteriaVisitor> clone() {
        return null;
    }
}
