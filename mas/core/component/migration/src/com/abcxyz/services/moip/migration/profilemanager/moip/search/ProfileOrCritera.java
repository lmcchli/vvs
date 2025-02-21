/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip.search;

import com.mobeon.common.util.criteria.Criteria;
import com.mobeon.common.util.criteria.OrCriteria;

/**
 *
 */
public class ProfileOrCritera extends OrCriteria<ProfileCriteriaVisitor> {

    /**
     * Create an OrCriteria with other criterias.
     *
     * @param criterias the criterias to be disjunctioned.
     * @throws IllegalArgumentException if less than two arguments is passed, or if null is passed.
     */
    public ProfileOrCritera(Criteria<ProfileCriteriaVisitor>... criterias) {
        super(criterias);
    }

    /**
     * Implenmentors should make sure that a deep copy of
     * the instance is returned.
     *
     * @return a deep copy of instance
     */
    public ProfileOrCritera clone() {
        return new ProfileOrCritera(getCriterias());
    }
}

