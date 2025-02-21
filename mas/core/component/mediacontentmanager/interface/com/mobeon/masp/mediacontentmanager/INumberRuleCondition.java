package com.mobeon.masp.mediacontentmanager;

import java.util.List;

/**
 * Interface for a number rule contidition
 * <p/>
 * Represents a single condition for a number rule.
 * <p/>
 * A condition has a range of quotients and remainders that it is valid for.
 * If the checked number's remainder and quotient when divided by the
 * number rule's divisor is in this range, the condition is
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
public interface INumberRuleCondition {
    /**
     * Add a new <code>IActionElement</code> to the end of the list.
     *
     * @param element <code>IActionElement</code> to add.
     */
    public void addActionElement(IActionElement element);

    /**
     * Returns this conditions list of <code>IActionElement</code>s.
     *
     * @return The list of <code>IActionElement</code>s.
     */
    public List<IActionElement> getActionElementList();

    /**
     * Check if this condition is "terminal".
     *
     * @return <code>true</code> if the condition is "terminal".
     * <code>false</code> if the condition is "not terminal".
     */
    public boolean isTerminal();

    /**
     * Check if the condition is "atomic".
     *
     * @return <code>true</code> if the condition is "atomic".
     * <code>false</code> if the condition is "not atomic".
     */
    public boolean isAtomic();

    /**
     * Check if the condition is "divide".
     *
     * @return <code>true</code> if the condition is "divide".
     * <code>false</code> if the condition is "not divide".
     */
    public boolean isDivide();

    /**
     * Check if this condition is satisfied for the given quotient and
     * remainder.
     *
     * @param quotient  The quotient from the division of the number that will be checked.
     * @param remainder The remainder from the division of the number that will be checked.
     * @return <code>true</code> if the <code>quotient</code> and <code>remainder</code>
     * is in this rule's range. <code>false</code> otherwise.
     */
    public boolean isSatified(long quotient, long remainder);
}
