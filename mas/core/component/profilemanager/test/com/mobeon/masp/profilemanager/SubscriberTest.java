/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.mailbox.*;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.profilemanager.search.ProfileStringCriteria;
import com.mobeon.masp.profilemanager.ldap.MockLdapCtxFactory;
import com.mobeon.masp.profilemanager.greetings.*;
import com.mobeon.masp.profilemanager.pool.DirContextPoolManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.CommunicationException;
import javax.naming.directory.SearchControls;
import jakarta.activation.MimeType;

import org.jmock.Mock;
import org.jmock.core.Constraint;

import java.util.concurrent.TimeoutException;
import java.io.File;

/**
 * Subscriber Tester.
 *
 * @author mande
 * @since <pre>11/28/2005</pre>
 * @version 1.0
 */
public class SubscriberTest extends DistributionListMockObjectBaseTestCase
{
    private static ILogger log;

    private static final String CFGFILE = "test/com/mobeon/masp/profilemanager/profilemanager.xml";
    private static final String[] EMSERVICEDN = new String[]{
            "emservicename=webmail, ou=services, o=mobeon.com",
            "emservicename=msgtype_email, ou=services, o=mobeon.com",
            "emservicename=msgtype_voice, ou=services, o=mobeon.com",
            "emservicename=sms_notification, ou=services, o=mobeon.com",
            "emservicename=address_book, ou=services, o=mobeon.com",
            "emservicename=call_handling, ou=services, o=mobeon.com" };
    private static final String UMPASSWORD = "08123b0z06742U0C";
    private static final String PASSWORD = "3Z191R240A0L472u";
    private static final String BILLINGNUMBER = "19161";
    private static final String BADLOGINCOUNT = "1";
    private static final String AUTOPLAY = "yes";
    private static final String EMFTL = "00,F";
    private static final String MAILQUOTA= "1572864";
    private static final String CALLERXFER= "false";
    private static final String ADMININFO = "uniqueidentifier=um33";
    private static final String UID = "mande1";
    private static final String MAILHOST = "mailhost";
    private static final String EMFTLFUNCTIONS = "00,F,PIN:M,SPO:M,ACG:M";
    private static final int DELTA = 30;
    private static final int CDGMAX = 10;
    private static final String GRTADM = "GrtAdm_33";
    private static final String GRTADMPWD = "Gr8Pw4GA";
    private static final String HOST = "ockelbo.lab.mobeon.com";
    private static final int PORT = 143;
    private static final boolean CALLERXFERTOCOVERAGE = false;
    private static final String COS_DN = "cos=22,ou=c6,o=mobeon.com";
    private static final String COMMUNITY_DN = "ou=c6,o=mobeon.com";
    private static final String CN = "AndreasCom";
    private static final int TRY_LIMIT = 3;
    private static final int READ_TIMEOUT = 2000;
    private static final int WRITE_TIMEOUT = 3000;
    private static final String PROVIDER_URL = "ldap://" + HOST + ":389";

    private Subscriber subscriber;
    private Mock mockServiceInstance;
    private IMediaObject allCalls;
    private Mock mockGreetingManager;

    public SubscriberTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        log = ILoggerFactory.getILogger(ProfileManagerImplTest.class);

        setUpDirContext();

        setUpProfileContext(getConfiguration(CFGFILE));

        // Setup subscriber
        subscriber = new Subscriber(baseContext);
        subscriber.setDistinguishedName(ProfileLevel.COMMUNITY, COMMUNITY_DN);
        subscriber.setDistinguishedName(ProfileLevel.USER, "uniqueidentifier=um35,ou=c6,o=mobeon.com");
        subscriber.setDistinguishedName(ProfileLevel.BILLING, "billingnumber=19161,uniqueidentifier=um35,ou=c6,o=mobeon.com");
        addAttribute(subscriber, "autoplay", AUTOPLAY);           // boolean data type
        addAttribute(subscriber, "badlogincount", BADLOGINCOUNT); // integer data type
        addAttribute(subscriber, "billingnumber", BILLINGNUMBER); // string data type, readonly
        addAttribute(subscriber, "callerxfer", CALLERXFER);       // boolean data type, readonly
        addAttribute(subscriber, "emservicedn", EMSERVICEDN);     // string data type, readonly
        addAttribute(subscriber, "emftl", EMFTL);   // string data type
        addAttribute(subscriber, "mailquota", MAILQUOTA);         // integer data type, readonly
        addAttribute(subscriber, "umpassword", UMPASSWORD);       // xstring data type
        addAttribute(subscriber, "cosdn", COS_DN);

        // Setup serviceinstance
        mockServiceInstance = mock(IServiceInstance.class);

        // Setup MediaObject
        MediaObjectFactory mediaObjectFactory = new MediaObjectFactory();
        MediaProperties mediaProperties = new MediaProperties(new MimeType("audio/wav"));
        mediaProperties.addLength(new MediaLength(MediaLength.LengthUnit.MILLISECONDS, 2000));
        allCalls = mediaObjectFactory.create(new File("allcalls.wav"), mediaProperties);

        // Register MockLdapCtxFactory as Verifiable object
        MockLdapCtxFactory.registerToVerify(this);

        // Make sure that each test gets a new pool
        DirContextPoolManager.getInstance().removeDirContextPool(PROVIDER_URL);
    }

    private void addAttribute(Subscriber subscriber, String name, String... values) {
        subscriber.setAttribute(name, new ProfileAttribute(values));
    }

    public void tearDown() throws Exception {
    }


    private void setUpServiceLocator(String service, String host, int port) {
        mockServiceLocator.expects(once()).method("locateService").with(eq(service)).
                will(returnValue(mockServiceInstance.proxy()));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.HOSTNAME)).
                will(returnValue(host));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.PORT)).
                will(returnValue(Integer.toString(port)));
    }

    private void setUpServiceLocatorHostKnown(String service, String host, int port) {
        mockServiceLocator.expects(once()).method("locateService").with(eq(service), eq(host)).
                will(returnValue(mockServiceInstance.proxy()));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.PORT)).
                will(returnValue(Integer.toString(port)));
    }

    private Mock setUpGreetingAdmin() {
        addAttribute(subscriber, "admininfo", ADMININFO); // Must be added here due to method testGetGreetingAdminInfoMissing
        Mock mockGreetingAdmin = mock(IProfile.class);
        IProfile[] profiles = new IProfile[]{(IProfile)mockGreetingAdmin.proxy()};
        mockProfileManager.expects(once()).method("getProfile").
                with(eq(COMMUNITY_DN), eq(new ProfileStringCriteria("uniqueidentifier", "um33"))).
                will(returnValue(profiles));
        return mockGreetingAdmin;
    }

    /**
     * Test getting string value attribute from a subscriber
     * @throws Exception
     */
    public void testGetStringAttribute() throws Exception {
        String value = subscriber.getStringAttribute("billingnumber");
        assertEquals(BILLINGNUMBER, value);

        // Test getting multivalue attribute
        value = subscriber.getStringAttribute("emservicedn");
        assertEquals(EMSERVICEDN[0], value);
    }

    /**
     * Test getting an unknown string attribute from a subscriber
     * @throws Exception
     */
    public void testGetStringAttributeUnknown() throws Exception {
        try {
            subscriber.getStringAttribute("unknownattribute");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting an attribute of the wrong type from a subscriber
     * @throws Exception
     */
    public void testGetStringAttributeWrongType() throws Exception {
        try {
            subscriber.getStringAttribute("autoplay");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting default string value attribute from a subscriber
     * @throws Exception
     */
    public void testGetStringAttributeDefault() throws Exception {
        String value = subscriber.getStringAttribute("emftlfunctions");
        assertEquals(EMFTLFUNCTIONS, value);
    }

    /**
     * Test getting string values attribute from a subscriber
     * @throws Exception
     */
    public void testGetStringAttributes() throws Exception {
        String[] values = subscriber.getStringAttributes("billingnumber");
        assertEquals(new String[]{BILLINGNUMBER}, values);

        // Test getting multivalue attribute
        values = subscriber.getStringAttributes("emservicedn");
        assertEquals(EMSERVICEDN, values);
    }

    /**
     * Test getting unknown string attribute from a subscriber
     * @throws Exception
     */
    public void testGetStringAttributesUnknown() throws Exception {
        try {
            subscriber.getStringAttributes("unknownattribute");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting string values attribute of the wrong type from a subscriber
     */
    public void testGetStringAttributesWrongType() throws Exception {
        try {
            subscriber.getStringAttributes("autoplay");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting default string values attribute from a subscriber
     * @throws Exception
     */
    public void testGetStringAttributesDefault() throws Exception {
        String[] values = subscriber.getStringAttributes("emftlfunctions");
        assertEquals(new String[]{EMFTLFUNCTIONS}, values);
    }

    /**
     * Test setting string value attribute on a subscriber
     */
    public void testSetStringAttribute() throws Exception {
        String inhoursstart = "1234";
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").with(
                eq("billingnumber=19161,uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                isModificationItem("inhoursstart", inhoursstart)
        );
        subscriber.setStringAttribute("inhoursstart", inhoursstart);
        String value = subscriber.getStringAttribute("inhoursstart");
        assertEquals(inhoursstart, value);
    }

    /**
     * Test setting string values attribute on a subscriber
     */
    public void testSetStringAttributes() throws Exception {
        String[] inhoursstart = new String[]{"1234"};
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").with(
                eq("billingnumber=19161,uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                isModificationItem("inhoursstart", inhoursstart)
        );
        subscriber.setStringAttributes("inhoursstart", inhoursstart);
        String[] values = subscriber.getStringAttributes("inhoursstart");
        assertEquals(inhoursstart, values);
    }

    /**
     * Test setting read only string values attribute on a subscriber
     */
    public void testSetStringAttributesReadOnly() throws Exception {
        String[] mail = new String[]{"mande1@lab.mobeon.com"};
        try {
            subscriber.setStringAttributes("mail", mail);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test removing string values attribute on a subscriber
     */
    public void testDeleteStringAttribute() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                isModificationItem("emftl")
        );
        subscriber.setStringAttribute("emftl", null);
        try {
            subscriber.getStringAttribute("emftl");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test removing string values attribute on a subscriber
     */
    public void testDeleteStringAttributes() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                isModificationItem("emftl")
        );
        subscriber.setStringAttributes("emftl", null);
        try {
            subscriber.getStringAttribute("emftl");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test setting string values attribute on a subscriber when DirContext creation fails
     */
    public void testSetStringAttributesDirContextNamingException() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        MockLdapCtxFactory.throwInitialException(getNamingException());
        String[] inhoursstart = new String[]{"1234"};
        try {
            subscriber.setStringAttributes("inhoursstart", inhoursstart);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Wrong exception type", e.getCause() instanceof NamingException);
        }
    }

    public void testSetStringAttributesDirContextCommunicationException() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        MockLdapCtxFactory.throwInitialException(
                getCommunicationException(),
                getCommunicationException(),
                getCommunicationException()
        );
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        String[] inhoursstart = new String[]{"1234"};
        try {
            subscriber.setStringAttributes("inhoursstart", inhoursstart);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Wrong exception type", e.getCause() instanceof NamingException);
        }
    }

    // Todo: This testcase is not correct, is it needed for coverage?
    public void testSetStringAttributesDirContextCommunicationExceptionTimeout() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        MockLdapCtxFactory.throwInitialException(
                getCommunicationException(),
                getCommunicationException(),
                getCommunicationException()
        );
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        String[] inhoursstart = new String[]{"1234"};
        try {
            subscriber.setStringAttributes("inhoursstart", inhoursstart);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Wrong exception type", e.getCause() instanceof NamingException);
        }
    }

    /**
     * Test setting string values attribute on a subscriber when write fails
     */
    public void testSetStringAttributesWriteNamingException() throws Exception {
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        setUpDirContext();
        mockDirContext.expects(once()).method("modifyAttributes").
                will(throwException(getNamingException()));
        String[] inhoursstart = new String[]{"1234"};
        try {
            subscriber.setStringAttributes("inhoursstart", inhoursstart);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Wrong exception type", e.getCause() instanceof NamingException);
        }
    }

    public void testSetStringAttributesWriteCommunicationException() throws Exception {
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpDirContext();
        for (int i = 0; i < TRY_LIMIT; i++) {
            setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
            mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
            mockDirContext.expects(once()).method("modifyAttributes").
                    will(throwException(getCommunicationException()));
            mockDirContext.expects(once()).method("close");
        }
        String[] inhoursstart = new String[]{"1234"};
        try {
            subscriber.setStringAttributes("inhoursstart", inhoursstart);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Wrong exception type", e.getCause() instanceof NamingException);
        }
    }

    public void testSetStringAttributesWriteCommunicationExceptionTimeout() throws Exception {
        // Decrease timeout, so we don't have to wait so long
        int tryTimeLimit = 100;
        baseContext.getConfig().setTryTimeLimit(tryTimeLimit);
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpDirContext();
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockServiceLocator.expects(once()).method("reportServiceError").with(eq(mockServiceInstance.proxy()));
        mockDirContext.expects(once()).method("modifyAttributes").
                will(sleepAndThrowException(tryTimeLimit + DELTA, getCommunicationException()));
        mockDirContext.expects(once()).method("close");
        String[] inhoursstart = new String[]{"1234"};
        try {
            subscriber.setStringAttributes("inhoursstart", inhoursstart);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Wrong exception type", e.getCause() instanceof NamingException);
        }
    }

    public void testSetStringAttributesNullPointerException() throws Exception {
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpDirContext();
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").
                will(throwException(new NullPointerException()));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        String[] inhoursstart = new String[]{"1234"};
        try {
            subscriber.setStringAttributes("inhoursstart", inhoursstart);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue("Cause should be NullPointerException", e.getCause() instanceof NullPointerException);
        }
    }

    public void testSetStringAttributesTimeout() throws Exception {
        // Decrease timeout, so we don't have to wait so long
        int writeTimeout = 100;
        baseContext.getConfig().setWriteTimeout(writeTimeout);
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpDirContext();
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").
                will(sleep(writeTimeout * 2));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        String[] inhoursstart = new String[]{"1234"};
        try {
            subscriber.setStringAttributes("inhoursstart", inhoursstart);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be TimeoutException", e.getCause() instanceof TimeoutException);
        }
    }

    /**
     * Test setting string attributes when the modify interrupted
     * @throws Exception
     */
    public void testSetStringAttributesInterrupted() throws Throwable {
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpDirContext();
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").
                will(sleep(WRITE_TIMEOUT + DELTA));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    log.debug("Calling setStringAttributes");
                    String[] inhoursstart = new String[]{"1234"};
                    subscriber.setStringAttributes("inhoursstart", inhoursstart);
                    fail("Expected InterruptedException");
                } catch (ProfileManagerException e) {
                    assertTrue("Cause should be InterruptedException", e.getCause() instanceof InterruptedException);
                    log.debug("Caught ProfileManagerException due to InterruptedException");
                }
            }
        });
        thread.setUncaughtExceptionHandler(this);
        thread.start();
        // Wait for thread to call search on DirContext
        Thread.sleep(100);
        thread.interrupt();
        thread.join();
        if (exceptionMap.containsKey(thread)) {
            throw exceptionMap.get(thread);
        }
    }

    /**
     * Test setting string values attribute of unknown attribute from a subscriber
     */
    public void testSetStringAttributesUnknown() throws Exception {
        String[] mail = new String[]{"mande1@lab.mobeon.com"};
        try {
            subscriber.setStringAttributes("unknownattribute", mail);
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test setting string values attribute of wrong type from a subscriber
     */
    public void testSetStringAttributesWrongType() throws Exception {
        String[] badlogincount = new String[]{"1"};
        try {
            subscriber.setStringAttributes("badlogincount", badlogincount);
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting encoded string value attribute from a subscriber
     * @throws Exception
     */
    public void testGetXStringAttribute() throws Exception {
        String value = subscriber.getStringAttribute("umpassword");
        assertEquals("Password should be encoded", "1111", value);
    }

    /**
     * Test setting encoded string value attribute on a subscriber
     */
    public void testSetXStringAttribute() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        String umpassword = "abcd";
        mockDirContext.expects(once()).method("modifyAttributes").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                isModificationItem("umpassword", "3Z191R240A0L472u")
        );
        subscriber.setStringAttribute("umpassword", umpassword);
        String value = subscriber.getStringAttribute("umpassword");
        assertEquals(umpassword, value);
    }

    /**
     * Test setting encoded string values attribute on a subscriber
     */
    public void testSetXStringAttributes() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        String[] umpassword = new String[]{"1234", "abcd"};
        mockDirContext.expects(once()).method("modifyAttributes").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                isModificationItem("umpassword", "6X1S0a273H3i1E75", "3Z191R240A0L472u")
        );
        subscriber.setStringAttributes("umpassword", umpassword);
        String[] values = subscriber.getStringAttributes("umpassword");
        assertEquals(umpassword, values);
    }

    /**
     * Test removing string values attribute on a subscriber
     */
    public void testDeleteXStringAttribute() throws Exception {
        // Delete attribute
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                isModificationItem("umpassword")
        );
        subscriber.setStringAttribute("umpassword", null);
        try {
            subscriber.getStringAttribute("umpassword");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting integer value attribute from a subscriber
     * @throws Exception
     */
    public void testGetIntegerAttribute() throws Exception {
        int value = subscriber.getIntegerAttribute("badlogincount");
        assertEquals(1, value);
    }

    /**
     * Test getting an unknown integer attribute from a subscriber
     * @throws Exception
     */
    public void testGetIntegerAttributeUnknown() throws Exception {
        try {
            subscriber.getIntegerAttribute("unknownattribute");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting an integer attribute of the wrong type from a subscriber
     * @throws Exception
     */
    public void testGetIntegerAttributeWrongType() throws Exception {
        try {
            subscriber.getIntegerAttribute("autoplay");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting default integer value attribute from a subscriber
     * @throws Exception
     */
    public void testGetIntegerAttributeDefault() throws Exception {
        int value = subscriber.getIntegerAttribute("cdgmax");
        assertEquals(CDGMAX, value);
    }

    /**
     * Test getting integer values attribute from a subscriber
     * @throws Exception
     */
    public void testGetIntegerAttributes() throws Exception {
        int[] values = subscriber.getIntegerAttributes("badlogincount");
        assertEquals(new int[]{1}, values);

        // Test multivalue attribute
        addAttribute(subscriber, "badlogincount", "1", "2", "3", "4", "5");
        values = subscriber.getIntegerAttributes("badlogincount");
        assertEquals(new int[]{1, 2, 3, 4, 5}, values);
    }

    /**
     * Test getting unknown integer attribute from a subscriber
     * @throws Exception
     */
    public void testGetIntegerAttributesUnknown() throws Exception {
        try {
            subscriber.getIntegerAttributes("unknownattribute");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting integer attribute of the wrong type from a subscriber
     * @throws Exception
     */
    public void testGetIntegerAttributesWrongType() throws Exception {
        try {
            subscriber.getIntegerAttributes("autoplay");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting default integer values attribute from a subscriber
     * @throws Exception
     */
    public void testGetIntegerAttributesDefault() throws Exception {
        int[] values = subscriber.getIntegerAttributes("cdgmax");
        assertEquals(new int[]{CDGMAX}, values);
    }

    /**
     * Test setting integer value attribute on a subscriber
     */
    public void testSetIntegerAttribute() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        int badlogincount = 3;
        mockDirContext.expects(once()).method("modifyAttributes").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                isModificationItem("badlogincount", "3")
        );
        subscriber.setIntegerAttribute("badlogincount", badlogincount);
        int value = subscriber.getIntegerAttribute("badlogincount");
        assertEquals(badlogincount, value);
    }

    /**
     * Test setting integer values attribute on a subscriber
     */
    public void testSetIntegerAttributes() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        int[] badlogincount = new int[]{1, 2, 3};
        mockDirContext.expects(once()).method("modifyAttributes").with(
                eq("uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                isModificationItem("badlogincount", "1", "2", "3")
        );
        subscriber.setIntegerAttributes("badlogincount", badlogincount);
        int[] values = subscriber.getIntegerAttributes("badlogincount");
        assertEquals(badlogincount, values);
    }

    /**
     * Test setting read only integer values attribute on a subscriber
     */
    public void testSetIntegerAttributesReadOnly() throws Exception {
        int[] mailquota = new int[]{0};
        try {
            subscriber.setIntegerAttributes("mailquota", mailquota);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test setting integer values attribute on a subscriber when DirContext creation fails
     */
    public void testSetIntegerAttributesDirContextException() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        MockLdapCtxFactory.throwInitialException(new NamingException("namingexception"));
        int[] badlogincount = new int[]{1, 2, 3};
        try {
            subscriber.setIntegerAttributes("badlogincount", badlogincount);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Wrong exception type", e.getCause() instanceof NamingException);
        }
    }

    /**
     * Test setting integer values attribute on a subscriber when write fails
     */
    public void testSetIntegerAttributesWriteException() throws Exception {
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        setUpDirContext();
        mockDirContext.expects(once()).method("modifyAttributes").
                will(throwException(new NamingException("namingexception")));
        int[] badlogincount = new int[]{1, 2, 3};
        try {
            subscriber.setIntegerAttributes("badlogincount", badlogincount);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Wrong exception type", e.getCause() instanceof NamingException);
        }
    }

    public void testSetIntegerAttributesNullPointerException() throws Exception {
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpDirContext();
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").
                will(throwException(new NullPointerException()));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        int[] badlogincount = new int[]{1, 2, 3};
        try {
            subscriber.setIntegerAttributes("badlogincount", badlogincount);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue("Cause should be NullPointerException", e.getCause() instanceof NullPointerException);
        }
    }

    public void testSetIntegerAttributesTimeout() throws Exception {
        // Decrease timeout, so we don't have to wait so long
        int writeTimeout = 100;
        baseContext.getConfig().setWriteTimeout(writeTimeout);
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpDirContext();
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").
                will(sleep(2 * writeTimeout));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        int[] badlogincount = new int[]{1, 2, 3};
        try {
            subscriber.setIntegerAttributes("badlogincount", badlogincount);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be TimeoutException", e.getCause() instanceof TimeoutException);
        }
    }

    /**
     * Test setting integer attributes when the modify interrupted
     * @throws Exception
     */
    public void testSetIntegerAttributesInterrupted() throws Throwable {
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpDirContext();
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").
                will(sleep(WRITE_TIMEOUT + DELTA));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    log.debug("Calling setStringAttributes");
                    int[] badlogincount = new int[]{1, 2, 3};
                    subscriber.setIntegerAttributes("badlogincount", badlogincount);
                    fail("Expected InterruptedException");
                } catch (ProfileManagerException e) {
                    assertTrue("Cause should be InterruptedException", e.getCause() instanceof InterruptedException);
                    log.debug("Caught ProfileManagerException due to InterruptedException");
                }
            }
        });
        thread.setUncaughtExceptionHandler(this);
        thread.start();
        // Wait for thread to call search on DirContext
        Thread.sleep(100);
        thread.interrupt();
        thread.join();
        if (exceptionMap.containsKey(thread)) {
            throw exceptionMap.get(thread);
        }
    }

    /**
     * Test setting integer values attribute of the wrong type from a subscriber
     */
    public void testSetIntegerAttributesUnknown() throws Exception {
        int[] badlogincount = new int[]{1, 2, 3};
        try {
            subscriber.setIntegerAttributes("unknownattribute", badlogincount);
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test setting integer values attribute of wrong type from a subscriber
     */
    public void testSetIntegerAttributesWrongType() throws Exception {
        int[] inhoursstart = new int[]{12345};
        try {
            subscriber.setIntegerAttributes("inhoursstart", inhoursstart);
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting boolean value attribute from a subscriber
     * @throws Exception
     */
    public void testGetBooleanAttribute() throws Exception {
        boolean value = subscriber.getBooleanAttribute("autoplay");
        assertEquals(true, value);
    }

    /**
     * Test getting an unknown boolean attribute from a subscriber
     * @throws Exception
     */
    public void testGetBooleanAttributeUnknown() throws Exception {
        try {
            subscriber.getBooleanAttribute("unknownattribute");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting an boolean attribute of the wrong type from a subscriber
     * @throws Exception
     */
    public void testGetBooleanAttributeWrongType() throws Exception {
        try {
            subscriber.getBooleanAttribute("badlogincount");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting default boolean value attribute from a subscriber
     * @throws Exception
     */
    public void testGetBooleanAttributeDefault() throws Exception {
        boolean value = subscriber.getBooleanAttribute("callerxfertocoverage");
        assertEquals(CALLERXFERTOCOVERAGE, value);
    }

    /**
     * Test getting boolean values attribute from a subscriber
     * @throws Exception
     */
    public void testGetBooleanAttributes() throws Exception {
        boolean[] values = subscriber.getBooleanAttributes("autoplay");
        assertEquals(new boolean[]{true}, values);

        // Test multivalue attribute
        addAttribute(subscriber, "autoplay", "yes", "no", "yes", "no", "no");
        values = subscriber.getBooleanAttributes("autoplay");
        assertEquals(new boolean[]{true, false, true, false, false}, values);
    }

    /**
     * Test getting unknown boolean attribute from a subscriber
     * @throws Exception
     */
    public void testGetBooleanAttributesUnknown() throws Exception {
        try {
            subscriber.getBooleanAttributes("unknownattribute");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting boolean attribute of the wrong type from a subscriber
     * @throws Exception
     */
    public void testGetBooleanAttributesWrongType() throws Exception {
        try {
            subscriber.getBooleanAttributes("badlogincount");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getting default boolean values attribute from a subscriber
     * @throws Exception
     */
    public void testGetBooleanAttributesDefault() throws Exception {
        boolean[] values = subscriber.getBooleanAttributes("callerxfertocoverage");
        assertEquals(new boolean[]{CALLERXFERTOCOVERAGE}, values);
    }

    /**
     * Test setting boolean value attribute on a subscriber
     */
    public void testSetBooleanAttribute() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        boolean autoplay = false;
        mockDirContext.expects(once()).method("modifyAttributes").with(
                eq("billingnumber=19161,uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                isModificationItem("autoplay", "no")
        );
        subscriber.setBooleanAttribute("autoplay", autoplay);
        boolean value = subscriber.getBooleanAttribute("autoplay");
        assertEquals(autoplay, value);
    }

    /**
     * Test setting boolean values attribute on a subscriber
     */
    public void testSetBooleanAttributes() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        boolean[] autoplay = new boolean[]{false};
        mockDirContext.expects(once()).method("modifyAttributes").with(
                eq("billingnumber=19161,uniqueidentifier=um35,ou=c6,o=mobeon.com"),
                isModificationItem("autoplay", "no")
        );
        subscriber.setBooleanAttributes("autoplay", autoplay);
        boolean[] values = subscriber.getBooleanAttributes("autoplay");
        assertEquals(autoplay, values);
    }

    /**
     * Test setting read only integer values attribute on a subscriber
     */
    public void testSetBooleanAttributesReadOnly() throws Exception {
        boolean[] callerxfer = new boolean[]{true};
        try {
            subscriber.setBooleanAttributes("callerxfer", callerxfer);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test setting boolean values attribute on a subscriber when DirContext creation fails
     */
    public void testSetBooleanAttributesDirContextException() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        MockLdapCtxFactory.throwInitialException(new NamingException("namingexception"));
        boolean[] autoplay = new boolean[]{false, true, false};
        try {
            subscriber.setBooleanAttributes("autoplay", autoplay);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be NamingException", e.getCause() instanceof NamingException);
        }
    }

    /**
     * Test setting boolean values attribute on a subscriber when write fails
     */
    public void testSetBooleanAttributesWriteException() throws Exception {
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        setUpDirContext();
        mockDirContext.expects(once()).method("modifyAttributes").
                will(throwException(new NamingException("namingexception")));
        boolean[] autoplay = new boolean[]{false, true, false};
        try {
            subscriber.setBooleanAttributes("autoplay", autoplay);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Wrong exception type", e.getCause() instanceof NamingException);
        }
    }

    public void testSetBooleanAttributesNullPointerException() throws Exception {
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpDirContext();
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").
                will(throwException(new NullPointerException()));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        boolean[] autoplay = new boolean[]{false, true, false};
        try {
            subscriber.setBooleanAttributes("autoplay", autoplay);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue("Cause should be NullPointerException", e.getCause() instanceof NullPointerException);
        }
    }

    public void testSetBooleanAttributesTimeout() throws Exception {
        // Decrease timeout, so we don't have to wait so long
        int writeTimeout = 100;
        baseContext.getConfig().setWriteTimeout(writeTimeout);
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpDirContext();
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").
                will(sleep(2 * writeTimeout));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        boolean[] autoplay = new boolean[]{false, true, false};
        try {
            subscriber.setBooleanAttributes("autoplay", autoplay);
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Cause should be TimeoutException", e.getCause() instanceof TimeoutException);
        }
    }

    /**
     * Test setting string attributes when the modify interrupted
     * @throws Exception
     */
    public void testSetBooleanAttributesInterrupted() throws Throwable {
        // Mock the DirContext
        baseContext.setDirContextEnv(getDirContextEnv());
        setUpDirContext();
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("modifyAttributes").
                will(sleep(WRITE_TIMEOUT + DELTA));
        // Use stubs because the cancellation can happen before close is called
        mockDirContext.stubs().method("close");
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    log.debug("Calling setStringAttributes");
                    boolean[] autoplay = new boolean[]{false, true, false};
                    subscriber.setBooleanAttributes("autoplay", autoplay);
                    fail("Expected InterruptedException");
                } catch (ProfileManagerException e) {
                    assertTrue("Cause should be InterruptedException", e.getCause() instanceof InterruptedException);
                    log.debug("Caught ProfileManagerException due to InterruptedException");
                }
            }
        });
        thread.setUncaughtExceptionHandler(this);
        thread.start();
        // Wait for thread to call search on DirContext
        Thread.sleep(100);
        thread.interrupt();
        thread.join();
        if (exceptionMap.containsKey(thread)) {
            throw exceptionMap.get(thread);
        }
    }

    /**
     * Test setting boolean values attribute of the wrong type from a subscriber
     */
    public void testSetBooleanAttributesUnknown() throws Exception {
        boolean[] autoplay = new boolean[]{false, true, false};
        try {
            subscriber.setBooleanAttributes("unknownattribute", autoplay);
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test setting boolean values attribute of wrong type from a subscriber
     */
    public void testSetBooleanAttributesWrongType() throws Exception {
        boolean[] inhoursstart = new boolean[]{false};
        try {
            subscriber.setBooleanAttributes("inhoursstart", inhoursstart);
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }


    public void testGetCOS() throws Exception {
        addAttribute(subscriber, "inhoursdow", "1245");
        addAttribute(subscriber, "inhoursstart", "0830");
        addAttribute(subscriber, "inhoursend", "1700");
        subscriber.setDistinguishedName(ProfileLevel.COS, COS_DN); // Ordinarily done by ProfileManager
        mockProfileManager.expects(once()).method("getCos").with(eq(COS_DN)).will(returnValue(getCos()));
        ICos cos = subscriber.getCos();
        assertEquals("12345", cos.getStringAttribute("inhoursdow"));
        assertEquals("1245", subscriber.getStringAttribute("inhoursdow"));
        assertEquals("0800", cos.getStringAttribute("inhoursstart"));
        assertEquals("0830", subscriber.getStringAttribute("inhoursstart"));
        assertEquals("1630", cos.getStringAttribute("inhoursend"));
        assertEquals("1700", subscriber.getStringAttribute("inhoursend"));
        try {
            cos.getStringAttribute("unknownattribute");
            fail("Expected UnknownAttributeException");
        } catch (UnknownAttributeException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetCosUserProvisioningException() throws Exception {
        subscriber.setDistinguishedName(ProfileLevel.COS, COS_DN); // Ordinarily done by ProfileManager
        mockProfileManager.expects(once()).method("getCos").with(eq(COS_DN)).
                will(throwException(new UserProvisioningException("getCos")));
        try {
            subscriber.getCos();
            fail("Expected UserProvisioningException");
        } catch (UserProvisioningException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test get a greeting when greeting admin info is missing
     * @throws Exception
     */
    public void testGetGreetingAdminInfoMissing() throws Exception {
        try {
            subscriber.getGreeting(new GreetingSpecification("allcalls", GreetingFormat.VOICE));
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test get a greeting when greeting admin info is invalid
     * @throws Exception
     */
    public void testGetGreetingAdminInfoInvalid() throws Exception {
        addAttribute(subscriber, "admininfo", "missingequalsign"); // Must be added here due to method testGetGreetingAdminInfoMissing
        try {
            subscriber.getGreeting(new GreetingSpecification("allcalls", GreetingFormat.VOICE));
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test get a greeting when greeting admin lookup fails
     * @throws Exception
     */
    public void testGetGreetingMurHostException() throws Exception {
        addAttribute(subscriber, "admininfo", ADMININFO); // Must be added here due to method testGetGreetingAdminInfoMissing
        mockProfileManager.expects(once()).method("getProfile").
                with(eq(COMMUNITY_DN), eq(new ProfileStringCriteria("uniqueidentifier", "um33"))).
                will(throwException(new HostException("murhost")));
        try {
            subscriber.getGreeting(new GreetingSpecification("allcalls", GreetingFormat.VOICE));
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue("Expected HostException cause, got " + e.getCause(), e.getCause() instanceof HostException);
        }
    }

    /**
     * Test get a greeting when greeting admin lookup returns multiple hits
     * @throws Exception
     */
    public void testGetGreetingMultipleAdmins() throws Exception {
        addAttribute(subscriber, "admininfo", ADMININFO); // Must be added here due to method testGetGreetingAdminInfoMissing
        Mock mockGreetingAdmin = mock(IProfile.class);
        IProfile[] profiles = new IProfile[]{(IProfile)mockGreetingAdmin.proxy(), (IProfile)mockGreetingAdmin.proxy()};
        mockProfileManager.expects(once()).method("getProfile").
                with(eq(COMMUNITY_DN), eq(new ProfileStringCriteria("uniqueidentifier", "um33"))).
                will(returnValue(profiles));
        try {
            subscriber.getGreeting(new GreetingSpecification("allcalls", GreetingFormat.VOICE));
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test get a greeting when host service lookup fails
     * @throws Exception
     */
    public void testGetGreetingNoServiceFoundException() throws Exception {
        Mock mockGreetingAdmin = setUpGreetingAdmin();
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(HOST));
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.STORAGE), eq(HOST)).
                will(throwException(new NoServiceFoundException("")));
        GreetingSpecification greetingSpecification = new GreetingSpecification("allcalls", GreetingFormat.VOICE);
        try {
            subscriber.getGreeting(greetingSpecification);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(e.getCause() instanceof NoServiceFoundException);
        }
    }

    /**
     * Test get a greeting when service instance port property is not a number
     * @throws Exception
     */
    public void testGetGreetingNumberFormatException() throws Exception {
        Mock mockGreetingAdmin = setUpGreetingAdmin();
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(HOST));
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.STORAGE), eq(HOST)).
                will(returnValue(mockServiceInstance.proxy()));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.PORT)).
                will(returnValue("notanumber"));
        GreetingSpecification greetingSpecification = new GreetingSpecification("allcalls", GreetingFormat.VOICE);
        try {
            subscriber.getGreeting(greetingSpecification);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertEquals("Could not parse port property", e.getMessage());
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    /**
     * Test get a greeting when GreetingManager throws exception
     * @throws Exception
     */
    public void testGetGreetingProfileManagerException() throws Exception {
        Mock mockGreetingAdmin = setUpGreetingAdmin();

        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(HOST));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("uid")).
                will(returnValue(GRTADM));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("password")).
                will(returnValue(GRTADMPWD));
        setUpServiceLocatorHostKnown(IServiceName.STORAGE, HOST, PORT);

        GreetingSpecification greetingSpecification = new GreetingSpecification("allcalls", GreetingFormat.VOICE);
        subscriber.setGreetingManagerFactory(getMockGreetingManagerFactory(HOST, PORT, GRTADM, GRTADMPWD, getFolder(greetingSpecification)));
        mockGreetingManager.expects(once()).method("getGreeting").with(eq(greetingSpecification)).
                will(throwException(new ProfileManagerException("profilemanagerexception")));

        try {
            subscriber.getGreeting(greetingSpecification);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetGreeting() throws Exception {
        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                Mock mockGreetingAdmin = setUpGreetingAdmin();
                mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                        will(returnValue(HOST));
                mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("uid")).
                        will(returnValue(GRTADM));
                mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("password")).
                        will(returnValue(GRTADMPWD));
                setUpServiceLocatorHostKnown(IServiceName.STORAGE, HOST, PORT);
                GreetingSpecification greetingSpecification = new SpokenNameSpecification(type, format);
                subscriber.setGreetingManagerFactory(getMockGreetingManagerFactory(HOST, PORT, GRTADM, GRTADMPWD, getFolder(greetingSpecification)));
                Mock mockMediaObject = mock(IMediaObject.class);
                mockGreetingManager.expects(once()).method("getGreeting").with(eq(greetingSpecification)).
                        will(returnValue(mockMediaObject.proxy()));
                assertSame("MediaObjects should be same", mockMediaObject.proxy(), subscriber.getGreeting(greetingSpecification));
            }
        }
    }

    public void testGetGreetingAsync() throws Exception {
        GreetingSpecification greetingSpecification = new GreetingSpecification("allcalls", GreetingFormat.VOICE);
        try {
            subscriber.getGreetingAsync(greetingSpecification);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test set a greeting when specification is invalid
     * @throws Exception
     */
    public void testSetGreetingInvalidSpecification() throws Exception {
        for (GreetingFormat format : GreetingFormat.values()) {
            for (GreetingType type : GreetingType.values()) {
                SpokenNameSpecification specification;
                if (SUBID_TYPES.contains(type)) {
                    specification = new SpokenNameSpecification(type, format);
                } else {
                    specification = new SpokenNameSpecification(type, format, SUBID);
                }
                try {
                    subscriber.setGreeting(specification, null);
                    fail("Expected ProfileManagerException");
                } catch (ProfileManagerException e) {
                    assertTrue(true); // For statistical purposes
                }
            }
        }
    }

    /**
     * Test set a greeting when greeting admin info is missing
     * @throws Exception
     */
    public void testSetGreetingAdminInfoMissing() throws Exception {
        try {
            subscriber.setGreeting(new GreetingSpecification("allcalls", GreetingFormat.VOICE), allCalls);
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test set a greeting when greeting admin lookup fails
     * @throws Exception
     */
    public void testSetGreetingMurHostException() throws Exception {
        addAttribute(subscriber, "admininfo", ADMININFO); // Must be added here due to method testSetGreetingAdminInfoMissing
        mockProfileManager.expects(once()).method("getProfile").
                with(eq(COMMUNITY_DN), eq(new ProfileStringCriteria("uniqueidentifier", "um33"))).
                will(throwException(new HostException("murhost")));
        try {
            subscriber.setGreeting(new GreetingSpecification("allcalls", GreetingFormat.VOICE), allCalls);
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue("Expected HostException cause, got " + e.getCause(), e.getCause() instanceof HostException);
        }
    }

    /**
     * Test set a greeting when greeting admin lookup returns multiple hits
     * @throws Exception
     */
    public void testSetGreetingMultipleAdmins() throws Exception {
        addAttribute(subscriber, "admininfo", ADMININFO); // Must be added here due to method testGetGreetingAdminInfoMissing
        Mock mockGreetingAdmin = mock(IProfile.class);
        IProfile[] profiles = new IProfile[]{(IProfile)mockGreetingAdmin.proxy(), (IProfile)mockGreetingAdmin.proxy()};
        mockProfileManager.expects(once()).method("getProfile").
                with(eq(COMMUNITY_DN), eq(new ProfileStringCriteria("uniqueidentifier", "um33"))).
                will(returnValue(profiles));
        try {
            subscriber.setGreeting(new GreetingSpecification("allcalls", GreetingFormat.VOICE), allCalls);
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test set a greeting when host service lookup fails
     * @throws Exception
     */
    public void testSetGreetingNoServiceFoundException() throws Exception {
        Mock mockGreetingAdmin = setUpGreetingAdmin();
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(HOST));
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.STORAGE), eq(HOST)).
                will(throwException(new NoServiceFoundException("")));
        GreetingSpecification greetingSpecification = new GreetingSpecification("allcalls", GreetingFormat.VOICE);
        try {
            subscriber.setGreeting(greetingSpecification, allCalls);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(e.getCause() instanceof NoServiceFoundException);
        }
    }


    /**
     * Test set a greeting when service instance port property is not a number
     * @throws Exception
     */
    public void testSetGreetingNumberFormatException() throws Exception {
        Mock mockGreetingAdmin = setUpGreetingAdmin();
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(HOST));
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.STORAGE), eq(HOST)).
                will(returnValue(mockServiceInstance.proxy()));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.PORT)).
                will(returnValue("notanumber"));
        GreetingSpecification greetingSpecification = new GreetingSpecification("allcalls", GreetingFormat.VOICE);
        try {
            subscriber.setGreeting(greetingSpecification, allCalls);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertEquals("Could not parse port property", e.getMessage());
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    /**
     * Test set a greeting when GreetingManager throws exception
     * @throws Exception
     */
    public void testSetGreetingProfileManagerException() throws Exception {
        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                if (type == GreetingType.DIST_LIST_SPOKEN_NAME && format == GreetingFormat.VIDEO) {
                    // No support for video distlistspokenname yet
                    continue;
                }
                Mock mockGreetingAdmin = setUpGreetingAdmin();
                mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                        will(returnValue(MAIL_HOST));
                mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("uid")).
                        will(returnValue(GRTADM));
                mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("password")).
                        will(returnValue(GRTADMPWD));
                setUpServiceLocatorHostKnown(IServiceName.STORAGE, MAIL_HOST, PORT);

                GreetingSpecification greetingSpecification = new SpokenNameSpecification(type, format);
                if (SUBID_TYPES.contains(type)) {
                    greetingSpecification.setSubId(SUBID);
                }
                String folder = getFolder(greetingSpecification);
                subscriber.setGreetingManagerFactory(getMockGreetingManagerFactory(MAIL_HOST, PORT, GRTADM, GRTADMPWD, folder));
                IMediaObject mediaObject = mediaObjectMap.get(type).get(format);
                mockGreetingManager.expects(once()).method("setGreeting").
                        with(eq(BILLINGNUMBER), eq(greetingSpecification), eq(mediaObject)).
                        will(throwException(new ProfileManagerException("profilemanagerexception")));

                try {
                    subscriber.setGreeting(greetingSpecification, mediaObject);
                    fail("Expected ProfileManagerException");
                } catch (ProfileManagerException e) {
                    assertTrue(true); // For statistical purposes
                }
            }
        }
    }

    public void testSetGreeting() throws Exception {
        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                if (type == GreetingType.DIST_LIST_SPOKEN_NAME && format == GreetingFormat.VIDEO) {
                    // No support for video distlistspokenname yet
                    continue;
                }
                GreetingSpecification specification = new SpokenNameSpecification(type, format);
                if (SUBID_TYPES.contains(type)) {
                    specification.setSubId(SUBID);
                }
                Mock mockGreetingAdmin = setUpGreetingAdmin();

                mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                        will(returnValue(MAIL_HOST));
                mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("uid")).
                        will(returnValue(GRTADM));
                mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("password")).
                        will(returnValue(GRTADMPWD));
                setUpServiceLocatorHostKnown(IServiceName.STORAGE, MAIL_HOST, PORT);
                String folder = getFolder(specification);
                subscriber.setGreetingManagerFactory(getMockGreetingManagerFactory(MAIL_HOST, PORT, GRTADM, GRTADMPWD, folder));
                IMediaObject greeting = mediaObjectMap.get(type).get(format);
                mockGreetingManager.expects(once()).method("setGreeting").with(eq(BILLINGNUMBER), eq(specification), eq(greeting));
                subscriber.setGreeting(specification, greeting);
            }
        }
    }

    /**
     * Test get spoken name when greeting admin info is missing
     * @throws Exception
     */
    public void testGetSpokenNameAdminInfoMissing() throws Exception {
        try {
            subscriber.getSpokenName(GreetingFormat.VOICE);
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test get spoken name when greeting admin lookup fails
     * @throws Exception
     */
    public void testGetSpokenNameMurHostException() throws Exception {
        addAttribute(subscriber, "admininfo", ADMININFO); // Must be added here due to method testGetGreetingAdminInfoMissing
        mockProfileManager.expects(once()).method("getProfile").
                with(eq(COMMUNITY_DN), eq(new ProfileStringCriteria("uniqueidentifier", "um33"))).
                will(throwException(new HostException("murhost")));
        try {
            subscriber.getSpokenName(GreetingFormat.VOICE);
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue("Expected HostException cause, got " + e.getCause(), e.getCause() instanceof HostException);
        }
    }

    /**
     * Test get spoken name when greeting admin lookup returns multiple hits
     * @throws Exception
     */
    public void testGetSpokenNameMultipleAdmins() throws Exception {
        addAttribute(subscriber, "admininfo", ADMININFO); // Must be added here due to method testGetGreetingAdminInfoMissing
        Mock mockGreetingAdmin = mock(IProfile.class);
        IProfile[] profiles = new IProfile[]{(IProfile)mockGreetingAdmin.proxy(), (IProfile)mockGreetingAdmin.proxy()};
        mockProfileManager.expects(once()).method("getProfile").
                with(eq(COMMUNITY_DN), eq(new ProfileStringCriteria("uniqueidentifier", "um33"))).
                will(returnValue(profiles));
        try {
            subscriber.getSpokenName(GreetingFormat.VOICE);
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test get spoken name when host service lookup fails
     * @throws Exception
     */
    public void testGetSpokenNameNoServiceFoundException() throws Exception {
        Mock mockGreetingAdmin = setUpGreetingAdmin();
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(HOST));
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.STORAGE), eq(HOST)).
                will(throwException(new NoServiceFoundException("")));
        try {
            subscriber.getSpokenName(GreetingFormat.VOICE);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(e.getCause() instanceof NoServiceFoundException);
        }
    }


    /**
     * Test get spoken name when service instance port property is not a number
     * @throws Exception
     */
    public void testGetSpokenNameNumberFormatException() throws Exception {
        Mock mockGreetingAdmin = setUpGreetingAdmin();
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(HOST));
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.STORAGE), eq(HOST)).
                will(returnValue(mockServiceInstance.proxy()));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.PORT)).
                will(returnValue("notanumber"));
        try {
            subscriber.getSpokenName(GreetingFormat.VOICE);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertEquals("Could not parse port property", e.getMessage());
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    /**
     * Test get spoken name when GreetingManager throws exception
     * @throws Exception
     */
    public void testGetSpokenNameProfileManagerException() throws Exception {
        Mock mockGreetingAdmin = setUpGreetingAdmin();

        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(HOST));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("uid")).
                will(returnValue(GRTADM));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("password")).
                will(returnValue(GRTADMPWD));
        setUpServiceLocatorHostKnown(IServiceName.STORAGE, HOST, PORT);

        SpokenNameSpecification specification = new SpokenNameSpecification(GreetingType.SPOKEN_NAME, GreetingFormat.VOICE);
        subscriber.setGreetingManagerFactory(getMockGreetingManagerFactory(HOST, PORT, GRTADM, GRTADMPWD, getFolder(specification)));
        mockGreetingManager.expects(once()).method("getGreeting").with(eq(specification)).
                will(throwException(new ProfileManagerException("profilemanagerexception")));

        try {
            subscriber.getSpokenName(GreetingFormat.VOICE);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetSpokenName() throws Exception {
        for (GreetingFormat format : GreetingFormat.values()) {
            Mock mockGreetingAdmin = setUpGreetingAdmin();
            mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                    will(returnValue(HOST));
            mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("uid")).
                    will(returnValue(GRTADM));
            mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("password")).
                    will(returnValue(GRTADMPWD));
            setUpServiceLocatorHostKnown(IServiceName.STORAGE, HOST, PORT);
            GreetingSpecification greetingSpecification = new SpokenNameSpecification(
                    GreetingType.SPOKEN_NAME,
                    format
            );
            subscriber.setGreetingManagerFactory(getMockGreetingManagerFactory(HOST, PORT, GRTADM, GRTADMPWD, getFolder(greetingSpecification)));
            Mock mockMediaObject = mock(IMediaObject.class);
            mockGreetingManager.expects(once()).method("getGreeting").with(eq(greetingSpecification)).
                    will(returnValue(mockMediaObject.proxy()));
            assertSame("MediaObjects should be same", mockMediaObject.proxy(), subscriber.getSpokenName(format));
        }
    }

    public void testGetSpokenNameAsync() throws Exception {
        try {
            subscriber.getSpokenNameAsync(GreetingFormat.VOICE);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test sget spoken name when greeting admin info is missing
     * @throws Exception
     */
    public void testSetSpokenNameAdminInfoMissing() throws Exception {
        try {
            subscriber.setSpokenName(GreetingFormat.VOICE, allCalls);
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test set spoken name when greeting admin lookup fails
     * @throws Exception
     */
    public void testSetSpokenNameMurHostException() throws Exception {
        addAttribute(subscriber, "admininfo", ADMININFO); // Must be added here due to method testGetGreetingAdminInfoMissing
        mockProfileManager.expects(once()).method("getProfile").
                with(eq(COMMUNITY_DN), eq(new ProfileStringCriteria("uniqueidentifier", "um33"))).
                will(throwException(new HostException("murhost")));
        try {
            subscriber.setSpokenName(GreetingFormat.VOICE, allCalls);
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue("Expected HostException cause, got " + e.getCause(), e.getCause() instanceof HostException);
        }
    }

    /**
     * Test set spoken name when greeting admin lookup returns multiple hits
     * @throws Exception
     */
    public void testSetSpokenNameMultipleAdmins() throws Exception {
        addAttribute(subscriber, "admininfo", ADMININFO); // Must be added here due to method testGetGreetingAdminInfoMissing
        Mock mockGreetingAdmin = mock(IProfile.class);
        IProfile[] profiles = new IProfile[]{(IProfile)mockGreetingAdmin.proxy(), (IProfile)mockGreetingAdmin.proxy()};
        mockProfileManager.expects(once()).method("getProfile").
                with(eq(COMMUNITY_DN), eq(new ProfileStringCriteria("uniqueidentifier", "um33"))).
                will(returnValue(profiles));
        try {
            subscriber.setSpokenName(GreetingFormat.VOICE, allCalls);
            fail("Expected GreetingNotFoundException");
        } catch (GreetingNotFoundException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test set spoken name when host service lookup fails
     * @throws Exception
     */
    public void testSetSpokenNameNoServiceFoundException() throws Exception {
        Mock mockGreetingAdmin = setUpGreetingAdmin();
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(HOST));
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.STORAGE), eq(HOST)).
                will(throwException(new NoServiceFoundException("")));
        try {
            subscriber.setSpokenName(GreetingFormat.VOICE, allCalls);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(e.getCause() instanceof NoServiceFoundException);
        }
    }

    /**
     * Test get spoken name when service instance port property is not a number
     * @throws Exception
     */
    public void testSetSpokenNameNumberFormatException() throws Exception {
        Mock mockGreetingAdmin = setUpGreetingAdmin();
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(HOST));
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.STORAGE), eq(HOST)).
                will(returnValue(mockServiceInstance.proxy()));
        mockServiceInstance.expects(once()).method("getProperty").with(eq(IServiceInstance.PORT)).
                will(returnValue("notanumber"));
        try {
            subscriber.setSpokenName(GreetingFormat.VOICE, allCalls);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertEquals("Could not parse port property", e.getMessage());
            assertTrue(e.getCause() instanceof NumberFormatException);
        }
    }

    /**
     * Test set spoken name when GreetingManager throws exception
     * @throws Exception
     */
    public void testSetSpokenNameProfileManagerException() throws Exception {
        Mock mockGreetingAdmin = setUpGreetingAdmin();

        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(HOST));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("uid")).
                will(returnValue(GRTADM));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("password")).
                will(returnValue(GRTADMPWD));
        setUpServiceLocatorHostKnown(IServiceName.STORAGE, HOST, PORT);
        SpokenNameSpecification specification = new SpokenNameSpecification(GreetingType.SPOKEN_NAME, GreetingFormat.VOICE);
        IMediaObject spokenName = mediaObjectMap.get(specification.getType()).get(specification.getFormat());
        subscriber.setGreetingManagerFactory(getMockGreetingManagerFactory(HOST, PORT, GRTADM, GRTADMPWD, getFolder(specification)));
        mockGreetingManager.expects(once()).method("setGreeting").with(eq(BILLINGNUMBER), eq(specification), eq(spokenName)).
                will(throwException(new ProfileManagerException("profilemanagerexception")));

        try {
            subscriber.setSpokenName(GreetingFormat.VOICE, spokenName);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testSetSpokenName() throws Exception {
        for (GreetingFormat format : GreetingFormat.values()) {
            GreetingType type = GreetingType.SPOKEN_NAME;
            GreetingSpecification specification = new SpokenNameSpecification(type, format);
            Mock mockGreetingAdmin = setUpGreetingAdmin();

            mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                    will(returnValue(MAIL_HOST));
            mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("uid")).
                    will(returnValue(GRTADM));
            mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("password")).
                    will(returnValue(GRTADMPWD));
            setUpServiceLocatorHostKnown(IServiceName.STORAGE, MAIL_HOST, PORT);
            String folder = getFolder(specification);
            subscriber.setGreetingManagerFactory(getMockGreetingManagerFactory(MAIL_HOST, PORT, GRTADM, GRTADMPWD, folder));
            IMediaObject spokenName = mediaObjectMap.get(type).get(format);
            mockGreetingManager.expects(once()).method("setGreeting").with(eq(BILLINGNUMBER), eq(specification), eq(spokenName));
            subscriber.setSpokenName(format, spokenName);
        }
    }

    public void testGetDistributionLists() throws Exception {
        setUpServiceLocator(IServiceName.USER_REGISTER, HOST, 389);
        mockDirContext.expects(once()).method("search").with(
                eq(BASE_DN),
                eq("(objectclass=distributionlist)"),
                aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
        ).will(returnValue(getDistributionListsSearchResultNamingEnumeration(3)));
        IDistributionList[] distributionLists = subscriber.getDistributionLists();
        assertEquals("Expected 3 distribution lists", 3, distributionLists.length);
        for (IDistributionList distributionList : distributionLists) {
            assertEquals("Expected 1 member", 1, distributionList.getMembers().length);
            assertEquals("member" + distributionList.getID(), distributionList.getMembers()[0]);
        }
    }

    public void testGetDistributionListsNamingException() throws Exception {
        Throwable exception = new NamingException("namingexception");
        setUpServiceLocator(IServiceName.USER_REGISTER, HOST, 389);
        mockDirContext.expects(once()).method("search").with(
                eq(BASE_DN),
                eq("(objectclass=distributionlist)"),
                aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
        ).will(throwException(exception));
        try {
            subscriber.getDistributionLists();
            fail("Expected HostException");
        } catch (HostException e) {
            assertSame("Cause should be NamingException", exception, e.getCause());
        }
    }

    public void testGetDistributionListsCommunicationException() throws Exception {
        // Increase timeouts, since it is try limit that is tested
        baseContext.getConfig().setTryTimeLimit(1000);
        baseContext.getConfig().setWriteTimeout(1000);
        Throwable exception = new CommunicationException("communicationexception");
        for (int i = 0; i < TRY_LIMIT; i++) {
            setUpServiceLocator(IServiceName.USER_REGISTER, HOST, 389);
            mockServiceLocator.expects(once()).method("reportServiceError").with(same(mockServiceInstance.proxy()));
            mockDirContext.expects(once()).method("search").with(
                    eq(BASE_DN),
                    eq("(objectclass=distributionlist)"),
                    aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
            ).will(throwException(exception));
            mockDirContext.expects(once()).method("close");
        }
        try {
            subscriber.getDistributionLists();
            fail("Expected HostException");
        } catch (HostException e) {
            assertSame("Cause should be CommunicationException", exception, e.getCause());
        }
    }

    public void testGetDistributionListsUnexpectedException() throws Exception {
        Throwable exception = new NullPointerException("nullpointerexception");
        setUpServiceLocator(IServiceName.USER_REGISTER, HOST, 389);
        mockDirContext.expects(once()).method("search").with(
                eq(BASE_DN),
                eq("(objectclass=distributionlist)"),
                aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
        ).will(throwException(exception));
        try {
            subscriber.getDistributionLists();
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertSame("Cause should be NullPointerException", exception, e.getCause());
        }
    }

    public void testCreateDistributionList() throws Exception {
        String id = "1";
        addAttribute(subscriber, "mail", MAIL);
        addAttribute(subscriber, "mailhost", MAIL_HOST);
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        mockDirContext.expects(once()).method("createSubcontext").with(eq(DIST_LIST_DN), eq(getCreateSubcontextAttributesComponent(DIST_LIST_ID)));
        IDistributionList distributionList = subscriber.createDistributionList(id);
        assertNotNull("Distribution list should not be null", distributionList);
        assertEquals("Distribution list should be empty", 0, distributionList.getMembers().length);
    }

    public void testCreateDistributionListNamingException() throws Exception {
        String id = "1";
        addAttribute(subscriber, "mail", MAIL);
        addAttribute(subscriber, "mailhost", MAIL_HOST);
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        Throwable exception = new NamingException("namingexception");
        mockDirContext.expects(once()).method("createSubcontext").with(eq(DIST_LIST_DN), eq(getCreateSubcontextAttributesComponent(DIST_LIST_ID))).
                will(throwException(exception));
        try {
            subscriber.createDistributionList(id);
            fail("Expected HostException");
        } catch (HostException e) {
            assertSame("Cause should be NamingException", exception, e.getCause());
        }
    }

    public void testCreateDistributionListCommunicationException() throws Exception {
        String id = "1";
        addAttribute(subscriber, "mail", MAIL);
        addAttribute(subscriber, "mailhost", MAIL_HOST);
        Throwable exception = new CommunicationException("communicationexception");
        for (int i = 0; i < TRY_LIMIT; i++) {
            setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
            mockServiceLocator.expects(once()).method("reportServiceError").with(same(mockServiceInstance.proxy()));
            mockDirContext.expects(once()).method("createSubcontext").with(eq(DIST_LIST_DN), eq(getCreateSubcontextAttributesComponent(DIST_LIST_ID))).
                    will(throwException(exception));
            mockDirContext.expects(once()).method("close");
        }
        try {
            subscriber.createDistributionList(id);
            fail("Expected HostException");
        } catch (HostException e) {
            assertSame("Cause should be CommunicationException", exception, e.getCause());
        }
    }

    public void testCreateDistributionListUnexpectedException() throws Exception {
        String id = "1";
        addAttribute(subscriber, "mail", MAIL);
        addAttribute(subscriber, "mailhost", MAIL_HOST);
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        Throwable exception = new NullPointerException("nullpointerexception");
        mockDirContext.expects(once()).method("createSubcontext").with(eq(DIST_LIST_DN), eq(getCreateSubcontextAttributesComponent(DIST_LIST_ID))).
                will(throwException(exception));
        try {
            subscriber.createDistributionList(id);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertSame("Cause should be NullPointerException", exception, e.getCause());
        }
    }

    public void testDeleteDistributionList() throws Exception {
        addAttribute(subscriber, "mail", MAIL);
        setUpServiceLocator(IServiceName.USER_REGISTER, HOST, 389);
        mockDirContext.expects(once()).method("search").with(
                eq(BASE_DN),
                eq("(objectclass=distributionlist)"),
                aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
        ).will(returnValue(getDistributionListsSearchResultNamingEnumeration(1)));
        IDistributionList[] distributionLists = subscriber.getDistributionLists();

        Mock mockGreetingAdmin = setUpGreetingAdmin();
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(MAIL_HOST));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("uid")).
                will(returnValue(GRTADM));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("password")).
                will(returnValue(GRTADMPWD));
        // The servicelocator returns the same mocked service instance, so the call order is important
        // Todo: use local variable serviceInstance instead
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        setUpServiceLocatorHostKnown(IServiceName.STORAGE, MAIL_HOST, PORT);
        SpokenNameSpecification greetingSpecification = new SpokenNameSpecification(GreetingType.DIST_LIST_SPOKEN_NAME, GreetingFormat.VOICE, "1");
        String folder = getFolder(greetingSpecification);
        subscriber.setGreetingManagerFactory(getMockGreetingManagerFactory(MAIL_HOST, PORT, GRTADM, GRTADMPWD, folder));
        mockGreetingManager.expects(once()).method("setGreeting").with(eq(BILLINGNUMBER), eq(greetingSpecification), NULL);
        mockDirContext.expects(once()).method("destroySubcontext").with(eq(DIST_LIST_DN));
        subscriber.deleteDistributionList(distributionLists[0]);
    }

    public void testDeleteDistributionListNamingException() throws Exception {
        addAttribute(subscriber, "mail", MAIL);
        setUpServiceLocator(IServiceName.USER_REGISTER, HOST, 389);
        mockDirContext.expects(once()).method("search").with(
                eq(BASE_DN),
                eq("(objectclass=distributionlist)"),
                aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
        ).will(returnValue(getDistributionListsSearchResultNamingEnumeration(1)));
        IDistributionList[] distributionLists = subscriber.getDistributionLists();

        Mock mockGreetingAdmin = setUpGreetingAdmin();
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(MAIL_HOST));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("uid")).
                will(returnValue(GRTADM));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("password")).
                will(returnValue(GRTADMPWD));
        Throwable exception = new NamingException("namingexception");
        // The servicelocator returns the same mocked service instance, so the call order is important
        // Todo: use local variable serviceInstance instead
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        setUpServiceLocatorHostKnown(IServiceName.STORAGE, MAIL_HOST, PORT);
        SpokenNameSpecification greetingSpecification = new SpokenNameSpecification(GreetingType.DIST_LIST_SPOKEN_NAME, GreetingFormat.VOICE, "1");
        String folder = getFolder(greetingSpecification);
        subscriber.setGreetingManagerFactory(getMockGreetingManagerFactory(MAIL_HOST, PORT, GRTADM, GRTADMPWD, folder));
        mockGreetingManager.expects(once()).method("setGreeting").with(eq(BILLINGNUMBER), eq(greetingSpecification), NULL);
        mockDirContext.expects(once()).method("destroySubcontext").with(eq(DIST_LIST_DN)).will(throwException(exception));
        try {
            subscriber.deleteDistributionList(distributionLists[0]);
            fail("Expected HostException");
        } catch (HostException e) {
            assertSame("Cause should be NamingException", exception, e.getCause());
        }
    }

    public void testDeleteDistributionListCommunicationException() throws Exception {
        addAttribute(subscriber, "mail", MAIL);
        setUpServiceLocator(IServiceName.USER_REGISTER, HOST, 389);
        mockDirContext.expects(once()).method("search").with(
                eq(BASE_DN),
                eq("(objectclass=distributionlist)"),
                aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
        ).will(returnValue(getDistributionListsSearchResultNamingEnumeration(1)));
        IDistributionList[] distributionLists = subscriber.getDistributionLists();

        Mock mockGreetingAdmin = setUpGreetingAdmin();
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(MAIL_HOST));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("uid")).
                will(returnValue(GRTADM));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("password")).
                will(returnValue(GRTADMPWD));
        Throwable exception = new CommunicationException("communicationexception");
        SpokenNameSpecification greetingSpecification = new SpokenNameSpecification(GreetingType.DIST_LIST_SPOKEN_NAME, GreetingFormat.VOICE, "1");
        String folder = getFolder(greetingSpecification);
        subscriber.setGreetingManagerFactory(getMockGreetingManagerFactory(MAIL_HOST, PORT, GRTADM, GRTADMPWD, folder));
        mockGreetingManager.expects(once()).method("setGreeting").with(eq(BILLINGNUMBER), eq(greetingSpecification), NULL);
        // The servicelocator returns the same mocked service instance, so the call order is important
        // Todo: use local variable serviceInstance instead
        for (int i = 0; i < TRY_LIMIT; i++) {
            setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
            mockServiceLocator.expects(once()).method("reportServiceError").with(same(mockServiceInstance.proxy()));
            mockDirContext.expects(once()).method("destroySubcontext").with(eq(DIST_LIST_DN)).will(throwException(exception));
            mockDirContext.expects(once()).method("close");
        }
        setUpServiceLocatorHostKnown(IServiceName.STORAGE, MAIL_HOST, PORT);
        try {
            subscriber.deleteDistributionList(distributionLists[0]);
            fail("Expected HostException");
        } catch (HostException e) {
            assertSame("Cause should be NamingException", exception, e.getCause());
        }
    }

    public void testDeleteDistributionListUnexpectedException() throws Exception {
        addAttribute(subscriber, "mail", MAIL);
        setUpServiceLocator(IServiceName.USER_REGISTER, HOST, 389);
        mockDirContext.expects(once()).method("search").with(
                eq(BASE_DN),
                eq("(objectclass=distributionlist)"),
                aSearchControl(READ_TIMEOUT, SearchControls.ONELEVEL_SCOPE)
        ).will(returnValue(getDistributionListsSearchResultNamingEnumeration(1)));
        IDistributionList[] distributionLists = subscriber.getDistributionLists();

        Mock mockGreetingAdmin = setUpGreetingAdmin();
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("mailhost")).
                will(returnValue(MAIL_HOST));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("uid")).
                will(returnValue(GRTADM));
        mockGreetingAdmin.expects(once()).method("getStringAttribute").with(eq("password")).
                will(returnValue(GRTADMPWD));
        Throwable exception = new NullPointerException("nullpointerexception");
        // The servicelocator returns the same mocked service instance, so the call order is important
        // Todo: use local variable serviceInstance instead
        setUpServiceLocator(IServiceName.USER_REGISTER_WRITE, HOST, 389);
        setUpServiceLocatorHostKnown(IServiceName.STORAGE, MAIL_HOST, PORT);
        SpokenNameSpecification greetingSpecification = new SpokenNameSpecification(GreetingType.DIST_LIST_SPOKEN_NAME, GreetingFormat.VOICE, "1");
        String folder = getFolder(greetingSpecification);
        subscriber.setGreetingManagerFactory(getMockGreetingManagerFactory(MAIL_HOST, PORT, GRTADM, GRTADMPWD, folder));
        mockGreetingManager.expects(once()).method("setGreeting").with(eq(BILLINGNUMBER), eq(greetingSpecification), NULL);
        mockDirContext.expects(once()).method("destroySubcontext").with(eq(DIST_LIST_DN)).will(throwException(exception));
        try {
            subscriber.deleteDistributionList(distributionLists[0]);
            fail("Expected ProfileManagerException");
        } catch (ProfileManagerException e) {
            assertSame("Cause should be NullPointerException", exception, e.getCause());
        }
    }

    public void testGetMailboxUserProvisioningException() throws Exception {
        try {
            subscriber.getMailbox();
            fail("Expected UserProvisioningException");
        } catch (HostException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test getMailbox() when ILocateService throws NoServiceFoundException
     * @throws Exception
     */
    public void testGetMailboxNoServiceFoundException() throws Exception {
        // Must be added here due to method testGetMailboxUserProvisioningException
        setUpMailboxCredentials();
        mockServiceLocator.expects(once()).
                method("locateService").
                with(eq(IServiceName.STORAGE), eq(MAILHOST)).
                will(throwException(new NoServiceFoundException(MAILHOST)));

        try {
            subscriber.getMailbox();
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Expected NoServiceFoundException cause, got " + e.getCause(),
                    e.getCause() instanceof NoServiceFoundException);
        }
    }

    private void setUpMailboxCredentials() {
        addAttribute(subscriber, "uid", UID);
        addAttribute(subscriber, "mailhost", MAILHOST);
        addAttribute(subscriber, "password", PASSWORD);
        addAttribute(subscriber, "mail", MAIL);
    }

    /**
     * Tests getMailbox when mailbox account manager throws MailboxException
     * @throws Exception
     */
    public void testGetMailboxHostException() throws Exception {
        // Must be added here due to method testGetMailboxUserProvisioningException
        setUpMailboxCredentials();
        Mock mockServiceInstance = mock(IServiceInstance.class);
        mockServiceLocator.expects(once()).
                method("locateService").
                with(eq(IServiceName.STORAGE), eq(MAILHOST)).
                will(returnValue(mockServiceInstance.proxy()));
        mockMailboxAccountManager.expects(once()).method("getMailbox").
                with(eq(mockServiceInstance.proxy()), isMailboxProfile(UID, "abcd", MAIL)).
                will(throwException(new MailboxException("")));
        try {
            subscriber.getMailbox();
            fail("Expected HostException");
        } catch (HostException e) {
            assertTrue("Expected MailboxException cause, got " + e.getCause(), e.getCause() instanceof MailboxException);
        }
    }

    public void testGetMailbox() throws Exception {
        // Must be added here due to method testGetMailboxUserProvisioningException
        setUpMailboxCredentials();
        Mock mockServiceInstance = mock(IServiceInstance.class);
        mockServiceLocator.expects(once()).
                method("locateService").
                with(eq(IServiceName.STORAGE), eq(MAILHOST)).
                will(returnValue(mockServiceInstance.proxy()));

        Mock mockMailbox = mock(IMailbox.class);
        mockMailboxAccountManager.expects(once()).method("getMailbox").
                with(eq(mockServiceInstance.proxy()), isMailboxProfile(UID, "abcd", MAIL)).
                will(returnValue(mockMailbox.proxy()));
        IMailbox mailbox = subscriber.getMailbox();
        assertEquals("Returned mailbox should be same", mockMailbox.proxy(), mailbox);

        // Next get should use cached mailbox
        mockServiceLocator.expects(once()).
                method("locateService").
                with(eq(IServiceName.STORAGE), eq(MAILHOST)).
                will(returnValue(mockServiceInstance.proxy()));
        mailbox = subscriber.getMailbox();
        assertEquals("Returned mailbox should be same", mockMailbox.proxy(), mailbox);

        // Close should close mailbox also
        mockMailbox.expects(once()).method("close");
        subscriber.close();
    }

    public void testGetMailboxCloseFailure() throws Exception {
        // Must be added here due to method testGetMailboxUserProvisioningException
        setUpMailboxCredentials();
        Mock mockServiceInstance = mock(IServiceInstance.class);
        mockServiceLocator.expects(once()).
                method("locateService").
                with(eq(IServiceName.STORAGE), eq(MAILHOST)).
                will(returnValue(mockServiceInstance.proxy()));

        Mock mockMailbox = mock(IMailbox.class);
        mockMailboxAccountManager.expects(once()).method("getMailbox").
                with(eq(mockServiceInstance.proxy()), isMailboxProfile(UID, "abcd", MAIL)).
                will(returnValue(mockMailbox.proxy()));
        subscriber.getMailbox();

        mockMailbox.expects(once()).method("close").will(throwException(new MailboxException("mailboxexception")));
        subscriber.close();
    }

    public void testGetMailbox1() throws Exception {
        String mailHost = "mailhost";
        try {
            subscriber.getMailbox(mailHost);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetMailbox2() throws Exception {
        String mailHost = "mailhost";
        String accountID = "accountid";
        try {
            subscriber.getMailbox(mailHost, accountID);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetMailbox3() throws Exception {
        String mailHost = "mailhost";
        String accountID = "accountid";
        String accountPassword = "accountPassword";
        try {
            subscriber.getMailbox(mailHost, accountID, accountPassword);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testToString() throws Exception {
        Subscriber subscriber = new Subscriber(baseContext);
        assertEquals("IProfile(uid=UNKNOWN, telephonenumber=UNKNOWN)", subscriber.toString());


        subscriber = new Subscriber(baseContext);
        addAttribute(subscriber, "uid", UID);
        assertEquals("IProfile(uid=" + UID + ", telephonenumber=UNKNOWN)", subscriber.toString());

        subscriber = new Subscriber(baseContext);
        addAttribute(subscriber, "billingnumber", BILLINGNUMBER);
        assertEquals("IProfile(uid=UNKNOWN, telephonenumber=" + BILLINGNUMBER + ")", subscriber.toString());

        subscriber = new Subscriber(baseContext);
        addAttribute(subscriber, "uid", UID);
        addAttribute(subscriber, "billingnumber", BILLINGNUMBER);
        assertEquals("IProfile(uid=" + UID + ", telephonenumber=" + BILLINGNUMBER + ")", subscriber.toString());
    }

    private ICos getCos() {
        ProfileAttributes profileAttributes = new ProfileAttributes(baseContext);
        profileAttributes.put("inhoursdow", new ProfileAttribute(new String[]{"12345"}));
        profileAttributes.put("inhoursstart", new ProfileAttribute(new String[]{"0800"}));
        profileAttributes.put("inhoursend", new ProfileAttribute(new String[]{"1630"}));
        return new ProfileSettings(baseContext, profileAttributes);
    }


    private String getFolder(GreetingSpecification greetingSpecification) {
        switch (greetingSpecification.getType()) {
            case ALL_CALLS:
            case BUSY:
            case CDG:
            case EXTENDED_ABSENCE:
            case NO_ANSWER:
            case OUT_OF_HOURS:
            case OWN_RECORDED:
            case TEMPORARY:
            case SPOKEN_NAME:
                return BILLINGNUMBER + "/Greeting";
            case DIST_LIST_SPOKEN_NAME:
                return BILLINGNUMBER + "/DistList";
            default:
                throw new IllegalArgumentException(greetingSpecification.getType().toString());
        }
    }

    private GreetingManagerFactory getMockGreetingManagerFactory(String host, int port, String userId, String password, String folder) {
        Mock mockGreetingManagerFactory = mock(GreetingManagerFactory.class);
        mockGreetingManager = mock(GreetingManager.class);
        mockGreetingManagerFactory.expects(once()).method("getGreetingManager").with(new Constraint[] {
                same(baseContext),
                eq(host),
                eq(port),
                eq(userId),
                eq(password),
                eq(folder)
        }).will(returnValue(mockGreetingManager.proxy()));
        return (GreetingManagerFactory)mockGreetingManagerFactory.proxy();
    }

    private NamingEnumeration getDistributionListsSearchResultNamingEnumeration(int number) {
        Mock mockNamingEnumeration = mock(NamingEnumeration.class);
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(false));
        for (int i = 1; i <= number; i++) {
            addDistributionList(mockNamingEnumeration, Integer.toString(i), "member" + Integer.toString(i));
        }
        return (NamingEnumeration)mockNamingEnumeration.proxy();
    }

    private void addDistributionList(Mock mockNamingEnumeration, String id, String... members) {
        mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(true));
        mockNamingEnumeration.expects(once()).method("next").will(returnValue(getDistributionListSearchResult(id, members)));
    }

    public static Test suite() {
        return new TestSuite(SubscriberTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
