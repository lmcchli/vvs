package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jan 5, 2006
 * Time: 9:59:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLVXMLTagTest extends ApplicationBasicTestCase<ApplicationVXMLVXMLTagTest> {
    static {
        testLanguage("vxml");
        testSubdir("vxml");
        testCases(
                testCase("vxml_1")
        );
        store(ApplicationVXMLVXMLTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLVXMLTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for ApplicationGotoTagTest
     *
     * @return the new testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLVXMLTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLVXMLTagTest.class);
    }

    /**
     * @throws Exception
     */
    public void testVXMLVXMLTag1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("vxml_1", 100000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\spassed.*");
        lfe.add3LevelFailureTrigger(".*Application.*TCFAIL.*");
        lfe.add3LevelFailureTrigger(".*Application.*TCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

}
