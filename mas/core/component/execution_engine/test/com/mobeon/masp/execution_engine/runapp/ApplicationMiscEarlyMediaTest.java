package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;
import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.execution_engine.runapp.mock.CallManagerMock;
import com.mobeon.masp.execution_engine.runapp.mock.ConfigurationGroupMock;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: May 3, 2006
 * Time: 11:01:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationMiscEarlyMediaTest extends ApplicationBasicTestCase<ApplicationMiscEarlyMediaTest> {

    static {
        testLanguage("misc");
        testSubdir("earlymedia");
        testCases(
                testCase("earlymedia_1"),
                testCase("earlymedia_2"),
                testCase("earlymedia_3")
                );
        store(ApplicationMiscEarlyMediaTest.class);
    }



    /**
     * Creates this test case
     */
    public ApplicationMiscEarlyMediaTest(String event)
    {
        super (event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationMiscEarlyMediaTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationMiscEarlyMediaTest.class);
    }

    /**
     * Test that we are able to play early media to an inbound call, i.e. before it is connected, and that we also
     * may play early media after it is connected.
     * @throws Exception
     */
    public void testEarlymedia1() throws Exception {
        setTestCaseTimeout(40000);
        ConfigurationGroupMock.accepttimeout = 40000;

        LogFileExaminer lfe = runSimpleTest("earlymedia_1");

        lfe.add2LevelRequired(".*\\sTCPASS VXML earlymedia app.*");
        lfe.add2LevelRequired(".*Entering OutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*MOCK: OutboundMediaStreamMock.Thread PlayFinishedEvent.*");

        lfe.add2LevelRequired(".*\\sTCPASS VXML real app.*");
        lfe.add2LevelRequired(".*Entering OutboundMediaStreamMock.play.*");
        lfe.add2LevelRequired(".*MOCK: OutboundMediaStreamMock.Thread PlayFinishedEvent.*");

        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");

        validateTest(lfe);
    }

    /**
     * Test that it is possible to identify that an outbound call has early media available, and it is possible to join them
     * at the progressing state.
     * @throws Exception
     */
    public void testEarlymedia2() throws Exception {
        setTestCaseTimeout(40000);
        ConfigurationGroupMock.accepttimeout = 40000;
        setEarlyMediaAtProgressing(true);

        LogFileExaminer lfe = runSimpleTest("earlymedia_2");

        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");

        validateTest(lfe);
    }

    /**
     * Test that it is possible to receive earlymediafailed event.
     * @throws Exception
     */
    public void testEarlymedia3() throws Exception {
        setTestCaseTimeout(40000);
        super.setResponseToNegotiateEarlyMedia(CallManagerMock.EventType.EARLYMEDIAFAILED_EVENT);

        LogFileExaminer lfe = runSimpleTest("earlymedia_3");

        lfe.add2LevelRequired(".*\\sTCPASS CCXML.*");

        validateTest(lfe);
        super.setResponseToNegotiateEarlyMedia(CallManagerMock.EventType.EARLYMEDIAAVAILABLE_EVENT);
    }

}
