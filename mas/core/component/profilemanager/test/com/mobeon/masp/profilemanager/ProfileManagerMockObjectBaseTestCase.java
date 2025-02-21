/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import javax.naming.NamingException;
import javax.naming.CommunicationException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;

import java.util.*;

import junit.framework.Assert;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.*;
import com.mobeon.masp.mailbox.IMailboxAccountManager;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.provisionmanager.IProvisioning;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;

/**
 * Abstract base class for MockObjectTestCase classes
 *
 * @author mande
 */
public abstract class ProfileManagerMockObjectBaseTestCase implements Thread.UncaughtExceptionHandler {
    protected static final String LOG4J_CONFIGURATION = "log4jconf.xml";
    protected static final String APPLICATION_NAME = "applicationname";
    protected static final String CFG_USER_REGISTER_NAME = "userregistername";
    protected static final String CFG_TYPE = "type";
    protected static final String CFG_TRUE = "true";
    protected static final String CFG_FALSE = "false";
    protected static final String CFG_WRITELEVEL = "writelevel";
    protected static final String FALSESTRING = "falsestring";
    protected static final String TRUESTRING = "truestring";
    protected static final String CFG_DEFAULTVALUE = "default";
    protected static final String CFG_SEARCHORDER = "searchorder";
    protected static final String CFG_PROVISIONINGNAME = "provisioningname";

    protected Mockery mockery = new JUnit4Mockery();

//    protected DirContext mockDirContext;
    protected static final String PROFILEMANAGERCFG = "../profilemanager/test/com/mobeon/masp/profilemanager/profilemanager.xml";
    protected BaseContext baseContext;
    protected ILocateService mockServiceLocator;
    protected IMailboxAccountManager mockMailboxAccountManager;
    protected IProfileManager mockProfileManager;
    protected IProvisioning mockProvisioning;
    protected IEventDispatcher mockEventDispatcher;
    private IMediaObjectFactory mockMediaObjectFactory;


    protected Map<Thread, Throwable> exceptionMap = Collections.synchronizedMap(new HashMap<Thread, Throwable>());


    protected void setUp() throws Exception {
        // Initialize console logging
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    public void tearDown() throws Exception {
    }

    protected void setUpProfileContext(IConfiguration configuration) throws ProfileManagerException {
        baseContext = new BaseContext();
        baseContext.setConfiguration(configuration);
        baseContext.setServiceLocator(getServiceLocator());
        baseContext.setDirContextEnv(getDirContextEnv());
        baseContext.setSessionProperties(getSessionProperties());
        baseContext.setMailboxAccountManager(getMailboxAccountManager());
        baseContext.setMediaObjectFactory(getMediaObjectFactory());
        baseContext.setProfileManager(getProfileManager());
        baseContext.setProvisioning(getProvisioning());
        baseContext.setEventDispatcher(getEventDispatcher());
        baseContext.init();
    }

    public static <T> void assertEquals(T[] expected, T[] actual) {
        assertEquals("", expected, actual);
    }

    public static <T> void assertEquals(String applicationName, T[] expected, T[] actual) {
        Assert.assertTrue(applicationName + "\nExpected:" + Arrays.toString(expected) + "\nActual  :" + Arrays.toString(actual),
                Arrays.equals(expected, actual));
    }

    public static <T> void assertEquals(List<T> expected, List<T> actual) {
        assertEquals("", expected, actual);
    }

    public static <T> void assertEquals(String applicationName, List<T> expected, List<T> actual) {
    	Assert.assertTrue(applicationName + "\nExpected:" + expected.toString() + "\nActual  :" + actual.toString(),
                expected.equals(actual));
    }

    public static void assertEquals(int[] expected, int[] actual) {
        assertEquals("", expected, actual);
    }

    public static void assertEquals(String applicationName, int[] expected, int[] actual) {
    	Assert.assertTrue(applicationName + "\nExpected:" + Arrays.toString(expected) + "\nActual  :" + Arrays.toString(actual),
                Arrays.equals(expected, actual));
    }

    public static void assertEquals(boolean[] expected, boolean[] actual) {
        assertEquals("", expected, actual);
    }

    public static void assertEquals(String applicationName, boolean[] expected, boolean[] actual) {
    	Assert.assertTrue(applicationName + "\nExpected:" + Arrays.toString(expected) + "\nActual  :" + Arrays.toString(actual),
                Arrays.equals(expected, actual));
    }

    protected IGroup getMockAttributeGroup(String applicationName, String userRegisterName, String type)
    throws UnknownParameterException {
        return getMockAttributeGroup(applicationName, userRegisterName, type, null);
    }

    protected IGroup getMockAttributeGroup(String applicationName, String userRegisterName, String type, String level)
    throws UnknownParameterException {
        return getMockAttributeGroup(applicationName, userRegisterName, type, level, null);
    }

    protected IGroup getMockAttributeGroup(String applicationName, String userRegisterName, String type, String level,
                                         String defaultValue) throws UnknownParameterException {
        return getMockAttributeGroup(applicationName, userRegisterName, type, level, defaultValue, null);
    }

    protected IGroup getMockAttributeGroup(final String applicationName, final String userRegisterName,
    		final String type, final String level, final String defaultValue, final String searchOrder) throws UnknownParameterException {

    	final IGroup mockIGroup = mockery.mock(IGroup.class, "mock" + capitalize(applicationName) + "Group");
    	mockery.checking(new Expectations(){{
    		oneOf(mockIGroup).getName();
    		will(returnValue(applicationName));

            oneOf(mockIGroup).getString(CFG_USER_REGISTER_NAME);
            will(returnValue(userRegisterName));

            oneOf(mockIGroup.getString(CFG_TYPE));
            will(returnValue(type));
    	}});

        if (type.compareTo("boolean") == 0) {
        	mockery.checking(new Expectations(){{
        		oneOf(mockIGroup).getString(CFG_TRUE);
        		will(returnValue(TRUESTRING));

        		oneOf(mockIGroup).getString(CFG_FALSE);
        		will(returnValue(FALSESTRING));
        	}});
        }

        if (level != null) {
        	mockery.checking(new Expectations(){{
        		oneOf(mockIGroup).getString(CFG_WRITELEVEL, "unknown");
        		will(returnValue(level));
        	}});
        } else {
        	mockery.checking(new Expectations(){{
        		oneOf(mockIGroup).getString(CFG_WRITELEVEL, "unknown");
        		will(returnValue("unknown"));
        	}});
        }

        if (defaultValue != null) {
        	mockery.checking(new Expectations(){{
        		oneOf(mockIGroup).getString(CFG_DEFAULTVALUE);
        		will(returnValue(defaultValue));
        	}});
        } else {
        	mockery.checking(new Expectations(){{
        		oneOf(mockIGroup).getString(CFG_DEFAULTVALUE);
        		will(throwException(new UnknownParameterException(CFG_DEFAULTVALUE, getParentGroup())));
        	}});
        }

        if (searchOrder != null) {
        	mockery.checking(new Expectations(){{
        		oneOf(mockIGroup).getString(CFG_SEARCHORDER);
        		will(returnValue(searchOrder));
        	}});
        } else {
        	mockery.checking(new Expectations(){{
        		oneOf(mockIGroup).getString(CFG_SEARCHORDER);
        		will(throwException(new UnknownParameterException(CFG_SEARCHORDER, getParentGroup())));
        	}});
        }

        return mockIGroup;
    }

    protected IMailboxAccountManager getMailboxAccountManager() {
        if (mockMailboxAccountManager == null) {
        	mockMailboxAccountManager = mockery.mock(IMailboxAccountManager.class);
        }
        return mockMailboxAccountManager;
    }

    protected IMediaObjectFactory getMediaObjectFactory() {
        if (mockMediaObjectFactory == null) {
            mockMediaObjectFactory = mockery.mock(IMediaObjectFactory.class);
        }
        return mockMediaObjectFactory;
    }

    protected IProfileManager getProfileManager() {
        if (mockProfileManager == null) {
            mockProfileManager = mockery.mock(IProfileManager.class);
        }
        return mockProfileManager;
    }

    protected IProvisioning getProvisioning() {
        if (mockProvisioning == null) {
            mockProvisioning = mockery.mock(IProvisioning.class);
        }
        return mockProvisioning;
    }

    protected IEventDispatcher getEventDispatcher() {
        if (mockEventDispatcher == null) {
            mockEventDispatcher = mockery.mock(IEventDispatcher.class);
            mockery.checking(new Expectations(){{
                oneOf(mockEventDispatcher).addEventReceiver(with(any(BaseContext.class)));
            }});
        }
        return mockEventDispatcher;
    }

    protected IConfiguration getConfiguration(String... cfgFiles) {
        // Setup configuration
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(cfgFiles);
        return configurationManager.getConfiguration();
    }

    protected Hashtable<String, String> getDirContextEnv() {
        Hashtable<String, String> dirContextEnv = new Hashtable<String, String>();
        dirContextEnv.put("java.naming.factory.initial", "com.mobeon.masp.profilemanager.ldap.MockLdapCtxFactory");
        return dirContextEnv;
    }

    protected Properties getSessionProperties() {
        Properties sessionProperties = new Properties();
        sessionProperties.put("mail.debug", "true");
        sessionProperties.put("mail.imap.auth.plain.disable", "true");
        sessionProperties.put("mail.imap.partialfetch", "false");

        return sessionProperties;
    }

    private ILocateService getServiceLocator() {
        mockServiceLocator = mockery.mock(ILocateService.class);
        return mockServiceLocator;
    }

    protected IGroup getParentGroup() {
        final IGroup mockParentGroup = mockery.mock(IGroup.class);
        mockery.checking(new Expectations(){{
            oneOf(mockParentGroup).getFullName();
            will(returnValue("parentgroupfullname"));
        }});
        return mockParentGroup;
    }

    /**
     * Makes first character uppercase
     * @param string the string to title case
     * @return Returns the string with the first character in uppercase
     */
    protected String capitalize(String string) {
        if (string.length() > 0) {
            return string.substring(0, 1).toUpperCase() + string.substring(1);
        } else {
            return string;
        }
    }

    protected NamingException getNamingException() {
        return new NamingException("namingexception");
    }

    protected NamingException getCommunicationException() {
        return new CommunicationException("communicationexception");
    }

    public void uncaughtException(Thread t, Throwable e) {
        exceptionMap.put(t, e);
    }
}
