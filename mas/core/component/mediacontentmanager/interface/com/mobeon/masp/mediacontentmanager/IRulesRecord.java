package com.mobeon.masp.mediacontentmanager;

import java.util.SortedMap;

/**
 * Interface for a rules record.
 * <p/>
 * A rules record holds a mapping of divisors to {@link INumberRule}s for one
 * {@link com.mobeon.masp.mediacontentmanager.IMediaQualifier.QualiferType}
 * and one or many
 * {@link com.mobeon.masp.mediacontentmanager.IMediaQualifier.Gender}s.
 *
 * @author mmawi
 */
public interface IRulesRecord {

    /**
     * Add a gender for the rules record.
     * @param gender The gender.
     */
    public void addGender(IMediaQualifier.Gender gender);

    /**
     * Used to match a rule with a gender and type.
     * @param gender    The <code>Gender</code> that the rule
     *                  is compared to.
     * @param ruleType  The <code>QualiferType</code> that the
     *                  rule is compared to.
     * @return <code>true</code> if the rule matches the specified
     * <code>gender</code> and <code>ruleType</code>, <code>false</code>
     * otherwise.
     */
    public boolean compareRule(IMediaQualifier.Gender gender, IMediaQualifier.QualiferType ruleType);

    /**
     * Return this rules record's <code>INumberRule</code>s sorted
     * on divisor, descending.
     * @return The list of <code>INumberRule</code>s associated with this
     * rules record.
     */
    public SortedMap<Long, INumberRule> getNumberRules();

    /**
     * Return the <code>INumberRule</code> for a divisor.
     * @param divisor   The divisor to look for.
     * @return Returns the <code>INumberRule</code> for the divisor.
     * If the divisor is not found, <code>null</code> is returned.
     */
    public INumberRule getNumberRule(Long divisor);

    /**
     * Add a <code>INumberRule</code> to the rules record.
     * @param numberRule The INumberRule to add.
     */
    public void addNumberRule(INumberRule numberRule);
}
