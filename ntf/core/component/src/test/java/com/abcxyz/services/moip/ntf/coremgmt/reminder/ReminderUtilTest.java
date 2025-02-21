package com.abcxyz.services.moip.ntf.coremgmt.reminder;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import com.abcxyz.messaging.common.message.CodingFailureException;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.userinfo.EmailFilterInfo;
import com.mobeon.ntf.userinfo.MmsFilterInfo;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.OdlFilterInfo;
import com.mobeon.ntf.userinfo.SIPFilterInfo;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.UserInfo.NotifState;

public class ReminderUtilTest {
    
    private static NotificationEmail email;
    private static ConfigManager mockConfigManager;
    
    @BeforeClass
    public static void setUp() throws MsgStoreException, CodingFailureException {        
        NtfEvent reminderEvent = new NtfEvent("tn0/20101021-14h20/144_r9007fbb3610f0-0.ntf-reminder;try=1;exp=1287685644547;rmsa=msid:796620cf67f92a96;RecipientId=5143457906;reminder=1");
        email = new NotificationEmail(reminderEvent);
        email.init();
        
        //ConfigManager mock object needed for instantiation of NotificationFilter object
        mockConfigManager = createNiceMock(ConfigManager.class);
        Config.injectNtfConfigManager(mockConfigManager);        
        expect(mockConfigManager.getParameter(NotificationConfigConstants.DEFAULT_NOTIFICATION_FILTER)).andReturn("1;n;a;evfm;;;997;;;;;OFF;;").anyTimes();
        expect(mockConfigManager.getParameter(NotificationConfigConstants.DEFAULT_NOTIFICATION_FILTER_2)).andReturn("1;y;a;s;SMS,EML;slamdown,slamdown;998;;;;;SLAMDOWN;;").anyTimes();
        replay(mockConfigManager);        
        verify(mockConfigManager);
    }
    
    private void reset(){
        email.setEmailType(-1);
    }
    
    @Test
    public void testNoNewMessages(){
        UserMailbox inbox = new UserMailbox(0, 0, 0, 0, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        //No calls to NotificationFilter object expected.
        replay(mockNotificationFilter);
        
        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_SMS, email, inbox, mockNotificationFilter,null);
        
        assertTrue(filterInfo == null);
        verify(mockNotificationFilter);
        reset();
    }

    @Test
    public void testNewVoiceMessageSmsNotifTypeAllowed(){
        UserMailbox inbox = new UserMailbox(1, 0, 3, 0, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        expect(mockNotificationFilter.getSmsFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(new SmsFilterInfo(new Properties(), null, null));
        replay(mockNotificationFilter);
        
        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_SMS, email, inbox, mockNotificationFilter,null);
        
        assertTrue(filterInfo instanceof SmsFilterInfo);
        assertTrue(email.getEmailType() == Constants.NTF_VOICE);        
        verify(mockNotificationFilter);
        reset();
    }

    @Test
    public void testNewVoiceMessageSmsNotifTypeNotAllowed(){
        UserMailbox inbox = new UserMailbox(12, 0, 0, 0, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        expect(mockNotificationFilter.getSmsFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        replay(mockNotificationFilter);
        
        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_SMS, email, inbox, mockNotificationFilter,null);
        
        assertTrue(filterInfo == null);    
        assertTrue(email.getEmailType() == Constants.NTF_VOICE);    
        verify(mockNotificationFilter);
        reset();
    }

    @Test
    public void testNewEmailMessageMmsNotifTypeAllowed(){
        UserMailbox inbox = new UserMailbox(0, 0, 3, 0, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        expect(mockNotificationFilter.getMmsFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(new MmsFilterInfo(null));
        replay(mockNotificationFilter);
        
        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_MMS, email, inbox, mockNotificationFilter,null);

        assertTrue(filterInfo instanceof MmsFilterInfo);
        assertTrue(email.getEmailType() == Constants.NTF_EMAIL);       
        verify(mockNotificationFilter);
        reset();
    }

    @Test
    public void testNewEmailMessageMmsNotifTypeNotAllowed(){
        UserMailbox inbox = new UserMailbox(0, 0, 10, 0, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        expect(mockNotificationFilter.getMmsFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        replay(mockNotificationFilter);
        
        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_MMS, email, inbox, mockNotificationFilter,null);
        
        assertTrue(filterInfo == null);   
        assertTrue(email.getEmailType() == Constants.NTF_EMAIL);            
        verify(mockNotificationFilter);
        reset();
    }

    @Test
    public void testNewFaxMessageEmailNotifTypeAllowed(){
        UserMailbox inbox = new UserMailbox(0, 5, 0, 0, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        expect(mockNotificationFilter.getEmailFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(new EmailFilterInfo(new Properties(), null));
        replay(mockNotificationFilter);
        
        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_EML, email, inbox, mockNotificationFilter,null);
        
        assertTrue(filterInfo instanceof EmailFilterInfo);
        assertTrue(email.getEmailType() == Constants.NTF_FAX);
        verify(mockNotificationFilter);
        reset();
    }

    @Test
    public void testNewFaxMessageEmailNotifTypeNotAllowed(){
        UserMailbox inbox = new UserMailbox(0, 1, 0, 0, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        expect(mockNotificationFilter.getEmailFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        replay(mockNotificationFilter);
        
        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_EML, email, inbox, mockNotificationFilter,null);
        
        assertTrue(filterInfo == null);    
        assertTrue(email.getEmailType() == Constants.NTF_FAX);    
        verify(mockNotificationFilter);
        reset();
    }

    @Test
    public void testNewVideoMessageSipMwiNotifTypeAllowed(){
        UserMailbox inbox = new UserMailbox(0, 0, 0, 8, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        expect(mockNotificationFilter.getSIPFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(new SIPFilterInfo(null));
        replay(mockNotificationFilter);

        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_SIPMWI, email, inbox, mockNotificationFilter,null);

        assertTrue(filterInfo instanceof SIPFilterInfo);
        assertTrue(email.getEmailType() == Constants.NTF_VIDEO);
        verify(mockNotificationFilter);
        reset();
    }

    @Test
    public void testNewVideoMessageSipMwiNotifTypeNotAllowed(){
        UserMailbox inbox = new UserMailbox(0, 0, 0, 9, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        expect(mockNotificationFilter.getSIPFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        replay(mockNotificationFilter);

        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_SIPMWI, email, inbox, mockNotificationFilter,null);
         
        assertTrue(filterInfo == null);    
        assertTrue(email.getEmailType() == Constants.NTF_VIDEO);    
        verify(mockNotificationFilter);
        reset();
    }

    @Test
    public void testNewAllTypesMessageOutdialNotifTypeAllowedForVideoMessageOnly(){
        UserMailbox inbox = new UserMailbox(1, 2, 3, 4, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        expect(mockNotificationFilter.getOdlFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        expect(mockNotificationFilter.getOdlFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        expect(mockNotificationFilter.getOdlFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        expect(mockNotificationFilter.getOdlFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(new OdlFilterInfo(new Properties(), null));
        replay(mockNotificationFilter);

        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_ODL, email, inbox, mockNotificationFilter,null);

        assertTrue(filterInfo instanceof OdlFilterInfo);
        assertTrue(email.getEmailType() == Constants.NTF_VIDEO);
        verify(mockNotificationFilter);
        reset();
    }

    @Test
    public void testNewAllTypesMessageOutdialNotifTypeNotAllowed(){
        UserMailbox inbox = new UserMailbox(9, 8, 7, 6, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        expect(mockNotificationFilter.getOdlFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        expect(mockNotificationFilter.getOdlFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        expect(mockNotificationFilter.getOdlFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        expect(mockNotificationFilter.getOdlFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        replay(mockNotificationFilter);

        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_ODL, email, inbox, mockNotificationFilter,null);

        assertTrue(filterInfo == null);  
        assertTrue(email.getEmailType() == Constants.NTF_VIDEO);    
        verify(mockNotificationFilter);
        reset();
    }

    @Test
    public void testNewVoiceVideoMessageOutdialNotifTypeAllowedForFaxVideoMessageOnly(){
        UserMailbox inbox = new UserMailbox(1, 0, 0, 4, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        expect(mockNotificationFilter.getOdlFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        expect(mockNotificationFilter.getOdlFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(new OdlFilterInfo(new Properties(), null));
        replay(mockNotificationFilter);

        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_ODL, email, inbox, mockNotificationFilter,null);

        assertTrue(filterInfo instanceof OdlFilterInfo);
        assertTrue(email.getEmailType() == Constants.NTF_VIDEO);
        verify(mockNotificationFilter);
        reset();
    }

    @Test
    public void testNewVoiceVideoMessageOutdialNotifTypeAllowedForFaxMessageOnly(){
        UserMailbox inbox = new UserMailbox(1, 0, 0, 4, 0, 0, 0, 0, false);
        NotificationFilter mockNotificationFilter = createMock(NotificationFilter.class);
        expect(mockNotificationFilter.getOdlFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        expect(mockNotificationFilter.getOdlFilterInfo((NotificationEmail)anyObject(), (GregorianCalendar)anyObject(), (NotificationGroup)anyObject())).andReturn(null);
        replay(mockNotificationFilter);

        Object filterInfo = ReminderUtil.getReminderFilterInfo(Constants.NTF_ODL, email, inbox, mockNotificationFilter,null);

        assertTrue(filterInfo == null); 
        assertTrue(email.getEmailType() == Constants.NTF_VIDEO);
        verify(mockNotificationFilter);
        reset();
    }

    
    /*
     * The following tests verifies that the NotificationEmail object for a reminder will work in retrieving filter info as expected.
     * The NotificationEmail object for a reminder does not have real message-specific values (since reminders are mailbox-specific).
     * The expectation is that a filter matches if it is at the message type level (no specific "from" or "subject").
     */
    
    @Test
    public void testNewVoiceMessageNotifFilterNoFrom(){
        //Test case: New voice message in mailbox and user's filter has a match for no from.
        UserInfo mockUser = createMock(UserInfo.class);
        boolean isAllNotifDisabled = false;
        String[] filterSpec = setfilter("filter1", 1, true, true, "a", "evf", "MWI,SMS", "c", "", "", false, "");
        String[] deliveryProfile = {"111,222;SMS,ODL;",
                "333;MWI,WAP,MMS;M",
                "444;MWI;I",
                "555,666;MWI,ODL;F"};
        GregorianCalendar receivedDate = new GregorianCalendar();
        int emailType = Constants.NTF_VOICE;
        int notifType = Constants.NTF_SMS;
        email.setEmailType(emailType);
        expect(mockUser.getLogin()).andReturn((String)anyObject());
        expect(mockUser.isNotificationServiceEnabled(notifType)).andReturn(true);
        expect(mockUser.getFilter().isNotifTypeDisabledOnUser(notifType,null)).andReturn(NotifState.ENABLED);
        expect(mockUser.isNotificationServiceEnabled(Constants.NTF_MWI)).andReturn(true);
        expect(mockUser.getFilter().isNotifTypeDisabledOnUser(Constants.NTF_MWI,null)).andReturn(NotifState.ENABLED);        
        replay(mockUser);

        //The expectation is that a match will be found.
        NotificationFilter notifFilter = new NotificationFilter(filterSpec, isAllNotifDisabled, mockUser, deliveryProfile);
        System.out.println(notifFilter.toString());
        assertTrue(notifFilter.getSmsFilterInfo(email, receivedDate, null) != null);
        
        verify(mockUser);
    }

    @Test
    public void testNewVoiceMessageNotifFilterHasFrom(){
        //Test case: New voice message in mailbox and user's filter has only a match for a specific from (5143457900).
        UserInfo mockUser = createMock(UserInfo.class);
        boolean isAllNotifDisabled = false;
        String[] filterSpec = setfilter("filter1", 1, true, true, "a", "evf", "MWI,SMS", "c", "", "", false, "5143457900");
        String[] deliveryProfile = {"111,222;SMS,ODL;",
                "333;MWI,WAP,MMS;M",
                "444;MWI;I",
                "555,666;MWI,ODL;F"};
        GregorianCalendar receivedDate = new GregorianCalendar();
        int emailType = Constants.NTF_VOICE;
        int notifType = Constants.NTF_SMS;
        email.setEmailType(emailType);
        expect(mockUser.getLogin()).andReturn((String)anyObject());
        expect(mockUser.isNotificationServiceEnabled(notifType)).andReturn(true);      
        replay(mockUser);

        //The expectation is that no match will be found and the default match which has notify=false is used.
        NotificationFilter notifFilter = new NotificationFilter(filterSpec, isAllNotifDisabled, mockUser, deliveryProfile);
        System.out.println(notifFilter.toString());
        assertTrue(notifFilter.getSmsFilterInfo(email, receivedDate, null) == null);
        
        verify(mockUser);
    }

    @Test
    public void testNewEmailMessageNotifFilterNoFromNoSubject(){
        //Test case: New email message in mailbox and user's filter has a match for no from and no subject.
        UserInfo mockUser = createMock(UserInfo.class);
        boolean isAllNotifDisabled = false;
        String[] filterSpec = setfilter("filter1", 1, true, true, "a", "evf", "MWI,SMS", "c,c", "", "", false, "");
        String[] deliveryProfile = {"111,222;MWI,ODL;",
                "333;MWI,WAP,MMS;M",
                "444;MWI;I",
                "555,666;MWI,ODL;F"};
        GregorianCalendar receivedDate = new GregorianCalendar();
        int emailType = Constants.NTF_EMAIL;
        int notifType = Constants.NTF_SMS;
        email.setEmailType(emailType);
        expect(mockUser.getLogin()).andReturn((String)anyObject());
        expect(mockUser.isNotificationServiceEnabled(notifType)).andReturn(true);
        expect(mockUser.getFilter().isNotifTypeDisabledOnUser(notifType,null)).andReturn(NotifState.DISABLED);
        expect(mockUser.getNotifNumber()).andReturn("").anyTimes();
        expect(mockUser.isNotificationServiceEnabled(Constants.NTF_MWI)).andReturn(true);
        expect(mockUser.getFilter().isNotifTypeDisabledOnUser(Constants.NTF_MWI,null)).andReturn(NotifState.ENABLED);
        replay(mockUser);

        //The expectation is that a match will be found.
        NotificationFilter notifFilter = new NotificationFilter(filterSpec, isAllNotifDisabled, mockUser, deliveryProfile);
        assertTrue(notifFilter.getSmsFilterInfo(email, receivedDate, null) != null);
        
        verify(mockUser);
    }

    @Test
    public void testNewEmailMessageNotificationFilterHasFrom(){
        //Test case: New email message in mailbox and user's filter has only a match for a specific from (5143457900).
        UserInfo mockUser = createMock(UserInfo.class);
        boolean isAllNotifDisabled = false;
        String[] filterSpec = setfilter("filter1", 1, true, true, "a", "evf", "MWI,SMS", "c,c", "5143457900", "", false, "");
        String[] deliveryProfile = {"111,222;MWI,ODL;",
                "333;MWI,WAP,MMS;M",
                "444;MWI;I",
                "555,666;MWI,ODL;F"};
        GregorianCalendar receivedDate = new GregorianCalendar();
        int emailType = Constants.NTF_EMAIL;
        int notifType = Constants.NTF_SMS;
        email.setEmailType(emailType);
        expect(mockUser.getLogin()).andReturn((String)anyObject());
        expect(mockUser.isNotificationServiceEnabled(notifType)).andReturn(true);        
        replay(mockUser);

        //The expectation is that no match will be found and the default match which has notify=false is used.
        NotificationFilter notifFilter = new NotificationFilter(filterSpec, isAllNotifDisabled, mockUser, deliveryProfile);
        System.out.println(notifFilter.toString());
        assertTrue(notifFilter.getSmsFilterInfo(email, receivedDate, null) == null);
        
        verify(mockUser);
    }

    @Test
    public void testNewEmailMessageNotificationFilterHasSubject(){
        //Test case: New email message in mailbox and user's filter has only a match for a specific subject.
        UserInfo mockUser = createMock(UserInfo.class);
        boolean isAllNotifDisabled = false;
        String[] filterSpec = setfilter("filter1", 1, true, true, "a", "evf", "MWI", "c", "", "emailsubject1", false, "");
        String[] deliveryProfile = {"111,222;MWI,ODL;",
                "333;MWI,WAP,MMS;M",
                "444;MWI;I",
                "555,666;MWI,ODL;F"};
        GregorianCalendar receivedDate = new GregorianCalendar();
        int emailType = Constants.NTF_EMAIL;
        int notifType = Constants.NTF_SMS;
        email.setEmailType(emailType);
        expect(mockUser.getLogin()).andReturn((String)anyObject());
        expect(mockUser.isNotificationServiceEnabled(notifType)).andReturn(true);        
        replay(mockUser);

        //The expectation is that no match will be found and the default match which has notify=false is used.
        NotificationFilter notifFilter = new NotificationFilter(filterSpec, isAllNotifDisabled, mockUser, deliveryProfile);
        assertTrue(notifFilter.getSmsFilterInfo(email, receivedDate, null) == null);
        
        verify(mockUser);
    }
    
    private String[] setfilter(String name,
            int prio,
            boolean active,
            boolean notify,
            String time,
            String depType,
            String notifType,
            String content,
            String from,
            String subject,
            boolean urgent,
            String voiceFaxFrom) {
        String[] filterSpec = {"Test"};
        Properties fSpec = new Properties();
        fSpec.setProperty(name, ""
                + (active? "1": "0") + ";"
                + (notify? "y": "n") + ";"
                + time + ";"
                + depType + ";"
                + notifType + ";"
                + content + ";"
                + prio + ";"
                + from + ";"
                + subject + ";"
                + (urgent? "y": "") + ";"
                + voiceFaxFrom + ";"
                + name + ";"
                + ";");
        filterSpec= (fSpec.values().toArray(filterSpec));
        return filterSpec;
    }
}
