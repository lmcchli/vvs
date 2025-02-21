/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.mailbox.mock.MimeMessageMock;
import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;

import jakarta.mail.Message;
import jakarta.activation.MimeType;

import org.jmock.core.Constraint;

/**
 * JavamailForwardMessage Tester.
 *
 * @author MANDE
 * @since <pre>12/13/2006</pre>
 * @version 1.0
 */
public class JavamailForwardMessageTest extends JavamailBaseTestCase {
    JavamailForwardMessage javamailForwardMessage;

    public JavamailForwardMessageTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        Message message = new MimeMessageMock(folderMock, getVoiceMessageInputStream(), 1);
        javamailForwardMessage = new JavamailForwardMessage(javamailContext, message);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testStore() throws Exception {
        // Todo: test that content comes before forwarded message
        String host = "host";
        String subject = "subject";
        MailboxMessageType type = MailboxMessageType.VOICE;
        String sender = "sender <sender@host.com>";
        String recipient = "recipient <recipient@host.com>";
        MediaObjectFactory mediaObjectFactory = new MediaObjectFactory();
        IMediaObject message = mediaObjectFactory.create(
                "messagecontent",
                new MediaProperties(
                        new MimeType("audio", "wav"),
                        "extension",
                        10,
                        new MediaLength(MediaLength.LengthUnit.MILLISECONDS, 3000)
                )
        );
        IMediaObject spokenName = mediaObjectFactory.create(
                "spokennamecontent",
                new MediaProperties(
                        new MimeType("audio", "wav"),
                        "extension",
                        10,
                        new MediaLength(MediaLength.LengthUnit.MILLISECONDS, 1000)
                )
        );
        javamailForwardMessage.addContent(
                message,
                new MessageContentProperties(
                        "contentfilename",
                        "contentdescription",
                        "messagelanguage"
                )
        );
        javamailForwardMessage.setSpokenNameOfSender(
                spokenName,
                new MessageContentProperties(
                        "spokennamefilename",
                        "spokennamedescription",
                        "spokennamelanguage"
                )
        );
        javamailForwardMessage.setSubject(subject);
        javamailForwardMessage.setType(MailboxMessageType.VOICE);
        javamailForwardMessage.setSender(sender);
        javamailForwardMessage.setRecipients(recipient);
        Constraint messageConstraint = aMessageWith(
                subject, type, sender, new String[] {recipient}, new String[0], false, false,
                sender, null, "en", 3, null);
        testStore(javamailForwardMessage, messageConstraint, host);
    }

    public static Test suite() {
        return new TestSuite(JavamailForwardMessageTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
