package com.mobeon.common.trafficeventsender;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.util.SystemPropertyHandler;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;

/**
 * Tests the MfsClient behaviour
 *
 * @author estberg
 *
 */
@RunWith(JMock.class)
public class MfsClientTest {

    private static final String LOGGEDIN_FILENAME = "loggedin";
    
    private Mockery mockery = new JUnit4Mockery();

    /**
     * Extends MfsClient by using the MfsEventManager mock
     * @author estberg
     *
     */
    class MfsClientStub extends MfsClient {

        public MfsClientStub(IMfsEventManager eventManager, IDirectoryAccess mcd, ICommonMessagingAccess mfs) {
            super(eventManager, mcd, mfs);
        }
    }

    // Set for holding created files, used by FileStub
    public static Set<String> fileExistSet = new TreeSet<String>();
    // Set for holding created directories, used by FileStub
    public static Set<String> dirExistSet = new TreeSet<String>();


    class MfsEventManagerStub extends MfsEventManager {

        public MfsEventManagerStub(ICommonMessagingAccess cma,
                IDirectoryAccess da) {
            super(CommonMessagingAccess.getInstance(), da);
        }
    }
    
    @BeforeClass
    public static void setupBefore() throws ConfigurationException, ConfigurationDataException {
		CommonTestingSetup.setup();
    }
    
    @AfterClass
    public static void cleanAfter() {
    	CommonTestingSetup.tearDown();
    }

    /**
     * Test SetupPropertyNameList error handling due to unknown property
     * @throws TrafficEventSenderException 
     */
    @Test
    public void testSetupPropertyNameListWrongProperty() throws TrafficEventSenderException {
    	final String phone = "12345678";
    	IDirectoryAccess jmockMcd = mockery.mock(IDirectoryAccess.class);
    	ICommonMessagingAccess jmockMfs = mockery.mock(ICommonMessagingAccess.class);
    	final IMfsEventManager jmockMfsEventManager = mockery.mock(IMfsEventManager.class);
        MfsClient mfsClient = new MfsClientStub(
                jmockMfsEventManager,
                jmockMcd,
                jmockMfs);
        
        TrafficEvent errPropEvent = new TrafficEvent(MfsClient.EVENT_SLAMDOWNINFORMATION);
        errPropEvent.setProperty("erronoeus property", "some value");

        try {
            mfsClient.sendTrafficEvent(errPropEvent);
            Assert.fail("Expected TrafficEventSenderException");
        } catch (TrafficEventSenderException e) {
        	// Expecting exception
        }

        // Add a correct property - should still not work
        errPropEvent.setProperty("callednumber", phone);

        try {
            mfsClient.sendTrafficEvent(errPropEvent);
            Assert.fail("Expected TrafficEventSenderException");
        } catch (TrafficEventSenderException e) {
        	// Expecting exception
        }
    }

    /**
     * Test SetupPropertyNameList error handling due to unknown event name
     */
    @Test
    public void testSetupPropertyNameListWrongName() {
    	IDirectoryAccess jmockMcd = mockery.mock(IDirectoryAccess.class);
    	ICommonMessagingAccess jmockMfs = mockery.mock(ICommonMessagingAccess.class);
    	IMfsEventManager jmockMfsEventManager = mockery.mock(IMfsEventManager.class);
        MfsClient mfsClient = new MfsClientStub(
                jmockMfsEventManager,
                jmockMcd,
                jmockMfs);

        TrafficEvent errNameEvent = new TrafficEvent("wrong name");
        errNameEvent.setProperty("callednumber", "12345678");

        try {
            mfsClient.sendTrafficEvent(errNameEvent);
            Assert.fail("Expected TrafficEventSenderException");
        } catch (TrafficEventSenderException e) {
        	// Expecting exception
        }
    }

    /**
     * Test that SetupPropertyNameList works when given OK events
     * @throws TrafficEventSenderException 
     */
    @Test
    public void testSetupPropertyNameListOk() throws TrafficEventSenderException {
    	IDirectoryAccess jmockMcd = mockery.mock(IDirectoryAccess.class);
    	ICommonMessagingAccess jmockMfs = mockery.mock(ICommonMessagingAccess.class);
    	final IMfsEventManager jmockMfsEventManager = mockery.mock(IMfsEventManager.class);
    	final String number = "12345678";
    	
    	mockery.checking(new Expectations(){{
    		oneOf(jmockMfsEventManager).storeEvent(with(any(String.class)), with(any(TrafficEvent.class)));
    		oneOf(jmockMfsEventManager).createLoginFile(number);
    		oneOf(jmockMfsEventManager).removeLoginFile(number);
    	}});
        MfsClient mfsClient = new MfsClientStub(
                jmockMfsEventManager,
                jmockMcd,
                jmockMfs);

        String[] validEventNames = new String[] {
        		MfsClient.EVENT_LOGININFORMATION, 
        		MfsClient.EVENT_LOGOUTINFORMATION,
        		MfsClient.EVENT_MWIOFF};
        for (String eventName : validEventNames) {
            // Create with OK name
            TrafficEvent okEvent = new TrafficEvent(eventName);
            // Add valid properties
            okEvent.setProperty("callednumber", "12345678");
            if (eventName.equals(MfsClient.EVENT_SLAMDOWNINFORMATION)) {
            	okEvent.setProperty(MoipMessageEntities.SLAMDOWN_CALLING_NUMBER_PROPERTY, "111");
            	okEvent.setProperty(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY, "1212");
            }

            try {
                mfsClient.sendTrafficEvent(okEvent);
            } catch (TrafficEventSenderException e) {
                Assert.fail("Unexpected exception caught for event " + eventName);
            }
        }
    }

    @Test
    public void testLoginAndLogoutInformationEventSuccess() {
    	final String number = "11111111";
    	final String msid = "abcd1234";
    	
    	final IDirectoryAccessSubscriber jmockSubscriber = mockery.mock(IDirectoryAccessSubscriber.class);
    	mockery.checking(new Expectations(){{
    		
    		atLeast(1).of(jmockSubscriber).getSubscriberIdentity("msid:");
    		will(returnValue(msid));
    	}});

    	final IDirectoryAccess jmockMcd = mockery.mock(IDirectoryAccess.class);
    	mockery.checking(new Expectations(){{
    		atLeast(1).of(jmockMcd).lookupSubscriber(number);
    		will(returnValue(jmockSubscriber));
    	}});


        IMfsEventManager eventManager = new MfsEventManagerStub(
                CommonMessagingAccess.getInstance(),
                jmockMcd);

        MfsClient mfsClient = new MfsClientStub(
                eventManager,
                jmockMcd,
                CommonMessagingAccess.getInstance());

        // Create with OK name
        String loginEventName = "logininformation";
        TrafficEvent loginEvent = new TrafficEvent(loginEventName);
        // Add valid properties
        loginEvent.setProperty("callednumber", number);

        try {
            mfsClient.sendTrafficEvent(loginEvent);
        } catch (TrafficEventSenderException e) {
            Assert.fail("Unexpected exception caught for event " + loginEventName + ": " + e.getMessage());
        }

    	String strPath = CommonMessagingAccess.getInstance().getMoipPrivateFolder(msid, true);

    	// Check that the stubbed "file" was created
        String separator = System.getProperty("file.separator");
        Assert.assertTrue(new File(strPath + separator + number + separator + "events" + separator + LOGGEDIN_FILENAME).exists());

        // Test the fileExists method of MfsEventManager, positively
        Assert.assertTrue(eventManager.loginFileExists(number));

        // Test the logoutinformation event
        String logoutEventName = "logoutinformation";
        TrafficEvent logoutEvent = new TrafficEvent(logoutEventName);
        // Add valid properties
        logoutEvent.setProperty("callednumber", number);

        try {
            mfsClient.sendTrafficEvent(logoutEvent);
        } catch (TrafficEventSenderException e) {
            Assert.fail("Unexpected exception caught for event " + loginEventName + ": " + e.getMessage());
        }

        // Check that the stubbed "file" was deleted
        Assert.assertFalse(new File(strPath + separator + number + separator + "events" + separator + LOGGEDIN_FILENAME).exists());

        // Test the fileExists method of MfsEventManager, negatively
        Assert.assertFalse(eventManager.loginFileExists(number));

    }

}
