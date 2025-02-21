package com.mobeon.masp.mediacontentmanager;

import java.util.List;

/**
 * Interface for a number rule.
 * <p/>
 * Represents a rule. There is one rule for each divisor. Each rule may have
 * several conditions, {@link INumberRuleCondition}s, for different ranges of
 * the quotient and remainder.
 * The checked number is divided by the rule's divisor.
 *
 * @author mmawi
 */
public interface INumberRule {
    /**
     * Return the divisor for this rule.
     *
     * @return  The divisor for this rule.
     */
    public long getDivisor();

    /**
     * Adds a new condition to this rules.
     *
     * @param condition     The condition to add.
     */
    public void addCondition(INumberRuleCondition condition);

    /**
     * Return this rule's conditions.
     *
     * @return This rule's list of conditions.
     */
    public List<INumberRuleCondition> getConditionList();
}
