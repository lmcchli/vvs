/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.mobeon.masp.execution_engine.platformaccess.util.TimeUtil;
import com.mobeon.masp.mailbox.IStorableMessage;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.IProfile;
import com.mobeon.masp.profilemanager.search.ProfileOrCritera;
import com.mobeon.masp.profilemanager.search.ProfileStringCriteria;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

import java.util.Calendar;
import java.util.Date;

/**
 * Test the Storable message functions on PlatformAccess
 *
 * @author ermmaha
 */
public class PlatformAccessStorableMessageTest extends PlatformAccessMailboxBaseTest {
    private Mock jmockStorableMessage0;

    private String ADDRESS_OF_PROFILE0 = "singlerecipient@mobeon.com";
    private String MAILHOST_OF_PROFILE0 = "the.host.company.com";
    private String ADDRESS_OF_PROFILE1 = "singlerecipient2@mobeon.com";
    private String MAILHOST_OF_PROFILE1 = MAILHOST_OF_PROFILE0;
    private String ADDRESS_OF_PROFILE2 = "singlerecipient3@mobeon.com";
    private String MAILHOST_OF_PROFILE2 = "anotherhost.company.com";

    private String ADDRESS_OF_PROFILE3 = "singlerecipient4@mobeon.com";
    private String MAILHOST_OF_PROFILE3 = "";

    private String ADDRESS_OF_PROFILE4 = "singlerecipient433@mobeon.com";
    private String MAILHOST_OF_PROFILE4 = null;

    private String ADDRESS_OF_PROFILE5 = null;
    private String MAILHOST_OF_PROFILE5 = "yetanotherhost.company.com";

    protected Mock jmockProfileId2Local;
    protected Mock jmockProfileId3HasNoMailhost;
    protected Mock jmockProfileId4HasNullMailhost;
    protected Mock jmockProfileId5HasNullMail;

    public PlatformAccessStorableMessageTest(String name) {
        super(name);

        jmockStorableMessage0 = mock(IStorableMessage.class);
        jmockIStorableMessageFactory.stubs().method("create").withNoArguments().
                will(returnValue(jmockStorableMessage0.proxy()));
        jmockProfileId2Local = mock(IProfile.class);
        jmockProfileId3HasNoMailhost = mock(IProfile.class);
        jmockProfileId4HasNullMailhost = mock(IProfile.class);
        jmockProfileId5HasNullMail = mock(IProfile.class);
        setupProfileLocal();
        setupProfileAttributesLocal();
    }

    /**
     * Tests the messageCreateNew function. Asserts the id's that are returned.
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageCreateNew() throws Exception {
        int storableMessageId = platformAccess.messageCreateNew();
        assertEquals(0, storableMessageId);

        storableMessageId = platformAccess.messageCreateNew();
        assertEquals(1, storableMessageId);

        jmockIStorableMessageFactory.expects(once()).method("create").will(
                throwException(new MailboxException("Could not create IStrorableMessage")));
        try {
            platformAccess.messageCreateNew();
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
        }
    }

    /**
     * Tests the messageSetStorableProperty function.
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageSetStorableProperty() throws Exception {
        // setup the expected properties
        String address = "ermmaha@mobeon.com";
        String recipientAddress1 = "a@b.com";
        String recipientAdress2 = "b@c.com";
        String[] recipients = new String[]{recipientAddress1, recipientAdress2};
        String timeTest = TimeUtil.getCurrentTime(null);

        jmockStorableMessage0.expects(once()).method("setSender").with(eq(address));
        jmockStorableMessage0.expects(once()).method("addRecipient").with(eq(recipientAddress1));
        jmockStorableMessage0.expects(once()).method("addRecipient").with(eq(recipientAdress2));
        jmockStorableMessage0.expects(once()).method("setSecondaryRecipients").with(eq(recipients));
        jmockStorableMessage0.expects(once()).method("setSubject").with(eq("subject"));
        jmockStorableMessage0.expects(once()).method("setReplyToAddress").with(eq(address));
        jmockStorableMessage0.expects(once()).method("setType").with(eq(MailboxMessageType.VOICE));
        jmockStorableMessage0.expects(once()).method("setLanguage").with(eq("sv"));
        jmockStorableMessage0.expects(once()).method("setDeliveryDate");
        jmockStorableMessage0.expects(once()).method("setUrgent").with(eq(false));
        jmockStorableMessage0.expects(once()).method("setConfidential").with(eq(true));

        int storableMessageId = platformAccess.messageCreateNew();
        platformAccess.messageSetStorableProperty(storableMessageId, "sender", new String[]{address});
        platformAccess.messageSetStorableProperty(storableMessageId, "recipients", recipients);
        platformAccess.messageSetStorableProperty(storableMessageId, "secondaryrecipients", recipients);
        platformAccess.messageSetStorableProperty(storableMessageId, "subject", new String[]{"subject"});
        platformAccess.messageSetStorableProperty(storableMessageId, "replytoaddr", new String[]{address});
        platformAccess.messageSetStorableProperty(storableMessageId, "type", new String[]{"voice"});
        platformAccess.messageSetStorableProperty(storableMessageId, "language", new String[]{"sv"});
        platformAccess.messageSetStorableProperty(storableMessageId, "deliverydate", new String[]{timeTest});
        platformAccess.messageSetStorableProperty(storableMessageId, "urgent", new String[]{"false"});
        platformAccess.messageSetStorableProperty(storableMessageId, "confidential", new String[]{"true"});

        // test invalid property name
        try {
            platformAccess.messageSetStorableProperty(storableMessageId, "NoSuchMethod", new String[]{""});
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test invalid storableMessageId
        jmockStorableMessage0.expects(never()).method("setSender");
        try {
            platformAccess.messageSetStorableProperty(99, "sender", new String[]{address});
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test invalid vvadatestring
        jmockStorableMessage0.expects(never()).method("setDeliveryDate");
        try {
            platformAccess.messageSetStorableProperty(storableMessageId, "deliverydate", new String[]{"2005-11-10 14:33 : 32"});
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test null recipient
        jmockStorableMessage0.expects(never()).method("addRecipient");
        try {
            platformAccess.messageSetStorableProperty(storableMessageId, "recipients", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
        // test null recipientsSubscriberId
        jmockStorableMessage0.expects(never()).method("addRecipient");
        try {
            platformAccess.messageSetStorableProperty(storableMessageId, "recipientsSubscriberId", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
        // test null property name
        try {
            platformAccess.messageSetStorableProperty(storableMessageId, null, new String[]{"whatever"});
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }


    }

    /**
     * test that if one recipient is used in call to messageSetStorableProperty, followed by a messageStore,
     * the message will go to the preferred storage
     *
     * @throws Exception
     */
    public void testStorableMessageOneRecipient() throws Exception {
        // setup the expected properties
        String recipientAddress = "a@b.com";
        String[] singleRecipient = new String[]{recipientAddress};

        jmockStorableMessage0.expects(once()).method("addRecipient").with(eq(recipientAddress));
        jmockStorableMessage0.expects(once()).method("store").withNoArguments();

        int storableMessageId = platformAccess.messageCreateNew();
        platformAccess.messageSetStorableProperty(storableMessageId, "recipients", singleRecipient);
        platformAccess.messageStore(storableMessageId);
    }

    /**
     * test that if one subscriberId is used in call to messageSetStorableProperty, followed by a messageStore,
     * the message will go to the subscriber's storage
     *
     * @throws Exception
     */
    public void testStorableMessageOneSubscriberId() throws Exception {
        // setup the expected properties
        String[] singleSubscriberId = new String[]{"161074"};
        String addressOfSingleRecipient = ADDRESS_OF_PROFILE0;
        String mailHostOfSingleRecipient = MAILHOST_OF_PROFILE0;

        jmockStorableMessage0.expects(once()).method("addRecipient").with(eq(addressOfSingleRecipient));
        jmockStorableMessage0.expects(once()).method("store").with(eq(mailHostOfSingleRecipient));

        // check that getProfile is called just once
        IProfile[] profiles = new IProfile[]{(IProfile) jmockProfileId0.proxy()};

        jmockProfileManager.expects(once()).method("getProfile").
                with(eq(new ProfileOrCritera(
                        new ProfileStringCriteria("billingnumber", "161074"),
                        new ProfileStringCriteria("emmin", "161074")
                )), eq(false)).
                will(returnValue(profiles));


        int storableMessageId = platformAccess.messageCreateNew();
        assertTrue(platformAccess.subscriberExist("161074"));  // this will cause a getProfile
        // These should not cause getProfile
        platformAccess.messageSetStorableProperty(storableMessageId, "recipientsSubscriberId", singleSubscriberId);
        platformAccess.messageStore(storableMessageId);
    }

    /**
     * test that if one subscriberId and two recipients is used in call to messageSetStorableProperty,
     * followed by a messageStore, the message will go to the "preferred storage"
     *
     * @throws Exception
     */
    public void testStorableMessageOneSubscriberIdTwoRecipients() throws Exception {
        // setup the expected properties
        String address1 = "a@b.com";
        String address2 = "b@c.com";
        String[] recipients = new String[]{address1, address2};
        String[] singleSubscriberId = new String[]{"161074"};
        String addressOfSingleRecipient = ADDRESS_OF_PROFILE0;

        jmockStorableMessage0.expects(once()).method("addRecipient").with(eq(addressOfSingleRecipient));
        jmockStorableMessage0.expects(once()).method("addRecipient").with(eq(address1));
        jmockStorableMessage0.expects(once()).method("addRecipient").with(eq(address2));
        jmockStorableMessage0.expects(once()).method("store").withNoArguments();

        int storableMessageId = platformAccess.messageCreateNew();
        platformAccess.messageSetStorableProperty(storableMessageId, "recipientsSubscriberId", singleSubscriberId);
        platformAccess.messageSetStorableProperty(storableMessageId, "recipients", recipients);
        platformAccess.messageStore(storableMessageId);
    }

    /**
     * test that if two subscriberId with same mailhost is used in call to messageSetStorableProperty,
     * followed by a messageStore, the message will go to the subscriber's storage
     *
     * @throws Exception
     */
    public void testStorableMessageTwoSubscriberIdSameMailhost() throws Exception {
        String[] subscribers = new String[]{"161074", "161075"};

        jmockStorableMessage0.expects(once()).method("addRecipient").with(eq(ADDRESS_OF_PROFILE0));
        jmockStorableMessage0.expects(once()).method("addRecipient").with(eq(ADDRESS_OF_PROFILE1));
        jmockStorableMessage0.expects(once()).method("store").with(eq(MAILHOST_OF_PROFILE0));

        int storableMessageId = platformAccess.messageCreateNew();
        platformAccess.messageSetStorableProperty(storableMessageId, "recipientsSubscriberId", subscribers);
        platformAccess.messageStore(storableMessageId);
    }

    /**
     * test that if recipientsSubscriberId is supplied but that subscriber does not exist,
     * a datanotfound will result
     *
     * @throws Exception
     */
    public void testStorableMessageSubscriberDoesNotExist() throws Exception {
        // setup the expected properties
        String subscriber = "SUBSCRIBER_NOT_FOUND";
        String[] subscribers = new String[]{subscriber};

        jmockStorableMessage0.expects(never()).method("addRecipient");
        jmockStorableMessage0.expects(never()).method("store").withNoArguments();
        jmockProfileManager.stubs().method("getProfile").
                with(eq(new ProfileOrCritera(
                        new ProfileStringCriteria("billingnumber", subscriber),
                        new ProfileStringCriteria("emmin", subscriber)
                )),
                        eq(false)).
                will(returnValue(null));

        int storableMessageId = platformAccess.messageCreateNew();
        boolean gotException = false;
        try {
            platformAccess.messageSetStorableProperty(storableMessageId, "recipientsSubscriberId", subscribers);
        } catch (PlatformAccessException e) {
            gotException = true;
            if (! e.getMessage().equals(EventType.DATANOTFOUND)) {
                fail("Exception type was: + " + e.getMessage());
            }
        }
        if (! gotException) {
            fail("No Exception for messageSetStorableProperty");
        }
    }

    /**
     * test that if you add a subscriber who is not a subscriber, you get an exception
     *
     * @throws Exception
     */
    public void testStorableMessageTwoSubscriberIdDifferentMailhosts() throws Exception {
        String[] subscribers = new String[]{"161074", "161076"};

        jmockStorableMessage0.expects(once()).method("addRecipient").with(eq(ADDRESS_OF_PROFILE0));
        jmockStorableMessage0.expects(once()).method("addRecipient").with(eq(ADDRESS_OF_PROFILE2));
        jmockStorableMessage0.expects(once()).method("store").withNoArguments();

        int storableMessageId = platformAccess.messageCreateNew();
        platformAccess.messageSetStorableProperty(storableMessageId, "recipientsSubscriberId", subscribers);
        platformAccess.messageStore(storableMessageId);
    }

    /**
     * Test that if a subscriber has an empty mailhost attribute, the mail will
     * go to the preferred storage
     *
     * @throws Exception
     */
    public void testStorableMessageSubscriberWithEmptyMailHost() throws Exception {
        // setup the expected properties
        String[] subscribers = new String[]{"161077"};

        jmockStorableMessage0.expects(once()).method("addRecipient").with(eq(ADDRESS_OF_PROFILE3));
        jmockStorableMessage0.expects(once()).method("store").withNoArguments();

        int storableMessageId = platformAccess.messageCreateNew();
        platformAccess.messageSetStorableProperty(storableMessageId, "recipientsSubscriberId", subscribers);
        platformAccess.messageStore(storableMessageId);
    }

    /**
     * Test that if a subscriber has null mailhost attribute, a system error will be thrown
     *
     * @throws Exception
     */
    public void testStorableMessageSubscriberWithNullMailHost() throws Exception {
        // setup the expected properties
        String[] subscribers = new String[]{"161078"};

        jmockStorableMessage0.expects(never()).method("addRecipient");

        int storableMessageId = platformAccess.messageCreateNew();
        boolean gotException = false;
        try {
            platformAccess.messageSetStorableProperty(storableMessageId, "recipientsSubscriberId", subscribers);
        } catch (PlatformAccessException e) {
            gotException = true;
            if (! e.getMessage().equals(EventType.SYSTEMERROR)) {
                fail("Exception type was: + " + e.getMessage());
            }
        }
        if (! gotException) {
            fail("No Exception for messageStore");
        }
    }

    /**
     * Test that if a subscriber has null mail attribute, a system error will be thrown
     *
     * @throws Exception
     */
    public void testStorableMessageSubscriberWithNullMail() throws Exception {
        // setup the expected properties
        String[] subscribers = new String[]{"161079"};

        jmockStorableMessage0.expects(never()).method("addRecipient");

        int storableMessageId = platformAccess.messageCreateNew();
        boolean gotException = false;
        try {
            platformAccess.messageSetStorableProperty(storableMessageId, "recipientsSubscriberId", subscribers);
        } catch (PlatformAccessException e) {
            gotException = true;
            if (! e.getMessage().equals(EventType.SYSTEMERROR)) {
                fail("Exception type was: + " + e.getMessage());
            }
        }
        if (! gotException) {
            fail("No Exception for messageStore");
        }
    }


    /**
     * Tests the messageGetStorableProperty function.
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageGetStorableProperty() throws Exception {
        String sender = "ermmaha@mobeon.com";
        String[] recipients = new String[]{"a@b.com", "b@c.com"};
        String[] secRecipients = new String[]{"test1@domain.com", "test2@domain.com"};
        jmockStorableMessage0.expects(once()).method("getSender").will(returnValue(sender));
        jmockStorableMessage0.expects(once()).method("getRecipients").will(returnValue(recipients));
        jmockStorableMessage0.expects(once()).method("getSecondaryRecipients").will(returnValue(secRecipients));
        jmockStorableMessage0.expects(once()).method("getSubject").will(returnValue("subject"));
        jmockStorableMessage0.expects(once()).method("getReplyToAddress").will(returnValue("replyto@test.com"));
        jmockStorableMessage0.expects(once()).method("getType").will(returnValue(MailboxMessageType.FAX));
        jmockStorableMessage0.expects(once()).method("getLanguage").will(returnValue("en"));
        jmockStorableMessage0.expects(once()).method("isUrgent").will(returnValue(true));
        jmockStorableMessage0.expects(once()).method("isConfidential").will(returnValue(false));

        int storableMessageId = platformAccess.messageCreateNew();
        String[] result = platformAccess.messageGetStorableProperty(storableMessageId, "sender");
        assertEquals(sender, result[0]);

        result = platformAccess.messageGetStorableProperty(storableMessageId, "recipients");
        assertArray(recipients, result);

        result = platformAccess.messageGetStorableProperty(storableMessageId, "secondaryrecipients");
        assertArray(secRecipients, result);

        result = platformAccess.messageGetStorableProperty(storableMessageId, "subject");
        assertEquals("subject", result[0]);

        result = platformAccess.messageGetStorableProperty(storableMessageId, "replytoaddr");
        assertEquals("replyto@test.com", result[0]);

        result = platformAccess.messageGetStorableProperty(storableMessageId, "type");
        assertEquals("fax", result[0]);

        result = platformAccess.messageGetStorableProperty(storableMessageId, "language");
        assertEquals("en", result[0]);

        result = platformAccess.messageGetStorableProperty(storableMessageId, "urgent");
        assertEquals("true", result[0]);

        result = platformAccess.messageGetStorableProperty(storableMessageId, "confidential");
        assertEquals("false", result[0]);

        // test the DeliveryDate property. Make a test Date object and compare the result with it.
        Date time = Calendar.getInstance().getTime();
        jmockStorableMessage0.expects(once()).method("getDeliveryDate").will(returnValue(time));
        String timeTest = TimeUtil.getCurrentTime(null);
        result = platformAccess.messageGetStorableProperty(storableMessageId, "deliverydate");
        assertEquals(timeTest, result[0]);

        // test null value
        jmockStorableMessage0.expects(once()).method("getSender").will(returnValue(null));
        try {
            platformAccess.messageGetStorableProperty(storableMessageId, "sender");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
        }

        // test invalid property name
        try {
            platformAccess.messageGetStorableProperty(storableMessageId, "NoSuchMethod");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test invalid storableMessageId
        jmockStorableMessage0.expects(never()).method("getSender");
        try {
            platformAccess.messageGetStorableProperty(99, "sender");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Tests the messageStore function.
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageStore() throws Exception {
        jmockStorableMessage0.expects(once()).method("store");

        int storableMessageId = platformAccess.messageCreateNew();
        platformAccess.messageStore(storableMessageId);

        jmockStorableMessage0.expects(never()).method("store");
        try {
            platformAccess.messageStore(99);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        jmockStorableMessage0.expects(once()).method("store").will(
                throwException(new MailboxException("Could not store the IStorableMessage")));
        try {
            platformAccess.messageStore(storableMessageId);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
        }
    }

    /**
     * Tests the messageSetSpokenNameOfSender function.
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageSetSpokenNameOfSender() throws Exception {
        Mock spokenName = mock(IMediaObject.class);
        jmockStorableMessage0.expects(once()).method("setSpokenNameOfSender").with(eq(spokenName.proxy()), isA(MessageContentProperties.class));

        int storableMessageId = platformAccess.messageCreateNew();
        platformAccess.messageSetSpokenNameOfSender(storableMessageId, (IMediaObject) spokenName.proxy(),
                "description", "spoken.wav", "sv");

        try {
            jmockStorableMessage0.expects(never()).method("setSpokenNameOfSender");
            platformAccess.messageSetSpokenNameOfSender(88, (IMediaObject) spokenName.proxy(), null, null, null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Tests the messageAddMediaObject function.
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageAddMediaObject() throws Exception {
        Mock mediaObject = mock(IMediaObject.class);
        jmockStorableMessage0.expects(once()).method("addContent").with(eq(mediaObject.proxy()), isA(MessageContentProperties.class));

        int storableMessageId = platformAccess.messageCreateNew();
        platformAccess.messageAddMediaObject(storableMessageId, (IMediaObject) mediaObject.proxy(),
                "description", "powerslave.mp3", "en");

        try {
            jmockStorableMessage0.expects(never()).method("addContent");
            platformAccess.messageAddMediaObject(88, (IMediaObject) mediaObject.proxy(), null, null, null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    private void setupProfileLocal() {

        IProfile[] profiles2 = new IProfile[]{(IProfile) jmockProfileId2Local.proxy()};

        jmockProfileManager.stubs().method("getProfile").
                with(eq(new ProfileOrCritera(
                        new ProfileStringCriteria("billingnumber", "161076"),
                        new ProfileStringCriteria("emmin", "161076")
                )), eq(false)).
                will(returnValue(profiles2));

        IProfile[] profiles3 = new IProfile[]{(IProfile) jmockProfileId3HasNoMailhost.proxy()};

        jmockProfileManager.stubs().method("getProfile").
                with(eq(new ProfileOrCritera(
                        new ProfileStringCriteria("billingnumber", "161077"),
                        new ProfileStringCriteria("emmin", "161077")
                )), eq(false)).
                will(returnValue(profiles3));

        IProfile[] profiles4 = new IProfile[]{(IProfile) jmockProfileId4HasNullMailhost.proxy()};

        jmockProfileManager.stubs().method("getProfile").
                with(eq(new ProfileOrCritera(
                        new ProfileStringCriteria("billingnumber", "161078"),
                        new ProfileStringCriteria("emmin", "161078")
                )), eq(false)).
                will(returnValue(profiles4));

        IProfile[] profiles5 = new IProfile[]{(IProfile) jmockProfileId5HasNullMail.proxy()};

        jmockProfileManager.stubs().method("getProfile").
                with(eq(new ProfileOrCritera(
                        new ProfileStringCriteria("billingnumber", "161079"),
                        new ProfileStringCriteria("emmin", "161079")
                )), eq(false)).
                will(returnValue(profiles5));

    }

    private void setupProfileAttributesLocal() {
        jmockProfileId0.stubs().method("getStringAttributes").with(eq("mail")).will(returnValue(new String[]{ADDRESS_OF_PROFILE0}));
        jmockProfileId0.stubs().method("getStringAttributes").with(eq("mailhost")).will(returnValue(new String[]{MAILHOST_OF_PROFILE0}));

        jmockProfileId1.stubs().method("getStringAttributes").with(eq("mail")).will(returnValue(new String[]{ADDRESS_OF_PROFILE1}));
        jmockProfileId1.stubs().method("getStringAttributes").with(eq("mailhost")).will(returnValue(new String[]{MAILHOST_OF_PROFILE1}));

        jmockProfileId2Local.stubs().method("getStringAttributes").with(eq("mail")).will(returnValue(new String[]{ADDRESS_OF_PROFILE2}));
        jmockProfileId2Local.stubs().method("getStringAttributes").with(eq("mailhost")).will(returnValue(new String[]{MAILHOST_OF_PROFILE2}));

        jmockProfileId3HasNoMailhost.stubs().method("getStringAttributes").with(eq("mail")).will(returnValue(new String[]{ADDRESS_OF_PROFILE3}));
        jmockProfileId3HasNoMailhost.stubs().method("getStringAttributes").with(eq("mailhost")).will(returnValue(new String[]{MAILHOST_OF_PROFILE3}));

        jmockProfileId4HasNullMailhost.stubs().method("getStringAttributes").with(eq("mail")).will(returnValue(new String[]{ADDRESS_OF_PROFILE4}));
        jmockProfileId4HasNullMailhost.stubs().method("getStringAttributes").with(eq("mailhost")).will(returnValue(MAILHOST_OF_PROFILE4));

        jmockProfileId5HasNullMail.stubs().method("getStringAttributes").with(eq("mail")).will(returnValue(ADDRESS_OF_PROFILE5));
        jmockProfileId5HasNullMail.stubs().method("getStringAttributes").with(eq("mailhost")).will(returnValue(new String[]{MAILHOST_OF_PROFILE5}));

    }

    public static Test suite() {
        return new TestSuite(PlatformAccessStorableMessageTest.class);
    }
}

