package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.util.TestEvent;
import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Feb 6, 2006
 * Time: 4:05:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLAudioTagTest extends ApplicationBasicTestCase<ApplicationVXMLAudioTagTest> {


    static {
        testLanguage("vxml");
        testSubdir("audio");
        testCases(
                testCase("audio_1")
        );
        store(ApplicationVXMLAudioTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLAudioTagTest(String
            event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite
            () {
        return genericSuite(ApplicationVXMLAudioTagTest.class);
    }

    protected void setUp
            () throws Exception {
        genericSetUp(ApplicationVXMLAudioTagTest.class);
    }

    /**
     * Verify that a missing audio file does not result in error.badfetch
     *
     * @throws Exception
     */
    public void testVXMLAudioTag1
            () throws Exception {

        // Setup the call
        String testCase = "audio_1";
        setUpAndRunTest(testCase);
    }

    private void setUpAndRunTest
            (String
                    testCase) {
        boolean exited = createCallAndWaitForCompletion(testCase, 20000);
        if (!exited) {
            fail("The application timed out!");
        }


        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(TestEvent.CREATE_PLAYABLE_OBJECT_TM_P, new IllegalArgumentException());

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS VXML: Audio 1 OK.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    private void setupCommonVerification
            () {
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*INFO TCPASS VXML: Audio 1 OK.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }
}
