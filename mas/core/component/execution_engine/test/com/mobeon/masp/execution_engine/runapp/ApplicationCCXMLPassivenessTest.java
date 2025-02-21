package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

import com.mobeon.masp.execution_engine.runapp.mock.ConfigurationGroupMock;
import com.mobeon.masp.execution_engine.runapp.mock.CallManagerMock;
import com.mobeon.masp.execution_engine.runapp.mock.ServiceRequestRunner;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 27, 2006
 * Time: 6:33:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationCCXMLPassivenessTest extends ApplicationBasicTestCase<ApplicationCCXMLPassivenessTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("ccxml");
        testSubdir("passiveness");
        testCases(
                testCase("passiveness_1"),
                testCase("passiveness_2"),
                testCase("passiveness_3"),
                testCase("passiveness_4"),
                testCase("passiveness_5"),
                testCase("passiveness_6"),
                testCase("passiveness_7"),
                testCase("passiveness_8"),
                testCase("passiveness_9"),
                testCase("passiveness_10"),
                testCase("passiveness_11"),
                testCase("passiveness_12"),
                testCase("passiveness_13"),
                testCase("passiveness_14"),
                testCase("passiveness_15"),
                testCase("passiveness_16"),
                testCase("passiveness_17"),
                testCase("passiveness_18")
                );
        store(ApplicationCCXMLPassivenessTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationCCXMLPassivenessTest (String event)
    {
        super (event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLPassivenessTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLPassivenessTest.class);
    }

    /**
     * Test that the passiveness timer triggers if the application does not perform
     * an accept, reject or proxy after receiving alerting.
     * @throws Exception
     */
    public void testCCXMLPassiveness1 () throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_1", ConfigurationGroupMock.accepttimeout+8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*error\\.connection.*");
        lfe.add2LevelRequired(".*MOCK: CallMock.disconnect.*");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
    }

    /**
     * Verify that error.connection is delivered to the application if the CallManager never responds to accept()
     * @throws Exception
     */
    public void testCCXMLPassiveness2 () throws Exception {

        setResponseToAccept(CallManagerMock.EventType.WITHHOLD);
        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_2", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.ignoreTransition("error.connection");
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setResponseToAccept(CallManagerMock.EventType.CONNECTED_EVENT);

    }

    /**
     * Verify that there will be no error.connection if everything works as it should.
     * @throws Exception
     */
    public void testCCXMLPassiveness3 () throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_3", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();


        lfe.add2LevelRequired(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.*PlayFinishedEvent.*");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");
        lfe.add3LevelFailureTrigger(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
    }

    /**
     * Verify that if connection.disconnected is the reply to accept(), there will be no error.connection delivered.
     * @throws Exception
     */
    public void testCCXMLPassiveness4 () throws Exception {

        setResponseToAccept(CallManagerMock.EventType.DISCONNECTED_EVENT);

        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_4", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");

        lfe.add3LevelFailureTrigger(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        // reset
        setResponseToAccept(CallManagerMock.EventType.CONNECTED_EVENT);
    }

    /**
     * Verify that error.connection is delivered to the application if the CallManager never responds to join()
     * @throws Exception
     */
    public void testCCXMLPassiveness5 () throws Exception {

        setWithholdJoinAttempt(true);
        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_5", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.ignoreTransition("error.connection");
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setWithholdJoinAttempt(false);
    }

    /**
     * Verify that error.connection is delivered to the application if the CallManager never responds to unjoin()
     * @throws Exception
     */
    public void testCCXMLPassiveness6 () throws Exception {

        setWithholdUnjoinAttempt(true);
        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_6", ConfigurationGroupMock.callManagerWaitTime + 20000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.ignoreTransition("error.connection");
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setWithholdUnjoinAttempt(false);
    }

    /**
     * Verify that error.connection is delivered to the application if the CallManager never responds to reject()
     * @throws Exception
     */
    public void testCCXMLPassiveness7 () throws Exception {

        setWithholdRejectAttempt(true);
        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_7", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.ignoreTransition("error.connection");
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setWithholdRejectAttempt(false);
    }

    /**
     * Verify that error.connection is delivered to the application if the CallManager never responds to disconnect()
     * @throws Exception
     */
    public void testCCXMLPassiveness8 () throws Exception {


        setWithholdDisconnectAttempt(true);
        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_8", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.ignoreTransition("error.connection");
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setWithholdDisconnectAttempt(false);
    }

    /**
     * Verify that error.connection is delivered to the application if the CallManager never responds to createcall()
     * @throws Exception
     */
    public void testCCXMLPassiveness9 () throws Exception {


        ConfigurationGroupMock.createcalladditionaltimeout = 4000;
        setResponseToCreateCall(CallManagerMock.EventType.WITHHOLD, null);

        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_9", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.ignoreTransition("error.connection");
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setResponseToCreateCall(CallManagerMock.EventType.NONE, null);
    }

    /**
     * Verify that error.connection is delivered to the application if the CallManager never responds to negotiateEarlyMedia()
     * @throws Exception
     */
    public void testCCXMLPassiveness10 () throws Exception {

        // Do this to make sure the "accpet timeout" does not trigger first:
        ConfigurationGroupMock.accepttimeout = ConfigurationGroupMock.callManagerWaitTime * 2;

        setWithholdnegotiateEarlyMedia(true);
        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_10", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.ignoreTransition("error.connection");
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*Expected event for systemSetEarlyMediaResource did not arrive in time.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setWithholdnegotiateEarlyMedia(false);
    }

    /**
     * Verify that the event supervision works when there are
     * two equests after each other sent to CallManager (i.e. the second is sent before the response
     * for the first is received).
     *
     * @throws Exception
     */
    public void testCCXMLPassiveness11 () throws Exception {

        ConfigurationGroupMock.createcalladditionaltimeout = 4000;
        ConfigurationGroupMock.callManagerWaitTime = 9000;

        setDelayBeforeResponseToDisconnect(3000);
        setDelayBeforeResponseToCreateCall(6000);

        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_11", 2 *(ConfigurationGroupMock.callManagerWaitTime + 8000));
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(".*\\sTCPASS1.*");
        lfe.add2LevelRequired(".*\\sTCPASS2.*");
        lfe.add2LevelRequired(".*\\sTCPASS3.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());

        setDelayBeforeResponseToDisconnect(0);
        setDelayBeforeResponseToCreateCall(0);
        ConfigurationGroupMock.defaultValues();
    }

    /**
     * Verify that error.requesttimeout is delivered to the application if the CallManager never responds to sendSIPMessage
     * @throws Exception
     */
    public void testCCXMLPassiveness12 () throws Exception {

        // Do this to make sure the "accpet timeout" does not trigger first:
        ConfigurationGroupMock.accepttimeout = ConfigurationGroupMock.callManagerWaitTime * 2;

        setWithholdsendSIPMessage(true);
        // Setup the session
        ServiceRequestRunner r = createServiceRequestRunner("passiveness_12");
        r.startCall();
        boolean exited = r.waitForExecutionToFinish(ConfigurationGroupMock.callManagerWaitTime + 8000);

        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*error\\.requesttimeout.*");
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(".*TCPASS CCXML.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setWithholdsendSIPMessage(false);
    }

    /**
     * Verify that error.connection is delivered to the application if the CallManager never responds to proxy().
     * @throws Exception
     */
    public void testCCXMLPassiveness13 () throws Exception {

        setResponseToProxy(CallManagerMock.EventType.WITHHOLD);
        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_13", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.ignoreTransition("error.connection");
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.addIgnored(".*\\sExecuting <log expr=.*");
        lfe.add2LevelRequired(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add2LevelRequired(".*\\serror.connection received.*Expected event for proxy did not arrive in time.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setResponseToProxy(CallManagerMock.EventType.PROXY_EVENT);
    }

    /**
     * Verify that if the CallManager responds to proxy() in time with a ProxiedEvent, connection.proxied is delivered 
     * to the application and the passiveness timer does not call Disconnecter.disconnect().
     * @throws Exception
     */
    public void testCCXMLPassiveness14 () throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_14", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(".*\\sconnection.proxied received.*");

        lfe.add3LevelFailureTrigger(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setResponseToProxy(CallManagerMock.EventType.PROXY_EVENT);
    }

    /**
     * Verify that if the CallManager responds to proxy() in time with a DisconnectedEvent, connection.disconnected is 
     * delivered to the application and the passiveness timer does not call Disconnecter.disconnect() .
     * @throws Exception
     */
    public void testCCXMLPassiveness15 () throws Exception {

        setResponseToProxy(CallManagerMock.EventType.DISCONNECTED_EVENT);
        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_15", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(".*\\sconnection.disconnected received.*");

        lfe.add3LevelFailureTrigger(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setResponseToProxy(CallManagerMock.EventType.PROXY_EVENT);
    }

    /**
     * Verify that if the CallManager responds to proxy() in time with a ErrorEvent, error.connection is delivered to 
     * the application and the passiveness timer does not call Disconnecter.disconnect().
     * @throws Exception
     */
    public void testCCXMLPassiveness16 () throws Exception {

        setResponseToProxy(CallManagerMock.EventType.ERROR_CONNECTION);
        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_16", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.ignoreTransition("error.connection");
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.addIgnored(".*\\sExecuting <log expr=.*");
        lfe.addIgnored(".*\\sERROR INBOUND.*");
        lfe.add2LevelRequired(".*\\serror.connection received.*Reason = error.connection.*");

        lfe.add3LevelFailureTrigger(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setResponseToProxy(CallManagerMock.EventType.PROXY_EVENT);
    }

    /**
     * Verify that if the CallManager responds to proxy() in time with a FailedEvent, a connection.failed is 
     * delivered to the application and the passiveness timer does not call Disconnecter.disconnect() .
     * @throws Exception
     */
    public void testCCXMLPassiveness17 () throws Exception {

        setResponseToProxy(CallManagerMock.EventType.FAILED_EVENT);
        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_17", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.add2LevelRequired(".*\\sconnection.failed received.*");

        lfe.add3LevelFailureTrigger(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setResponseToProxy(CallManagerMock.EventType.PROXY_EVENT);
    }

    /**
     * Verify that if the CallManager responds to proxy() in time with a NotAllowedEvent, error.notallowed is delivered to 
     * the application and the passiveness timer does not call Disconnecter.disconnect().
     * @throws Exception
     */
    public void testCCXMLPassiveness18 () throws Exception {

        setResponseToProxy(CallManagerMock.EventType.ERROR_NOT_ALLOWED);
        // Setup the call
        boolean exited = createCallAndWaitForCompletion ("passiveness_18", ConfigurationGroupMock.callManagerWaitTime + 8000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();
        lfe.ignoreTransition("error.notallowed");
        lfe.addIgnored(TestEvent.WAITSET_REQUIRED);
        lfe.addIgnored(".*\\sExecuting <log expr=.*");
        lfe.add2LevelRequired(".*\\serror.notallowed received.*");

        lfe.add3LevelFailureTrigger(TestEvent.DISCONNECTER_DISCONNECT);
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
        setResponseToProxy(CallManagerMock.EventType.PROXY_EVENT);
    }

}
