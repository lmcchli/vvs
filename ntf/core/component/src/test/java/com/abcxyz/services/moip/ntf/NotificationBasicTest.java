package com.abcxyz.services.moip.ntf;

import java.io.File;
import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.mfs.MfsConfiguration;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mrd.data.Reason;
import com.abcxyz.messaging.mrd.operation.SendMessageReq;
import com.abcxyz.messaging.mrd.operation.SendMessageResp;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryHandling;
import com.abcxyz.services.moip.ntf.coremgmt.EventSentListener;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.NtfMessageService;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.out.sms.SMSClientStub;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.CommonMessagingAccessTest;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.common.sms.SMSClient;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NtfEventHandler;
import com.mobeon.ntf.NtfMain;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.userinfo.UserInfo;

public class NotificationBasicTest {

    private final String from = "491721092600";
    private final String to = "tel:+491721092605";
    
    private static CommonMessagingAccess commonMessagingAccess = null;
    static private String strDirectoy = "C:\\opt\\moip\\mfs";
    MessageInfo msgInfo = null;
    private static SMSClientStub smsClientStub = null;

    @BeforeClass
    static public void startup() throws Exception {
        String userDir = System.getProperty("user.dir");
        System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
        System.setProperty("ntfHome", userDir + "/test/junit/" );
	 	System.setProperty("backendConfigDirectory", userDir + "/../ipms_sys2/backend/cfg");

        McdStub directoryAccess = new McdStub();

        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "Name=sms;Active=yes;Notify=yes;ValidTime=Always;Priority=1;CriteriaMsgHighPriority=no;MsgDepositType=Voice;NotifType=SMS,EML;NotifContentSMS=Subject;NotifContentEML=Subject;NotifContentMWI=false;CriteriaTelephoneFrom=");
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_SERVICES, "msgtype_voice");
        directoryAccess.addSubcriberProfileIdentity(URI.create("msid:111112462ffff"));
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE, "10");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_COS_IDENTITY, "cos:1");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_NOTIF_NUMBER, "12345");

        CommonTestingSetup.setup();
        System.setProperty("-Dabcxyz.mfs.userdir.create", "true");
        
        CommonMessagingAccessTest.setMcdStub(directoryAccess);
        CommonMessagingAccessTest.setUp();
        
        BasicConfigurator.configure();

        smsClientStub = new SMSClientStub();
        SMSOut.setSmsClient(smsClientStub);
    }

    @AfterClass
    static public void tearDown() {
        CommonMessagingAccessTest.stop();
    }

    @Test
    public void testInjectNTFDefaultEvent_OK() throws Exception {

        // Start NTF
        NtfMain ntf = new NtfMain();

        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(NtfMain.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(4000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.OK, 1), 1);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.NORMAL_RETRY, 0), 0);
        assertEquals(NtfEventHandler.getNumberOfNotificationCompleted(), 1);
        assertEquals(NtfEventHandler.getNumberOfNotificationRetry(), 0);
    }

    @Test
    public void testInjectNTFDefaultEvent_FAILED() throws Exception {

        // Start NTF
        NtfMain ntf = new NtfMain();
        NtfEventHandler.resetNumberOfNotification();

        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Set the SMS-C response stub to the desired result
        smsClientStub.reset();
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_FAILED);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(NtfMain.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(2000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        
        // Failed case are never retried and therefore considered OK
        assertEquals(listener.getResponse(EventSentListener.SendStatus.OK,1), 1);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.NORMAL_RETRY, 0), 0);
        assertEquals(NtfEventHandler.getNumberOfNotificationCompleted(), 1);
        assertEquals(NtfEventHandler.getNumberOfNotificationRetry(), 0);
    }

    @Test
    public void testInjectNTFDefaultEvent_FAILED_TEMPORARY() throws Exception {

        // Start NTF
        NtfMain ntf = new NtfMain();
        NtfEventHandler.resetNumberOfNotification();

        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Set the SMS-C response stub to the desired result
        smsClientStub.reset();
        smsClientStub.setSmsClientResponse(SMSClient.SEND_FAILED_TEMPORARY);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(NtfMain.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(2000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        
        // SMS-C failed case means SMS-c connection down and will retry
        assertEquals(listener.getResponse(EventSentListener.SendStatus.OK, 0),  0);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.NORMAL_RETRY, 1), 1);
        assertEquals(NtfEventHandler.getNumberOfNotificationCompleted(), 0);
        assertEquals(NtfEventHandler.getNumberOfNotificationRetry(), 1);
    }

    @Test
    public void testInjectNTFDefaultEvent_RETRY() throws Exception {

        // Start NTF
        NtfMain ntf = new NtfMain();
        NtfEventHandler.resetNumberOfNotification();

        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Set the SMS-C response stub to the desired result
        smsClientStub.reset();
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_RETRY);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(NtfMain.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(2000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.OK, 0), 0);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.NORMAL_RETRY, 1), 1);
        assertEquals(NtfEventHandler.getNumberOfNotificationCompleted(), 0);
        assertEquals(NtfEventHandler.getNumberOfNotificationRetry(), 1);
    }

    @Test
    public void testInjectNTFDefaultEvent_SMSC_FAILED() throws Exception {

        // Start NTF
        NtfMain ntf = new NtfMain();
        NtfEventHandler.resetNumberOfNotification();

        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Set the SMS-C response stub to the desired result
        smsClientStub.reset();
        smsClientStub.setSmsUnitResponse(SMSClient.SEND_FAILED);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(NtfMain.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(10000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        
        // SMS-C failed case means SMS-c connection down and will retry
        assertEquals(listener.getResponse(EventSentListener.SendStatus.OK, 0),  0);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.NORMAL_RETRY, 1), 1);
        assertEquals(NtfEventHandler.getNumberOfNotificationCompleted(), 0);
        assertEquals(NtfEventHandler.getNumberOfNotificationRetry(), 1);
    }

    
    
    class MyListener implements EventSentListener {

        private int responseReceived_OK = 0;
        private int responseReceived_NORMAL_RETRY = 0;

        @Override
        public void sendStatus(NtfEvent event, SendStatus status)
        {
            NtfEventHandler ntfEventHandler = (NtfEventHandler)NtfEventHandlerRegistry.getNtfEventReceiver();

            if (status.equals(EventSentListener.SendStatus.OK)) {
                responseReceived_OK++;
            }
            if (status.equals(EventSentListener.SendStatus.NORMAL_RETRY)) {
                responseReceived_NORMAL_RETRY++;
            }
            
            ntfEventHandler.decreaseNumberOfNotificationCurrent();
        }
        
        public int getResponse(EventSentListener.SendStatus status, int expectedResult) {

            int response = 0;
            
            if (status.equals(EventSentListener.SendStatus.OK)) {
                if (responseReceived_OK >= expectedResult) {
                    response = responseReceived_OK;
                } else {
                    try {
                        // Let's wait a bit to give time the process to finish
                        int count = 0;
                        while (count < 10) {
                            Thread.sleep(1000);
                            if (responseReceived_OK >= expectedResult) {
                                response = responseReceived_OK;
                                break;
                            }
                            count++;
                        }
                    } catch (Exception e) {;}
                }
            }
            
            if (status.equals(EventSentListener.SendStatus.NORMAL_RETRY)) {
                if (responseReceived_NORMAL_RETRY >= expectedResult) {
                    response = responseReceived_NORMAL_RETRY;
                } else {
                    try {
                        // Let's wait a bit to give time the process to finish
                        int count = 0;
                        while (count < 10) {
                            Thread.sleep(1000);
                            if (responseReceived_NORMAL_RETRY >= expectedResult) {
                                response = responseReceived_NORMAL_RETRY;
                                break;
                            }
                            count++;
                        }
                    } catch (Exception e) {;}
                }
            }
            
            return response;
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
            return "DefaultNotificationEventType";
        }

        @Override
        public String scheduleEvent(NtfEvent event, long delay)
        {
            return "DefaultNotificationEventType";
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
            return null;
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
        SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = "im";
        req.destRcptID.value = to;

        req.rMsa.value = msgInfo.rmsa.toString();
        req.rMsgID.value = msgInfo.rmsgid.toString();
        req.oMsa.value = msgInfo.omsa.toString();
        req.oMsgID.value = msgInfo.omsgid.toString();

        req.eventType.value = NtfEventTypes.DEFAULT_NTF.getName();
        req.eventID.value = "id";

        HashMap<String, String> extra = new HashMap<String, String>();
        extra.put("srv-type", "foo");
        extra.put("BarFoo", "bar");

        req.extraValue = extra;
        req.eventID.value = "myid";
        return req;
    }

    private void storeMfsMessage() throws Exception {

        Calendar now = Calendar.getInstance();

        deleteDirectory(new File(strDirectoy + "\\internal"));


        CommonMessagingAccess.setMcd(new McdStub());
        commonMessagingAccess = CommonMessagingAccess.getInstance();

        final ConfigManager mfsConfig = MfsConfiguration.getInstance();
        mfsConfig.setParameter(MfsConfiguration.MfsRootPath, strDirectoy);
        commonMessagingAccess.reInitializeMfs(mfsConfig);

        // create and store messages for testing
        final Container1 c1_1 = new Container1();
        c1_1.setFrom(from);
        c1_1.setTo(to);
        c1_1.setSubject("subject");
        c1_1.setMsgClass("voice");
        c1_1.setDateTime(now.getTimeInMillis());
        final Container2 c2_1 = new Container2();
        final MsgBodyPart[] c3_1Parts = new MsgBodyPart[1];
        c3_1Parts[0] = new MsgBodyPart();
        final StateAttributes attributes1 = new StateAttributes();
        attributes1.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
        attributes1.setAttribute(Container1.Message_class, c1_1.getMsgClass());
        msgInfo = commonMessagingAccess.storeMessageTest(c1_1, c2_1, c3_1Parts, attributes1);

        final Container1 c1_1a = new Container1();
        c1_1a.setFrom(from);
        c1_1a.setTo(to);
        c1_1a.setSubject("subject");
        c1_1a.setMsgClass("voice");
        c1_1a.setDateTime(now.getTimeInMillis());
        final Container2 c2_1a = new Container2();
        final MsgBodyPart[] c3_1Partsa = new MsgBodyPart[1];
        c3_1Partsa[0] = new MsgBodyPart();
        final StateAttributes attributes1a = new StateAttributes();
        attributes1a.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
        attributes1a.setAttribute(Container1.Message_class, c1_1a.getMsgClass());
        msgInfo = commonMessagingAccess.storeMessageTest(c1_1a, c2_1a, c3_1Partsa, attributes1a);
    }

    private static boolean deleteDirectory(final File path) {
        if(path.exists()) {
            final File[] files = path.listFiles();
            for(int i = 0; i < files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

}
