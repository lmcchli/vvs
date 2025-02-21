/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import static com.mobeon.masp.execution_engine.util.TestEvent.*;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.*;
import junit.framework.Test;

import java.util.List;

/**
 * @author David Looberger
 */
public class ApplicationVXMLConformanceTest extends ApplicationBasicTestCase<ApplicationVXMLConformanceTest> {

    static {
        testLanguage("vxml");
        testSubdir("conformance_testcases");
        testCases(
                testCase("conf_377"),
                testCase("defer_until_waiting"),
                testCase("exit_if_waiting_final_processing")
        );
        store(ApplicationVXMLConformanceTest.class);
    }

    /**
     * Constructor for this test suite, must be called from the testclass that inherits
     * this class through a super(event) call.
     *
     * @param event
     */
    public ApplicationVXMLConformanceTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLConformanceTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLConformanceTest.class);
    }

    /**
     * Verify that it is possible to exit from a block
     *
     * @throws Exception
     */
    public void testVXMLConf1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("conf_377", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sconf:fail.*");
        lfe.add2LevelRequired(".*\\(TC 2290, CF 377\\) conf:pass.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that it is possible to exit from a block
     *
     * @throws Exception
     */
    public void testVXMLConf2() throws Exception {

        // Setup the call
        declareWait(ACCEPT);

        InboundCallMock icm = createCall("defer_until_waiting");
        icm.startCall();
        waitFor(ACCEPT, 5000);
        declareNoWait();
        Thread.sleep(4000);
        icm.disconnect();

        // Wait for the call to complete
        boolean exited = icm.waitForExecutionToFinish(30000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sconf:fail.*");
        lfe.add2LevelRequired(".*Start running the scripts.*");
        lfe.add2LevelRequired(".*In between the scripts.*");
        lfe.add2LevelRequired(".*Got DataNotFound.*");
        lfe.add2LevelRequired(".*\\sTCPASS: OK.*");
        lfe.add3LevelFailureTrigger(".*INFO .*Done running the scripts.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that the interpreter exits if the call has been hung up,
     * and the application still tries to enter the waiting state
     *
     * @throws Exception
     */
    public void testVXMLConf3() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("exit_if_waiting_final_processing");
        icm.startCall();

        // Wait for the call to complete
        boolean exited = icm.waitForExecutionToFinish(20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*Should get here.*");

        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}
