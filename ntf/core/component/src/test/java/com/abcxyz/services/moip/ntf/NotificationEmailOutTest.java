package com.abcxyz.services.moip.ntf;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.makeThreadSafe;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;

import junitx.util.PrivateAccessor;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mrd.data.Reason;
import com.abcxyz.messaging.mrd.operation.SendMessageReq;
import com.abcxyz.messaging.mrd.operation.SendMessageResp;
import com.abcxyz.services.moip.ntf.coremgmt.EventSentListener;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.NtfMessageService;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryHandling;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.CommonMessagingAccessTest;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.common.email.EmailClient;
import com.mobeon.common.email.EmailResultHandler;
import com.mobeon.common.email.request.MimeContainer;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NtfMain;
import com.mobeon.ntf.out.email.EmailOut;

public class NotificationEmailOutTest {

	private int responseReceived_OK = 0;
    private int responseReceived_NORMAL_RETRY = 0;
    private MessageInfo msgInfo = null;
    private static NtfMain ntf;
    private static McdStub dirAccess = new McdStub();
    private static CommonMessagingAccess commonMessagingAccess = null;
        
    static String componentName = "smtp";
    
    @BeforeClass
    static public void startup() throws Exception {
    	
    	String userDir = System.getProperty("user.dir");
        System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
        System.setProperty("ntfHome", userDir + "/test/junit/" );        

        dirAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "Name=sms;Active=yes;Notify=yes;ValidTime=Always;Priority=1;CriteriaMsgHighPriority=no;MsgDepositType=Voice;NotifType=EML;NotifContentEML=Subject;NotifContentMWI=false;CriteriaTelephoneFrom=");
        dirAccess.addCosProfileAttribute(DAConstants.ATTR_SERVICES, "msgtype_voice");
        dirAccess.addSubscriberProfileAttribute(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE, "10");
        dirAccess.addSubscriberProfileAttribute(DAConstants.ATTR_NOTIF_NUMBER, "addr@host.domain");
        dirAccess.addSubcriberProfileIdentity(URI.create("msid:111112462ffff"));
        dirAccess.addSubscriberProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE, "Email=a.b@abcxyz.com;NotifType=EML;MobileNumber=;IPNumber=");
                 
        CommonMessagingAccessTest.setMcdStub(dirAccess);
        CommonMessagingAccessTest.setUp();

    	ntf = new NtfMain();
    }

    @AfterClass
    static public void tearDown() {
        CommonMessagingAccessTest.stop();
    }
    
    
    @Test
    public void testEmail_OK() throws Exception {
        
        // Wait for NTF to boot (30 seconds)
        Thread.sleep(30000);

        ntf.getEventHandler().resetNumberOfNotification();
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;   
        Config.loadCfg();
                
        EmailClient emailClientMock = createNiceMock(EmailClient.class);
        makeThreadSafe(emailClientMock, true);        
        PrivateAccessor.setField(EmailOut.get(), "emailClient", emailClientMock);
        expect(emailClientMock.sendEmailMessage((MimeContainer) anyObject(), 
                 anyInt(), 
                anyInt(), 
                (EmailResultHandler)anyObject(), 
                anyInt())).andStubReturn(EmailClient.SEND_OK);        
        
        replay(emailClientMock);
        
        storeMfsMessage();

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)    
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(2000);

        verify(emailClientMock);
        
        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
    }
    
    @Test
    public void testEmail_OK_WITHDeliveryProfile() throws Exception {
        ntf.getEventHandler().resetNumberOfNotification();
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;
        
        Config.loadCfg();
        
        //add a delivery profile for the email notification
        dirAccess.addSubscriberProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE, "addr1@host.domain,addr2@host.domain;EML;M");
        
        EmailClient emailClientMock = createNiceMock(EmailClient.class);
	    PrivateAccessor.setField(EmailOut.get(), "emailClient", emailClientMock);	    
        makeThreadSafe(emailClientMock, true);
        
        expect(emailClientMock.sendEmailMessage((MimeContainer) anyObject(), 
                 anyInt(), 
                anyInt(), 
                (EmailResultHandler)anyObject(), 
                anyInt())).andStubReturn(EmailClient.SEND_OK);
                
        replay(emailClientMock);
        
        storeMfsMessage();

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)        
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(2000);

        verify(emailClientMock);
        
        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);
        assertEquals(responseReceived_NORMAL_RETRY, 0);
    }
    
    @Test
    public void testEmail_FAILED() throws Exception {
        ntf.getEventHandler().resetNumberOfNotification();
        responseReceived_OK = 0;
        responseReceived_NORMAL_RETRY = 0;
        
        Config.loadCfg();
        
		EmailClient emailClientOutMock = createNiceMock(EmailClient.class);
	    PrivateAccessor.setField(EmailOut.get(), "emailClient", emailClientOutMock);

        makeThreadSafe(emailClientOutMock, true);
        expect(emailClientOutMock.sendEmailMessage((MimeContainer) anyObject(), 
                 anyInt(), 
                anyInt(), 
                (EmailResultHandler)anyObject(), 
                anyInt())).andStubReturn(EmailClient.SEND_FAILED);
  
        replay(emailClientOutMock);
        
        storeMfsMessage();
        
        // Inject the NtfEvent into NTF (simulating MRD calling NTF)        
        MyListener listener = new MyListener();

        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        

        SendMessageReq req = getSendMessageReq();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(2000);

        verify(emailClientOutMock);
        // Receiving the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(responseReceived_OK, 1);// EMAIL failed returns as OK (to cleanup the scheduler)
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


    private SendMessageReq getSendMessageReq() {
        SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = "im";
        req.destRcptID.value = "123456";

        req.rMsa.value = msgInfo.rmsa.toString();
        req.rMsgID.value = msgInfo.rmsgid.toString();
        req.oMsa.value = msgInfo.omsa.toString();
        req.oMsgID.value = msgInfo.omsgid.toString();

        req.eventType.value = NtfEventTypes.DEFAULT_NTF.getName();
        req.eventID.value = "id";

        HashMap<String, String> extra = new HashMap<String, String>();
        extra.put("BarFoo", "bar");

        req.extraValue = extra;
        req.eventID.value = "myid";
        return req;
    }
    
    private void storeMfsMessage() throws Exception {

        Calendar now = Calendar.getInstance();

        CommonMessagingAccess.setMcd(new McdStub());
        commonMessagingAccess = CommonMessagingAccess.getInstance();

        // create and store messages for testing
        final Container1 c1_1 = new Container1();
        c1_1.setFrom("491721092600");
        c1_1.setTo("491721092605");
        c1_1.setSubject("Voice message from Mobeon Office");
        c1_1.setMsgClass("voice");
        c1_1.setDateTime(now.getTimeInMillis());
        final Container2 c2_1 = new Container2();
        
        //String textMsg = header + voiceheader + voicebody;
        String textMsg = "Base 64 encoded audio";
        
		MsgBodyPart part = new MsgBodyPart("AUDIO/wav; name=message.wav", textMsg.getBytes(), true);
		part.addPartHeader("Content-Description", "Cisco voice Message   (20 seconds )");
		part.addPartHeader("Content-Disposition", "inline; voice=Voice-Message; filename=\"message .wav\"");

		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  part;
        
        msgInfo = commonMessagingAccess.storeMessageTest(c1_1, c2_1, c3Parts, new StateAttributes());
    }
}
