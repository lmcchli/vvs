/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.profilemanager.*;
import com.mobeon.masp.profilemanager.subscription.Subscription;
import com.mobeon.masp.profilemanager.greetings.GreetingNotFoundException;
import com.mobeon.masp.profilemanager.search.ProfileStringCriteria;
import com.mobeon.masp.profilemanager.search.ProfileOrCritera;
import com.mobeon.masp.profilemanager.search.ProfileAndCritera;
import com.mobeon.masp.mediaobject.IMediaObject;
import org.jmock.Mock;

/**
 * Tests the Subscriber related functions in PlatformAccess. Those are located in the SubscriberProfileManager class.
 *
 * @author ermmaha
 */
public class PlatformAccessSubscriberProfileTest extends PlatformAccessTest {
    private String phoneNumber0 = "161074";
    private String phoneNumber1 = "161075";
    private String errorPhoneNumber = "55512345";

    protected Mock jmockCosId0;
    protected Mock jmockCosId1;


    public PlatformAccessSubscriberProfileTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        setupProfileAttributes();
        setupCOS();
    }

    /**
     * Tests the subscriberExist function. Uses multiple instances of PlatformAccess to access the function.
     * All instances should return same result and only use the IProfileManager one time for each number.
     *
     * @throws Exception
     */
    public void testSubscriberExist() throws Exception {

        //Setup expectations so that getProfile is called only once for each number.
        IProfile[] profiles0 = new IProfile[]{(IProfile) jmockProfileId0.proxy()};
        IProfile[] profiles1 = new IProfile[]{(IProfile) jmockProfileId1.proxy()};
        jmockProfileManager.expects(once()).method("getProfile").
                with(eq(new ProfileOrCritera(
                        new ProfileStringCriteria("billingnumber", phoneNumber0),
                        new ProfileStringCriteria("emmin", phoneNumber0)
                )), eq(false)).
                will(returnValue(profiles0));

        jmockProfileManager.expects(once()).method("getProfile").
                with(eq(new ProfileOrCritera(
                        new ProfileStringCriteria("billingnumber", phoneNumber1),
                        new ProfileStringCriteria("emmin", phoneNumber1)
                        )), eq(false)).
                will(returnValue(profiles1));

        PlatformAccess platformAccess1 = createPlatformAccess();
        PlatformAccess platformAccess2 = createPlatformAccess();

        boolean result = platformAccess1.subscriberExist(phoneNumber0);
        assertTrue(result);

        result = platformAccess2.subscriberExist(phoneNumber1);
        assertTrue(result);

        //test same again (should be cached and not retrieved via profile)
        jmockProfileManager.expects(never()).method("getProfile");
        result = platformAccess1.subscriberExist(phoneNumber0);
        assertTrue(result);
        result = platformAccess2.subscriberExist(phoneNumber0);
        assertTrue(result);
        result = platformAccess1.subscriberExist(phoneNumber1);
        assertTrue(result);
        result = platformAccess2.subscriberExist(phoneNumber1);
        assertTrue(result);

        //test a number that does not exist
        jmockProfileManager.stubs().method("getProfile").
                with(eq(new ProfileOrCritera(
                        new ProfileStringCriteria("billingnumber", "999000"),
                        new ProfileStringCriteria("emmin", "999000")
                        )), eq(false)).
                will(returnValue(new IProfile[0]));

        result = platformAccess1.subscriberExist("999000");
        assertFalse(result);

        //test some exceptions
        try {
            jmockProfileManager.expects(once()).method("getProfile").will(
                    throwException(new UnknownAttributeException("Unknown attribute")));
            platformAccess1.subscriberExist("999000");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEREAD, e.getMessage());
        }

        try {
            jmockProfileManager.expects(once()).method("getProfile").will(
                    throwException(new HostException("Unknown host")));
            platformAccess1.subscriberExist("999000");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEREAD, e.getMessage());
        }
    }

    /**
     * Tests the subscriberExist function. Multithreaded test.
     *
     * @throws Exception
     */
    public void testSubscriberExistMT() throws Exception {
        int size = 25;
        Thread[] threads = new Thread[size];
        final Object lock = new Object();
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    PlatformAccess platformAccess2;
                    synchronized (lock) {
                        platformAccess2 = createPlatformAccess(); // jmock invocations are not threadsafe
                    }
                    boolean result = platformAccess2.subscriberExist(phoneNumber1);
                    assertTrue(result);
                    result = platformAccess2.subscriberExist(errorPhoneNumber);
                    assertFalse(result);
                }
            });
            threads[i].start();

            PlatformAccess platformAccess1;
            synchronized (lock) {
                platformAccess1 = createPlatformAccess();
            }
            boolean result = platformAccess1.subscriberExist(phoneNumber1);
            assertTrue(result);
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * Tests the subscriberGetStringAttribute function.
     *
     * @throws Exception if test case fails.
     */
    public void testSubscriberGetStringAttribute() throws Exception {
        // test single value
        String[] result = platformAccess.subscriberGetStringAttribute(phoneNumber0, "gender");
        assertEquals("F", result[0]);
        // test multi value
        result = platformAccess.subscriberGetStringAttribute(phoneNumber0, "emfilter");
        assertArray(new String[]{"filter1", "filter2"}, result);

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberGetStringAttribute(errorPhoneNumber, "emfilter");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetStringAttribute") > -1);
            assertTrue(e.getDescription().indexOf(errorPhoneNumber) > -1);
        }

        // test invalid attribute name
        jmockProfileId0.stubs().method("getStringAttributes").with(eq("notanattribute"))
                .will(throwException(new UnknownAttributeException("Invalid attrname")));
        try {
            platformAccess.subscriberGetStringAttribute(phoneNumber0, "notanattribute");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetStringAttribute") > -1);
        }
    }

    /**
     * Tests the subscriberGetIntegerAttribute function.
     *
     * @throws Exception if test case fails.
     */
    public void testSubscriberGetIntegerAttribute() throws Exception {
        // test single value
        int[] result = platformAccess.subscriberGetIntegerAttribute(phoneNumber1, "l");
        assertEquals(5, result[0]);

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberGetIntegerAttribute(errorPhoneNumber, "l");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetIntegerAttribute") > -1);
            assertTrue(e.getDescription().indexOf(errorPhoneNumber) > -1);
        }

        // test invalid attribute name
        jmockProfileId1.stubs().method("getIntegerAttributes").with(eq("notanattribute"))
                .will(throwException(new UnknownAttributeException("Invalid attrname")));
        try {
            platformAccess.subscriberGetIntegerAttribute(phoneNumber1, "notanattribute");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetIntegerAttribute") > -1);
        }
    }

    /**
     * Tests the subscriberGetBooleanAttribute function.
     *
     * @throws Exception if test case fails.
     */
    public void testSubscriberGetBooleanAttribute() throws Exception {
        //test single value
        boolean[] result = platformAccess.subscriberGetBooleanAttribute(phoneNumber0, "faxenabled");
        assertEquals(true, result[0]);

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberGetBooleanAttribute(errorPhoneNumber, "faxenabled");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetBooleanAttribute") > -1);
            assertTrue(e.getDescription().indexOf(errorPhoneNumber) > -1);
        }

        //test invalid attribute name
        jmockProfileId0.stubs().method("getBooleanAttributes").with(eq("notanattribute"))
                .will(throwException(new UnknownAttributeException("Invalid attrname")));
        try {
            platformAccess.subscriberGetBooleanAttribute(phoneNumber0, "notanattribute");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetBooleanAttribute") > -1);
        }
    }

    /**
     * Tests the subscriberSetStringAttribute function.
     *
     * @throws Exception if test case fails
     */
    public void testSubscriberSetStringAttribute() throws Exception {
        String[] attrValue = new String[]{"value1", "value2"};

        String attrName = "emfilter";
        jmockProfileId0.expects(once()).method("setStringAttributes").with(eq(attrName), eq(attrValue));
        platformAccess.subscriberSetStringAttribute(phoneNumber0, attrName, attrValue);

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberSetStringAttribute(errorPhoneNumber, "emfilter", attrValue);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberSetStringAttribute") > -1);
            assertTrue(e.getDescription().indexOf(errorPhoneNumber) > -1);
        }

        // test invalid attribute name
        jmockProfileId0.expects(once()).method("setStringAttributes").with(eq("notanattribute"), eq(attrValue)).
                will(throwException(new UnknownAttributeException("Invalid attrname")));
        try {
            platformAccess.subscriberSetStringAttribute(phoneNumber0, "notanattribute", attrValue);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEWRITE, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberSetStringAttribute") > -1);
        }
    }

    /**
     * Tests the subscriberSetIntegerAttribute function.
     *
     * @throws Exception if test case fails
     */
    public void testSubscriberSetIntegerAttribute() throws Exception {
        int[] attrValue = new int[]{1, 2};

        String attrName = "emfilter";
        jmockProfileId0.expects(once()).method("setIntegerAttributes").with(eq(attrName), eq(attrValue));
        platformAccess.subscriberSetIntegerAttribute(phoneNumber0, attrName, attrValue);

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberSetIntegerAttribute(errorPhoneNumber, attrName, attrValue);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberSetIntegerAttribute") > -1);
            assertTrue(e.getDescription().indexOf(errorPhoneNumber) > -1);
        }

        // test invalid attribute name
        jmockProfileId0.expects(once()).method("setIntegerAttributes").with(eq("notanattribute"), eq(attrValue)).
                will(throwException(new UnknownAttributeException("Invalid attrname")));
        try {
            platformAccess.subscriberSetIntegerAttribute(phoneNumber0, "notanattribute", attrValue);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEWRITE, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberSetIntegerAttribute") > -1);
        }
    }

    /**
     * Tests the subscriberSetBooleanAttribute function.
     *
     * @throws Exception if test case fails
     */
    public void testSubscriberSetBooleanAttribute() throws Exception {
        boolean[] attrValue = new boolean[]{true, false};

        String attrName = "emfilter";
        jmockProfileId0.expects(once()).method("setBooleanAttributes").with(eq(attrName), eq(attrValue));
        platformAccess.subscriberSetBooleanAttribute(phoneNumber0, attrName, attrValue);

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberSetBooleanAttribute(errorPhoneNumber, attrName, attrValue);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberSetBooleanAttribute") > -1);
            assertTrue(e.getDescription().indexOf(errorPhoneNumber) > -1);
        }

        // test invalid attribute name
        jmockProfileId0.expects(once()).method("setBooleanAttributes").with(eq("notanattribute"), eq(attrValue)).
                will(throwException(new UnknownAttributeException("Invalid attrname")));
        try {
            platformAccess.subscriberSetBooleanAttribute(phoneNumber0, "notanattribute", attrValue);
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEWRITE, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberSetBooleanAttribute") > -1);
        }
    }

    /**
     * Tests the subscriberGetCosStringAttribute function.
     *
     * @throws Exception if test case fails.
     */
    public void testSubscriberGetCosStringAttribute() throws Exception {
        // test value
        String[] result = platformAccess.subscriberGetCosStringAttribute(phoneNumber0, "emservicedn");
        assertArray(new String[]{"emservicedn1", "emservicedn2"}, result);

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberGetCosStringAttribute(errorPhoneNumber, "emfilter");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetCosStringAttribute") > -1);
            assertTrue(e.getDescription().indexOf(errorPhoneNumber) > -1);
        }

        // test invalid attribute name
        jmockCosId0.expects(once()).method("getStringAttributes").with(eq("notanattribute"))
                .will(throwException(new UnknownAttributeException("Invalid attrname")));
        try {
            platformAccess.subscriberGetCosStringAttribute(phoneNumber0, "notanattribute");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetCosStringAttribute") > -1);
        }
    }

    /**
     * Tests the subscriberGetCosIntegerAttribute function.
     *
     * @throws Exception if test case fails.
     */
    public void testSubscriberGetCosIntegerAttribute() throws Exception {
        // test single value
        int[] result = platformAccess.subscriberGetCosIntegerAttribute(phoneNumber0, "maxloginlockout");
        assertEquals(6, result[0]);

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberGetCosIntegerAttribute(errorPhoneNumber, "l");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetCosIntegerAttribute") > -1);
            assertTrue(e.getDescription().indexOf(errorPhoneNumber) > -1);
        }

        // test invalid attribute name
        jmockCosId0.expects(once()).method("getIntegerAttributes").with(eq("notanattribute"))
                .will(throwException(new UnknownAttributeException("Invalid attrname")));
        try {
            platformAccess.subscriberGetCosIntegerAttribute(phoneNumber0, "notanattribute");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetCosIntegerAttribute") > -1);
        }
    }

    /**
     * Tests the subscriberGetCosBooleanAttribute function.
     *
     * @throws Exception if test case fails.
     */
    public void testSubscriberGetCosBooleanAttribute() throws Exception {
        // test single value
        boolean[] result = platformAccess.subscriberGetCosBooleanAttribute(phoneNumber0, "autoplay");
        assertEquals(true, result[0]);

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberGetCosBooleanAttribute(errorPhoneNumber, "autoplay");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetCosBooleanAttribute") > -1);
            assertTrue(e.getDescription().indexOf(errorPhoneNumber) > -1);
        }

        // test invalid attribute name
        jmockCosId0.expects(once()).method("getBooleanAttributes").with(eq("notanattribute"))
                .will(throwException(new UnknownAttributeException("Invalid attrname")));
        try {
            platformAccess.subscriberGetCosBooleanAttribute(phoneNumber0, "notanattribute");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetCosBooleanAttribute") > -1);
        }
    }

    /**
     * Tests the subscriberGetGreeting function.
     *
     * @throws Exception if testcase fails.
     */
    public void testSubscriberGetGreeting() throws Exception {
        Mock jmockGreetingId0 = mock(IMediaObject.class);

        jmockProfileId0.expects(once()).method("getGreeting").withAnyArguments().
                will(returnValue(jmockGreetingId0.proxy()));

        IMediaObject result = platformAccess.subscriberGetGreeting(phoneNumber0, "allcalls", VOICE, null);
        assertEquals(jmockGreetingId0.proxy(), result);

        // test exceptions
        jmockProfileId0.expects(once()).method("getGreeting").withAnyArguments().
                will(throwException(new GreetingNotFoundException("greeting not found")));
        try {
            platformAccess.subscriberGetGreeting(phoneNumber0, "cdg", VOICE, "123"); // test a cdg number too
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetGreeting") > -1);
        }

        // test errors from MS
        jmockProfileId0.expects(once()).method("getGreeting").withAnyArguments().
                will(throwException(new ProfileManagerException("Read timed out")));
        try {
            platformAccess.subscriberGetGreeting(phoneNumber0, "allcalls", VOICE, null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetGreeting") > -1);
        }

        // test invalid mediatype (will throw IllegalArgumentException from GreetingTypeUtil)
        jmockProfileId0.expects(never()).method("getGreeting");
        try {
            platformAccess.subscriberGetGreeting(phoneNumber0, "allcalls", "wrongmediatype", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetGreeting") > -1);
        }

        // test invalid greetingtype (null) (will throw IllegalArgumentException from GreetingTypeUtil)
        jmockProfileId0.expects(never()).method("getGreeting");
        try {
            platformAccess.subscriberGetGreeting(phoneNumber0, null, "wrongmediatype", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetGreeting") > -1);
        }

        // test invalid mediatype (null) (will throw IllegalArgumentException from GreetingTypeUtil)
        jmockProfileId0.expects(never()).method("getGreeting");
        try {
            platformAccess.subscriberGetGreeting(phoneNumber0, "allcalls", null, null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetGreeting") > -1);
        }

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberGetGreeting(errorPhoneNumber, "allcalls", "wrongmediatype", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetGreeting") > -1);
        }
    }

    /**
     * Tests the subscriberSetGreeting function.
     *
     * @throws Exception if testcase fails
     */
    public void testSubscriberSetGreeting() throws Exception {
        Mock jmockGreetingId0 = mock(IMediaObject.class);

        jmockProfileId0.expects(once()).method("setGreeting").withAnyArguments();

        platformAccess.subscriberSetGreeting(phoneNumber0, "allcalls", VOICE, null, (IMediaObject) jmockGreetingId0.proxy());

        // test exceptions
        jmockProfileId0.stubs().method("setGreeting").will(throwException(new UnknownAttributeException("Invalid greeting")));
        try {
            platformAccess.subscriberSetGreeting(phoneNumber0, "allcalls", VOICE, "", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberSetGreeting") > -1);
        }

        // test invalid mediatype (will throw IllegalArgumentException from GreetingTypeUtil)
        jmockProfileId0.expects(never()).method("setGreeting");
        try {
            platformAccess.subscriberSetGreeting(phoneNumber0, "allcalls", "wrongmediatype", "", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberSetGreeting") > -1);
        }

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberSetGreeting(errorPhoneNumber, "allcalls", "wrongmediatype", "", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberSetGreeting") > -1);
        }
    }

    /**
     * Tests the subscriberGetSpokenName function.
     *
     * @throws Exception if testcase fails.
     */
    public void testSubscriberGetSpokenName() throws Exception {
        Mock jmockSpokenNameId0 = mock(IMediaObject.class);
        Mock jmockSpokenNameId1 = mock(IMediaObject.class);

        jmockProfileId0.stubs().method("getSpokenName").withAnyArguments().will(returnValue(jmockSpokenNameId0.proxy()));
        jmockProfileId1.stubs().method("getSpokenName").withAnyArguments().will(returnValue(jmockSpokenNameId1.proxy()));

        IMediaObject result = platformAccess.subscriberGetSpokenName(phoneNumber0, VOICE);
        assertEquals(jmockSpokenNameId0.proxy(), result);

        // test exceptions
        jmockProfileId0.expects(once()).method("getSpokenName").withAnyArguments().
                will(throwException(new GreetingNotFoundException("Spoken name not found")));
        try {
            platformAccess.subscriberGetSpokenName(phoneNumber0, VOICE);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetSpokenName") > -1);
        }

        jmockProfileId0.expects(once()).method("getSpokenName").withAnyArguments().
                will(throwException(new ProfileManagerException("MS Error")));
        try {
            platformAccess.subscriberGetSpokenName(phoneNumber0, VIDEO);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetSpokenName") > -1);
        }

        // test invalid mediatype (will throw IllegalArgumentException from GreetingTypeUtil)
        try {
            platformAccess.subscriberGetSpokenName(phoneNumber0, "Wrongmedia");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetSpokenName") > -1);
        }

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberGetSpokenName(errorPhoneNumber, "Wrongmedia");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberGetSpokenName") > -1);
        }
    }

    /**
     * Tests the subscriberSetSpokenName function.
     *
     * @throws Exception if testcase fails.
     */
    public void testSubscriberSetSpokenName() throws Exception {
        Mock jmockSpokenNameId0 = mock(IMediaObject.class);

        jmockProfileId0.expects(once()).method("setSpokenName").withAnyArguments();

        platformAccess.subscriberSetSpokenName(phoneNumber0, VOICE, (IMediaObject) jmockSpokenNameId0.proxy());

        //test exceptions
        jmockProfileId0.expects(once()).method("setSpokenName").will(throwException(new UnknownAttributeException("Spoken name attribute not found")));
        try {
            platformAccess.subscriberSetSpokenName(phoneNumber0, VOICE, null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberSetSpokenName") > -1);
        }

        // test invalid mediatype (will throw IllegalArgumentException from GreetingTypeUtil)
        try {
            platformAccess.subscriberSetSpokenName(phoneNumber0, "Wrongmedia", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberSetSpokenName") > -1);
        }

        // test invalid user (phonenumber not found)
        try {
            platformAccess.subscriberSetSpokenName(errorPhoneNumber, "Wrongmedia", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("subscriberSetSpokenName") > -1);
        }
    }

//    /**
//     * Tests the systemGetSubscribers function.
//     *
//     * @throws Exception if testcase fails.
//     */
//    public void testSystemGetSubscribers() throws Exception {
//    	Mock jmockProfile1 = mock(IProfile.class);
//        jmockProfile1.expects(once()).method("getStringAttribute").will(returnValue("998877"));
//        Mock jmockProfile2 = mock(IProfile.class);
//        jmockProfile2.expects(once()).method("getStringAttribute").will(returnValue("332211"));
//        IProfile[] profiles = new IProfile[]{(IProfile) jmockProfile1.proxy(), (IProfile) jmockProfile2.proxy()};
//
// 
//        jmockProfileManager.expects(once()).method("getProfile").
//        		with(eq(new ProfileStringCriteria("gender","F")), eq(false)).will(returnValue(profiles));
//
//        String[] numbers = platformAccess.systemGetSubscribers("gender", "F");
//        assertArray(new String[]{"998877", "332211"}, numbers);
//       
//        // test no subscriber found (will return empty array)
//        jmockProfileManager.expects(once()).method("getProfile").
//                with(isA(ProfileStringCriteria.class), eq(false)).will(returnValue(new IProfile[0]));
//        numbers = platformAccess.systemGetSubscribers("gender", "F");
//        assertArray(new String[0], numbers);
//
//        // test some exceptions
//        jmockProfileManager.expects(once()).method("getProfile").
//                will(throwException(new UnknownAttributeException("Unknown attribute")));
//        try {
//            platformAccess.systemGetSubscribers("gender", "F");
//            fail("Expected PlatformAccessException");
//        } catch (PlatformAccessException e) {
//            assertEquals(EventType.PROFILEREAD, e.getMessage());
//        }
//
//        // set the community too
//        String community = "C1";
//        platformAccess.systemSetCommunityRestriction(community);
//
//        jmockProfileManager.expects(once()).method("getProfile").
//                will(throwException(new HostException("Unknown host")));
//        try {
//            platformAccess.systemGetSubscribers("gender", "F");
//            fail("Expected PlatformAccessException");
//        } catch (PlatformAccessException e) {
//            assertEquals(EventType.PROFILEREAD, e.getMessage());
//        }
//    }

    /**
     * Tests the systemSetLimitSearchScope function.
     *
     * @throws Exception if testcase fails.
     */
    public void testSystemSetPartionRestriction() throws Exception {
        // Setup a mocked profile
        Mock jmockProfile1 = mock(IProfile.class);
        jmockProfile1.expects(atLeastOnce()).method("getStringAttributes").will(returnValue(new String[]{"555555"}));
        IProfile[] profiles = new IProfile[]{(IProfile) jmockProfile1.proxy()};

        // expect a call with the limitscope set to true
        jmockProfileManager.expects(once()).method("getProfile").
                with(isA(ProfileOrCritera.class), eq(true)).will(returnValue(profiles));

        platformAccess.systemSetPartitionRestriction(true);

        String[] numbers = platformAccess.subscriberGetStringAttribute("123", "F");
        assertArray(new String[]{"555555"}, numbers);

        // expect a call with the limitscope set to false
        jmockProfileManager.expects(once()).method("getProfile").
                with(isA(ProfileOrCritera.class), eq(false)).will(returnValue(profiles));

        platformAccess.systemSetPartitionRestriction(false);

        jmockProfile1.expects(atLeastOnce()).method("getStringAttributes").will(returnValue(new String[]{"555555"}));
        numbers = platformAccess.subscriberGetStringAttribute("123", "F");
        assertArray(new String[]{"555555"}, numbers);
    }

    
//    /**
//     * Tests the systemSetCommunityRestriction with the systemGetSubscribers function.
//     *
//     * @throws Exception if testcase fails.
//     */
//    public void testSystemSetCommunityRestrictionWithGetSubscribers() throws Exception {
//        // with community restriction
//        platformAccess.systemSetCommunityRestriction("C1");
//        Mock jmockProfile = mock(IProfile.class);
//        jmockProfile.expects(atLeastOnce()).method("getStringAttribute").will(returnValue("556677"));
//        IProfile[] profiles = new IProfile[]{(IProfile) jmockProfile.proxy()};
//     
//        jmockProfileManager.expects(atLeastOnce()).method("getProfile").
//        		with(eq(new ProfileAndCritera(
//    						new ProfileStringCriteria("community", "C1"),
//    						new ProfileStringCriteria("gender", "F"))),
//    			     eq(false)).will(returnValue(profiles)); 
//        String [] numbers = platformAccess.systemGetSubscribers("gender", "F");
//        assertArray(new String[]{"556677"}, numbers);
//        
//        // w/o community restriction
//        platformAccess.systemSetCommunityRestriction("C1");
//        platformAccess.systemClearCommunityRestriction();
//        jmockProfileManager.expects(atLeastOnce()).method("getProfile").
//		with(eq(new ProfileStringCriteria("gender", "F")),
//		     eq(false)).will(returnValue(profiles)); 
//        numbers = platformAccess.systemGetSubscribers("gender", "F");
//        assertArray(new String[]{"556677"}, numbers);
//    }
 
    /**
     * Tests the close function.
     *
     * @throws Exception
     */
    public void testClose() throws Exception {
        //try to call close before any IProfile(s) is loaded
        jmockProfileId0.expects(never()).method("close").withNoArguments();
        platformAccess.close();

        //try the close command, load the profiles first
        jmockProfileId0.expects(once()).method("close").withNoArguments();
        jmockProfileId1.expects(once()).method("close").withNoArguments();
        platformAccess.subscriberExist(phoneNumber0);
        platformAccess.subscriberExist(phoneNumber1);
        platformAccess.close();
    }

    /**
     * Tests the subscriberGetDistributionListIds function.
     *
     * @throws Exception
     */
    public void testSubscriberGetDistributionListIds() throws Exception {
        // Test exception
        jmockProfileId0.expects(once()).method("getDistributionLists").will(throwException(new ProfileManagerException("profilemanagerexception")));
        try {
            platformAccess.subscriberGetDistributionListIds(phoneNumber0);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEREAD, e.getMessage());
            assertTrue(e.getDescription().indexOf("phoneNumber=" + phoneNumber0) > -1);
        }

        Mock jmockDistributionList1 = mock(IDistributionList.class);
        jmockDistributionList1.expects(once()).method("getID").will(returnValue("1"));
        Mock jmockDistributionList2 = mock(IDistributionList.class);
        jmockDistributionList2.expects(once()).method("getID").will(returnValue("2"));

        IDistributionList[] lists = getDistributionLists(jmockDistributionList1, jmockDistributionList2);

        jmockProfileId0.expects(once()).method("getDistributionLists").will(returnValue(lists));

        String[] ids = platformAccess.subscriberGetDistributionListIds(phoneNumber0);
        assertArray(new String[]{"2", "1"}, ids);

        // Lists should be cached
        ids = platformAccess.subscriberGetDistributionListIds(phoneNumber0);
        assertArray(new String[]{"2", "1"}, ids);

        //test invalid phonenumber
        jmockProfileId0.expects(never()).method("getDistributionLists");
        try {
            platformAccess.subscriberGetDistributionListIds(errorPhoneNumber);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("phoneNumber=" + errorPhoneNumber) > -1);
        }
    }

    /**
     * Tests the subscriberAddDistributionList function.
     *
     * @throws Exception
     */
    public void testSubscriberAddDistributionList() throws Exception {
        String distList = "2";
        Mock jmockDistributionList = mock(IDistributionList.class, "DistributionList2");
        jmockProfileId0.expects(once()).method("getDistributionLists").
                will(returnValue(getDistributionLists()));
        jmockProfileId0.expects(once()).method("createDistributionList").with(eq(distList)).
                will(returnValue(jmockDistributionList.proxy()));
        platformAccess.subscriberAddDistributionList(phoneNumber0, distList);

        // Distribution list should be cached
        jmockDistributionList.expects(once()).method("getMembers").will(returnValue(new String[0]));
        String[] strings = platformAccess.distributionListGetMembers(phoneNumber0, distList);
        assertEquals("List should have 0 members", 0, strings.length);

        distList = "3";
        // test exception
        jmockProfileId0.expects(once()).method("createDistributionList").
                will(throwException(new ProfileManagerException("profilemanagerexception")));
        try {
            platformAccess.subscriberAddDistributionList(phoneNumber0, "1");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEWRITE, e.getMessage());
        }

        // Creating additional lists does not invoke getDistributionLists
        jmockProfileId0.expects(once()).method("createDistributionList").with(eq(distList));
        platformAccess.subscriberAddDistributionList(phoneNumber0, distList);

        // Test adding existing distribution list
        try {
            platformAccess.subscriberAddDistributionList(phoneNumber0, distList);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEWRITE, e.getMessage());
            assertTrue(e.getDescription().indexOf("distListNumber=" + distList) > -1);
        }

        //test invalid phonenumber
        try {
            platformAccess.subscriberAddDistributionList(errorPhoneNumber, distList);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("phoneNumber=" + errorPhoneNumber) > -1);
        }
    }

    /**
     * Tests the subscriberDeleteDistributionList function.
     *
     * @throws Exception if testcase fails.
     */
    public void testSubscriberDeleteDistributionList() throws Exception {
        Mock jmockDistributionList1 = mock(IDistributionList.class, "DistList1");
        jmockDistributionList1.expects(once()).method("getID").will(returnValue("1"));
        Mock jmockDistributionList2 = mock(IDistributionList.class, "DistList2");
        jmockDistributionList2.expects(once()).method("getID").will(returnValue("2"));
        Mock jmockDistributionList3 = mock(IDistributionList.class, "DistList3");
        jmockDistributionList3.expects(once()).method("getID").will(returnValue("3"));
        IDistributionList[] distLists = getDistributionLists(jmockDistributionList1, jmockDistributionList2, jmockDistributionList3);
        jmockProfileId0.expects(once()).method("getDistributionLists").will(returnValue(distLists));
        jmockProfileId0.expects(once()).method("deleteDistributionList").with(eq(jmockDistributionList1.proxy()));
        platformAccess.subscriberDeleteDistributionList(phoneNumber0, "1");

        // Deleting additional lists does not invoke getDistributionLists
        jmockProfileId0.expects(once()).method("deleteDistributionList").with(eq(jmockDistributionList2.proxy()));
        platformAccess.subscriberDeleteDistributionList(phoneNumber0, "2");

        // Test nonexisting distribution list, distribution list should not be cached
        try {
            platformAccess.subscriberDeleteDistributionList(phoneNumber0, "1");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("distListNumber=1") > -1);
        }

        // test exception
        jmockProfileId0.expects(once()).method("deleteDistributionList").
                will(throwException(new ProfileManagerException("profilemanagerexception")));
        try {
            platformAccess.subscriberDeleteDistributionList(phoneNumber0, "3");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEWRITE, e.getMessage());
        }

        // test invalid phonenumber
        try {
            platformAccess.subscriberDeleteDistributionList(errorPhoneNumber, "2");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("phoneNumber=" + errorPhoneNumber) > -1);
        }
    }

    /**
     * Tests the distributionListAddMember function.
     *
     * @throws Exception if testcase fails.
     */
    public void testDistributionListAddMember() throws Exception {
        Mock jmockDistributionList = mock(IDistributionList.class, "DistList2");
        String distList = "2";
        jmockDistributionList.expects(once()).method("getID").will(returnValue(distList));
        jmockDistributionList.expects(once()).method("addMember").with(eq("a@b.c"));
        jmockProfileId0.expects(once()).method("getDistributionLists").
                will(returnValue(getDistributionLists(jmockDistributionList)));
        platformAccess.distributionListAddMember(phoneNumber0, distList, "a@b.c");

        // Adding additional members does not invoke getDistributionLists
        jmockDistributionList.expects(once()).method("addMember").with(eq("a@b.c"));
        platformAccess.distributionListAddMember(phoneNumber0, distList, "a@b.c");

        // Test nonexisting distribution list
        try {
            platformAccess.distributionListAddMember(phoneNumber0, "nonexisting", "a@b.c");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("distListNumber=nonexisting") > -1);
        }

        // test exception
        jmockDistributionList.expects(once()).method("addMember").with(eq("a@b.c")).
                will(throwException(new ProfileManagerException("profilemanagerexception")));
        try {
            platformAccess.distributionListAddMember(phoneNumber0, distList, "a@b.c");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEREAD, e.getMessage());
            assertTrue(e.getDescription().indexOf("distListNumber=2") > -1);
        }

        //test invalid phonenumber
        try {
            platformAccess.distributionListAddMember(errorPhoneNumber, distList, "a@b.c");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("phoneNumber=" + errorPhoneNumber) > -1);
        }
    }

    /**
     * Tests the distributionListDeleteMember function.
     *
     * @throws Exception
     */
    public void testDistributionListDeleteMember() throws Exception {
        Mock jmockDistributionList = mock(IDistributionList.class, "DistList2");
        String distList = "2";
        jmockDistributionList.expects(once()).method("getID").will(returnValue(distList));
        jmockDistributionList.expects(once()).method("removeMember").with(eq("a@b.c"));
        jmockProfileId0.expects(once()).method("getDistributionLists").
                will(returnValue(getDistributionLists(jmockDistributionList)));
        platformAccess.distributionListDeleteMember(phoneNumber0, distList, "a@b.c");

        // Removing additional members does not invoke getDistributionLists
        jmockDistributionList.expects(once()).method("removeMember").with(eq("a@b.c"));
        platformAccess.distributionListDeleteMember(phoneNumber0, distList, "a@b.c");

        // Test nonexisting distribution list
        try {
            platformAccess.distributionListDeleteMember(phoneNumber0, "nonexisting", "a@b.c");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("distListNumber=nonexisting") > -1);
        }

        //test exception
        jmockDistributionList.expects(once()).method("removeMember").with(eq("a@b.c")).
                will(throwException(new ProfileManagerException("profilemanagerexception")));
        try {
            platformAccess.distributionListDeleteMember(phoneNumber0, distList, "a@b.c");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEWRITE, e.getMessage());
            assertTrue(e.getDescription().indexOf("distListNumber=2") > -1);
        }

        //test invalid phonenumber
        try {
            platformAccess.distributionListDeleteMember(errorPhoneNumber, distList, "a@b.c");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("phoneNumber=" + errorPhoneNumber) > -1);
        }
    }

    /**
     * Tests the distributionListGetMembers function.
     *
     * @throws Exception
     */
    public void testDistributionListGetMembers() throws Exception {
        String distList = "2";
        // Test exception
        jmockProfileId0.expects(once()).method("getDistributionLists").will(throwException(new ProfileManagerException("profilemanagerexception")));
        try {
            platformAccess.distributionListGetMembers(phoneNumber0, distList);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEREAD, e.getMessage());
            assertTrue(e.getDescription().indexOf("distListNumber=" + distList) > -1);
        }

        Mock jmockDistributionList = mock(IDistributionList.class);
        jmockDistributionList.expects(once()).method("getID").will(returnValue(distList));
        jmockDistributionList.expects(once()).method("getMembers").will(returnValue(new String[]{"a@b.c", "x@y.z"}));
        jmockProfileId0.expects(once()).method("getDistributionLists").
                will(returnValue(getDistributionLists(jmockDistributionList)));
        String[] members = platformAccess.distributionListGetMembers(phoneNumber0, distList);
        assertArray(new String[]{"a@b.c", "x@y.z"}, members);

        // Distribution list should be cached
        jmockDistributionList.expects(once()).method("getMembers").will(returnValue(new String[]{"a@b.c", "x@y.z"}));
        members  = platformAccess.distributionListGetMembers(phoneNumber0, distList);
        assertArray(new String[]{"a@b.c", "x@y.z"}, members);

        // Test nonexisting distribution list
        try {
            platformAccess.distributionListGetMembers(phoneNumber0, "nonexisting");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("distListNumber=nonexisting") > -1);
        }

        //test invalid phonenumber
        try {
            platformAccess.distributionListGetMembers(errorPhoneNumber, distList);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("phoneNumber=" + errorPhoneNumber) > -1);
        }
    }

    /**
     * Tests the distributionListGetSpokenName function.
     *
     * @throws Exception if testcase fails.
     */
    public void testDistributionListGetSpokenName() throws Exception {
        String distList = "2";
        Mock jmockSpokenName = mock(IMediaObject.class);
        Mock jmockDistributionList = mock(IDistributionList.class, "DistList2");
        jmockDistributionList.expects(once()).method("getID").will(returnValue(distList));
        jmockDistributionList.expects(once()).method("getSpokenName").will(returnValue(jmockSpokenName.proxy()));

        jmockProfileId0.expects(once()).method("getDistributionLists").
                will(returnValue(getDistributionLists(jmockDistributionList)));

        IMediaObject spokenName = platformAccess.distributionListGetSpokenName(phoneNumber0, distList);
        assertEquals(jmockSpokenName.proxy(), spokenName);

        // test empty (return null)
        jmockDistributionList.expects(once()).method("getSpokenName").will(returnValue(null));
        try {
            platformAccess.distributionListGetSpokenName(phoneNumber0, "2");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
        }

        // Test nonexisting distribution list
        try {
            platformAccess.distributionListGetSpokenName(phoneNumber0, "nonexisting");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("distListNumber=nonexisting") > -1);
        }

        // test exception
        jmockDistributionList.expects(once()).method("getSpokenName").
                will(throwException(new ProfileManagerException("profilemanagerexception")));
        try {
            platformAccess.distributionListGetSpokenName(phoneNumber0, "2");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEREAD, e.getMessage());
            assertTrue(e.getDescription().indexOf("distListNumber=2") > -1);
        }

        // test invalid phonenumber
        try {
            platformAccess.distributionListGetSpokenName(errorPhoneNumber, "2");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("phoneNumber=" + errorPhoneNumber) > -1);
        }
    }

    /**
     * Tests the distributionListSetSpokenName function.
     *
     * @throws Exception
     */
    public void testDistributionListSetSpokenName() throws Exception {
        String distList = "2";
        Mock jmockSpokenName = mock(IMediaObject.class);
        Mock jmockDistributionList = mock(IDistributionList.class, "DistList2");
        jmockDistributionList.expects(once()).method("getID").will(returnValue(distList));
        jmockDistributionList.expects(once()).method("setSpokenName").with(eq(jmockSpokenName.proxy()));

        jmockProfileId0.expects(once()).method("getDistributionLists").
                will(returnValue(getDistributionLists(jmockDistributionList)));

        platformAccess.distributionListSetSpokenName(phoneNumber0, "2", (IMediaObject)jmockSpokenName.proxy());

        // Test nonexisting distribution list
        try {
            platformAccess.distributionListSetSpokenName(phoneNumber0, "nonexisting", (IMediaObject)jmockSpokenName.proxy());
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("distListNumber=nonexisting") > -1);
        }

        //test exception
        jmockDistributionList.expects(once()).method("setSpokenName").with(eq(jmockSpokenName.proxy())).
                will(throwException(new ProfileManagerException("profilemanagerexception")));
        try {
            platformAccess.distributionListSetSpokenName(phoneNumber0, "2", (IMediaObject)jmockSpokenName.proxy());
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.PROFILEWRITE, e.getMessage());
            assertTrue(e.getDescription().indexOf("distListNumber=2") > -1);
        }

        //test invalid phonenumber
        try {
            platformAccess.distributionListSetSpokenName(errorPhoneNumber, "2", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("phoneNumber=" + errorPhoneNumber) > -1);
        }
    }

//    /**
//     * Tests the subscriberCreate function.
//     *
//     * @throws Exception
//     */
//    public void testSubscriberCreate() throws Exception {
//        String adminUid = "adminuid";
//        String cosName = "cosname";
//        String[] attrNames = new String[] {"name1", "name2"};
//        String[] attrValues = new String[] {"value1", "value2"};
//
//        // test the method that does not take a cosName
//        jmockProfileManager.expects(once()).method("createSubscription").with(isA(Subscription.class), eq(adminUid));
//
//        platformAccess.subscriberCreate(attrNames, attrValues, adminUid, null);
//
//        // test the method that does take a cosName
//        jmockProfileManager.expects(once()).method("createSubscription").with(isA(Subscription.class), eq(adminUid), eq(cosName));
//
//        platformAccess.subscriberCreate(attrNames, attrValues, adminUid, cosName);
//
//        // test exceptions
//        jmockProfileManager.expects(once()).method("createSubscription").
//                will(throwException(new ProfileManagerException("Error when creating subcriber")));
//        try {
//            platformAccess.subscriberCreate(attrNames, attrValues, adminUid, cosName);
//            fail("Expected PlatformAccessException");
//        } catch (PlatformAccessException e) {
//            assertEquals(EventType.SYSTEMERROR, e.getMessage());
//        }
//
//        // test wrong length
//        try {
//            platformAccess.subscriberCreate(new String[] {"name1"}, attrValues, adminUid, cosName);
//            fail("Expected PlatformAccessException");
//        } catch (PlatformAccessException e) {
//            assertEquals(EventType.SYSTEMERROR, e.getMessage());
//        }
//    }

//    /**
//     * Tests the subscriberDelete function.
//     *
//     * @throws Exception
//     */
//    public void testSubscriberDelete() throws Exception {
//        String adminUid = "adminuid";
//        String telephonenumber = "0702660291";
//
//        jmockProfileManager.expects(once()).method("deleteSubscription").with(isA(Subscription.class), eq(adminUid));
//
//        platformAccess.subscriberDelete(telephonenumber, adminUid);
//
//        // test exceptions
//        jmockProfileManager.expects(once()).method("deleteSubscription").
//                will(throwException(new ProfileManagerException("Error when deleting subcriber")));
//        try {
//            platformAccess.subscriberDelete(telephonenumber, adminUid);
//            fail("Expected PlatformAccessException");
//        } catch (PlatformAccessException e) {
//            assertEquals(EventType.SYSTEMERROR, e.getMessage());
//        }
//    }

    private void setupProfileAttributes() {
        jmockProfileId0.stubs().method("getStringAttributes").with(eq("gender")).will(returnValue(new String[]{"F"}));
        jmockProfileId0.stubs().method("getStringAttributes").with(eq("emfilter")).
                will(returnValue(new String[]{"filter1", "filter2"}));

        jmockProfileId0.stubs().method("getBooleanAttributes").with(eq("faxenabled")).
                will(returnValue(new boolean[]{true}));

        jmockProfileId1.stubs().method("getIntegerAttributes").with(eq("l")).
                will(returnValue(new int[]{5}));
    }

    private void setupCOS() {
        jmockCosId0 = mock(ICos.class);
        jmockCosId1 = mock(ICos.class);

        jmockProfileId0.stubs().method("getCos").withNoArguments().will(returnValue(jmockCosId0.proxy()));
        jmockProfileId1.stubs().method("getCos").withNoArguments().will(returnValue(jmockCosId1.proxy()));

        jmockCosId0.stubs().method("getStringAttributes").with(eq("emservicedn")).
                will(returnValue(new String[]{"emservicedn1", "emservicedn2"}));

        jmockCosId0.stubs().method("getIntegerAttributes").with(eq("maxloginlockout")).
                will(returnValue(new int[]{6}));

        jmockCosId0.stubs().method("getBooleanAttributes").with(eq("autoplay")).
                will(returnValue(new boolean[]{true}));
    }


    private IDistributionList[] getDistributionLists(Mock... mockDistLists) {
        IDistributionList[] distLists = new IDistributionList[mockDistLists.length];
        for (int i = 0; i < mockDistLists.length; i++) {
            distLists[i] = (IDistributionList)mockDistLists[i].proxy();
        }
        return distLists;
    }

    public static Test suite() {
        return new TestSuite(PlatformAccessSubscriberProfileTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
