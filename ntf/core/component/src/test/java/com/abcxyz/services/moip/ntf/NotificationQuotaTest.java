package com.abcxyz.services.moip.ntf;

import java.io.File;
import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.After;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.easymock.EasyMock.expect;

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
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NtfMain;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.userinfo.UserInfo;

@RunWith(PowerMockRunner.class) 
@PrepareForTest(Config.class)
public class NotificationQuotaTest {

    private final String from = "491721092600";
    private final static String to = "491721092605";
    
    private int responseReceived_OK = 0;
    private int responseReceived_NORMAL_RETRY = 0;
    
    // FIXME CommonMessagingAccess does not initialize properly on Linux
//    private static CommonMessagingAccess commonMessagingAccess = CommonMessagingAccess.getInstance();
    private static CommonMessagingAccess commonMessagingAccess = null;
    
    static private String strDirectoy = "C:\\opt\\moip\\mfs";
    MessageInfo msgInfo = null;
    private static SMSClientStub smsClientStub = null;

//    @Before
    public void startup() throws Exception {
        String userDir = System.getProperty("user.dir");
        System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
        System.setProperty("ntfHome", userDir + "/test/junit/" );

        McdStub directoryAccess = new McdStub();

        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "Name=sms;Active=yes;Notify=yes;ValidTime=Always;Priority=1;CriteriaMsgHighPriority=no;MsgDepositType=Voice;NotifType=SMS,EML;NotifContentSMS=Subject;NotifContentEML=Subject;NotifContentMWI=false;CriteriaTelephoneFrom=");
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_SERVICES, "msgtype_voice");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE, "10");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_COS_IDENTITY, "cos:1");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_NOTIF_NUMBER, "12345");
        directoryAccess.addSubcriberProfileIdentity(URI.create("msid:111112462ffff"));
        
        CommonTestingSetup.setup();
        System.setProperty("-Dabcxyz.mfs.userdir.create", "true");
        
        CommonMessagingAccessTest.setMcdStub(directoryAccess);
        CommonMessagingAccessTest.setUp();
        
        BasicConfigurator.configure();
        
        smsClientStub = new SMSClientStub();
        SMSOut.setSmsClient(smsClientStub);
    }

//    @After
    public void tearDown() {
        CommonMessagingAccessTest.stop();
    }

    @Test
    @Ignore("Ignored until CommonMessagingAccess initialization is fixed")
    public void testNotificationQuota_QuotaAndNotification() throws Exception {

        // Start NTF
        NtfMain ntf = new NtfMain();
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;

        Thread.sleep(30000);
        
        // Set the NTF configuration to the appropriate values 
        PowerMock.mockStaticPartialStrict(Config.class, "isCheckQuota", "isDiscardWhenQuota", "isWarnWhenQuota");
        expect(Config.isCheckQuota()).andReturn(true).anyTimes();
        expect(Config.isDiscardWhenQuota()).andReturn(false).anyTimes();
        expect(Config.isWarnWhenQuota()).andReturn(true).anyTimes();
        expect(Config.isDiscardWhenQuota()).andReturn(false).anyTimes();
        PowerMock.replayAll(Config.class);

        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);


        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(8000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationCompleted(), 1);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationRetry(), 0);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationHoldback(), 0);
        
        // SMS-C must receive both notification and quota warning 
        assertEquals(2, smsClientStub.getNumberOfRequests());
        
       
    }

    @Test
    @Ignore("Ignored until CommonMessagingAccess initialization is fixed")
    public void testNotificationQuota_QuotaAndNoNotification() throws Exception {
        
        // Start NTF
        NtfMain ntf = new NtfMain();
        ntf.getEventHandler().resetNumberOfNotification();
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;

        // Set the NTF configuration to the appropriate values 
        PowerMock.mockStaticPartialStrict(Config.class, "isCheckQuota", "isDiscardWhenQuota", "isWarnWhenQuota");
        expect(Config.isCheckQuota()).andReturn(true).anyTimes();
        expect(Config.isDiscardWhenQuota()).andReturn(true).anyTimes();
        expect(Config.isWarnWhenQuota()).andReturn(true).anyTimes();
        expect(Config.isDiscardWhenQuota()).andReturn(true).anyTimes();
        PowerMock.replayAll(Config.class);
        
        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(8000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationCompleted(), 1);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationRetry(), 0);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationHoldback(), 0);

        // SMS-C must receive a quota warning and no notification 
        assertEquals(smsClientStub.getNumberOfRequests(), 1);
        

    }
    
    @Test
    @Ignore("Ignored until CommonMessagingAccess initialization is fixed")
    public void testNotificationQuota_NoQuotaAndNoNotification() throws Exception {
        
        // Start NTF
        NtfMain ntf = new NtfMain();
        ntf.getEventHandler().resetNumberOfNotification();
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;

        // Set the NTF configuration to the appropriate values 
        PowerMock.mockStaticPartialStrict(Config.class, "isCheckQuota", "isDiscardWhenQuota", "isWarnWhenQuota");
        expect(Config.isCheckQuota()).andReturn(true).anyTimes();
        expect(Config.isDiscardWhenQuota()).andReturn(true).anyTimes();
        expect(Config.isWarnWhenQuota()).andReturn(false).anyTimes();
        expect(Config.isDiscardWhenQuota()).andReturn(true).anyTimes();
        PowerMock.replayAll(Config.class);

        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(8000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationCompleted(), 1);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationRetry(), 0);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationHoldback(), 0);

        // SMS-C must not receive any request (neither notification nor quota warning) 
        assertEquals(smsClientStub.getNumberOfRequests(), 0);
        

    }
    
    @Test
    @Ignore("Ignored until CommonMessagingAccess initialization is fixed")
    public void testNotificationQuota_NoQuotaAndNotification() throws Exception {
        
        // Start NTF
        NtfMain ntf = new NtfMain();
        ntf.getEventHandler().resetNumberOfNotification();
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;

        // Set the NTF configuration to the appropriate values 
        PowerMock.mockStaticPartialStrict(Config.class, "isCheckQuota", "isDiscardWhenQuota", "isWarnWhenQuota");
        expect(Config.isCheckQuota()).andReturn(true).anyTimes();
        expect(Config.isDiscardWhenQuota()).andReturn(false).anyTimes();
        expect(Config.isWarnWhenQuota()).andReturn(false).anyTimes();
        expect(Config.isDiscardWhenQuota()).andReturn(false).anyTimes();
        PowerMock.replayAll(Config.class);
        
        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(8000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationCompleted(), 1);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationRetry(), 0);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationHoldback(), 0);

        // SMS-C must receive a notification and no quota warning 
        assertEquals(smsClientStub.getNumberOfRequests(), 1);
        

    }
    
    class MyListener implements EventSentListener {

        @Override
        public void sendStatus(NtfEvent event, SendStatus status)
        {
            if (status.equals(EventSentListener.SendStatus.OK)) {
                responseReceived_OK++;
            }
            if (status.equals(EventSentListener.SendStatus.NORMAL_RETRY)) {
                responseReceived_NORMAL_RETRY++;
            }
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
