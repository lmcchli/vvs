/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.Restrictions;

/**
 * Test the trafficEventSend function in PlatformAccess.
 *
 * @author ermmaha
 */
public class PlatformAccessTrafficEventTest extends PlatformAccessTest {

    public PlatformAccessTrafficEventTest(String name) {
        super(name);
    }

    /**
     * Tests the trafficEventSend function.
     *
     * @throws Exception if test case fails.
     */
    public void testTrafficEventSend() throws Exception {
        jmockTrafficEventSender.expects(once()).method("reportTrafficEvent").with(isA(TrafficEvent.class), eq(null));
        platformAccess.trafficEventSend("name", new String[]{"prop1", "prop2"}, new String[]{"value1", "value2"}, false);

        //test with a restriction
        jmockTrafficEventSender.expects(once()).method("reportTrafficEvent").with(isA(TrafficEvent.class), isA(Restrictions.class));
        platformAccess.trafficEventSend("name", new String[]{"prop1", "prop2"}, new String[]{"value1", "value2"}, true);

        //test invalid arguments
        try {
            platformAccess.trafficEventSend("name", new String[]{"prop1", "prop2"}, new String[]{"value1"}, false);
            fail("Expected PlatformAccessException");
        } catch(PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR , e.getMessage());
            assertTrue(e.getDescription().indexOf("trafficEventSend") > -1);
        }

        //test exception
        try {
            jmockTrafficEventSender.expects(once()).method("reportTrafficEvent").
                    will(throwException(new TrafficEventSenderException("Invalid eventtype")));
            platformAccess.trafficEventSend("name", new String[]{"prop1", "prop2"}, new String[]{"value1", "value2"}, false);
            fail("Expected PlatformAccessException");
        } catch(PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR , e.getMessage());
            assertTrue(e.getDescription().indexOf("trafficEventSend") > -1);
        }
    }

    public static Test suite() {
        return new TestSuite(PlatformAccessTrafficEventTest.class);
    }
}
