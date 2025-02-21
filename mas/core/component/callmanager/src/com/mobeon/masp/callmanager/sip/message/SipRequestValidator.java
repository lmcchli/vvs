/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sip.header.SipContentData;
import com.mobeon.masp.callmanager.sip.SipMessageSender;
import com.mobeon.masp.callmanager.sip.SipConstants;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;

import javax.sip.message.Request;
import java.text.ParseException;
import java.util.Collection;

/**
 * This class is responsible for validating SIP requests.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class SipRequestValidator {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    // Factories necessary to create and send SIP responses
    private final SipMessageSender sipMessageSender;
    private final SipResponseFactory sipResponseFactory;

    public SipRequestValidator(SipMessageSender sipMessageSender,
                               SipResponseFactory sipResponseFactory) {
        this.sipMessageSender = sipMessageSender;
        this.sipResponseFactory = sipResponseFactory;
    }

    /**
     * Validates a SIP request according to the general rules in section 8.2 in
     * RFC 3261. If the SIP request is not validated ok, a SIP final response
     * is sent to inform the peer UA of the error.
     * <p>
     * The Request method is validated as described in section 8.2.1 of
     * RFC 3261, @see SipRequestValidator#validateRequestMethod
     * <p>
     * The To header field and Request-URI is validated as described in section
     * 8.2.2.1 of RFC 3261, @see SipRequestValidator#validateToAndRequestUri
     * <p>
     * The SIP request is checked for a SIP loopback as described in section
     * 8.2.2.2 of RFC 3261, @see SipRequestValidator#validateNoLoopback
     * <p>
     * The Request header field is checked for unsupported extensions as
     * described in section 8.2.2.3 of RFC 3261,
     * @see SipRequestValidator#validateRequireHeader
     * <p>
     * The Content header fields are validated as described in
     * section 8.2.3 of RFC 3261, @see SipRequestValidator#validateContent
     * <p>
     * The Supported header field is validated as described in
     * section 8.2.4 of RFC 3261,
     * @see SipRequestValidator#validateSupportedHeader
     * @param sipRequestEvent
     * @return true if the request was validated ok and false otherwise.
     */
    public boolean validateGeneralRequest(SipRequestEvent sipRequestEvent) {
        boolean verifiedOk = true;

        try {
            validateRequestMethod(sipRequestEvent);

            validateToAndRequestUri(sipRequestEvent);

            validateNoLoopback(sipRequestEvent);

            validateRequireHeader(sipRequestEvent);

            validateContent(sipRequestEvent);

            validateSupportedHeader(sipRequestEvent);

            validateRequestVersion(sipRequestEvent);
        } catch (UnsupportedOperationException e) {
            verifiedOk = false;
        }

        return verifiedOk;
    }

    /**
     * Validates the request method of a SIP request as described in section
     * 8.2.1 of RFC 3261.
     * <p>
     * If the method is known but not supported, a SIP "Method Not Allowed"
     * response is sent and UnsupportedOperationException is thrown.
     * <p>
     * If the method is unknown, a SIP "Not Implemented" response is
     * sent and UnsupportedOperationException is thrown.
     *
     * @param sipRequestEvent
     * @throws UnsupportedOperationException if the request method was not
     * supported.
     */
    private void validateRequestMethod(SipRequestEvent sipRequestEvent) {
        boolean requestIsValid = true;

        try {
            if (!sipRequestEvent.getSipMessage().isMethodSupported()) {
                if (sipRequestEvent.getSipMessage().isMethodKnownButUnsupported()) {
                    requestIsValid = false;

                    if (log.isInfoEnabled()) log.info("SIP " + sipRequestEvent.getSipMessage().getMethod() +
                                                      " request is known but not " +
                                                      "supported. It is rejected with a SIP 405 response. " +
                                                      "Supported methods are: " +
                                                      SipConstants.getSupportedMethods());

                    SipResponse sipResponse = sipResponseFactory.
                            createMethodNotAllowedResponse(sipRequestEvent);
                    sipMessageSender.sendResponse(sipResponse);
                } else {
                    requestIsValid = false;

                    if (log.isInfoEnabled()) log.info("SIP " + sipRequestEvent.getSipMessage().getMethod() +
                                                      " request is unknown and " +
                                                      "not supported. It is rejected with a SIP 501 response. " +
                                                      "Supported methods are: " +
                                                      SipConstants.getSupportedMethods());

                    SipResponse sipResponse = sipResponseFactory.
                            createNotImplementedResponse(sipRequestEvent);
                    sipMessageSender.sendResponse(sipResponse);
                }
            }
        } catch (ParseException e) {
            if (log.isDebugEnabled())
                log.debug("Could not create the failure response.", e);
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Could not send the failure response.", e);
        }

        if (!requestIsValid) {
            throw new UnsupportedOperationException("SIP request method " +
                    sipRequestEvent.getSipMessage().getMethod() +
                    " is not supported.");
        }
    }

    /**
     * Validates the request version of a SIP request. Only version "SIP/2.0"
     * is supported.
     * <p>
     * If the version is known but not supported, a SIP "Version Not Supported"
     * response is sent and UnsupportedOperationException is thrown.
     *
     * @param sipRequestEvent
     * @throws UnsupportedOperationException if the request version was not
     * supported.
     */
    void validateRequestVersion(SipRequestEvent sipRequestEvent) {
        boolean requestIsValid = true;

        try {
            if (!sipRequestEvent.getSipMessage().isSipVersionSupported()) {
                requestIsValid = false;

                if (log.isInfoEnabled()) log.info("SIP " + sipRequestEvent.getSipMessage().getMethod() +
                                                  " request contains unsupported version " +
                                                  sipRequestEvent.getSipMessage().getVersion() +
                                                  ". It is rejected with a SIP 505 response. " +
                                                  "Supported versions are: " +
                                                  SipConstants.getSupportedVersions());

                SipResponse sipResponse = sipResponseFactory.
                        createVersionNotSupportedResponse(sipRequestEvent);
                sipMessageSender.sendResponse(sipResponse);
            }
        } catch (ParseException e) {
            if (log.isDebugEnabled())
                log.debug("Could not create the failure response.", e);
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Could not send the failure response.", e);
        }

        if (!requestIsValid) {
            throw new UnsupportedOperationException("SIP request version " +
                    sipRequestEvent.getSipMessage().getVersion()  +
                    " is not supported.");
        }
    }

    /**
     * Validates the To header field and the Request-URI of a SIP request
     * as described in Section 8.2.2.1 (To and Request-URI) of RFC 3261.
     * <p>
     * If the To-header is unknown or invalid, a SIP "Forbidden" response is
     * sent and UnsupportedOperationException is thrown.
     * <p>
     * If the URI scheme is unsupported, a SIP "Unsupported URI Scheme"
     * response is sent and UnsupportedOperationException is thrown.
     * <p>
     * If the Request-URI address is not handled by Call Manager, a SIP
     * "Not Found" response is sent and UnsupportedOperationException is thrown.
     *
     * @param sipRequestEvent
     * @throws UnsupportedOperationException if the To header field or the
     * Request-URI was not supported and a final error response has been sent.
     */
    public void validateToAndRequestUri(SipRequestEvent sipRequestEvent) {
        boolean requestIsValid = true;

        try {
            // Validate To header field
            if (!sipRequestEvent.getSipMessage().isToHeaderValid()) {
                requestIsValid = false;

                if (log.isInfoEnabled()) log.info("SIP " + sipRequestEvent.getSipMessage().getMethod() +
                                                  " request contains an invalid To header. " +
                                                  "It is rejected with a SIP 403 response. ");

                SipResponse sipResponse = sipResponseFactory.
                        createForbiddenResponse(sipRequestEvent);
                sipMessageSender.sendResponse(sipResponse);
            }
            // Validate URI scheme used in Request-URI
            else if (!sipRequestEvent.getSipMessage().isUriSchemeSupported()) {
                requestIsValid = false;

                if (log.isInfoEnabled()) log.info("SIP " + sipRequestEvent.getSipMessage().getMethod() +
                                                  " request contains a Request-URI " +
                                                  "with an unsupported URI scheme. " +
                                                  "It is rejected with a SIP 416 response. ");

                SipResponse sipResponse = sipResponseFactory.
                        createUnsupportedUriSchemeResponse(sipRequestEvent);
                sipMessageSender.sendResponse(sipResponse);
            }
            // Validate the value of the Request-URI
            else if (!sipRequestEvent.getSipMessage().isRequestUriValid()) {
                requestIsValid = false;

                if (log.isInfoEnabled()) log.info("SIP " + sipRequestEvent.getSipMessage().getMethod() +
                                                  " request contains an invalid " +
                                                  "Request-URI. It is rejected with a SIP 404 response. ");

                SipResponse sipResponse = sipResponseFactory.
                        createNotFoundResponse(sipRequestEvent);
                sipMessageSender.sendResponse(sipResponse);
            }

        } catch (ParseException e) {
            if (log.isDebugEnabled())
                log.debug("Could not create the failure response.", e);
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Could not send the failure response.", e);
        }

        if (!requestIsValid) {
            throw new UnsupportedOperationException(
                    "Unsupported To header field or Request-URI.");
        }
    }

    /**
     * Detects a SIP loopback in the SIP request as described in
     * Section 8.2.2.2 (Merged Requests) of RFC 3261.
     * <p>
     * If a loopback is detected, a SIP "Loopback Detected" response is
     * sent and UnsupportedOperationException is thrown.
     *
     * @param sipRequestEvent
     * @throws UnsupportedOperationException if a loopback was detected.
     */
    private void validateNoLoopback(SipRequestEvent sipRequestEvent) {
        // TODO: Phase 2! Implement SIP loopback detection.
    }

    /**
     * Validates the Require header field of a SIP request as described in
     * Section 8.2.2.3 (Require) of RFC 3261.
     * <p>
     * If unsupported extensions are found in the Require header, a SIP
     * "Bad Extension" response is sent and UnsupportedOperationException is
     * thrown. If the request is an ACK or a CANCEL this validation is not
     * performed since Require header fields in those requests should be
     * ignored.
     *
     * @param sipRequestEvent
     * @throws UnsupportedOperationException if the To header field or the
     * Request-URI was not supported and a final error response has been sent.
     */
    private void validateRequireHeader(SipRequestEvent sipRequestEvent) {
        boolean requestIsValid = true;

        String method = sipRequestEvent.getSipMessage().getMethod();
        if (!(method.equals(Request.ACK)) && !(method.equals(Request.CANCEL))) {
            Collection<String> unsupportedExtensions = sipRequestEvent.
                    getSipMessage().getUnsupportedButRequiredExtensions();

            if (!unsupportedExtensions.isEmpty()) {
                try {
                    requestIsValid = false;

                    if (log.isInfoEnabled()) log.info("SIP " + sipRequestEvent.getSipMessage().getMethod() +
                                                      " request contains the " +
                                                      "following required but unsupported extensions: " +
                                                      unsupportedExtensions +
                                                      ". It is rejected with a SIP 420 response. ");

                    SipResponse sipResponse = sipResponseFactory.
                            createBadExtensionResponse(sipRequestEvent,
                                    unsupportedExtensions);
                    sipMessageSender.sendResponse(sipResponse);
                } catch (ParseException e) {
                    if (log.isDebugEnabled())
                        log.debug("Could not create the failure response.", e);
                } catch (Exception e) {
                    if (log.isDebugEnabled())
                        log.debug("Could not send the failure response.", e);
                }
            }

            if (!requestIsValid) {
                throw new UnsupportedOperationException(
                        "Unsupported but required extension: " +
                                unsupportedExtensions);
            }
        }

    }

    /**
     * Validates the Content header fields of a SIP request as described in
     * Section 8.2.3 (Content Processing) of RFC 3261.
     * <p>
     * If unsupported Content-Language, Content-Encoding, or Content-Type are
     * found, a SIP 415 (Unsupported Media Type) response is sent and
     * UnsupportedOperationException is thrown.
     *
     * @param sipRequestEvent
     * @throws UnsupportedOperationException if the To header field or the
     * Request-URI was not supported and a final error response has been sent.
     */
    private void validateContent(SipRequestEvent sipRequestEvent) {
        boolean requestIsValid = true;
        Collection<SipContentData> contents =
                sipRequestEvent.getSipMessage().getContent();

        for (SipContentData content : contents) {

            if (log.isDebugEnabled())
                log.debug("Validating SIP content: " + content);

            // Validate body content if body part is mandatory
            if (content.isContentRequired()) {
                if ((!content.isContentLanguageSupported()) ||
                    (!content.isContentEncodingSupported()) ||
                    (!content.isContentTypeSupported())) {

                    requestIsValid = false;
                    try {

                        if (log.isInfoEnabled()) log.info("SIP " +
                                                          sipRequestEvent.getSipMessage().getMethod() +
                                                          " request contains an " +
                                                          "unsupported content language, content encoding or " +
                                                          "content type. It is rejected with a SIP 415 response. " +
                                                          "Supported content languages: " +
                                                          SipConstants.getSupportedLanguages() +
                                                          ". Supported content encodings: " +
                                                          SipConstants.getSupportedEncodings() +
                                                          ". Supported content types: " +
                                                          SipConstants.getSupportedContentTypes());

                        SipResponse sipResponse = sipResponseFactory.
                                createUnsupportedMediaTypeResponse(sipRequestEvent);
                        sipMessageSender.sendResponse(sipResponse);
                    } catch (ParseException e) {
                        if (log.isDebugEnabled())
                            log.debug("Could not create the failure response.", e);
                    } catch (Exception e) {
                        if (log.isDebugEnabled())
                            log.debug("Could not send the failure response.", e);
                    }

                    break;
                }
            }
        }

        if (!requestIsValid) {
            throw new UnsupportedOperationException("Unsupported content.");
        }
    }

    /**
     * Validates the Supported header field of a SIP request as described in
     * Section 8.2.4 (Applying Extensions) of RFC 3261.
     * <p>
     * If a required header is not supported by peer, a SIP "Extension Required"
     * response is sent and UnsupportedOperationException is thrown.
     * <p>
     * Since this is not recommended behavior, currently Call Manager never
     * requires the peer to support a specific extension.
     * @param sipRequestEvent
     * @throws UnsupportedOperationException if the To header field or the
     * Request-URI was not supported and a final error response has been sent.
     */
    private void validateSupportedHeader(SipRequestEvent sipRequestEvent) {
        //TODO: Phase 2! Implement validation of Supported header when PRACK is implemented.
    }
}

