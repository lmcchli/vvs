/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.profilemanager.ldap.MockLdapCtxFactory;
import com.mobeon.masp.profilemanager.pool.DirContextPoolManager;
import com.mobeon.masp.profilemanager.pool.DirContextPool;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.ConfigurationChanged;
import com.mobeon.masp.util.javamail.StoreManager;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.Hashtable;

import org.jmock.Mock;

/**
 * BaseContext Tester.
 *
 * @author mande
 * @since <pre>03/28/2006</pre>
 * @version 1.0
 */
public class BaseContextTest extends ConfigurationBaseTestCase {
    private static final String PROFILE_MANAGER_CFG = "test/com/mobeon/masp/profilemanager/profilemanager.xml";
    private static final String TIMEOUT_KEY = "com.sun.jndi.ldap.connect.timeout";
    private static final String READ_TIMEOUT = "2000";
    private static final String WRITE_TIMEOUT = "3000";
    private static final String ADMIN = "cn=directory manager";
    private static final String PASSWORD = "emmanager";
    private static final String READ_HOSTNAME = "readhostname";
    private static final String READ_PORT = "1234";
    private static final String WRITE_HOSTNAME = "writehostname";
    private static final String WRITE_PORT = "5678";
    private Mock mockServiceInstance;
    private IConfiguration configuration;
    private String readProviderUrl;

    public BaseContextTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        setUpDirContext();
        configuration = getConfiguration(PROFILE_MANAGER_CFG);
        setUpProfileContext(configuration);
        readProviderUrl = "ldap://" + READ_HOSTNAME + ":" + READ_PORT;
        DirContextPoolManager.getInstance().removeDirContextPool(readProviderUrl);
    }

    private void setUpGetDirContextRead() {
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER)).
                will(returnValue(getServiceInstance()));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.HOSTNAME)).
                will(returnValue(READ_HOSTNAME));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.PORT)).
                will(returnValue(READ_PORT));
    }

    private void setUpGetDirContextWrite() {
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER_WRITE)).
                will(returnValue(getServiceInstance()));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.HOSTNAME)).
                will(returnValue(WRITE_HOSTNAME));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.PORT)).
                will(returnValue(WRITE_PORT));
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetConfiguration() throws Exception {
        assertEquals("Configuration should be same", configuration, baseContext.getConfiguration());
    }

    public void testMissingConfiguration() throws Exception {
        try {
            setUpProfileContext(getConfigurationProfileManagerGroupMissing());
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testConfigurationHasChanged() throws Exception {
        IConfiguration oldConfiguration = baseContext.getConfiguration();
        BaseConfig oldConfig = baseContext.getConfig();
        StoreManager oldStoreManager = baseContext.getStoreManager();
        IConfiguration newConfiguration = getMockConfiguration();
        ConfigurationChanged event = new ConfigurationChanged(newConfiguration);
        // A local event should not be handled
        baseContext.doEvent(event);
        assertSame("IConfiguration should not have changed", baseContext.getConfiguration(), oldConfiguration);
        assertNotSame("IConfiguration should not be from event", newConfiguration, baseContext.getConfiguration());
        assertSame("BaseConfig should not have changed", baseContext.getConfig(), oldConfig);
        assertSame("StoreManager should not have changed", baseContext.getStoreManager(), oldStoreManager);

        // A global event should be handled
        baseContext.doGlobalEvent(event);
        assertNotSame("IConfiguration should have changed", baseContext.getConfiguration(), oldConfiguration);
        assertSame("IConfiguration should be from event", newConfiguration, baseContext.getConfiguration());
        assertNotSame("BaseConfig should have changed", baseContext.getConfig(), oldConfig);
        assertNotSame("StoreManager should have changed", baseContext.getStoreManager(), oldStoreManager);
    }

    public void testConfigurationHasChangedConfigurationException() throws Exception {
        IConfiguration oldConfiguration = baseContext.getConfiguration();
        BaseConfig oldConfig = baseContext.getConfig();
        StoreManager oldStoreManager = baseContext.getStoreManager();
        IConfiguration newConfiguration = getConfigurationProfileManagerGroupMissing();
        baseContext.doGlobalEvent(new ConfigurationChanged(newConfiguration));
        assertSame("IConfiguration should not have changed", baseContext.getConfiguration(), oldConfiguration);
        assertNotSame("IConfiguration should not be from event", newConfiguration, baseContext.getConfiguration());
        assertSame("BaseConfig should not have changed", baseContext.getConfig(), oldConfig);
        assertSame("StoreManager should not have changed", baseContext.getStoreManager(), oldStoreManager);
    }

    public void testGetDirContextRead() throws Exception {
        setUpGetDirContextRead();
        DirContextPool pool = DirContextPoolManager.getInstance().getDirContextPool(readProviderUrl);
        DirContext dirContext = baseContext.getDirContext(getLdapServiceInstanceDecorator(Direction.READ), Direction.READ);
        assertEquals("Pool size should be 1", 1, pool.getSize());
        Hashtable<?, ?> environment = MockLdapCtxFactory.getEnvironment();
        assertEquals(READ_TIMEOUT, (String)environment.get(TIMEOUT_KEY));
        assertEquals(readProviderUrl, environment.get(Context.PROVIDER_URL));
        assertEquals(ADMIN, (String)environment.get(Context.SECURITY_PRINCIPAL));
        assertEquals(PASSWORD, (String)environment.get(Context.SECURITY_CREDENTIALS));
        baseContext.returnDirContext(dirContext);
        assertEquals("Pool size should be 1", 1, pool.getSize());
        setUpGetDirContextRead();
        dirContext = baseContext.getDirContext(getLdapServiceInstanceDecorator(Direction.READ), Direction.READ);
        mockDirContext.expects(once()).method("close");
        baseContext.returnDirContext(dirContext, true);
        assertEquals("Pool size should be 0", 0, pool.getSize());
    }

    public void testGetDirContextNamingException() throws Exception {
        setUpGetDirContextRead();
        MockLdapCtxFactory.throwInitialException(getNamingException());
        try {
            baseContext.getDirContext(getLdapServiceInstanceDecorator(Direction.READ), Direction.READ);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue(true); // For statistical purposes
        }
        // Register MockLdapCtxFactory as Verifiable object
        MockLdapCtxFactory.registerToVerify(this);
    }

    public void testGetDirContextWrite() throws Exception {
        setUpGetDirContextWrite();
        String providerUrl = "ldap://" + WRITE_HOSTNAME + ":" + WRITE_PORT;
        DirContextPool pool = DirContextPoolManager.getInstance().getDirContextPool(providerUrl);
        DirContext dirContext = baseContext.getDirContext(getLdapServiceInstanceDecorator(Direction.WRITE), Direction.WRITE);
        assertEquals("Pool size should be 1", 1, pool.getSize());
        Hashtable<?, ?> environment = MockLdapCtxFactory.getEnvironment();
        assertEquals(WRITE_TIMEOUT, (String)environment.get(TIMEOUT_KEY));
        assertEquals(providerUrl, environment.get(Context.PROVIDER_URL));
        assertEquals(ADMIN, (String)environment.get(Context.SECURITY_PRINCIPAL));
        assertEquals(PASSWORD, (String)environment.get(Context.SECURITY_CREDENTIALS));
        baseContext.returnDirContext(dirContext);
        assertEquals("Pool size should be 1", 1, pool.getSize());
        setUpGetDirContextWrite();
        dirContext = baseContext.getDirContext(getLdapServiceInstanceDecorator(Direction.WRITE), Direction.WRITE);
        mockDirContext.expects(once()).method("close");
        baseContext.returnDirContext(dirContext, true);
        assertEquals("Pool size should be 0", 0, pool.getSize());
    }

    public void testGetServiceInstanceNoServiceFoundException() throws Exception {
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.USER_REGISTER)).
                will(throwException(new NoServiceFoundException(IServiceName.USER_REGISTER)));
        try {
            baseContext.getServiceInstance(Direction.READ);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testCloseDirContextNamingException() throws Exception {
        setUpGetDirContextRead();
        DirContext dirContext = baseContext.getDirContext(getLdapServiceInstanceDecorator(Direction.READ), Direction.READ);
        mockDirContext.expects(once()).method("close").will(throwException(new NamingException()));
        baseContext.returnDirContext(dirContext, true);
    }

    public void testGetProvisioning() throws Exception {
        assertEquals(getProvisioning(), baseContext.getProvisioning());
    }

    public void testGetMailboxAccountManager() throws Exception {
        assertSame(
                "Should return set MailboxAccountManager",
                getMailboxAccountManager(),
                baseContext.getMailboxAccountManager()
        );
    }

    public void testGetMediaObjectFactory() throws Exception {
        assertSame(
                "Should return set MediaObjectFactory",
                getMediaObjectFactory(),
                baseContext.getMediaObjectFactory()
        );
    }

    public void testGetProfileManager() throws Exception {
        assertSame(
                "Should return set ProfileManager",
                getProfileManager(),
                baseContext.getProfileManager()
        );
    }

    private IServiceInstance getServiceInstance() {
        mockServiceInstance = mock(IServiceInstance.class);
        return (IServiceInstance)mockServiceInstance.proxy();
    }

    private LdapServiceInstanceDecorator getLdapServiceInstanceDecorator(Direction direction) throws Exception {
        return baseContext.getServiceInstance(direction);
    }

    public static Test suite() {
        return new TestSuite(BaseContextTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
