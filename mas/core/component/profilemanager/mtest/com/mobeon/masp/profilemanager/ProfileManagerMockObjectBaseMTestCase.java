/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.masp.mailbox.javamail.JavamailMailboxAccountManager;
import com.mobeon.masp.mailbox.javamail.JavamailContextFactory;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.profilemanager.greetings.GreetingMockObjectBaseTestCase;
import com.mobeon.common.externalcomponentregister.ExternalComponentRegister;
import com.mobeon.common.message_sender.jakarta.JakartaCommonsSmtpInternetMailSender;
import com.mobeon.common.provisionmanager.Provisioning;

import javax.naming.Context;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

/**
 * Abstract base class for MockObjectTestCase classes
 *
 * @author mande
 */
public abstract class ProfileManagerMockObjectBaseMTestCase extends GreetingMockObjectBaseTestCase {
    private static final String MAILBOXCFG = "../profilemanager/mtest/com/mobeon/masp/profilemanager/mailbox.xml";
    protected BaseContext profileContext;

    protected IProfileManager profileManager;

    public ProfileManagerMockObjectBaseMTestCase(String string) {
        super(string);
    }

    public void setUp() throws Exception {
        super.setUp();

        // Setup configuration
        IConfiguration configuration = getConfiguration(PROFILEMANAGERCFG, MAILBOXCFG);

        // Setup external component register context
        //com.mobeon.common.externalcomponentregister.BaseContext registerContext = new com.mobeon.common.externalcomponentregister.BaseContext();
        
        /**
         * comment out for now, since the package name changes makes thing very complicated
         */
        //registerContext.setConfiguration(configuration);
        
        //registerContext.setDirContextEnv(getDirContextEnv());
        
        /**
         * comment out for now, since the package name changes makes thing very complicated
         */
        //registerContext.setEventDispatcher(getEventDispatcher());
        //registerContext.setServiceAlgorithmMap(getServiceAlgorithmMap());
        //mockEventDispatcher.expects(once()).method("addEventReceiver").with(same(registerContext));
        //registerContext.init();

        // Setup servicelocator
        ExternalComponentRegister serviceLocator = ExternalComponentRegister.getInstance();

        // Setup mailboxaccountmanager
        JavamailMailboxAccountManager sessionAdapter = new JavamailMailboxAccountManager();
        JavamailContextFactory javamailContextFactory = new JavamailContextFactory();
        javamailContextFactory.setConfiguration(configuration);
        javamailContextFactory.setMediaObjectFactory(new MediaObjectFactory());
        javamailContextFactory.setInternetMailSender(new JakartaCommonsSmtpInternetMailSender());
        sessionAdapter.setContextFactory(javamailContextFactory);

        // Setup provisioning
        Provisioning provisioning = new Provisioning();
        provisioning.setConfiguration(configuration);
        provisioning.setServiceLocator(serviceLocator);

        // Setup profile context
        profileContext = new BaseContext();
        profileContext.setConfiguration(configuration);
        profileContext.setServiceLocator(serviceLocator);
        profileContext.setMailboxAccountManager(sessionAdapter);
        profileContext.setMediaObjectFactory(new MediaObjectFactory());
        profileContext.setDirContextEnv(getDirContextEnv());
        profileContext.setSessionProperties(getSessionProperties());
        profileContext.setProvisioning(provisioning);
        profileContext.setEventDispatcher(getEventDispatcher());
        profileContext.init();

        // Setup profile manager
        this.profileManager = new ProfileManagerImpl();
        ProfileManagerImpl profileManager = (ProfileManagerImpl)this.profileManager;
        profileManager.setContext(profileContext);
        profileManager.init();
    }


    protected Hashtable<String, String> getDirContextEnv() {
        Hashtable<String, String> dirContextEnv = new Hashtable<String, String>();
        dirContextEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        return dirContextEnv;
    }

    private Map<String, String> getServiceAlgorithmMap() {
        Map<String, String> serviceAlgorithmMap = new HashMap<String, String>();
        serviceAlgorithmMap.put("userregister", "com.mobeon.common.externalcomponentregister.algotithm.LogicalMultiMasterChooser");
        return serviceAlgorithmMap;
    }

    protected Properties getSessionProperties() {
        Properties sessionProperties = new Properties();
        sessionProperties.put("mail.debug", "false");
        sessionProperties.put("mail.imap.auth.plain.disable", "true");
        sessionProperties.put("mail.imap.partialfetch", "false");

        return sessionProperties;
    }
}
