/**
 * 
 */
package com.mobeon.ntf.out.outdial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.IdGenerationException;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.ntf.event.OdlEvent;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.McdStub;

/**
 * @author egeobli
 *
 */
public class MfsEventStoreTest {
	
	private static final String phoneNumber = "1234";
	private static final String notifNumber = phoneNumber;
	private static final String odlPrefix = "odl-";
	
	private static MessageInfo msgInfo = new MessageInfo();

	// FIXME This test class needs reviewing - it does not initialize properly on Linux

	/**
	 * @throws java.lang.Exception
	 */
//	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CommonTestingSetup.setup();
		
		msgInfo.omsa = new MSA ("omsa"); //OMSA not used any way
		msgInfo.rmsa = MFSFactory.getMSA(phoneNumber, true); //
		msgInfo.omsgid = MFSFactory.getAnyOmsgid(phoneNumber, "voice"); //
		msgInfo.rmsgid = MFSFactory.getAnyRmsgid(phoneNumber); //

		McdStub directoryAccess = new McdStub();
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "1;y;a;evf;SMS,ODL;s,c;1;;;;;default;;");
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_SERVICES, "msgtype_voice");
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_OUTDIAL_SEQUENCE, "default");
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE, 
        		"NotifType=SMS,ODL,MWI,EML;MobileNumber=" + 
        		phoneNumber + 
        		";IPNumber=15143457900,888888888;Email=test@abc.com,foo@bar.com");
        directoryAccess.addSubcriberProfileIdentity(URI.create("msid:111112462ffff"));
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_COS_IDENTITY, "cos:1");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_NOTIF_NUMBER, notifNumber);
        CommonMessagingAccess.setMcd(directoryAccess);
	}

	/**
	 * @throws java.lang.Exception
	 */
//	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		CommonTestingSetup.tearDown();
	}

	/**
	 * @throws java.lang.Exception
	 */
//	@Before
	public void setUp() throws Exception {
		String privateFolder = getPrivateFolder();
		File privateFolderFile = new File(privateFolder + File.separator + notifNumber + File.separator + "events");
		if (!privateFolderFile.exists()) {
			privateFolderFile.mkdirs();
			assertTrue(privateFolderFile.exists());
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
//	@After
	public void tearDown() throws Exception {
		String privateFolder = getPrivateFolder();
		CommonTestingSetup.deleteDir(privateFolder);
	}

	/**
	 * Test method for {@link com.mobeon.ntf.out.outdial.MfsEventStore#get(java.lang.String)}.
	 * @throws IdGenerationException 
	 */
	@Ignore("Ignored until class initialization is fixed")
	@Test
	public void testGet() throws IOException, IdGenerationException {
		final String trigger = "testGet";

        OdlEvent expectedEvent = new OdlEvent(phoneNumber, notifNumber, msgInfo, trigger, 111);
		expectedEvent.setCurrentOperation(10);
		expectedEvent.setCurrentState(20);
		expectedEvent.setOdlCode(30);
		Properties expEventProps = expectedEvent.getEventProperties();

		String privateFolder = getPrivateFolder();
		File privateFolderFile = new File(privateFolder + File.separator + notifNumber + File.separator + "events");
		File expFile = new File(privateFolderFile, odlPrefix + expectedEvent.getOdlEventKey());
		FileWriter writer = null;
		try {
			writer = new FileWriter(expFile);
			expEventProps.store(writer, null);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		
		// Retrieve the save OdlEvent.
		MfsEventStore eventStore = new MfsEventStore();
		List<OdlEvent> eventList = eventStore.get(notifNumber);
		assertNotNull(eventList);
		assertTrue(eventList.size() == 1);
		
		Properties readProps = eventList.get(0).getEventProperties();
		assertNotNull(readProps);
		assertEquals(expEventProps, readProps);
		
		// Retrieve a non existing OdlEvent.
		assertTrue(expFile.delete());
		eventList = eventStore.get(notifNumber);
		assertNull(eventList);
	}

	/**
	 * Test method for {@link com.mobeon.ntf.out.outdial.MfsEventStore#put(java.lang.String, com.abcxyz.services.moip.ntf.event.OdlEvent)}.
	 */
	@Test
    @Ignore("Ignored until class initialization is fixed")
	public void testPutStringOdlEvent() {
		final String trigger = "testPutStringOdlEvent";

        OdlEvent expectedEvent = new OdlEvent(phoneNumber, notifNumber, msgInfo, trigger, 111);
		
		String privateFolder = getPrivateFolder();
		File privateFolderFile = new File(privateFolder + File.separator + notifNumber + File.separator + "events");
		File expFile = new File(privateFolderFile, odlPrefix + expectedEvent.getOdlEventKey());
		
		MfsEventStore eventStore = new MfsEventStore();
		eventStore.put(notifNumber, expectedEvent);
		
		assertTrue(expFile.exists());
	}

	/**
	 * Test method for {@link com.mobeon.ntf.out.outdial.MfsEventStore#put(java.lang.String, java.util.List)}.
	 */
	@Test
    @Ignore("Ignored until class initialization is fixed")
	public void testPutStringListOfOdlEvent() {
		final String trigger = "testPutStringListOfOdlEvent";
		final int nbEvents = 20;

		ArrayList<OdlEvent> eventList = new ArrayList<OdlEvent>(nbEvents);
		for (int i = 0; i < nbEvents; ++i) {
			OdlEvent event = new OdlEvent(phoneNumber + i, notifNumber, msgInfo, trigger, 111);
			eventList.add(event);
		}
		
		String privateFolder = getPrivateFolder();
		File privateFolderFile = new File(privateFolder + File.separator + notifNumber + File.separator + "events");

		MfsEventStore eventStore = new MfsEventStore();
		eventStore.put(notifNumber, eventList);
		
		for (int i = 0; i < nbEvents; ++i) {
			File expFile = new File(privateFolderFile, odlPrefix + eventList.get(i).getOdlEventKey());
			assertTrue(expFile.exists());
		}
	}

	/**
	 * Test method for {@link com.mobeon.ntf.out.outdial.MfsEventStore#remove(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
    @Ignore("Ignored until class initialization is fixed")
	public void testRemoveString() throws IOException {
		final String trigger = "testRemoveString";
		final int nbEvents = 20;

		String privateFolder = getPrivateFolder();
		File privateFolderFile = new File(privateFolder + File.separator + notifNumber + File.separator + "events");

		for (int i = 0; i < nbEvents; ++i) {
			OdlEvent event = new OdlEvent(phoneNumber + i, notifNumber, msgInfo, trigger, 111);
			File expFile = new File(privateFolderFile, odlPrefix + event.getOdlEventKey());
			FileWriter writer = null;
			Properties props = event.getEventProperties();
			try {
				writer = new FileWriter(expFile);
				props.store(writer, null);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
			
			assertTrue(expFile.exists());
		}

		MfsEventStore eventStore = new MfsEventStore();
		eventStore.remove(notifNumber);
		
		String[] fileList = privateFolderFile.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(odlPrefix);
			}
		});
		
		assertNotNull(fileList);
		assertTrue(fileList.length == 0);
	}

	/**
	 * Test method for {@link com.mobeon.ntf.out.outdial.MfsEventStore#remove(com.abcxyz.services.moip.ntf.event.OdlEvent)}.
	 * @throws IOException 
	 */
	@Test
    @Ignore("Ignored until class initialization is fixed")
	public void testRemoveOdlEvent() throws IOException {
		final String trigger = "testRemoveOdlEvent";

        OdlEvent event = new OdlEvent(phoneNumber, notifNumber, msgInfo, trigger, 111);
		Properties expEventProps = event.getEventProperties();

		String privateFolder = getPrivateFolder();
		File privateFolderFile = new File(privateFolder + File.separator + notifNumber + File.separator + "events");
		File expFile = new File(privateFolderFile, odlPrefix + event.getOdlEventKey());
		FileWriter writer = null;
		try {
			writer = new FileWriter(expFile);
			expEventProps.store(writer, null);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		
		assertTrue(expFile.exists());

		MfsEventStore eventStore = new MfsEventStore();
		eventStore.remove(event);
		
		assertFalse(expFile.exists());
	}

	/**
	 * Test method for {@link com.mobeon.ntf.out.outdial.MfsEventStore#update(java.util.Observable, java.lang.Object)}.
	 */
	@Test
    @Ignore("Ignored until class initialization is fixed")
	public void testUpdate() {
		String trigger = "trig1";
		int odlCode = 0;
		int state = 0;
		int operation = 0;
		
		OdlEvent event = new OdlEvent(phoneNumber, notifNumber, msgInfo, trigger, odlCode);
		event.setCurrentState(state);
		event.setCurrentOperation(operation);
		
		MfsEventStore eventStore = new MfsEventStore();
		
		eventStore.put(notifNumber, event);
		
		List<OdlEvent> readEvents = eventStore.get(notifNumber);
		assertNotNull(readEvents);
		assertTrue(readEvents.size() == 1);
		compare(event, readEvents.get(0));
		
		// Change trigger
		event.setOdlTrigger("newTrigger");
		event.notifyObservers();
		readEvents = eventStore.get(notifNumber);
		assertNotNull(readEvents);
		assertTrue(readEvents.size() == 1);
		compare(event, readEvents.get(0));
		
		// Change ODL code
		event.setOdlCode(1);
		event.notifyObservers();
		readEvents = eventStore.get(notifNumber);
		assertNotNull(readEvents);
		assertTrue(readEvents.size() == 1);
		compare(event, readEvents.get(0));

		// Change state
		event.setCurrentState(1);
		event.notifyObservers();
		readEvents = eventStore.get(notifNumber);
		assertNotNull(readEvents);
		assertTrue(readEvents.size() == 1);
		compare(event, readEvents.get(0));
		
		// Change operation
		event.setCurrentOperation(1);
		event.notifyObservers();
		readEvents = eventStore.get(notifNumber);
		assertNotNull(readEvents);
		assertTrue(readEvents.size() == 1);
		compare(event, readEvents.get(0));
		
	}
	
	private String getMSID() {
		return CommonMessagingAccess.getInstance().getMcd().lookupSubscriber(notifNumber).getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_MSID);
	}
	
	private String getPrivateFolder() {
		String msid = getMSID();
		return CommonMessagingAccess.getInstance().getMoipPrivateFolder(msid, true);
	}
	
	private void compare(OdlEvent event1, OdlEvent event2) {
		assertEquals(event1.getOdlTrigger(), event2.getOdlTrigger());
		assertEquals(event1.getOdlCode(), event2.getOdlCode());
		assertEquals(event1.getCurrentState(), event2.getCurrentState());
		assertEquals(event1.getOperationCode(), event2.getOperationCode());
	}

}
