package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;
import com.mobeon.masp.execution_engine.runapp.mock.CallManagerMock;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 8, 2006
 * Time: 5:08:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationCCXMLAcceptTagTest extends ApplicationBasicTestCase<ApplicationCCXMLAcceptTagTest> {

    static {
        testLanguage("ccxml");
        testSubdir("accept");
        testCases(
                testCase("accept_1"),
                testCase("accept_2")
        );
        store(ApplicationCCXMLAcceptTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationCCXMLAcceptTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLAcceptTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLAcceptTagTest.class);
    }

    /**
     * Test that if you use <accept/> in a transition for a non-connection
     * event, you will get an error.semantic (TR 27282)
     *
     * @throws Exception
     */
    public void testVXMLAcceptTag1() throws Exception {
        setTestCaseTimeout(10000);
        LogFileExaminer lfe = runSimpleTest("accept_1");
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*error.semantic.*");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);
    }

    /**
     * Test that it is possible to receive error.notallowed from CallManager.
     *
     * @throws Exception
     */
    public void testVXMLAcceptTag2() throws Exception {
        setResponseToAccept(CallManagerMock.EventType.ERROR_NOT_ALLOWED);

        LogFileExaminer lfe = runSimpleTest("accept_2");
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*error.notallowed.*");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);
        setResponseToAccept(CallManagerMock.EventType.CONNECTED_EVENT);        
    }

}
