package com.mobeon.masp.profilemanager.cache;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * TimedCache Tester.
 *
 * @author mande
 * @since <pre>02/21/2006</pre>
 * @version 1.0
 */
public class TimedCacheTest extends TestCase
{
    private TimedCache<String, Object> timedCache;
    private Object cachedObject;
    private static final int TIMEOUT = 100;

    public TimedCacheTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        timedCache = new TimedCache<String, Object>(TIMEOUT);
        cachedObject = new Object();
        timedCache.put("entry", cachedObject);
    }

    public void tearDown() throws Exception {
    }

    public void testGet() throws Exception {
        assertEquals("Cached object should exist", cachedObject, timedCache.get("entry"));
    }

    public void testGetExpiredEntry() throws Exception {
        synchronized(this) {
            wait(TIMEOUT + 10);
        }
        assertNull("Cached object should be null", timedCache.get("entry"));
    }

    public static Test suite() {
        return new TestSuite(TimedCacheTest.class);
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
}
