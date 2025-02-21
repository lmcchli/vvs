/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import junit.framework.TestCase;

/**
 * ConfigurationReader Tester.
 * <p>
 * This test case only covers the part of the ConfigurationReader class
 * that is not verified in the Call Manager "component" tests.
 *
 * @author Malin Flodin
 */
public class ConfigurationReaderTest extends TestCase
{
    private ConfigurationReader reader;

    public void setUp() throws Exception {
        super.setUp();

        reader = new ConfigurationReader();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetConfigWhenNotInitialized() throws Exception {
        try {
            reader.getConfig();
            fail("Exception not thrown when expected.");
        } catch (IllegalStateException e) {
        }
    }

    public void testSetInitialConfigurationWhenConfigIsNull() throws Exception {
        try {
            reader.setInitialConfiguration(null);
            fail("Exception not thrown when expected.");
        } catch (IllegalArgumentException e) {
        }
    }
}
