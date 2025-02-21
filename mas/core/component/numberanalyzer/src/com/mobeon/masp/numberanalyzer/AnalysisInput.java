/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

/**
 * Implementation of IAnalysisInput. Contains info about name on the rule, number and an optional regioncode.
 *
 * @author ermmaha
 */
public class AnalysisInput implements IAnalysisInput {

    private String ruleName;
    private String number;
    private String regionCode;

    public void setRule(String ruleName) {
        this.ruleName = ruleName;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setInformationContainingRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getRule() {
        return ruleName;
    }

    public String getNumber() {
        return number;
    }

    public String getInformationContainingRegionCode() {
        return regionCode;
    }
}
