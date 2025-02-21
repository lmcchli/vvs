package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.execution_engine.util.TestEvent;
import static com.mobeon.masp.execution_engine.util.TestEvent.*;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.*;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.util.TimeValue;
import junit.framework.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The testsuite for the property tag.
 */
public class ApplicationVXMLPropertyTagTest extends ApplicationBasicTestCase<ApplicationVXMLPropertyTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("vxml");
        testSubdir("property");
        testCases(
                new ApplicationTestCase("property_1", "test:/test/com/mobeon/masp/execution_engine/runapp/applications/vxml/property/property_1.xml"),
                new ApplicationTestCase("property_2", "test:/test/com/mobeon/masp/execution_engine/runapp/applications/vxml/property/property_2.xml"),
                new ApplicationTestCase("property_3", "test:/test/com/mobeon/masp/execution_engine/runapp/applications/vxml/property/property_3.xml")
        );
        store(ApplicationVXMLPropertyTagTest.class);

    }

    private static final int WAITTIME = 40000;

    /**
     * Creates this test case
     */
    public ApplicationVXMLPropertyTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLPropertyTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLPropertyTagTest.class);
    }

    /**
     * Tests script inside a script tag by declaring a set of variables, setting some values
     * on them and verify their existance and value outside the script tag. See script_1.ccxml.
     *
     * @throws Exception
     */
    public void testVXMLPropertyTag1() throws Exception {

        declareWait(ACCEPT);
        InboundCallMock icm = createCall("property_1");
        icm.startCall();
        waitFor(ACCEPT, 5000);
        declareNoWait();
        // Wait for first prompt
        icm.waitForPlay(4000);
        // Send a DTMF token, just to test things
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 200);

        // Wait for the call to complete
        boolean exited = icm.waitForExecutionToFinish(WAITTIME);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sProperty\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }


    /**
     * Tests script inside a script tag by declaring a set of variables, setting some values
     * on them and verify their existance and value outside the script tag. See script_1.ccxml.
     *
     * @throws Exception
     */
    public void testVXMLPropertyTag2() throws Exception {

        InboundCallMock icm = createCall("property_2");
        icm.startCall();

        // Wait for first prompt
        icm.waitForPlay(2000);
        // Send a DTMF token, just to test things
        icm.sleep(1000);
        icm.sendDTMF(ControlToken.DTMFToken.ONE, 100, 200);

        // Wait for the call to complete
        boolean exited = icm.waitForExecutionToFinish(WAITTIME);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(TestEvent.NOINPUT_STARTING, new TimeValue(1500, TimeUnit.MILLISECONDS));
        lfe.add2LevelRequired(".*\\sTCPASS\\sVXML:\\sProperty\\sok.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }


    /**
     * Test that setting of properties inside a block
     * by PlatformAccess will set the form-level property
     *
     * @throws Exception
     */
    public void testVXMLPropertyTag3() throws Exception {

        InboundCallMock icm = createCall("property_3");
        icm.startCall();

        // Wait for the call to complete
        boolean exited = icm.waitForExecutionToFinish(10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add2LevelRequired(TestEvent.PROPERTYSTACK_PUT_PROPERTY, "timeout", "3s");
        lfe.add2LevelRequired(TestEvent.PROPERTYSTACK_PUT_PROPERTY_OLD, "timeout", "2s");
        lfe.add2LevelRequired(TestEvent.PROPERTYSTACK_PUT_PROPERTY, "timeout", "4s");
        lfe.add2LevelRequired(TestEvent.PROPERTYSTACK_PUT_PROPERTY_OLD, "timeout", "3s");

        lfe.failOnUndefinedErrors();

        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}