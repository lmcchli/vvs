/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.criteria.basic;

import com.mobeon.masp.util.criteria.Criteria;
import com.mobeon.masp.util.criteria.OrCriteria;

/**
 * @author qhast
 */
public class BasicOrCriteria extends OrCriteria<BasicCriteriaVisitor> {

    public BasicOrCriteria(Criteria<BasicCriteriaVisitor>... criterias) {
        super(criterias);
    }

    public BasicOrCriteria clone() {
        return new BasicOrCriteria(getCriterias());
    }

}
