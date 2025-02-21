package com.abcxyz.services.moip.ntf;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mrd.data.Reason;
import com.abcxyz.messaging.mrd.operation.SendMessageReq;
import com.abcxyz.messaging.mrd.operation.SendMessageResp;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.EventSentListener;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.NtfMessageService;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryHandling;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.out.sms.SMSClientStub;
import com.mobeon.common.cmnaccess.CommonMessagingAccessTest;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.common.cmnaccess.MfsEventManagerStub;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NtfEventHandler;
import com.mobeon.ntf.NtfMain;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.slamdown.SlamdownList;
import com.mobeon.ntf.slamdown.SlamdownSender;

public class NotificationMcnTest {

    private int responseReceived_OK = 0;
    private static NtfMain ntf;    

    MessageInfo msgInfo = null;
    private static SMSClientStub smsClientStub = null;

    // This test class needs reviewing - does not initialize properly when executed on Linux
    
//    @BeforeClass
    static public void startup() throws Exception {
        String userDir = System.getProperty("user.dir");
        System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
        System.setProperty("ntfHome", userDir + "/test/junit/" );
        System.setProperty("rootdirectory", userDir + "/test/junit/cdr");
        System.setProperty("rootconfigdirectory", userDir + "/../ipms_sys2/backend/cfg/cdrgen");
        System.setProperty("asciirepository", userDir + "/test/junit/cdr/rep/");
        System.setProperty("asn1berrepository", userDir + "/test/junit/cdr/asn1berRep/");
        
        CommonMessagingAccessTest.setUp();
        
        BasicConfigurator.configure();

        smsClientStub = new SMSClientStub();
        SMSOut.setSmsClient(smsClientStub);
        
        // Start NTF
        ntf = new NtfMain();
    	Config.updateCfg();
    }

//    @AfterClass
    static public void tearDown() {
        System.out.println("tearDown");
/*        try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
*/
		
        CommonMessagingAccessTest.stop();
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    public void testInjectNTFMcnnEvent_Ok() throws Exception {

    	File f = new File("mcn.txt");
    	if(!f.exists()){
    		f.createNewFile();
    	}
    	responseReceived_OK = 0;

    	//Set MfsEventManager stub
        MfsEventManagerStub mfsEventManager = new MfsEventManagerStub();
        TrafficEvent event = new TrafficEvent();
        event.setProperty(MoipMessageEntities.MCN_TIMESTAMP_PROPERTY, new Long(System.currentTimeMillis()).toString());
        event.setProperty(MoipMessageEntities.MCN_CALLING_NUMBER_PROPERTY, "333333333");
        TrafficEvent event2 = new TrafficEvent();
        event2.setProperty(MoipMessageEntities.MCN_TIMESTAMP_PROPERTY, new Long(System.currentTimeMillis()).toString());
        event2.setProperty(MoipMessageEntities.MCN_CALLING_NUMBER_PROPERTY, "222222222");
        TrafficEvent event3 = new TrafficEvent();
        event3.setProperty(MoipMessageEntities.MCN_TIMESTAMP_PROPERTY, new Long(System.currentTimeMillis()).toString());
        event3.setProperty(MoipMessageEntities.MCN_CALLING_NUMBER_PROPERTY, "222222222");        
        
        mfsEventManager.setTrafficEvents(new TrafficEvent[]{event, event2, event3});
        SlamdownList.setMfsEventManager(mfsEventManager);
        SlamdownSender.setMfsEventManager(mfsEventManager);

        //Setup subscriber MCD profile
        McdStub directoryAccess = new McdStub();
        CommonMessagingAccessTest.setMcdStub(directoryAccess);
        
        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(2000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(1, responseReceived_OK);
        
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    public void testInjectNTFMcnEvent_1RECIPIENT_2SMSOk() throws Exception {

    	File f = new File("slam.txt");
    	if(!f.exists()){
    		f.createNewFile();
    	}
    	responseReceived_OK = 0;

    	//Set MfsEventManager stub
        MfsEventManagerStub mfsEventManager = new MfsEventManagerStub();
        int size = 10;
        TrafficEvent[] events = new TrafficEvent[size];
        for(int i=0;i<size;i++){
        	events[i] = new TrafficEvent();
            events[i].setProperty(MoipMessageEntities.MCN_TIMESTAMP_PROPERTY, new Long(System.currentTimeMillis()).toString());
            events[i].setProperty(MoipMessageEntities.MCN_CALLING_NUMBER_PROPERTY, new Integer(i).toString());        	
        }
        mfsEventManager.setTrafficEvents(events);
        SlamdownList.setMfsEventManager(mfsEventManager);    
        SlamdownSender.setMfsEventManager(mfsEventManager);

        //Setup subscriber MCD profile
        McdStub directoryAccess = new McdStub();
        CommonMessagingAccessTest.setMcdStub(directoryAccess);
        
        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(2000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(1, responseReceived_OK);
    }
    
    class MyListener implements EventSentListener {

        @Override
        public void sendStatus(NtfEvent event, SendStatus status)
        {
            NtfEventHandler ntfEventHandler = (NtfEventHandler)NtfEventHandlerRegistry.getNtfEventReceiver();

            try {
                if (status.equals(EventSentListener.SendStatus.OK) && event.isEventServiceType(MoipMessageEntities.SERVICE_TYPE_MCN)) {
                    responseReceived_OK++;
                    return;
                }

                NtfRetryHandling handler = NtfEventHandlerRegistry.getEventHandler(event.getEventServiceTypeKey());
                handler.cancelEvent(event.getReferenceId());
            } finally {
                ntfEventHandler.decreaseNumberOfNotificationCurrent();
            }
        }
    }

    private SendMessageReq getSendMessageReq() {
        SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = "im";
        req.destRcptID.value = "123456";

        req.rMsa.value = "";
        req.rMsgID.value = "";
        req.oMsa.value = "";
        req.oMsgID.value = "";

        req.eventType.value = MoipMessageEntities.SERVICE_TYPE_MCN;
        req.eventID.value = "id";

        HashMap<String, String> extra = new HashMap<String, String>();
        extra.put(MoipMessageEntities.MCN_EVENT_FILE_PROPERTY, "mcn.txt");

        req.extraValue = extra;
        req.eventID.value = "myid";
        return req;
    }
}
