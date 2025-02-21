package com.mobeon.masp.profilemanager.distributionlists;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.util.FileExtension;
import com.abcxyz.messaging.common.util.HashGenerator;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccessException;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryUpdater;
import com.abcxyz.services.moip.distributionlist.IDistributionList;
import com.mobeon.masp.profilemanager.IMultimediaDistributionList;
import com.mobeon.masp.profilemanager.MasTestHelper;

public class DistributionListTest {
	
	public enum DIRECTORY_UPDATER_FAIL {
		SUBSCRIBER_LOOKUP_FAIL,
		SHORTLIST_LOOKUP_FAIL,
		SUBSCRIBER_UPDATE_FAIL,
		SHORTLIST_CREATE_FAIL,
		NO_FAIL
	}
	
	protected IDirectoryUpdater createMockDirectoryUpdater(String aTestUserMsid, String aListOwnerTel, int anId){
		return createMockDirectoryUpdaterWithFailSpec(aTestUserMsid, aListOwnerTel, anId, DIRECTORY_UPDATER_FAIL.NO_FAIL);
	}
	
	protected IDirectoryUpdater createMockDirectoryUpdaterForListLookup(String aTestUserMsid, String aListOwnerMsisdn, int anId, ProfileContainer aShortlistToReturn){
		IDirectoryUpdater mockUpdater = null;
		String listMuid = "muid:+" + aListOwnerMsisdn +  "_" + String.valueOf(anId);

		ProfileContainer dummyProfile = createDummySubscriber(aListOwnerMsisdn);
		
		try {
			mockUpdater= createNiceMock(IDirectoryUpdater.class);
			expect(mockUpdater.lookup("shortlist", new URI(listMuid))).andReturn(aShortlistToReturn).anyTimes();
			expect(mockUpdater.lookup("subscriber", new URI(aTestUserMsid))).andReturn(dummyProfile).anyTimes();

		}
		catch (Exception e){
			
		}
		return mockUpdater;
	}
	
	protected IDirectoryUpdater createMockDirectoryUpdaterWithFailSpec(String aTestUserMsid, String aListOwnerMsisdn, int anId, DIRECTORY_UPDATER_FAIL aFailureType){
		IDirectoryUpdater mockUpdater = null;
		String listMuid = "muid:+" + aListOwnerMsisdn +  "_" + String.valueOf(anId);
		
		ProfileContainer dummyShortlist = createDummyShortlist(listMuid, aTestUserMsid); 
		ProfileContainer dummyProfile = createDummySubscriber(aListOwnerMsisdn);
		try {
			mockUpdater= createNiceMock(IDirectoryUpdater.class);
			if (aFailureType == DIRECTORY_UPDATER_FAIL.SUBSCRIBER_LOOKUP_FAIL){
				expect(mockUpdater.lookup("subscriber", new URI(aTestUserMsid))).andReturn(null).once();
				return mockUpdater; // when specifying a failure, just return immediately
			}
			else {
				expect(mockUpdater.lookup("subscriber", new URI(aTestUserMsid))).andReturn(dummyProfile).anyTimes();
			}
			
			if (aFailureType == DIRECTORY_UPDATER_FAIL.SHORTLIST_CREATE_FAIL){
				mockUpdater.createProfile((String)anyObject(), (URI)anyObject(), (Profile)anyObject());
				expectLastCall().andThrow(new DirectoryAccessException("Mock message: could not create shortlist")).once();
				return mockUpdater; // when specifying a failure, just return immediately
			}
			else {
				mockUpdater.createProfile((String)anyObject(), (URI)anyObject(), (Profile)anyObject());
				expectLastCall().once();
			}

			if (aFailureType == DIRECTORY_UPDATER_FAIL.SHORTLIST_LOOKUP_FAIL){
				expect(mockUpdater.lookup("shortlist", new URI(listMuid))).andThrow(new DirectoryAccessException("MOck message: could not lookup shortlist")).once();
				return mockUpdater; // when specifying a failure, just return immediately
			}
			else {
				expect(mockUpdater.lookup("shortlist", new URI(listMuid))).andReturn(dummyShortlist).anyTimes();
			}
	
			

			if (aFailureType == DIRECTORY_UPDATER_FAIL.SUBSCRIBER_UPDATE_FAIL){
				mockUpdater.updateProfile((String)anyObject(), (URI) anyObject(), (List<Modification>) anyObject());
				expectLastCall().andThrow(new DirectoryAccessException("Mock message: could not update subscriber")).once();
				
				// If the subscriber update fails, should we delete the list?
				return mockUpdater; // when specifying a failure, just return immediately
			}
			else {
				mockUpdater.updateProfile((String)anyObject(), (URI) anyObject(), (List<Modification>) anyObject());
				expectLastCall().once();
			}
			return mockUpdater;
		} 
		catch (DirectoryAccessException e) {
			e.printStackTrace();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected IDistributionList createMockDistributionList(){
		IDistributionList mockList = createNiceMock(IDistributionList.class);
		return mockList;
	}
	
	protected IMultimediaDistributionList createMockMultimediaDistributionList(){
		IMultimediaDistributionList mockList = createNiceMock(IMultimediaDistributionList.class);
		return mockList;
	}
	
	
	protected LogAgent createMockLogAgent(){
		LogAgent mockLog = createNiceMock(LogAgent.class);
		mockLog.debug((String) anyObject());
		expectLastCall().anyTimes();
		mockLog.error((String) anyObject());
		expectLastCall().anyTimes();
		mockLog.info((String) anyObject());
		expectLastCall().anyTimes();
		mockLog.fatal((String) anyObject());
		expectLastCall().anyTimes();
		
		expect(mockLog.isDebugEnabled()).andReturn(false).anyTimes();
		return mockLog;
	}
	
	
	protected void deleteTestDirectory(){
		String expectedDlistDirectory = MasTestHelper.getMfsTestRootPath() + File.separator
		+ "internal" + File.separator + "a" + File.separator + "71"
		+ File.separator + "a2" + File.separator + "cc"
		+ File.separator + "6c" + File.separator + "4f7b2a3";
		FileExtension myFile = new FileExtension(expectedDlistDirectory); 
		myFile.deleteDir();
	}
	
	protected ProfileContainer createDummyShortlist(String aListMuid, String aSubscriberMsid){
		StringBuffer sb = new StringBuffer("msid:");
		
		ProfileContainer dummyShortlist = new ProfileContainer();
		try {
			dummyShortlist.addIdentity(aListMuid);
			sb.append(HashGenerator.getInstance().hashData(aListMuid, 8));
			dummyShortlist.addIdentity(new URI(sb.toString()));
			dummyShortlist.addAttributeValue("MOIPListOwner", aSubscriberMsid);
			dummyShortlist.addAttributeValue("MOIPListEntry", aSubscriberMsid + ";tel:5555555555");
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return dummyShortlist;
		
	}
	
	protected ProfileContainer createShortlist(String aListMuid, String aSubscriberMsid, String listEntries[]){
		StringBuffer sb = new StringBuffer("msid:");
		
		ProfileContainer dummyShortlist = new ProfileContainer();
		try {
			dummyShortlist.addIdentity(aListMuid);
			sb.append(HashGenerator.getInstance().hashData(aListMuid, 8));
			dummyShortlist.addIdentity(new URI(sb.toString()));
			dummyShortlist.addAttributeValue("MOIPListOwner", aSubscriberMsid);
			for (int i=0; i < listEntries.length; i++){
				dummyShortlist.addAttributeValue("MOIPListEntry", listEntries[i]);
			}
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return dummyShortlist;
		
	}
	
	protected ProfileContainer createDummySubscriber(String aListOwnerTel){
		ProfileContainer dummyProfile = new ProfileContainer();
		
		try {
			String subscriberMuid = "muid:" + aListOwnerTel;
			dummyProfile.addIdentity(new URI("msid:" + HashGenerator.getInstance().hashData(subscriberMuid, 8)));
			dummyProfile.addIdentity(new URI(subscriberMuid));
			dummyProfile.addIdentity(new URI("tel:+" + aListOwnerTel));
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return dummyProfile;
	}
}
