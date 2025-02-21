package com.mobeon.masp.execution_engine.runapp;

import static com.mobeon.masp.util.test.MASTestSwitches.*;
import static com.mobeon.masp.execution_engine.util.TestEventGenerator.*;
import static com.mobeon.masp.execution_engine.util.TestEvent.*;

import com.mobeon.masp.execution_engine.Case;
import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import junit.framework.Test;

/**
 * Tests the application watchdog functionality
 *
 * @author Mikael Andersson
 */
public class ApplicationWatchdogTest extends ApplicationBasicTestCase<ApplicationWatchdogTest> {

    static {
        enableAutomaticXML();
        testLanguage("misc");
        testSubdir("watchdog");
        ApplicationBasicTestCase.testCases(
                testCase("watchdog_1"),
                testCase("watchdog_2"),
                testCase("watchdog_3"),
                testCase("watchdog_4"),
                testCase("watchdog_5"),
                testCase("watchdog_6")
        );
        store(ApplicationWatchdogTest.class);
    }
    /**
     * Creates this test case
     */
    public ApplicationWatchdogTest(String event) {
        super(event, "test_log.xml");
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationWatchdogTest.class);
    }

    public void setUp() throws Exception {
        genericSetUp(ApplicationWatchdogTest.class);
    }

    public static void beforeSuite() {
        enableWatchdogTesting();        
    }

    protected void beforeEachTest() {
        super.beforeEachTest();
    }

    protected void afterEachTest() {
        super.afterEachTest();
    }

    /**
     * Test that if you do <accept/> , but nothing more, we
     * can detect that
     *
     * @throws Exception
     */
    public void testHangAfterAccept() throws Exception {
        setTestCaseTimeout(10000);
        LogFileExaminer lfe = runSimpleTest("watchdog_1", true);
        lfe.add2LevelRequired(".*WARN Dumping active CCXML context.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);
    }

    public void testHangInTerminalState() throws Exception {
        setTestCaseTimeout(15000);
        // Start saving the log
        runSimpleTest("watchdog_2");

        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors(true);
        lfe.add2LevelRequired(".*\\sTCPASS1.*");
        lfe.add2LevelRequired(".*\\sTCPASS2.*");
        lfe.add2LevelRequired(".*WARN Dumping active CCXML context.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);
    }

    public void testHangInTerminalStateAfterDisconnectEvent() throws Exception {
        setTestCaseTimeout(15000);
        declareWait(ACCEPT);
        InboundCallMock icm = runSimpleInteractiveTest("watchdog_3");
        waitFor(ACCEPT,5000);
        declareNoWait();
        icm.sleep(1500);
        icm.disconnectCall();
        waitSimpleInteractiveTest(icm);

        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors(true);
        lfe.add2LevelRequired(".*\\sTCPASS1.*");
        lfe.add2LevelRequired(".*WARN Dumping active CCXML context.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);
    }

    public void testMissedDialogExit() throws Exception {
        setTestCaseTimeout(15000);
        InboundCallMock icm = runSimpleInteractiveTest("watchdog_4");
        waitSimpleInteractiveTest(icm);

        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors(true);
        lfe.add2LevelRequired(".*\\sTCPASS1.*");
        lfe.add2LevelRequired(".*WARN Dumping active CCXML context.*");
        lfe.add2LevelRequired(".*\\sTCPASS2.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);
    }

    public void testMissedDialogTransfer() throws Exception {
        setTestCaseTimeout(15000);
        InboundCallMock icm = runSimpleInteractiveTest("watchdog_5");
        waitSimpleInteractiveTest(icm);

        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors(true);
        lfe.add2LevelRequired(".*\\sTCPASS1.*");
        lfe.add2LevelRequired(".*WARN Dumping active CCXML context.*");
        lfe.add2LevelRequired(".*\\sTCPASS2.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);
    }

    public void testOutboundCall() throws Exception {
        setTestCaseTimeout(15000);
        InboundCallMock icm = runSimpleInteractiveTest("watchdog_6");
        waitSimpleInteractiveTest(icm);

        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors(true);
        lfe.add2LevelRequired(".*\\sTCPASS1.*");
        lfe.add2LevelRequired(".*WARN Dumping active CCXML context.*");
        lfe.add2LevelRequired(".*\\sTCPASS2.*");
        lfe.add1LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);
    }

}
