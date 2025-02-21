package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.callmanager.*;
import com.mobeon.masp.callmanager.RedirectingParty;
import com.mobeon.masp.execution_engine.runapp.mock.ConfigurationGroupMock;
import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Feb 13, 2006
 * Time: 12:52:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLSessionVariableTest extends ApplicationBasicTestCase<ApplicationVXMLSessionVariableTest> {

    static {
        testLanguage("vxml");
        testSubdir("sessionvars");
        testCases(
                testCase("sessionvars_1"),
                testCase("sessionvars_2"),
                testCase("sessionvars_3"),
                testCase("sessionvars_4"),
                testCase("sessionvars_5"),
                testCase("sessionvars_7"),
                testCase("sessionvars_8"),
                testCase("sessionvars_9"),
                testCase("sessionvars_10"),
                testCase("sessionvars_11"),
                testCase("sessionvars_12"),
                testCase("sessionvars_13"),
                testCase("sessionvars_14"),
                testCase("sessionvars_15"),
                testCase("sessionvars_16"),
                testCase("sessionvars_17"),
                testCase("sessionvars_18"),
                testCase("sessionvars_19"),
                testCase("sessionvars_20"),
                testCase("sessionvars_21"),
                testCase("sessionvars_22"),
                testCase("sessionvars_23"),
                testCase("sessionvars_24"),
                testCase("sessionvars_25"),
                testCase("sessionvars_26"),
                testCase("sessionvars_27"),
                testCase("sessionvars_28"),

               testCase("sessionvars_29"),
                testCase("sessionvars_30"),
                testCase("sessionvars_31")
        );
        store(ApplicationVXMLSessionVariableTest.class);

    }

    public ApplicationVXMLSessionVariableTest(String event) {
        super (event, "test_log_info.xml");
        log.info("MOCK: Setting up " + event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLSessionVariableTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLSessionVariableTest.class);
    }

    /**
     * test that session.connection.local.number is given right value
     * if the dialog is started at "connected"
     */
    public void testSessionConnectionLocalNumberConnected() {
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\s4660161085.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.local.number is given right value
     * if the dialog is started at "alerting"
     */
    public void testSessionConnectionLocalNumberAlerting() {
        ConfigurationGroupMock.accepttimeout = 20000;

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_2", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\s4660161085.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }


    /**
     * test that session.connection.remote.number is given right value
     * if the dialog is started at "connected"
     */
    public void testSessionConnectionRemoteNumberConnected() {
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_3", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\s4660161084.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.remote.number is given right value
     * if the dialog is started at "alerting"
     */
    public void testSessionConnectionRemoteNumberAlerting() {
        ConfigurationGroupMock.accepttimeout = 20000;

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_4", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\s4660161084.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.redirect.number is given right value
     * if the dialog is started at "connected"
     * TODO: this variable has been questioned 2005-02-13
     */
    public void testSessionConnectionRedirectNumberConnected() {
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_5", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\s4660161086.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    public void testSessionConnectionLocalUserConnected() {
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_7", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\s4660161085@mobeon.com.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    public void testSessionConnectionLocalUserAlerting() {
        ConfigurationGroupMock.accepttimeout = 20000;

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_8", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\s4660161085@mobeon.com.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }


    /**
     * test that session.connection.remote.user is given right value
     * if the dialog is started at "connected"
     */
    public void testSessionConnectionRemoteUserConnected() {
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_9", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\s4660161084@mobeon.com.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.remote.user is given right value
     * if the dialog is started at "alerting"
     */
    public void testSessionConnectionRemoteUserAlerting() {
        ConfigurationGroupMock.accepttimeout = 20000;
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_10", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\s4660161084@mobeon.com.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.redirect[].user is given right value
     * if the dialog is started at "connected"
     */
    public void testSessionConnectionRedirectUserConnected() {
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_11", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\s4660161086@mobeon.com.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.redirect[].user is given right value
     * if the dialog is started at "alerting"
     */
    public void testSessionConnectionRedirectUserAlerting() {
        ConfigurationGroupMock.accepttimeout = 20000;

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_12", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\s4660161086@mobeon.com.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }


    /**
     * test that session.connection.redirect[].pi is given right value
     * if the dialog is started at "connected". value of pi:
     * restricted.
     */
    public void testSessionConnectionRedirectPiConnected() {
        // Setup the call
        c_party_presentationIndicator = CallPartyDefinitions.PresentationIndicator.RESTRICTED;

        boolean exited = createCallAndWaitForCompletion("sessionvars_13", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\spresentation\\s1.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.redirect[].pi is given right value
     * if the dialog is started at "connected". value of pi: allowed
     */
    public void testSessionConnectionRedirectPiConnected2() {
        // Setup the call
        c_party_presentationIndicator = CallPartyDefinitions.PresentationIndicator.ALLOWED;

        boolean exited = createCallAndWaitForCompletion("sessionvars_14", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\spresentation\\s0.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.redirect[].reason is correctly mapped to
     * user busy
     */
    public void testSessionConnectionRedirectReason11() {
        // Setup the call
        redirectingReason = RedirectingParty.RedirectingReason.USER_BUSY;

        boolean exited = createCallAndWaitForCompletion("sessionvars_15", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sreason\\suser\\sbusy.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.redirect[].reason is correctly mapped to
     * no reply
     */
    public void testSessionConnectionRedirectReason12() {
        // Setup the call
        redirectingReason = RedirectingParty.RedirectingReason.NO_REPLY;

        boolean exited = createCallAndWaitForCompletion("sessionvars_16", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.add1LevelFailureTrigger(".*[Ee]xception.*");

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sreason\\sno\\sreply.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.redirect[].reason is correctly mapped to
     * mobile subscriber not reachable
     */
    public void testSessionConnectionRedirectReason13() {
        // Setup the call
        redirectingReason = RedirectingParty.RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE;

        boolean exited = createCallAndWaitForCompletion("sessionvars_17", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sreason\\smobile\\ssubscriber\\snot\\sreachable.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.redirect[].reason is correctly mapped to
     * deflection immediate response
     */
    public void testSessionConnectionRedirectReason14() {
        // Setup the call
        redirectingReason = RedirectingParty.RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE;

        boolean exited = createCallAndWaitForCompletion("sessionvars_18", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sreason\\sdeflection\\simmediate\\sresponse.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.redirect[].reason is correctly mapped to
     * "unknown"
     */
    public void testSessionConnectionRedirectReason15() {
        // Setup the call
        redirectingReason = RedirectingParty.RedirectingReason.UNKNOWN;

        boolean exited = createCallAndWaitForCompletion("sessionvars_19", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sreason\\sunknown.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.redirect[].reason is correctly mapped to
     * "unconditional"
     */
    public void testSessionConnectionRedirectReason16(){
        // Setup the call
        redirectingReason = RedirectingParty.RedirectingReason.UNCONDITIONAL;

        boolean exited = createCallAndWaitForCompletion ("sessionvars_29", 20000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sreason\\sunconditional.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
    }

    /**
     * test that session.connection.redirect[].reason is correctly mapped to
     * "deflection during alerting"
     */
    public void testSessionConnectionRedirectReason17(){
        // Setup the call
        redirectingReason = RedirectingParty.RedirectingReason.DEFLECTION_DURING_ALERTING;

        boolean exited = createCallAndWaitForCompletion ("sessionvars_30", 20000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sreason\\sdeflection\\sduring\\salerting.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
    }



    /**
     * test that session.connection.calltype is correctly set to voice
     */
    public void testSessionConnectionCallTypeVoice() {
        // Setup the call
        callType = CallProperties.CallType.VOICE;

        boolean exited = createCallAndWaitForCompletion("sessionvars_20", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\scalltype\\svoice.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.calltype is correctly set to video
     */
    public void testSessionConnectionCallTypeVideo() {
        // Setup the call
        callType = CallProperties.CallType.VIDEO;

        boolean exited = createCallAndWaitForCompletion("sessionvars_21", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\scalltype\\svideo.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.local.uri is given right value
     */
    public void testSessionConnectionLocalURI() {
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_22", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\ssip:4660161085@mobeon.com;user=phone.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.remote.uri is given right value
     */
    public void testSessionConnectionRemoteURI() {
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_23", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\ssip:4660161084@mobeon.com;user=phone.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.redirect[0].uri is given right value
     */
    public void testSessionConnectionRedirectURI() {
        // Setup the call
        boolean exited = createCallAndWaitForCompletion("sessionvars_24", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\ssip:4660161086@mobeon.com;user=phone.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.remote.pi is given right value
     */
    public void testSessionConnectionRemotePi() {
        // Setup the call
        a_party_presentationIndicator = CallPartyDefinitions.PresentationIndicator.RESTRICTED;

        boolean exited = createCallAndWaitForCompletion("sessionvars_25", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\spresentation\\s1.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * test that session.connection.remote._numbercomplete is true when it should be true.
     */
    public void testSessionConnectionRemoteNumberCompleteIsTrue(){
        // Setup the call
        super.a_party_completion = NumberCompletion.COMPLETE;

        boolean exited = createCallAndWaitForCompletion ("sessionvars_26", 20000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*VXML: Number complete: true.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
    }

    /**
     * test that session.connection.remote._numbercomplete is false when it should be false.
     */
    public void testSessionConnectionRemoteNumberCompleteIsFalse(){
        // Setup the call
        super.a_party_completion = NumberCompletion.INCOMPLETE;

        boolean exited = createCallAndWaitForCompletion ("sessionvars_27", 20000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*VXML: Number complete: false.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
    }

    /**
     * test that session.connection.remote._numbercomplete is undefined when it should be undefined.
     */
    public void testSessionConnectionRemoteNumberCompleteIsUndefined(){
        // Setup the call
        super.a_party_completion = NumberCompletion.UNKNOWN;

        boolean exited = createCallAndWaitForCompletion ("sessionvars_28", 20000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*VXML: Number complete: undefined.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
    }

    /**
     * test that session.connection._bitrate has expected value
     */
    public void testSessionConnectionBitRate(){
        // Setup the call
        super.inboundBitRate = 8765;

        boolean exited = createCallAndWaitForCompletion ("sessionvars_31", 20000);
        if (!exited)
        {
            fail ("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer ();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*VXML: Bitrate: 8765.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail (lfe.getReason ());
    }

}
