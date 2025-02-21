/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.grammar;

import org.apache.log4j.xml.DOMConfigurator;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

import com.mobeon.masp.mediacontentmanager.xml.GrammarMapper;
import com.mobeon.masp.mediacontentmanager.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.MultiThreadedTeztCase;

/**
 * JUnit test for the {@link GenericNumberBuilder} class.
 *
 * @author mmawi
 */
public class GenericNumberBuilderTest extends MultiThreadedTeztCase {
    /**
     * The logger used.
     */
    private static ILogger LOGGER =
            ILoggerFactory.getILogger(GenericNumberBuilderTest.class);


    /**
     * The filename of the English grammar file.
     */
    private static String grammarFileName =
            "applications/mediacontentpackages/en_audio_1/grammar.xml";
    /**
     * The filename of the German grammar file.
     */
    private static String grammarFileName2 =
            "test/grammarfiles/german_grammar.xml";

    /**
     * The <code>GrammarMapper</code> used by this tester.
     */
    private GrammarMapper grammarMapper;
    /**
     * The <code>IGenericNumberBuilder</code> used by this tester.
     */
    private static IGenericNumberBuilder numberBuilder = new GenericNumberBuilder();
    /**
     * A list of <code>RulesRecord</code>s mapped from the XML file.
     */
    private List<RulesRecord> rulesRecordList;

    /**
     * URL to a well-formed XML with valid content.
     */
    private URL englishGrammar = null;
    private URL germanGrammar = null;

    /**
     * Simple constructor.
     * @param s
     */
    public GenericNumberBuilderTest(String s) {
        super(s);
    }

    public void setUp() throws Exception {
        super.setUp();
        grammarMapper = new GrammarMapper();

        try {
            File file = new File(grammarFileName);
            englishGrammar = file.toURL();
        } catch (MalformedURLException e) {
            fail("Failed to create url from: "+ grammarFileName);
        }
        try {
            File file = new File(grammarFileName2);
            germanGrammar = file.toURL();
        } catch (MalformedURLException e) {
            fail("Failed to create url from: "+ grammarFileName2);
        }
    }

    // Cleans up after each test
    public void tearDown() throws Exception {
        super.tearDown();
        grammarMapper = null;
    }


    /**
     * Tests for the {@link GenericNumberBuilder#buildNumber(IRulesRecord, Long)}
     * method.
     * <p/>
     * <pre>
     * 1. Parse the English grammar.xml
     *  Condition:
     *      A URL to the English grammar.xml is created.
     *      The grammar.xml is well formed and correct.
     *
     *  Action:
     *      Parse the xml file using the grammar mapper.
     *
     *  Result:
     *      A non-null list of RulesRecords is returned.
     * <p/>
     * 2. Test decomposition of English Number, gender: Female
     * Condition:
     *      The grammar.xml has been parsed and a list of RulesRecords exist.
     *
     *  Action:
     *      2.1) Decompose 2085
     *      2.2) Decompose 0
     *      2.3) Decompose 101
     *      2.4) Decompose 405296
     *      2.5) Decompose 3
     *
     *  Result:
     *      2.1) 4 MessageElements are returned: 2, 1e3, 80, 5
     *      2.2) 1 MessageElement is returned: 0
     *      2.3) 3 MessageElements are returned: 1, 100, 1
     *      2.4) 8 MessageElements are returned: 4, 100, 5, 1e3, 2, 100, 90, 6
     *      2.5) 1 MessageElement is returned: 3
     * <p/>
     * 3. Test decomposition of English DateDM and CompleteDate, gender: None
     * Condition:
     *      The grammar.xml has been parsed and a list of RulesRecords exist.
     *
     *  Action:
     *      3.1) Decompose 230500 (May 23rd)
     *
     *  Result:
     *      3.1) 2 MessageElements are returned: may, dom23
     *
     *  Action:
     *      3.2) Decompose 2008230500 (May 23rd 2008)
     *
     *  Result:
     *      3.2) 2 MessageElements are returned: may, dom23, year2008
     * <p/>
     * 4. Test decomposition of English Time24, gender: None
     * Condition:
     *      The grammar.xml has been parsed and a list of RulesRecords exist.
     *
     *  Action:
     *      4.1) Decompose 153700 (15:37:00)
     *      4.2) Decompose 110000 (11:00:00)
     *      4.3) Decompose 230100 (23:01:00)
     *
     *  Result:
     *      4.1) 3 MessageElements are returned: 15, 30, 7
     *      4.2) 2 MessageElements are returned: 11, hour
     *      4.3) 4 MessageElements are returned: 20, 3, o, 1
     * <p/>
     * 5. Test decomposition of English Time12, gender: None
     * Condition:
     *      The grammar.xml has been parsed and a list of RulesRecords exist.
     *
     *  Action:
     *      5.1) Decompose 3377000 (3:37 pm)
     *      5.2) Decompose 9010000 (9:01 am)
     *
     *  Result:
     *      5.1) 4 MessageElements are returned: 3, 30, 7, pm
     *      5.2) 4 MessageElements are returned: 9, o, 1, am
     * <p/>
     * 6. Parse the German grammar.xml
     *  Condition:
     *      A URL to the German grammar.xml is created.
     *      The grammar.xml is well formed and correct.
     *
     *  Action:
     *      Parse the grammar file using the grammar mapper.
     *
     *  Result:
     *      A non-null list of RulesRecords is returned.
     * <p/>
     * 7. Test decomposition of German Number, gender: None
     * Condition:
     *      The grammar.xml has been parsed and a list of RulesRecords exist.
     *
     *  Action:
     *      7.1) Decompose 1
     *      7.2) Decompose 21
     *      7.3) Decompose 2085
     *
     *  Result:
     *      7.1) 1 MessageElement is returned: 1a
     *      7.2) 3 MessageElements are returned: 1, and, 20
     *      7.3) 5 MessageElements are returned: 2, 1e3, 5, and, 80
     * <p/>
     * 8. Test decomposition of German Time24, gender: None
     * Condition:
     *      The grammar.xml has been parsed and a list of RulesRecords exist.
     *
     *  Action:
     *      8.1) Decompose 210100 (21:01:00)
     *
     *  Result:
     *      8.1) 5 MessageElements are returned: 1, and, 20, oclock, 1a
     * <p/>
     * 9. Test decomposition of German DateDM and CompleteDate, gender: Female
     * Condition:
     *      The grammar.xml has been parsed and a list of RulesRecords exist.
     *
     *  Action:
     *      9.1) Decompose 101200 (10th Dec)
     *      9.2) Decompose 310100 (31st Jan)
     *      9.3) Decompose 2009101200 (10th Dec 2009)
     *      9.4) Decompose 2007310100 (31st Jan 2007)
     *
     *  Result:
     *      9.1) 2 MessageElements are returned: dom10, dec
     *      9.2) 2 MessageElements are returned: dom31, jan
     *      9.3) 3 MessageElements are returned: dom10, dec, year2009
     *      9.4) 3 MessageElements are returned: dom31, jan, year2007
     * </p>
     * 10. Test decomposition of special cases.
     * Condition:
     *      The grammar.xml has been parsed and a list of RulesRecords exist.
     *      Some special rules for Female, Number are used:
     *      Rule for 90 contains an invalid swap value, -4.
     *      Rule for 4 contains an extra "and" with a skip element.
     *      Rule for 2 contains a select action, 2 or 2a if it is last in the result.
     *
     *  Action:
     *      10.1) Decompose 99, with swap beyond beginning.
     *      10.2) Decompose 4, with skip element in front of.
     *      10.3) Decompose 2, with select.
     *      10.4) Decompose 200, with select for 2.
     *      10.5) Test null as input
     *
     *  Result:
     *      10.1) 3 MessageElements are returned: 9, and, 90
     *      10.2) 1 MessageElement is returned: 4
     *      10.3) 1 MessageElement is returned: 2a
     *      10.4) 2 MessageElemets are returned: 2, 100
     *      10.5) Null is returned.
     * </pre>
     */
    public void testBuildNumber() {
        // 1 Read file content.
        rulesRecordList = grammarMapper.fromXML(englishGrammar);
        assertNotNull(rulesRecordList);

        Long number;
        List<String> resultList;
        for (RulesRecord rule : rulesRecordList) {
            if (rule.compareRule(IMediaQualifier.Gender.Female, IMediaQualifier.QualiferType.Number)) {
                // 2.1 Decompose 2085
                number = (long) 2085;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 4", 4, resultList.size());
                assertEquals("First element is not 2", "2", resultList.get(0));
                assertEquals("Second element is not 1000", "1e3", resultList.get(1));
                assertEquals("Third element is not 80", "80", resultList.get(2));
                assertEquals("Fourth element is not 5", "5", resultList.get(3));

                // 2.2 Decompose 0
                number = (long) 0;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 1", 1, resultList.size());
                assertEquals("First element is not 0", "0", resultList.get(0));

                // 2.3 Decompose 101
                number = (long) 101;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 3", 3, resultList.size());
                assertEquals("First element is not 1", "1", resultList.get(0));
                assertEquals("Second element is not 100", "100", resultList.get(1));
                assertEquals("Third element is not 1", "1", resultList.get(2));

                // 2.4 Decompose 405296
                number = (long) 405296;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 8", 8, resultList.size());
                assertEquals("First element is not 4", "4", resultList.get(0));
                assertEquals("Second element is not 100", "100", resultList.get(1));
                assertEquals("Third element is not 5", "5", resultList.get(2));
                assertEquals("Fourth element is not 1000", "1e3", resultList.get(3));
                assertEquals("Fifth element is not 2", "2", resultList.get(4));
                assertEquals("Sixth element is not 100", "100", resultList.get(5));
                assertEquals("Seventh element is not 90", "90", resultList.get(6));
                assertEquals("Eigth element is not 6", "6", resultList.get(7));

                // 2.5 Decompose 3
                number = (long) 3;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 1", 1, resultList.size());
                assertEquals("First element is not 3", "3", resultList.get(0));
            }
            else if (rule.compareRule(IMediaQualifier.Gender.None, IMediaQualifier.QualiferType.DateDM)) {
                // 3.1 Decompose 2305 (May 23rd)
                number = (long) 230500;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 2", 2, resultList.size());
                assertEquals("First element is not may", "may", resultList.get(0));
                assertEquals("Second element is not 23rd", "dom23", resultList.get(1));
            }
            else if (rule.compareRule(IMediaQualifier.Gender.None, IMediaQualifier.QualiferType.CompleteDate)) {
                // 3.2 Decompose 2008-05-23 (May 23rd 2008)
                number = (long) 2008230500;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 3", 3, resultList.size());
                assertEquals("First element is not may", "may", resultList.get(0));
                assertEquals("Second element is not 23rd", "dom23", resultList.get(1));
                assertEquals("Third element is not 2008", "year2008", resultList.get(2));
            }
            else if (rule.compareRule(IMediaQualifier.Gender.None, IMediaQualifier.QualiferType.Time24)) {
                // 4.1 Decompose 153700 (15:37:00)
                number = (long) 153700;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 3", 3, resultList.size());
                assertEquals("First element is not 15", "15", resultList.get(0));
                assertEquals("Second element is not 30", "30", resultList.get(1));
                assertEquals("Third element is not 7", "7", resultList.get(2));

                // 4.2 Decompose 110000 (11:00:00)
                number = (long) 110000;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 2", 2, resultList.size());
                assertEquals("First element is not 11", "11", resultList.get(0));
                assertEquals("Second element is not hour", "hour", resultList.get(1));

                // 4.3 Decompose 230100 (23:01:00)
                number = (long) 230100;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 4", 4, resultList.size());
                assertEquals("First element is not 20", "20", resultList.get(0));
                assertEquals("Second element is not 3", "3", resultList.get(1));
                assertEquals("Third element is not o", "o", resultList.get(2));
                assertEquals("Fourth element is not 1", "1", resultList.get(3));
            }
            else if (rule.compareRule(IMediaQualifier.Gender.None, IMediaQualifier.QualiferType.Time12)) {
                // 5.1 Decompose 3377000 (3:37 pm)
                number = (long) 3377000;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 4", 4, resultList.size());
                assertEquals("First element is not 3", "3", resultList.get(0));
                assertEquals("Second element is not 30", "30", resultList.get(1));
                assertEquals("Third element is not 7", "7", resultList.get(2));
                assertEquals("Fourth element is not pm", "pm", resultList.get(3));

                // 5.2 Decompose 9010000 (9:01 am)
                number = (long) 9010000;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 4", 4, resultList.size());
                assertEquals("First element is not 9", "9", resultList.get(0));
                assertEquals("Second element is not o", "o", resultList.get(1));
                assertEquals("Third element is not 1", "1", resultList.get(2));
                assertEquals("Fourth element is not am", "am", resultList.get(3));
            }

        }

        // 6 Read file content.
        rulesRecordList = grammarMapper.fromXML(germanGrammar);
        assertNotNull(rulesRecordList);

        for (RulesRecord rule : rulesRecordList) {
            if (rule.compareRule(IMediaQualifier.Gender.None, IMediaQualifier.QualiferType.Number)) {
                // 7.1 Decompose 1
                number = (long) 1;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 1", 1, resultList.size());
                assertEquals("First element is not 1a", "1a", resultList.get(0));

                // 7.2 Decompose 21
                number = (long) 21;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 3", 3, resultList.size());
                assertEquals("First element is not 1", "1", resultList.get(0));
                assertEquals("Second element is not and", "and", resultList.get(1));
                assertEquals("Third element is not 20", "20", resultList.get(2));

                // 7.3 Decompose 2085
                number = (long) 2085;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 5", 5, resultList.size());
                assertEquals("First element is not 2", "2", resultList.get(0));
                assertEquals("Second element is not 1000", "1e3", resultList.get(1));
                assertEquals("Third element is not 5", "5", resultList.get(2));
                assertEquals("Fourth element is not and", "and", resultList.get(3));
                assertEquals("Fourth element is not 80", "80", resultList.get(4));
            }
            else if (rule.compareRule(IMediaQualifier.Gender.None, IMediaQualifier.QualiferType.Time24)) {
                // 8.1 Decompose 210100 (21:01:00)
                number = (long) 210100;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 5", 5, resultList.size());
                assertEquals("First element is not 1", "1", resultList.get(0));
                assertEquals("Second element is not and", "and", resultList.get(1));
                assertEquals("Third element is not 20", "20", resultList.get(2));
                assertEquals("Fourth element is not oclock", "oclock", resultList.get(3));
                assertEquals("Fifth element is not 1a", "1a", resultList.get(4));
            }
            else if (rule.compareRule(IMediaQualifier.Gender.Female, IMediaQualifier.QualiferType.DateDM)) {
                // 9.1 Decompose 10/12 (10th Dec)
                number = (long) 101200;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 2", 2, resultList.size());
                assertEquals("First element is not dom10", "dom10", resultList.get(0));
                assertEquals("Second element is not dec", "dec", resultList.get(1));

                // 9.2 Decompose 31/01 (31st Jan)
                number = (long) 310100;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 2", 2, resultList.size());
                assertEquals("First element is not dom31", "dom31", resultList.get(0));
                assertEquals("Second element is not jan", "jan", resultList.get(1));
            }
            else if (rule.compareRule(IMediaQualifier.Gender.Female, IMediaQualifier.QualiferType.CompleteDate)) {
                // 9.3 Decompose 10/12/2009 (10th Dec 2009)
                number = (long) 2009101200;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 3", 3, resultList.size());
                assertEquals("First element is not dom10", "dom10", resultList.get(0));
                assertEquals("Second element is not dec", "dec", resultList.get(1));
                assertEquals("Third element is not year2009", "year2009", resultList.get(2));

                // 9.4 Decompose 31/01 (31st Jan 2007)
                number = (long) 2007310100;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 3", 3, resultList.size());
                assertEquals("First element is not dom31", "dom31", resultList.get(0));
                assertEquals("Second element is not jan", "jan", resultList.get(1));
                assertEquals("Third element is not year2007", "year2007", resultList.get(2));
            }
            else if (rule.compareRule(IMediaQualifier.Gender.Female, IMediaQualifier.QualiferType.Number)) {
                // 10.1 Decompose 99, with swap beyond beginning
                number = (long) 99;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 3", 3, resultList.size());
                assertEquals("First element is not 9", "9", resultList.get(0));
                assertEquals("Second element is not and", "and", resultList.get(1));
                assertEquals("Third element is not 90", "90", resultList.get(2));

                // 10.2 Decompose 4, with a skip
                number = (long) 4;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 1", 1, resultList.size());
                assertEquals("First element is not 4", "4", resultList.get(0));

                // 10.3 Decompose 2
                number = (long) 2;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 1", 1, resultList.size());
                assertEquals("First element is not 2a", "2a", resultList.get(0));

                // 10.4 Decompose 200
                number = (long) 200;
                resultList = numberBuilder.buildNumber(rule, number);
                assertEquals("Size of resultList is not 2", 2, resultList.size());
                assertEquals("First element is not 2", "2", resultList.get(0));
                assertEquals("Second element is not 100", "100", resultList.get(1));

                // 10.5 Test empty number
                resultList = numberBuilder.buildNumber(rule, null);
                assertNull("Result should be null for empty number!", resultList);
            }
        }
    }

    public void testEmptyInput() throws Exception {
        IRulesRecord rulesRecord = new RulesRecord(IMediaQualifier.QualiferType.Number);
        List resultList;
        resultList = numberBuilder.buildNumber(rulesRecord, (long)1234);

        assertNull("Result should be null for an empty rules record!", resultList);
    }


    /**
     * Tests the thread-safety of {@link GenericNumberBuilder}
     * <p/>
     * <pre>
     * Test if several clients with different rules can use the
     * same GenericNumberBuilder.
     * Condition:
     *      English and German grammar xml files has been set up.
     *
     * Action:
     *      Create 9 clients. Clients 0, 2, 4, 6 and 8 uses English
     *      rules and clients 1, 3, 5 and 7 uses German rules. Each client
     *      continously decomposes a number that is 21 + client id, i.e.
     *      client 0 decomposes 21, client 1 decomposes 22, etc. The
     *      clients run for 2 seconds.
     *
     * Result:
     *      Client 0 will always get the result "20", "1"
     *      Client 1 will always get the result "2", "and", "20"
     *      Client 2 will always get the result "20", "3"
     *      Client 3 will always get the result "4", "and", "20"
     *      Client 4 will always get the result "20", "5"
     *      Client 5 will always get the result "6", "and", "20"
     *      Client 6 will always get the result "20", "7"
     *      Client 7 will always get the result "8", "and", "20"
     *      Client 8 will always get the result "20", "9"
     * </pre>
     */
    public void testConcurrent() throws Exception {
        final GenericNumberBuilderClient[] clients = new GenericNumberBuilderClient[9];

        rulesRecordList = grammarMapper.fromXML(englishGrammar);
        assertNotNull("No English rules records found.", rulesRecordList);

        RulesRecord numberRuleEn = null;
        for (RulesRecord rule : rulesRecordList) {
            if (rule.compareRule(IMediaQualifier.Gender.None, IMediaQualifier.QualiferType.Number)) {
                numberRuleEn = rule;
            }
        }

        List<RulesRecord> germanRulesRecordList = grammarMapper.fromXML(germanGrammar);
        assertNotNull("No German rules records found.", germanRulesRecordList);

        RulesRecord numberRuleDe = null;
        for (RulesRecord rule : germanRulesRecordList) {
            if (rule.compareRule(IMediaQualifier.Gender.None, IMediaQualifier.QualiferType.Number)) {
                numberRuleDe = rule;
            }
        }

        for (int i = 0; i < clients.length; i++) {
            // Every second client should use English grammar...
            if (i % 2 == 0) {
                clients[i] = new GenericNumberBuilderClient(numberBuilder, numberRuleEn, i);
            }
            // ...and German grammar.
            else {
                clients[i] = new GenericNumberBuilderClient(numberBuilder, numberRuleDe, i);
            }
        }

        //Start the client threads
        runTestCaseRunnables(clients);

        // Let the clients run for two seconds
        new Timer().schedule(new TimerTask() {
            public void run() {
                for (GenericNumberBuilderClient client : clients) {
                    client.setDone();
                }
            }
        }, 2000);

        //Join the threads
        joinTestCaseRunnables(clients);
    }

    /**
     * Since AssertionFailedErrors thrown in a thread are not returned to JUnit,
     * we use the MultiThreadedTeztCase solution. Each client implements a
     * {@link TestCaseRunnable}, which holds the result of the execution. If an
     * exception is thrown within the thread, it uses its own exception handler
     * to save the result.
     */
    private class GenericNumberBuilderClient extends TestCaseRunnable {

        private IGenericNumberBuilder genericNumberBuilder;
        private RulesRecord rule;
        private int clientNumber;
        private boolean done = false;

        /**
         * Create a new <code>GenericNumberBuilderClient</code>.
         * @param numberBuilder The <code>GenericNumberBuilder</code> this
         *                      client should use.
         * @param rule          The <code>RulesRecord</code> that contains
         *                      the number rules this client should use to
         *                      decompose numbers.
         * @param clientNumber  The id number of this client.
         */
        public GenericNumberBuilderClient(IGenericNumberBuilder numberBuilder,
                                          RulesRecord rule, int clientNumber) {
            this.genericNumberBuilder = numberBuilder;
            this.rule = rule;
            this.clientNumber = clientNumber;
        }

        /**
         * Make the client stop.
         */
        public void setDone() {
            done = true;
        }

        /**
         * The actual test case that is performed.
         * If the used <code>RulesRecord</code> is English, the number should be
         * "20", "X", where X is the client number (1 - 9).
         * If the rule is German, the number should be "X", "and", "20".
         */
        public void runTestCase() {
            while (!done) {
                Long number = (long) clientNumber + 21;
                // Check if English
                if (clientNumber % 2 == 0) {
                    String lastDigit = String.valueOf(clientNumber + 1);
                    List<String> resultList = genericNumberBuilder.buildNumber(rule, number);

                    assertEquals("Client " + clientNumber + " failed, resultList size is not 2",
                            2, resultList.size());
                    assertEquals("Client " + clientNumber + " failed, first element is not 20",
                            "20", resultList.get(0));
                    assertEquals("Client " + clientNumber + " failed, second element is not " + lastDigit,
                            lastDigit + "", resultList.get(1));
                }
                // German
                else {
                    String firstDigit = String.valueOf(clientNumber + 1);
                    List<String> resultList = genericNumberBuilder.buildNumber(rule, number);

                    assertEquals("Client " + clientNumber + " failed, resultList size is not 3",
                            3, resultList.size());
                    assertEquals("Client " + clientNumber + " failed, first element is not " + firstDigit,
                            firstDigit + "", resultList.get(0));
                    assertEquals("Client " + clientNumber + " failed, second element is not and",
                            "and", resultList.get(1));
                    assertEquals("Client " + clientNumber + " failed, third element is not 20",
                            "20", resultList.get(2));
                }
            }
        }
    }
}
