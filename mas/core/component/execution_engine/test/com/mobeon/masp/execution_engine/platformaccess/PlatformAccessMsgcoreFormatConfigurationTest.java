package com.mobeon.masp.execution_engine.platformaccess;


import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.Before;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.ITrafficEventSender;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.mailbox.IStorableMessageFactory;
import com.mobeon.masp.mediacontentmanager.IMediaContentManager;
import com.mobeon.masp.mediahandler.MediaHandlerFactory;
import com.mobeon.masp.mediaobject.ContentTypeMapper;
import com.mobeon.masp.numberanalyzer.INumberAnalyzer;
import com.mobeon.masp.profilemanager.IProfileManager;


public class PlatformAccessMsgcoreFormatConfigurationTest extends TestCase {
	ConfigurationManagerImpl cm = null;
	PlatformAccessImpl platformAccessImpl = null;
	
	@Before
	public void setUp() throws Exception {
		 ILoggerFactory.configureAndWatch("cfg/logmanager.xml");
		 super.setUp();

	        // Create a configuration manager and read the configuration file
	        cm = new ConfigurationManagerImpl();
	        cm.setConfigFile("../vva/cfg/vva.conf");
	        platformAccessImpl = getPlatformAccess();
    }
    
    private PlatformAccessImpl getPlatformAccess() {

        ExecutionContext mockExecutionContext = EasyMock.createMock(VXMLExecutionContext.class);
        
        //message sender
        IStorableMessageFactory mockIStorableMessageFactory = EasyMock.createMock(IStorableMessageFactory.class);
        //profile
        IProfileManager mockProfileManager = EasyMock.createMock(IProfileManager.class);
        SubscriberProfileManager  subscriberProfileManager = new SubscriberProfileManager(mockProfileManager);
        
        //mediacontent
        IMediaContentManager mockMediaContentManager = EasyMock.createMock(IMediaContentManager.class);
        MediaManager mediaManager = new MediaManager(mockMediaContentManager, mockExecutionContext);
        
        //mailbox manager
        MailboxManager mailboxManager = new MailboxManager(mockIStorableMessageFactory);
        
        //session
        ISession mockISession = EasyMock.createMock(ISession.class);
        EasyMock.expect(mockISession.getData("MailboxManager")).andReturn(mailboxManager).anyTimes();       
        EasyMock.expect(mockISession.getData("SubscriberProfileManager")).andReturn(subscriberProfileManager).anyTimes();       
        EasyMock.expect(mockISession.getData("MediaManager")).andReturn(mediaManager).anyTimes();
        
        EasyMock.expect(mockExecutionContext.getSession()).andReturn(mockISession).anyTimes();
        
        //replay
        EasyMock.replay(mockExecutionContext);
        EasyMock.replay(mockISession);

       //numberanalyzer
        INumberAnalyzer mockNumberAnalyzer = EasyMock.createMock(INumberAnalyzer.class);

        //trafficeventsender
        ITrafficEventSender mockTrafficEventSender = EasyMock.createMock(ITrafficEventSender.class);

        MediaHandlerFactory mockMediaHandlerFactory = EasyMock.createMock(MediaHandlerFactory.class);
        ContentTypeMapper mockContentTypeMapper = EasyMock.createMock(ContentTypeMapper.class);
        ConfigManager mockConfigManager = EasyMock.createMock(ConfigManager.class);
                
        return  new  PlatformAccessImpl( 	mockExecutionContext,
                 							mockNumberAnalyzer,
                 							mockProfileManager,
                 							cm.getConfiguration(),
                 							mockIStorableMessageFactory,
                 							mockMediaContentManager,
                 							mockTrafficEventSender,
                 							mockMediaHandlerFactory,
                 							mockContentTypeMapper,
                 							mockConfigManager);
    }


    public void testSystemGetConfigurationParameter() throws Exception {
    	IConfiguration config = cm.getConfiguration();
    	
    	// parameter exists in vva group
    	System.out.println("Parameter timesettingseveningstart in vva has the value " + config.getGroup("vva.conf").getInteger("timesettingseveningstart"));
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "eveningstart"), "1700");
    	
    	//group name doesn't start with vva
    	try {
    		System.out.println(platformAccessImpl.systemGetConfigurationParameter("NOTvva.timesettings", "eveningstart"));
    		fail("Failed! The group name should not start with vva!");
    	} catch (Exception ex) {
    		assertNotNull(ex);
    		assertEquals(ex.getMessage(),"error.com.mobeon.platform.datanotfound");
    	}
    	// parameter doesn't exist in vva
    	junit.framework.Assert.assertNull(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "eveningstartNOTEXISTING"));
    }
    
    public void testAllVvaConfigurationParameters() throws Exception {
    	//dialog behaviour parameters
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "autoplayofold"), "yes");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "callbackenabled"), "yes");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "depositnoquotacheck"), "yes");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "faxfromaddress"), "billingnumber");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "initpinanyphone"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "languagenumbers"), "[]");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "loginsetlanguages"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "maxloginattempts"), "3");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "messagepartsplayorder"), "[status,from,date,body]");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "missedcallnotifservice"), "yes");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "redirectcausenoslamdown"), "[no reply]");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "redirectcausenomcn"), "[]");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "sendmwioff"), "yes");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "sendessmessage"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "systemunpauseanykey"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "timeouttomainmenu"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "testsoapenabled"), "false");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.dialogbehaviour", "testsoapurl"), "http://host:port/soap");
    	//incoming call parameters
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.incomingcall", "admingreetingdnis"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.incomingcall", "aniequalsdepositid"), "retrieval");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.incomingcall", "depositdnis"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.incomingcall", "directdepositdnis"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.incomingcall", "faxprintdnis"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.incomingcall", "missedcallnotifnumbers"), "[11112]");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.incomingcall", "retrievaldnis"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.incomingcall", "retrievalprefix"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.incomingcall", "retrievalallowedatdepositdnis"), "no");
    	//media parameters
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.media", "defaultlanguage"), "en");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.media", "defaultttslanguage"), "en");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.media", "variantvideo"), "video.h263.pcmu");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.media", "variantvoice"), "voice.pcmu");
    	//number analysis parameters    	
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.numberanalysis", "callednumberretrprefixrule"), "RETRIEVALPREFIXRULE");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.numberanalysis", "callednumberrule"), "INBOUNDCALL");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.numberanalysis", "callingnumberrule"), "INBOUNDCALL");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.numberanalysis", "echonumberfromdtmfrule"), "ECHONUMBER");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.numberanalysis", "faxprintdtmfrule"), "FAXPRINTRULE");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.numberanalysis", "faxprintmurrule"), "FAXPRINTRULE");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.numberanalysis", "missedcallnotifdestrule"), "MISSEDCALLNOTIFICATION");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.numberanalysis", "missedcallnotiffromrule"), "MISSEDCALLNOTIFICATION");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.numberanalysis", "mwinotificationrule"), "MWINOTIFICATION");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.numberanalysis", "redirectingnumberrule"), "INBOUNDCALL");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.numberanalysis", "slamdownnumberrule"), "SLAMDOWNCALL");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.numberanalysis", "subscribernumberrule"), "INBOUNDCALL");
    	//outdial parameters
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.outdial", "outdialanswertimeout"), "30");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.outdial", "outdialnotificationani"), "");

    	//time settings parameters
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "afternoonstart"), "1200");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "autoplaydelay"), "3");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "eveningstart"), "1700");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "forwardtime"), "3");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "gkinterdigittimeout"), "1000");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "inputtimeout"), "5");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "interdigittimeout"), "3");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "maxsilenceduration"), "6000");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "minrecmsg"), "1000");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "minrecnotmsg"), "1000");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "morningstart"), "0000");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.timesettings", "rewindtime"), "3");
    	//voice sms parameters
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "accountingmethod"), "disabled");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "acclistennewvideo"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "acclistennewvoice"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "acclistenoldvideo"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "acclistenoldvoice"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "accreplynewvideo"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "accreplynewvoice"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "accreplyoldvideo"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "accreplyoldvoice"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "accsendvideo"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "accsendvoice"), "no");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "cosname"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "depositprefix"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "depositprefixpostpaid"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "depositprefixprepaid"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "messagecycles"), "1");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "murprefix"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "retrievalnew"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "retrievalnewpostpaid"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "retrievalnewprepaid"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "retrievalold"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "retrievaloldpostpaid"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "retrievaloldprepaid"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "useradminuid"), "");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "validitytimedebit"), "5");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.voicesms", "validitytimerefund"), "5");
    	//sip to xmp parameters
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.siptoxmp", "default"), "550");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.siptoxmp", "failed"), "550:404,500-599,600-699");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.siptoxmp", "ok"), "200:200");
    	assertEquals(platformAccessImpl.systemGetConfigurationParameter("vva.siptoxmp", "retry"), "450:400-403,405-499");
	 
    }

}
