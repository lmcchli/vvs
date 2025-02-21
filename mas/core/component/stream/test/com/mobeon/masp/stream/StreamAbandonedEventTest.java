/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;

import junit.framework.TestCase;

/**
 * Testclass for class of {@link StreamAbandonedEvent}.
 * 
 * @author J�rgen Terner
 */
public class StreamAbandonedEventTest extends TestCase {
    
    protected StreamFactoryImpl mFactory;

    /**
     * Creates the test.
     * 
     * @param name Name of this test.
     */
    public StreamAbandonedEventTest(String name)
    {
       super(name);
    }
    
    /* JavaDoc in base class. */
    public void setUp() {
        mFactory = new StreamFactoryImpl();
        try {
            mFactory.setContentTypeMapper(new ContentTypeMapperImpl());
            ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
            cm.setConfigFile("cfg/mas_stream.xml");
            mFactory.setConfiguration(cm.getConfiguration());
            mFactory.init();        
        }
        catch (Exception e) {
            fail("Failed to initiate the stream factory: " + e);
        }
    }

    /* JavaDoc in base class. */
    public void tearDown() {
    }
    
    /**
     * <pre>
     * Constructor test.
     * 
     * Wrong arguments
     * ----------------------------
     * Action: Call constructor with stream = null.
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * ----------------------------
     * Action: Call constructor with stream != null.
     * Result: No exceptions are thrown.
     *         getMessage returns null.
     *         getStream returns the given stream.
     *         getId returns the given stream.
     * </pre>
     */
    public void testConstructor() {
        // Wrong arguments
        try {
            new StreamAbandonedEvent(null);
            fail("Wrong arguments should have caused an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException(
                    "Wrong arguments: Unexpected exception",
                    IllegalArgumentException.class, e);
        }

        // Correct arguments
        try {
            IMediaStream stream = mFactory.getOutboundMediaStream();
            StreamAbandonedEvent event = 
                new StreamAbandonedEvent(stream);
            assertNull("Correct arguments : unexpected message value.",
                    event.getMessage());
            assertSame("Correct arguments: unexpected id value.",
                    stream, event.getId());
            assertSame("Correct arguments: unexpected stream value.",
                    stream, event.getStream());
//            stream.delete();
        }
        catch (Exception e) {
            fail("Correct arguments: Unexpected exception: " + e);
        }
    }

    /**
     * Tests for equals(Object obj).
     * 
     * Common cases
     * ------------
     * Description: Control that two equal object gives true and two different
     *              objects give false. 
     * Condition: An array with three objects: 
     *     o1 = new StreamAbandonedEvent(new OutboundMediaStream());
     *     o2 = new StreamAbandonedEvent(new OutboundMediaStream());
     *     o3 = new StreamAbandonedEvent(new OutboundMediaStream());
     * Action: Call equals for all combinations.
     * Result: true for same index, false for different index. 
     * 
     * Compare to self
     * ---------------
     * Description: Control that equals to self gives true. 
     * Condition: obj = new StreamAbandonedEvent(new OutboundMediaStream())
     * Result: obj.equals(obj) = true 
     * 
     * Compare to null
     * ---------------
     * Description: Control that comparison to null gives false. 
     * Condition: obj = new StreamAbandonedEvent(new OutboundMediaStream())
     * Result:  obj.equals(null) = false 
     * 
     * Wrong type
     * ----------
     * Description: Control that comparison with different type gives false.
     * 
     * Condition: obj = new StreamAbandonedEvent(stream)
     * Action:
     *    1. obj.equals(new Integer(0)).
     *    2. obj.equals(new StreamAbandonedEvent(stream) {}) (subclass)
     * Resultat: false 
     */
    public void testEquals()
    {
        // Common cases
        StreamAbandonedEvent[] objList = new StreamAbandonedEvent[] {
                new StreamAbandonedEvent(mFactory.getOutboundMediaStream()),
                new StreamAbandonedEvent(mFactory.getOutboundMediaStream()),
                new StreamAbandonedEvent(mFactory.getOutboundMediaStream())
        };
        for (int i = 0; i < objList.length; i++)
        {
            for (int j = 0; j < objList.length; j++)
            {
                if (i == j) 
                {
                    assertTrue("Equals, " + i + " and " + j,
                            objList[i].equals(objList[j]));
                }
                else 
                {
                    assertFalse("Different, " + i + " and " + j,
                            objList[i].equals(objList[j]));
                }
            }
        }
        
        // Compare to self
        StreamAbandonedEvent event = 
            new StreamAbandonedEvent(mFactory.getOutboundMediaStream());
        assertTrue("Compare to self", event.equals(event));
        
        // Compare to null
        assertFalse("Compare to null", event.equals(null));
        
        // Wrong type 1
        IMediaStream stream = mFactory.getOutboundMediaStream();
        event = new StreamAbandonedEvent(stream);
        assertFalse("Wrong type 1", event.equals(new Integer(0)));
        // Wrong type 2
        assertFalse("Wrong type 2", 
                event.equals(new StreamAbandonedEvent(stream){}));
//        stream.delete();
    }
    
    /**
     * Tests for hashCode().
     * 
     * Common cases
     * ------------
     * Description: Controls that two object that are equal gives the same
     *              hashcode. Also controls that two objects that are not
     *              equals gives different hashcode (not required by the spec
     *              but is preferrable). 
     * Condition: An array with the following objects: 
     *    1. o1 = new StreamAbandonedEvent(new OutboundMediaStream())
     *    2. o2 = new StreamAbandonedEvent(new OutboundMediaStream())
     *    3. o3 = new StreamAbandonedEvent(stream1)
     *    4. o4 = new StreamAbandonedEvent(stream1) 
     *
     * Action: Test equals on all combinations.
     * Result: If equal, hashCode should be equal for both objects.
     *         If not equal, hashCode should give different values.
     */
    public void testHashCode()
    {
        // Common cases
        IMediaStream stream = mFactory.getOutboundMediaStream();
        StreamAbandonedEvent[] objList = new StreamAbandonedEvent[] {
                new StreamAbandonedEvent(mFactory.getOutboundMediaStream()),
                new StreamAbandonedEvent(mFactory.getOutboundMediaStream()),
                new StreamAbandonedEvent(stream),
                new StreamAbandonedEvent(stream)
        };
        for (int i = 0; i < objList.length; i++) {
            for (int j = 0; j < objList.length; j++) {
                if (objList[i].equals(objList[j])) {
                    assertEquals("Equals, " + i + " and " + j,
                        objList[i].hashCode(), objList[j].hashCode());
                }
                else {
                    assertTrue("Different, " + i + " and " + j,
                        objList[i].hashCode() != objList[j].hashCode());
                }
            }
        }
    }
}