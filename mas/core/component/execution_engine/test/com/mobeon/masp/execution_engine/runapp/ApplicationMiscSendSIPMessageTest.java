package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.declareWait;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.waitFor;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.declareNoWait;
import static com.mobeon.masp.execution_engine.util.TestEvent.PLAY_STARTED;
import static com.mobeon.masp.execution_engine.util.TestEvent.ACCEPT;
import com.mobeon.masp.util.NamedValue;
import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.execution_engine.runapp.mock.ServiceRequestRunner;
import com.mobeon.masp.stream.RecordFinishedEvent;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.callmanager.events.SipMessageResponseEvent;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: 2007-mar-12
 * Time: 18:18:15
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationMiscSendSIPMessageTest extends ApplicationBasicTestCase<ApplicationMiscSendSIPMessageTest>{

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("misc");
        testSubdir("sendsipmessage");
        testCases(
                testCase("sendsipmessage_1")
        );
        store(ApplicationMiscSendSIPMessageTest.class);
    }

    /**
     * Creates this test case
     */
    public ApplicationMiscSendSIPMessageTest(String event) {
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
        return genericSuite(ApplicationMiscSendSIPMessageTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationMiscSendSIPMessageTest.class);
    }

    public void testSendSIPMessage1() throws Exception {

        List<NamedValue<String,String>> parameters = new ArrayList<NamedValue<String,String>>();
        parameters.add(new NamedValue<String,String>("responsecode", "102"));
        parameters.add(new NamedValue<String,String>("responsetext", "theTextMessage"));
        parameters.add(new NamedValue<String,String>("retryafter", "900"));

        SipMessageResponseEvent sipMessageResponseEvent = new SipMessageResponseEvent(parameters);

        super.setSipMessageResponseEvent(sipMessageResponseEvent);

        ServiceRequestRunner r = createServiceRequestRunner("sendsipmessage_1");
        r.startCall();
        boolean exited = r.waitForExecutionToFinish(20000);
        TestAppender.stopSave(log);

        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        // Verify that the request is sent as expected

        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Collection size: 13*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter sendto:12322*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter messageaccount:12322*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter messageswaiting:yes*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter voicemessagenew:2*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter voicemessageold:1*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter faxmessagenew:1*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter faxmessageold:6*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter videomessagenew:2*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter videomessageold:2*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter emailmessagenew:3*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter emailmessageold:7*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter outboundcallserverhost:da_hostname.*");
        lfe.add2LevelRequired(".*MOCK: CallManagerMock\\.sendSipMessage. Parameter outboundcallserverport:8877.*");

        // verify output from CCXML app

        lfe.add2LevelRequired(".*RESPONSECODE 102.*");
        lfe.add2LevelRequired(".*RESPONSETEXT theTextMessage.*");
        lfe.add2LevelRequired(".*RETRYAFTER 900.*");
        // Verify that a parameter that does not exist in the event has the value "undefined"
        lfe.add2LevelRequired(".*KALLE undefined.*");


        lfe.failOnUndefinedErrors();
        lfe.add1LevelFailureTrigger(".*TCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }

    }
}
