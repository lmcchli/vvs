package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.SessionInfoMock;
import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.execution_engine.runapp.mock.CallManagerMock;
import com.mobeon.masp.execution_engine.runapp.mock.SessionInfoFactoryMock;
import com.mobeon.masp.execution_engine.runapp.mock.ServiceEnablerMock;
import com.mobeon.masp.execution_engine.runapp.mock.ServiceRequestRunner;
import com.mobeon.masp.stream.RecordFinishedEvent;
import com.mobeon.masp.operateandmaintainmanager.CallActivity;
import com.mobeon.masp.operateandmaintainmanager.SessionInfo;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.Connection;
import com.mobeon.masp.util.test.MASTestSwitches;
import junit.framework.Test;

import java.util.List;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 1, 2006
 * Time: 2:48:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLSessionInfoTest extends ApplicationBasicTestCase<ApplicationVXMLSessionInfoTest> {

    static {
        testLanguage("vxml");
        testSubdir("sessioninfo");
        testCases(
                testCase("sessioninfo_1"),
                testCase("sessioninfo_2"),
                testCase("sessioninfo_3"),
                testCase("sessioninfo_4"),
                testCase("sessioninfo_5"),
                testCase("sessioninfo_6"),
                testCase("sessioninfo_7"),
                testCase("sessioninfo_8"),
                testCase("sessioninfo_9"),
                testCase("sessioninfo_10"),
                testCase("sessioninfo_11"),
                testCase("sessioninfo_12"),
                testCase("sessioninfo_13"),
                testCase("sessioninfo_14")
        );
        store(ApplicationVXMLSessionInfoTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLSessionInfoTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLSessionInfoTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLSessionInfoTest.class);
    }

    /**
     * Check that call setup parameters were correctly set
     *
     * Far end connection properties are not tested in this test case.
     *
     * @throws Exception
     */
    public void testSessionInfo1() throws Exception {

        // Setup the call
        String serviceName = "sessioninfo_1";
        boolean exited = createCallAndWaitForCompletion(serviceName, 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        SessionInfoMock sessionInfo = getSessionInfoMock();
        if (! sessionInfo.getAni().equals(aNumber)) {
            fail("Anumber not correct");
        }
        if (! sessionInfo.getDnis().equals(bNumber)) {
            fail("Bnumber not correct");
        }
        if (! sessionInfo.getRdnis().equals((cNumber))) {
            fail("Cnumber not correct");
        }
        if (! sessionInfo.getCallType().getInfo().equals("voice")) {
            fail("Service name not correct");
        }
        if(! sessionInfo.getService().equals(serviceName)){
            fail("serviceName was not correct, was:"+serviceName);
        }
        if(! sessionInfo.getSessionInit().equals(ServiceEnablerMock.sessionInitiator)){
            fail("Session initiator was incorrect, was:"+sessionInfo.getSessionInit());
        }

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * TR 28125.
     * Verify that if the call is hungup during playing a prompt, EE
     * will not update the sessionInfo object when receiving playFinished.
     * @throws Exception
     */
    public void testSessionInfo2() throws Exception {

             // Setup the call
        InboundCallMock icm = createCall("sessioninfo_2");

        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);
        icm.startCall();
        icm.sleep(5000);
        icm.disconnectCall();

        // Wait for the call to complete
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
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
        /*

    final InboundCallMock icm = createCall("sessioninfo_2");
    icm.startCall();
    icm.sleep(5000);
    icm.disconnectCall();
    boolean exited = icm.waitForExecutionToFinish(20000);

    if (!exited) {
        fail("The application timed out!");
    }

    // Verify the output
    List<String> l = TestAppender.getOutputList();
    LogFileExaminer lfe = new LogFileExaminer();

    lfe.add2LevelRequired(".*\\sTCPASS.*");

    lfe.failOnUndefinedErrors();
    lfe.add1LevelFailureTrigger(".*ERROR.*");

    // SessionInfoFactoryMock will do the verification of this test case
    // so we don't need anymore verifications.

    boolean success = lfe.evaluateLogFile(l);
    if (!success)
        fail(lfe.getReason());
        */
    }

    /**
     * Check that the "outbound activity" is correct if the application plays 2 prompts to
     * completion
     * @throws Exception
     */
    public void testSessionInfo3() throws Exception {

        final InboundCallMock icm = createCall("sessioninfo_3");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        SessionInfoMock sessionInfo = getSessionInfoMock();

        // We expect play, idle, play, idle
        List<CallActivity> outboundActivityHistory = sessionInfo.getOutboundActivityHistory();
        if(outboundActivityHistory.size() != 4){
            fail("Unexpected size of outboundActivityHistory:"+outboundActivityHistory.size());
        }
        CallActivity callActivity1 = outboundActivityHistory.get(0);
        CallActivity callActivity2 = outboundActivityHistory.get(1);
        CallActivity callActivity3 = outboundActivityHistory.get(2);
        CallActivity callActivity4 = outboundActivityHistory.get(3);

        if(! (callActivity1.equals(CallActivity.PLAY) &&
                callActivity2.equals(CallActivity.IDLE) &&
                callActivity3.equals(CallActivity.PLAY) &&
                callActivity4.equals(CallActivity.IDLE))){
            fail("Unexpected call activities: "+callActivity1+":"+callActivity2+":"+callActivity3+":"+callActivity4);
        }
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that "outbound activity" is correct if the call is hungup during playing of a prompt
     * @throws Exception
     */
    public void testSessionInfo4() throws Exception {

        final InboundCallMock icm = createCall("sessioninfo_4");
        icm.startCall();
        icm.sleep(5000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(20000);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());

        SessionInfoMock sessionInfo = getSessionInfoMock();

        // We expect play, idle
        List<CallActivity> outboundActivityHistory = sessionInfo.getOutboundActivityHistory();
        if(outboundActivityHistory.size() != 2){
            fail("Unexpected size of outboundActivityHistory:"+outboundActivityHistory.size());
        }
        CallActivity callActivity1 = outboundActivityHistory.get(0);
        CallActivity callActivity2 = outboundActivityHistory.get(1);

        if(! (callActivity1.equals(CallActivity.PLAY) &&
                callActivity2.equals(CallActivity.IDLE))){
            fail("Unexpected call activities: "+callActivity1+":"+callActivity2);
        }

    }

    /**
     * Verify that "inbound activity" is correct if a record is allowed to run to completion
     * @throws Exception
     */
    public void testSessionInfo5() throws Exception {

        final InboundCallMock icm = createCall("sessioninfo_5");
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(20000);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());

        SessionInfoMock sessionInfo = getSessionInfoMock();

        // We expect record, idle
        List<CallActivity> inboundActivityHistory = sessionInfo.getInboundActivityHistory();
        if(inboundActivityHistory.size() != 2){
            fail("Unexpected size of inboundActivityHistory: "+inboundActivityHistory.size());
        }
        CallActivity callActivity1 = inboundActivityHistory.get(0);
        CallActivity callActivity2 = inboundActivityHistory.get(1);

        if(! (callActivity1.equals(CallActivity.RECORD) &&
                callActivity2.equals(CallActivity.IDLE))){
            fail("Unexpected call activities: "+callActivity1+":"+callActivity2);
        }

    }

    /**
     * Verify that "inbound activity" is correct if the call is hungup during a record
     * @throws Exception
     */
    public void testSessionInfo6() throws Exception {

        final InboundCallMock icm = createCall("sessioninfo_6");
        icm.setRecordFinished(20000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);
        icm.startCall();
        icm.sleep(5000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(20000);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());

        SessionInfoMock sessionInfo = getSessionInfoMock();

        // We expect record, idle
        List<CallActivity> inboundActivityHistory = sessionInfo.getInboundActivityHistory();
        if(inboundActivityHistory.size() != 2){
            fail("Unexpected size of inboundActivityHistory:"+inboundActivityHistory.size());
        }
        CallActivity callActivity1 = inboundActivityHistory.get(0);
        CallActivity callActivity2 = inboundActivityHistory.get(1);

        if(! (callActivity1.equals(CallActivity.RECORD) &&
                callActivity2.equals(CallActivity.IDLE))){
            fail("Unexpected call activities: "+callActivity1+":"+callActivity2);
        }

    }

    /**
     * Test that 2 sessionInfos are correctly created in a transfer scenario
     * @throws Exception
     */
    public void testSessionInfo7() throws Exception {

        // We want to find the 2 sessionInfos created in this test case in SessionInfoFactoryMock.returnedSessionInfos,
        // so clear its current content first:
        SessionInfoFactoryMock.reset();

        callType = CallProperties.CallType.VIDEO;
        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 3000);

        try {
            final InboundCallMock icm = createCall("sessioninfo_7");
            icm.startCall();
            icm.sleep(12000);
            icm.disconnectCall();
            boolean exited = icm.waitForExecutionToFinish(40000);

            TestAppender.stopSave(log);
            if (!exited) {
                fail("The application timed out!");
            }


            // Verify that there is one inbound and one outbound sessionInfo. The session Initiator of the outbound one
            // shall be the session ID

            if(SessionInfoFactoryMock.getReturnedSessionInfos().size() != 2){
                fail("Unexpected size of returnedSessionInfos:"+SessionInfoFactoryMock.getReturnedSessionInfos().size());
            }

            Collection<SessionInfo> sessionInfos = SessionInfoFactoryMock.getReturnedSessionInfos().values();
            SessionInfoMock inboundSessionInfo = null;
            SessionInfoMock outboundSessionInfo = null;



            for (SessionInfo sessionInfo : sessionInfos) {
                SessionInfoMock sessionInfoMock = (SessionInfoMock) sessionInfo;
                if(sessionInfoMock.getDirection().getShortInfo().equals("in")){
                    inboundSessionInfo = sessionInfoMock;
                } else {
                    outboundSessionInfo = sessionInfoMock;
                }
            }

            if(inboundSessionInfo == null || outboundSessionInfo == null){
                fail("inboundSessionInfo or outboundSessionInfo were null:"+inboundSessionInfo+outboundSessionInfo);
            }
            if(! outboundSessionInfo.getSessionInit().equals(outboundSessionInfo.getSessionId())){
                fail("The outbound sessionInfo did not have the session id as session initiator:"+outboundSessionInfo.getSessionInit());
            }

            // Verify the output
            List<String> l = TestAppender.getOutputList();
            LogFileExaminer lfe = new LogFileExaminer();
            lfe.add2LevelRequired(".*\\sTCPASS.*");
            lfe.failOnUndefinedErrors();
            lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

            boolean success = lfe.evaluateLogFile(l);
            if (!success) {
                fail(lfe.getReason());
            }
        } finally {
            setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE,0);
        }
    }


    /**
     * Test that far end connection properties for an inbound call are not set at alerting.
     * @throws Exception
     */
    public void testSessionInfo8() throws Exception {

        setInboundFarEndConnections(makeInboundProp());
        try {

            final InboundCallMock icm = createCall("sessioninfo_8");
            icm.startCall();
            boolean exited = icm.waitForExecutionToFinish(40000);

            TestAppender.stopSave(log);
            if (!exited) {
                fail("The application timed out!");
            }

            SessionInfoMock sessionInfo = getSessionInfoMock();
            assertTrue(sessionInfo.getProp().equals(""));

            // Verify the output
            List<String> l = TestAppender.getOutputList();
            LogFileExaminer lfe = new LogFileExaminer();

            lfe.add2LevelRequired(".*\\sTCPASS.*");

            lfe.failOnUndefinedErrors();
            lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

            boolean success = lfe.evaluateLogFile(l);
            if (!success) {
                fail(lfe.getReason());
            }

        }finally {
            setInboundFarEndConnections(makeEmptyInboundProp());
        }
    }

    /**
     * Test that far end connection properties for an inbound call are set at connected.
     * @throws Exception
     */
    public void testSessionInfo9() throws Exception {

        setInboundFarEndConnections(makeInboundProp());

        final InboundCallMock icm = createCall("sessioninfo_9");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        SessionInfoMock sessionInfo = getSessionInfoMock();
        String expectedProp = "RTP 127.0.0.1:1010; SIP 127.0.0.1:5060; ";
        assertTrue(sessionInfo.getProp().equals(expectedProp));

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();


        lfe.add2LevelRequired(".*\\sTCPASS.*");

        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that far end connection properties for an inbound call are set at early media available event.
     * @throws Exception
     */
    public void testSessionInfo10() throws Exception {

        setInboundFarEndConnections(makeInboundProp());

        final InboundCallMock icm = createCall("sessioninfo_10");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        SessionInfoMock sessionInfo = getSessionInfoMock();
        String expectedProp = "RTP 127.0.0.1:1010; SIP 127.0.0.1:5060; ";
        assertTrue(sessionInfo.getProp().equals(expectedProp));

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();


        lfe.add2LevelRequired(".*\\sTCPASS.*");

        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that far end connection properties for an inbound call are not set at early media failed event.
     * @throws Exception
     */

    public void testSessionInfo11() throws Exception {

        super.setResponseToNegotiateEarlyMedia(CallManagerMock.EventType.EARLYMEDIAFAILED_EVENT);
        setInboundFarEndConnections(makeInboundProp());

        final InboundCallMock icm = createCall("sessioninfo_11");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        SessionInfoMock sessionInfo = getSessionInfoMock();
        assertTrue(sessionInfo.getProp().equals(""));

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();


        lfe.add2LevelRequired(".*\\sTCPASS.*");

        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
        super.setResponseToNegotiateEarlyMedia(CallManagerMock.EventType.EARLYMEDIAAVAILABLE_EVENT);

    }

    /**
     * Verify that far end connection properties are set at progressing
     * event with early media.
     * @throws Exception
     */
    public void testSessionInfo12() throws Exception {

        setEarlyMediaAtProgressing(true);
        setOutboundFarEndConnections(makeOutboundProp());

        ServiceRequestRunner r = createServiceRequestRunner("sessioninfo_12");
        r.startCall();
        boolean exited = r.waitForExecutionToFinish(20000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        SessionInfoMock sessionInfo = getSessionInfoMock();
        String expectedProp = "RTP 115.0.0.1:4040; SIP 115.0.0.1:3030; ";

        assertTrue(sessionInfo.getProp().equals(expectedProp));

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();


        lfe.add2LevelRequired(".*\\sTCPASS.*");

        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that far end connection properties are not set at progressing
     * event, when there is no early media.
     * @throws Exception
     */
    public void testSessionInfo13() throws Exception {

        setEarlyMediaAtProgressing(false);
        setOutboundFarEndConnections(makeOutboundProp());

        ServiceRequestRunner r = createServiceRequestRunner("sessioninfo_13");
        r.startCall();
        boolean exited = r.waitForExecutionToFinish(20000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        SessionInfoMock sessionInfo = getSessionInfoMock();

        assertTrue(sessionInfo.getProp().equals(""));

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();


        lfe.add2LevelRequired(".*\\sTCPASS.*");

        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that far end connection properties for an outbound call are set at connected.
     *
     * @throws Exception
     */
    public void testSessionInfo14() throws Exception {

        super.setSendProgressingEvent(false);
        setOutboundFarEndConnections(makeOutboundProp());
        ServiceRequestRunner r = createServiceRequestRunner("sessioninfo_14");
        r.startCall();
        boolean exited = r.waitForExecutionToFinish(20000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        SessionInfoMock sessionInfo = getSessionInfoMock();
        String expectedProp = "RTP 115.0.0.1:4040; SIP 115.0.0.1:3030; ";

        assertTrue(sessionInfo.getProp().equals(expectedProp));

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();


        lfe.add2LevelRequired(".*\\sTCPASS.*");

        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    private Set<Connection> makeInboundProp() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName("127.0.0.1");
        com.mobeon.masp.callmanager.Connection sipConnection = new com.mobeon.masp.callmanager.Connection("SIP", inetAddress, 5060);
        com.mobeon.masp.callmanager.Connection rtpConnection = new com.mobeon.masp.callmanager.Connection("RTP", inetAddress, 1010);
        Set<Connection> farEndConnections = new TreeSet<Connection>();
        farEndConnections.add(sipConnection);
        farEndConnections.add(rtpConnection);
        return farEndConnections;
    }

    private Set<Connection> makeOutboundProp() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName("115.0.0.1");
        com.mobeon.masp.callmanager.Connection sipConnection = new com.mobeon.masp.callmanager.Connection("SIP", inetAddress, 3030);
        com.mobeon.masp.callmanager.Connection rtpConnection = new com.mobeon.masp.callmanager.Connection("RTP", inetAddress, 4040);
        Set<Connection> farEndConnections = new TreeSet<Connection>();
        farEndConnections.add(sipConnection);
        farEndConnections.add(rtpConnection);
        return farEndConnections;
    }

}
