/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import static com.mobeon.masp.execution_engine.util.TestEvent.*;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.*;
import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.stream.ControlToken;
import junit.framework.Test;

import java.util.List;

public class ApplicationVXMLGrammarTest extends ApplicationBasicTestCase<ApplicationVXMLGrammarTest> {


    static{
        testSubdir("grammar");
        testLanguage("vxml");
        testCases(
                testCase("grammar_dtmfterm"),
                testCase("grammar_scope"),
                testCase("grammar_scope2"),
                testCase("grammar_ruleref"),
                testCase("grammar_2"),
                testCase("grammar_TR26990"),
                testCase("grammar_asr"),
                testCase("grammar_asr_no_input"),
                testCase("grammar_asr_error"),
                testCase("grammar_asr_no_match"),
                testCase("grammar_gsne"),
                testCase("grammar_asr_dtmf_bargein")
        );
        store(ApplicationVXMLGrammarTest.class);
    }

    /**
     * Constructor for this test suite, must be called from the testclass that inherits
     * this class through a super(event) call.
     *
     * @param event
     */
    public ApplicationVXMLGrammarTest(String event) {
        super(event);
    }

    public static Test suite() {
        return genericSuite(ApplicationVXMLGrammarTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLGrammarTest.class);
    }

    public void testVXMLGrammarDTMFTerm() throws Exception {
        //boolean exited = createCallAndWaitForCompletion("grammar_dtmfterm", 5000);

        InboundCallMock icm = createCall("grammar_dtmfterm");
        icm.startCall();
        icm.waitForPlay(6000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
        boolean exited = icm.waitForExecutionToFinish(2000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }

    }

    public void testVXMLGrammarRuleref() throws Exception {
        final InboundCallMock icm = createCall("grammar_ruleref");
        icm.startCall();

        icm.waitForPlay(2000);
        icm.sleep(1500);
        icm.sendDTMF(ControlToken.DTMFToken.FOUR, 100, 500);
        icm.sleep(200);
        icm.sendDTMF(ControlToken.DTMFToken.SEVEN, 100, 500);
        icm.sleep(200);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
        icm.sleep(200);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
        icm.sleep(200);
        icm.sendDTMF(ControlToken.DTMFToken.HASH, 100, 500);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS: In filled.*");
        lfe.add2LevelRequired(".*\\sTCPASS: Success with value 4711#.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }

    }

    public void testVXMLGrammarScope() throws Exception {
        final InboundCallMock icm = createCall("grammar_scope");
        icm.startCall();
        icm.waitForPlay(5000);
        icm.sleep(2000);
        icm.sendDTMF(ControlToken.DTMFToken.EIGHT, 100, 500);
        icm.waitForPlay(2000);
        icm.sleep(1000);
        icm.sendDTMF(ControlToken.DTMFToken.SEVEN, 100, 500);
        icm.waitForPlay(1000);
        icm.sleep(1000);
        icm.sendDTMF(ControlToken.DTMFToken.SIX, 100, 500);


        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        // lfe.add1LevelFailureTrigger(".*[Ee]rror.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }

    }


    /**
     * This is an embryo of a test case Tomas Andersen wanted..not done yet...
     *
     * @throws Exception
     */
    public void testVXMLGrammar2() throws Exception {
        final InboundCallMock icm = createCall("grammar_2");
        icm.startCall();
        icm.waitForPlay(1000);

        new Thread() {
            public void run() {
                try {
                    icm.sleep(2000);
                    icm.sendDTMF(ControlToken.DTMFToken.STAR, 100, 500);
                    icm.sleep(200);
                    icm.sendDTMF(ControlToken.DTMFToken.STAR, 100, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();
        icm.waitForPlay(20000);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*PASS*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testTR26990() throws Exception {
        final InboundCallMock icm = createCall("grammar_TR26990");
        icm.startCall();
        icm.waitForPlay(1000);

        icm.sleep(2000);
        icm.sendDTMF(ControlToken.DTMFToken.STAR, 100, 500);
        icm.sleep(200);
        icm.sendDTMF(ControlToken.DTMFToken.SEVEN, 100, 500);

        boolean exited = icm.waitForExecutionToFinish(45000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS: We have a corrrect no match*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * The grammar scope node reported an nullpointer,since the grammar was null.
     * This tc is used to trace why that occured.
     *
     * @throws Exception
     */
    public void testGrammarScopeNodeError() throws Exception {
        final InboundCallMock icm = createCall("grammar_gsne");
        icm.startCall();
        icm.waitForPlay(1000);

        icm.sleep(2000);
        icm.sendDTMF(ControlToken.DTMFToken.HASH, 100, 500);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*grammar_GrammarScopeNodeError.*");
        lfe.add2LevelRequired(".*GOT #.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testGrammarScope2() throws Exception {
        final InboundCallMock icm = createCall("grammar_scope2");
        icm.startCall();
        icm.waitForPlay(1000);

        icm.sleep(2000);
        icm.sendDTMF(ControlToken.DTMFToken.EIGHT, 100, 500);
        icm.sleep(400);
        icm.sendDTMF(ControlToken.DTMFToken.SEVEN, 100, 500);
        icm.sleep(400);
        icm.sendDTMF(ControlToken.DTMFToken.SIX, 100, 500);


        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");
        lfe.add2LevelRequired(".*\\sTCPASS: In leaf2*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testASR() throws Exception {
        declareWait(RECOGNIZER_RECOGNIZE);
        final InboundCallMock icm = createCall("grammar_asr");
        icm.startCall();
        waitFor(RECOGNIZER_RECOGNIZE,5000);
        declareNoWait();
        icm.waitForPlay(5000);
        icm.recognizeSucceded("test:/test/com/mobeon/masp/execution_engine/runapp/applications/vxml/grammar/grammar_asr.vxml?5", "one", "1");

        boolean exited = icm.waitForExecutionToFinish(30000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }


    public void testASRFailed() throws Exception {
        declareWait(RECOGNIZER_RECOGNIZE);
        final InboundCallMock icm = createCall("grammar_asr_error");
        icm.startCall();
        waitFor(RECOGNIZER_RECOGNIZE,5000);
        declareNoWait();
        icm.waitForPlay(5000);
        icm.recognizeFailed();

        boolean exited = icm.waitForExecutionToFinish(30000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testASRNoMatch() throws Exception {
        declareWait(RECOGNIZER_RECOGNIZE);
        final InboundCallMock icm = createCall("grammar_asr_no_match");
        icm.startCall();
        waitFor(RECOGNIZER_RECOGNIZE,5000);
        declareNoWait();
        icm.waitForPlay(5000);
        icm.recognizeNoMatch();

        boolean exited = icm.waitForExecutionToFinish(30000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testASRNoInput() throws Exception {
        declareWait(ACCEPT,RECOGNIZER_RECOGNIZE);
        final InboundCallMock icm = createCall("grammar_asr_no_input");
        icm.startCall();
        waitFor(ACCEPT,5000);
        icm.waitForPlay(5000);
        waitFor(RECOGNIZER_RECOGNIZE,5000);
        declareNoWait();
        icm.recognizeNoInput();

        boolean exited = icm.waitForExecutionToFinish(30000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    public void testASRDTMFBargeIn() throws Exception {
        declareWait(ACCEPT,RECOGNIZER_PREPARE);
        final InboundCallMock icm = createCall("grammar_asr_dtmf_bargein");
        icm.startCall();
        waitFor(ACCEPT,5000);
        waitFor(RECOGNIZER_PREPARE,5000);
        icm.waitForPlay(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE,100,50);
        declareNoWait();
        icm.recognizeNoInput();

        boolean exited = icm.waitForExecutionToFinish(30000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out");
        }
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add1LevelFailureTrigger(".*[Ww]arning.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

}
