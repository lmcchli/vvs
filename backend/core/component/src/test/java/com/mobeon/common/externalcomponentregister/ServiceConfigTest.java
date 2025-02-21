/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.externalcomponentregister;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class ServiceConfigTest extends TestCase {
    private ServiceConfig serviceConfig;

    public ServiceConfigTest() {
        super();
    }

    public void setUp() throws Exception {
        super.setUp();
		System.setProperty("componentservicesconfig", "src/test/java/com/mobeon/common/externalcomponentregister/componentservices.config");
        serviceConfig = new ServiceConfig();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        serviceConfig = null;
    }

    /**
     * Test getting unknown property
     *
     * @throws Exception
     */
    public void testReadConfiguration() throws Exception {
    	try {
    		assertTrue((serviceConfig.getServiceInstanceList().size() > 0));
    	}
        catch (Exception e) {
        	e.printStackTrace(System.out);
        	assertTrue(false);
        }
    }

    public static Test suite() {
        return new TestSuite(ServiceConfigTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
