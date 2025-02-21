/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import com.mobeon.common.configuration.*;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.profilemanager.pool.DirContextPoolManager;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BaseConfig Tester.
 *
 * @author mande
 * @since <pre>02/22/2006</pre>
 * @version 1.0
 */
public class BaseConfigTest extends ConfigurationBaseTestCase {
    private BaseConfig config;
    private static final boolean LIMIT_SCOPE = true;
    public static final int TRY_LIMIT = 3;
    public static final int TRY_TIME_LIMIT = 500;

    public BaseConfigTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        config = new BaseConfig();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMissingConfiguration() throws Exception {
        try {
            setUpProfileContext(getConfigurationProfileManagerGroupMissing());
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testProfileManagerProperties() throws Exception {
        config.init(getMockConfiguration());
        assertEquals(LIMIT_SCOPE, config.getLimitScope());
        assertEquals(COSCACHE_TIMEOUT, config.getCosCacheTimeout());
    }

    public void testProfileManagerPropertiesParameterTypeException() throws Exception {
        try {
            config.init(getMockConfigurationProfileManagerParameterTypeException());
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testUserRegisterProperties() throws Exception {
        config.init(getMockConfiguration());
        assertEquals(READ_TIMEOUT, config.getReadTimeout());
        assertEquals(WRITE_TIMEOUT, config.getWriteTimeout());
        assertEquals("cn=Directory Manager", config.getAdmin());
        assertEquals("emmanager", config.getPassword());
        assertEquals("o=mobeon.com", config.getDefaultSearchbase());
        assertEquals(TRY_LIMIT, config.getTryLimit());
        assertEquals(TRY_TIME_LIMIT, config.getTryTimeLimit());
    }

    public void testUserRegisterPropertiesUnknownGroupException() throws Exception {
        try {
            config.init(getMockConfigurationUserRegisterUnknownGroupException());
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testConnectionPoolProperties() throws Exception {
        config.init(getMockConfiguration());
        assertEquals(
                "Pool should have configured size",
                config.getPoolMaxSize(),
                DirContextPoolManager.getInstance().getMaxSize()
        );
        assertEquals(
                "Pool should have configured timeout",
                config.getReadTimeout(),
                DirContextPoolManager.getInstance().getTimeoutLimit()
        );
        assertEquals(
                "Pool should have configured lifetime",
                config.getConnectionLifeTime(),
                DirContextPoolManager.getInstance().getForcedReleaseContextLimit()
        );
    }

    public void testAttributeMap() throws Exception {
        config.init(getMockConfiguration());
        List<ProfileLevel> defaultSearchOrder = config.getDefaultSearchOrder();
        assertEquals(
                new ProfileLevel[]{ProfileLevel.COMMUNITY, ProfileLevel.COS, ProfileLevel.BILLING, ProfileLevel.USER},
                defaultSearchOrder.toArray(new ProfileLevel[] {})
        );
        Map<String, ProfileMetaData> applicationAttributeMap = config.getApplicationAttributeMap();
        assertEquals(2, applicationAttributeMap.size());
        Map<String, Set<ProfileMetaData>> userRegisterAttributeMap = config.getUserRegisterAttributeMap();
        assertEquals(1, userRegisterAttributeMap.size());
        assertEquals(2, userRegisterAttributeMap.get("userRegisterName").size());
    }

    public void testAttributeMapUnknownGroupException() throws Exception {
        try {
            config.init(getMockConfigurationAttributeMapUnknownGroupException());
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testProvisioningMap() throws Exception {
        config.init(getMockConfiguration());
        Map<String, String> provisioningMap = config.getProvisioningMap();
        assertEquals(2, provisioningMap.size());
        assertEquals("provisioningName", provisioningMap.get("applicationName"));
        assertEquals("provisioningName", provisioningMap.get("applicationName3"));
    }

    public void testGetPassword() throws Exception {
        config.init(getMockConfiguration());
        assertEquals("secret", config.getUserAdminPassword());
    }

    public void testGetProvisioningUnknownGroupException() throws Exception {
        try {
            config.init(getMockConfigurationProvisioningUnknownGroupException());
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Overridden method without addEventReceiver expectation, since this is not called from BaseConfig
     * @return a mocked EventDispatcher for test purposes
     */
    protected IEventDispatcher getEventDispatcher() {
        if (mockEventDispatcher == null) {
            mockEventDispatcher = mock(IEventDispatcher.class);
        }
        return (IEventDispatcher)mockEventDispatcher.proxy();
    }

    private IConfiguration getMockConfigurationProfileManagerParameterTypeException() {
        Mock mockProfileManagerGroup = mock(IGroup.class, "mockProfileManagerGroup");
        mockProfileManagerGroup.expects(once()).method("getString").with(eq("limitscope"), eq(DEFAULT_LIMIT_SCOPE_STRING)).
                will(returnValue(LIMIT_SCOPE_STRING));
        mockProfileManagerGroup.expects(once()).method("getInteger").with(eq("coscachetimeout"), eq(DEFAULT_COSCACHE_TIMEOUT)).
                will(throwException(getParameterTypeException("integer")));
        Mock mockConfiguration = mock(IConfiguration.class);
        mockConfiguration.expects(once()).method("getGroup").with(eq("profilemanager")).
                will(returnValue(mockProfileManagerGroup.proxy()));
        return (IConfiguration)mockConfiguration.proxy();
    }

    private IConfiguration getMockConfigurationUserRegisterUnknownGroupException() {
        Mock mockProfileManagerGroup = mock(IGroup.class, "mockProfileManagerGroup");
        addProfileManagerGroupAttributes(mockProfileManagerGroup);
        mockProfileManagerGroup.expects(once()).method("getGroup").with(eq("userregister")).
                will(throwException(getUnknownGroupException("userregister")));
        Mock mockConfiguration = mock(IConfiguration.class);
        mockConfiguration.expects(once()).method("getGroup").with(eq("profilemanager")).
                will(returnValue(mockProfileManagerGroup.proxy()));
        return (IConfiguration)mockConfiguration.proxy();
    }

    private IConfiguration getMockConfigurationProvisioningUnknownGroupException() {
        Mock mockProfileManagerGroup = mock(IGroup.class, "mockProfileManagerGroup");
        addProfileManagerGroupAttributes(mockProfileManagerGroup);
        mockProfileManagerGroup.expects(once()).method("getGroup").with(eq("userregister")).
                will(returnValue(getMockUserRegisterGroup()));
        mockProfileManagerGroup.expects(once()).method("getGroup").with(eq("connectionpool")).
                will(returnValue(getMockConnectionPoolGroup()));
        mockProfileManagerGroup.expects(once()).method("getGroup").with(eq("provisioning")).
                will(throwException(getUnknownGroupException("provisioning")));
        Mock mockConfiguration = mock(IConfiguration.class);
        mockConfiguration.expects(once()).method("getGroup").with(eq("profilemanager")).
                will(returnValue(mockProfileManagerGroup.proxy()));
        return (IConfiguration)mockConfiguration.proxy();
    }

    private IConfiguration getMockConfigurationAttributeMapUnknownGroupException() {
        Mock mockProfileManagerGroup = mock(IGroup.class, "mockProfileManagerGroup");
        addProfileManagerGroupAttributes(mockProfileManagerGroup);
        mockProfileManagerGroup.expects(once()).method("getGroup").with(eq("userregister")).
                will(returnValue(getMockUserRegisterGroup()));
        mockProfileManagerGroup.expects(once()).method("getGroup").with(eq("connectionpool")).
                will(returnValue(getMockConnectionPoolGroup()));
        mockProfileManagerGroup.expects(once()).method("getGroup").with(eq("provisioning")).
                will(returnValue(getMockProvisioningGroup()));
        mockProfileManagerGroup.expects(once()).method("getGroup").with(eq("attributemap")).
                will(throwException(getUnknownGroupException("attributemap")));
        Mock mockConfiguration = mock(IConfiguration.class);
        mockConfiguration.expects(once()).method("getGroup").with(eq("profilemanager")).
                will(returnValue(mockProfileManagerGroup.proxy()));
        return (IConfiguration)mockConfiguration.proxy();
    }


    private ParameterTypeException getParameterTypeException(String type) {
        return new ParameterTypeException(type);
    }

    public static Test suite() {
        return new TestSuite(BaseConfigTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
