package com.mobeon.common.cmnaccess;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.message.CodingFailureException;
import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.Message;
import com.abcxyz.messaging.common.message.MessageStreamingResult;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.mrd.operation.MsgSvrRegisterReq;
import com.abcxyz.messaging.mfs.MfsConfiguration;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.operation.MsgServerOperations;
import com.abcxyz.messaging.mrd.operation.SendMessageReq;
import com.abcxyz.messaging.mrd.operation.SendMessageResp;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;
import org.junit.Ignore;

@Ignore("This test needs revewing - it fails on Windows")
public class CommonMessagingAccessTest  {


	static private CommonMessagingAccess commonMessagingAccess =null;
	static private String strDirectoy = "/tmp/moip/mfs";//"C:\\opt\\moip\\mfs";
	static CountDownLatch locker;
	static MyNtf myntf;
	static private IDirectoryAccess directoryAccess = null;

	public CommonMessagingAccessTest() {
	}

	@BeforeClass
	static public void setUp() throws Exception {
	    deleteDirectory(new File(strDirectoy + "\\internal"));
	    deleteDirectory(new File(strDirectoy + "\\external"));
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
	 		System.setProperty("backendConfigDirectory", curDir + "/../ipms_sys2/backend/cfg");
	    	backendFile = curDir +  "/../ipms_sys2/backend/cfg/backend.conf";
            mcrFilename = curDir + "/../ipms_sys2/backend/cfg/componentservices.cfg";
            normalizationFile = curDir + "/../ipms_sys2/backend/cfg/formattingRules.conf";
	    } else {
	 		System.setProperty("backendConfigDirectory", curDir + "/cfg");
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

	    CommonMessagingAccess.getInstance().setServiceName(MoipMessageEntities.MESSAGE_SERVICE_NTF);
	    CommonMessagingAccess.getInstance().initMrd();
	    CommonMessagingAccess.getInstance().initMasEventHandlers();
	    locker = new CountDownLatch(1);
	    myntf = registerLocalNtf(locker);
	}

	@AfterClass
	static public void stop() {
	    commonMessagingAccess.stop();
	}

	/**
	 *
	 * @param dirAccess
	 * @throws Exception
	 */
	static public void setMcdStub(final IDirectoryAccess dirAccess) throws Exception {
        directoryAccess = dirAccess;
    }

	public static boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	  }


	@Test
	public void testStoreMessage() throws Exception{
		String from = "1111111";
		String msgClass = "voice";
		String to = "2222222";
		Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		c1.setMsgClass(msgClass);

		Container2 c2 = new Container2();
		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  new MsgBodyPart();
		StateAttributes attributes = new StateAttributes();
		int result1 = commonMessagingAccess.storeMessage(c1, c2, c3Parts, attributes);

		assertEquals(MessageStreamingResult.streamingOK, result1);
		if (locker != null) {
	        locker.await(2000, TimeUnit.MILLISECONDS);
	        assertTrue (myntf.sendMessageReceived == 1);
		}

		int result2 = commonMessagingAccess.storeMessage(c1, c2, c3Parts, null);
		assertEquals(result2, MessageStreamingResult.streamingOK);
	}



    @Test
	public void testReadMessage() throws Exception {
		Container1 c1 = new Container1();
		c1.setFrom("3333333");
		c1.setTo("4444444");

		/**
		 * these extra addresses are to test normalization
		 */
		c1.setTo("1111111");
		c1.setTo("Anne Marie <2222222> ");
		c1.setTo("5555555");
		c1.setCc("Joe Boo <6666666>");
		c1.setBcc("7777777");
		c1.setBcc("John Smith <8888888>");
		Container2 c2 = new Container2();
		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  new MsgBodyPart();
		StateAttributes attributes = new StateAttributes();
		MessageInfo msgInfo = commonMessagingAccess.storeMessageTest(c1, c2, c3Parts, attributes);
		assertNotNull(msgInfo);

		Message message = commonMessagingAccess.readMessage(msgInfo);
		assertNotNull(message);

	}


    @Test
	public void testSearchMessages() throws Exception {
		Container1 c1 = new Container1();
		c1.setFrom("5555555");
		c1.setTo("6666666");
		Container2 c2 = new Container2();
		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  new MsgBodyPart();
		int result;

		StateAttributes attributes = new StateAttributes();
		attributes.setAttribute("visibility", "true");
		result = commonMessagingAccess.storeMessage(c1, c2, c3Parts, attributes);
		assertEquals(result, MessageStreamingResult.streamingOK);

		Container1 c1_2 = new Container1();
		c1_2.setFrom("5555555");
		c1_2.setTo("6666666");
		Container2 c2_2 = new Container2();
		MsgBodyPart[] c3Parts_2 = new MsgBodyPart[1];
		c3Parts_2[0] =  new MsgBodyPart();

		StateAttributes attributes2 = new StateAttributes();
		attributes2.setAttribute("visibility", "true");
		result = commonMessagingAccess.storeMessage(c1_2, c2_2, c3Parts_2, attributes2);
		assertEquals(result, MessageStreamingResult.streamingOK);

		Container1 c1_3 = new Container1();
		c1_3.setFrom("5555555");
		c1_3.setTo("6666666");
		Container2 c2_3 = new Container2();
		MsgBodyPart[] c3Parts_3 = new MsgBodyPart[1];
		c3Parts_3[0] =  new MsgBodyPart();

		StateAttributes attributes3 = new StateAttributes();
		attributes3.setAttribute("visibility", "false");
		result = commonMessagingAccess.storeMessage(c1_3, c2_3, c3Parts_3, attributes3);
		assertEquals(result, MessageStreamingResult.streamingOK);

		StateAttributesFilter filter = new StateAttributesFilter();
		filter.setAttributeValue("visibility", "true");
		Message[] messages = commonMessagingAccess.searchMessages(new MSA(CommonMessagingAccess.directoryAccess.lookupSubscriber("6666666").getSubscriberIdentity(MCDConstants.IDENTITY_SCHEME_MSID)), filter);

		assertNotNull(messages);
		assertEquals(2,messages.length);
	}


    @Test
	public void testCountMessages() throws Exception  {
		Container1 c1 = new Container1();
		c1.setFrom("7777777");
		c1.setTo("8888888");
		Container2 c2 = new Container2();
		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  new MsgBodyPart();
		int result;

		StateAttributes attributes = new StateAttributes();
		attributes.setAttribute("visibility2", "true");
		result = commonMessagingAccess.storeMessage(c1, c2, c3Parts, attributes);
		assertEquals(result, MessageStreamingResult.streamingOK);


		Container1 c1_2 = new Container1();
		c1_2.setFrom("7777777");
		c1_2.setTo("8888888");
		Container2 c2_2 = new Container2();
		MsgBodyPart[] c3Parts_2 = new MsgBodyPart[1];
		c3Parts_2[0] =  new MsgBodyPart();

		StateAttributes attributes2 = new StateAttributes();
		attributes2.setAttribute("visibility2", "true");
		result = commonMessagingAccess.storeMessage(c1_2, c2_2, c3Parts_2, attributes2);
		assertEquals(result, MessageStreamingResult.streamingOK);

		Container1 c1_3 = new Container1();
		c1_3.setFrom("7777777");
		c1_3.setTo("8888888");
		Container2 c2_3 = new Container2();
		MsgBodyPart[] c3Parts_3 = new MsgBodyPart[1];
		c3Parts_3[0] =  new MsgBodyPart();

		StateAttributes attributes3 = new StateAttributes();
		attributes3.setAttribute("visibility2", "false");
		result = commonMessagingAccess.storeMessage(c1_3, c2_3, c3Parts_3, attributes3);
		assertEquals(result, MessageStreamingResult.streamingOK);


		StateAttributesFilter filter = new StateAttributesFilter();
		filter.setAttributeValue("visibility2", "true");
		int number =  commonMessagingAccess.countMessages(new MSA(CommonMessagingAccess.directoryAccess.lookupSubscriber("8888888").getSubscriberIdentity(MCDConstants.IDENTITY_SCHEME_MSID)), filter);

		assertEquals(2,number);

	}

    @Test
	public void testUpdateState() throws Exception
	{
		Container1 c1 = new Container1();
		c1.setFrom("9999999");
		c1.setTo("8888888");
		Container2 c2 = new Container2();
		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  new MsgBodyPart();
		StateFile state;

		StateAttributes attributes = new StateAttributes();
		attributes.setAttribute("state", "UNSEEN");
		MessageInfo msgInfo = commonMessagingAccess.storeMessageTest(c1, c2, c3Parts, attributes);
		state = CommonMessagingAccess.getMFS().readState(msgInfo);
		assertEquals("UNSEEN", state.getAttribute("state"));

		state = new StateFile(msgInfo);
		state.setAttribute("state", "SEEN");

		commonMessagingAccess.updateState(state);
		state = CommonMessagingAccess.getMFS().readState(msgInfo);
		assertEquals("SEEN", state.getAttribute("state"));
	}

    @Test
	public void testDeleteMessage() throws Exception
	{
		Container1 c1 = new Container1();
		c1.setFrom("9999999");
		c1.setTo("8888888");
		Container2 c2 = new Container2();
		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  new MsgBodyPart();
		MessageInfo msgInfo = commonMessagingAccess.storeMessageTest(c1, c2, c3Parts, null);

		StateFile state = new StateFile(msgInfo);

		commonMessagingAccess.deleteMessage(state);

		try
		{
			commonMessagingAccess.readMessage(msgInfo);
			fail("message was not deleted");
		}
		catch(MsgStoreException ex)
		{
			assertEquals("Message not found",ex.getMessage());
		}

	}

    @Test
    public void testExpireAndDeleteMessage() {

    	try {
			String to = "+15141234567";
			String from = "+15143457900";

			Container1 c1 = new Container1();
			c1.setFrom(from);
			c1.setTo(to);
			Container2 c2 = new Container2();
			MsgBodyPart[] c3Parts = new MsgBodyPart[1];
			c3Parts[0] =  new MsgBodyPart();
			MessageInfo msgInfo = commonMessagingAccess.storeMessageTest(c1, c2, c3Parts, null);

			StateFile stateFile = new StateFile(msgInfo);



			Date _10MinFromNow = new Date(System.currentTimeMillis()+ (10 *  60000));

			commonMessagingAccess.setMessageExpiry(_10MinFromNow, stateFile);
			commonMessagingAccess.updateState(stateFile);
			commonMessagingAccess.cancelScheduledEvent(stateFile);
			commonMessagingAccess.deleteMessage(stateFile);

			try
			{
				commonMessagingAccess.readMessage(msgInfo);
				fail("message was not deleted");
			}
			catch(MsgStoreException ex)
			{
				assertEquals("Message not found",ex.getMessage());
			}

		} catch (CodingFailureException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}

    }

    @Test
    public void testGeoRedMSIDOptimization(){

		String to = "+15141234567";  //Internal
		String from = "+15143457900"; //External

		try {
		Container1 c1 = new Container1();
		c1.setFrom(from);
		c1.setTo(to);
		Container2 c2 = new Container2();
		MsgBodyPart[] c3Parts = new MsgBodyPart[1];
		c3Parts[0] =  new MsgBodyPart();
//
		commonMessagingAccess.storeMessage(c1, c2, c3Parts, null);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
    	}

    }


//    @Test Broken - Needs fixing
//    public void testTriggerSendMessage() throws MsgAccessingException, InterruptedException {
//        locker = new CountDownLatch(1);
//
//
//        MessageInfo msg = new MessageInfo();
//        msg.omsa = MsgStoreServer.getMSA("myfrom", true);
//        msg.rmsa = MsgStoreServer.getMSA("myto", true);
//
//        Properties properties = new Properties();
//        CommonMessagingAccess.getInstance().notifyNtf(EventTypes.QUOTA_WARNING, msg, "5143457900", properties);
//
//        locker.await(2000, TimeUnit.MILLISECONDS);
//
//        assertTrue(myntf.smreq.eventType.equals(EventTypes.QUOTA_WARNING.getName()));
//
//    }

    static MyNtf registerLocalNtf(CountDownLatch locker) {
        MsgSvrRegisterReq req = new MsgSvrRegisterReq();
        req.msgClass.value = "ntf";
        req.msgServer = new MyNtf(locker);

        CommonMessagingAccess.getDispacher().msgServerRegister(req);

        return (MyNtf)req.msgServer;
    }

    static class MyNtf extends MsgServerOperations {

        CountDownLatch locker;

        SendMessageReq smreq;

        MyNtf(CountDownLatch locker) {
            this.locker = locker;
        }

        void setLatcher(CountDownLatch locker) {
            this.locker = locker;
        }

        int sendMessageReceived;
        public SendMessageResp sendMessage(SendMessageReq smreq) {
            sendMessageReceived ++;
            this.smreq = smreq;

            locker.countDown();
            return new SendMessageResp();

        }

    }
 }
