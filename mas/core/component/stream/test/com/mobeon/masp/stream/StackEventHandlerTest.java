/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import org.jmock.MockObjectTestCase;

/**
 * Testclass for {@link StackEventHandler}.
 * 
 * @author J�rgen Terner
 */
public class StackEventHandlerTest extends MockObjectTestCase {
    /**
     * Creates the test.
     * 
     * @param name Name of this test.
     */
    public StackEventHandlerTest(String name)
    {
       super(name);
    }
    
    /* JavaDoc in base class. */
    public void setUp() {
        try {
            ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
            cm.setConfigFile("cfg/mas_stream.xml");
            StreamConfiguration.getInstance().setInitialConfiguration(
                    cm.getConfiguration());
            StreamConfiguration.getInstance().update();
        }
        catch (Exception e) {
            fail("Failed to initiate the stream factory.");
        }
    }

    /* JavaDoc in base class. */
    public void tearDown() {
    }
    
    /**
     * <pre>
     * Constructor test.
     * 
     * Constructor
     * -----------
     * Action: Call new.
     * Result: No exception is thrown.
     * </pre>
     */
    public void testConstructor() {
        // Wrong arguments
        try {
            new StackEventHandler();
        }
        catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    
    /**
     * <pre>
     * Tests for method getEventNotifier.
     * 
     * Wrong arguments
     * ---------------
     * Action: Call method with null as argument.
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * -----------------
     * Action: Call method with a non-null dispatcher.
     * Result: A non-null reference.
     * </pre>
     */
    public void testGetEventNotifier() {
        // Wrong arguments
        try {
            StackEventHandler.getEventNotifier(null);
            fail("Wrong arguments should cause an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong arguments: " +
                    "Unexpected exception.",
                    IllegalArgumentException.class, e);    
        }
        
        // Correct arguments
        try {
            IEventDispatcher d = (IEventDispatcher)mock(IEventDispatcher.class).proxy();
            assertNotNull("Correct arguments",
                    StackEventHandler.getEventNotifier(d));
        }
        catch (Exception e) {
            fail("Correct arguments: Unexpected exception: " + e);
        }
    }
}
