package com.mobeon.masp.profilemanager.distributionlists;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.oam.OamTestHelper;
import com.abcxyz.messaging.common.util.FileExtension;
import com.abcxyz.messaging.mfs.MFS;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryUpdater;
import com.abcxyz.services.moip.distributionlist.DistributionList;
import com.abcxyz.services.moip.distributionlist.DistributionListException;
import com.abcxyz.services.moip.distributionlist.DistributionListManager;
import com.abcxyz.services.moip.distributionlist.DistributionListManagerImpl;
import com.abcxyz.services.moip.distributionlist.IDistributionList;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.masp.profilemanager.MasTestHelper;


public class MultimediaDistributionListTest extends DistributionListTest {
	private static String mfsRootPath = null;
	private static OAMManager mockOam = null;
	private static String oldFileLinkValue = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("abcxyz.mfs.filelink", "com.abcxyz.messaging.sysutils.link.WindowsJNI");
		System.setProperty("abcxyz.mfs.notSetUmask","true");

		mfsRootPath = FileExtension.createTempDir("unittest").getAbsolutePath();
		mockOam = OamTestHelper.getMockOam(null, null, 0, 0, 0);
		MFSFactory.setOamManager(mockOam);
	}



	@Test
	public void testSimpleCreateAndThenRemove(){
		String testUserMsid = "msid:a71a2cc6c4f7b2a3";
		String msisdn = "491721093078";

		IDirectoryUpdater mockUpdater = createMockDirectoryUpdater(testUserMsid, msisdn, 1);
	    String listMuid = "muid:+" + msisdn +  "_" + String.valueOf(1);

		try {
			replay(mockUpdater);
			MultimediaDistributionList list  = new MultimediaDistributionList( mockOam.getLogAgent(), null,1,testUserMsid, mockUpdater);
			try {
				list.create(1);
			} catch (DistributionListException e) {
				e.printStackTrace();
				fail("Unexpected exception");
			}
			IDirectoryUpdater secondMockUpdater = createMockDirectoryUpdater(testUserMsid, msisdn, 1);
			replay(secondMockUpdater);
			Profile listProfile =  mockUpdater.lookup("shortlist", new URI(listMuid));
	        String shortlistMsid = listProfile.getIdentities("msid")[0].toString();

			MultimediaDistributionList listForRemoval  = new MultimediaDistributionList( mockOam.getLogAgent(),shortlistMsid,1, testUserMsid, secondMockUpdater);

			try {
				listForRemoval.remove(1);
			} catch (DistributionListException e) {
				e.printStackTrace();
				fail("Unexpected exception");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			fail("Unexpected exception");
		}
		verify(mockUpdater);
	}


	@Test
	public void testCreateWithInvalidIdExpectFail(){
		String testUserMsid = "msid:a71a2cc6c4f7b2a3";
		String msisdn = "491721093078";
		IDirectoryUpdater mockUpdater = createMockDirectoryUpdater(testUserMsid, msisdn, 1);
		String listMuid = "muid:+" + msisdn +  "_" + String.valueOf(1);


		try {
			replay(mockUpdater);
			MultimediaDistributionList list  = new MultimediaDistributionList(mockOam.getLogAgent(),null,0, testUserMsid, mockUpdater);
			list.create(0);
		}
		catch (DistributionListException e) {
			System.out.println("EXCELLENT! Got exception when creating with invalid id");
		}
		catch (Exception e1) {
			e1.printStackTrace();
			fail("Unexpected exception");
		}
		mockUpdater = null;
		mockUpdater = createMockDirectoryUpdater(testUserMsid, msisdn, 1);
		try {
			MultimediaDistributionList list  = new MultimediaDistributionList(mockOam.getLogAgent(), testUserMsid, mockUpdater);
			list.create(101);
		}
		catch (DistributionListException e) {
			System.out.println("EXCELLENT! Got exception when creating with invalid id");
		}
		catch (Exception e1) {
			e1.printStackTrace();
			fail("Unexpected exception");
		}

	}

	@Test
	public void testCreateButPAIsUnavailableForSubscriberLookupExpectFail(){
		String testUserMsid = "msid:a71a2cc6c4f7b2a3";
		String msisdn = "491721093078";

		IDirectoryUpdater mockUpdater = createMockDirectoryUpdaterWithFailSpec(testUserMsid, msisdn, 1,DIRECTORY_UPDATER_FAIL.SUBSCRIBER_LOOKUP_FAIL);

		try {
			replay(mockUpdater);
			MultimediaDistributionList list  = new MultimediaDistributionList(mockOam.getLogAgent(),null,1, testUserMsid, mockUpdater);
			try {
				list.create(1);
			} catch (DistributionListException e) {
				System.out.println("EXCELLENT! Got an exception when PA is unavailable");
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
			fail("Unexpected exception");
		}
		verify(mockUpdater);
	}

	@Test
	public void testCreateShortlistCreateFailsExpectFail(){
		String testUserMsid = "msid:a71a2cc6c4f7b2a3";
		String msidn = "491721093078";

		IDirectoryUpdater mockUpdater = createMockDirectoryUpdaterWithFailSpec(testUserMsid, msidn,1, DIRECTORY_UPDATER_FAIL.SHORTLIST_CREATE_FAIL);

		try {
			replay(mockUpdater);
			MultimediaDistributionList list  = new MultimediaDistributionList(mockOam.getLogAgent(),null,1,testUserMsid, mockUpdater);
			try {
				list.create(1);
			}
			catch (DistributionListException e) {
				System.out.println("EXCELLENT! Got an exception when shortlist cannot be created: " + e.getMessage());
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
			fail("Unexpected exception");
		}
		verify(mockUpdater);
	}

	@Test
	public void testCreateShortlistUpdateSubscriberFailsExpectFail(){
		String testUserMsid = "msid:a71a2cc6c4f7b2a3";
		String msisdn = "491721093078";

		IDirectoryUpdater mockUpdater = createMockDirectoryUpdaterWithFailSpec(testUserMsid, msisdn,1, DIRECTORY_UPDATER_FAIL.SUBSCRIBER_UPDATE_FAIL);

		try {
			replay(mockUpdater);
			MultimediaDistributionList list  = new MultimediaDistributionList(mockOam.getLogAgent(),null,1, testUserMsid, mockUpdater);
			try {
				list.create(1);
			}
			catch (DistributionListException e) {
				System.out.println("EXCELLENT! Got an exception when subscriber cannot be updated: " + e.getMessage());
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
			fail("Unexpected exception");
		}
		verify(mockUpdater);
	}

	@Test
	public void testCreateAndAddSpokenNameAndThenGet(){
		String testUserMsid = "msid:a71a2cc6c4f7b2a3";
		String msisdn = "491721093078";
		IDirectoryUpdater mockUpdater = createMockDirectoryUpdater(testUserMsid, msisdn, 1);
		try {
			replay(mockUpdater);
			MultimediaDistributionList list  = new MultimediaDistributionList(mockOam.getLogAgent(),null,  1, testUserMsid, mockUpdater);
			list.create(1);
			list.setProperty("Content-Duration", "5 seconds");
			File mediaFile = new File("/userfiles/views/lmcantl_msgcoreapp_r20__snapshot/msgcore_mcc/messagedepositor/component/data/message_07.3gp");
			FileInputStream is = new FileInputStream(mediaFile);
			list.setMedia(is);
			list.store();

			IDirectoryUpdater mockUpdaterForGet = createMockDirectoryUpdater(testUserMsid, msisdn, 1);
			replay(mockUpdaterForGet);
			MultimediaDistributionList listToGet  = new MultimediaDistributionList(mockOam.getLogAgent(), null, 1, testUserMsid, mockUpdaterForGet);
			InputStream is2 = listToGet.getMedia();
			assertTrue(is2 != null);
			String value =listToGet.getProperty("Content-Duration");
			assertTrue(value.equals("5 seconds"));
		}
		catch (DistributionListException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
		verify(mockUpdater);
	}

	@Test
	public void testShortlistWith2EntriesButSecondOneIsRemovedFromMCDExpectUnknownTel(){
		String testUserMsid = "msid:a71a2cc6c4f7b2a3";
		String msidn = "491721093078";

		String listEntries[] = { testUserMsid, "msid:aaaabbbbccccdddd" };
		ProfileContainer shortlist = this.createShortlist("muid:" + msidn + "_1", testUserMsid, listEntries);
		IDirectoryUpdater mockUpdater = createMockDirectoryUpdaterForListLookup(testUserMsid, msidn,1, shortlist);

		try {
			replay(mockUpdater);
			MultimediaDistributionList list  = new MultimediaDistributionList(mockOam.getLogAgent(),null,1,testUserMsid, mockUpdater);
			String members[] = list.getMembers();
			assertTrue(members.length == 2);
			assertTrue(members[0].equals("+" + msidn));
			assertTrue(members[1].equals("1111111111"));
		}
		catch (Exception e1) {
			e1.printStackTrace();
			fail("Unexpected exception");
		}
		verify(mockUpdater);

	}

	@Test
	public void testShortlistWith2EntriesButSecondOneIsRemovedFromMCDExpectAliasTel(){
		String testUserMsid = "msid:a71a2cc6c4f7b2a3";
		String msidn = "491721093078";
		String alias = "491721093078";

		String listEntries[] = { testUserMsid, "msid:aaaabbbbccccdddd;" + "tel:" + alias };
		ProfileContainer shortlist = this.createShortlist("muid:" + msidn + "_1", testUserMsid, listEntries);
		IDirectoryUpdater mockUpdater = createMockDirectoryUpdaterForListLookup(testUserMsid, msidn,1, shortlist);

		try {
			replay(mockUpdater);
			MultimediaDistributionList list  = new MultimediaDistributionList(mockOam.getLogAgent(),null,1,testUserMsid, mockUpdater);
			String members[] = list.getMembers();
			assertTrue(members.length == 2);
			assertTrue(members[0].equals("+" + "491721093078"));
			assertTrue(members[1].equals(alias));
		}
		catch (Exception e1) {
			e1.printStackTrace();
			fail("Unexpected exception");
		}
		verify(mockUpdater);

	}

	/**
	 * This method creates a DistributionList fixture for unit tests
	 * @param prefix this is an optional prefix to prepend to the msid (for negative tests)
	 * @param anMsid
	 * @return
	 */
	private DistributionList createDistributionList(String prefix, String anMsid){
		String fullmsid = anMsid;
		if (prefix != null){
			fullmsid = prefix + fullmsid;
		}
		DistributionList list = new DistributionList(mockOam.getLogAgent(),fullmsid);
		list.setCommonMessagingAccess(createCommonMessagingAccess(anMsid));
		return list;
	}

	private ICommonMessagingAccess createCommonMessagingAccess(String anMsid){
		ICommonMessagingAccess mockAccess = createNiceMock(ICommonMessagingAccess.class);
		expect(mockAccess.getMoipPrivateFolder((String)anMsid, true)).andReturn(buildExpectedPrivateDirectory()).anyTimes();
		replay(mockAccess);
		return mockAccess;
	}

	private String buildExpectedPrivateDirectory(){
		return mfsRootPath + File.separator
		+ "internal" + File.separator + "a" + File.separator + "71"
		+ File.separator + "a2" + File.separator + "cc"
		+ File.separator + "6c" + File.separator + "4f7b2a3"
		+ File.separator + "private" + File.separator + "moip";
	}

	private String buildExpectedDlistDirectory(){
		return buildExpectedPrivateDirectory() + File.separator + "dlists"+ File.separator ;
	}


	private String buildExpectedDlistDirectory(int nextId){
		return buildExpectedDlistDirectory() + File.separator + String.valueOf(nextId);
	}
}
