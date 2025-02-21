/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

/**
 * This interface is used for creating input needed for number analysis.
 * The information needed is the number analysis rule to use and the number to analyse.
 */
public interface IAnalysisInput {
    /**
     * Sets the rule to use for the number analysis
     * @param ruleName The name of the rule
     */
    public void setRule(String ruleName);

    /**
     * Sets the number which should be analyzed
     * @param number The number to analyze
     */
    public void setNumber(String number);

    /**
     * Optionally set information containing a region code, e.g. the ANI of the caller
     * @param information 
     */
    public void setInformationContainingRegionCode(String information);

    /**
     * Returns the rule to use for the number analysis
     * @return current rule
     */
    public String getRule();

    /**
     * Returns the number to analyze
     * @return current number
     */
    public String getNumber();

    /**
     * Returns region code information
     * @return current region code information
     */
    public String getInformationContainingRegionCode();
}
