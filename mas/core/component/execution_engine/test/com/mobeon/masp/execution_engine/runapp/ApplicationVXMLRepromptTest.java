/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import static com.mobeon.masp.execution_engine.util.TestEvent.*;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.*;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.stream.ControlToken;
import junit.framework.Test;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;

/**
 * @author David Looberger
 */
public class ApplicationVXMLRepromptTest extends ApplicationBasicTestCase<ApplicationVXMLRepromptTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("reprompt");
        testCases(
                testCase("reprompt_1"),
                testCase("reprompt_3"),
                testCase("reprompt_4")
        );
        store(ApplicationVXMLRepromptTest.class);
    }

    /**
     * Constructor for this test suite, must be called from the testclass that inherits
     * this class through a super(event) call.
     *
     * @param event
     */
    public ApplicationVXMLRepromptTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLRepromptTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLRepromptTest.class);
    }

    /**
     * Test of reprompt mechanism using the default handler when noinput is thrown.
     *
     * @throws Exception
     */
    public void testVXMLRepromptTag1a() throws Exception {

        declareWait(ACCEPT);
        // Setup the call
        // Setup the call
        InboundCallMock icm = createCall("reprompt_1");
        icm.startCall();
        waitFor(ACCEPT, 5000);
        declareNoWait();
        icm.sleep(6000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*INFO .*Some prompt.*");
        lfe.add2LevelRequired(".*INFO .*Some prompt.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sReprompt\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test of reprompt mechanism using the default handler when nomatch is thrown.
     *
     * @throws Exception
     */
    public void testVXMLRepromptTag1b() throws Exception {

        // Setup the call
        // Setup the call
        InboundCallMock icm = createCall("reprompt_1");
        icm.startCall();
        waitForVxmlStart();
        icm.sleep(500);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 40);
        icm.sleep(800);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*INFO .*Some prompt.*");
        lfe.add2LevelRequired(".*INFO .*The input is not valid in this contex.*");
        lfe.add2LevelRequired(".*INFO .*Some prompt.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sReprompt\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test of reprompt and prompt collection when noinput is thrown.
     *
     * @throws Exception
     */
    public void testVXMLRepromptTag3() throws Exception {

        // Setup the call
        // Setup the call
        InboundCallMock icm = createCall("reprompt_3");
        icm.startCall();
        waitForVxmlStart();
        icm.sleep(1000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*INFO .*Prompt 1.*");
        lfe.add2LevelRequired(".*INFO .*Prompt 2.*");
        lfe.add2LevelRequired(".*INFO .*Prompt 3.*");
        lfe.add2LevelRequired(".*INFO .*Prompt 1.*");
        lfe.add2LevelRequired(".*INFO .*Prompt 2.*");
        lfe.add2LevelRequired(".*INFO .*Prompt 3.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sReprompt\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }


    /**
     * Test of reprompt and prompt collection when noinput is thrown.
     *
     * @throws Exception
     */
    public void testVXMLRepromptTag4() throws Exception {

        // Setup the call
        // Setup the call
        declareWait(ACCEPT, PLAY_STARTED, NOINPUT_STARTING);
        InboundCallMock icm = createCall("reprompt_4");
        icm.startCall();
        waitFor(ACCEPT, 5000);
        waitFor(PLAY_STARTED, 5000);
        declareNoWait(PLAY_STARTED);
        waitFor(NOINPUT_STARTING, 5000);
        waitFor(NOINPUT_STARTING, 5000);
        waitFor(NOINPUT_STARTING, 5000);
        waitFor(NOINPUT_STARTING, 5000);
        declareNoWait();
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sPrompt 1.*");
        lfe.add2LevelRequired(".*\\sPrompt 1.*");
        lfe.add2LevelRequired(".*\\sPrompt 2.*");
        lfe.add2LevelRequired(".*\\sPrompt 3.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML\\:\\sReprompt\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}
