/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.common.configuration.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.externalcomponentregister.IServiceName;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.builder.StubBuilder;
import org.jmock.core.Stub;
import org.jmock.core.Invocation;
import org.jmock.core.stub.ReturnStub;
import org.jmock.core.stub.ThrowStub;

import jakarta.mail.internet.MailDateFormat;
import jakarta.activation.MimeTypeParseException;
import jakarta.activation.MimeType;
import java.io.*;
import java.util.Arrays;

/**
 * @author QHAST
 */
public abstract class BaseMailboxTestCase extends MockObjectTestCase {

    protected static final String LOG4J_CONFIGURATION = "../log4jconf.xml";
    protected static MailDateFormat DF = new MailDateFormat();
    protected ILogger logger;

    //Top level configuration
    protected Mock configurationMock;
    protected Mock latestConfigurationMock;
    protected StubBuilder configurationGetConfigurationStubBuilder;

    //Mailbox configuration
    protected Mock mailboxConfigurationGroupMock;
    protected Mock addtionalPropertyAdd1Mock;
    protected Mock addtionalPropertyAdd2Mock;
    protected Mock eventDispatcherMock;
    protected StubBuilder latestConfigurationGetMailboxGroupStubBuilder;
    protected StubBuilder mailboxConfigurationGetAdditionalPropertiesGroupsStubBuilder;
    protected StubBuilder additionalProperty1GetNameParameterStubBuilder;

    //Message sender configuration
    protected Mock messageSenderConfigurationGroupMock;
    protected StubBuilder latestConfigurationGetMessageSenderGroupStubBuilder;
    protected StubBuilder smtpServiceNameParameterStubBuilder;
    protected StubBuilder smtpRetriesParameterStubBuilder;
    protected StubBuilder smtpConnectionTimeoutParameterStubBuilder;
    protected StubBuilder smtpCommandTimeoutParameterStubBuilder;


    public BaseMailboxTestCase(String name) {
        super(name);
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
        //DOMConfigurator.configure(LOG4J_CONFIGURATION);
    }




    public void setUp() throws Exception {
        super.setUp();
        logger = ILoggerFactory.getILogger(getClass());

        //Top level configuration
        configurationMock = mock(IConfiguration.class);
        latestConfigurationMock = mock(IConfiguration.class);
        configurationGetConfigurationStubBuilder = configurationMock.stubs().method("getConfiguration").withNoArguments();
        configurationGetConfigurationStubBuilder.will(returnValue(latestConfigurationMock.proxy()));
        eventDispatcherMock = mock(IEventDispatcher.class);

        //Mailbox configuration
        mailboxConfigurationGroupMock = mock(IGroup.class);
        addtionalPropertyAdd1Mock = mock(IGroup.class);
        addtionalPropertyAdd2Mock = mock(IGroup.class);
        latestConfigurationGetMailboxGroupStubBuilder = latestConfigurationMock.stubs().method("getGroup").with(eq("mailbox"));
        latestConfigurationGetMailboxGroupStubBuilder.will(returnValue(mailboxConfigurationGroupMock.proxy()));
        mailboxConfigurationGetAdditionalPropertiesGroupsStubBuilder = mailboxConfigurationGroupMock.stubs().method("getGroups").with(eq("message.additionalproperty"));
        mailboxConfigurationGroupMock.stubs().method("getFullName").withNoArguments().will(returnValue("mailbox"));
        additionalProperty1GetNameParameterStubBuilder = addtionalPropertyAdd1Mock.stubs().method("getString").with(eq("name"));
        additionalProperty1GetNameParameterStubBuilder.will(returnValue("add1"));
        addtionalPropertyAdd1Mock.stubs().method("getString").with(eq("field")).will(returnValue(""));
        addtionalPropertyAdd1Mock.stubs().method("getFullName").withNoArguments().will(returnValue("additionalproperty"));
        addtionalPropertyAdd2Mock.stubs().method("getString").with(eq("name")).will(returnValue(""));
        addtionalPropertyAdd2Mock.stubs().method("getString").with(eq("field")).will(returnValue(""));
        addtionalPropertyAdd2Mock.stubs().method("getFullName").withNoArguments().will(returnValue("additionalproperty"));
        mailboxConfigurationGetAdditionalPropertiesGroupsStubBuilder.will(returnValue(
                            Arrays.asList(new IGroup[]{
                                    (IGroup)addtionalPropertyAdd1Mock.proxy(),
                                    (IGroup)addtionalPropertyAdd2Mock.proxy()
                            })));

        //Message sender configuration
        messageSenderConfigurationGroupMock = mock(IGroup.class);
        smtpServiceNameParameterStubBuilder = messageSenderConfigurationGroupMock.stubs().method("getString").with(eq("smtpservicename"));
        smtpServiceNameParameterStubBuilder.will(returnValue(IServiceName.SMTP_STORAGE));
        smtpRetriesParameterStubBuilder = messageSenderConfigurationGroupMock.stubs().method("getInteger").with(eq("smtpretries"),eq(1));
        smtpRetriesParameterStubBuilder.will(returnValue(10));
        smtpConnectionTimeoutParameterStubBuilder = messageSenderConfigurationGroupMock.stubs().method("getInteger").with(eq("smtpconnectiontimeout"),eq(5000));
        smtpConnectionTimeoutParameterStubBuilder.will(returnValue(5000));
        smtpCommandTimeoutParameterStubBuilder = messageSenderConfigurationGroupMock.stubs().method("getInteger").with(eq("smtpcommandtimeout"),eq(5000));
        smtpCommandTimeoutParameterStubBuilder.will(returnValue(5000));
        latestConfigurationGetMessageSenderGroupStubBuilder = latestConfigurationMock.stubs().method("getGroup").with(eq("messagesender"));
        latestConfigurationGetMessageSenderGroupStubBuilder.will(returnValue(messageSenderConfigurationGroupMock.proxy()));


    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    protected IMediaObject getMedia(String filename, String mimeType) throws MediaObjectException, MimeTypeParseException {
        return createMediaObject(filename,new MimeType(mimeType));
    }

    protected static IMediaObject createMediaObject(String data, MimeType mimeType) {
        Mock mo = new Mock(IMediaObject.class);
        MediaProperties mp = new MediaProperties(mimeType);
        mo.stubs().method("getMediaProperties").withNoArguments().will(new ReturnStub(mp));
        Stub getInputStreamStub;
        Stub isImmutableStub;
        Stub getSizeStub;
        if(data != null) {
            getInputStreamStub = new MediaObjectInputStreamStub(data);
            isImmutableStub = new ReturnStub(true);
            getSizeStub = new ReturnStub(data.length());
        } else {
            getInputStreamStub = new ThrowStub(new IllegalStateException("Media object is not Immutable!"));
            isImmutableStub = new ReturnStub(false);
            getSizeStub = new ReturnStub(0);
        }
        mo.stubs().method("getInputStream").withNoArguments().will(getInputStreamStub);
        mo.stubs().method("isImmutable").withNoArguments().will(isImmutableStub);
        mo.stubs().method("getSize").withNoArguments().will(getSizeStub);
        return (IMediaObject) mo.proxy();
    }

    private static class MediaObjectInputStreamStub implements Stub {

        private String data;

        private MediaObjectInputStreamStub(String data) {
            this.data = data!=null?data:"";
        }

        public Object invoke(Invocation invocation) throws Throwable {
            return new ByteArrayInputStream(data.getBytes());
        }

        public StringBuffer describeTo(StringBuffer buffer) {
            return buffer.append("returns \"").append(data).append("\" as an new InputStream");
        }
    }

}
