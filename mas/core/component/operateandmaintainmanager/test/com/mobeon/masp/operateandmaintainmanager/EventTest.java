package com.mobeon.masp.operateandmaintainmanager;

import junit.framework.TestCase;
//import com.mobeon.masp.execution_engine.eventnotifier.IEventDispatcher;
//import com.mobeon.masp.execution_engine.eventnotifier.MulticastDispatcher;
//import com.mobeon.masp.execution_engine.eventnotifier.Multicaster;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.MulticastDispatcher;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.ConfigurationChanged;

import java.util.ArrayList;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class EventTest extends TestCase{
//    IEventDispatcher eventDispatcher = new MulticastDispatcherMock();
//    EventHandlerMock eventHandler = new EventHandlerMock();
    IConfiguration config;
    MulticastDispatcher ommEventDispatcher;
    ILogger log;
    OMManager omm = new OMManager();

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch("logmanager.xml");
        log = ILoggerFactory.getILogger(ProvidedServiceTest.class);

        ommEventDispatcher = new MulticastDispatcher();
        ConfigurationManagerImpl configuration = new ConfigurationManagerImpl();
        configuration.setConfigFile("mas.xml");
        config = configuration.getConfiguration();
        omm.setConfigurationManager(configuration); // set config manager to be able to reload config
        omm.setConfiguration(config);               // set configuration for om
        omm.setEventDispatcher(ommEventDispatcher);
        omm.init();

    }

    public void tearDown() throws Exception {
        super.tearDown();
        try {
            omm.finalize();
            omm = null;
        } catch (Throwable throwable) {
            throwable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void testGlobalEvent(){
//        ILoggerFactory.configureAndWatch("logmanager.xml");
//        log = ILoggerFactory.getILogger(ProvidedServiceTest.class);
        IEventDispatcher eventDispatcher = new MulticastDispatcherMock();
        EventHandlerMock eventHandler = new EventHandlerMock();


        eventDispatcher.addEventReceiver(eventHandler);
        ArrayList list = eventDispatcher.getEventReceivers();

        assertTrue(list.get(0).getClass().isInstance(eventHandler));

        eventDispatcher.fireGlobalEvent(new EventMock());
        eventDispatcher.fireEvent(new EventMock());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        assertEquals(true,eventHandler.nonGlobalEvent);
        assertEquals(true,eventHandler.globalEvent);
    }


    public void testOmmGlobalEvent(){
        EventHandlerMock eventHandler = new EventHandlerMock();




        ServiceEnablerInfo serviceEnablerInfo;
        // Create a new service enabler
        // ServiceEnablerOperateImplMock implements the ServiceEnabler interface
        ServiceEnablerOperateImplMock seop = new ServiceEnablerOperateImplMock("sename");
        seop.setProtocol("sip");

        try {
             serviceEnablerInfo = omm.getServiceEnablerStatistics(seop);
             ommEventDispatcher.addEventReceiver(eventHandler);
             ommEventDispatcher.fireGlobalEvent(new ConfigurationChanged(config));

         } catch (Exception e) {
             e.printStackTrace();
         }


        //ommEventDispatcher.fireGlobalEvent(new EventMock());
        //ommEventDispatcher.fireEvent(new EventMock());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        assertEquals(false,eventHandler.nonGlobalEvent);
        //assertEquals(true,eventHandler.serviceShutdownEvent);
        assertEquals(true,eventHandler.globalEvent);
        assertEquals(true,eventHandler.configurationChanged);

    }


    public void testEvents(){

        ThrowExceptionMock ex = new ThrowExceptionMock();

        try {
            ex.throwIlegalSessionInstanceException();
            assertTrue(false);
        } catch (IlegalSessionInstanceException e) {
            assertTrue(e.getCause().getMessage().equals("exception"));
        }

        try {
            ex.throwIllegalParameterException();
            assertTrue(false);
        } catch (IlegalServiceParametersException e) {
            assertTrue(e.getCause().getMessage().equals("exception"));
        }
    }


}
