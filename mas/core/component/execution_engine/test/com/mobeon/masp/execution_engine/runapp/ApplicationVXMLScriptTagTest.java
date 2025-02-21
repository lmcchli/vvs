package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.util.TestEvent;
import junit.framework.Test;

import java.util.List;

/**
 * This test suite contains script tag tests for voicexml.
 */
public class ApplicationVXMLScriptTagTest extends ApplicationBasicTestCase<ApplicationVXMLScriptTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("script");
        testCases(
                testCase("script_1"),
                testCase("script_2"),
                testCase("script_3"),
                testCase("script_4"),
                testCase("script_5"),
                testCase("script_6"),
                testCase("script_7"),
                testCase("script_8"),
                testCase("script_9"),
                testCase("script_10")
        );
        store(ApplicationVXMLScriptTagTest.class);

    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLScriptTagTest(String event) {
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
        return genericSuite(ApplicationVXMLScriptTagTest.class);
    }


    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLScriptTagTest.class);
    }

    /**
     * Tests script inside a script tag by declaring a set of variables, setting some values
     * on them and verify their existance and value outside the script tag. See script_1.ccxml.
     *
     * @throws Exception
     */
    public void testVXMLScriptTag1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sScript\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sScript\\sok\\s2.*");
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
    public void testVXMLScriptTag2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_2", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sScript\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sScript\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * This test defines a function and use it and check its output.
     *
     * @throws Exception
     */
    public void testVXMLScriptTag3() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_3", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sScript\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test src attribute of the script in form scope.
     *
     * @throws Exception
     */
    public void testVXMLScriptTag4() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_4", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sScript\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test src attribute of the script tag in global scope.
     *
     * @throws Exception
     */
    public void testVXMLScriptTag5() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_5", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sScript\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * verify that evaluating weird ecma logs an error.
     * this TC may fail in the future if someone changes
     * the EE code doing the log :(
     *
     * @throws Exception
     */
    public void testVXMLScriptTag6() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_6", 20000);
        if (!exited) {
            fail("The application timed out!");
        }
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(TestEvent.SCOPE_COMPILATION_FAILED);

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }


    /**
     * Verify that executing a script containing an undefined variable throws
     * error.semantic
     *
     * @throws Exception
     */
    public void testVXMLScriptTag7() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_7", 20000);
        if (!exited) {
            fail("The application timed out!");
        }
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(TestEvent.SCOPE_EVALUATION_FAILED);

        // Check that also _message got a nice value
        lfe.add2LevelRequired(".*\\spass.*Evaluation.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that <script> in a <form> is evaluated.
     *
     * @throws Exception
     */
    public void testVXMLScriptTag8() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_8", 20000);
        if (!exited) {
            fail("The application timed out!");
        }
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\spass*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that <script> in a <form> is evaluated.
     *
     * @throws Exception
     */
    public void testVXMLScriptTag9() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_9", 20000);
        if (!exited) {
            fail("The application timed out!");
        }
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sThe value is 1*");
        lfe.add2LevelRequired(".*\\sThe value is 4*");
        lfe.add2LevelRequired(".*\\sThe value is 8*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that it is possible to use &lt operator (TR 27511)
     *
     * @throws Exception
     */
    public void testVXMLScriptTag10() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("script_10", 20000);
        if (!exited) {
            fail("The application timed out!");
        }
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS VXML: Script ok 1.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}