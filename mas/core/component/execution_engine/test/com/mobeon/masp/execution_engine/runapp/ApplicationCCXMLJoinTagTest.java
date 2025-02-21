package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

/**
 * Test suite for the subdialog tag.
 */
public class ApplicationCCXMLJoinTagTest extends ApplicationBasicTestCase<ApplicationCCXMLJoinTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("ccxml");
        testSubdir("join");
        testCases(
                testCase("join-unjoined"),
                testCase("join-has-dialog")
        );
        store(ApplicationCCXMLJoinTagTest.class);
    }


    /**
     * Creates this test case
     */
    public ApplicationCCXMLJoinTagTest(String event) {
        super(event);
        log.info("MOCK: Setting up " + event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLJoinTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLJoinTagTest.class);
    }

    /**
     * Tests joining two connections which is not previousle joined
     *
     * @throws Exception
     */
    public void testJoinUnjoined() throws Exception {

        setTestCaseTimeout(20000);
        LogFileExaminer lfe = runSimpleTest("join-unjoined");

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sjoin-unjoined-001.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");

        validateTest(lfe);
    }

    /**
     * Tests joining two connections when one of them is
     * already joined to a dialog with full duplex.
     *
     * @throws Exception
     */
    public void testJoinHasDialog() throws Exception {

        setTestCaseTimeout(20000);
        LogFileExaminer lfe = runSimpleTest("join-has-dialog");

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sjoin-unjoined-001.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");

        validateTest(lfe);
    }
}
