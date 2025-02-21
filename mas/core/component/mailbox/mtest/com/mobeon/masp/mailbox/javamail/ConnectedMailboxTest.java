/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.masp.mailbox.BaseMailboxTestCase;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.imap.ImapProperties;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.common.message_sender.jakarta.JakartaCommonsSmtpInternetMailSender;
import com.mobeon.masp.util.content.PageCounter;
import com.mobeon.masp.util.content.PageBreakingStringCounter;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import org.jmock.Mock;
import org.jmock.builder.StubBuilder;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author QHAST
 */
public abstract class ConnectedMailboxTest extends BaseMailboxTestCase {

    protected MailboxProfile mailboxProfile;
    protected IMailbox mbox;

    //SMTP Service
    protected Mock smtpServiceInstanceMock;
    protected StubBuilder smtpServiceHostnameStubBuilder;
    protected StubBuilder smtpServicePortStubBuilder;

    //IMAP Service
    protected Mock imapServiceInstanceMock;
    protected StubBuilder imapServiceHostnameStubBuilder;
    protected StubBuilder imapServicePortStubBuilder;

    //Service loctor
    protected Mock serviceLocator;
    protected StubBuilder serviceLocatorGetSmtpStubBuilder;
    protected StubBuilder serviceLocatorGetSmtpWithHosnameStubBuilder;

    //Mailbox IMAP configuration
    protected Mock mailboxImapConfiguration;
    protected StubBuilder mailboxConfigurationGetImapConnectionTimeoutStubBuilder;
    protected StubBuilder mailboxConfigurationGetImapCommandTimeoutStubBuilder;


    //Misc
    protected JavamailContextFactory jContextFactory;
    protected JavamailMailboxAccountManager accountManager;

    public ConnectedMailboxTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        //SMTP Service
        smtpServiceInstanceMock = mock(IServiceInstance.class);
        smtpServiceHostnameStubBuilder = smtpServiceInstanceMock.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME));
        smtpServiceHostnameStubBuilder.will(returnValue("ockelbo.lab.mobeon.com"));
        smtpServicePortStubBuilder = smtpServiceInstanceMock.stubs().method("getProperty").with(eq(IServiceInstance.PORT));
        smtpServicePortStubBuilder.will(returnValue("25"));

        //IMAP Service
        imapServiceInstanceMock = mock(IServiceInstance.class);
        imapServiceHostnameStubBuilder = imapServiceInstanceMock.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME));
        imapServiceHostnameStubBuilder.will(returnValue("ockelbo.lab.mobeon.com"));
        imapServicePortStubBuilder = imapServiceInstanceMock.stubs().method("getProperty").with(eq(IServiceInstance.PORT));
        imapServicePortStubBuilder.will(returnValue("143"));

        //Service locator
        serviceLocator = mock(ILocateService.class);
        serviceLocatorGetSmtpStubBuilder = serviceLocator.stubs().method("locateService").with(eq(IServiceName.SMTP_STORAGE));
        serviceLocatorGetSmtpStubBuilder.will(returnValue(smtpServiceInstanceMock.proxy()));
        serviceLocatorGetSmtpWithHosnameStubBuilder = serviceLocator.stubs().method("locateService").with(isA(String.class),eq(IServiceName.SMTP_STORAGE));
        serviceLocatorGetSmtpWithHosnameStubBuilder.will(returnValue(smtpServiceInstanceMock.proxy()));

        //Mailbox IMAP configuration
        mailboxImapConfiguration = mock(IGroup.class);
        mailboxConfigurationGroupMock.stubs().method("getGroup").with(eq("imap")).will(returnValue(mailboxImapConfiguration.proxy()));
        mailboxConfigurationGetImapConnectionTimeoutStubBuilder = mailboxImapConfiguration.stubs().method("getInteger").with(eq("connectiontimeout"),isA(Integer.class));
        mailboxConfigurationGetImapConnectionTimeoutStubBuilder.will(returnValue(5000));
        mailboxConfigurationGetImapCommandTimeoutStubBuilder = mailboxImapConfiguration.stubs().method("getInteger").with(eq("commandtimeout"),isA(Integer.class));
        mailboxConfigurationGetImapCommandTimeoutStubBuilder.will(returnValue(5000));

        JakartaCommonsSmtpInternetMailSender sender = new JakartaCommonsSmtpInternetMailSender();
        sender.setServiceLocator((ILocateService)serviceLocator.proxy());
        sender.setConfiguration((IConfiguration)configurationMock.proxy());
        eventDispatcherMock.expects(once()).method("addEventReceiver").with(eq(sender));
        sender.setEventDispatcher((IEventDispatcher)eventDispatcherMock.proxy());
        sender.init();

        jContextFactory = new JavamailContextFactory();
        jContextFactory.setMediaObjectFactory(new MediaObjectFactory());
        jContextFactory.setConfiguration((IConfiguration)configurationMock.proxy());
        jContextFactory.setInternetMailSender(sender);
        Map<String, PageCounter> pcMap = new HashMap<String, PageCounter>();
        pcMap.put("image/tiff", new PageBreakingStringCounter("Fax Image"));
        jContextFactory.setPageCounterMap(pcMap);
        Properties javamailSessionProperties = new Properties();
        javamailSessionProperties.put("mail.imap.auth.plain.disable","true");
        javamailSessionProperties.put("mail.imap.partialfetch","false");
        javamailSessionProperties.put("mail.debug","true");
        jContextFactory.setDefaultSessionProperties(javamailSessionProperties);

        ImapProperties imapProperties = new ImapProperties();
        imapProperties.setMessageUsageFolderNames(new String[]{"inbox"});
        jContextFactory.setImapProperties(imapProperties);

        JavamailBehavior jBehavior = new JavamailBehavior();
        jBehavior.setCloseNonSelectedFolders(true);
        jContextFactory.setJavamailBehavior(jBehavior);

        accountManager = new JavamailMailboxAccountManager();
        accountManager.setContextFactory(jContextFactory);

        //mbox = accountManager.getMailbox((IServiceInstance)imapServiceInstanceMock.proxy(),mailboxProfile);

    }

    protected IMailbox getMailbox() throws MailboxException {
        return accountManager.getMailbox((IServiceInstance)imapServiceInstanceMock.proxy(),mailboxProfile);
    }


}
