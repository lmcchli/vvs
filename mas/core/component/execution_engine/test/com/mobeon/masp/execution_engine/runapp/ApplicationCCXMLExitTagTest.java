package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

import com.mobeon.masp.execution_engine.runapp.mock.SessionInfoMock;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jul 3, 2006
 * Time: 1:19:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationCCXMLExitTagTest extends ApplicationBasicTestCase<ApplicationCCXMLExitTagTest> {

    static {
        testLanguage("ccxml");
        testSubdir("exit");
        testCases(
                testCase("exit_1")
        );
        store(ApplicationCCXMLExitTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationCCXMLExitTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLExitTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLExitTagTest.class);
    }

    /**
     * verify that if the application performs <exit> when there is an inbound call in the alerting state,
     * the call is automatically disconnected.
     * @throws Exception
     */
    public void testExit1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("exit_1", 10000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*MOCK: CallMock.disconnect.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}
