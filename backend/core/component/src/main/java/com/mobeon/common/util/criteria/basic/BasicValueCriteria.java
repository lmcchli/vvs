/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.criteria.basic;

import com.mobeon.common.util.criteria.ValueCriteria;

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
     */
    public void accept(BasicCriteriaVisitor visitor) {
        visitor.visitBasicValueCriteria(this);
    }

    public BasicValueCriteria clone() {
        return new BasicValueCriteria(getName(), getValue());
    }

}
