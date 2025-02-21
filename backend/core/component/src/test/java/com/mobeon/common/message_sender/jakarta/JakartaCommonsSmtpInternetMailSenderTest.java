package com.mobeon.common.message_sender.jakarta;

import com.dumbster.smtp.SimpleSmtpServer;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.message_sender.MessageSenderBaseTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.core.Stub;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

/**
 * JakartaCommonsSmtpInternetMailSender Tester.
 *
 * @author MANDE
 * @version 1.0
 * @since <pre>08/28/2006</pre>
 */
public class JakartaCommonsSmtpInternetMailSenderTest extends MessageSenderBaseTestCase {
    private static final Session SESSION = Session.getInstance(new Properties());

    static {
        ILoggerFactory.configureAndWatch("log4jconf.xml");
    }

    private JakartaCommonsSmtpInternetMailSender internetMailSender;
    private Mock mockServiceLocator = mock(ILocateService.class);
    private Mock mockEventDispatcher;
    // Use other port than default since Norton Antivirus intercepts in Windows environment
    private static final int SMTP_PORT = 2525;

    public JakartaCommonsSmtpInternetMailSenderTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        internetMailSender = new JakartaCommonsSmtpInternetMailSender();
        internetMailSender.setConfiguration(getConfiguration("cfg/backend.conf"));
        internetMailSender.setServiceLocator(getServiceLocator());
        setUpEventDispatcher();
        internetMailSender.setEventDispatcher(getEventDispatcher());
        internetMailSender.init();
    }

    private void setUpEventDispatcher() {
        mockEventDispatcher = mock(IEventDispatcher.class);
        mockEventDispatcher.expects(once()).method("addEventReceiver").with(same(internetMailSender));
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetSetSoLinger() throws Exception {
        internetMailSender.setSoLingerTime(2000);
        assertEquals(2, internetMailSender.getSoLingerTime());
        internetMailSender.setSoLingerTime(-1);
        assertEquals(-1, internetMailSender.getSoLingerTime());
        internetMailSender.setSoLingerTime(100);
        assertEquals(1, internetMailSender.getSoLingerTime());
    }

    public void testIsSetTcpNoDelay() throws Exception {
        // Default false
        assertFalse("TcpNoDelay should be default false", internetMailSender.isTcpNoDelay());
        internetMailSender.setTcpNoDelay(true);
        assertTrue("TcpNoDelay should be true", internetMailSender.isTcpNoDelay());
    }

    public void testSendInternetMail() throws Exception {
        SimpleSmtpServer smtpServer = SimpleSmtpServer.start(SMTP_PORT);
        mockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.SMTP_STORAGE)).
                will(returnServiceInstance());
        internetMailSender.sendInternetMail(getMessage());
        smtpServer.stop();
        assertEquals(1, smtpServer.getReceivedEmailSize());
    }

    private Stub returnServiceInstance() {
        Mock mockServiceInstance = mock(IServiceInstance.class);
        mockServiceInstance.expects(once()).method("getProperty").with(eq("hostname")).will(returnValue("localhost"));
        mockServiceInstance.expects(once()).method("getProperty").with(eq("port")).
                will(returnValue(Integer.toString(SMTP_PORT)));
        return returnValue(mockServiceInstance.proxy());
    }

    private MimeMessage getMessage() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println("From: mande1@lab.mobeon.com");
        printWriter.println("To: mande2@lab.mobeon.com");
        printWriter.println("");
        printWriter.println("body");
        ByteArrayInputStream bais = new ByteArrayInputStream(stringWriter.toString().getBytes());
        return new MimeMessage(SESSION, bais);
    }

    private ILocateService getServiceLocator() {
        return (ILocateService) mockServiceLocator.proxy();
    }

    private IEventDispatcher getEventDispatcher() {
        return (IEventDispatcher) mockEventDispatcher.proxy();
    }

    public static Test suite() {
        return new TestSuite(JakartaCommonsSmtpInternetMailSenderTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
