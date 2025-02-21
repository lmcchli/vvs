/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.search;

import com.mobeon.masp.util.criteria.AndCriteria;
import com.mobeon.masp.util.criteria.Criteria;

/**
 * Documentation
 *
 * @author mande
 */
public class ProfileAndCritera extends AndCriteria<ProfileCriteriaVisitor> {

    /**
     * Create an AndCriteria with other criterias.
     *
     * @param criterias the criterias to be conjunctioned.
     * @throws IllegalArgumentException if less than two arguments is passed, or if null is passed.
     */
    public ProfileAndCritera(Criteria<ProfileCriteriaVisitor>... criterias) {
        super(criterias);
    }

    /**
     * Implentors should make sure that a deep copy of
     * the instance is returned.
     *
     * @return a deep copy of instance
     */
    public Criteria<ProfileCriteriaVisitor> clone() {
        return new ProfileAndCritera(getCriterias());
    }
}
