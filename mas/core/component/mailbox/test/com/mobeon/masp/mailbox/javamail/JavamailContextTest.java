package com.mobeon.masp.mailbox.javamail;

import junit.framework.*;
import com.mobeon.masp.mailbox.BaseMailboxTestCase;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.message_sender.IInternetMailSender;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;

import java.util.Properties;

import org.jmock.Mock;

import jakarta.mail.Session;

/**
 * Tests the JavamailContext class
 *
 * @author mande
 */
public class JavamailContextTest extends BaseMailboxTestCase {
    JavamailContext javamailContext;
    JavamailContextFactory javamailContextFactory;

    public JavamailContextTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        javamailContextFactory = new JavamailContextFactory();
        javamailContextFactory.setConfiguration((IConfiguration)configurationMock.proxy());
        Mock mockInternetSender = mock(IInternetMailSender.class);
        javamailContextFactory.setInternetMailSender((IInternetMailSender)mockInternetSender.proxy());
        javamailContextFactory.setMediaObjectFactory(new MediaObjectFactory());
        Properties javamailSessionProperties = new Properties();
        javamailSessionProperties.put("mail.imap.auth.plain.disable","true");
        javamailSessionProperties.put("mail.imap.partialfetch","false");
        javamailSessionProperties.put("mail.debug","true");
        javamailContextFactory.setDefaultSessionProperties(javamailSessionProperties);
        mailboxConfigurationGroupMock.stubs().method("getGroup").with(eq("imap")).will(returnValue(getImapGroup()));

    }

    public void testSession() throws Exception {
        JavamailContext javamailContext = javamailContextFactory.create();
        Session session1 = javamailContext.getJavamailSession();
        javamailContext = javamailContextFactory.create();
        Session session2 = javamailContext.getJavamailSession();
        assertSame("Session should be cached", session1, session2);
        setUp();
        javamailContext = javamailContextFactory.create();
        session2 = javamailContext.getJavamailSession();
        assertNotSame("Session should not be cached", session1, session2);
    }

    private IGroup getImapGroup() {
        Mock mockImapGroup = mock(IGroup.class, "mockImapGroup");
        mockImapGroup.stubs().method("getInteger").with(eq("connectiontimeout"), eq(5000)).
                will(returnValue(5000));
        mockImapGroup.stubs().method("getInteger").with(eq("commandtimeout"), eq(5000)).
                will(returnValue(5000));
        return (IGroup)mockImapGroup.proxy();
    }

    public static Test suite() {
        return new TestSuite(JavamailContextTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}