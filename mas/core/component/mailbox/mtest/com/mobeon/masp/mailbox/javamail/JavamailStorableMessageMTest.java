/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.masp.mailbox.*;
import com.mobeon.masp.mediaobject.IMediaObject;
import static com.mobeon.masp.mediaobject.MediaLength.LengthUnit.MILLISECONDS;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.common.message_sender.SmtpInternetMailSender;
import com.mobeon.common.message_sender.jakarta.JakartaCommonsSmtpInternetMailSender;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.configuration.IConfiguration;
import com.dumbster.smtp.SimpleSmtpServer;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.core.Stub;
import org.jmock.core.Invocation;

import java.util.*;

/**
 * JavamailSender Tester.
 *
 * @author qhast
 */
public class JavamailStorableMessageMTest extends ConnectedMailboxTest
{
    private JavamailStorableMessageFactory storableMessageFactory;

   private String host = "ockelbo.lab.mobeon.com";
   private int port = 25;

    //private String host = "localhost";
    //private int port = 26;


    public JavamailStorableMessageMTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();



        Mock serviceInstance = mock(IServiceInstance.class);
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue(host));
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue(String.valueOf(port)));
        serviceInstance.stubs().method("getServiceName").withNoArguments().will(returnValue(IServiceName.SMTP_STORAGE));

        Mock serviceInstance2 = mock(IServiceInstance.class);
        serviceInstance2.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue(host));
        serviceInstance2.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue(String.valueOf(port+1)));
        serviceInstance2.stubs().method("getServiceName").withNoArguments().will(returnValue(IServiceName.SMTP_STORAGE));

        Mock serviceInstance3 = mock(IServiceInstance.class);
        serviceInstance3.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue(host));
        serviceInstance3.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue(String.valueOf(port+2)));
        serviceInstance3.stubs().method("getServiceName").withNoArguments().will(returnValue(IServiceName.SMTP_STORAGE));

        Mock serviceInstance4 = mock(IServiceInstance.class);
        serviceInstance4.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue(host));
        serviceInstance4.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue(String.valueOf(port+3)));
        serviceInstance4.stubs().method("getServiceName").withNoArguments().will(returnValue(IServiceName.SMTP_STORAGE));

        Mock serviceInstance5 = mock(IServiceInstance.class);
        serviceInstance5.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue(host));
        serviceInstance5.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue(String.valueOf(port+4)));
        serviceInstance5.stubs().method("getServiceName").withNoArguments().will(returnValue(IServiceName.SMTP_STORAGE));

        Mock errornousServiceInstance1 = mock(IServiceInstance.class);
        errornousServiceInstance1.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue("dumbo"));
        errornousServiceInstance1.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue(String.valueOf(25)));
        errornousServiceInstance1.stubs().method("getServiceName").withNoArguments().will(returnValue(IServiceName.SMTP_STORAGE));

        Mock errornousServiceInstance2 = mock(IServiceInstance.class);
        errornousServiceInstance2.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue(host));
        errornousServiceInstance2.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue(String.valueOf(143)));
        errornousServiceInstance2.stubs().method("getServiceName").withNoArguments().will(returnValue(IServiceName.SMTP_STORAGE));

        ServiceIntanceReturnerStub serviceIntanceReturnerStub =
                new ServiceIntanceReturnerStub(
                        (IServiceInstance)errornousServiceInstance1.proxy(),
                        (IServiceInstance)errornousServiceInstance2.proxy(),
                        (IServiceInstance)serviceInstance.proxy(),
                        (IServiceInstance)serviceInstance2.proxy(),
                        (IServiceInstance)serviceInstance3.proxy(),
                        (IServiceInstance)serviceInstance4.proxy(),
                        (IServiceInstance)serviceInstance5.proxy()
                        );

        Mock serviceLocator = mock(ILocateService.class);
        serviceLocator.stubs().method("locateService").with(eq(IServiceName.SMTP_STORAGE),eq(host)).will(serviceIntanceReturnerStub);
        serviceLocator.stubs().method("locateService").with(eq(IServiceName.SMTP_STORAGE)).will(serviceIntanceReturnerStub);

        serviceLocator.stubs().method("reportServiceError").withAnyArguments();

        SmtpInternetMailSender sender = new JakartaCommonsSmtpInternetMailSender();

        /*
        JavamailSender sender = new JavamailSender();
        Properties sessionProperties = new Properties();
        sessionProperties.put("mail.debug","true");
        sessionProperties.put("mail.smtp.sendpartial","true");
        sender.setDefaultSessionProperties(sessionProperties);
        */

        sender.setServiceLocator((ILocateService)serviceLocator.proxy());
        sender.setConfiguration((IConfiguration)configurationMock.proxy());
        eventDispatcherMock.expects(once()).method("addEventReceiver").with(eq(sender));
        sender.setEventDispatcher((IEventDispatcher)eventDispatcherMock.proxy());
        sender.init();


        JavamailContextFactory c = new JavamailContextFactory();
        //c.setConfiguration((IConfiguration)configurationMock.proxy());
        c.setConfiguration((IConfiguration)configurationMock.proxy());
        c.setInternetMailSender(sender);
        c.setMediaObjectFactory(new MediaObjectFactory());

        storableMessageFactory = new JavamailStorableMessageFactory();
        storableMessageFactory.setContextFactory(c);
    }

    public void testStoreMessage() throws Exception
    {
        IMediaObject spokenName = getMedia("spoken.wav","audio/wav");
        spokenName.getMediaProperties().setFileExtension("wav");
        spokenName.getMediaProperties().addLengthInUnit(MILLISECONDS,2567);

        IMediaObject faxMessage = getMedia("message.tif","image/tiff");
        faxMessage.getMediaProperties().setFileExtension("tif");

        IMediaObject voiceMessage = getMedia("message.wav","audio/wav");
        voiceMessage.getMediaProperties().setFileExtension("wav");
        voiceMessage.getMediaProperties().addLengthInUnit(MILLISECONDS,2341);

        IMediaObject videoMessage = getMedia("message.mov","video/quicktime");
        voiceMessage.getMediaProperties().setFileExtension("mov");
        voiceMessage.getMediaProperties().addLengthInUnit(MILLISECONDS,1334);

        IMediaObject textMessage = getMedia("message.txt","text/plain");
        textMessage.getMediaProperties().setFileExtension("txt");

        IMediaObject jpgMessage = getMedia("red.jpg","image/jpeg");
        jpgMessage.getMediaProperties().setFileExtension("jpg");



        IStorableMessage m = storableMessageFactory.create();

        m.setType(MailboxMessageType.VOICE);
        //m.setSender("Hawkanne Stolt (0703002054) <302102054@lab.mobeon.com>");
        m.setSender("\"John Blund (302102054)\" <302102054@lab.mobeon.com>");
        m.setSubject("Testing Storing VOICE Message");
        m.setRecipients("John Blund (302102054) <302102054@lab.mobeon.com>","nisse@lab.mobeon.com");

        m.setSpokenNameOfSender(spokenName,new MessageContentProperties("spoken","Originator's spoken name","en"));

        m.addContent(voiceMessage,new MessageContentProperties("message","Voice message","en"));
        m.addContent(jpgMessage,new MessageContentProperties("photo","Photo image","en"));
        m.addContent(faxMessage,new MessageContentProperties("fax","Fax message","en"));
        m.addContent(videoMessage,new MessageContentProperties("message","Video message","en"));
        send(m);

        Date now = new Date();
        long offset = 120000;
        Date deliveryDate = new Date(now.getTime()+offset);
        m.setUrgent(true);
        m.setConfidential(true);
        m.setSubject("Testing Storing VOICE Message deferred delivery at "+deliveryDate);
        m.setDeliveryDate(deliveryDate);
        m.setSpokenNameOfSender(videoMessage,new MessageContentProperties("spoken","Originator's spoken name","en"));
        send(m);

        m.setDeliveryDate(null);
        m.setSpokenNameOfSender(textMessage,new MessageContentProperties("spoken","Originator's spoken name","en"));
        send(m);


    }

    private void send(IStorableMessage m) throws MailboxException {
        SimpleSmtpServer server1 = SimpleSmtpServer.start(port);
        SimpleSmtpServer server2 = SimpleSmtpServer.start(port+1);
        SimpleSmtpServer server3 = SimpleSmtpServer.start(port+2);
        SimpleSmtpServer server4 = SimpleSmtpServer.start(port+3);
        SimpleSmtpServer server5 = SimpleSmtpServer.start(port+4);
        m.store();
        server1.stop();
        server2.stop();
        server3.stop();
        server4.stop();
        server5.stop();
    }

    public static Test suite()
    {
        return new TestSuite(JavamailStorableMessageMTest.class);
    }


    private static class ServiceIntanceReturnerStub implements Stub {

        private final List<IServiceInstance> serviceInstances;
        private int index = 0;

        public ServiceIntanceReturnerStub(IServiceInstance... serviceInstances) {
            this.serviceInstances = Arrays.asList(serviceInstances);
            this.index = this.serviceInstances.size()-1;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            IServiceInstance result = null;
            if(invocation.invokedMethod.getName().equals("locateService")) {
                Class[] pTypes = invocation.invokedMethod.getParameterTypes();
                if(pTypes.length==2 && pTypes[0].equals(String.class) && pTypes[1].equals(String.class)) {
                    index=0;
                } else {
                    index=++index%serviceInstances.size();
                }
                if(index == 0) {
                    Collections.shuffle(this.serviceInstances,new Random(System.currentTimeMillis()));
                }
                result = serviceInstances.get(index);
            }
            return result;
        }

        public StringBuffer describeTo(StringBuffer stringBuffer) {
            return new StringBuffer("returns one of "+serviceInstances);
        }
    }





}
