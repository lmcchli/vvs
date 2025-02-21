/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.mail.internet.ContentDisposition;
import jakarta.activation.MimeType;

import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.mediaobject.MediaProperties;

/**
 * ContentDispositionHeaderUtil Tester.
 *
 * @author MANDE
 * @since <pre>12/20/2006</pre>
 * @version 1.0
 */
public class ContentDispositionHeaderUtilTest extends JavamailBaseTestCase {
    public ContentDispositionHeaderUtilTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCoverage() throws Exception {
        // Only to get full coverage of util class with static methods
        new ContentDispositionHeaderUtil();
    }

    public void testCreateContentDisposition() throws Exception {
        MessageContentProperties contentProperties = new MessageContentProperties();
        contentProperties.setFilename("filename.extension");
        MediaProperties mediaProperties = new MediaProperties();
        mediaProperties.setContentType(new MimeType("audio/wav"));
        ContentDisposition expected = new ContentDisposition("inline; filename=filename.extension; voice=Voice-Message");
        ContentDisposition actual =
                ContentDispositionHeaderUtil.createContentDisposition(false, contentProperties, mediaProperties);
        assertEquals(expected.toString(), actual.toString());
        expected = new ContentDisposition("inline; filename=filename.extension; voice=Originator-Spoken-Name");
        actual = ContentDispositionHeaderUtil.createContentDisposition(true, contentProperties, mediaProperties);
        assertEquals(expected.toString(), actual.toString());

        mediaProperties.setContentType(new MimeType("video/quicktime"));
        expected = new ContentDisposition("inline; filename=filename.extension; video=Video-Message");
        actual = ContentDispositionHeaderUtil.createContentDisposition(false, contentProperties, mediaProperties);
        assertEquals(expected.toString(), actual.toString());
        expected = new ContentDisposition("inline; filename=filename.extension; video=Originator-Spoken-Name");
        actual = ContentDispositionHeaderUtil.createContentDisposition(true, contentProperties, mediaProperties);
        assertEquals(expected.toString(), actual.toString());

        mediaProperties.setContentType(new MimeType("text/plain"));
        expected = new ContentDisposition("inline; filename=filename.extension; category=Originator-Spoken-Name");
        actual = ContentDispositionHeaderUtil.createContentDisposition(true, contentProperties, mediaProperties);
        assertEquals(expected.toString(), actual.toString());
    }

    public static Test suite() {
        return new TestSuite(ContentDispositionHeaderUtilTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
