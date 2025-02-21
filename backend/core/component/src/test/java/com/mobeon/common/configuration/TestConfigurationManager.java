/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.configuration;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.logging.ILoggerFactory;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * User: eperber
 * Date: 2005-okt-07
 * Time: 11:12:56.
 */
public final class TestConfigurationManager extends MockObjectTestCase {
    private IConfigurationManager cfgMgr;
    private Mock mockEventDispatcher = null;

    static {
        ILoggerFactory.configureAndWatch("log4jconf.xml");
    }

    public TestConfigurationManager() {
    }

    /**
     * .
     */
    public void setUp() throws Exception {
        super.setUp();
        mockEventDispatcher = mock(IEventDispatcher.class);
        cfgMgr = new ConfigurationManagerImpl();
        cfgMgr.setConfigFile("test/com/mobeon/common/configuration/cfg/mas.cfg");
        cfgMgr.setEventDispatcher((IEventDispatcher) mockEventDispatcher.proxy());
    }

    /**
     * .
     */
    public void testInit() {
        mockEventDispatcher.expects(never()).method("fireGlobalEvent").with(isA(ConfigurationChanged.class));
        assertTrue(true);
    }

    /**
     * .
     *
     * @throws MissingConfigurationFileException
     *
     * @throws ConfigurationLoadException
     */
    public void testGetConfiguration() throws ConfigurationException {
        IConfiguration config1 = cfgMgr.getConfiguration();
        assertNotNull(config1);
        assertFalse(config1.getBackupUsed());
    }

    /**
     * .
     *
     * @throws MissingConfigurationFileException
     *
     * @throws ConfigurationLoadException
     */
    public void testReload() throws ConfigurationException {
        mockEventDispatcher.expects(atLeastOnce()).method("fireGlobalEvent").with(isA(ConfigurationChanged.class));
        IConfiguration config1 = cfgMgr.getConfiguration();
        assertFalse(config1.getBackupUsed());
        assertTrue(cfgMgr.reload());
        IConfiguration config2 = cfgMgr.getConfiguration();
        assertFalse(config2.getBackupUsed());
        assertNotSame(config1, config2);
    }

    /**
     * .
     *
     * @throws MissingConfigurationFileException
     *
     * @throws ConfigurationLoadException
     */
    public void testSetConfigFile() throws ConfigurationException {
        mockEventDispatcher.expects(atLeastOnce()).method("fireGlobalEvent").with(isA(ConfigurationChanged.class));
        IConfiguration config1 = cfgMgr.getConfiguration();
        assertFalse(config1.getBackupUsed());
        cfgMgr.setConfigFile("test/com/mobeon/common/configuration/cfg/mas2.cfg");
        IConfiguration config2 = cfgMgr.getConfiguration();
        assertFalse(config2.getBackupUsed());
        assertNotSame(config1, config2);
        assertTrue(cfgMgr.reload());
        IConfiguration config3 = cfgMgr.getConfiguration();
        assertFalse(config3.getBackupUsed());
        assertNotSame(config2, config3);
    }

    /**
     * .
     */
    public void testFailedInit() throws ConfigurationException {
        try {
            cfgMgr = new ConfigurationManagerImpl();
            // At this time the currentConfig value of ConfigurationManager is
            // null. That means the application should exit if any errors
            // occurs.
            cfgMgr.setConfigFile("test/com/mobeon/common/configuration/cfg/unknown.cfg");
            cfgMgr.reload();
            fail("Should have thrown MissingConfigurationFileException.");
        } catch (java.lang.RuntimeException e) {
            assertTrue("Should have thrown MissingConfigurationFileException.", e.getCause() instanceof MissingConfigurationFileException);
        }
        // Just to ensure that currentConfig has a value.
        cfgMgr.setConfigFile("test/com/mobeon/common/configuration/cfg/mas.cfg");
        assertTrue(cfgMgr.reload());

        // Test to see if everything is ok when an old configuration exists.
        cfgMgr.setConfigFile("test/com/mobeon/common/configuration/cfg/unknown.cfg");
        assertFalse(cfgMgr.reload());
    }

    /**
     * .
     */
    public void testFailedWithBackup() {
        // At this time the currentConfig value of ConfigurationManager is
        // null. That means the application should exit if any errors
        // occurs.
        cfgMgr.setConfigFile("test/com/mobeon/common/configuration/cfg/failedWithBackup.cfg");
        IConfiguration config = cfgMgr.getConfiguration();
        assertNotNull(config);
        assertTrue(cfgMgr.getBackupUsed());
    }

    /**
     * .
     */
    public void testFailedReload() throws ConfigurationException {
        cfgMgr.getConfiguration();
        cfgMgr.setConfigFile("test/com/mobeon/common/configuration/cfg/unknown.cfg");
        assertFalse(cfgMgr.reload());
    }
}
