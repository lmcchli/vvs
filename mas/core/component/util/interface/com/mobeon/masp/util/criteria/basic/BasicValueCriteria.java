/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.criteria.basic;

import com.mobeon.masp.util.criteria.ValueCriteria;

/**
 * @author qhast
 */
public class BasicValueCriteria extends ValueCriteria<Object, BasicCriteriaVisitor> {

    public BasicValueCriteria(String name, Object value) {
        super(name, value);
    }

    /**
     * Accepts the visitor.
     *
     * @param visitor
     * @see com.mobeon.masp.util.criteria.CriteriaVisitor
     */
    public void accept(BasicCriteriaVisitor visitor) {
        visitor.visitBasicValueCriteria(this);
    }

    public BasicValueCriteria clone() {
        return new BasicValueCriteria(getName(), getValue());
    }

}
