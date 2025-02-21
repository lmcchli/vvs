/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.abcxyz.services.moip.migration.profilemanager.moip.pool.DirContextPoolManager;
import com.mobeon.common.configuration.ConfigurationChanged;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.IMailboxAccountManager;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.profilemanager.HostException;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.common.provisionmanager.IProvisioning;
import com.mobeon.common.util.javamail.BasicStoreManager;
import com.mobeon.common.util.javamail.LoggerJavamailDebugOutputStream;
import com.mobeon.common.util.javamail.StoreManager;

import jakarta.mail.Session;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Context object for profilemanager classes
 *
 * @author mande
 */
public class BaseContext implements IEventReceiver {
    private static final HostedServiceLogger log = new HostedServiceLogger(ILoggerFactory.getILogger(BaseContext.class));

    private static final String READ_SERVICE = "userregister";
    private static final String WRITE_SERVICE = "userregisterwrite";

    private IConfiguration configuration;
    private IMailboxAccountManager mailboxAccountManager;
    private ILocateService serviceLocator;
    private IProfileManager profileManager;
    private StoreManager storeManager;
    private IMediaObjectFactory mediaObjectFactory;
    private BaseConfig baseConfig;
    private IEventDispatcher eventDispatcher;

    private Hashtable<String, String> dirContextEnv;
    private Properties sessionProperties;
    private IProvisioning provisioning;

    public void doEvent(Event event) {
    }

    public synchronized void doGlobalEvent(Event event) {
        if (event instanceof ConfigurationChanged) {
            ConfigurationChanged configurationChanged = (ConfigurationChanged) event;
            IConfiguration configuration = configurationChanged.getConfiguration();
            try {
                initConfiguration(configuration);
                // If successful, replace old configuration
                this.configuration = configuration;
            } catch (ProfileManagerException e) {
                log.error(e.getMessage() + ". Keeping old configuration");
            }
        }
    }

    public synchronized BaseConfig getConfig() {
        return baseConfig;
    }

    protected synchronized IConfiguration getConfiguration() {
        return configuration;
    }

    public synchronized void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Retrieves a DirContext. The user of the DirContext is responsible for calling returnDirContext.
     *
     * @param serviceInstance the directory server to connect to
     * @param direction       indicates if context should be used for reading or writing
     * @return a DirContext
     * @throws HostException if encountering problems creating DirContext
     */
    protected DirContext getDirContext(LdapServiceInstanceDecorator serviceInstance, Direction direction) throws HostException {
        Hashtable<String, String> dirContextEnv = new Hashtable<String, String>(getDirContextEnv());
        String providerUrl = getProviderUrl(serviceInstance);
        dirContextEnv.put(Context.PROVIDER_URL, providerUrl);
        dirContextEnv.put(Context.SECURITY_PRINCIPAL, getConfig().getAdmin());
        dirContextEnv.put(Context.SECURITY_CREDENTIALS, getConfig().getPassword());
        dirContextEnv.put("com.sun.jndi.ldap.connect.timeout", getTimeout(direction));
        try {
            DirContext dirContext = DirContextPoolManager.getInstance().getDirContext(dirContextEnv);
            if (log.isDebugEnabled()) log.debug("Return DirContext <" + dirContext.toString() + ">");
            return dirContext;
        } catch (NamingException e) {
            String errmsg = "DirContext creation failed: " + e;
            if (log.isDebugEnabled()) log.debug(errmsg);
            throw new HostException(errmsg, e);
        }
    }

    /**
     * Returns a DirContext while handling possible exception. If the DirContext cannot be returned
     * a warning log entry is created.
     *
     * @param dirContext the DirContext which should be closed. If <code>null</code> nothing happens.
     * @logs.warning "Could not close DirContext" - if DirContext could not be closed.
     * Check exception for possible explanation.
     */
    protected void returnDirContext(DirContext dirContext) {
        returnDirContext(dirContext, false);
    }

    /**
     * Returns a DirContext while handling possible exception. If the DirContext cannot be returned
     * a warning log entry is created.
     *
     * @param dirContext the DirContext which should be returned
     * @param release    if the DirContext should be released
     */
    public void returnDirContext(DirContext dirContext, boolean release) {
        if (log.isDebugEnabled()) log.debug("returnDirContext(dirContext=" + dirContext + ", release=" + release);
        if (dirContext != null) {
            try {
                DirContextPoolManager.getInstance().returnDirContext(dirContext, release);
            } catch (NamingException e) {
                log.warn("Could not close DirContext", e);
                // Not much to do about this...
            }
        }
        if (log.isDebugEnabled()) log.debug("returnDirContext(DirContext, boolean) returns void");
    }

    protected Hashtable<String, String> getDirContextEnv() {
        return dirContextEnv;
    }

    public void setDirContextEnv(Hashtable<String, String> dirContextEnv) {
        this.dirContextEnv = dirContextEnv;
    }

    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    protected IMailboxAccountManager getMailboxAccountManager() {
        return mailboxAccountManager;
    }

    public void setMailboxAccountManager(IMailboxAccountManager mailboxAccountManager) {
        this.mailboxAccountManager = mailboxAccountManager;
    }

    public IMediaObjectFactory getMediaObjectFactory() {
        return mediaObjectFactory;
    }

    public void setMediaObjectFactory(IMediaObjectFactory mediaObjectFactory) {
        this.mediaObjectFactory = mediaObjectFactory;
    }

    protected IProfileManager getProfileManager() {
        return profileManager;
    }

    public void setProfileManager(IProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    public void setProvisioning(IProvisioning provisioning) {
        this.provisioning = provisioning;
    }

    protected IProvisioning getProvisioning() {
        return provisioning;
    }

    protected LdapServiceInstanceDecorator getServiceInstance(Direction direction) throws ProfileManagerException {
        try {
            IServiceInstance serviceInstance = getServiceLocator().locateService(getServiceName(direction));
            if (log.isDebugEnabled()) {
                if (log.isDebugEnabled()) log.debug("Return service instance <" + serviceInstance.toString() + ">");
            }
            return LdapServiceInstanceDecorator.createLdapServiceInstanceDecorator(serviceInstance);
        } catch (NoServiceFoundException e) {
            if (log.isDebugEnabled()) log.debug(e.getMessage());
            throw new HostException("Service <" + direction + "> could not be located. " + e.getMessage(), e);
        }
    }

    /**
     * Returns the service name to use for reading or writing
     *
     * @param direction the direction of the service to use, read or write
     * @return the service name to use
     * @logs.warning "Unknown Direction &lt;direction&gt;, using READ" - Implementation error, the Direction enum
     * has been extended and this is not handled here, the service name for reading will be used.
     */
    private String getServiceName(Direction direction) {
        switch (direction) {
            case READ:
                return READ_SERVICE;
            case WRITE:
                return WRITE_SERVICE;
            default:
                log.warn("Unknown Direction <" + direction + ">, using " + Direction.READ);
                return READ_SERVICE;
        }
    }

    protected ILocateService getServiceLocator() {
        return serviceLocator;
    }

    public void setServiceLocator(ILocateService serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    protected Properties getSessionProperties() {
        return sessionProperties;
    }

    public void setSessionProperties(Properties sessionProperties) {
        this.sessionProperties = sessionProperties;
    }

    public synchronized StoreManager getStoreManager() {
        return storeManager;
    }

    public synchronized void init() throws ProfileManagerException {
        try {
            initConfiguration(configuration);
            // Register as eventreceiver
            eventDispatcher.addEventReceiver(this);
        } catch (ProfileManagerException e) {
            log.fatal(e.getMessage());
            throw e;
        }
    }

    private void initConfiguration(IConfiguration configuration) throws ProfileManagerException {
        BaseConfig baseConfig = new BaseConfig();
        baseConfig.init(configuration);
        // If successful, replace old BaseConfig
        this.baseConfig = baseConfig;
        setUpStoreManager();
    }

    private void setUpStoreManager() {
        Properties sessionProperties = new Properties(getSessionProperties());
        sessionProperties.put("mail.imap.connectiontimeout", Integer.toString(getConfig().getReadTimeout()));
        sessionProperties.put("mail.imap.timeout", Integer.toString(getConfig().getReadTimeout()));
        Session javamailSession = Session.getInstance(sessionProperties);
        if (javamailSession.getDebug()) {
            javamailSession.setDebugOut(new PrintStream(new LoggerJavamailDebugOutputStream(log)));
        }
        storeManager = new BasicStoreManager(javamailSession);
    }


    /**
     * Creates a service provider URL to use when creating DirContext objects
     *
     * @param serviceInstance The decorated service instance to create the URL for
     * @return an URL to use when creating DirContext objects
     */
    private String getProviderUrl(LdapServiceInstanceDecorator serviceInstance) {
        StringBuffer providerUrl = new StringBuffer(serviceInstance.getProtocol());
        providerUrl.append("://");
        providerUrl.append(serviceInstance.getHost());
        providerUrl.append(":");
        providerUrl.append(serviceInstance.getPort());
        if (log.isDebugEnabled()) log.debug("Return provider URL <" + providerUrl.toString() + ">");
        return providerUrl.toString();
    }

    /**
     * Retrieves the timeout property for the dircontext environment
     *
     * @param direction if the timeout should be for reading or writing
     * @return a string representing the millisecond timeout
     * @logs.warning "Unknown Direction &lt;direction&gt;, using read timeout" - Implementation error, the Direction enum
     * has been extended and this is not handled here, the timeout for reading will be used.
     */
    private String getTimeout(Direction direction) {
        int timeout;
        switch (direction) {
            case READ:
                timeout = getConfig().getReadTimeout();
                break;
            case WRITE:
                timeout = getConfig().getWriteTimeout();
                break;
            default:
                log.warn("Unknown Direction <" + direction + ">, using read timeout");
                timeout = getConfig().getReadTimeout();
        }
        if (log.isDebugEnabled()) log.debug("Setting timeout <" + Integer.toString(timeout) + ">");
        return Integer.toString(timeout);
    }
}
