/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import org.junit.Ignore;

import junit.framework.TestCase;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.configuration.RemoteParty;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * RemotePartyControllerImpl Tester.
 *
 * @author Malin Flodin
 */
public class RemotePartyControllerImplTest extends TestCase
{
	/* The SSPs were removed from the config and from the code. There is no need to test them.
	 */
    RemoteParty originalRemoteParty;
    RemoteParty updatedRemoteParty;

    RemotePartyControllerImpl remotePartyController;


    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch("cfg/logmanager.xml");
    }

    /**
     * Reads the config file callManager.conf.
     * @throws Exception if setup fails.
     */
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

        CallManagerControllerImpl cmController = new CallManagerControllerImpl();
        CMUtils.getInstance().setCallManagerController(cmController);

        remotePartyController = new RemotePartyControllerImpl();
    }

    public void testDefaultSipProxyInRemoteParties() throws Exception {
        assertEquals(1, remotePartyController.getAmountOfConfiguredRemoteParties());
        assertEquals("sipproxy:5060",remotePartyController.getRandomRemotePartyAddress().toString());
    }

    public void testConfiguredSipProxyInRemoteParties() throws Exception {
    	originalRemoteParty = new RemoteParty();
    	originalRemoteParty.setSipProxy("localhost", 5555);
        ConfigurationReader.getInstance().getConfig().setRemoteParty(originalRemoteParty);
        remotePartyController.reInitialize();
        assertEquals(1, remotePartyController.getAmountOfConfiguredRemoteParties());
        assertEquals("localhost:5555",remotePartyController.getRandomRemotePartyAddress().toString());
    }

}
