package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.util.TestEvent;
import junit.framework.Test;

import java.util.List;

/**
 * @author David Looberger
 */
public class ApplicationVXMLExternalDocsTest extends ApplicationBasicTestCase<ApplicationVXMLExternalDocsTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("externaldocs");
        testCases(
                testCase("external_1"),
                testCase("external_2"),
                testCase("external_3")
        );
        store(ApplicationVXMLExternalDocsTest.class);
    }

    /**
     * Constructor for this test suite, must be called from the testclass that inherits
     * this class through a super(event) call.
     *
     * @param event
     */
    public ApplicationVXMLExternalDocsTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for ApplicationGotoTagTest
     *
     * @return the new testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLExternalDocsTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLExternalDocsTest.class);
    }

    /**
     * Test goto to an external document, where an external script file is evaluated and an external audio file is played
     *
     * @throws Exception
     */
    public void testGotoExternalTest() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("external_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*Function returned DO SOME.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML: out1 OK.*");
        lfe.add2LevelRequired(TestEvent.PROMPT_QUEUE_PLAY, 1);
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test goto to a specified form in an external document, where an external script file is evaluated
     *
     * @throws Exception
     */
    public void testGotoExternalSubFormTest() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("external_2", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*Function returned DO SOME.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML: out2 OK.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Test goto to an external document, having the same application attr as the originating
     * document. In the target document an script located in the root document is executed.
     *
     * @throws Exception
     */
    public void testGotoExternalSameRootTest() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("external_3", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*Function returned INTERNAL STUFF.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML: out3 OK.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }
}
