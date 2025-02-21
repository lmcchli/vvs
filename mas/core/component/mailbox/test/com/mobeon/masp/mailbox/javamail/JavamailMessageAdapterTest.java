/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import junit.framework.*;
import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mailbox.mock.*;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.common.message_sender.SmtpOptions;
import com.mobeon.common.message_sender.InternetMailSenderException;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.eclipse.angus.mail.imap.IMAPFolder;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MailDateFormat;
import jakarta.mail.*;
import jakarta.mail.search.FlagTerm;
import java.util.*;
import java.io.*;

import org.jmock.Mock;
import org.jmock.core.Constraint;

/**
 * JavamailMessageAdapter tester
 *
 * @author mande
 */
public class JavamailMessageAdapterTest extends JavamailBaseTestCase {
    {
        ILoggerFactory.configureAndWatch("../log4jconf.xml");
    }

    private ImapFolderMock imapFolderMock;
    private JavamailFolderAdapter javamailFolderAdapter;

    public JavamailMessageAdapterTest(String string) {
        super(string);
    }

    protected void setUp() throws Exception {
        super.setUp();
        setUpMockImapStore();
        setUpMockImapFolder();
        javamailFolderAdapter = new JavamailFolderAdapter(folderMock, javamailContext, javamailStoreAdapter);
    }

    private void setUpMockImapStore() throws Exception {
        Mock mockImapStore = mock(ImapStoreInterface.class);
        ImapStoreMock.setMockImapStore(mockImapStore);
    }

    protected void setUpMockImapFolder() throws Exception {
        Mock mockImapFolder = mock(ImapFolderInterface.class);
        mockImapFolder.stubs().method("getFullName").will(returnValue("mockImapFolder"));
        imapFolderMock = new ImapFolderMock("mockImapFolder", '/', getImapStore());
        imapFolderMock.setMockImapFolder(mockImapFolder);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMessageWithWrongFolder() throws Exception {
        FolderMock wrongFolder = getFolderMock(getMockFolder("wrongFolder"));
        Message message = new MimeMessageMock(wrongFolder, 1);
        try {
            new JavamailMessageAdapter(message, javamailContext, javamailFolderAdapter);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    /**
     * Test parsing a delivery report
     * @throws Exception
     */
    public void testParseDeliveryReport() throws Exception {
        MimeMessageMock mimeMessage = new MimeMessageMock(folderMock, getUndeliveredFaxMessageInputStream(), 1);
        JavamailMessageAdapter message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        assertTrue(message.isDeliveryReport());
        assertEquals(message.getDeliveryReport(), DeliveryStatus.PRINT_FAILED);
    }
    
    public void testParseDeliveryReportTR31639() throws Exception {
        MimeMessageMock mimeMessage = new MimeMessageMock(folderMock, getUndeliveredFaxMessageInputStreamTR31639(), 1);
        JavamailMessageAdapter message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        assertTrue(message.isDeliveryReport());
        assertEquals(message.getDeliveryReport(), DeliveryStatus.PRINT_FAILED);
    }
    
    public void testParseDeliveryReportTR31888() throws Exception {
        MimeMessageMock mimeMessage = new MimeMessageMock(folderMock, getUndeliveredFaxMessageInputStreamTR31888(), 1);
        JavamailMessageAdapter message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        assertTrue(message.isDeliveryReport());
        assertEquals(message.getDeliveryReport(), DeliveryStatus.PRINT_FAILED);
    }
    
    public void testParseDeliveryReportTR31888_B() throws Exception {
        MimeMessageMock mimeMessage = new MimeMessageMock(folderMock, getUndeliveredFaxMessageInputStreamTR31888_B(), 1);
        JavamailMessageAdapter message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        assertTrue(message.isDeliveryReport());
        assertEquals(message.getDeliveryReport(), DeliveryStatus.PRINT_FAILED);
    }
    
    public void testParseDeliveryReportTR31888_C() throws Exception {
        MimeMessageMock mimeMessage = new MimeMessageMock(folderMock, getUndeliveredFaxMessageInputStreamTR31888_C(), 1);
        JavamailMessageAdapter message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        assertTrue(message.isDeliveryReport());
        assertEquals(message.getDeliveryReport(), DeliveryStatus.PRINT_FAILED); 
    }

    /**
     * Test print message when invalid arguments are supplied
     * @throws Exception
     */
    public void testPrintInvalidArguments() throws Exception {
        Message mimeMessage = new MimeMessageMock(folderMock, getVoiceMessageInputStream(), 1);
        JavamailMessageAdapter message =
                new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        try {
            message.print(null, "sender");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // For statistical purposes
        }
        try {
            message.print("", "sender");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // For statistical purposes
        }
        try {
            message.print("destination", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // For statistical purposes
        }
        try {
            message.print("destination", "");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // For statistical purposes
        }
        try {
            message.print("destination", "sender");
            fail("Expected InvalidMessageException");
        } catch (InvalidMessageException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testPrint() throws Exception {
        Message mimeMessage = new MimeMessageMock(folderMock, getVoiceMessageInputStream(), 1);
        JavamailMessageAdapter message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        String sender = "<sender@host.com>";
        String destination = "destination";
        mockInternetMailSender.expects(once()).method("sendInternetMail").with(
                aMessageWith(
                        "Voice Message From John Doe", MailboxMessageType.VOICE, sender,
                        new String[] { "FAX=" + destination + "@mfc" }, new String [0], false, false, "emailaddress",
                        null, "en", 1, null
                ),
                theSmtpOptions(sender)
        ).will(throwException(new InternetMailSenderException("internetmailsenderexception")));
        try {
            message.print(destination, sender);
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true);
        }
        mockInternetMailSender.expects(once()).method("sendInternetMail").with(
                aMessageWith(
                        "Voice Message From John Doe", MailboxMessageType.VOICE, sender,
                        new String[] { "FAX=" + destination + "@mfc" }, new String [0], false, false, "emailaddress",
                        null, "en", 1, null
                ),
                theSmtpOptions(sender)
        );
        message.print(destination, sender);
    }

    public void testForward() throws Exception {
        Message mimeMessage = new MimeMessageMock(folderMock, getVoiceMessageInputStream(), 1);
        JavamailMessageAdapter message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        JavamailForwardMessage forwardMessage = message.forward();
        String sender = "Sender <sender@host.com>";
        String recipient = "recipient";
        forwardMessage.setSender(sender);
        forwardMessage.setSubject("Fwd: " + message.getSubject());
        forwardMessage.setType(MailboxMessageType.VOICE);
        forwardMessage.setRecipients(recipient);
        mockInternetMailSender.expects(once()).method("sendInternetMail").with(
                aMessageWith(
                        "Fwd: Voice Message From John Doe", MailboxMessageType.VOICE, "Sender <sender@host.com>",
                        new String[] { recipient }, new String [0], false, false, sender,
                        null, "en", 1, null
                ),
                NULL
        );
        forwardMessage.store();
    }

    public void testCopy() throws Exception {
        Message mimeMessage = new MimeMessageMock(folderMock, getVoiceMessageInputStream(), 1);
        JavamailMessageAdapter message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        Mock mockIFolder = mock(IFolder.class, "NoJavamailFolderAdapter");
        try {
            message.copy((IFolder)mockIFolder.proxy());
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
            assertTrue(true); // For statistical purposes
        }
        FolderMock copyFolderMock = getFolderMock(getMockFolder("copyFolder"));
        JavamailFolderAdapter copyFolder = new JavamailFolderAdapter(copyFolderMock, javamailContext, javamailStoreAdapter);
        folderMock.expects(once()).method("isOpen").will(returnValue(false));
        folderMock.expects(once()).method("open").will(throwException(new MessagingException("messagingexception")));
        try {
            message.copy(copyFolder);
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        folderMock.expects(once()).method("isOpen").will(returnValue(true));
        copyFolderMock.expects(once()).method("exists").will(returnValue(true));
        copyFolderMock.expects(once()).method("appendMessages").with(anArray(mimeMessage));
        message.copy(copyFolder);
    }

    public void testSaveChanges() throws Exception {
        // Todo: how to test this?
        // Use ImapFolder
        JavamailFolderAdapter javamailFolderAdapter = new JavamailFolderAdapter(imapFolderMock, javamailContext, javamailStoreAdapter);
        Message mimeMessage = new MimeMessageMock(imapFolderMock, getVoiceMessageInputStream(), 1);
        JavamailMessageAdapter message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        message.setState(StoredMessageState.READ);
        message.saveChanges();
    }

    public void testReceivedDate() throws Exception {
        JavamailFolderAdapter javamailFolderAdapter = new JavamailFolderAdapter(imapFolderMock, javamailContext, javamailStoreAdapter);

        // Test empty message
        MimeMessage mimeMessage = new MimeMessageMock(imapFolderMock, new ByteArrayInputStream(new byte[] {}), 1);
        JavamailMessageAdapter message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        assertNull(message.getReceivedDate());

        // Test empty date
        String dateString = "";
        mimeMessage = new MimeMessageMock(imapFolderMock, getReceivedDateMessageInputStream(dateString), 1);
        message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        assertNull(message.getReceivedDate());

        // Test invalid date
        dateString = "invalid date";
        mimeMessage = new MimeMessageMock(imapFolderMock, getReceivedDateMessageInputStream(dateString), 1);
        message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        assertNull(message.getReceivedDate());

        // Test valid date
        dateString = "Wed, 31 May 2006 09:40:00 +0100";
        mimeMessage = new MimeMessageMock(imapFolderMock, getReceivedDateMessageInputStream(dateString), 1);
        message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        MailDateFormat mailDate = new MailDateFormat();
        assertEquals(mailDate.parse(dateString), message.getReceivedDate());

    }

    public void testRenewIssuedDate() throws Exception {
        // Test when folder not IMAPFolder
        MimeMessage mimeMessage = new MimeMessageMock(folderMock, getRenewIssuedDateMessageInputStream(), 1);
        JavamailMessageAdapter message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        folderMock.expects(once()).method("isOpen").will(returnValue(true));

        try {
            message.renewIssuedDate();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }

        // Test message without Message-ID
        javamailFolderAdapter = new JavamailFolderAdapter(imapFolderMock, javamailContext, javamailStoreAdapter);
        mimeMessage = new MimeMessageMock(imapFolderMock, getReceivedDateMessageInputStream(""), 1);
        message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        imapFolderMock.expects(once()).method("isOpen").will(returnValue(true));
        try {
            message.renewIssuedDate();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }

        // Test returning no message
        javamailFolderAdapter = new JavamailFolderAdapter(imapFolderMock, javamailContext, javamailStoreAdapter);
        mimeMessage = new MimeMessageMock(imapFolderMock, getRenewIssuedDateMessageInputStream(), 1);
        message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        imapFolderMock.expects(once()).method("isOpen").will(returnValue(true));
        imapFolderMock.expects(once()).method("doCommand").with(isA(IMAPFolder.ProtocolCommand.class));
        imapFolderMock.expects(once()).method("search").will(returnValue(new Message[]{}));
        try {
            message.renewIssuedDate();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }

        // Test returning one message
        javamailFolderAdapter = new JavamailFolderAdapter(imapFolderMock, javamailContext, javamailStoreAdapter);
        mimeMessage = new MimeMessageMock(imapFolderMock, getRenewIssuedDateMessageInputStream(), 1);
        MimeMessage renewedMessage = new MimeMessageMock(imapFolderMock, getRenewIssuedDateMessageInputStream(), 1);
        message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        imapFolderMock.expects(once()).method("isOpen").will(returnValue(true));
        imapFolderMock.expects(once()).method("doCommand").with(isA(IMAPFolder.ProtocolCommand.class));
        imapFolderMock.expects(once()).method("search").will(returnValue(new Message[]{renewedMessage}));
        message.renewIssuedDate();
        assertTrue("Old message should have Deleted flag set", mimeMessage.match(new FlagTerm(new Flags(Flags.Flag.DELETED), true)));

        // Test returning more than one message
        javamailFolderAdapter = new JavamailFolderAdapter(imapFolderMock, javamailContext, javamailStoreAdapter);
        mimeMessage = new MimeMessageMock(imapFolderMock, getRenewIssuedDateMessageInputStream(), 1);
        renewedMessage = new MimeMessageMock(imapFolderMock, getRenewIssuedDateMessageInputStream(), 1);
        message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        imapFolderMock.expects(once()).method("isOpen").will(returnValue(true));
        imapFolderMock.expects(once()).method("doCommand").with(isA(IMAPFolder.ProtocolCommand.class));
        imapFolderMock.expects(once()).method("search").will(returnValue(new Message[]{renewedMessage, renewedMessage}));
        message.renewIssuedDate();
        assertTrue("Old message should have Deleted flag set", mimeMessage.match(new FlagTerm(new Flags(Flags.Flag.DELETED), true)));
    }

    public void testContentSize() throws Exception {
        // Test Content-Duration field
        MimeMessage mimeMessage = new MimeMessageMock(folderMock, getMultiPartMessageInputStream("Content-Duration: 5"), 1);
        JavamailMessageAdapter message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        List<IMessageContent> mclist =  message.getContent();
        Iterator<IMessageContent> it = mclist.iterator();
        IMessageContent mc = it.next();
        MediaProperties mp = mc.getMediaProperties();
        assertTrue("Content length ms is missing",mp.hasLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));

        // Test Content-Duration empty (non-existing)
        mimeMessage = new MimeMessageMock(folderMock, getMultiPartMessageInputStream(""), 1);
        message = new JavamailMessageAdapter(mimeMessage, javamailContext, javamailFolderAdapter);
        message.parseMessage();
        mclist =  message.getContent();
        it = mclist.iterator();
        mc = it.next();
        mp = mc.getMediaProperties();
        assertFalse("Content length ms should not exist",mp.hasLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
    }

    // Create an InputStream that contains a multipart voicemessage
    private InputStream getMultiPartMessageInputStream(String contentDuration) {
        StringWriter sw =  new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Return-path: <torsten2@lab.mobeon.com>");
        pw.println("Received: from anita (anita.ipms.lab.mobeon.com [10.11.0.38])");
        pw.println(" by ockelbo.lab.mobeon.com");
        pw.println(" (iPlanet Messaging Server 5.2 Patch 2 (built Jul 14 2004))");
        pw.println(" with SMTP id <0J7L0046MCKIL7@ockelbo.lab.mobeon.com> for");
        pw.println(" torsten1@lab.mobeon.com; Mon, 23 Oct 2006 15:38:43 +0200 (MEST)");
        pw.println("Date: Mon, 23 Oct 2006 15:38:43 +0200 (MEST)");
        pw.println("Date-warning: Date header was inserted by ockelbo.lab.mobeon.com");
        pw.println("From: \"torsten2 eriksson (1265)\" <torsten2@lab.mobeon.com>");
        pw.println("Subject: Voice message from torsten2 eriksson");
        pw.println("To: torsten1@lab.mobeon.com");
        pw.println("Message-id: <7720611.11161610324479.JavaMail.root@anita>");
        pw.println("MIME-version: 1.0");
        pw.println("Content-type: multipart/voice-message;");
        pw.println(" boundary=\"----=_Part_1_23107698.1161610324461\"");
        pw.println("Content-language: en");
        pw.println("Original-recipient: rfc822;torsten1@lab.mobeon.com");
        pw.println();
        pw.println("------=_Part_1_23107698.1161610324461");
        pw.println("Content-Type: audio/wav; name=message.wav");
        pw.println("Content-Transfer-Encoding: base64");
        pw.println(contentDuration);
        pw.println("Content-Description: Mobeon voice message (4 second(s))");
        pw.println("Content-Language: en");
        pw.println("Content-Disposition: inline; filename=message.wav; voice=Voice-Message");
        pw.println();
        pw.println("UklGRuaZAABXQVZFZm10IBIAAAAHAAEAQB8AAEAfAAABAAgAAABkYXRhwJkAAL7ZYs/X1ejbxt5m");
        pw.println("zc5c0c78y9Pv1c9r2th5+NDld9143dzh6d7U2erwz29c1mlp0+xy3utbTthRVN9OSdToTdTdV1L0");
        pw.println("XknpfkhW2lJK1U9C329MX1xWVe1sS+VYSd1TSvVYUPnm4W304klx01VX1l1H12xDV9lMU8x1UM56");
        pw.println("Yt5ZaFto/Vjn9+5r3+5rzs5TzctRzMX0zdDr7N/ZaH/c32/d2PDM3O3S3Vre93hOzrzMubk9LN0/");
        pw.println("MMOwrbxNeiIgx7/Yn6DL1lkxKiosaa2tqKKvUDosJTVTxri0ssw3KiwyM/W5srC1vk80OTg2SdC9");
        pw.println("vrpoS0AxNjw9TMu4vL7JQTY7OjZH2c3Cuc1ZQz5ESVbq7GdcZkA9TUVScFnWZfd4W8j+1NFf4l1p");
        pw.println("08TAwtM/U1ZGYb7uWr3UXdP0S2jZ1L2/sri/uslS6udNyL/XvbrPzVtGQknj2r66v8nQ3FFMcF9g");
        pw.println("u8jCwG1ZU0VNafbGydbJWVXxRUtbaf3Fy2XS01TqxtzSusPcz08+QkY/X+lM13nu8N1MSX5OXPnc");
        pw.println("------=_Part_1_23107698.1161610324461--");
        pw.println();
        return new ByteArrayInputStream(sw.toString().getBytes());
    }
    private InputStream getReceivedDateMessageInputStream(String dateString) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Received: ; " + dateString);
        pw.println();
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    private InputStream getRenewIssuedDateMessageInputStream() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Message-ID: <messageid@domain>");
        pw.println();
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    private InputStream getUndeliveredFaxMessageInputStream() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("From: Internet Mail Delivery <postmaster@lab.mobeon.com>");
        pw.println("Subject: Delivery Notifigation: Delivery has failed");
        pw.println("To: mande1@lab.mobeon.com");
        pw.println("Content-type: multipart/report; report-type=delivery-status; boundary=deliveryreportboundary");
        pw.println();
        pw.println("--deliveryreportboundary");
        pw.println("Content-type: text/plain; charset=us-ascii");
        pw.println("Content-language: en-us");
        pw.println();
        pw.println("humanreadablemessage");
        pw.println();
        pw.println("--deliveryreportboundary");
        pw.println("Content-type: message/delivery-status");
        pw.println();
        pw.println("Final-recipient: rfc822;FAX=302102916@fax.ipt1.lab.mobeon.com");
        pw.println("--deliveryreportboundary");
        pw.println("Content-type: message/rfc822");
        pw.println();
        appendFaxMessage(pw);
        pw.println("--deliveryreportboundary--");
        appendFaxMessage(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }
    
    private InputStream getUndeliveredFaxMessageInputStreamTR31639() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Return-path: <>"); //eol
        pw.println("Received: from process-daemon.rover.lab.mobeon.com by rover.lab.mobeon.com"); //eol
        pw.println(" (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006))"); //eol
        pw.println(" id <0JVX00H012ZLPJ00@rover.lab.mobeon.com> for 1912@ca.lab.mobeon.com; Fri,"); //eol
        pw.println(" 08 Feb 2008 12:09:21 +0100 (MET)"); //eol
        pw.println("Received: from rover.lab.mobeon.com"); //eol
        pw.println(" (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006))"); //eol
        pw.println(" id <0JVX00H022ZKPI00@rover.lab.mobeon.com>; Fri,"); //eol
        pw.println(" 08 Feb 2008 12:09:20 +0100 (MET)"); //eol
        pw.println("Date: Fri, 08 Feb 2008 12:09:20 +0100 (MET)"); //eol
        pw.println("From: postmaster@ca.lab.mobeon.com"); //eol
        pw.println("Subject: Delivery Notification: Delivery has failed"); //eol
        pw.println("To: 1912@ca.lab.mobeon.com"); //eol
        pw.println("Message-id: <0JVX00H042ZKPI00@rover.lab.mobeon.com>"); //eol
        pw.println("MIME-version: 1.0"); //eol
        pw.println("Content-type: multipart/report;"); //eol----------------------------------------------------------------------------->>(2)
        pw.println(" boundary=\"Boundary_(ID_Afd8fhR5FgEacy6p8hftTQ)\"; report-type=delivery-status"); //eol------------------------------->>(3)
        pw.println("Original-recipient: rfc822;1912@ca.lab.mobeon.com"); //eol
        pw.println(); //eol
        pw.println(); //eol
        pw.println("--Boundary_(ID_Afd8fhR5FgEacy6p8hftTQ)"); //eol
        pw.println("Content-type: text/plain; charset=us-ascii"); //eol-------------------------------------------------------
        pw.println("Content-language: en-US"); //eol
        pw.println(); //eol
        //======================================
        pw.println("This report relates to a message you sent with the following header fields:"); //eol
        pw.println(""); //eol
        pw.println("  Message-id: <27546477.01202469740108.JavaMail.root@halland-5>"); //eol
        pw.println("  Date: Fri, 08 Feb 2008 12:09:19 +0100 (MET)"); //eol
        pw.println("  From: \"Johan Fahlgren (1912)\" <1912@ca.lab.mobeon.com>"); //eol
        pw.println("  To: FAX=4528@fax.ca.lab.mobeon.com"); //eol
        pw.println("  Subject: This is a fax"); //eol
        pw.println(); //eol
        pw.println("Your message cannot be delivered to the following recipients:"); //eol
        pw.println(""); //eol
        pw.println("  Recipient address: FAX=4528@fax.ca.lab.mobeon.com"); //eol
        pw.println("  Original address: FAX=4528@mfc"); //eol
        pw.println("  Reason: Illegal host/domain name found"); //eol
        pw.println(); //eol
        pw.println(); //eol
        pw.println("--Boundary_(ID_Afd8fhR5FgEacy6p8hftTQ)"); //eol
        pw.println("Content-type: message/delivery-status"); //eol------------------------------------------------------------ (E 4)
        pw.println(); //eol
        pw.println("Reporting-MTA: dns;rover.lab.mobeon.com (tcp_fax-daemon)"); //eol
        pw.println(); //eol
        pw.println("Original-recipient: rfc822;FAX=4528@mfc"); //eol
        pw.println("Final-recipient: rfc822;FAX=4528@fax.ca.lab.mobeon.com"); //eol ------------------------------------------ (E 5)
        pw.println("Action: failed"); //eol
        pw.println("Status: 5.4.4 (Illegal host/domain name found)"); //eol
        pw.println(); //eol
        pw.println("--Boundary_(ID_Afd8fhR5FgEacy6p8hftTQ)"); //eol
        pw.println("Content-type: message/rfc822"); //eol---------------------------------------------------------------------
        pw.println(); //eol
        pw.println("Return-path: <1912@ca.lab.mobeon.com>"); //eol
        pw.println("Received: from tcp_fax-daemon.rover.lab.mobeon.com by rover.lab.mobeon.com"); //eol
        pw.println(" (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006))"); //eol
        pw.println(" id <0JVX00H022ZKPI00@rover.lab.mobeon.com>; Fri,"); //eol
        pw.println(" 08 Feb 2008 12:09:20 +0100 (MET)"); //eol
        pw.println("Received: from halland-5 (halland-5.lab.su.sw.abcxyz.se [150.132.7.15])"); //eol
        pw.println(" by rover.lab.mobeon.com"); //eol
        pw.println(" (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006))"); //eol
        pw.println(" with SMTP id <0JVX00BZA2ZJ5V00@rover.lab.mobeon.com> for FAX=4528@mfc; Fri,"); //eol
        pw.println(" 08 Feb 2008 12:09:19 +0100 (MET)"); //eol
        pw.println("Date: Fri, 08 Feb 2008 12:09:19 +0100 (MET)"); //eol
        pw.println("Date-warning: Date header was inserted by rover.lab.mobeon.com"); //eol
        pw.println("From: \"Johan Fahlgren (1912)\" <1912@ca.lab.mobeon.com>"); //eol
        pw.println("Subject: This is a fax"); //eol
        pw.println("To: FAX=4528@fax.ca.lab.mobeon.com"); //eol
        pw.println("Reply-to: 1912@ca.lab.mobeon.com"); //eol
        pw.println("Message-id: <27546477.01202469740108.JavaMail.root@halland-5>"); //eol
        pw.println("MIME-version: 1.0"); //eol
        pw.println("Content-type: multipart/fax-message;"); //eol----------------------------------------------------------------------(1)
        pw.println(" boundary=\"Boundary_(ID_26ynLR8/x3TXSc7hU9zuWA)\""); //eol
        pw.println("Content-language: en"); //eol
        pw.println(); //eol
        pw.println(); //eol
        pw.println("--Boundary_(ID_26ynLR8/x3TXSc7hU9zuWA)"); //eol
        pw.println("Content-type: message/rfc822"); //eol----------------------------------------------------------------------
        pw.println(); //eol
        pw.println("Return-path: <4746451111@lab.mobeon.com>"); //eol
        pw.println("Return-path: <FAX=1780100010@faxe.telephony.su.erm.abcxyz.se>"); //eol
        pw.println("Received: from brage (brage.mobeon.com [150.132.5.213])"); //eol
        pw.println(" by rover.lab.mobeon.com (Sun Java System Messaging Server 6.2-8.02 (built Dec"); //eol
        pw.println(" 21 2006)) with ESMTP id <0JVX00BZ62UY5V00@rover.lab.mobeon.com> for"); //eol
        pw.println(" 1912@ca.lab.mobeon.com; Fri, 08 Feb 2008 12:06:34 +0100 (MET)"); //eol
        pw.println("Received: from faxe. (faxe.telephony.su.erm.abcxyz.se [10.101.0.111])"); //eol
        pw.println(" by snowtrix.mvas2.su.erm.abcxyz.se"); //eol
        pw.println(" (iPlanet Messaging Server 5.2 (built Feb 21 2002))"); //eol
        pw.println(" with ESMTP id <0H7700LVPQSH7X@snowtrix.mvas2.su.erm.abcxyz.se> for"); //eol
        pw.println(" tomas@ims-ms-daemon (ORCPT FAX=1790161022@snowtrix.mvas2.su.erm.abcxyz.se)"); //eol
        pw.println(" ; Mon, 16 Dec 2002 14:06:16 +0100 (CET)"); //eol
        pw.println("Received: by faxe. for <FAX=1790161022@snowtrix.mvas2.su.erm.abcxyz.se>"); //eol
        pw.println(" (with Cisco NetWorks); Mon, 16 Dec 2002 13:50:15 +0100"); //eol
        pw.println("Date: Mon, 16 Dec 2002 13:50:15 +0100"); //eol
        pw.println("From: 4746451111@lab.mobeon.com"); //eol
        pw.println("Subject: This is a fax"); //eol
        pw.println("To: 1912@ca.lab.mobeon.com"); //eol
        pw.println("Message-id: <2EFE2002135015556@faxe>"); //eol
        pw.println("MIME-version: 1.0"); //eol
        pw.println("X-Mailer: IOS (tm) 5350 Software (C5350-IS-M)"); //eol
        pw.println("Content-type: multipart/fax-message;"); //eol----------------------------------------------------------------------(1)
        pw.println(" boundary=\"Boundary_(ID_JXTft1LE366Fd92ZXWKa8w)\""); //eol
        pw.println("X-Priority: 0"); //eol
        pw.println("X-Account-Id:"); //eol
        pw.println("X-Mozilla-Status: 8001"); //eol
        pw.println("X-Mozilla-Status2: 00000000"); //eol
        pw.println("Original-recipient: rfc822;1912@ca.lab.mobeon.com"); //eol
        pw.println(); //eol
        pw.println(); //eol
        pw.println("--Boundary_(ID_JXTft1LE366Fd92ZXWKa8w)"); //eol
        pw.println("Content-id: <2EFF2002135054556@faxe.>"); //eol
        pw.println("Content-type: image/tiff; name=\"FaxMessage.tif\"; application=faxbw"); //eol---------------------------------
        pw.println("Content-transfer-encoding: base64 "); //eol       
        //====================================================
        pw.println();
        appendFaxMessage(pw);
        pw.println("--deliveryreportboundary--");
        appendFaxMessage(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }  
    
    private InputStream getUndeliveredFaxMessageInputStreamTR31888() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        //====================================================
        pw.println("Return-path: <>"); //eol
        pw.println("Received: from mgw01 (mgw01.testmoip.mymeteor.ie [10.80.2.2])"); //eol
        pw.println(" by ms01.testmoip.mymeteor.ie"); //eol
        pw.println(" (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006))"); //eol
        pw.println(" with ESMTP id <0JYL00H0ZNWV1X00@ms01.testmoip.mymeteor.ie> for"); //eol
        pw.println(" 353857093334@mail.testmoip.mymeteor.ie; Mon, 31 Mar 2008 15:50:56 +0100 (IST)"); //eol
        pw.println("Received: by mgw01 for <353857093334@mail.testmoip.mymeteor.ie>"); //eol
        pw.println(" (with Cisco NetWorks); Mon, 31 Mar 2008 14:50:59 +0000"); //eol
        pw.println("Date: Mon, 31 Mar 2008 14:50:59 +0000"); //eol
        pw.println("From: Fax Daemon <postmaster@mymeteor.ie>"); //eol
        pw.println("Subject: Delivery Status Notification"); //eol
        pw.println("To: 353857093334@mail.testmoip.mymeteor.ie"); //eol
        pw.println("Message-id: <00912008145059282@mgw01>"); //eol
        pw.println("MIME-version: 1.0"); //eol
        pw.println("X-Mailer: Technical Support: http://www.cisco"); //eol
        pw.println("Content-type: multipart/report; report-type=delivery-status;"); //eol
        pw.println(" boundary=\"yradnuoB=_00902008145058746.mgw01\""); //eol
        pw.println("Original-recipient: rfc822;353857093334@mail.testmoip.mymeteor.ie"); //eol
        pw.println(); //eol
        pw.println("--yradnuoB=_00902008145058746.mgw01"); //eol
        pw.println("Content-ID: <00922008145059282@mgw01>"); //eol
        pw.println(); //eol
        pw.println(); //eol
        
        // OK Upto here

        pw.println("This is a Delivery Status Notification for your message"); //eol
        pw.println("  dated: Mon, 31 Mar 2008 15:50:40 +0100 (IST)"); //eol
        pw.println("  subject: Fax Message"); //eol
        pw.println("  received at: Mon, 31 Mar 2008 14:50:43 +0000"); //eol
        pw.println(); //eol
        pw.println(); //eol
        pw.println("Delivery was unsuccessful to the following recipient(s):"); //eol
        pw.println("  <FAX=014@fax.mail.testmoip.mymeteor.ie> "); //eol
        pw.println("Failed with the status of: 554 5.3.0 An unknown error occurred"); //eol
        pw.println("The cause code is: call rejected (21)"); //eol
        pw.println("The fax error text is: none"); //eol
        pw.println(); //eol
        pw.println("--yradnuoB=_00902008145058746.mgw01"); //eol
        pw.println("Content-ID: <00932008145059282@mgw01>"); //eol
        pw.println("Content-Type: message/delivery-status"); //eol
        pw.println(); //eol
        
        // OK Upto here                
        
        pw.println("Reporting-MTA: x-local-hostname; mgw01.testmoip.mymeteor.ie"); //eol
        pw.println("Received-From-MTA: "); //eol
        pw.println("Arrival-Date: Mon, 31 Mar 2008 14:50:43 +0000"); //eol
        pw.println(); //eol
        pw.println("Original-Recipient: rfc822;FAX=014@mfc"); //eol
        pw.println("Final-Recipient: rfc822; FAX=014@fax.mail.testmoip.mymeteor.ie"); //eol
        pw.println("Action: failed with cause code - call rejected (21); fax error text - none"); //eol
        pw.println("Status: 554 5.3.0 An unknown error occurred"); //eol
        pw.println("X-Total-Pages: 0"); //eol
        pw.println("X-Accept-Features: (| (TIFF-S) (TIFF-F) )"); //eol
        pw.println(); //eol        
        
        // OK Upto here                
        
        pw.println("--yradnuoB=_00902008145058746.mgw01"); //eol

        pw.println("Content-ID: <00942008145059282@mgw01>"); //eol
        
        
     // OK Upto here
        pw.println("Content-Type: message/rfc822-headers"); //eol    //Is this the problem ??? 
                                                                       //Usually it is just "Content-Type: message/rfc822"
        //pw.println("Content-Type: message/rfc822-");  //This will cause an exception
        //pw.println("Content-Type: message/rfc822");  //This is OK                                                             
                                                                       
     //crash above        
        
        //   OK ?????????????     
        pw.println(); //eol
 
        pw.println("Received: from mas01 (mas01.testmoip.mymeteor.ie [10.80.2.5]) by ms01.testmoip.mymeteor.ie (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006)) with SMTP id <0JYL00H0XNWG1X00@ms01.testmoip.mymeteor.ie> for FAX=014@mfc; Mon, 31 Mar 2008 15:50:40 +0100 (IST)"); //eol
        pw.println(); //eol
        //   OK ?????????????
        //pw.println("--yradnuoB=_00902008145058746.mgw01--"); //eol
        //====================================================
        pw.println();
        appendFaxMessage(pw);
        pw.println("--yradnuoB=_00902008145058746.mgw01--"); //eol
        appendFaxMessage(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }
    
    private InputStream getUndeliveredFaxMessageInputStreamTR31888_B() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        //==================================================== 
        pw.println("Return-path: <>"); //eol
        pw.println("Received: from process-daemon.ms01.testmoip.mymeteor.ie by"); //eol
        pw.println(" ms01.testmoip.mymeteor.ie"); //eol
        pw.println(" (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006))"); //eol
        pw.println(" id <0JYL00J01NP5WO00@ms01.testmoip.mymeteor.ie> for"); //eol
        pw.println(" 353857093334@mail.testmoip.mymeteor.ie; Mon, 31 Mar 2008 15:50:55 +0100 (IST)"); //eol
        pw.println("Received: from ms01.testmoip.mymeteor.ie"); //eol
        pw.println(" (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006))"); //eol
        pw.println(" id <0JYL00J05NWGVC00@ms01.testmoip.mymeteor.ie>; Mon,"); //eol
        pw.println(" 31 Mar 2008 15:50:55 +0100 (IST)"); //eol
        pw.println("Date: Mon, 31 Mar 2008 15:50:55 +0100 (IST)"); //eol
        pw.println("From: postmaster@mail.testmoip.mymeteor.ie"); //eol
        pw.println("Subject: Delivery Notification: Delivery has failed"); //eol
        pw.println("To: 353857093334@mail.testmoip.mymeteor.ie"); //eol
        pw.println("Message-id: <0JYL00J07NWVVC00@ms01.testmoip.mymeteor.ie>"); //eol
        pw.println("MIME-version: 1.0"); //eol
        pw.println("Content-type: multipart/report;"); //eol
        pw.println(" boundary=\"Boundary_(ID_My09rmygcHAslmathCMfNA)\"; report-type=delivery-status"); //eol
        pw.println("Original-recipient: rfc822;353857093334@mail.testmoip.mymeteor.ie"); //eol
        pw.println(); //eol
        pw.println(); //eol
        pw.println("--Boundary_(ID_My09rmygcHAslmathCMfNA)"); //eol
        pw.println("Content-type: text/plain; charset=us-ascii"); //eol
        pw.println("Content-language: en-US"); //eol
        pw.println(); //eol
        pw.println("This report relates to a message you sent with the following header fields:"); //eol
        pw.println(); //eol
        pw.println("  Message-id: <3439835.31206975035115.JavaMail.root@mas01>"); //eol
        pw.println("  Date: Mon, 31 Mar 2008 15:50:40 +0100 (IST)"); //eol
        pw.println("  From: \"353857093334 353857093334 (353857093334)\""); //eol
        pw.println("   <353857093334@mail.testmoip.mymeteor.ie>"); //eol
        pw.println("  To: FAX=014@fax.mail.testmoip.mymeteor.ie"); //eol
        pw.println("  Subject: Fax Message"); //eol
        pw.println(); //eol
        pw.println("Your message cannot be delivered to the following recipients:"); //eol
        pw.println(); //eol
        pw.println("  Recipient address: FAX=014@fax.mail.testmoip.mymeteor.ie"); //eol
        pw.println("  Original address: FAX=014@mfc"); //eol
        pw.println("  Reason: SMTP transmission failure has occurred"); //eol
        pw.println("  Diagnostic code: smtp;554 5.3.0 An unknown error occurred"); //eol
        pw.println("  Remote system: dns;mgw01.testmoip.mymeteor.ie (mgw01.testmoip.mymeteor.ie Cisco NetWorks ESMTP server)"); //eol
        pw.println(); //eol
        pw.println(); //eol
        pw.println("--Boundary_(ID_My09rmygcHAslmathCMfNA)"); //eol
        pw.println("Content-type: message/delivery-status"); //eol
        pw.println(); //eol
        pw.println("Reporting-MTA: dns;ms01.testmoip.mymeteor.ie (tcp_fax-daemon)"); //eol
        pw.println(); //eol
        pw.println("Original-recipient: rfc822;FAX=014@mfc"); //eol
        pw.println("Final-recipient: rfc822;FAX=014@fax.mail.testmoip.mymeteor.ie"); //eol
        pw.println("Action: failed"); //eol
        pw.println("Status: 5.3.0 (SMTP transmission failure has occurred)"); //eol
        pw.println("Remote-MTA: dns;mgw01.testmoip.mymeteor.ie"); //eol
        pw.println(" (mgw01.testmoip.mymeteor.ie Cisco NetWorks ESMTP server)"); //eol
        pw.println("Diagnostic-code: smtp;554 5.3.0 An unknown error occurred"); //eol
        pw.println(); //eol
        pw.println("--Boundary_(ID_My09rmygcHAslmathCMfNA)"); //eol
        pw.println("Content-type: message/rfc822"); //eol
        pw.println(); //eol
        pw.println("Return-path: <353857093334@mail.testmoip.mymeteor.ie>"); //eol
        pw.println("Received: from tcp_fax-daemon.ms01.testmoip.mymeteor.ie by"); //eol
        pw.println(" ms01.testmoip.mymeteor.ie"); //eol
        pw.println(" (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006))"); //eol
        pw.println(" id <0JYL00J05NWGVC00@ms01.testmoip.mymeteor.ie>; Mon,"); //eol
        pw.println(" 31 Mar 2008 15:50:55 +0100 (IST)"); //eol
        pw.println("Received: from mas01 (mas01.testmoip.mymeteor.ie [10.80.2.5])"); //eol
        pw.println(" by ms01.testmoip.mymeteor.ie"); //eol
        pw.println(" (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006))"); //eol
        pw.println(" with SMTP id <0JYL00H0XNWG1X00@ms01.testmoip.mymeteor.ie> for FAX=014@mfc;"); //eol
        pw.println(" Mon, 31 Mar 2008 15:50:40 +0100 (IST)"); //eol
        pw.println("Date: Mon, 31 Mar 2008 15:50:40 +0100 (IST)"); //eol
        pw.println("Date-warning: Date header was inserted by ms01.testmoip.mymeteor.ie"); //eol
        pw.println("From: \"353857093334 353857093334 (353857093334)\""); //eol
        pw.println(" <353857093334@mail.testmoip.mymeteor.ie>"); //eol
        pw.println("Subject: Fax Message"); //eol
        pw.println("To: FAX=014@fax.mail.testmoip.mymeteor.ie"); //eol
        pw.println("Reply-to: 353857093334@mail.testmoip.mymeteor.ie"); //eol
        pw.println("Message-id: <3439835.31206975035115.JavaMail.root@mas01>"); //eol
        pw.println("MIME-version: 1.0"); //eol
        pw.println("Content-type: multipart/fax-message;"); //eol
        pw.println(" boundary=\"Boundary_(ID_HmYLzHM/zZFKcCqh/JFqmA)\""); //eol
        pw.println("Content-language: en"); //eol
        pw.println(); //eol
        pw.println(); //eol
        pw.println("--Boundary_(ID_HmYLzHM/zZFKcCqh/JFqmA)"); //eol
        pw.println("Content-type: message/rfc822"); //eol
        pw.println(); //eol
        pw.println("Return-path: <FAX=4307000@mgw01.testmoip.mymeteor.ie>"); //eol
        pw.println("Received: from mgw01 (mgw01.testmoip.mymeteor.ie [10.80.2.2])"); //eol
        pw.println(" by ms01.testmoip.mymeteor.ie"); //eol
        pw.println(" (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006))"); //eol
        pw.println(" with ESMTP id <0JYL00H08N571X00@ms01.testmoip.mymeteor.ie> for"); //eol
        pw.println(" FAX=3538557093334@mail.testmoip.mymeteor.ie; Mon,"); //eol
        pw.println(" 31 Mar 2008 15:34:41 +0100 (IST)"); //eol
        pw.println("Received: by mgw01 for <FAX=3538557093334@mail.testmoip.mymeteor.ie>"); //eol
        pw.println(" (with Cisco NetWorks); Mon, 31 Mar 2008 14:34:23 +0000"); //eol
        pw.println("Date: Mon, 31 Mar 2008 14:34:23 +0000"); //eol
        pw.println("From: 353 1 4307010 <FAX=4307000@mgw01.testmoip.mymeteor.ie>"); //eol
        pw.println("Subject: Fax Message"); //eol
        pw.println("To: 3538557093334 <FAX=3538557093334@mail.testmoip.mymeteor.ie>"); //eol
        pw.println("Message-id: <00892008143423360@mgw01>"); //eol
        pw.println("MIME-version: 1.0"); //eol
        pw.println("X-Mailer: Technical Support: http://www.cisco"); //eol
        pw.println("Content-type: multipart/fax-message;"); //eol
        pw.println(" boundary=\"Boundary_(ID_UPTpp1zWE1cmZSTEUfltaQ)\""); //eol
        pw.println("X-Account-Id:"); //eol
        pw.println("Original-recipient: rfc822;FAX=3538557093334@mail.testmoip.mymeteor.ie"); //eol
        pw.println(); //eol
        pw.println(); //eol
        pw.println("--Boundary_(ID_UPTpp1zWE1cmZSTEUfltaQ)"); //eol
        pw.println("Content-id: <008A2008143444360@mgw01>"); //eol
        pw.println("Content-type: image/tiff; name=\"FaxMessage.tif\"; application=faxbw"); //eol
        pw.println("Content-type: image/tiff; name=\"FaxMessage.tif\"; application=faxbw"); //eol
        pw.println("Content-transfer-encoding: base64"); //eol
        pw.println("Content-disposition: attachment"); //eol
        pw.println(); //eol
        pw.println("SUkqAAgAAAAYAP4ABAABAAAAAgAAAAABAwABAAAAwAYAAAEBAwABAAAAFAkAAAIBAwABAAAAAQAA"); //eol
        pw.println("AAMBAwABAAAAAwAAAAYBAwABAAAAAAAAAAoBAwABAAAAAQAAAA4BAgAKAAAALgEAABEBBAABAAAA"); //eol
        //
        pw.println("TZqAAU2agAFNmoABTZqAAU2agAFNmoABTZqAAU2agAFNmoABTZqAAU2agAFNmoA="); //eol
        pw.println(); //eol
        pw.println("--Boundary_(ID_UPTpp1zWE1cmZSTEUfltaQ)--"); //eol
        pw.println(); //eol
        pw.println("--Boundary_(ID_HmYLzHM/zZFKcCqh/JFqmA)--"); //eol
        pw.println(); //eol
        //pw.println("--Boundary_(ID_My09rmygcHAslmathCMfNA)--"); //eol
        //====================================================
        pw.println();
        appendFaxMessage(pw);
        pw.println("--Boundary_(ID_My09rmygcHAslmathCMfNA)--"); //eol
        appendFaxMessage(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    private InputStream getUndeliveredFaxMessageInputStreamTR31888_C() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        //====================================================
        pw.println("Return-path: <>"); //eol
        pw.println("Received: from mgw01 (mgw01.testmoip.mymeteor.ie [10.80.2.2])"); //eol
        pw.println(" by ms01.testmoip.mymeteor.ie"); //eol
        pw.println(" (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006))"); //eol
        pw.println(" with ESMTP id <0JYL00H0ZNWV1X00@ms01.testmoip.mymeteor.ie> for"); //eol
        pw.println(" 353857093334@mail.testmoip.mymeteor.ie; Mon, 31 Mar 2008 15:50:56 +0100 (IST)"); //eol
        pw.println("Received: by mgw01 for <353857093334@mail.testmoip.mymeteor.ie>"); //eol
        pw.println(" (with Cisco NetWorks); Mon, 31 Mar 2008 14:50:59 +0000"); //eol
        pw.println("Date: Mon, 31 Mar 2008 14:50:59 +0000"); //eol
        pw.println("From: Fax Daemon <postmaster@mymeteor.ie>"); //eol
        pw.println("Subject: Delivery Status Notification"); //eol
        pw.println("To: 353857093334@mail.testmoip.mymeteor.ie"); //eol
        pw.println("Message-id: <00912008145059282@mgw01>"); //eol
        pw.println("MIME-version: 1.0"); //eol
        pw.println("X-Mailer: Technical Support: http://www.cisco"); //eol
        pw.println("Content-type: multipart/report; report-type=delivery-status;"); //eol
        pw.println(" boundary=\"yradnuoB=_00902008145058746.mgw01\""); //eol
        pw.println("Original-recipient: rfc822;353857093334@mail.testmoip.mymeteor.ie"); //eol
        pw.println(); //eol
        pw.println("--yradnuoB=_00902008145058746.mgw01"); //eol
        pw.println("Content-ID: <00922008145059282@mgw01>"); //eol
        pw.println(); //eol
        pw.println(); //eol
        
        // OK Upto here

        pw.println("This is a Delivery Status Notification for your message"); //eol
        pw.println("  dated: Mon, 31 Mar 2008 15:50:40 +0100 (IST)"); //eol
        pw.println("  subject: Fax Message"); //eol
        pw.println("  received at: Mon, 31 Mar 2008 14:50:43 +0000"); //eol
        pw.println(); //eol
        pw.println(); //eol
        
        
        pw.println("Delivery was unsuccessful to the following recipient(s):"); //eol
        pw.println("  <FAX=014@fax.mail.testmoip.mymeteor.ie> "); //eol
        pw.println("Failed with the status of: 450 4.4.2 Modem timed out"); //eol
        pw.println("The cause code is: no user answer (19)"); //eol
        pw.println("The fax error text is: no user answer (19): No answer T30 timeout"); //eol
        
        
        //pw.println("Delivery was unsuccessful to the following recipient(s):"); //eol
        //pw.println("  <FAX=014@fax.mail.testmoip.mymeteor.ie> "); //eol
        //pw.println("Failed with the status of: 554 5.3.0 An unknown error occurred"); //eol
        //pw.println("The cause code is: call rejected (21)"); //eol
        //pw.println("The fax error text is: none"); //eol        
        
        
        
        pw.println(); //eol
        pw.println("--yradnuoB=_00902008145058746.mgw01"); //eol
        pw.println("Content-ID: <00932008145059282@mgw01>"); //eol
        pw.println("Content-Type: message/delivery-status"); //eol
        pw.println(); //eol
        
        // OK Upto here                
        
        pw.println("Reporting-MTA: x-local-hostname; mgw01.testmoip.mymeteor.ie"); //eol
        pw.println("Received-From-MTA: "); //eol
        pw.println("Arrival-Date: Mon, 31 Mar 2008 14:50:43 +0000"); //eol
        pw.println(); //eol
        pw.println("Original-Recipient: rfc822;FAX=014@mfc"); //eol
        pw.println("Final-Recipient: rfc822; FAX=014@fax.mail.testmoip.mymeteor.ie"); //eol
        pw.println("Action: failed with cause code - call rejected (21); fax error text - none"); //eol
        pw.println("Status: 554 5.3.0 An unknown error occurred"); //eol
        pw.println("X-Total-Pages: 0"); //eol
        pw.println("X-Accept-Features: (| (TIFF-S) (TIFF-F) )"); //eol
        pw.println(); //eol        
        
        // OK Upto here                
        
        pw.println("--yradnuoB=_00902008145058746.mgw01"); //eol

        pw.println("Content-ID: <00942008145059282@mgw01>"); //eol
        
        
     // OK Upto here
        pw.println("Content-Type: message/rfc822-headers"); //eol    //Is this the problem ??? 
                                                                       //Usually it is just "Content-Type: message/rfc822"
        //pw.println("Content-Type: message/rfc822-");  //This will cause an exception
        //pw.println("Content-Type: message/rfc822");  //This is OK                                                             
                                                                       
     //crash above        
        
        //   OK ?????????????     
        pw.println(); //eol
 
        pw.println("Received: from mas01 (mas01.testmoip.mymeteor.ie [10.80.2.5]) by ms01.testmoip.mymeteor.ie (Sun Java System Messaging Server 6.2-8.02 (built Dec 21 2006)) with SMTP id <0JYL00H0XNWG1X00@ms01.testmoip.mymeteor.ie> for FAX=014@mfc; Mon, 31 Mar 2008 15:50:40 +0100 (IST)"); //eol
        pw.println(); //eol
        //   OK ?????????????
        //pw.println("--yradnuoB=_00902008145058746.mgw01--"); //eol
        //====================================================
        pw.println();
        appendFaxMessage(pw);
        pw.println("--yradnuoB=_00902008145058746.mgw01--"); //eol
        appendFaxMessage(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    
    protected IMAPStore getImapStore() throws NoSuchProviderException {
        return (IMAPStore)SESSION.getStore("mockimapstore");
    }

    private Constraint theSmtpOptions(String envelopeFrom) {
        return new SmtpOptionsConstraint(envelopeFrom);
    }

    private static class SmtpOptionsConstraint implements Constraint {
        private String envelopeFrom;

        public SmtpOptionsConstraint(String envelopeFrom) {
            this.envelopeFrom = envelopeFrom;
        }

        public boolean eval(Object o) {
            if (!(o instanceof SmtpOptions)) {
                return false;
            }
            return ((SmtpOptions)o).getEnvelopeFrom().equals(envelopeFrom);
        }

        public StringBuffer describeTo(StringBuffer buffer) {
            return buffer.append("the SMTP option envelopeFrom=").append(envelopeFrom);
        }
    }

    private <T> Constraint anArray(T... elements) {
        return new ArrayConstraint<T>(elements);
    }

    private static class ArrayConstraint<T> implements Constraint {
        private T[] elements;

        public ArrayConstraint(T[] elements) {
            this.elements = elements;
        }

        public boolean eval(Object o) {
            T[] elements = (T[])o;
            if (elements.length != this.elements.length) {
                return false;
            }
            for (int i = 0; i < elements.length; i++) {
                if (elements[i] == null) {
                    if (this.elements != null) {
                        return false;
                    }
                } else {
                    if (this.elements == null || !elements[i].equals(this.elements[i])) {
                        return false;
                    }
                }
            }
            return true;
        }

        public StringBuffer describeTo(StringBuffer buffer) {
            return buffer.append(Arrays.toString(elements));
        }
    }

    public static Test suite() {
        return new TestSuite(JavamailMessageAdapterTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}