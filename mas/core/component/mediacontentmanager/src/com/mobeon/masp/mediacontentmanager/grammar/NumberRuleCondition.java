/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.grammar;

import com.mobeon.masp.mediacontentmanager.IActionElement;
import com.mobeon.masp.mediacontentmanager.INumberRuleCondition;

import java.util.List;
import java.util.ArrayList;

/**
 * Default implementation of {@link INumberRuleCondition}.
 * <p/>
 * Represents a single condition for a {@link NumberRule}.
 * <p/>
 * A condition has a range of quotients and remainders that it is valid for.
 * If the checked number's remainder and quotient when divided by the
 * <code>NumberRule</code>'s divisor is in this range, the condition is
 * satisfied. Its {@link IActionElement}s shall then be applied to the result
 * of the number decomposition.
 * <p/>
 * A condition has three properties: atomic, terminal and divide.
 * <ul>
 * <li>An atomic condition means that the checked number will not be combined
 * by multiple numbers. E.g. in english rule: 23 is not atomic, it is combined
 * by "twenty" and "three". 17 is atomic, it is only "seventeen".</li>
 * <li>If a condition is not terminal, the quotient will be checked further
 * before this condition's actions are applied. For example in english rule,
 * if 200 is decomposed, we will get a quotient 2 when it is divided by 100.
 * This quotient should also be decomposed to see how many "hundreds" there
 * are. The result is then "two" "hundred".</li>
 * <li>If the condition is divide, the checked number should be divided by the
 * condition's divisor. If the condition is then satisfied, no more conditions
 * are tested for this divisor. If the condition is not divide, the next
 * condition will also be tested even if this condition is satisfied.</li>
 * </ul>
 *
 * @author mmawi
 */
public class NumberRuleCondition implements INumberRuleCondition {

    /**
     * Used to determine if the condition is atomic or not.
     * An atomic condition means that the checked number will not be combined
     * by multiple numbers. E.g. in english rule: 23 is not atomic, it is
     * combined by "twenty" and "three". 17 is atomic, it is only "seventeen".
     */
    private boolean atomic;

    /**
     * This condition's lower bound for the quotient.
     */
    private long quotientFrom;

    /**
     * This condition's upper bound for the quotient.
     */
    private long quotientTo;

    /**
     * This condition's lower bound for the remainder.
     */
    private long remainderFrom;

    /**
     * This condition's upper bound for the remainder.
     */
    private long remainderTo;

    /**
     * Used to determine if the quotient shall be checked further before this
     * condition's action are applied. For example in english rule, if 200 is
     * decomposed, we will get a quotient 2 when it is divided by 100. This
     * quotient should also be decomposed to see how many "hundreds" there are.
     * The result is then "two" "hundred".
     * <code>true</code> The quotient will not be checked further.
     * <code>false</code> The quotient will be checked further.
     */
    private boolean terminal;

    /**
     * Used to determine if the checked number must be divided.
     * <code>true</code> The number will be divided.
     * <code>false</code> The number will not be divided.
     */
    private boolean divide;

    /**
     * List of <code>IActionElement</code>s that together will decide the final
     * message's structure.
     */
    private List<IActionElement> actionElementList = new ArrayList<IActionElement>();

    /**
     * Creates a new condition with the given ranges of quotient and remainder
     * and the given properties.
     *
     * @param atomic        If the condition is "atomic".
     * @param quotientFrom  The lower bound of the quotient.
     * @param quotientTo    The upper bound of the quotient.
     * @param remainderFrom The lower bound of the remainder.
     * @param remainderTo   The upper bound of the remainder.
     * @param terminal      It the condition is "terminal".
     * @param divide        If the condition is "divide".
     */
    public NumberRuleCondition(boolean atomic, long quotientFrom, long quotientTo, long remainderFrom,
                               long remainderTo, boolean terminal, boolean divide) {
        this.atomic = atomic;
        this.quotientFrom = quotientFrom;
        this.quotientTo = quotientTo;
        this.remainderFrom = remainderFrom;
        this.remainderTo = remainderTo;
        this.terminal = terminal;
        this.divide = divide;
    }

    /**
     * Add a new <code>IActionElement</code> to the end of the list.
     *
     * @param element <code>IActionElement</code> to add.
     */
    public void addActionElement(IActionElement element) {
        actionElementList.add(element);
    }

    /**
     * Returns this conditions list of <code>IActionElement</code>s.
     *
     * @return The list of <code>IActionElement</code>s.
     */
    public List<IActionElement> getActionElementList() {
        return actionElementList;
    }

    /**
     * Check if this condition is "terminal".
     *
     * @return <code>true</code> if the condition is "terminal".
     * <code>false</code> if the condition is "not terminal".
     */
    public boolean isTerminal() {
        return terminal;
    }

    /**
     * Check if the condition is "atomic".
     *
     * @return <code>true</code> if the condition is "atomic".
     * <code>false</code> if the condition is "not atomic".
     */
    public boolean isAtomic() {
        return atomic;
    }

    /**
     * Check if the condition is "divide".
     *
     * @return <code>true</code> if the condition is "divide".
     * <code>false</code> if the condition is "not divide".
     */
    public boolean isDivide() {
        return divide;
    }

    /**
     * Check if this condition is satisfied for the given quotient and
     * remainder.
     *
     * @param quotient  The quotient from the division of the number that will be checked.
     * @param remainder The remainder from the division of the number that will be checked.
     * @return <code>true</code> if the <code>quotient</code> and <code>remainder</code>
     * is in this rule's range. <code>false</code> otherwise.
     */
    public boolean isSatified(long quotient, long remainder) {
        return (quotient >= quotientFrom && quotient <= quotientTo) &&
                (remainder >= remainderFrom && remainder <= remainderTo);
    }
}
