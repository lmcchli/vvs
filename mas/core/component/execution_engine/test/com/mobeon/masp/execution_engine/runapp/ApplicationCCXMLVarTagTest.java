package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import java.util.List;

/**
 * This test suite the var tag in CCXML.
 */
public class ApplicationCCXMLVarTagTest extends ApplicationBasicTestCase<ApplicationCCXMLVarTagTest> {

    /**
     * The list of all testcases that we need to execute
     */
    static {
        testLanguage("ccxml");
        testSubdir("var");
        testCases(
                new ApplicationTestCase("var_1", "test:/test/com/mobeon/masp/execution_engine/runapp/applications/ccxml/var/var_1.xml"),
                new ApplicationTestCase("var_2", "test:/test/com/mobeon/masp/execution_engine/runapp/applications/ccxml/var/var_2.xml"),
                new ApplicationTestCase("var_scope_1", "test:/test/com/mobeon/masp/execution_engine/runapp/applications/ccxml/var/var_scope_1.xml")
        );
        store(ApplicationCCXMLVarTagTest.class);
    }

    ;

    /**
     * Creates this test case
     */
    public ApplicationCCXMLVarTagTest(String event) {
        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLVarTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLVarTagTest.class);
    }

    /**
     * Tests a simple var tag construction without the expr attribute to see that it has the
     * value undefined. See var_1.ccxml.
     */
    public void testCCXMLVarTag1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("var_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sVar\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sVar\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests a simple var tag construction with expr attribute to see that its value have been
     * set correctly. See var_2.ccxml.
     */
    public void testCCXMLVarTag2() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("var_2", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sVar\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sVar\\sok\\s2.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sVar\\sok\\s3.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sVar\\sok\\s4.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

    /**
     * Tests a simple var tag construction to verify scope handling between ccxml and
     * transition scope. See var_scope_1.ccxml.
     */
    public void testCCXMLVarTagScopeDocumentTransition1() throws Exception {

        // Setup the call
        boolean exited = createCallAndWaitForCompletion("var_scope_1", 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sVar\\sok\\s1.*");
        lfe.add2LevelRequired(".*\\sTCPASS\\sCCXML:\\sVar\\sok\\s2.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        lfe.add3LevelFailureTrigger(".*\\sTCPASS.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success)
            fail(lfe.getReason());
    }

}