package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * This class executes the test sequences for the CCXML log tag.
 */
public class ApplicationCCXMLLogTagTest extends ApplicationBasicTestCase<ApplicationCCXMLLogTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("ccxml");
        testSubdir("log");
        testCases(
                new ApplicationTestCase("log_1", "test:/test/com/mobeon/masp/execution_engine/runapp/applications/ccxml/log/log_1.xml"),
                new ApplicationTestCase("log_2", "test:/test/com/mobeon/masp/execution_engine/runapp/applications/ccxml/log/log_2.xml")
        );
        store(ApplicationCCXMLLogTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationCCXMLLogTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLLogTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLLogTagTest.class);
    }

    /**
     * A test of <log expr="'TCPASS CCXML: Log 1'"/> that is supposed to log the
     * text TCPASS CCXML: Log 1 to the log file when executed. See log_1.ccxml.
     *
     * @throws Exception
     */
    public void testCCXMLLogTag1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("log_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sLog\\s1.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * A test of <log label="'info'" expr="'TCPASS CCXML: Log 1'"/> that is supposed to log the
     * text TCPASS CCXML: Log 1 to the log file. See log_2.ccxml.
     *
     * @throws Exception
     */
    public void testCCXMLLogTag2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("log_2", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*INFO.*\\sTCPASS\\sCCXML:\\sLog\\s1.*");

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}
