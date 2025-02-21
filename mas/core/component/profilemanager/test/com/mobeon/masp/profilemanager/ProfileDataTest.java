/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * ProfileData Tester.
 *
 * @author mande
 * @since <pre>11/25/2005</pre>
 * @version 1.0
 */
public class ProfileDataTest extends ProfileManagerMockObjectBaseTestCase
{
    private ProfileData profileData;
    private String[] origData;

    public ProfileDataTest(String name) {
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
        profileData = new ProfileData(data);
        // Changes in array should not affect ProfileData
        for (int i = 0; i < data.length; i++) {
            data[i] = "newitem" + Integer.toString(i + 1);
        }
        assertEquals("ProfileData should be unchanged", origData, profileData.getData());
    }

    public void tearDown() throws Exception {
    }

    public void testGetData() throws Exception {
        String[] data = profileData.getData();
        assertEquals(origData, data);
        // Changes in array should not affect profileData
        for (int i = 0; i < data.length; i++) {
            data[i] = "newitem" + Integer.toString(i + 1);
        }
        assertEquals("ProfileData should be unchanged", origData, profileData.getData());
    }

    public void testSetData() throws Exception {
        String[] data = new String[3];
        for (int i = 0; i < data.length; i++) {
            data[i] = "newitem" + Integer.toString(i + 1);
        }
        String[] oldData = data.clone();
        profileData.setData(data);
        assertEquals(data, profileData.getData());
        // Changes in array should not affect profileData
        for (int i = 0; i < data.length; i++) {
            data[i] = "neweritem" + Integer.toString(i + 1);
        }
        assertEquals("ProfileData should be unchanged", oldData, profileData.getData());
    }

    public static Test suite() {
        return new TestSuite(ProfileDataTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
