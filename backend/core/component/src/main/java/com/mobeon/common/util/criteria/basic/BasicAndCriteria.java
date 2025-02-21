/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.criteria.basic;

import com.mobeon.common.util.criteria.AndCriteria;
import com.mobeon.common.util.criteria.Criteria;

/**
 * @author qhast
 */
public class BasicAndCriteria extends AndCriteria<BasicCriteriaVisitor> {

    public BasicAndCriteria(Criteria<BasicCriteriaVisitor>... criterias) {
        super(criterias);
    }

    public BasicAndCriteria clone() {
        return new BasicAndCriteria(getCriterias());
    }
}
