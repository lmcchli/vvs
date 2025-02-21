package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.sip.header.PEarlyMedia.PEarlyMediaTypes;
import com.mobeon.masp.execution_engine.runapp.mock.CallManagerMock;
import com.mobeon.masp.execution_engine.runapp.mock.FailedEventInfo;
import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;

import junit.framework.Test;

import java.util.List;

import com.mobeon.masp.callmanager.NumberCompletion;
/**
 * Created by IntelliJ IDEA.
 * User: etomste
 * Date: 2005-dec-30
 * Time: 08:07:21
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationCCXMLEventTest extends ApplicationBasicTestCase<ApplicationCCXMLEventTest> {
    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("ccxml");
        testSubdir("event");
        testCases(
                testCase("event_connectionid"),
                testCase("event_eventid"),
                testCase("event_eventsource"),
                testCase("event_eventsourcetype"),
                testCase("event_name"),
                testCase("event_connection_connectionid"),
                testCase("event_connection_state"),
                testCase("event_connection_local"),
                testCase("event_connection_remote"),
                testCase("event_connection_redirect"),
                testCase("event_order_1"),
                testCase("event_errstate_1"),
                testCase("event_failedstate_1"),
                testCase("event_numbercomplete_undefined"),
                testCase("event_numbercomplete_false"),
                testCase("event_numbercomplete_true"),
                testCase("event_connection_pheader")
        );
        store(ApplicationCCXMLEventTest.class);

    }

    /**
     * Creates this test case
     */
    public ApplicationCCXMLEventTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLEventTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLEventTest.class);
    }

    /**
     * A test of an event name in ccxml, i.e. that it have a valid value.
     *
     * @throws Exception
     */
    public void testEventName() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("event_name", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sconnection.alerting.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * A test of the event connectionid in ccxml, i.e. that it have a valid value.
     *
     * @throws Exception
     */
    public void testEventConnectionId() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("event_connectionid", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * A test of the event eventid in ccxml, i.e. that it have a valid value.
     *
     * @throws Exception
     */
    public void testEventEventid() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("event_eventid", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * A test of the event eventid in ccxml, i.e. that it have a valid value.
     *
     * @throws Exception
     */
    public void testEventEventsource() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("event_eventsource", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * A test of the event eventsourcetype in ccxml, i.e. that it have a valid value.
     *
     * @throws Exception
     */
    public void testEventEventsourcetype() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("event_eventsourcetype", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Tests to see if the connectionid in the connection object in the event
     * object is  set to the same as the event objects connection id and that
     * it has a valid value.
     *
     * @throws Exception
     */
    public void testCCXMLConnectionConnectionid() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("event_connection_connectionid", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sConnectionid\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests to see if the state is set in the connection object in the
     * event object.
     *
     * @throws Exception
     */
    public void testCCXMLConnectionState() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("event_connection_state", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sState ok.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sState ok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests to see if the local property of the connection object is set in the
     * event object.
     *
     * @throws Exception
     */
    public void testCCXMLConnectionLocal() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("event_connection_local", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sLocal\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sLocal\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests to see if the redirect property of the connection object is set in the
     * event object.
     *
     * @throws Exception
     */
    public void testCCXMLConnectionRedirect() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("event_connection_redirect", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sRedirect\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sRedirect\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }


    /**
     * Tests to see if the remote property of the connection object is set
     * in the event object.
     *
     * @throws Exception
     */
    public void testCCXMLConnectionRemote() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("event_connection_remote", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sRemote\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sRemote\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests to see if the events are only handled by one transition.
     *
     * @throws Exception
     */
    public void testCCXMLOrder1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("event_order_1", 8000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sconnection.alerting.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sconnection.connected.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sdialog.exit.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests to see if a disconnected event received in error state is handled properly.
     *
     * @throws Exception
     */
    public void testCCXMLEvent_Errorstate_1() throws Exception {

        // Setup the call
        setResponseToAccept(CallManagerMock.EventType.ERROR_CONNECTION);
        boolean exited = createCallAndWaitForCompletion("event_errstate_1", 10000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        //lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS CCXML0: connection\\.alerting.*");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML1: error\\.connection ERROR.*");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML2.*");

        lfe.add1LevelFailureTrigger(".*Received event EVENT_DISCONNECTED in terminal state ERROR.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());

        setResponseToAccept(CallManagerMock.EventType.CONNECTED_EVENT);
    }

    /**
     * Tests to see if a disconnected event received in failed state is handled properly.
     *
     * @throws Exception
     */
    public void testCCXMLEvent_Failedstate_1() throws Exception {

        // Setup the call
        FailedEventInfo failedEventInfo = new FailedEventInfo(FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND,
                "event failed since test environment wants it to fail",
                621);
        setResponseToCreateCall(CallManagerMock.EventType.FAILED_EVENT, failedEventInfo);
        boolean exited = createCallAndWaitForCompletion("event_failedstate_1", 10000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS CCXML0: connection.alerting.*");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML1: connection.connected.*");
        lfe.add2LevelRequired(".*\\sTCPASS CCXML2: connection.disconnected.*");

//        lfe.add1LevelFailureTrigger(".*Received event EVENT_DISCONNECTED in terminal state FAILED.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        setResponseToCreateCall(CallManagerMock.EventType.CONNECTED_EVENT, null);
    }

    /**
     * test that connection.remote._numbercomplete is true when it should be true.
     *
     * @throws Exception
     */
    public void testNumberCompleteTrue() throws Exception {

        // Setup the call
        super.a_party_completion = NumberCompletion.COMPLETE;
        boolean exited = createCallAndWaitForCompletion ("event_numbercomplete_true", 10000);
        if (!exited) {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*CCXML alerting numbercomplete: true.*");
        lfe.add2LevelRequired(".*CCXML connected numbercomplete: true.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
    }

    /**
     * test that connection.remote._numbercomplete is false when it should be false.
     *
     * @throws Exception
     */
    public void testNumberCompleteFalse() throws Exception {

        // Setup the call
        super.a_party_completion = NumberCompletion.INCOMPLETE;
        boolean exited = createCallAndWaitForCompletion ("event_numbercomplete_false", 10000);
        if (!exited) {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*CCXML alerting numbercomplete: false.*");
        lfe.add2LevelRequired(".*CCXML connected numbercomplete: false.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
    }

    /**
     * test that connection.remote._numbercomplete is undefined when it should be undefined.
     *
     * @throws Exception
     */
    public void testNumberCompleteUndefined() throws Exception {

        // Setup the call
        super.a_party_completion = NumberCompletion.UNKNOWN;
        boolean exited = createCallAndWaitForCompletion ("event_numbercomplete_undefined", 10000);
        if (!exited) {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*CCXML alerting numbercomplete: undefined.*");
        lfe.add2LevelRequired(".*CCXML connected numbercomplete: undefined.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
    }

    /**
     * Tests to see if the pHeader property of the connection object is set in the
     * event object.
     *
     * @throws Exception
     */
    public void testCCXMLConnectionPheader() throws Exception {
    	String service = "event_connection_pheader";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12175143457900");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);
        icm.setPEarlyMediaValue(PEarlyMediaTypes.PEARLY_MEDIA_INACTIVE.getValue());
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();        
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sPheader\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sPheader\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        validateTest(lfe);
    }

}
