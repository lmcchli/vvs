/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sip.SipMessageSender;
import com.mobeon.masp.callmanager.sip.events.SipRequestEventImpl;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.RequestEvent;
import javax.sip.SipProvider;
import javax.sip.ServerTransaction;
import javax.sip.Dialog;
import javax.sip.message.Request;

/**
 * SipRequestValidator Tester.
 *
 * @author Malin Flodin
 */
public class SipRequestValidatorTest extends MockObjectTestCase
{
    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    private Mock mockedMessageSender = mock(SipMessageSender.class);
    private Mock mockedResponseFactory = mock(SipResponseFactory.class);
    private Mock mockedRequest = mock(Request.class);
    private Mock mockedDialog = mock(Dialog.class);
    private Mock mockedSipProvider = mock(SipProvider.class);
    private Mock mockedServerTransaction = mock(ServerTransaction.class);

    private SipRequestValidator validator;
    private SipRequestEventImpl sipRequestEvent;

    public void setUp() throws Exception {
        super.setUp();
        validator = new SipRequestValidator(
                (SipMessageSender)mockedMessageSender.proxy(),
                (SipResponseFactory)mockedResponseFactory.proxy());
        sipRequestEvent = new SipRequestEventImpl(
                new RequestEvent(mockedSipProvider.proxy(),
                        (ServerTransaction)mockedServerTransaction.proxy(),
                        (Dialog)mockedDialog.proxy(),
                        (Request)mockedRequest.proxy()));
        mockedRequest.stubs().method("getMethod").will(returnValue("INVITE"));
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testValidateGeneralRequest() throws Exception {
        //TODO: Test goes here...
    }

    public void testValidateToAndRequestUri() throws Exception {
        //TODO: Test goes here...
    }

    public void testValidateRequestVersion() throws Exception {
        mockedRequest.stubs().method("getSIPVersion").
                will(returnValue("sip/3.0"));
        mockedResponseFactory.expects(once()).
                method("createVersionNotSupportedResponse");
        mockedMessageSender.expects(once()).method("sendResponse");

        try {
            validator.validateRequestVersion(sipRequestEvent);
            fail("Exception not throwm when expected.");
        } catch (UnsupportedOperationException e) {
        }

    }
}
