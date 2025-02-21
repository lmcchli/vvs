package com.abcxyz.services.moip.masevent;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.Message;
import com.abcxyz.messaging.common.message.MessageStreamingResult;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.mfs.MfsConfiguration;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mrd.data.ActiveEventID;
import com.abcxyz.messaging.mrd.data.DestMessageClass;
import com.abcxyz.messaging.mrd.data.DestRecipientID;
import com.abcxyz.messaging.mrd.data.InformEventProp;
import com.abcxyz.messaging.mrd.data.InformEventResult;
import com.abcxyz.messaging.mrd.data.InformEventType;
import com.abcxyz.messaging.mrd.data.OrigMessageClass;
import com.abcxyz.messaging.mrd.data.RecipientMsa;
import com.abcxyz.messaging.mrd.data.RecipientMsgID;
import com.abcxyz.messaging.mrd.data.RetryEnabled;
import com.abcxyz.messaging.mrd.data.SenderMsa;
import com.abcxyz.messaging.mrd.data.SenderMsgID;
import com.abcxyz.messaging.mrd.data.TransactionID;
import com.abcxyz.messaging.mrd.data.Version;
import com.abcxyz.messaging.mrd.operation.DispatcherOperations;
import com.abcxyz.messaging.mrd.operation.InformEventReq;
import com.abcxyz.messaging.mrd.operation.InformEventResp;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;

public class VmEventHandlerTest {

	static private CommonMessagingAccess commonMessagingAccess =null;
	static private String strDirectoy = "C:\\opt\\moip\\mfs";
	static CountDownLatch locker;
	static private IDirectoryAccess directoryAccess = null;

	
	@Before
	public void setUp() throws Exception 
	{
		System.setProperty("abcxyz.mfs.userdir.create", "true");
	    System.setProperty("abcxyz.mrd.noAYL", "true");
	    System.setProperty("abcxyz.messaging.scheduler.memory", "true");
	    System.setProperty("abcxyz.moip.componentcheckers", "");

	    Collection<String> configFilenames = new LinkedList<String>();

	    String curDir = System.getProperty("user.dir");
	    String backendFile = "";
		String mcrFilename = "";
		String normalizationFile = "";
	    if (curDir.endsWith("backend") == false ) {
	    	backendFile = curDir +  "/../ipms_sys2/backend/cfg/backend.conf";
            mcrFilename = curDir + "/../ipms_sys2/backend/cfg/componentservices.cfg";
            normalizationFile = curDir + "/../ipms_sys2/backend/cfg/formattingRules.conf";
	    } else {
	    	backendFile = curDir +  "/cfg/backend.conf";
	    	mcrFilename = curDir + "/cfg/componentservices.cfg";
	    	normalizationFile = curDir + "/cfg/formattingRules.conf";
	    }

		System.setProperty("componentservicesconfig", mcrFilename);
		System.setProperty("normalizationconfig", normalizationFile);

	    configFilenames.add(backendFile);

	    IConfiguration configuration;
	    configuration = new ConfigurationImpl(null,configFilenames,false);

	    if (directoryAccess == null) {
	        CommonMessagingAccess.setMcd(new McdStub());
	    } else {
	        CommonMessagingAccess.setMcd(directoryAccess);
	    }
	    commonMessagingAccess = CommonMessagingAccess.getInstance();
	    commonMessagingAccess.setConfiguration(configuration) ;

	    ConfigManager mfsConfig = MfsConfiguration.getInstance();
	    boolean success = (new File(strDirectoy)).mkdir();
	    mfsConfig.setParameter(MfsConfiguration.MfsRootPath, strDirectoy);
	    commonMessagingAccess.reInitializeMfs(mfsConfig);
	}
	
	@Test
	public void testInformEvent()throws Exception
	{
		Container1 c1 = new Container1();
		c1.setFrom("1111111");
		c1.setTo("2222222");
		c1.setMsgClass("voice");
		Container2 c2 = new Container2();
		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  new MsgBodyPart();
		StateAttributes attributes = new StateAttributes();
		MessageInfo msgInfo = commonMessagingAccess.storeMessageTest(c1, c2, c3Parts, attributes);
		
		DispatcherOperations dispatcher = CommonMessagingAccess.getDispacher();
		
		Version version = new Version();
	    TransactionID transID = new TransactionID("1");
	    OrigMessageClass origMsgClass = new OrigMessageClass("vvm");
	    DestMessageClass destMsgClass = new DestMessageClass("mas");
	    DestRecipientID destRcptID = new DestRecipientID(msgInfo.rmsgid);
	    RecipientMsa rMsa = new RecipientMsa();
	    rMsa.setValue(msgInfo.rmsa.getId());
	    RecipientMsgID rMsgID = new RecipientMsgID(msgInfo.rmsgid);
	    SenderMsa oMsa = new SenderMsa(msgInfo.omsa.getId());
	    SenderMsgID oMsgID = new SenderMsgID(msgInfo.omsgid);
	    InformEventType informEventType = new InformEventType(InformEventType.MSG_READ);
	    ActiveEventID activeEventID = new ActiveEventID(); //event id handle
		
	    InformEventReq req = new InformEventReq(version, transID, origMsgClass, destMsgClass, destRcptID, rMsa, rMsgID, oMsa, oMsgID, informEventType, activeEventID);
	    
	    req.origMsgClass = new OrigMessageClass("vvm");
	    req.destMsgClass = new DestMessageClass("mas");
	    req.rMsgID.value = msgInfo.rmsgid;
	    
	    VmEventHandler vmHander = new VmEventHandler();
	    InformEventResp resp = vmHander.informEvent(req);
	    //InformEventResp resp = dispatcher.informEvent(req);
		
		assertEquals(InformEventResult.OK, resp.informEventResult.value);
		
	}
	
}
