package com.abcxyz.services.moip.broadcastannouncement;

import java.util.ArrayList;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.broadcastannouncement.BroadcastAnnouncement;
import com.abcxyz.services.broadcastannouncement.BroadcastException;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;

public class BroadcastManager extends com.abcxyz.services.broadcastannouncement.BroadcastManager{

	private static BroadcastManager instance  = null;
	
	private BroadcastManager() throws Exception{
        super(CommonOamManager.getInstance().getMcdOam());
    }
	

	public static BroadcastManager getInstance() throws BroadcastException{
	    if (instance == null){
	        try{
	            instance = new BroadcastManager();
	        }catch (Exception e){
	            throw new BroadcastException("BroadcastException: BroadcastManager cannot be initialized!\n" + e.getMessage());
	        }
	    }
		return instance;
	}

	public void setLogAgent(LogAgent logger){
		this.logger = logger;
	}

	/**
	 * Get all the broadcast announcements applicable to this subscriber
	 * @param subscriber the subscriber phone number
	 * @return an ArrayList of broadcast announcements
	 * @throws BroadcastException If an error occur while retrieving the broadcast announcement
	 */
	public ArrayList<BroadcastAnnouncement> getBroadcastAnnouncements(String subscriber) throws BroadcastException {
		IDirectoryAccessSubscriber das = DirectoryAccess.getInstance().lookupSubscriber(subscriber);
		return getBroadcastAnnouncements(das.getSubscriberProfile().getProfile(), das.getCosProfile().getProfile());
	}

	/**
	 * Get all the broadcast announcements applicable to this subscriber
	 * @param subscriber the subscriber profile
	 * @return an ArrayList of broadcast announcements
	 * @throws BroadcastException If an error occur while retrieving the broadcast announcement
	 */
	public ArrayList<BroadcastAnnouncement> getBroadcastAnnouncements(IDirectoryAccessSubscriber subscriber) throws BroadcastException {
		return getBroadcastAnnouncements(subscriber.getSubscriberProfile().getProfile(), subscriber.getCosProfile().getProfile());
	}


}
