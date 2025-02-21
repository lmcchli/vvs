/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.externalcomponentregister;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashSet;
import java.util.Set;

/**
 * ServiceInstanceImpl Tester.
 *
 * @author mande
 * @version 1.0
 * @since <pre>10/27/2005</pre>
 */
public class ServiceInstanceTest extends TestCase {
    private IServiceInstance serviceInstance;

    public ServiceInstanceTest() {
        super();
    }

    public void setUp() throws Exception {
        super.setUp();
        serviceInstance = new ServiceInstanceImpl("serviceName");
    }

    public void tearDown() throws Exception {
        super.tearDown();
        serviceInstance = null;
    }

    /**
     * Test getting unknown property
     *
     * @throws Exception
     */
    public void testGetUnknownProperty() throws Exception {
        // Test getting unknown property
        String unknownProperty = "unknownProperty";
        String value = serviceInstance.getProperty(unknownProperty);
        assertEquals(null, value);
    }

    /**
     * Test getting known property
     *
     * @throws Exception
     */
    public void testGetProperty() throws Exception {
        // Test getting known property
        String knownProperty = "knownProperty";
        String value = "value";
        serviceInstance.setProperty(knownProperty, value);
        assertEquals(value, serviceInstance.getProperty(knownProperty));
    }

    /**
     * Test setting property
     *
     * @throws Exception
     */
    public void testSetProperty() throws Exception {
        // Test getting known property
        String knownProperty = "knownProperty";
        String value = "value";
        serviceInstance.setProperty(knownProperty, value);
        assertEquals(value, serviceInstance.getProperty(knownProperty));
    }

    public void testGetSetServiceName() throws Exception {
        serviceInstance.setServiceName("serviceName");
        assertEquals("serviceName", serviceInstance.getServiceName());
    }

    public void testEqualsAndHashCode() throws Exception {
        IServiceInstance serviceInstance1 = new ServiceInstanceImpl("serviceName");
        IServiceInstance serviceInstance2 = new ServiceInstanceImpl("serviceName");
        IServiceInstance serviceInstance3 = new ServiceInstanceImpl("serviceName");

        String serviceName = "serviceName";
        serviceInstance1.setServiceName(serviceName);
        serviceInstance2.setServiceName(serviceName);
        serviceInstance3.setServiceName("anotherServiceName");
        String knownProperty = "knownProperty";
        String value = "value";
        serviceInstance1.setProperty(knownProperty, value);
        serviceInstance2.setProperty(knownProperty, value);
        serviceInstance3.setProperty(knownProperty, "anotherValue");
        assertEquals(serviceInstance1, serviceInstance2);
        assertEquals(serviceInstance1.hashCode(), serviceInstance2.hashCode());
        assertFalse(serviceInstance1.equals(serviceInstance3));
        assertFalse(serviceInstance1.hashCode() == serviceInstance3.hashCode());
    }

    public void testToString() throws Exception {
        serviceInstance.setServiceName("service");
        serviceInstance.setProperty(IServiceInstance.HOSTNAME, "hostname");
        serviceInstance.setProperty(IServiceInstance.PORT, "port");
        assertEquals("hostname", serviceInstance.getProperty(IServiceInstance.HOSTNAME));
        assertEquals("port", serviceInstance.getProperty(IServiceInstance.PORT));
    }

    public static Test suite() {
        return new TestSuite(ServiceInstanceTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
