/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip;

import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.RedirectingParty;
import com.mobeon.masp.callmanager.SipUtils;
import com.mobeon.masp.callmanager.configuration.CallManagerConfiguration;
import com.mobeon.masp.callmanager.configuration.ConfigConstants;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.gtd.GtdDescription;
import com.mobeon.masp.callmanager.gtd.GtdFactory;
import com.mobeon.masp.callmanager.gtd.GtdParseException;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.header.SipContentSubType;
import com.mobeon.masp.callmanager.CallPartyDefinitions.PresentationIndicator;
import com.mobeon.masp.callmanager.callhandling.CallParameters;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.sip.message.Request;
import javax.sip.header.ToHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ExtensionHeader;
import javax.sip.address.Address;
import javax.sip.address.URI;
import javax.sip.address.SipURI;
import javax.sip.address.TelURL;
import java.util.*;
import java.text.ParseException;

import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.ParserFactory;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import gov.nist.javax.sip.header.ims.PrivacyHeader;
import gov.nist.javax.sip.header.ims.HistoryInfoHeader;

/**
 * This class is a singleton used for call parameter retrieval of SIP requests.
 * <p>
 * NOTE: This class uses the NIST SIP stack directly (i.e. not through the
 * JAIN SIP interface). This is not the desired way to implement usage of
 * the SIP stack but was chosen in order to make use of existing code rather
 * than writing similar code again.
 *
 * @author Malin Flodin
 */
public class CallParameterRetrieval {

    private final ILogger log = ILoggerFactory.getILogger(getClass());
    private ArrayList<String> userAgentWithPhoneInUriButNoUserParameter =
            new ArrayList<String>();
    private boolean emptyUserAgentWithPhoneInUriButNoUserParameter = false;
    private boolean allUserAgentWithPhoneInUriButNoUserParameter = false;

    private static final String ANONYMOUS = "anonymous";
    private static final String UNKNOWN = "unknown";
    private static final String PRIVACY_NONE = "none";
    private static final String PRIVACY_OFF = "off";
    private static final String PRIVACY_HEADER = "header";
    private static final String PRIVACY_SESSION = "session";
    private static final String PRIVACY_HISTORY = "history";


    private static final String PARTY_CALLING = "calling";
    private static final String PARTY_PARAMETER = "party";
    private static final String PRIVACY_PARAMETER = "privacy";
    private static final String REASON_PARAMETER = "reason";

    // Different User Agents that has special treatment
    private static final String UA_CISCO = "cisco";

    static CallParameterRetrieval oneInstance = new CallParameterRetrieval();
    private static final String REMOTE_PARTY_HEADER = "Remote-Party-ID";
    private static final String DIVERSION_HEADER = "Diversion";

    public static CallParameterRetrieval getInstance() {
        return oneInstance;
    }

    private CallParameterRetrieval() {

        userAgentWithPhoneInUriButNoUserParameter =
                ConfigurationReader.getInstance().getConfig().getUserAgentWithPhoneInUriButNoUserParameter();
        if( userAgentWithPhoneInUriButNoUserParameter.contains(ConfigConstants.USER_AGENT_EMPTY)) {
            emptyUserAgentWithPhoneInUriButNoUserParameter = true;
        }
        if( userAgentWithPhoneInUriButNoUserParameter.contains(ConfigConstants.USER_AGENT_ALL)) {
            allUserAgentWithPhoneInUriButNoUserParameter = true;
        }

    }

    /**
     * Adds a useragent to the list of useragents
     * that receives special handling when user=phone is missing
     * @param userAgentText the text to look to match the useragent with
     */
    public void addUserAgent(String userAgentText) {
        if (ConfigConstants.USER_AGENT_EMPTY.equalsIgnoreCase(userAgentText)) {
            emptyUserAgentWithPhoneInUriButNoUserParameter = true;
        } else if (ConfigConstants.USER_AGENT_ALL.equalsIgnoreCase(userAgentText)) {
            allUserAgentWithPhoneInUriButNoUserParameter = true;
        } else if (userAgentText != null) {
            userAgentWithPhoneInUriButNoUserParameter.add(userAgentText);
        }
    }

    /**
     * Removes a useragent from the list of useragents
     * that receives special handling when user=phone is missing.
     * @param userAgentText the useragent to remove.
     */
    public void removeUserAgent(String userAgentText) {
        if (ConfigConstants.USER_AGENT_EMPTY.equalsIgnoreCase(userAgentText)) {
            emptyUserAgentWithPhoneInUriButNoUserParameter = false;
        } else if (ConfigConstants.USER_AGENT_ALL.equalsIgnoreCase(userAgentText)) {
            allUserAgentWithPhoneInUriButNoUserParameter = false;
        } else if (userAgentText != null) {
            userAgentWithPhoneInUriButNoUserParameter.remove(userAgentText);
        }
    }


    /**
     * Retrieves call parameters from the SIP message.
     * <p>
     * First the call parameters are retrieved from the Request-URI or the To
     * header field if present there (indicated by the parameter test=on)
     * if configuration indicates that test input shall be supported.
     * This is not the normal way to retrieve
     * call parameters but since few SIP phones used in testing allows
     * adding non-standardized SIP headers, another means must be used to set
     * call parameters when testing using a SIP phone. The methods
     * {@link #retrieveCallParametersInRequestUri(Request)} and
     * {@link #retrieveCallParametersInToUri(Request)} are used for this purpose.
     * <p>
     * If call parameters where not found in the Request-URI or To header they
     * are retrieved in the normal standardized and drafted manner using methods
     * {@link #retrieveCalledParty(Request)}, {@link #retrieveCallingParty(Request)},
     * and {@link #retrieveRedirectingParty(Request)}
     *
     * @param   event
     * @return  The call parameters. Null is never returned. If call parameters
     *          could not be retrieved, an empty {@link CallParameters} instance
     *          is returned.
     */
    public CallParameters retrieveCallParameters(
            SipRequestEvent event, CallManagerConfiguration config) {
        CallParameters callParameters = null;
        Request request = null;
        if(event != null){
            request = event.getRequest();
        }

        if (config.getSupportTestInput()) {
            // For testing purposes only, checks for "test=on" parameter
            callParameters = retrieveCallParametersInRequestUri(request);
            if (callParameters == null) {
                callParameters = retrieveCallParametersInToUri(request);
            }
        }

        // If not found at the test location, retrieve the parameters from
        // the "normal" location.
        if (callParameters == null) {
            callParameters = new CallParameters();
            callParameters.setCalledParty(retrieveCalledParty(request));
            callParameters.setCallingParty(retrieveCallingParty(request));
            callParameters.setRedirectingParty(retrieveRedirectingParty(request));
        }
        CallingParty callingParty = callParameters.getCallingParty();
        RedirectingParty redirectingParty = callParameters.getRedirectingParty();
        if(callingParty != null){
            parseGtd(event, callingParty);
        }
        if(redirectingParty != null){
            parseGtdRedirectingParty(event, redirectingParty);
        }
        return callParameters;
    }

    /**
     * Parses the GTD, if one exists in the event, otherwise this method does nothing.
     * If a number completion is found in the GTD it is set in the supplied CallingParty.
     * @param event
     * @param callingParty
     */
    private void parseGtd(SipRequestEvent event, CallingParty callingParty) {
        String gtd = event.getSipMessage().getContent(SipContentSubType.GTD);
        if ((gtd != null) && gtd.length() != 0) {
            GtdDescription gtdDescription;
            try {
                gtdDescription = GtdFactory.parseGtd(gtd);
                if(gtdDescription != null){
                    callingParty.setNumberCompletion(gtdDescription.getCallingPartyCompletion());
                }
            } catch (GtdParseException e) {
                log.warn("Failed to parse Gtd", e);
            }
        }
    }

    private void parseGtdRedirectingParty(SipRequestEvent event, RedirectingParty redirectingParty) {
        String gtd = event.getSipMessage().getContent(SipContentSubType.GTD);
        if ((gtd != null) && gtd.length() != 0) {
            GtdDescription gtdDescription;
            try {
                gtdDescription = GtdFactory.parseGtd(gtd);

                if(gtdDescription != null){
                    //If redirectingReason has already been fetched from the Diversion field of the SIP Invite
                    //message, it is safe to update the RedirectingReason for Redirecting Party at this point.
                    //The RedirectingReason should only be changed/updated if it has the value corresponding
                    //to MOBILE_SUBSCRIBER_NOT_REACHABLE
                    RedirectingParty.RedirectingReason reason =  gtdDescription.getRedirectingPartyRedirectingReason();
                    if (reason == RedirectingParty.RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE) {
                        redirectingParty.setRedirectingReason(reason);
                    }
                }
            } catch (GtdParseException e) {
                log.warn("Failed to parse Gtd", e);
            }
        }
    }

    /**
     * Retrieves the Called party from the Request-URI of the
     * <param>request</param>.
     * @param   request
     * @return  The retrieved called party. Null is returned if the
     *          <param>request</param> is null or if the called party could not
     *          be found.
     */
    private CalledParty retrieveCalledParty(Request request) {
        CalledParty calledParty = null;

        if (request != null) {
            URI uri = request.getRequestURI();

            if (log.isDebugEnabled())
                log.debug("Retrieving Called Party using Request-URI: " + uri);

            if (uri != null) {
                calledParty = createCalledParty(
                        uri.toString(),
                        getSipUserFromUri(uri, request),
                        getNumberFromUri(uri, request));
            }
        }

        return calledParty;
    }

    /**
     * Retrieves the Calling party from the <param>request</param>.
     * The calling party is retrieved in the following manner:
     * <br>
     * <ol type="1">
     * <li>
     * from the P-Asserted-Identity and Privacy header fields, or
     * (if P-Asserted-Identity header field is missing)
     * </li>
     * <li>
     * from the Remote-Party-ID header field, or (if not there)
     * </li>
     * <li>
     * from the From and Privacy header fields
     * </li>
     * </ol>
     * @param   request
     * @return  The retrieved called party. Null is returned if the
     *          <param>request</param> is null or if the calling party could not
     *          be found.
     */
    private CallingParty retrieveCallingParty(Request request) {
        // First try to retrieve it from the P-Asserted-Identity + Privacy headers
        CallingParty callingParty = retrieveCallingPartyUsingAssertedIdAndPrivacy(request);
        if (callingParty != null) {
            callingParty.setPAssertedIdentityCallingParty(callingParty);
        }

        // Second, try to retrieve it from the Remote-Party-ID header
        if (callingParty == null) {
            callingParty = retrieveCallingPartyUsingRemotePartyId(request);
        }

        // Third, try to retrieve it from the From + Privacy headers
        CallingParty fromCallingParty = retrieveCallingPartyUsingFromAndPrivacy(request);
        if (fromCallingParty != null) {
            if (callingParty == null) {
                callingParty = fromCallingParty;
            }
            callingParty.setFromCallingParty(fromCallingParty);
        }

        return callingParty;
    }

    /**
     * Retrieves the Redirecting party in the following order of preference:
     *  1. From the Diversion header if found
     *  2. From the History-Info header if found
     *
     * @param   request
     * @return  The retrieved redirecting party. Null is returned if the
     *          <param>request</param> is null or if the redirecting party
     *          could not be found.
     */
    private RedirectingParty retrieveRedirectingParty(Request request) {

        RedirectingParty redirectingParty = retrieveRedirectingPartyUsingDiversion(request);

        if (redirectingParty == null) {
            redirectingParty = retrieveRedirectingPartyUsingHistoryInfo(request);
        }

        return redirectingParty;
    }


    /**
     * Retrieves the calling party from the P-Asserted-Identity and Privacy
     * headers of the <param>request</param.
     * <p>
     * If multiple P-Asserted-Identity header fields are present, the first
     * instance containing a telephone number is retrieved. If a telephone
     * number cannot be found in any of the header fields instances, the first
     * header field instance is selected as calling party.
     * <p>
     * NOTE: This method uses the NIST SIP stack directly (i.e. not through the
     * JAIN SIP interface). This is not the desired way to implement usage of
     * the SIP stack but was chosen since the IMS header support in the stack is
     * not supported by the interface but only in the stack implementation.
     *
     * @param   request
     * @return  The retrieved calling party. Null is returned if the
     *          <param>request</param> is null or if the calling party
     *          could not be found.
     */
    private CallingParty retrieveCallingPartyUsingAssertedIdAndPrivacy(
            Request request) {

        CallingParty callingParty = null;

        if (request != null) {

            PAssertedIdentityHeader firstIdHeader = null;

            // Loop through the P-Asserted-Identity headers to find the first
            // one containing a telephone number.
            ListIterator idHeaders =
                    request.getHeaders(PAssertedIdentityHeader.NAME);
            while (idHeaders.hasNext()) {
                Object object = idHeaders.next();

                if (object instanceof PAssertedIdentityHeader) {
                    PAssertedIdentityHeader assertedId =
                            (PAssertedIdentityHeader)object;

                    // Save the first P-Asserted-Id header to use if no
                    // telephone number can be found
                    if (firstIdHeader == null)
                        firstIdHeader = assertedId;

                    if (assertedId.getAddress() != null) {
                        URI uri = assertedId.getAddress().getURI();
                        if (uri != null) {
                            String phoneNumber = getNumberFromUri(uri, request);
                            String displayName = assertedId.getAddress().getDisplayName();
                            if (phoneNumber != null) {

                                if (log.isDebugEnabled())
                                    log.debug("Retrieving Calling Party using " +
                                            "P-Asserted-Identity header <" +
                                            assertedId + ">");

                                callingParty = createCallingParty(
                                        uri.toString(),
                                        getSipUserFromUri(uri, request),
                                        phoneNumber,
                                        getPrivacy(request),
                                        displayName);
                                break;
                            }
                        }
                    }
                }
            }

            // If no telephone number was found, use the first
            // P-Asserted-Identity header instance to create the calling party
            if ((callingParty == null) && (firstIdHeader != null)) {
                if (firstIdHeader.getAddress() != null) {
                    URI uri = firstIdHeader.getAddress().getURI();
                    String displayName = firstIdHeader.getAddress().getDisplayName();
                    if (uri != null) {
                        if (log.isDebugEnabled())
                            log.debug("Retrieving Calling Party using " +
                                    "P-Asserted-Identity header <" +
                                    firstIdHeader + ">");

                        callingParty = createCallingParty(
                                uri.toString(),
                                getSipUserFromUri(uri, request),
                                null,
                                getPrivacy(request),
                                displayName);
                    }
                }
            }
        }

        return callingParty;
    }

    /**
     * Retrieves the calling party from the Remote-Party-ID header of
     * the <param>request</param.
     * <p>
     * NOTE: This method uses the NIST SIP stack directly (i.e. not through the
     * JAIN SIP interface). This is not the desired way to implement usage of
     * the SIP stack but was chosen in order to make use of existing code rather
     * than writing similar code again.
     *
     * @param   request
     * @return  The retrieved calling party. Null is returned if the
     *          <param>request</param> is null or if the calling party
     *          could not be found.
     */
    private CallingParty retrieveCallingPartyUsingRemotePartyId(Request request) {
        CallingParty callingParty = null;

        if (request != null) {

            ListIterator remotePartyHeaders = request.getHeaders(REMOTE_PARTY_HEADER);
            if (remotePartyHeaders != null) {
                while (remotePartyHeaders.hasNext()) {
                    ExtensionHeader remotePartyIdExtension =
                            (ExtensionHeader)remotePartyHeaders.next();

                    if (log.isDebugEnabled())
                        log.debug("Retrieving Calling Party using <"
                                + remotePartyIdExtension + ">");

                    if (remotePartyIdExtension != null) {

                        // Parse the Remote-Party-ID header as if it was a From header.
                        // This is done to reuse the parsing implementation and can be done
                        // since the Remote-Party-ID has the same general syntax as From.
                        FromHeader remoteParty =
                                parseExtensionAsFromHeader(remotePartyIdExtension);

                        if ((remoteParty != null) && (remoteParty.getAddress() != null)) {

                            String partyParameter = remoteParty.getParameter(PARTY_PARAMETER);
                            if ((partyParameter == null) ||
                                    partyParameter.equalsIgnoreCase(PARTY_CALLING)) {

                                URI uri = remoteParty.getAddress().getURI();
                                if (uri != null) {

                                    String privacyParameter = remoteParty.getParameter(PRIVACY_PARAMETER);
                                    String displayName = remoteParty.getAddress().getDisplayName();
                                    callingParty = createCallingParty(
                                            uri.toString(),
                                            getSipUserFromUri(uri, request),
                                            getNumberFromUri(uri, request),
                                            getRemotePartyIdPrivacy(privacyParameter),
                                            displayName);
                                    return callingParty;
                                }
                            }
                        }
                    }
                }
            }
        }
        return callingParty;
    }

    /**
     * Retrieves the calling party from the From and Privacy headers of
     * the <param>request</param.
     * <p>
     * NOTE: This method uses the NIST SIP stack directly (i.e. not through the
     * JAIN SIP interface). This is not the desired way to implement usage of
     * the SIP stack but was chosen in order to make use of existing code rather
     * than writing similar code again. The reason for doing this is since the
     * Privacy header field is not supported by the interface but rather the
     * stack implementation.
     *
     * @param   request
     * @return  The retrieved calling party. Null is returned if the
     *          <param>request</param> is null or if the calling party
     *          could not be found.
     */
    private CallingParty retrieveCallingPartyUsingFromAndPrivacy(Request request) {
        CallingParty callingParty = null;

        if (request != null) {
            FromHeader from = (FromHeader) request.getHeader(FromHeader.NAME);

            if (log.isDebugEnabled())
                log.debug("Retrieving Calling Party using <" + from + ">");

            if ((from != null) && (from.getAddress() != null)) {

                URI uri = from.getAddress().getURI();
                String displayName = from.getAddress().getDisplayName();
                if (uri != null) {
                    callingParty = createCallingParty(
                            uri.toString(),
                            getSipUserFromUri(uri, request),
                            getNumberFromUri(uri, request),
                            getPrivacy(request),
                            displayName);
                }
            }
        }
        return callingParty;
    }


    /**
     * Retrieves the redirecting party from the History-Info header of
     * the <param>request</param>.
     * The History-Info header is defined in RFC4244 and its usage in IMS is
     * defined in ETSI TS 183 004 (CDIV)
     *
     * @param   request - The request to retrieve information from.
     * @return  The retrieved redirecting party. Null is returned if the
     *          <param>request</param> is null or if the redirecting party
     *          could not be found.
     */
    public RedirectingParty retrieveRedirectingPartyUsingHistoryInfo(Request request) {

        RedirectingParty redirectingParty = null;

        if (request != null) {

            // Create a list from the Iterator
            ArrayList<HistoryInfoHeader> histInfoList = new ArrayList<HistoryInfoHeader>();
            Iterator it = request.getHeaders(HistoryInfoHeader.NAME);
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof HistoryInfoHeader)
                    histInfoList.add((HistoryInfoHeader)o);
                else
                    log.warn("Not a HistoryInfoHeader, ignoring unexpected object type.");
            }

            int len = histInfoList.size();

            if (len >= 2) {

                HistoryInfoHeader historyInfo2ndLast = histInfoList.get(len-2);
                HistoryInfoHeader historyInfoLast = histInfoList.get(len-1);

                String reason = SipUtils.unescape(
                        historyInfoLast.getReasonHeader());
                String sipCause = getSipCauseFromReason(reason);

                //TR:HO65262 - History Header parsing error
                log.debug("Extracting history info");
                if(sipCause == null) {
                   reason = SipUtils.unescape(historyInfo2ndLast.getReasonHeader());
                   sipCause = getSipCauseFromReason(reason);
                }

              //TR: HN74765 - MIO 1.0_VM : Call forward Unconditional, VM looking for reason in History header but it should not 
                if (sipCause == null) {
                    log.debug("From last histroy header");
                    sipCause = getSipCauseFromUriParams(historyInfoLast);
                    log.debug("Cause = " + sipCause);
                }
                if (sipCause == null) {
                    log.debug("From 2nd last histroy header");
                    sipCause = getSipCauseFromUriParams(historyInfo2ndLast);
                    log.debug("Cause = " + sipCause);
                }
                
                if ( sipCause != null) {

                    if (log.isDebugEnabled()) {
                        log.debug("Retrieving Redirecting Party using History-Info <"
                                + historyInfo2ndLast + ", " + historyInfoLast + ">");
                    }

                    // Retrieve hi-targeted-to-uri without headers
                    URI uri = historyInfo2ndLast.getUriNoHeaders();

                    PresentationIndicator pi = getPiForHistory(historyInfo2ndLast, request);

                    redirectingParty = createRedirectingParty(
                            uri.toString(),
                            getSipUserFromUri(uri, request),
                            getNumberFromUri(uri, request),
                            pi,
                            sipCause);

                } else {
                    log.debug("Could not extract Reason from the last History-Info entry. " +
                            "RedirectingParty could not be extracted");
                }

            } else if (len == 1) {
                log.debug("Only one History-Info header entry found. " +
                        "RedirectingParty could not be extracted");
            } else {
                log.debug("No History-Info header entry found. " +
                        "RedirectingParty could not be extracted");

            }

        }
        return redirectingParty;
    }


    /**
     * Retrieve the presentation indicator (PI) for the Redirecting Party
     * when retrieved from the History-Info header. The following applies:
     *  - The PI is RESTRICTED if the Privacy in the History-Info is restricted OR if the
     *      Request Privacy contains any of: history, header or session
     *  - The PI is UNKNOWN if the Privacy header is neither present in the
     *      HistoryInfo header nor in the request.
     *  - In all other cases the PI is ALLOWED
     *
     * @param historyInfo - The History-Info header to extract History-Info Privacy from.
     * @param request - The Request to extract the "global" Privacy header.
     * @return PresentationIndicator
     */
    protected PresentationIndicator getPiForHistory(HistoryInfoHeader historyInfo, Request request) {


        // Retrieve presentation indicator from Privacy header in Request
        PresentationIndicator piRequest = PresentationIndicator.UNKNOWN;
        if (request != null) {
            ListIterator privIt = request.getHeaders(PrivacyHeader.NAME);
            while (privIt.hasNext()) {
                PrivacyHeader privacyHeader = (PrivacyHeader) privIt.next();
                if (privacyHeader != null) {
                    if (privacyHeader.getPrivacy().equals(PRIVACY_HISTORY) ||
                            privacyHeader.getPrivacy().equals(PRIVACY_HEADER) ||
                            privacyHeader.getPrivacy().equals(PRIVACY_SESSION)) {

                        piRequest = PresentationIndicator.RESTRICTED;
                        break;
                    } else {
                        piRequest = PresentationIndicator.ALLOWED;
                    }
                }
            }
        }


        // Retrieve presentation indicator from Privacy header in
        // the History-Info header
        PresentationIndicator piHistInfo= PresentationIndicator.UNKNOWN;
        if (historyInfo != null) {
            String[] privacyValues = historyInfo.getPrivacyValues();
            if (privacyValues != null) {
                for (String histPrivacy : privacyValues) {
                    if (histPrivacy.equals(PRIVACY_HISTORY)) {
                        piHistInfo = PresentationIndicator.RESTRICTED;
                        break;
                    } else {
                        piHistInfo = PresentationIndicator.ALLOWED;
                    }
                }
            }
        }


        // Set PresentaionIndicator to the most restricted of the
        // (possibly) two sources for Privacy
        PresentationIndicator pi;
        if (piRequest == PresentationIndicator.UNKNOWN &&
                piHistInfo == PresentationIndicator.UNKNOWN)  {
            pi = PresentationIndicator.UNKNOWN;
        } else if (piRequest == PresentationIndicator.RESTRICTED ||
                piHistInfo == PresentationIndicator.RESTRICTED) {
            pi = PresentationIndicator.RESTRICTED;
        } else {
            pi = PresentationIndicator.ALLOWED;
        }

        return pi;

    }


    /**
     * Parse a SIP cause number from a given Reason header string as specified in RFC3326
     *
     * Example:
     *  SIP;cause=302  will return "302"
     *  SIP;xyz=anything;cause=503;text=everything  will return "503"
     *  Q.850;cause=16  will return null
     *
     * @param reason - String to parse
     * @return the cause number if found, null otherwise.
     */
    protected String getSipCauseFromReason(String reason) {
        if (reason == null)
            return null;

        // Must always start with "SIP"
        if (!reason.startsWith("SIP"))
            return null;

        // Find the cause parameter
        int causeStart = reason.indexOf(";cause=",3);
        if (causeStart < 0)
            return null;

        // Position start cursor after "="
        causeStart += 7;

        // Position end cursor at next ";" or at end of string
        int causeEnd = reason.indexOf(";",causeStart);
        if (causeEnd < 0)
            causeEnd = reason.length();

        // Assert that SIP cause is not empty
        if (causeStart >= causeEnd)
            return null;

        // Assert that the SIP cause only contain numbers
        for (int i = causeStart; i<causeEnd; i++) {
            if (!Character.isDigit(reason.charAt(i)))
                return null;
        }

        // Return the found cause value
        return reason.substring(causeStart, causeEnd);

    }
    
    /**
     * Retrieve SIP Cause from URI parameters
     * 
     * eg: <sip:abc@abc.sip.abc.ca;cause=302>
     * 
     * @param historyInfo
     * @return sipCause
     */
    protected String getSipCauseFromUriParams(HistoryInfoHeader historyInfo) {
        log.debug("Extracting cause from URI Params");
    
        if (historyInfo == null)
            return null;
        try {
            Address address = historyInfo.getAddress();
            SipUri uri = (SipUri)address.getURI();
            return uri.getParameter("cause");
        } catch (Exception e) {
            log.debug("Exception in getSipCauseFromUriParams: " + e.getMessage());
            return null;
        }
    }


    /**
     * Retrieves the redirecting party from the Diversion header of
     * the <param>request</param.
     * <p>
     * NOTE: This method uses the NIST SIP stack directly (i.e. not through the
     * JAIN SIP interface). This is not the desired way to implement usage of
     * the SIP stack but was chosen in order to make use of existing code rather
     * than writing similar code again.

     * @param   request
     * @return  The retrieved redirecting party. Null is returned if the
     *          <param>request</param> is null or if the redirecting party
     *          could not be found.
     */
    RedirectingParty retrieveRedirectingPartyUsingDiversion(
            Request request) {

        RedirectingParty redirectingParty = null;

        if (request != null) {

            ExtensionHeader diversionExtension =
                    (ExtensionHeader)request.getHeader(DIVERSION_HEADER);

            if (diversionExtension != null) {

                if (log.isDebugEnabled())
                    log.debug("Retrieving Redirecting Party using Diversion header <" + diversionExtension + ">");

                // Parse the Diversion header as if it was a From header.
                // This is done to reuse the parsing implementation and can be
                // done since the Diversion has the same general syntax as From.
                FromHeader diversion =
                        parseExtensionAsFromHeader(diversionExtension);

                if ((diversion != null) && (diversion.getAddress() != null)) {

                    URI uri = diversion.getAddress().getURI();
                    if (uri != null) {

                        PresentationIndicator pi =
                                retrievePrivacyFromDiversionHeader(
                                        request, diversion);

                        String reason = SipUtils.unescape(
                                diversion.getParameter(REASON_PARAMETER));

                        redirectingParty = createRedirectingParty(
                                uri.toString(),
                                getSipUserFromUri(uri, request),
                                getNumberFromUri(uri, request),
                                pi,
                                reason);
                    }
                }

            } else {
                log.debug("No Diversion header found");
            }
        }
        return redirectingParty;
    }

    /**
     * Retrieves the privacy (or presentation indication) from the Diversion
     * header field of the <param>request</param>.
     * Since the JAIN SIP API does not support the Diversion header and since it
     * basically has the same syntax as a From header, the
     * <param>diversion</param> header is given as a {@link FromHeader}.
     * <p>
     * This method checks the User-Agent header field. If the user agent
     * indicates a Cisco gateway, the presentation indicator is retrieved using
     * {@link #getPrivacyFromDisplayName(Address)}. Otherwise the presentation
     * indicator is retrieved using {@link #getRemotePartyIdPrivacy(String)}
     * on theprivacy parameter retrieved from the Diversion header field.
     *
     * @param   request
     * @param   diversion The Diversion header field in the {@link FromHeader}
     *                    format.
     * @return  The retrieved privacy or {@link PresentationIndicator}.
     *          Null is never returned.
     */
    private PresentationIndicator retrievePrivacyFromDiversionHeader(
            Request request, FromHeader diversion) {

        // First retrieve the user agent header field to check if the user agent
        // is a Cisco gateway
        UserAgentHeader userAgent =
                (UserAgentHeader) request.getHeader(UserAgentHeader.NAME);

        if (log.isDebugEnabled())
            log.debug("Checking user-agent header to detect how redirecting PI " +
                    "should be retrieved <" + userAgent + ">");

        boolean isCiscoGW = false;
        if (userAgent != null) {
            String agent = "";
            ListIterator products = userAgent.getProduct();

            if (products != null) {
                while (products.hasNext()) {
                    agent += products.next();
                    if (products.hasNext())
                        agent += " ";
                }

                if (log.isDebugEnabled())
                    log.debug("User agent is: " + agent);

                if (agent.toLowerCase().contains(UA_CISCO))
                    isCiscoGW = true;
            }
        }

        PresentationIndicator pi;
        if (isCiscoGW) {
            pi = getPrivacyFromDisplayName(diversion.getAddress());
        } else {
            String privacyParameter = diversion.getParameter(PRIVACY_PARAMETER);
            pi = getRemotePartyIdPrivacy(privacyParameter);
        }
        return pi;
    }


    private CalledParty createCalledParty(
            String uri, String sipUser, String number) {

        CalledParty calledParty = new CalledParty();
        calledParty.setUri(uri);
        calledParty.setSipUser(sipUser);

        // Handle 0000 as if not set at all to support Radvision GW
        if ((number != null) && (number.equals("0000")))
            calledParty.setTelephoneNumber(null);
        else
            calledParty.setTelephoneNumber(SipUtils.unescape(number));

        if (log.isDebugEnabled())
            log.debug("Called Party: " + calledParty);

        return calledParty;
    }

    private CallingParty createCallingParty(
            String uri, String sipUser, String number, PresentationIndicator pi, String displayName) {

        CallingParty callingParty = new CallingParty();
        callingParty.setUri(uri);
        callingParty.setSipUser(sipUser);

        // Handle 0000 as if not set at all to support Radvision GW
        if ((number != null) && (number.equals("0000")))
            callingParty.setTelephoneNumber(null);
        else
            callingParty.setTelephoneNumber(SipUtils.unescape(number));

        callingParty.setPresentationIndicator(pi);
        callingParty.setFromDisplayName(displayName);

        if (log.isDebugEnabled())
            log.debug("Calling Party: " + callingParty);

        return callingParty;
    }

    private RedirectingParty createRedirectingParty(
            String uri,
            String sipUser,
            String number,
            PresentationIndicator pi,
            String redirReason) {

        RedirectingParty redirectingParty = new RedirectingParty();
        redirectingParty.setUri(uri);
        redirectingParty.setSipUser(sipUser);

        // Handle 0000 as if not set at all to support Radvision GW
        if ((number != null) && (number.equals("0000")))
            redirectingParty.setTelephoneNumber(null);
        else
            redirectingParty.setTelephoneNumber(SipUtils.unescape(number));

        redirectingParty.setPresentationIndicator(pi);
        redirectingParty.setRedirectingReason(redirReason);

        if (log.isDebugEnabled())
            log.debug("Redirecting Party: " + redirectingParty);

        return redirectingParty;

    }

    private String extractTelephoneNumber(String numberString) {
        String telephoneNumber = null;

        if (numberString != null) {

            if ((numberString.length() > 0) && (numberString.startsWith("+"))) {
                telephoneNumber = extractGlobalNumber(numberString.substring(1));
            } else {
                telephoneNumber = extractLocalNumber(numberString);
            }
        }

        return telephoneNumber;
    }

    /**
     * Extracts a global number
     * (according to the TEL URI specification in RFC 3966) from the
     * <param>numberString</param>.
     * The following characters are valid in a global number:
     * 0-9
     * <br>
     * Visual separators [-.()] are also allowed and are removed from the
     * number string before returned.
     * <p>
     * If the <param>numberString</param> contains a valid global number, the
     * visual separators are stripped.
     * @param numberString      A telephone number in string format.
     * @return                  A global telephone number or null if a the number
     *                          string is not a valid global number.
     */
    private String extractGlobalNumber(String numberString) {
        String globalNumber = null;

        if (numberString != null) {
            String tmpString = removeVisualSeparators(numberString);

            if ((tmpString.matches("\\d*"))) {
                globalNumber = tmpString;
            }
        }

        return globalNumber;
    }

    /**
     * Extracts a local number
     * (according to the TEL URI specification in RFC 3966) from the
     * <param>numberString</param>.
     * The following characters are valid in a local number:
     * a-f, A-F, 0-9, * and #
     * <br>
     * Visual separators [-.()] are also allowed and are removed from the
     * number string before returned.
     * <p>
     * If the <param>numberString</param> contains a valid local number, the
     * visual separators are stripped.
     * @param numberString      A telephone number in string format.
     * @return                  A local telephone number or null if a the number
     *                          string is not a valid local number.
     */
    private String extractLocalNumber(String numberString) {
        String localNumber = null;

        if (numberString != null) {
            String tmpString = removeVisualSeparators(numberString);

            if ((tmpString.matches("[a-fA-F0-9*#]*"))) {
                localNumber = tmpString;
            }
        }

        return localNumber;
    }

    /**
     * Returns the SIP user name from an URI.
     * @param uri MUST NOT be null.
     * @return The SIP user name or null if not found.
     */
    private String getSipUserFromUri(URI uri, Request request) {
        String sipUser = null;
        if (uri.isSipURI()) {
            SipURI sipUri = (SipURI)uri;
            sipUser = getUser(sipUri.getUser());

            String phoneNumber = getNumberFromUri(uri, request);
            if (phoneNumber != null)
                sipUser = phoneNumber;
        }
        return sipUser;
    }

    /**
     * Removes any user parameters from the user information of a SIP URI.
     * @param userInfo
     * @return The user name without any user parameters.
     */
    private String getUser(String userInfo) {
        String user = null;
        if (userInfo != null)
            user = userInfo.split(";")[0];

        return user;
    }


    /**
     * Returns the telephone number from an URI in a SIP request.
     * @param uri MUST NOT be null.
     * @param request MUST NOT be null
     * @return The telephone number or null if not found.
     */
    private String getNumberFromUri(URI uri, Request request) {
        String number = null;
        if (uri.isSipURI()) {
            SipURI sipUri = (SipURI)uri;
            number = retrievePhoneNumberFromSipUri(sipUri, request);
        } else if (uri.getScheme().equals("tel")) {
            TelURL telUrl = (TelURL)uri;
            number = removeVisualSeparators(telUrl.getPhoneNumber());
        }
        return number;
    }

    /**
     * Returns the privacy status indicated in the display name of an address.
     * The privacy status depends upon the display name like this:
     * <ul>
     * <li>"anonymous" => {@link PresentationIndicator.RESTRICTED}</li>
     * <li>"unknown" => {@link PresentationIndicator.UNKNOWN}</li>
     * <li>All others => {@link PresentationIndicator.ALLOWED}</li>
     * </ul>
     * {@link PresentationIndicator.UNKNOWN} is returned if <param>address</param>
     * is null.
     * @param address
     * @return True if the display name is "anonymous", false otherwise.
     */
    private PresentationIndicator getPrivacyFromDisplayName(Address address) {

        PresentationIndicator privacy = PresentationIndicator.UNKNOWN;

        if (address != null) {
            privacy = PresentationIndicator.ALLOWED;

            String displayName = address.getDisplayName();
            if (displayName != null) {
                if (displayName.equalsIgnoreCase(ANONYMOUS)) {
                    privacy = PresentationIndicator.RESTRICTED;
                } else if (displayName.equalsIgnoreCase(UNKNOWN)) {
                    privacy = PresentationIndicator.UNKNOWN;
                }
            }
        }

        return privacy;
    }

    /**
     * Checks a string representing a presentation indicator and returns the
     * corresponding presentation indicator. The format of the privacy string
     * is given as for the Remote-Party-ID header with the exception that the
     * value "on" results in allowed presentation as well.
     * <p>
     * The <param>privacyString</param> is handled like this:
     * <ul>
     * <li>"off" => {@link PresentationIndicator.ALLOWED}</li>
     * <li>other non-null values => {@link PresentationIndicator.RESTRICTED}</li>
     * <li>null => {@link PresentationIndicator.UNKNOWN}</li>
     * </ul>
     *
     * @param   privacyString
     * @return The {@link PresentationIndicator} as described above.
     */
    private PresentationIndicator getRemotePartyIdPrivacy(String privacyString) {
        PresentationIndicator pi = PresentationIndicator.UNKNOWN;

        if (privacyString != null) {
            String piLower = privacyString.toLowerCase();
            if (piLower.equals(PRIVACY_OFF)) {
                pi = PresentationIndicator.ALLOWED;
            } else {
                pi = PresentationIndicator.RESTRICTED;
            }
        }
        return pi;
    }

    /**
     * Checks a privacy header representing a presentation indicator and returns
     * the corresponding presentation indicator.
     * <p>
     * The <param>privacyHeader</param> is handled like this:
     * <ul>
     * <li>equals parameter "none" => {@link PresentationIndicator.ALLOWED}</li>
     * <li>equals parameter "history" => {@link PresentationIndicator.ALLOWED}</li>
     * <li>otherwise if not null => {@link PresentationIndicator.RESTRICTED}</li>
     * <li>null => {@link PresentationIndicator.UNKNOWN}</li>
     * </ul>
     *
     * @param  request to retrieve Privacy header from.
     * @return The {@link PresentationIndicator} as described above.
     */
    protected PresentationIndicator getPrivacy(Request request) {

        PresentationIndicator pi = PresentationIndicator.UNKNOWN;

        if (request != null) {
            StringBuffer privacy = new StringBuffer("");
            Iterator privacyHeaders = request.getHeaders(PrivacyHeader.NAME);
            boolean firstTime = true;
            while (privacyHeaders.hasNext()) {
                Object o = privacyHeaders.next();
                if (o instanceof PrivacyHeader) {
                    if (firstTime)  {
                        firstTime = false;
                    } else {
                        privacy.append(";");
                    }
                    privacy.append(((PrivacyHeader)o).getPrivacy());
                }
            }

            if (privacy.length() > 0) {
                if (privacy.toString().equals(PRIVACY_NONE) ||
                        privacy.toString().equals(PRIVACY_HISTORY)) {
                    pi = PresentationIndicator.ALLOWED;
                } else {
                    pi = PresentationIndicator.RESTRICTED;
                }
            }

            log.debug("Retrieving Privacy from <" + privacy + "> Privacy=" + pi);

        } else {
            log.warn("Request must not be null! Ignoring.");
            return null;
        }

        return pi;
    }

    /**
     * Parses an extension header (i.e. a not standardized header) as if it was
     * a From header. This method MUST only be used if the extension header has
     * the same general syntax as the From header.
     * <p>
     * NOTE: This method uses the NIST SIP stack directly (i.e. not through the
     * JAIN SIP interface). This is not the desired way to implement usage of
     * the SIP stack but was chosen in order to make use of existing code rather
     * than writing similar code again.
     * @param   extension The extension header to parse.
     * @return  a From header if the extension could be parsed, null otherwise.
     */
    private FromHeader parseExtensionAsFromHeader(ExtensionHeader extension) {
        FromHeader fromHeader = null;
        if (extension != null) {
            String hdrstring ;
            if (extension.getName().equalsIgnoreCase(DIVERSION_HEADER)) {
               String diversion[] = extension.getValue().split(",");
               hdrstring = "From: " + diversion[0];
               log.debug("Diversion header of interest : " +  diversion[0]);
            }   else {
               hdrstring = "From: " + extension.getValue();
            }
            try {
                HeaderParser hdrParser =
                        ParserFactory.createParser(hdrstring + "\n");
                fromHeader = (FromHeader)hdrParser.parse();
            } catch (ParseException ex) {
                if (log.isDebugEnabled())
                    log.debug("Could not parse the extension header <" +
                            extension.toString() + ">");
            }
        }
        return fromHeader;
    }

    /**
     * Retrieves a phone number from a SIP URI.
     * The phone number is the user part of the SIP URI if the URI parameter
     * user=phone is set.
     * The phone number is the user part of the SIP URI if the URI parameter
     * user=phone is not set, but the User-Agent header indicates a special client.
     * @param sipUri
     * @param request Is needed to determine the User-Agent header.
     * @return The phone number retrieved from the SIP URI.
     */
    private String retrievePhoneNumberFromSipUri(SipURI sipUri, Request request) {
        String phoneNumber = null;

        String userParam = sipUri.getUserParam();
        if ((userParam != null) && (userParam.equals("phone"))) {
            if (sipUri.getUser() != null) {
                phoneNumber = extractTelephoneNumber(getUser(sipUri.getUser()));
            }

        } else {
            phoneNumber = retrievePhoneNumberFromSipUriForSpecialClient(
                    sipUri, request);
        }

        return phoneNumber;
    }

    /**
     * Retrieves a phone number from a SIP URI for a special client.
     * The phone number is the user part of the SIP URI if the URI parameter
     * user=phone is not set, but the User-Agent header indicates a special client.
     * @param sipUri
     * @param request Is needed to determine the User-Agent header.
     * @return The phone number retrieved from the SIP URI.
     */
    private String retrievePhoneNumberFromSipUriForSpecialClient(
            SipURI sipUri, Request request)
    {
        String phoneNumber = null;
        boolean specialHandling = false;

        UserAgentHeader userAgent =
                (UserAgentHeader) request.getHeader(UserAgentHeader.NAME);

        if (log.isDebugEnabled())
            log.debug("Checking user agent header to find out how to " +
                    "retrieve phone numbers in URIs: " + userAgent);
        if( allUserAgentWithPhoneInUriButNoUserParameter) {
            specialHandling = true;
        } else if( userAgent == null && emptyUserAgentWithPhoneInUriButNoUserParameter ) {
            specialHandling = true;
        } else if (userAgent != null) {
            String agent = "";
            ListIterator products = userAgent.getProduct();

            if (products != null) {
                while (products.hasNext()) {
                    agent = agent + " " + products.next();
                }

                if (log.isDebugEnabled())
                    log.debug("User agent is: " + agent);

                for (String ua : userAgentWithPhoneInUriButNoUserParameter) {
                    if (agent.toLowerCase().contains(ua)) {
                        specialHandling = true;
                        break;
                    }
                }
            }
        }
        if( specialHandling ) {
            if (log.isDebugEnabled())
                log.debug("This user agent deserves special treatment.");
            phoneNumber = extractTelephoneNumber(sipUri.getUser());
        }

        return phoneNumber;
    }

    /**
     * Removes visual separators from the phone number and returns the
     * stripped version.
     * The visual separators removed are "-", ".", "(" and ")".
     * @param phoneNumber   The original phone number.
     * @return              Returns the original phone number minus any visual
     *                      separators.
     */
    private String removeVisualSeparators(String phoneNumber) {
        return phoneNumber.replaceAll("[-.()]", "");
    }

    /**
     * Tries to retrieve the call parameters from the Request-URI.
     * This is not normally where call parameters are located, but this
     * support has been implemented to make it easier for soft-phones to set
     * call parameters.
     * @param request
     * @return The call parameters if it could be retrieved from the Request-URI
     * or null otherwise.
     */
    private CallParameters retrieveCallParametersInRequestUri(Request request) {
        CallParameters callParameters = null;
        if (request != null) {
            URI uri = request.getRequestURI();
            if (log.isDebugEnabled())
                log.debug("Trying to retrieve call parameters using Request-URI <" +
                        uri + ">");
            callParameters = getCallParametersFromUri(uri);
        }
        return callParameters;
    }

    /**
     * Tries to retrieve the call parameters from the To URI.
     * This is not normally where call parameters are located, but this
     * support has been implemented to make it easier for soft-phones to set
     * call parameters.
     * @param request
     * @return The call parameters if it could be retrieved from the To URI
     * or null otherwise.
     */
    private CallParameters retrieveCallParametersInToUri(Request request) {
        CallParameters callParameters = null;
        if (request != null) {
            ToHeader to = (ToHeader) request.getHeader(ToHeader.NAME);
            if (log.isDebugEnabled())
                log.debug("Trying to retrieve Called party settings using <" + to + ">");

            if ((to != null) && (to.getAddress() != null)) {
                URI uri = to.getAddress().getURI();
                callParameters = getCallParametersFromUri(uri);
            }
        }
        return callParameters;
    }

    /**
     * Retrieves call parameters contained in the given URI.
     * This is not normally where calling party parameters are located, but this
     * support has been implemented to make it easier for soft-phones to set
     * call parameters.
     * @param uri
     * @return The call parameters.
     */
    private CallParameters getCallParametersFromUri(URI uri) {
        CallParameters callParameters = null;
        if (uri != null) {
            if (uri.isSipURI()) {
                SipURI sipUri = (SipURI)uri;
                String testParam = sipUri.getParameter("test");
                if ((testParam != null) && (testParam.toLowerCase().equals("on"))) {
                    callParameters = new CallParameters();
                    // Retrieve called party
                    callParameters.setCalledParty(createCalledParty(
                            uri.toString(), null, sipUri.getParameter("called")));
                    // Retrieve calling party
                    callParameters.setCallingParty(createCallingParty(
                            uri.toString(), null, sipUri.getParameter(PARTY_CALLING),
                            getRemotePartyIdPrivacy(sipUri.getParameter("calling_privacy")), null));
                    // Retrieve redirecting party
                    callParameters.setRedirectingParty(createRedirectingParty(
                            uri.toString(), null, sipUri.getParameter("redir"),
                            getRemotePartyIdPrivacy(sipUri.getParameter("redir_privacy")),
                            SipUtils.unescape(sipUri.getParameter("redir_cause"))));
                }
            }
        }
        return callParameters;
    }
}
