package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import com.mobeon.masp.execution_engine.runapp.mock.CallManagerMock;

public class ApplicationCCXMLProxyTagTest  extends ApplicationBasicTestCase<ApplicationCCXMLProxyTagTest> {

    static {
        testLanguage("ccxml");
        testSubdir("proxy");
        testCases(
                testCase("proxy_success"),
                testCase("proxy_no_connectionid_attribute"),
                testCase("proxy_no_server_attribute"),
                testCase("proxy_no_port_attribute"),
                testCase("proxy_non_connection_event"),
                testCase("proxy_not_allowed_event")
        );
        store(ApplicationCCXMLProxyTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationCCXMLProxyTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setup
     * only !!!
     *
     * @return a test suite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLProxyTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLProxyTagTest.class);
    }

    /**
     * Test if the proxy tag is used in a transition for an alerting
     * event, it will succeed.
     *
     * @throws Exception
     */
    public void testCCXMLProxyTagSuccess() throws Exception {
        setTestCaseTimeout(10000);
        LogFileExaminer lfe = runSimpleTest("proxy_success");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);
    }

    /**
     * Test if the connectionid attribute of the proxy tag is omitted, 
     * it will still succeed (since the execution engine will use the id of 
     * connection of the last event sent).
     *
     * @throws Exception
     */
    public void testCCXMLProxyTagNoConnectionIdAttribute() throws Exception {
        setTestCaseTimeout(10000);
        LogFileExaminer lfe = runSimpleTest("proxy_no_connectionid_attribute");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);
    }
    
    /**
     * Test if the required server attribute of the proxy tag is omitted,
     * an error.semantic will occur.
     *
     * @throws Exception
     */
    public void testCCXMLProxyTagNoServerAttribute() throws Exception {
        LogFileExaminer lfe = runSimpleTest("proxy_no_server_attribute");
        lfe.addIgnored(".*error.semantic.*");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);  
    }

    /**
     * Test if the required port attribute of the proxy tag is omitted,
     * an error.semantic will occur.
     *
     * @throws Exception
     */
    public void testCCXMLProxyTagNoPortAttribute() throws Exception {
        LogFileExaminer lfe = runSimpleTest("proxy_no_port_attribute");
        lfe.addIgnored(".*error.semantic.*");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);       
    }    

    /**
     * Test if the proxy tag is used in a transition for a non-connection
     * event, an error.semantic will occur.
     *
     * @throws Exception
     */
    public void testCCXMLProxyTagNonConnectionEvent() throws Exception {
        setTestCaseTimeout(10000);
        LogFileExaminer lfe = runSimpleTest("proxy_non_connection_event");
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
    public void testCCXMLProxyTagNotAllowed() throws Exception {
    	setResponseToProxy(CallManagerMock.EventType.ERROR_NOT_ALLOWED);

        LogFileExaminer lfe = runSimpleTest("proxy_not_allowed_event");
        lfe.addIgnored(".*error.notallowed.*");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);
        setResponseToProxy(CallManagerMock.EventType.PROXY_EVENT);        
    }
}

