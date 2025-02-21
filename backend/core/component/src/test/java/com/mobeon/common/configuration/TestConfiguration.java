/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.configuration;

import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.LinkedList;

/**
 * User: eperber
 * Date: 2005-sep-20
 * Time: 12:51:46.
 */
public final class TestConfiguration extends TestCase {

    static {
        ILoggerFactory.configureAndWatch("log4j2conf.xml");
    }

    private final Collection<String> configFilenames = new LinkedList<String>();
    private final Collection<String> configFilenames2 = new LinkedList<String>();
    private final Collection<String> unknownFilenames = new LinkedList<String>();
    private final Collection<String> failedFilenames = new LinkedList<String>();
    private final Collection<String> failedWithBackupFilenames = new LinkedList<String>();
    private final String configFilename = "./src/test/java/com/mobeon/common/configuration/cfg/mas.cfg";
    private final String configFilename2 = "./src/test/java/com/mobeon/common/configuration/cfg/mas2.cfg";
    private final String unknownFilename = "unknownFile.cfg";
    private final String failedFilename = "./src/test/java/com/mobeon/common/configuration/cfg/failed.cfg";
    private final String failedWithBackupFilename = "./src/test/java/com/mobeon/common/configuration/cfg/failedWithBackup.cfg";

    public TestConfiguration() {
        configFilenames.add(configFilename);
        configFilenames.add(configFilename2);
        configFilenames2.add(configFilename);
        configFilenames2.add(configFilename2);
        unknownFilenames.add(unknownFilename);
        failedFilenames.add(failedFilename);
        failedWithBackupFilenames.add(failedWithBackupFilename);
    }

    /**
     * @throws MissingConfigurationFileException
     *
     */
    public void testInit() throws ConfigurationException {
        ConfigurationImpl config = new ConfigurationImpl(null, configFilenames, false);
        assertNotNull(config);
    }

    /**
     * .
     */
    public void testFailedInit() throws ConfigurationException {
        try {
            new ConfigurationImpl(null, unknownFilenames, false);
            fail("Should have thrown MissingConfigurationFileException.");
        } catch (MissingConfigurationFileException e) {
            assertTrue(true);
        }

        try {
            new ConfigurationImpl(null, failedFilenames, false);
            fail("Should have thrown ConfigurationLoadException.");
        } catch (ConfigurationLoadException e) {
            assertTrue(true);
        }
    }

    /**
     * @throws MissingConfigurationFileException
     *
     */
    public void testGetUnkwnownGroup() throws ConfigurationException {
        ConfigurationImpl config = new ConfigurationImpl(null, configFilenames, false);
        try {
            config.getGroup("UnknownGroup");
            fail("Should have thrown UnknownGroupException.");
        } catch (UnknownGroupException e) {
            assertEquals("UnknownGroup", e.getGroupName());
        }
    }

    /**
     * .
     *
     * @throws UnknownGroupException
     * @throws MissingConfigurationFileException
     *
     */
    public void testGetGroup() throws ConfigurationException {
        ConfigurationImpl config = new ConfigurationImpl(null, configFilenames, false);
        IGroup g = config.getGroup("KnownGroup");
        assertNotNull(g);
        assertEquals("KnownGroup", g.getName());
    }

    public void testMultipleCfgFiles() throws ConfigurationException {
        ConfigurationImpl config = new ConfigurationImpl(null, configFilenames2, false);

        IGroup g = config.getGroup("KnownGroup");
        assertNotNull(g);
        assertEquals("KnownGroup", g.getName());

        g = config.getGroup("KnownGroup2");
        assertNotNull(g);
        assertEquals("KnownGroup2", g.getName());
    }

    public void testGetBackupUsed() throws Exception {
        ConfigurationImpl config = new ConfigurationImpl(null, configFilenames, true);
        assertFalse(config.getBackupUsed());

        ConfigurationImpl config2 = new ConfigurationImpl(null, failedWithBackupFilenames, true);
        assertTrue(config2.getBackupUsed());
    }
}
