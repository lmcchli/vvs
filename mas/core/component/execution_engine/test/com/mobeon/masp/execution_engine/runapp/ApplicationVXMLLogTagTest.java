package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;


/**
 * This class contains testcases for the log tag for voice xml.
 *
 * @author Tomas Stenlund
 */
public class ApplicationVXMLLogTagTest extends ApplicationBasicTestCase<ApplicationVXMLLogTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("log");
        testCases(
                testCase("log_1"),
                testCase("log_2"),
                testCase("log_3"),
                testCase("log_4"),
                testCase("log_5"),
                testCase("log_6"),
                testCase("log_7"),
                testCase("log_8"),
                testCase("log_9")

        );
        store(ApplicationVXMLLogTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLLogTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLLogTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLLogTagTest.class);
    }

    /**
     * A test of the log tag with a string between the opening and closing log tag
     * that is supposed to write the log text to the log file. See log_vxml_1.vxml for
     * more details.
     *
     * @throws Exception
     */
    public void testVXMLLogTag1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("log_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sLog\\s1.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * A test of the log tag using the expr parameter that is supposed to write the expressions
     * to the log file, see log_vxml_2.vxml for more details.
     *
     * @throws Exception
     */
    public void testVXMLLogTag2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("log_2", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sLog\\s2A.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sLog\\s2B.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * A test of the log tag using the expr parameter that is supposed to write the expressions
     * to the log file, see log_vxml_3.vxml for more details.
     *
     * @throws Exception
     */
    public void testVXMLLogTag3() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("log_3", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sLog\\s3 Added.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * A test of the log tag using the expr parameter that is supposed to write the expressions
     * to the log file, see log_vxml_4.vxml for more details.
     *
     * @throws Exception
     */
    public void testVXMLLogTag4() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("log_4", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sLog\\s4\\sAdded.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * A test of the log tag using the expr parameter that is supposed to write the expressions
     * to the log file, see log_vxml_5.vxml for more details.
     *
     * @throws Exception
     */
    public void testVXMLLogTag5() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("log_5", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sLog\\s5.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * A test of the log tag using the expr parameter that is supposed to write the expressions
     * to the log file, see log_vxml_6.vxml for more details.
     *
     * @throws Exception
     */
    public void testVXMLLogTag6() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("log_6", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sLog\\s6\\sAdded.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * A test of the log tag using the expr parameter that is supposed to write the expressions
     * to the log file, see log_vxml_7.vxml for more details.
     *
     * @throws Exception
     */
    public void testVXMLLogTag7() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("log_7", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sLog\\s7\\sAdded\\sAdded.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * A test of the log tag sending the log entry to the severity level specified by the level attribute,
     * see log_vxml_8.vxml for more details.
     *
     * @throws Exception
     */
    public void testVXMLLogTag8() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("log_8", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".* WARN .*\\sTCPASS\\sVXML:\\sLog\\s8.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that it is possible to log using the "expr"
     * attribute of the log tag.
     *
     * @throws Exception
     */
    public void testVXMLLogTag9() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("log_9", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".* WARN .*\\sexpression TCPASS VXML: Log 9*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}
