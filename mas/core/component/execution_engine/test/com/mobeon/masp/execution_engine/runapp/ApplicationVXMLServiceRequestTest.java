package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.ServiceRequestRunner;
import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.execution_engine.runapp.mock.ServiceRequestManagerMock;
import com.mobeon.masp.servicerequestmanager.ServiceResponse;
import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 10, 2006
 * Time: 4:37:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLServiceRequestTest extends ApplicationBasicTestCase<ApplicationVXMLServiceRequestTest> {

    static {
        testLanguage("vxml");
        testSubdir("servicerequest");
        testCases(
                testCase("service_1"),
                testCase("service_2"),
                testCase("service_3"),
                testCase("service_4"),
                testCase("service_5"),
                testCase("service_6"),
                testCase("service_7"),
                testCase("service_8"),
                testCase("service_9"),
                testCase("service_10"),
                testCase("service_11"),
                testCase("service_12"),
                testCase("service_13"),
                testCase("service_14"),
                testCase("service_15"),
                testCase("service_16"),
                testCase("service_17"),
                testCase("service_18"),
                testCase("service_19"),
                testCase("service_20"),
                testCase("service_21"),
                testCase("service_22"),
                testCase("service_23"),
                testCase("service_24")
        );
        store(ApplicationVXMLServiceRequestTest.class);
    }


    /**
     * Creates this test case
     */
    public ApplicationVXMLServiceRequestTest(String event) {
        // Use only info-logging since running all test cases with debug log takes too much memory...
        super (event, "test_log_info.xml");

    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLServiceRequestTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLServiceRequestTest.class);
    }

    /**
     * Test a positive case
     *
     * @throws Exception
     */
    public void testVXMLServiceRequest1() throws Exception {

        ServiceRequestRunner r = createServiceRequestRunner("service_1");
        r.startCall();
        boolean exited = r.waitForExecutionToFinish(20000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*CCXML loaded.*");
        lfe.add2LevelRequired(".*CCXML connected.*");
        lfe.add2LevelRequired(".*INFO MOCK: ServiceRequestManagerMock.sendResponse.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*ERROR.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test a negative case where we terminate the session
     *
     * @throws Exception
     */
    public void testVXMLServiceRequest2() throws Exception {

        // delay the connected event to pretend it never happens
        setDelayBeforeResponseToCreateCall(20000);

        ServiceRequestRunner r = createServiceRequestRunner("service_2");
        r.startCall();
        Thread.sleep(6000);
        r.terminate();
        boolean exited = r.waitForExecutionToFinish(20000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*CCXML loaded.*");
        lfe.add2LevelRequired(".*CCXML killed.*");
        lfe.add2LevelRequired(".*MOCK: Call finished!.*ApplicationEnded.*");

        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*ERROR.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
        setDelayBeforeResponseToCreateCall(0);
    }

    /**
     * Verify that it is possible to send a service request and retrieve
     * 1 parameter from its response and all "standard" header parameters.
     * Values of parameters etc are different from test case testVXMLServiceRequest4.
     * @throws Exception
     */
    public void testVXMLServiceRequest3() throws Exception {

        // Setup the response that will be returned by ServiceRequstManagerMock.
        ServiceResponse serviceResponse = new ServiceResponse();
        int statusCode = 20;
        serviceResponse.setStatusCode(statusCode);
        String statusText = "status text here";
        serviceResponse.setStatusText(statusText);
        String parameterValue = "theParamValue is here";
        serviceResponse.setParameter("paramName", parameterValue);
        int transactionID = 99;
        serviceResponse.setTransactionId(transactionID);
        String aClientID = "ywyyeyee";
        serviceResponse.setClientId(aClientID);

        ServiceRequestManagerMock.setResponse(serviceResponse);

        InboundCallMock icm = createCall("service_3");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. hostName:ockelbo");
        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. serviceID:aService");
        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. responseRequired:true");
        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. validityTime:66");
        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. parameter:param88,value99");

        lfe.add2LevelRequired(".*\\sTC: paramName:"+parameterValue);
        lfe.add2LevelRequired(".*\\sTC: TransactionId:"+transactionID);
        lfe.add2LevelRequired(".*\\sTC: ClientId:"+aClientID);
        lfe.add2LevelRequired(".*\\sTC: StatusCode:"+statusCode);
        lfe.add2LevelRequired(".*\\sTC: StatusText:"+statusText);

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that it is possible to send a service request and retrieve
     * 1 parameter from its response and all "standard" header parameters.
     * Values of parameters etc are different from test case testVXMLServiceRequest3.
     * @throws Exception
     */
    public void testVXMLServiceRequest4() throws Exception {

        // Setup the response that will be returned by ServiceRequstManagerMock.
        ServiceResponse serviceResponse = new ServiceResponse();
        int statusCode = 10;
        serviceResponse.setStatusCode(statusCode);
        String statusText = "The status text";
        serviceResponse.setStatusText(statusText);
        String parameterValue = "theParamValue";
        serviceResponse.setParameter("paramName", parameterValue);
        int transactionID = 44;
        serviceResponse.setTransactionId(transactionID);
        String aClientID = "gagajjjj";
        serviceResponse.setClientId(aClientID);

        ServiceRequestManagerMock.setResponse(serviceResponse);

        InboundCallMock icm = createCall("service_4");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. hostName:anita");
        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. serviceID:someService");
        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. responseRequired:true");
        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. validityTime:10");
        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. parameter:param1,value1");

        lfe.add2LevelRequired(".*\\sTC: paramName:"+parameterValue);
        lfe.add2LevelRequired(".*\\sTC: TransactionId:"+transactionID);
        lfe.add2LevelRequired(".*\\sTC: ClientId:"+aClientID);
        lfe.add2LevelRequired(".*\\sTC: StatusCode:"+statusCode);
        lfe.add2LevelRequired(".*\\sTC: StatusText:"+statusText);

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that it is possible to retrieve 100 parameter values.
     * @throws Exception
     */
    public void testVXMLServiceRequest5() throws Exception {

        ServiceResponse serviceResponse = makeServiceResponse();

        final String paramValueBase = "theParamValue";
        int numParams = 100;
        for(int i=0;i<numParams;i++){
            serviceResponse.setParameter("paramName"+i, paramValueBase+i);
        }
        ServiceRequestManagerMock.setResponse(serviceResponse);

        InboundCallMock icm = createCall("service_5");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();

        for(int i=0;i<numParams;i++){
            lfe.add2LevelRequired(".*\\sTC: Param:"+paramValueBase+i);
        }

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that if ServiceRequestManager returns a null service response,
     * there is a system error.
     * @throws Exception
     */
    public void testVXMLServiceRequest6() throws Exception {

        ServiceRequestManagerMock.setResponse(null);

        InboundCallMock icm = createCall("service_6");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * verify that if the application tries to retrieve parameters from the response before there is one,
     * there is a system error.
     * @throws Exception
     */
    public void testVXMLServiceRequest7() throws Exception {

        ServiceRequestManagerMock.setResponse(null);

        InboundCallMock icm = createCall("service_7");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * verify that if the application tries to retrieve header parameters from the response before there is one,
     * there is a system error.
     * @throws Exception
     */
    public void testVXMLServiceRequest8() throws Exception {

        ServiceRequestManagerMock.setResponse(null);

        InboundCallMock icm = createCall("service_8");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * verify that if the application tries to retrieve a non-predefined header parameter,
     * there is a system error.
     * @throws Exception
     */
    public void testVXMLServiceRequest9() throws Exception {

        ServiceRequestManagerMock.setResponse(makeServiceResponse());

        InboundCallMock icm = createCall("service_9");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * verify that if the application supplies null when asking for a parameter,
     * there is a system error.
     * @throws Exception
     */
    public void testVXMLServiceRequest10() throws Exception {

        ServiceRequestManagerMock.setResponse(makeServiceResponse());

        InboundCallMock icm = createCall("service_10");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * verify that if the application supplies null when asking for a header parameter,
     * there is a system error.
     * @throws Exception
     */
    public void testVXMLServiceRequest11() throws Exception {

        ServiceRequestManagerMock.setResponse(makeServiceResponse());

        InboundCallMock icm = createCall("service_11");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that if application supplies null hostName in systemSendServiceRequest,
     * a serviceRequestManager method without hostName will be chosen.
     * @throws Exception
     */
    public void testVXMLServiceRequest12() throws Exception {

        ServiceRequestManagerMock.setResponse(makeServiceResponse());

        InboundCallMock icm = createCall("service_12");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest ServiceRequest.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that if application supplies null serviceName in systemSendServiceRequest,
     * there is a system error.
     * @throws Exception
     */
    public void testVXMLServiceRequest13() throws Exception {

        ServiceRequestManagerMock.setResponse(makeServiceResponse());

        InboundCallMock icm = createCall("service_13");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that if the application supplies null for parameterNames when calling systemSendServiceRequest,
     * there is a system error.
     * @throws Exception
     */
    public void testVXMLServiceRequest14() throws Exception {

        ServiceRequestManagerMock.setResponse(makeServiceResponse());

        InboundCallMock icm = createCall("service_14");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that if the application supplies null for parameterValues when calling systemSendServiceRequest,
     * there is a system error.
     * @throws Exception
     */
    public void testVXMLServiceRequest15() throws Exception {

        ServiceRequestManagerMock.setResponse(makeServiceResponse());

        InboundCallMock icm = createCall("service_15");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that if the application has supplied 2 parameterNames and 3 parameterValues,
     * there is a system error.
     * @throws Exception
     */
    public void testVXMLServiceRequest16() throws Exception {

        ServiceRequestManagerMock.setResponse(makeServiceResponse());

        InboundCallMock icm = createCall("service_16");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that it is possible to send 100 service requests and after each
     * send, retrieve a parameter.
     * @throws Exception
     */
    public void testVXMLServiceRequest17() throws Exception {

        ServiceResponse serviceResponse = makeServiceResponse();
        serviceResponse.setParameter("paramName", "parameterValue");

        ServiceRequestManagerMock.setResponse(serviceResponse);

        InboundCallMock icm = createCall("service_17");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        for(int i=0;i<100;i++){
            lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. hostName.*");
        }
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that it is possiblöe to send a request without parameters.
     * @throws Exception
     */
    public void testVXMLServiceRequest18() throws Exception {

        ServiceResponse serviceResponse = makeServiceResponse();
        ServiceRequestManagerMock.setResponse(serviceResponse);

        InboundCallMock icm = createCall("service_18");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. no parameters.");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that it is possible to send a request using "false" for ReportIndication
     * @throws Exception
     */
    public void testVXMLServiceRequest19() throws Exception {

        ServiceRequestManagerMock.setResponse(null);  // no response when ReportIndication false

        InboundCallMock icm = createCall("service_19");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*MOCK: ServiceRequestManagerMock.sendRequest. responseRequired:false");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that if null is passed in the parameterNames array,
     * there is a system error.
     * @throws Exception
     */
    public void testVXMLServiceRequest20() throws Exception {

        ServiceRequestManagerMock.setResponse(makeServiceResponse());

        InboundCallMock icm = createCall("service_20");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that if null is passed in the parameterValues array,
     * there is a system error.
     * @throws Exception
     */
    public void testVXMLServiceRequest21() throws Exception {

        ServiceRequestManagerMock.setResponse(makeServiceResponse());

        InboundCallMock icm = createCall("service_21");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * Verify that if the application asks for a parameter that does not exiswt in the response,
     * there is a datanotfound error.
     * @throws Exception
     */
    public void testVXMLServiceRequest22() throws Exception {

        ServiceRequestManagerMock.setResponse(makeServiceResponse());

        InboundCallMock icm = createCall("service_22");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * If the application first performs request-response, and then makes a request
     * where ReportIndication is false, there shall be a system error on attempt to retrieve a parameter
     * @throws Exception
     */
    public void testVXMLServiceRequest23() throws Exception {

        ServiceResponse serviceResponse = makeServiceResponse();
        serviceResponse.setParameter("paramName", "theParamvalue");

        ServiceRequestManagerMock.setResponse(serviceResponse);

        InboundCallMock icm = createCall("service_23");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS1.*");
        lfe.add2LevelRequired(".*\\sTCPASS2.*");
        lfe.add2LevelRequired(".*\\sTCPASS3.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }

    /**
     * If the application first performs request-response, and then makes a request
     * where ReportIndication is false, there shall be a system error on attempt to retrieve a header parameter
     * @throws Exception
     */
    public void testVXMLServiceRequest24() throws Exception {

        ServiceResponse serviceResponse = makeServiceResponse();
        serviceResponse.setParameter("paramName", "theParamvalue");
        ServiceRequestManagerMock.setResponse(serviceResponse);

        InboundCallMock icm = createCall("service_24");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS1.*");
        lfe.add2LevelRequired(".*\\sTCPASS2.*");
        lfe.add2LevelRequired(".*\\sTCPASS3.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        ServiceRequestManagerMock.setResponse(null);
    }


    private ServiceResponse makeServiceResponse() {
        ServiceResponse serviceResponse = new ServiceResponse();
        int statusCode = 10;
        serviceResponse.setStatusCode(statusCode);
        String statusText = "The status text";
        serviceResponse.setStatusText(statusText);
        int transactionID = 44;
        serviceResponse.setTransactionId(transactionID);
        String aClientID = "gagajjjj";
        serviceResponse.setClientId(aClientID);
        return serviceResponse;
    }

}
