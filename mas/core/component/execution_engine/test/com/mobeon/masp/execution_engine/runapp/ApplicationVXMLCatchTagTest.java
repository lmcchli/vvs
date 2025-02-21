/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import static com.mobeon.masp.execution_engine.util.TestEvent.*;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.*;
import com.mobeon.masp.stream.ControlToken;
import junit.framework.Test;

import java.util.List;

/**
 * @author David Looberger
 */
public class ApplicationVXMLCatchTagTest extends ApplicationBasicTestCase<ApplicationVXMLCatchTagTest> {
    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("catchtag");
        testCases(
                testCase("catch_1"),
                testCase("catch_2"),
                testCase("catch_3"),
                testCase("catch_4"),
                testCase("catch_5"),
                testCase("catch_6"),
                testCase("catch_7"),
                testCase("catch_8"),
                testCase("catch_9"),
                testCase("catch_10"),
                testCase("catch_11"),
                testCase("catch_12"),
                testCase("catch_13"),
                testCase("catch_14"),
                testCase("catch_15")
        );
        store(ApplicationVXMLCatchTagTest.class);
    }

    private static final long WAITTIME = 20000;

    /**
     * Constructor for this test suite, must be called from the testclass that inherits
     * this class through a super(event) call.
     *
     * @param event
     */
    public ApplicationVXMLCatchTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLCatchTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLCatchTagTest.class);
    }

    private LogFileExaminer setupStandardFailureTriggers() {
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        return lfe;
    }

    public void testVXMLCatchTag1() throws Exception {
        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("catch_1", WAITTIME);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = setupStandardFailureTriggers();

        lfe.add2LevelRequired(".*TCPASS VXML: Catch 1 someevent.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testVXMLCatchTag2() throws Exception {

        // Setup the call
        TestAppender.clear();
        boolean exited = createCallAndWaitForCompletion("catch_2", WAITTIME);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = setupStandardFailureTriggers();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sCatch\\s2.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testVXMLCatchTag3() throws Exception {
        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("catch_3", WAITTIME);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = setupStandardFailureTriggers();


        lfe.addIgnored(".*error.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sCatch\\s3.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testVXMLCatchTag4() throws Exception {
        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("catch_4", WAITTIME);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = setupStandardFailureTriggers();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sCatch\\s4.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testVXMLCatchTag5() throws Exception {
        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("catch_5", WAITTIME);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = setupStandardFailureTriggers();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sCatch\\s5.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testVXMLCatchTag6() throws Exception {
        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("catch_6", WAITTIME);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = setupStandardFailureTriggers();

        lfe.addIgnored(".*error.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testVXMLCatchTag7() throws Exception {
        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("catch_7", WAITTIME);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = setupStandardFailureTriggers();
        lfe.addIgnored(".*conference\\.joined.*");
        lfe.add2LevelRequired(".*Catch\\s7.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that if an event is thrown during Initialization and Select
     * phases of the FIA, and the catch does not result in a transition
     * from the current dialog, FIA execution will terminate.
     *
     * @throws Exception
     */
    public void testVXMLCatchTag8() throws Exception {
        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("catch_8", WAITTIME);
        if (!exited) {
            fail("The application timed out!");
        }

        List<String> l = TestAppender.getOutputList();

        LogFileExaminer lfe = new LogFileExaminer();
        lfe.addIgnored(".*error\\.semantic.*");
        lfe.addIgnored(".*ReferenceError.*");

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*FAIL.*");
        lfe.add2LevelRequired(".*\\sTCPASS*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that if an event is triggered after a form item is selected
     * (i.e. during the Collect and Process phases), and the catch does not
     * result in a transition, the current FIA loop is terminated and Select
     * phase is reentered.
     *
     * @throws Exception
     */
    public void testVXMLCatchTag9() throws Exception {
        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("catch_9", WAITTIME);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output. There will be errors in the log
        // so we cant use standard log file examination.
        List<String> l = TestAppender.getOutputList();

        LogFileExaminer lfe = new LogFileExaminer();

        lfe.addIgnored(".*error\\.semantic.*");

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*FAIL.*");

        lfe.add2LevelRequired(".*looking fine so far*");
        lfe.add2LevelRequired(".*\\sTCPASS*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }

    }

    /**
     * Verify that catch clauese in form items is triggered
     */
    public void testVXMLCatchTag10() throws Exception {
        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("catch_10", WAITTIME);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output. There will be errors in the log
        // so we cant use standard log file examination.
        List<String> l = TestAppender.getOutputList();

        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS VXML: Catch 10.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }

    }

    /**
     * Verify that catch clauese in form items is triggered
     */
    public void testVXMLCatchTag11() throws Exception {
        // Setup the call
        // Setup the call
        declareWait(ACCEPT);

        InboundCallMock icm = createCall("catch_11");
        icm.startCall();
        waitFor(ACCEPT, 5000);
        declareNoWait();
        icm.sleep(500);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 40);
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

        lfe.add2LevelRequired(".*\\sSay something.*");
        lfe.add2LevelRequired(".*\\sThe input is not valid in this contex.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML: Catch 11 ok.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that we can do a number of gotos and in the last invoked
     * leaf doc throw an event. The event handlers in the documents
     * passed through shall not be triggered. Rather shall the catch
     * in the root doc be triggered.
     *
     * @throws Exception
     */
    public void testVXMLCatchTag12() throws Exception {
        TestAppender.clear();
        // Setup the call
        // Setup the call
        InboundCallMock icm = createCall("catch_12");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(5000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sVXMLTCpass.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that if we goto from a form, its catch is no longer active.
     *
     * @throws Exception
     */
    public void testVXMLCatchTag13() throws Exception {
        TestAppender.clear();
        // Setup the call
        // Setup the call
        InboundCallMock icm = createCall("catch_13");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sVXMLTCpass.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that if we goto from a form item to another within the same form, the first
     * form items catch is no longer active.
     *
     * @throws Exception
     */
    public void testVXMLCatchTag14() throws Exception {
        TestAppender.clear();
        // Setup the call
        // Setup the call
        InboundCallMock icm = createCall("catch_14");
        icm.startCall();
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

        lfe.add2LevelRequired(".*\\sTCPASS: catch 14.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Simulate that PlatformAccess originates a VXML event, where the "message"
     * spans several lines. (We used to have a Rhino error when dealing
     * with such lines)
     *
     * @throws Exception
     */
    public void testVXMLCatchTag15() throws Exception {
        TestAppender.clear();
        // Setup the call
        InboundCallMock icm = createCall("catch_15");
        icm.startCall();
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

        lfe.add2LevelRequired(".*PASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}
