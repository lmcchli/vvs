package com.abcxyz.services.moip.ntf.odl;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.abcxyz.messaging.cdrgen.config.CDRGenConfig;
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.mnr.SubscriberInfo;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.impl.StdoutLogger;
import com.abcxyz.messaging.common.ssmg.AnyTimeInterrogationResult;
import com.abcxyz.messaging.common.ssmg.interfaces.AlertSCHandler;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.IdGenerationException;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccessSubscriber;
import com.abcxyz.services.moip.common.directoryaccess.MoipProfile;
import com.abcxyz.services.moip.common.ss7.ISs7Manager;
import com.abcxyz.services.moip.common.ss7.Ss7Exception;
import com.abcxyz.services.moip.ntf.coremgmt.NtfCmnManager;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.OdlEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.OdlEventHandlerCall;
import com.abcxyz.services.moip.ntf.coremgmt.OdlEventHandlerLogin;
import com.abcxyz.services.moip.ntf.coremgmt.OdlEventHandlerStart;
import com.abcxyz.services.moip.ntf.coremgmt.OdlEventHandlerWait;
import com.abcxyz.services.moip.ntf.coremgmt.OdlEventHandlerWaitOn;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.event.OdlEvent;
import com.abcxyz.services.moip.ntf.out.sms.SMSClientStub;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.CommonMessagingAccessTest;
import com.mobeon.common.cmnaccess.DirAccessSubscriberStub;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.logging.LogAgentFactory;
import com.mobeon.common.sms.SMSClient;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.out.outdial.IEventStore;
import com.mobeon.ntf.out.outdial.OdlFactory;
import com.mobeon.ntf.out.outdial.OutdialNotificationOut;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.userinfo.OdlFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.mcd.McdUserInfo;

public class OdlOutTest {

	/**
	 * The logger object
	 */
	private static final LogAgent logger = LogAgentFactory.getLogAgent(OdlOutTest.class);

	static UserInfo userInfo;
	static UserMailbox inbox;

	static OutdialNotificationOut odlout;
	static OdlFilterInfo odlFilter;
	static OdlCallerStub callerStub;
	static String myNumber = "12345";
	static String myDialNumber = "34567";
	static MessageInfo myMsg;
	static IEventStore eventStore;
    private static SMSClientStub smsClientStub = null;
    public static McdStubOutdial directoryAccess;

    /**
     * Initialiser for the CDR generator Library.
     */
    static class CdrGenInitializer {
    	/**
    	 * Sets the basic configuration for the cdrgen library.
    	 * <p>
    	 * Sets the configuration file path to point to the backend configuration directory:
    	 * <i>../ipms_sys2/backend/cfg/cdrgen.</i>
    	 * </p>
    	 * @throws Exception exception 
    	 */
    	public static void setup() throws Exception {
            String userDir = System.getProperty("user.dir");
            File path = new File(userDir + "/../ipms_sys2/backend/cfg/cdrgen");
            // Note: CDRGenConfig.PROP_NAME_ROOTCONFIGDIRECTORY contains upper and lower cases,
            // but MFS uses a lower case version only.
    		System.setProperty(CDRGenConfig.PROP_NAME_ROOTCONFIGDIRECTORY.toLowerCase(), path.getAbsolutePath());
    	}
    }

    static class TestOdlFactory extends OdlFactory {

    	/**
    	 * Constructs a ODL event factory that always returns the same
    	 * event store.<br/>
    	 * This instance registers itself as the factory instance.
    	 */
    	TestOdlFactory() {
    		OdlFactory defaultFactory = OdlFactory.getInstance();
    		eventStore = defaultFactory.createEventStore();
    		setInstance(this);
    	}

		@Override
		public IEventStore createEventStore() {
			return eventStore;
		}
    }

	@BeforeClass
    static public void setup() throws ConfigurationException, ConfigurationDataException, IdGenerationException {

	    // Create an ODL factory that becomes the factory instance
	    // returned by OdlFactory.getInstance();
	    new TestOdlFactory();

	    String userDir = System.getProperty("user.dir");
	    System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
	    System.setProperty("ntfHome", userDir + "/test/junit/" );

	    try {
	        // Initialize the CDRGen interface
	        CdrGenInitializer.setup();

	        CommonTestingSetup.setup();
	        System.setProperty("-Dabcxyz.mfs.userdir.create", "true");
            
	        BasicConfigurator.configure();
            
	        smsClientStub = new SMSClientStub();
	        SMSOut.setSmsClient(smsClientStub);
            
            directoryAccess = new McdStubOutdial();
	        CommonMessagingAccessTest.setMcdStub(directoryAccess);
	        CommonMessagingAccessTest.setUp();
	        MfsEventManager.setDirectoryAccess(directoryAccess);
	        CommonMessagingAccess.setMcd(directoryAccess);

	        ISs7Manager ss7mgrStub = new Ss7mgrStub();
	        CommonMessagingAccess.setSs7Manager(ss7mgrStub);

	        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_CN_SERVICES, "moip");
	        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_CN_SERVICES, "moip");
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new ConfigurationException(ex.getMessage());
	    }

//    	NtfCmnManager.getInstance().start();
	    Config.loadCfg();

	    OdlEventHandler odlHandler = new OdlEventHandler();
	    NtfEventHandlerRegistry.registerEventHandler(odlHandler);
	    NtfEventHandlerRegistry.registerEventSentListener(odlHandler.getEventServiceName(), odlHandler);

        OdlEventHandlerStart odlHandlerStart = new OdlEventHandlerStart();
        NtfEventHandlerRegistry.registerEventHandler(odlHandlerStart);
        NtfEventHandlerRegistry.registerEventSentListener(odlHandlerStart.getEventServiceName(), odlHandlerStart);

        OdlEventHandlerLogin odlHandlerLogin = new OdlEventHandlerLogin();
        NtfEventHandlerRegistry.registerEventHandler(odlHandlerLogin);
        NtfEventHandlerRegistry.registerEventSentListener(odlHandlerLogin.getEventServiceName(), odlHandlerLogin);

        OdlEventHandlerWaitOn odlHandlerWaitOn = new OdlEventHandlerWaitOn();
        NtfEventHandlerRegistry.registerEventHandler(odlHandlerWaitOn);
        NtfEventHandlerRegistry.registerEventSentListener(odlHandlerWaitOn.getEventServiceName(), odlHandlerWaitOn);

        OdlEventHandlerWait odlHandlerWait = new OdlEventHandlerWait();
        NtfEventHandlerRegistry.registerEventHandler(odlHandlerWait);
        NtfEventHandlerRegistry.registerEventSentListener(odlHandlerWait.getEventServiceName(), odlHandlerWait);

        OdlEventHandlerCall odlHandlerCall = new OdlEventHandlerCall();
        NtfEventHandlerRegistry.registerEventHandler(odlHandlerCall);
        NtfEventHandlerRegistry.registerEventSentListener(odlHandlerCall.getEventServiceName(), odlHandlerCall);

	    callerStub = new OdlCallerStub();
	    OutdialNotificationOut.setOdlCaller(callerStub);
	    OutdialNotificationOut.setMer(false);

	    odlout = new OutdialNotificationOut();
	    Properties props = new Properties();
	    props.put("ODL", "c");

	    odlFilter = new OdlFilterInfo(props, new String[]{myDialNumber});
	    userInfo = new MyUserInfo();
	    inbox = new UserMailbox(1, 1, 1, 1,0,0,0,0, true);

	    smsClientStub = new SMSClientStub();
	    SMSOut.setSmsClient(smsClientStub);

	    //deposit one message
	    myMsg = new MessageInfo();
	    myMsg.omsa = new MSA ("omsa"); //OMSA not used any way
	    myMsg.rmsa = MFSFactory.getGen2MSA(myNumber, true); //
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
    	OdlEventHandler eventHandler = (OdlEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.OUTDIAL.getName());
    	eventHandler.reset();

    	String privateFolder = CommonMessagingAccess.getInstance().getMoipPrivateFolder("111112462", true);
    	File eventDir = new File(privateFolder, myDialNumber + "/events");
    	File[] eventFiles = eventDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().startsWith("odl-");
			}
    	});
    	if (eventFiles != null) {
    		for (File file : eventFiles) {
    			logger.debug("Removing event file: " + file);
    			file.delete();
    		}
    	}
    }

    @AfterClass
    static public void tearDown() throws InterruptedException {
    	Thread.sleep(2000);
    	CommonMessagingAccessTest.stop();
    	NtfCmnManager.getInstance().stop();
		//String dir = MfsFileFolder.getStateDir(myMsg.omsa, myMsg.rmsa, myMsg.omsgid, myMsg.rmsgid);
		//CommonTestingSetup.deleteDir(dir);
    }

    @Test
    public void testOutdial_NewDeposit_WhileNothingPending() throws InterruptedException {

        // New deposit (level-2) to Outdial
        System.out.println("New deposit (level-2) to Outdial");
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
        int  count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);

        Thread.sleep(2000);
        callerStub.sendOk(myDialNumber);

        Thread.sleep(2000);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 1);
    }

    @Test
    public void testOutdial_SecondDeposit_WhileWaitingPhoneOn() throws InterruptedException {

        // Set the subscriber profile to use the PhoneOn
        directoryAccess.setSubscriberProfile(myNumber, true);

        // New deposit (level-2) to Outdial 
        System.out.println("New deposit (level-2) to Outdial");
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
        int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(2000);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 0);

        // Second deposit (level-2) to Outdial while already waiting for PhoneOn response from SMSc 
        System.out.println("Second deposit (level-2) to Outdial while waiting for Reminder");
        count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(2000);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 0);

        // Set the subscriber profile NOT to use the PhoneOn
        directoryAccess.setSubscriberProfile(myNumber, false);
    }

    @Test
    public void testOutdial_SecondDeposit_WhileWaitingCall() throws InterruptedException {

        // New deposit (level-2) to Outdial 
        System.out.println("New deposit (level-2) to Outdial");
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
        int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(2000);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 1);
        // No XMP answer sent to Outdial (call retry scheduler to kick-in) 

        // Second deposit (level-2) to Outdial while already waiting for Call 
        System.out.println("Second deposit (level-2) to Outdial while waiting for Reminder");
        count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(1000);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 1);
    }

    @Test
    public void testOutdial_SecondDeposit_WhileWaitingReminder() throws InterruptedException {

        // Set the Reminder feature Enabled
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_REMINDER_ENABLED, "yes");

        // Reset NTF to take the new configuration
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_REMINDER_INTERVAL_IN_MIN, "1");
        OdlEventHandler odlHandler = new OdlEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(odlHandler);
        NtfEventHandlerRegistry.registerEventSentListener(odlHandler.getEventServiceName(), odlHandler);
        odlout = new OutdialNotificationOut();

        // New deposit (level-2) to Outdial 
        System.out.println("New deposit (level-2) to Outdial");
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
        int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(1000);
        callerStub.sendOk(myDialNumber);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 1);
        Thread.sleep(1000);

        // Second deposit (level-2) to Outdial while waiting for Reminder 
        System.out.println("Second deposit (level-2) to Outdial while waiting for Reminder");
        count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(1000);
        callerStub.sendOk(myDialNumber);

        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 2);
        Thread.sleep(1000);
    }

    @Test
    public void testOutdial_SecondDepositFaulty_WhileWaitingReminder_IsReminder0() throws InterruptedException {

        // Set the Reminder feature Enabled
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_REMINDER_ENABLED, "yes");

        // Reset NTF to take the new configuration
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_REMINDER_INTERVAL_IN_MIN, "1");
        OdlEventHandler odlHandler = new OdlEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(odlHandler);
        NtfEventHandlerRegistry.registerEventSentListener(odlHandler.getEventServiceName(), odlHandler);
        odlout = new OutdialNotificationOut();

        // New deposit (level-2) to Outdial 
        System.out.println("New deposit (level-2) to Outdial");
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
        int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(1000);
        callerStub.sendOk(myDialNumber);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 1);
        Thread.sleep(1000);

        // Second deposit (level-2) to Outdial while waiting for Reminder 
        System.out.println("Second deposit (level-2) to Outdial while waiting for Reminder");
        count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(1000);

        // Inject a permanent XMP error for the second deposit
        callerStub.sendResponse(myDialNumber, 401);

        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 2);
        Thread.sleep(1000);
    }

    @Test
    public void testOutdial_SecondDepositFaulty_WhileWaitingReminder_IsReminder1() throws InterruptedException {

        // Set the Reminder feature Enabled
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_REMINDER_ENABLED, "yes");

        // Set the Reminder schema to be the first to kick-in
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_REMINDER_INTERVAL_IN_MIN, "1");
        OdlEventHandler odlHandler = new OdlEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(odlHandler);
        NtfEventHandlerRegistry.registerEventSentListener(odlHandler.getEventServiceName(), odlHandler);

        // Set the Start and Login schemas later not to kick-in
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_START_RETRY_SCHEMA, "5:TRY=3 CONTINUE");
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_LOGIN_RETRY_SCHEMA, "5:TRY=3 CONTINUE");
        OdlEventHandlerStart odlEventHandlerStart = new OdlEventHandlerStart();
        NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerStart);
        NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerStart.getEventServiceName(), odlEventHandlerStart);
        OdlEventHandlerLogin odlEventHandlerLogin = new OdlEventHandlerLogin();
        NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerLogin);
        NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerLogin.getEventServiceName(), odlEventHandlerLogin);

        // Reset NTF to take the new configuration
        odlout = new OutdialNotificationOut();

        // New deposit (level-2) to Outdial 
        System.out.println("New deposit (level-2) to Outdial");
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
        int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(1000);
        callerStub.sendOk(myDialNumber);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 1);

        // Wait at least 1 minute for the Reminder timer to retry
        System.out.println("Waiting 1 minute for the Reminder timer to kick-in");
        Thread.sleep(70000);

        // Inject an OK for the second notification on first deposit (isReminder is now TRUE)
        callerStub.sendOk(myDialNumber);
        Thread.sleep(2000);

        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 2);

        // assert that there is a rescheduled Reminder timer
    }

    @Test
    public void testOutdial_SecondDeposit_WhileWaitingLoggedin() throws InterruptedException {

        // Create a login file
        IMfsEventManager mfsEventManager = new MfsEventManager(CommonMessagingAccess.getInstance(), CommonMessagingAccess.getInstance().getMcd());
        try {
            mfsEventManager.createLoginFile(myNumber);

            // New deposit (level-2) to Outdial 
            System.out.println("New deposit (level-2) to Outdial");
            NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
            int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
            assertTrue (count > 0);

            Thread.sleep(2000);
            System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
            assertTrue(callerStub.getCallMade() == 0);

            // Second deposit (level-2) to Outdial while waiting LOGIN timer
            System.out.println("Second deposit (level-2) to Outdial while waiting for Reminder");
            count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
            assertTrue (count > 0);
            assertTrue(callerStub.getCallMade() == 0);

            Thread.sleep(2000);

        } catch (TrafficEventSenderException tese) {
            logger.error("Exception ", tese);
        } finally {
            try {
                mfsEventManager.removeLoginFile(myNumber);
            } catch (TrafficEventSenderException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testOutdial_Retry_Wait() throws InterruptedException {

        // New deposit (level-2) to Outdial 
        System.out.println("New deposit (level-2) to Outdial");
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
        int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(1000);

        // Inject a temporary XMP error for the Wait retry to kick-in (state.1.622 = 2/wait 120; call)
        callerStub.sendResponse(myDialNumber, 622);

        // Wait 75 seconds for the Wait to retry
        System.out.println("Waiting 75 seconds");
        Thread.sleep(75000);

        // Inject an OK for the second notification on first deposit
        callerStub.sendOk(myDialNumber);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 2);
    }

    @Test
    public void testOutdial_Retry_WaitSmall() throws InterruptedException {

        // New deposit (level-2) to Outdial 
        System.out.println("New deposit (level-2) to Outdial");
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
        int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(1000);

        // Inject a temporary XMP error for the Wait retry to kick-in (state.1.614 = 2/wait 10; call)
        callerStub.sendResponse(myDialNumber, 614);

        // Wait 15 seconds for the Wait to retry
        System.out.println("Waiting 15 seconds");
        Thread.sleep(15000);

        // Inject an OK for the second notification on first deposit
        callerStub.sendOk(myDialNumber);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 2);
    }

    @Test
    public void testOutdial_Retry_Call() throws InterruptedException {

        // New deposit (level-2) to Outdial 
        System.out.println("New deposit (level-2) to Outdial");
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
        int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(1000);

        // Wait 65 seconds for the Call to retry (because of no XMP response)
        System.out.println("Waiting 65 seconds");
        Thread.sleep(65000);

        // Inject an OK for the second notification on first deposit
        callerStub.sendOk(myDialNumber);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());

        // OdlCall stub has been called twice (getCallMade() == 2) even if it answered once. 
        assertTrue(callerStub.getCallMade() == 2);
    }

    @Test
    public void testOutdial_Retry_Reminder() throws InterruptedException {

        // Set the Reminder feature Enabled
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_REMINDER_ENABLED, "yes");

        // Set the Reminder schema to be the first to kick-in
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_REMINDER_INTERVAL_IN_MIN, "1");
        OdlEventHandler odlHandler = new OdlEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(odlHandler);
        NtfEventHandlerRegistry.registerEventSentListener(odlHandler.getEventServiceName(), odlHandler);

        // Set the Start and Login schemas later not to kick-in
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_START_RETRY_SCHEMA, "5:TRY=3 CONTINUE");
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_LOGIN_RETRY_SCHEMA, "5:TRY=3 CONTINUE");
        OdlEventHandlerStart odlEventHandlerStart = new OdlEventHandlerStart();
        NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerStart);
        NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerStart.getEventServiceName(), odlEventHandlerStart);
        OdlEventHandlerLogin odlEventHandlerLogin = new OdlEventHandlerLogin();
        NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerLogin);
        NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerLogin.getEventServiceName(), odlEventHandlerLogin);

        // Reset NTF to take the new configuration
        odlout = new OutdialNotificationOut();

        // New deposit (level-2) to Outdial 
        System.out.println("New deposit (level-2) to Outdial");
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
        int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(1000);

        // Inject a temporary XMP error for the call retry to kick-in (state.1.622 = 2/wait 120; call)
        callerStub.sendOk(myDialNumber);

        // Wait at least 1 minute for the Reminder timer to retry
        System.out.println("Waiting 1 minute");
        Thread.sleep(65000);

        // Inject an OK for the second notification on first deposit
        callerStub.sendOk(myDialNumber);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 2);
    }

    @Test
    public void testOutdial_Retry_Login() throws InterruptedException {

        IMfsEventManager mfsEventManager = new MfsEventManager(CommonMessagingAccess.getInstance(), CommonMessagingAccess.getInstance().getMcd());
        try {
            // Create a login file
            mfsEventManager.createLoginFile(myNumber);

            // New deposit (level-2) to Outdial 
            System.out.println("New deposit (level-2) to Outdial");
            NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
            int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
            assertTrue (count > 0);

            Thread.sleep(1000);
            System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
            assertTrue(callerStub.getCallMade() == 0);

            // Delete the login file
            mfsEventManager.removeLoginFile(myNumber);

            // Wait at least 1 minute for the Login timer to retry
            System.out.println("Waiting 1 minute");
            Thread.sleep(65000);

            // Inject an OK for the retry notification on first deposit
            callerStub.sendOk(myDialNumber);
            System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
            assertTrue(callerStub.getCallMade() == 1);

        } catch (TrafficEventSenderException tese) {
            logger.error("Exception ", tese);
        } finally {
            try {
                mfsEventManager.removeLoginFile(myNumber);
            } catch (TrafficEventSenderException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testOutdial_Expiry_Login() throws InterruptedException {

        IMfsEventManager mfsEventManager = new MfsEventManager(CommonMessagingAccess.getInstance(), CommonMessagingAccess.getInstance().getMcd());
        try {
            // Create a login file
            mfsEventManager.createLoginFile(myNumber);

            // Set the Start and Login schemas later not to kick-in
            Config.setCfgVar(NotificationConfigConstants.OUTDIAL_LOGIN_RETRY_SCHEMA, "1:try=1 CONTINUE");
            Config.setCfgVar(NotificationConfigConstants.OUTDIAL_LOGIN_EXPIRE_TIME_IN_MIN, "2");
            OdlEventHandlerLogin odlEventHandlerLogin = new OdlEventHandlerLogin();
            NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerLogin);
            NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerLogin.getEventServiceName(), odlEventHandlerLogin);

            // Reset NTF to take the new configuration
            odlout = new OutdialNotificationOut();

            // New deposit (level-2) to Outdial 
            System.out.println("New deposit (level-2) to Outdial");
            NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
            int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
            assertTrue (count > 0);

            Thread.sleep(1000);
            System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
            assertTrue(callerStub.getCallMade() == 0);

            // Wait at least 2 minutes for the Login timer to expire
            System.out.println("Waiting 2 minutes");
            Thread.sleep(125000);

            // Assert that a odllg-expir happended on the OdlWorker

            assertTrue(callerStub.getCallMade() == 0);

        } catch (TrafficEventSenderException tese) {
            logger.error("Exception ", tese);
        } finally {
            try {
                mfsEventManager.removeLoginFile(myNumber);
            } catch (TrafficEventSenderException e) {
                e.printStackTrace();
            }
        }
        
    }

    @Test
    public void testOutdial_Expiry_Call() {
        
        // Set the Start and Login schemas later not to kick-in
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_CALL_RETRY_SCHEMA, "1:try=1 CONTINUE");
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_CALL_EXPIRE_TIME_IN_MIN, "2");
        OdlEventHandlerCall odlEventHandlerCall = new OdlEventHandlerCall();
        NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerCall);
        NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerCall.getEventServiceName(), odlEventHandlerCall);

        // Reset NTF to take the new configuration
        odlout = new OutdialNotificationOut();

        // New deposit (level-2) to Outdial 
        System.out.println("New deposit (level-2) to Outdial");
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
        int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);

        // Wait 125 seconds for the Call to expire (because of no XMP response)
        System.out.println("Waiting 125 seconds");
        //Thread.sleep(125000);

        // OdlCall stub has been called twice (getCallMade() == 2) even if it answered once. 
        /**
         * Call has no expiry since it starts a odlst, then cancel it to schedule a odlcl
         * Meaning that odlcl will never increment its retries since it is re-initialised every time.
         */ 
        //assertTrue(callerStub.getCallMade() == 2);
    }

    @Test
    public void testOutdial_Expiry_Reminder() throws InterruptedException {

        // Set the Reminder feature Enabled
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_REMINDER_ENABLED, "yes");

        // Set the Start, Login and Reminder schemas later not to kick-in
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_REMINDER_INTERVAL_IN_MIN, "1");
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_REMINDER_EXPIRE_IN_MIN, "2");
        OdlEventHandler odlHandler = new OdlEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(odlHandler);
        NtfEventHandlerRegistry.registerEventSentListener(odlHandler.getEventServiceName(), odlHandler);
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_START_RETRY_SCHEMA, "5:TRY=3 CONTINUE");
        OdlEventHandlerStart odlEventHandlerStart = new OdlEventHandlerStart();
        NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerStart);
        NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerStart.getEventServiceName(), odlEventHandlerStart);
        Config.setCfgVar(NotificationConfigConstants.OUTDIAL_LOGIN_RETRY_SCHEMA, "5:TRY=3 CONTINUE");
        OdlEventHandlerLogin odlEventHandlerLogin = new OdlEventHandlerLogin();
        NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerLogin);
        NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerLogin.getEventServiceName(), odlEventHandlerLogin);

        // Reset NTF to take the new configuration
        odlout = new OutdialNotificationOut();

        // New deposit (level-2) to Outdial 
        System.out.println("New deposit (level-2) to Outdial");
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.DEFAULT_NTF.getName(), myMsg, null, null);
        int count = odlout.notify(userInfo, odlFilter, myNumber, ntfEvent);
        assertTrue (count > 0);
        Thread.sleep(1000);

        // Inject an OK for the Reminder timer to start
        callerStub.sendOk(myDialNumber);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 1);

        // Wait at least 2 minutes for the call timer to retry
        System.out.println("Waiting 2 minutes");
        Thread.sleep(125000);
/*
        // Inject an OK for the second notification on first deposit
        callerStub.sendOk(myDialNumber);
        System.out.println("callerStub.getCallMade(): " + callerStub.getCallMade());
        assertTrue(callerStub.getCallMade() == 2);
*/
        System.out.println("After 2 minutes");
    }

    /**
     * case: user is logged in so notification will be retried;
     * 		 change state file to "read";
     * 		 when retry comes in, no notification should be sent.
     * 		 change back state file to "new";
     * @throws InterruptedException {@link InterruptedException}
     */
    @Test
    public void testReadMsgWontBeNotified() throws InterruptedException {
//    	OdlEventHandler.setLoginRetryTimer(60000);

        IMfsEventManager mfsEventManager = new MfsEventManager(CommonMessagingAccess.getInstance(),
        		CommonMessagingAccess.getInstance().getMcd());
        try {
			mfsEventManager.createLoginFile(myNumber);
		} catch (TrafficEventSenderException e) {
			e.printStackTrace();
		}

		//first notify
    	odlout.notify(userInfo, odlFilter, myNumber);

    	Thread.sleep(40000);
    	assertTrue(callerStub.getCallMade() == 0);

    	OdlEvent event = retrieveOdlEvent(myDialNumber);
    	assertTrue (event.getReferenceId() != null); //login retry check

    	try {
			mfsEventManager.removeLoginFile(myNumber);
		} catch (TrafficEventSenderException e) {
			e.printStackTrace();
		}

		//update state file to "read"
        StateFile sfile = new StateFile(myMsg);
        sfile.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
        try {
			CommonMessagingAccess.getMfs().updateState(sfile);
		} catch (MsgStoreException e) {
			e.printStackTrace();
		}

    	Thread.sleep(70000);//login retry should have been kicked off

    	assertTrue(callerStub.getCallMade() == 0);

//    	OdlEventHandler.setLoginRetryTimer(0);

    	//put back state file to "new"
        sfile = new StateFile(myMsg);

        sfile.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
        try {
			CommonMessagingAccess.getMfs().updateState(sfile);
		} catch (MsgStoreException e) {
			e.printStackTrace();
		}
    }

    /**
     * manual test only, change max wait hours = 1m
     *
     * test when user is logged in, no call is made, when it's too old, an SMS fail is sent
     *
     * @throws InterruptedException {@link InterruptedException}
     */
    @Test
    public void testFileTooOldWithLogin() throws InterruptedException {

//    	OdlEventHandler.setLoginRetryTimer(30000);

        IMfsEventManager mfsEventManager = new MfsEventManager();
    	try {
    		try {
    			mfsEventManager.createLoginFile(myNumber);
    		} catch (TrafficEventSenderException e) {
    			e.printStackTrace();
    		}

    		odlout.notify(userInfo, odlFilter, myNumber);

    		Thread.sleep(40000); //a call wait period
    		assertTrue(callerStub.getCallMade() == 0);

    		OdlEvent event = retrieveOdlEvent(myDialNumber);
    		assertTrue (event.getReferenceId() != null); //login retry check
    		assertTrue (event.isFromLogin()); //login

    		Thread.sleep(40000);//let login retry come back

    		assertTrue (smsClientStub.getSendMultiCalled() == 1);

    	} finally {
        	try {
    			mfsEventManager.removeLoginFile(myNumber);
    		} catch (TrafficEventSenderException e) {
    			e.printStackTrace();
    		}

        	//OdlEventHandler.setLoginRetryTimer(0);
    	}

    }

    @Test
    public void testUserLogin() throws InterruptedException {

    	//OdlEventHandler.setLoginRetryTimer(60000);

        IMfsEventManager mfsEventManager = new MfsEventManager(CommonMessagingAccess.getInstance(),
        		CommonMessagingAccess.getInstance().getMcd());
        try {
			mfsEventManager.createLoginFile(myNumber);
		} catch (TrafficEventSenderException e) {
			e.printStackTrace();
		}

    	odlout.notify(userInfo, odlFilter, myNumber);

    	Thread.sleep(40000);
    	assertTrue(callerStub.getCallMade() == 0);

    	OdlEvent event = retrieveOdlEvent(myDialNumber);
    	assertTrue (event.getReferenceId() != null); //login retry check

    	try {
			mfsEventManager.removeLoginFile(myNumber);
		} catch (TrafficEventSenderException e) {
			e.printStackTrace();
		}

    	Thread.sleep(70000);//call retry

    	assertTrue(callerStub.getCallMade() == 1);
    	callerStub.sendOk(myDialNumber);

    	Thread.sleep(2000);
    	assertTrue(callerStub.getCallMade() == 1);

    	//OdlEventHandler.setLoginRetryTimer(0);

    }

    @Test
    public void testLastDefaultCodeTransition() throws InterruptedException {

    	int  count = odlout.notify(userInfo, odlFilter, myNumber);
    	assertTrue (count > 0);

    	Thread.sleep(40000);
    	callerStub.sendResponse(myDialNumber, 404); //to state 1

    	Thread.sleep(60000);
    	callerStub.sendResponse(myDialNumber, 200); //END

    	Thread.sleep(2000); // not wait for the last one

    	assertTrue(callerStub.getCallMade() == 2);
    }

    @Test
    public void testDefaultStateTransition() throws InterruptedException {

    	int  count = odlout.notify(userInfo, odlFilter, myNumber);
    	assertTrue (count > 0);

    	Thread.sleep(40000);
    	callerStub.sendResponse(myDialNumber, 404); //to state 1

    	Thread.sleep(60000);
    	callerStub.sendResponse(myDialNumber, 202); //END

    	Thread.sleep(2000);
    	assertTrue(callerStub.getCallMade() == 2);
}

    @Test
    public void testStateTransition() throws InterruptedException {

    	int  count = odlout.notify(userInfo, odlFilter, myNumber);
    	assertTrue (count > 0);

    	Thread.sleep(40000);
    	callerStub.sendResponse(myDialNumber, 404); //to state 1

    	Thread.sleep(60000);
    	callerStub.sendResponse(myDialNumber, 404); //to state 2

    	Thread.sleep(60000);
    	callerStub.sendResponse(myDialNumber, 404); //to state 3

    	Thread.sleep(60000);
    	callerStub.sendResponse(myDialNumber, 404); //to state 4, end

    	Thread.sleep(2000);
    	assertTrue(callerStub.getCallMade() == 4);
    }

    @Test
    public void testSendOdlCallRetry() throws InterruptedException {

        NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.EVENT_TYPE_ODL_CALL.getName());
        //eventHandlerCall.setCallTimeout(45000);

    	int  count = odlout.notify(userInfo, odlFilter, myNumber);
    	assertTrue (count > 0);

    	Thread.sleep(100000); //60s + 40s

    	callerStub.sendOk(myDialNumber);

    	Thread.sleep(2000);
        NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.EVENT_TYPE_ODL_CALL.getName());
    	assertTrue(callerStub.getCallMade() == 2);

        //eventHandlerCall.setCallTimeout(60*45*1000);
}

    @Test
    public void testSendSmsFailed() throws InterruptedException {
    	int  count = odlout.notify(userInfo, odlFilter, myNumber);
    	assertTrue (count > 0);

    	Thread.sleep(65000);
    	callerStub.sendResponse(myDialNumber, 500);

    	Thread.sleep(2000);
    }

    @Test
    public void testSendSmsReplaced() throws InterruptedException {
    	int  count = odlout.notify(userInfo, odlFilter, myNumber);
    	assertTrue (count > 0);

    	Thread.sleep(65000);
    	callerStub.sendResponse(myDialNumber, 925);

    	Thread.sleep(2000);
    	assertTrue(callerStub.getCallMade() == 1);
}

    @Test
    public void testSendOdlEvent() throws InterruptedException {

    	int  count = odlout.notify(userInfo, odlFilter, myNumber);
    	assertTrue (count > 0);

    	Thread.sleep(70000);
    	callerStub.sendOk(myDialNumber);

    	Thread.sleep(2000);
    	assertTrue(callerStub.getCallMade() == 1);
    }

    @Test
    public void testWaitOnTimeout() throws InterruptedException {
    	OdlEventHandler eventHandler = (OdlEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.OUTDIAL.getName());
    	eventHandler.setWaitOnTimeout(60000);
    	smsClientStub.setSmsClientResponse(SMSClient.SEND_FAILED_TEMPORARY);

    	int count = odlout.notify(userInfo, odlFilter, myNumber);
    	assertTrue(count > 0);

    	Thread.sleep(70000);

    	assertTrue(smsClientStub.getNumberOfRequests() == 2);
    	smsClientStub.setSmsClientResponse(SMSClient.SEND_OK);

    	Thread.sleep(9000 + 61000);
    	callerStub.sendResponse(myDialNumber, 200);

    	Thread.sleep(2000);
    	assertTrue(callerStub.getCallMade() == 1);
    }

    static class McdStubOutdial extends McdStub {

        private Hashtable<String, DirectoryAccessSubscriber> subscribers = new Hashtable<String, DirectoryAccessSubscriber>();
        private ProfileContainer cosProfileContainer = new ProfileContainer();

        public McdStubOutdial() {
            ProfileContainer subscriberProfileContainer1 = new ProfileContainer();
            ProfileContainer subscriberProfileContainer2 = new ProfileContainer();

            // Number 12345
            subscriberProfileContainer1.addAttributeValue(DAConstants.ATTR_COS_IDENTITY, "cos:1");
            subscriberProfileContainer1.addAttributeValue(DAConstants.ATTR_NOTIF_NUMBER, myDialNumber);
            subscriberProfileContainer1.addAttributeValue(DAConstants.ATTR_DELIVERY_PROFILE,
                    "NotifType=SMS,ODL,MWI,EML;MobileNumber=" +
                    "514123" +
                    ";IPNumber=15143457900,888888888;Email=test@abc.com,foo@bar.com");
            subscriberProfileContainer1.addIdentity(URI.create("msid:827ccb0eea8a706c"));
            //subscriberProfileContainer1.addAttributeValue(DAConstants.ATTR_FILTER, "f11;Active=yes;ValidTime=Always;Priority=1;NotifContentSMS=Header;NotifContentEML=Header;Notify=no;CriteriaMsgHighPriority=no;MsgDepositType=Voice;NotifType=ODL;NotifContentMWI=false;CriteriaTelephoneFrom=");
            subscriberProfileContainer1.addAttributeValue(DAConstants.ATTR_FILTER, "Name=outdial;Active=yes;Notify=yes;ValidTime=Always;Priority=1;CriteriaMsgHighPriority=no;MsgDepositType=Voice,Video;NotifType=ODL;NotifContentSMS=Subject;NotifContentEML=Subject;NotifContentMWI=false;CriteriaTelephoneFrom=");
            DirectoryAccessSubscriber sub1 = new DirAccessSubscriberStub(new MoipProfile(subscriberProfileContainer1, new StdoutLogger("McdStub")), this.getCos(), null, new StdoutLogger("McdStub"));
            subscribers.put(myNumber, sub1);

            // Number 34567
            subscriberProfileContainer2.addAttributeValue(DAConstants.ATTR_COS_IDENTITY, "cos:1");
            subscriberProfileContainer2.addAttributeValue(DAConstants.ATTR_NOTIF_NUMBER, myDialNumber);
            subscriberProfileContainer2.addAttributeValue(DAConstants.ATTR_DELIVERY_PROFILE,
                    "NotifType=SMS,ODL,MWI,EML;MobileNumber=" +
                    "514123" +
                    ";IPNumber=15143457900,888888888;Email=test@abc.com,foo@bar.com");
            subscriberProfileContainer2.addIdentity(URI.create("msid:111112462ffff"));
            //subscriberProfileContainer2.addAttributeValue(DAConstants.ATTR_FILTER, "f11;Active=yes;ValidTime=Always;Priority=1;NotifContentSMS=Header;NotifContentEML=Header;Notify=no;CriteriaMsgHighPriority=no;MsgDepositType=Voice;NotifType=ODL;NotifContentMWI=false;CriteriaTelephoneFrom=");
            subscriberProfileContainer2.addAttributeValue(DAConstants.ATTR_FILTER, "Name=outdial;Active=yes;Notify=yes;ValidTime=Always;Priority=1;CriteriaMsgHighPriority=no;MsgDepositType=Voice,Video;NotifType=ODL;NotifContentSMS=Subject;NotifContentEML=Subject;NotifContentMWI=false;CriteriaTelephoneFrom=");
                
            DirectoryAccessSubscriber sub2 = new DirAccessSubscriberStub(new MoipProfile(subscriberProfileContainer2, new StdoutLogger("McdStub")), this.getCos(), null, new StdoutLogger("McdStub"));
            subscribers.put(myDialNumber, sub2);
        }

        private MoipProfile getCos() {
            if (cosProfileContainer.attributesSize() == 0) {
                // Add default values
                //directoryAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "1;y;a;evf;SMS,ODL;s,c;1;;;;;default;;");
                cosProfileContainer.addAttributeValue(DAConstants.ATTR_FILTER, "f11;Active=yes;ValidTime=Always;Priority=1;NotifContentSMS=Header;NotifContentEML=Header;Notify=no;CriteriaMsgHighPriority=no;MsgDepositType=Voice;NotifType=ODL;NotifContentMWI=false;CriteriaTelephoneFrom=");
                cosProfileContainer.addAttributeValue(DAConstants.ATTR_SERVICES, "mwi_notification");
                cosProfileContainer.addAttributeValue(DAConstants.ATTR_SERVICES, "msgtype_voice");
                cosProfileContainer.addAttributeValue(DAConstants.ATTR_SERVICES, "outdial_notification");
                cosProfileContainer.addAttributeValue(DAConstants.ATTR_CN_SERVICES, "moip");
            }

            MoipProfile cosProfile = new MoipProfile(cosProfileContainer, new StdoutLogger("McdStub"));
            return cosProfile;
        }
        
        public DirectoryAccessSubscriber lookupSubscriber(String subscriberIdentity)
        {
            return subscribers.get(subscriberIdentity);
        }

        public void setSubscriberProfile(String subscriberIdentity, boolean phoneOn) {
            subscribers.remove(subscriberIdentity);

            ProfileContainer subscriberProfileContainer1 = new ProfileContainer();
            // Number 12345
            if (phoneOn) {
                subscriberProfileContainer1.addAttributeValue(DAConstants.ATTR_OUTDIAL_SEQUENCE, "phoneon");
            }
            subscriberProfileContainer1.addAttributeValue(DAConstants.ATTR_COS_IDENTITY, "cos:1");
            subscriberProfileContainer1.addAttributeValue(DAConstants.ATTR_NOTIF_NUMBER, myDialNumber);
            subscriberProfileContainer1.addAttributeValue(DAConstants.ATTR_DELIVERY_PROFILE,
                    "NotifType=SMS,ODL,MWI,EML;MobileNumber=" +
                    "514123" +
                    ";IPNumber=15143457900,888888888;Email=test@abc.com,foo@bar.com");
            subscriberProfileContainer1.addIdentity(URI.create("msid:827ccb0eea8a706c"));
            //subscriberProfileContainer1.addAttributeValue(DAConstants.ATTR_FILTER, "f11;Active=yes;ValidTime=Always;Priority=1;NotifContentSMS=Header;NotifContentEML=Header;Notify=no;CriteriaMsgHighPriority=no;MsgDepositType=Voice;NotifType=ODL;NotifContentMWI=false;CriteriaTelephoneFrom=");
            subscriberProfileContainer1.addAttributeValue(DAConstants.ATTR_FILTER, "Name=outdial;Active=yes;Notify=yes;ValidTime=Always;Priority=1;CriteriaMsgHighPriority=no;MsgDepositType=Voice,Video;NotifType=ODL;NotifContentSMS=Subject;NotifContentEML=Subject;NotifContentMWI=false;CriteriaTelephoneFrom=");
            DirectoryAccessSubscriber sub1 = new DirAccessSubscriberStub(new MoipProfile(subscriberProfileContainer1, new StdoutLogger("McdStub")), this.getCos(), null, new StdoutLogger("McdStub"));
            subscribers.put(subscriberIdentity, sub1);
        }
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
			return myNumber;
		}
	}

    static class Ss7mgrStub implements ISs7Manager { 
        public SubscriberInfo getSubscriberInfo(String s)
        throws Ss7Exception{return null;}

    public String getImsi(String s)
        throws Ss7Exception{return null;} 

    public  void setUnconditionalDivertInHlr(String s)
        throws Ss7Exception{}

    public void setConditionalDivertInHlr(String s)
        throws Ss7Exception{}

    public  void cancelUnconditionalDivertInHlr(String s)
        throws Ss7Exception{}

    public void cancelConditionalDivertInHlr(String s)
        throws Ss7Exception{}

    public  Boolean getDivertStatusInHlr(String s, String s1)
        throws Ss7Exception{return null;}

    public int getRoamingStatus_ATI(String aPhoneNumber){return -1;}
    
    public  Boolean isRoaming(String s){return null;}

    public  Boolean isRoaming_ATI(String s){return null;}
    
    public Boolean isRoaming_ATI(AnyTimeInterrogationResult result){ return null; }
    

    public  Boolean isRoaming_SRI(String s){return null;}

    public  Boolean isRoamingSRI(String s)
        throws Ss7Exception{return null;}

    public  AnyTimeInterrogationResult requestATI(String s)
        throws Ss7Exception{return null;}

    public  Boolean registerAlertScHandler(AlertSCHandler alertschandler)
        throws Ss7Exception{return true;}

    public Boolean registerAlertScHandlerWithRetry(AlertSCHandler alertSCHandler,int alertSCRegistrationNumOfRetry,int alertSCRegistrationTimeInSecBetweenRetry){return true;}

    public  void sendMtForwardSM(String s, String s1, String s2)
        throws Ss7Exception{}

    public  void sentReportSMDeliveryStatus(String s)
        throws Ss7Exception{}

    public  Boolean useHlr(){return false;}

    public int getSubscriberRoamingStatus(String s) 
            throws Ss7Exception{return 0;}
    
    public String getSubStatusHlrInterrogationMethod() {return "ati";}
	}

	private OdlEvent retrieveOdlEvent(String number) {
		List<OdlEvent> eventList = eventStore.get(number);
		Assert.assertNotNull(eventList);
		Assert.assertFalse(eventList.isEmpty());
		return eventList.get(0);
	}

}
