/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.criteria.basic;

import com.mobeon.common.util.criteria.CriteriaVisitor;

/**
 * @author qhast
 */
public interface BasicCriteriaVisitor extends CriteriaVisitor {

    public void visitBasicValueCriteria(BasicValueCriteria criteria);

}
