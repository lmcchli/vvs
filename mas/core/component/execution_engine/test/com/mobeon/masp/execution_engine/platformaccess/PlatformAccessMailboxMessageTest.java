/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

import java.util.*;
import java.io.ByteArrayInputStream;

import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.util.criteria.Criteria;
import com.mobeon.masp.execution_engine.platformaccess.util.TimeUtil;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

/**
 * Tests the Message related function in PlatformAccess
 *
 * @author ermmaha
 */
public class PlatformAccessMailboxMessageTest extends PlatformAccessMailboxBaseTest {

    //messages
    //user0
    private Mock jmockId0MessageInbox0;
    private Mock jmockId0MessageInbox1;
    private Mock jmockId0MessageTrash0;
    private Mock jmockId0MessageTrash1;
    //user1
    private Mock jmockId1MessageInbox0;
    private Mock jmockId1MessageInbox1;
    private Mock jmockId1MessageInbox2;
    private Mock jmockId1MessageTrash0;
    private Mock jmockId1MessageTrash1;
    private Mock jmockId1MessageTrash2;
    private Mock jmockId1MessageTrash3;

    //messagecontent
    //user0
    Mock jmockId0MessageInbox0Content0;
    Mock jmockId0MessageInbox1Content0;
    Mock jmockId0MessageTrash0Content0;
    Mock jmockId0MessageTrash0Content1;

    //mediaobject
    //user0
    Mock jmockId0MessageInbox0Content0Media0;
    Mock jmockId0MessageInbox1Content0Media0;
    Mock jmockId0MessageTrash0Content0Media0;
    Mock jmockId0MessageTrash0Content1Media0;

    public PlatformAccessMailboxMessageTest(String name) {
        super(name);

        jmockId0MessageInbox0 = mock(IStoredMessage.class);
        jmockId0MessageInbox1 = mock(IStoredMessage.class);
        jmockId0MessageTrash0 = mock(IStoredMessage.class);
        jmockId0MessageTrash1 = mock(IStoredMessage.class);
        jmockId1MessageInbox0 = mock(IStoredMessage.class);
        jmockId1MessageInbox1 = mock(IStoredMessage.class);
        jmockId1MessageInbox2 = mock(IStoredMessage.class);
        jmockId1MessageTrash0 = mock(IStoredMessage.class);
        jmockId1MessageTrash1 = mock(IStoredMessage.class);
        jmockId1MessageTrash2 = mock(IStoredMessage.class);
        jmockId1MessageTrash3 = mock(IStoredMessage.class);

        jmockId0MessageInbox0Content0 = mock(IMessageContent.class);
        jmockId0MessageInbox1Content0 = mock(IMessageContent.class);
        jmockId0MessageTrash0Content0 = mock(IMessageContent.class);
        jmockId0MessageTrash0Content1 = mock(IMessageContent.class);

        setupMediaObjects();
        setupMediaProperties();
        setupMessages();
        setupFolders();
    }

    /**
     * Tests the mailboxGetMessages function. Asserts the messagesId's that are returned
     *
     * @throws Exception if testcase fails.
     */
    public void testMailboxGetMessages() throws Exception {
        String types = commaSeparate(VOICE, VIDEO, FAX, EMAIL);
        String states = commaSeparate(NEW, READ, DELETED);
        String priorities = commaSeparate(URGENT, NONURGENT);
        String orders = commaSeparate(TYPE, STATE);

        // test messageids from mailbox0 and INBOX
        int mailboxId = platformAccess.subscriberGetMailbox("161074");
        int folderId0 = platformAccess.mailboxGetFolder(mailboxId, INBOX);
        int messageListId = platformAccess.mailboxGetMessageList(folderId0, types, states, priorities, orders, FIFO);
        int[] messageIds = platformAccess.mailboxGetMessages(messageListId);
        assertIntArray(new int[]{0,1}, messageIds);

        // test messageids from mailbox0 and TRASH
        int folderId1 = platformAccess.mailboxGetFolder(mailboxId, TRASH);
        messageListId = platformAccess.mailboxGetMessageList(folderId1, types, states, priorities, orders, LIFO);
        messageIds = platformAccess.mailboxGetMessages(messageListId);
        assertIntArray(new int[]{2,3}, messageIds);

        // test messageids from mailbox1 and INBOX
        mailboxId = platformAccess.subscriberGetMailbox("161075");
        int folderId2 = platformAccess.mailboxGetFolder(mailboxId, INBOX);
        messageListId = platformAccess.mailboxGetMessageList(folderId2, types, states, priorities, orders, LIFO);
        messageIds = platformAccess.mailboxGetMessages(messageListId);
        assertIntArray(new int[]{4, 5, 6}, messageIds);

        // test invalid search strings
        int folderId3 = platformAccess.mailboxGetFolder(mailboxId, TRASH);
        try {
            platformAccess.mailboxGetMessageList(folderId3, "wrongtype", states, priorities, orders, LIFO);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test messageids from mailbox0 and TRASH
        folderId3 = platformAccess.mailboxGetFolder(mailboxId, TRASH);
        messageListId = platformAccess.mailboxGetMessageList(folderId3, types, states, priorities, orders, LIFO);
        messageIds = platformAccess.mailboxGetMessages(messageListId);
        assertIntArray(new int[]{7, 8, 9, 10}, messageIds);

        // test a folderid that does not exist
        jmockFolderId0Inbox.expects(never()).method("searchMessages");
        jmockFolderId0Trash.expects(never()).method("searchMessages");
        jmockFolderId1Inbox.expects(never()).method("searchMessages");
        jmockFolderId1Trash.expects(never()).method("searchMessages");
        try {
            platformAccess.mailboxGetMessageList(99, types, states, priorities, orders, LIFO);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test exception
        jmockFolderId1Trash.expects(once()).method("searchMessages").
                will(throwException(new MailboxException("Error")));
        try {
            platformAccess.mailboxGetMessageList(folderId3, types, states, priorities, orders, LIFO);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("mailboxGetMessageList") > -1);
        }

        // test a messageListId that does not exist
        try {
            platformAccess.mailboxGetMessages(99);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            assertTrue(e.getDescription().indexOf("mailboxGetMessages") > -1);
        }
    }

    /**
     * Tests the mailboxGetMessageSubList function. Asserts the messagesListId's that are returned
     *
     * @throws Exception if testcase fails.
     */
    public void testMailboxGetMessageSubList() throws Exception {
        String types = commaSeparate(VOICE, VIDEO, FAX, EMAIL);
        String states = commaSeparate(NEW, READ, DELETED);
        String priorities = commaSeparate(URGENT, NONURGENT);
        String orders = commaSeparate(TYPE, STATE);

        // test messageids from mailbox1 and TRASH
        int mailboxId = platformAccess.subscriberGetMailbox("161075");
        int folderId = platformAccess.mailboxGetFolder(mailboxId, TRASH);
        int messageListId = platformAccess.mailboxGetMessageList(folderId, types, states, priorities, orders, FIFO);
        assertEquals(0, messageListId);
        int messageSubListId = platformAccess.mailboxGetMessageSubList(messageListId, types, states, priorities, orders, FIFO);
        assertEquals(1, messageSubListId);
        int[] messageIds = platformAccess.mailboxGetMessages(messageListId);
        assertIntArray(new int[]{0, 1, 2, 3}, messageIds);
        int[] messageSubIds = platformAccess.mailboxGetMessages(messageSubListId);
        assertIntArray(new int[]{4}, messageSubIds);

        // test to search in a list that does not exist
        try {
            platformAccess.mailboxGetMessageSubList(99, types, states, priorities, orders, FIFO);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test invalid search strings
        try {
            platformAccess.mailboxGetMessageSubList(messageListId, "wrongtype", states, priorities, orders, FIFO);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Tests the mailboxGetNumberOfMessages function. Asserts the number of messages that are returned
     *
     * @throws Exception if testcase fails.
     */
    public void testMailboxGetNumberOfMessages() throws Exception {
        String types = commaSeparate(VOICE, VIDEO, FAX, EMAIL);
        String states = commaSeparate(NEW, READ, DELETED);
        String priorities = commaSeparate(URGENT, NONURGENT);
        String orders = commaSeparate(TYPE, STATE);

        // test messageids from mailbox1 and TRASH
        int mailboxId = platformAccess.subscriberGetMailbox("161075");
        int folderId = platformAccess.mailboxGetFolder(mailboxId, TRASH);
        int messageListId = platformAccess.mailboxGetMessageList(folderId, types, states, priorities, orders, FIFO);
        assertEquals(0, messageListId);

        int numberOfMessages = platformAccess.mailboxGetNumberOfMessages(messageListId, types, states, priorities);
        // ToDo fix a better implementation of StubIStoredMessageList.select()
        assertEquals(1, numberOfMessages);

        // test to search in a list that does not exist
        try {
            platformAccess.mailboxGetNumberOfMessages(99, types, states, priorities);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test invalid search strings
        try {
            platformAccess.mailboxGetNumberOfMessages(messageListId, "wrongtype", states, priorities);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Tests the messageGetContent function. Asserts the messageContentIds's that are returned
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageGetContent() throws Exception {
        String types = commaSeparate(VOICE, VIDEO, FAX, EMAIL);
        String states = commaSeparate(NEW, READ, DELETED);
        String priorities = commaSeparate(URGENT, NONURGENT);
        String orders = commaSeparate(TYPE, STATE);

        // (mailbox0) test contentId from first mail in inbox
        int mailboxId = platformAccess.subscriberGetMailbox("161074");
        int folderId = platformAccess.mailboxGetFolder(mailboxId, INBOX);
        int messageListId = platformAccess.mailboxGetMessageList(folderId, types, states, priorities, orders, FIFO);
        int[] messageIds = platformAccess.mailboxGetMessages(messageListId);
        int[] messageContentIds = platformAccess.messageGetContent(messageIds[0]);
        assertIntArray(new int[]{0}, messageContentIds);

        // (mailbox0) test a messageId that does not exist
        try {
            platformAccess.messageGetContent(99);
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // (mailbox0) test contentId from first mail in trash
        folderId = platformAccess.mailboxGetFolder(mailboxId, TRASH);
        messageListId = platformAccess.mailboxGetMessageList(folderId, types, states, priorities, orders, FIFO);
        messageIds = platformAccess.mailboxGetMessages(messageListId);
        messageContentIds = platformAccess.messageGetContent(messageIds[0]);
        assertIntArray(new int[]{1, 2}, messageContentIds);

        // (mailbox1) test contentId from first mail in inbox. Should be empty
        int mailboxId1 = platformAccess.subscriberGetMailbox("161075");
        int folderId1 = platformAccess.mailboxGetFolder(mailboxId1, INBOX);
        messageListId = platformAccess.mailboxGetMessageList(folderId1, types, states, priorities, orders, FIFO);
        int[] messageIds1 = platformAccess.mailboxGetMessages(messageListId);
        int[] messageContentIds1 = platformAccess.messageGetContent(messageIds1[0]);
        assertIntArray(new int[0], messageContentIds1);

        // test exception
        jmockId1MessageInbox0.expects(once()).method("getContent").
                will(throwException(new MailboxException("Error")));
        try {
            platformAccess.messageGetContent(messageIds1[0]);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageGetContent") > -1);
        }
    }

    /**
     * Tests the messagesGetMediaObject function. Asserts the IMediaObject that are returned
     *
     * @throws Exception
     */
    public void testMessageGetMediaObject() throws Exception {
        String types = commaSeparate(VOICE, VIDEO, FAX, EMAIL);
        String states = commaSeparate(NEW, READ, DELETED);
        String priorities = commaSeparate(URGENT, NONURGENT);
        String orders = commaSeparate(TYPE, STATE);

        // (mailbox0) test IMediaObject from first content from first mail in inbox
        int mailboxId0 = platformAccess.subscriberGetMailbox("161074");
        int folderId0 = platformAccess.mailboxGetFolder(mailboxId0, INBOX);
        int messageListId = platformAccess.mailboxGetMessageList(folderId0, types, states, priorities, orders, FIFO);
        int[] messageIds = platformAccess.mailboxGetMessages(messageListId);
        int[] messageContentIds = platformAccess.messageGetContent(messageIds[0]);
        IMediaObject iMediaObject = platformAccess.messageGetMediaObject(messageContentIds[0]);
        assertEquals(jmockId0MessageInbox0Content0Media0.proxy(), iMediaObject);

        // (mailbox0) test IMediaObject from first content from first mail in trash
        folderId0 = platformAccess.mailboxGetFolder(mailboxId0, TRASH);
        messageListId = platformAccess.mailboxGetMessageList(folderId0, types, states, priorities, orders, FIFO);
        messageIds = platformAccess.mailboxGetMessages(messageListId);
        messageContentIds = platformAccess.messageGetContent(messageIds[0]);
        iMediaObject = platformAccess.messageGetMediaObject(messageContentIds[0]);
        assertEquals(jmockId0MessageTrash0Content0Media0.proxy(), iMediaObject);

        // (mailbox0) test IMediaObject from second content from first mail in trash
        iMediaObject = platformAccess.messageGetMediaObject(messageContentIds[1]);
        assertEquals(jmockId0MessageTrash0Content1Media0.proxy(), iMediaObject);

        // (mailbox0) test a messageContentId that does not exist
        try {
            platformAccess.messageGetMediaObject(99);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test exception
        jmockId0MessageTrash0Content0.expects(once()).method("getMediaObject").
                will(throwException(new MailboxException("Error")));
        try {
            platformAccess.messageGetMediaObject(messageContentIds[0]);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageGetMediaObject") > -1);
        }

        // test no mediaobject (will return null)
        jmockId0MessageTrash0Content0.expects(once()).method("getMediaObject").
                withNoArguments().will(returnValue(null));
        try {
            platformAccess.messageGetMediaObject(messageContentIds[0]);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageGetMediaObject") > -1);
        }
    }

    /**
     * Tests the messageGetMediaProperties function. Asserts the Strings that are returned
     *
     * @throws Exception
     */
    public void testMessageGetMediaProperties() throws Exception {
        String types = commaSeparate(VOICE, VIDEO, FAX, EMAIL);
        String states = commaSeparate(NEW, READ, DELETED);
        String priorities = commaSeparate(URGENT, NONURGENT);
        String orders = commaSeparate(TYPE, STATE);

        // (mailbox0) test MediaProperties from first content from first mail in inbox
        int mailboxId0 = platformAccess.subscriberGetMailbox("161074");
        int folderId0 = platformAccess.mailboxGetFolder(mailboxId0, INBOX);
        int messageListId = platformAccess.mailboxGetMessageList(folderId0, types, states, priorities, orders, FIFO);
        int[] messageIds = platformAccess.mailboxGetMessages(messageListId);
        int[] messageContentIds = platformAccess.messageGetContent(messageIds[0]);
        String mediaProperty = platformAccess.messageGetMediaProperties(messageContentIds[0]);
        assertEquals("audio/pcmu", mediaProperty);

        // (mailbox0) test MediaProperties from first content from first mail in trash
        folderId0 = platformAccess.mailboxGetFolder(mailboxId0, TRASH);
        messageListId = platformAccess.mailboxGetMessageList(folderId0, types, states, priorities, orders, FIFO);
        messageIds = platformAccess.mailboxGetMessages(messageListId);
        messageContentIds = platformAccess.messageGetContent(messageIds[0]);
        mediaProperty = platformAccess.messageGetMediaProperties(messageContentIds[0]);
        assertEquals("video/h263", mediaProperty);

        // (mailbox0) test a messageContentId that does not exist
        try {
            platformAccess.messageGetMediaProperties(99);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test exception
        jmockId0MessageTrash0Content0.expects(once()).method("getMediaProperties").
                will(throwException(new MailboxException("Error")));
        try {
            platformAccess.messageGetMediaProperties(messageContentIds[0]);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageGetMediaProperties") > -1);
        }
    }

    /**
     * Tests the messageGetStoredProperty function. Asserts the Strings that are returned
     *
     * @throws Exception
     */
    public void testMessageGetStoredProperty() throws Exception {
        // setup responses for the properties
        jmockId0MessageInbox0.expects(once()).method("getSender").will(returnValue("mac@test.com"));
        jmockId0MessageInbox0.expects(once()).method("getRecipients").will(returnValue(new String[]{"mac@test.com", "mac2@test.com"}));
        jmockId0MessageInbox0.expects(once()).method("getSecondaryRecipients").will(returnValue(new String[]{"ccmac@test.com", "ccmac2@test.com"}));
        jmockId0MessageInbox0.expects(once()).method("getSubject").will(returnValue("Id0Inbox0"));
        jmockId0MessageInbox0.expects(once()).method("getReplyToAddress").will(returnValue("replytoaddr"));
        jmockId0MessageInbox0.expects(once()).method("getType").will(returnValue(MailboxMessageType.FAX));
        jmockId0MessageInbox0.expects(once()).method("getState").will(returnValue(StoredMessageState.NEW));
        jmockId0MessageInbox0.expects(once()).method("getLanguage").will(returnValue("en"));
        jmockId0MessageInbox0.expects(once()).method("isUrgent").will(returnValue(false));
        jmockId0MessageInbox0.expects(once()).method("isConfidential").will(returnValue(true));
        jmockId0MessageInbox0.expects(once()).method("isForward").will(returnValue(true));
        jmockId0MessageInbox0.expects(once()).method("isDeliveryReport").will(returnValue(false));
        jmockId0MessageInbox0.expects(once()).method("getDeliveryReport").will(returnValue(DeliveryStatus.STORE_FAILED));

        int messageId = getTestMessageId();

        String[] result = platformAccess.messageGetStoredProperty(messageId, "sender");
        assertEquals("mac@test.com", result[0]);

        result = platformAccess.messageGetStoredProperty(messageId, "recipients");
        assertArray(new String[]{"mac@test.com", "mac2@test.com"}, result);

        result = platformAccess.messageGetStoredProperty(messageId, "secondaryrecipients");
        assertArray(new String[]{"ccmac@test.com", "ccmac2@test.com"}, result);

        result = platformAccess.messageGetStoredProperty(messageId, "subject");
        assertEquals("Id0Inbox0", result[0]);

        result = platformAccess.messageGetStoredProperty(messageId, "replytoaddr");
        assertEquals("replytoaddr", result[0]);

        result = platformAccess.messageGetStoredProperty(messageId, "type");
        assertEquals("fax", result[0]);

        result = platformAccess.messageGetStoredProperty(messageId, "state");
        assertEquals("new", result[0]);

        result = platformAccess.messageGetStoredProperty(messageId, "language");
        assertEquals("en", result[0]);

        result = platformAccess.messageGetStoredProperty(messageId, "urgent");
        assertEquals("false", result[0]);

        result = platformAccess.messageGetStoredProperty(messageId, "confidential");
        assertEquals("true", result[0]);

        result = platformAccess.messageGetStoredProperty(messageId, "forwarded");
        assertEquals("true", result[0]);

        result = platformAccess.messageGetStoredProperty(messageId, "deliveryreport");
        assertEquals("false", result[0]);

        result = platformAccess.messageGetStoredProperty(messageId, "deliverystatus");
        assertEquals("store-failed", result[0]);
        // test null value on getDeliveryReport method
        jmockId0MessageInbox0.expects(once()).method("getDeliveryReport").will(returnValue(null));
        result = platformAccess.messageGetStoredProperty(messageId, "deliverystatus");
        assertEquals("false", result[0]);

        //Test the ReceivedDate property. Make a test Date object and compare the result with it.
        Date time = Calendar.getInstance().getTime();
        jmockId0MessageInbox0.expects(once()).method("getReceivedDate").will(returnValue(time));
        String timeTest = TimeUtil.getCurrentTime(null);
        result = platformAccess.messageGetStoredProperty(messageId, "receiveddate");
        assertEquals(timeTest, result[0]);

        // test invalid property name
        try {
            platformAccess.messageGetStoredProperty(messageId, "NoSuchMethod");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test value not found
        jmockId0MessageInbox0.expects(once()).method("getLanguage").will(returnValue(null));
        try {
            platformAccess.messageGetStoredProperty(messageId, "language");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
        }

        //test invalid mailboxid
        jmockId0MessageInbox0.expects(never()).method("getLanguage");
        try {
            platformAccess.messageGetStoredProperty(99, "language");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Tests the messageSetStoredProperty function.
     *
     * @throws Exception
     */
    public void testMessageSetStoredProperty() throws Exception {
        // setup responses for the properties
        jmockId0MessageInbox0.expects(once()).method("setState").with(eq(StoredMessageState.SAVED));
        jmockId0MessageInbox0.expects(once()).method("saveChanges");
        int messageId = getTestMessageId();

        platformAccess.messageSetStoredProperty(messageId, "state", new String[]{"saved"});

        // test invalid propertyname
        jmockId0MessageInbox0.expects(never()).method("setState");
        try {
            platformAccess.messageSetStoredProperty(messageId, "invalidname", new String[]{"notused"});
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test exception
        jmockId0MessageInbox0.expects(once()).method("setState").with(eq(StoredMessageState.SAVED));
        jmockId0MessageInbox0.expects(once()).method("saveChanges").will(throwException(new MailboxException("Error")));
        try {
            platformAccess.messageSetStoredProperty(messageId, "state", new String[]{"saved"});
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
        }

        // test invalid mailboxid
        jmockId0MessageInbox0.expects(never()).method("setState");
        try {
            platformAccess.messageSetStoredProperty(99, "state", new String[]{"saved"});
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Tests the messageContentSize function.
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageContentSize() throws Exception {
        String types = commaSeparate(VOICE, VIDEO, FAX, EMAIL);
        String states = commaSeparate(NEW, READ, DELETED);
        String priorities = commaSeparate(URGENT, NONURGENT);
        String orders = commaSeparate(TYPE, STATE);

        // (mailbox0) test MediaProperties from first content in first mail in inbox
        int mailboxId = platformAccess.subscriberGetMailbox("161074");
        int folderId = platformAccess.mailboxGetFolder(mailboxId, INBOX);
        int messageListId = platformAccess.mailboxGetMessageList(folderId, types, states, priorities, orders, FIFO);
        int[] messageIds = platformAccess.mailboxGetMessages(messageListId);
        int[] messageContentIds = platformAccess.messageGetContent(messageIds[0]);
        int result = platformAccess.messageContentSize(messageContentIds[0]);
        assertEquals(123456789, result);

        // (mailbox0) test MediaProperties from first content from first mail in trash
        folderId = platformAccess.mailboxGetFolder(mailboxId, TRASH);
        messageListId = platformAccess.mailboxGetMessageList(folderId, types, states, priorities, orders, FIFO);
        messageIds = platformAccess.mailboxGetMessages(messageListId);
        messageContentIds = platformAccess.messageGetContent(messageIds[0]);
        result = platformAccess.messageContentSize(messageContentIds[0]);
        assertEquals(987654321, result);

        // mailbox0) test a HTML mail (second content from first mail in trash)
        // must setup the mediaobject to return a inputstream with a html string
        ByteArrayInputStream is0 = new ByteArrayInputStream("<html>hello</html>".getBytes("UTF-8"));
        jmockId0MessageTrash0Content1Media0.expects(once()).method("getInputStream").will(returnValue(is0));
        result = platformAccess.messageContentSize(messageContentIds[1]);
        assertEquals(5, result);

        // test a messageContentId that does not exist
        try {
            platformAccess.messageContentSize(99);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageContentSize") > -1);
        }

        // test exception
        jmockId0MessageTrash0Content0.expects(once()).method("getMediaProperties").
                will(throwException(new MailboxException("Error")));
        try {
            platformAccess.messageContentSize(messageContentIds[0]);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageContentSize") > -1);
        }
    }

    /**
     * Tests the messageContentLength function.
     *
     * @throws Exception
     */
    public void testMessageContentLength() throws Exception {
        String types = commaSeparate(VOICE, VIDEO, FAX, EMAIL);
        String states = commaSeparate(NEW, READ, DELETED);
        String priorities = commaSeparate(URGENT, NONURGENT);
        String orders = commaSeparate(TYPE, STATE);

        // (mailbox0) test content length from first content in first mail in inbox
        int mailboxId = platformAccess.subscriberGetMailbox("161074");
        int folderId = platformAccess.mailboxGetFolder(mailboxId, INBOX);
        int messageListId = platformAccess.mailboxGetMessageList(folderId, types, states, priorities, orders, FIFO);
        int[] messageIds = platformAccess.mailboxGetMessages(messageListId);
        int[] messageContentIds = platformAccess.messageGetContent(messageIds[0]);
        int result = platformAccess.messageContentLength(messageContentIds[0], "milliseconds");
        assertEquals(50000, result);

        // (mailbox0) test contentlength from first content in second mail in inbox
        // This content has  no length
        // First check milliseconds
        messageContentIds = platformAccess.messageGetContent(messageIds[1]);
        result = platformAccess.messageContentLength(messageContentIds[0], "milliseconds");
        assertEquals(-1, result);
        // and then pages
        result = platformAccess.messageContentLength(messageContentIds[0], "pages");
        assertEquals(-1, result);

        // (mailbox0) test MediaProperties from first content from first mail in trash
        folderId = platformAccess.mailboxGetFolder(mailboxId, TRASH);
        messageListId = platformAccess.mailboxGetMessageList(folderId, types, states, priorities, orders, FIFO);
        messageIds = platformAccess.mailboxGetMessages(messageListId);
        messageContentIds = platformAccess.messageGetContent(messageIds[0]);
        result = platformAccess.messageContentLength(messageContentIds[0], "pages");
        assertEquals(6, result);

        // test a type that is not supported on the MediaProperties
        result=platformAccess.messageContentLength(0, "pages");
        assertEquals(-1, result);

        // test a type that is not supported on the MediaProperties
        result=platformAccess.messageContentLength(messageContentIds[0], "milliseconds");
        assertEquals(-1, result);

        // test invalid type
        try {
            platformAccess.messageContentLength(messageContentIds[0], "notatype");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageContentLength") > -1);
            System.out.println("ERROR " + e.getDescription());
        }

        // test a messageContentId that does not exist
        try {
            platformAccess.messageContentLength(99, "pages");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageContentLength") > -1);
        }

        // test exception
        jmockId0MessageTrash0Content0.expects(once()).method("getMediaProperties").
                will(throwException(new MailboxException("Error")));
        try {
            platformAccess.messageContentLength(messageContentIds[0], "milliseconds");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageContentLength") > -1);
        }
    }

    /**
     * Tests the messageForward function.
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageForward() throws Exception {
        jmockId0MessageInbox0.expects(once()).method("forward").withNoArguments();

        // test messageids from mailbox0 and INBOX
        int mailboxId = platformAccess.subscriberGetMailbox("161074");
        int folderId0 = platformAccess.mailboxGetFolder(mailboxId, INBOX);
        int messageListId = platformAccess.mailboxGetMessageList(folderId0, VOICE, "", "", "", LIFO);
        int[] ids = platformAccess.mailboxGetMessages(messageListId);

        int storableMessageId = platformAccess.messageForward(ids[0]);
        assertEquals(0, storableMessageId);

        // test exception
        jmockId0MessageInbox0.expects(once()).method("forward").will(throwException(new MailboxException("Error")));
        try {
            platformAccess.messageForward(ids[0]);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageForward") > -1);
        }

        // test invalid messageId
        jmockId0MessageInbox0.expects(never()).method("forward");
        try {
            platformAccess.messageForward(99);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Tests the messageGetSpokenNameOfSender function. Asserts the MediaObjects that are returned
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageGetSpokenNameOfSender() throws Exception {
        Mock jmockSpokenName = mock(IMediaObject.class);
        jmockId0MessageInbox0.expects(once()).method("getSpokenNameOfSender").
                withNoArguments().will(returnValue(jmockSpokenName.proxy()));

        IMediaObject spokenName = platformAccess.messageGetSpokenNameOfSender(getTestMessageId());
        assertEquals(jmockSpokenName.proxy(), spokenName);

        // test no spokenname (will return null)
        jmockId0MessageInbox0.expects(once()).method("getSpokenNameOfSender").
                withNoArguments().will(returnValue(null));
        try {
            platformAccess.messageGetSpokenNameOfSender(getTestMessageId());
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.DATANOTFOUND, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageGetSpokenNameOfSender") > -1);
        }

        // test exception
        jmockId0MessageInbox0.expects(once()).method("getSpokenNameOfSender").
                will(throwException(new MailboxException("Error")));
        try {
            platformAccess.messageGetSpokenNameOfSender(getTestMessageId());
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageGetSpokenNameOfSender") > -1);
        }

        // test invalid messageid
        jmockId0MessageInbox0.expects(never()).method("getSpokenNameOfSender");
        try {
            platformAccess.messageGetSpokenNameOfSender(99);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            assertTrue(e.getDescription().indexOf("messageGetSpokenNameOfSender") > -1);
        }
    }

    /**
     * Tests the messagePrint function.
     *
     * @throws Exception if testcase fails.
     */
    public void testMessagePrint() throws Exception {
        String destination = "5555-90900";
        jmockId0MessageInbox0.expects(once()).method("print").with(eq(destination), isA(String.class));

        platformAccess.messagePrint(getTestMessageId(), destination, "sender");

        // test invalid messageid
        jmockId0MessageInbox0.expects(never()).method("print");
        try {
            platformAccess.messagePrint(99, destination, "sender");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test exception
        jmockId0MessageInbox0.expects(once()).method("print").will(throwException(new MailboxException("Error")));
        try {
            platformAccess.messagePrint(getTestMessageId(), destination, "sender");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
        }
    }

    /**
     * Tests the messageRenewIssuedDate function.
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageSetExpiryDate() throws Exception {
        jmockId0MessageInbox0.expects(once()).method("messageSetExpiryDate").withNoArguments();

        platformAccess.messageSetExpiryDate(getTestMessageId(), "1");

        // test invalid messageid
        jmockId0MessageInbox0.expects(never()).method("messageSetExpiryDate");
        try {
            platformAccess.messageSetExpiryDate(99, "1");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test exception
        jmockId0MessageInbox0.expects(once()).method("messageSetExpiryDate").will(throwException(new MailboxException("Error")));
        try {
            platformAccess.messageSetExpiryDate(getTestMessageId(), "1");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
        }
    }

    /**
     * Tests the messageCopyToFolder function.
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageCopyToFolder() throws Exception {
        jmockId0MessageInbox0.expects(once()).method("copy").with(eq(jmockFolderId0Inbox.proxy()));
        // test messageids from mailbox0 and INBOX
        int mailboxId = platformAccess.subscriberGetMailbox("161074");
        int folderId0 = platformAccess.mailboxGetFolder(mailboxId, INBOX);
        int messageListId = platformAccess.mailboxGetMessageList(folderId0, VOICE, "", "", "", LIFO);
        int[] ids = platformAccess.mailboxGetMessages(messageListId);

        platformAccess.messageCopyToFolder(mailboxId, ids[0], INBOX);

        // test exception
        jmockId0MessageInbox0.expects(once()).method("copy").will(throwException(new MailboxException("Error")));
        try {
            platformAccess.messageCopyToFolder(mailboxId, ids[0], INBOX);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
        }

        // test invalid foldername
        jmockMailboxId0.expects(once()).method("getFolder").will(throwException(new FolderNotFoundException("No such folder")));
        try {
            platformAccess.messageCopyToFolder(mailboxId, ids[0], "nofolder");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
        }

        // test invalid messageid
        try {
            platformAccess.messageCopyToFolder(mailboxId, 99, INBOX);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test invalid mailboxid
        try {
            platformAccess.messageCopyToFolder(99, ids[0], INBOX);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Tests the messageCopyToFolder function.
     *
     * @throws Exception if testcase fails.
     */
    public void testMessageCopyToFolder2() throws Exception {
        Mock jmockFolderSubtrash = mock(IFolder.class);
        jmockFolderId0Trash.expects(once()).method("getFolder").with(eq("subtrash")).
                will(returnValue(jmockFolderSubtrash.proxy()));

        jmockId0MessageInbox0.expects(once()).method("copy").with(eq(jmockFolderSubtrash.proxy()));
        // test messageids from mailbox0 and INBOX
        int mailboxId = platformAccess.subscriberGetMailbox("161074");
        int folderId0 = platformAccess.mailboxGetFolder(mailboxId, INBOX);
        int messageListId = platformAccess.mailboxGetMessageList(folderId0, VOICE, "", "", "", LIFO);
        int[] ids = platformAccess.mailboxGetMessages(messageListId);
        int folderIdTrash = platformAccess.mailboxGetFolder(mailboxId, TRASH);

        platformAccess.messageCopyToFolder(mailboxId, folderIdTrash, ids[0], "subtrash");

        // test invalid foldername
        jmockFolderId0Trash.expects(once()).method("getFolder").with(eq("nofolder")).
                will(throwException(new FolderNotFoundException("nofolder")));
        try {
            platformAccess.messageCopyToFolder(mailboxId, folderIdTrash, ids[0], "nofolder");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.MAILBOX, e.getMessage());
        }

        // test invalid folderid
        try {
            platformAccess.messageCopyToFolder(mailboxId, 99, ids[0], INBOX);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test invalid messageid
        try {
            platformAccess.messageCopyToFolder(mailboxId, folderIdTrash, 99, INBOX);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test invalid mailboxid
        try {
            platformAccess.messageCopyToFolder(99, folderIdTrash, ids[0], INBOX);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    private void printIds(int[] ids) {
        for (int i = 0; i < ids.length; i++) {
            System.out.print(ids[i] + " ");
        }
        System.out.println("");
    }

    private void setupMediaObjects() {
        // setup mediaobjects on some mocked IMessageContent
        jmockId0MessageInbox0Content0Media0 = mock(IMediaObject.class);
        jmockId0MessageInbox0Content0.stubs().method("getMediaObject").will(returnValue(jmockId0MessageInbox0Content0Media0.proxy()));
        jmockId0MessageInbox1Content0Media0 = mock(IMediaObject.class);
        jmockId0MessageInbox1Content0.stubs().method("getMediaObject").will(returnValue(jmockId0MessageInbox1Content0Media0.proxy()));
        jmockId0MessageTrash0Content0Media0 = mock(IMediaObject.class);
        jmockId0MessageTrash0Content0.stubs().method("getMediaObject").will(returnValue(jmockId0MessageTrash0Content0Media0.proxy()));
        jmockId0MessageTrash0Content1Media0 = mock(IMediaObject.class);
        jmockId0MessageTrash0Content1.stubs().method("getMediaObject").will(returnValue(jmockId0MessageTrash0Content1Media0.proxy()));
    }

    private void setupMediaProperties() {
        // setup mediaproperties on some mocked IMessageContent
        // MediaProperties can't be mocked (not an interface)
        MimeType voice = null;
        MimeType video = null;
        MimeType html = null;
        try {
            voice = new MimeType("audio/pcmu");
            video = new MimeType("video/h263");
            html = new MimeType("text/html");
        } catch (MimeTypeParseException e) {
            fail("Could not create MimeTypes for mocking MediaProperties " + e);
        }

        MediaProperties prop0 = new MediaProperties(voice);
        prop0.addLengthInUnit(MediaLength.LengthUnit.MILLISECONDS, 50000);
        prop0.setSize(123456789);
        jmockId0MessageInbox0Content0.stubs().method("getMediaProperties").will(returnValue(prop0));

        // Add a mediacontent that has no length just size.
        MediaProperties prop3 = new MediaProperties(voice);
        prop3.setSize(123456789);
        jmockId0MessageInbox1Content0.stubs().method("getMediaProperties").will(returnValue(prop3));

        MediaProperties prop1 = new MediaProperties(video);
        prop1.addLengthInUnit(MediaLength.LengthUnit.PAGES, 6);
        prop1.setSize(987654321);
        jmockId0MessageTrash0Content0.stubs().method("getMediaProperties").will(returnValue(prop1));

        MediaProperties prop2 = new MediaProperties(html);
        prop2.setSize(666); //should be counted in a different way
        jmockId0MessageTrash0Content1.stubs().method("getMediaProperties").will(returnValue(prop2));
    }

    private void setupMessages() {
        // message0 in INBOX for mailbox0 will have 1 messagecontent
        List<IMessageContent> id0MessageInbox0ContentList = new ArrayList<IMessageContent>();
        id0MessageInbox0ContentList.add((IMessageContent) jmockId0MessageInbox0Content0.proxy());
        jmockId0MessageInbox0.stubs().method("getContent").will(returnValue(id0MessageInbox0ContentList));
        // message1 in INBOX for mailbox0 will have 1 messagecontent without mediaprop length
        List<IMessageContent> id0MessageInbox1ContentList = new ArrayList<IMessageContent>();
        id0MessageInbox1ContentList.add((IMessageContent) jmockId0MessageInbox1Content0.proxy());
        jmockId0MessageInbox1.stubs().method("getContent").will(returnValue(id0MessageInbox1ContentList));

        //message0 in TRASH for mailbox0 will have 2 messagecontents
        List<IMessageContent> id0MessageTrash0ContentList = new ArrayList<IMessageContent>();
        id0MessageTrash0ContentList.add((IMessageContent) jmockId0MessageTrash0Content0.proxy());
        id0MessageTrash0ContentList.add((IMessageContent) jmockId0MessageTrash0Content1.proxy());
        jmockId0MessageTrash0.stubs().method("getContent").will(returnValue(id0MessageTrash0ContentList));

        //message0 in INBOX for mailbox1 will have 0 messagecontent (empty)
        List<IMessageContent> id1MessageInbox0ContentList = new ArrayList<IMessageContent>();
        jmockId1MessageInbox0.stubs().method("getContent").will(returnValue(id1MessageInbox0ContentList));
    }

    private void setupFolders() {
        StubIStoredMessageList inboxMessagesId0 = new StubIStoredMessageList();
        inboxMessagesId0.add((IStoredMessage) jmockId0MessageInbox0.proxy());
        inboxMessagesId0.add((IStoredMessage) jmockId0MessageInbox1.proxy());
        jmockFolderId0Inbox.stubs().method("searchMessages").will(returnValue(inboxMessagesId0));

        StubIStoredMessageList trashMessagesId0 = new StubIStoredMessageList();
        trashMessagesId0.add((IStoredMessage) jmockId0MessageTrash0.proxy());
        trashMessagesId0.add((IStoredMessage) jmockId0MessageTrash1.proxy());
        jmockFolderId0Trash.stubs().method("searchMessages").will(returnValue(trashMessagesId0));

        StubIStoredMessageList inboxMessagesId1 = new StubIStoredMessageList();
        inboxMessagesId1.add((IStoredMessage) jmockId1MessageInbox0.proxy());
        inboxMessagesId1.add((IStoredMessage) jmockId1MessageInbox1.proxy());
        inboxMessagesId1.add((IStoredMessage) jmockId1MessageInbox2.proxy());
        jmockFolderId1Inbox.stubs().method("searchMessages").will(returnValue(inboxMessagesId1));

        StubIStoredMessageList trashMessagesId1 = new StubIStoredMessageList();
        trashMessagesId1.add((IStoredMessage) jmockId1MessageTrash0.proxy());
        trashMessagesId1.add((IStoredMessage) jmockId1MessageTrash1.proxy());
        trashMessagesId1.add((IStoredMessage) jmockId1MessageTrash2.proxy());
        trashMessagesId1.add((IStoredMessage) jmockId1MessageTrash3.proxy());
        jmockFolderId1Trash.stubs().method("searchMessages").will(returnValue(trashMessagesId1));
    }

    /**
     * Helper method to load mailbox and folder and then retrieve a messageId used for testing.
     *
     * @return int
     */
    private int getTestMessageId() {
        String types = commaSeparate(VOICE, VIDEO, FAX, EMAIL);
        String states = commaSeparate(NEW, READ, DELETED);
        String priorities = commaSeparate(URGENT, NONURGENT);
        String orders = commaSeparate(TYPE, STATE);

        //test messageids from mailbox0 and INBOX
        int mailboxId0 = platformAccess.subscriberGetMailbox("161074");
        int folderId0 = platformAccess.mailboxGetFolder(mailboxId0, INBOX);
        int messageListId = platformAccess.mailboxGetMessageList(folderId0, types, states, priorities, orders, FIFO);
        int[] messageIds = platformAccess.mailboxGetMessages(messageListId);
        return messageIds[0];
    }

    public static Test suite() {
        return new TestSuite(PlatformAccessMailboxMessageTest.class);
    }
}

class StubIStoredMessageList extends ArrayList<IStoredMessage> implements IStoredMessageList {

    public IStoredMessageList select(Criteria<MessagePropertyCriteriaVisitor> criteria) {
        StubIStoredMessageList list = new StubIStoredMessageList();
        if (!isEmpty()) {
            list.add(get(0));
        }
        return list;
    }
}
