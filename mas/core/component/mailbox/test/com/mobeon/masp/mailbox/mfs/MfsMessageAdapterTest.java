package com.mobeon.masp.mailbox.mfs;

import java.util.Properties;

import org.jmock.integration.junit4.JMock;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.scheduler.SchedulerFactory;
import com.abcxyz.messaging.scheduler.SchedulerManager;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.masp.mailbox.StoredMessageState;

@RunWith(JMock.class)
public class MfsMessageAdapterTest extends MsfBaseTest
{	
	private MfsMessageAdapter mfsMessageAdapter;
	private MfsMock mfs;
	private MessageInfo messageInfo;
	
	public MfsMessageAdapterTest(String name) {
		super(name);
	}
	
	public MfsMessageAdapterTest() {
		super("MfsMessageAdapterTest");
	}

	@BeforeClass
	public static void setUpBefore() {
		try {
			CommonTestingSetup.setup();

	        // Retrieve the scheduler's instance from the factory
		    // and start it
			SchedulerManager scheduler = SchedulerFactory.getSchedulerManager();
			scheduler.init(CommonOamManager.getInstance().getMrdOam());
			scheduler.start();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
	}

	@AfterClass
	static public void tearDown() {
		SchedulerManager scheduler = SchedulerFactory.getSchedulerManager();
		scheduler.stop();
		CommonTestingSetup.tearDown();
	}

	@Before
    public void setUp() throws Exception {
        super.setUp();
        mfs = new MfsMock();
        MfsContext context = new MfsContext(null);
        messageInfo = new MessageInfo(new MSA("omsa"), new MSA("rmsa"), "omsgid", "rmsgid");
        StateFile s = new StateFile(messageInfo);
        mfsMessageAdapter = new MfsMessageAdapter(s, context);
    }


	@Test
    public void testStoreState() throws Exception 
    {
		mfsMessageAdapter.setState(StoredMessageState.READ);
    	try {
			mfsMessageAdapter.saveChanges();
			
			/*
			 * Since the time passed to MfsMessageAdapter.saveChanges() is specified in days,
			 * we are not going to wait that long. Instead we will reconstruct an AppliEventInfo
			 * class and call the MessageCleaner object directly.
			 */
			StateFile stateFile = mfs.getStateFile(messageInfo);
			String eventId = stateFile.getAttribute(Constants.EXPIRY_EVENT_ID);
			
			AppliEventInfo eventInfo = new AppliEventInfo();
			Properties properties = new Properties();
			properties.setProperty(MoipMessageEntities.OMSA, messageInfo.omsa.toString());
			properties.setProperty(MoipMessageEntities.RMSA, messageInfo.rmsa.toString());
			properties.setProperty(MoipMessageEntities.OMSGID, messageInfo.omsgid);
			properties.setProperty(MoipMessageEntities.RMSGID, messageInfo.rmsgid);
			eventInfo.setEventId(eventId);
			eventInfo.setEventProperties(properties);
			
			MessageCleaner messageCleaner = new MessageCleaner(mfs);
			messageCleaner.eventFired(eventInfo);
		} catch (Exception e) {
			Assert.fail("Got error " + e.toString());
		}
    }
}
