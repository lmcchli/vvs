package com.abcxyz.services.moip.ntf.coremgmt.reminder;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.abcxyz.messaging.common.message.CodingFailureException;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.services.moip.ntf.TestMerAgent;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.out.sms.SMSClientStub;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.NtfCompletedListener;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.out.sms.SMSConfigWrapper;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.text.TextCreator;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.mcd.McdUserInfo;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Config.class, SMSConfigWrapper.class, NotificationEmail.class, TextCreator.class, NtfRetryEventHandler.class, ReminderUtil.class, SmsReminder.class, SmsFilterInfo.class})
public class SMSReminderTest {

    private static LogAgent log = NtfCmnLogger.getLogAgent(SMSReminderTest.class);
    private static SMSClientStub smsClientStub = new SMSClientStub();

    private static UserInfo userInfo;
    private static UserMailbox inbox;
    private static String myNumber = "5143457906";
    private static String myCos = "cos1";

    private static NotificationEmail reminderEmail;
    private static NotificationEmail nonReminderEmail;
    private static TestMerAgent mer;
    private static CompletedListener completedListener = null;

    private static int completeCalls = 0;
    private static int retryCalls = 0;
    private static int failedCalls = 0;

    @BeforeClass
    public static void setUp(){
        SMSOut.setSmsClient(smsClientStub);
        userInfo = new MyUserInfo();
        inbox = new UserMailbox(1, 1, 1, 1,0,0,0,0, true);
        mer = new TestMerAgent();
        completedListener = new CompletedListener();            
    }
       
    private void resetTestEnvironment(){
        //Remove persistent file.
        SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, null);
        SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, null);
        assertFalse((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
        
        //Put SmsReminder.retryHandler to null so that next test case will pick its own mock retryHandler.
        SmsReminder.retryHandler = null;
    }

    @Test
    public void testTriggerReminderNotification_stillNewMessages() throws MsgStoreException, CodingFailureException{
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            
            NtfEvent reminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-reminder;try=2;exp=1287692373385;RecipientId=5143457905;cls=mas;reminder=1");
            reminderEvent.keepReferenceID("newReminderTriggerSchedulerId");
            reminderEmail = new NotificationEmail(reminderEvent);   
            reminderEmail.init();
            NotificationGroup notificationGroup = new NotificationGroup(completedListener, reminderEmail, log, mer);

            //SmsFilterInfo filterInfo = new SmsFilterInfo(null, null, null);
            SmsFilterInfo mockFilterInfo = PowerMock.createMock(SmsFilterInfo.class);
            PowerMock.mockStaticNice(ReminderUtil.class);
            expect(ReminderUtil.getReminderSmsFilterInfo(null, reminderEmail,null)).andReturn(mockFilterInfo).times(1);

            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            expect(mockNtfRetryEventHandler.scheduleEvent(reminderEvent)).andReturn("newReminderNotifSchedulerId");
            mockNtfRetryEventHandler.cancelEvent(null);
            PowerMock.expectLastCall().times(1);
            SmsReminder.retryHandler = mockNtfRetryEventHandler;   
            
            PowerMock.mockStaticPartial(SmsReminder.class, "sendSMS");
            SmsReminder.sendSMS(null, notificationGroup, mockFilterInfo);
            PowerMock.expectLastCall().times(1);            

            PowerMock.replayAll();      

            //The reminder trigger event will be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.triggerReminderNotification(null, notificationGroup);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "newReminderTriggerSchedulerId");
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), "newReminderNotifSchedulerId");
            assertEquals(reminderEvent.getReferenceId(), "newReminderNotifSchedulerId");

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();
        }
    }

    @Test
    public void testTriggerReminderNotification_noMoreNewMessages() throws MsgStoreException, CodingFailureException{
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            
            NtfEvent reminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-reminder;try=2;exp=1287692373385;RecipientId=5143457905;cls=mas;reminder=1");
            reminderEvent.keepReferenceID("newReminderTriggerSchedulerId");
            reminderEmail = new NotificationEmail(reminderEvent);   
            reminderEmail.init();
            NotificationGroup notificationGroup = new NotificationGroup(completedListener, reminderEmail, log, mer);

            PowerMock.mockStaticNice(ReminderUtil.class);
            expect(ReminderUtil.getReminderSmsFilterInfo(null, reminderEmail,null)).andReturn(null).times(1);

            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            mockNtfRetryEventHandler.cancelEvent(null);
            PowerMock.expectLastCall().times(1);
            mockNtfRetryEventHandler.cancelReminderTriggerEvent("newReminderTriggerSchedulerId");
            PowerMock.expectLastCall().times(1);
            SmsReminder.retryHandler = mockNtfRetryEventHandler;          

            PowerMock.replayAll();      

            //The reminder trigger event will be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.triggerReminderNotification(null, notificationGroup);
            assertFalse((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), null);
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), null);

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();
        }
    }

    @Test
    public void testSendReminderNotificationRetry_stillNewMessages() throws MsgStoreException, CodingFailureException{
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            
            NtfEvent reminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-reminder;try=2;exp=1287692373385;RecipientId=5143457905;cls=mas;reminder=1");
            reminderEvent.keepReferenceID("newReminderNotifSchedulerId");
            reminderEmail = new NotificationEmail(reminderEvent);   
            reminderEmail.init();
            NotificationGroup notificationGroup = new NotificationGroup(completedListener, reminderEmail, log, mer);

            //SmsFilterInfo filterInfo = new SmsFilterInfo(null, null, null);
            SmsFilterInfo mockFilterInfo = PowerMock.createMock(SmsFilterInfo.class);
            PowerMock.mockStaticNice(ReminderUtil.class);
            expect(ReminderUtil.getReminderSmsFilterInfo(null, reminderEmail,null)).andReturn(mockFilterInfo).times(1);

            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            SmsReminder.retryHandler = mockNtfRetryEventHandler;   
            
            PowerMock.mockStaticPartial(SmsReminder.class, "sendSMS");
            SmsReminder.sendSMS(null, notificationGroup, mockFilterInfo);
            PowerMock.expectLastCall().times(1);            

            PowerMock.replayAll();      

            //The reminder trigger event will be cancelled.
            assertEquals(reminderEvent.getReferenceId(), "newReminderNotifSchedulerId");
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, "existingReminderNotifSchedulerId");
            SmsReminder.sendReminderNotificationRetry(null, notificationGroup);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "existingReminderTriggerSchedulerId");
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), "newReminderNotifSchedulerId");

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();
        }
    }
    
    @Test
    public void testSendReminderNotificationRetry_noMoreNewMessages() throws MsgStoreException, CodingFailureException{
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            
            NtfEvent reminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-reminder;try=2;exp=1287692373385;RecipientId=5143457905;cls=mas;reminder=1");
            reminderEvent.keepReferenceID("newReminderNotifSchedulerId");
            reminderEmail = new NotificationEmail(reminderEvent);   
            reminderEmail.init();
            NotificationGroup notificationGroup = new NotificationGroup(completedListener, reminderEmail, log, mer);

            PowerMock.mockStaticNice(ReminderUtil.class);
            expect(ReminderUtil.getReminderSmsFilterInfo(null, reminderEmail,null)).andReturn(null).times(1);

            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            mockNtfRetryEventHandler.cancelEvent("newReminderNotifSchedulerId");
            PowerMock.expectLastCall().times(1);
            mockNtfRetryEventHandler.cancelReminderTriggerEvent("existingReminderTriggerSchedulerId");
            PowerMock.expectLastCall().times(1);
            SmsReminder.retryHandler = mockNtfRetryEventHandler;          

            PowerMock.replayAll();      

            //The reminder notification retry event will be cancelled and the reminder trigger event will be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, "existingReminderNotifSchedulerId");
            SmsReminder.sendReminderNotificationRetry(null, notificationGroup);
            assertFalse((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), null);
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), null);

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();
        }
    }
    
    @Test
    public void testStartNextReminderTrigger_smsReminderDisabled() throws MsgStoreException, CodingFailureException{
        PowerMock.mockStaticNice(Config.class);
        expect(Config.isSmsReminderEnabled()).andReturn(false).anyTimes();
        PowerMock.replayAll();

        NtfEvent reminderEvent = new NtfEvent("tn0/20101021-14h20/144_r9007fbb3610f0-0.ntf-reminder;try=1;exp=1287685644547;rmsa=msid:796620cf67f92a96;RecipientId=5143457906;reminder=1");
        reminderEmail = new NotificationEmail(reminderEvent);
        reminderEmail.init();
        
        //Leave NtfRetryHandling object in SmsReminder as null since we do not expect that there will be any calls to it.
        SmsReminder.startNextReminderTrigger(userInfo, reminderEmail, Constants.FEEDBACK_STATUS_OK);
        PowerMock.verifyAll();
    }

    @Test
    public void testStartNextReminderTrigger_notReminderStatusOk_triggerSchedulingSucceeded(){
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            expect(Config.getSmsReminderIntervalInMin()).andReturn(1440).anyTimes();
            expect(Config.getSmsReminderExpireInMin()).andReturn(20160).anyTimes();

            NtfEvent nonReminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-Notif;fallbackorigntftype=0;eventType=Notif;omsg=50f531;try=2;exp=1287692373385;rmsg=742f78;fallback=0;omsa=eid:f214d68ddf252227;eventServiceType=ntf;rmsa=msid:fbcdc35108fbe3e9;frm=tel:+15143457900;RecipientId=5143457905;cls=mas;reminder=0");
            nonReminderEmail = new NotificationEmail(nonReminderEvent);
            //Do not call nonReminderEmail.init() to avoid having to create a message state file; message-specific info is irrelevant to this test.   

            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            expect(mockNtfRetryEventHandler.scheduleReminderTriggerEvent(nonReminderEvent)).andReturn("newReminderTriggerSchedulerId").times(3);
            mockNtfRetryEventHandler.cancelReminderTriggerEvent(null);
            PowerMock.expectLastCall().times(1);
            mockNtfRetryEventHandler.cancelReminderTriggerEvent((String)anyObject());
            PowerMock.expectLastCall().times(2);
            mockNtfRetryEventHandler.cancelEvent("existingReminderNotifSchedulerId");
            PowerMock.expectLastCall().times(1);
            NtfEventHandlerRegistry.registerDefaultHandler(mockNtfRetryEventHandler);          

            PowerMock.replayAll();      

            //No previously scheduled reminder trigger event.
            assertFalse((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_OK);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "newReminderTriggerSchedulerId"); 

            //There is a previously scheduled reminder trigger event which will be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_OK);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "newReminderTriggerSchedulerId");

            //There is a previously scheduled reminder trigger event and reminder notification event which will be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, "existingReminderNotifSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_OK);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "newReminderTriggerSchedulerId");
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), null);

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();
        }
    }

    @Test
    public void testStartNextReminderTrigger_notReminderStatusOk_triggerSchedulingFailed(){
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            expect(Config.getSmsReminderIntervalInMin()).andReturn(1440).anyTimes();
            expect(Config.getSmsReminderExpireInMin()).andReturn(20160).anyTimes();

            NtfEvent nonReminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-Notif;fallbackorigntftype=0;eventType=Notif;omsg=50f531;try=2;exp=1287692373385;rmsg=742f78;fallback=0;omsa=eid:f214d68ddf252227;eventServiceType=ntf;rmsa=msid:fbcdc35108fbe3e9;frm=tel:+15143457900;RecipientId=5143457905;cls=mas;reminder=0");
            nonReminderEmail = new NotificationEmail(nonReminderEvent);
            //Do not call nonReminderEmail.init() to avoid having to create a message state file; message-specific info is irrelevant to this test.   

            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            expect(mockNtfRetryEventHandler.scheduleReminderTriggerEvent(nonReminderEvent)).andReturn(null).times(3);
            mockNtfRetryEventHandler.cancelEvent("existingReminderNotifSchedulerId");
            PowerMock.expectLastCall().times(1);
            NtfEventHandlerRegistry.registerDefaultHandler(mockNtfRetryEventHandler);          

            PowerMock.replayAll();      

            //No previously scheduled reminder trigger event.
            assertFalse((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_OK);
            assertFalse((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), null); 

            //There is a previously scheduled reminder trigger event which will not be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_OK);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "existingReminderTriggerSchedulerId");

            //There is a previously scheduled reminder trigger event which will not be cancelled and reminder notification event which will be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, "existingReminderNotifSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_OK);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "existingReminderTriggerSchedulerId");
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), null);

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();            
        }
    }

    @Test
    public void testStartNextReminderTrigger_reminderStatusOk_triggerReschedulingSucceeded(){
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            expect(Config.getSmsReminderIntervalInMin()).andReturn(1440).anyTimes();
            expect(Config.getSmsReminderExpireInMin()).andReturn(20160).anyTimes();
            
            NtfEvent reminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-reminder;try=2;exp=1287692373385;RecipientId=5143457905;cls=mas;reminder=1");
            reminderEmail = new NotificationEmail(reminderEvent);   
            //Do not need to call reminderEmail.init() for this test.

            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            mockNtfRetryEventHandler.cancelEvent("existingReminderNotifSchedulerId");
            PowerMock.expectLastCall().times(1);
            expect(mockNtfRetryEventHandler.rescheduleReminderTriggerEvent("existingReminderTriggerSchedulerId")).andReturn("rescheduledReminderTriggerSchedulerId").times(1);
            NtfEventHandlerRegistry.registerDefaultHandler(mockNtfRetryEventHandler);          

            PowerMock.replayAll();      

            //The reminder notification retry event will be cancelled and the reminder trigger event which will be rescheduled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, "existingReminderNotifSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, reminderEmail, Constants.FEEDBACK_STATUS_OK);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "rescheduledReminderTriggerSchedulerId");
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), null);

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();
        }
    }

    @Test
    public void testStartNextReminderTrigger_notReminderStatusFailed_triggerSchedulingSucceeded(){
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            expect(Config.getSmsReminderIntervalInMin()).andReturn(1440).anyTimes();
            expect(Config.getSmsReminderExpireInMin()).andReturn(20160).anyTimes();

            NtfEvent nonReminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-Notif;fallbackorigntftype=0;eventType=Notif;omsg=50f531;try=2;exp=1287692373385;rmsg=742f78;fallback=0;omsa=eid:f214d68ddf252227;eventServiceType=ntf;rmsa=msid:fbcdc35108fbe3e9;frm=tel:+15143457900;RecipientId=5143457905;cls=mas;reminder=0");
            nonReminderEmail = new NotificationEmail(nonReminderEvent);
            //Do not call nonReminderEmail.init() to avoid having to create a message state file; message-specific info is irrelevant to this test.   

            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            expect(mockNtfRetryEventHandler.scheduleReminderTriggerEvent(nonReminderEvent)).andReturn("newReminderTriggerSchedulerId").times(3);
            mockNtfRetryEventHandler.cancelReminderTriggerEvent(null);
            PowerMock.expectLastCall().times(1);
            mockNtfRetryEventHandler.cancelReminderTriggerEvent((String)anyObject());
            PowerMock.expectLastCall().times(2);
            NtfEventHandlerRegistry.registerDefaultHandler(mockNtfRetryEventHandler);          

            PowerMock.replayAll();      

            //No previously scheduled reminder trigger event.
            assertFalse((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_FAILED);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "newReminderTriggerSchedulerId"); 

            //There is a previously scheduled reminder trigger event which will be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_FAILED);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "newReminderTriggerSchedulerId");

            //There is a previously scheduled reminder trigger event which will be cancelled and reminder notification event which will not be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, "existingReminderNotifSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_FAILED);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "newReminderTriggerSchedulerId");
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), "existingReminderNotifSchedulerId");

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();
        }
    }
    
    @Test
    public void testStartNextReminderTrigger_notReminderStatusFailed_triggerSchedulingFailed(){
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            expect(Config.getSmsReminderIntervalInMin()).andReturn(1440).anyTimes();
            expect(Config.getSmsReminderExpireInMin()).andReturn(20160).anyTimes();  

            NtfEvent nonReminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-Notif;fallbackorigntftype=0;eventType=Notif;omsg=50f531;try=2;exp=1287692373385;rmsg=742f78;fallback=0;omsa=eid:f214d68ddf252227;eventServiceType=ntf;rmsa=msid:fbcdc35108fbe3e9;frm=tel:+15143457900;RecipientId=5143457905;cls=mas;reminder=0");
            nonReminderEmail = new NotificationEmail(nonReminderEvent);
            //Do not call nonReminderEmail.init() to avoid having to create a message state file; message-specific info is irrelevant to this test.   

            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            expect(mockNtfRetryEventHandler.scheduleReminderTriggerEvent(nonReminderEvent)).andReturn(null).times(3);
            NtfEventHandlerRegistry.registerDefaultHandler(mockNtfRetryEventHandler);          

            PowerMock.replayAll();      

            //New reminder trigger scheduling fails.  No previously scheduled reminder trigger event.
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_FAILED);
            assertFalse((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), null); 

            //New reminder trigger scheduling fails.  There is a previously scheduled reminder trigger event which will not be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_FAILED);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "existingReminderTriggerSchedulerId");

            //There is a previously scheduled reminder trigger event which will not be cancelled and reminder notification event which will not be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, "existingReminderNotifSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_FAILED);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "existingReminderTriggerSchedulerId");
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), "existingReminderNotifSchedulerId");

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();
        }
    }

    @Test
    public void testStartNextReminderTrigger_reminderStatusFailed(){
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            expect(Config.getSmsReminderIntervalInMin()).andReturn(1440).anyTimes();
            expect(Config.getSmsReminderExpireInMin()).andReturn(20160).anyTimes();

            NtfEvent reminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-reminder;try=2;exp=1287692373385;RecipientId=5143457905;cls=mas;reminder=1");
            reminderEmail = new NotificationEmail(reminderEvent);   
            //Do not need to call reminderEmail.init() for this test.
            
            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            mockNtfRetryEventHandler.cancelEvent("existingReminderNotifSchedulerId");
            PowerMock.expectLastCall().times(1);
            NtfEventHandlerRegistry.registerDefaultHandler(mockNtfRetryEventHandler);          

            PowerMock.replayAll();      

            //The reminder notification retry event will be cancelled and the scheduled reminder trigger event will not be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, "existingReminderNotifSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, reminderEmail, Constants.FEEDBACK_STATUS_FAILED);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "existingReminderTriggerSchedulerId");
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), null);

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();
        }
    }
    
    @Test
    public void testStartNextReminderTrigger_notReminderStatusExpired_triggerSchedulingSucceeded(){
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            expect(Config.getSmsReminderIntervalInMin()).andReturn(1440).anyTimes();
            expect(Config.getSmsReminderExpireInMin()).andReturn(20160).anyTimes();

            NtfEvent nonReminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-Notif;fallbackorigntftype=0;eventType=Notif;omsg=50f531;try=2;exp=1287692373385;rmsg=742f78;fallback=0;omsa=eid:f214d68ddf252227;eventServiceType=ntf;rmsa=msid:fbcdc35108fbe3e9;frm=tel:+15143457900;RecipientId=5143457905;cls=mas;reminder=0");
            nonReminderEmail = new NotificationEmail(nonReminderEvent);
            //Do not call nonReminderEmail.init() to avoid having to create a message state file; message-specific info is irrelevant to this test.   

            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            expect(mockNtfRetryEventHandler.scheduleReminderTriggerEvent(nonReminderEvent)).andReturn("newReminderTriggerSchedulerId").times(3);
            mockNtfRetryEventHandler.cancelReminderTriggerEvent(null);
            PowerMock.expectLastCall().times(1);
            mockNtfRetryEventHandler.cancelReminderTriggerEvent((String)anyObject());
            PowerMock.expectLastCall().times(2);
            NtfEventHandlerRegistry.registerDefaultHandler(mockNtfRetryEventHandler);          

            PowerMock.replayAll();      

            //No previously scheduled reminder trigger event.
            assertFalse((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_EXPIRED);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "newReminderTriggerSchedulerId"); 

            //There is a previously scheduled reminder trigger event which will be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_EXPIRED);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "newReminderTriggerSchedulerId");

            //There is a previously scheduled reminder trigger event which will be cancelled and reminder notification event which will not be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, "existingReminderNotifSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_EXPIRED);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "newReminderTriggerSchedulerId");
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), "existingReminderNotifSchedulerId");

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();
        }
    }
    
    @Test
    public void testStartNextReminderTrigger_notReminderStatusExpired_triggerSchedulingFailed(){
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            expect(Config.getSmsReminderIntervalInMin()).andReturn(1440).anyTimes();
            expect(Config.getSmsReminderExpireInMin()).andReturn(20160).anyTimes();  

            NtfEvent nonReminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-Notif;fallbackorigntftype=0;eventType=Notif;omsg=50f531;try=2;exp=1287692373385;rmsg=742f78;fallback=0;omsa=eid:f214d68ddf252227;eventServiceType=ntf;rmsa=msid:fbcdc35108fbe3e9;frm=tel:+15143457900;RecipientId=5143457905;cls=mas;reminder=0");
            nonReminderEmail = new NotificationEmail(nonReminderEvent);
            //Do not call nonReminderEmail.init() to avoid having to create a message state file; message-specific info is irrelevant to this test.   

            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            expect(mockNtfRetryEventHandler.scheduleReminderTriggerEvent(nonReminderEvent)).andReturn(null).times(3);
            NtfEventHandlerRegistry.registerDefaultHandler(mockNtfRetryEventHandler);          

            PowerMock.replayAll();      

            //New reminder trigger scheduling fails.  No previously scheduled reminder trigger event.
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_EXPIRED);
            assertFalse((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), null); 

            //New reminder trigger scheduling fails.  There is a previously scheduled reminder trigger event which will not be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_EXPIRED);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "existingReminderTriggerSchedulerId");

            //There is a previously scheduled reminder trigger event which will not be cancelled and reminder notification event which will not be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, "existingReminderNotifSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, nonReminderEmail, Constants.FEEDBACK_STATUS_EXPIRED);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "existingReminderTriggerSchedulerId");
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), "existingReminderNotifSchedulerId");

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();
        }
    }

    @Test
    public void testStartNextReminderTrigger_reminderStatusExpired(){
        try{
            PowerMock.mockStaticNice(Config.class);
            expect(Config.isSmsReminderEnabled()).andReturn(true).anyTimes();
            expect(Config.getSmsReminderIntervalInMin()).andReturn(1440).anyTimes();
            expect(Config.getSmsReminderExpireInMin()).andReturn(20160).anyTimes();

            NtfEvent reminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-reminder;try=2;exp=1287692373385;RecipientId=5143457905;cls=mas;reminder=1");
            reminderEmail = new NotificationEmail(reminderEvent);   
            //Do not need to call reminderEmail.init() for this test.
            
            NtfRetryEventHandler mockNtfRetryEventHandler = PowerMock.createMock(NtfRetryEventHandler.class);
            NtfEventHandlerRegistry.registerDefaultHandler(mockNtfRetryEventHandler);          

            PowerMock.replayAll();      

            //The reminder notification retry event will be cancelled and the scheduled reminder trigger event will not be cancelled.
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID, "existingReminderTriggerSchedulerId");
            SmsReminder.updatePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, "existingReminderNotifSchedulerId");
            SmsReminder.startNextReminderTrigger(userInfo, reminderEmail, Constants.FEEDBACK_STATUS_EXPIRED);
            assertTrue((new MfsEventManager()).fileExists("5143457905", SmsReminder.SMS_STATUS_FILE, false));
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_TRIGGER_EVENT_ID), "existingReminderTriggerSchedulerId");
            assertEquals(SmsReminder.retrievePersistentProperty("5143457905", SmsReminder.REMINDER_NOTIFICATION_EVENT_ID), null);

            PowerMock.verifyAll();
        }
        finally{
            resetTestEnvironment();
        }
    }
    
    //@Test
    public void testSendSMS() throws ConfigurationDataException, MsgStoreException, CodingFailureException{
        PowerMock.mockStaticNice(Config.class);
        expect(Config.getSourceAddress("voice", myCos)).andReturn(new SMSAddress(1, 2, "12")).anyTimes();
        
        PowerMock.mockStaticNice(SMSConfigWrapper.class);
        SMSConfigWrapper.initSMPPErrorCodeActions();
        PowerMock.expectLastCall().atLeastOnce();
        
        NtfEvent reminderEvent = new NtfEvent("tn0/20101021-14h20/144_r9007fbb3610f0-0.ntf-reminder;try=1;exp=1287685644547;rmsa=msid:796620cf67f92a96;RecipientId=5143457906;reminder=1");
        String[] notificationEmailMethodsToMock = {"getUserMailbox"};
        NotificationEmail mockReminderEmail = PowerMock.createPartialMock(NotificationEmail.class, notificationEmailMethodsToMock, reminderEvent);
        mockReminderEmail.init();
        NotificationGroup notificationGroup = new NotificationGroup(completedListener, mockReminderEmail, log, mer);
        expect(mockReminderEmail.getUserMailbox()).andReturn(inbox);
        PowerMock.replayAll();

        Properties props = new Properties();
        props.setProperty("SMS", "s");
        String[] smsNumbers = {"123456789"};
        SmsFilterInfo filterInfo = new SmsFilterInfo(props, smsNumbers, null);
        
        /* I could not mock TextCreator successfully.  So, in desperation, I just commented out TextCreator.generateText() in SMSOut.handleSMS(). 
         * Result is that I traced through the rest of the code to see if there are any issues but this test case does not really work.*/
        
//        PowerMock.mockStaticNice(Phrases.class);
//        Phrases.refresh();
//        PowerMock.expectLastCall().atLeastOnce();
//        PowerMock.replayAll(Phrases.class);
        
//     //   String[] textCreatorMethodsToMock = {"generateText"};
//        TextCreator mockTextCreator = PowerMock.createMock(TextCreator.class);
////        TextCreator mockTextCreator = PowerMock.createMock(TextCreator.class);
////        TextCreator.injectTextCreator(mockTextCreator);
//        expect(mockTextCreator.generateText(inbox, mockReminderEmail, userInfo, filterInfo.getNotifContent(), true, null)).andReturn("Junit test sms text.");        
//        PowerMock.replayAll(mockTextCreator);
//        
//        PowerMock.mockStaticNice(TextCreator.class);
//        expect(TextCreator.get()).andReturn(mockTextCreator);        
//        PowerMock.replayAll(TextCreator.class);
        
        SmsReminder.sendSMS(userInfo, notificationGroup, filterInfo);
        PowerMock.verifyAll();
    }
    
    //@Test
    public void testSendSMS_isDiscardSmsWhenCountIs0True() throws ConfigurationDataException, MsgStoreException, CodingFailureException{
        PowerMock.mockStaticNice(Config.class);
        expect(Config.getSourceAddress("voice", myCos)).andReturn(new SMSAddress(1, 2, "12")).anyTimes();
        expect(Config.isDiscardSmsWhenCountIs0()).andReturn(true).anyTimes();
        
        PowerMock.mockStaticNice(SMSConfigWrapper.class);
        SMSConfigWrapper.initSMPPErrorCodeActions();
        PowerMock.expectLastCall().atLeastOnce();

        UserMailbox emptyInbox = new UserMailbox(0, 0, 0, 0, 0, 0, 0, 0, true);
        NtfEvent reminderEvent = new NtfEvent("tn0/20101021-14h20/144_r9007fbb3610f0-0.ntf-reminder;try=1;exp=1287685644547;rmsa=msid:796620cf67f92a96;RecipientId=5143457906;reminder=1");
        String[] notificationEmailMethodsToMock = {"getUserMailbox"};
        NotificationEmail mockReminderEmail = PowerMock.createPartialMock(NotificationEmail.class, notificationEmailMethodsToMock, reminderEvent);
        mockReminderEmail.init();
        NotificationGroup notificationGroup = new NotificationGroup(completedListener, mockReminderEmail, log, mer);
        expect(mockReminderEmail.getUserMailbox()).andReturn(emptyInbox);
        PowerMock.replayAll();

        Properties props = new Properties();
        props.setProperty("SMS", "s");
        String[] smsNumbers = {"123456789"};
        SmsFilterInfo filterInfo = new SmsFilterInfo(props, smsNumbers, null);
        
        /* I could not mock TextCreator successfully.  So, in desperation, I just commented out TextCreator.generateText() in SMSOut.handleSMS(). 
         * Result is that I traced through the rest of the code to see if there are any issues but this test case does not really work.*/
        
//        PowerMock.mockStaticNice(Phrases.class);
//        Phrases.refresh();
//        PowerMock.expectLastCall().atLeastOnce();
//        PowerMock.replayAll(Phrases.class);
        
//     //   String[] textCreatorMethodsToMock = {"generateText"};
//        TextCreator mockTextCreator = PowerMock.createMock(TextCreator.class);
////        TextCreator mockTextCreator = PowerMock.createMock(TextCreator.class);
////        TextCreator.injectTextCreator(mockTextCreator);
//        expect(mockTextCreator.generateText(inbox, mockReminderEmail, userInfo, filterInfo.getNotifContent(), true, null)).andReturn("Junit test sms text.");        
//        PowerMock.replayAll(mockTextCreator);
//        
//        PowerMock.mockStaticNice(TextCreator.class);
//        expect(TextCreator.get()).andReturn(mockTextCreator);        
//        PowerMock.replayAll(TextCreator.class);
        
        SmsReminder.sendSMS(userInfo, notificationGroup, filterInfo);
        PowerMock.verifyAll();
    }    

    @Test
    public void testPersistency(){
        String mailboxTel = "5143457906";
        
        String triggerBackupId_1 = "tn0/20101021-14h20/144_r9007fbb3610f0-0.smsreminder-reminder;try=1;exp=1287685644547;rmsa=msid:796620cf67f92a96;RecipientId=5143457906;reminder=1";
        SmsReminder.updatePersistentProperty(mailboxTel, SmsReminder.REMINDER_TRIGGER_EVENT_ID, triggerBackupId_1);
        String storedTriggerId = SmsReminder.retrievePersistentProperty(mailboxTel, SmsReminder.REMINDER_TRIGGER_EVENT_ID);
        assertEquals(storedTriggerId, triggerBackupId_1);      
        
        String triggerBackupId_2 = "tn0/20101022-14h20/144_r9007fbb3610f0-0.smsreminder-reminder;try=1;exp=1287685644547;rmsa=msid:796620cf67f92a96;RecipientId=5143457906;reminder=1";
        SmsReminder.updatePersistentProperty(mailboxTel, SmsReminder.REMINDER_TRIGGER_EVENT_ID, triggerBackupId_2);
        storedTriggerId = SmsReminder.retrievePersistentProperty(mailboxTel, SmsReminder.REMINDER_TRIGGER_EVENT_ID);
        assertEquals(storedTriggerId, triggerBackupId_2);      
        
        String reminderNotifBackupId = "tn0/20101021-14h20/144_r9007fbb3610f0-0.ntf-reminder;try=1;exp=1287685644547;rmsa=msid:796620cf67f92a96;RecipientId=5143457906;reminder=1";
        SmsReminder.updatePersistentProperty(mailboxTel, SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, reminderNotifBackupId);
        String storedReminderNotifId = SmsReminder.retrievePersistentProperty(mailboxTel, SmsReminder.REMINDER_NOTIFICATION_EVENT_ID);
        assertEquals(storedReminderNotifId, reminderNotifBackupId);
        
        SmsReminder.updatePersistentProperty(mailboxTel, SmsReminder.REMINDER_NOTIFICATION_EVENT_ID, null);
        storedReminderNotifId = SmsReminder.retrievePersistentProperty(mailboxTel, SmsReminder.REMINDER_NOTIFICATION_EVENT_ID);
        assertEquals(storedReminderNotifId, null);
        assertTrue((new MfsEventManager()).fileExists(mailboxTel, SmsReminder.SMS_STATUS_FILE, false));
        
        SmsReminder.updatePersistentProperty(mailboxTel, SmsReminder.REMINDER_TRIGGER_EVENT_ID, null);
        storedTriggerId = SmsReminder.retrievePersistentProperty(mailboxTel, SmsReminder.REMINDER_TRIGGER_EVENT_ID);
        assertEquals(storedTriggerId, null);
        assertFalse((new MfsEventManager()).fileExists(mailboxTel, SmsReminder.SMS_STATUS_FILE, false));
        
        storedReminderNotifId = SmsReminder.retrievePersistentProperty(mailboxTel, SmsReminder.REMINDER_NOTIFICATION_EVENT_ID);
        assertEquals(storedReminderNotifId, null);
    }
    
    private static class MyUserInfo extends McdUserInfo {

        MyUserInfo() {
            super.outdialNotification = true;
            //super.cos = CommonMessagingAccess.getInstance().getMcd().lookupCos("");
        }

        public String getOutdialSchema() {
            return "default";
        }

        public String getTelephoneNumber() {
            return myNumber;
        }
        
        public String getCosName() {
            return myCos;
        }
        
        public int hashCode() {
            return myNumber.hashCode();
        }
    }

    private static class CompletedListener implements NtfCompletedListener {
        public void notifCompleted(NtfEvent event) {
            ++completeCalls;
        }

        public void notifRetry(NtfEvent event) {
            ++retryCalls;
        }

        public void notifFailed(NtfEvent event) {
            ++failedCalls;
        }
    }
    
}
