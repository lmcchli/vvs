/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.callmanager.sip.header.SipContentSubType;
import com.mobeon.masp.callmanager.sip.SipConstants;
import com.mobeon.masp.callmanager.sip.SipStackWrapper;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.RedirectDestination;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;

import javax.sip.message.Response;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.InvalidArgumentException;
import javax.sip.Dialog;
import javax.sip.SipException;
import javax.sip.header.SupportedHeader;
import javax.sip.header.RecordRouteHeader;
import java.text.ParseException;
import java.util.Collection;

import gov.nist.javax.sip.header.ims.PChargingVectorHeader;

/**
 * A class creating SIP responses.
 * <p>
 * NOTE: When creating a response, the original request is NOT cloned
 * although the headers (in the incoming request and outgoing response) are
 * shared and any modification to the headers of the outgoing response will
 * result in a modification of the incoming request.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class SipResponseFactoryImpl implements SipResponseFactory {
    
    private final SipStackWrapper sipStackWrapper;
    private final MessageFactory messageFactory;

    public SipResponseFactoryImpl(SipStackWrapper sipStackWrapper,
                                  MessageFactory messageFactory) {
        this.sipStackWrapper = sipStackWrapper;
        this.messageFactory = messageFactory;
    }

    //====================== Methods to create responses ====================

    public SipResponse createBadExtensionResponse(
            SipRequestEvent sipRequestEvent,
            Collection<String> unsupportedExtensions) throws ParseException {

        SipResponse sipResponse =
                createResponse(Response.BAD_EXTENSION, sipRequestEvent);
        sipResponse.addUnsupportedHeader(unsupportedExtensions);
        return sipResponse;
    }

    public SipResponse createExtensionRequiredResponse(
            SipRequestEvent sipRequestEvent,
            String requiredExtension) throws ParseException {

        SipResponse sipResponse = createResponse(Response.EXTENSION_REQUIRED, sipRequestEvent);
        sipResponse.addRequireHeader(requiredExtension);
        return sipResponse;
    }

    /**
     * Creates a "Bad Request" response. Sets the reason phrase.
     * @param sipRequestEvent
     * @param reason
     * @return the response
     * @throws ParseException if the response could not be created.
     */
    public SipResponse createBadRequestResponse(
        SipRequestEvent sipRequestEvent, String reason) throws ParseException
    {
        SipResponse sipResponse =
                createResponse(Response.BAD_REQUEST, sipRequestEvent);
        sipResponse.getResponse().setReasonPhrase(reason);
        return sipResponse;
    }
    
    public SipResponse createBadEventResponse(
            SipRequestEvent sipRequestEvent, String reason) throws ParseException
        {
            SipResponse sipResponse =
                    createResponse(Response.BAD_EVENT, sipRequestEvent);
            sipResponse.getResponse().setReasonPhrase(reason);
            return sipResponse;
        }

    public SipResponse createBusyHereResponse(SipRequestEvent sipRequestEvent)
            throws ParseException {
        return createResponse(Response.BUSY_HERE, sipRequestEvent);
    }

    public SipResponse createErrorResponse(int responseType,
                                           SipRequestEvent sipRequestEvent,
                                           String message)
            throws ParseException, IllegalArgumentException {

        SipResponse sipResponse;
        switch(responseType) {
            case Response.BAD_REQUEST:
                sipResponse = createBadRequestResponse(sipRequestEvent, message);
                break;
            case Response.FORBIDDEN:
                sipResponse = createForbiddenResponse(sipRequestEvent);
                break;
            case Response.BAD_EXTENSION:
                sipResponse = createResponse(responseType, sipRequestEvent);
                break;
            case Response.EXTENSION_REQUIRED:
                sipResponse = createExtensionRequiredResponse(sipRequestEvent, message);
                break;
            case Response.SERVER_INTERNAL_ERROR:
                sipResponse = createServerInternalErrorResponse(sipRequestEvent);
                break;
            case Response.SERVICE_UNAVAILABLE:
                sipResponse = createServiceUnavailableResponse(sipRequestEvent, message);
                break;
            case Response.SERVER_TIMEOUT:
                sipResponse = createServerTimeoutErrorResponse(sipRequestEvent);
                break;
            case Response.REQUEST_TERMINATED:
                sipResponse = createRequestTerminatedResponse(sipRequestEvent);
                break;
            case Response.REQUEST_PENDING:
                sipResponse = createRequestPendingResponse(sipRequestEvent);
                break;
            case Response.REQUEST_TIMEOUT:
                sipResponse = createRequestTimeoutResponse(sipRequestEvent);
                break;
            case Response.NOT_ACCEPTABLE_HERE:
            case Response.MOVED_TEMPORARILY:
                sipResponse = createErrorResponse(responseType, sipRequestEvent);
                break;            
            case SipConstants.PRECONDITION_FAILURE:
                sipResponse = createResponse(responseType, sipRequestEvent);
                break;
            default:
                throw new IllegalArgumentException("SIP response type " +
                        responseType + " not supported.");
        }
        return sipResponse;
    }

    public SipResponse createForbiddenResponse(SipRequestEvent sipRequestEvent)
            throws ParseException {
        return createResponse(Response.FORBIDDEN, sipRequestEvent);
    }
    
    public SipResponse createErrorResponse(int responseCode, SipRequestEvent sipRequestEvent)
    	throws ParseException {
    	return createResponse(responseCode, sipRequestEvent);
    }

    public SipResponse createMethodNotAllowedResponse(
            SipRequestEvent sipRequestEvent) throws ParseException
    {
        SipResponse sipResponse =
                createResponse(Response.METHOD_NOT_ALLOWED, sipRequestEvent);
        sipResponse.addAllowHeader();
        return sipResponse;
    }

    public SipResponse createNotFoundResponse(SipRequestEvent sipRequestEvent)
            throws ParseException {
        return createResponse(Response.NOT_FOUND, sipRequestEvent);
    }

    public SipResponse createNotAcceptableHereResponse(
            SipRequestEvent sipRequestEvent, SipWarning warning)
            throws ParseException, InvalidArgumentException {
        SipResponse sipResponse =
                createResponse(Response.NOT_ACCEPTABLE_HERE, sipRequestEvent);
        sipResponse.addWarningHeader(warning, sipStackWrapper.getHost());
        return sipResponse;
    }

    public SipResponse createNotImplementedResponse(
            SipRequestEvent sipRequestEvent) throws ParseException
    {
        return createResponse(Response.NOT_IMPLEMENTED, sipRequestEvent);
    }

    public SipResponse createOkResponse(SipRequestEvent sipRequestEvent,
                                        String sdp, String contactUser)
            throws ParseException, InvalidArgumentException {

        SipResponse sipResponse =
                createResponse(Response.OK, sipRequestEvent);

        String method = sipRequestEvent.getMethod();
        if ((method.equals(Request.OPTIONS)) || (method.equals(Request.INVITE))) {
            sipResponse.addAcceptHeader();
            sipResponse.addAcceptEncodingHeader();
            sipResponse.addAcceptLanguageHeader();
            sipResponse.addAllowHeader();
            
            if (!((ConfigurationReader.getInstance().getConfig().getContactUriOverride()).equals(""))) {
                //String contact = "<sip:mas01@mas3.teledienst.nl>";
                //Header header = null;
                //Header header = (Header)contact;
                String contact = ConfigurationReader.getInstance().getConfig().getContactUriOverride();
                sipResponse.addContactHeader(contact, null);
            }
            else {
                sipResponse.addContactHeader(
                    contactUser, sipStackWrapper.getHost(),
                    sipStackWrapper.getPort(), null);
            }
            //if (sipResponse.containsFromHeader()) {
            // Probably no need to do anything in case of IMS (IMT)
            //}
            
            sipResponse.addSupportedHeader(false);
        } else if ((method.equals(Request.PRACK)) || (method.equals(Request.UPDATE))) {
            boolean sessionEstablishment = ConfigurationReader.getInstance().getConfig().isSessionEstablishmentEnabled();
            if (sessionEstablishment) {
                sipResponse.addSupportedHeader(false);
            }
        }

        if (sdp != null) {
            sipResponse.addBody(SipContentSubType.SDP, sdp);
        }
        return sipResponse;
    }

    public SipResponse createAcceptedResponse(SipRequestEvent sipRequestEvent)throws ParseException, InvalidArgumentException {
    	SipResponse sipResponse =
    		createResponse(Response.ACCEPTED, sipRequestEvent);
    	return sipResponse;
    }

    public SipResponse createRequestPendingResponse(
            SipRequestEvent sipRequestEvent) throws ParseException {
        return createResponse(
                Response.REQUEST_PENDING, sipRequestEvent);
    }

    public SipResponse createRequestTerminatedResponse(
            SipRequestEvent sipRequestEvent) throws ParseException {
        return createResponse(
                Response.REQUEST_TERMINATED, sipRequestEvent);
    }

    public SipResponse createRequestTimeoutResponse(
            SipRequestEvent sipRequestEvent) throws ParseException {
        return createResponse(
                Response.REQUEST_TIMEOUT, sipRequestEvent);
    }

    public SipResponse createRingingResponse(
            SipRequestEvent sipRequestEvent, String sdp, String contactUser,
            boolean reliable)
            throws ParseException, InvalidArgumentException, SipException {
        SipResponse sipResponse;
        if (!reliable) {
            sipResponse = createResponse(Response.RINGING, sipRequestEvent);
        } else {
            sipResponse =
                    createReliableResponse(Response.RINGING, sipRequestEvent);
        }
           
       if (!((ConfigurationReader.getInstance().getConfig().getContactUriOverride()).equals(""))) {
            String contact = ConfigurationReader.getInstance().getConfig().getContactUriOverride();
            sipResponse.addContactHeader(contact, null);
        }
        else {
            sipResponse.addContactHeader(
                    contactUser, sipStackWrapper.getHost(),
                    sipStackWrapper.getPort(), null);
        }

        if (sdp != null)
            sipResponse.addBody(SipContentSubType.SDP, sdp);

        return sipResponse;
    }

    public SipResponse createServerInternalErrorResponse(
            SipRequestEvent sipRequestEvent) throws ParseException {
        return createResponse(Response.SERVER_INTERNAL_ERROR, sipRequestEvent);
    }

    public SipResponse createServerTimeoutErrorResponse(
            SipRequestEvent sipRequestEvent) throws ParseException {
        return createResponse(Response.SERVER_TIMEOUT, sipRequestEvent);
    }

    public SipResponse createSessionProgressResponse(
            SipRequestEvent sipRequestEvent, String sdp,
            String contactUser, boolean reliable, boolean regularRetransmission, boolean requirePrecondition)
                    throws ParseException, InvalidArgumentException, SipException {
        if (!regularRetransmission && sdp == null)
            throw new InvalidArgumentException("SDP must not be null");

        SipResponse sipResponse;
        if (!reliable) {
            sipResponse = createResponse(Response.SESSION_PROGRESS, sipRequestEvent);
        } else {
            sipResponse = createReliableResponse(Response.SESSION_PROGRESS, sipRequestEvent);
        }

        if (regularRetransmission && !ConfigurationReader.getInstance().getConfig().getPChargingVectorInRegularSessionProgressRetransmission()) {
            sipResponse.getResponse().removeHeader(PChargingVectorHeader.NAME);
        }

        sipResponse.addAcceptHeader();
        sipResponse.addAcceptEncodingHeader();
        sipResponse.addAcceptLanguageHeader();
        sipResponse.addAllowHeader();
        if (!((ConfigurationReader.getInstance().getConfig().getContactUriOverride()).equals(""))) {
            String contact = ConfigurationReader.getInstance().getConfig().getContactUriOverride();
            sipResponse.addContactHeader(contact, null);
        } else {
            sipResponse.addContactHeader(contactUser, sipStackWrapper.getHost(), sipStackWrapper.getPort(), null);
        }

        sipResponse.addSupportedHeader(false);

        if (requirePrecondition) {
            sipResponse.addRequireHeader(SipConstants.EXTENSION_PRECONDITION);
        }

        if (sdp != null)
            sipResponse.addBody(SipContentSubType.SDP, sdp);

        return sipResponse;
    }

    public SipResponse createServiceUnavailableResponse(
            SipRequestEvent sipRequestEvent, String reason)
            throws ParseException {
        SipResponse sipResponse = createResponse(
                Response.SERVICE_UNAVAILABLE, sipRequestEvent);
        sipResponse.getResponse().setReasonPhrase(reason);
        return sipResponse;
    }

    public SipResponse createTransactionDoesNotExistResponse(
            SipRequestEvent sipRequestEvent) throws ParseException {
        return createResponse(
                Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, sipRequestEvent);
    }

    public SipResponse createTryingResponse(SipRequestEvent sipRequestEvent, String contactUser)
        throws ParseException, InvalidArgumentException {

        SipResponse sipResponse = createResponse(Response.TRYING, sipRequestEvent);

        if (!ConfigurationReader.getInstance().getConfig().getApplicationProxyMode()) {
            if (!((ConfigurationReader.getInstance().getConfig().getContactUriOverride()).equals(""))) {
                String contact = ConfigurationReader.getInstance().getConfig().getContactUriOverride();
                sipResponse.addContactHeader(contact, null);
            } else {
                sipResponse.addContactHeader(contactUser, sipStackWrapper.getHost(), sipStackWrapper.getPort(), null);
            }                
        }        return sipResponse;
    }

    public SipResponse createUnsupportedMediaTypeResponse (
            SipRequestEvent sipRequestEvent) throws ParseException {
        SipResponse sipResponse =
                createResponse(Response.UNSUPPORTED_MEDIA_TYPE, sipRequestEvent);
        sipResponse.addAcceptHeader();
        sipResponse.addAcceptLanguageHeader();
        sipResponse.addAcceptEncodingHeader();
        return sipResponse;
    }

    public SipResponse createUnsupportedUriSchemeResponse(
            SipRequestEvent sipRequestEvent) throws ParseException {
        return createResponse(Response.UNSUPPORTED_URI_SCHEME, sipRequestEvent);
    }

    public SipResponse createVersionNotSupportedResponse(
            SipRequestEvent sipRequestEvent) throws ParseException
    {
        return createResponse(Response.VERSION_NOT_SUPPORTED, sipRequestEvent);
    }

    public SipResponse createForwardedResponse(SipRequestEvent sipRequestEvent,
                                               SipResponseEvent sipResponseEvent)
            throws ParseException {

        Request originalRequest = sipRequestEvent.getRequestEvent().getRequest();
        Response originalResponse = sipResponseEvent.getResponseEvent().getResponse();
        int responseCode = sipResponseEvent.getResponseCode();

        Response response = messageFactory.createResponse(responseCode, originalRequest);

        SupportedHeader supportedHeader =
                (SupportedHeader)originalResponse.getHeader(SupportedHeader.NAME);
        response.addHeader(supportedHeader);

        SipResponse sipResponse = new SipResponse(response, sipRequestEvent.getServerTransaction(),
                sipRequestEvent.getSipProvider());
        sipResponse.addToTag(sipRequestEvent.getToTag());
        return sipResponse;
    }

    //=========================== Private methods =========================

    /**
     * Creates a response for the request contained in the
     * <param>sipRequestEvent</param>. The response to create is given in
     * <param>statusCode</param>.
     *
     * @param statusCode        Indicates which response type to create.
     * @param sipRequestEvent   Contains the request to respond to.
     * @return                  Returns the created response.
     * @throws ParseException   ParseException is thrown if the response could
     *                          not be created due to illegal
     *                          <param>statusCode</param>. 1xx-6xx are valid
     *                          values.
     */
    private SipResponse createResponse(int statusCode,
                                       SipRequestEvent sipRequestEvent)
            throws ParseException
    {
        Request request = sipRequestEvent.getRequestEvent().getRequest();
        Response response = messageFactory.createResponse(statusCode, request);
        SipResponse sipResponse =
                new SipResponse(response, sipRequestEvent.getServerTransaction(),
                        sipRequestEvent.getSipProvider());
        sipResponse.addToTag(sipRequestEvent.getToTag());

        PChargingVectorHeader pChargingVector = (PChargingVectorHeader)
                request.getHeader(PChargingVectorHeader.NAME);       
        
        if (pChargingVector != null) { 
            if (pChargingVector.getTerminatingIOI() == null) {
                pChargingVector.setTerminatingIOI(CMUtils.getInstance().getLocalHost());
            }
            sipResponse.addPChargingVector(pChargingVector);
        }

        if ( !(sipResponse.containsRecordRouteHeader())) {
            RecordRouteHeader pRecordRoute = (RecordRouteHeader)
                    sipRequestEvent.getRequest().getHeader(RecordRouteHeader.NAME);
            
            if (pRecordRoute != null) {
                sipResponse.addRecordRoute(pRecordRoute);
            }
        }
        
        return sipResponse;
    }

    /**
     * Creates a reliable response for the request contained in the
     * <param>sipRequestEvent</param>. The response to create is given in
     * <param>statusCode</param>.
     *
     * @param statusCode        Indicates which response type to create.
     * @param sipRequestEvent   Contains the request to respond to.
     * @return                  Returns the created response.
     * @throws ParseException   ParseException is thrown if the to tag could
     *                          not be added to the response.
     * @throws InvalidArgumentException
     *                          InvalidArgumentException is thrown if the
     *                          response could not be created due to illegal
     *                          <param>statusCode</param>. 101-199 are valid
     *                          values for reliable responses.
     *                          It is also thrown if the dialog contained in the
     *                          <param>sipRequestEvent</param> is null.
     * @throws SipException     SipException is thrown if the response could
     *                          not be created due to the current dialog state.
     */
    private SipResponse createReliableResponse(int statusCode,
                                               SipRequestEvent sipRequestEvent)
            throws ParseException, InvalidArgumentException, SipException {
        Dialog dialog = sipRequestEvent.getDialog();

        if (dialog == null)
            throw new InvalidArgumentException(
                    "Dialog must not be null when creating a reliable response.");

        Response response = dialog.createReliableProvisionalResponse(statusCode);
        SipResponse sipResponse = new SipResponse(
                response,
                sipRequestEvent.getServerTransaction(),
                sipRequestEvent.getSipProvider());
        sipResponse.addToTag(sipRequestEvent.getToTag());


        PChargingVectorHeader pChargingVector = (PChargingVectorHeader)
                sipRequestEvent.getRequest().getHeader(PChargingVectorHeader.NAME);


        if (pChargingVector != null) {
            // Add our local host as term. ioi if it isn't set already
            if (pChargingVector.getTerminatingIOI() == null) {
                pChargingVector.setTerminatingIOI(CMUtils.getInstance().getLocalHost());
            }
            sipResponse.addPChargingVector(pChargingVector);
        }

        if ( !(sipResponse.containsRecordRouteHeader())) {
            RecordRouteHeader pRecordRoute = (RecordRouteHeader)
                    sipRequestEvent.getRequest().getHeader(RecordRouteHeader.NAME);
            
            if (pRecordRoute != null) {
                sipResponse.addRecordRoute(pRecordRoute);
            }
        }
        
        return sipResponse;
    }
    
    public SipResponse createRedirectResponse(int statusCode, SipRequestEvent sipRequestEvent,RedirectDestination destination) throws ParseException, InvalidArgumentException {
        SipResponse response = createResponse(statusCode,sipRequestEvent);

        response.addContactHeader(destination.getUri(),null);       
        return response;
    }

}
