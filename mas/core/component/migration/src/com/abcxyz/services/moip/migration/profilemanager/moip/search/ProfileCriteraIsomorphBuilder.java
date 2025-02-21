/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip.search;

import com.mobeon.common.util.criteria.CriteriaIsomorphBuilder;
import com.mobeon.masp.profilemanager.UnknownAttributeException;

/**
 * Created by IntelliJ IDEA.
 * User: QHAST
 * Date: 2005-nov-15
 * Time: 14:28:24
 * To change this template use File | Settings | File Templates.
 */
public abstract class ProfileCriteraIsomorphBuilder<T> extends CriteriaIsomorphBuilder<T> implements ProfileCriteriaVisitor {

    public ProfileCriteraIsomorphBuilder(Object startParent) {
        super(startParent);
    }

    /**
     * Called by ProfileStringCriteria nodes.
     *
     * @param stringCriteria
     */
    public void visitProfileStringCriteria(ProfileStringCriteria stringCriteria) {
        try {
            save(stringCriteria.getParent(),createProfileStringCriteriaIsomorph(stringCriteria));
        } catch (UnknownAttributeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called by ProfileBooleanCriteria nodes.
     *
     * @param booleanCriteria
     */
    public void visitProfileBooleanCriteria(ProfileBooleanCriteria booleanCriteria) {
        save(booleanCriteria.getParent(), createProfileBooleanCriteriaIsomorph(booleanCriteria));
    }

    /**
     * Called by ProfileIntegerCriteria nodes.
     *
     * @param integerCriteria
     */
    public void visitProfileIntegerCriteria(ProfileIntegerCriteria integerCriteria) {
        save(integerCriteria.getParent(), createProfileIntegerCriteriaIsomorph(integerCriteria));
    }

    protected abstract T createProfileStringCriteriaIsomorph(ProfileStringCriteria stringCriteria) throws UnknownAttributeException;

    protected abstract T createProfileBooleanCriteriaIsomorph(ProfileBooleanCriteria booleanCriteria);

    protected abstract T createProfileIntegerCriteriaIsomorph(ProfileIntegerCriteria integerCriteria);


}
