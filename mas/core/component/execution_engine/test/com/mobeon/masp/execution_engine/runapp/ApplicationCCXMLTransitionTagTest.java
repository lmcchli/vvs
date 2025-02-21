/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

public class ApplicationCCXMLTransitionTagTest extends ApplicationBasicTestCase<ApplicationCCXMLTransitionTagTest> {


    static {
        testLanguage("ccxml");
        testSubdir("transition");
        testCases(
                testCase("transition_1"),
                testCase("transition-err-in-alerting"),
                testCase("transition-platform-access1")
                );
        store(ApplicationCCXMLTransitionTagTest.class);
    }



    /**
     * Creates this test case
     */
    public ApplicationCCXMLTransitionTagTest(String event)
    {
        super (event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLTransitionTagTest.class);
    }


    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLTransitionTagTest.class);
    }

    /**
     * Tests the transition tag in ccxml with and without name attribute,
     * see transition_1.ccxml.
     *
     * @throws Exception
     */
    public void testCCXMLTransitionTag1() throws Exception {

        LogFileExaminer lfe = runSimpleTest("transition_1");
        lfe.add2LevelRequired(".*Log_TM.*TCPASS.*?TransitionWithoutName.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        validateTest(lfe);
    }

    public void testErrorInAlerting() {
        LogFileExaminer lfe = runSimpleTest("transition-err-in-alerting");
        lfe.addIgnored(".*error.semantic.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add2LevelRequired(".*CallMock.disconnect.*");
        validateTest(lfe);

    }

    /**
     * Test that a platform access error is propagated to the application
     * (TR 27117)
     */
    public void testPlatformAccessError1() {
        LogFileExaminer lfe = runSimpleTest("transition-platform-access1");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        validateTest(lfe);
    }

}
