/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.criteria.basic;

import com.mobeon.common.util.criteria.CriteriaIsomorphBuilder;

/**
 * @author qhast
 */
public abstract class BasicCriteraIsomorphBuilder<T> extends CriteriaIsomorphBuilder<T> implements BasicCriteriaVisitor {

    public BasicCriteraIsomorphBuilder(Object startParent) {
        super(startParent);
    }

    public void visitBasicValueCriteria(BasicValueCriteria criteria) {
        save(criteria.getParent(), createBasicValueCriteriaIsomorph(criteria));
    }

    protected abstract T createBasicValueCriteriaIsomorph(BasicValueCriteria criteria);

}
