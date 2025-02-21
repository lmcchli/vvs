/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.externalcomponentregister;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * ExternalComponentRegister Tester.
 *
 * @author mande
 * @version 1.0
 * @since <pre>10/27/2005</pre>
 */
public class ExternalComponentRegisterTest extends TestCase {
    private ILocateService serviceLocator;

    public ExternalComponentRegisterTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("componentservicesconfig", "src/test/java/com/mobeon/common/externalcomponentregister/componentservices.config");
        System.setProperty("componentservicesrefreshperiod", "7000");
        // Initialize service locator
        ExternalComponentRegister externalComponentRegister = ExternalComponentRegister.getInstance();
        this.serviceLocator = externalComponentRegister;
        externalComponentRegister.refreshConfig(); //force a refresh so previous tests do not affect current.
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test locating service which will not be found
     *
     * @throws Exception
     */
    public void testLocateServiceUnknown() throws Exception {
        String serviceName = "unknownService";
        try {
            serviceLocator.locateService(serviceName);
            fail("Expected NoServiceFoundException");
            return;
        } catch (NoServiceFoundException e) {
            assertTrue(true); // For statistical purposes
            return;
        } catch (Throwable t) {
            fail("Expected NoServiceFoundException but got: " + t.getMessage() + " " + t.getStackTrace() );
            return;
        }
    }


    /**
     * Test get service instance which will be found
     *
     * @throws Exception
     */
    public void testGetServiceInstancesFound() throws Exception {
        List<IServiceInstance> serviceInstances1 = serviceLocator.getServiceInstances(IServiceName.MASTER_AGENT);
        assertNotNull(serviceInstances1);
        assertEquals(4, serviceInstances1.size());

        List<IServiceInstance> serviceInstances2 = serviceLocator.getServiceInstances(IServiceName.EVEN_REPORTING);
        assertNotNull(serviceInstances2);
        assertEquals(1, serviceInstances2.size());
        assertTrue(true);
    }

    /**
     * Test get service instance which will be found
     *
     * @throws Exception
     */
    public void testGetServiceInstancesNotFound() throws Exception {
        try {
            serviceLocator.getServiceInstances("unknownService");
            fail("Expected NoServiceFoundException");
        } catch (NoServiceFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }


    /**
     * Test locating service which will be found
     *
     * @throws Exception
     */
    public void testLocateServiceKnown() throws Exception {
        IServiceInstance serviceInstance = serviceLocator.locateService(IServiceName.MASTER_AGENT);
        assertNotNull(serviceInstance);
        assertEquals(IServiceName.MASTER_AGENT, serviceInstance.getServiceName());
        assertEquals("161", serviceInstance.getProperty(IServiceInstance.PORT));
        assertEquals("SNMP", serviceInstance.getProperty(IServiceInstance.PROTOCOL));

        serviceInstance = serviceLocator.locateService(IServiceName.EVEN_REPORTING);
        assertNotNull(serviceInstance);
        assertEquals(IServiceName.EVEN_REPORTING, serviceInstance.getServiceName());
        assertEquals("1813", serviceInstance.getProperty(IServiceInstance.PORT));
        assertEquals("RADIUS-MA", serviceInstance.getProperty(IServiceInstance.PROTOCOL));
    }

    /**
     * Test locating hosted service when service will not be found
     *
     * @throws Exception
     */
    public void testLocateServiceHostUnknownService() throws Exception {
        String serviceName = "unknownService";
        try {
            serviceLocator.locateService(serviceName, "smsc.host1.com");
            fail("Expected NoServiceFoundException");
        } catch (NoServiceFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test locating hosted service when host will not be found
     *
     * @throws Exception
     */
    public void testLocateServiceHostUnknownHost() throws Exception {
        try {
            serviceLocator.locateService(IServiceName.EVEN_REPORTING, "mer.host.unknown.com");
            fail("Expected NoServiceFoundException");
        } catch (NoServiceFoundException e) {
            assertTrue(true); // For statistical purposes
        }
        try {
            serviceLocator.locateService(IServiceName.MASTER_AGENT, "emanate.host..unknown.com");
            fail("Expected NoServiceFoundException");
        } catch (NoServiceFoundException e) {
            assertTrue(true); // For statistical purposes
        }

    }

    /**
     * Test locating hosted service which will be found
     *
     * @throws Exception
     */
    public void testLocateServiceKnownWithHostName() throws Exception {
        IServiceInstance serviceInstance = serviceLocator.locateService(IServiceName.MASTER_AGENT, "emanate.host1.com");
        assertNotNull(serviceInstance);
        assertEquals(IServiceName.MASTER_AGENT, serviceInstance.getServiceName());
        assertEquals("161", serviceInstance.getProperty(IServiceInstance.PORT));
        assertEquals("emanate.host1.com", serviceInstance.getProperty(IServiceInstance.HOSTNAME));

        serviceInstance = serviceLocator.locateService(IServiceName.EVEN_REPORTING, "mer.host1.com");
        assertNotNull(serviceInstance);
        assertEquals(IServiceName.EVEN_REPORTING, serviceInstance.getServiceName());
        assertEquals("1813", serviceInstance.getProperty(IServiceInstance.PORT));
        assertEquals("mer.host1.com", serviceInstance.getProperty(IServiceInstance.HOSTNAME));
    }


    /**
     * Test locating service with component name but not found
     *
     * @throws Exception
     */
    public void testLocateServiceWithComponentNameNotFound() throws Exception {
        try {
            serviceLocator.locateServiceByComponentName(IServiceName.EVEN_REPORTING, "mer@mer.unknown.com");
            fail("Expected NoServiceFoundException");
        } catch (NoServiceFoundException e) {
            assertTrue(true); // For statistical purposes
        }
        try {
            serviceLocator.locateServiceByComponentName(IServiceName.MASTER_AGENT, "emanate@emanate.unknown.com");
            fail("Expected NoServiceFoundException");
        } catch (NoServiceFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test locating service with component name and found
     *
     * @throws Exception
     */
    public void testLocateServiceWithComponentNameFound() throws Exception {
        IServiceInstance serviceInstance = serviceLocator.locateServiceByComponentName(IServiceName.MASTER_AGENT, "emanate@emanate.host1.com");
        assertNotNull(serviceInstance);
        assertEquals(IServiceName.MASTER_AGENT, serviceInstance.getServiceName());
        assertEquals("161", serviceInstance.getProperty(IServiceInstance.PORT));
        assertEquals("emanate.host1.com", serviceInstance.getProperty(IServiceInstance.HOSTNAME));

        serviceInstance = serviceLocator.locateServiceByComponentName(IServiceName.EVEN_REPORTING, "mer@mer.host1.com");
        assertNotNull(serviceInstance);
        assertEquals(IServiceName.EVEN_REPORTING, serviceInstance.getServiceName());
        assertEquals("1813", serviceInstance.getProperty(IServiceInstance.PORT));
        assertEquals("mer.host1.com", serviceInstance.getProperty(IServiceInstance.HOSTNAME));
    }


    /**
     * Test locating another service
     *
     * @throws Exception
     */
    public void testGetAnotherService() throws Exception {
        IServiceInstance serviceInstance;
        IServiceInstance anotherService;

        for (int i = 0; i < 10; i++) {
        	serviceInstance = serviceLocator.locateService(IServiceName.MASTER_AGENT);
        	anotherService = serviceLocator.getAnotherService(serviceInstance);
            assertNotNull("Service instance should not be null", serviceInstance);
            assertNotNull("Service instance should not be null", anotherService);
            assertFalse("Service instances should not be equal", anotherService.equals(serviceInstance));
            assertFalse("Service host name should not be equal", serviceInstance.getProperty(IServiceInstance.HOSTNAME).equals(anotherService.getProperty(IServiceInstance.HOSTNAME)));
            assertEquals(serviceInstance.getServiceName(), anotherService.getServiceName());
        }
    }

    /**
     * Test locating another service when it cannot be found
     *
     * @throws Exception
     */
    public void testGetAnotherServiceNoServiceFoundException() throws Exception {
        IServiceInstance serviceInstance = serviceLocator.locateService(IServiceName.EVEN_REPORTING);
        try {
            serviceLocator.getAnotherService(serviceInstance);
            fail("Expected NoServiceFoundException");
        } catch (NoServiceFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test get service instance which will be found
     *
     * @throws Exception
     */
    public void testLocateServiceInstanceFound() throws Exception {
    	IServiceInstance serviceInstance;
    	String hostname;
    	boolean correct = true;

    	/**
    	 * Must return of the 4 instances configured
    	 */
    	for (int i = 0; i < 10; i++) {
    		serviceInstance = serviceLocator.locateService(IServiceName.MASTER_AGENT);
    		assertNotNull(serviceInstance);
    		hostname = serviceInstance.getProperty(IServiceInstance.HOSTNAME);
    		if (!(hostname.equals("emanate.host1.com") ||
    				hostname.equals("emanate.host2.com") ||
    				hostname.equals("emanate.host3.com") ||
    				hostname.equals("emanate.host4.com"))) {
    			correct = false;
    		}

    		assertTrue(correct);
    	}
    }

    /**
     * Test the refresh of the componentservices configuration file.
     * At the begining, the config file has the service "MasterAgent", after 7 seconds, it reload another config file
     * in which that service doesn't exist anymore.
     * @throws Exception
     */
    public void testRefreshComponentServicesConfig() throws Exception {

    	System.setProperty("componentservicesconfig", "src/test/java/com/mobeon/common/externalcomponentregister/componentservices_refresh.config");

    	try {
    		Thread.sleep(7500);
    		serviceLocator.locateService(IServiceName.MASTER_AGENT);
    		assertTrue(false);
    	}
    	catch (InterruptedException e) {
    		assertTrue(false);
    	}
    	catch (NoServiceFoundException e) {
    		assertTrue(true);
    	} finally {
    	    //force it back to standard configuration so that it does not effect subsequent tests.
    	    System.setProperty("componentservicesconfig", "src/test/java/com/mobeon/common/externalcomponentregister/componentservices.config");
    	    ((ExternalComponentRegister) serviceLocator).refreshConfig();
    	}
    }

    public static Test suite() {
        return new TestSuite(ExternalComponentRegisterTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
