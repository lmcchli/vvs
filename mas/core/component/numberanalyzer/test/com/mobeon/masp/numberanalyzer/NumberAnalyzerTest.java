/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.MockObjectTestCase;

/**
 * Test the NumberAnalyzer class but does not set any configuration. The rules are added to the class via the
 * addRulesConfig method.
 *
 * @author ermmaha
 */
public class NumberAnalyzerTest extends MockObjectTestCase {

    static {
        // Initialize console logging
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch("../numberanalyzer/log4jconf.xml");
    }

    public NumberAnalyzerTest(String name) {
        super(name);
    }

    /**
     * Test invalid values on the expressions
     *
     * @throws Exception if testcase fails.
     */
    public void testInvalidExpressions() throws Exception {
        // test invalid value
        try {
            makeRule("errorlen", null, "    ");
            fail("Expected NumberAnalyzerException");
        } catch (NumberAnalyzerException nax) {
            System.out.println("NumberAnalyzerException " + nax);
        }

        // test invalid value on length
        try {
            makeRule("errorlen", null, "4,a");
            fail("Expected NumberAnalyzerException");
        } catch (NumberAnalyzerException nax) {
            System.out.println("NumberAnalyzerException " + nax);
        }

        // test min length larger than max length
        try {
            makeRule("errorlen", null, "12,4");
            fail("Expected NumberAnalyzerException");
        } catch (NumberAnalyzerException nax) {
            System.out.println("NumberAnalyzerException " + nax);
        }

        // test groupexpression with no input
        try {
            makeRule("errorlen", null, "$i0");
            fail("Expected NumberAnalyzerException");
        } catch (NumberAnalyzerException nax) {
            System.out.println("NumberAnalyzerException " + nax);
        }

        // test wasted groupexpression
        try {
            makeRule("errorlen", "\\d*", "crazy");
            fail("Expected NumberAnalyzerException");
        } catch (NumberAnalyzerException nax) {
            System.out.println("NumberAnalyzerException " + nax);
        }

        // test wasted groupidentifier on the groupexpression (must run the analyser to find that error)
        Rule rule = makeRule("errorlen", "\\d*", "$i1");
        NumberAnalyzer numberAnalyzer = new NumberAnalyzer();
        numberAnalyzer.addRulesConfig(rule);
        IAnalysisInput analysisInput = numberAnalyzer.getAnalysisInput();
        analysisInput.setRule("errorlen");
        analysisInput.setNumber("555161074");
        assertException(numberAnalyzer, analysisInput);

        // same
        rule = makeRule("errorlen", "\\d*", "$ix");
        numberAnalyzer = new NumberAnalyzer();
        numberAnalyzer.addRulesConfig(rule);
        analysisInput = numberAnalyzer.getAnalysisInput();
        analysisInput.setRule("errorlen");
        analysisInput.setNumber("555161074");
        assertException(numberAnalyzer, analysisInput);
    }

    /**
     * Do a test with a rule that has regioncodes configuration
     *
     * @throws Exception if testcase fails.
     */
    public void testAnalyzeNumber() throws Exception {
        Rule rule = makeRule("Rule1", null, null);
        Rule subRule1 = makeRule("NumberLength", null, "4,12");
        Rule subRule2 = makeRule("Local1", "^(555)(\\d*)$", "$i2");
        subRule2.setRegionCodeRuleName("RegionCodes");
        rule.addSubRule(subRule1);
        rule.addSubRule(subRule2);
        Rule regionCodes = makeRule("RegionCodes", "060,061,062", null);

        NumberAnalyzer numberAnalyzer = new NumberAnalyzer();
        numberAnalyzer.addRulesConfig(rule);
        numberAnalyzer.addRulesConfig(regionCodes);

        IAnalysisInput analysisInput = numberAnalyzer.getAnalysisInput();
        analysisInput.setRule("Rule1");
        analysisInput.setNumber("555161074");
        analysisInput.setInformationContainingRegionCode("060161074");

        String result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("060161074", result);

        //change regioncode
        analysisInput.setInformationContainingRegionCode("062161074");
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("062161074", result);

        //test with no regioncode
        analysisInput.setInformationContainingRegionCode(null);
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("161074", result);

        //test not configured regioncode
        analysisInput.setInformationContainingRegionCode("101");
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("161074", result);

        //test invalid name on regioncode
        subRule2.setRegionCodeRuleName("RegionCodesNOTFOUND");
        analysisInput.setInformationContainingRegionCode("060161074");
        assertException(numberAnalyzer, analysisInput);

        //test no match
        analysisInput.setNumber("666161074");
        assertException(numberAnalyzer, analysisInput);
    }

    /**
     * @throws Exception if testcase fails.
     */
    public void testAnalyzeNumber2() throws Exception {
        NumberAnalyzer numberAnalyzer = new NumberAnalyzer();
        setupRules(numberAnalyzer);
        IAnalysisInput analysisInput = numberAnalyzer.getAnalysisInput();

        doTestInboundcall(numberAnalyzer, analysisInput);

        doTestSubscriberoutdial(numberAnalyzer, analysisInput);

        //Test with rule that does not exists.
        analysisInput.setRule("DOESNOTEXIST");
        analysisInput.setNumber("1000");
        assertException(numberAnalyzer, analysisInput);

        //Test if number is null.
        analysisInput.setRule("INBOUNDCALL");
        analysisInput.setNumber(null);
        assertException(numberAnalyzer, analysisInput);
    }

    public void testFixedReturnNumber() throws Exception {
        Rule rule = makeRule("FixedReturnNumber", null, null);
        Rule subRule1 = makeRule("NumberLength", null, "4,12");
        Rule subRule2 = makeRule("Fixed", "^(060)([0-9]+)$", "060161000");
        rule.addSubRule(subRule1);
        rule.addSubRule(subRule2);
        NumberAnalyzer numberAnalyzer = new NumberAnalyzer();
        numberAnalyzer.addRulesConfig(rule);
        IAnalysisInput analysisInput = numberAnalyzer.getAnalysisInput();
        analysisInput.setRule("FixedReturnNumber");
        analysisInput.setNumber("060161916");

        String result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("060161000", result);
    }

    private void doTestInboundcall(NumberAnalyzer numberAnalyzer, IAnalysisInput analysisInput) throws Exception {
        analysisInput.setRule("INBOUNDCALL");

        analysisInput.setNumber("1000"); //test match of Office subrule
        String result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("4660101000", result);
        analysisInput.setNumber("1099"); //test match of Office subrule
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("4660101099", result);
        analysisInput.setNumber("1900"); //test match of Office subrule
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("4660101900", result);
        analysisInput.setNumber("1999"); //test match of Office subrule
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("4660101999", result);

        analysisInput.setNumber("10000");  //test match of Local subrule
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("466010000", result);

        analysisInput.setNumber("999999");  //test match of Local subrule
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("4660999999", result);


        analysisInput.setNumber("01000000");  //test match of National subrule
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("461000000", result);
        analysisInput.setNumber("0199999999");  //test match of National subrule
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("46199999999", result);

        analysisInput.setNumber("00000000000");  //test match of International subrule
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("0000000000", result);
        analysisInput.setNumber("09999999999");  //test match of International subrule
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("9999999999", result);

        analysisInput.setNumber("000000000000");  //test match of Unknown subrule
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("0000000000", result);
        analysisInput.setNumber("009999999999");  //test match of Unknown subrule
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("9999999999", result);

        System.out.println("result " + result);

        //Check length
        analysisInput.setNumber("999");
        assertException(numberAnalyzer, analysisInput);
        analysisInput.setNumber("9991119991119"); //13digits
        assertException(numberAnalyzer, analysisInput);

        //Check no match
        analysisInput.setNumber("3000");
        assertException(numberAnalyzer, analysisInput);
    }

    private void doTestSubscriberoutdial(NumberAnalyzer numberAnalyzer, IAnalysisInput analysisInput) throws Exception {
        analysisInput.setRule("SUBSCRIBEROUTDIAL");

        analysisInput.setNumber("0710000"); //test match of Block071 subrule
        assertException(numberAnalyzer, analysisInput);

        analysisInput.setNumber("071999999"); //test match of Block071 subrule
        assertException(numberAnalyzer, analysisInput);

        ///Check no match
        analysisInput.setNumber("333");
        assertException(numberAnalyzer, analysisInput);
    }

    private void assertException(NumberAnalyzer numberAnalyzer, IAnalysisInput analysisInput) {
        try {
            numberAnalyzer.analyzeNumber(analysisInput);
        } catch (NumberAnalyzerException nax) {
            System.out.println("NumberAnalyzerException " + nax);
            //if (nax.getReason() != null) System.out.println(nax.getReason());
            return;
        }
        fail("Expected NumberAnalyzerException");
    }

    private void setupRules(NumberAnalyzer numberAnalyzer) throws Exception {
        numberAnalyzer.addRulesConfig(makeInboundCallRule());
        numberAnalyzer.addRulesConfig(makeSubscriberoutdialRule());
    }

    private Rule makeInboundCallRule() throws Exception {
        Rule rule = makeRule("INBOUNDCALL", null, null);
        Rule subRule1 = makeRule("NumberLength", null, "4,12");
        Rule subRule2 = makeRule("Office", "^(1[09])([0-9]{2})$", "466010$i1$i2");
        Rule subRule3 = makeRule("Local", "^([1-9])([0-9]{4,5})$", "4660$i1$i2");
        Rule subRule4 = makeRule("National", "^(0)(1)([0-9]{6,8})$", "46$i2$i3");
        Rule subRule5 = makeRule("International", "^(0)([0-9]{10})$", "$i2");
        Rule subRule6 = makeRule("Unknown", "^(00)([0-9]{10})$", "$i2");
        //Rule subRule2 = makeRule("All", "^(060)([0-9]+)", "$i1");
        rule.addSubRule(subRule1);
        rule.addSubRule(subRule2);
        rule.addSubRule(subRule3);
        rule.addSubRule(subRule4);
        rule.addSubRule(subRule5);
        rule.addSubRule(subRule6);
        return rule;
    }

    private Rule makeSubscriberoutdialRule() throws Exception {
        Rule rule = makeRule("SUBSCRIBEROUTDIAL", null, null);
        Rule subRule1 = makeRule("NumberLength", null, "4,12");
        Rule subRule2 = makeRule("Block071", "^(071)([0-9]{4,6})$", "Block");
        rule.addSubRule(subRule1);
        rule.addSubRule(subRule2);
        return rule;
    }

    private Rule makeRule(String name, String inputExpr, String returnExpr) throws Exception {
        Rule rule = new Rule(name);
        rule.setExpressions(inputExpr, returnExpr);
        return rule;
    }

    public static Test suite() {
        return new TestSuite(NumberAnalyzerTest.class);
    }
}
