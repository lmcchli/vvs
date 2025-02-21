package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.numberanalyzer.INumberAnalyzer;
import com.mobeon.masp.numberanalyzer.IAnalysisInput;
import com.mobeon.masp.numberanalyzer.NumberAnalyzerException;

/**
 * A mock object for the number analyzer.
 */
public class NumberAnalyzerMock extends BaseMock implements INumberAnalyzer {

    /**
     * Constructs the mock object for the number analyzer
     */
    public NumberAnalyzerMock ()
    {
        super ();
        log.info ("MOCK: NumberAnalyzerMock.NumberAnalyzerMock");
    }

/**
     * Factory method for creating a new IAnalysisInput to send as information to the number analyzer
     * @return An empty IAnalysisInput
     */
    public IAnalysisInput getAnalysisInput()
    {
        log.info ("MOCK: NumberAnalyzerMock.getAnalysisInput");
        log.info ("MOCK: NumberAnalyzerMock.getAnalysisInput unimplemented");
        return null;
    }

    /**
     * Analyzes a number according to rule and optional region code information
     * @param input The number(s) and rule to use for the analysis
     * @return The result of the number analysis
     * @throws com.mobeon.masp.numberanalyzer.NumberAnalyzerException when some error occured when the number is analyzed
     */
    public String analyzeNumber(IAnalysisInput input) throws NumberAnalyzerException
    {
        log.info ("MOCK: NumberAnalyzerMock.anaylzeNumber");
        log.info ("MOCK: NumberAnalyzerMock.anaylzeNumber unimplemented");
        return "";
    }

}
