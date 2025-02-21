/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.common.configuration.UnknownParameterException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

/**
 * Test the systemGetConfigurationParameter function in PlatformAccess.
 * <p/>
 * Date: 2005-okt-20
 *
 * @author ermmaha
 */
public class PlatformAccessConfigurationTest extends PlatformAccessTest {

    protected Mock jmockGroup;

    public PlatformAccessConfigurationTest(String name) {
        super(name);
        jmockGroup = mock(IGroup.class);
        jmockGroup.stubs().method("getFullName").will(returnValue("vva.conf"));
        jmockGroup.stubs().method("getString").with(eq("incomingcallparam1")).will(returnValue("value1"));
        jmockGroup.stubs().method("getString").with(eq("incomingcallwrongparam")).will(throwException(new UnknownParameterException("no such param", (IGroup) jmockGroup.proxy())));
    }

    public static Test suite() {
        return new TestSuite(PlatformAccessConfigurationTest.class);
    }

    /**
     * Tests the systemGetConfigurationParameter function.
     *
     * @throws Exception if test case fails.
     */
    public void testSystemGetConfigurationParameter() throws Exception {
        jmockConfiguration.expects(once()).method("getGroup").with(eq("vva.conf")).will(returnValue((jmockGroup.proxy())));
        jmockGroup.expects(once()).method("getString").with(eq("incomingcallparam1")).will(returnValue("value1"));
        String param = platformAccess.systemGetConfigurationParameter("vva.incomingcall", "param1");
        assertEquals("value1", param);

        // test exceptions from an invalid group call
        jmockConfiguration.expects(once()).method("getGroup").with(eq("wrongvva.conf")).will(throwException(new UnknownGroupException("no such group", null)));
        try {
            platformAccess.systemGetConfigurationParameter("wrongvva.incomingcall", "name1");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
        }

        // test exceptions from an invalid param call
        jmockConfiguration.expects(once()).method("getGroup").with(eq("vva.conf")).will(returnValue((jmockGroup.proxy())));
        jmockGroup.expects(once()).method("getString").with(eq("incomingcallwrongparam")).will(throwException(new UnknownParameterException("no such param", (IGroup) jmockGroup.proxy())));
        try {
            platformAccess.systemGetConfigurationParameter("vva.incomingcall", "wrongparam");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
        }
    }
}
