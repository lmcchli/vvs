/*
 * PhoneOnMapTest.java
 * JUnit based test
 *
 * Created on den 21 september 2004, 12:58
 */

package com.mobeon.ntf.out.outdial.test;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import junit.framework.*;

import com.mobeon.ntf.out.outdial.*;
import com.mobeon.ntf.util.DelayLoggerProxy;
import com.mobeon.common.storedelay.SDLogger;

/**
 *
 */
public class PhoneOnMapTest extends TestCase
{

    public PhoneOnMapTest(java.lang.String testName)
    {
        super(testName);
    }
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        SDLogger.setLogger(new DelayLoggerProxy());
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(PhoneOnMapTest.class);
        return suite;
    }

    public void testExists()
    {
        PhoneOnMap map = new PhoneOnMap();
        assertFalse("Non existing does not exists", map.exists("12345"));
        map.add("12345", "test@example.org");
        assertTrue("Number exists now", map.exists("12345"));
    }

    public void testRetreive()
    {
        PhoneOnMap map = new PhoneOnMap();
        map.add("12345", "test1@example.org");
        map.add("12345", "test2@example.org");
        map.add("23456", "test3@example.org");
        map.add("23456", "test3@example.org");
        List l1 = map.getEmails("12345");
        assertEquals("Members in list", 2, l1.size());
        List l2 = map.getEmails("23456");
        assertEquals("Members in list", 1, l2.size());

        List l3 = map.getEmails("34567");
        assertNull("34567 never inserted", l3);

        map.remove("12345");
        List l4 = map.getEmails("12345");
        assertNull("12345 removed now", l4);
    }


}
