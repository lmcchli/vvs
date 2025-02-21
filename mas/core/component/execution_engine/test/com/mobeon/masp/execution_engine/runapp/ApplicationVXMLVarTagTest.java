package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Feb 10, 2006
 * Time: 9:38:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLVarTagTest extends ApplicationBasicTestCase<ApplicationVXMLVarTagTest> {

    static {
        testLanguage("vxml");
        testSubdir("var");
        testCases(

                testCase("var_1"),
                testCase("var_2"),
                testCase("var_3"),
                testCase("var_4")
        );
        store(ApplicationVXMLVarTagTest.class);


    }

    public ApplicationVXMLVarTagTest(String event) {
        super(event, "test_log_info.xml");
    }

    /**
     * Defines the test suite
     *
     * @return the new testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLVarTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLVarTagTest.class);
    }

    /**
     * Verify that it is possible to declare a <var> without expression,
     * and then it will be undefined.
     *
     * @throws Exception
     */
    public void testVXMLVarTag1() throws Exception {

        // Setup the call
        String testCase = "var_1";
        setUpAndRunTest(testCase);
    }

    /**
     * Veriify that it is possible to assign an integer to a variable.
     *
     * @throws Exception
     */
    public void testVXMLVarTag2() throws Exception {

        // Setup the call
        String testCase = "var_2";
        setUpAndRunTest(testCase);
    }

    /**
     * Verify that declaring a var setting "expr" to an undefined variable leads to semantic error
     *
     * @throws Exception
     */
    public void testVXMLVarTag3() throws Exception {

        // Setup the call
        String testCase = "var_3";
        boolean exited = createCallAndWaitForCompletion(testCase, 20000);
        if (!exited) {
            fail("The application timed out!");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.addIgnored(".*error\\.semantic.*");
        lfe.addIgnored(".*ReferenceError:.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that it is possible to declare a variable after a semantic error
     *
     * @throws Exception
     */
    public void testVXMLVarTag4() throws Exception {

        // Setup the call
        String testCase = "var_4";
        boolean exited = createCallAndWaitForCompletion(testCase, 20000);
        if (!exited) {
            fail("The application timed out!");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.addIgnored(".*error\\.semantic.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
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

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }
}
