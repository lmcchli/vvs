/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * ProfileAttribute Tester.
 *
 * @author mande
 * @since <pre>11/25/2005</pre>
 * @version 1.0
 */
public class ProfileAttributeTest extends ProfileManagerMockObjectBaseTestCase
{
    private ProfileAttribute profileAttribute;
    private String[] origData;

    public ProfileAttributeTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        String[] data = new String[3];
        for (int i = 0; i < data.length; i++) {
            data[i] = "item" + Integer.toString(i + 1);
        }
        // Store copy of original data
        origData = data.clone();
        profileAttribute = new ProfileAttribute(data);
        // Changes in array should not affect ProfileAttribute
        for (int i = 0; i < data.length; i++) {
            data[i] = "newitem" + Integer.toString(i + 1);
        }
        assertEquals("ProfileAttribute should be unchanged", origData, profileAttribute.getData());
    }

    public void tearDown() throws Exception {
    }

    public void testGetData() throws Exception {
        String[] data = profileAttribute.getData();
        assertEquals(origData, data);
        // Changes in array should not affect profileAttribute
        for (int i = 0; i < data.length; i++) {
            data[i] = "newitem" + Integer.toString(i + 1);
        }
        assertEquals("ProfileAttribute should be unchanged", origData, profileAttribute.getData());
    }

    public void testSetData() throws Exception {
        String[] data = new String[3];
        for (int i = 0; i < data.length; i++) {
            data[i] = "newitem" + Integer.toString(i + 1);
        }
        String[] oldData = data.clone();
        profileAttribute.setData(data);
        assertEquals(data, profileAttribute.getData());
        // Changes in array should not affect profileAttribute
        for (int i = 0; i < data.length; i++) {
            data[i] = "neweritem" + Integer.toString(i + 1);
        }
        assertEquals("ProfileAttribute should be unchanged", oldData, profileAttribute.getData());
    }

    public static Test suite() {
        return new TestSuite(ProfileAttributeTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
