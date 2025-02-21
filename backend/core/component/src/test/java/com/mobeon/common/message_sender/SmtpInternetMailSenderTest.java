/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.common.message_sender;

import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SmtpInternetMailSender Tester.
 *
 * @author MANDE
 * @version 1.0
 * @since <pre>08/17/2006</pre>
 */
public class SmtpInternetMailSenderTest extends MessageSenderBaseTestCase {

    static {
        ILoggerFactory.configureAndWatch("log4jconf.xml");
    }

    protected static final Session SESSION = Session.getInstance(new Properties());

    private SmtpInternetMailSender<SmtpInternetMailSenderConfig> smtpInternetMailSender;
    private Mock mockServiceLocator;
    private Mock mockServiceInstance;
    private SmtpInternetMailSenderConfig smtpInternetMailSenderConfig;

    public SmtpInternetMailSenderTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        smtpInternetMailSender = new SmtpInternetMailSender<SmtpInternetMailSenderConfig>() {

            {
                atomicConfig = new AtomicReference<SmtpInternetMailSenderConfig>();
            }

            protected SmtpInternetMailSenderConfig newConfig() {
                return new SmtpInternetMailSenderConfig();
            }

            protected void sendInternetMailWork(MimeMessage message, String host, SmtpOptions options) {
            }
        };
        mockServiceLocator = mock(ILocateService.class);
        smtpInternetMailSender.setServiceLocator(getMockServiceLocator());
        smtpInternetMailSender.setConfiguration(getConfiguration("cfg/backend.xml"));
        smtpInternetMailSenderConfig = smtpInternetMailSender.getConfig();
        mockServiceInstance = mock(IServiceInstance.class);
        mockServiceInstance.stubs().method("getServiceName").will(returnValue(IServiceName.SMTP_STORAGE));
        mockServiceInstance.stubs().method("getProperty").with(eq("hostname")).will(returnValue("hostname"));
        mockServiceInstance.stubs().method("getProperty").with(eq("port")).will(returnValue("25"));
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetSetConfiguration() throws Exception {
        Mock mockConfiguration = mock(IConfiguration.class);
        smtpInternetMailSender.setConfiguration((IConfiguration) mockConfiguration.proxy());
        assertSame(mockConfiguration.proxy(), smtpInternetMailSender.getConfiguration());
    }

    public void testGetSetServiceLocator() throws Exception {
        Mock mockServiceLocator = mock(ILocateService.class);
        smtpInternetMailSender.setServiceLocator((ILocateService) mockServiceLocator.proxy());
        assertSame(mockServiceLocator.proxy(), smtpInternetMailSender.getServiceLocator());
    }

    public void testGetSetEventDispatcher() throws Exception {
        assertNull(smtpInternetMailSender.getEventDispatcher());
        Mock mockEventDispatcher = mock(IEventDispatcher.class);
        smtpInternetMailSender.setEventDispatcher((IEventDispatcher) mockEventDispatcher.proxy());
        assertSame(mockEventDispatcher.proxy(), smtpInternetMailSender.getEventDispatcher());
    }

    public void testCheckToAddresses() throws Exception {
        String addressWithoutDomain = "notification.off";
        ByteArrayInputStream messageInputStream = getNotificationMessage(addressWithoutDomain);
        MimeMessage mimeMessage = new MimeMessage(SESSION, messageInputStream);
        String domain = "domain";
        Address[] recipients = new Address[]{
                new InternetAddress(addressWithoutDomain + "@" + domain),
        };
        smtpInternetMailSender.addDomainToAddresses(mimeMessage, domain);
        assertEquals(recipients, mimeMessage.getRecipients(Message.RecipientType.TO));

        // Test with ordinary recipients
        String[] rcpts = new String[]{"mande1@lab.mobeon.com", "mande2@lab.mobeon.com", "mande3@lab.mobeon.com"};
        messageInputStream = getNotificationMessage(rcpts);
        mimeMessage = new MimeMessage(SESSION, messageInputStream);
        recipients = new Address[rcpts.length];
        for (int i = 0; i < recipients.length; i++) {
            recipients[i] = new InternetAddress(rcpts[i]);
        }
        assertEquals(recipients, mimeMessage.getRecipients(Message.RecipientType.TO));
        smtpInternetMailSender.addDomainToAddresses(mimeMessage, domain);
        assertEquals(recipients, mimeMessage.getRecipients(Message.RecipientType.TO));

        // Test without recipients
        messageInputStream = getNotificationMessage();
        mimeMessage = new MimeMessage(SESSION, messageInputStream);
        smtpInternetMailSender.addDomainToAddresses(mimeMessage, domain);
    }

    public void testCheckToAddressesMessagingException() throws Exception {
        ByteArrayInputStream messageInputStream = getNotificationMessage("invalidaddress@");
        MimeMessage mimeMessage = new MimeMessage(SESSION, messageInputStream);
        String domain = "domain";
        try {
            smtpInternetMailSender.addDomainToAddresses(mimeMessage, domain);
            fail("Expected InternetMailSenderException");
        } catch (InternetMailSenderException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    public void testGetRecipientsEmailAddresses() throws Exception {
        // Test with ordinary recipients
        String[] rcpts = new String[]{"mande1@lab.mobeon.com", "mande2@lab.mobeon.com", "mande3@lab.mobeon.com"};
        ByteArrayInputStream messageInputStream = getNotificationMessage(rcpts);
        MimeMessage mimeMessage = new MimeMessage(SESSION, messageInputStream);

        String[] recipients = smtpInternetMailSender.getRecipientsEmailAddresses(mimeMessage);
        assertEquals(rcpts, recipients);

        // Test without recipients
        messageInputStream = getNotificationMessage();
        mimeMessage = new MimeMessage(SESSION, messageInputStream);
        recipients = smtpInternetMailSender.getRecipientsEmailAddresses(mimeMessage);
        assertNotNull("Recipients should not be null", recipients);
        assertEquals("Recipients should be empty", 0, recipients.length);
    }

    /**
     * Test retrieving smtp service instances in some situations
     *
     * @throws Exception
     */
    public void testGetSmtpService() throws Exception {
        // Test get preferred host
        IServiceInstance instance = getServiceInstance();
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.SMTP_STORAGE)).
                will(returnValue(instance));
        SmtpInternetMailSender.SmtpServiceInstanceDecorator smtpService =
                smtpInternetMailSender.getSmtpService(smtpInternetMailSenderConfig);
        assertEquals(instance, smtpService.getDecoratedServiceInstance());

        // Test get specified host
        String knownhost = "knownhost";
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.SMTP_STORAGE), eq(knownhost)).
                will(returnValue(instance));
        smtpService = smtpInternetMailSender.getSmtpService(knownhost, smtpInternetMailSenderConfig);
        assertEquals(instance, smtpService.getDecoratedServiceInstance());

        // Test get specified host that does not exist
        String unknownhost = "unknownhost";
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.SMTP_STORAGE), eq(unknownhost)).
                will(throwException(new NoServiceFoundException("noservicefoundexception")));
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.SMTP_STORAGE)).
                will(returnValue(instance));
        smtpService = smtpInternetMailSender.getSmtpService(unknownhost, smtpInternetMailSenderConfig);
        assertEquals(instance, smtpService.getDecoratedServiceInstance());
    }

    /**
     * Test retrieving smtp service instances in some situations where NoServiceFoundException is thrown
     *
     * @throws Exception
     */
    public void testGetSmtpServiceNoServiceFoundException() throws Exception {
        // Test get preferred host
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.SMTP_STORAGE)).
                will(throwException(new NoServiceFoundException("noservicefoundexception")));
        try {
            smtpInternetMailSender.getSmtpService(smtpInternetMailSenderConfig);
            fail("Expected InternetMailSenderException");
        } catch (InternetMailSenderException e) {
            assertTrue(true); // For statistical purposes
        }

        // Test get specified host
        String unknownhost = "unknownhost";
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.SMTP_STORAGE), eq(unknownhost)).
                will(throwException(new NoServiceFoundException("noservicefoundexception")));
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.SMTP_STORAGE)).
                will(throwException(new NoServiceFoundException("noservicefoundexception")));
        try {
            smtpInternetMailSender.getSmtpService(unknownhost, smtpInternetMailSenderConfig);
            fail("Expected InternetMailSenderException");
        } catch (InternetMailSenderException e) {
            assertTrue(true); // For statistical purposes
        }
    }

    private IServiceInstance getServiceInstance() {
        return (IServiceInstance) mockServiceInstance.proxy();
    }

    private ILocateService getMockServiceLocator() {
        return (ILocateService) mockServiceLocator.proxy();
    }

    private ByteArrayInputStream getNotificationMessage(String... rcpts) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        for (String rcpt : rcpts) {
            printWriter.println("To: " + rcpt);
        }
        printWriter.println("");
        printWriter.println("body");
        return new ByteArrayInputStream(stringWriter.toString().getBytes());
    }

    public static Test suite() {
        return new TestSuite(SmtpInternetMailSenderTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
