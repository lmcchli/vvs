/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.criteria;

/**
 * Defines a visitor able to be accepted by a criteria tree.
 * Classes implementing CriteriaVisitor can
 * "visit" a criteria tree by using
 * {@link Criteria#accept(CriteriaVisitor)} 
 *  <br>
 * When accepting a visitor the criteria tree
 * do a depth-first search for nodes. Criteria children are always
 * visited before parents. For example in the below tree will E and F
 * be visited before A and D. D before A. C before B and A. B before A.
 * <br>
 * This is only guarenteed for children and ancestors NOT for siblings, cousins etc.
 * e.g. B may be visited before E, E before F and vice versa.
 * <pre>
 * OrCriteria A
 *  |
 *  +-- NotCriteria B
 *  |    |
 *  |    +-- xCriteria C
 *  |
 *  +-- AndCriteria D
 *       |
 *       +-- xCriteria E
 *       |
 *       +-- xCriteria F
 *
 * </pre>
 * @author qhast
 * @see Criteria
 */
public interface CriteriaVisitor {


    /**
     * Called by NotCriteria nodes.
     * @param c
     */
    public void visitNotCriteria(NotCriteria c);

    /**
     * Called by AndCriteria nodes.
     * @param c
     */
    public void visitAndCriteria(AndCriteria c);

    /**
     * Called by OrCriteria nodes.
     * @param c
     */
    public void visitOrCriteria(OrCriteria c);

}
