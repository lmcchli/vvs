package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;

import com.mobeon.masp.execution_engine.runapp.mock.CallManagerMock;
import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;
import com.mobeon.masp.execution_engine.runtime.Msgs;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: May 2, 2006
 * Time: 11:58:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationCCXMLRejectTagTest extends ApplicationBasicTestCase<ApplicationCCXMLRejectTagTest> {

     static {
        testLanguage("ccxml");
        testSubdir("reject");
        testCases(
                testCase("reject_1"),
                testCase("reject_no_eventtype_no_reason"),
                testCase("reject_eventtype"),
                testCase("reject_reason"),
                testCase("reject_eventtype_reason")
                );
         store(ApplicationCCXMLRejectTagTest.class);
    }


    /**
     * Creates this test case
     */
    public ApplicationCCXMLRejectTagTest(String event)
    {
        super (event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setup
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationCCXMLRejectTagTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationCCXMLRejectTagTest.class);
    }

    /**
     * Tests that a reject when handling error.connection does not result in
     * NullPointerExceoption (we used to have such an exception)
     *
     * @throws Exception
     */
    public void testCCXMLRejectTag1() throws Exception {

        setResponseToAccept(CallManagerMock.EventType.ERROR_CONNECTION);

        LogFileExaminer lfe = runSimpleTest("reject_1");
        lfe.addIgnored(".*error.connection.*");
        lfe.addIgnored(".*SessionInfoMock.*");
        lfe.addIgnored(".*transition event.*error.*\\*.*");
        lfe.addIgnored(".*error\\.notallowed.*");
        lfe.addIgnored(".*is in state ERROR, forced disconnect.*");
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        lfe.add3LevelFailureTrigger(".*\\sTCFAIL.*");
        validateTest(lfe);
        setResponseToAccept(CallManagerMock.EventType.CONNECTED_EVENT);
    }

    /**
     * Test if the reject tag is used without its rejecteventtype and reason attributes: 
     * the reject tag will compile; no reject event type and the default reason be received by 
     * the InboundCall object.
     *
     * @throws Exception
     */
    public void testCCXMLRejectTagNoRejectEventTypeAndNoReason() throws Exception {

    	InboundCallMock icm = createCall("reject_no_eventtype_no_reason");
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        validateTest(lfe);
        
        // Verify the reject event type name and reject reason
        String rejectEventTypeName = icm.getRejectEventTypeName();
        if(rejectEventTypeName != null)
        {
        	fail("Expecting null reject event type but got: " + rejectEventTypeName);
        }
        String rejectReason = icm.getRejectReason();
        if(!Msgs.message(Msgs.CALL_REJECTED).equals(rejectReason)){
        	fail("Expecting reject reason to be \"" + Msgs.message(Msgs.CALL_REJECTED) + "\" but got: " + rejectReason);
        }
    }

    /**
     * Test if the reject tag is used with its rejecteventtype attribute: 
     * the reject tag will compile; the reject event type will be parsed correctly; and
     * the given reject event type and default reason be received by the InboundCall object.
     *
     * @throws Exception
     */
    public void testCCXMLRejectTagRejectEventType() throws Exception {

    	InboundCallMock icm = createCall("reject_eventtype");
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        validateTest(lfe);
        
        // Verify the reject event type name and reject reason
        String rejectEventTypeName = icm.getRejectEventTypeName();
        if(!"_488_NOT_ACCEPTABLE_HERE".equals(rejectEventTypeName))
        {
        	fail("Expecting _488_NOT_ACCEPTABLE_HERE reject event type but got: " + rejectEventTypeName);
        }
        String rejectReason = icm.getRejectReason();
        if(!Msgs.message(Msgs.CALL_REJECTED).equals(rejectReason)){
        	fail("Expecting reject reason to be \"" + Msgs.message(Msgs.CALL_REJECTED) + "\" but got: " + rejectReason);
        }
    }    

    /**
     * Test if the reject tag is used without its reason attribute: 
     * the reject tag will compile; no reject event type and the given reason be received by 
     * the InboundCall object.
     *
     * @throws Exception
     */
    public void testCCXMLRejectTagReason() throws Exception {

    	InboundCallMock icm = createCall("reject_reason");
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        validateTest(lfe);
        
        // Verify the parsed reject event type name and reject reason
        String rejectEventTypeName = icm.getRejectEventTypeName();
        if(rejectEventTypeName != null)
        {
        	fail("Expecting null reject event type but got: " + rejectEventTypeName);
        }
        String rejectReason = icm.getRejectReason();
        if(!"A reason to reject.".equals(rejectReason)){
        	fail("Expecting \"A reason to reject.\" but got: " + rejectReason);
        }
    }

    /**
     * Test if the reject tag is used with its rejecteventtype and reason attributes: 
     * the reject tag will compile; the given reject event type and reason will be parsed  
     * correctly and be received by the InboundCall object.
     *
     * @throws Exception
     */
    public void testCCXMLRejectTagRejectEventTypeAndReason() throws Exception {

    	InboundCallMock icm = createCall("reject_eventtype_reason");
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.add2LevelRequired(".*\\sTCPASS.*");
        validateTest(lfe);
        
        // Verify the parsed reject event type name and reject reason
        String rejectEventTypeName = icm.getRejectEventTypeName();
        if(!"_488_NOT_ACCEPTABLE_HERE".equals(rejectEventTypeName))
        {
        	fail("Expecting _488_NOT_ACCEPTABLE_HERE reject event type but got: " + rejectEventTypeName);
        }
        String rejectReason = icm.getRejectReason();
        if(!"A reason to reject.".equals(rejectReason)){
        	fail("Expecting \"A reason to reject.\" but got: " + rejectReason);
        }
    }    
}
