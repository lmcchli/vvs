package com.mobeon.masp.util.check;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * Check Tester.
 *
 * @author mande
 * @since <pre>12/07/2005</pre>
 * @version 1.0
 */
public class CheckTest extends TestCase
{
    private Check check;

    public CheckTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        check = new Check();
    }

    public void testIsOK() throws Exception {
        assertTrue("Check should be ok", check.isOK());
        //TODO: Further testing
    }

    public void testGetErrorMessage() throws Exception {
        assertEquals("Error message should be empty", "", check.getErrorMessage());
        //TODO: Further testing
    }

    public void testCheckin() throws Exception {
        assertEquals("Password should be 3Z191R240A0L472u", "3Z191R240A0L472u", check.checkin("abcd"));
        assertEquals("Password should be 2u3a0C4W3V2k1B16", "2u3a0C4W3V2k1B16", check.checkin("Gr8Pw4GA"));
    }

    public void testCheckout() throws Exception {
        assertEquals("Password should be abcd", "abcd", check.checkout("3Z191R240A0L472u"));
        assertEquals("Password should be Gr8Pw4GA", "Gr8Pw4GA", check.checkout("2u3a0C4W3V2k1B16"));
    }

    public static Test suite() {
        return new TestSuite(CheckTest.class);
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
}
