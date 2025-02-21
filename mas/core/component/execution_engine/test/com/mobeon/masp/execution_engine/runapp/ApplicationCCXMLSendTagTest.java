/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.CallManagerMock;
import junit.framework.Test;

import java.util.List;

/**
 * This class executes the test sequences for the CCXML log tag.
 */
public class ApplicationCCXMLSendTagTest extends ApplicationBasicTestCase<ApplicationCCXMLSendTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("ccxml");
        testSubdir("send");
        testCases(
                testCase("send_1"),
                testCase("send_2"),
                testCase("send_3"),
                testCase("send_4")
        );
        store(ApplicationCCXMLSendTagTest.class);

    }

    /**
     * Creates this test case
     */
    public ApplicationCCXMLSendTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLSendTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLSendTagTest.class);
    }

    /**
     * Tests sending a simple event.
     *
     * @throws Exception
     */
    public void testCCXMLSend1() throws Exception {

        setResponseToAccept(CallManagerMock.EventType.CONNECTED_EVENT); // dirty fix since some earlier test case setup destroyed this one
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("send_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*Log_TM.*TCPASS\\sCCXML:\\ssend\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests sending a simple event with delay.
     *
     * @throws Exception
     */
    public void testCCXMLSend2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("send_2", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*Log_TM.*TCPASS\\sCCXML:\\ssend\\s1\\sok.*");
        lfe.add2LevelRequired(".*Log_TM.*TCPASS\\sCCXML:\\ssend\\s2\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test that if two events are sent with different delay, they arrive in the expected order.
     *
     * @throws Exception
     */
    public void testCCXMLSend3() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("send_3", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test that if two events are sent with different delay, they arrive in the expected order.
     * (The events are created in another order compared to testCCXMLSend3)
     *
     * @throws Exception
     */
    public void testCCXMLSend4() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("send_4", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}