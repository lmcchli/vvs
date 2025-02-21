/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.search;

import com.mobeon.masp.util.criteria.OrCriteria;
import com.mobeon.masp.util.criteria.Criteria;

/**
 * Created by IntelliJ IDEA.
 * User: QHAST
 * Date: 2005-nov-15
 * Time: 14:27:07
 * To change this template use File | Settings | File Templates.
 */
public class ProfileOrCritera extends OrCriteria<ProfileCriteriaVisitor>  {

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
     * Implentors should make sure that a deep copy of
     * the instance is returned.
     *
     * @return a deep copy of instance
     */
    public ProfileOrCritera clone() {
        return new ProfileOrCritera(getCriterias());
    }
}

