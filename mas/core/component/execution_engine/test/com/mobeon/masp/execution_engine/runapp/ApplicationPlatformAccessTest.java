/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.ApplicationBasicTestCase;
import com.mobeon.masp.execution_engine.runapp.TestAppender;
import com.mobeon.masp.execution_engine.runapp.LogFileExaminer;
import junit.framework.Test;

import java.util.List;

/**
 * Date: 2006-feb-10
 *
 * @author ermmaha
 */
public class ApplicationPlatformAccessTest extends ApplicationBasicTestCase<ApplicationPlatformAccessTest> {

    static {
        testLanguage("misc");
        testSubdir("platformaccess");
        testCases(
                testCase("platform_1"),
                testCase("platform_2")
                );
        store(ApplicationPlatformAccessTest.class);
    }

    public static Test suite() {
        return genericSuite(ApplicationPlatformAccessTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationPlatformAccessTest.class);
    }



    /**
     * Creates this test case
     */
    public ApplicationPlatformAccessTest(String event) {
        super(event);
        log.info("MOCK: Setting up " + event);
    }

    /**
     * Test that platformAccess send an event if it fails,
     * and that _message becomes defined.
     * We throw 2 events in this test case.
     * @throws Exception
     */
    public void testSendEvent() throws Exception {
        // Setup the call
        String testCase = "platform_1";
        setUpAndRunTest(testCase);
    }

    private void setUpAndRunTest(String testCase) {
        boolean exited = createCallAndWaitForCompletion(testCase, 5000);
        if (!exited) {
            fail("The application timed out!");
        }
         // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARNING.*");
        lfe.add2LevelRequired(".*\\spass.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that a platformAccess event causes the name and reason properties to be set on the event
     */
    public void testEventPropertiesInCCXML() throws Exception {
        // Setup the call
        String testCase = "platform_2";
        boolean exited = createCallAndWaitForCompletion(testCase, 5000);
        if (!exited) {
            fail("The application timed out!");
        }
         // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*TCPASS CCXML: Name = error.com.mobeon.platform.datanotfound, Reason = extraData.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }



}
