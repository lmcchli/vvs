/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.MessagingException;
import jakarta.activation.MimeType;
import java.io.*;
import java.util.List;

import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.util.content.PageCounter;
import org.jmock.Mock;

/**
 * JavamailPartAdapter Tester.
 *
 * @author MANDE
 * @since <pre>12/19/2006</pre>
 * @version 1.0
 */
public class JavamailPartAdapterTest extends JavamailBaseTestCase {
    private JavamailFolderAdapter javamailFolderAdapter;
    private MimeBodyPart part;
    private JavamailPartAdapter javamailPartAdapter;

    public JavamailPartAdapterTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        javamailFolderAdapter = new JavamailFolderAdapter(folderMock, javamailContext, javamailStoreAdapter);
        part = new MimeBodyPart(getVoicePartInputStream());
        javamailPartAdapter = new JavamailPartAdapter(part, javamailContext, javamailFolderAdapter);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetMediaProperties() throws Exception {
        MediaProperties mediaProperties = javamailPartAdapter.getMediaProperties();
        assertTrue("Content type should match", mediaProperties.getContentType().match(new MimeType("audio/wav")));
        assertEquals("contentdispositionextension", mediaProperties.getFileExtension());
        assertEquals(0, mediaProperties.getSize());
        assertTrue(mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
        assertFalse(mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.PAGES));
        assertEquals(2000, mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
        List<MediaLength> allMediaLengths = mediaProperties.getAllMediaLengths();
        assertEquals(1, allMediaLengths.size());
        // Properties should be cached
        assertSame(mediaProperties, javamailPartAdapter.getMediaProperties());

        // Remove some headers and parse
        part.removeHeader("Content-Duration");
        part.removeHeader("Content-Disposition");
        javamailPartAdapter = new JavamailPartAdapter(part, javamailContext, javamailFolderAdapter);
        mediaProperties = javamailPartAdapter.getMediaProperties();
        assertTrue("Content type should match", mediaProperties.getContentType().match(new MimeType("audio/wav")));
        assertEquals("contenttypeextension", mediaProperties.getFileExtension());
        assertEquals(0, mediaProperties.getSize());
        assertFalse(mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
        assertFalse(mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.PAGES));
        allMediaLengths = mediaProperties.getAllMediaLengths();
        assertEquals(0, allMediaLengths.size());
        // Properties should be cached
        assertSame(mediaProperties, javamailPartAdapter.getMediaProperties());

        // Remove filename from Content-Type and parse
        part.setHeader("Content-Type", "audio/wav");
        javamailPartAdapter = new JavamailPartAdapter(part, javamailContext, javamailFolderAdapter);
        mediaProperties = javamailPartAdapter.getMediaProperties();
        assertTrue("Content type should match", mediaProperties.getContentType().match(new MimeType("audio/wav")));
        assertNull(mediaProperties.getFileExtension());
        assertEquals(0, mediaProperties.getSize());
        // Properties should be cached
        assertSame(mediaProperties, javamailPartAdapter.getMediaProperties());

        // Test page counting of fax message
        part = new MimeBodyPart(getFaxPartInputStream());
        javamailPartAdapter = new JavamailPartAdapter(part, javamailContext, javamailFolderAdapter);
        mediaProperties = javamailPartAdapter.getMediaProperties();
        assertTrue("Content type should match", mediaProperties.getContentType().match(new MimeType("image/tiff")));
        assertEquals(0, mediaProperties.getSize());
        assertTrue(mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.PAGES));
        allMediaLengths = mediaProperties.getAllMediaLengths();
        assertEquals(1, allMediaLengths.size());
        assertEquals("contenttypeextension", mediaProperties.getFileExtension());
        // Properties should be cached
        assertSame(mediaProperties, javamailPartAdapter.getMediaProperties());

        // Test when parse fails
        Mock mockPageCounter = mock(PageCounter.class);
        javamailContext.getPageCounterMap().put("image/tiff", (PageCounter)mockPageCounter.proxy());
        mockPageCounter.expects(once()).method("countPages").with(isA(InputStreamReader.class)).
                will(throwException(new IOException("ioexception")));
        javamailPartAdapter = new JavamailPartAdapter(part, javamailContext, javamailFolderAdapter);
        try {
            javamailPartAdapter.getMediaProperties();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetContentProperties() throws Exception {
        MessageContentProperties contentProperties = javamailPartAdapter.getContentProperties();
        assertEquals("Abcxyz voice message (3 seconds)", contentProperties.getDescription());
        assertEquals("contentdispositionfilename", contentProperties.getFilename());
        assertEquals(null, contentProperties.getLanguage());
        // Properties should be cached
        assertSame(contentProperties, javamailPartAdapter.getContentProperties());

        // Remove some headers and parse
        part.removeHeader("Content-Disposition");
        part.removeHeader("Content-Description");
        javamailPartAdapter = new JavamailPartAdapter(part, javamailContext, javamailFolderAdapter);
        contentProperties = javamailPartAdapter.getContentProperties();
        assertNull(contentProperties.getDescription());
        assertEquals("contenttypefilename", contentProperties.getFilename());
        assertNull(contentProperties.getLanguage());
        // Properties should be cached
        assertSame(contentProperties, javamailPartAdapter.getContentProperties());
    }

    public void testGetMediaObject() throws Exception {
        folderMock.expects(once()).method("isOpen").will(returnValue(false));
        folderMock.expects(once()).method("open").will(throwException(new MessagingException("messagingexception")));
        try {
            javamailPartAdapter.getMediaObject();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        folderMock.expects(once()).method("isOpen").will(returnValue(true));
        IMediaObject mediaObject = javamailPartAdapter.getMediaObject();
        assertEquals(0, mediaObject.getSize());
        MediaProperties mediaProperties = mediaObject.getMediaProperties();
        assertTrue("Content type should match", mediaProperties.getContentType().match(new MimeType("audio/wav")));
        assertEquals("contentdispositionextension", mediaProperties.getFileExtension());
        assertEquals(0, mediaProperties.getSize());
        assertTrue(mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
        assertFalse(mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.PAGES));
        assertEquals(2000, mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
        List<MediaLength> allMediaLengths = mediaProperties.getAllMediaLengths();
        assertEquals(1, allMediaLengths.size());
    }

    public void testGetPart() throws Exception {
        assertSame(part, javamailPartAdapter.getPart());
    }

    public void testMimeTypeParseException() throws Exception {
        // Set invalid MimeType
        part.setHeader("Content-Type", "invalid");
        javamailPartAdapter = new JavamailPartAdapter(part, javamailContext, javamailFolderAdapter);
        try {
            javamailPartAdapter.getMediaProperties();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
        javamailPartAdapter = new JavamailPartAdapter(part, javamailContext, javamailFolderAdapter);
        try {
            javamailPartAdapter.getContentProperties();
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    private InputStream getVoicePartInputStream() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        appendVoicePart(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    private InputStream getFaxPartInputStream() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        appendFaxPart(pw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    private void appendVoicePart(PrintWriter pw) {
        pw.println("Content-type: audio/wav; name=contenttypefilename.contenttypeextension");
        pw.println("Content-Transfer-Encoding: base64");
        pw.println("Content-Disposition: inline; voice=Voice-Message; filename=contentdispositionfilename.contentdispositionextension");
        pw.println("Content-Description: Abcxyz voice message (3 seconds)");
        pw.println("Content-Duration: 2");
        pw.println();
    }

    private void appendFaxPart(PrintWriter pw) {
        pw.println("Content-type: image/tiff; name=contenttypefilename.contenttypeextension");
        pw.println("Content-Transfer-Encoding: base64");
        pw.println();
    }

    public static Test suite() {
        return new TestSuite(JavamailPartAdapterTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
