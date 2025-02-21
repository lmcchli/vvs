package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.stream.RecordFinishedEvent;
import junit.framework.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jan 4, 2006
 * Time: 9:43:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLGotoTagTest extends ApplicationBasicTestCase<ApplicationVXMLGotoTagTest> {

    static {
        testLanguage("vxml");
        testSubdir("goto");
        testCases(
                testCase("goto_1"),
                testCase("goto_2"),
                testCase("goto_3"),
                testCase("goto_4"),
                testCase("goto_5"),
                testCase("goto_6"),
                testCase("goto_7"),
                testCase("goto_8"),
                testCase("goto_9"),
                testCase("goto_10"),
                testCase("goto_11"),
                testCase("goto_12"),
                testCase("goto_13"),
                testCase("goto_14"),
                testCase("goto_15"),
                testCase("goto_16"),
                testCase("goto_17"),
                testCase("goto_18"),
                testCase("goto_19"),
                testCase("goto_20"),
                testCase("goto_21"),
                testCase("goto_22"),
                testCase("goto_23"),
                testCase("goto_24"),
                testCase("goto_25"),
                testCase("goto_26"),
                testCase("goto_27"),
                testCase("goto_28"),
                testCase("goto_29"),
                testCase("goto_30"),
                testCase("goto_31"),
                testCase("goto_32"),
                testCase("goto_33"),
                testCase("goto_34"),
                testCase("goto_35"),
                testCase("goto_36"),
                testCase("goto_37"),
                testCase("goto_38"),
                testCase("goto_39"),
                testCase("goto_40"),
                testCase("goto_41")
        );
        store(ApplicationVXMLGotoTagTest.class);

    }

    /**
     * Creates this test case
     */
    public ApplicationVXMLGotoTagTest(String event) {
        // Use only info-logging since running all test cases with debug log takes too much memory...
        super(event, "test_log_info.xml");
    }

    /**
     * Defines the test suite for ApplicationGotoTagTest
     *
     * @return the new testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLGotoTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLGotoTagTest.class);
    }

    /**
     * Verify that it is possible to goto from a root to a leaf document.
     * Global variables in the root doc shall still be visible. Local
     * variables in the form where goto was executed shall not be visible.
     * Use "next" to identify the target. (conf 53, 56, 59)
     *
     * @throws Exception
     */
    public void testVXMLGotoTag1() throws Exception {

        // Setup the call
        String testCase = "goto_1";
        setUpAndRunTest(testCase);
    }

    private void setUpAndRunTest(String testCase) {


        boolean exited = createCallAndWaitForCompletion(testCase, 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        setupCommonVerification();
    }

    /**
     * Verify that it is possible to goto from a leaf to a leaf document.
     * Global variables in the root doc shall still be visible.
     * Global variables in the entered leaf document shall be
     * visible. (conf 72)
     *
     * @throws Exception
     */
    public void testVXMLGotoTag2() throws Exception {

        // Setup the call
        String testCase = "goto_2";
        boolean exited = createCallAndWaitForCompletion(testCase, 20000);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\shello1.*");
        lfe.add2LevelRequired(".*\\sthe first leaf doc.*");
        lfe.add2LevelRequired(".*\\sgo to to next leaf doc.*");
        lfe.add2LevelRequired(".*\\sIn the next leaf doc.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that it is possible to perform goto from a form to another in
     * the same document using only a fragment identifier. Global variables
     * shall be intact. (conf 533)
     *
     * @throws Exception
     */
    public void testVXMLGotoTag3() throws Exception {

        // Setup the call
        String testCase = "goto_3";
        setUpAndRunTest(testCase);
    }

    /**
     * Verify that it is possible to goto from a leaf to a root document.
     * Global variables in the root doc shall still be visible. A root
     * document variable set from the leaf document shall retain the
     * value when execution resumes in the root doc. Use "expr" to
     * identify the target. (conf 73)
     *
     * @throws Exception
     */
    public void testVXMLGotoTag4() throws Exception {

        // Setup the call
        String testCase = "goto_4";
        setUpAndRunTest(testCase);
    }

    /**
     * verify that if a goto is done from the root document to itself using
     * absolute URI, all variables are reinitialized. (conf 75)
     *
     * @throws Exception
     */
    public void testVXMLGotoTag5() throws Exception {

        // Setup the call
        String testCase = "goto_5";
        setUpAndRunTest(testCase);
    }


    /**
     * verify that if a goto is done from the root document to itself
     * using relative URI before the fragment identifier, all variables
     * are reinitialized. (conf 75)
     *
     * @throws Exception
     */
    public void testVXMLGotoTag6() throws Exception {

        // Setup the call
        String testCase = "goto_6";
        setUpAndRunTest(testCase);
    }

    /**
     * verify that if a goto is done from the leaf document to itself using
     * absolute URI, all variables are reinitialized. (conf 534)passed
     *
     * @throws Exception
     */
    public void testVXMLGotoTag7() throws Exception {

        // Setup the call
        String testCase = "goto_7";
        setUpAndRunTest(testCase);
    }

    private void setupCommonVerification() {
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * verify that if a goto is done from the leaf document to itself using
     * relative URI before the fragment identifier, all variables are
     * reinitialized.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag8() throws Exception {

        // Setup the call
        String testCase = "goto_8";
        setUpAndRunTest(testCase);
    }


    /**
     * Verify that it is possible to goto the second form in the target
     * document by using fully qualified URI and a fragment identifier.
     * (conf 528)
     *
     * @throws Exception
     */
    public void testVXMLGotoTag9() throws Exception {

        // Setup the call
        String testCase = "goto_9";
        setUpAndRunTest(testCase);
    }

    /**
     * Verify that it is possible to goto the second form in the target
     * document by using fully qualified URI and a fragment identifier.
     * "expr" shall be used to identify the target. (conf 530)
     *
     * @throws Exception
     */
    public void testVXMLGotoTag10() throws Exception {

        // Setup the call
        String testCase = "goto_10";
        setUpAndRunTest(testCase);
    }

    /**
     * Verify that it is possible to goto the second form in the target
     * document by using a relative URI and a fragment identifier.
     * (conf 529)
     *
     * @throws Exception
     */
    public void testVXMLGotoTag11() throws Exception {

        // Setup the call
        String testCase = "goto_11";
        setUpAndRunTest(testCase);
    }

    /**
     * Verify that it is possible to perform goto betwen two documents 100
     * times.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag12() throws Exception {

        // Setup the call
        String testCase = "goto_12";
        boolean exited = createCallAndWaitForCompletion(testCase, 120000);
        if (!exited) {
            fail("The application timed out!");
        }

        setupCommonVerification();
    }

    /**
     * Verify that it is possible to perform goto from a document level catch
     * to another form.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag13() throws Exception {

        // Setup the call
        String testCase = "goto_13";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    /**
     * Verify that it is possible to goto the first form in another
     * document by using only the document name in the goto (i.e a
     * fragment identifier identifying the form shall not be used
     * in this case). (conf 527)
     *
     * @throws Exception
     */
    public void testVXMLGotoTag14() throws Exception {

        // Setup the call
        String testCase = "goto_14";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }

        setupCommonVerification();
    }

    /**
     * Verify that it is possible to goto the first form in another
     * document by using  fully qualified URI in the goto (i.e a
     * fragment identifier identifying the form shall not be used
     * in this case). (conf 526)
     *
     * @throws Exception
     */
    public void testVXMLGotoTag15() throws Exception {

        // Setup the call
        String testCase = "goto_15";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }

        setupCommonVerification();
    }

    /**
     * Verify that it is possible to goto the same form again.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag16() throws Exception {

        // Setup the call
        String testCase = "goto_16";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }

        setupCommonVerification();
    }

    /**
     * Verify that error.badfetch will be thrown if an
     * attempt to goto a nonexisting document is attempted,
     * using next (conf 531).
     *
     * @throws Exception
     */
    public void testVXMLGotoTag17() throws Exception {

        // Setup the call
        String testCase = "goto_17";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    private void setUpVerificationForBadfetch() {
        // Since the logfile will contain "error.badfetch" and Error
        // although it is not an error in the EE,
        // we must setup special verification rules.
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*error.*");
        lfe.addIgnored(".*ERROR.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that goto to "#" will yield the first form in the current
     * document.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag18() throws Exception {

        // Setup the call
        String testCase = "goto_18";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }

        setupCommonVerification();
    }

    /**
     * Verify that ECMAScript code defined in the root
     * document can be referenced in the leaf document
     * after goto (conf 61).
     *
     * @throws Exception
     */
    public void testVXMLGotoTag19() throws Exception {

        // Setup the call
        String testCase = "goto_19";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }

        setupCommonVerification();
    }

    /**
     * Verify that it is possible to goto in a chain of 5 documents.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag20() throws Exception {

        // Setup the call
        String testCase = "goto_20";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }

        setupCommonVerification();
    }

    /**
     * Verify that goto "" will yield error.badfetch.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag21() throws Exception {

        // Setup the call
        String testCase = "goto_21";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }

        setUpVerificationForBadfetch();
    }

    /**
     * Verify that it is possible to go from the third
     * form item to the first by using nextitem.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag22() throws Exception {

        // Setup the call
        String testCase = "goto_22";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }

        setupCommonVerification();
    }

    /**
     * Verify that it is possible to go from the third
     * form item to the first by using expritem.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag23() throws Exception {

        // Setup the call
        String testCase = "goto_23";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }

        setupCommonVerification();
    }

    /**
     * Verify that it is possible to go from the first form item
     * to the third by using nextitem. The third item shall be
     * considered visited after being executed.
     */

    public void testVXMLGotoTag24() throws Exception {

        // Setup the call
        String testCase = "goto_24";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setupCommonVerification();
    }

    /**
     * Verify that it is possible to go from the third form item to the
     * first by using nextitem. The condition of the form item shall be false.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag25() throws Exception {

        // Setup the call
        String testCase = "goto_25";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setupCommonVerification();
    }

    /**
     * Verify that it is possible to go from the third form item to the
     * first by using expritem. The condition of the form item shall be false.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag26() throws Exception {

        // Setup the call
        String testCase = "goto_26";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setupCommonVerification();
    }

    /**
     * Verify that it is possible to do "goto nextitem" to a field.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag27() throws Exception {

        // Setup the call
        String testCase = "goto_27";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        // Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that it is possible to do "goto nextitem" to a record.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag28() throws Exception {

        InboundCallMock icm = createCall("goto_28");
        icm.setRecordFinished(4000,
                RecordFinishedEvent.CAUSE.RECORDING_STOPPED);
        icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(100000);
        if (!exited) {
            fail("The application timed out!");
        }
// Verify the output
        List<String> l = TestAppender.getOutputList();
        LogFileExaminer lfe = new LogFileExaminer();

        lfe.failOnUndefinedErrors();

        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        boolean success = lfe.evaluateLogFile(l);
        if (!success) {
            fail(lfe.getReason());
        }
    }

    /**
     * Verify that error.badfetch is thrown if there is a goto to
     * a nonexisting form item, using nextitem.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag29() throws Exception {

        // Setup the call
        String testCase = "goto_29";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    /**
     * Verify that error.badfetch will be thrown if an
     * attempt to goto a nonexisting document is attempted,
     * using expr (conf 532).
     *
     * @throws Exception
     */
    public void testVXMLGotoTag30() throws Exception {

        // Setup the call
        String testCase = "goto_30";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    /**
     * Verify that error.badfetch is thrown if there
     * is a goto to an existing form item in another form,
     * using nextitem.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag31() throws Exception {

        // Setup the call
        String testCase = "goto_31";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    /**
     * Verify that going to a document which does not
     * have any dialog, using the style goto next="docname"
     * will result in error.badfetch.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag32() throws Exception {

        // Setup the call
        String testCase = "goto_32";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    /**
     * Verify that error.badfetch will be thrown if an attempt
     * to goto a existing document, but a nonexisting form, is
     * attempted, using next.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag33() throws Exception {

        // Setup the call
        String testCase = "goto_33";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    /**
     * Verify that error.badfetch will be thrown if an attempt
     * to goto a existing document, but a nonexisting form,
     * is attempted, using expr.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag34() throws Exception {

        // Setup the call
        String testCase = "goto_34";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    /**
     * Verify that goto to "an expression evaluating to true" (the boolean
     * value) will yield error.badfetch, even if there is a target called
     * "true" (the string).
     *
     * @throws Exception
     */
    public void testVXMLGotoTag35() throws Exception {

        // Setup the call
        String testCase = "goto_35";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    /**
     * Verify that an interapplication goto results in a badfetch.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag36() throws Exception {

        // Setup the call
        String testCase = "goto_36";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    /**
     * Verify that using both next and nextitem results in error.badfetch.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag37() throws Exception {

        // Setup the call
        String testCase = "goto_37";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    /**
     * Verify that using more than one of nextitem or expritem
     * results in error.badfetch.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag38() throws Exception {

        // Setup the call
        String testCase = "goto_38";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    /**
     * verify that it is possible to go from root to leaf,
     * and then from form to form, with ECMA in the root scope
     * intact.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag39() throws Exception {

        // Setup the call
        String testCase = "goto_39";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setUpVerificationForBadfetch();
    }

    /**
     * verify that catches gets popped correctly when moving between documents
     *
     * @throws Exception
     */
    public void testVXMLGotoTag40() throws Exception {

        // Setup the call
        String testCase = "goto_40";
        boolean exited = createCallAndWaitForCompletion(testCase, 30000);
        if (!exited) {
            fail("The application timed out!");
        }
        setupCommonVerification();
    }

    /**
     * Verify that it is possible to go from root to leaf to leaf to root
     * many times.
     *
     * @throws Exception
     */
    public void testVXMLGotoTag41() throws Exception {

        // Setup the call
        String testCase = "goto_41";
        boolean exited = createCallAndWaitForCompletion(testCase, 60000);
        if (!exited) {
            fail("The application timed out!");
        }
        setupCommonVerification();
    }


}
