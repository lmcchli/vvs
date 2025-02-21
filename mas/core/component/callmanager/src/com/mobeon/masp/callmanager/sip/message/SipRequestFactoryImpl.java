/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.header.SipContentSubType;
import com.mobeon.masp.callmanager.sip.header.SipHeaderFactory;
import com.mobeon.masp.callmanager.sip.SipStackWrapper;
import com.mobeon.masp.callmanager.sip.SipConstants;
import com.mobeon.masp.callmanager.CallPartyDefinitions.PresentationIndicator;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.DiversionParty;
import com.mobeon.masp.callmanager.SubscribeCall;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.configuration.RestrictedOutboundHeaders;
import com.mobeon.masp.callmanager.configuration.RestrictedOutboundHeaders.RestrictedHeader;

import com.mobeon.masp.callmanager.configuration.ConfigurationReader;

import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.address.TelURL;
import javax.sip.address.URI;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.InvalidArgumentException;
import javax.sip.Dialog;
import javax.sip.SipException;
import javax.sip.ClientTransaction;

import java.util.ArrayList;
import java.util.ListIterator;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.io.IOException;
import java.text.ParseException;

import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.address.ParameterNames;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.ViaList;
import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.Expires;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.RouteList;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.SubscriptionState;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import gov.nist.javax.sip.header.ims.PAssertedIdentityList;
import gov.nist.javax.sip.header.ims.PChargingVectorHeader;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.header.ims.SIPHeaderNamesIms;
import gov.nist.javax.sip.parser.CSeqParser;
import gov.nist.javax.sip.parser.CallIDParser;
import gov.nist.javax.sip.parser.FromParser;
import gov.nist.javax.sip.parser.RouteParser;
import gov.nist.javax.sip.parser.SubscriptionStateParser;
import gov.nist.javax.sip.parser.ToParser;
import gov.nist.javax.sip.parser.URLParser;
import gov.nist.javax.sip.parser.ims.PChargingVectorParser;

/**
 * TODO: Drop 6! Modify documentation, no longer singleton!
 * A singleton creating SIP requests.
 * Requires initialisation (i.e. a call to method init) before any request is
 * created.
 *
 * This class is thread-safe but requires initialisation before any further
 * usage.
 *
 * TODO: Drop 6! Document this class.
 */
public class SipRequestFactoryImpl implements SipRequestFactory {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private static final int INITIAL_CSEQ = 1;
    private static final int DEFAULT_MAX_FORWARDS = 70;
    private static final String SUBSCRIPTION_STATE_FOR_UNSOLICITED_MWI = "active";

    private final SipStackWrapper sipStackWrapper;
    private final MessageFactory messageFactory;
    private final SipHeaderFactory sipHeaderFactory;
    private final SipProvider sipProvider;


    public SipRequestFactoryImpl(SipStackWrapper sipStackWrapper,
                                 MessageFactory messageFactory,
                                 SipHeaderFactory sipHeaderFactory,
                                 SipProvider sipProvider) {
        this.sipStackWrapper = sipStackWrapper;
        this.messageFactory = messageFactory;
        this.sipHeaderFactory = sipHeaderFactory;
        this.sipProvider = sipProvider;
    }

    //====================== Methods to create requests ====================

    // Methods to create requests
    public SipRequest createAckRequest(Dialog dialog) throws SipException {
        Request request = dialog.createRequest(Request.ACK);
        return new SipRequest(request);
    }

    public SipRequest createByeRequest(Dialog dialog,
                                       PChargingVectorHeader pChargingVector)
            throws SipException, ParseException {

        Request request = dialog.createRequest(Request.BYE);
        SipRequest sipRequest = new SipRequest(request);

        if (pChargingVector != null)
            sipRequest.addPChargingVector(pChargingVector);

        return sipRequest;
    }

    /**
     * @param transaction
     * @return
     * @throws SipException
     */
    public SipRequest createCancelRequest (ClientTransaction transaction)
            throws SipException {
        Request request = transaction.createCancel();
        return new SipRequest(request);
    }

    public SipRequest createInfoRequest(Dialog dialog,
                                        InfoType type,
                                        String body,
                                        PChargingVectorHeader pChargingVector)
            throws SipException, ParseException {

    	Request request = dialog.createRequest(Request.INFO);
        SipRequest sipRequest = new SipRequest(request);
        if (pChargingVector != null)
            sipRequest.addPChargingVector(pChargingVector);

        sipRequest.addBody(SipContentSubType.MEDIA_CONTROL, body);

        return sipRequest;
    }

    public SipRequest createNewInviteRequest(SipURI contact,
                                             SipRequest initialInviteRequest,
                                             String userAgent,
                                             PresentationIndicator pi,
                                             boolean preventLoopback,
                                             String callId,
                                             PChargingVectorHeader pChargingVector)
            throws InvalidArgumentException, ParseException {

        Request request = initialInviteRequest.getRequest();
        
        PAssertedIdentityList pAssertedIdentityList = null; 
        ListIterator paiListIterator = request.getHeaders(PAssertedIdentityHeader.NAME);
        if(paiListIterator.hasNext()) {
            // At least one P-Asserted-Identity header, create and populate list
            pAssertedIdentityList = new PAssertedIdentityList();
            while( paiListIterator.hasNext() ) {
                pAssertedIdentityList.add(paiListIterator.next());
            }
            
        }
        
        
        SipRequest sipRequest = createInviteRequest(
                contact,
                (ToHeader)request.getHeader(ToHeader.NAME),
                (FromHeader)request.getHeader(FromHeader.NAME),
                pAssertedIdentityList,
                request.getHeader(SipConstants.HDR_REMOTE_PARTY_ID),
                sipHeaderFactory.createCallIdHeader(callId),
                userAgent,
                ((ExpiresHeader)request.getHeader(ExpiresHeader.NAME)).getExpires(),
                pi,
                preventLoopback,
                null,
                initialInviteRequest.getContent(SipContentSubType.SDP));

        if (pChargingVector != null)
            sipRequest.addPChargingVector(pChargingVector);

        return sipRequest;
    }

    /**
     * According to section 13.2.1 of RFC 3261
     * Also sets Contact header field and loopback prevention.
     * To, from, fromTag, sdp and callId must not be null!
     */
    public SipRequest createInviteRequest(SipURI remotePartyUri,
                                          CalledParty to,
                                          CallingParty from,
                                          DiversionParty diversion,
                                          String userAgent, int expiration,
                                          Boolean preventLoopback,
                                          String sdp, String callId,
                                          PChargingVectorHeader pChargingVector,
                                          RestrictedOutboundHeaders restrictedHeaders)
            throws InvalidArgumentException, ParseException {

        SipRequest sipRequest;

        Header remotePartyIdHeader = null;
        PAssertedIdentityList pAssertedIdentityList = null;

        // Only include Remote-Party-ID header if it is NOT restricted
        if (!restrictedHeaders.isRestricted(RestrictedHeader.REMOTE_PARTY_ID))
            remotePartyIdHeader = sipHeaderFactory.createRemotePartyIdHeader(
                    from, sipStackWrapper.getHost());

        // Only include P-Asserted-Identity header if it is NOT restricted
        if (!restrictedHeaders.isRestricted(RestrictedHeader.P_ASSERTED_IDENTITY)) {
            
            PAssertedIdentityHeader pAssertedIdentityFirstHeader =  null;
            PAssertedIdentityHeader pAssertedIdentitySecondHeader =  null;

            if( from.getPAssertedIdentityFirstValue() != null ) {
                               
                try {
                    pAssertedIdentityFirstHeader = sipHeaderFactory.createPAssertedIdentityHeader(
                                                                        from.getPAssertedIdentityFirstValue(),
                                                                        from.getPAssertedIdentityDisplayName());
                }
                catch(ParseException e) {
                    log.info("Could not generate a PAssertedID-value for first P-Asserted-Identity header (" + from.getPAssertedIdentityFirstValue()  + 
                            ") created from the callerinfopaidisplaynamefirstvalue hint specified in CCXML application. " +
                            "First P-Asserted-Identity header value will be generated from CallingParty.");
                }
            }
            
            if( pAssertedIdentityFirstHeader == null ) {
                pAssertedIdentityFirstHeader = sipHeaderFactory.createPAssertedIdentityHeader(from, sipStackWrapper.getHost());
            }
            
            if( from.getPAssertedIdentitySecondValue() != null ) {
                try {
                    pAssertedIdentitySecondHeader = sipHeaderFactory.createPAssertedIdentityHeader(
                                                                        from.getPAssertedIdentitySecondValue(),
                                                                        from.getPAssertedIdentitySecondValueDisplayName());
                    
                    // Validate that we have at most one TEL uri and one SIP uri (SIPS is not supported)
                    // RFC3325 (sect. 9.1): There may be one or two P-Asserted-Identity values. If there is one value, it MUST be a sip, sips, or tel URI.
                    //                      If there are two values, one value MUST be a sip or sips URI and the other MUST be a tel URI.
                    String scheme1 = pAssertedIdentityFirstHeader.getAddress().getURI().getScheme();
                    String scheme2 = pAssertedIdentitySecondHeader.getAddress().getURI().getScheme();
                    
                    if(!((scheme1.equals(ParameterNames.SIP_URI_SCHEME) && scheme2.equals(ParameterNames.TEL_URI_SCHEME)) || 
                            (scheme1.equals(ParameterNames.TEL_URI_SCHEME) && scheme2.equals(ParameterNames.SIP_URI_SCHEME))) ) {
                           
                           if(log.isDebugEnabled())
                               log.debug("Illegal schemes (" + scheme1 + ", " + scheme2  + ") found when attempting to generate two P-Asserted-Identity headers (" +
                                       pAssertedIdentityFirstHeader.toString() + " and " + pAssertedIdentitySecondHeader.toString() + "). Only one TEL and one SIP URI is allowed. Dropping second value." );
                           
                           // drop the second value
                           pAssertedIdentitySecondHeader = null;
                       }
                }
                catch(ParseException e) {
                    log.info("Could not generate a PAssertedID-value for second P-Asserted-Identity header (" + from.getPAssertedIdentityFirstValue()  + 
                            ") created from the callerinfopaidisplaynamesecondvalue hint specified in CCXML application. " +
                            " SIP INVITE will be sent with only one P-Asserted-Identity value.");
                }
            }
            
            pAssertedIdentityList = new PAssertedIdentityList();
            pAssertedIdentityList.add(pAssertedIdentityFirstHeader);
            
            if (pAssertedIdentitySecondHeader != null)
                pAssertedIdentityList.add(pAssertedIdentitySecondHeader);
        }
            

        if (from.getPresentationIndicator() == PresentationIndicator.ALLOWED) {
            sipRequest = createInviteRequest(
                    remotePartyUri,
                    sipHeaderFactory.createToHeader(to, remotePartyUri.getHost()),
                    sipHeaderFactory.createFromHeader(
                            from,
                            sipStackWrapper.getHost(),
                            sipStackWrapper.generateTag()),
                            pAssertedIdentityList,
                    remotePartyIdHeader,
                    sipHeaderFactory.createCallIdHeader(callId),
                    userAgent, expiration, from.getPresentationIndicator(),
                    preventLoopback,
                    diversion,
                    sdp);

        } else {
            sipRequest = createInviteRequest(
                    remotePartyUri,
                    sipHeaderFactory.createToHeader(to, remotePartyUri.getHost()),
                    sipHeaderFactory.createAnonymousFromHeader(
                            sipStackWrapper.getHost(),
                            sipStackWrapper.generateTag()),
                            pAssertedIdentityList,
                    remotePartyIdHeader,
                    sipHeaderFactory.createCallIdHeader(callId),
                    userAgent, expiration, from.getPresentationIndicator(),
                    preventLoopback,
                    diversion,
                    sdp);
        }

        if (pChargingVector != null)
            sipRequest.addPChargingVector(pChargingVector);

        return sipRequest;

    }

    public SipRequest createOptionsRequest(String localHost, int localPort,
                                           String remoteHost, int remotePort,
                                           int cSeq)
            throws InvalidArgumentException, ParseException {

        return createGeneralRequest(
                sipHeaderFactory.createUri(null, remoteHost, remotePort),
                sipHeaderFactory.createToHeader((String)null, remoteHost),
                sipHeaderFactory.createFromHeader(
                        null, localHost, sipStackWrapper.generateTag()),
                cSeq, sipProvider.getNewCallId(), Request.OPTIONS,
                DEFAULT_MAX_FORWARDS);
    }

    public SipRequest createPrackRequest(Dialog dialog, Response response, PChargingVectorHeader pChargingVector)
            throws SipException, ParseException {
        SipRequest sipRequest = new SipRequest(dialog.createPrack(response));

        if (pChargingVector != null)
            sipRequest.addPChargingVector(pChargingVector);

        return sipRequest;
    }

    public SipRequest createRegisterRequest(String registeredName,
                                            String remoteHost, int remotePort,
                                            int cSeq, CallIdHeader callIdHeader,
                                            Integer expires)
            throws ParseException, InvalidArgumentException {

        SipRequest sipRequest = createGeneralRequest(
                sipHeaderFactory.createUri(null, remoteHost, remotePort),
                sipHeaderFactory.createToHeader(
                        registeredName,
                        sipStackWrapper.getHost()),
                sipHeaderFactory.createFromHeader(
                        null, sipStackWrapper.getHost(),
                        sipStackWrapper.generateTag()),
                cSeq, callIdHeader, Request.REGISTER, 0);


       if (!((ConfigurationReader.getInstance().getConfig().getContactUriOverride()).equals(""))) {
            String contact = ConfigurationReader.getInstance().getConfig().getContactUriOverride();
            sipRequest.addContactHeader(contact, expires);
        }
        else {
            // Add Contact header field
            sipRequest.addContactHeader(
                registeredName,
                sipStackWrapper.getHost(),
                sipStackWrapper.getPort(),
                expires);
        }

        return sipRequest;
    }



    public SipRequest createNotifyRequest(String event,
                                          URI sendTo,
                                          URI sentFrom,
                                          int cSeq,
                                          CallIdHeader callIdHeader,
                                          String body,
                                          PChargingVectorHeader pChargingVector)
            throws ParseException, InvalidArgumentException {

        // Create NOTIFY request
        SipRequest sipRequest = createGeneralRequest(
                sendTo,
                sipHeaderFactory.createToHeader(sendTo),
                sipHeaderFactory.createFromHeader(sentFrom,
                        sipStackWrapper.generateTag()),
                cSeq,
                callIdHeader,
                Request.NOTIFY,
                DEFAULT_MAX_FORWARDS );


       if (!((ConfigurationReader.getInstance().getConfig().getContactUriOverride()).equals(""))) {
            String contact = ConfigurationReader.getInstance().getConfig().getContactUriOverride();
            sipRequest.addContactHeader(contact, null);
        }
        else {
            sipRequest.addContactHeader(sentFrom, null);
        }
        sipRequest.addEventHeader(event);
        if (pChargingVector != null)
            sipRequest.addPChargingVector(pChargingVector);

        if (SipConstants.MWI_EVENT_TYPE.equalsIgnoreCase(event)) {
            sipRequest.addSubscriptionStateHeader(SUBSCRIPTION_STATE_FOR_UNSOLICITED_MWI);
            if (body != null) {
                sipRequest.addBody(SipContentSubType.SIMPLE_MESSAGE_SUMMARY, body);
            }
        }

        return sipRequest;

    }

    public SipRequest createSipRequest(RemotePartyAddress uas, SipRequestEvent sourceSipRequestEvent)
    throws ParseException, InvalidArgumentException, UnsupportedEncodingException, TooManyHopsException {

        SIPRequest sourceSipRequest = (SIPRequest)sourceSipRequestEvent.getRequest();

        // Copy of the original SipRequest
        SIPRequest targetSipRequest = (SIPRequest)sourceSipRequest.clone();

        // Update the Request-URI in the Request Line (based on Execution Engine's information)
        SipUri sourceUri = updateSipRequestURI(sourceSipRequest, uas);
        if (sourceUri != null) {
            targetSipRequest.setRequestURI(sourceUri);
        } else {
            return null;
        }

        // Post-processing on the message header

        // Update the Max-Forward header
        MaxForwardsHeader uacMaxForwards = (MaxForwardsHeader)sourceSipRequest.getHeader(MaxForwardsHeader.NAME);
        if (uacMaxForwards != null) {
            uacMaxForwards.decrementMaxForwards();
            targetSipRequest.setMaxForwards(uacMaxForwards);
        }

        // Update the VIA header
        ViaList viaList = targetSipRequest.getViaHeaders();

        ViaHeader viaHeader = sipHeaderFactory.createViaHeader(
                CMUtils.getInstance().getLocalHost(),
                CMUtils.getInstance().getLocalPort(),
                SipConstants.TRANSPORT_UDP,
                createBranch(sourceSipRequest));

        viaList.addFirst(viaHeader);
        targetSipRequest.setHeader(viaList);

        return new SipRequest(targetSipRequest);
    }

    //=========================== Private methods =========================

    /**
     * Update the Request-URI in the incoming SIP INVITE to the UAS provided by CallManager's client.
     * <p>
     * If the received Request-URI is in TelURL format, it is transformed to SipURI format.  
     * 
     * @param {@link SIPRequest} sourceSipRequest
     * @param {@link RemotePartyAddress} uas
     * @return {@link SipUri}
     */
    private SipUri updateSipRequestURI(SIPRequest sourceSipRequest, RemotePartyAddress uas) {
        SipUri sourceUri = null;

        try {
            if (sourceSipRequest.getRequestURI() instanceof TelURL) {
                // Convert the TelURL received in the SipRequest to a SipURI.
                sourceUri = new SipUri();
                TelURL telUrl = (TelURL)sourceSipRequest.getRequestURI();
                sourceUri.setUser(telUrl.getPhoneNumber());
                sourceUri.setHost(uas.getHost());
                sourceUri.setPort(uas.getPort());
                sourceUri.setTransportParam(sourceSipRequest.getTopmostVia().getTransport());

                // Gateway SHOULD send SipURI in the incoming SipRequests
                log.warn("SipRequest " + sourceSipRequest.getFirstLine() + " Request-URI updated to SipURI (" + sourceUri + ")");

            } else if (sourceSipRequest.getRequestURI() instanceof SipUri) {
                // Retrieve the sourceUri from original request
                sourceUri = (SipUri)sourceSipRequest.getRequestURI();

                // Update the host:port to the UAS
                sourceUri.setHost(uas.getHost());
                sourceUri.setPort(uas.getPort());
            }
        } catch (Exception e) {
            log.error("Unable to update Request-URI " + sourceSipRequest.getRequestURI() + " to " + uas);
            sourceUri = null;
        }
        return sourceUri;
    }

    /**
     * According to section 13.2.1 of RFC 3261
     * Also sets Contact header field and loopback prevention.
     *
     * To, from, fromTag and sdp must not be null!
     */
    private SipRequest createInviteRequest(SipURI requestUri,
                                           ToHeader toHeader,
                                           FromHeader fromHeader,
                                           PAssertedIdentityList assertedIdHeaderList,
                                           Header remotePartyIdHeader,
                                           CallIdHeader callIdHeader,
                                           String userAgent,
                                           int expiration,
                                           PresentationIndicator pi,
                                           boolean preventLoopback,
                                           DiversionParty diversionParty,
                                           String sdp)
            throws InvalidArgumentException, ParseException {

        SipRequest sipRequest = createGeneralRequest(
                requestUri, toHeader, fromHeader, INITIAL_CSEQ,
                callIdHeader, Request.INVITE,
                ConfigurationReader.getInstance().getConfig().getNewCallInviteMaxForwards());// used to be DEFAULT_MAX_FORWARDS 
        
        // Add History-Info header field
        if(ConfigurationReader.getInstance().getConfig().addHistoryInfoToNewCallInvite()) {
        	String historyInfo = "<" + requestUri.toString() + ">;index=1";
        	sipRequest.addHistoryInfoHeader(historyInfo);
        }

        // Add Contact header field

        if (!((ConfigurationReader.getInstance().getConfig().getContactUriOverride()).equals(""))) {
            String contact = ConfigurationReader.getInstance().getConfig().getContactUriOverride();
            sipRequest.addContactHeader(contact, null);
        } else {
            sipRequest.addContactHeader(userAgent, sipStackWrapper.getHost(), sipStackWrapper.getPort(), null);
        }

        // Add Allow header field
        sipRequest.addAllowHeader();

        // Add Supported header field
        sipRequest.addSupportedHeader(true);

        // Add Accept header fields
        sipRequest.addAcceptHeader();
        sipRequest.addAcceptEncodingHeader();
        sipRequest.addAcceptLanguageHeader();

        // Add Expires header field
        sipRequest.addExpiresHeader(expiration);

        // Add User-Agent header field
        sipRequest.addUserAgentHeader(userAgent);

        // Add SDP body and necessary header fields indicating body content
        sipRequest.addBody(SipContentSubType.SDP, sdp);

        // Add P-Asserted-Identity header
        if (assertedIdHeaderList != null)
            sipRequest.addPAssertedIdentityHeaders(assertedIdHeaderList);

        // Add Remote-Party-ID header
        if (remotePartyIdHeader != null)
            sipRequest.addRemotePartyIdHeader(remotePartyIdHeader, pi);

        // Add Privacy header
        sipRequest.addPrivacyHeader(pi);

        // Add Loopback Prevention
        if (preventLoopback) {
            sipRequest.addDiversionHeader(diversionParty);
        }

        return sipRequest;
    }

    /**
     * According to section 8.1.1 of RFC 3261
     */
    private SipRequest createGeneralRequest(URI requestUri,
                                            ToHeader toHeader,
                                            FromHeader fromHeader,
                                            int cSeq,
                                            CallIdHeader callIdHeader,
                                            String method, int maxForwards)
            throws ParseException, InvalidArgumentException {

        // Create CSeq Header field
        CSeqHeader cSeqHeader = sipHeaderFactory.createCSeqHeader(cSeq, method);

        // Create Max-Forwards header field
        MaxForwardsHeader maxForwardsHeader =
            sipHeaderFactory.createMaxForwardsHeader(maxForwards);

        // Create initial Via header field
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader viaHeader = sipHeaderFactory.createViaHeader(
                sipStackWrapper.getHostAddress(),
                sipStackWrapper.getPort(),
                SipConstants.TRANSPORT_UDP,
                null);
        viaHeaders.add(viaHeader);

        // Create the request.
        Request request =
            messageFactory.createRequest(
                requestUri,
                method,
                callIdHeader,
                cSeqHeader,
                fromHeader,
                toHeader,
                viaHeaders,
                maxForwardsHeader);

        return new SipRequest(request);
    }

    /**
     * Generate a branch from the original SipRequest
     * Based on RFC 3261, Chapter 16.11-2
     * @param sourceSipRequest
     * @return branch
     */
    private String createBranch(SIPRequest sourceSipRequest) {
        String targetBranch = null;

        // Use original branch info from original SIP INVITE
        Via sourceTopMostVia = (Via)sourceSipRequest.getViaHeaders().getFirst();
        Integer sourceTopMostViaBranchHashCode = sourceTopMostVia.getBranch().hashCode();

        if (Integer.signum(sourceTopMostViaBranchHashCode) < 0) {
            targetBranch = Integer.toString(sourceTopMostViaBranchHashCode).substring(1);
        } else {
            targetBranch = Integer.toString(sourceTopMostViaBranchHashCode);
        }

        return SIPConstants.BRANCH_MAGIC_COOKIE.concat(targetBranch);
    }
    //TODO - there are obvious race condition in this procedure - locking mechanism must be provided
    //TODO - IO performance should be improved as well - reuse the stream for read write - java nio, RandomAccessFile eventually
    public SipRequest createNotifyRequest(String mailboxId, String dialogInfoFile, String body,  
            PChargingVectorHeader pChargingVector) throws ParseException, InvalidArgumentException, IOException, SipException, TrafficEventSenderException  {
  
		    // TODO - PChargingVector - new or the one from subscribe? 
            MfsEventManager mfsEventManager = MfsEventFactory.getMfsEvenManager();
		    Properties prop = mfsEventManager.getProperties(mailboxId, dialogInfoFile);
	    
	    	if (log.isDebugEnabled())log.debug("Dialog info from file: " + prop);
            	    	
	    	String requestUriString = prop.getProperty(SubscribeCall.NOTIFY_REQUEST_URI);
	    	SipURI requestUri = (SipURI) (new URLParser(requestUriString).parse()); 
			
	    	String fromString = prop.getProperty(SIPHeader.FROM).trim() + "\n";;
            From from = (From) new FromParser(fromString).parse();  
            
			String toString = prop.getProperty(SIPHeader.TO).trim() + "\n";;
            To to = (To) new ToParser(toString).parse(); 

			RouteList routeList = null;
			String routeString = prop.getProperty(SIPHeader.ROUTE);
			if (routeString != null) {
				routeString = routeString.trim() + '\n';
				RouteParser routeParser = new RouteParser(routeString);
				routeList =  (RouteList)routeParser.parse();

				
			}
			String callidString = prop.getProperty(SIPHeader.CALL_ID).trim() + "\n";;
            CallID callID = (CallID) new CallIDParser(callidString).parse();  
            
            //TODO - has the CSEQ been incremented since the previous notify
            String cseqString = prop.getProperty(SIPHeader.CSEQ).trim() + "\n";;
			CSeq cseq = (CSeq) new CSeqParser(cseqString).parse();
			
            String subscriptionStateString = prop.getProperty(SIPHeader.SUBSCRIPTION_STATE).trim() + "\n";;
            SubscriptionState subscriptionState = (SubscriptionState) new SubscriptionStateParser(subscriptionStateString).parse();

			
            MessageFactory mfactory = SipFactory.getInstance().createMessageFactory();
            
            // Create initial Via header field
            ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
            ViaHeader viaHeader = sipHeaderFactory.createViaHeader(
                    sipStackWrapper.getHostAddress(),
                    sipStackWrapper.getPort(),
                    SipConstants.TRANSPORT_UDP,
                    null);
            viaHeaders.add(viaHeader);
            
            // Create Max-Forwards header field
            MaxForwardsHeader maxForwardsHeader =
                sipHeaderFactory.createMaxForwardsHeader(DEFAULT_MAX_FORWARDS);
            
			gov.nist.javax.sip.message.SIPRequest newNotify = (gov.nist.javax.sip.message.SIPRequest) mfactory.
	        createRequest(requestUri, 
	        			Request.NOTIFY, 
	        			callID,
	        			cseq, 
	        			from, 
	        			to,
	        			viaHeaders,
	        			maxForwardsHeader);
			if (routeList != null) {
				newNotify.addFirst(routeList);
			}
			
            if (subscriptionState.getState().equalsIgnoreCase(SubscriptionState.TERMINATED)) {
            	if (log.isDebugEnabled())log.debug("Subscription has been terminated. Deleting the dialog info file: " + dialogInfoFile);
            	// delete the dialog info file
            	mfsEventManager.removeFile(mailboxId, dialogInfoFile, true);
            } else {
            	long expiryDateMs = Long.parseLong(prop.getProperty(SubscribeCall.EXPIRY_DATE));
            	long expiresL = expiryDateMs - System.currentTimeMillis();
            	Expires expiresHeader = new Expires();
            	int expiresI = (int)(expiresL /1000) +1; // +1 for round up 
            	expiresHeader.setExpires(expiresI);
            	newNotify.addHeader(expiresHeader);
            	// increment the Cseq for the new notify
            	CSeq newCseq = new CSeq();           	
            	newCseq.setSeqNumber(cseq.getSeqNumber() +1);
            	newCseq.setMethod(Request.NOTIFY);
            	prop.put(SIPHeader.CSEQ, newCseq.toString());
            	mfsEventManager.storeProperties(mailboxId, dialogInfoFile, prop);
            }
			
			newNotify.addHeader(subscriptionState);
			
		    gov.nist.javax.sip.header.Event evh = new gov.nist.javax.sip.header.Event();
			evh.setEventType(SipConstants.MWI_EVENT_TYPE);
			newNotify.addHeader(evh);
			
			SipRequest sipRequest = new SipRequest(newNotify);
			sipRequest.addBody( SipContentSubType.SIMPLE_MESSAGE_SUMMARY,body);
			
			// Add the pchargingvector
			// TODO check rfc to find out if subscribe pcharging should be reused
			String subscribePChargingString = prop.getProperty(SIPHeaderNamesIms.P_CHARGING_VECTOR);
			if (subscribePChargingString != null) {
				subscribePChargingString = subscribePChargingString.trim() + '\n';
				PChargingVectorHeader subscribePCharging = (PChargingVectorHeader)new PChargingVectorParser(subscribePChargingString).parse();
				newNotify.addHeader(subscribePCharging);
				
			} else {
				sipRequest.addPChargingVector(pChargingVector);
			}
			
			//TODO - the contact header - should the contact generated by the dialog be used, or the logic bellow?
            
	       if (!((ConfigurationReader.getInstance().getConfig().getContactUriOverride()).equals(""))) {
	            String contactStr = ConfigurationReader.getInstance().getConfig().getContactUriOverride();
	            sipRequest.addContactHeader(contactStr, null);
	        }
	        else {
	            sipRequest.addContactHeader(from.getAddress().getURI(), null);
	        }
		    
	       
	       
 	        return sipRequest;
    	
    }
    
}
