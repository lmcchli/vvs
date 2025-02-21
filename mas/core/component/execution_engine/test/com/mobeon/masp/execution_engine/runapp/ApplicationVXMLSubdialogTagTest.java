package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.util.test.MASTestSwitches;
import junit.framework.Test;

import java.util.List;

/**
 * Test suite for the subdialog tag.
 */
public class ApplicationVXMLSubdialogTagTest extends ApplicationBasicTestCase<ApplicationVXMLSubdialogTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("subdialog");
        testCases(
                testCase("subdialog_1"),
                testCase("subdialog_2"),
                testCase("subdialog_3"),
                testCase("subdialog_4"),
                testCase("subdialog_5"),
                testCase("subdialog_6"),
                testCase("subdialog_7"),
                testCase("subdialog_8"),
                testCase("subdialog_9"),
                testCase("subdialog_10"),
                testCase("subdialog_10"),
                testCase("subdialog_11"),
                testCase("subdialog_12"),
                testCase("subdialog_13")
        );
        store(ApplicationVXMLSubdialogTagTest.class);
    }


    /**
     * Creates this test case
     */
    public ApplicationVXMLSubdialogTagTest(String event) {
        // Use only info-logging since running all test cases with debug log takes too much memory...
        super(event, "test_log_info.xml");
        log.info("MOCK: Setting up " + event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLSubdialogTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLSubdialogTagTest.class);
    }

    /**
     * Tests a subdialog between two forms in the same document.
     *
     * @throws Exception
     */
    public void testVXMLSubdialog1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subdialog_1");
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*Subaru subdialog got started.*");
        lfe.add2LevelRequired(".*Subdialog:s filled got called.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }


    /**
     * Tests a subdialog to different document.
     *
     * @throws Exception
     */
    public void testVXMLSubdialog2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subdialog_2");
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_b-001.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_a-001.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_a-002.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\ssubdialog-001.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }

    /**
     * Tests a subdialog to different document with param.
     *
     * @throws Exception
     */
    public void testVXMLSubdialog3() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subdialog_3", 10000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_b-001.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sp_test=12.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_a-001.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_a-002.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\ssubdialog-001.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }

    /**
     * Tests a subdialog to non-existant document.
     *
     * @throws Exception
     */
    public void testVXMLSubdialog4() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subdialog_4", 10000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_a-003.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }

    public void testVXMLSubdialog5() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subdialog_5", 10000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\salpha\\(5\\)-001=25.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_b-001.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\salpha\\(5\\)-002=25.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_a-001.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_a-002.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }

    public void testVXMLSubdialog6() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subdialog_6", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_c-001.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_b-001.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_b-002.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_a-001.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_a-002.*");

        lfe.add2LevelRequired(".*MOCK: Entering OutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.Thread PlayFinishedEvent.*PLAY_FINISHED.*");
        lfe.add2LevelRequired(".*MOCK: Entering OutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.Thread PlayFinishedEvent.*PLAY_FINISHED.*");

        lfe.add2LevelRequired(".*\\sTCPASS CCXML: subdialog-001.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }


    /**
     * test that it is possible to throw an event after
     * calling a subdialog
     *
     * @throws Exception
     */
    public void testVXMLSubdialog7() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subdialog_7", 10000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog-001.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }

    /**
     * Verify event handling and throws from native java code
     * calling a subdialog
     *
     * @throws Exception
     */
    public void testVXMLSubdialog8() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subdialog_8", 10000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\ssubdialog_a-005.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\smain-001.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }

    /**
     * Verify event handling and throws from native java code
     * using subdialog and goto
     *
     * @throws Exception
     */
    public void testVXMLSubdialog9() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subdialog_9", 10000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS VXML: subdialog_a-001.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML: subdialog_b-001.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML: subdialog_main-001.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }

    /**
     * Verify return with event param
     */
    public void testVXMLSubdialog10() throws Exception {
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subdialog_10", 5000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS VXML: subdialog_a-001.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }

    /**
     * Verify return with event connection.disconnect.hangup
     * (TR 27244).
     * <p/>
     * This test case also tests that hanging up while playing in
     * a subdialog works fine (we used to get an error like
     * "No handler found for event play.finished" here)
     */
    public void testVXMLSubdialog11() throws Exception {
        // Setup the call
        TestAppender.clear();
        final InboundCallMock icm = createCall("subdialog_11");
        icm.startCall();
        // Wait for the call to be loaded/compiled
        icm.sleep(2000);
        new Thread() {
            public void run() {
                try {
                    // Wait a few ms for the play to start
                    icm.sleep(2000);
                    icm.disconnect();
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();

        icm.waitForPlay(11000);


        boolean exited = icm.waitForExecutionToFinish(10000);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.addIgnored(".*CCXMLEventProcessor.*No handler found for event.*play\\.finished.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*No handler found for event.*play\\.finished.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");

        //No handler found for event
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }

    /**
     * Hangup the call while a subdialog with only <block>s is executing. return from the subdialog.
     * The hangup event shall be handled as soon as the invoking context reaches waiting state.
     *
     * @throws Exception
     */
    public void testVXMLSubdialog12() throws Exception {
        // Setup the call
        TestAppender.clear();
        final InboundCallMock icm = createCall("subdialog_12");
        icm.startCall();
        // Wait for the call to be loaded/compiled
        icm.sleep(4000);
        icm.disconnectCall();

        boolean exited = icm.waitForExecutionToFinish(10000);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS1.*");
        lfe.add2LevelRequired(".*\\sTCPASS2.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        //No handler found for event
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }

    /**
     * Hangup the call before entering a subdialog. The subdialog contains a <record> tag and the hangup
     * event shall be triggered as soon as the interpreter enters the waiting state.
     *
     * @throws Exception
     */
    public void testVXMLSubdialog13() throws Exception {
        // Setup the call
        TestAppender.clear();
        final InboundCallMock icm = createCall("subdialog_13");
        icm.startCall();
        // Wait for the call to be loaded/compiled
        icm.sleep(4000);
        icm.disconnectCall();

        boolean exited = icm.waitForExecutionToFinish(10000);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        //No handler found for event
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }


}
