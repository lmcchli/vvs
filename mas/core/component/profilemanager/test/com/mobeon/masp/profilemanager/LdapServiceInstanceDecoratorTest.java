package com.mobeon.masp.profilemanager;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import org.jmock.Mock;

/**
 * LdapServiceInstanceDecorator Tester.
 *
 * @author mande
 * @since <pre>04/28/2006</pre>
 * @version 1.0
 */
public class LdapServiceInstanceDecoratorTest extends ProfileManagerMockObjectBaseTestCase {
    private static final String HOSTNAME = "hostname";
    private static final int PORT = 389;
    private LdapServiceInstanceDecorator ldapServiceInstanceDecorator;
    private Mock mockServiceInstance;
    private static final String PROTOCOL = "ldap";

    public LdapServiceInstanceDecoratorTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        IServiceInstance mockServiceInstance = getServiceInstance(HOSTNAME, PORT);
        ldapServiceInstanceDecorator = LdapServiceInstanceDecorator.createLdapServiceInstanceDecorator(mockServiceInstance);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateNoHost() throws Exception {
        try {
            LdapServiceInstanceDecorator.createLdapServiceInstanceDecorator(getServiceInstance(null));
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testCreateNoPort() throws Exception {
        try {
            LdapServiceInstanceDecorator.createLdapServiceInstanceDecorator(getServiceInstance(HOSTNAME, -1));
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetProperty() throws Exception {
        String name = "name";
        String value = "value";
        mockServiceInstance.expects(once()).method("getProperty").with(eq(name)).will(returnValue(value));
        assertEquals("Value should be equal", value, ldapServiceInstanceDecorator.getProperty(name));
    }

    public void testSetProperty() throws Exception {
        String name = "name";
        String value = "value";
        mockServiceInstance.expects(once()).method("setProperty").with(eq(name), eq(value));
        ldapServiceInstanceDecorator.setProperty(name, value);
    }

    public void testGetServiceName() throws Exception {
        String serviceName = "servicename";
        mockServiceInstance.expects(once()).method("getServiceName").will(returnValue(serviceName));
        assertEquals("Servicename should be equal", serviceName, ldapServiceInstanceDecorator.getServiceName());
    }

    public void testSetServiceName() throws Exception {
        String serviceName = "servicename";
        mockServiceInstance.expects(once()).method("setServiceName").with(eq(serviceName));
        ldapServiceInstanceDecorator.setServiceName(serviceName);
    }

    public void testGetHost() throws Exception {
        assertEquals("Host should be equal", HOSTNAME, ldapServiceInstanceDecorator.getHost());
    }

    public void testGetPort() throws Exception {
        assertEquals("Port should be equal", PORT, ldapServiceInstanceDecorator.getPort());
    }

    public void testGetProtocol() throws Exception {
        assertEquals("Protocol should be equal", PROTOCOL, ldapServiceInstanceDecorator.getProtocol());
    }

    public void testGetDecoratedServiceInstance() throws Exception {
        assertSame(
                "Decorated instance should be same",
                mockServiceInstance.proxy(),
                ldapServiceInstanceDecorator.getDecoratedServiceInstance()
        );
    }

    public void testToString() throws Exception {
        assertEquals("toString should be delegated", "mockIServiceInstance", ldapServiceInstanceDecorator.toString());
    }

    private IServiceInstance getServiceInstance(String host) {
        mockServiceInstance = mock(IServiceInstance.class);
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.HOSTNAME)).
                will(returnValue(host));
        return (IServiceInstance)mockServiceInstance.proxy();
    }

    private IServiceInstance getServiceInstance(String host, int port) {
        mockServiceInstance = mock(IServiceInstance.class);
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.HOSTNAME)).
                will(returnValue(host));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.PORT)).
                will(returnValue(port < 0 ? null : Integer.toString(port)));
        return (IServiceInstance)mockServiceInstance.proxy();
    }

    private IServiceInstance getServiceInstanceHostException(Exception exception) {
        mockServiceInstance = mock(IServiceInstance.class);
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.HOSTNAME)).
                will(throwException(exception));
        return (IServiceInstance)mockServiceInstance.proxy();
    }

    private IServiceInstance getServiceInstancePortException(String host, Exception exception) {
        mockServiceInstance = mock(IServiceInstance.class);
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.HOSTNAME)).
                will(returnValue(host));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.PORT)).
                will(throwException(exception));
        return (IServiceInstance)mockServiceInstance.proxy();
    }

    public static Test suite() {
        return new TestSuite(LdapServiceInstanceDecoratorTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
