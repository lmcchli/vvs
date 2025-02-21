/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.greetings;

import org.jmock.integration.junit4.JMock;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;

import junit.framework.TestSuite;
import junit.framework.Assert;

/**
 * GreetingSpecification Tester.
 *
 * @author mande
 * @since <pre>12/05/2005</pre>
 * @version 1.0
 */
@RunWith(JMock.class)
public class GreetingSpecificationTest extends GreetingMockObjectBaseTestCase {


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test constructor
     */
    @Test
    public void testGreetingSpecification() {
        GreetingSpecification specification;
        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                if (type == GreetingType.DIST_LIST_SPOKEN_NAME && format == GreetingFormat.VIDEO) {
                    // No support for video distlistspokenname yet
                    continue;
                }
                if (STRING_REPRESENTED_TYPES.contains(type)) {
                    specification = new GreetingSpecification(greetingMap.get(type), format);
                } else {
                    specification = new GreetingSpecification(type, format);
                }
                if (SUBID_TYPES.contains(type)) {
                    Assert.assertFalse(specification + " should be invalid", specification.isValid());
                    specification.setSubId(SUBID);
                    Assert.assertEquals("SubId should be " + SUBID, SUBID, specification.getSubId());
                } else {
                	Assert.assertTrue(specification + " should be valid", specification.isValid());
                	Assert.assertNull("SubId should be null", specification.getSubId());
                }
                Assert.assertTrue(specification + " should be valid", specification.isValid());
                Assert.assertEquals("Type should be " + type, type, specification.getType());
                Assert.assertEquals("Format should be " + format, format, specification.getFormat());
                if (!SUBID_TYPES.contains(type)) {
                    specification.setSubId(SUBID);
                    Assert.assertFalse(specification + " should be invalid", specification.isValid());
                }
            }
        }
    }

    /**
     * Test getting a subscriber's nonexisting greeting
     */
    @Test
    public void testGreetingSpecificationIllegalArgumentException() throws Exception {
        try  {
            new GreetingSpecification("nonexisting", GreetingFormat.VOICE);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        	Assert.assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test the type setter
     */
    @Test
    public void testSetType() {
        for (GreetingType type : GreetingType.values()) {
            if (STRING_REPRESENTED_TYPES.contains(type)) {
                GreetingSpecification greetingSpecification = new GreetingSpecification();
                greetingSpecification.setType(greetingMap.get(type));
                Assert.assertEquals("Type should be " + type, type, greetingSpecification.getType());
            }
        }
    }

    /**
     * Test the format setter
     */
    @Test
    public void testSetFormat() {
        for (GreetingFormat format : GreetingFormat.values()) {
            GreetingSpecification greetingSpecification = new GreetingSpecification();
            greetingSpecification.setFormat(format);
            Assert.assertEquals("Format should be " + format, format, greetingSpecification.getFormat());
        }
    }

    /**
     * Test the subid setter
     */
    @Test
    public void testSetSubId() {
        GreetingSpecification greetingSpecification = new GreetingSpecification();
        greetingSpecification.setSubId(SUBID);
        Assert.assertEquals("SubId should be " + SUBID, SUBID, greetingSpecification.getSubId());
    }

    @Test
    public void testEquals() throws Exception {
        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                GreetingSpecification greetingSpecification1 = new GreetingSpecification(type, format);
                GreetingSpecification greetingSpecification2 = new GreetingSpecification(type, format);
                Assert.assertTrue("Specifications should be equal", greetingSpecification1.equals(greetingSpecification1));
                Assert.assertTrue("Specifications should be equal", greetingSpecification1.equals(greetingSpecification2));
                Assert.assertTrue("Specifications should be equal", greetingSpecification2.equals(greetingSpecification1));
                Assert.assertFalse("Specification should not be equal", greetingSpecification2.equals(new Object()));
            }
        }
    }

    @Test
    public void testHashCode() throws Exception {
        for (GreetingType type1 : GreetingType.values()) {
            for (GreetingFormat format1 : GreetingFormat.values()) {
                GreetingSpecification specification1 = new GreetingSpecification(type1, format1);
                for (GreetingType type2 : GreetingType.values()) {
                    for (GreetingFormat format2 : GreetingFormat.values()) {
                        GreetingSpecification specification2 = new GreetingSpecification(type2, format2);
                        if (specification2.equals(specification1)) {
                        	Assert.assertTrue("Hashcode should be equal", specification1.hashCode() == specification2.hashCode());
                        } else {
                        	Assert.assertFalse("Hashcode should be inequal", specification1.hashCode() == specification2.hashCode());
                        }
                    }
                }
            }
        }
    }

    public static junit.framework.Test suite() {
        return new TestSuite(GreetingSpecificationTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
