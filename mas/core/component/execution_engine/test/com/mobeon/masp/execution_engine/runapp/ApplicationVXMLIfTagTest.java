package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.stream.ControlToken;
import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Feb 10, 2006
 * Time: 1:03:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLIfTagTest extends ApplicationBasicTestCase<ApplicationVXMLIfTagTest> {

    static {
        testLanguage("vxml");
        testSubdir("if");
        testCases(
                testCase("if_1"),
                testCase("if_2"),
                testCase("if_3"),
                testCase("if_4"),
                testCase("if_5"),
                testCase("if_6"),
                testCase("if_7"),
                testCase("if_8")
        );
        store(ApplicationVXMLIfTagTest.class);


    }

    public ApplicationVXMLIfTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite
     *
     * @return the new testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLIfTagTest.class);
    }

    protected void genericSetUp(Class clazz) throws Exception {
        genericSetUp(ApplicationVXMLIfTagTest.class);
    }

    /**
     * Verify that it is possible to execute a simple if-statement.
     *
     * @throws Exception
     */
    public void testVXMLIfTag1() throws Exception {

        // Setup the call
        String testCase = "if_1";
        setUpAndRunTest(testCase);
    }

    /**
     * verify that it is possible to execute an if-else
     *
     * @throws Exception
     */
    public void testVXMLIfTag2() throws Exception {

        // Setup the call
        String testCase = "if_2";
        setUpAndRunTest(testCase);
    }

    /**
     * Verify that if-elseif works ok.
     *
     * @throws Exception
     */
    public void testVXMLIfTag3() throws Exception {

        // Setup the call
        String testCase = "if_3";
        setUpAndRunTest(testCase);
    }

    /**
     * Verify that if-elseif-else works ok.
     *
     * @throws Exception
     */

    public void testVXMLIfTag4() throws Exception {

        // Setup the call
        String testCase = "if_4";
        setUpAndRunTest(testCase);
    }

    /**
     * Verify that if-elseif-elseif-else works ok.
     *
     * @throws Exception
     */
    public void testVXMLIfTag5() throws Exception {

        // Setup the call
        String testCase = "if_5";
        setUpAndRunTest(testCase);
    }

    /**
     * Verify that if-elseif-elseif-else works ok.
     * In this case the last elseif will be true.
     *
     * @throws Exception
     */

    public void testVXMLIfTag6() throws Exception {

        // Setup the call
        String testCase = "if_6";
        setUpAndRunTest(testCase);
    }

    /**
     * Verify that if in filled works. The correct prompt should be played.
     *
     * @throws Exception
     */

    public void testVXMLIfTag7() throws Exception {

        // Setup the call
        final InboundCallMock icm = createCall("if_7");
        icm.startCall();
        new Thread() {
            public void run() {
                try {
                    icm.sleep(2000);
                    icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();
        icm.waitForPlay(2000);
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS: The value of c1 is 1.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that if the application contains so many nested if-statements so there is a stack exhaustion in EE,
     * the EE is able to disconnect the call (I dint really know where to put this test case).
     *
     * @throws Exception
     */
    public void testVXMLIfTag8() throws Exception {

        final InboundCallMock icm = createCall("if_8");

        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        // Since we are forcing a stack exhaustion in this test case we want this line to occur:
        lfe.add2LevelRequired(".*Engine stack exhausted.*");
        lfe.add2LevelRequired(".*CallMock.disconnect.*");

        lfe.addIgnoredException(".*Engine stack exhausted.*");

        lfe.addIgnored(".*error.*");
        lfe.addIgnored(".*ERROR.*");
        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    private void setUpAndRunTest(String testCase) {
        boolean exited = createCallAndWaitForCompletion(testCase, 5000);
        if (!exited) {
            fail("The application timed out!");
        }
        setupCommonVerification();
    }

    private void setupCommonVerification() {
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\spass.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }
}
