/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.condition;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;

/**
 *  A <code>ConditionInterpreter</code> interprets a <code>Condition</code>
 *  with input <code>IMediaQualifier</code>s.
 *
 *
 *
 * @see Condition
 * @see com.mobeon.masp.mediacontentmanager.IMediaQualifier
 *
 *  @author Mats Egland
 */
public interface ConditionInterpreter {
    /**
     * Interprets if the condition is true of false.
     * The condition may contain any number of variables that
     * is replaced with actual value. The two lists of qualifers
     * is the input to the condition. The first list holds the
     * names of the parameters
     *
     *
     * The qualifiers paramter
     * <code>names</code> holds the names of the specified
     * is paramters for the condition. A qualifier has a name and a
     * value. The condition may contain any number of variables, which
     * name is mathced against the qualifier-names, and the values is
     * inserted instead.
     * The resulting condition is then interpreted.
     *
     * <p/>
     * Example:
     *  Condition:      "numberOfMessages > 1 && currentDate > 2005-11-30"
     *  Expression 1:    Name="numberOfMessages", Value="2"
     *  Expression 2:    Name="currentDate"     , Value="2005-12-30"
     *
     *  Yields the following condition after inserting the
     *  qualifiers by matching the qualifier-number to the
     *  position in the array.
     *
     *   Final Condition: "2 > 1 && 2005-12-30 > 2005-11-30"
     *
     *
     * @param cond          The condition to interpret.
     *
     * @param qualifers         The parameters that the the condition
     *                      contains.
     *                      These qualifiers has only a name.
     *
     *
     *
     *
     * @return              The interpreted result of the condition.
     *
     * @throws ConditionInterpreterException If fails to interpret the condition.
     */
    public boolean interpretCondition(Condition cond,
                                      IMediaQualifier[] qualifers)
            throws ConditionInterpreterException;

}
