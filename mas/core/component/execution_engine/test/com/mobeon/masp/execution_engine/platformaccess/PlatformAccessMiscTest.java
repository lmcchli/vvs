/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.platformaccess.util.MessageTypeUtil;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.voicexml.runtime.PropertyStack;
import com.mobeon.masp.mailbox.DeliveryStatus;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.StoredMessageState;
import com.mobeon.masp.numberanalyzer.IAnalysisInput;
import com.mobeon.masp.numberanalyzer.NumberAnalyzerException;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;
import com.mobeon.masp.servicerequestmanager.ServiceRequest;
import com.mobeon.masp.servicerequestmanager.ServiceRequestManagerException;
import com.mobeon.masp.servicerequestmanager.ServiceResponse;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

/**
 * Test some miscellaneous function in PlatformAccess. For example numberanalyzer and service request functions.
 *
 * @author ermmaha
 */
public class PlatformAccessMiscTest extends PlatformAccessTest {

    public PlatformAccessMiscTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(PlatformAccessMiscTest.class);
    }

    /**
     * Tests the systemSetProperty function.
     *
     * @throws Exception if test case fails.
     */
    public void testSystemSetProperty() throws Exception {
        PropertyStack propertyStack = new PropertyStack();
        jmockExecutionContext.expects(once()).method("getProperties").will(returnValue(propertyStack));

        platformAccess.systemSetProperty("name", "value");
    }

    /**
     * Tests the systemGetServiceRequestParameter function.
     *
     * @throws Exception if test case fails.
     */
    public void testSystemGetServiceRequestParameter() throws Exception {
        // test the scenario when the ServiceRequest does not exist in the session
        try {
            platformAccess.systemGetServiceRequestParameter("number");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            String descr = e.getDescription();
            assertTrue(descr.indexOf("systemGetServiceRequestParameter") > -1);
        }

        // test the good scenario
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setParameter("number", "1234567");

        stubSession.setData(ISession.SERVICE_REQUEST, serviceRequest);

        String param = platformAccess.systemGetServiceRequestParameter("number");
        assertEquals("1234567", param);

        // test a parameter that does not exist in the ServiceRequest
        param = platformAccess.systemGetServiceRequestParameter("noname");
        assertTrue(param == null);
    }

    /**
     * Tests the systemSendServiceResponse function.
     *
     * @throws Exception if test case fails.
     */
    public void testSystemSendServiceResponse() throws Exception {
        Mock jmockServiceRequestManager = mock(IServiceRequestManager.class);
        jmockExecutionContext.stubs().method("getServiceRequestManager").will(returnValue(jmockServiceRequestManager.proxy()));

        jmockServiceRequestManager.expects(once()).method("sendResponse").with(isA(String.class), isA(ServiceResponse.class));
        // test without optional parameters
        platformAccess.systemSendServiceResponse(200, "Status Ok", null, null);

        jmockServiceRequestManager.expects(once()).method("sendResponse").with(isA(String.class), isA(ServiceResponse.class));
        // test with optional parameters
        platformAccess.systemSendServiceResponse(200, "Status Ok", new String[]{"param1", "param2"}, new String[]{"value1", "value2"});

        // test with invalid length on optional parameters arrays
        try {
            platformAccess.systemSendServiceResponse(200, "Status Ok", new String[]{"param1", "param2"}, new String[]{"value1"});
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            String descr = e.getDescription();
            assertTrue(descr.indexOf("systemSendServiceResponse") > -1);
        }

        // test exception
        jmockServiceRequestManager.stubs().method("sendResponse").will(throwException(new ServiceRequestManagerException("Error")));
        try {
            platformAccess.systemSendServiceResponse(200, "Status Ok", null, null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            String descr = e.getDescription();
            assertTrue(descr.indexOf("systemSendServiceResponse") > -1);
        }
    }

    /**
     * Tests the systemAnalyzeNumber function.
     *
     * @throws Exception if test case fails.
     */
    public void testSystemAnalyzeNumber() throws Exception {
        Mock jmockInput = mock(IAnalysisInput.class);
        jmockInput.stubs().method("setRule");
        jmockInput.stubs().method("setNumber");
        jmockInput.stubs().method("setInformationContainingRegionCode");

        jmockNumberAnalyzer.stubs().method("getAnalysisInput").will(returnValue(jmockInput.proxy()));
        jmockNumberAnalyzer.expects(once()).method("analyzeNumber").with(isA(IAnalysisInput.class));

        platformAccess.systemAnalyzeNumber("rule", "161089", null);

        try {
            jmockNumberAnalyzer.expects(once()).method("analyzeNumber").
                    will(throwException(new NumberAnalyzerException("No rule matched the number 161089", "NOMATCH")));
            platformAccess.systemAnalyzeNumber("rule", "161089", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.NUMBERANALYSIS, e.getMessage());
            String descr = e.getDescription();
            assertTrue(descr.indexOf("systemAnalyzeNumber") > -1);
            assertTrue(descr.indexOf("NOMATCH") > -1);
            //System.out.println(descr);
        }

        try {
            jmockNumberAnalyzer.expects(once()).method("analyzeNumber").
                    will(throwException(new NumberAnalyzerException("The number is blocked", "BLOCKED")));
            platformAccess.systemAnalyzeNumber("rule", "161089", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.NUMBERANALYSIS, e.getMessage());
            String descr = e.getDescription();
            assertTrue(descr.indexOf("systemAnalyzeNumber") > -1);
            assertTrue(descr.indexOf("BLOCKED") > -1);
        }

        try {
            jmockNumberAnalyzer.expects(once()).method("analyzeNumber").
                    will(throwException(new NumberAnalyzerException("Rule rule was not found", "NORULE")));
            platformAccess.systemAnalyzeNumber("rule", "161089", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.NUMBERANALYSIS, e.getMessage());
            String descr = e.getDescription();
            assertTrue(descr.indexOf("systemAnalyzeNumber") > -1);
            assertTrue(descr.indexOf("NORULE") > -1);
        }

        try {
            jmockNumberAnalyzer.expects(once()).method("analyzeNumber").
                    will(throwException(new NumberAnalyzerException("Wrong min length on number 3 < 4", "MIN=4")));
            platformAccess.systemAnalyzeNumber("rule", "123", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.NUMBERANALYSIS, e.getMessage());
            String descr = e.getDescription();
            assertTrue(descr.indexOf("systemAnalyzeNumber") > -1);
            assertTrue(descr.indexOf("MIN=4") > -1);
        }

        try {
            jmockNumberAnalyzer.expects(once()).method("analyzeNumber").
                    will(throwException(new NumberAnalyzerException("Wrong max length on number 13 > 12", "MAX=12")));
            platformAccess.systemAnalyzeNumber("rule", "1234567890123", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.NUMBERANALYSIS, e.getMessage());
            String descr = e.getDescription();
            assertTrue(descr.indexOf("systemAnalyzeNumber") > -1);
            assertTrue(descr.indexOf("MAX=12") > -1);
        }

        try {
            jmockNumberAnalyzer.expects(once()).method("analyzeNumber").
                    will(throwException(new NumberAnalyzerException("Number length must be exactly 8", "EXACTLY=8")));
            platformAccess.systemAnalyzeNumber("rule", "123456789", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.NUMBERANALYSIS, e.getMessage());
            String descr = e.getDescription();
            assertTrue(descr.indexOf("systemAnalyzeNumber") > -1);
            assertTrue(descr.indexOf("EXACTLY=8") > -1);
        }
    }

    /**
     * Tests the systemLog function.
     *
     * @throws Exception if test case fails.
     */
    public void testSystemLog() throws Exception {
        platformAccess.systemLog(0, "FATAL");
        platformAccess.systemLog(1, "ERROR");
        platformAccess.systemLog(2, "WARN");
        platformAccess.systemLog(3, "INFO");
        platformAccess.systemLog(4, "DEBUG");
    }

    /**
     * Test the systemSendSIPMessage function
     *
     * @throws Exception
     */
    public void testSendSIPMessage() throws Exception {

        jmockCallManager.stubs().method("sendSipMessage");
        jmockConfigurationGroup.stubs().method("getInteger").will(returnValue(20000));
        Mock jmockStubEventDispatcher = mock(IEventDispatcher.class);

        Mock jmockCCXMLExecutionContext = mock(CCXMLExecutionContext.class);
        jmockCCXMLExecutionContext.stubs().method("getCallManager").will(returnValue(jmockCallManager.proxy()));
        jmockCCXMLExecutionContext.stubs().method("getSession").will(returnValue(stubSession));
        jmockCCXMLExecutionContext.stubs().method("getEventDispatcher").will(returnValue(jmockStubEventDispatcher.proxy()));
        jmockCCXMLExecutionContext.stubs().method("waitForEvent");

        PlatformAccess platformAccess1 = createPlatformAccess((ExecutionContext) jmockCCXMLExecutionContext.proxy());

        // Test that it is possible to invoke systemSendSIPMessage with 2 "valid" arrays
        String[] paramArray = {"paramName"};
        String[] valueArray = {"paramName"};
        platformAccess1.systemSendSIPMessage("siprequest", paramArray, valueArray);

        // Test that invoking systemSendSIPMessage with null methodName results in an exception
        boolean gotException = false;
        try {
            platformAccess1.systemSendSIPMessage(null, paramArray, valueArray);
        } catch (PlatformAccessException e) {
            gotException = true;
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
        assertEquals(gotException, true);

        // Test that invoking systemSendSIPMessage with null params array results in an exception
        gotException = false;
        try {
            platformAccess1.systemSendSIPMessage("siprequest", null, valueArray);
        } catch (PlatformAccessException e) {
            gotException = true;
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
        assertEquals(gotException, true);

        // Test that invoking systemSendSIPMessage with null values array results in an exception
        gotException = false;
        try {
            platformAccess1.systemSendSIPMessage("siprequest", paramArray, null);
        } catch (PlatformAccessException e) {
            gotException = true;
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
        assertEquals(gotException, true);

        // test that invoking systemSendSIPMessage with a bigger params array will result in an exception
        gotException = false;
        try {
            platformAccess1.systemSendSIPMessage("siprequest",
                    new String[]{"paramName", "pName"},
                    new String[]{"value"});
        } catch (PlatformAccessException e) {
            gotException = true;
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
        assertEquals(gotException, true);

        // test that invoking systemSendSIPMessage with a bigger valueNames array will result in an exception
        gotException = false;
        try {
            platformAccess1.systemSendSIPMessage("siprequest",
                    new String[]{"paramName"},
                    new String[]{"value", "value2"});
        } catch (PlatformAccessException e) {
            gotException = true;
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
        assertEquals(gotException, true);

        // Test that if systemSendSIPMessage with an execution context which is not a CCXMLExecutionContext,
        // there will be an exception
        gotException = false;
        PlatformAccess platformAccess2 = createPlatformAccess((ExecutionContext) jmockExecutionContext.proxy());
        try {
            platformAccess2.systemSendSIPMessage("siprequest", paramArray, valueArray);
        } catch (PlatformAccessException e) {
            gotException = true;
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
        assertEquals(gotException, true);


    }

    /**
     * Unneccesary test just to get 100% coverage of the class StubSession
     *
     * @throws Exception if test case fails.
     */
    public void testStubSession() throws Exception {
        stubSession.setData("name", "value");
        stubSession.setId(null);
        stubSession.setMdcItems(null);
        stubSession.setSessionLogData("name", "value");
        stubSession.getId();
        stubSession.getData("name");
        stubSession.registerSessionInLogger();
        stubSession.dispose();
    }

    /**
     * Unneccesary test just to get 100% coverage of the class MessageTypeUtil
     *
     * @throws Exception if test case fails.
     */
    public void testMessageTypeUtil() throws Exception {
        MessageTypeUtil.stringToMessageType("voice");
        MessageTypeUtil.stringToMessageType("video");
        MessageTypeUtil.stringToMessageType("fax");
        MessageTypeUtil.stringToMessageType("email");
        try {
            MessageTypeUtil.stringToMessageType("notype");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        MessageTypeUtil.messageTypeToString(MailboxMessageType.VOICE);
        MessageTypeUtil.messageTypeToString(MailboxMessageType.VIDEO);
        MessageTypeUtil.messageTypeToString(MailboxMessageType.FAX);
        MessageTypeUtil.messageTypeToString(MailboxMessageType.EMAIL);

        MessageTypeUtil.stringToMessageState("new");
        MessageTypeUtil.stringToMessageState("read");
        MessageTypeUtil.stringToMessageState("saved");
        MessageTypeUtil.stringToMessageState("deleted");
        try {
            MessageTypeUtil.stringToMessageState("nostate");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        assertEquals("deleted", MessageTypeUtil.messageStateToString(StoredMessageState.DELETED));
        assertEquals("new", MessageTypeUtil.messageStateToString(StoredMessageState.NEW));
        assertEquals("saved", MessageTypeUtil.messageStateToString(StoredMessageState.SAVED));
        assertEquals("read", MessageTypeUtil.messageStateToString(StoredMessageState.READ));

        assertEquals("store-failed", MessageTypeUtil.deliveryStatusToString(DeliveryStatus.STORE_FAILED));
        assertEquals("print-failed", MessageTypeUtil.deliveryStatusToString(DeliveryStatus.PRINT_FAILED));
    }
}
