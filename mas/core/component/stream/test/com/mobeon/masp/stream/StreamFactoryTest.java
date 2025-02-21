/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;

import junit.framework.TestCase;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Testclass for {@link StreamFactoryImpl}.
 * 
 * @author Jörgen Terner
 */
public class StreamFactoryTest extends TestCase {
    protected StreamFactoryImpl mFactory;

    /**
     * Initializes the Stream Factory.
     */
    public void setUp() {
        mFactory = new StreamFactoryImpl();
        try {
            mFactory.setContentTypeMapper(new ContentTypeMapperImpl());
            ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
            cm.setConfigFile("cfg/mas_stream.xml");
            mFactory.setConfiguration(cm.getConfiguration());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Failed to initiate the stream factory.");
        }
    }

    /**
     * <pre>
     * Tests for method getOutboundMediaStream.
     * 
     * Illegal state
     * -------------
     * Action: Call method before calling init
     * Result: IllegalStateException.
     * 
     * Correct state
     * -------------
     * Action: Call method after calling init
     * Result: A non-null reference.
     * </pre>
     */
    public void testGetOutboundMediaStream() {
        // Illegal state
        try {
            mFactory.getOutboundMediaStream();
            fail("IllegalStateException should have been thrown.");
        }
        catch (Exception e) {
            JUnitUtil.assertException(
                    "IllegalState: Unexpected exception",
                    IllegalStateException.class, e);
        }

        // Correct state
        mFactory.init();
        assertNotNull("This method should never return null.",
            mFactory.getOutboundMediaStream());
    }

    /**
     * <pre>
     * Tests for method getInboundMediaStream.
     * 
     * Illegal state
     * -------------
     * Action: Call method before calling init
     * Result: IllegalStateException.
     * 
     * Correct state
     * -------------
     * Action: Call method after calling init
     * Result: A non-null reference.
     * </pre>
     */
    public void testGetInboundMediaStream() {
        // Illegal state
        try {
            mFactory.getInboundMediaStream();
            fail("IllegalStateException should have been thrown.");
        }
        catch (Exception e) {
            JUnitUtil.assertException(
                    "IllegalState: Unexpected exception",
                    IllegalStateException.class, e);
        }

        // Correct state
        mFactory.init();
        assertNotNull("This method should never return null.",
            mFactory.getInboundMediaStream());
    }
 }