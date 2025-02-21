/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import java.util.HashMap;

/**
 * Defines certain call party properties and corresponding value ranges.
 * Also contains call properties common to all types of call parties
 * (calling/called/redirecting).
 *
 * This class is thread safe.
 *
 * @author Malin Flodin
 */
public class CallPartyDefinitions {

    private static final HashMap<PresentationIndicator, String> PI_NAMES;

    public static enum PresentationIndicator {
        /** Unknown if call party may be presented. */
        UNKNOWN,
        /** Call party presentation is allowed. */
        ALLOWED,
        /** Call party presentation is restricted. */
        RESTRICTED
    }

    private String telephoneNumber;
    private String sipUser;
    private String uri;

    static {
        PI_NAMES = new HashMap<PresentationIndicator, String>();
        PI_NAMES.put(PresentationIndicator.UNKNOWN, "unknown");
        PI_NAMES.put(PresentationIndicator.ALLOWED, "allowed");
        PI_NAMES.put(PresentationIndicator.RESTRICTED, "restricted");
    }


    /**
     * Returns the telephone number.
     * <p>
     * The telephone number is presented according to the format specified
     * in E.164.
     * @return the telephone number or null if not set.
     */
    public synchronized String getTelephoneNumber() {
        return telephoneNumber;
    }

    /**
     * Sets the telephone number.
     * @param telephoneNumber The telephone number according to the format
     * specified in E.164.
     */
    public synchronized void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    /**
     * Returns the user part (see RFC 3261) of a SIP or SIPS URI
     * containing the SIP user name only.
     * @return the SIP user or null if not set.
     */
    public synchronized String getSipUser() {
        return sipUser;
    }

    /**
     * Sets the SIP user name. The SIP user name shall follow the format of a
     * user part (see RFC 3261) of a SIP or SIPS URI.
     * @param sipUser
     */
    public synchronized void setSipUser(String sipUser) {
        this.sipUser = sipUser;
    }

    /**
     * Returns the URI. The format of an URI is specified in RFC 2396.
     * @return the URI or null of not set.
     */
    public synchronized String getUri() {
        return uri;
    }

    /**
     * Sets the URI. The URI should be specified according to the format
     * defined in RFC 2396.
     * @param uri
     */
    public synchronized void setUri(String uri) {
        this.uri = uri;
    }

    public static String presentationIndicatorToString(PresentationIndicator pi) {
        String piString = null;
        if (pi != null)
            piString = PI_NAMES.get(pi);
        return piString;
    }

    public String toString() {
        return "<Uri = " + getUri() + ">, <SipUser = " + getSipUser() +
                ">, <TelephoneNumber = " + getTelephoneNumber() + ">";
    }
}
