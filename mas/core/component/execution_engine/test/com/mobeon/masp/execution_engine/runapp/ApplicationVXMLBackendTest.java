package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.ConfigurationGroupMock;
import junit.framework.Test;

import java.util.List;

/**
 * This suite tests the backend connection.
 *
 * @author Tomas Stenlund
 */
public class ApplicationVXMLBackendTest extends ApplicationBasicTestCase<ApplicationVXMLBackendTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("backend");
        testCases(
                testCase("folder_1"),
                testCase("folder_2"),
                testCase("subscriber_1"),
                testCase("subscriber_2"),
                testCase("subscriber_3"),
                testCase("configuration_1")
        );
        store(ApplicationVXMLBackendTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLBackendTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLBackendTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLBackendTest.class);
    }

    /**
     * This testcase tries to do folder manipulation through the backend
     * interface.
     *
     * @throws Exception
     */
    public void testVXMLBackendFolder1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("folder_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*MOCK:\\sProfileManagerMock\\.getProfile.*");
        lfe.add2LevelRequired(".*MOCK:\\sMailboxMock\\.MailboxMock.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\smas\\.subscriberGetMailbox\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * This testcase tries to do folder manipulation through the backend
     * interface.
     *
     * @throws Exception
     */
    public void testVXMLBackendFolder2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("folder_2", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*MOCK:\\sProfileManagerMock\\.getProfile.*");
        lfe.add2LevelRequired(".*MOCK:\\sMailboxMock\\.MailboxMock.*");
        lfe.add2LevelRequired(".*MOCK:\\sMailboxMock\\.addFolder\\sname\\sINBOX.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\smas\\.mailboxGetFolder\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * This testcase tests various functions with subscriber manipulation.
     *
     * @throws Exception
     */
    public void testVXMLBackendSubscriber1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subscriber_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*MOCK:\\sProfileManagerMock\\.getProfile.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\smas\\.subscriberExist\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * This testcase tests various functions with subscriber manipulation.
     *
     * @throws Exception
     */
    public void testVXMLBackendSubscriber2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subscriber_2", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\smas\\.subscriberGetStringAttribute\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * This testcase tests various functions with subscriber manipulation.
     *
     * @throws Exception
     */
    public void testVXMLBackendSubscriber3() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("subscriber_3", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sUnknown\\sok.*datanotfound.*unknown.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * verify that if we change the configuration between two calls, platformAccess will understand this.
     *
     * @throws Exception
     */
    public void testVXMLBackendConfiguration1() throws Exception {

        ConfigurationGroupMock.hostName = "ingvar";

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("configuration_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*VALUE:ingvar.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }

        // setup call 2

        ConfigurationGroupMock.hostName = "perola";
        LogFileExaminer lfe2 = runSimpleTest("configuration_1");
        lfe2.failOnUndefinedErrors();

        // expectation for the CCXML file:
        lfe2.add2LevelRequired(".*VALUE:perola.*");
        // expectation for the VXML file:
        lfe2.failOnUndefinedErrors();
        validateTest(lfe2);
    }

}