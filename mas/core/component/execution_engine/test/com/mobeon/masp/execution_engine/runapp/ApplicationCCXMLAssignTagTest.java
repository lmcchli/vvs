package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * This class executes the test sequences for the CCXML log tag.
 */
public class ApplicationCCXMLAssignTagTest extends ApplicationBasicTestCase<ApplicationCCXMLAssignTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("ccxml");
        testSubdir("assign");
        testCases(
                testCase("assign_1"),
                testCase("assign_2"),
                testCase("assign_3"),
                testCase("assign_4")
        );
        store(ApplicationCCXMLAssignTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationCCXMLAssignTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLAssignTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLAssignTagTest.class);
    }

    /**
     * Tests the assign tag in ccxml for error.semantic when assigning an undefined variable,
     * see assign_1.ccxml.
     *
     * @throws Exception
     */
    public void testCCXMLAssignTag1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("assign_1", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.addIgnored(".*error\\.semantic.*");
        lfe.failOnUndefinedErrors();


        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sAssign\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests the assign in ccxml for assignmnt of variables in different scope, both in
     * transition and ccxml scope.
     *
     * @throws Exception
     */
    public void testCCXMLAssignTag2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("assign_2", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sAssign\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sAssign\\sok\\s2.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sAssign\\sok\\s3.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sAssign\\sok\\s4.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests the assign in ccxml for assignment of undefined variables giving the scope qualifier,
     * such as ccxml.test1 and transition.test2.
     *
     * @throws Exception
     */
    public void testCCXMLAssignTag3() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("assign_3", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sAssign\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sAssign\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests variables are undefined when not declared using var or assign.
     *
     * @throws Exception
     */
    public void testCCXMLAssignTag4() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("assign_4", 5000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sScope\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sScope\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}
