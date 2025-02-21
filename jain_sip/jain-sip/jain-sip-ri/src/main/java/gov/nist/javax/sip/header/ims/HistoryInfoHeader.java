package gov.nist.javax.sip.header.ims;

import javax.sip.address.URI;
import javax.sip.header.HeaderAddress;
import javax.sip.header.Parameters;
import javax.sip.header.Header;
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
public interface HistoryInfoHeader extends HeaderAddress, Parameters, Header {

    public final static String NAME = "History-Info";

    /**
     * Retrieve the mandatory index parameter from the History-Info header.
     * The method will return null if no such parameter exist.
     *
     * @return String representing the index parameter if found, null otherwise.
     */
    public String getIndex();

    /**
     * Retrieve the optional Reason header embedded in the SipURI in this History-Info header.
     * The method will return null if no such header exist or if the URI
     * has any other scheme than SIP or SIPS.
     * No unescaping will be performed on the header value.
     *
     * @return a String containing the value of the Reason header if it exist,
     * null otherwise.
     */
    public String getReasonHeader();

    /**
     * Set the optional Reason header embedded in the SipURI in this History-Info header.
     * It is only possible to set the Reason header if the URI has a SIP or SIPS scheme.
     * The provided reason string must be properly escaped according to RFC3261.
     *
     * @param reason - String containing the value of the Reason header.
     * @throws ParseException if Reason header could not be set.
     */
    public void setReasonHeader(String reason) throws ParseException;

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
    public String[] getPrivacyValues();

    /**
     * Set the optional Privacy header embedded in the SipURI in this History-Info header.
     * It is only possible to set the header if the URI has a SIP or SIPS scheme.
     * The provided string must be properly escaped according to RFC3261.
     *
     * @param privacy - String containing the value of the Privacy header.
     * @throws ParseException if Reason header could not be set.
     */
    public void setPrivacyHeader(String privacy) throws ParseException;

    /**
     * Extract the URI in the History-Info header. Any embedded
     *  headers will be removed from the URI returned.
     * @return the hi-targeted-to-uri without headers if found, null otherwise
     */
    public URI getUriNoHeaders();

}
