package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.ConfigurationGroupMock;
import com.mobeon.masp.execution_engine.util.TestEvent;
import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 12, 2006
 * Time: 8:26:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationMiscConfigTest extends ApplicationBasicTestCase<ApplicationMiscConfigTest> {


    static {
        testLanguage("misc");
        testSubdir("config");
        testCases(
                testCase("config_1")
                );
        store(ApplicationMiscConfigTest.class);
    }


    /**
     * Creates this test case
     */
    public ApplicationMiscConfigTest(String event)
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
        return genericSuite(ApplicationMiscConfigTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationMiscConfigTest.class);
    }

    /**
     * Test that it is possible to re-configure between two calls.
     * @throws Exception
     */
    public void testMiscConfig1() throws Exception {

        // setup call 1

        ConfigurationGroupMock.engineStackSize = 105;   // Must be larger than 100 to have effect at reuse
        ConfigurationGroupMock.traceEnabled = "true";
        ConfigurationGroupMock.generateOps = "true";
        ConfigurationGroupMock.alwaysCompile = "true";
        ConfigurationGroupMock.opsPath = "test";
        ConfigurationGroupMock.accepttimeout = 12000;
        ConfigurationGroupMock.callManagerWaitTime = 7000;
        ConfigurationGroupMock.createcalladditionaltimeout = 9000;
        // No reuse of engines since it may cause events to appear in wrong order
        ConfigurationGroupMock.engineccxmlpoolsize = 0;
        ConfigurationGroupMock.enginevxmlpoolsize = 0;


        LogFileExaminer lfe = runSimpleTest("config_1");

        lfe.add2LevelRequired(".*BTC: CallManagerMock.createInboundCall:onehostname.*");

        // expectation for the CCXML file:
        lfe.add2LevelRequired(TestEvent.APPLICATION_COMPILER_GENERATEOPS,true);
        // expectation for the VXML file:
        lfe.add2LevelRequired(TestEvent.APPLICATION_COMPILER_GENERATEOPS,true);
        lfe.add2LevelRequired(TestEvent.APPLICATION_COMPILER_PATH,"test");

        lfe.add2LevelRequired(TestEvent.ENGINE_STACKSIZE,105);
        lfe.add2LevelRequired(TestEvent.ENGINE_TRACE,true);

        lfe.add2LevelRequired(TestEvent.CONNECTION_CONFIG_ACCEPT_TIMEOUT,12000);
        lfe.add2LevelRequired(TestEvent.CONNECTION_CONFIG_CALLMGR_WAIT,7000);
        lfe.add2LevelRequired(TestEvent.CONNECTION_CONFIG_CREATECALL_ADDITIONAL,9000);

        lfe.failOnUndefinedErrors();
        lfe.add3LevelFailureTrigger(".*ERROR*.");
        validateTest(lfe);

        // setup call 2

        ConfigurationGroupMock.engineStackSize = 110;
        ConfigurationGroupMock.traceEnabled = "false";
        ConfigurationGroupMock.generateOps = "false";
        ConfigurationGroupMock.alwaysCompile = "true";
        ConfigurationGroupMock.accepttimeout = 14000;
        ConfigurationGroupMock.callManagerWaitTime = 15000;
        ConfigurationGroupMock.createcalladditionaltimeout = 16000;

        LogFileExaminer lfe2 = runSimpleTest("config_1");

        // expectation for the CCXML file:
        lfe2.add2LevelRequired(TestEvent.APPLICATION_COMPILER_GENERATEOPS,false);
        // expectation for the VXML file:
        lfe2.add2LevelRequired(TestEvent.APPLICATION_COMPILER_GENERATEOPS,false);

        lfe2.add2LevelRequired(TestEvent.ENGINE_STACKSIZE,110);
        lfe2.add2LevelRequired(TestEvent.ENGINE_TRACE,false);

        lfe2.add2LevelRequired(TestEvent.CONNECTION_CONFIG_ACCEPT_TIMEOUT,14000);
        lfe2.add2LevelRequired(TestEvent.CONNECTION_CONFIG_CALLMGR_WAIT,15000);
        lfe2.add2LevelRequired(TestEvent.CONNECTION_CONFIG_CREATECALL_ADDITIONAL,16000);

        lfe2.add3LevelFailureTrigger(".*ERROR*.");
        lfe2.failOnUndefinedErrors();
        validateTest(lfe2);
    }

}

