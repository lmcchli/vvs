/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.provisionmanager.cai;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Map;

/**
 * Tests the CAIResponse class
 *
 * @author ermmaha
 */
public class CAIResponseTest extends TestCase {
    public CAIResponseTest(String string) {
        super(string);
    }

    /**
     * @throws Exception
     */
    public void testResponse() throws Exception {
        String tmp = "RESP:0:Successful:BADLOGINCOUNT,0:cn,161011;\\r\\nCAI>";

        CAIResponse r = new CAIResponse(tmp);

        assertEquals(0, r.getCode());
        assertEquals("Successful", r.getMessage());

        Map<String, String> attributes = r.getAttributes();
        assertEquals("0", attributes.get("BADLOGINCOUNT"));
        assertEquals("161011", attributes.get("cn"));

        tmp = "RESP:5002:Invalid credentials;";

        r = new CAIResponse(tmp);

        assertEquals(5002, r.getCode());
        assertEquals("Invalid credentials", r.getMessage());

        assertNull(r.getAttributes());
        
         //commas in the attribute value
        tmp = "RESP:0:Successful:creatorsname,u1,ou=C1,o=mobeon.com;\\r\\nCAI>";

        r = new CAIResponse(tmp);
        attributes = r.getAttributes();
        assertEquals("u1,ou=C1,o=mobeon.com", attributes.get("creatorsname"));
    }

    /**
     * Do some tests with syntax errors on the response string
     *
     * @throws Exception
     */
    public void testFailedResponse() throws Exception {
        //XRESP instead of RESP
        String tmp = "XRESP:0:Successful:BADLOGINCOUNT,0:cn,161011;\\r\\nCAI>";

        try {
            new CAIResponse(tmp);
            fail("Expected CAIException");
        } catch (CAIException e) {
            System.out.println(e);
        }

        //value missing for an attribute
        tmp = "RESP:0:Successful:BADLOGINCOUNT;\\r\\nCAI>";

        try {
            new CAIResponse(tmp);
            fail("Expected CAIException");
        } catch (CAIException e) {
            System.out.println(e);
        }

        //No semicolon at the end
        tmp = "RESP:0:Successful:\\r\\nCAI>";

        try {
            new CAIResponse(tmp);
            fail("Expected CAIException");
        } catch (CAIException e) {
            System.out.println(e);
        }

        //Missing colons
        tmp = "RESP,0,Successful;\\r\\nCAI>";

        try {
            new CAIResponse(tmp);
            fail("Expected CAIException");
        } catch (CAIException e) {
            System.out.println(e);
        }

        //Blank string
        tmp = "";

        try {
            new CAIResponse(tmp);
            fail("Expected CAIException");
        } catch (CAIException e) {
            System.out.println(e);
        }

        //null
        try {
            new CAIResponse(null);
            fail("Expected CAIException");
        } catch (CAIException e) {
            System.out.println(e);
        }
    }

     public static Test suite() {
        return new TestSuite(CAIResponseTest.class);
    }
}
