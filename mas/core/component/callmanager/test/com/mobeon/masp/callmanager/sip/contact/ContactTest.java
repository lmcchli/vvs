/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.contact;

import junit.framework.TestCase;

import javax.sip.address.SipURI;
import javax.sip.InvalidArgumentException;
import java.util.TreeSet;
import java.util.Iterator;
import java.text.ParseException;

/**
 * Contact Tester.
 *
 * @author Malin Flodin
 */
public class ContactTest extends TestCase
{

    private static final Float ONE = new Float(1.0);
    private static final Float TWO = new Float(2.0);
    private static final SipURI ONE_URI = new MyURI("one");
    private static final SipURI TWO_URI = new MyURI("two");
    private static final SipURI THREE_URI = new MyURI("three");

    ContactComparator comparator = new ContactComparator();
    TreeSet<Contact> treeSet = new TreeSet<Contact>(comparator);

    protected void setUp() throws Exception {
        super.setUp();
        treeSet.add(new Contact(TWO, ONE_URI));
        treeSet.add(new Contact(ONE, TWO_URI));
        treeSet.add(new Contact(ONE, THREE_URI));
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        treeSet.clear();
    }

    /**
     * This test case is used to insert various Contacts into a tree set and
     * then retrive them in the correct order sorted after decreasing Q value.
     * @throws Exception if the test case fails.
     */
    public void testContactInSet() throws Exception {
        // Get one entry at a time
        Contact contact = treeSet.first();
        assertEquals(TWO, contact.getQ());
        assertEquals(ONE_URI, contact.getSipUri());
        treeSet.remove(contact);

        contact = treeSet.first();
        assertEquals(ONE, contact.getQ());
        assertEquals(TWO_URI, contact.getSipUri());
        treeSet.remove(contact);

        contact = treeSet.first();
        assertEquals(ONE, contact.getQ());
        assertEquals(THREE_URI, contact.getSipUri());
        treeSet.remove(contact);
    }

    /**
     * Verifies that the equals methods returns true if the two contacts are
     * the same object or if the uri of the objects are equal.
     * @throws Exception if test case fails.
     */
    public void testEquals() throws Exception {
        Contact contact1 = new Contact(new Float(1.0), new MyURI("uri1"));
        Contact contact2;

        // Verify for two contacts that differ in uri
        contact2 = new Contact(new Float(1.0), new MyURI("uri2"));
        assertFalse(contact1.equals(contact2));

        // Verify for two contacts that are equivalent
        contact2 = new Contact(new Float(1.0), new MyURI("uri1"));
        assertEquals(contact1, contact2);

        // Verify for the same object
        assertEquals(contact1, contact1);

        // Verify for objects of different types
        assertFalse(contact1.equals(false));

        // Verify for null
        assertFalse(contact1.equals(null));
    }

    /**
     * Verifies that the hash code is equal for two equal contacts.
     * @throws Exception if test case fails.
     */
    public void testHashCode() throws Exception {
        Contact contact1 = new Contact(new Float(1.0), new MyURI("uri1"));
        int hashCode1 = contact1.hashCode();

        Contact contact2;
        int hashCode2;

        // Verify for two contacts that differ in uri
        contact2 = new Contact(new Float(1.0), new MyURI("uri2"));
        hashCode2 = contact2.hashCode();
        assertFalse(hashCode1 == hashCode2);

        // Verify for two contacts that are equivalent
        contact2 = new Contact(new Float(1.0), new MyURI("uri1"));
        hashCode2 = contact2.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    /**
     * Verifies that toString produces the expected output containing both
     * Q-value and URI.
     * @throws Exception if test case fails.
     */
    public void testToString() throws Exception {
        Contact contact1 = new Contact(new Float(1.0), new MyURI("uri1"));
        assertEquals("ContactEntry: <Q=1.0>, <URI=uri1>", contact1.toString());
    }

    /**
     * Verifies that the contact comparator throws ClassCastException if
     * forced to compare objects that are not of class Contact.
     * @throws Exception if test case fails.
     */
    public void testContactComparator() throws Exception {
        TreeSet treeSet = new TreeSet(comparator);
        treeSet.add(new Contact(TWO, ONE_URI));

        try {
            treeSet.add(false);
            fail("Exception not thrown when expected.");
        } catch (ClassCastException e) {
        }

    }

    /* ========================= Private stuff ========================== */

    private static class MyURI implements SipURI {

        private String uri;

        public MyURI(String uri) {
            this.uri = uri;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final MyURI minURI = (MyURI) o;
            return uri.equals(minURI.uri);
        }

        public int hashCode() {
            return uri.hashCode();
        }

        public String getScheme() {
            return "SIP";
        }

        public Object clone() {
            return null;
        }

        public boolean isSipURI() {
            return true;
        }

        public void setUser(String string) throws ParseException {
        }

        public String getUser() {
            return null;
        }

        public void setUserPassword(String string) throws ParseException {
        }

        public String getUserPassword() {
            return null;
        }

        public boolean isSecure() {
            return false;
        }

        public void setSecure(boolean b) {
        }

        public void setHost(String string) throws ParseException {
        }

        public String getHost() {
            return null;
        }

        public void setPort(int i) {
        }

        public int getPort() {
            return 0;
        }

        public void removePort() {
        }

        public String getHeader(String string) {
            return null;
        }

        public void setHeader(String string, String string1) throws ParseException {
        }

        public Iterator getHeaderNames() {
            return null;
        }

        public String getTransportParam() {
            return null;
        }

        public void setTransportParam(String string) throws ParseException {
        }

        public int getTTLParam() {
            return 0;
        }

        public void setTTLParam(int i) throws InvalidArgumentException {
        }

        public String getMethodParam() {
            return null;
        }

        public void setMethodParam(String string) throws ParseException {
        }

        public void setUserParam(String string) throws ParseException {
        }

        public String getUserParam() {
            return null;
        }

        public String getMAddrParam() {
            return null;
        }

        public void setMAddrParam(String string) throws ParseException {
        }

        public boolean hasLrParam() {
            return false;
        }

        public void setLrParam() {
        }

        public String toString() {
            return uri;
        }

        public String getParameter(String string) {
            return null;
        }

        public void setParameter(String string, String string1) throws ParseException {
        }

        public Iterator getParameterNames() {
            return null;
        }

        public void removeParameter(String string) {
        }
    }

}
