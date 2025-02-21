/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.search;

import com.mobeon.masp.util.criteria.CriteriaVisitor;

/**
 * Created by IntelliJ IDEA.
 * User: QHAST
 * Date: 2005-nov-15
 * Time: 14:21:06
 * To change this template use File | Settings | File Templates.
 */
public interface ProfileCriteriaVisitor extends CriteriaVisitor {

    /**
     * Called by ProfileStringCriteria nodes.
     * @param stringCriteria
     */
    public void visitProfileStringCriteria(ProfileStringCriteria stringCriteria);

    /**
     * Called by ProfileBooleanCriteria nodes.
     * @param booleanCriteria
     */
    public void visitProfileBooleanCriteria(ProfileBooleanCriteria booleanCriteria);

    /**
     * Called by ProfileIntegerCriteria nodes.
     * @param integerCriteria
     */
    public void visitProfileIntegerCriteria(ProfileIntegerCriteria integerCriteria);
}
