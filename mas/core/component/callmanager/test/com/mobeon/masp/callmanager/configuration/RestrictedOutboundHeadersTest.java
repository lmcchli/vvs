/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import junit.framework.TestCase;

import junit.framework.*;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.configuration.RestrictedOutboundHeaders.RestrictedHeader;

/**
 * RestrictedOutboundHeaders Tester.
 * <p>
 * This test case only covers the part of the RestrictedOutboundHeaders class
 * that is not verified in the Call Manager "component" tests.
 *
 * @author Malin Nyfeldt
 */
public class RestrictedOutboundHeadersTest extends TestCase {

    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    public void setUp() throws Exception {
        super.setUp();

        // Create a configuration manager and read the configuration file
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile("cfg/callManager.conf");
        CMUtils.getInstance().setCallManagerLicensing(new CallManagerLicensingMock());

        // Initialize configuration now to be able to setup SSPs before CM
        ConfigurationReader.getInstance().setInitialConfiguration(
                cm.getConfiguration());
        ConfigurationReader.getInstance().update();
    }

    /**
     * Verifies that if no restricted outbound headers exists in the
     * configuration, an empty set is returned.
     * @throws Exception if test case fails.
     */
    public void testParseRestrictedHeadersWhenNoConfigurationExists()
            throws Exception {

        RestrictedOutboundHeaders headers = ConfigurationReader.getInstance().getConfig().getRestrictedOutboundHeaders();

        assertFalse(headers.isRestricted(RestrictedHeader.REMOTE_PARTY_ID));
        assertFalse(headers.isRestricted(RestrictedHeader.P_ASSERTED_IDENTITY));
        assertEquals(0, headers.getAmountOfRestrictedHeaders());
    }

    /**
     * Verifies that no restricted outbound headers exists in the default value.
     * @throws Exception if test case fails.
     */
    public void testGetDefaultRestrictedHeaders() throws Exception {
        RestrictedOutboundHeaders headers = RestrictedOutboundHeaders.
                getDefaultRestrictedOutboundHeaders();

        assertFalse(headers.isRestricted(RestrictedHeader.REMOTE_PARTY_ID));
        assertFalse(headers.isRestricted(RestrictedHeader.P_ASSERTED_IDENTITY));
        assertEquals(0, headers.getAmountOfRestrictedHeaders());
    }

}
