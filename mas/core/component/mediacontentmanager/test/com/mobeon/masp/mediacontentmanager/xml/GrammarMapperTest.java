/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.xml;

import org.jmock.MockObjectTestCase;
import org.apache.log4j.xml.DOMConfigurator;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediacontentmanager.grammar.RulesRecord;
import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediacontentmanager.IActionElement;
import com.mobeon.masp.mediacontentmanager.INumberRule;
import com.mobeon.masp.mediacontentmanager.INumberRuleCondition;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.util.List;
import java.util.SortedMap;

/**
 * JUnit test for the {@link GrammarMapper} class.
 *
 * @author mmawi
 */
public class GrammarMapperTest extends MockObjectTestCase {
    /**
     * The logger used.
     */
    private static ILogger LOGGER =
            ILoggerFactory.getILogger(GrammarMapperTest.class);

    /**
     * The mapper object tested.
     */
    private GrammarMapper grammarMapper;

    private List<RulesRecord> rulesRecordList;

    /**
     * The grammar.xml file to parse.
     */
    private static String grammarFileName = "test/grammarfiles/grammar.xml";
            //"applications/mediacontentpackages/en_audio_1/grammar.xml";
    /**
     * A well-formed file but with illegal content.
     */
    private static String illegalContentFile =
            "test/grammarfiles/IllegalGrammar.xml";
    /**
     * A not-well-formed file.
     */
    private static String notWellFormedContentFile =
            "test/grammarfiles/NotWellFormedGrammar.xml";
    /**
     * URL to a well-formed XML with valid content.
     */
    private URL validPackageXML = null;
    /**
     * URL to a well-formed XML, but that has illegal content.
     */
    private URL illegalContentPackageXML = null;
    /**
     * URL to a non-well-formed XML
     */
    private URL notWellFormedXML = null;

    public void setUp() throws Exception {
        super.setUp();
        grammarMapper = new GrammarMapper();

        try {
            File file = new File(grammarFileName);
            validPackageXML = file.toURL();
        } catch (MalformedURLException e) {
            fail("Failed to create url from: "+ grammarFileName);
        }
        try {
            File file = new File(illegalContentFile);
            illegalContentPackageXML = file.toURL();
        } catch (MalformedURLException e) {
            fail("Failed to create url from: " + illegalContentFile);
        }
        try {
            File file = new File(notWellFormedContentFile);
            notWellFormedXML = file.toURL();
        } catch (MalformedURLException e) {
            fail("Failed to create url from: " + notWellFormedContentFile);
        }
    }

    // Cleans up after each test
    public void tearDown() throws Exception {
        super.tearDown();
        grammarMapper = null;
    }

    /**
     * Tests for the {@link GrammarMapper#fromXML(java.net.URL)}
     * method.
     * <p/>
     * <pre>
     * 1. Illegal Files
     *  Condition:
     * <p/>
     *  Action:
     *      1.1) file is Well-formed but illegal content.
     *      1.2) file is not Well-formed.
     *  Result:
     *      1-2. SaxMapperException
     * <p/>
     * 2. Parse Grammar file.
     *  Condition:
     *      A FileReader is created to the Grammar file.
     *      The Grammar file is well formed.
     *  Action:
     *  Result:
     *      A non-null List of RulesRecords is returned.
     * <p/>
     * 3. Validate content in returned object from a grammar xml.
     * Condition:
     *      A non-null List of RulesRecords is returned from
     *      a well formed Grammar xml.
     *  Action:
     *  Result:
     *      3.1) There are two rules in the xml file.
     *      3.2) The first rule is "Number", "Female".
     *      3.3) The first rule contains the divisors "1000" and "100".
     *      3.4) The divisor "1000" have two conditions.
     *      3.5) The second rule is "Number", "Male,None".
     *      3.6) The second rule contains only the divisor "1000".
     *      3.7) The divisor "1000" have one condition with two actions.
     *      3.8) The first action is "mediafile" with reference "1e3.wav"
     *           and the second is "swap" with value "-1".
     *
     * </pre>
     */
    public void testFromXML() {
        // 1.1 Illegal content
        try {
            List<RulesRecord> rrList =
                    grammarMapper.fromXML(illegalContentPackageXML);
            fail("SaxMapperException should be thrown if illegal content xml");
        } catch (SaxMapperException e) {
            /*ok*/
        }

        // 1.2 Not well-formed XML.
        try {
            List<RulesRecord> rrList =
                    grammarMapper.fromXML(notWellFormedXML);
            fail("SaxMapperException should be thrown if not well formed xml");
        } catch (SaxMapperException e) {
            /*ok*/
        }

        //2
        rulesRecordList = grammarMapper.fromXML(validPackageXML);
        assertNotNull(rulesRecordList);

        // 3
        // 3.1 Check that there are two rules in grammar.xml
        assertEquals("Did not find two rules in XML file.", 2,
                rulesRecordList.size());

        // 3.2 Check that the first rule have gender=female and type=number
        RulesRecord firstRule = rulesRecordList.get(0);
        assertNotNull(firstRule);
        assertTrue("Rule is not of type number or not gender female.",
                firstRule.compareRule(IMediaQualifier.Gender.Female, IMediaQualifier.QualiferType.Number));

        // 3.3 Check that the divisors 1000 and 100 are in the first rule
        SortedMap<Long, INumberRule> numberRules = firstRule.getNumberRules();
        assertNotNull(numberRules);
        assertEquals("There is not exactly 2 divisors in the first rule.", 2,
                numberRules.size());
        assertTrue("Divisor 100 not found in the first rule.",
                numberRules.containsKey(new Long("100")));
        assertTrue("Divisor 1000 not found in the first rule.",
                numberRules.containsKey(new Long("1000")));

        // 3.4 Check that there are two conditions for the divisor 1000.
        List<INumberRuleCondition> conditionList =
                numberRules.get(new Long("1000")).getConditionList();
        assertNotNull(conditionList);
        assertEquals("There is not exactly 2 conditions for the divisor 1000.", 2,
                conditionList.size());

        // 3.5 Check that the second rule have gender=male,none and type=number
        RulesRecord secondRule = rulesRecordList.get(1);
        assertNotNull(secondRule);
        assertTrue("Rule is not of type number or not gender male.",
                secondRule.compareRule(IMediaQualifier.Gender.Male, IMediaQualifier.QualiferType.Number));
        assertTrue("Rule is not of type number or not gender none.",
                secondRule.compareRule(IMediaQualifier.Gender.None, IMediaQualifier.QualiferType.Number));

        // 3.6 Check that the second rule have a divisor 1000.
        INumberRule numberRule = secondRule.getNumberRule((long) 1000);
        assertNotNull(numberRule);

        // 3.7 Check that the divisor 1000 have one condition.
        List<INumberRuleCondition> conditions = numberRule.getConditionList();
        assertEquals("There is not exactly one condition for divisor 1000.",
                1, conditions.size());

        // 3.8 Check that the condition have two actions: mediafile and swap
        List<IActionElement> actions = conditions.get(0).getActionElementList();
        assertEquals("The first action is not a mediafile.",
                IActionElement.ActionType.mediafile, actions.get(0).getType());
        assertEquals("The secont action is not swap.",
                IActionElement.ActionType.swap, actions.get(1).getType());
        assertEquals("The mediafile does not have ref 1e3.wav",
                "1e3.wav", actions.get(0).getMediaFileName());
        assertEquals("The swap is not -1",
                -1, actions.get(1).getSwapValue());
    }

    public void testInvalidContentsRuleType() throws Exception {
        URL grammarUrl = null;
        String filename = "test/grammarfiles/invalidRuleType.xml";
        try {
            File file = new File(filename);
            grammarUrl = file.toURL();
            List<RulesRecord> rrList = grammarMapper.fromXML(grammarUrl);
            fail("SaxMapperException expected when grammar file contains invalid Rule type!");
        } catch (MalformedURLException e) {
            fail("Failed to create url from: " + filename);
        } catch (SaxMapperException e) {
            //OK
        }
    }

    public void testInvalidContentsGender() throws Exception {
        URL grammarUrl = null;
        String filename = "test/grammarfiles/invalidGender.xml";
        try {
            File file = new File(filename);
            grammarUrl = file.toURL();
            List<RulesRecord> rrList = grammarMapper.fromXML(grammarUrl);
            fail("SaxMapperException expected when grammar file contains invalid Gender type!");
        } catch (MalformedURLException e) {
            fail("Failed to create url from: " + filename);
        } catch (SaxMapperException e) {
            //OK
        }
    }

    public void testInvalidContentsAction() throws Exception {
        URL grammarUrl = null;
        String filename = "test/grammarfiles/invalidAction.xml";
        try {
            File file = new File(filename);
            grammarUrl = file.toURL();
            List<RulesRecord> rrList = grammarMapper.fromXML(grammarUrl);
            fail("SaxMapperException expected when grammar file contains invalid Action type!");
        } catch (MalformedURLException e) {
            fail("Failed to create url from: " + filename);
        } catch (SaxMapperException e) {
            //OK
        }
    }

    public void testInvalidContentsDivisor() throws Exception {
        URL grammarUrl = null;
        String filename = "test/grammarfiles/invalidDivisor.xml";
        try {
            File file = new File(filename);
            grammarUrl = file.toURL();
            List<RulesRecord> rrList = grammarMapper.fromXML(grammarUrl);
            fail("SaxMapperException expected when grammar file contains invalid Action type!");
        } catch (MalformedURLException e) {
            fail("Failed to create url from: " + filename);
        } catch (SaxMapperException e) {
            //OK
        }
    }

}
