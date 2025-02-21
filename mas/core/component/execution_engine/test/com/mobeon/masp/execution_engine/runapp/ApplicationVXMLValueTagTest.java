package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Feb 7, 2006
 * Time: 10:17:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLValueTagTest extends ApplicationBasicTestCase<ApplicationVXMLValueTagTest> {
    static {
        testLanguage("vxml");
        testSubdir("value");
        testCases(
                testCase("value_1"),
                testCase("value_2"),
                testCase("value_3")
        );
        store(ApplicationVXMLValueTagTest.class);
    }

    public ApplicationVXMLValueTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite
     *
     * @return the new testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLValueTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLValueTagTest.class);
    }

    /**
     * Verify that <value> can be used in <log>.
     * The TC is stolen from the conformance test 377
     *
     * @throws Exception
     */
    public void testVXMLValueTag1() throws Exception {

        // Setup the call
        String testCase = "value_1";
        setUpAndRunTest(testCase);
    }

    /**
     * Another TC where we log the value of a variable using <value>
     *
     * @throws Exception
     */
    public void testVXMLValueTag2() throws Exception {

        // Setup the call
        String testCase = "value_2";
        setUpAndRunTest(testCase);
    }

    /**
     * Another TC where we log the value of a variable using <value>
     *
     * @throws Exception
     */
    public void testVXMLValueTag3() throws Exception {

        // Setup the call
        String testCase = "value_3";
        boolean exited = createCallAndWaitForCompletion(testCase, 5000);
        if (!exited) {
            fail("The application timed out!");
        }

        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS: TESTING.*");
        lfe.add2LevelRequired(".*\\sTCPASS: 12.*");
        lfe.add2LevelRequired(".*\\sTCPASS: 13.13.*");
        lfe.add2LevelRequired(".*\\sTCPASS: false.*");
        lfe.add2LevelRequired(".*\\sTCPASS: TESTING.*");
        lfe.add2LevelRequired(".*\\sTCPASS: 7.*");
        lfe.add2LevelRequired(".*\\sTCPASS: 7.*");
        lfe.add2LevelRequired(".*\\sTCPASS: true");
        lfe.add2LevelRequired(".*\\sTCPASS: TESTING.*");


        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    private void setUpAndRunTest(String testCase) {
        boolean exited = createCallAndWaitForCompletion(testCase, 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        setupCommonVerification();
    }

    private void setupCommonVerification() {
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\spass.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }
}
