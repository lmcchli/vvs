/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import static com.mobeon.masp.execution_engine.util.TestEvent.*;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.*;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.stream.RecordFinishedEvent;
import junit.framework.Test;

import java.util.List;

/**
 * @author David Looberger
 */
public class ApplicationVXMLMarkTagTest extends ApplicationBasicTestCase<ApplicationVXMLMarkTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("mark");
        testCases(
                testCase("mark_1"),
                testCase("mark_2"),
                testCase("mark_3")
        );
        store(ApplicationVXMLMarkTagTest.class);

    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLMarkTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLMarkTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLMarkTagTest.class);
    }

    /**
     * @throws Exception
     */
    public void testVXMLMarkTag1() throws Exception {

        declareWait(ACCEPT);
        final InboundCallMock icm = createCall("mark_1");
        icm.startCall();
        waitFor(ACCEPT, 5000);
        declareNoWait();
        new Thread() {
            public void run() {
                try {
                    icm.sleep(10000);
                    icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();
        icm.waitForPlay(4100);
        boolean exited = icm.waitForExecutionToFinish(40000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sMark\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sMark\\s1b.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test mark tags when hanging up during a prompt play
     *
     * @throws Exception
     */
    public void testVXMLMarkTag2() throws Exception {

        final InboundCallMock icm = createCall("mark_2");
        icm.startCall();
        icm.sleep(2500);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(40000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sMark\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testVXMLMarkTag3() throws Exception {

        final InboundCallMock icm = createCall("mark_3");
        icm.startCall();
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);
        boolean exited = icm.waitForExecutionToFinish(40000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }


}
