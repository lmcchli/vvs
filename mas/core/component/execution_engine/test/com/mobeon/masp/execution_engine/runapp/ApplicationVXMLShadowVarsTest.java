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
public class ApplicationVXMLShadowVarsTest extends ApplicationBasicTestCase<ApplicationVXMLShadowVarsTest> {
    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("shadowvars");
        testCases(
                testCase("shadow_1"),
                testCase("shadow_2")
        );
        store(ApplicationVXMLShadowVarsTest.class);
    }

    private static final long WAITTIME = 20000;

    /**
     * Constructor for this test suite, must be called from the testclass that inherits
     * this class through a super(event) call.
     *
     * @param event
     */
    public ApplicationVXMLShadowVarsTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setup
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLShadowVarsTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLShadowVarsTest.class);
    }

    private LogFileExaminer setupStandardFailureTriggers() {
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        return lfe;
    }

    public void testVXMLShadowVars1() throws Exception {

        // Setup the call
        declareWait(ACCEPT);
        InboundCallMock icm = createCall("shadow_1");
        // Start the call
        icm.startCall();
        waitFor(ACCEPT, 5000);
        declareNoWait();
        icm.sleep(7000);
        // Send a DTMF token, just to test things
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 200);

        // Wait for the call to complete
        boolean exited = icm.waitForExecutionToFinish(WAITTIME);
        TestAppender.stopSave(log);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = setupStandardFailureTriggers();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sShadowVars\\sok\\s1\\.1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sShadowVars\\sok\\s1\\.2.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sShadowVars\\sok\\s1\\.3.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that if a hangup event already is buffered when a <field> is reached,
     * the execution continues in a <catch> and in the <catch> the shadow variable is undefined.
     * Example: <field name="kalle"> makes a variable called "kalle$" undefined in tha <catch>.
     * TR 28017.
     *
     * @throws Exception
     */
    public void testVXMLShadowVars2() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("shadow_2");
        // Start the call
        icm.startCall();
        icm.sleep(3000);
        icm.disconnectCall();
        // Wait for the call to complete
        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = setupStandardFailureTriggers();

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

}
