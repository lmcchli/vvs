package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * Runs testcases on the script tag
 */
public class ApplicationCCXMLScriptTagTest extends ApplicationBasicTestCase<ApplicationCCXMLScriptTagTest> {

    /**
     * The list of all testcases that we need to execute
     */

    static {
        testLanguage("ccxml");
        testSubdir("script");
        testCases(
                testCase("script_1"),
                testCase("script_2"),
                testCase("script_3"),
                testCase("script_4"),
                testCase("script_5"));
        store(ApplicationCCXMLScriptTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationCCXMLScriptTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLScriptTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLScriptTagTest.class);
    }

    /**
     * Tests script inside a script tag by declaring a set of variables, setting some values
     * on them and verify their existance and value outside the script tag. See script_1.ccxml.
     *
     * @throws Exception
     */
    public void testCCXMLScriptTag1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sScript\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sScript\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests script tag in the same manner as test case 1, but using CCDATA instead.
     *
     * @throws Exception
     */
    public void testCCXMLScriptTag2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_2", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sScript\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sScript\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * This test defines a function in ccxml scope and uses it in transition scope
     * and checks its output.
     *
     * @throws Exception
     */
    public void testCCXMLScriptTag3() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_3", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sScript\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests a script with an src attribute in ccxml scope and use the function declared in
     * that script in transition scope.
     *
     * @throws Exception
     */
    public void testCCXMLScriptTag4() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_4", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sScript\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests a script with a src attribute in transition state that defines a function which
     * is later used in the same transition scope.
     *
     * @throws Exception
     */
    public void testCCXMLScriptTag5() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_5", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sScript\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }
}
