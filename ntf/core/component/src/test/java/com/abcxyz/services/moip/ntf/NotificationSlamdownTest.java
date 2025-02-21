package com.abcxyz.services.moip.ntf;

import java.net.URI;
import java.util.HashMap;

//import org.apache.log4j.BasicConfigurator;
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

import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mrd.data.Reason;
import com.abcxyz.messaging.mrd.operation.SendMessageReq;
import com.abcxyz.messaging.mrd.operation.SendMessageResp;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.EventSentListener;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.NtfMessageService;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryHandling;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.out.sms.SMSClientStub;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.CommonMessagingAccessTest;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.common.sms.SMSClient;
import com.mobeon.common.trafficeventsender.MfsClient;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NtfEventHandler;
import com.mobeon.ntf.NtfMain;
import com.mobeon.ntf.out.sms.SMSOut;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Config.class)
public class NotificationSlamdownTest {

    private int responseReceived_OK = 0;
    private static NtfMain ntf;

    MessageInfo msgInfo = null;
    private static SMSClientStub smsClientStub = null;
    
    @Before
    public void startup() throws Exception {
        String userDir = System.getProperty("user.dir");
        System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
        System.setProperty("ntfHome", userDir + "/test/junit/" );
/*
        //CommonOamManagerTest.initOam();
        //CommonMessagingAccess.setMcd(null);
        CommonMessagingAccessTest.setUp();
        BasicConfigurator.configure();

        smsClientStub = new SMSClientStub();
        SMSOut.setSmsClient(smsClientStub);

        // Start NTF
        ntf = new NtfMain();
        Config.loadCfg();
*/
        
        CommonTestingSetup.setup();
        System.setProperty("-Dabcxyz.mfs.userdir.create", "true");
        
//        BasicConfigurator.configure();
        
        smsClientStub = new SMSClientStub();
        SMSOut.setSmsClient(smsClientStub);
        
        // Start NTF
        ntf = new NtfMain();
        Config.updateCfg();

        McdStub directoryAccess = new McdStub();
        CommonMessagingAccessTest.setMcdStub(directoryAccess);
        CommonMessagingAccessTest.setUp();
        
        MfsEventManager.setDirectoryAccess(directoryAccess);
        CommonMessagingAccess.setMcd(directoryAccess);

        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_CN_SERVICES, "moip");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_CN_SERVICES, "moip");
    }

    @After
    public void tearDown() {
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        CommonMessagingAccessTest.stop();
    }

    @Test
    public void testSlamdown_No_SmsType0() throws Exception {

        Thread.sleep(1000);
        for (int i=0;i<100;i++) {       System.out.println(i); }
        NtfEventHandler.resetNumberOfNotification();
        smsClientStub.reset();
        responseReceived_OK = 0;
        String callingNumber = "4503457910";
        String subscriberNumber = "5143457910";
        String notificationNumber = "5143457911";

        // By default, NTF uses SMS-Type-0, make sure to skip that part to send the notification directly
        PowerMock.mockStaticPartialStrict(Config.class, "getDoSmsType0Slamdown");
        expect(Config.getDoSmsType0Slamdown()).andReturn(false).anyTimes();
        replayAll();

        // Setup subscriber MCD profile
        McdStub directoryAccess = new McdStub();
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "1;y;a;evf;SMS;s;1;;;;;default;;");
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_SERVICES, "msgtype_voice");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_COS_IDENTITY, "cos:1");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_NOTIF_NUMBER, notificationNumber);
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE, "1111111111;SMS;M");
        directoryAccess.addSubcriberProfileIdentity(URI.create("msid:111112462ffff"));
        CommonMessagingAccessTest.setMcdStub(directoryAccess);

        MfsEventManager mfsEventManager = new MfsEventManager();
        TrafficEvent trafficEvent = new TrafficEvent();
        trafficEvent.setName(MfsClient.EVENT_SLAMDOWNINFORMATION);
        trafficEvent.setProperty(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY, new Long(System.currentTimeMillis()).toString());
        trafficEvent.setProperty(MoipMessageEntities.SLAMDOWN_CALLING_NUMBER_PROPERTY, callingNumber);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);

        // Validating slamdown file generated
        Thread.sleep(8000);
        String[] slamdownFilesCreated = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION + "_");
        assertTrue(slamdownFilesCreated != null);

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        Thread.sleep(4000);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        String slamdownFileName = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION + "_")[0];
        SendMessageReq req = getSendMessageReq(subscriberNumber, notificationNumber, slamdownFileName, true);
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(10000);

        // Validating the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        System.out.println("responseReceived_OK" +  responseReceived_OK);
        assertEquals(1, responseReceived_OK);

        // Validating no Slamdown file anymore
        String[] slamdownFilesRemoved = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION);
        if (slamdownFilesRemoved != null) {
            System.out.println("file: " + slamdownFilesRemoved[0]);
        }

        String[] slamdownFilesRemoved2 = mfsEventManager.getFilePathsNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION);
        if (slamdownFilesRemoved2 != null) {
            System.out.println("file: " + slamdownFilesRemoved2[0]);
        }

        assertTrue(slamdownFilesRemoved == null);

        // Validating the SMSClientStub received only 1 request (the SMS-Info)
        int numberOfRequests = smsClientStub.getNumberOfRequests();
        assertTrue(numberOfRequests == 1);

        // Validating the SMS-Info request is about the given calling number
        String[] callers = smsClientStub.getCallers();
        assertTrue(callers.length == 1);
        assertTrue(callers[0].equals(callingNumber));
    }

    @Test
    public void testSlamdown_With_SmsType0() throws Exception {
    	NtfEventHandler.resetNumberOfNotification();
        smsClientStub.reset();
        responseReceived_OK = 0;
        String callingNumber = "4503457900";
        String subscriberNumber = "5143457900";
        String notificationNumber = "5143457901";

        // Setup subscriber MCD profile
        McdStub directoryAccess = new McdStub();
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "1;y;a;evf;SMS;s;1;;;;;default;;");
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_SERVICES, "msgtype_voice");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_COS_IDENTITY, "cos:1");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_NOTIF_NUMBER, notificationNumber);
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE, "1111111111;SMS;M");
        directoryAccess.addSubcriberProfileIdentity(URI.create("msid:111112462ffff"));
        CommonMessagingAccessTest.setMcdStub(directoryAccess);

        MfsEventManager mfsEventManager = new MfsEventManager();
        TrafficEvent trafficEvent = new TrafficEvent();
        trafficEvent.setName(MfsClient.EVENT_SLAMDOWNINFORMATION);
        trafficEvent.setProperty(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY, new Long(System.currentTimeMillis()).toString());
        trafficEvent.setProperty(MoipMessageEntities.SLAMDOWN_CALLING_NUMBER_PROPERTY, callingNumber);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);

        // Validating slamdown file generated
        Thread.sleep(4000);
        String[] slamdownFilesCreated = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION + "_");
        assertTrue(slamdownFilesCreated != null);

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        Thread.sleep(4000);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        String slamdownFileName = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION + "_")[0];
        SendMessageReq req = getSendMessageReq(subscriberNumber, notificationNumber, slamdownFileName, true);
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(2000);

        // Validating the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(1, responseReceived_OK);
        Thread.sleep(10000);

        // Validating no Slamdown file anymore
        String[] slamdownFilesRemoved = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION);
        assertTrue(slamdownFilesRemoved == null);

        // Validating the SMSClientStub received 2 requests (SMS-Type-0 and SMS-Info)
        assertTrue(smsClientStub.getNumberOfRequests() == 2);

        // Validating the SMS-Info request is about the given calling number
        String[] callers = smsClientStub.getCallers();
        assertTrue(callers.length == 1);
        assertTrue(callers[0].equals(callingNumber));
    }

    @Test
    public void testSlamdown_SmsType0_Retry() throws Exception {
    	NtfEventHandler.resetNumberOfNotification();
        smsClientStub.reset();
        responseReceived_OK = 0;
        String callingNumber = "4503457900";
        String subscriberNumber = "5143457900";
        String notificationNumber = "5143457901";

        // Setup subscriber MCD profile
        McdStub directoryAccess = new McdStub();
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "1;y;a;evf;SMS;s;1;;;;;default;;");
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_SERVICES, "msgtype_voice");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_COS_IDENTITY, "cos:1");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_NOTIF_NUMBER, notificationNumber);
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE, "1111111111;SMS;M");
        directoryAccess.addSubcriberProfileIdentity(URI.create("msid:111112462ffff"));
        CommonMessagingAccessTest.setMcdStub(directoryAccess);

        MfsEventManager mfsEventManager = new MfsEventManager();
        TrafficEvent trafficEvent = new TrafficEvent();
        trafficEvent.setName(MfsClient.EVENT_SLAMDOWNINFORMATION);
        trafficEvent.setProperty(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY, new Long(System.currentTimeMillis()).toString());
        trafficEvent.setProperty(MoipMessageEntities.SLAMDOWN_CALLING_NUMBER_PROPERTY, callingNumber);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);

        // Validating slamdown file generated
        Thread.sleep(4000);
        String[] slamdownFilesCreated = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION + "_");
        assertTrue(slamdownFilesCreated != null);

        // Set the SMS-C response to SEND_FAILED_TEMPORARY in order to force a retry from the scheduler
        smsClientStub.setSmsClientResponse(SMSClient.SEND_FAILED_TEMPORARY);
        Thread.sleep(4000);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        String slamdownFileName = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION + "_")[0];
        SendMessageReq req = getSendMessageReq(subscriberNumber, notificationNumber, slamdownFileName, true);
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        // Wait for the NotificationHandler to retrieve the event from the queue
        Thread.sleep(3000);

        // Set the SMS-C response to SEND_FAILED_TEMPORARY in order to force a retry from the scheduler
        smsClientStub.setSmsClientResponse(SMSClient.SEND_OK);

        // Sleep as long as the configured value (default config is 1 minute)
        String slamdownMcnSmsUnitRetrySchema = Config.getSlamdownMcnSmsUnitRetrySchema();
        int intervalInMinute = Integer.valueOf(slamdownMcnSmsUnitRetrySchema.substring(0, slamdownMcnSmsUnitRetrySchema.indexOf(":")));
        System.out.println("Wait for Scheduler to retry in " + intervalInMinute + " minute(s)...");
        Thread.sleep(((intervalInMinute) * 60 * 1000) + (10 * 1000));

        // Validating the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(1, responseReceived_OK);

        // Validating no Slamdown file anymore
        String[] slamdownFilesRemoved = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION);
        assertTrue(slamdownFilesRemoved == null);

        // Validating the SMSClientStub received 2 requests (SMS-Type-0 and SMS-Info)
        int numberOfRequests = smsClientStub.getNumberOfRequests();
        assertTrue(numberOfRequests == 2);

        // Validating the SMS-Info request is about the given calling number
        String[] callers = smsClientStub.getCallers();
        assertTrue(callers.length == 1);
        assertTrue(callers[0].equals(callingNumber));
    }

    @Test
    public void testSlamdown_SmsType0_Retry_With_Second_Slamdown() throws Exception {
    	NtfEventHandler.resetNumberOfNotification();
        smsClientStub.reset();
        responseReceived_OK = 0;
        String callingNumber = "4503457900";
        String callingNumber2 = "4503457902";
        String subscriberNumber = "5143457900";
        String notificationNumber = "5143457901";

        // Setup subscriber MCD profile
        McdStub directoryAccess = new McdStub();
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "1;y;a;evf;SMS;s;1;;;;;default;;");
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_SERVICES, "msgtype_voice");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_COS_IDENTITY, "cos:1");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_NOTIF_NUMBER, notificationNumber);
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE, "1111111111;SMS;M");
        directoryAccess.addSubcriberProfileIdentity(URI.create("msid:111112462ffff"));
        CommonMessagingAccessTest.setMcdStub(directoryAccess);

        MfsEventManager mfsEventManager = new MfsEventManager();
        TrafficEvent trafficEvent = new TrafficEvent();
        trafficEvent.setName(MfsClient.EVENT_SLAMDOWNINFORMATION);
        trafficEvent.setProperty(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY, new Long(System.currentTimeMillis()).toString());
        trafficEvent.setProperty(MoipMessageEntities.SLAMDOWN_CALLING_NUMBER_PROPERTY, callingNumber);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);
        
        // Validating slamdown file generated
        Thread.sleep(4000);
        String[] slamdownFilesCreated = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION + "_");
        assertTrue(slamdownFilesCreated != null);

        // Set the SMS-C response to SEND_FAILED_TEMPORARY in order to force a retry from the scheduler
        smsClientStub.setSmsClientResponse(SMSClient.SEND_FAILED_TEMPORARY);
        Thread.sleep(4000);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        String slamdownFileName = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION + "_")[0];
        SendMessageReq req = getSendMessageReq(subscriberNumber, notificationNumber, slamdownFileName, true);
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        // Wait for the NotificationHandler to retrieve the event from the queue
        Thread.sleep(3000);

        // Set the SMS-C response to SEND_FAILED_TEMPORARY in order to force a retry from the scheduler
        smsClientStub.setSmsClientResponse(SMSClient.SEND_OK);

        // Inject a second event to NTF
        TrafficEvent trafficEvent2 = new TrafficEvent();
        trafficEvent2.setName(MfsClient.EVENT_SLAMDOWNINFORMATION);
        trafficEvent2.setProperty(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY, new Long(System.currentTimeMillis()).toString());
        trafficEvent2.setProperty(MoipMessageEntities.SLAMDOWN_CALLING_NUMBER_PROPERTY, callingNumber2);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent2);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);
        mfsEventManager.storeEvent(subscriberNumber, trafficEvent);

        // Wait for the message to be stored in MFS
        Thread.sleep(2000);
        String slamdownFileName2 = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION + "_")[1];
        SendMessageReq req2 = getSendMessageReq(subscriberNumber, notificationNumber, slamdownFileName2, true);
        Thread.sleep(10000);
        service.sendMessage(req2);

        // Sleep as long as the configured value (default config is 1 minute)
        String slamdownMcnSmsUnitRetrySchema = Config.getSlamdownMcnSmsUnitRetrySchema();
        int intervalInMinute = Integer.valueOf(slamdownMcnSmsUnitRetrySchema.substring(0, slamdownMcnSmsUnitRetrySchema.indexOf(":")));
        System.out.println("Wait for Scheduler to retry in " + intervalInMinute + " minute(s)...");
        Thread.sleep(((intervalInMinute) * 60 * 1000) + (10 * 1000));

        // Validating the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 2);
        assertEquals(2, responseReceived_OK);

        // Validating no Slamdown file anymore
        String[] slamdownFilesRemoved = mfsEventManager.getFilesNameStartingWith(notificationNumber, MfsClient.EVENT_SLAMDOWNINFORMATION);
        assertTrue(slamdownFilesRemoved == null);

        // Validating the SMSClientStub received 2 requests (SMS-Type-0 and SMS-Info)
        int numberOfRequests = smsClientStub.getNumberOfRequests();
        assertTrue(numberOfRequests == 2);

        // Validating the SMS-Info request is about the given calling number
        String[] callers = smsClientStub.getCallers();
        assertTrue(callers.length == 2);
        assertTrue(callers[0].equals(callingNumber));
    }

    class MyListener implements EventSentListener {

        @Override
        public void sendStatus(NtfEvent event, SendStatus status)
        {
            if (status.equals(EventSentListener.SendStatus.OK) &&
                    (event.isEventServiceType(MoipMessageEntities.SERVICE_TYPE_SLAMDOWN) || event.isEventServiceType(MoipMessageEntities.SERVICE_TYPE_MCN))) {
                responseReceived_OK++;
            } else {
                System.out.println("NotificationSlamdownTest: EventSentListener: " + status);
            }

            NtfRetryHandling handler = NtfEventHandlerRegistry.getEventHandler(event.getEventServiceTypeKey());
            handler.cancelEvent(event.getReferenceId());
        }
    }


    private SendMessageReq getSendMessageReq(String subscriberNumber, String notificationNumber, String fileName, boolean slamdownCase) {
        SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = "im";
        req.destRcptID.value = subscriberNumber;

        req.rMsa.value = "";
        req.rMsgID.value = "";
        req.oMsa.value = "";
        req.oMsgID.value = "";

        req.eventType.value = (slamdownCase ? MoipMessageEntities.SERVICE_TYPE_SLAMDOWN : MoipMessageEntities.SERVICE_TYPE_MCN);
        req.eventID.value = "id";

        HashMap<String, String> extra = new HashMap<String, String>();
        extra.put(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY, fileName);
        extra.put(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY, notificationNumber);

        req.extraValue = extra;
        req.eventID.value = "myid";
        return req;
    }

    public static void main(String[] args) {
        String src = "1320933731000_15148181112-36057c9ae342444390e9853baf3553a5-3003.smsunit-smsunit";
        String dest = "15148181112-36057c9ae342444390e9853baf3553a5";
        boolean compare = src.contains(dest);
        System.out.println(compare);
        
    }
}
