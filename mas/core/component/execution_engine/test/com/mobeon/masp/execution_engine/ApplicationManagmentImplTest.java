/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import junit.framework.*;
import org.jmock.*;

import com.mobeon.masp.execution_engine.compiler.ApplicationCompilerImpl;
import com.mobeon.masp.execution_engine.compiler.IApplicationCompiler;
import com.mobeon.masp.execution_engine.components.ApplicationCompilerComponent;
import com.mobeon.common.configuration.GroupCardinalityException;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.operateandmaintainmanager.Supervision;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;

/**
 * @author Mikael Andersson
 */
public class ApplicationManagmentImplTest extends Case {
    Mock applicationCompilerMock;
    IApplicationCompiler applicationCompiler;
    private Mock configurationManagerMock;
    private IConfigurationManager configurationManager;
    private Mock groupMock = new Mock(IGroup.class);
    private int xmpServicePort = 8080;
    private String sipServiceName = "Default";
    private int sipServicePort = 5060;

    public ApplicationManagmentImplTest(String event) {
        super(event);
    }

    /**
     * Verifies that configuring a new service mapping works with valid
     * data, and that we invalid data wont't be entered into the mapping.
     *
     * @throws Exception
     */
    public void testSetMapServiceToApplicationURI() throws Exception {
        String test = "http://localhost/test.ccxml";


        ApplicationManagmentImpl ami = new ApplicationManagmentImpl();
        {/* Scope block */
            HashMap<Integer, URL> newMap = new HashMap<Integer, URL>();
            newMap.put(1, new URL(test));
            ami.setMapServiceToApplicationURI(newMap);
        }
        Map serviceMap = ami.getServiceMap();
        String url = (String) serviceMap.get("1");
        if (url == null)
            die("Testing URL missing/not found in service map");
        if (!test.equals(url))
            die("Resulting url does not equal input url: " + test + " != " + url);

        String key = "inva+lid";
        trySetMapping(key, "file:/test.xml", ami);

        if (ami.getServiceMap().containsKey(key))
            die("Invalid key \"" + key + "\" passed validation");

        String value = "fajl://";
        trySetMapping( "valid", value, ami);
        if (ami.getServiceMap().containsValue(value))
            die("Invalid key \"" + value + "\" passed validation");


    }

    /**
     * Validates that loading a known good application succeds.
     * @throws Exception
     */
 /*   public void testLoad() throws Exception {
        String test = "test:/applications/default.xml";
        ApplicationManagmentImpl ami = setupApplicationExecution(new URI(test));
        IApplicationExecution iae = ami.load("default");
        if (iae == null)
            die("ApplicationImpl execution returned null !");
    }*/
    
    
    public void testLoadJavaApp() throws Exception {
        String test = "com.mobeon.masp.application.sipmwi.SubscribeMessageInfoApplication";
        ApplicationManagmentImpl ami = setupApplicationExecution(new URI(test));
        IApplicationExecution iae = ami.load("default");
        if (iae == null)
            die("ApplicationImpl execution returned null !");
    }

    /**
     * Validates that given an URI pointing to a known good
     * application, we get a non-null IApplicationExecution instance
     *
     * @throws Exception
     */
    public void testGetAppFromURI() throws Exception {
        String test = "test:/applications/default.xml";

        URI testURI = new URI(test);
        ApplicationManagmentImpl ae = setupApplicationExecution(testURI);
        IApplicationExecution iae = ae.getAppFromURI(testURI, "default");
        if (iae == null)
            die("getAppFromURI should not return null for a valid file");
    }

    /**
     * Validates that the constructor doesn't set fields that should
     * be dependency injected, and that fields that should have a
     * default configuration, has one.
     *
     * @throws Exception
     */
    public void testApplicationManagmentImpl() throws Exception {
        ApplicationManagmentImpl ami = new ApplicationManagmentImpl();
        Map<String, String> m = ami.getServiceMap();
        if (m == null)
            die("ServiceMap shold never be null");
        if (m.size() == 0)
            die("Initial service map should not be empty");
        if (ami.getParameterBlock() == null)
            die("ParameterBlock should never be null");
        if (ami.getContext().getApplicationExecutionFactory() != null)
            die("ApplicationExecutionFactory not null and spring is not running, this seems wrong !");
        if (ami.getContext().getApplicationCompiler() != null)
            die("ApplicationCompilerImpl not null and spring is not running, this seems wrong !");
    }

    private void setupApplicationCompiler() {
        applicationCompilerMock = new Mock(ApplicationCompilerComponent.class);
        applicationCompiler = (IApplicationCompiler) applicationCompilerMock.proxy();
    }

    private void setupConfigurationManager() {
    	
    	

        groupMock.stubs().method("getString").with(eq("generateops")).will(returnValue("false"));
        groupMock.stubs().method("getString").with(eq("alwayscompile")).will(returnValue("false"));
        groupMock.stubs().method("getInteger").with(eq("watchdogtimeout")).will(returnValue(30000));
        groupMock.stubs().method("getString").with(eq("applicationWorkingDir"),eq("/opt/moip/mas/")).will(returnValue("/opt/moip/mas/"));



        Mock configurationMock = new Mock(IConfiguration.class);
        configurationMock.stubs().method("getGroup").will(returnValue(groupMock.proxy()));
        configurationMock.stubs().method("getConfiguration").will(returnValue(configurationMock.proxy()));

        configurationManagerMock = new Mock(IConfigurationManager.class);
        configurationManagerMock.stubs().method("getConfiguration").will(returnValue(configurationMock.proxy()));
        configurationManager = (IConfigurationManager) configurationManagerMock.proxy();
    }

    private ApplicationManagmentImpl setupApplicationExecution(URI testURI) throws MalformedURLException {
        HashMap<String, URI> serviceMap = new HashMap<String, URI>();
        serviceMap.put("default", testURI);
        setupApplicationCompiler();
        setupConfigurationManager();

        ApplicationManagmentImpl ami = new ApplicationManagmentImpl();

        ApplicationExecutionFactory applicationExecutionFactory = new ApplicationExecutionFactoryImpl();
        ami.setApplicationExecutionFactory(applicationExecutionFactory);

        ami.setMapServiceToApplicationURI(serviceMap);

        ami.setApplicationCompiler(applicationCompiler);
        ami.setConfigurationManager(configurationManager);

        Mock callManagerMock = new Mock(CallManager.class);
        callManagerMock.stubs().method("setApplicationManagment");
        callManagerMock.stubs().method("setSupervision");

        ami.setCallManager((CallManager)callManagerMock.proxy());

        Map<String, ServiceEnabler> protocolMap = new HashMap<String, ServiceEnabler>();
        Mock serviceEnablerMock = new Mock(ServiceEnabler.class);
        serviceEnablerMock.stubs().method("initService");
        protocolMap.put("xmp", (ServiceEnabler)serviceEnablerMock.proxy());
        protocolMap.put("sip", (ServiceEnabler)serviceEnablerMock.proxy());
        ami.setMapProtocolToServiceEnabler(protocolMap);

        Map<String, ArrayList<String>> mapProtocolToService = new HashMap<String, ArrayList<String>>();
        ArrayList<String> xmpServices = new ArrayList<String>();
        xmpServices.add(IServiceName.OUT_DIAL_NOTIFICATION+":"+xmpServicePort);
        ArrayList<String> sipServices = new ArrayList<String>();

        sipServices.add(sipServiceName +":"+sipServicePort);
        mapProtocolToService.put("xmp", xmpServices);
        mapProtocolToService.put("sip", sipServices);

        Mock superVisionMock = new Mock(Supervision.class);
        superVisionMock.stubs().method("createProvidedServiceEntry");
        ami.setSupervision((Supervision)superVisionMock.proxy());

        ami.setMapProtocolToService(mapProtocolToService);
        try {
			ami.setConfiguration(configurationManager.getConfiguration());
		} catch (GroupCardinalityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownGroupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        ApplicationCompilerImpl realApplicationCompiler = new ApplicationCompilerImpl();
        realApplicationCompiler.setConfigurationManager(configurationManager);

      /*  applicationCompilerMock.expects(once()).
                method("compileApplication").with(eq(testURI)).
                will(returnValue(realApplicationCompiler.compileApplication(testURI)));*/
        ami.clearApplications();
        return ami;
    }

    /**
     * Verify that if the hostname config param is "xmp:host1;sip:host2", the ServiceEnablers willl be initialized accordingly
     * @throws Exception
     */
    public void testServiceEnablerInit1() throws Exception{
        String test = "test:/applications/default.xml";
        ApplicationManagmentImpl ami = setupApplicationExecution(new URI(test));
        stubHostName("xmp:host1;sip:host2");

        Map<String, ServiceEnabler> protocolMap = new HashMap<String, ServiceEnabler>();

        Mock xmpServiceEnablerMock = new Mock(ServiceEnabler.class);
        expectInitService(xmpServiceEnablerMock, IServiceName.OUT_DIAL_NOTIFICATION, xmpServicePort, "host1");
        protocolMap.put("xmp", (ServiceEnabler)xmpServiceEnablerMock.proxy());

        Mock sipServiceEnablerMock = new Mock(ServiceEnabler.class);
        expectInitService(sipServiceEnablerMock, sipServiceName, sipServicePort, "host2");
        protocolMap.put("sip", (ServiceEnabler)sipServiceEnablerMock.proxy());

        ami.setMapProtocolToServiceEnabler(protocolMap);

        ami.init();
    }

    /**
     * Verify that if the hostname config param is "localhost", the ServiceEnablers willl be initialized accordingly
     * @throws Exception
     */
    public void testServiceEnablerInit2() throws Exception{
        String test = "test:/applications/default.xml";
        ApplicationManagmentImpl ami = setupApplicationExecution(new URI(test));
        stubHostName("localhost");

        Map<String, ServiceEnabler> protocolMap = new HashMap<String, ServiceEnabler>();

        Mock xmpServiceEnablerMock = new Mock(ServiceEnabler.class);
        expectInitService(xmpServiceEnablerMock, IServiceName.OUT_DIAL_NOTIFICATION, xmpServicePort, "localhost");
        protocolMap.put("xmp", (ServiceEnabler)xmpServiceEnablerMock.proxy());

        Mock sipServiceEnablerMock = new Mock(ServiceEnabler.class);
        expectInitService(sipServiceEnablerMock, sipServiceName, sipServicePort, "localhost");
        protocolMap.put("sip", (ServiceEnabler)sipServiceEnablerMock.proxy());

        ami.setMapProtocolToServiceEnabler(protocolMap);

        ami.init();
    }

    private void stubHostName(String hostName) {
        groupMock.stubs().method("getString").with(eq("hostname")).will(returnValue(hostName));
    }

    private void expectInitService(Mock serviceEnablerMock, String serviceName, int servicePort, String hostName) {
        serviceEnablerMock.expects(once()).method("initService").with(eq(serviceName), eq(hostName), eq(servicePort));
    }

    private void trySetMapping(String key, String value, ApplicationManagmentImpl ami) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put(key, value);
        ami.setMapServiceToApplicationURI(hashMap);
    }

  /*  public static Test suite() {
        return new TestSuite(ApplicationManagmentImplTest.class);
    }*/
}