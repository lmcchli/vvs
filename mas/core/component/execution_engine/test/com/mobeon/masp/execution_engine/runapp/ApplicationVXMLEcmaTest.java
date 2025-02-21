/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * @author David Looberger
 */
public class ApplicationVXMLEcmaTest extends ApplicationBasicTestCase<ApplicationVXMLEcmaTest> {


    static {
        testLanguage("vxml");
        testSubdir("ecma");
        testCases(
                testCase("ecma_1"),
                testCase("ecma_2"),
                testCase("ecma_3")
        );
        store(ApplicationVXMLEcmaTest.class);
    }

    ;

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLEcmaTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLEcmaTest.class);
    }

    /**
     * Constructor for this test suite, must be called from the testclass that inherits
     * this class through a super(event) call.
     *
     * @param event
     */
    public ApplicationVXMLEcmaTest(String event) {
        super(event);
    }


    /**
     * Test that the marshalling of datatypes returned from Java object works correctly in ECMA script.
     *
     * @throws Exception
     */
    public void testVXMLEcmaTag1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("ecma_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add2LevelRequired(".*\\sValue:\\s+1.*");
        lfe.add2LevelRequired(".*\\sValue:\\s+2.*");
        lfe.add2LevelRequired(".*\\sValue:\\s+1.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML: ECMA OK.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test that an event rasied from a script triggers the corresponding catch handler directly.
     *
     * @throws Exception
     */
    public void testVXMLEcmaTag2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("ecma_2", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML: ECMA OK.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test that an event rasied from a script triggers the corresponding catch handler directly,
     * and in NOT propagated to the CCXML runtime
     *
     * @throws Exception
     */
    public void testVXMLEcmaTag3() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("ecma_3", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add1LevelFailureTrigger(".*\\(CCXML\\) Event datanotfound put on queue.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML: ECMA OK caught datanotfound.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML: ECMA OK Done.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }
}
