/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import junit.framework.TestCase;
import org.apache.log4j.xml.DOMConfigurator;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import com.mobeon.common.configuration.ConfigurationManagerImpl;

public class FreePortHandlerTest extends TestCase {
    StreamFactoryImpl factory;
    FreePortHandler portHandler;

    public void setUp() {
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        factory = new StreamFactoryImpl();
        try {
            cm.setConfigFile("cfg/mas_stream.xml");
        }
        catch (Exception e) {
            fail("Failed to initiate the configuration manager: " + e);
        }
        try {
            StreamConfiguration.getInstance().setInitialConfiguration(cm.getConfiguration());
        }
        catch (Exception e) {
            fail("Failed to initiate the stream factory.");
        }
        try {
            factory.setContentTypeMapper(new ContentTypeMapperImpl());
            cm.setConfigFile("../cfg/mas.xml");
            StreamConfiguration.getInstance().setInitialConfiguration(cm.getConfiguration());
            factory.setConfiguration(cm.getConfiguration());
        }
        catch (Exception e) {
            fail("Failed to initiate the stream factory.");
        }
        portHandler = FreePortHandler.getInstance();
        portHandler.unInitialize();
        portHandler.setBase(4712);
        portHandler.setSize(10);
    }

    public void testDefaultLockPair() {
        assertEquals("Check port base:", 4712, portHandler.getBase());
        assertEquals("Check port base:", 10, portHandler.getSize());
        portHandler.initilialize();
        takeAll();
    }

    public void testDefaultRelasePair() {
        assertEquals("Check port base:", 4712, portHandler.getBase());
        assertEquals("Check port base:", 10, portHandler.getSize());
        portHandler.initilialize();
        takeAll();
        releaseAll();
    }

    public void testRoundRobinAllocation()
    {
        portHandler.initilialize();
        int firstPair = portHandler.lockPair();
        portHandler.releasePair(firstPair);
        int secondPair = portHandler.lockPair();
        assertFalse(firstPair == secondPair);
        assertEquals(portHandler.getBase(), firstPair);
        assertEquals(portHandler.getBase()+2, secondPair);
    }

    public void testDefaultConfigLockPair() {
        factory.init();
        assertEquals("Default port base: ", 23000, StreamConfiguration.getInstance().getPortPoolBase());
        assertEquals("Default port size: ", 250, StreamConfiguration.getInstance().getPortPoolSize());
        portHandler.initilialize();
        assertEquals("Check port base:", 23000, portHandler.getBase());
        assertEquals("Check port base:", 250, portHandler.getSize());
        takeAll();
    }

    public void testDefaultConfigRelasePair() {
        factory.init();
        assertEquals("Default port base: ", 23000, StreamConfiguration.getInstance().getPortPoolBase());
        assertEquals("Default port size: ", 250, StreamConfiguration.getInstance().getPortPoolSize());
        portHandler.initilialize();
        assertEquals("Check port base:", 23000, portHandler.getBase());
        assertEquals("Check port base:", 250, portHandler.getSize());
        takeAll();
        releaseAll();
    }

    void takeAll() {
        // Allocate all ports.
        for (int i=0; i < portHandler.getSize(); i++) {
            int portBase = portHandler.lockPair();
            assertTrue(portBase > 0);
        }
        // Verify that there are no more free ports.
        int portBase = portHandler.lockPair();
        assertEquals(portBase, -1);
    }

    void releaseAll() {
        // Ensure that all ports are released
        for (int i=0; i < portHandler.getSize(); i++) {
            portHandler.releasePair(i*2 + portHandler.getBase());
        }
        // Allocate two ports
        int portBase1 = portHandler.lockPair();
        int portBase2 = portHandler.lockPair();
        // Ensure that we got the two first ports
        assertEquals(portBase1, portHandler.getBase());
        assertEquals(portBase2, portHandler.getBase()+2);
        // Release first port
        portHandler.releasePair(portBase1);
        // Verify that we get the third port.
        int portBase3 = portHandler.lockPair();
        assertEquals(portBase3, portHandler.getBase()+4);
    }
}
