/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.profilemanager.greetings;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * GreetingManagerFactoryImpl Tester.
 *
 * @author MANDE
 * @since <pre>08/28/2006</pre>
 * @version 1.0
 */
public class GreetingManagerFactoryImplTest extends TestCase {
    public GreetingManagerFactoryImplTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetGreetingManager() throws Exception {
        GreetingManagerFactoryImpl greetingManagerFactory = new GreetingManagerFactoryImpl();
        GreetingManager greetingManager = greetingManagerFactory.getGreetingManager(null, "userid", "folder");
        assertTrue("Factory should create GreetingManagerImpl objects", greetingManager instanceof GreetingManagerImpl);
    }

    public static Test suite() {
        return new TestSuite(GreetingManagerFactoryImplTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
