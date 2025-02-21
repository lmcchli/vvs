/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit;

import org.cafesip.sipunit.SipTransaction;

import javax.sip.message.Request;
import javax.sip.message.Response;

import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;

/**
 * Call Manager component test case to verify SIP OPTIONS request and
 * received out-of-dialog.
 * @author Malin Flodin
 */
public class OutOfDialogOptionsTest extends SipUnitCase {

    /**
     * Verifies that an OPTIONS with unsupported extensions results in a SIP
     * Bad Extension response.
     * @throws Exception when the test case fails.
     */
    public void testOptionsWithUnsupportedExtensions() throws Exception {
        Request options = simulatedPhone.createRequest(Request.OPTIONS,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);

        // Add Require header with unsupported extension
        options.addHeader(
                simulatedPhone.getHeaderFactory().
                        createRequireHeader("unsupportedExtension"));

        SipTransaction transaction =
                simulatedPhone.sendOutOfDialogRequest(options);

        // Wait for response
        simulatedPhone.assertResponseReceived(
                transaction, Response.BAD_EXTENSION, Request.OPTIONS);

        assertTotalConnectionStatistics(0);
    }

    /**
     * Verifies that an OPTIONS with unsupported Content-Encoding results in a
     * SIP Unsupported Media Type response with a Accept-Encoding header.
     * @throws Exception when the test case fails.
     */
    public void testOptionsWithUnsupportedContentEncoding() throws Exception {
        Request options = simulatedPhone.createRequest(Request.OPTIONS,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);

        // Add unsupported Content-Encoding header
        options.addHeader(simulatedPhone.getHeaderFactory().
                createContentEncodingHeader("gzip"));

        SipTransaction transaction =
                simulatedPhone.sendOutOfDialogRequest(options);

        // Wait for response
        Response response = simulatedPhone.assertResponseReceived(
                transaction, Response.UNSUPPORTED_MEDIA_TYPE, Request.OPTIONS);

        // Verify that the response contains an Accept-Encoding header
        simulatedPhone.assertAcceptEncodingHeader(response, "identity");

        assertTotalConnectionStatistics(0);
    }

    /**
     * Verifies that an OPTIONS with unsupported Content-Type results in a
     * SIP Unsupported Media Type response with a Accept header.
     * @throws Exception when the test case fails.
     */
    public void testOptionsWithUnsupportedContentType() throws Exception {
        Request options = simulatedPhone.createRequest(Request.OPTIONS,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);

        // Add unsupported ContentType header
        simulatedPhone.addBody(options, "illegal", "type", null, false, "body".getBytes());

        SipTransaction transaction =
                simulatedPhone.sendOutOfDialogRequest(options);

        // Wait for response
        Response response = simulatedPhone.assertResponseReceived(
                transaction, Response.UNSUPPORTED_MEDIA_TYPE, Request.OPTIONS);

        // Verify that the response contains an Accept header
        simulatedPhone.assertAcceptHeader(response, "application", "sdp");
        simulatedPhone.assertAcceptHeader(response, "application", "media_control+xml");

        assertTotalConnectionStatistics(0);
    }


    /**
     * Verifies that the Experienced-Operational-Status header is set to "up"
     * in an OK response to an OPTIONS if the administrative state is unlocked
     * and we have a remote party.
     * @throws Exception
     */
    public void testOptionsWhenAdminStateUnlocked() throws Exception {
        Request options = simulatedPhone.createRequest(Request.OPTIONS,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);

        SipTransaction transaction =
                simulatedPhone.sendOutOfDialogRequest(options);

        // Wait for response
        Response response = simulatedPhone.assertResponseReceived(
                transaction, Response.OK, Request.OPTIONS);

        // Verify that the response contains an Accept header
        simulatedPhone.assertExperiencedOperationalStatus(response, "up");
    }

    /**
     * Verifies that the Experienced-Operational-Status header is set to "up"
     * in a SIP "Temporarily Unavailable" response to an OPTIONS if the
     * administrative state is locked.
     * @throws Exception
     */
    public void testOptionsWhenAdminStateLocked() throws Exception {
        simulatedSystem.lock();

        Request options = simulatedPhone.createRequest(Request.OPTIONS,
                PhoneSimulator.OUT_OF_DIALOG, PhoneSimulator.WITH_BODY, null, false, false);

        SipTransaction transaction =
                simulatedPhone.sendOutOfDialogRequest(options);

        // Wait for response
        Response response = simulatedPhone.assertResponseReceived(
                transaction, Response.SERVICE_UNAVAILABLE, Request.OPTIONS);

        // Verify that the response contains an Accept header
        simulatedPhone.assertExperiencedOperationalStatus(response, "up");
    }
}
