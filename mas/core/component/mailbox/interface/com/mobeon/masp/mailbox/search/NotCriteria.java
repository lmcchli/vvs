/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.search;

import com.mobeon.masp.util.criteria.Criteria;

/**
 * Represents a criteria of logical negation of a criteria.
 *
 * @author qhast
 * @see OrCriteria
 * @see AndCriteria
 */
public class NotCriteria extends com.mobeon.masp.util.criteria.NotCriteria<MessagePropertyCriteriaVisitor> {

    /**
     * Create a NotCriteria with other criteria.
     *
     * @param criteria the criteria to be negated.
     */
    public NotCriteria(Criteria<MessagePropertyCriteriaVisitor> criteria) {
        super(criteria);
    }

    public NotCriteria clone() {
        return new NotCriteria(getCriteria());
    }

}
