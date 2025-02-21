/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.common.message_sender;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import org.jmock.MockObjectTestCase;

import java.util.Arrays;

/**
 * Documentation
 *
 * @author mande
 */
public abstract class MessageSenderBaseTestCase extends MockObjectTestCase {

    public MessageSenderBaseTestCase(String name) {
        super(name);
    }

    protected IConfiguration getConfiguration(String... cfgFiles) {
        // Setup configuration
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(cfgFiles);
        return configurationManager.getConfiguration();
    }

    protected static <T> void assertEquals(T[] expected, T[] actual) {
        assertEquals("", expected, actual);
    }

    private static <T> void assertEquals(String message, T[] expected, T[] actual) {
        assertTrue(message + "\nExpected:" + Arrays.toString(expected) + "\nActual  :" + Arrays.toString(actual),
                Arrays.equals(expected, actual));
    }
}
