package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Feb 8, 2006
 * Time: 1:06:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLAssignTagTest extends ApplicationBasicTestCase<ApplicationVXMLAssignTagTest> {

    static {
        testLanguage("vxml");
        testSubdir("assign");
        testCases(
                testCase("assign_1")
        );
        store(ApplicationVXMLAssignTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLAssignTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLAssignTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLAssignTagTest.class);
    }

    /**
     * Verify that trying to assign to a non-existing variable results in
     * error.semantic.
     *
     * @throws Exception
     */
    public void testVXMLAssignTag1() throws Exception {
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("assign_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*error.semantic.*");
        lfe.add2LevelRequired(".*\\spass*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
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
