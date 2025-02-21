/* **********************************************************************
 * Copyright (c) ABCXYZ 2009. All Rights Reserved.
 * Reproduction in whole or in part is prohibited without the
 * written consent of the copyright owner.
 *
 * ABCXYZ MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ABCXYZ SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * **********************************************************************/

package com.abcxyz.services.moip.ntf.coremgmt;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.EventProperties;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.scheduler.SchedulerManager;
import com.abcxyz.messaging.scheduler.SchedulerStartFailureException;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.userinfo.UserInfo;


@RunWith(PowerMockRunner.class)
@PrepareForTest(Config.class)
public class NtfRetryEventHandlerTest {

    static SchedulerManager scheduler;
    static OAMManager oam;

    // This test class needs reviewing - Totally hangs on Linux during continuous integration builds
    
    @Test
    public void testDummy() {
        // Dummy test case until we fix these tests.
        // Needed because maven test runner does not like running a class with no tests in it
    }
    
//    @BeforeClass
    static public void startup() throws ConfigurationException, ConfigurationDataException {

        CommonTestingSetup.setup();

        File path = new File("/tmp/scheduler/events");
        if (path.exists() == false) {
        	path.mkdirs();
        }

        //start scheduler
        try {
            NtfCmnManager.getInstance().startScheduler(CommonOamManager.getInstance().getMrdOam());
        } catch (SchedulerStartFailureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


//    @AfterClass
    static public void tearDown() {
    	try {
			Thread.sleep(100000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //stop scheduler
        NtfCmnManager.getInstance().stopScheduler();
        System.setProperty("abcxyz.messaging.scheduler.memory", "false");
    }

    @Test
    public void testScheduleEvent() throws InvalidEventIDException, SchedulerStartFailureException {
        PowerMock.mockStaticNice(Config.class);
        expect(Config.isSmsReminderEnabled()).andReturn(false).anyTimes();
        expect(Config.getNtfEventsRootPath()).andReturn("/opt/moip/events/ntf").anyTimes();
        PowerMock.replayAll(Config.class);
        
    	RetryEventInfo eventInfo = new RetryEventInfo(NtfEventTypes.DEFAULT_NTF.getName());

        eventInfo.setEventRetrySchema("15s 30s 60s STOP");

        NtfRetryEventHandler handler = new NtfRetryEventHandler(eventInfo);

        Properties eventProperties = new Properties();
        eventProperties.put("id", "msgid");
        eventProperties.put("to", "5143457900");
        
        NtfCmnManager.getInstance().start();
        
        //schedule with a random ID
        NtfEvent ntfEvent = handler.scheduleEvent(eventProperties);
        String id = ntfEvent.getReferenceId();

        assertTrue (id != null);
        handler.cancelEvent(id);

        EventID ID = new EventID(id);

        assertTrue (ID.getUid().endsWith("0"));
        assertTrue (ID.getProperty("id").equalsIgnoreCase("msgid"));
        assertTrue (ID.getProperty("to").equalsIgnoreCase("5143457900"));

        ntfEvent = handler.scheduleEvent("myid", eventProperties);
        id = ntfEvent.getReferenceId();

        assertTrue (id != null);
        handler.cancelEvent(id);

        ID = new EventID(id);

        assertTrue (ID.getUid().endsWith("myid-0"));
        assertTrue (ID.getProperty("id").equalsIgnoreCase("msgid"));
        assertTrue (ID.getProperty("to").equalsIgnoreCase("5143457900"));
    }

    @Test
    public void testScheduleReminderTriggerEvent() throws InvalidEventIDException, SchedulerStartFailureException {
        PowerMock.mockStaticNice(Config.class);
        expect(Config.getNtfEventsRootPath()).andReturn("/opt/moip/events/ntf").anyTimes();
        expect(Config.getSmsReminderIntervalInMin()).andReturn(1440).anyTimes();
        expect(Config.getSmsReminderExpireInMin()).andReturn(20160).anyTimes();
        PowerMock.replayAll();
                
        RetryEventInfo eventInfo = new RetryEventInfo(NtfEventTypes.DEFAULT_NTF.getName());
        eventInfo.setEventRetrySchema("15s 30s 60s STOP");

        NtfRetryEventHandler handler = new NtfRetryEventHandler(eventInfo);
        NtfCmnManager.getInstance().start();
        
        NtfEvent nonReminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-Notif;fallbackorigntftype=0;eventType=Notif;omsg=50f531;try=2;exp=1287692373385;rmsg=742f78;fallback=0;omsa=eid:f214d68ddf252227;eventServiceType=ntf;rmsa=msid:fbcdc35108fbe3e9;frm=tel:+15143457900;RecipientId=5143457905;cls=mas;reminder=0");        
        String id = handler.scheduleReminderTriggerEvent(nonReminderEvent);
        System.out.println("scheduled event: " + id);
        assertTrue (id != null);
        EventID ID = new EventID(id);
        assertTrue (ID.getProperty(Constants.DEST_RECIPIENT_ID).equalsIgnoreCase("5143457905"));
        assertTrue (ID.getProperty("reminder").equalsIgnoreCase("1"));
        
        handler.cancelReminderTriggerEvent(id);
        PowerMock.verifyAll();
    }
    
    @Test
    public void testRescheduleReminderTriggerEvent() throws InvalidEventIDException, SchedulerStartFailureException {
        PowerMock.mockStaticNice(Config.class);
        expect(Config.getNtfEventsRootPath()).andReturn("/opt/moip/events/ntf").anyTimes();
        expect(Config.getSmsReminderIntervalInMin()).andReturn(1440).anyTimes();
        expect(Config.getSmsReminderExpireInMin()).andReturn(20160).anyTimes();
        PowerMock.replayAll();
                
        RetryEventInfo eventInfo = new RetryEventInfo(NtfEventTypes.DEFAULT_NTF.getName());
        eventInfo.setEventRetrySchema("15s 30s 60s STOP");

        NtfRetryEventHandler handler = new NtfRetryEventHandler(eventInfo);
        NtfCmnManager.getInstance().start();
        
        //Successfully rescheduled case (reminder notification succeeds in more than 1 second)
        NtfEvent nonReminderEvent = new NtfEvent("tn0/20101021-16h15/272_r50f531742f780-0.ntf-Notif;fallbackorigntftype=0;eventType=Notif;omsg=50f531;try=2;exp=1287692373385;rmsg=742f78;fallback=0;omsa=eid:f214d68ddf252227;eventServiceType=ntf;rmsa=msid:fbcdc35108fbe3e9;frm=tel:+15143457900;RecipientId=5143457905;cls=mas;reminder=0");
        String oldReminderBackupId = handler.scheduleReminderTriggerEvent(nonReminderEvent);
        System.out.println("scheduled original trigger event: " + oldReminderBackupId);
        long delay = System.currentTimeMillis() + 1000;
        while (System.currentTimeMillis() < delay) { } //Simulate time needed to send reminder notification before rescheduling reminder trigger backup event.                       
        String newReminderBackupId = handler.rescheduleReminderTriggerEvent(oldReminderBackupId);        
        System.out.println("old event: " + oldReminderBackupId);
        System.out.println("new event: " + newReminderBackupId);
        assertTrue (newReminderBackupId != null);        
        EventID oldID = new EventID(oldReminderBackupId);
        EventID newID = new EventID(newReminderBackupId);
        assertEquals (newID.getProperty(Constants.DEST_RECIPIENT_ID), "5143457905");
        assertEquals (newID.getProperty("reminder"), "1");
        assertEquals (newID.getProperty(EventProperties.EXPIRY_TIME), oldID.getProperty(EventProperties.EXPIRY_TIME));
        assertEquals (newID.getProperty(EventProperties.NUM_TRIED), oldID.getProperty(EventProperties.NUM_TRIED));
        assertTrue(newID.getEventTime() > oldID.getEventTime());
        handler.cancelReminderTriggerEvent(newReminderBackupId);    
        
        //No need to reschedule because new rescheduled time stamp is same as the original time stamp (reminder notification succeeds in less than 1 second).
        oldReminderBackupId = handler.scheduleReminderTriggerEvent(nonReminderEvent);
        System.out.println("scheduled original trigger event: " + oldReminderBackupId);
        delay = System.currentTimeMillis() + 100;
        while (System.currentTimeMillis() < delay) { } //Simulate time needed to send reminder notification before rescheduling reminder trigger backup event.                       
        newReminderBackupId = handler.rescheduleReminderTriggerEvent(oldReminderBackupId);        
        System.out.println("old event: " + oldReminderBackupId);
        System.out.println("new event: " + newReminderBackupId);
        assertTrue (newReminderBackupId != null);        
        oldID = new EventID(oldReminderBackupId);
        newID = new EventID(newReminderBackupId);
        assertTrue (newReminderBackupId == oldReminderBackupId);
        
        //Attempt to rescheduled past expiry
        String expiryTime = String.valueOf(System.currentTimeMillis() + Config.getSmsReminderIntervalInMin() * 60 * 1000);
        oldReminderBackupId = "tn0/20110316-09h45/184_5143457905-c4dbee410b5f4ae282d526e0b7a35bc8-0.smsreminder-reminder;reminder=1;try=1;RecipientId=5143457905;exp=" + expiryTime;                
        newReminderBackupId = handler.rescheduleReminderTriggerEvent(oldReminderBackupId);        
        System.out.println("old event: " + oldReminderBackupId);
        System.out.println("new event: " + newReminderBackupId);
        assertTrue (newReminderBackupId == null);    

        //Attempt to rescheduled expiry event
        expiryTime = String.valueOf(System.currentTimeMillis() + Config.getSmsReminderIntervalInMin() * 60 * 1000 * 3);
        oldReminderBackupId = "tn0/20110316-09h45/184_5143457905-c4dbee410b5f4ae282d526e0b7a35bc8-0.smsreminder-" + EventID.EVENT_TYPE_EXPIRY + ";reminder=1;try=1;RecipientId=5143457905;exp=" + expiryTime;                
        newReminderBackupId = handler.rescheduleReminderTriggerEvent(oldReminderBackupId);        
        System.out.println("old event: " + oldReminderBackupId);
        System.out.println("new event: " + newReminderBackupId);
        assertTrue (newReminderBackupId == oldReminderBackupId);    

        //Attempt to rescheduled invalid event id (no service name or event type provided)
        expiryTime = String.valueOf(System.currentTimeMillis() + Config.getSmsReminderIntervalInMin() * 60 * 1000 *3);
        oldReminderBackupId = "tn0/20110316-09h45/184_5143457905-c4dbee410b5f4ae282d526e0b7a35bc8-0.;reminder=1;try=1;RecipientId=5143457905;exp=" + expiryTime;                
        newReminderBackupId = handler.rescheduleReminderTriggerEvent(oldReminderBackupId);        
        System.out.println("old event: " + oldReminderBackupId);
        System.out.println("new event: " + newReminderBackupId);
        assertTrue (newReminderBackupId == null);    

        //Attempt to rescheduled event id with invalid expiry time
        expiryTime = "abc";
        oldReminderBackupId = "tn0/20110316-09h45/184_5143457905-c4dbee410b5f4ae282d526e0b7a35bc8-0.smsreminder-reminder;reminder=1;try=1;RecipientId=5143457905;exp=" + expiryTime;                
        newReminderBackupId = handler.rescheduleReminderTriggerEvent(oldReminderBackupId);        
        System.out.println("old event: " + oldReminderBackupId);
        System.out.println("new event: " + newReminderBackupId);
        assertTrue (newReminderBackupId == oldReminderBackupId);    

        //Attempt to rescheduled null event id 
        oldReminderBackupId = null;                
        newReminderBackupId = handler.rescheduleReminderTriggerEvent(oldReminderBackupId);        
        System.out.println("old event: " + oldReminderBackupId);
        System.out.println("new event: " + newReminderBackupId);
        assertTrue (newReminderBackupId == null);    

        PowerMock.verifyAll();        
    }
    
//    @Test
    public void testDefaultHandler() throws InvalidEventIDException, InterruptedException {
    	RetryEventInfo eventInfo = new RetryEventInfo(NtfEventTypes.DEFAULT_NTF.getName());

        eventInfo.setEventRetrySchema("5s 35s 120s STOP");

        NtfRetryEventHandler handler = new NtfRetryEventHandler(eventInfo);

        MessageInfo msgInfo = new MessageInfo();
        msgInfo.omsa = new MSA("omsa");
        msgInfo.rmsa = new MSA("rmsa");
        msgInfo.omsgid = "omsgid";
        msgInfo.rmsgid = "rmsgid";


        Properties eventProperties = new Properties();
        eventProperties.put("id", "msgid");
        eventProperties.put("to", "5143457900");


        NtfEvent myEvent = new NtfEvent(eventInfo.getEventTypeKey(), msgInfo, eventProperties, "id");
        String id = handler.scheduleEvent(myEvent);
        System.out.print("Scheduled event: " + id);

        assertTrue (id != null);

        EventID ID = new EventID(id);

        assertTrue (ID.getUid().endsWith("0"));
        assertTrue (ID.getProperty("id").equalsIgnoreCase("msgid"));
        assertTrue (ID.getProperty("to").equalsIgnoreCase("5143457900"));

        //wait for checking if the event is fired
        Thread.sleep(5000);

        assertTrue(handler.getNumOfFiredNotifEvent() == 1);

        id = handler.getLastBackup();

        System.out.print("Next scheduled event: " + id);
        //cancel backup event
        handler.cancelEvent(id);


        assertTrue(handler.getNumOfCancelledEvent() == 1);

        //cancel the second one
        //wait for checking if the event is fired
        Thread.sleep(11000);

        assertTrue(handler.getNumOfFiredNotifEvent() == 1);
    }

//    @Test
    public void testWithResonseFromEventReceiver() {

        MyReceiver receiver =  new MyReceiver();
        MyListener listener = new MyListener();

        RetryEventInfo eventInfo = new RetryEventInfo("msg");
        eventInfo.setEventRetrySchema("5s 5s 5s STOP");
        NtfRetryEventHandler handler = new NtfRetryEventHandler(eventInfo);

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(receiver);
        NtfEventHandlerRegistry.registerDefaultHandler(handler);


        MessageInfo msgInfo = new MessageInfo();
        msgInfo.omsa = new MSA("omsa");
        msgInfo.rmsa = new MSA("rmsa");
        msgInfo.omsgid = "omsgid";
        msgInfo.rmsgid = "rmsgid";


        Properties eventProperties = new Properties();
        eventProperties.put("id", "msgid");
        eventProperties.put("to", "5143457900");


        NtfEvent myEvent = new NtfEvent(eventInfo.getEventTypeKey(), msgInfo, eventProperties, "id");
        myEvent.setSentListener(listener);
        String id = handler.scheduleEvent(myEvent);
        myEvent.keepReferenceID(id);

        //send to receiver
        receiver.sendEvent(myEvent);
        assertTrue (listener.statusReceived);

    }

    class MyReceiver implements NtfEventReceiver {

        int eventRceived;
        boolean listenerSet;
        boolean retryCreated;
        @Override
        public void sendEvent(NtfEvent event)
        {
            eventRceived++;
            listenerSet = event.getSentListener() != null;
            retryCreated = event.getReferenceId() != null;

            //send back response
            event.getSentListener().sendStatus(event, EventSentListener.SendStatus.OK);
        }

    }

    class MyListener implements EventSentListener {
        boolean statusReceived;
        @Override
        public void sendStatus(NtfEvent event, SendStatus status)
        {
            statusReceived = true;
        }

    }


    class MyHandler implements NtfRetryHandling {

        @Override
        public void cancelEvent(String eventId)
        {
        }

        @Override
        public String scheduleEvent(NtfEvent event)
        {
            return "myDummyEvent";
        }

        @Override
        public String scheduleEvent(NtfEvent event, long delay)
        {
            return "myDummyEvent";
        }

		@Override
		public NtfEvent scheduleEvent(String uid, Properties properties) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public NtfEvent scheduleEvent(Properties properties) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getEventServiceName() {

			return "ntf";
		}

        @Override
        public void cancelReminderTriggerEvent(String eventId) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public String rescheduleReminderTriggerEvent(String oldEventId) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String scheduleReminderTriggerEvent(NtfEvent event) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String scheduleAutoUnlockPinEvent(NtfEvent event, UserInfo user) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String scheduleAutoUnlockPinSmsEvent(NtfEvent event) {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
