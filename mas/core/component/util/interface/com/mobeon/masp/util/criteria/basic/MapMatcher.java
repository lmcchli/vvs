/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.criteria.basic;

import com.mobeon.masp.util.criteria.Criteria;

import java.util.List;
import java.util.Map;

/**
 * Matches a {@link Map<String,Object>} to a {@link Criteria<BasicCriteriaVisitor>}.
 * If the map entries matches the criteria {@link #getIsomorph()} will return true (otherwise false).
 * @author qhast
 */
public class MapMatcher extends BasicCriteraIsomorphBuilder<Boolean>{

    /**
     * Target map.
     */
    private Map<String,Object> target;


    /**
     * Matches map to criteria.
     * @param criteria
     * @param target target map.
     * @return true if map matches criteria (otherwise false).
     */
    public static boolean match(Criteria<BasicCriteriaVisitor> criteria, Map<String,Object> target) {
        MapMatcher m = new MapMatcher(criteria.getParent(),target);
        criteria.accept(m);
        return m.getIsomorph();
    }

    /**
     * Contructs with startparent and target map.
     * @param startParent
     * @param target
     */
    public MapMatcher(Object startParent,Map<String,Object> target) {
        super(startParent);
        this.target = target;
    }

    /**
     * Builds a Basic value criteria isomorph.
     * A boolean indicating if critera matches target map.
     * @param criteria value criteria to match map entry.
     * @return true if map matches criteria.
     */
    protected Boolean createBasicValueCriteriaIsomorph(BasicValueCriteria criteria) {
        return target.get(criteria.getName()).equals(criteria.getValue());
    }

    /**
     * Creates a NOT criteria isomorph from another isomorph.
     *
     * @param isomorph isomorph to negated.
     * @return !isomorph
     */
    protected Boolean createNotCriteriaIsomorph(Boolean isomorph) {
        return !isomorph;
    }

    /**
     * Creates an AND criteria isomorph from a collection of isomorphs.
     *
     * @param isomorphs isomorphs to be conjunctioned.
     * @return true if all elements in list are true (otherwise false).
     */
    protected Boolean createAndCriteriaIsomorph(List<Boolean> isomorphs) {
       for(Boolean b : isomorphs) {
            if(!b) return false;
        }
        return true;
    }

    /**
     * Creates an OR criteria isomorph from a collection of isomorphs.
     *
     * @param isomorphs isomorphs to be disjunctioned.
     * @return true if at least one element in list is true (otherwise false).
     */
    protected Boolean createOrCriteriaIsomorph(List<Boolean> isomorphs) {
        for(Boolean b : isomorphs) {
            if(b) return true;
        }
        return false;
    }


}
