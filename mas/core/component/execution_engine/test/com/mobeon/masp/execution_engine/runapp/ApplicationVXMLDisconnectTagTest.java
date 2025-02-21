package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Feb 13, 2006
 * Time: 10:52:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLDisconnectTagTest extends ApplicationBasicTestCase<ApplicationVXMLDisconnectTagTest> {


    static {
        testLanguage("vxml");
        testSubdir("disconnect");
        testCases(
                testCase("disconnect_1"),
                testCase("disconnect_2")
        );
        store(ApplicationVXMLDisconnectTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLDisconnectTagTest(String event) {
        super(event);
        log.info("MOCK: Setting up " + event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLDisconnectTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLDisconnectTagTest.class);
    }

    /**
     * Test that it is possible to disconnect. Queued prompt shall played before the connection.disconnect.hangup event is processed.
     *
     * @throws Exception
     */
    public void testCCXMLDisconnectTag1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("disconnect_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        lfe.add2LevelRequired(".*INFO Playing a prompt.*");
        lfe.add2LevelRequired(".*INFO And one more.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that a local catch is selected for disconnect
     * over a document-level one.
     *
     * @throws Exception
     */
    public void testCCXMLDisconnectTag2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("disconnect_2", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}
