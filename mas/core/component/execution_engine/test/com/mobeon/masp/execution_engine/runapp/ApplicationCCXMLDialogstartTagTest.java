package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.util.test.MASTestSwitches;
import junit.framework.Test;

import java.util.List;

/**
 * Tets class for dialog start tag in ccxml.
 */
public class ApplicationCCXMLDialogstartTagTest extends ApplicationBasicTestCase<ApplicationCCXMLDialogstartTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("ccxml");
        testSubdir("dialogstart");
        testCases(
                testCase("dialogstart_1"),
                testCase("dialogstart_2"),
                testCase("dialogstart_3"),
                testCase("dialogstart_4"),
                testCase("dialogstart_5"),
                testCase("dialogstart_6"),
                testCase("dialogstart_7"),
                testCase("dialogstart_8"),
                testCase("dialogstart_9"));
        store(ApplicationCCXMLDialogstartTagTest.class);


    }
    /**
     * Creates this test case
     */
    public ApplicationCCXMLDialogstartTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLDialogstartTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLDialogstartTagTest.class);
    }

    /**
     * Test of a plain vanilla dialogstart, to see that the dialog can be started.
     *
     * @throws Exception
     */
    public void testCCXMLDialogstartTag1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("dialogstart_1", 5000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sDialogstart\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test of a plain vanilla dialogstart, to see that the correct events and
     * order of them is generated.
     *
     * @throws Exception
     */
    public void testCCXMLDialogstartTag2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("dialogstart_2", 5000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.ignoreLogElement();
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sdialog.started.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sdialog.exit.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests the dialogid parameter, that it gets set !
     *
     * @throws Exception
     */
    public void testCCXMLDialogstartTag3() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("dialogstart_3", 5000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sDialogstart\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests the dialogid parameter in the dlg event is correct.
     *
     * @throws Exception
     */
    public void testCCXMLDialogstartTag4() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("dialogstart_4", 5000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sDialogstart\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests that the error.dialog.notstarted event is thrown for a nonexistant dialog.
     *
     * @throws Exception
     */
    public void testCCXMLDialogstartTag5() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("dialogstart_5", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.addIgnored(".*error\\.dialog\\.notstarted.*");

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sDialogstart\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests that a started dialog can be terminated by a FAR_END hangup, through connection.disconnected
     * that throws a connection.disconnect.hanup to VoiceXML.
     *
     * @throws Exception
     */
    public void testCCXMLDialogstartTag6() throws Exception {

        final InboundCallMock icm = createCall("dialogstart_6");
        icm.startCall();
        icm.sleep(5000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.ignoreLogElement();
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sdialogstart\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sdialogstart\\sok\\s2.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sdialogstart\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sdialogstart\\sok\\s3.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that if do dialogstart within a transition, the
     * entire transition is run to completion before dialog.started is handled.
     * <p/>
     * CCXML specifies this as: Any events that arrive while an event is already
     * being processed are just placed on the queue for later.
     *
     * @throws Exception
     */
    public void testCCXMLDialogstartTag7() throws Exception {

        final InboundCallMock icm = createCall("dialogstart_7");
        icm.startCall();
        icm.sleep(5000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sdialogstart\\sok\\s1.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that it is possible to do <dialogstart> to start a leaf
     * document and that variables from the root context are visible.
     *
     * @throws Exception
     */
    public void testCCXMLDialogstartTag8() throws Exception {

        final InboundCallMock icm = createCall("dialogstart_8");
        icm.startCall();
        icm.sleep(5000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASSVXML.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test that we can not do dialogstart when there is no connection related with the current event.
     *
     * @throws Exception
     */
    public void testJoinInvalidConnection() throws Exception {

        // TODO this test case fails when all tests are run and it surely loks like a bug
        super.setTestCaseTimeout(10000);

        LogFileExaminer lfe = runSimpleTest("dialogstart_9");

        lfe.failOnUndefinedErrors();

        lfe.addIgnored(".*error\\.semantic.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");

        validateTest(lfe);
    }


}