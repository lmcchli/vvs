package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import static com.mobeon.masp.execution_engine.util.TestEvent.*;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.*;
import com.mobeon.masp.stream.ControlToken;
import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Mar 30, 2006
 * Time: 4:03:10 PM
 * To change this template use File | Settings | File Templates.
 */


/**
 * This class tests events sent to the application in various stages
 * (e.g. before any form has been initialized) and are therefore not related
 * to any particular tag.
 */


public class ApplicationVXMLEventTest extends ApplicationBasicTestCase<ApplicationVXMLEventTest> {
    static {
        testLanguage("vxml");
        testSubdir("event");
        testCases(
                testCase("event_1"),
                testCase("event_2")
        );
        store(ApplicationVXMLEventTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLEventTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLEventTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLEventTest.class);
    }

    /**
     * Verify that e don't crash if there is a DTMF during
     * document initialization.
     *
     * @throws Exception
     */
    public void testEventDuringDocInit() throws Exception {
        declareWait(ACCEPT);
        final InboundCallMock icm = createCall("event_1");
        icm.startCall();
        waitFor(ACCEPT, 5000);
        declareNoWait();
        icm.sleep(5000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 500);

        boolean exited = icm.waitForExecutionToFinish(15000);

        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*ERROR.*");

        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that a disconnect during document initialization is not
     * handled until a form is reached.
     *
     * @throws Exception
     */
    public void testEventDuringDocInit2() throws Exception {
        final InboundCallMock icm = createCall("event_2");

        // Not a perfect test case:
        // we hope the VXML app is started within 5 seconds.
        icm.startCall();
        icm.sleep(5000);
        icm.disconnect();

        boolean exited = icm.waitForExecutionToFinish(15000);

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

}
