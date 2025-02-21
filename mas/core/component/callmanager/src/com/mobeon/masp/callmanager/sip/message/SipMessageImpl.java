/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import com.mobeon.masp.callmanager.sip.header.SipContentData;
import com.mobeon.masp.callmanager.sip.header.SipContentParser;
import com.mobeon.masp.callmanager.sip.header.SipContentSubType;
import com.mobeon.masp.callmanager.sip.header.SipContentType;
import com.mobeon.masp.callmanager.sip.header.SipHeaderFactory;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.callmanager.sip.SipConstants;
import com.mobeon.masp.callmanager.sip.contact.Contact;
import com.mobeon.masp.callmanager.sip.contact.ContactComparator;
import com.mobeon.masp.callmanager.CallPartyDefinitions.PresentationIndicator;
import com.mobeon.masp.callmanager.DiversionParty;
import com.mobeon.masp.callmanager.ExperiencedOperationalStatus;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.configuration.CallManagerConfiguration;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.releasecausemapping.Q850CauseLocationPair;
import com.mobeon.masp.callmanager.CallProperties.CallType;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.header.AllowHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.RequireHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.Header;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.CallInfoHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.ReasonHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.InvalidArgumentException;
import javax.sip.Transaction;
import javax.sip.ClientTransaction;
import javax.sip.address.URI;
import javax.sip.address.SipURI;
import java.util.Collection;
import java.util.ListIterator;
import java.util.HashSet;
import java.util.TreeSet;
import java.text.ParseException;

import gov.nist.javax.sip.header.SupportedList;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import gov.nist.javax.sip.header.ims.PAssertedIdentityList;
import gov.nist.javax.sip.header.ims.PrivacyHeader;
import gov.nist.javax.sip.header.ims.PChargingVectorHeader;

/**
 * A container for a SIP message. It is a base class extended by
 * {@link SipRequest} and {@link SipResponse}.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public abstract class SipMessageImpl implements SipMessage {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    protected final Message message;
    private final SipHeaderFactory sipHeaderFactory;

    // Thread-safe due to use of synchronized on methods where they are accessed.
    private boolean contentIsParsed = false;
    private Collection<SipContentData> contents;
    private SipContentData sdpContent;
    private SipContentData mediaControlContent;
    private SipContentData gtdContent;


    /**
     * @param   message The underlying SIP stack message.
     * @throws  IllegalArgumentException if <param>message</param> is null.
     */
    public SipMessageImpl(Message message) {
        if (message == null)
            throw new IllegalArgumentException("Message must not be null.");

        this.message = message;
        this.sipHeaderFactory = CMUtils.getInstance().getSipHeaderFactory();
    }


    //================ Methods to add header fields to a message ===============

    public void addAcceptHeader() throws ParseException {
        for (SipContentType contentType : SipConstants.getSupportedContentTypes()) {
            message.addHeader(sipHeaderFactory.createAcceptHeader(
                    contentType.getType(), contentType.getSubType()));
        }
    }

    public void addAcceptEncodingHeader() throws ParseException {
        message.addHeader(sipHeaderFactory.createAcceptEncodingHeader(null));
    }

    public void addAcceptLanguageHeader() throws ParseException {
    }

    /**
     * Add Allow header to the message.
     * UPDATE method is not added to all messages.  Therefore, callers of this method must add (if required).
     */
    public void addAllowHeader() throws ParseException {
        for (String method : SipConstants.getSupportedMethods()) {
            // Skip the UPDATE method, must be added by the callers (if required)
            if (Request.UPDATE.equalsIgnoreCase(method)) {
                continue;
            }
            message.addHeader(sipHeaderFactory.createAllowHeader(method));
        }
    }

    /**
     * Add Allow header method
     * @param method that must be added to the Allow header
     */
    public void addAllowHeader(String method) throws ParseException {
        if (method != null) {
            message.addHeader(sipHeaderFactory.createAllowHeader(method));
        }
    }

    public void removeAllowHeader() {
    	message.removeHeader(AllowHeader.NAME);
    }
    
    
    public void addPAssertedIdentityHeader(PAssertedIdentityHeader assertedIdHeader)
            throws ParseException {
        PAssertedIdentityHeader idHeader = assertedIdHeader;
        if (idHeader == null) {
            idHeader = sipHeaderFactory.createPAssertedIdentityHeader(
                    (FromHeader)message.getHeader(FromHeader.NAME));
        }
        message.addHeader(idHeader);
    }
    
    public void addPAssertedIdentityHeaders(PAssertedIdentityList assertedIdHeaderList)
            throws ParseException {
        PAssertedIdentityList idHeader = assertedIdHeaderList;
        if (assertedIdHeaderList == null) {
            assertedIdHeaderList = new PAssertedIdentityList();
            assertedIdHeaderList.add( sipHeaderFactory.createPAssertedIdentityHeader(
                                        (FromHeader)message.getHeader(FromHeader.NAME)));
        }
        message.addHeader(idHeader);
    }
    
    public void addHistoryInfoHeader(String historyInfo)
    		throws ParseException {
    	message.addHeader(sipHeaderFactory.createGenericHeader("History-Info", historyInfo));
    }

    public void addContactHeader(
            String contactUser, String host, int port, Integer expires)
            throws ParseException, InvalidArgumentException {
        message.addHeader(sipHeaderFactory.createContactHeader(
                contactUser, host, port, expires));
    }

    public void addContactHeader(URI uri, Integer expires)
            throws ParseException, InvalidArgumentException {
        message.addHeader(
                sipHeaderFactory.createContactHeader(uri, expires));
    }


   public void addContactHeader(String userAndDomain, Integer expires)
            throws ParseException, InvalidArgumentException {
        message.addHeader(sipHeaderFactory.createContactHeader(
                userAndDomain, expires));
    }

    public void addContactHeaderExpiration(int expires)
            throws InvalidArgumentException {
        ContactHeader contactHeader =
                (ContactHeader)message.getHeader(ContactHeader.NAME);
        if (contactHeader != null) {
            contactHeader.setExpires(expires);
        }
    }

    public void addDiversionHeader(DiversionParty diversionParty) throws ParseException {
        message.addHeader(sipHeaderFactory.createDiversionHeader(diversionParty));
    }

    public void addExpiresHeader(int expires)
            throws InvalidArgumentException {
        message.addHeader(sipHeaderFactory.createExpiresHeader(expires));
    }

    public void addBody(SipContentSubType type, String body)
            throws ParseException {
        if (body != null) {
            ContentTypeHeader contentTypeHeader;
            switch (type) {
                case SDP:
                    contentTypeHeader = sipHeaderFactory.createContentTypeHeader(
                            SipConstants.CT_APPLICATION,
                            SipConstants.CST_SDP);
                    message.setContent(body, contentTypeHeader);
                    if (log.isDebugEnabled())
                        log.debug("Added body content type: " +
                                SipConstants.CT_APPLICATION +
                                SipConstants.CST_SDP);
                    break;
                case MEDIA_CONTROL:
                    contentTypeHeader = sipHeaderFactory.createContentTypeHeader(
                            SipConstants.CT_APPLICATION,
                            SipConstants.CST_MEDIA_CONTROL);
                    message.setContent(body, contentTypeHeader);
                    if (log.isDebugEnabled())
                        log.debug("Added body content type: " +
                                SipConstants.CT_APPLICATION +
                                SipConstants.CST_MEDIA_CONTROL);
                    break;

                case SIMPLE_MESSAGE_SUMMARY:
                    contentTypeHeader = sipHeaderFactory.createContentTypeHeader(
                            SipConstants.CT_APPLICATION,
                            SipConstants.CST_SIMPLE_MESSAGE_SUMMARY);
                    message.setContent(body, contentTypeHeader);
                    if (log.isDebugEnabled())
                        log.debug("Added body content type: " +
                                SipConstants.CT_APPLICATION +
                                SipConstants.CST_SIMPLE_MESSAGE_SUMMARY);

                    break;
                default:
                    log.warn("addBody(): Unknown content sub type: " + type);

            }
        }
    }

    public void addOperationalStatusHeader(
            ExperiencedOperationalStatus experiencedOperationalStatus)
            throws ParseException {
        message.addHeader(sipHeaderFactory.
                createExperiencedOperationalStatusHeader(
                experiencedOperationalStatus));
    }

    public void addPrivacyHeader(PresentationIndicator pi)
            throws ParseException {
        PrivacyHeader header = sipHeaderFactory.createPrivacyHeader(pi);
        if (header != null) {
            message.addHeader(header);
        }
    }

    public void addRemotePartyIdHeader(
            Header remotePartyIdHeader, PresentationIndicator pi)
            throws ParseException {
        if (remotePartyIdHeader == null) {
            remotePartyIdHeader = sipHeaderFactory.createRemotePartyIdHeader(
                    (FromHeader)message.getHeader(FromHeader.NAME), pi);
        }
        message.addHeader(remotePartyIdHeader);
    }

    public void addSupportedHeader(Boolean isInviteRequest) throws ParseException {
        CallManagerConfiguration config = ConfigurationReader.getInstance().getConfig();

        SupportedList supportedList = new SupportedList();
        supportedList.add(sipHeaderFactory.createSupportedHeader(SipConstants.EXTENSION_100REL));

        if (config.isPreconditionEnabled()) {
            supportedList.add(sipHeaderFactory.createSupportedHeader(SipConstants.EXTENSION_PRECONDITION));
        }
        
        if (isInviteRequest && config.addHistoryInfoToNewCallInvite()) {
        	supportedList.add(sipHeaderFactory.createSupportedHeader(SipConstants.EXTENSION_HISTINFO));
        }

        message.addHeader(supportedList);
    }

    public void addRequireHeader(String extension) throws ParseException {
        message.addHeader(sipHeaderFactory.createRequireHeader(extension));
    }

    public void addToTag(String toTag)
            throws ParseException {
        if (toTag != null) {
            ToHeader to = (ToHeader) message.getHeader(ToHeader.NAME);
            if (to != null)
                to.setTag(toTag);
        }
    }

    public void addUnsupportedHeader(Collection<String> unsupportedExtensions)
            throws ParseException {
        for (String s : unsupportedExtensions) {
            message.addHeader(sipHeaderFactory.createUnsupportedHeader(s));
        }
    }

    public void addUserAgentHeader(String userAgent)
            throws ParseException {
        message.addHeader(sipHeaderFactory.createUserAgentHeader(userAgent));
    }

    public void addWarningHeader(SipWarning warning, String host)
            throws InvalidArgumentException, ParseException {
        message.addHeader(sipHeaderFactory.createWarningHeader(warning, host));
    }


    /**
     * Add an Event header to this Sip Message.
     * @param event - Event type to set.
     * @throws ParseException
     */
    public void addEventHeader(String event) throws ParseException {
        message.addHeader(sipHeaderFactory.createEventHeader(event));
    }

    /**
     * Add a Subscription-State header to the Sip message.
     * @param subscriptionState - Subscription state to set.
     * @throws ParseException
     */
    public void addSubscriptionStateHeader(String subscriptionState)
            throws ParseException {
        message.addHeader(sipHeaderFactory.
                createSubscriptionStateHeader(subscriptionState));

    }

    /**
     * Add a P-Charging-Vector from an existing one.
     * Copies ICID, ICID-Generated-At, Original-IOI.
     * Populates Terminating-IOI if it is empty, otherwise that is copied aswell.
     * @param pChargingVectorHeader - Original P-Charging-Vector
     * @throws ParseException if the header could not be created/added.
     */
    public void addPChargingVector(PChargingVectorHeader pChargingVectorHeader)
            throws ParseException {

        String icid = pChargingVectorHeader.getICID();
        String icidGeneratedAt = pChargingVectorHeader.getICIDGeneratedAt();
        String origIOI = pChargingVectorHeader.getOriginatingIOI();
        String termIOI = pChargingVectorHeader.getTerminatingIOI();

        message.addHeader(sipHeaderFactory.createPChargingVectorHeader(
                icid, icidGeneratedAt, origIOI, termIOI));
    }
    
    public void setPEearlyMediaHeader(String emParam)
            throws ParseException {

        message.setHeader(sipHeaderFactory.createPEarlyMediaHeader(emParam));
    }

    public void addRecordRoute(RecordRouteHeader pRecordRoute)
            throws ParseException {
        message.addHeader(sipHeaderFactory.createRecordRouteHeader(pRecordRoute));
    }
    
//================= Methods to check the content of the message ===============

    public boolean isMethodKnownButUnsupported() {
        return SipConstants.isMethodKnownButUnsupported(getMethod());
    }

    public boolean isMethodSupported() {
        return SipConstants.isMethodSupported(getMethod());
    }

    public boolean isReliableProvisionalResponsesRequired() {
        boolean required = false;

        ListIterator requireHeaders = message.getHeaders(RequireHeader.NAME);
        if (requireHeaders != null) {
            while (requireHeaders.hasNext()) {
                String extension =
                        ((RequireHeader)requireHeaders.next()).getOptionTag();
                if (extension.equals(SipConstants.EXTENSION_100REL))
                    required = true;
            }
        }
        return required;
    }

    public boolean isReliableProvisionalResponsesSupported() {
        boolean supported = false;

        ListIterator supportedHeaders = message.getHeaders(SupportedHeader.NAME);
        if (supportedHeaders != null) {
            while (supportedHeaders.hasNext()) {
                String extension =
                        ((SupportedHeader)supportedHeaders.next()).getOptionTag();
                if (extension.equals(SipConstants.EXTENSION_100REL))
                    supported = true;
            }
        }
        return supported;
    }

    public boolean isRequired(String extension) {
        boolean required = false;

        ListIterator requireHeaders = message.getHeaders(RequireHeader.NAME);
        if (requireHeaders != null) {
            while (requireHeaders.hasNext()) {
                String currentExtension =((RequireHeader)requireHeaders.next()).getOptionTag();
                if (currentExtension.equals(extension))
                    required = true;
            }
        }
        return required;
    }

    public boolean isSupported(String extension) {
        boolean supported = false;

        ListIterator supportedHeaders = message.getHeaders(SupportedHeader.NAME);
        if (supportedHeaders != null) {
            while (supportedHeaders.hasNext()) {
                String currentExtension = ((SupportedHeader)supportedHeaders.next()).getOptionTag();
                if (currentExtension.equals(extension))
                    supported = true;
            }
        }
        return supported;
    }

    public boolean isRequestUriValid() {
        return true;
    }

    public boolean isSipVersionSupported() {
        return SipConstants.isSipVersionSupported(message.getSIPVersion());
    }

    public boolean isToHeaderValid() {
        return true;
    }

    public boolean isUriSchemeSupported() {
        return true;
    }

    public boolean containsMediaControl() {
        return getContent(SipContentSubType.MEDIA_CONTROL) != null;
    }

    public boolean containsSdp() {
        return getContent(SipContentSubType.SDP) != null;
    }
    
    public boolean containsFromHeader() {
        return ((FromHeader)message.getHeader(FromHeader.NAME))  != null;
    }
    
    public boolean containsRecordRouteHeader() {
        return ((RecordRouteHeader)message.getHeader(RecordRouteHeader.NAME)) != null;
    }
    

//=============== Methods to retrieve the content of the message ==============

    public CallType getCallInfoType() {
        CallType callType = CallType.UNKNOWN;

        CallInfoHeader callInfoHeader =
                (CallInfoHeader)message.getHeader(CallInfoHeader.NAME);

        if (log.isDebugEnabled())
            log.debug("Checking Call-Info header: " + callInfoHeader);

        if ((callInfoHeader != null) && (callInfoHeader.getInfo() != null)) {
            String info = callInfoHeader.getInfo().toString().toLowerCase();
            if (info.contains("voice")) {
                callType = CallType.VOICE;
            } else if (info.contains("video")) {
                callType = CallType.VIDEO;
            }
        }

        if (log.isDebugEnabled())
            log.debug("Call Type from Call-Info header: " + callType);
        return callType;
    }

    public String getCallId() {
        return ((CallIdHeader)message.getHeader(CallIdHeader.NAME)).getCallId();
    }

    public TreeSet<Contact> getContacts(URI uri) {
        ListIterator contactHeaders =
                message.getHeaders(ContactHeader.NAME);

        ContactComparator comparator = new ContactComparator();
        TreeSet<Contact> set = new TreeSet<Contact>(comparator);

        while (contactHeaders.hasNext()) {
            Object object = contactHeaders.next();
            if (object instanceof ContactHeader) {
                ContactHeader contact = (ContactHeader)object;
                URI contactUri = contact.getAddress().getURI();

                // Since the comparator will find two Contacts with equal URI as
                // equal, only one of them will be inserted, thus ensuring that
                // the contact set contains only unique contacts. But, we have
                // to compare the contact to the URI given in the call to this
                // method, since that is the original URI that was used. All
                // contacts equal to the original URI should not be placed in
                // the set.
                if (!(contactUri.equals(uri)) && (contactUri.isSipURI()))
                    set.add(new Contact(contact.getQValue(), (SipURI)contactUri));
            }
        }
        return set;
    }

    public synchronized Collection<SipContentData> getContent() {
        if (!contentIsParsed)
            parseContent();

        return contents;
    }

    public synchronized String getContent(SipContentSubType subType) {
        String content = null;

        if (!contentIsParsed)
            parseContent();

        if ((subType == SipContentSubType.SDP) && (sdpContent != null))
            content = sdpContent.getContent();
        else if ((subType == SipContentSubType.MEDIA_CONTROL) &&
                (mediaControlContent != null))
            content = mediaControlContent.getContent();
        else if ( (subType == SipContentSubType.GTD) &&
                (gtdContent != null))
            content = gtdContent.getContent();

        return content;
    }

    public int getContactExpireTime() {
        if (log.isDebugEnabled())
            log.debug("Retrieving contact expiration from Expires and Contact headers.");

        // First retrive the expire time from the Expires header field
        int expireTime = getExpireTimeFromExpiresHeader();

        // Then try to retrieve the expire time from the contact.
        // If the contact has an expire time set, it overrides the
        // one set in the Expires header field.
        Integer contactExpiration = getExpireTimeFromContactHeader();
        if (contactExpiration >= 0) {
            expireTime = contactExpiration;
        }

        if (log.isDebugEnabled())
            log.debug("Contact expiration: " + expireTime);
        return expireTime;
    }

    public int getExpireTimeFromContactHeader() {
        ContactHeader contactHeader =
                (ContactHeader)message.getHeader(ContactHeader.NAME);

        if (log.isDebugEnabled())
            log.debug("Checking Contact header: " + contactHeader);

        int contactExpiration = -1;
        if (contactHeader != null) {
            contactExpiration = contactHeader.getExpires();
        }

        if (log.isDebugEnabled())
            log.debug("Contact header expiration: " + contactExpiration);
        return contactExpiration;
    }

    public int getExpireTimeFromExpiresHeader() {
        ExpiresHeader expiresHeader =
                (ExpiresHeader)message.getHeader(ExpiresHeader.NAME);

        int expireTime = -1;
        if (expiresHeader != null) {
            expireTime = expiresHeader.getExpires();
        }

        return expireTime;
    }

    public String getFromHeaderTag() {
        return ((FromHeader)message.getHeader(FromHeader.NAME)).getTag();
    }

    public URI getFromHeaderUri() {
        return ((FromHeader)message.getHeader(FromHeader.NAME)).getAddress().getURI();
    }
    
    public long getCSeq() {
    	return ((CSeqHeader)message.getHeader(CSeqHeader.NAME)).getSeqNumber();
    }
    
    public abstract String getMethod();

    public String getOperationalStatus() {
        String status = null;
        ExtensionHeader header = (ExtensionHeader)message.getHeader(
                SipConstants.HDR_EXPERIENCED_OPERATIONAL_STATUS);
        if (header != null) {
            status = header.getValue();
        }
        return status;
    }

    public Q850CauseLocationPair getQ850CauseLocation() {
        Q850CauseLocationPair q850Pair = null;

        ReasonHeader header = (ReasonHeader)message.getHeader(ReasonHeader.NAME);

        if (log.isDebugEnabled())
            log.debug("Retrieving Q.850 cause/location from Reason header: " +
                    header);

        if (header != null) {
            String protocol = header.getProtocol();
            int cause = header.getCause();

            if ((protocol != null) &&
                    (protocol.toLowerCase().equals(SipConstants.Q850)) &&
                    (cause > -1)) {

                Integer location = null;
                String locationString =
                        header.getParameter(SipConstants.Q850_LOCATION);

                if (locationString != null) {
                    try {
                        location = Integer.parseInt(locationString);
                    } catch (NumberFormatException ex) {
                        if (log.isDebugEnabled())
                            log.debug("Q.850 location not given as an integer " +
                                    "in Reason header. It is ignored.");
                    }
                } else {
                    if (log.isDebugEnabled())
                        log.debug("No Q.850 location found in Reason header.");
                }

                try {
                    q850Pair = new Q850CauseLocationPair(cause, location);
                } catch (IllegalArgumentException e) {
                    if (log.isDebugEnabled())
                        log.debug("No valid Q.850 cause/location found in " +
                                "Reason header: " + e.getMessage());
                }
            }
        }

        if (log.isDebugEnabled())
            log.debug("Retrieved from Reason header: " + q850Pair);

        return q850Pair;
    }


    public String getToHeaderTag() {
        return ((ToHeader)message.getHeader(ToHeader.NAME)).getTag();
    }

    public URI getToHeaderUri() {
        return ((ToHeader)message.getHeader(ToHeader.NAME)).getAddress().getURI();
    }

    public Collection<String> getUnsupportedButRequiredExtensions() {

        ListIterator requireHeaders = message.getHeaders(RequireHeader.NAME);
        Collection<String> unsupportedExtensions = new HashSet<String>();
        if (requireHeaders != null) {

            while (requireHeaders.hasNext()) {
                String extension =
                        ((RequireHeader)requireHeaders.next()).getOptionTag();
                if (!SipConstants.isExtensionSupported(extension)) {
                    unsupportedExtensions.add(extension);
                }
            }
        }

        return unsupportedExtensions;
    }

    public String getUserAgent() {

        UserAgentHeader userAgent =
            (UserAgentHeader) message.getHeader(UserAgentHeader.NAME);

        if (log.isDebugEnabled())
            log.debug("Retrieving User-Agent header: " + userAgent);

        String agent = "";
        if (userAgent != null) {
            ListIterator products = userAgent.getProduct();

            if (products != null) {
                while (products.hasNext()) {
                    agent = agent + " " + products.next();
                }

                if (log.isDebugEnabled())
                    log.debug("Retrieved User-Agent is: " + agent);
            }
        }

        return agent;
    }

    public String getVersion() {
        return message.getSIPVersion();
    }

    public String getTransactionId(Transaction transaction) {

        String transactionId;

        if (transaction instanceof ClientTransaction) {
            transactionId = getCallId() + ":" +
                    getFromHeaderTag();

        } else {
            transactionId = getCallId() + ":" +
                    getToHeaderTag();
        }

        return transactionId.toLowerCase();
    }

    //TODO: Move this method content to CallDispatcher instead or to OutboundCallImpl.
    public String createEarlyDialogIdForOutboundCall() {
        String earlyDialogId;

        earlyDialogId = getCallId();
        if (getFromHeaderTag() != null)
            earlyDialogId += ":" + getFromHeaderTag();

        return earlyDialogId.toLowerCase();
    }


//============================ Private methods ==============================

    private void parseContent() {
        contents = SipContentParser.getInstance().parseMessageContent(message);

        for (SipContentData content : contents) {
            if (content.isContentEncodingSupported()) {
                if (content.isContentSdp())
                    sdpContent = content;
                else if (content.isContentMediaControl())
                    mediaControlContent = content;
                else if (content.isContentGtd())
                    gtdContent = content;
            }
        }

        contentIsParsed = true;
    }
}
