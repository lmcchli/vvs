/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;


import javax.sip.header.ToHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.address.Address;
import javax.sip.address.URI;
import javax.sip.address.SipURI;

/**
 * A mocked SIPMessage used in basic tests.
 * @author Malin Flodin
 */
public abstract class SipMessageMock extends MockObjectTestCase {

    private static final String CALL_ID     = "1234567890";
    private static final String FROM_TAG    = "FromTag";
    private static final String TO_TAG      = "ToTag";

    private Mock mockMessage;
    private SipMessage sipMessage;

    // Mocked headers
    protected Mock mockCallIdHeader = mock(CallIdHeader.class);
    protected Mock mockFromHeader   = mock(FromHeader.class);
    protected Mock mockToHeader     = mock(ToHeader.class);
    protected Mock mockFromAddress  = mock(Address.class);
    protected Mock mockToAddress    = mock(Address.class);
    protected Mock mockFromSipUri   = mock(SipURI.class);
    protected Mock mockToSipUri     = mock(SipURI.class);


    public void init(Mock mockMessage, SipMessage sipMessage) {
        this.mockMessage = mockMessage;
        this.sipMessage = sipMessage;

        setupExpectations();
    }

    private void setupAddressExpectations() {        
        mockFromSipUri.stubs().method("getUser").will(returnValue("fromuser"));
        mockFromSipUri.stubs().method("getHost").will(returnValue("host"));
        mockFromSipUri.stubs().method("getPort").will(returnValue(-1));
        mockToSipUri.stubs().method("getUser").will(returnValue("touser"));
        mockToSipUri.stubs().method("getHost").will(returnValue("host"));
        mockToSipUri.stubs().method("getPort").will(returnValue(-1));
        mockFromAddress.stubs().method("getURI").will(returnValue(mockFromSipUri.proxy()));
        mockToAddress.stubs().method("getURI").will(returnValue(mockToSipUri.proxy()));
    }

    private void setupCallIDHeaderExpectations() {
        mockMessage.stubs().method("getHeader").with(eq("Call-ID")).
                will(returnValue(mockCallIdHeader.proxy()));
        mockCallIdHeader.stubs().method("getCallId").will(returnValue(CALL_ID));
    }

    private void setupFromHeaderExpectations() {
        mockMessage.stubs().method("getHeader").with(eq("From")).
                will(returnValue(mockFromHeader.proxy()));
        mockFromHeader.stubs().method("getAddress").will(returnValue(mockFromAddress.proxy()));
        mockFromHeader.stubs().method("getTag").will(returnValue(FROM_TAG));
    }

    private void setupToHeaderExpectations() {
        mockMessage.stubs().method("getHeader").with(eq("To")).
                will(returnValue(mockToHeader.proxy()));
        mockToHeader.stubs().method("getAddress").
                will(returnValue(mockToAddress.proxy()));
        mockToHeader.stubs().method("getTag").will(returnValue(TO_TAG));
    }

    private void setupExpectations() {

        setupAddressExpectations();
        setupCallIDHeaderExpectations();
        setupFromHeaderExpectations();
        setupToHeaderExpectations();
    }

    public SipMessage getSipMessage() {
        return sipMessage;
    }

    // This is only included to make IntelliJ happy. Not used at all.
    public SipMessageMock() {
    }

    public void testDoNothing() throws Exception {
    }
}
