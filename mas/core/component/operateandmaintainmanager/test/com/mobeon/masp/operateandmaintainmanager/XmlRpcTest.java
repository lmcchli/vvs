package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.configuration.*;
import com.mobeon.masp.execution_engine.events.MASStarted;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.OMMConfiguration;
import com.mobeon.masp.rpcclient.Command;
import com.mobeon.masp.rpcclient.RpcClient;
import com.mobeon.masp.rpcclient.XmlRpcEncode;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class XmlRpcTest extends TestCase {
    ILogger log;
    OMManager omm = new OMManager();
    IConfiguration config;

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch("logmanager.xml");
        log = ILoggerFactory.getILogger(ProvidedServiceTest.class);

        MulticastDispatcherMock eventDispatcher = new MulticastDispatcherMock();
        ConfigurationManagerImpl configuration = new ConfigurationManagerImpl();
        configuration.setConfigFile("mas.xml");
        config = configuration.getConfiguration();
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

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test that close/open and XMLRpc.
     * This test uses XML Rpc connection.
     */
    public void testRpcCommand() {

        MulticastDispatcherMock eventDispatcher = new MulticastDispatcherMock();

        // Create a new service enabler
        // ServiceEnablerOperateImplMock implements the ServiceEnabler interface
        ServiceEnablerOperateImplMock seop = new ServiceEnablerOperateImplMock("sename");
        seop.setProtocol("sip");
        seop.setSupervision(omm);

        ServiceEnabler serviceEnabler = null;
        try {
            serviceEnabler = (ServiceEnabler) omm.getServiceEnablerStatistics(seop);
            seop.setServiceInfo(serviceEnabler);
            //serviceEnabler.init();
        } catch (Exception e) {
            assertTrue(false);
        }

        // Create provided Service entry object
        ProvidedService providedService;
        try {
            // Creates the provided service on the service enabler.
            providedService = omm.createProvidedServiceEntry("Service", "loclahost", 1111, seop);

            // set values on the provided service entry object.
            providedService.setApplication("appname", "appversion");
            providedService.setStatus(Status.UNKNOWN);
        } catch (IlegalServiceParametersException e) {
            e.printStackTrace();
        }

        eventDispatcher.fireGlobalEvent(new MASStarted());

        Command rpcCmd = new Command(config);

        rpcCmd.status();

        rpcCmd.lock();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(omm.getServiceEnablers().get(seop.toString()).getServiceEnablerStatus(), ServiceEnabler.ServiceEnablerStatus.CLOSE);// "locked");

        rpcCmd.unlock();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(omm.getServiceEnablers().get(seop.toString()).getServiceEnablerStatus(), ServiceEnabler.ServiceEnablerStatus.OPEN); // "unlocked");

        rpcCmd.shutdown();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(omm.getServiceEnablers().get(seop.toString()).getServiceEnablerStatus(), ServiceEnabler.ServiceEnablerStatus.CLOSE); //"locked");
    }

    public void testRpcClient() throws ParameterTypeException {

        // Create a new service enabler
        // ServiceEnablerOperateImplMock implements the ServiceEnabler interface
        ServiceEnablerOperateImplMock seop = new ServiceEnablerOperateImplMock("sename");
        seop.setProtocol("sip");

        // get the ServiceEnablerstatistics object from OMM and set to ServiceEnabler.
        ServiceEnabler serviceEnabler = null;
        try {
            serviceEnabler = (ServiceEnabler) omm.getServiceEnablerStatistics(seop);
            seop.setServiceInfo(serviceEnabler);
        } catch (Exception e) {
            assertTrue(false);
        }

        // Create provided Service entry object
        ProvidedService providedService;
        try {
            // Creates the provided service on the service enabler.
            providedService = omm.createProvidedServiceEntry("Service", "loclahost", 1111, seop);

            // set values on the provided service entry object.
            providedService.setApplication("appname", "appversion");
            providedService.setStatus(Status.UNKNOWN);
        } catch (IlegalServiceParametersException e) {
            e.printStackTrace();
        }

        IGroup mainGroup = null;
        try {
            mainGroup = config.getGroup("operateandmaintainmanager.omm");
        } catch (GroupCardinalityException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnknownGroupException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        String hostName = mainGroup.getString(OMMConfiguration.HOSTNAME, "localhost");
        Integer rpcPort = mainGroup.getInteger(OMMConfiguration.PORT, 8081);

        RpcClient rpcClient = new RpcClient(hostName, rpcPort.toString());
        try {
            // This test is onlu made to check that no exeception is thrown
            rpcClient.getMonitorConnectionData();
            rpcClient.getMibAttributes();
            rpcClient.getMonitorStatisticData();
            rpcClient.sendCommand("reloadConfiguration", "");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void testUpdateServiceStatus() {

        MulticastDispatcherMock eventDispatcher = new MulticastDispatcherMock();

        // Get configuration for rpc client
        IGroup mainGroup = null;
        try {
            mainGroup = config.getGroup("operateandmaintainmanager.omm");
        } catch (GroupCardinalityException e) {
            e.printStackTrace();
        } catch (UnknownGroupException e) {
            e.printStackTrace();
        }

        // setup rpc client
        RpcClient rpcClient = null;
        Integer rpcPort = null;
        try {
            assert mainGroup != null;
            String hostName = mainGroup.getString("hostname", "localhost");
            rpcPort = mainGroup.getInteger("port", 8081);
            rpcClient = new RpcClient(hostName, rpcPort.toString());
        } catch (ParameterTypeException e) {
            e.printStackTrace();
        }

        // Create a new service enabler
        // ServiceEnablerOperateImplMock implements the ServiceEnabler interface
        ServiceEnablerOperateImplMock seop = new ServiceEnablerOperateImplMock("sename");
        seop.setProtocol("sip");
        seop.setHost("loclahost");
        seop.setPort("1111");
        seop.setSupervision(omm);

        // get the ServiceEnablerstatistics object from OMM and set to ServiceEnabler.
        ServiceEnabler serviceEnabler = null;
        try {
            serviceEnabler = (ServiceEnabler) omm.getServiceEnablerStatistics(seop);
            seop.setServiceInfo(serviceEnabler);
        } catch (Exception e) {
            assertTrue(false);
        }

        // Create provided Service entry object
        ProvidedService providedService = null;
        try {
            // Creates the provided service on the service enabler.
            providedService = omm.createProvidedServiceEntry("Service", "loclahost", 1111, seop);

            // set values on the provided service entry object.
            providedService.setApplication("appname", "appversion");
            providedService.setStatus(Status.UNKNOWN);
        } catch (IlegalServiceParametersException e) {
            e.printStackTrace();
        }

        // create a status object to be sent via rpc.
        Vector<Serializable> param = new Vector<Serializable>(2);
        ProvidedServiceEntry p = (ProvidedServiceEntry) providedService;
        assert p != null;
        param.add(p.getServiceName() + "-" + p.getHost() + "-" + p.getPort());
        param.add(XmlRpcEncode.encode(Status.IMPAIRED));
        try {
            assert rpcClient != null;
            rpcClient.sendCommand("updateServiceStatus", param);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // check if status has been set by OM.
        ProvidedServiceEntry pse = (ProvidedServiceEntry) omm.getServiceEnablers().get(seop.toString()).getProvidedServices().get("Service-loclahost-1111");
        //ProvidedServiceEntry pse = (ProvidedServiceEntry)omm.getServiceEnablers().get(seop.toString()).getProvidedServices().get("Service");
        assertEquals(Status.IMPAIRED, pse.getStatus());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
