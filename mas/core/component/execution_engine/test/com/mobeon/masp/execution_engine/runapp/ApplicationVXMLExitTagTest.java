package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Feb 14, 2006
 * Time: 9:10:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLExitTagTest extends ApplicationBasicTestCase<ApplicationVXMLExitTagTest> {
    static {
        testLanguage("vxml");
        testSubdir("exit");
        testCases(
                testCase("exit_1"),
                testCase("exit_2"),
                testCase("exit_3"),
                testCase("exit_4"),
                testCase("exit_5")
        );
        store(ApplicationVXMLExitTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLExitTagTest(String event) {
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
        return genericSuite(ApplicationVXMLExitTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLExitTagTest.class);
    }

    /**
     * Verify that it is possible to exit from a block
     *
     * @throws Exception
     */
    public void testVXMLExitTag1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("exit_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        lfe.add2LevelRequired(".*\\sexpectedString.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that it is possible to exit from a document-level catch
     *
     * @throws Exception
     */
    public void testVXMLExitTag2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("exit_2", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        lfe.add2LevelRequired(".*\\sexpectedString.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that it is possible to exit from a subdialog
     *
     * @throws Exception
     */
    public void testVXMLExitTag3() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("exit_3", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        lfe.add2LevelRequired(".*\\sexpectedString.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify this aspect of VXML spec: "Before the interpreter exits all queued prompts are played to completion."
     *
     * In this test case the exit is performed by explicit <exit/>
     * @throws Exception
     */
    public void testVXMLExitTag4() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("exit_4", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify this aspect of VXML spec: "Before the interpreter exits all queued prompts are played to completion."
     *
     * In this test case the exit is performed since no form item remains eligible to select.
     * @throws Exception
     */
    public void testVXMLExitTag5() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("exit_5", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}
