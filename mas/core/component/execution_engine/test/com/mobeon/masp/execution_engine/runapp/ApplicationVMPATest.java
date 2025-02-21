package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.RedirectingParty;
import com.mobeon.masp.execution_engine.runapp.mock.InboundCallMock;

import junit.framework.Test;

public class ApplicationVMPATest extends ApplicationBasicTestCase<ApplicationVMPATest> {

    /**
     * The list of all testcases that we need to execute
     */

    static {
         testLanguage("vmpa");
         testSubdir("");
         testCases(
                 testCase("vmpa_provisioned_sub_divert_deposit"),
                 testCase("vmpa_provisioned_sub_direct_deposit"),
                 testCase("vmpa_provisioned_sub_short_code_retrieval"),
                 testCase("vmpa_long_dial_retrieval"),
                 testCase("vmpa_legacy_sub_divert_deposit"),
                 testCase("vmpa_legacy_sub_direct_deposit"),
                 testCase("vmpa_legacy_sub_short_code_retrieval"),
                 testCase("vmpa_unknown_sub_divert_deposit"),
                 testCase("vmpa_unknown_sub_direct_deposit"),
                 testCase("vmpa_unknown_sub_short_code_retrieval"),
                 testCase("vmpa_redirecting_number_empty_string"),
                 testCase("vmpa_redirecting_number_undefined"),
                 testCase("vmpa_called_number_empty_string"),
                 testCase("vmpa_called_number_undefined"),
                 testCase("vmpa_called_number_unknown"),
                 testCase("vmpa_opco_vip_empty_string_divert_deposit"),
                 testCase("vmpa_opco_vip_undefined_divert_deposit"),
                 testCase("vmpa_opco_port_empty_string_divert_deposit"),
                 testCase("vmpa_opco_port_undefined_divert_deposit"),
                 testCase("vmpa_opco_vip_empty_string_direct_deposit"),
                 testCase("vmpa_opco_vip_undefined_direct_deposit"),
                 testCase("vmpa_opco_port_empty_string_direct_deposit"),
                 testCase("vmpa_opco_port_undefined_direct_deposit"),
                 testCase("vmpa_opco_vip_empty_string_short_code_retrieval"),
                 testCase("vmpa_opco_vip_undefined_short_code_retrieval"),
                 testCase("vmpa_opco_port_empty_string_short_code_retrieval"),
                 testCase("vmpa_opco_port_undefined_short_code_retrieval"),
                 testCase("vmpa_opco_database_unreachable_divert_deposit"),
                 testCase("vmpa_opco_database_unreachable_direct_deposit"),
                 testCase("vmpa_opco_database_unreachable_short_code_retrieval"),
                 testCase("vmpa_load_compile_ccxml")
                 );
        store(ApplicationVMPATest.class);
     }

    /**
     * Creates this test case
     */
    public ApplicationVMPATest (String event)
    {
      super (event, "test_log_info.xml");
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setup
     * only !!!
     *
     * @return a test suite
     */
    public static Test suite() {
        return genericSuite(ApplicationVMPATest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVMPATest.class);
    }

    public void testVMPAProvisionedSubDivertDeposit() {
        LogFileExaminer lfe = runSimpleTest("vmpa_provisioned_sub_divert_deposit");
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*lookup error.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.add2LevelRequired(".*\\sDivert Deposit proxy start.*");
        lfe.add2LevelRequired(".*\\sproxy succeeded.*");
        validateTest(lfe);
    }
    
    public void testVMPAProvisionedSubDirectDeposit() {
    	String service = "vmpa_provisioned_sub_direct_deposit";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12185142223333");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*lookup error.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.add2LevelRequired(".*\\sDirect Deposit proxy start.*");
        lfe.add2LevelRequired(".*\\sproxy succeeded.*");
        validateTest(lfe);
    }

    public void testVMPAProvisionedSubShortCodeRetrieval() {
    	String service = "vmpa_provisioned_sub_short_code_retrieval";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12175143457900");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*lookup error.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.add2LevelRequired(".*\\sShort Code Retrieval proxy start.*");
        lfe.add2LevelRequired(".*\\sproxy succeeded.*");
        validateTest(lfe);
    }

    public void testVMPAProvisionedLongDialRetrieval() {
    	String service = "vmpa_long_dial_retrieval";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "6121121");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*lookup error.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.add2LevelRequired(".*\\sLong Dial Retrieval B2BUA start.*");
        lfe.add2LevelRequired(".*\\sconnection connected successfully.*");
        validateTest(lfe);
    }
    
    public void testVMPALegacySubDivertDeposit() {
        LogFileExaminer lfe = runSimpleTest("vmpa_legacy_sub_divert_deposit");
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*lookup error.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.add2LevelRequired(".*\\sDivert Deposit legacy reject.*");
        validateTest(lfe);
    }
    
    public void testVMPALegacySubDirectDeposit() {
    	String service = "vmpa_legacy_sub_direct_deposit";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12185142223333");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*lookup error.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.add2LevelRequired(".*\\sDirect Deposit legacy reject.*");
        validateTest(lfe);
    }

    public void testVMPALegacySubShortCodeRetrieval() {
    	String service = "vmpa_legacy_sub_short_code_retrieval";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12175143457900");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*lookup error.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.add2LevelRequired(".*\\sShort Code Retrieval legacy reject.*");
        validateTest(lfe);
    }

    public void testVMPAUnknownSubDivertDeposit() {
        LogFileExaminer lfe = runSimpleTest("vmpa_unknown_sub_divert_deposit");
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sDivert Deposit unknown subscriber reject.*");
        validateTest(lfe);
    }

    public void testVMPAUnknownSubDirectDeposit() {
    	String service = "vmpa_unknown_sub_direct_deposit";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12185142223333");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sDirect Deposit unknown subscriber reject.*");
        validateTest(lfe);
    }

    public void testVMPAUnknownSubShortCodeRetrieval() {
    	String service = "vmpa_unknown_sub_short_code_retrieval";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12175143457900");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sShort Code Retrieval unknown subscriber reject.*");
        validateTest(lfe);
    }

    public void testVMPARedirectingNumberEvaluatesToEmptyString() {
    	String service = "vmpa_redirecting_number_empty_string";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        RedirectingParty c = new RedirectingParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12175143457777");
        setupCInformation(c, "");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, c, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        //An empty string redirecting number is taken to mean there was no diversion;
        //the short code in the B number and the ccxml test setup will cause proxying of the call.
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*lookup error.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.add2LevelRequired(".*\\sShort Code Retrieval proxy start.*");
        lfe.add2LevelRequired(".*\\sproxy succeeded.*");
        validateTest(lfe);    	
    }

    public void testVMPARedirectingNumberEvaluatesToUndefined() {
    	//Creating a RedirectingParty object with a null telephone number did not work to give an 
    	//undefined analyzed number (because of implementation in CCXMLMirror.java(?); doing 
    	//connection.redirect[0].number in ccxml, gave an empty string).  
    	//So, the ccxml loaded is setup for the number analysis to return undefined for the redirecting 
    	//number and we do not have to make the C number undefined here in the Call setup.
        LogFileExaminer lfe = runSimpleTest("vmpa_redirecting_number_undefined");
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sRedirecting number is undefined.*");
        validateTest(lfe);
    }

    public void testVMPACalledNumberEvaluatesToEmptyString() {
    	String service = "vmpa_called_number_empty_string";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sCalled number is an empty string.*");
        validateTest(lfe);    	
    }

    public void testVMPACalledNumberEvaluatesToUndefined() {
    	//Creating a CalledParty object with a null telephone number did not work to give an 
    	//undefined analyzed number.  
    	//So, the ccxml loaded is setup for the number analysis to return undefined for the called 
    	//number and we do not have to make the B number undefined here in the Call setup.
    	
    	String service = "vmpa_called_number_undefined";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12175143457900");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sCalled number is undefined.*");
        validateTest(lfe);    	
    }

    public void testVMPACalledNumberEvaluatesToUnknown() {
    	String service = "vmpa_called_number_unknown";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "33335143457900"); //set B number to unknown number
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sCalled number is unknown.*");
        validateTest(lfe);    	
    }

    public void testVMPAOpcoVipEmptyStringDivertDeposit() {
        LogFileExaminer lfe = runSimpleTest("vmpa_opco_vip_empty_string_divert_deposit");
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sDivert Deposit Opco VIP is an empty string.*");
        validateTest(lfe);
    }
    
    public void testVMPAOpcoVipUndefinedDivertDeposit() {
        LogFileExaminer lfe = runSimpleTest("vmpa_opco_vip_undefined_divert_deposit");
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sDivert Deposit Opco VIP is undefined.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoPortEmptyStringDivertDeposit() {
        LogFileExaminer lfe = runSimpleTest("vmpa_opco_port_empty_string_divert_deposit");
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sDivert Deposit Opco port is an empty string.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoPortUndefinedDivertDeposit() {
        LogFileExaminer lfe = runSimpleTest("vmpa_opco_port_undefined_divert_deposit");
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sDivert Deposit Opco port is undefined.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoVipEmptyStringDirectDeposit() {
    	String service = "vmpa_opco_vip_empty_string_direct_deposit";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12185142223333");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sDirect Deposit Opco VIP is an empty string.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoVipUndefinedDirectDeposit() {
    	String service = "vmpa_opco_vip_undefined_direct_deposit";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12185142223333");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sDirect Deposit Opco VIP is undefined.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoPortEmptyStringDirectDeposit() {
    	String service = "vmpa_opco_port_empty_string_direct_deposit";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12185142223333");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sDirect Deposit Opco port is an empty string.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoPortUndefinedDirectDeposit() {
    	String service = "vmpa_opco_port_undefined_direct_deposit";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12185142223333");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sDirect Deposit Opco port is undefined.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoVipEmptyStringShortCodeRetrieval() {
    	String service = "vmpa_opco_vip_empty_string_short_code_retrieval";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12175143457900");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sShort Code Retrieval Opco VIP is an empty string.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoVipUndefinedhortCodeRetrieval() {
    	String service = "vmpa_opco_vip_undefined_short_code_retrieval";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12175143457900");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sShort Code Retrieval Opco VIP is undefined.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoPortEmptyStringShortCodeRetrieval() {
    	String service = "vmpa_opco_port_empty_string_short_code_retrieval";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12175143457900");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sShort Code Retrieval Opco port is an empty string.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoPortUndefinedShortCodeRetrieval() {
    	String service = "vmpa_opco_port_undefined_short_code_retrieval";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12175143457900");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sShort Code Retrieval Opco port is undefined.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoDatabaseUnreachableDivertDeposit() {
        LogFileExaminer lfe = runSimpleTest("vmpa_opco_database_unreachable_divert_deposit");
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*lookup error.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sDivert Deposit subscrber lookup problem reject.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoDatabaseUnreachableDirectDeposit() {
    	String service = "vmpa_opco_database_unreachable_direct_deposit";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12185142223333");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*lookup error.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sDirect Deposit subscrber lookup problem reject.*");
        validateTest(lfe);
    }

    public void testVMPAOpcoDatabaseUnreachableShortCodeRetrieval() {
    	String service = "vmpa_opco_database_unreachable_short_code_retrieval";
    	
    	// Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        setupAInformation(a, "5143457900");
        setupBInformation(b, "12175143457900");
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, null, callType, mimeType, inboundBitRate);      
        boolean exited = waitForCallCompletion(icm, 10000);
        TestAppender.stopSave(log);
        if (!exited) {
            fail("The application timed out!");
        }
        
        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors();
        lfe.addIgnored(".*Error parsing.*");
        lfe.addIgnored(".*lookup error.*");
        lfe.addIgnored(".*REJECT_REASON.*ERROR.*");
        lfe.addIgnored(".*LOOKUP_ERROR.*");
        lfe.addIgnored(".*error.*CCXML.*");
        lfe.addIgnored(".*Evaluation of.*");
        lfe.add2LevelRequired(".*\\sShort Code Retrieval subscrber lookup problem reject.*");
        validateTest(lfe);
    }

    public void testVMPALoadsAndCompiles(){
    	/** This is a quick test to ensure that the real VMPA ccxml
    		loads and compiles with no errors.  You need to:
    		- put the ccxml to be tested in the runapp/applications/vmpa directory
    		- put the name of your ccxml in vmpa_load_compile_ccxml.xml (default is incoming_call.ccxml)
    		- uncomment the line below to run a simple test    		
    	 */
    	//runSimpleTest("vmpa_load_compile_ccxml");
    }
}
