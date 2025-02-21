package com.abcxyz.services.moip.ntf.coremgmt;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.mrd.data.DestMessageClass;
import com.abcxyz.messaging.mrd.data.EventType;
import com.abcxyz.messaging.mrd.data.InformEventType;
import com.abcxyz.messaging.mrd.data.OrigMessageClass;
import com.abcxyz.messaging.mrd.data.Reason;
import com.abcxyz.messaging.mrd.data.RecipientMsa;
import com.abcxyz.messaging.mrd.data.Result;
import com.abcxyz.messaging.mrd.data.TransactionID;
import com.abcxyz.messaging.mrd.data.Version;
import com.abcxyz.messaging.mrd.operation.InformEventReq;
import com.abcxyz.messaging.mrd.operation.InformEventResp;
import com.abcxyz.messaging.mrd.operation.SendMessageReq;
import com.abcxyz.messaging.mrd.operation.SendMessageResp;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.ntf.userinfo.UserInfo;

public class NtfMessageServiceTest
{
    @BeforeClass
    static public void startup() throws ConfigurationException, ConfigurationDataException {

        BasicConfigurator.configure();
        CommonTestingSetup.setup();
        System.setProperty("abcxyz.messaging.scheduler.memory", "true");
    }


    @AfterClass
    static public void tearDown() {
        System.setProperty("abcxyz.messaging.scheduler.memory", "false");
    }

    @Test
    public void testSendMessageWithoutEventReceiver() {
        SendMessageReq req = getSendMessageReq();

        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);
        assertTrue (resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertTrue (service.getNumOfSendMessageReceived() == 1);
    }

    @Test
    public void testSpecialSendMessageWithoutEventReceiver() {
        SendMessageReq req = getSendMessageReq(NtfEventTypes.SLAMDOWN.getName());

        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);
        assertTrue (resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertTrue (service.getNumOfSendMessageReceived() == 1);
    }
    @Test
    public void testSendMessageWithExpire() {

        SendMessageReq req = getSendMessageReq();
        req.eventType.value = EventType.EXPIRY;

        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        assertTrue (resp.result.value.equalsIgnoreCase(Result.OK));
        assertTrue (service.getNumOfExpiredMessageReceived() == 1);
    }

    @Test
    public void testSendMessageWithEventReceiver() {

        MyReceiver receiver =  new MyReceiver();
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(receiver);
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();

        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);
        assertTrue (resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertTrue (service.getNumOfSendMessageReceived() == 1);

        assertTrue (receiver.eventRceived == 1);
        assertTrue (receiver.listenerSet);
        assertTrue (receiver.retryCreated);
    }

    @Test
    public void testSpecialSendMessageWithEventReceiver() {

    	SlamdownReceiver receiver =  new SlamdownReceiver();
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerEventReceiver(NtfEventTypes.SLAMDOWN.getName(), receiver);
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq(NtfEventTypes.SLAMDOWN.getName());

        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);
        assertTrue (resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertTrue (service.getNumOfSendMessageReceived() == 1);

        assertTrue (receiver.slmdRceived == 1);
    }
    
    @Ignore("Initial test cleanup for continuous integration - This test needs reviewing: it fails on Linux.")
    @Test
    public void testInformEventWithSUBSCRIBER_ACTIVITY_DETECTED(){
        NtfMessageService service = new NtfMessageService();
        InformEventReq req = new InformEventReq();
       	req.informEventType = new InformEventType(EventTypes.SUBSCRIBER_ACTIVITY_DETECTED.getName());
       	req.version = new Version("1.0");
       	req.transID = new TransactionID("test");
       	req.rMsa = new RecipientMsa(); req.rMsa.value = "test";
       	req.origMsgClass = new OrigMessageClass("test");
       	req.destMsgClass = new DestMessageClass("test");
		InformEventResp resp = service.informEvent(req);
		assertTrue(resp.informEventResult.value == Result.OK);
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
        }

    }
    class SlamdownReceiver implements NtfEventReceiver {
        int slmdRceived;

        public void sendEvent(NtfEvent event)
        {
            slmdRceived++;

        }

    }

    class MyListener implements EventSentListener {

        @Override
        public void sendStatus(NtfEvent event, SendStatus status)
        {
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
			return null;
		}

		@Override
		public NtfEvent scheduleEvent(Properties properties) {
			return null;
		}

		@Override
		public String getEventServiceName() {
			return "ntf";
		}

        @Override
        public void cancelReminderTriggerEvent(String eventId) {            
        }

        @Override
        public String rescheduleReminderTriggerEvent(String oldEventId) {
            return null;
        }

        @Override
        public String scheduleReminderTriggerEvent(NtfEvent event) {
            return null;
        }

        @Override
        public String scheduleAutoUnlockPinEvent(NtfEvent event, UserInfo user) {
            return null;
        }

        @Override
        public String scheduleAutoUnlockPinSmsEvent(NtfEvent event) {
            return null;
        }

    }

	private SendMessageReq getSendMessageReq() {
		return getSendMessageReq(EventType.DELIVERY);
	}

    private SendMessageReq getSendMessageReq(String serviceName) {

        SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = "ntf";
        req.destRcptID.value = "tel:+123456";
        req.rMsa.value = "msid:637jd";
        req.rMsgID.value = "29dkjd";
        req.oMsa.value = "eid:1234";
        req.oMsgID.value = "dkdfdfd";
        req.eventType.value = serviceName;
        req.eventID.value = "id";

        HashMap<String, String> extra = new HashMap<String, String>();
        extra.put("srv-type", "foo");
        extra.put("BarFoo", "bar");

        req.extraValue = extra;
        req.eventID.value = "myid";
        return req;

    }

}
