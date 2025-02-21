/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.util.criteria.Criteria;


/**
 * Represents a criteria of logical disjunction between of a collection of criterias.
 *
 * @author qhast
 * @see AndCriteria
 * @see NotCriteria
 */
public class OrCriteria extends com.mobeon.masp.util.criteria.OrCriteria<MessagePropertyCriteriaVisitor> {


    /**
     * Create an OrCriteria with other criterias.
     * 
     * @param criterias the criterias to be disjunctioned.
     */
    public OrCriteria(Criteria<MessagePropertyCriteriaVisitor>... criterias) {
        super(criterias);
    }

    public OrCriteria clone() {
        return new OrCriteria(getCriterias());
    }

}
