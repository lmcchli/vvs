package com.mobeon.masp.operateandmaintainmanager;

import junit.framework.TestCase;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class ProvidedServiceTest  extends TestCase {
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

    public void testRegisterProvidedService(){
//        ILoggerFactory.configureAndWatch("logmanager.xml");
//        log = ILoggerFactory.getILogger(ProvidedServiceTest.class);

        MulticastDispatcherMock eventDispatcher = new MulticastDispatcherMock();

        // Create a new service enabler
        // ServiceEnablerOperateImplMock implements the ServiceEnabler interface
        ServiceEnablerOperateImplMock seop = new ServiceEnablerOperateImplMock("sename");
        seop.setProtocol("sip");

        ServiceEnabler serviceEnabler = null;
        try {
            serviceEnabler = (ServiceEnabler)omm.getServiceEnablerStatistics(seop);
            seop.setServiceInfo(serviceEnabler);
        } catch (Exception e) {
            assertTrue(false);
        }


        // Create provided Service entry object
        ProvidedService providedService;
        try {
            // Creates the provided service on the service enabler.
            providedService= omm.createProvidedServiceEntry("service_name","loclahost",1111,seop);

            // set values on the provided service entry object.
            providedService.setApplication("appname","appversion");
            providedService.setStatus(Status.UNKNOWN);
        } catch (IlegalServiceParametersException e) {
            e.printStackTrace();
        }

        // Retreive the serviceenablerinfo object to be able to set data to the service enabler.
        ServiceEnablerInfo serviceEnablerInfo;
        try {
            serviceEnablerInfo=omm.getServiceEnablerStatistics(seop);
            serviceEnablerInfo.incrementCurrentConnections(CallType.VOICE,CallDirection.INBOUND);
            serviceEnablerInfo.incrementNumberOfConnections(CallType.VOICE,CallResult.ABANDONED,CallDirection.INBOUND);
            serviceEnablerInfo.setMaxConnections(50);
            serviceEnablerInfo.setProtocol("test");

            serviceEnabler = (ServiceEnabler)serviceEnablerInfo;


            assertEquals((int)serviceEnabler.getMaxConnections(),(int)50);
            assertEquals(serviceEnabler.getProtocol(),"test");
            assertEquals((long)serviceEnabler.getCurrentCounter().sumCounters(),(long)1);
            assertEquals((long)serviceEnabler.getTotalCounter().sumCounters(),1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // locks serviceenablers, but mas shuld report unlocked disabled.
        omm.getOperateMAS().setAdminState("unlocked");
        omm.getOperateMAS().setMasDisabled();
        //assertTrue(seop.state.equals("Unlocked"));
        assertTrue(seop.state.equals(ServiceEnabler.ServiceEnablerStatus.CLOSE));        // The service enabler is closed
        assertTrue(omm.getStatus().equals("Disabeld"));

    }




    public void testRegisterProvidedService_Neg(){

        ServiceEnablerOperateImplMock seop = new ServiceEnablerOperateImplMock("sename");
        seop.setProtocol("sip");
        
        // Create provided Service entry object
        ProvidedService providedService;

        try {
            // Creates the provided service on the service enabler.
            providedService= omm.createProvidedServiceEntry(null,"loclahost",1111,seop);
            assertTrue(false);
        } catch (IlegalServiceParametersException e) {
            assertTrue(true);
        }

        try {
            // Creates the provided service on the service enabler.
            providedService= omm.createProvidedServiceEntry("Service",null,1111,seop);
            assertTrue(false);
        } catch (IlegalServiceParametersException e) {
            assertTrue(true);
        }

        try {
            // Creates the provided service on the service enabler.
            providedService= omm.createProvidedServiceEntry("Service","loclahost",null,seop);
            assertTrue(false);
        } catch (IlegalServiceParametersException e) {
            assertTrue(true);
        }

        try {
            // Creates the provided service on the service enabler.
            providedService= omm.createProvidedServiceEntry("Service","loclahost",1111,null);
            assertTrue(false);
        } catch (IlegalServiceParametersException e) {
            assertTrue(true);
        }


    }
}




