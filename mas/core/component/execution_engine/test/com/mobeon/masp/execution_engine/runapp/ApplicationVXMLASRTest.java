package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;
import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.execution_engine.runapp.mock.MediaTranslationManagerMock;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.*;
import static com.mobeon.masp.execution_engine.util.TestEvent.*;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermteri
 * Date: 2006-aug-17
 * Time: 12:56:52
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLASRTest extends ApplicationBasicTestCase<ApplicationVXMLASRTest> {
    static {
        testLanguage("vxml");
        testSubdir("asr");
        testCases(
                testCase("asr_1"),
                testCase("asr_2")

        );
        store(ApplicationVXMLASRTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLASRTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLASRTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLASRTest.class);
    }

    /**
     * TR 28367. Verify that if mediatranslationmanager throws an IllegalStateException,
     * the application will receive an error-noresource event
     *
     * @throws Exception
     */
    public void testVXMLASR1() throws Exception {
        try {
            MediaTranslationManagerMock.illegalExceptionAtOpen = true;

            final InboundCallMock icm = createCall("asr_1");
            icm.startCall();
            boolean exited = icm.waitForExecutionToFinish(20000);

            TestAppender.stopSave(log);
            if (!exited) {
                fail("The application timed out!");
            }

            // Verify the output
            List<String> l = TestAppender.getOutputList();
            LogFileExaminer lfe = new LogFileExaminer();
            lfe.add2LevelRequired(".*\\sTCPASS.*");
            lfe.failOnUndefinedErrors();
            lfe.addIgnored(".*error\\.noresource.*");
            lfe.addIgnored(".*IllegalStateException.*");
            lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

            boolean success = lfe.evaluateLogFile(l);
            if (!success) {
                fail(lfe.getReason());
            }
        } finally {
            MediaTranslationManagerMock.illegalExceptionAtOpen = false;
        }
    }

    public void testRecognizeSucceded() throws Exception {
        declareWait(ACCEPT,RECOGNIZER_PREPARE,RECOGNIZER_RECOGNIZE);
        InboundCallMock icm = runSimpleInteractiveTest("asr_2");
        waitFor(ACCEPT,10000);
        waitFor(RECOGNIZER_PREPARE,5000);
        waitFor(RECOGNIZER_RECOGNIZE,5000);
        declareNoWait();
        icm.recognizeSucceded("test:/test/com/mobeon/masp/execution_engine/runapp/applications/vxml/asr/asr_2.vxml?10", "one", "1");
        waitSimpleInteractiveTest(icm);

        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);

    }
}
