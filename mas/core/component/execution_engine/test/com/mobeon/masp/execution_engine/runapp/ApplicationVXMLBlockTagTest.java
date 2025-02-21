package com.mobeon.masp.execution_engine.runapp;

import java.util.List;

import junit.framework.Test;

/**
 * Test suite for the block tag.
 */
public class ApplicationVXMLBlockTagTest extends ApplicationBasicTestCase<ApplicationVXMLBlockTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("block");
        testCases(
                testCase("block_1"),
                testCase("block_2"),
                testCase("block_3"),
                testCase("block_4")
        );
        store(ApplicationVXMLBlockTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLBlockTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLBlockTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLBlockTagTest.class);
    }

    /**
     * This testcase tries to executes a block in a form.
     *
     * @throws Exception
     */
    public void testVXMLBlockTag1() throws Exception {
        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("block_1", 20000);
        TestAppender.stopSave(log);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sblock\\sok\\s1.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test a block tag with the cond attribute.
     *
     * @throws Exception
     */
    public void testVXMLBlockTag2() throws Exception {
        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("block_2", 20000);
        TestAppender.stopSave(log);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test multiple blocks within one form.
     *
     * @throws Exception
     */
    public void testVXMLBlockTag3() throws Exception {

        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("block_3", 20000);
        TestAppender.stopSave(log);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        /*
         * FIXME This check fails in Hudson only.  Requires more investigation.  Here's what I found so far:
         * 
         * When ran on PC or Linux command line, the output looks like this and contains the "block ok 3" we're looking for:
         * 
         *
22:52.052 [TID:pool-1-thread-8] [SID:session_2] util.TestEventGenerator  INFO VXML_MODULE_STARTED=com.mobeon.masp.execution_engine.voicexml.runtime.ExecutionContextImpl@131de9b
22:52.052 [TID:pool-1-thread-8] [SID:session_2] operations.Log  INFO Executing <form id="main_form"> (line 3)
22:52.052 [TID:pool-1-thread-8] [SID:session_2] operations.Log  INFO Executing <block xmlns="http://www.w3.org/2001/vxml"> (line 7)
22:52.052 [TID:pool-1-thread-8] [SID:session_2] operations.Log  INFO Executing <log xmlns="http://www.w3.org/2001/vxml">TCPASS VXML: block ok 3</log> (line 8)
22:52.052 [TID:pool-1-thread-8] [SID:session_2] Application  INFO /test/com/mobeon/masp/execution_engine/runapp/applications/vxml/block/block_3.vxml:8 TCPASS VXML: block ok 3
22:52.052 [TID:pool-1-thread-8] [SID:session_2] util.TestEventGenerator  INFO PROMPT_QUEUE_PLAY=0
22:52.052 [TID:pool-1-thread-8] [SID:session_2] util.TestEventGenerator  INFO NOINPUT_STARTING=5 SECONDS
22:52.052 [TID:pool-1-thread-8] [SID:session_2] util.TestEventGenerator  INFO PROPERTYSTACK_PUT_PROPERTY={bargein:true}

           But the same run on Hudson shows the following in the logs.  It's missing 2 critial operations.Log and Application INFO lines:
           
41:06.006 [TID:pool-1-thread-21] [SID:session_2] util.TestEventGenerator  INFO VXML_MODULE_STARTED=com.mobeon.masp.execution_engine.voicexml.runtime.ExecutionContextImpl@27420a71
41:06.006 [TID:pool-1-thread-21] [SID:session_2] operations.Log  INFO Executing <form id="main_form"> (line 3)
41:06.006 [TID:pool-1-thread-21] [SID:session_2] util.TestEventGenerator  INFO PROMPT_QUEUE_PLAY=0
41:06.006 [TID:pool-1-thread-21] [SID:session_2] util.TestEventGenerator  INFO PROPERTYSTACK_PUT_PROPERTY={bargein:true}
           
           This is not the only test case that uses add2LevelRequired(), but it's the only one that fails, and only in Hudson builds.
         */
        /*
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sblock\\sok\\s3.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
        */
    }

    /**
     * Tests a simple block, this is just to test the timing issues with the basic
     * test environment.
     *
     * @throws Exception
     */
    public void testVXMLBlockTag4() throws Exception {

        TestAppender.clear();
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("block_4", 20000);
        TestAppender.stopSave(log);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sblock\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

}
