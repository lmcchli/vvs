/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxAuthenticationFailedException;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.logging.ILoggerFactory;
import org.jmock.Mock;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import java.util.Properties;

/**
 * JavamailMailboxAccountManager Tester.
 *
 * @author MANDE
 * @since <pre>12/07/2006</pre>
 * @version 1.0
 */
public class JavamailMailboxAccountManagerTest extends JavamailBaseTestCase {
    private JavamailMailboxAccountManager javamailMailboxAccountManager;

    public JavamailMailboxAccountManagerTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        javamailMailboxAccountManager = new JavamailMailboxAccountManager();
        JavamailContextFactory javamailContextFactory = getJavamailContextFactory();
        javamailContextFactory.setDefaultSessionProperties(getDefaultSessionProperties());
        javamailMailboxAccountManager.setContextFactory(javamailContextFactory);
    }

    /**
     * Adds additional expectations to MockStore when BasicStoreManager is used.
     * @throws Exception
     */
    @Override
    protected void setUpMockStore() throws Exception {
        super.setUpMockStore();
        // The addConnectionListener is only called if debug is enabled in BasicStoreManager
        if (ILoggerFactory.getILoggerFromCategory("com.mobeon.masp.util.javamail.BasicStoreManager").isDebugEnabled()) {
            mockStore.expects(once()).method("addConnectionListener");
        }
    }

    private Properties getDefaultSessionProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.debug", "true");
        return properties;
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetMailboxAuthenticationFailedException() throws Exception {
        mockStore.expects(once()).method("connect").
                will(throwException(new AuthenticationFailedException("authenticationfailedexception")));

        try {
            javamailMailboxAccountManager.getMailbox(
                    getMockServiceInstance("mailhost", 143),
                    new MailboxProfile("accountid", "accountpassword", "emailaddress")
            );
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue("Expected MailboxAuthenticationFailedException", e instanceof MailboxAuthenticationFailedException);
        }
    }

    public void testGetMailboxMessagingException() throws Exception {
        mockStore.expects(once()).method("connect").
                will(throwException(new MessagingException("messagingexception")));

        try {
            javamailMailboxAccountManager.getMailbox(
                    getMockServiceInstance("mailhost", 143),
                    new MailboxProfile("accountid", "accountpassword", "emailaddress")
            );
            fail("Expected MailboxException");
        } catch (MailboxException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetMailbox() throws Exception {
        mockStore.expects(once()).method("connect");

        IMailbox mailbox = javamailMailboxAccountManager.getMailbox(
                getMockServiceInstance("mailhost", 143),
                new MailboxProfile("accountid", "accountpassword", "emailaddress")
        );
        assertNotNull("Mailbox should not be null", mailbox);
    }

    private IServiceInstance getMockServiceInstance(String hostName, int port) {
        Mock mockServiceInstance = mock(IServiceInstance.class);
        mockServiceInstance.expects(once()).method("getProperty").with(eq("hostname")).will(returnValue(hostName));
        mockServiceInstance.expects(once()).method("getProperty").with(eq("port")).will(returnValue(Integer.toString(port)));
        return (IServiceInstance)mockServiceInstance.proxy();
    }

    public static Test suite() {
        return new TestSuite(JavamailMailboxAccountManagerTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
