package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.declareWait;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.waitFor;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.declareNoWait;
import static com.mobeon.masp.execution_engine.util.TestEvent.PLAY_STARTED;
import static com.mobeon.masp.execution_engine.util.TestEvent.ACCEPT;
import static com.mobeon.masp.execution_engine.util.TestEvent.VXML_MODULE_STARTED;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.stream.RecordFinishedEvent;
import junit.framework.Test;

import jakarta.activation.MimeType;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Test suite for the VXML prompt tag.
 *
 * @author Tomas Stenlund
 */

public class ApplicationVXMLPromptTagTest extends ApplicationBasicTestCase<ApplicationVXMLPromptTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("prompt");
        testCases(
                testCase("prompt_audio_1"),
                testCase("prompt_audio_2"),
                testCase("prompt_audio_3"),
                testCase("prompt_audio_4"),
                testCase("prompt_audio_5"),
                testCase("prompt_audio_6"),
                testCase("prompt_audio_7"),
                testCase("prompt_audio_8"),
                testCase("prompt_audio_9"),
                testCase("prompt_audio_10"),
                testCase("prompt_audio_11"),
                testCase("prompt_1"),
                testCase("prompt_2"),
                testCase("prompt_3"),
                testCase("prompt_4"),
                testCase("prompt_5"),
                testCase("prompt_6"),
                testCase("prompt_7"),
                testCase("prompt_8"),
                testCase("prompt_9"),
                testCase("prompt_10"),
                testCase("prompt_11"),
                testCase("prompt_12"),
                testCase("prompt_13"),
                testCase("prompt_14"),
                testCase("prompt_15"),
                testCase("prompt_16"),
                testCase("prompt_17"),
                testCase("prompt_18"),
                testCase("prompt_19"),
                testCase("prompt_20"),
                testCase("prompt_21"),
                testCase("prompt_22")
        );
        store(ApplicationVXMLPromptTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLPromptTagTest(String event) {
        // Use only info-logging since running all test cases with debug log takes too much memory...
        super(event, "test_log_info.xml");
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLPromptTagTest.class);
    }


    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLPromptTagTest.class);

    }

    /**
     * This testcase tries to play a beep.wav with no parameters.
     *
     * @throws Exception
     */
    public void testVXMLPromptTagAudio1() throws Exception {

        // Setup the call
        TestAppender.clear();
        InboundCallMock icm = createCall("prompt_audio_1");
        icm.startCall();
        icm.waitForPlay(20000);
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play\\sPlayOption\\s0.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play\\sCursor\\s0.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play\\sSize\\s128958.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play\\sPlayjob\\sstarted.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * This testcase tries to play to consecutive beep.wav with no parameters.
     *
     * @throws Exception
     */
    public void testVXMLPromptTagAudio2() throws Exception {

        // Setup the call
        TestAppender.clear();
        InboundCallMock icm = createCall("prompt_audio_2");
        icm.startCall();

        // Send a tokenn, just to test things
        icm.waitForPlay(20000);
        icm.waitForPlay(20000);

        // Wait for the call to complete
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock.play\\sPlayOption\\s0.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock.play\\sCursor\\s0.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock.play\\sSize\\s128958.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play\\sPlayjob\\sstarted.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock.play\\sPlayOption\\s0.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock.play\\sCursor\\s0.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock.play\\sSize\\s128958.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.play\\sPlayjob\\sstarted.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify:
     * "Exactly one of "src" or "expr" must be specified; otherwise,
     * an error.badfetch event is thrown"
     *
     * @throws Exception
     */
    public void testVXMLPromptTagAudio3() throws Exception {

        // Setup the call
        TestAppender.clear();
        String testCase = "prompt_audio_3";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARNING.*");
        lfe.add2LevelRequired(".*\\spass.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Retrieves and plays a media object from the system resourcfes.
     *
     * @throws Exception
     */
    public void testVXMLPromptTagAudio5() throws Exception {

        // Setup the call
        TestAppender.clear();

        rememberMimeType(new MimeType("audio/pcmu"));
        final InboundCallMock icm = createCall("prompt_audio_5");
        icm.startCall();

        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARNING.*");
        lfe.add3LevelFailureTrigger(".*\\sMOCK:\\sOutboundMediaStreamMock.play.*multiple\\sMediaObjects\\sprovided.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Retrieves and plays a media object from the system resources, this plays a
     * NULL media object.
     *
     * @throws Exception
     */
    public void testVXMLPromptTagAudio6() throws Exception {

        // Setup the call
        TestAppender.clear();
        rememberMimeType(new MimeType("audio/pcmu"));
        final InboundCallMock icm = createCall("prompt_audio_6");
        icm.startCall();

        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARNING.*");
        lfe.add3LevelFailureTrigger(".*\\sMOCK:\\sOutboundMediaStreamMock.play.*multiple\\sMediaObjects\\sprovided.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Retrieves and plays a media object from the system resources, this plays a
     * an array with no media objects.
     *
     * @throws Exception
     */
    public void testVXMLPromptTagAudio7() throws Exception {

        // Setup the call
        TestAppender.clear();
        rememberMimeType(new MimeType("audio/pcmu"));
        final InboundCallMock icm = createCall("prompt_audio_7");
        icm.startCall();

        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARNING.*");
        lfe.add3LevelFailureTrigger(".*\\sMOCK:\\sOutboundMediaStreamMock.play.*multiple\\sMediaObjects\\sprovided.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that the bargin attribute works.
     *
     * @throws Exception
     */
    public void testVXMLPromptTagAudio8() throws Exception {

        // Setup the call
        TestAppender.clear();
        final InboundCallMock icm = createCall("prompt_audio_8");
        icm.startCall();
        // Wait for the call to be loaded/compiled
        icm.sleep(2000);
        new Thread() {

            public void run() {

                try {
                    // Wait a few ms for the play to start
                    icm.sleep(500);
                    icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 200);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();

        icm.waitForPlay(4100);

        new Thread() {
            public void run() {
                try {
                    // Wait a few ms for the play to start
                    icm.sleep(4000);
                    icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 200);

                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();

        icm.waitForPlay(4100);

        boolean exited = icm.waitForExecutionToFinish(10000);

        if (!exited) {
            fail("The application timed out!");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARNING.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*\\sTCPASS: OK f1.*");
        lfe.add2LevelRequired(".*\\sMOCK:\\sOutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*\\sTCPASS: OK f2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that the bargin attribute works when queueing multimple prompts with different bargein values.
     *
     * @throws Exception
     */
    public void testVXMLPromptTagAudio9() throws Exception {

        // Setup the call
        TestAppender.clear();
        final InboundCallMock icm = createCall("prompt_audio_9");
        icm.startCall();
        // Wait for the call to be loaded/compiled
        icm.sleep(2000);
        new Thread() {
            public void run() {
                try {
                    // Wait a fe
                    icm.sleep(5500);
                    icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 200);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();

        icm.waitForPlay(4100);
        icm.waitForPlay(4100);
        icm.waitForPlay(4100);


        boolean exited = icm.waitForExecutionToFinish(20000);

        if (!exited) {
            fail("The application timed out!");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARNING.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.Thread PlayFinishedEvent.*PLAY_FINISHED.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.Thread PlayFinishedEvent.*PLAY_FINISHED.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.Thread PlayFinishedEvent.*PLAY_FINISHED.*");
        lfe.add2LevelRequired(".*\\sTCPASS: OK f1.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }


    /**
     * Verify that the com.mobon.platform.audio_offset property works.
     *
     * @throws Exception
     */
    public void testVXMLPromptTagAudio10() throws Exception {

        // Setup the call
        TestAppender.clear();
        final InboundCallMock icm = createCall("prompt_audio_10");
        icm.startCall();
        icm.waitForPlay(4100);

        boolean exited = icm.waitForExecutionToFinish(20000);

        if (!exited) {
            fail("The application timed out!");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARNING.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.play Cursor 3000.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.Thread PlayFinishedEvent.*PLAY_FINISHED.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that the execution enginge waits for play.finished when an prompt is interrupted.
     *
     * @throws Exception
     */
    public void testVXMLPromptTagAudio11() throws Exception {

        // Setup the call
        TestAppender.clear();
        final InboundCallMock icm = createCall("prompt_audio_11");
        icm.startCall();
        new Thread() {
            public void run() {
                try {
                    // Wait for the prompt to start playing
                    icm.sleep(2000);
                    icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();
        icm.waitForPlay(4100);


        boolean exited = icm.waitForExecutionToFinish(20000);

        if (!exited) {
            fail("The application timed out!");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARNING.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.cancel.*");
        lfe.add2LevelRequired(".*PlayFinishedEvent\\(PLAY_CANCELLED\\).*");
        lfe.add2LevelRequired(".*\\sTCPASS: OK.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Tests the conditional parameter on a prompt
     *
     * @throws Exception
     */
    public void testVXMLPromptTag1() throws Exception {

        // Setup the call
        TestAppender.clear();
        InboundCallMock icm = createCall("prompt_1");
        icm.startCall();

        // Send a tokenn, just to test things
        //icm.waitForPlay (20000);
        //icm.waitForPlay (20000);
        //icm.sendDTMF (ControlToken.DTMFToken.ONE, 100, 200);

        // Wait for the call to complete
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sPrompt\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sMOCK:\\sOutboundMediaStreamMock.play.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Prompt with bargein set as true.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag2() throws Exception {

        // Setup the call
        TestAppender.clear();
        final InboundCallMock icm = createCall("prompt_2");

        icm.waitForPlay(5000);
        new Thread() {
            public void run() {
                try {
                    icm.sleep(1000);
                    // The first DTMF should be discarded, since bargein == false
                    icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();

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
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML:\\sPrompt\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Testing reprompt.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag3() throws Exception {

        TestAppender.clear();
        InboundCallMock icm = createCall("prompt_3");
        icm.startCall();
        icm.waitForPlay(3000);
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.HASH, 100, 500);
        icm.waitForPlay(3000);
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML:\\sPrompt\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Testing reprompt.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag4() throws Exception {

        final InboundCallMock icm = createCall("prompt_4");
        icm.startCall();
        new Thread() {
            public void run() {
                try {
                    icm.sleep(2000);
                    // The first DTMF should be discarded, since bargein == false
                    icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 500);
                    icm.sleep(5000);
                    icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
                    icm.sleep(3500);
                    icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();
        icm.waitForPlay(5000);
        // There should be one reprompt
        icm.waitForPlay(5000);

        icm.waitForPlay(3000);
        icm.waitForPlay(3000);


        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML:\\sPrompt\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Testing reprompt.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag5() throws Exception {

        final InboundCallMock icm = createCall("prompt_5");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(60000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML:\\sPrompt\\s5\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Testing prompt queueing.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag6() throws Exception {

        final InboundCallMock icm = createCall("prompt_6");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(60000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");
        lfe.add2LevelRequired(".*Soon exiting.*");
        lfe.add2LevelRequired(".*In block 1.*");
        lfe.add2LevelRequired(".*In block 2.*");
        lfe.add2LevelRequired(".*In field 1.*");
        lfe.add2LevelRequired(".*In field 2.*");


        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Testing prompt queueing, and prompt in <if>.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag7() throws Exception {

        final InboundCallMock icm = createCall("prompt_7");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(60000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");
        lfe.add2LevelRequired(".*\\sTCPASS VXML:\\sPrompt\\sOK.*");
        lfe.add2LevelRequired(".*In true.*");
        lfe.add2LevelRequired(".*In Catch.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test that ECMA in a prompt is evaluated when the prompt is
     * queued.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag8() throws Exception {

        final InboundCallMock icm = createCall("prompt_8");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(60000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");
        lfe.add2LevelRequired(".*The value is 5.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sPrompt\\sOK.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * This testcase verifies the order of played prompts in the block and field of
     * a form.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag9() throws Exception {

        final InboundCallMock icm = createCall("prompt_9");
        icm.startCall();
        icm.sleep(2000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 100);
        boolean exited = icm.waitForExecutionToFinish(5000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");

        lfe.add2LevelRequired(".*This is prompt 1.*");
        lfe.add2LevelRequired(".*This is prompt 2.*");
        lfe.add2LevelRequired(".*This is prompt 3.*");
        lfe.add2LevelRequired(".*This is prompt 4.*");
        lfe.add2LevelRequired(".*This is prompt 5.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Check that if Stream throws exception at play,
     * EE will not die.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag10() throws Exception {

        final InboundCallMock icm = createCall("prompt_10");
        icm.setThrowIllegalArgumentAtPlay(true);
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 100);
        boolean exited = icm.waitForExecutionToFinish(5000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");

        lfe.add2LevelRequired(".*Prompt ok.*");
        lfe.add2LevelRequired(".*MOCK: OutboundMediaStreamMock.play: Throwing in play.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that queing prompts in 3 forms will cause the prompts
     * to be played at <field> in the third form.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag11() throws Exception {

        final InboundCallMock icm = createCall("prompt_11");
        icm.startCall();
        icm.sleep(2000);
        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");

        lfe.add2LevelRequired(".*About to play.*");
        lfe.add2LevelRequired(".*INFO.*Prompt from form1.*");
        lfe.add2LevelRequired(".*INFO.*Prompt from form2.*");
        lfe.add2LevelRequired(".*INFO.*Prompt from form3.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that queuing prompts from a form in one document, and then queing
     * a prompt from another document, will cause the prompts
     * to be played at <field> in the second document.
     * The transition is done with <goto>
     *
     * @throws Exception
     */
    public void testVXMLPromptTag12() throws Exception {

        final InboundCallMock icm = createCall("prompt_12");
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 100);
        boolean exited = icm.waitForExecutionToFinish(5000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");

        lfe.add2LevelRequired(".*About to play.*");
        lfe.add2LevelRequired(".*INFO.*Prompt from form1.*");
        lfe.add2LevelRequired(".*INFO.*Prompt from form2.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that queuing prompts from a form in one document, and then queing
     * a prompt from another document, will cause the prompts
     * to be played at <field> in the second document.
     * The transition is done with <subdialog>
     *
     * @throws Exception
     */
    public void testVXMLPromptTag13() throws Exception {

        final InboundCallMock icm = createCall("prompt_13");
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 100);
        boolean exited = icm.waitForExecutionToFinish(30000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");

        lfe.add2LevelRequired(".*About to play.*");
        lfe.add2LevelRequired(".*INFO.*Prompt from form1.*");
        lfe.add2LevelRequired(".*INFO.*Prompt from form2.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Hanging up, when playing a prompt in a record
     */
    public void testVXMLPromptTag14() throws Exception {

        final InboundCallMock icm = createCall("prompt_14");
        icm.startCall();

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

        icm.waitForPlay(4000);
        icm.setRecordFinished(4000, RecordFinishedEvent.CAUSE.RECORDING_STOPPED);


        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");

        lfe.add2LevelRequired(".*\\sTCPASS.*");

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Test of queueing of conditional prompt
     */
    public void testVXMLPromptTag15() throws Exception {

        final InboundCallMock icm = createCall("prompt_15");
        icm.startCall();

        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");

        lfe.add2LevelRequired(".*INFO .*The first prompt should be played.*");
        lfe.add2LevelRequired(".*INFO .*The third prompt should be played.*");

        lfe.add3LevelFailureTrigger(".*INFO :*The second prompt should not be played.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that is is possible to play prompts
     * from a subdialog.
     * ermkese wrote this test case since
     * there was a bug where a SuperVision object
     * was not copied to the the "new execution context" caused by the
     * subdialog and therefore we had a null pointer exception.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag16() throws Exception {

        final InboundCallMock icm = createCall("prompt_16");
        icm.startCall();

        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add1LevelFailureTrigger(".*ERROR.*");
        lfe.add1LevelFailureTrigger(".*WARN.*");
        lfe.add2LevelRequired(".*This is a prompt.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that if DTMF is buffered while transitiioning to a field, the prompts are not played if
     * bargein=true
     *
     * @throws Exception
     */
    public void testVXMLPromptTag17() throws Exception {

        declareWait(VXML_MODULE_STARTED);

        final InboundCallMock icm = createCall("prompt_17");
        icm.startCall();

        waitFor(VXML_MODULE_STARTED, 5000);
        declareNoWait(VXML_MODULE_STARTED);

        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 200);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 200);

        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*MOCK: OutboundMediaStreamMock\\.play.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that it is possible to bargein a long prompt
     *
     * @throws Exception
     */
    public void testVXMLPromptTag18() throws Exception {

        final InboundCallMock icm = createCall("prompt_18");
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 200);

        boolean exited = icm.waitForExecutionToFinish(10000);
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
     * TR 28897. Verify that barging in with "*1" does not result in a warning log.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag19() throws Exception {

        final InboundCallMock icm = createCall("prompt_19");
        setDelayBeforeResponseToPlay(2000);
        icm.startCall();
        icm.sleep(3000);
        icm.sendDTMF(ControlToken.DTMFToken.STAR, 100, 200);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 200);

        boolean exited = icm.waitForExecutionToFinish(10000);
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
     * There was a scenario when a prompt together with a DTMF
     * could be considered a bargein and EE cancelled the playing, and hence hung
     * waiting for a playFinished never happening..
     *
     * @throws Exception
     */

    public void testVXMLPromptTag20() throws Exception {

        // The fault was sporadic, so we try a few times
        for (int i = 0; i < 10; i++) {
            final InboundCallMock icm = createCall("prompt_20");

            // We play a media object and use this to send a DTMF as soon as the play is finished

            icm.invokeWhenPlayFinished(
                    new Callable() {
                        public Object call() throws Exception {
                            icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 200);
                            return null;
                        }
                    });

            icm.startCall();
            boolean exited = icm.waitForExecutionToFinish(10000);
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
    }

    /**
     * TR 29876. Verify that it is possible to play an array
     * of media objects where the array was create in ECMA.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag21() throws Exception {

        final InboundCallMock icm = createCall("prompt_21");
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*OutboundMediaStreamMock.Thread PlayFinishedEvent.*PLAY_FINISHED.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * TR 29574
     * A play.failed could be lingering and arrive to a form not ready for it, leading
     * to error log.
     *
     * @throws Exception
     */
    public void testVXMLPromptTag22() throws Exception {

        super.setSendPlayFailedAfterDelay(true);
        super.setDelayBeforePlayFailed(3000);

        declareWait(TestEvent.PROMPT_QUEUE_PLAY);

        final InboundCallMock icm = createCall("prompt_22");
        icm.startCall();
        waitFor(TestEvent.PROMPT_QUEUE_PLAY, 5000);
        declareNoWait(TestEvent.PROMPT_QUEUE_PLAY);
        icm.disconnectCall();

        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }

        super.setSendPlayFailedAfterDelay(false);
        super.setDelayBeforePlayFailed(0);
    }

}
