/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.criteria;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Builds an isomorph tree from a critera tree.
 * @author qhast
 * @see com.mobeon.masp.util.criteria.Criteria
 */
public abstract class CriteriaIsomorphBuilder<T> implements CriteriaVisitor {

    private Map<Object,List<T>> criteriaChildrenMap = new Hashtable<Object,List<T>>();

    private T isomorph;

    private final Object startParent;

    protected CriteriaIsomorphBuilder(Object startParent) {
        this.startParent = startParent;
    }

    public void visitNotCriteria(NotCriteria c) {
        List<T> concreteChildren = criteriaChildrenMap.get(c);
        save(c.getParent(),createNotCriteriaIsomorph(concreteChildren.get(0)));
    }

    public void visitAndCriteria(AndCriteria c) {
        save(c.getParent(), createAndCriteriaIsomorph(criteriaChildrenMap.get(c)));
    }

    public void visitOrCriteria(OrCriteria c) {
        save(c.getParent(), createOrCriteriaIsomorph(criteriaChildrenMap.get(c)));
    }

    /**
     * Gets the message property criteria isomorph build by this builder.
     * @return the final isomorph.
     */
    protected T getIsomorph() {
        return isomorph;
    }

    protected Map<Object, List<T>> getCriteriaChildrenMap() {
        return criteriaChildrenMap;
    }

    /**
     * Saves an isomorph as child to a abstract parent.
     * The child(ren)is use at the later composition of the isomorph parent.
     * @param parent the abstract criteria parent.
     * @param isomorph isomorph child.
     */
    protected void save(Object parent, T isomorph) {

        if(parent == this.startParent) {
            this.isomorph = isomorph;
        } else {
            List<T> children = criteriaChildrenMap.get(parent);
            if(children == null) {
                children = new Vector<T>();
                criteriaChildrenMap.put(parent,children);
            }
            children.add(isomorph);
        }
    }

     /**
     * Creates a NOT criteria isomorph from another isomorph.
     * @param isomorph isomorph to negated.
     * @return a NOT criteria isomorph.
     */
    protected abstract T createNotCriteriaIsomorph(T isomorph);

    /**
     * Creates an AND criteria isomorph from a collection of isomorphs.
     * @param isomorphs isomorphs to be conjunctioned.
     * @return a AND criteria isomorph.
     */
    protected abstract T createAndCriteriaIsomorph(List<T> isomorphs);

    /**
     * Creates an OR criteria isomorph from a collection of isomorphs.
     * @param isomorphs isomorphs to be disjunctioned.
     * @return an OR criteria isomorph.
     */
    protected abstract T createOrCriteriaIsomorph(List<T> isomorphs);

}
