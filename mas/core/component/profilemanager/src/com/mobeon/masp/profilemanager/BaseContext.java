/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import com.mobeon.masp.mailbox.IMailboxAccountManager;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.configuration.*;
import com.mobeon.masp.util.javamail.StoreManager;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.common.provisionmanager.IProvisioning;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.Event;


import java.util.Hashtable;
import java.util.Properties;


/**
 * Context object for profilemanager classes
 *
 * @author mande
 */
public class BaseContext implements IEventReceiver {

    private IConfiguration configuration;
    private IMailboxAccountManager mailboxAccountManager;
    private ILocateService serviceLocator;
    private IProfileManager profileManager;
    private StoreManager storeManager;
    private IMediaObjectFactory mediaObjectFactory;
    private IEventDispatcher eventDispatcher;

    private Hashtable<String, String> dirContextEnv;
    private Properties sessionProperties;
    private IProvisioning provisioning;

    public void doEvent(Event event) {
    }

    public synchronized void doGlobalEvent(Event event) {
        if (event instanceof ConfigurationChanged) {
            ConfigurationChanged configurationChanged = (ConfigurationChanged)event;
            IConfiguration configuration = configurationChanged.getConfiguration();

            // If successful, replace old configuration
            this.configuration = configuration;

        }
    }

    protected synchronized IConfiguration getConfiguration() {
        return configuration;
    }

    public synchronized void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
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
            eventDispatcher.addEventReceiver(this);

    }








}
