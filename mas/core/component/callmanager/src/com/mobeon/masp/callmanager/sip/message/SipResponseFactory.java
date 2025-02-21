/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import com.mobeon.masp.callmanager.RedirectDestination;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import java.util.Collection;
import java.text.ParseException;

/**
 * Interface towards the SIP response factory. Creating SIP responses is done
 * using this interface. Only used to simplify basic test.
 *
 * @author Malin Nyfeldt
 */
public interface SipResponseFactory {

    // Methods to create responses
    public SipResponse createBadExtensionResponse(
            SipRequestEvent sipRequestEvent,
            Collection<String> unsupportedExtensions) throws ParseException;

    public SipResponse createExtensionRequiredResponse(
            SipRequestEvent sipRequestEvent,
            String requiredExtension) throws ParseException;

    public SipResponse createBadRequestResponse(
        SipRequestEvent sipRequestEvent, String reason) throws ParseException;

    public SipResponse createBadEventResponse(
            SipRequestEvent sipRequestEvent, String reason) throws ParseException;
    
    public SipResponse createBusyHereResponse(SipRequestEvent sipRequestEvent)
            throws ParseException;

    /**
     * TODO: Document which types of responses this could be.
     * Not responses that takes more input than message.
     * @param responseType
     * @param sipRequestEvent
     * @param message
     * @return
     * @throws ParseException
     */
    public SipResponse createErrorResponse(
            int responseType,
            SipRequestEvent sipRequestEvent,
            String message) throws ParseException;

    public SipResponse createForbiddenResponse(SipRequestEvent sipRequestEvent)
            throws ParseException;

    public SipResponse createMethodNotAllowedResponse(
            SipRequestEvent sipRequestEvent) throws ParseException;

    public SipResponse createNotAcceptableHereResponse(
            SipRequestEvent sipRequestEvent, SipWarning warning)
            throws ParseException, InvalidArgumentException;

    public SipResponse createNotFoundResponse(SipRequestEvent sipRequestEvent)
            throws ParseException;

    public SipResponse createNotImplementedResponse(
            SipRequestEvent sipRequestEvent) throws ParseException;

    public SipResponse createOkResponse(SipRequestEvent sipRequestEvent,
                                        String sdp, String contactUser)
            throws ParseException, InvalidArgumentException;
    
    public SipResponse createAcceptedResponse(SipRequestEvent sipRequestEvent)
    		throws ParseException, InvalidArgumentException;

    public SipResponse createRequestPendingResponse(
            SipRequestEvent sipRequestEvent) throws ParseException;

    public SipResponse createRequestTerminatedResponse(
            SipRequestEvent sipRequestEvent) throws ParseException;

    public SipResponse createRequestTimeoutResponse(
            SipRequestEvent sipRequestEvent) throws ParseException;

    /**
     * Creates and returns a SIP 180 "Ringing" response.
     * The response is based on the SIP request contained in the
     * <param>sipRequestEvent</param>.
     *
     * @param sipRequestEvent   The request event that shall be responded to.
     * @param sdp               An SDP carrying media properties.
     *                          May be null if no SDP shall be included in the
     *                          response.
     * @param contactUser       The user to put in the Contact header field.
     * @param reliable          A boolean that indicates whether the response
     *                          should be created reliably or not.
     *                          If set to true, a reliable response will be
     *                          created.
     * @return Returns the created response.
     * @throws ParseException   The response could not be created.
     * @throws InvalidArgumentException
     *                          The response could not be created.
     * @throws SipException     The response could not be created.
     */
    public SipResponse createRingingResponse(
            SipRequestEvent sipRequestEvent,
            String sdp, String contactUser, boolean reliable)
            throws ParseException, InvalidArgumentException, SipException;

    public SipResponse createServerInternalErrorResponse(
            SipRequestEvent sipRequestEvent) throws ParseException;

    /**
     * Creates and returns a SIP 183 "Session Progress" response.
     * The response is based on the SIP request contained in the
     * <param>sipRequestEvent</param>.
     * 
     *
     * @param sipRequestEvent   The request event that shall be responded to.
     * @param sdp               An SDP carrying media properties.
     *                          MUST NOT be null since this response always
     *                          should carry an SDP. Unless regularRetransmission is true.
     * @param contactUser       The user to put in the Contact header field.
     * @param reliable          A boolean that indicates whether the response
     *                          should be created reliably or not.
     *                          If set to true, a reliable response will be
     *                          created.
     * @param regularRetransmission A boolean that indicates if the response is 
     *                              created for retransmission during extended early media 
     *                              as described in RFC 3261 (section 13.3.1.1 Progress).
     * @param requirePrecondition A boolean that indicates whether the response should contain 
     *                            a 'precondition' extension in the 'Require' header
     * @return Returns the created response.
     * @throws ParseException   The response could not be created.
     * @throws InvalidArgumentException
     *                          The response could not be created. This occurs
     *                          for example if <param>sdp</param> is null.
     * @throws SipException     The response could not be created.
     */
    public SipResponse createSessionProgressResponse(
            SipRequestEvent sipRequestEvent, String sdp,
            String contactUser, boolean reliable, boolean regularRetransmission, boolean requirePrecondition)
            throws ParseException, InvalidArgumentException, SipException;

    public SipResponse createServiceUnavailableResponse(
            SipRequestEvent sipRequestEvent, String reason)
            throws ParseException;

    public SipResponse createTransactionDoesNotExistResponse(
            SipRequestEvent sipRequestEvent) throws ParseException;

    public SipResponse createTryingResponse(
            SipRequestEvent sipRequestEvent, String contactUser)
            throws ParseException, InvalidArgumentException;

    public SipResponse createUnsupportedMediaTypeResponse (
            SipRequestEvent sipRequestEvent) throws ParseException;

    public SipResponse createUnsupportedUriSchemeResponse(
            SipRequestEvent sipRequestEvent) throws ParseException;

    public SipResponse createVersionNotSupportedResponse(
            SipRequestEvent sipRequestEvent) throws ParseException;

    public SipResponse createForwardedResponse(
            SipRequestEvent sipRequestEvent, SipResponseEvent sipResponseEvent)
            throws ParseException;
    
    public SipResponse createRedirectResponse(int statusCode, SipRequestEvent sipRequestEvent,
            RedirectDestination destination) 
            throws ParseException, InvalidArgumentException ;
    
}
