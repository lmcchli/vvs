package com.mobeon.ntf.text;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import java.io.File;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
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
import org.powermock.core.classloader.annotations.*;

@PowerMockIgnore("jdk.internal.reflect.*")
@SuppressStaticInitializationFor("com.abcxyz.messaging.mfs.MFSFactory") // Required because of a dependency problem with LibSysUtils
@RunWith(PowerMockRunner.class)
@PrepareForTest({Config.class, Template.class})
public class TemplateBytesTest {

    protected NotificationEmail mockNotificationEmail = null;
    protected String language = "en";
    protected String cosName = "cos:foobar";
    CallerInfo caller = null;
    MyNotifierNotificationInfo notifInfo = null;
    boolean generateDefault = true;


    @Before
    public void setUp()
    {
        
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
        
        TemplateBytes template = new TemplateBytes(
                notifType,
                null,
                language,
                cosName,
                "USASCII");
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String expectedText = "You have one new voice message, 2 new faxes, 3 new emails and 4 new video messages.";        
        byte[] bytes = template.generateBytes(inbox, mockNotificationEmail, user, true, caller, notifInfo);
        String byteString = new String(bytes, "US-ASCII");
        assertTrue("Result [" + byteString + "] not the same as expected result [" + expectedText + "]", byteString.equals(expectedText));
    }
    

    
    //@Test
    @Ignore
    public void testHeaderSMSNotification() throws Exception {
        UserInfo user = new UserForUnitTests();
        String notifType = "h";      // SMS notification "header"

        createConfigMock();
        createNoficationEmailMock(Constants.NTF_EMAIL);
        PowerMock.replayAll();
        
        TemplateBytes template = new TemplateBytes(
                notifType,
                null,
                language,
                cosName,
                "USASCII");
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String expectedText = "You have a new confidential# email message from an unknown caller. Message received at 14:20, 2005-01-01";
        byte[] bytes = template.generateBytes(inbox, mockNotificationEmail, user, true, caller, notifInfo);
        String byteString = new String(bytes, "US-ASCII");
        assertTrue("Result [" + byteString + "] not the same as expected result [" + expectedText + "]", byteString.equals(expectedText));
    }


    //@Test
    @Ignore
    public void testSubjectSMSlNotification() throws Exception {
        UserInfo user = new UserForUnitTests();
        String notifType = "s";  // SMS notification "subject"

        createConfigMock();
        createNoficationEmailMock(Constants.NTF_EMAIL);
        PowerMock.replayAll();
        
        TemplateBytes template = new TemplateBytes(
                notifType,
                null,
                language,
                cosName,
                "USASCII");
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String expectedText = "You have a new confidential# email message regarding \"Check this out\". Message received at 14:20, 2005-01-01";        
        //String expectedText = "You have a new confidential# email message regarding \"Wally\". Message received at 14:20, 2005-01-01";        
        byte[] bytes = template.generateBytes(inbox, mockNotificationEmail, user, true, caller, notifInfo);
        String byteString = new String(bytes, "US-ASCII");
        assertTrue("Result [" + byteString + "] not the same as expected result [" + expectedText + "]", byteString.equals(expectedText));
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
        
        TemplateBytes template = new TemplateBytes(
                notifType,
                null,
                language,
                cosName,
                "USASCII");
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String expectedText = "subj=Check this out from=an unknown caller payload= size=100 numAttach=2 msgText=Hello! status=urgent quota=Your voice mail quota is reached.  " +
                "date=" + date.toString() + " phone=5143457900 type=voice priority= urgent confidential= confidential count= uid=myUID tag=propValue";
        byte[] bytes = template.generateBytes(inbox, mockNotificationEmail, user, true, caller, notifInfo);
        String byteString = new String(bytes, "US-ASCII");
        assertTrue("Result [" + byteString + "] not the same as expected result [" + expectedText + "]", byteString.equals(expectedText));
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
        expect(mockNotifierNotificationInfo.getMessagePayloadAsBytes()).andReturn(("This is the payload content.").getBytes()).anyTimes();
        expect(mockNotifierNotificationInfo.getIsUrgent()).andReturn(true).anyTimes();
        expect(mockNotifierNotificationInfo.getDate()).andReturn(date).anyTimes();
        expect(mockNotifierNotificationInfo.getReceiverPhoneNumber()).andReturn("5143457900").anyTimes();
        expect(mockNotifierNotificationInfo.getProperty("propName")).andReturn("propValue").anyTimes();
        PowerMock.replayAll();
        
        TemplateBytes template = new TemplateBytes(
                notifType,
                null,
                language,
                cosName,
                "USASCII");
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String expectedText = "subj= from=5147900345 payload=This is the payload content. size= numAttach= msgText= status=urgent quota=" +
                " date=" + date.toString() + " phone=5143457900 type=My Notif Type priority= urgent confidential= count= uid= tag=propValue";
        byte[] bytes = template.generateBytes(inbox, null, user, true, caller, mockNotifierNotificationInfo);
        String byteString = new String(bytes, "US-ASCII");
        assertTrue("Result [" + byteString + "] not the same as expected result [" + expectedText + "]", byteString.equals(expectedText));
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
        
        TemplateBytes template = new TemplateBytes(
                notifType,
                null,
                language,
                cosName,
                "USASCII");
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        String expectedText = "subj= from=5147900345 payload= size= numAttach= msgText= status= quota=" +
                " date=" + date.toString() + " phone= type= priority= confidential= count=3 uid= tag=";
        byte[] bytes = template.generateBytes(inbox, null, user, true, mockCallerInfo, null);
        String byteString = new String(bytes, "US-ASCII");
        assertTrue("Result [" + byteString + "] not the same as expected result [" + expectedText + "]", byteString.equals(expectedText));
    }

    
    //@Test
    @Ignore
    public void testGenerateDefaultTrue() throws Exception {
        UserInfo user = new UserForUnitTests();        
        String notifType = "notifcationcontainer_test";
        Date date = new Date();

        createConfigMock();
        
        MyNotifierNotificationInfo mockNotifierNotificationInfo = createNiceMock(MyNotifierNotificationInfo.class); 
        expect(mockNotifierNotificationInfo.getSenderVisibility()).andReturn(true).anyTimes();       
        expect(mockNotifierNotificationInfo.getSenderPhoneNumber()).andReturn("5147900345").anyTimes();
        expect(mockNotifierNotificationInfo.getNotificationType()).andReturn("myNotifType").anyTimes();
        expect(mockNotifierNotificationInfo.getMessagePayloadAsBytes()).andThrow(new NotifierMfsException("Testing", NotifierMfsExceptionCause.FILE_DOES_NOT_EXIST));;
        expect(mockNotifierNotificationInfo.getIsUrgent()).andReturn(true).anyTimes();
        expect(mockNotifierNotificationInfo.getDate()).andReturn(date).anyTimes();
        expect(mockNotifierNotificationInfo.getReceiverPhoneNumber()).andReturn("5143457900").anyTimes();
        expect(mockNotifierNotificationInfo.getProperty("propName")).andReturn("propValue").anyTimes();
        PowerMock.replayAll();
        
        TemplateBytes template = new TemplateBytes(
                notifType,
                null,
                language,
                cosName,
                "USASCII");
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        boolean isGenerateDefault = true;
        String expectedText = "You have new messages.";
        byte[] bytes = template.generateBytes(inbox, null, user, isGenerateDefault, caller, mockNotifierNotificationInfo);
        String byteString = new String(bytes, "US-ASCII");
        assertTrue("Result [" + byteString + "] not the same as expected result [" + expectedText + "]", byteString.equals(expectedText));
    }

    
    //@Test
    @Ignore
    public void testGenerateDefaultTrue2() throws Exception {
        UserInfo user = new UserForUnitTests();        
        String notifType = "notifcationcontainer_test";
        Date date = new Date();

        createConfigMock();
        NtfEvent mockNtfEvent = createNiceMock(NtfEvent.class);
        
        NotificationEmail mockNotificationEmail = createNiceMock(NotificationEmail.class);        
        expect(mockNotificationEmail.isConfidential()).andReturn(true).anyTimes();
        expect(mockNotificationEmail.getSubject()).andReturn("Check this out").anyTimes();
        expect(mockNotificationEmail.getEmailType()).andReturn(Constants.NTF_VOICE).anyTimes();
        expect(mockNotificationEmail.getMessageLength()).andThrow(new MsgStoreException());
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
        
        TemplateBytes template = new TemplateBytes(
                notifType,
                null,
                language,
                cosName,
                "USASCII");
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        boolean isGenerateDefault = true;
        String expectedText = "You have new messages.";
        byte[] bytes = template.generateBytes(inbox, mockNotificationEmail, user, isGenerateDefault, caller, notifInfo);
        String byteString = new String(bytes, "US-ASCII");
        assertTrue("Result [" + byteString + "] not the same as expected result [" + expectedText + "]", byteString.equals(expectedText));
    }

    
    //@Test (expected= TemplateMessageGenerationException.class)
    @Ignore
    public void testGenerateDefaultFalse() throws Exception {
        UserInfo user = new UserForUnitTests();        
        String notifType = "notifcationcontainer_test";
        Date date = new Date();

        createConfigMock();
        
        MyNotifierNotificationInfo mockNotifierNotificationInfo = createNiceMock(MyNotifierNotificationInfo.class); 
        expect(mockNotifierNotificationInfo.getSenderVisibility()).andReturn(true).anyTimes();       
        expect(mockNotifierNotificationInfo.getSenderPhoneNumber()).andReturn("5147900345").anyTimes();
        expect(mockNotifierNotificationInfo.getNotificationType()).andReturn("myNotifType").anyTimes();
        expect(mockNotifierNotificationInfo.getMessagePayloadAsBytes()).andThrow(new NotifierMfsException("Testing", NotifierMfsExceptionCause.FILE_DOES_NOT_EXIST));;
        expect(mockNotifierNotificationInfo.getIsUrgent()).andReturn(true).anyTimes();
        expect(mockNotifierNotificationInfo.getDate()).andReturn(date).anyTimes();
        expect(mockNotifierNotificationInfo.getReceiverPhoneNumber()).andReturn("5143457900").anyTimes();
        expect(mockNotifierNotificationInfo.getProperty("propName")).andReturn("propValue").anyTimes();
        PowerMock.replayAll();
        
        TemplateBytes template = new TemplateBytes(
                notifType,
                null,
                language,
                cosName,
                "USASCII");
        
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0,0,0,0,false);
        boolean isGenerateDefault = false;
        template.generateBytes(inbox, null, user, isGenerateDefault, caller, mockNotifierNotificationInfo);        
    }

    
    
    
    private void createConfigMock() {
        mockStatic(Config.class);

        String templateResource = "templates/en-c-USASCII.cphr";
        String phraseDir = getPathName(templateResource);
        if (phraseDir != null) {
            expect(Config.getPhraseDirectory()).andReturn(phraseDir).anyTimes();
        } else {
            fail("Could not load necessary resource [" + templateResource + "]. Check target/test-classes folder");
        }
    }
    
    /**
     * 
     * @param depType  The type of message that was deposited (email, voicemail, fax, etc)
     */
    private void createNoficationEmailMock(int depType) {
        mockNotificationEmail = createNiceMock(NotificationEmail.class);
        
        expect(mockNotificationEmail.isConfidential()).andReturn(true).anyTimes();
        expect(mockNotificationEmail.isUrgent()).andReturn(false).anyTimes();
        expect(mockNotificationEmail.getSubject()).andReturn("Check this out").anyTimes();
        expect(mockNotificationEmail.getEmailType()).andReturn(depType).anyTimes();
        expect(mockNotificationEmail.getSender()).andReturn("Wally").anyTimes();
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
        java.net.URL url = TemplateBytesTest.getFileName(filename);
        
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
