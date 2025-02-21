/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import com.mobeon.common.message_sender.IInternetMailSender;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.masp.mailbox.imap.ImapProperties;
import com.mobeon.masp.util.content.PageCounter;

import java.util.Properties;
import java.util.HashMap;

/**
 * JavamailContextFactory Tester.
 *
 * @author MANDE
 * @since <pre>12/20/2006</pre>
 * @version 1.0
 */
public class JavamailContextFactoryTest extends JavamailBaseTestCase {
    private JavamailContextFactory javamailContextFactory;

    public JavamailContextFactoryTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        javamailContextFactory = new JavamailContextFactory();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetGetJavamailBehavior() throws Exception {
        assertNull(javamailContextFactory.getJavamailBehavior());
        JavamailBehavior javamailBehavior = new JavamailBehavior();
        javamailContextFactory.setJavamailBehavior(javamailBehavior);
        assertSame(javamailBehavior, javamailContextFactory.getJavamailBehavior());
    }

    public void testSetGetInternetMailSender() throws Exception {
        assertNull(javamailContextFactory.getInternetMailSender());
        Mock mockInternetMailSender = mock(IInternetMailSender.class);
        javamailContextFactory.setInternetMailSender((IInternetMailSender)mockInternetMailSender.proxy());
        assertSame(mockInternetMailSender.proxy(), javamailContextFactory.getInternetMailSender());
    }

    public void testSetGetDefaultSessionProperties() throws Exception {
        assertNull(javamailContextFactory.getDefaultSessionProperties());
        Properties defaultSessionProperties = new Properties();
        javamailContextFactory.setDefaultSessionProperties(defaultSessionProperties);
        assertSame(defaultSessionProperties, javamailContextFactory.getDefaultSessionProperties());
    }

    public void testCreate() throws Exception {
        javamailContextFactory.setConfiguration(getMockConfiguration());
        Mock mockInternetMailSender = mock(IInternetMailSender.class);
        javamailContextFactory.setInternetMailSender((IInternetMailSender)mockInternetMailSender.proxy());
        Mock mockMediaObjectFactory = mock(IMediaObjectFactory.class);
        javamailContextFactory.setMediaObjectFactory((IMediaObjectFactory)mockMediaObjectFactory.proxy());
        JavamailBehavior javamailBehavior = new JavamailBehavior();
        javamailContextFactory.setJavamailBehavior(javamailBehavior);
        ImapProperties imapProperties = new ImapProperties();
        javamailContextFactory.setImapProperties(imapProperties);
        HashMap<String, PageCounter> pageCounterMap = new HashMap<String, PageCounter>(0);
        javamailContextFactory.setPageCounterMap(pageCounterMap);
        JavamailContext javamailContext = javamailContextFactory.create();
        assertNotNull(javamailContext);
        assertSame(javamailContext.getImapProperties(), imapProperties);
        assertSame(javamailContext.getInternetMailSender(), mockInternetMailSender.proxy());
        assertSame(javamailContext.getJavamailBehavior(), javamailBehavior);
        assertSame(javamailContext.getMediaObjectFactory(), mockMediaObjectFactory.proxy());
        assertSame(javamailContext.getPageCounterMap(), pageCounterMap);

        // With MailboxProfile
        MailboxProfile mailboxProfile = new MailboxProfile();
        javamailContextFactory.setConfiguration(getMockConfiguration());
        javamailContext = javamailContextFactory.create(mailboxProfile);
        assertNotNull(javamailContext);
        assertSame(javamailContext.getImapProperties(), imapProperties);
        assertSame(javamailContext.getInternetMailSender(), mockInternetMailSender.proxy());
        assertSame(javamailContext.getJavamailBehavior(), javamailBehavior);
        assertSame(javamailContext.getMediaObjectFactory(), mockMediaObjectFactory.proxy());
        assertSame(javamailContext.getMailboxProfile(), mailboxProfile);
        assertSame(javamailContext.getPageCounterMap(), pageCounterMap);
    }


    public static Test suite() {
        return new TestSuite(JavamailContextFactoryTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
