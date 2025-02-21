/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

import org.jmock.MockObjectTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.HashMap;
import java.util.List;

/**
 * Date: 2006-jan-18
 *
 * @author ermmaha
 */
public class NumberAnalyzerConfigurationTest extends MockObjectTestCase {
    private static final String cfgFile = System.getProperty("user.dir") + "/numberanalyzer/test/com/mobeon/masp/numberanalyzer/numberanalyzer.xml";

    static {
        // Initialize console logging
        // Sets the configuration file for the logging
        //ILoggerFactory.configureAndWatch("../numberanalyzer/log4jconf.xml");
    }

    protected IConfiguration configuration;

    public NumberAnalyzerConfigurationTest(String name) throws Exception {
        super(name);
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(cfgFile);
        configuration = configurationManager.getConfiguration();
    }

    public void testSetConfiguration() throws Exception {
        NumberAnalyzerConfiguration nac = NumberAnalyzerConfiguration.getInstance();
        try {
            nac.setConfiguration(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * test the getRules method
     *
     * @throws Exception
     */
    public void testGetRules() throws Exception {
        NumberAnalyzerConfiguration nac = NumberAnalyzerConfiguration.getInstance();
        nac.setConfiguration(configuration);
        nac.update();

        HashMap<String, Rule> rules = nac.getRules();
        System.out.println("RULES " + rules);

        //Start testing the INBOUNDCALL rule from the config
        Rule toTest = makeRule("INBOUNDCALL", null, null);
        toTest.addSubRule(makeRule("NumberLength", null, "4,12"));
        toTest.addSubRule(makeRule("VeryAll", "^([0-9]*)$", "$i1"));

        Rule inboundRule = rules.get("INBOUNDCALL");
        assertEquals(toTest.getName(), inboundRule.getName());
        assertNull(inboundRule.getInputExpr()); //No input attr --> null
        assertNull(inboundRule.getReturnExpr()); //No return attr --> null

        //test the subrules
        List<Rule> subrules = inboundRule.getSubRules();
        assertEquals(toTest.getSubRules().size(), subrules.size());

        //test the length rule
        Rule numberLengthRule = subrules.get(1);
        assertNull(numberLengthRule.getInputExpr());
        ReturnExpression r = numberLengthRule.getReturnExpr();
        assertTrue(r instanceof LengthReturnExpression);
        assertEquals("4,12", r.toString());
        LengthReturnExpression l = (LengthReturnExpression) r;
        assertEquals(4, l.getMin());
        assertEquals(12, l.getMax());

        //test the "All" rule
        Rule allRule = subrules.get(0);
        assertEquals("^([0-9]*)$", allRule.getInputExpr().toString());
        r = allRule.getReturnExpr();
        assertTrue(r instanceof GroupReturnExpression);
        GroupReturnExpression g = (GroupReturnExpression) r;
        assertEquals("$i1", g.toString());

        doTest2(rules);
    }

    private void doTest2(HashMap<String, Rule> rules) throws Exception {

        //Start testing the RETRIEVALPREFIXRULE rule from the config
        Rule toTest = makeRule("RETRIEVALPREFIXRULE", null, null);
        toTest.addSubRule(makeRule("RetrievalPrefix", "^([0-9]*)$", "$i1"));

        Rule retrievalRule = rules.get("RETRIEVALPREFIXRULE");
        assertEquals(toTest.getName(), retrievalRule.getName());
        assertNull(retrievalRule.getInputExpr()); //No input attr --> null
        assertNull(retrievalRule.getReturnExpr()); //No return attr --> null

        //test the subrules
        List<Rule> subrules = retrievalRule.getSubRules();
        assertEquals(toTest.getSubRules().size(), subrules.size());

        //test the Retrieval rule
        Rule retrievalPrefixRule = subrules.get(0);
        assertEquals("^([0-9]*)$", retrievalPrefixRule.getInputExpr().toString());
        ReturnExpression r = retrievalPrefixRule.getReturnExpr();
        assertTrue(r instanceof GroupReturnExpression);
        GroupReturnExpression g = (GroupReturnExpression) r;
        assertEquals("$i1", g.toString());
    }

    /**
     * test some error handling
     *
     * @throws Exception
     */
    public void testErrors() throws Exception {

    }

    private Rule makeRule(String name, String inputExpr, String returnExpr) throws Exception {
        Rule rule = new Rule(name);
        rule.setExpressions(inputExpr, returnExpr);
        return rule;
    }

    public static Test suite() {
        return new TestSuite(NumberAnalyzerConfigurationTest.class);
    }
}
