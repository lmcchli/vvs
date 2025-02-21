/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.contact;

import javax.sip.address.SipURI;

/**
 * This class represents a SIP Contact. It consists of a Q value and a SIP URI.
 * <p>
 * The methods {@link #equals(Object)} and {@link #hashCode()} have been
 * overridden. Two contacts are equal if the SIP URIs are equal.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class Contact {
    private Float q;
    private SipURI uri;

    public Contact(Float q, SipURI contact) {
        this.q = q;
        this.uri = contact;
    }

    /**
     * Returns the Q value of the Contact. Contacts with higher Q value should
     * be used before contacts with lower value.
     * @return The Q value to use for the Contact.
     */
    public Float getQ() {
        return q;
    }

    /**
     * Returns the Contact SIP URI.
     * @return The Contact SIP URI.
     */
    public SipURI getSipUri() {
        return uri;
    }


    /**
     * Returns whether two Contacts are equal. Two contacts are equal if their
     * URIs are equal.
     * @param o
     * @return True if the URI of the two contacts are equal. False otherwise.
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Contact contact = (Contact) o;

        return uri.equals(contact.uri);
    }

    /**
     * The hash code for the Contact is the same as the hash code for the URI.
     * @return The hash code for the URI.
     */
    public int hashCode() {
        return uri.hashCode();
    }

    public String toString() {
        return "ContactEntry: <Q=" + q + ">, <URI=" + uri + ">";
    }
}
