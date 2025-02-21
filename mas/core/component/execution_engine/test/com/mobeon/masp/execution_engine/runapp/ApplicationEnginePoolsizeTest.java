package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;
import com.mobeon.masp.execution_engine.runapp.mock.ConfigurationGroupMock;
import com.mobeon.masp.execution_engine.util.TestEvent;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 12, 2006
 * Time: 8:26:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationEnginePoolsizeTest extends ApplicationBasicTestCase<ApplicationEnginePoolsizeTest> {

    static int stackSize = 108;

    static {
        testLanguage("misc");
        testSubdir("config");
        testCases(
                testCase("config_1")
                );
        store(ApplicationEnginePoolsizeTest.class);
    }


    /**
     * Creates this test case
     */
    public ApplicationEnginePoolsizeTest(String event)
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
        return genericSuite(ApplicationEnginePoolsizeTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationEnginePoolsizeTest.class);
    }


    public void testDummyTest() throws Exception {

    }

    /**
     * Test Engine pool size.
     * The number of engines that have been created by previous test cases is hard to predict, this means that
     * only a simple test can be performed.
     * The test case first makes sure that two engines exist and then verifies that they are reused if
     * poolsize is greater than two and that new engines are created if poolsize is 0.
     * @throws Exception
     */
    public void testEnginePoolsize1() throws Exception {

        sleepBeforeStopSaveLog = 100;

        // setup call 1

        ConfigurationGroupMock.engineccxmlpoolsize = 1;
        ConfigurationGroupMock.enginevxmlpoolsize = 1;

        runSimpleTest("config_1");                      // Make sure that at least one engine exist per type
        Thread.sleep(1000);
        LogFileExaminer lfe = runSimpleTest("config_1");

        lfe.add2LevelRequired(".*BTC: CallManagerMock.createInboundCall:onehostname.*");

        // expectation for the VXML file:
        lfe.add2LevelRequired(TestEvent.CONFIG_ENGINE_POOLSIZE,1);
        lfe.add2LevelRequired(TestEvent.ENGINE_REUSED);
        lfe.add2LevelRequired(TestEvent.CONFIG_ENGINE_POOLSIZE,1);
        lfe.add2LevelRequired(TestEvent.ENGINE_REUSED);
        lfe.add2LevelRequired(TestEvent.ENGINE_RELEASED);
        lfe.add2LevelRequired(TestEvent.ENGINE_RELEASED);

        lfe.failOnUndefinedErrors();
        lfe.add3LevelFailureTrigger(".*ERROR*.");
        Thread.sleep(1000);
        validateTest(lfe);

        // setup call 2

        ConfigurationGroupMock.engineccxmlpoolsize = 0;       // A new engine must be created
        ConfigurationGroupMock.enginevxmlpoolsize = 0;       // A new engine must be created

        Thread.sleep(2000);
        LogFileExaminer lfe2 = runSimpleTest("config_1");

        lfe2.add2LevelRequired(".*BTC: CallManagerMock.createInboundCall:onehostname.*");

        // expectation for the VXML file:
        lfe2.add2LevelRequired(TestEvent.CONFIG_ENGINE_POOLSIZE,0);
        lfe2.add2LevelRequired(TestEvent.ENGINE_CREATED);
        lfe2.add2LevelRequired(TestEvent.CONFIG_ENGINE_POOLSIZE,0);
        lfe2.add2LevelRequired(TestEvent.ENGINE_CREATED);

        lfe2.add3LevelFailureTrigger(".*ERROR*.");
        lfe2.failOnUndefinedErrors();
        validateTest(lfe2);
    }

    public void testEnginePoolsize2() throws Exception {

        // setup call 1

        ConfigurationGroupMock.engineStackSize = 105;   // Must be larger than 100 to have effect at reuse
        ConfigurationGroupMock.traceEnabled = "true";
        ConfigurationGroupMock.engineccxmlpoolsize = 25;
        ConfigurationGroupMock.enginevxmlpoolsize = 40;


        LogFileExaminer lfe = runSimpleTest("config_1");

        lfe.add2LevelRequired(".*BTC: CallManagerMock.createInboundCall:onehostname.*");

        lfe.add2LevelRequired(TestEvent.CONFIG_ENGINE_POOLSIZE,25);

        lfe.add2LevelRequired(TestEvent.ENGINE_STACKSIZE,105);
        lfe.add2LevelRequired(TestEvent.ENGINE_TRACE,true);

        lfe.add2LevelRequired(TestEvent.CONFIG_ENGINE_POOLSIZE,40);

        lfe.failOnUndefinedErrors();
        lfe.add3LevelFailureTrigger(".*ERROR*.");
        validateTest(lfe);

        // setup call 2

        ConfigurationGroupMock.engineStackSize = stackSize;
        ConfigurationGroupMock.traceEnabled = "false";
        ConfigurationGroupMock.engineccxmlpoolsize = 45;
        ConfigurationGroupMock.enginevxmlpoolsize = 80;


        LogFileExaminer lfe2 = runSimpleTest("config_1");

        lfe2.add2LevelRequired(TestEvent.CONFIG_ENGINE_POOLSIZE,45);

        lfe2.add2LevelRequired(TestEvent.ENGINE_STACKSIZE,stackSize++);
        lfe2.add2LevelRequired(TestEvent.ENGINE_TRACE,false);

        lfe2.add2LevelRequired(TestEvent.CONFIG_ENGINE_POOLSIZE,80);

        lfe2.add3LevelFailureTrigger(".*ERROR*.");
        lfe2.failOnUndefinedErrors();
        validateTest(lfe2);
    }

}
