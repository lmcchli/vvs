package com.abcxyz.services.moip.ntf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.MfsConfiguration;
import com.abcxyz.messaging.mfs.MsgStoreServer;
import com.abcxyz.messaging.mfs.MsgStoreServerFactory;
import com.abcxyz.messaging.mfs.data.StateFileHandle;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateFile;
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
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.CommonMessagingAccessTest;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NtfMain;
import com.mobeon.ntf.out.sms.SMSOut;

public class NotificationMWIOffTest {

    private int responseReceived_OK = 0;
    private int responseReceived_NORMAL_RETRY = 0;
    private static SMSClientStub smsClientStub = null;
    private static NtfMain ntf;
    private static McdStub dirAccess = new McdStub();
    private static CommonMessagingAccess commonMessagingAccess = null;
    static private String strDirectoy = "C:\\opt\\moip\\mfs";
    private static MSA omsa;
    private static MSA user;

    @BeforeClass
    static public void startup() throws Exception {
    	
    	String userDir = System.getProperty("user.dir");
        System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
        System.setProperty("ntfHome", userDir + "/test/junit/" );        

        CommonMessagingAccessTest.setUp();
        
        omsa = MFSFactory.getMSA("491721092601", true);
    	user = MFSFactory.getMSA("491721092602", true);

        ntf = new NtfMain();
        
        //CommonOamManagerTest.initOam();
    	CommonMessagingAccess.setMcd(dirAccess);

        smsClientStub = new SMSClientStub();
        SMSOut.setSmsClient(smsClientStub);
    }

    @AfterClass
    static public void tearDown() {
        CommonMessagingAccessTest.stop();
    }

    
    @Test
    public void testInjectNTF_MWI_OFF_Event_Type_OK() throws Exception {
    	
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        

        SendMessageReq req = getSendMessageMWIOffReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(1000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
    }
    
    
    
    @Test
    public void testInjectNTF_MWI_OFF_Event_Type_FAILED() throws Exception {
    	
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_FAILED);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        

        SendMessageReq req = getSendMessageMWIOffReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(1000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
    }
    
    @Test
    public void testInjectNTF_MWI_OFF_Event_Type_OK_DeliveryProfile() throws Exception {
    	
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsClientResponse(Constants.FEEDBACK_STATUS_OK);
        
        
        dirAccess.addSubscriberProfileAttribute(DAConstants.ATTR_COS_IDENTITY, "cos:1");
        dirAccess.addSubscriberProfileAttribute(DAConstants.ATTR_NOTIF_NUMBER, "123456");
        dirAccess.addSubcriberProfileIdentity(URI.create("msid:111112462ffff"));
        dirAccess.addSubscriberProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE, "123456,2121;MWI,SMS;M");

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        

        SendMessageReq req = getSendMessageMWIOffReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(1000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
    }
    
    @Test
    public void testMWI_OFF_MWICount_ON_ONE_NEW_MESSAGE() throws Exception {
    	
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;
                
        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        
        storeNewMfsMessage(MoipMessageEntities.MESSAGE_NEW, false);
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        
        SendMessageReq req = getSendMessageMWIOffReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(1000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
    }
    
    @Test
    public void testMWI_OFF_MWICount_ON_TWO_NEW_MESSAGES() throws Exception {
        
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;
                
        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        
        storeNewMfsMessage(MoipMessageEntities.MESSAGE_NEW, true);
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        
        SendMessageReq req = getSendMessageMWIOffReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(1000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
    }
    
    @Test
    public void testMWI_OFF_MWICount_ON_NO_NEW_MESSAGES() throws Exception {
    	
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;
        
        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        storeNewMfsMessage(MoipMessageEntities.MESSAGE_READ, false);
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        

        SendMessageReq req = getSendMessageMWIOffReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(1000);

        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
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


    private SendMessageReq getSendMessageMWIOffReq() {
        SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = "im";
        req.destRcptID.value = "tel:+123456";

        req.rMsa.value = user.toString();
        req.rMsgID.value = "";
        req.oMsa.value = omsa.toString();
        req.oMsgID.value = "";

        req.eventType.value = MoipMessageEntities.SERVICE_TYPE_MWI_OFF;
        req.eventID.value = "id";

        HashMap<String, String> extra = new HashMap<String, String>();
        extra.put("BarFoo", "bar");

        req.extraValue = extra;
        req.eventID.value = "myid";
        return req;
    }
    
    private void storeNewMfsMessage(String messageState, boolean flag) throws Exception {
        
    	deleteDirectory(new File(strDirectoy + "\\internal"));

    	CommonMessagingAccess.setMcd(new McdStub());
        commonMessagingAccess = CommonMessagingAccess.getInstance();
    	
        final ConfigManager mfsConfig = MfsConfiguration.getInstance();
        mfsConfig.setParameter(MfsConfiguration.MfsRootPath, strDirectoy);
        commonMessagingAccess.reInitializeMfs(mfsConfig);
    	MsgStoreServer mfs = MsgStoreServerFactory.getMfsStoreServer();

		//create voice new,
		StateFile state = new StateFile(omsa, user,
				MFSFactory.getAnyOmsgid("491721092600", "mas"),
				MFSFactory.getAnyRmsgid("491721092605") );

		state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, messageState);
		state.setC1Attribute(Container1.Message_class, "voice");
		StateFileHandle handle = mfs.createState(state);
		handle.release();
    	if (flag) {
    	    state = new StateFile(omsa, user,
                    MFSFactory.getAnyOmsgid("491721092600", "mas"),
                    MFSFactory.getAnyRmsgid("491721092605") );

            state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, messageState);
            state.setC1Attribute(Container1.Message_class, "voice");
            handle = mfs.createState(state);
            handle.release();
    	}
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
