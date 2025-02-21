/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.common.message_sender.SmtpOptions;
import com.mobeon.common.message_sender.InternetMailSenderException;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;

import org.jmock.core.Constraint;

import jakarta.mail.internet.MimeMessage;
import jakarta.activation.MimeType;
import java.util.Date;
import java.util.Calendar;

/**
 * JavamailStorableMessage Tester.
 *
 * @author MANDE
 * @since <pre>12/08/2006</pre>
 * @version 1.0
 */
public class JavamailStorableMessageTest extends JavamailBaseTestCase {
    private JavamailStorableMessage javamailStorableMessage;

    public JavamailStorableMessageTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        javamailStorableMessage = new JavamailStorableMessage(javamailContext);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test setting and getting smtp options
     * @throws Exception
     */
    public void testSetGetSmtpOptions() throws Exception {
        assertNull("smtpOptions should be null", javamailStorableMessage.getSmtpOptions());
        SmtpOptions smtpOptions = new SmtpOptions();
        javamailStorableMessage.setSmtpOptions(smtpOptions);
        assertSame("smtpOptions should be same", smtpOptions, javamailStorableMessage.getSmtpOptions());
    }

    public void testStoreWorkInternetMailSenderException() throws Exception {
        mockInternetMailSender.expects(once()).method("sendInternetMail").with(isA(MimeMessage.class), NULL).
                will(throwException(new InternetMailSenderException("internetmailsenderexception")));
        try {
            javamailStorableMessage.storeWork(null);
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        String host = "host";
        mockInternetMailSender.expects(once()).method("sendInternetMail").with(isA(MimeMessage.class), eq(host), NULL).
                will(throwException(new InternetMailSenderException("internetmailsenderexception")));
        try {
            javamailStorableMessage.storeWork(host);
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testStoreWorkMessagingException() throws Exception {
        // Todo: see if this exception can be provoked
    }

    public void testStore() throws Exception {
        // Todo: test order and content of body parts
        String host = "host";
        // Test setters
        String subject = "subject";
        MailboxMessageType type = MailboxMessageType.VOICE;
        String sender = "sender <sender@host.com>";
        String recipient = "recipient <recipient@host.com>";
        String secondaryRecipient = "secondaryrecipient <secondaryrecipient@host.com>";
        MediaObjectFactory mediaObjectFactory = new MediaObjectFactory();
        IMediaObject spokenName = mediaObjectFactory.create(
                "spokennamecontent",
                new MediaProperties(
                        new MimeType("audio", "wav"),
                        "extension",
                        10,
                        new MediaLength(MediaLength.LengthUnit.MILLISECONDS, 1000)
                )
        );
        IMediaObject message = mediaObjectFactory.create(
                "messagecontent",
                new MediaProperties(
                        new MimeType("audio", "wav"),
                        "extension",
                        10,
                        new MediaLength(MediaLength.LengthUnit.MILLISECONDS, 3000)
                )
        );
        String additionalProperty = "additionalpropertyvalue";
        javamailStorableMessage.setSubject(subject);
        javamailStorableMessage.setType(type);
        javamailStorableMessage.setSender(sender);
        javamailStorableMessage.setDeliveryDate(new Date()); // "Old" date should be as if no date has been supplied
        javamailStorableMessage.setRecipients(recipient);
        javamailStorableMessage.setSecondaryRecipients(secondaryRecipient);
        javamailStorableMessage.setSpokenNameOfSender(spokenName, new MessageContentProperties("spokennamefilename", "spokennamedescription", "spokennamelanguage"));
        javamailStorableMessage.addContent(message, new MessageContentProperties("contentfilename", "contentdescription", "messagelanguage"));
        javamailStorableMessage.setAdditionalProperty(ADDITIONAL_PROPERTY, additionalProperty);
        Constraint messageConstraint = aMessageWith(
                subject, type, sender, new String[] {recipient}, new String[] {secondaryRecipient}, false, false,
                sender, null, "en", 2, additionalProperty);
        testStore(javamailStorableMessage, messageConstraint, host);
        // Test adders and changes
        javamailStorableMessage.addRecipient(recipient);
        javamailStorableMessage.addSecondaryRecipient(secondaryRecipient);
        javamailStorableMessage.setUrgent(true);
        javamailStorableMessage.setConfidential(true);
        String replyToAddress = "replytoaddress <replytoaddress@host.com>";
        javamailStorableMessage.setReplyToAddress(replyToAddress);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1); // One day in the future
        Date deliveryDate = calendar.getTime();
        javamailStorableMessage.setDeliveryDate(deliveryDate);
        // Future delivery changes subdomain
        recipient = "recipient <recipient@deferred.host.com>";
        secondaryRecipient = "secondaryrecipient <secondaryrecipient@deferred.host.com>";
        String language = "en,sv";
        javamailStorableMessage.setLanguage(language);

        messageConstraint = aMessageWith(
                subject, type, sender, new String[] {recipient, recipient},
                new String[] {secondaryRecipient, secondaryRecipient}, true, true, replyToAddress, deliveryDate,
                language, 2, additionalProperty);
        testStore(javamailStorableMessage, messageConstraint, host);
    }

    public static Test suite() {
        return new TestSuite(JavamailStorableMessageTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
