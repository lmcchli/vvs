/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import jakarta.mail.*;
import jakarta.mail.internet.MailDateFormat;
import jakarta.mail.internet.MimeMessage;

import com.mobeon.masp.mailbox.mock.*;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxBaseTestCase;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.masp.mailbox.imap.ImapProperties;
import com.mobeon.masp.util.content.PageCounter;
import com.mobeon.masp.util.content.PageBreakingStringCounter;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.message_sender.IInternetMailSender;

import java.util.*;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.ByteArrayInputStream;

/**
 * Base test case for javamail test classes
 *
 * @author mande
 */
public abstract class JavamailBaseTestCase extends MailboxBaseTestCase {
    protected static final Session SESSION = Session.getInstance(new Properties());
    protected FolderMock folderMock;
    protected Mock mockInternetMailSender = mock(IInternetMailSender.class);
    protected JavamailContext javamailContext;
    protected JavamailStoreAdapter javamailStoreAdapter;
    protected Mock mockStore;
    static final String ADDITIONAL_PROPERTY = "property1";
    static final String ADDITIONAL_PROPERTY_HEADER_NAME = "property1headername";
    Mock mockFolder;
    Mock mockAppender;

    public JavamailBaseTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        setUpMockStore();
        setUpMockFolder();
        setUpJavamailContext();
        setUpJavamailStoreAdapter();
    }

    /**
     * Sets up a StoreMock for
     * @throws Exception
     */
    protected void setUpMockStore() throws Exception {
        mockStore = mock(StoreInterface.class);
        StoreMock.setMockStore(mockStore);
    }

    protected void setUpMockFolder() throws Exception {
        mockFolder = getMockFolder("mockFolder");
        folderMock = getFolderMock(mockFolder);
    }

    protected void setUpJavamailContext() throws MailboxException {
        JavamailContextFactory javamailContextFactory = getJavamailContextFactory();
        javamailContext = javamailContextFactory.create(new MailboxProfile("accountid", "accountpassword", "emailaddress"));
    }

    protected void setUpJavamailStoreAdapter() throws NoSuchProviderException {
        javamailStoreAdapter = new JavamailStoreAdapter(getStore(), javamailContext);
    }

    /**
     * Sets up a mocked Appender, so that log behavior can be tested
     */
    void setUpMockAppender() {
        mockAppender = mock(Appender.class);
        Logger.getRootLogger().addAppender((Appender)mockAppender.proxy());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tears down a mocked Appender, so that log behavior no longer is tested
     */
    void tearDownMockAppender() {
        Logger.getRootLogger().removeAppender((Appender)mockAppender.proxy());
    }

    /**
     * Gets a mock object mocking FolderInterface. Used for handling expectations, constraints and stub behavior for
     * <code>FolderMock</code> objects.
     * @return a mock of a FolderInterface
     * @param roleName
     */
    Mock getMockFolder(String roleName) {
        Mock mockFolder = mock(FolderInterface.class, roleName);
        mockFolder.stubs().method("getFullName").will(returnValue("mockFolder"));
        return mockFolder;
    }

    /**
     * Gets a FolderMock mocking a Folder object. Needs a mock object mocking FolderInterface for expectations,
     * constraints and stub behavior.
     * @param mockFolder the mock object to use for expectations, constraints and stub behavior.
     * @return a FolderMock
     * @throws Exception
     */
    FolderMock getFolderMock(Mock mockFolder) throws Exception {
        FolderMock folderMock = new FolderMock(getStore());
        folderMock.setMockFolder(mockFolder);
        return folderMock;
    }

    protected Store getStore() throws NoSuchProviderException {
        return SESSION.getStore("imap");
    }

    protected JavamailContextFactory getJavamailContextFactory() {
        JavamailContextFactory javamailContextFactory = new JavamailContextFactory();
        javamailContextFactory.setDefaultSessionProperties(new Properties());
        javamailContextFactory.setInternetMailSender(getMockInternetMailSender());
        javamailContextFactory.setImapProperties(new ImapProperties());
        javamailContextFactory.setJavamailBehavior(new JavamailBehavior());
        javamailContextFactory.setPageCounterMap(getPageCounterMap());
        javamailContextFactory.setConfiguration(getMockConfiguration());
        javamailContextFactory.setMediaObjectFactory(new MediaObjectFactory());
        return javamailContextFactory;
    }

    private HashMap<String, PageCounter> getPageCounterMap() {
        HashMap<String, PageCounter> pageCounterMap = new HashMap<String, PageCounter>();
        pageCounterMap.put("image/tiff", new PageBreakingStringCounter("Fax Image"));
        return pageCounterMap;
    }

    protected IGroup getMockConfigGroup() {
        Mock mockConfigGroup = mock(IGroup.class, "mockConfigGroup");
        // The stubs method is used here since BaseConfig.additionalPropertyMap is static so getGroups is only called once
        mockConfigGroup.stubs().method("getGroups").with(eq("message.additionalproperty")).
                will(returnValue(Arrays.asList(getAdditionalPropertyGroup())));
        mockConfigGroup.expects(once()).method("getGroup").with(eq("imap")).
                will(returnValue(getImapGroup()));
        return (IGroup)mockConfigGroup.proxy();
    }

    protected IGroup getAdditionalPropertyGroup() {
        Mock additionalPropertyGroup = mock(IGroup.class, "mockAdditionalPropertyGroup");
        // The stubs method is used here since BaseConfig.additionalPropertyMap is static so getGroups is only called once
        additionalPropertyGroup.stubs().method("getString").with(eq("name")).will(returnValue(ADDITIONAL_PROPERTY));
        additionalPropertyGroup.stubs().method("getString").with(eq("field")).will(returnValue(ADDITIONAL_PROPERTY_HEADER_NAME));
        return (IGroup)additionalPropertyGroup.proxy();
    }

    private IGroup getImapGroup() {
        Mock mockImapGroup = mock(IGroup.class, "mockImapGroup");
        mockImapGroup.expects(once()).method("getInteger").with(eq("connectiontimeout"), eq(5000)).
                will(returnValue(5000));
        mockImapGroup.expects(once()).method("getInteger").with(eq("commandtimeout"), eq(5000)).
                will(returnValue(5000));
        return (IGroup)mockImapGroup.proxy();
    }

    private IInternetMailSender getMockInternetMailSender() {
        return (IInternetMailSender)mockInternetMailSender.proxy();
    }

    InputStream getVoiceMessageInputStream() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        appendVoiceMessage(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    void appendVoiceMessage(PrintWriter pw) {
        pw.println("Content-Type: multipart/voice-message; boundary=\"voicemessageboundary\"");
        pw.println("Subject: Voice Message From John Doe");
        pw.println();
        pw.println("--voicemessageboundary");
        pw.println("Content-type: audio/wav");
        pw.println();
        pw.println();
        pw.println("--voicemessageboundary--");
    }

    protected Constraint aMessageWith(String subject, MailboxMessageType type, String sender, String[] recipients, String[] secondaryRecipients, boolean urgent, boolean confidential, String replyToAddress, Date deliveryDate, String language, int parts, String additionalProperty) {
        return new MessageConstraint(subject, type, sender, recipients, secondaryRecipients, urgent, confidential, replyToAddress, deliveryDate, language, parts, additionalProperty);
    }

    /**
     * Tests storing a message with and without using the host parameter
     * @param javamailStorableMessage
         * @param messageConstraint the expected message constraint
     * @param host the host to use for storage
     * @throws com.mobeon.masp.mailbox.MailboxException
     */
    protected void testStore(JavamailStorableMessage javamailStorableMessage, Constraint messageConstraint, String host) throws MailboxException {
        mockInternetMailSender.expects(once()).method("sendInternetMail").with(messageConstraint, NULL);
        javamailStorableMessage.store(null);
        mockInternetMailSender.expects(once()).method("sendInternetMail").with(messageConstraint, eq(host), NULL);
        javamailStorableMessage.store(host);
    }

    protected Constraint anInfoLog() {
        return new LogLevelConstraint(Level.INFO);
    }

    protected Constraint aWarnLog() {
        return new LogLevelConstraint(Level.WARN);
    }

    protected Constraint aDebugLog() {
        return new LogLevelConstraint(Level.DEBUG);
    }

    protected InputStream getVideoMessageInputStream() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        appendVideoMessage(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    protected InputStream getFaxMessageInputStream() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        appendFaxMessage(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    protected InputStream getEmailMessageInputStream() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        appendEmailMessage(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    protected void appendVideoMessage(PrintWriter pw) {
        pw.println("Content-Type: multipart/x-video-message; boundary=\"videomessageboundary\"");
        pw.println();
        pw.println("--videomessageboundary");
        pw.println("Content-type: video/quicktime");
        pw.println();
        pw.println();
        pw.println("--videomessageboundary--");
    }

    protected void appendFaxMessage(PrintWriter pw) {
        pw.println("Content-Type: multipart/fax-message; boundary=\"faxmessageboundary\"");
        pw.println();
        pw.println("--faxmessageboundary");
        pw.println("Content-type: image/tiff");
        pw.println();
        pw.println();
        pw.println("--faxmessageboundary--");
    }

    protected void appendEmailMessage(PrintWriter pw) {
        pw.println("Content-Type: text/plain");
        pw.println();
        pw.println("body");
    }

    protected Message getMessage(String... headers) throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        for (String header : headers) {
            pw.println(header);
        }
        return new MimeMessageMock(folderMock, new ByteArrayInputStream(sw.toString().getBytes()), 1);
    }

    /**
     * Gets urgent messages that should be supported
     * @return
     * @throws Exception
     */
    protected Message[] getUrgentMessages() throws Exception {
        List<Message> messages = new ArrayList<Message>();
        messages.add(getMessage("X-Priority: 1"));
        messages.add(getMessage("X-Priority: 2"));
        messages.add(getMessage("X-Priority: 1 junk"));
        messages.add(getMessage("X-Priority: 2 junk"));
        messages.add(getMessage("X-Priority: high"));
        messages.add(getMessage("X-Priority: High"));
        messages.add(getMessage("X-Priority: HIGH"));
        messages.add(getMessage("Priority: Urgent"));
        messages.add(getMessage("Importance: High"));
        return messages.toArray(new Message[messages.size()]);
    }

    /**
     * Gets confidential messages that should be supported
     * @return
     * @throws Exception
     */
    protected Message[] getConfidentialMessages() throws Exception {
        List<Message> messages = new ArrayList<Message>();
        messages.add(getMessage("Sensitivity: Personal"));
        messages.add(getMessage("Sensitivity: Private"));
        messages.add(getMessage("Sensitivity: Company-Confidential"));
        messages.add(getMessage("X-Sensitivity: Personal"));
        messages.add(getMessage("X-Sensitivity: Private"));
        messages.add(getMessage("X-Sensitivity: Company-Confidential"));
        return messages.toArray(new Message[messages.size()]);
    }

    private static class MessageConstraint implements Constraint {
        private static final MailDateFormat MAIL_DATE_FORMAT = new MailDateFormat();

        private String subject;
        private MailboxMessageType type;
        private String sender;
        private String[] recipients;
        private String[] secondaryRecipients;
        private boolean urgent;
        private boolean confidential;
        private String replyToAddress;
        private Date deliveryDate;
        private String language;
        private int parts;
        private String additionalProperty;

        public MessageConstraint(String subject, MailboxMessageType type, String sender, String[] recipients,
                                 String[] secondaryRecipients, boolean urgent, boolean confidential,
                                 String replyToAddress, Date deliveryDate, String language, int parts,
                                 String additionalProperty)
        {
            this.subject = subject;
            this.type = type;
            this.sender = sender;
            this.recipients = recipients;
            this.secondaryRecipients = secondaryRecipients;
            this.urgent = urgent;
            this.confidential = confidential;
            this.replyToAddress = replyToAddress;
            this.deliveryDate = deliveryDate;
            this.language = language;
            this.parts = parts;
            this.additionalProperty = additionalProperty;
        }

        public boolean eval(Object o) {
            if (!(o instanceof MimeMessage)) {
                return false;
            }
            MimeMessage message = (MimeMessage)o;
            try {
                // Check message subject
                if (!message.getSubject().equals(subject)) {
                    return false;
                }
                // Check message type. Use own parsing of Message class
                if (!JavamailMessageAdapter.readType(message).equals(type)) {
                    return false;
                }
                // Chekc message sender. Use own parsing of Message class
                if (!JavamailMessageAdapter.extractAddressString(message.getFrom()).equals(sender)) {
                    return false;
                }
                // Check message recipients.
                if (!Arrays.toString(message.getRecipients(Message.RecipientType.TO)).equals(Arrays.toString(recipients))) {
                    return false;
                }
                // Check message secondary recipients. Use own parsing of Message class
                if (!Arrays.toString(JavamailMessageAdapter.extractAddressStrings(
                        message.getRecipients(Message.RecipientType.CC))).equals(Arrays.toString(secondaryRecipients))) {
                    return false;
                }
                // Check message urgency. Use own parsing of Message class
                if (UrgentHeaderUtil.isUrgent(message) != urgent) {
                    return false;
                }
                // Check message confidentiality. Use own parsing of Message class
                if (ConfidentialHeaderUtil.isConfidential(message) != confidential) {
                    return false;
                }
                // Check message reply to address.
                if (!Arrays.toString(message.getReplyTo()).equals(Arrays.toString(new String [] {replyToAddress}))) {
                    return false;
                }
                // Check message delivery date
                if (message.getSentDate() != null && !dateEquals(message.getSentDate())) {
                    return false;
                }
                String[] header = message.getHeader("Deferred-Delivery");
                if (header != null && header.length == 1 && !dateEquals(MAIL_DATE_FORMAT.parse(header[0]))) {
                    return false;
                }
                if (message.getSentDate() != null && !dateEquals(message.getSentDate())) {
                    return false;
                }
                // Check message language. Use own parsing of Message class
                if (!JavamailMessageAdapter.parseLanguage(message).equals(language)) {
                    return false;
                }
                // Check number of message parts Todo: check the actual parts?
                if (!message.getContentType().matches("(?s)multipart/.*")) {
                    return false;
                }
                Multipart multipart = (Multipart)message.getContent();
                if (multipart.getCount() != parts) {
                    return false;
                }
                // Check additional properties
                String[] additionalProperties = message.getHeader(ADDITIONAL_PROPERTY_HEADER_NAME);
                if (additionalProperty != null && additionalProperties != null &&
                        !additionalProperties[0].equals(additionalProperty)) {
                    return false;
                }
            } catch (Exception e) {
                fail("MessageConstraint.eval(Object) threw " + e);
            }
            return true;
        }

        public StringBuffer describeTo(StringBuffer buffer) {
            buffer.append("a message containing ");
            buffer.append("subject=").append(subject);
            buffer.append(", type=").append(type);
            buffer.append(", sender=").append(sender);
            buffer.append(", recipients=").append(Arrays.toString(recipients));
            buffer.append(", secondaryRecipients=").append(Arrays.toString(secondaryRecipients));
            buffer.append(", urgent=").append(urgent);
            buffer.append(", confidential=").append(confidential);
            buffer.append(", replyToAddress=").append(replyToAddress);
            buffer.append(", deliveryDate=").append(deliveryDate);
            buffer.append(", language=").append(language);
            buffer.append(", parts=").append(parts);
            return buffer;
        }

        /**
         * Checks if the actual date is equal to the expected date. Since Javamail does not have millisecond precision,
         * it checks if the difference between the dates is less than that.
         * @param actual the date to compare
         * @return true, if the difference between the dates is less than 1000 milliseconds, otherwise false
         */
        private boolean dateEquals(Date actual) {
            return Math.abs(actual.getTime() - deliveryDate.getTime()) < 1000;
        }
    }
}
