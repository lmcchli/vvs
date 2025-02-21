package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.execution_engine.runapp.mock.*;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.util.test.MASTestSwitches;
import junit.framework.Test;

import jakarta.activation.MimeType;
import java.util.List;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Mar 27, 2006
 * Time: 9:51:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLTransferTagTest extends ApplicationBasicTestCase<ApplicationVXMLTransferTagTest> {

    static {
        testLanguage("vxml");
        testSubdir("transfer");
        testCases(
                testCase("transfer_1"),
                testCase("transfer_2"),
                testCase("transfer_3"),
                testCase("transfer_4"),
                testCase("transfer_5"),
                testCase("transfer_6"),
                testCase("transfer_7"),
                testCase("transfer_8"),
                testCase("transfer_9"),
                testCase("transfer_10"),
                testCase("transfer_11"),
                testCase("transfer_12"),
                testCase("transfer_13"),
                testCase("transfer_14"),
                testCase("transfer_15"),
                testCase("transfer_16"),
                testCase("transfer_17"),
                testCase("transfer_18"),
                testCase("transfer_19"),
                testCase("transfer_20"),
                testCase("transfer_21"),
                testCase("transferaudioexpr"),
                testCase("transferaudioexpr_mo"),
                testCase("transfer_22"),
                testCase("transfer_23"),
                testCase("transfer_24"),
                testCase("transfer_25"),
                testCase("transfer_transfer_properties_1"),
                testCase("transfer_transfer_properties_2"),
                testCase("transfer_transfer_properties_3")
        );
        store(ApplicationVXMLTransferTagTest.class);

    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLTransferTagTest(String event) {
        super (event, "test_log_info.xml");
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLTransferTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLTransferTagTest.class);
    }

    /**
     * Test a "positive" transfer where the callee disconnects.
     *
     * @throws Exception
     */
    public void testVXMLTransferTag1() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 3000);

        final InboundCallMock icm = createCall("transfer_1");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test a "positive" transfer where the caller disconnects.
     *
     * @throws Exception
     */
    public void testVXMLTransferTag2() throws Exception {

        // I had to default the "outboundCallEventAfterConnected" since
        // the value remained when the entire suite was run...
        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 0);

        final InboundCallMock icm = createCall("transfer_2");

        icm.startCall();
        icm.sleep(10000);
        icm.disconnectCall();

        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that it is possible to cancel a transfer by entering DTMF
     *
     * @throws Exception
     */
    public void testVXMLTransferTag3() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 0);
        final InboundCallMock icm = createCall("transfer_3");

        icm.startCall();
        icm.sleep(9000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that it is possible to define a call duration
     * and that this will lead to maxtime_disconnect
     * <p/>
     * Also test that joining back the dialog in the order "dialogId in_connectiondid". We had a bug (TR 27667)
     * saying that this join only works if the order is "in_connectiondid dialogId".
     *
     * @throws Exception
     */
    public void testVXMLTransferTag4() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 0);
        final InboundCallMock icm = createCall("transfer_4");

        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test "busy" scenario
     *
     * @throws Exception
     */
    public void testVXMLTransferTag5() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 0);
        FailedEventInfo failedEventInfo = new FailedEventInfo(FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND,
                "event failed since test environment wants it to fail",
                603); // <--- busy

        setResponseToCreateCall(CallManagerMock.EventType.FAILED_EVENT, failedEventInfo);
        final InboundCallMock icm = createCall("transfer_5");

        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test "noanswer" scenario
     *
     * @throws Exception
     */
    public void testVXMLTransferTag6() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 0);
        FailedEventInfo failedEventInfo = new FailedEventInfo(FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND,
                "event failed since test environment wants it to fail",
                610);  // <---- no answer

        setResponseToCreateCall(CallManagerMock.EventType.FAILED_EVENT, failedEventInfo);
        final InboundCallMock icm = createCall("transfer_6");

        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test "network_busy" scenario
     *
     * @throws Exception
     */
    public void testVXMLTransferTag7() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 0);
        FailedEventInfo failedEventInfo = new FailedEventInfo(FailedEvent.Reason.REJECTED_BY_NEAR_END,
                CallDirection.OUTBOUND,
                "event failed since test environment wants it to fail",
                620);  // <---- network_busy

        setResponseToCreateCall(CallManagerMock.EventType.FAILED_EVENT, failedEventInfo);
        final InboundCallMock icm = createCall("transfer_7");

        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that "destexpr" can be used to specify callee in VXML
     *
     * @throws Exception
     */
    public void testVXMLTransferTag8() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 3000);
        setResponseToCreateCall(CallManagerMock.EventType.CONNECTED_EVENT, null);


        final InboundCallMock icm = createCall("transfer_8");
        icm.startCall();
        icm.sleep(12000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*CALLING:121212.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that a transfer with false "cond" is not executed.
     *
     * @throws Exception
     */
    public void testVXMLTransferTag9() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 3000);

        final InboundCallMock icm = createCall("transfer_9");
        icm.startCall();
        icm.sleep(6000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
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
    }

    /**
     * Test that a transfer with the form item defined using "expr"
     * is not executed.
     *
     * @throws Exception
     */
    public void testVXMLTransferTag10() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 3000);

        final InboundCallMock icm = createCall("transfer_10");
        icm.startCall();
        icm.sleep(6000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
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
    }

    /**
     * Test that a non-matching DTMF has no effect on the transfer. Prompts preceding the transfer shall be aborted (this is normal bargein)
     *
     * @throws Exception
     */
    public void testVXMLTransferTag11() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 3000);

        final InboundCallMock icm = createCall("transfer_11");
        icm.startCall();
        icm.sleep(6000);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 40);
        icm.sleep(5000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*INFO MOCK: OutboundMediaStreamMock\\.cancel.*");
        lfe.add2LevelRequired(".*MOCK: OutboundMediaStreamMock\\.Thread PlayFinishedEvent.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }

    }

    /**
     * Test that maxtime and connecttimeout can be set via properties
     *
     * @throws Exception
     */
    public void testVXMLTransferTag12() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 3000);

        AppConfigurationGroupMock.setParameter("transfer_connecttimeout","6s");
        AppConfigurationGroupMock.setParameter("transfer_maxtime","7s");

        final InboundCallMock icm = createCall("transfer_12");
        icm.startCall();
        icm.sleep(15000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        if (!exited) {
            fail("The application timed out!");
        }
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*CONNECTTIMEOUT:6s.*");
        lfe.add2LevelRequired(".*MAXTIME:7s.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }

        // Start a second call to this application
        // with different values on the parameter
        AppConfigurationGroupMock.setParameter("transfer_connecttimeout","8s");
        AppConfigurationGroupMock.setParameter("transfer_maxtime","9s");

        final InboundCallMock icm2 = createCall("transfer_12");
        icm2.startCall();
        icm2.sleep(15000);
        icm2.disconnectCall();

        exited = icm2.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        l = TestAppender.getOutputList();
        lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*CONNECTTIMEOUT:8s.*");
        lfe.add2LevelRequired(".*MAXTIME:9s.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
        AppConfigurationGroupMock.clear();
    }

    /**
     * Verify that presentation indicatior and calling number can
     * be specified in the application. Also verify that the outbound call
     * is a voice call (same as the inbound call)
     *
     * @throws Exception
     */
    public void testVXMLTransferTag13() throws Exception {

        callType = CallProperties.CallType.VOICE;
        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 3000);

        final InboundCallMock icm = createCall("transfer_13");
        icm.startCall();
        icm.sleep(15000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*CallManagerMock\\.createCall:CallingNumber=8765123.*");
        lfe.add2LevelRequired(".*CallManagerMock\\.createCall:PresentationIndicator=ALLOWED.*");
        lfe.add2LevelRequired(".*CallManagerMock\\.createCall:callType=VOICE.*");

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that it is possible to play using transferaudio,
     * and no other queued prompts.
     *
     * @throws Exception
     */
    public void testVXMLTransferTag14() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 10000);

        final InboundCallMock icm = createCall("transfer_14");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*Entering OutboundMediaStreamMock\\.play.*");
        lfe.add2LevelRequired(".*dialog\\.transfer received.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that it is possible to play using transferaudio,
     * with also one queued prompt.
     *
     * @throws Exception
     */
    public void testVXMLTransferTag15() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 10000);

        final InboundCallMock icm = createCall("transfer_15");
        icm.startCall();
        icm.sleep(20000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*Entering OutboundMediaStreamMock\\.play.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*Entering OutboundMediaStreamMock\\.play.*");
        lfe.add2LevelRequired(".*dialog\\.transfer received.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * If the resource cannot be fetched, the error is ignored and the
     * transfer continues
     *
     * @throws Exception
     */
    public void testVXMLTransferTag16() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 10000);

        final InboundCallMock icm = createCall("transfer_16");
        icm.startCall();
        icm.sleep(20000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*\\sTCPASS.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that if matching DTMF is pressed before the transfer
     * is started, it is not started at all.
     * transfer continues
     *
     * @throws Exception
     */
    public void testVXMLTransferTag17() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 10000);

        final InboundCallMock icm = createCall("transfer_17");
        icm.startCall();


        new Thread() {
            public void run() {
                try {
                    // Give some time to start the call
                    icm.sleep(4000);
                    // Let the play take 8 seconds, this gives us plenty
                    // of time to enter a DTMF during prompt play
                    icm.waitForPlay(8000);

                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();

        // After 4 seconds send a DTMF

        new Thread() {
            public void run() {
                try {
                    // Wait a few ms for the play to start
                    icm.sleep(3000);
                    icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 200);

                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();


        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*OutboundMediaStreamMock\\.cancel.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that presentation indicatior and calling number can
     * be specified in the application (other values than in a testcase above)
     * Also verify that the outbound call
     * is a video call (same as the inbound call)
     *
     * @throws Exception
     */
    public void testVXMLTransferTag18() throws Exception {
        callType = CallProperties.CallType.VIDEO;
        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 3000);

        final InboundCallMock icm = createCall("transfer_18");
        icm.startCall();
        icm.sleep(12000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*CallManagerMock\\.createCall:CallingNumber=334455.*");
        lfe.add2LevelRequired(".*CallManagerMock\\.createCall:PresentationIndicator=RESTRICTED.*");
        lfe.add2LevelRequired(".*CallManagerMock\\.createCall:callType=VIDEO.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that the form item variable is filled also when the call
     * is shorter than the transferaudio (there was a bug such that other test cases worked by coincidence since the
     * playFinished handler set events to "enabled" again)
     *
     * @throws Exception
     */
    public void testVXMLTransferTag19() throws Exception {
        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 0);
        final InboundCallMock icm = createCall("transfer_19");

        icm.startCall();
        icm.sleep(9000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock.*Play.*Finished.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * test that transfer may start also from a subdialog
     *
     * @throws Exception
     */
    public void testVXMLTransferTag20() throws Exception {
        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 0);
        final InboundCallMock icm = createCall("transfer_20");

        icm.startCall();
        icm.sleep(9000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that it is possible to terminate transfer with DTMF even if there was transferaudio played
     * (TR 27704)
     *
     * @throws Exception
     */
    public void testVXMLTransferTag21() throws Exception {
        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 0);
        final InboundCallMock icm = createCall("transfer_21");

        icm.startCall();
        icm.sleep(9000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testVXMLTransferTagAudio_transferaudioexpr() throws Exception {
        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 10000);

        final InboundCallMock icm = createCall("transferaudioexpr");
        icm.startCall();
        icm.sleep(20000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*Entering OutboundMediaStreamMock\\.play.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*Entering OutboundMediaStreamMock\\.play.*");
        lfe.add2LevelRequired(".*dialog\\.transfer received.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }


    public void testVXMLTransferTagAudio_transferaudioexpr_mo() throws Exception {
        rememberMimeType(new MimeType("audio/pcmu"));
        setOutboundCallEventAfterConnected(CallManagerMock.EventType.DISCONNECTED_EVENT, 10000);

        final InboundCallMock icm = createCall("transferaudioexpr_mo");
        icm.startCall();
        icm.sleep(20000);
        icm.disconnectCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*Entering OutboundMediaStreamMock\\.play.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*Entering OutboundMediaStreamMock\\.play.*");
        lfe.add2LevelRequired(".*dialog\\.transfer received.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that it is possible to cancel a transfer by entering DTMF during ongoing transferaudio
     *
     * @throws Exception
     */
    public void testVXMLTransferTag22() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 0);
        final InboundCallMock icm = createCall("transfer_22");

        icm.startCall();
        icm.sleep(9000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*dialog\\.transfer received.*");
        lfe.add2LevelRequired(".*MOCK: OutboundMediaStreamMock\\.cancel.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that a non-matching DTMF during transferaudio does not terminate the transferaudio.
     *
     * @throws Exception
     */
    public void testVXMLTransferTag23() throws Exception {

        setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 0);

        final InboundCallMock icm = createCall("transfer_23");

        icm.startCall();
        icm.sleep(6000);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 40);
        icm.sleep(5000);
        icm.disconnectCall();

        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*InboundCallMock\\.sendDTMF DTMF sent.*");

        // We expect the play to be cancelled at the time of disconnect:
        lfe.add2LevelRequired(".*MOCK: InboundCallMock\\.disconnect.*");
        lfe.add2LevelRequired(".*MOCK: OutboundMediaStreamMock\\.cancel.*");

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that CCXML can define "error.connection.noauthorization" as result of transfer
     * and that VXML can catch it as "error.connection"
     *
     * @throws Exception
     */
    public void testVXMLTransferTag24() throws Exception {

        final InboundCallMock icm = createCall("transfer_24");

        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*error\\.connection.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that CCXML can define "error.unsupported.transfer.bridge " as result of transfer
     * and that VXML can catch it as "error.unsupported"
     *
     * @throws Exception
     */
    public void testVXMLTransferTag25() throws Exception {

        final InboundCallMock icm = createCall("transfer_25");

        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*error\\.unsupported.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that 4 "transfer properties" are correctly sent from VXML to CCXML.
     * @throws Exception
     */
    public void testVXMLTransferTagTransferProperties1() throws Exception {

        final InboundCallMock icm = createCall("transfer_transfer_properties_1");

        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*OUTBOUNDCALLSERVERHOST: 108.109.11.12.*");
        lfe.add2LevelRequired(".*OUTBOUNDCALLSERVERPORT: 1212.*");
        lfe.add2LevelRequired(".*CALLTYPE: video.*");
        lfe.add2LevelRequired(".*NUMBER: 88.*");
        lfe.add2LevelRequired(".*BTC: CallManagerMock.createCall:outboundcallserverhost=108.109.11.12.*");
        lfe.add2LevelRequired(".*BTC: CallManagerMock.createCall:outboundcallserverport=1212.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify a testcase where the "transfer property" is defined but has no sub-properties.
     * @throws Exception
     */
    public void testVXMLTransferTagTransferProperties2() throws Exception {

        final InboundCallMock icm = createCall("transfer_transfer_properties_2");

        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*BTC: CallManagerMock.createCall:outboundcallserverhost=null.*");
        lfe.add2LevelRequired(".*BTC: CallManagerMock.createCall:outboundcallserverport=-1.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify a testcase where the outboundcallserverhost is defined but not the port
     * @throws Exception
     */
    public void testVXMLTransferTagTransferProperties3() throws Exception {

        final InboundCallMock icm = createCall("transfer_transfer_properties_3");

        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(40000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*OUTBOUNDCALLSERVERHOST: 108.109.11.12.*");
        lfe.add2LevelRequired(".*OUTBOUNDCALLSERVERPORT: undefined.*");
        lfe.add2LevelRequired(".*BTC: CallManagerMock.createCall:outboundcallserverhost=108.109.11.12.*");
        lfe.add2LevelRequired(".*BTC: CallManagerMock.createCall:outboundcallserverport=-1.*");

        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

}
