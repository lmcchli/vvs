package com.mobeon.ntf.text;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Date;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException.NotifierMfsExceptionCause;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.slamdown.CallerInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import org.powermock.core.classloader.annotations.*;

@PowerMockIgnore("jdk.internal.reflect.*")
//@SuppressStaticInitializationFor("com.mobeon.ntf.Config")
@SuppressStaticInitializationFor("com.abcxyz.messaging.mfs.MFSFactory") // Required because of a dependency problem with LibSysUtils
@RunWith(PowerMockRunner.class)
@PrepareForTest({Config.class, Template.class})
public class TemplateTest {
    
    protected NotificationEmail mockNotificationEmail = null;
    protected String language = "en";
    protected String cosName = "cos:foobar";
    CallerInfo caller = null;
    MyNotifierNotificationInfo notifInfo = null;
    boolean generateDefault = true;
    
    @Before
    public void setUp()
    {
        Logger.setLogLevel(Logger.L_ERROR);        
    }

    @After
    public void tearDown()
    {

    }

    //@Test
    @Ignore
    public void testCountSMSNotification() throws Exception {
        UserInfo user = new UserForUnitTests();
        String notifType = "c";      // SMS notification "count"

        createConfigMock();
        createNoficationEmailMock(Constants.NTF_EMAIL);
        PowerMock.replayAll();
        
        Template template = new Template(
                Phrases.getTemplateStrings(language),
                notifType,
                Phrases.getCphrTemplateStrings(language, cosName),
                language);
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String expectedText = "You have one new voice message, 2 new faxes, 3 new emails and 4 new video messages.";        
        String text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
        
        inbox = new UserMailbox(0, 0, 0, 0, 0,0,0,0,false);
        expectedText = "You have no new messages.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
        
        inbox = new UserMailbox(0, 0, 0, 1, 0,0,0,0,false);
        expectedText = "You have one new video message.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
        
        inbox = new UserMailbox(0, 0, 0, 3, 0,0,0,0,false);
        expectedText = "You have 3 new video messages.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
        
        inbox = new UserMailbox(0, 0, 1, 0, 0,0,0,0,false);
        expectedText = "You have one new email.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(0, 0, 2, 0, 0,0,0,0,false);
        expectedText = "You have 2 new emails.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(0, 1, 0, 0, 0,0,0,0,false);
        expectedText = "You have one new fax.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(0, 1, 1, 0, 0,0,0,0,false);
        expectedText = "You have one new fax and one new email.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(0, 1, 3, 0, 0,0,0,0,false);
        expectedText = "You have one new fax and 3 new emails.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(0, 2, 0, 0, 0,0,0,0,false);
        expectedText = "You have 2 new faxes.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(0, 3, 1, 0, 0,0,0,0,false);
        expectedText = "You have 3 new faxes and one new email.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(0, 4, 4, 4, 0,0,0,0,false);
        expectedText = "You have 4 new faxes, 4 new emails and 4 new video messages.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(1, 0, 0, 0, 0,0,0,0,false);
        expectedText = "You have one new voice message.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(1, 0, 1, 0, 0,0,0,0,false);
        expectedText = "You have one new voice message and one new email.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(1, 0, 5, 0, 0,0,0,0,false);
        expectedText = "You have one new voice message and 5 new emails.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(1, 1, 0, 0, 0,0,0,0,false);
        expectedText = "You have one new voice message and one new fax.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(1, 1, 1, 0, 0,0,0,0,false);
        expectedText = "You have one new voice message, one new fax and one new email.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(1, 1, 6, 0, 0,0,0,0,false);
        expectedText = "You have one new voice message, one new fax and 6 new emails.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(1, 5, 0, 0, 0,0,0,0,false);
        expectedText = "You have one new voice message and 5 new faxes.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(1, 6, 1, 0, 0,0,0,0,false);
        expectedText = "You have one new voice message, 6 new faxes and one new email.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(1, 7, 7, 0, 0,0,0,0,false);
        expectedText = "You have one new voice message, 7 new faxes and 7 new emails.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(2, 0, 0, 0, 0,0,0,0,false);
        expectedText = "You have 2 new voice messages.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(3, 0, 1, 0, 0,0,0,0,false);
        expectedText = "You have 3 new voice messages and one new email.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(4, 0, 8, 0, 0,0,0,0,false);
        expectedText = "You have 4 new voice messages and 8 new emails.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(5, 1, 0, 0, 0,0,0,0,false);
        expectedText = "You have 5 new voice messages and one new fax.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(6, 1, 1, 0, 0,0,0,0,false);
        expectedText = "You have 6 new voice messages, one new fax and one new email.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(7, 1, 9, 0, 0,0,0,0,false);
        expectedText = "You have 7 new voice messages, one new fax and 9 new emails.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(8, 8, 8, 0, 0,0,0,0,false);
        expectedText = "You have 8 new voice messages, 8 new faxes and 8 new emails.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(9, 9, 1, 3, 0,0,0,0,false);
        expectedText = "You have 9 new voice messages, 9 new faxes, one new email and 3 new video messages.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        inbox = new UserMailbox(10, 10, 10, 10, 0,0,0,0,false);
        expectedText = "You have 10 new voice messages, 10 new faxes, 10 new emails and 10 new video messages.";
        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));

        text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
    }


    //@Test
    @Ignore
    public void testHeaderSMSNotification() throws Exception {
        UserInfo user = new UserForUnitTests();
        String notifType = "h";      // SMS notification "header"

        createConfigMock();
        createNoficationEmailMock(Constants.NTF_EMAIL);
        PowerMock.replayAll();
        
        Template template = new Template(
                Phrases.getTemplateStrings(language),
                notifType,
                Phrases.getCphrTemplateStrings(language, cosName),
                language);
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String expectedText = "You have a new confidential email message from an unknown caller. Message received at 14:20, 2005-01-01";
        String text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
    }

    //@Test
    @Ignore
    public void testSubjectSMSlNotification() throws Exception {
        UserInfo user = new UserForUnitTests();
        String notifType = "s";  // SMS notification "subject"

        createConfigMock();
        createNoficationEmailMock(Constants.NTF_EMAIL);
        PowerMock.replayAll();
        
        Template template = new Template(
                Phrases.getTemplateStrings(language),
                notifType,
                Phrases.getCphrTemplateStrings(language, cosName),
                language);
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String expectedText = "You have a new confidential email message regarding \"Check this out\". Message received at 14:20, 2005-01-01";
        String text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
    }
    

    //@Test
    @Ignore
    public void testGeneralNotification() throws Exception {
        UserInfo user = new UserForUnitTests();
        String notifType = "general";
        String expectedText = "You have new messages.";

        createConfigMock();
        createNoficationEmailMock(Constants.NTF_EMAIL);
        PowerMock.replayAll();
        
        Template template = new Template(
                Phrases.getTemplateStrings(language),
                notifType,
                Phrases.getCphrTemplateStrings(language, cosName),
                language);
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        
        String text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
    }
    
    
    //@Test
    @Ignore
    public void testMMSNotification() throws Exception {
        UserInfo user = new UserForUnitTests();
        String notifType = "mms_subject";  // MMS notification
        String expectedText = "You have a new confidential voice message from ";

        createConfigMock();
        createNoficationEmailMock(Constants.NTF_EMAIL);
        PowerMock.replayAll();
        
        Template template = new Template(
                Phrases.getTemplateStrings(language),
                notifType,
                Phrases.getCphrTemplateStrings(language, cosName),
                language);
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
    }
    
    
    //@Test
    @Ignore
    public void testTagsNotificationEmail() throws Exception {
        UserInfo user = new UserForUnitTests();        
        String notifType = "notifcationcontainer_test";
        Date date = new Date();

        createConfigMock();
        NtfEvent mockNtfEvent = createNiceMock(NtfEvent.class);
        
        NotificationEmail mockNotificationEmail = createNiceMock(NotificationEmail.class);        
        expect(mockNotificationEmail.isConfidential()).andReturn(true).anyTimes();
        expect(mockNotificationEmail.getSubject()).andReturn("Check this out").anyTimes();
        expect(mockNotificationEmail.getEmailType()).andReturn(Constants.NTF_VOICE).anyTimes();
        expect(mockNotificationEmail.getMessageLength()).andReturn("100").anyTimes();
        expect(mockNotificationEmail.getNoOfAttachments()).andReturn(2).anyTimes();
        expect(mockNotificationEmail.getMessageText()).andReturn("Hello!").anyTimes();
        expect(mockNotificationEmail.isUrgent()).andReturn(true).anyTimes();
        expect(mockNotificationEmail.getVoiceQuotaExceeded()).andReturn(true).anyTimes();
        expect(mockNotificationEmail.getMessageReceivedDate()).andReturn(date).anyTimes();
        expect(mockNotificationEmail.getReceiverPhoneNumber()).andReturn("5143457900").anyTimes();
        expect(mockNotificationEmail.getUID()).andReturn("myUID").anyTimes();
        expect(mockNotificationEmail.getNtfEvent()).andReturn(mockNtfEvent).anyTimes();
        expect(mockNtfEvent.getProperty("propName")).andReturn("propValue").anyTimes();
        PowerMock.replayAll();
        
        Template template = new Template(
                Phrases.getTemplateStrings(language),
                notifType,
                Phrases.getCphrTemplateStrings(language, cosName),
                language);
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String expectedText = "subj=Check this out from=an unknown caller payload= size=100 numAttach=2 msgText=Hello! status=urgent quota=Your voice mail quota is reached.  " +
        		"date=" + date.toString() + " phone=5143457900 type=voice priority= urgent confidential= confidential count= uid=myUID tag=propValue";
        String text = template.generateText(inbox, mockNotificationEmail, user, generateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
    }
    

    //@Test
    @Ignore
    public void testTagsNotificationInfo() throws Exception {
        UserInfo user = new UserForUnitTests();        
        String notifType = "notifcationcontainer_test";
        Date date = new Date();

        createConfigMock();
        
        MyNotifierNotificationInfo mockNotifierNotificationInfo = createNiceMock(MyNotifierNotificationInfo.class); 
        expect(mockNotifierNotificationInfo.getSenderVisibility()).andReturn(true).anyTimes();       
        expect(mockNotifierNotificationInfo.getSenderPhoneNumber()).andReturn("5147900345").anyTimes();    
        expect(mockNotifierNotificationInfo.getNotificationType()).andReturn("myNotifType").anyTimes();
        expect(mockNotifierNotificationInfo.getMessagePayloadAsString()).andReturn("This is the payload content.").anyTimes();
        expect(mockNotifierNotificationInfo.getIsUrgent()).andReturn(true).anyTimes();
        expect(mockNotifierNotificationInfo.getDate()).andReturn(date).anyTimes();
        expect(mockNotifierNotificationInfo.getReceiverPhoneNumber()).andReturn("5143457900").anyTimes();
        expect(mockNotifierNotificationInfo.getProperty("propName")).andReturn("propValue").anyTimes();
        PowerMock.replayAll();
        
        Template template = new Template(
                Phrases.getTemplateStrings(language),
                notifType,
                Phrases.getCphrTemplateStrings(language, cosName),
                language);
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String expectedText = "subj= from=5147900345 payload=This is the payload content. size= numAttach= msgText= status=urgent quota=" +
                " date=" + date.toString() + " phone=5143457900 type=My Notif Type priority= urgent confidential= count= uid= tag=propValue";
        String text = template.generateText(inbox, null, user, generateDefault, caller, mockNotifierNotificationInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
    }
    

    //@Test
    @Ignore
    public void testTagsCallerInfo() throws Exception {
        UserInfo user = new UserForUnitTests();        
        String notifType = "notifcationcontainer_test";
        Date date = new Date();

        createConfigMock();
        
        CallerInfo mockCallerInfo = createNiceMock(CallerInfo.class);     
        expect(mockCallerInfo.getNumber()).andReturn("5147900345").anyTimes(); 
        expect(mockCallerInfo.getIsSendingAsGenericNotif()).andReturn(true).anyTimes();   
        expect(mockCallerInfo.getCallTime()).andReturn(date).anyTimes();  
        expect(mockCallerInfo.getVoiceCount()).andReturn(3).anyTimes();
        PowerMock.replayAll();
        
        Template template = new Template(
                Phrases.getTemplateStrings(language),
                notifType,
                Phrases.getCphrTemplateStrings(language, cosName),
                language);
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String expectedText = "subj= from=5147900345 payload= size= numAttach= msgText= status= quota=" +
                " date=" + date.toString() + " phone= type= priority= confidential= count=3 uid= tag=";
        String text = template.generateText(inbox, null, user, generateDefault, mockCallerInfo, null);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
    }
    

    //@Test
    @Ignore
    public void testGenerateDefaultTrue() throws Exception {
        UserInfo user = new UserForUnitTests();        
        String notifType = "notifcationcontainer_test";

        createConfigMock();
        
        MyNotifierNotificationInfo mockNotifierNotificationInfo = createNiceMock(MyNotifierNotificationInfo.class); 
        expect(mockNotifierNotificationInfo.getSenderVisibility()).andReturn(true).anyTimes();       
        expect(mockNotifierNotificationInfo.getSenderPhoneNumber()).andReturn("5147900345").anyTimes();
        expect(mockNotifierNotificationInfo.getNotificationType()).andReturn("myNotifType").anyTimes();
        expect(mockNotifierNotificationInfo.getMessagePayloadAsString()).andThrow(new NotifierMfsException("Testing", NotifierMfsExceptionCause.FILE_DOES_NOT_EXIST));
        PowerMock.replayAll();
        
        Template template = new Template(
                Phrases.getTemplateStrings(language),
                notifType,
                Phrases.getCphrTemplateStrings(language, cosName),
                language);
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        boolean isGenerateDefault = true;
        String expectedText = "You have new messages.";
        String text = template.generateText(inbox, null, user, isGenerateDefault, caller, mockNotifierNotificationInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
    }

    //@Test
    @Ignore
    public void testGenerateDefaultTrue2() throws Exception {
        UserInfo user = new UserForUnitTests();        
        String notifType = "notifcationcontainer_test";

        createConfigMock();
        
        NotificationEmail mockNotificationEmail = createNiceMock(NotificationEmail.class);        
        expect(mockNotificationEmail.isConfidential()).andReturn(true).anyTimes();
        expect(mockNotificationEmail.getSubject()).andReturn("Check this out").anyTimes();
        expect(mockNotificationEmail.getEmailType()).andReturn(Constants.NTF_VOICE).anyTimes();
        expect(mockNotificationEmail.getMessageLength()).andThrow(new MsgStoreException());
        PowerMock.replayAll();
        
        Template template = new Template(
                Phrases.getTemplateStrings(language),
                notifType,
                Phrases.getCphrTemplateStrings(language, cosName),
                language);
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        boolean isGenerateDefault = true;
        String expectedText = "You have new messages.";
        String text = template.generateText(inbox, mockNotificationEmail, user, isGenerateDefault, caller, notifInfo);
        assertTrue("Result [" + text + "] not the same as expected result [" + expectedText + "]", text.equals(expectedText));
    }
    
    //@Test (expected= TemplateMessageGenerationException.class)
    @Ignore
    public void testGenerateDefaultFalse() throws Exception {
        UserInfo user = new UserForUnitTests();        
        String notifType = "notifcationcontainer_test";

        createConfigMock();
        
        MyNotifierNotificationInfo mockNotifierNotificationInfo = createNiceMock(MyNotifierNotificationInfo.class); 
        expect(mockNotifierNotificationInfo.getSenderVisibility()).andReturn(true).anyTimes();       
        expect(mockNotifierNotificationInfo.getSenderPhoneNumber()).andReturn("5147900345").anyTimes();
        expect(mockNotifierNotificationInfo.getNotificationType()).andReturn("myNotifType").anyTimes();
        expect(mockNotifierNotificationInfo.getMessagePayloadAsString()).andThrow(new NotifierMfsException("Testing", NotifierMfsExceptionCause.FILE_DOES_NOT_EXIST));
        PowerMock.replayAll();
        
        Template template = new Template(
                Phrases.getTemplateStrings(language),
                notifType,
                Phrases.getCphrTemplateStrings(language, cosName),
                language);
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        boolean isGenerateDefault = false;
        template.generateText(inbox, null, user, isGenerateDefault, caller, mockNotifierNotificationInfo);
    }
    
    
    
    private void createConfigMock() {
        mockStatic(Config.class);

        String templateResource = "templates/en.cphr";
        String phraseDir = getPathName(templateResource);
        if (phraseDir != null) {
            expect(Config.getPhraseDirectory()).andReturn(phraseDir).anyTimes();
        } else {
            fail("Could not load necessary resource [" + templateResource + "]. Check target/test-classes folder");            
        }
    }
    
    /**
     * 
     * @param depositType  The type of message that was deposited (email, voicemail, fax, etc)
     */
    private void createNoficationEmailMock(int depositType) {
        mockNotificationEmail = createNiceMock(NotificationEmail.class);
        
        expect(mockNotificationEmail.isConfidential()).andReturn(true).anyTimes();
        expect(mockNotificationEmail.isUrgent()).andReturn(false).anyTimes();
        expect(mockNotificationEmail.getSubject()).andReturn("Check this out").anyTimes();
        expect(mockNotificationEmail.getEmailType()).andReturn(depositType).anyTimes();
    }
    
    @SuppressWarnings("unused")
    private static class UserForUnitTests extends TestUser {
        String ph = "+4670000000";
        String date = "2005-01-01";
        String time = "14:20";
        
        public String getUsersDate(Date d) {return d == null ? date : d.toString();}
        public String getUsersTime(Date d) {return d == null ? time : ""+d.getTime();}
        public String getCosName() {return "cos:cos1";}
    }

    
    protected static String getPathName(String filename) {
        String pathName = null;
        java.net.URL url = TemplateTest.getFileName(filename);
        
        if (url != null) {
            File urlFile = new File(url.getFile());
            pathName = urlFile.getParent();
        }
        return pathName;
    }
    
    protected static java.net.URL getFileName(String fileName) {
        
        System.out.println("TemplateTest.getFileName : [" + fileName + "]");
        java.net.URL url = ClassLoader.getSystemResource(fileName);
        
        if (url != null) {
            System.out.println("TemplateTest.getFileName : URL for [" + fileName + "] is [" + url.getFile() + "]");
        } else {
            System.out.println("TemplateTest.getFileName : URL for [" + fileName + "] is null.");
        }
        return url;
    }
    

    private class MyNotifierNotificationInfo extends ANotifierNotificationInfo {
    }
}
