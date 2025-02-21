package com.mobeon.masp.operateandmaintainmanager;

import junit.framework.TestCase;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class ConsumedServiceTest extends TestCase {
    ILogger log;
    OMManager omm = new OMManager();

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch("logmanager.xml");
        log = ILoggerFactory.getILogger(ProvidedServiceTest.class);

        MulticastDispatcherMock eventDispatcher = new MulticastDispatcherMock();
        ConfigurationManagerImpl configuration = new ConfigurationManagerImpl();
        configuration.setConfigFile("mas.xml");
        IConfiguration config = configuration.getConfiguration();
        omm.setConfigurationManager(configuration); // set config manager to be able to reload config
        omm.setConfiguration(config);               // set configuration for om
        omm.setEventDispatcher(eventDispatcher);
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


    public void testRegisterConsumedService(){
 //       ILoggerFactory.configureAndWatch("logmanager.xml");
 //       log = ILoggerFactory.getILogger(ProvidedServiceTest.class);

        MulticastDispatcherMock eventDispatcher = new MulticastDispatcherMock();

 /*       OMManager omm = new OMManager();
        ConfigurationManagerImpl configuration = new ConfigurationManagerImpl();
        configuration.setConfigFile("mas.xml");
        IConfiguration config = configuration.getConfiguration();
        omm.setConfigurationManager(configuration); // set config manager to be able to reload config
        omm.setConfiguration(config);               // set configuration for om
        omm.setEventDispatcher(eventDispatcher);
        omm.init();
   */


        ConsumedService consumedService = omm.createConsumedServiceEntry("consumed_service","localhost",1234);

        assertEquals((long)consumedService.getSuccessOperations(),(long)0);
        assertEquals((long)consumedService.getFaildOperations(),(long)0);
        consumedService.incrementFailedOperations();
        consumedService.incrementSuccessOperations();
        assertEquals((long)consumedService.getSuccessOperations(),(long)1);
        assertEquals((long)consumedService.getFaildOperations(),(long)1);
        consumedService.setStatus(Status.UP);

        ConsumedServiceEntry consumedServiceEntry = (ConsumedServiceEntry) consumedService;
        consumedServiceEntry.getStatus();
        assertEquals(Status.UP,consumedServiceEntry.getStatus());
        assertEquals("consumed_service",consumedServiceEntry.getServiceName());consumedServiceEntry.getServiceName();
        consumedServiceEntry.getStatusChangeTime();




/*        try {
            omm.finalize();
            omm = null;
        } catch (Throwable throwable) {
            throwable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
  */

    }


}
