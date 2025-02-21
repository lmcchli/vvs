/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.criteria.basic;

import com.mobeon.common.util.criteria.Criteria;

import java.util.Iterator;
import java.util.List;

/**
 * Builds a SQL statement from a "basic critera".
 *
 * @author qhast
 */
public class BasicSqlStatementBuilder extends BasicCriteraIsomorphBuilder<String> {

    public static String buildStatement(String selectFrom, Criteria<BasicCriteriaVisitor> criteria) {
        BasicSqlStatementBuilder b = new BasicSqlStatementBuilder(criteria.getParent());
        criteria.accept(b);
        return selectFrom + " WHERE " + b.getIsomorph();
    }

    public BasicSqlStatementBuilder(Object startParent) {
        super(startParent);
    }

    protected String createBasicValueCriteriaIsomorph(BasicValueCriteria criteria) {
        return new StringBuffer(criteria.getName()).append("='").append(criteria.getValue()).append("'").toString();
    }

    /**
     * Creates a NOT criteria isomorph from another isomorph.
     *
     * @param isomorph isomorph to negated.
     * @return a NOT criteria isomorph.
     */
    protected String createNotCriteriaIsomorph(String isomorph) {
        return new StringBuffer("NOT (").append(isomorph).append(")").toString();
    }

    /**
     * Creates an AND criteria isomorph from a collection of isomorphs.
     *
     * @param isomorphs isomorphs to be conjunctioned.
     * @return a AND criteria isomorph.
     */
    protected String createAndCriteriaIsomorph(List<String> isomorphs) {
        Iterator<String> it = isomorphs.iterator();
        StringBuffer b = new StringBuffer("(");
        while (it.hasNext()) {
            b.append(it.next());
            if (it.hasNext()) {
                b.append(" AND ");
            }
        }
        b.append(")");
        return b.toString();
    }

    /**
     * Creates an OR criteria isomorph from a collection of isomorphs.
     *
     * @param isomorphs isomorphs to be disjunctioned.
     * @return an OR criteria isomorph.
     */
    protected String createOrCriteriaIsomorph(List<String> isomorphs) {
        Iterator<String> it = isomorphs.iterator();
        StringBuffer b = new StringBuffer("(");
        while (it.hasNext()) {
            b.append(it.next());
            if (it.hasNext()) {
                b.append(" OR ");
            }
        }
        b.append(")");
        return b.toString();
    }


}
