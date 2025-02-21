package com.mobeon.masp.callmanager.sip.header;

import gov.nist.javax.sip.header.SIPHeader;

import java.text.ParseException;

import javax.sip.header.ExtensionHeader;

/**  
 * PEarlyMedia SIP Header.
 */
public class PEarlyMedia extends SIPHeader implements PEarlyMediaHeader, ExtensionHeader {

    protected String value;

    /** PEarlyMediaTypes */
    public enum PEarlyMediaTypes {

        PEARLY_MEDIA_SENDRECV  ("sendrecv"),
        PEARLY_MEDIA_SENDONLY  ("sendonly"),
        PEARLY_MEDIA_RECVONLY  ("recvonly"),
        PEARLY_MEDIA_INACTIVE  ("inactive"),
        PEARLY_MEDIA_GATED     ("gated"),
        PEARLY_MEDIA_SUPPORTED ("supported");

        private String emParam;

        PEarlyMediaTypes(String emParam) {
            this.emParam = emParam;
        }

        public String getValue() {
            return emParam;
        }
    }

    /**
     * Default constructor
     */
    public PEarlyMedia() {
        super(PEarlyMediaHeader.NAME);
        this.value = null;
    }

    /**
     * Constructor
     * @param PEarlyMediaType to set
     */
    public PEarlyMedia(PEarlyMediaTypes pEarlyMediaType) {
        super(PEarlyMediaHeader.NAME);
        this.value = pEarlyMediaType.getValue();
    }

    /**
     * Return canonical form of the header.
     * @return encoded header.
     */
    public String encode() {
        String retval = headerName + COLON;
        if (this.value != null)
            retval += SP + this.value;
        retval += NEWLINE;
        return retval;
    }

    /**
     * Just the encoded body of the header.
     * @return the string encoded header body.
     */
    public String encodeBody() {
        return this.value != null ? this.value : "";
    }

    /**
     * Sets the valueemParam to the new supplied <var>PEarlyMediaTypes</var> parameter.
     * @param String - the new value
     * @throws ParseException which signals that an error has been reached unexpectedly while parsing the value.
     */
    public void setValue(String value) throws ParseException {
        if (value == null) {
            throw new NullPointerException("Exception, PEarlyMedia, " + "setEmParam(), the emParam parameter is null");
        }
        this.value = value;
    }

    /**
     * Gets the value.
     * @return String value
     */
    public String getValue() {
        return this.value;
    }
}
