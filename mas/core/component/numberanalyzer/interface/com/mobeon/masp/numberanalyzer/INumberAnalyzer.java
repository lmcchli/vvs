/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

/**
 * Interface used to analyze numbers according to specified rules.
 */
public interface INumberAnalyzer {
    /**
     * Factory method for creating a new IAnalysisInput to send as information to the number analyzer
     * @return An empty IAnalysisInput
     */
    public IAnalysisInput getAnalysisInput();

    /**
     * Analyzes a number according to rule and optional region code information
     * @param input The number(s) and rule to use for the analysis
     * @return The result of the number analysis
     * @throws NumberAnalyzerException when some error occured when the number is analyzed
     */
    public String analyzeNumber(IAnalysisInput input) throws NumberAnalyzerException;
}
