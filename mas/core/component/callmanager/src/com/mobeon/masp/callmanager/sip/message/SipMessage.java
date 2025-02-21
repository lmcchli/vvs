/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import gov.nist.javax.sip.header.ims.PAssertedIdentityList;

import javax.sip.InvalidArgumentException;
import javax.sip.Transaction;
import javax.sip.address.URI;
import javax.sip.header.Header;
import java.text.ParseException;
import java.util.Collection;
import java.util.TreeSet;

import com.mobeon.masp.callmanager.CallPartyDefinitions;
import com.mobeon.masp.callmanager.CallPartyDefinitions.PresentationIndicator;
import com.mobeon.masp.callmanager.DiversionParty;
import com.mobeon.masp.callmanager.ExperiencedOperationalStatus;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.CallProperties.CallType;
import com.mobeon.masp.callmanager.releasecausemapping.Q850CauseLocationPair;
import com.mobeon.masp.callmanager.sip.header.SipContentSubType;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.callmanager.sip.header.SipContentData;
import com.mobeon.masp.callmanager.sip.contact.Contact;

/**
 * A container for a SIP message.
 * This is an interface implemented by {@link SipMessageImpl}.
 *
 * @author Malin Nyfeldt
 */
public interface SipMessage {

    // Methods to add header fields to a message

    /**
     * Adds an Accept header field that contains the currently supported
     * content types: application/sdp and application/media_control+xml
     * @throws  java.text.ParseException if the header field could not be created.
     */
    public void addAcceptHeader() throws ParseException;

    /**
     * Adds an Accept-Encoding header field that indicates that no encodings
     * are accepted, i.e. the Accept-Encoding header field is set to "identity".
     * @throws  ParseException if the header field could not be created.
     */
    public void addAcceptEncodingHeader() throws ParseException;

    /**
     * Adds an Accept-Language header field containing all languages that are
     * supported.
     * <p>
     * Currently, all languages are supported. This is indicated by NOT
     * including an Accept-Language header at all.
     * @throws  ParseException if the header field could not be created.
     */
    public void addAcceptLanguageHeader() throws ParseException;

    /**
     * Adds Allow headers for all request methods that are allowed:
     * INVITE, ACK, BYE, CANCEL, OPTIONS
     * @throws  ParseException if a header field could not be created.
     */
    public void addAllowHeader() throws ParseException;

    /**
     * Adds a P-Asserted-Identity header field to the SIP message.
     * If the <param>assertedIdHeader</param> is not null, it is added to the
     * SIP message.
     * Otherwise, a P-Asserted-Identity header field is created based on
     * information from the From header field and added to the SIP message.
     * @param assertedIdHeader The P-Asserted-Identity header to add.
     * @throws ParseException if the P-Asserted-Identity header could not be
     * created based on information from the From header field.
     */
    public void addPAssertedIdentityHeader(
            PAssertedIdentityHeader assertedIdHeader) throws ParseException;
    
    /**
     * Adds a P-Asserted-Identity header field to the SIP message.
     * If the <param>assertedIdHeaderList</param> is not null, it is added to the
     * SIP message.
     * Otherwise, a P-Asserted-Identity header field is created based on
     * information from the From header field and added to the SIP message.
     * @param assertedIdHeaderList The P-Asserted-Identity header list to add.
     * @throws ParseException if the P-Asserted-Identity header could not be
     * created based on information from the From header field.
     */
    public void addPAssertedIdentityHeaders(
            PAssertedIdentityList assertedIdHeaderList) throws ParseException;

    public void addHistoryInfoHeader(String historyInfo)
    		throws ParseException;    
    /**
     * Adds a Contact header with the given <param>expires</param> time.
     * <p>
     * The contact header will consist of a SIP URI with the SIP stacks values
     * for host and port.
     * <p>
     * The expires parameter is only set if <param>expires</param> is not null.
     * @param   contactUser
     * @param   expires Must be null or a positive integer.
     * @throws  ParseException if the Contact header could not be created
     * @throws  javax.sip.InvalidArgumentException If the expires time is a
     *          negative value.
     */
    public void addContactHeader(
            String contactUser, String host, int port, Integer expires)
            throws ParseException, InvalidArgumentException;

    /**
     * Adds a Contact header with the given <param>expires</param> time.
     * <p>
     * The contact header will consist of a SIP URI.
     * <p>
     * The expires parameter is only set if <param>expires</param> is not null.
     * @param   uri
     * @param   expires Must be null or a positive integer.
     * @throws  ParseException if the Contact header could not be created
     * @throws  javax.sip.InvalidArgumentException If the expires time is a negative value.
     */
    public void addContactHeader(URI uri, Integer expires)
        throws ParseException, InvalidArgumentException;

    /**
     * Adds an expiration time to the Contact-Header of the message.
     * @param expires Expiration time in second.
     * @throws InvalidArgumentException If the expiration time could not be set.
     */
    public void addContactHeaderExpiration(int expires)
            throws InvalidArgumentException;

    public void addDiversionHeader(DiversionParty diversionParty)
            throws ParseException;

    public void addExpiresHeader(int expires)
            throws InvalidArgumentException;

    public void addBody(SipContentSubType type, String body)
            throws ParseException;

    public void addOperationalStatusHeader(
            ExperiencedOperationalStatus experiencedOperationalStatus)
            throws ParseException;

    public void addPrivacyHeader(CallPartyDefinitions.PresentationIndicator pi)
            throws ParseException;

    public void addRemotePartyIdHeader(
            Header remotePartyIdHeader, PresentationIndicator pi)
            throws ParseException;

    public void addSupportedHeader(Boolean isInviteRequest) throws ParseException;

    public void addRequireHeader(String extension) throws ParseException;

    public void addToTag(String toTag)
            throws ParseException;

    public void addUnsupportedHeader(Collection<String> unsupportedExtensions)
            throws ParseException;

    public void addUserAgentHeader(String userAgent) throws ParseException;

    /**
     * Adds a Warning header field to the <param>message</param>. The type of
     * warning is given by <param>warning</param>.
     * @param   warning
     * @param   host
     * @throws  InvalidArgumentException    if an undefined warning code was used.
     * @throws  ParseException              if the header could not be created.
     */
    public void addWarningHeader(SipWarning warning, String host)
            throws InvalidArgumentException, ParseException;
    
    /**
     * Adds an P-Early-Media header field to the <param>message</param>. The value is set to  <param>emParam</param>.
     * Only one em-param is supported. If a P-Early-Media header is already present for the <param>message</param>, it is replaced. 
     * <p>
     * Valid values for em-param      = "sendrecv" / "sendonly" / "recvonly"
     *                                   / "inactive" / "gated" / "supported" / token
     * @param emParam The value for this header
     * @throws  java.text.ParseException if the header field could not be created.
     */
    public void setPEearlyMediaHeader(String emParam) throws ParseException;



    // Methods to check the content of the message

    /**
     * Checks whether the message method is known but not supported. Only methods
     * INFO and REGISTER are known but not supported.
     * @return  True if the method is known but not supported, false otherwise.
     */
    public boolean isMethodKnownButUnsupported();

    /**
     * Checks whether the message method is supported. The following methods are
     * supported: INVITE, ACK, BYE, CANCEL, OPTIONS
     * @return  True if the method is supported, false otherwise.
     */
    public boolean isMethodSupported();

    /**
     * Returns whether the message requires the use of the extension "100rel"
     * (specified in RFC 3262) or not.
     * @return  Returns true if the "100rel" extension is required,
     *          false otherwise.
     */
    public boolean isReliableProvisionalResponsesRequired();

    /**
     * Returns whether the message requires the use of the given extension.
     * @return  Returns true if the given extension is required, false otherwise.
     */
    public boolean isRequired(String extension);

    /**
     * Returns whether the message supports the use of the extension "100rel"
     * (specified in RFC 3262) or not.
     * @return  Returns true if the "100rel" extension is supported,
     *          false otherwise.
     */
    public boolean isReliableProvisionalResponsesSupported();

    /**
     * Returns whether the message supports the use of the given extension.
     * @return  Returns true if the given extension is supported, false otherwise.
     */
    public boolean isSupported(String extension);

    /**
     * Returns whether the Request-URI of this message is valid or not.
     * Currently all Request-URIs are supported. This means that all incoming
     * calls are handled regardless of Request-URI.
     * @return  True since currently all Request-URIs are considered valid.
     */
    public boolean isRequestUriValid();

    /**
     * Returns whether the given SIP version is supported or not.
     * Currently only the version "SIP/2.0" is supported.
     * @return  True if <param>sipVersion</param> is supported, false otherwise.
     */
    public boolean isSipVersionSupported();

    /**
     * Returns whether the To header field of this message is valid or not.
     * Currently all types of To header fields are supported. It is up to the
     * call client to reject calls if the recipient shall not be handled.
     * @return  True since currently all To headers are considered valid.
     */
    public boolean isToHeaderValid();

    /**
     * Returns whether the URI scheme used in the Request-URI is supported or not.
     * Currently all Request-URIs are supported regardless of URI scheme.
     * This means that all incoming calls are handled regardless of URI scheme
     * in the Request-URI.
     * @return  True since all URI schemes is supported for the Request-URI.
     */
    public boolean isUriSchemeSupported();

    /**
     * @return Returns true if this message contains a media control message
     * body. Otherwise, false is returned.
     */
    public boolean containsMediaControl();

    /**
     * @return Returns true if this message contains an SDP message
     * body. Otherwise, false is returned.
     */
    public boolean containsSdp();


    // Methods to retrieve the content of the message

    /**
     * Returns the call type found in the Call-Info header of the message.
     * @return  {@link CallType.VOICE} if the Call-Info header
     *              contained the string "voice",
     *          {@link CallType.VIDEO} if the Call-Info header
     *              contained the string "video", and
     *          {@link CallType.UNKNOWN} otherwise.
     */
    public CallProperties.CallType getCallInfoType();

    /**
     * @return the call ID from the CallId Header.
     */
    public String getCallId();

    /**
     * Retrieves the Contacts from this SIP message and returns them in a set of
     * contacts. Only unique contacts are placed in the set that is returned.
     * The <param>uri</param> is the original message contact and Contacts from
     * this SIP message can only be placed in the set if they differ from the
     * <param>uri</param>.
     * @param uri       The original URI.
     * @return          A set of unique Contacts sorted after Q value.
     *                  Contacts with higher Q value are placed first in the set.
     */
    public TreeSet<Contact> getContacts(URI uri);

    /**
     * Returns a collection of the SIP message contents.
     * @return  A collection of all content parts of the message or an empty
     *          collection if the body was not parsed correctly or contained no
     *          body.
     */
    public Collection<SipContentData> getContent();

    /**
     * Returns the body of a SIP message that matches the requested
     * {@link SipContentSubType}.
     * <p>
     * If <param>type</param> equals {@link SipContentSubType.SDP} a
     *  body part with Content-Type "application/sdp" is returned if present.
     * If <param>type</param> equals {@link SipContentSubType.MEDIA_CONTROL} a
     *  body part with Content-Type "application/media_control+xml" is returned
     * if present.
     * @param   subType
     * @return  The body of the requested <param>type</param> or null if it
     *          could not be found.
     */
    public String getContent(SipContentSubType subType);

    /**
     * Returns the Contact expire time for this message.
     * <p>
     * First the Contact header field is checked. If an Expires time is set
     * in the Contact header field this value is returned. If there are more
     * than one Contact header fields, the first one is used.
     * <p>
     * If there is not expires time in the Contact header field, the value of
     * the Expires header (if present) is returned instead.
     * <p>
     * If the expire time is not set in the Contact header or in an Expires
     * header, zero is returned.
     * @return  The expire time for a contact or -1 if not set.
     */
    public int getContactExpireTime();

    /**
     * @return the expires time in the Contact Header. Returns -1 if not set.
     */
    public int getExpireTimeFromContactHeader();

    /**
     * @return the expires time in the Expires Header. Returns -1 if not set.
     */
    public int getExpireTimeFromExpiresHeader();

    /**
     * @return the tag in the From header field.
     */
    public String getFromHeaderTag();

    /**
     * @return the URI in the From header field.
     */
    public URI getFromHeaderUri();

    public String getMethod();

    /**
     * @return  the value of the Experienced-Operational-Status header field
     *          in the message. Null is returned if the header
     *          field is not present.
     */
    public String getOperationalStatus();

    /**
     * Returns a Q.850 cause/location pair retrieved from the Reason header.
     * Null is returned if there is no Reason header, if the Reason header could
     * not be parsed, if the protocol is NOT Q.850 or if the cause value is not
     * set.
     * The returned pair contains a location that is null if location was not
     * set in the Reason header field or if the location was not an integer.
     *
     * @return Returns a pair of Q.850 cause/location.
     */
    public Q850CauseLocationPair getQ850CauseLocation();


    /**
     * @return the tag in the To header field.
     */
    public String getToHeaderTag();

    /**
     * @return the URI in the To header field.
     */
    public URI getToHeaderUri();

    /**
     * Returns a collection of all extensions that are required in this
     * <param>message</param> but not supported.
     * @return  A string collection of unsupported but required extensions.
     */
    public Collection<String> getUnsupportedButRequiredExtensions();

    /**
     * @return  the value of the User-Agent header field. 
     *          The empty string is returned if header is not set.
     */
    public String getUserAgent();

    /**
     * @return the version in the Request-URI.
     */
    public String getVersion();

    /**
     * @return the CSeq header value
     */
    public long getCSeq();
    
    /**
     * Calculates the transaction id for this SIP message based on which
     * <param>transaction</param> this message belongs to.
     * <p>
     * The transaction ID is based on the CallID, To and From tags.
     * For a client transaction, the transaction ID looks like this:
     * <callid>:<from tag>
     * For a server transaction, the transaction ID looks like this:
     * <callid>:<to tag>
     * <p>
     * The transaction ID can be used as a replacement if the dialog id cannot
     * be found for a transaction, e.g. when trying to locate a matching call
     * in the {@link com.mobeon.masp.callmanager.callhandling.CallDispatcher}.
     *
     * @param transaction
     *
     * @return A transaction id created from the sip message callId, To and From
     * header fields.
     */
    public String getTransactionId(Transaction transaction);

    /**
     * Constructs an early dialog ID that can be used for an outbound call.
     * The assumption made is that the SIP message is an INVITE.
     * <p>
     * The dialog ID is based on the CallID, To and From tags.
     * The dialog ID looks like this:
     * <callid>:<from tag>
     * <p>
     * This method is needed since an early dialog for an outbound call normally
     * should be created from the INVITE response. However, we cannot wait that
     * long to create the dialog ID, therefore a special method exists that
     * creates the early dialog from the sent INVITE message instead.
     *
     * TODO: Move this method content to CallDispatcher instead or to OutboundCallImpl.
     *
     * @return A dialog id
     */
    public String createEarlyDialogIdForOutboundCall();

}
