/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.util.criteria.Criteria;

/**
 * Represents a criteria of logical conjunction between of a collection of criterias.
 *
 * @author qhast
 * @see OrCriteria
 * @see NotCriteria
 */
public class AndCriteria extends com.mobeon.masp.util.criteria.AndCriteria<MessagePropertyCriteriaVisitor> {


    /**
     * Create an AndCriteria with other criterias.
     *
     * @param criterias the criterias to be conjunctioned.
     */
    public AndCriteria(Criteria<MessagePropertyCriteriaVisitor>... criterias) {
        super(criterias);
    }

    public AndCriteria clone() {
        return new AndCriteria(getCriterias());
    }

}
