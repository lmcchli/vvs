/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.Dialog;

/**
 * CallFactory Tester.
 *
 * @author Malin Nyfeldt
 */
public class CallFactoryTest extends MockObjectTestCase {

    CallFactory callFactory = new CallFactory();

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    /**
     * Verifies that an exception is thrown when creating an inbound call and
     * the dialog is null.
     * @throws Exception An exception is thrown if test case fails.
     */
    public void testCreateInboundCallWhenDialogIsNull() throws Exception {
        Mock mockSipRequestEvent = new Mock(SipRequestEvent.class);
        mockSipRequestEvent.expects(once()).method("getDialog").
                will(returnValue(null));
        try {
            CallFactory.createInboundCall(
                    (SipRequestEvent)mockSipRequestEvent.proxy());
            fail("CallException expected but did not occurr.");
        } catch (CallException e) {
        }
    }

    /**
     * Verifies that an exception is thrown when an inbound call could not be
     * created.
     * @throws Exception An exception is thrown if test case fails.
     */
    public void testCreateInboundCallWhenExceptionIsThrown() throws Exception {
        Mock mockSipRequestEvent = new Mock(SipRequestEvent.class);
        Mock mockDialog = new Mock(Dialog.class);
        mockSipRequestEvent.stubs().method("getDialog").
                will(returnValue(mockDialog.proxy()));
        mockSipRequestEvent.expects(once()).method("getRequest").
                will(returnValue(null));
        mockSipRequestEvent.expects(once()).method("getSipMessage").
                will(returnValue(null));

        try {
            CallFactory.createInboundCall(
                    (SipRequestEvent)mockSipRequestEvent.proxy());
            fail("CallException expected but did not occur.");
        } catch (CallException e) {
        }
    }

    /**
     * Verifies that an exception is thrown when an outbound call could not be
     * created.
     * @throws Exception An exception is thrown if test case fails.
     */
    public void testCreateOutboundCallWhenExceptionIsThrown() throws Exception {
        try {
            CallFactory.createOutboundCall(null, null, null, null);
            fail("CallException expected but did not occurr.");
        } catch (CallException e) {
        }
    }
}
