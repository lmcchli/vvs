package com.abcxyz.services.moip.ntf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
import com.abcxyz.services.moip.ntf.NotificationBasicTest.MyHandler;
import com.abcxyz.services.moip.ntf.NotificationBasicTest.MyListener;
import com.abcxyz.services.moip.ntf.coremgmt.EventSentListener;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.NtfMessageService;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryHandling;
import com.abcxyz.services.moip.ntf.coremgmt.EventSentListener.SendStatus;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.out.sms.SMSClientStub;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.CommonMessagingAccessTest;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NtfMain;
import com.mobeon.ntf.out.sms.SMSOut;

public class NotificationMWIOnTest {

	private int responseReceived_OK = 0;
    private int responseReceived_NORMAL_RETRY = 0;
    
    private static CommonMessagingAccess commonMessagingAccess = null;
    static private String strDirectoy = "C:\\opt\\moip\\mfs";
    MessageInfo msgInfo = null;
    private static SMSClientStub smsClientStub = null;

    @BeforeClass
    static public void startup() throws Exception {
        String userDir = System.getProperty("user.dir");
        System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
        System.setProperty("ntfHome", userDir + "/test/junit/" );

        McdStub directoryAccess = new McdStub();
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "Name=sms;Active=yes;Notify=yes;ValidTime=Always;Priority=1;CriteriaMsgHighPriority=no;MsgDepositType=Voice;NotifType=SMS,EML;NotifContentSMS=Subject;NotifContentEML=Subject;NotifContentMWI=false;CriteriaTelephoneFrom=");
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_SERVICES, "msgtype_voice");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_COS_IDENTITY, "cos:1");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_NOTIF_NUMBER, "12345");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE, "1111111111;SMS;M");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE, "10");
        directoryAccess.addSubcriberProfileIdentity(URI.create("msid:111112462ffff"));

        CommonTestingSetup.setup();
        System.setProperty("-Dabcxyz.mfs.userdir.create", "true");
        
        CommonMessagingAccessTest.setMcdStub(directoryAccess);
        CommonMessagingAccessTest.setUp();
        
        //BasicConfigurator.configure();

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
        ntf.getEventHandler().resetNumberOfNotification();
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;

        // Create MFS message (simulating MAS deposit)
        storeMfsMessage();

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        //MyHandler handler = new MyHandler();
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        //NtfEventHandlerRegistry.registerDefaultHandler(handler);

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(2000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationCompleted(), 1);
        assertEquals(ntf.getEventHandler().getNumberOfNotificationRetry(), 0);
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

            NtfRetryHandling handler = NtfEventHandlerRegistry.getEventHandler(event.getEventServiceTypeKey());
            handler.cancelEvent(event.getReferenceId());
        }
    }
    
    private SendMessageReq getSendMessageReq() {
        SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = "im";
        req.destRcptID.value = "tel:+123456";

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

        //deleteDirectory(new File(strDirectoy + "\\internal"));


        CommonMessagingAccess.setMcd(new McdStub());
        commonMessagingAccess = CommonMessagingAccess.getInstance();

        //final ConfigManager mfsConfig = MfsConfiguration.getInstance();
        //mfsConfig.setParameter(MfsConfiguration.MfsRootPath, strDirectoy);
        //commonMessagingAccess.reInitializeMfs(mfsConfig);

        // create and store messages for testing
        final Container1 c1_1 = new Container1();
        c1_1.setFrom("491721092600");
        c1_1.setTo("491721092605");
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
}
