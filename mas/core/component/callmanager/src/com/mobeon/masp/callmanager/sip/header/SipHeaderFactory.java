/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.header;

import com.mobeon.masp.callmanager.CallPartyDefinitions.PresentationIndicator;
import com.mobeon.masp.callmanager.*;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.sip.SipConstants;
import com.mobeon.masp.callmanager.sip.header.PEarlyMedia.PEarlyMediaTypes;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.InvalidArgumentException;
import java.text.ParseException;
import java.util.ArrayList;

import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import gov.nist.javax.sip.header.ims.PreferredIdentityHeader;
import gov.nist.javax.sip.header.ims.PrivacyHeader;
import gov.nist.javax.sip.header.ims.PChargingVectorHeader;
import gov.nist.javax.sip.header.HeaderFactoryImpl;

/**
 * This class is used to create new SIP headers used in SIP messages.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class SipHeaderFactory {
    

    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;

    public SipHeaderFactory(
            AddressFactory addressFactory, HeaderFactory headerFactory) {
        this.addressFactory = addressFactory;
        this.headerFactory = headerFactory;
    }

    public AcceptEncodingHeader createAcceptEncodingHeader(String encoding)
            throws ParseException {
        String encodingStr = encoding;
        if (encodingStr == null)
            encodingStr = SipConstants.ENCODING_NONE;

        return headerFactory.createAcceptEncodingHeader(encodingStr);
    }

    public AcceptHeader createAcceptHeader(String contentType, String subType)
            throws ParseException {
        return headerFactory.createAcceptHeader(contentType, subType);
    }

    public AllowHeader createAllowHeader(String method) throws ParseException {
        return headerFactory.createAllowHeader(method);
    }

    /**
     * This method creates a P-Asserted-Identity header field.
     * <p>
     * This method makes use of code in the implementation part of the NIST SIP
     * stack (i.e. not through the JAIN SIP interface). The reason for this is
     * that the IMS support in the SIP stack has not been added to the JAIN
     * interface yet.
     * @param callParty
     * @param host
     * @return
     * @throws ParseException
     */
    public PAssertedIdentityHeader createPAssertedIdentityHeader(
            CallingParty callParty, String host) throws ParseException {

        URI uri = createUriFromCallParty(callParty, host, null);
        Address address = addressFactory.createAddress(uri);
        
        String displayName = callParty.getPAssertedIdentityDisplayName();
        if( displayName != null ) {
            address.setDisplayName(displayName);
        }
        
        return ((HeaderFactoryImpl)headerFactory).
                createPAssertedIdentityHeader(address);
    }


    /**
     * This method creates a P-Asserted-Identity header field containing the given URI and display name.
     * <p>
     * This method makes use of code in the implementation part of the NIST SIP
     * stack (i.e. not through the JAIN SIP interface). The reason for this is
     * that the IMS support in the SIP stack has not been added to the JAIN
     * interface yet.
     * 
     * @param uri
     * @param displayName
     * @return
     * @throws ParseException
     */
    public PAssertedIdentityHeader createPAssertedIdentityHeader(
            String uri, String displayName) throws ParseException {

        URI uriForAddress = addressFactory.createURI(uri);
        Address address = addressFactory.createAddress(uriForAddress);
        
        if( displayName != null ) {
            address.setDisplayName(displayName);
        }

        return ((HeaderFactoryImpl)headerFactory).
                createPAssertedIdentityHeader(address);
    }
    
    /**
     * This method creates a P-Asserted-Identity header field.
     * <p>
     * This method makes use of code in the implementation part of the NIST SIP
     * stack (i.e. not through the JAIN SIP interface). The reason for this is
     * that the IMS support in the SIP stack has not been added to the JAIN
     * interface yet.
     * @param fromHeader
     * @return
     * @throws ParseException
     */
    public PAssertedIdentityHeader createPAssertedIdentityHeader(
            FromHeader fromHeader) throws ParseException {
        return ((HeaderFactoryImpl)headerFactory).
                createPAssertedIdentityHeader(fromHeader.getAddress());
    }

    /**
     * This method creates a CallId header field from the given id value.
     * @param callId The call id value.
     * @return The created header field.
     * @throws ParseException if the header field could not be created.
     */
    public CallIdHeader createCallIdHeader(String callId) throws ParseException {
        return headerFactory.createCallIdHeader(callId);
    }

    public Header createGenericHeader(String name, String value)
            throws ParseException {
    	return headerFactory.createHeader(name, value);
    }
    
    public ContactHeader createContactHeader(
            String contactUser, String host, int port, Integer expires)
            throws ParseException, InvalidArgumentException {

        URI contactUri = createUri(contactUser, host, port);
        Address contactAddress = addressFactory.createAddress(contactUri);
        ContactHeader contactHeader =
                headerFactory.createContactHeader(contactAddress);         

        if (expires != null)
            contactHeader.setExpires(expires);

        return contactHeader;
    }
    
    public ContactHeader createContactHeader(String userAndDomain, Integer expires)
            throws ParseException, InvalidArgumentException {


        Address contactAddress = addressFactory.createAddress(userAndDomain);
        ContactHeader contactHeader =

                headerFactory.createContactHeader(contactAddress);                
                

        if (expires != null)
            contactHeader.setExpires(expires);

        return contactHeader;
    }


    /**
     * Create a Contact header using given URI.
     * @param uri
     * @param expires - Expires time or null if expires should not be set.
     * @return a ContactHeader
     * @throws ParseException
     * @throws InvalidArgumentException
     */
    public ContactHeader createContactHeader(URI uri, Integer expires)
            throws ParseException, InvalidArgumentException {

        Address address = addressFactory.createAddress(uri);
        ContactHeader contactHeader =
                headerFactory.createContactHeader(address);

        if (expires != null)
            contactHeader.setExpires(expires);

        return contactHeader;
    }
    

    public ContentTypeHeader createContentTypeHeader(
            String type, String subType) throws ParseException {
       return headerFactory.createContentTypeHeader(type, subType);
    }

    public CSeqHeader createCSeqHeader(long cSeq, String method)
            throws InvalidArgumentException, ParseException {
        return headerFactory.createCSeqHeader(cSeq, method);
    }

    public Header createDiversionHeader(DiversionParty diversionParty) throws ParseException {
        URI localUri = createUriFromCallParty(diversionParty, diversionParty.getHostIp(), null);
        String diversion = "<" + localUri + ">";
        diversion += (diversionParty.getReason() != null) ? ";reason=" + diversionParty.getReason() : "";
        diversion += (diversionParty.getCounter() != null) ? ";counter=" + diversionParty.getCounter() : "";
        diversion += (diversionParty.getLimit() != null) ? ";limit=" + diversionParty.getLimit() : "";
        diversion += (diversionParty.getPrivacy() != null) ? ";privacy=" + diversionParty.getPrivacy() : "";
        diversion += (diversionParty.getScreen() != null) ? ";screen=" + diversionParty.getScreen() : "";
        diversion += (diversionParty.getExtension() != null) ? ";" + diversionParty.getExtension() : "";

        return headerFactory.createHeader(SipConstants.HDR_DIVERSION, diversion);
    }

    public Header createExperiencedOperationalStatusHeader(
            ExperiencedOperationalStatus experiencedOperationalStatus)
            throws ParseException {
        return headerFactory.createHeader(
                SipConstants.HDR_EXPERIENCED_OPERATIONAL_STATUS,
                experiencedOperationalStatus.getName());
    }

    public ExpiresHeader createExpiresHeader(int expires)
            throws InvalidArgumentException {
        return headerFactory.createExpiresHeader(expires);
    }

    public FromHeader createFromHeader(CallingParty callingParty, String host, String tag) throws ParseException {
        URI toUri = createUriFromCallParty(callingParty, host, null, callingParty.getFromUser());
        Address fromAddress = addressFactory.createAddress(callingParty.getFromDisplayName(), toUri);
        return headerFactory.createFromHeader(fromAddress, tag);
    }

    /**
     * Create From header using given URI and tag.
     * @param uri
     * @param tag
     * @return FromHeader
     * @throws ParseException
     */
    public FromHeader createFromHeader(URI uri, String tag)
            throws ParseException {
        Address address = addressFactory.createAddress(uri);
        return headerFactory.createFromHeader(address, tag);
    }

    public FromHeader createAnonymousFromHeader(String host, String tag)
            throws ParseException {
        URI fromUri = createUri("invalid", host, null);
        Address fromAddress = addressFactory.createAddress("Anonymous", fromUri);
        return headerFactory.createFromHeader(fromAddress, tag);
    }

    public MaxForwardsHeader createMaxForwardsHeader(int maxForwards)
            throws InvalidArgumentException {
        return headerFactory.createMaxForwardsHeader(maxForwards);
    }

    /**
     * @param pi
     * @return Null is returned if pi is unknown.
     * @throws ParseException
     */
    public PrivacyHeader createPrivacyHeader(PresentationIndicator pi)
            throws ParseException {
        PrivacyHeader header = null;
        if (pi == CallPartyDefinitions.PresentationIndicator.RESTRICTED) {
          header = ((HeaderFactoryImpl)headerFactory).createPrivacyHeader(
                  SipConstants.PRIVACY_RESTRICTED);
        } else if (pi == CallPartyDefinitions.PresentationIndicator.ALLOWED) {
            header = ((HeaderFactoryImpl)headerFactory).createPrivacyHeader(
                    SipConstants.PRIVACY_ALLOWED);
        }
        return header;
    }


    /**
     * Creates and returns a Reason header based on the Q.850 protocol.
     * The <param>cause</param> is set in the Reason header and if
     * <param>location</param> is not null, it is set in the Reason header
     * as well.
     * @param cause                     The Q.850 cause value. Range 0-127.
     * @param location                  The Q.850 location value. Range 0-15.
     * @return A SIP Reason header.
     * @throws InvalidArgumentException
     * {@link InvalidArgumentException} is thrown if the cause value is illegal.
     * @throws ParseException
     * {@link ParseException} is thrown if the Reason header could not be due
     * to illegal input parameters created.
     */
    public ReasonHeader createQ850ReasonHeader(int cause, Integer location)
            throws InvalidArgumentException, ParseException {
        ReasonHeader header =
                headerFactory.createReasonHeader(SipConstants.Q850, cause, null);

        if (location != null)
            header.setParameter(SipConstants.Q850_LOCATION, location.toString());

        return header;
    }

    public Header createRemotePartyIdHeader(
            CallingParty from, String host) throws ParseException {

        String privacy = null;
        PresentationIndicator pi = from.getPresentationIndicator();
        if (pi == PresentationIndicator.RESTRICTED) {
            privacy = SipConstants.REMOTE_PARTY_PRIVACY_RESTRICTED;
        } else if (pi == PresentationIndicator.ALLOWED) {
            privacy = SipConstants.REMOTE_PARTY_PRIVACY_ALLOWED;
        }
        URI uri = createUriFromCallParty(from, host, null);
        String party = "<" + uri.toString() + ">";

        party += ";party=calling";
        if (privacy != null) {
            party += ";privacy=" + privacy;
        }
        return headerFactory.createHeader(
                SipConstants.HDR_REMOTE_PARTY_ID, party);
    }

    public Header createRemotePartyIdHeader(
            FromHeader fromHeader, PresentationIndicator pi) throws ParseException {

        String privacy = null;
        if (pi == PresentationIndicator.RESTRICTED) {
            privacy = SipConstants.REMOTE_PARTY_PRIVACY_RESTRICTED;
        } else if (pi == PresentationIndicator.ALLOWED) {
            privacy = SipConstants.REMOTE_PARTY_PRIVACY_ALLOWED;
        }

        String party;
        String displayName = fromHeader.getAddress().getDisplayName();
        String uri = "<" + fromHeader.getAddress().getURI().toString() + ">";
        if (displayName != null) {
            party = displayName + " " + uri;
        } else {
            party = uri;
        }
        party += ";party=calling";
        if (privacy != null) {
            party += ";privacy=" + privacy;
        }
        return headerFactory.createHeader(
                SipConstants.HDR_REMOTE_PARTY_ID, party);
    }

    public SupportedHeader createSupportedHeader(String extension) throws ParseException {
        return headerFactory.createSupportedHeader(extension);
    }

    public RequireHeader createRequireHeader(String extension) throws ParseException {
        return headerFactory.createRequireHeader(extension);
    }

    public ToHeader createToHeader(String user, String host) throws ParseException {
        URI toUri = createUri(user, host, null);
        Address toAddress = addressFactory.createAddress(toUri);
        return headerFactory.createToHeader(toAddress, null);
    }

    /**
     * Create a To header using given URI
     * @param uri
     * @return ToHeader
     * @throws ParseException
     */
    public ToHeader createToHeader(URI uri)
            throws ParseException {
        Address address = addressFactory.createAddress(uri);
        return headerFactory.createToHeader(address,null);
    }



    public ToHeader createToHeader(CallPartyDefinitions callParty,
                                   String host)
            throws ParseException {
        URI toUri = createUriFromCallParty(callParty, host, null);
        Address toAddress = addressFactory.createAddress(toUri);
        return headerFactory.createToHeader(toAddress, null);
    }

    public UnsupportedHeader createUnsupportedHeader(String extension)
            throws ParseException {
        return headerFactory.createUnsupportedHeader(extension);
    }

    public URI createUriFromCallParty(CallPartyDefinitions callParty, String host, Integer port) throws ParseException {
        return createUriFromCallParty(callParty, host, port, null);
    }

    public URI createUriFromCallParty(CallPartyDefinitions callParty, String host, Integer port, String fromUserUri) throws ParseException {

        String uriHost;
        if (port != null) {
            uriHost = host + ":" + port;
        } else {
            uriHost = host;
        }

        URI uri;
        if (callParty != null) {
            if (callParty.getTelephoneNumber() != null) {

                uri = addressFactory.createSipURI(
                        SipUtils.escape(
                                (fromUserUri != null && fromUserUri.length() > 0) ? fromUserUri : callParty.getTelephoneNumber(),
                                SipUtils.USER_UNRESERVED),
                        uriHost);
                ((SipURI)uri).setParameter("user", "phone");

            } else if (callParty.getSipUser() != null) {

                uri = addressFactory.createURI("sip:" + callParty.getSipUser());
                if (port != null) ((SipURI)uri).setPort(port);

            } else {
                uri = addressFactory.createURI(callParty.getUri());
            }
        } else {
            uri = addressFactory.createSipURI(null, uriHost);
        }
        return uri;
    }

    
    public URI createUri(String user, String host, Integer port)
            throws ParseException {

        String uriHost = host;
        if (port != null) uriHost += ":" + port;
        return addressFactory.createSipURI(SipUtils.escape(user,
                SipUtils.USER_UNRESERVED), uriHost);
    }

    public UserAgentHeader createUserAgentHeader(String userAgent)
            throws ParseException {
        ArrayList<String> products = new ArrayList<String>();
        products.add(userAgent);
        return headerFactory.createUserAgentHeader(products);
    }

    public ViaHeader createViaHeader(
            String host, int port, String transport, String branch)
            throws InvalidArgumentException, ParseException {
        return headerFactory.createViaHeader(host, port, transport, branch);
    }

    public WarningHeader createWarningHeader(SipWarning warning, String host)
            throws InvalidArgumentException, ParseException {

        // Note! The "quoted-pair" escaping described in RFC3261 is not
        // implemented and therefor the warning texts used must be "pre-escaped".
        // See SipWarning.java for examples.
        return headerFactory.createWarningHeader(host, warning.getCode(),
                warning.getText());
    }

    /**
     * Create an Event header using specified event.
     * @param event
     * @return an Event header.
     * @throws ParseException
     */
    public EventHeader createEventHeader(String event) throws ParseException {
        return headerFactory.createEventHeader(event);
    }

    /**
     * Create a Subscription-State header with a given state.
     * The states specified in RFC 3265 are: "active", "pending" and "terminated".
     * @param subscriptionState - The state of subscription.
     * @return a Subscription-State header.
     * @throws ParseException
     */
    public SubscriptionStateHeader createSubscriptionStateHeader(String subscriptionState)
            throws ParseException {
        return headerFactory.createSubscriptionStateHeader(subscriptionState);
    }

    /**
     * Create a P-Charging-Vector header with given parameters
     * @param icid - ICID (Mandatory)
     * @param icidGeneratedAt
     * @param origIOI - Original IOI (might be null)
     * @param termIOI - Terminating IOI (might be null)
     * @return a new P-Charging-Vector
     * @throws ParseException if new header could not be created.
     */
    public PChargingVectorHeader createPChargingVectorHeader(
            String icid, String icidGeneratedAt,
            String origIOI, String termIOI)
            throws ParseException {

        PChargingVectorHeader chargingVector = ((HeaderFactoryImpl)headerFactory).
                createPChargingVectorHeader(icid);
        chargingVector.setOriginatingIOI(origIOI);
        chargingVector.setTerminatingIOI(termIOI);
        if (icidGeneratedAt != null)
            chargingVector.setICIDGeneratedAt(icidGeneratedAt);

        return chargingVector;
    }

    /**
     * Create a new P-Charging-Vector header from scratch
     * @param terminatingIOI - Set to true if this is the terminating ioi, set
     * to false if this is the originating ioi.
     * @return a new P-Charging-Vector
     * @throws ParseException if new header could not be created.
     */
    public PChargingVectorHeader createPChargingVectorHeader(boolean terminatingIOI)
            throws ParseException {

        String icid = SipUtils.generateICID();
        String localHost = CMUtils.getInstance().getLocalHost();
               
        if (terminatingIOI){
            String termIOI = ConfigurationReader.getInstance().getConfig().getTermIOI(); 

            //if no config is set for termIOI use localHost
            if(termIOI==null || termIOI.equals("")){
                termIOI=localHost;
            }
            return createPChargingVectorHeader(icid,localHost,null,termIOI);
        }
        else{
            return createPChargingVectorHeader(icid,localHost,localHost,null);
        }

    }
    
    /**
     * Creates proprietary SIP header. <br/><br/>
     * 
     * Only accepts header name starting with "X-", "x-", "P-" or "p-". <br/>
     * The following standard "P-" headers processed by the SIP stack are not accepted: 
     * P-Asserted-Identity, P-Preferred-Identity and P-Charging-Vector.<br/>
     * P-Early-Media must use a valid value [RFC 5009]  (eg: sendrecv, sendonly, etc.)<br/>
     * There is no extra validation done for the values of the other proprietary headers.
     * 
     * 
     * 
     * @param name header name
     * @param value header value
     * @return The {@link ExtensionHeader}
     * @throws ParseException If the header name or value cannot be parsed
     */
    public ExtensionHeader createProprietaryHeader(String name, String value) 
        throws ParseException {
        
        // We limit the type of SIP Header that can be added to proprietary SIP headers
        // in order to reduce the risk of interfering we normal SIP signalling.
        //
        // P-Charging-Vector is also excluded since it is handled differently by the Call Manager.      
        if( !(name.startsWith("P-") || name.startsWith("X-") || name.startsWith("p-") || name.startsWith("x-")) ||
            name.equalsIgnoreCase(PChargingVectorHeader.NAME) ||
            name.equalsIgnoreCase(PAssertedIdentityHeader.NAME) ||
            name.equalsIgnoreCase(PreferredIdentityHeader.NAME) ) {
            
            throw new ParseException(name + " is not a valid P-header or X-header", 0); 
        }
        else if (name.equalsIgnoreCase(PEarlyMediaHeader.NAME)){
            return createPEarlyMediaHeader(value);
        }
        else {
            return (ExtensionHeader)headerFactory.createHeader(name, value);
            
        }
         
    }
    
    /**
     * Create a P-Early-Media header. The value is set to  <param>emParam</param>.
     * Only one em-param is supported.
     * <p>
     * Valid values for em-param      = "sendrecv" / "sendonly" / "recvonly"
     *                                   / "inactive" / "gated" / "supported" / token
     * @param emParam The value for this header
     * @throws  java.text.ParseException if the header field could not be created.
     */
    public PEarlyMedia createPEarlyMediaHeader(String emParam) throws ParseException {
        if(PEarlyMediaTypes.PEARLY_MEDIA_SENDRECV.getValue().equalsIgnoreCase(emParam)){
            return new PEarlyMedia(PEarlyMediaTypes.PEARLY_MEDIA_SENDRECV);
        }
        else if(PEarlyMediaTypes.PEARLY_MEDIA_INACTIVE.getValue().equalsIgnoreCase(emParam)){
            return new PEarlyMedia(PEarlyMediaTypes.PEARLY_MEDIA_INACTIVE);
        }
        else if(PEarlyMediaTypes.PEARLY_MEDIA_SUPPORTED.getValue().equalsIgnoreCase(emParam)){
            return new PEarlyMedia(PEarlyMediaTypes.PEARLY_MEDIA_SUPPORTED);
        }
        else if(PEarlyMediaTypes.PEARLY_MEDIA_SENDONLY.getValue().equalsIgnoreCase(emParam)){
            return new PEarlyMedia(PEarlyMediaTypes.PEARLY_MEDIA_SENDONLY);
        }
        else if(PEarlyMediaTypes.PEARLY_MEDIA_RECVONLY.getValue().equalsIgnoreCase(emParam)){
            return new PEarlyMedia(PEarlyMediaTypes.PEARLY_MEDIA_RECVONLY);
        }
        else if(PEarlyMediaTypes.PEARLY_MEDIA_GATED.getValue().equalsIgnoreCase(emParam)){
            return new PEarlyMedia(PEarlyMediaTypes.PEARLY_MEDIA_GATED);
        }
        else {
            throw new ParseException(PEarlyMediaHeader.NAME + " header value is invalid :" + emParam, 0); 
        }
    }
    
    public RecordRouteHeader createRecordRouteHeader(RecordRouteHeader pRecordRoute) throws ParseException {
        return headerFactory.createRecordRouteHeader(pRecordRoute.getAddress());
    }

}
