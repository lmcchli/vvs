/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.criteria.basic;

import com.mobeon.masp.util.criteria.Criteria;
import com.mobeon.masp.util.criteria.NotCriteria;

/**
 * @author qhast
 */
public class BasicNotCriteria extends NotCriteria<BasicCriteriaVisitor> {

    public BasicNotCriteria(Criteria<BasicCriteriaVisitor> criteria) {
        super(criteria);
    }

    public BasicNotCriteria clone() {
        return new BasicNotCriteria(getCriteria());
    }

}
