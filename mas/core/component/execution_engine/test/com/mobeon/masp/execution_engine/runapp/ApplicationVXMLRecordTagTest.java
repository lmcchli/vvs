package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.runapp.mock.CallManagerMock;
import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRules;
import com.mobeon.masp.execution_engine.runtime.event.rule.SimpleEventRule;
import com.mobeon.masp.execution_engine.util.TestEvent;
import static com.mobeon.masp.execution_engine.util.TestEvent.*;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.*;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.stream.RecordFinishedEvent;
import com.mobeon.masp.util.test.MASTestSwitches;
import junit.framework.Test;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Testsuite for the record tag.
 *
 * @author Tomas Stenlund
 */
@SuppressWarnings({"MagicNumber"})
public class ApplicationVXMLRecordTagTest extends ApplicationBasicTestCase<ApplicationVXMLRecordTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("record");
        testCases(
                testCase("record_1"),
                testCase("record_2"),
                testCase("record_3"),
                testCase("record_4"),
                testCase("record_5"),
                testCase("record_6"),
                testCase("record_7"),
                testCase("record_8"),
                testCase("record_9"),
                testCase("record_10"),
                testCase("record_11"),
                testCase("record_11b"),
                testCase("record_12"),
                testCase("record_13"),
                testCase("record_14"),
                testCase("record_15"),
                testCase("record_16"),
                testCase("record_17"),
                testCase("record_18"),
                testCase("record_19"),
                testCase("record_20"),
                testCase("record_21"),
                testCase("record_22"),
                testCase("record_23"),
                testCase("record_24"),
                testCase("record_25"),
                testCase("record_26"),
                testCase("record_27"),
                testCase("record_28"),
                testCase("record_29")

        );
        store(ApplicationVXMLRecordTagTest.class);
        //addTestFilter("ApplicationVXMLRecordTagTest,RecordTag19/RecordTag20/RecordTag17");
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLRecordTagTest(String event) {
        // Use only info-logging since running all test cases with debug log takes too much memory...
        super(event, "test_log_info.xml");
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLRecordTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLRecordTagTest.class);
    }

    /**
     * This testcase tries to play a beep.wav with no parameters.
     *
     * @throws Exception
     */


    public void testVXMLRecordTag1() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_1");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();

        // Wait for the call to complete
        boolean exited = icm.waitForExecutionToFinish(30000);
        TestAppender.stopSave(log);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok.*");

        lfe.add2LevelRequired(".*\\srecording duration:\\s" + MASTestSwitches.scale(5000) + ".*");

        // ermkese: for some reason, the media object has size 4.
        // This is very likely to break in the future.
        lfe.add2LevelRequired(".*\\srecording size: .*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * This testcase tries to play a beep.wav with no parameters.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag2() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_2");

        // Send a token, just to test things
        icm.setRecordFinished(3000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();
        boolean played = icm.waitForPlay(8000);
        if (!played)
            fail("The application failed to play");
        played = icm.waitForPlay(8000);
        if (!played)
            fail("The application failed to play");

        // Wait for the call to complete
        boolean exited = icm.waitForExecutionToFinish(5000);
        TestAppender.stopSave(log);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * This testcase tries to play a beep.wav with no parameters.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag3() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_3");

        // Send a token, just to test things
        icm.setRecordFinished(3000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();

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

        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * This testcase tries to play a beep.wav with no parameters.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag4() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_4");

        // Send a token, just to test things
        icm.setRecordFinished(20000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);

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

        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok\\s2.*");
        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok\\s3.*");
        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok\\s4.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that a recording is done only iof cnd is true.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag5() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_5");

        // Send a token, just to test things
        icm.setRecordFinished(20000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);

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

        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord 5.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that a recording is done only iof cnd is true.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag6() throws Exception {

        // Setup the call
        final InboundCallMock icm = createCall("record_6");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();

        new Thread() {
            public void run() {
                try {
                    icm.sleep(2000);
                    icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 500);
                    icm.sleep(6000);
                    icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();

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

        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord 6a.*");
        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord 6b.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that a DTMF during prompt play in a record terminates the record, leaving the record shadowvars
     * undefined. Further test that the recoring may be interrupted by DTMF if the dtmfterm param is true.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag7() throws Exception {

        // Setup the call
        final InboundCallMock icm = createCall("record_7");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();

        new Thread() {
            public void run() {
                try {
                    icm.sleep(2000);
                    icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 500);
                    icm.sleep(6000);
                    icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();

        icm.waitForPlay(5000);

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

        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord 7.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * This testcase tries to play a beep.wav with no parameters.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag8() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_8");

        // Send a token, just to test things
        icm.setRecordFinished(3000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();

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

        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * This testcase checks that a recording is made if EE
     * receives "disconnect" first and then "record finished".
     *
     * @throws Exception
     */
    public void testVXMLRecordTag9() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_9");

        // Send a token, just to test things
        icm.setRecordFinished(20000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
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

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add2LevelRequired(".*\\sexiting correctly.*");


        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * verify that record shadow variables are set when recording is stopped
     * using DTMF
     *
     * @throws Exception
     */
    public void testVXMLRecordTag10() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_10");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();
        icm.sleep(2000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);

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

        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok.*");

        lfe.add2LevelRequired(".*\\srecording duration:\\s" + MASTestSwitches.scale(5000) + ".*");

        lfe.add2LevelRequired(".*\\stermchar: 1.*");


        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that a non-matching DTMF does not terminate an ongoing record.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag11() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_11");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();
        icm.sleep(1000);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 500);
        icm.sleep(1000);
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

        lfe.add2LevelRequired(".*In catch OK.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Similar to testVXMLRecordTag11  but verifies that a matching DTMF DOES  terminate an ongoing record.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag11b() throws Exception {

        declareWait(ACCEPT);
        // Setup the call
        InboundCallMock icm = createCall("record_11");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();
        waitFor(ACCEPT, 5000);
        declareNoWait();
        icm.sleep(1000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
        icm.sleep(3000);
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

        lfe.add2LevelRequired(".*In filled OK.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that if we start a record, no noinput-timeout will be triggered.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag12() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_12");

        // Send a token, just to test things
        icm.setRecordFinished(8000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
        icm.sleep(3000);
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

        lfe.add2LevelRequired(".*In filled OK.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that no no-input timeout is generated even if the
     * timeout property is set to 0. (TR 27519).
     *
     * @throws Exception
     */
    public void testVXMLRecordTag13() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_13");

        // Send a token, just to test things
        icm.setRecordFinished(8000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();
        icm.sleep(8000);
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
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that it is possible to record using type "audio/*"
     *
     * @throws Exception
     */
    public void testVXMLRecordTag14() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_14");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();

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
        lfe.add2LevelRequired(".*InboundMediaStreamMock.record type:AUDIO.*");
        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that it is possible to record using type "video/*"
     *
     * @throws Exception
     */
    public void testVXMLRecordTag15() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_15");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();

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
        lfe.add2LevelRequired(".*InboundMediaStreamMock.record type:VIDEO.*");
        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * TR XYXXX: after a finished record the interpreter remained in waiting state
     * and thus a hangup event got caught too early (shall be buffered until waiting state)
     */
    public void testVXMLRecordTag16() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_16");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();

        // Disconnect the call after a while
        icm.sleep(6000);
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
        lfe.add2LevelRequired(".*\\sTCPASS1.*");
        lfe.add2LevelRequired(".*\\sTCPASS2.*");
        lfe.add2LevelRequired(".*\\sTCPASS3.*");
        lfe.add2LevelRequired(".*\\sTCPASS4.*");

        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * DTMF entered during a prompt in &lt;record&gt; shall terminate the
     * recording without any event and the recording variable shall remain unfilled.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag17() throws Exception {


        declareWait(PLAY_STARTED, ACCEPT);
        // Setup the call
        InboundCallMock icm = createCall("record_17");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();
        waitFor(ACCEPT, 5000);

        // Wait for prompts to start playing
        waitFor(PLAY_STARTED, 5000);
        declareNoWait(PLAY_STARTED);

        //Sleep a little
        icm.sleep(1000);

        //Cancel recording with DTMF while prompt still playing
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);

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
        lfe.add2LevelRequired(".*\\sTCPOINT_BLOCK.*");
        lfe.add2LevelRequired(".*\\sTCPOINT_UNDEFINED.*");

        lfe.add3LevelFailureTrigger(".*\\sTCPOINT.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    static abstract class TestMethod {

        abstract void run() throws Exception;

        abstract void testRunning(InboundCallMock icm) throws Exception;

        abstract void declare();

        abstract void examineLog(LogFileExaminer lfe);

    }


    private class VXMLRecordTag17_2 extends RecordTestMethod {

        public VXMLRecordTag17_2() {
            super("record_17");
        }

        void testRunning(InboundCallMock icm) throws Exception {
            waitFor(ACCEPT, 5000);

            // Wait for prompts to start playing
            waitFor(RECORD_STARTED, 7000);
            declareNoWait(RECORD_STARTED);

            //Sleep a little
            icm.sleep(1000);

            //Cancel recording with DTMF after record has started
            icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
        }

        void declare() {
            declareWait(RECORD_STARTED, ACCEPT);
        }

        void examineLog(LogFileExaminer lfe) {
            lfe.add2LevelRequired(".*\\sTCPOINT_FILLED.*");
            lfe.add2LevelRequired(".*\\sTCPOINT_BLOCK.*");
            lfe.add2LevelRequired(".*\\sTCPOINT_DEFINED.*");

            lfe.add3LevelFailureTrigger(".*\\sTCPOINT.*");
        }
    }

    /**
     * DTMF entered during record if  input has been recorded shall terminate the
     * recording without filled being thrown. Note that since we have no silence
     * detection, this voice browser simply assumes that record has been recorded
     * as soon as the recording has started.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag17_2() throws Exception {
        new VXMLRecordTag17_2().run();
    }

    /**
     * Test that if the result of record is recdFailed, we will generate a noinput event
     *
     * @throws Exception
     */
    public void testVXMLRecordTag18() throws Exception {


        setResponseToRecord(CallManagerMock.EventType.RECORD_FAILED);
        // Setup the call
        InboundCallMock icm = createCall("record_18");

        // Start the call
        icm.startCall();
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

        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
        setResponseToRecord(CallManagerMock.EventType.RECORD_FINISHED);
    }

    /**
     * test that if the call gets disconnected, but the CCXML application has
     * not yet sent the hangup event because it's doing work in a transition,
     * doing a record will result in that we wait for the hangup event,
     * regardless of noinput timeout.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag19_1() throws Exception {
        declareWait(TestEvent.ACCEPT, TestEvent.ECMA_VAR, TestEvent.CCXML_MODULE_STARTED);

        // Setup the call
        InboundCallMock icm = createCall("record_19");
        // Start the call
        icm.startCall();
        final CCXMLExecutionContext ec = (CCXMLExecutionContext) waitFor(TestEvent.CCXML_MODULE_STARTED, 5000);
        waitFor(TestEvent.ECMA_VAR, 5000);
        waitFor(TestEvent.ECMA_VAR, 5000);
        declareNoWait(TestEvent.ECMA_VAR);
        ec.getEngine().runOnce(new Callable() {
            public Object call() throws Exception {
                ec.getCurrentScope().setValue("subcase", 1);
                return null;
            }
        });
        waitFor(TestEvent.ACCEPT, 8000);
        declareNoWait();

        icm.sleep(3000);
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
        lfe.add2LevelRequired(TestEvent.FINAL_PROCESSING_STATE);
        lfe.add2LevelRequired(".*\\sTCPOINT_CATCHALL.*");
        lfe.add2LevelRequired(".*\\sTCPOINT_DIALOG_EXIT.*");
        lfe.add3LevelFailureTrigger(".*\\STCPOINT");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * test that if the call gets disconnected, but the CCXML application has
     * not yet sent the hangup event, doing a record will result in a NOINPUT event.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag19_2() throws Exception {

        declareWait(
                TestEvent.ACCEPT,
                TestEvent.RECORD_STARTING,
                TestEvent.PROMPT_QUEUE_PLAY,
                TestEvent.ECMA_VAR,
                TestEvent.CCXML_MODULE_STARTED);

        // Setup the call
        InboundCallMock icm = createCall("record_19");
        // Start the call
        icm.startCall();
        final CCXMLExecutionContext ec = (CCXMLExecutionContext) waitFor(TestEvent.CCXML_MODULE_STARTED, 5000);
        waitFor(TestEvent.ECMA_VAR, 5000);
        waitFor(TestEvent.ECMA_VAR, 5000);
        declareNoWait(TestEvent.ECMA_VAR);
        ec.getEngine().runOnce(new Callable() {
            public Object call() throws Exception {
                ec.getCurrentScope().setValue("subcase", 2);
                return null;
            }
        });
        waitFor(TestEvent.ACCEPT, 8000);

        waitFor(TestEvent.PROMPT_QUEUE_PLAY, 5000);
        icm.disconnectCall();
        while (ec.getCurrentConnection().getState() == Connection.State.CONNECTED) icm.sleep(100);
        waitFor(TestEvent.RECORD_STARTING, 5000);
        declareNoWait();
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
        lfe.add2LevelRequired(".*\\sTCPOINT_NOINPUT.*");
        lfe.add2LevelRequired(".*\\sTCPOINT_HANGUP.*");
        lfe.add2LevelRequired(".*\\sTCPOINT_DIALOG_EXIT.*");
        lfe.add3LevelFailureTrigger(".*\\STCPOINT");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that if the call is hung up but the hangup event to VXML is delayed,
     * doing a number of record operations shall each and everyone result in a noinput event
     * until the hangup event is eventually delivered, at which point the hangup
     * handler shall be allowed to run to completion.
     * <p/>
     * NOTE: this test case recreated a bug but before the corection, it only failed
     * like 5% of the cases since it is depending on thread scheduling and arrival
     * time of hangup-event and noinput-event. However, now as this bug
     * is fixed it should never fail.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag20() throws Exception {
        // Setup the call
        declareWait(TestEvent.ACCEPT);
        InboundCallMock icm = createCall("record_20");

        // Start the call
        icm.startCall();
        waitFor(TestEvent.ACCEPT, 5000);
        icm.sleep(1000);
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
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Delay a playFinished and go to another form after record, this shall work without errors.
     * (before the execution continued too fast and the play finished could be delivered
     * to the second form)
     *
     * @throws Exception
     */
    public void testVXMLRecordTag21() throws Exception {

        setDelayBeforeResponseToPlay(1500);
        // Setup the call
        InboundCallMock icm = createCall("record_21");
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();
        icm.sleep(3000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);

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
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
        setDelayBeforeResponseToPlay(0);
    }

    /**
     * Test that it is possible to record using type "audio/*" set by property
     *
     * @throws Exception
     */
    public void testVXMLRecordTag22() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_22");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();

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
        lfe.add2LevelRequired(".*InboundMediaStreamMock.record type:AUDIO.*");
        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that it is possible to record using type "video/*" set by property
     *
     * @throws Exception
     */
    public void testVXMLRecordTag23() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_23");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();

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
        lfe.add2LevelRequired(".*InboundMediaStreamMock.record type:VIDEO.*");
        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * verify that if recording type is not set with neither attribute nor property the type will be "unknown"
     *
     * @throws Exception
     */
    public void testVXMLRecordTag24() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_24");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();

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
        lfe.add2LevelRequired(".*InboundMediaStreamMock.record type:UNKNOWN.*");
        lfe.add2LevelRequired(".*\\sTCPASS:\\srecord\\sok.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that if the recording has a grammar expecting "#" and "2#" is entered, the
     * "2" is discarded and the recording stops when "#" is entered. (We used to have a bug
     * regarding this, TR 28702)
     *
     * @throws Exception
     */
    public void testVXMLRecordTag25() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_25");

        // Send a token, just to test things
        icm.setRecordFinished(7000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 500);
        icm.sleep(500);
        icm.sendDTMF(ControlToken.DTMFToken.HASH, 100, 500);

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
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");


        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that entering "2" in a grammar requiring "#" does not terminate the recording, rather
     * the recording is stopped due to that maxtime of recording is reached.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag26() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_26");

        // Send a token, just to test things
        icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.MAX_RECORDING_DURATION_REACHED);

        // Start the call
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 500);

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
        //MOCK: InboundMediaStreamMock.cancel
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add1LevelFailureTrigger(".*MOCK: InboundMediaStreamMock\\.cancel.*");


        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that if the recording has a grammar expecting "2#" and "242#" is entered, the
     * "24" is discarded and the recording stops when "2#" is entered.
     *
     * @throws Exception
     */
    public void testVXMLRecordTag27() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_27");

        // Send a token, just to test things
        icm.setRecordFinished(7000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

        // Start the call
        icm.startCall();
        icm.sleep(5000);

        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 500);
        icm.sleep(500);
        icm.sendDTMF(ControlToken.DTMFToken.FOUR, 100, 500);
        icm.sleep(500);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 500);
        icm.sleep(500);
        icm.sendDTMF(ControlToken.DTMFToken.HASH, 100, 500);

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
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");


        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * TR 29611. There was a bug such that if a record was terminated by hanging up,
     * a detour intended to wait for connection.disconnect.hangup was executed too late, and hence tried to detour
     * the next event (in this test case this event is called "goodbye")
     *
     * @throws Exception
     */
    public void testVXMLRecordTag28() throws Exception {

        // The fault was a sporadic race condition, try a number of times
        for (int i = 0; i < 20; i++) {
            // Setup the call
            final InboundCallMock icm = createCall("record_28");

            icm.setRecordFinished(10000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

            // When record starts, sleep a while and disconnect the call
            icm.invokeWhenRecord(
                    new Callable() {
                        public Object call() throws Exception {
                            new Thread() {
                                public void run() {
                                    try {
                                        icm.sleep(1000);
                                        icm.disconnectCall();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    }
                                }
                            }.start();
                            return null;
                        }
                    });

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
            lfe.add2LevelRequired(".*MOCK: InboundMediaStreamMock\\.record.*");
            lfe.add2LevelRequired(".*TCPASS.*");
            lfe.add1LevelFailureTrigger(".*TCFAIL.*");


            boolean success = lfe.evaluateLogFile(l);
            if (!success) {
                fail(lfe.getReason());
                return;
            }
        }
    }

    public void testVXMLRecordTag29() throws Exception {

        // Setup the call
        InboundCallMock icm = createCall("record_29");

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
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");


        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    abstract class RecordTestMethod extends TestMethod {

        String service;

        protected RecordTestMethod(String service) {
            this.service = service;
        }

        void run() throws Exception {
            declare();
            // Setup the call
            InboundCallMock icm = createCall(service);

            // Send a token, just to test things
            icm.setRecordFinished(5000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);

            // Start the call
            icm.startCall();
            testRunning(icm);

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
            examineLog(lfe);
            boolean success = lfe.evaluateLogFile(l);
            if (!success) {
                fail(lfe.getReason());
            }

        }

    }

}
