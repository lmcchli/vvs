/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.provisionmanager;

import org.jmock.MockObjectTestCase;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.configuration.ConfigurationManagerImpl;

/**
 * Testcase for the ProvisioningConfiguration class
 *
 * @author ermmaha
 */
public class ProvisioningConfigurationTest extends MockObjectTestCase {
    private static final String cfgFile = "../provisionmanager/test/com/mobeon/masp/provisionmanager/provisionmanager.xml";
    private static final String LOG4J_CONFIGURATION = "../provisionmanager/log4jconf.xml";

    static {
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    protected IConfiguration configuration;

    public ProvisioningConfigurationTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        configuration = getConfiguration();
    }

    /**
     * Verifies all get methods in ProvisioningConfiguration
     *
     * @throws Exception if test case fails.
     */
    public void testConfiguration() throws Exception {
        ProvisioningConfiguration c = ProvisioningConfiguration.getInstance();
        c.setConfiguration(configuration);
        c.update();

        assertEquals(5, c.getConnectionPoolSize());
        assertEquals(10000, c.getConnectionTimeout());
        assertEquals(60000, c.getConnectionIdleTimeout());
        assertEquals(1, c.getCommandSendRetries());
        assertEquals("ockelbo.lab.mobeon.com", c.getDefaultMailhost());
    }

    private IConfiguration getConfiguration() throws Exception {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(cfgFile);
        return configurationManager.getConfiguration();
    }
}
