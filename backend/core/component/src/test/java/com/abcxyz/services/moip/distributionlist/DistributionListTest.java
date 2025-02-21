package com.abcxyz.services.moip.distributionlist;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;


import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;

import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.oam.OamTestHelper;
import com.abcxyz.messaging.common.util.FileExtension;
import com.abcxyz.messaging.common.util.SystemPropertyHandler;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;

public class DistributionListTest {
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
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//CommonTestingSetup.tearDown();
		if (oldFileLinkValue != null){
			SystemPropertyHandler.setProperty("abcxyz.mfs.filelink",oldFileLinkValue);
			System.setProperty("abcxyz.mfs.filelink", oldFileLinkValue);
		}
		
	}

	@Before
	public void setUp() throws Exception {
		deleteTestDirectory();
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testSimpleCreateAndThenRemove() {
		createTestDirectory(0);
		try {
			DistributionList dlist = createDistributionList(null, "a71a2cc6c4f7b2a3");
			int nextId = dlist.getNextAvailableId();
			assertTrue(nextId == 1);

			String createdDir = dlist.create(nextId);
			String expectedDlistDirectory = buildExpectedDlistDirectory(nextId);
					
			File created = new File(createdDir);
			String secondCreatedDir = dlist.create(++nextId);
			File secondCreated = new File(secondCreatedDir);
			
			File expected = new File(expectedDlistDirectory);
			System.out.println("testSimpleCreate>>Expected dir "
					+ expected.getCanonicalPath() + ", received " + created.getCanonicalPath());
			assertTrue(expected.getCanonicalPath().equals(
					created.getCanonicalPath()));
			
			dlist.remove(1);
			dlist.remove(2);
			
			if (created.exists()){
				fail("Problem. Removed the dir but still there!");
			}
			if (secondCreated.exists()){
				fail("Problem. Removed the second dir but still there!");
			}
			
			
			
		} catch (DistributionListException e) {
			fail("Did not create list. Got exception: " + e.getMessage());
		} catch (IOException e) {
			fail("Could not compare directories");
		}
	}
	
	@Test
	public void testNextAvailableId() {
		createTestDirectory(3);
		DistributionList dlist = createDistributionList(null, "a71a2cc6c4f7b2a3");
		int nextId;
		try {
			nextId = dlist.getNextAvailableId();
			assertTrue(nextId == 4);
		} catch (DistributionListException e) {
			fail("getNextAvailableId>>Caught unexpected exception");
		}
	}
	
	
	@Test
	public void testCreateDuplicateExpectSecondGetsException(){
		
		createTestDirectory(0);
		
		try {
			DistributionList dlist = createDistributionList(null,"a71a2cc6c4f7b2a3");
			int nextId = dlist.getNextAvailableId();
			assertTrue(nextId == 1);
			dlist.create(nextId);
			dlist.create(nextId);
			fail("Problem. Duplicate create didn't get exception");
		} 
		catch (DistributionListException e) {
			System.out.println("Excellent, got exception on duplicate create");
		}
	}
	
	@Test
	public void testSimpleCreateWithMsidPrefixSpecifiedExpectItToBeStripped() {
		
		createTestDirectory(0);
		try {
			DistributionList dlist = createDistributionList("msid:", "a71a2cc6c4f7b2a3");
			int nextId = dlist.getNextAvailableId();
			assertTrue(nextId == 1);

			String createdDir = dlist.create(nextId);
			String expectedDlistDirectory = buildExpectedDlistDirectory(nextId);
					
			File created = new File(createdDir);
			File expected = new File(expectedDlistDirectory);
			System.out.println("testSimpleCreateWithMsidPrefixSpecifiedExpectItToBeStripped>>Expected dir "
					+ expected.getCanonicalPath() + ", received " + created.getCanonicalPath());
			assertTrue(expected.getCanonicalPath().equals(
					created.getCanonicalPath()));
		} catch (DistributionListException e) {
			fail("Did not create list. Got exception: " + e.getMessage());
		} catch (IOException e) {
			fail("Could not compare directories");
		}
	}
	
	@Test
	public void testSimpleCreateWithNullMsidExpectExceptionsOnCreate() {
		
		createTestDirectory(0);
		int nextId = 1;
		DistributionList dlist = null;
		try {
			dlist = createDistributionList(null,null);
			nextId = dlist.getNextAvailableId();
			assertTrue(nextId == 1);
		} catch (DistributionListException e) {
			System.out.println("Perfect. Got exception on getNextAvailable Id");
		} 
			
		try {
			String createdDir = dlist.create(nextId);
			String expectedDlistDirectory = buildExpectedDlistDirectory(nextId);
					
			File created = new File(createdDir);
			File expected = new File(expectedDlistDirectory);
			System.out.println("testSimpleCreateWithMsidPrefixSpecifiedExpectItToBeStripped>>Expected dir "
					+ expected.getCanonicalPath() + ", received " + created.getCanonicalPath());
			assertTrue(expected.getCanonicalPath().equals(
					created.getCanonicalPath()));
		}
		catch (DistributionListException e) {
			System.out.println("Perfect. Got exception on create with null");
		}
		catch (IOException e) {
			fail("Could not compare directories");
		}
	}
	

	
	@Test
	public void testGetNextAvailableWithGarbageInDlistDirectory() {
		createTestDirectory(0);
		addStuffToTestDirectory("file", true);
		addStuffToTestDirectory("1", false);
		try {
			DistributionList dlist = createDistributionList(null, "a71a2cc6c4f7b2a3");
			int nextId = dlist.getNextAvailableId();
			assertTrue(nextId == 1);
		}
		catch (DistributionListException e) {
			fail("Problem getting nextAvailableId: " + e.getMessage());
		}
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
	
	private void createTestDirectory(int numberOfLists){
		String pathToDlistDir = buildExpectedDlistDirectory();
		File dir = new File(pathToDlistDir);
		dir.mkdirs();
		if (numberOfLists != 0){
			for (int i=1; i <= numberOfLists; i++){
				File currentDList = new File(pathToDlistDir + File.separator + String.valueOf(i));
				currentDList.mkdir();
			}
		}
	}
	
	private void addStuffToTestDirectory(String aName, boolean aDirectory){
		String pathToDlistDir = buildExpectedDlistDirectory();
		String pathToStuff = pathToDlistDir + File.separator + aName;
		
		if (aDirectory){
			File stuffDir = new File(pathToStuff);
			stuffDir.mkdir();
		}
		else {
			File stuffFile = new File(pathToStuff);
			try {
				stuffFile.createNewFile();
			} catch (IOException e) {
				System.err.println("BIG PROBLEM creating test file");
			}
		}
	}
	
	private void deleteTestDirectory(){
		String expectedDlistDirectory = mfsRootPath + File.separator
		+ "internal" + File.separator + "a" + File.separator + "71"
		+ File.separator + "a2" + File.separator + "cc"
		+ File.separator + "6c" + File.separator + "4f7b2a3";
		FileExtension myFile = new FileExtension(expectedDlistDirectory); 
		myFile.deleteDir();
	}
	

}
