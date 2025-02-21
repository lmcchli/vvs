package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * This class executes the test sequences for the CCXML log tag.
 */
public class ApplicationCCXMLIfTagTest extends ApplicationBasicTestCase<ApplicationCCXMLIfTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("ccxml");
        testSubdir("if");
        testCases(
                testCase("if_1"),
                testCase("if_2"),
                testCase("if_3"),
                testCase("if_4"),
                testCase("if_5")
        );
        store(ApplicationCCXMLIfTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationCCXMLIfTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLIfTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLIfTagTest.class);
    }

    /**
     * Tests if in ccxml regarding evaluation of a two if statements with a true and a
     * false value of the cond attribute. See if_1.ccxml.
     *
     * @throws Exception
     */
    public void testCCXMLIfTag1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("if_1", 15000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sBefore.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sIf\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sAfter.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests the if statement in ccxml regarding handling of if/else constructs for both
     * a true clause and a false clause. See if_2.ccxml.
     *
     * @throws Exception
     */
    public void testCCXMLIfTag2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("if_2", 15000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sBefore.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sIf\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sIf\\sok\\s2.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sAfter.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests the if tag in ccxml regarding a combination of if/else/else clauses for different
     * conditions. See if_3.ccxml
     *
     * @throws Exception
     */
    public void testCCXMLIfTag3() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("if_3", 15000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sBefore.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sIf\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sIf\\sok\\s2.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sAfter.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests the usage of elseif in different combinations with else for different
     * conditions. See if_4.ccxml.
     *
     * @throws Exception
     */
    public void testCCXMLIfTag4() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("if_4", 5000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sBefore.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sIf\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sIf\\sok\\s3.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sIf\\sok\\s4.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sAfter.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test a combination of if-else. Trying to recreate
     * a problem in the lab where some code if an if-else
     * was not executed.
     *
     * @throws Exception
     */
    public void testCCXMLIfTag5() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("if_5", 5000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*\\slanguage is10.*");
        lfe.add2LevelRequired(".*\\stryDefaultLanguage istrue.*");
        lfe.add2LevelRequired(".*\\svariantVoice is20.*");
        lfe.add2LevelRequired(".*\\svariantVideo is30.*");
        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}