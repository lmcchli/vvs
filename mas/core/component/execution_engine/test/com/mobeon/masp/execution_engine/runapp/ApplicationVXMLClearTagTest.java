/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * @author David Looberger
 */
public class ApplicationVXMLClearTagTest extends ApplicationBasicTestCase<ApplicationVXMLClearTagTest> {
    /**
     * Constructor for this test suite, must be called from the testclass that inherits
     * this class through a super(event) call.
     *
     * @param event
     */
    public ApplicationVXMLClearTagTest(String event) {
        super(event);
    }

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("clear");
        testCases(
                testCase("clear_1")
        );
        store(ApplicationVXMLClearTagTest.class);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLClearTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLClearTagTest.class);
    }

    public void testVXMLClearTag1() throws Exception {
        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("clear_1", 20000);
        TestAppender.stopSave(log);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sClear\\s1a.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sClear\\s1b.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sClear\\s1a.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sClear\\s1c.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

}
