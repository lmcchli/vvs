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
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.powermock.api.easymock.PowerMock.replayAll;
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
import com.mobeon.common.cmnaccess.MfsEventManagerStub;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NtfMain;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.slamdown.SlamdownList;
import com.mobeon.ntf.userinfo.UserInfo;

@RunWith(PowerMockRunner.class) 
@PrepareForTest(Config.class)
public class NotificationHoldbackTest {

    private final String from = "491721092600";
    private final static String to = "491721092605";
    
    private static CommonMessagingAccess commonMessagingAccess = CommonMessagingAccess.getInstance();
    static private String strDirectoy = "C:\\opt\\moip\\mfs";
    MessageInfo msgInfo = null;
    private static SMSClientStub smsClientStub = null;

    @Before
    public void startup() throws Exception {
        String userDir = System.getProperty("user.dir");
        System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
        System.setProperty("ntfHome", userDir + "/test/junit/" );
	 	System.setProperty("backendConfigDirectory", userDir + "/../ipms_sys2/backend/cfg");

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

    @After
    public void tearDown() {
        CommonMessagingAccessTest.stop();
    }

    @Test
    public void testNotificationHoldback_OK() throws Exception {

        // Start NTF
        NtfMain ntf = new NtfMain();

        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Create a login file (simulating that the user is logged in)
        IMfsEventManager mfsEventManager = new MfsEventManager();
        mfsEventManager.createLoginFile(to);
        
        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(4000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.OK, 0), 0);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.NORMAL_RETRY, 1), 1);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationHoldback(), 1);
    }

    @Test
    public void testNotificationHoldback_Slamdown_OK() throws Exception {

        File f = new File("slam.txt");
        if(!f.exists()){
            f.createNewFile();
        }
        
        //Set MfsEventManager stub
        MfsEventManagerStub mfsEventManager = new MfsEventManagerStub();
        TrafficEvent event = new TrafficEvent();
        event.setProperty(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY, new Long(System.currentTimeMillis()).toString());
        event.setProperty(MoipMessageEntities.SLAMDOWN_CALLING_NUMBER_PROPERTY, "333333333");
        mfsEventManager.setTrafficEvents(new TrafficEvent[]{event});
        SlamdownList.setMfsEventManager(mfsEventManager);       
        
        // Start NTF
        NtfMain ntf = new NtfMain();
        ntf.getEventHandler().resetNumberOfNotification();

        // Create a login file (simulating that the user is logged in)
        IMfsEventManager mfsEventManager2 = new MfsEventManager();
        mfsEventManager2.createLoginFile(to);
        
        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendSlamdownMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(4000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.OK, 1), 1);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.NORMAL_RETRY, 0), 0);
    }

    @Test
    public void testNotificationHoldback_Removed() throws Exception {

        // Start NTF
        NtfMain ntf = new NtfMain();
        ntf.getEventHandler().resetNumberOfNotification();

        // Set the SMS-C response stub to the desired result
        smsClientStub.reset();
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_RETRY);
        
        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Remove the login file (simulating that the user is not logged in anymore)
        IMfsEventManager mfsEventManager = new MfsEventManager();
        mfsEventManager.removeLoginFile(to);
        
        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(10000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.OK, 0), 0);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.NORMAL_RETRY, 1), 1);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationCompleted(), 0);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationRetry(), 1);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationHoldback(), 0);
    }
    
    @Test
    public void testNotificationHoldback_OldLoginFile() throws Exception {
        
        // Start NTF
        NtfMain ntf = new NtfMain();
        ntf.getEventHandler().resetNumberOfNotification();

        // Set the NTF configuration to the appropriate values 
        PowerMock.mockStaticPartialStrict(Config.class, "getLoginFileValidityPeriod");
        expect(Config.getLoginFileValidityPeriod()).andReturn(1).anyTimes();
        replayAll();
        
        // Set the SMS-C response stub to the desired result
        // Since the login file is too old, the subscriber will be notified
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);
        
        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Create a login file (simulating that the user is logged in)
        IMfsEventManager mfsEventManager = new MfsEventManager();
        mfsEventManager.createLoginFile(to);

        // Make the logginFile older by 5 minutes
        TestMfsEventManagerStub mfsEventManagerStub = new TestMfsEventManagerStub();
        mfsEventManagerStub.setLastModified((Calendar.getInstance().getTimeInMillis())-(1000*60*5));
        
        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(10000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.OK, 1), 1);
        assertEquals(listener.getResponse(EventSentListener.SendStatus.NORMAL_RETRY, 0), 0);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationCompleted(), 1);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationRetry(), 0);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationHoldback(), 0);
    }

    class TestMfsEventManagerStub extends MfsEventManager {
        
        public void setLastModified(long lastModified) {
            try {
                String filePath = generateFilePath(to, "loggedin", true);
                File logginFile = new File(filePath);
                logginFile.setLastModified(lastModified);
            } catch (Exception tese) {
                ;
            }
        }
    }
    
    class MyListener implements EventSentListener {
        
        private int responseReceived_OK = 0;
        private int responseReceived_NORMAL_RETRY = 0;

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
        
        public int getResponse(EventSentListener.SendStatus status, int expectedResult) {

            int response = 0;
            
            if (status.equals(EventSentListener.SendStatus.OK)) {
                if (responseReceived_OK >= expectedResult) {
                    System.out.println("responseReceived_OK:" + responseReceived_OK);
                    response = responseReceived_OK;
                } else {
                    try {
                        // Let's wait a bit to give time the process to finish
                        int count = 0;
                        while (count < 10) {
                            System.out.println("responseReceived_OK: wait!: " + responseReceived_OK);
                            Thread.sleep(1000);
                            if (responseReceived_OK >= expectedResult) {
                                System.out.println("responseReceived_OK: wait in!: " + responseReceived_OK);
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

    private SendMessageReq getSendSlamdownMessageReq() {
        SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = "im";
        req.destRcptID.value = to;

        req.rMsa.value = "";
        req.rMsgID.value = "";
        req.oMsa.value = "";
        req.oMsgID.value = "";

        req.eventType.value = MoipMessageEntities.SERVICE_TYPE_SLAMDOWN;
        req.eventID.value = "id";

        HashMap<String, String> extra = new HashMap<String, String>();
        extra.put(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY, "slam.txt");

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
