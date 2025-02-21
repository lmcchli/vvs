package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import static com.mobeon.masp.execution_engine.util.TestEvent.*;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.*;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.stream.RecordFinishedEvent;
import junit.framework.Test;

import java.util.List;

/**
 * Test suite for the field tag in forms.
 */
public class ApplicationVXMLFieldTagTest extends ApplicationBasicTestCase<ApplicationVXMLFieldTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("field");
        testCases(
                testCase("field_1"),
                testCase("field_2"),
                testCase("field_3"),
                testCase("field_4"),
                testCase("field_5"),
                testCase("field_5b"),
                testCase("field_a"),
                testCase("field_b"),
                testCase("field_6"),
                testCase("field_7"),
                testCase("field_8"),
                testCase("field_9"),
                testCase("field_10"),
                testCase("field_11"),
                testCase("field_12"),
                testCase("field_13"),
                testCase("field_14"),
                testCase("field_15"),
                testCase("field_16"),
                testCase("field_17"),
                testCase("field_18"),
                testCase("field_18"),
                testCase("field_19"),
                testCase("field_20"),
                testCase("field_21"),
                testCase("field_22"),
                testCase("field_23"),                
                testCase("field_24"),
                testCase("field_25"),
                testCase("field_26")
        );
        store(ApplicationVXMLFieldTagTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLFieldTagTest(String event) {
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
        return genericSuite(ApplicationVXMLFieldTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLFieldTagTest.class);
    }

    /**
     * Tests script inside a script tag by declaring a set of variables, setting some values
     * on them and verify their existance and value outside the script tag. See script_1.ccxml.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag1() throws Exception {

        // Setup the call
        // Setup the call

        declareWait(ACCEPT);
        final InboundCallMock icm = createCall("field_1");
        icm.startCall();
        waitFor(ACCEPT, 5000);
        declareNoWait();
        new Thread() {
            public void run() {
                try {
                    icm.sleep(4000);
                    icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }.start();
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sField\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sField\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }

    }

    /**
     * Tests noinput on the grammar for a field.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag2() throws Exception {

        // Setup the call
        // Setup the call
        InboundCallMock icm = createCall("field_2");
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
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sField\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests noinput on the grammar for a field.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag3() throws Exception {

        // Setup the call
        // Setup the call
        InboundCallMock icm = createCall("field_3");
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
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sField\\sok 3.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests input on a field local grammar for a field.
     *
     * @throws Exception
     */
    public void testVXMLFieldTagA() throws Exception {

        // Setup the call
        // Setup the call
        InboundCallMock icm = createCall("field_a");
        icm.startCall();
        icm.sleep(3000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sField\\sok.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sForm\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests input on a form local grammar for a form.
     *
     * @throws Exception
     */
    public void testVXMLFieldTagB() throws Exception {

        // Setup the call
        // Setup the call
        InboundCallMock icm = createCall("field_b");
        icm.startCall();
        icm.sleep(3000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);
        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sField\\sok.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sForm\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    public void testVXMLFieldTag4() throws Exception {

        // Setup the call
        // Setup the call
        InboundCallMock icm = createCall("field_4");
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

        lfe.add3LevelFailureTrigger(".*\\sTCPOINT.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that properties set in one field is not spilled over to another field. Further,
     * test the work-around used by VVA for different timeouts for differerent grammar rules.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag5() throws Exception {
        InboundCallMock icm = createCall("field_5");
        icm.startCall();
        icm.sleep(3000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);
        icm.sleep(500);
        icm.sendDTMF(ControlToken.DTMFToken.STAR, 100, 40);
        icm.sleep(1500);
        icm.sendDTMF(ControlToken.DTMFToken.EIGHT, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS: field 5 OK.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Using the same setup as testVXMLFieldTag5, verify that the correct
     * catch handler is selected when throwing a nomatch.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag5b() throws Exception {
        InboundCallMock icm = createCall("field_5");
        icm.startCall();
        icm.sleep(3000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);
        icm.sleep(1000);
        icm.sendDTMF(ControlToken.DTMFToken.STAR, 100, 40);
        icm.sleep(1500);
        icm.sendDTMF(ControlToken.DTMFToken.SEVEN, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*In f2 catch.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that noinput timeout can be triggered
     * even if there are no prompts to play (TR 27125)
     *
     * @throws Exception
     */
    public void testVXMLFieldTag6() throws Exception {
        InboundCallMock icm = createCall("field_6");
        disableScaling();
        icm.startCall();
        icm.sleep(4000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that noinput timeout does not generate a loop if the
     *
     * @throws Exception
     */
    public void testVXMLFieldTag7() throws Exception {
        InboundCallMock icm = createCall("field_7");
        disableScaling();
        icm.startCall();

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS: Bailing out.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that DTMF entered before a field is entered, is used
     * by the field, as soon as the field is entered.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag8() throws Exception {
        InboundCallMock icm = createCall("field_8");
        disableScaling();
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);
        icm.sleep(8000);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that DTMF entered for the first field is not re-used for the next field
     * due to some bug. We expect noinput in the second field.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag9() throws Exception {
        InboundCallMock icm = createCall("field_9");
        disableScaling();
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS 1.*");
        lfe.add2LevelRequired(".*\\sTCPASS 2.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that if 2 DTMF are entered while in a field, they are buffered and can
     * be used to fill in two fields.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag10() throws Exception {
        InboundCallMock icm = createCall("field_10");
        disableScaling();
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 40);


        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS 1.*");
        lfe.add2LevelRequired(".*\\sTCPASS 2.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that DTMF entered can be used also after transitioning to a subdialog
     *
     * @throws Exception
     */
    public void testVXMLFieldTag11() throws Exception {
        InboundCallMock icm = createCall("field_11");
        disableScaling();
        declareWait(ACCEPT);
        icm.startCall();
        waitFor(ACCEPT, 5000);
        declareNoWait();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);


        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * TR 27895. DTMF entered when no prompts were currently playing could sometimes cause the call
     * to hang (actually when the DTMF triggered the event handler registered from ExecutionContextImpl)
     *
     * @throws Exception
     */
    public void testVXMLFieldTag12() throws Exception {
        InboundCallMock icm = createCall("field_12");
        disableScaling();
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);
        // let the call stay in <filled> for a while
        icm.sleep(2000);
        // This DTMF used to hang the call
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * TR 27987. if noinput and connection.disconnect.hangup event are thrown at the same time they can not
     * interrupt each others' <catch> execution.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag13() throws Exception {
        InboundCallMock icm = createCall("field_13");
        disableScaling();
        icm.startCall();
        icm.sleep(3000);
        icm.disconnectCall();

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS1.*");
        lfe.add2LevelRequired(".*\\sTCPASS2.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * TR 28119: Make sure two (multi) DTMF works also when the second DTMF is entered
     * during interdigit timeout.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag14() throws Exception {
        InboundCallMock icm = createCall("field_14");
        disableScaling();
        icm.startCall();
        icm.sleep(3000);
        icm.sendDTMF(ControlToken.DTMFToken.STAR, 100, 40);
        icm.sleep(4000);
        icm.sendDTMF(ControlToken.DTMFToken.STAR, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that it is possible to enter a digit string like "1234" and end with a termchar
     * (TR 28177)
     *
     * @throws Exception
     */
    public void testVXMLFieldTag15() throws Exception {
        InboundCallMock icm = createCall("field_15");
        disableScaling();
        icm.startCall();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 40);
        icm.sleep(300);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 40);
        icm.sleep(300);
        icm.sendDTMF(ControlToken.DTMFToken.THREE, 100, 40);
        icm.sleep(300);
        icm.sendDTMF(ControlToken.DTMFToken.FOUR, 100, 40);
        icm.sleep(300);
        icm.sendDTMF(ControlToken.DTMFToken.HASH, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * n
     * Verify that it is possible to have a grammar accepting emtystring and immediately press the term char
     *
     * @throws Exception
     */
    public void testVXMLFieldTag16() throws Exception {
        InboundCallMock icm = createCall("field_16");
        declareWait(NOINPUT_STARTING, ACCEPT);
        disableScaling();
        icm.startCall();
        waitFor(ACCEPT, 5000);
        waitFor(NOINPUT_STARTING, 5000);
        declareNoWait();
        icm.sendDTMF(ControlToken.DTMFToken.HASH, 100, 40);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Verify that if there is first a hangup event and then a semantic error event during
     * transitioning state, the semantic error will first be delivered, and then the hangup
     * as soon as a <field> is reached.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag17() throws Exception {
        InboundCallMock icm = createCall("field_17");
        disableScaling();
        icm.startCall();
        icm.sleep(3000);
        icm.disconnect();

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.addIgnored(".*error\\.semantic.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * There was a problem where a nomatch event and then an immediate goto
     * to another form could cause execution to continue in the other form
     * even though there was an outstanding playFinished which should have
     * been waiting for in the first form.
     * The result was a warning log, which this test case recreated.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag18() throws Exception {


        setDelayBeforeResponseToPlay(1500);
        InboundCallMock icm = createCall("field_18");
        disableScaling();
        icm.startCall();
        icm.sleep(4000);
        icm.sendDTMF(ControlToken.DTMFToken.FOUR, 100, 500);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        setDelayBeforeResponseToPlay(0);
    }

    /**
     * TR 28491. If a prompt is playing and two DTMF arrive before the "play finished" for the barged in prompt,
     * the DTMF were lost.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag19() throws Exception {

        setDelayBeforeResponseToPlay(3000);
        InboundCallMock icm = createCall("field_19");
        disableScaling();
        icm.startCall();
        icm.sleep(4000);
        icm.sendDTMF(ControlToken.DTMFToken.SEVEN, 100, 500);
        icm.sleep(500);
        icm.sendDTMF(ControlToken.DTMFToken.EIGHT, 100, 500);


        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
        setDelayBeforeResponseToPlay(0);
    }

    /**
     * Another error revealed by very fast DTMF. TR 28491 was reopened
     *
     * @throws Exception
     */
    public void testVXMLFieldTag20() throws Exception {

        InboundCallMock icm = createCall("field_20");
        disableScaling();
        icm.startCall();
        icm.sleep(4000);
        icm.sendDTMF(ControlToken.DTMFToken.ZERO, 100, 500);
        icm.sendDTMF(ControlToken.DTMFToken.SIX, 100, 500);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
        icm.sendDTMF(ControlToken.DTMFToken.TWO, 100, 500);
        icm.sendDTMF(ControlToken.DTMFToken.THREE, 100, 500);
        icm.sendDTMF(ControlToken.DTMFToken.FOUR, 100, 500);
        icm.sendDTMF(ControlToken.DTMFToken.FIVE, 100, 500);

        // and now a short delay!
        icm.sleep(500);
        icm.sendDTMF(ControlToken.DTMFToken.SIX, 100, 500);


        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * TR 28965. Verify that if there is buffered DTMF, there will be no noinput event thrown when timeout=0
     *
     * @throws Exception
     */
    public void testVXMLFieldTag21() throws Exception {

        InboundCallMock icm = createCall("field_21");
        disableScaling();
        icm.startCall();

        // The bug is sporadic so we will basically test a field behaving correctly 10 times:

        icm.sleep(3000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
        for (int i = 0; i < 9; i++) {
            icm.sleep(5000);
            icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
        }

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * TR 29048. Verify that an application always start with a "clean" lastresult variable,
     * also if the preceding call was given DTMF and that call's lastresut was given a value.
     *
     * @throws Exception
     */
    public void testVXMLFieldTag22() throws Exception {

        for (int i = 0; i < 2; i++) {
            InboundCallMock icm = createCall("field_22");
            disableScaling();
            icm.startCall();

            icm.sleep(3000);
            icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);
            boolean exited = icm.waitForExecutionToFinish(20000);
            TestAppender.stopSave(log);
            if (!exited) {
                fail("The application timed out!");
            }

            // Verify the output
            List<String> l = TestAppender.getOutputList();
            LogFileExaminer lfe = new LogFileExaminer();
            lfe.add2LevelRequired(".*\\sTCPASS.*");
            lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

            lfe.failOnUndefinedErrors();

            boolean success = lfe.evaluateLogFile(l);
            if (!success)
                fail(lfe.getReason());
        }
    }

    /**
     * Kenneth Selin added this TC after noticing that if DTMF is entered between the point where EE
     * decides to play prompts and before it actually invokes play() on CallManager, the DTMF was ignored
     * and the entire prompt was played. What should really happen is that no prompt is played, or
     * just a little part of it (in case the control token arrives a little later). But it is a fault if an entire propt is
     * played to completion.
     *
     * The testcase uses the test event PROMPT_QUEUE_PLAY as the point when "where EE
     * decides to play prompts".
     * @throws Exception
     */
    public void testVXMLFieldTag23() throws Exception {

        // Since the fault is timing dependent, repeat the scenario
        // a few times to make it more likely to catch it (10 was not enough)
        for (int i = 0; i < 20; i++) {

            declareWait(PROMPT_QUEUE_PLAY);
            final InboundCallMock icm = createCall("field_23");
            disableScaling();
            icm.startCall();
            waitFor(PROMPT_QUEUE_PLAY, 5000);
            declareNoWait();
            icm.sendDTMF(ControlToken.DTMFToken.STAR, 100, 500);

            boolean exited = icm.waitForExecutionToFinish(20000);
            TestAppender.stopSave(log);
            if (!exited) {
                fail("The application timed out!");
            }

            // Verify the output
            List<String> l = TestAppender.getOutputList();
            LogFileExaminer lfe = new LogFileExaminer();
            lfe.add2LevelRequired(".*\\sTCPASS.*");
            lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
            lfe.add1LevelFailureTrigger(".*\\sMOCK:\\sOutboundMediaStreamMock\\.Thread\\sPlayFinishedEvent\\(PLAY_FINISHED\\).*");

            lfe.failOnUndefinedErrors();

            boolean success = lfe.evaluateLogFile(l);
            if (!success)
                fail(lfe.getReason());
        }
    }

    /**
     * TR 30534: a "nomatch" thrown from the VoiceXML using <throw> within <noinput>
     * had no effect.
     * @throws Exception
     */
    public void testVXMLFieldTag24() throws Exception {

        InboundCallMock icm = createCall("field_24");
        disableScaling();
        icm.startCall();

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*TCPASS1.*");
        lfe.add2LevelRequired(".*TCPASS2.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }
                      /**
     * TR 30304. If a grammar allows only "**" and the user only enters "*",
     * there shall be a nomatch.
     * @throws Exception
     */
    public void testVXMLFieldTag25() throws Exception {

        declareWait(PLAY_STARTED);
        final InboundCallMock icm = createCall("field_25");
        disableScaling();
        icm.startCall();
        waitFor(PLAY_STARTED, 5000);
        declareNoWait();
        icm.sendDTMF(ControlToken.DTMFToken.STAR, 100, 500);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
    * Same as testVXMLFieldTag25 but with a delayed play finished.
    * @throws Exception
    */
   public void testVXMLFieldTag26() throws Exception {

        super.setDelayBeforeResponseToPlay(3000);

        declareWait(PLAY_STARTED);
        final InboundCallMock icm = createCall("field_26");
        disableScaling();
        icm.startCall();
        waitFor(PLAY_STARTED, 5000);
        declareNoWait();
        icm.sendDTMF(ControlToken.DTMFToken.STAR, 100, 500);

        boolean exited = icm.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");

        lfe.failOnUndefinedErrors();

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
   }

}