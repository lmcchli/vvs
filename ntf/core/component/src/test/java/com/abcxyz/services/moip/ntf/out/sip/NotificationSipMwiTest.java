package com.abcxyz.services.moip.ntf.out.sip;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.IdGenerationException;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.message.MfsFileFolder;
import com.abcxyz.messaging.mfs.message.MfsStateFolderType;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.ntf.coremgmt.NtfCmnManager;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.SipMwiEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackHandler;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.event.SipMwiEvent;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.CommonMessagingAccessTest;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.out.sip.SIPOut;
import com.mobeon.ntf.userinfo.SIPFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.mcd.McdUserInfo;

public class NotificationSipMwiTest {

	static UserInfo userInfo;
	static UserMailbox inbox;

	static SIPOut sipOut;
	static SIPFilterInfo sipFilter;
	static SipMwiCallerStub callerStub;
	static String myNumber = "12345";
	static String myDialNumber = "34567";
	static MessageInfo myMsg;
	static FallbackHandler fallbackHandler = null;
	static IMfsEventManager mfsEventManager = null; 

    @BeforeClass
    static public void setup() throws ConfigurationException, ConfigurationDataException, IdGenerationException {

        try {
            CommonTestingSetup.setup();
            System.setProperty("-Dabcxyz.mfs.userdir.create", "true");

            McdStub directoryAccess = new McdStub();
            directoryAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "Name=sipmwi;Active=yes;Notify=yes;ValidTime=Always;Priority=1;CriteriaMsgHighPriority=no;MsgDepositType=Voice,Video;NotifType=MWI;NotifContentSMS=Subject;NotifContentEML=Subject;NotifContentMWI=true;CriteriaTelephoneFrom=");
            directoryAccess.addCosProfileAttribute(DAConstants.ATTR_SERVICES, "msgtype_voice");
            directoryAccess.addCosProfileAttribute(DAConstants.ATTR_CN_SERVICES, "moip");
            directoryAccess.addCosProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE, "NotifType=MWI;MobileNumber="+myDialNumber+";IPNumber=;Email=");
            
            directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_NOTIF_NUMBER, myDialNumber);
            directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE, "NotifType=MWI;MobileNumber="+myDialNumber+";IPNumber="+myDialNumber+";Email=");

            
            CommonMessagingAccessTest.setMcdStub(directoryAccess);
            CommonMessagingAccessTest.setUp();

            MfsEventManager.setDirectoryAccess(directoryAccess);
            CommonMessagingAccess.setMcd(directoryAccess);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ConfigurationException(ex.getMessage());
        }
        Config.loadCfg();
        Config.setCfgVar(NotificationConfigConstants.SIP_MWI_NOTIFY_RETRY_SCHEMA, "1 CONTINUE");
        Config.setCfgVar(NotificationConfigConstants.SIP_MWI_EXPIRE_TIME_IN_MIN, "2");
        Config.setCfgVar(NotificationConfigConstants.SIP_MWI_REMINDER_INTERVAL_IN_MIN, "3");
        Config.setCfgVar(NotificationConfigConstants.SIP_MWI_REMINDER_ENABLED, "yes");

        SipMwiEventHandler sipMwiEventHandler = new SipMwiEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(sipMwiEventHandler);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(sipMwiEventHandler.getEventServiceName(), sipMwiEventHandler); //register as event sent listener

        // Register event handlers for Fallbak mechanism
        FallbackEventHandler fallbackEventHandler = new FallbackEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(fallbackEventHandler);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(fallbackEventHandler.getEventServiceName(), fallbackEventHandler); //register as event sent listener
        

        mfsEventManager = new MfsEventManager();
    
        callerStub = new SipMwiCallerStub();
        SIPOut.setSipMwiCaller(callerStub);
        SIPOut.setNoMerNotification();

        sipOut = new SIPOut();
        Properties props = new Properties();
        props.put("MWI", "i");
    
        sipFilter = new SIPFilterInfo(new String[]{myDialNumber});
        userInfo = new MyUserInfo();
        inbox = new UserMailbox(1, 1, 1, 1, 1, 1, 1, 1, true);
    
        //deposit one message
        myMsg = new MessageInfo();
        myMsg.omsa = new MSA ("omsa"); //OMSA not used any way
        myMsg.rmsa = MFSFactory.getMSA(myNumber, true); //
        myMsg.omsgid = MFSFactory.getAnyOmsgid(myNumber, "voice"); //
        myMsg.rmsgid = MFSFactory.getAnyRmsgid(myNumber); //

        StateFile sfile = new StateFile(myMsg);
        sfile.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
        sfile.setC1Attribute(Container1.Message_class, ServiceName.VOICE);
        try {
            CommonMessagingAccess.getMfs().createState(sfile);
		} catch (MsgStoreException e) {
			e.printStackTrace();
		}
    }

    @Before
    public void setupBeforeTC() {
        callerStub.reset();
    	SipMwiEventHandler eventHandler = (SipMwiEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SIPMWI.getName());
    	eventHandler.reset();
    }

    @AfterClass
    static public void tearDown() throws InterruptedException {
    	Thread.sleep(1);
    	NtfCmnManager.getInstance().stop();
		String dir = MfsFileFolder.getStateDir(myMsg.omsa, myMsg.rmsa, myMsg.omsgid, myMsg.rmsgid, MfsStateFolderType.INBOX);
		CommonTestingSetup.deleteDir(dir);
    }
    
    @Test
    public void testNotificationSipMwi_200OK() throws InterruptedException {

        // Sip Mwi notification
        SipMwiEvent sipMwiEvent = new SipMwiEvent(myNumber, myDialNumber, myMsg);
        int count = sipOut.handleMWI(userInfo, sipFilter, inbox, myNumber, myMsg, sipMwiEvent);

        assertTrue (count > 0);
        assertTrue(null != mfsEventManager.getFilePathsNameStartingWith(myDialNumber, SipMwiEventHandler.SIPMWI_STATUS_FILE));
        Thread.sleep(3000);

        // Simulate XMP by sending back specific answer
        callerStub.sendOk(myDialNumber);
        Thread.sleep(1000);

        assertEquals(1, callerStub.getCallMade());
        SipMwiEventHandler sipMwiEventHandler = (SipMwiEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SIPMWI.getName());
        sipMwiEventHandler.retrieveSchedulerEventIdsPersistent(sipMwiEvent);
        sipMwiEventHandler.cancelReminderEvent(sipMwiEvent);
        assertTrue(sipMwiEvent.isSchedulerIdEmpty());
    }

    @Test
    public void testNotificationSipMwi_XMPRetryTime() throws InterruptedException {
        // Sip Mwi notification
        SipMwiEvent sipMwiEvent = new SipMwiEvent(myNumber, myDialNumber, myMsg);
        int count = sipOut.handleMWI(userInfo, sipFilter, inbox, myNumber, myMsg, sipMwiEvent);
        assertTrue (count == 1);
        assertTrue(null != mfsEventManager.getFilePathsNameStartingWith(myDialNumber, SipMwiEventHandler.SIPMWI_STATUS_FILE));
        Thread.sleep(3000);

        // Simulate XMP by sending back specific answer
        callerStub.sendResponseWithRetryTime(myDialNumber, 60);
        Thread.sleep(65000);

        callerStub.sendOk(myDialNumber);
        Thread.sleep(1000);

        assertEquals(2, callerStub.getCallMade());
        
        SipMwiEventHandler sipMwiEventHandler = (SipMwiEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SIPMWI.getName());
        sipMwiEventHandler.retrieveSchedulerEventIdsPersistent(sipMwiEvent);
        sipMwiEventHandler.cancelEvent(sipMwiEvent);
        sipMwiEventHandler.cancelReminderEvent(sipMwiEvent);
        
        assertEquals(1, sipMwiEventHandler.getNumOfFiredNotifEvent());
        
        // Cancel the future event in Scheduler
    }

    @Test
    public void testNotificationSipMwi_XMPTemporaryError() throws InterruptedException {
        // Sip Mwi notification
        SipMwiEvent sipMwiEvent = new SipMwiEvent(myNumber, myDialNumber, myMsg);
        int count = sipOut.handleMWI(userInfo, sipFilter, inbox, myNumber, myMsg, sipMwiEvent);
        assertTrue (count == 1);
        assertTrue(null != mfsEventManager.getFilePathsNameStartingWith(myDialNumber, SipMwiEventHandler.SIPMWI_STATUS_FILE));
        Thread.sleep(3000);

        // Simulate XMP by sending back specific answer
        callerStub.sendResponse(myDialNumber, 450);
        Thread.sleep(65000);

        callerStub.sendOk(myDialNumber);
        Thread.sleep(1000);

        assertEquals(2, callerStub.getCallMade());
        
        SipMwiEventHandler sipMwiEventHandler = (SipMwiEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SIPMWI.getName());
        sipMwiEventHandler.retrieveSchedulerEventIdsPersistent(sipMwiEvent);
        sipMwiEventHandler.cancelEvent(sipMwiEvent);
        sipMwiEventHandler.cancelReminderEvent(sipMwiEvent);
        
        assertEquals(1, sipMwiEventHandler.getNumOfFiredNotifEvent());
    }

    @Test
    public void testNotificationSipMwi_XMPPermanentError() throws InterruptedException {

        // Sip Mwi notification
        SipMwiEvent sipMwiEvent = new SipMwiEvent(myNumber, myDialNumber, myMsg);
        int count = sipOut.handleMWI(userInfo, sipFilter, inbox, myNumber, myMsg, sipMwiEvent);
        assertTrue (count == 1);
        assertTrue(null != mfsEventManager.getFilePathsNameStartingWith(myDialNumber, SipMwiEventHandler.SIPMWI_STATUS_FILE));
        Thread.sleep(3000);

        // Simulate XMP by sending back specific answer
        callerStub.sendResponse(myDialNumber, 302);
        Thread.sleep(1000);

        assertEquals(1, callerStub.getCallMade());
        SipMwiEventHandler sipMwiEventHandler = (SipMwiEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SIPMWI.getName());
        sipMwiEventHandler.retrieveSchedulerEventIdsPersistent(sipMwiEvent);
        
        assertTrue(sipMwiEvent.isSchedulerIdEmpty());
        sipMwiEventHandler.cancelReminderEvent(sipMwiEvent);
    }

    @Test
    public void testNotificationSipMwi_EventFired() throws InterruptedException {

        // Sip Mwi notification
        SipMwiEvent sipMwiEvent = new SipMwiEvent(myNumber, myDialNumber, myMsg);
        int count = sipOut.handleMWI(userInfo, sipFilter, inbox, myNumber, myMsg, sipMwiEvent);
        assertTrue (count == 1);
        String[] originalFiles = mfsEventManager.getFilePathsNameStartingWith(myDialNumber, SipMwiEventHandler.SIPMWI_STATUS_FILE); 
        assertTrue(originalFiles != null);

        // Do not answer anything from XMP (backup event from scheduler will then kick-in)
        Thread.sleep(65000);

        // Simulate XMP by sending back specific answer
        callerStub.sendOk(myDialNumber);
        Thread.sleep(1000);

        SipMwiEventHandler sipMwiEventHandler = (SipMwiEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SIPMWI.getName());
        sipMwiEventHandler.retrieveSchedulerEventIdsPersistent(sipMwiEvent);
        assertTrue(sipMwiEvent.isSchedulerIdEmpty());
    }

    @Test
    public void testNotificationSipMwi_200OK_Reminder200OK() throws InterruptedException {

        // Sip Mwi notification
        SipMwiEvent sipMwiEvent = new SipMwiEvent(myNumber, myDialNumber, myMsg);
        int count = sipOut.handleMWI(userInfo, sipFilter, inbox, myNumber, myMsg, sipMwiEvent);

        assertTrue (count > 0);
        assertTrue(null != mfsEventManager.getFilePathsNameStartingWith(myDialNumber, SipMwiEventHandler.SIPMWI_STATUS_FILE));
        Thread.sleep(3000);

        // Simulate XMP by sending back specific answer
        callerStub.sendOk(myDialNumber);
        Thread.sleep(1000);

        assertEquals(1, callerStub.getCallMade());
        
        SipMwiEventHandler sipMwiEventHandler = (SipMwiEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SIPMWI.getName());
        sipMwiEventHandler.retrieveSchedulerEventIdsPersistent(sipMwiEvent);
        
        assertTrue(sipMwiEvent.isSchedulerIdEmpty());
        assertTrue(!sipMwiEvent.isReminderSchedulerIdEmpty());
        
        // Do not answer anything from XMP (backup event from scheduler will then kick-in)
        Thread.sleep((3*60000)+5000);
        
        String oldTriggerId = sipMwiEvent.getReminderTriggerReferenceId();
       
        // Simulate XMP by sending back specific answer
        callerStub.sendOk(myDialNumber);
        Thread.sleep(1000);
        
        assertEquals(2, callerStub.getCallMade());
        assertEquals(1, sipMwiEventHandler.getNumOfFiredNotifEvent());
        
        sipMwiEventHandler.retrieveSchedulerEventIdsPersistent(sipMwiEvent);
        assertTrue(sipMwiEvent.isSchedulerIdEmpty());
        assertTrue(!sipMwiEvent.isReminderSchedulerIdEmpty());
        
        System.out.println("NotificationSipMwiTest.testNotificationSipMwi_200OK_Reminder200OK():\n" +
        		"OldTriggerId: "+oldTriggerId+"\n" +
        		"NewTriggerId: "+sipMwiEvent.getReminderTriggerReferenceId());
        
        sipMwiEventHandler.cancelReminderEvent(sipMwiEvent);
    }

    @Test
    public void testNotificationSipMwi_200OK_ReminderEventFired() throws InterruptedException {

        // Sip Mwi notification
        SipMwiEvent sipMwiEvent = new SipMwiEvent(myNumber, myDialNumber, myMsg);
        int count = sipOut.handleMWI(userInfo, sipFilter, inbox, myNumber, myMsg, sipMwiEvent);

        assertTrue (count > 0);
        assertTrue(null != mfsEventManager.getFilePathsNameStartingWith(myDialNumber, SipMwiEventHandler.SIPMWI_STATUS_FILE));
        Thread.sleep(3000);

        // Simulate XMP by sending back specific answer
        callerStub.sendOk(myDialNumber);
        Thread.sleep(1000);

        assertTrue(callerStub.getCallMade() == 1);
        
        SipMwiEventHandler sipMwiEventHandler = (SipMwiEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SIPMWI.getName());
        sipMwiEventHandler.retrieveSchedulerEventIdsPersistent(sipMwiEvent);
        
        assertTrue(sipMwiEvent.isSchedulerIdEmpty());
        assertTrue(!sipMwiEvent.isReminderSchedulerIdEmpty());
        
        // Do not answer anything from XMP (backup event from scheduler will then kick-in)
        Thread.sleep((3*60000)+5000);
       
        // Simulate XMP by sending back specific answer
        callerStub.sendOk(myDialNumber);
        Thread.sleep(1000);
        
        assertTrue(sipMwiEventHandler.getNumOfFiredNotifEvent() == 2);
        assertTrue(callerStub.getCallMade() == 3);
    }

    static class MyUserInfo extends McdUserInfo {

		MyUserInfo() {
			super.outdialNotification = true;
			super.cos = CommonMessagingAccess.getInstance().getMcd().lookupCos("");
		}

		public String getOutdialSchema() {
			return "default";
		}

		public String getTelephoneNumber() {
			return "123456";
		}
	}
}
