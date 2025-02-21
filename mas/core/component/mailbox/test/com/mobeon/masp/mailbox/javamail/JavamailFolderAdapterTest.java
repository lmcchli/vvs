/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import junit.framework.*;
import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mailbox.mock.MimeMessageMock;
import com.mobeon.masp.mailbox.mock.FolderMock;
import com.mobeon.masp.mailbox.search.TypeCriteria;
import com.mobeon.masp.mailbox.search.OrCriteria;
import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.mailbox.search.StoredMessageMatcher;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.criteria.Criteria;

import jakarta.mail.Message;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.event.ConnectionEvent;
import jakarta.mail.event.FolderEvent;
import jakarta.mail.event.MessageChangedEvent;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

/**
 * JavamailFolderAdapter test
 *
 * @author mande
 */
public class JavamailFolderAdapterTest extends JavamailBaseTestCase {
    protected static final String LOG4J_CONFIGURATION = "../log4jconf.xml";
    static {
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    private JavamailFolderAdapter javamailFolderAdapter;
    static final int JAVAMAIL_EVENTQUEUE_WAIT = 200;

    public JavamailFolderAdapterTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        // Superclass sets up folderMock, javamailContext and javamailStoreAdapter
        super.setUp();
        javamailFolderAdapter = new JavamailFolderAdapter(folderMock, javamailContext, javamailStoreAdapter);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test open a folder
     * @throws Exception
     */
    public void testOpen() throws Exception {
        folderMock.expects(once()).method("isOpen").will(returnValue(false));
        folderMock.expects(once()).method("open").with(eq(Folder.READ_WRITE));
        javamailFolderAdapter.open();
        folderMock.expects(once()).method("isOpen").will(returnValue(true));
        javamailFolderAdapter.open();
        folderMock.expects(once()).method("isOpen").will(returnValue(false));
        folderMock.expects(once()).method("open").will(throwException(new MessagingException()));
        try {
            javamailFolderAdapter.open();
            fail("Expected MessagingException");
        } catch (MessagingException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test create a folder
     * @throws Exception
     */
    public void testCreate() throws Exception {
        // Create existing folder
        folderMock.expects(once()).method("exists").will(returnValue(true));
        folderMock.expects(once()).method("getName").will(returnValue("folder")); // Used for exception message
        try {
            javamailFolderAdapter.create();
            fail("Expected FolderAlreadyExistsException");
        } catch (FolderAlreadyExistsException e) {
            assertTrue(true); // For statistical purposes
        }
        // Create failure
        folderMock.expects(once()).method("exists").will(returnValue(false));
        folderMock.expects(once()).method("create").will(throwException(new MessagingException("messagingexception")));
        folderMock.expects(once()).method("getName").will(returnValue("folder")); // Used for exception message
        try {
            javamailFolderAdapter.create();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Create success
        folderMock.expects(once()).method("exists").will(returnValue(false));
        folderMock.expects(once()).method("create").with(eq(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS)).
                will(returnValue(true));
        javamailFolderAdapter.create();
    }

    /**
     * Test deleting a folder
     * @throws Exception
     */
    public void testDelete() throws Exception {
        // Delete non-existing folder
        folderMock.expects(once()).method("exists").will(returnValue(false));
        try {
            javamailFolderAdapter.delete();
            fail("Expected FolderNotFoundException");
        } catch (FolderNotFoundException e) {
            assertTrue(true); // For statistical purposes
        }
        // Delete failure
        folderMock.expects(once()).method("exists").will(returnValue(true));
        folderMock.expects(once()).method("delete").with(eq(true)).
                will(throwException(new MessagingException("messagingexception")));
        folderMock.expects(once()).method("getName").will(returnValue("folder")); // Used for exception message
        try {
            javamailFolderAdapter.delete();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Delete success
        folderMock.expects(once()).method("exists").will(returnValue(true));
        folderMock.expects(once()).method("delete").with(eq(true)).will(returnValue(true));
        javamailFolderAdapter.delete();
    }

    /**
     * Test get folder name
     * @throws Exception
     */
    public void testGetName() throws Exception {
        String folderName = "folder";
        folderMock.expects(atLeastOnce()).method("getName").
                will(onConsecutiveCalls(returnValue(folderName), returnValue(folderName)));
        assertEquals(folderName, javamailFolderAdapter.getName());
    }

    /**
     * Test getting a subfolder
     * @throws Exception
     */
    public void testGetFolder() throws Exception {
        String folderName = "folder";
        folderMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(throwException(new jakarta.mail.FolderNotFoundException(folderMock, "foldernotfoundexception")));
        try {
            javamailFolderAdapter.getFolder(folderName);
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        folderMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(throwException(new MessagingException("messagingexception")));
        try {
            javamailFolderAdapter.getFolder(folderName);
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        FolderMock newFolderMock = getFolderMock(getMockFolder("newMockFolder"));
        folderMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(returnValue(newFolderMock));
        newFolderMock.expects(once()).method("isOpen").will(returnValue(true));
        JavamailFolderAdapter folder = javamailFolderAdapter.getFolder(folderName);
        assertNotNull("Folder should not be null", folder);
        assertSame(newFolderMock, folder.folder);
        // Folder should be cached
        JavamailFolderAdapter cachedFolder = javamailFolderAdapter.getFolder(folderName);
        assertSame(folder, cachedFolder);
    }

    /**
     * Test adding a subfolder
     * @throws Exception
     */
    public void testAddFolder() throws Exception {
        String folderName = "folder";
        // Test add folder when getting folder fails
        folderMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(throwException(new MessagingException("messagingexception")));
        try {
            javamailFolderAdapter.addFolder(folderName);
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test add folder when folder already exists
        FolderMock newFolderMock = getFolderMock(getMockFolder("newMockFolder"));
        folderMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(returnValue(newFolderMock));
        newFolderMock.expects(once()).method("exists").will(returnValue(true));
        newFolderMock.expects(once()).method("getName").will(returnValue("newfolder")); // Used for exception message
        try {
            javamailFolderAdapter.addFolder(folderName);
            fail("Expected FolderAlreadyExistsException");
        } catch (FolderAlreadyExistsException e) {
            assertTrue(true); // For statistical purposes;
        }
        // Test add folder when create fails
        folderMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(returnValue(newFolderMock));
        newFolderMock.expects(once()).method("exists").will(returnValue(false));
        newFolderMock.expects(once()).method("create").will(throwException(new MessagingException("messagingexception")));
        newFolderMock.expects(once()).method("getName").will(returnValue("newfolder")); // Used for exception message
        try {
            javamailFolderAdapter.addFolder(folderName);
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes;
        }
        // Test add folder successfully
        folderMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(returnValue(newFolderMock));
        newFolderMock.expects(once()).method("exists").will(returnValue(false));
        newFolderMock.expects(once()).method("create").will(returnValue(true));
        JavamailFolderAdapter folder = javamailFolderAdapter.addFolder(folderName);
        assertNotNull("Folder should not be null", folder);
        assertSame(newFolderMock, folder.folder);
    }

    public void testDeleteFolder() throws Exception {
        String folderName = "folder";
        // Test delete folder when retrieving folder fails
        folderMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(throwException(new MessagingException("messagingexception")));
        try {
            javamailFolderAdapter.deleteFolder(folderName);
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test delete folder when folder does not exist
        FolderMock newFolderMock = getFolderMock(getMockFolder("newMockFolder"));
        folderMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(returnValue(newFolderMock));
        newFolderMock.expects(once()).method("exists").will(returnValue(false));
        try {
            javamailFolderAdapter.deleteFolder(folderName);
            fail("Expected FolderNotFoundException");
        } catch (FolderNotFoundException e) {
            assertTrue(true); // For statistical purposes;
        }
        // Test delete folder when deleting folder fails
        folderMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(returnValue(newFolderMock));
        newFolderMock.expects(once()).method("exists").will(returnValue(true));
        newFolderMock.expects(once()).method("delete").with(eq(true)).
                will(throwException(new MessagingException("messagingexception")));
        newFolderMock.expects(once()).method("getName").will(returnValue("newfolder")); // Used for exception message
        try {
            javamailFolderAdapter.deleteFolder(folderName);
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes;
        }
        // Test deleting folder successfully
        folderMock.expects(once()).method("getFolder").with(eq(folderName)).
                will(returnValue(newFolderMock));
        newFolderMock.expects(once()).method("exists").will(returnValue(true));
        newFolderMock.expects(once()).method("delete").with(eq(true)).will(returnValue(true));
        javamailFolderAdapter.deleteFolder(folderName);
    }

    public void testGetMessages() throws Exception {
        // Test when get fails
        folderMock.expects(once()).method("isOpen").will(returnValue(true));
        folderMock.expects(once()).method("getMessages").will(throwException(new MessagingException("messagingexception")));
        try {
            javamailFolderAdapter.getMessages();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test when get succeeds
        folderMock.expects(once()).method("isOpen").will(returnValue(true));
        Message[] messages = new Message[0];
        folderMock.expects(once()).method("getMessages").will(returnValue(messages));
        IStoredMessageList messageList = javamailFolderAdapter.getMessages();
        assertEquals(0, messageList.size());
        // Test when comparator is submitted
        messages = new Message[2];
        messages[0] = new MimeMessageMock(folderMock, getVoiceMessageInputStream(), 2);
        messages[0].setSubject("b");
        messages[1] = new MimeMessageMock(folderMock, getVoiceMessageInputStream(), 1);
        messages[1].setSubject("a");
        folderMock.expects(once()).method("isOpen").will(returnValue(true));
        folderMock.expects(once()).method("getMessages").will(returnValue(messages));
        // Sort messages on subject
        messageList = javamailFolderAdapter.getMessages(new Comparator<IStoredMessage>() {
            public int compare(IStoredMessage o1, IStoredMessage o2) {
                return o1.getSubject().compareTo(o2.getSubject());
            }
        });
        assertEquals(2, messageList.size());
        // Messages should have been sorted
        assertEquals("a", messageList.get(0).getSubject());
        assertEquals("b", messageList.get(1).getSubject());
    }

    public void testSearchMessages() throws Exception {
        // Test when search fails
        folderMock.expects(once()).method("isOpen").will(returnValue(true));
        // Todo: test parameter constraint on search method
        folderMock.expects(once()).method("search").will(throwException(new MessagingException("messagingexception")));
        try {
            javamailFolderAdapter.searchMessages(new TypeCriteria(MailboxMessageType.VOICE));
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        // Test when search succeeds
        folderMock.expects(once()).method("isOpen").will(returnValue(true));
        Message[] messages = new Message[0];
        folderMock.expects(once()).method("search").will(returnValue(messages));
        IStoredMessageList messageList = javamailFolderAdapter.searchMessages(new TypeCriteria(MailboxMessageType.VOICE));
        assertEquals(0, messageList.size());
        // Test when comparator is submitted
        messages = new Message[2];
        messages[0] = new MimeMessageMock(folderMock, getVoiceMessageInputStream(), 2);
        messages[0].setSubject("b"); // Used for comparison
        messages[1] = new MimeMessageMock(folderMock, getVoiceMessageInputStream(), 1);
        messages[1].setSubject("a"); // Used for comparison
        folderMock.expects(once()).method("isOpen").will(returnValue(true));
        folderMock.expects(once()).method("search").will(returnValue(messages));
        // Sort messages on subject
        messageList = javamailFolderAdapter.searchMessages(
                new TypeCriteria(MailboxMessageType.VOICE),
                new Comparator<IStoredMessage>() {
                    public int compare(IStoredMessage o1, IStoredMessage o2) {
                        return o1.getSubject().compareTo(o2.getSubject());
                    }
                }
        );
        assertEquals(2, messageList.size());
        // Messages should have been sorted
        assertEquals("a", messageList.get(0).getSubject());
        assertEquals("b", messageList.get(1).getSubject());
    }

    /**
     * Test that search of message types returns correct delivery reports
     * @throws Exception
     */
    public void testDeliveryMessages() throws Exception {
        // Set up expected messages depending on different search criteria
        Map<Criteria<MessagePropertyCriteriaVisitor>, Integer> expectations =
                new HashMap<Criteria<MessagePropertyCriteriaVisitor>, Integer>();
        expectations.put(new TypeCriteria(MailboxMessageType.VOICE), 1);
        expectations.put(new TypeCriteria(MailboxMessageType.VIDEO), 1);
        expectations.put(new TypeCriteria(MailboxMessageType.FAX), 1);
        expectations.put(new TypeCriteria(MailboxMessageType.EMAIL), 1);
        expectations.put(new OrCriteria(
                new TypeCriteria(MailboxMessageType.VOICE),
                new TypeCriteria(MailboxMessageType.VIDEO)
        ), 2);
        expectations.put(new OrCriteria(
                new TypeCriteria(MailboxMessageType.VOICE),
                new TypeCriteria(MailboxMessageType.VIDEO),
                new TypeCriteria(MailboxMessageType.FAX)
        ), 3);
        expectations.put(new OrCriteria(
                new TypeCriteria(MailboxMessageType.VOICE),
                new TypeCriteria(MailboxMessageType.VIDEO),
                new TypeCriteria(MailboxMessageType.FAX),
                new TypeCriteria(MailboxMessageType.EMAIL)
        ), 4);

        // Test expectations
        for (Map.Entry<Criteria<MessagePropertyCriteriaVisitor>,Integer> entry : expectations.entrySet()) {
            folderMock.expects(once()).method("isOpen").will(returnValue(true));
            folderMock.expects(once()).method("search").will(returnValue(getDeliveryMessageList()));
            IStoredMessageList messageList = javamailFolderAdapter.searchMessages(entry.getKey());
            assertEquals(entry.getValue().intValue(), messageList.size());
            for (IStoredMessage message : messageList) {
                assertTrue("Message of wrong type", StoredMessageMatcher.match(entry.getKey(), message));
            }
        }
    }

    /**
     * Test that the Debugger listener writes <code>ConnectionEvent</code>:s to debug
     * @throws Exception
     */
    public void testConnectionListeners() throws Exception {
        setUpMockAppender();
        mockAppender.expects(once()).method("doAppend").with(aDebugLog());
        folderMock.notifyConnectionListeners(ConnectionEvent.OPENED);
        mockAppender.expects(once()).method("doAppend").with(aDebugLog());
        folderMock.notifyConnectionListeners(ConnectionEvent.CLOSED);
        mockAppender.expects(once()).method("doAppend").with(aDebugLog());
        folderMock.notifyConnectionListeners(ConnectionEvent.DISCONNECTED);
        // Wait for the Javamail-EventQueue
        Thread.sleep(JAVAMAIL_EVENTQUEUE_WAIT);
        tearDownMockAppender();
    }

    /**
     * Test that the Debugger listener writes <code>FolderEvent</code>:s to debug
     * @throws Exception
     */
    public void testFolderListeners() throws Exception {
        FolderMock renamedFolder = getFolderMock(getMockFolder("renamedfolder"));
        setUpMockAppender();
        mockAppender.expects(once()).method("doAppend").with(aDebugLog());
        folderMock.notifyFolderListeners(FolderEvent.CREATED);
        mockAppender.expects(once()).method("doAppend").with(aDebugLog());
        folderMock.notifyFolderListeners(FolderEvent.DELETED);
        mockAppender.expects(once()).method("doAppend").with(aDebugLog());
        renamedFolder.expects(once()).method("getName").will(returnValue("renamedfoldername"));
        folderMock.notifyFolderRenamedListeners(renamedFolder);
        // Wait for the Javamail-EventQueue
        Thread.sleep(JAVAMAIL_EVENTQUEUE_WAIT);
        tearDownMockAppender();
    }

    /**
     * Test that the Debugger listener writes <code>MessageChangedEvent</code>:s to debug
     * @throws Exception
     */
    public void testMessageChangedListeners() throws Exception {
        MimeMessageMock message = new MimeMessageMock(folderMock, getVoiceMessageInputStream(), 1);
        setUpMockAppender();
        mockAppender.expects(once()).method("doAppend").with(aDebugLog());
        folderMock.notifyMessageChangedListeners(MessageChangedEvent.ENVELOPE_CHANGED, message);
        mockAppender.expects(once()).method("doAppend").with(aDebugLog());
        folderMock.notifyMessageChangedListeners(MessageChangedEvent.FLAGS_CHANGED, message);
        mockAppender.expects(once()).method("doAppend").with(aDebugLog());
        folderMock.notifyMessageChangedListeners(-1, message); // Unknown event type
        // Wait for the Javamail-EventQueue
        Thread.sleep(JAVAMAIL_EVENTQUEUE_WAIT);
        tearDownMockAppender();
    }

    /**
     * Test that the Debugger listener writes <code>MessageCountEvent</code>:s to debug
     * @throws Exception
     */
    public void testMessageCountListeners() throws Exception {
        setUpMockAppender();
        mockAppender.expects(once()).method("doAppend").with(aDebugLog());
        folderMock.notifyMessageAddedListeners(new Message[0]);
        mockAppender.expects(once()).method("doAppend").with(aDebugLog());
        folderMock.notifyMessageRemovedListeners(true, new Message[0]);
        // Wait for the Javamail-EventQueue
        Thread.sleep(JAVAMAIL_EVENTQUEUE_WAIT);
        tearDownMockAppender();
    }

    private Message[] getDeliveryMessageList() throws Exception {
        InputStream[] streams = new InputStream[] {
                getVoiceMessageDeliveryReportInputStream(),
                getVideoMessageDeliveryReportInputStream(),
                getFaxMessageDeliveryReportInputStream(),
                getEmailMessageDeliveryReportInputStream()
        };
        Message[] messages = new Message[streams.length];
        for (int i = 0; i < streams.length; i++) {
            messages[i] = new MimeMessageMock(folderMock, streams[i], i);
        }
        return messages;
    }

    private InputStream getVoiceMessageDeliveryReportInputStream() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        appendDeliveryReportHeader(pw);
        appendVoiceMessage(pw);
        appendDeliveryReportFooter(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    private InputStream getVideoMessageDeliveryReportInputStream() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        appendDeliveryReportHeader(pw);
        appendVideoMessage(pw);
        appendDeliveryReportFooter(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    private InputStream getFaxMessageDeliveryReportInputStream() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        appendDeliveryReportHeader(pw);
        appendFaxMessage(pw);
        appendDeliveryReportFooter(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    private InputStream getEmailMessageDeliveryReportInputStream() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        appendDeliveryReportHeader(pw);
        appendEmailMessage(pw);
        appendDeliveryReportFooter(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    private void appendDeliveryReportHeader(PrintWriter pw) {
        pw.println("Content-Type: multipart/report; report-type=delivery-status; boundary=\"deliveryreportboundary\"");
        pw.println();
        pw.println("--deliveryreportboundary");
        pw.println("Content-Type: text/plain");
        pw.println("");
        pw.println("human readable message");
        pw.println("");
        pw.println("--deliveryreportboundary");
        pw.println("Content-Type: message/delivery-status");
        pw.println("");
        pw.println("");
        pw.println("--deliveryreportboundary");
        pw.println("Content-Type: message/rfc822");
        pw.println();
    }

    private void appendDeliveryReportFooter(PrintWriter pw) {
        pw.println();
        pw.println("--deliveryreportboundary--");
    }

    public static Test suite() {
        return new TestSuite(JavamailFolderAdapterTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}