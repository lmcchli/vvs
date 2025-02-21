/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip.search;

import com.mobeon.common.util.criteria.CriteriaVisitor;

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
    public void visitProfileStringCriteria(com.abcxyz.services.moip.migration.profilemanager.moip.search.ProfileStringCriteria stringCriteria);

    /**
     * Called by ProfileBooleanCriteria nodes.
     * @param booleanCriteria
     */
    public void visitProfileBooleanCriteria(ProfileBooleanCriteria booleanCriteria);

    /**
     * Called by ProfileIntegerCriteria nodes.
     * @param integerCriteria
     */
    public void visitProfileIntegerCriteria(com.abcxyz.services.moip.migration.profilemanager.moip.search.ProfileIntegerCriteria integerCriteria);
}
