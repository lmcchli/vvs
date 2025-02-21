package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.mobeon.common.externalcomponentregister.IServiceInstance;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.profilemanager.ProfileManagerException;

/**
 * Documentation
 *
 * @author mande
 */
public class LdapServiceInstanceDecorator implements IServiceInstance {
    private static final ILogger LOG = ILoggerFactory.getILogger(LdapServiceInstanceDecorator.class);

    private static final String PROTOCOL = "ldap";

    private String host;
    private int port;
    private IServiceInstance decoratedServiceInstance;

    private LdapServiceInstanceDecorator() {
    }

    public String getProperty(String s) {
        return decoratedServiceInstance.getProperty(s);
    }

    public void setProperty(String s1, String s2) {
        decoratedServiceInstance.setProperty(s1, s2);
    }

    public String getServiceName() {
        return decoratedServiceInstance.getServiceName();
    }

    public void setServiceName(String s) {
        decoratedServiceInstance.setServiceName(s);
    }

 /*   public void setServiceStatus(ServiceStatus serviceStatus) {
        decoratedServiceInstance.setServiceStatus(serviceStatus);
    }

    public ServiceStatus getServiceStatus() {
        return decoratedServiceInstance.getServiceStatus();
    }*/

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return PROTOCOL;
    }

    public IServiceInstance getDecoratedServiceInstance() {
        return decoratedServiceInstance;
    }

    @Override
    public String toString() {
        return decoratedServiceInstance.toString();
    }

    /**
     * @param serviceInstance
     * @return service instance decorator
     * @throws ProfileManagerException if service instance not provides host and port values.
     */
    public static LdapServiceInstanceDecorator createLdapServiceInstanceDecorator(IServiceInstance serviceInstance)
            throws ProfileManagerException {
        LdapServiceInstanceDecorator ldapServiceInstanceDecorator = new LdapServiceInstanceDecorator();
        ldapServiceInstanceDecorator.init(serviceInstance);
        return ldapServiceInstanceDecorator;
    }

    private void init(IServiceInstance serviceInstance) throws ProfileManagerException {

    	decoratedServiceInstance = serviceInstance;

    	host = decoratedServiceInstance.getProperty(IServiceInstance.HOSTNAME);

    	if (host == null) {
    		LOG.error("Could not find service instance property <" + IServiceInstance.HOSTNAME + ">.");
    		throw new ProfileManagerException("Instance property <" + IServiceInstance.HOSTNAME + "> is null.");
    	}

    	try {
    		port = Integer.valueOf(decoratedServiceInstance.getProperty(IServiceInstance.PORT));



    	} catch (NumberFormatException e) {
    		LOG.error("Could not find service instance property <" + IServiceInstance.PORT + ">.");
    		e.printStackTrace(System.out);
    		throw new ProfileManagerException("Instance property <" + IServiceInstance.PORT + "> is null.");

    	}
    }
}
