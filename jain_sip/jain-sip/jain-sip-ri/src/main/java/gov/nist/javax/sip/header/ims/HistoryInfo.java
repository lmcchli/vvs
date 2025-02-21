package gov.nist.javax.sip.header.ims;

import gov.nist.javax.sip.header.AddressParametersHeader;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;

import javax.sip.address.URI;
import javax.sip.address.SipURI;
import javax.sip.header.ReasonHeader;
import java.text.ParseException;

/**
 * History-Info header according to RFC 4244.
 * Multiple headers are allowed.
 * Each header contains an URI with optional headers embedded:
 *  - A Privacy header can indicate the privacy of the header. A value of "history" means that
 * privacy is restricted. See RFC 3323.
 *  - A Reason header can indicate the reason of redirection according to RFC 3326.
 * Each header should also have the index parameter set.
 *
 * @author Mats Hägg
 */
public final class HistoryInfo
        extends AddressParametersHeader
        implements HistoryInfoHeader {

    public static final String INDEX = "index";

    public HistoryInfo() {
        super(NAME);
    }


    /**
     * Encode body of the header into a canonical String.
     *
     * @return string encoding of the header value.
     */
    protected String encodeBody() {
        StringBuffer encoding = new StringBuffer();

        if (address.getAddressType() == AddressImpl.NAME_ADDR) {
            encoding.append(address.encode());
        } else {
            // Encoding in canonical form must have <> around address.
            encoding.append("<").append(address.encode()).append(">");
        }
        if (!parameters.isEmpty()) {
            encoding.append(SEMICOLON).append(parameters.encode());
        }

        return encoding.toString();
    }


    /**
     * Retrieve the mandatory index parameter from the History-Info header.
     * The method will return null if no such parameter exist.
     *
     * @return String representing the index parameter if found, null otherwise.
     */
    public String getIndex() {
        return getParameter(HistoryInfo.INDEX);
    }


    /**
     * Extract the URI in the History-Info header. Any embedded
     *  headers will be removed from the URI returned.
     * @return the hi-targeted-to-uri without headers if found, null otherwise
     */
    public URI getUriNoHeaders() {
        if (address == null)
            return null;

        URI uri = address.getURI();
        if (uri instanceof SipUri) {
            // Clone URI and remove headers from cloned URI
            // (Only SipURI can contain headers)
            SipUri uriNoHeaders = (SipUri)uri.clone();
            uriNoHeaders.clearQheaders();
            return uriNoHeaders;
        } else {
            return uri;
        }

    }


    /**
     * Retrieve the optional Reason header embedded in the SipURI in this History-Info header.
     * The method will return null if no such header exist or if the URI
     * has any other scheme than SIP or SIPS.
     * No unescaping will be performed on the header value.
     *
     * @return a String containing the value of the Reason header if it exist,
     * null otherwise.
     */
    public String getReasonHeader() {
        if (address.getURI() instanceof SipURI) {
            return ((SipURI)address.getURI()).getHeader(ReasonHeader.NAME);
        } else {
            return null;
        }
    }

    /**
     * Set the optional Reason header embedded in the SipURI in this History-Info header.
     * It is only possible to set the Reason header if the URI has a SIP or SIPS scheme.
     * The provided reason string must be properly escaped according to RFC3261.
     *
     * @param reason - String containing the value of the Reason header.
     * @throws ParseException if Reason header could not be set.
     */
    public void setReasonHeader(String reason) throws ParseException {
        if (address.getURI() instanceof SipURI) {
            ((SipURI)address.getURI()).setHeader(ReasonHeader.NAME, reason);
        } else {
            throw new ParseException("Only SIP/SIPS URI schemes support headers",0);
        }
    }



    /**
     * Retrieve the optional Privacy header embedded in the SipURI in this History-Info header.
     * The method will return null if no such header exist or if the URI
     * has any other scheme than SIP or SIPS.
     * If found the  Privacy header will be separated into privacy values (separated by ";"
     * according to RFC3323) and returned in an array of Strings.
     * No unescaping will be performed on the values.
     *
     * @return a String[] containing the list of values found in the Privacy header
     * if it exists or null otherwise.
     */
    public String[] getPrivacyValues() {
        if (address.getURI() instanceof SipURI) {
            String privacy = ((SipURI)address.getURI()).getHeader(PrivacyHeader.NAME);
            if (privacy != null)
                return privacy.split("%3[bB]");
        }
        return null;
    }


    /**
     * Set the optional Privacy header embedded in the SipURI in this History-Info header.
     * It is only possible to set the header if the URI has a SIP or SIPS scheme.
     * The provided string must be properly escaped according to RFC3261.
     *
     * @param privacy - String containing the value of the Privacy header.
     * @throws ParseException if Reason header could not be set.
     */
    public void setPrivacyHeader(String privacy) throws ParseException {
        if (address.getURI() instanceof SipURI) {
            ((SipURI)address.getURI()).setHeader(PrivacyHeader.NAME, privacy);
        } else {
            throw new ParseException("Only SIP/SIPS URI schemes support headers",0);
        }
    }


}
