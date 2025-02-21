/**
 * Copyright (c) 2009 Abcxyz
 * All Rights Reserved
 */

package com.mobeon.ntf.slamdown;

import java.io.File;
import java.util.Properties;

import com.abcxyz.messaging.mfs.MsgStoreServerFactory;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * SlamdownList remembers information about slamdown information to one phone.
 */
public class SlamdownList {

    public static final String SLAMDOWNMCN_STATUS_FILE = "slamdownmcn.status";
    public static final String SLAMDOWNMCN_PHONE_ON_LOCK_FILE = "slamdownmcn_phoneon.lock";

    public static final int NOTIFICATION_TYPE_SLAMDOWN = 0;
    public static final int NOTIFICATION_TYPE_MCN_EXTERNAL = 1;
    public static final int NOTIFICATION_TYPE_MCN_INTERNAL = 2;

    public static final String PATTERN_FILE_ALL = "all";
    public static final String PATTERN_FILE_PENDING = "_";
    public static final String PATTERN_FILE_FINAL = "f";
    
    /** Slamdown states */
    public static final byte STATE_SENDING_UNIT = 0;
    //public static final byte STATE_AGGREGATING = 1; // Must be introduced in a second phase 2
    public static final byte STATE_WAITING_PHONE_ON = 2;
    public static final byte STATE_SENDING_INFO = 3;
    public static final byte STATE_DONE = 4;

    public static final String[] STATE_STRING = {
        "Idle",
        "Aggregating",
        "Waiting PhoneOn",
        "Sending Info",
        "Done"
    };

    /** Slamdown events */
    public static final int EVENT_NEW_NOTIF = 0;
    public static final int EVENT_NEW_NOTIF_WITHOUT_PHONE_ON_REQ = 1;
    public static final int EVENT_SCHEDULER_RETRY = 2;
    public static final int EVENT_SCHEDULER_EXPIRY = 3;
    public static final int EVENT_CLIENT_PHONE_ON_SENT_SUCCESSFULLY = 4;
    public static final int EVENT_CLIENT_RETRY = 5;
    public static final int EVENT_CLIENT_FAILED = 6;
    public static final int EVENT_PHONE_ON_OK = 7;
    public static final int EVENT_PHONE_ON_RETRY = 8;
    public static final int EVENT_PHONE_ON_FAILED = 9;
    public static final int EVENT_SMS_INFO_RESPONSE_SUCCESSFUL = 10;

    public static final String[] EVENT_STRING = {
        "New Notification",
        "New Notification without Phone-On",
        "Scheduler Retry",
        "Scheduler Expiry",
        "SMS Unit PhoneOn Sent Successfully",
        "SMS Unit Retry",
        "SMS Unit Failed",
        "PhoneOn OK",
        "PhoneOn Retry",
        "PhoneOn Failed",
        "SMS-Info Response successful"
    };
    
    private static final MsgStoreServerFactory.TYPE storeType = MsgStoreServerFactory.getConfiguredType();
    private SlamdownListAbstract slImpl = null;

	/**
	 * Constructor
	 * @param subscriberNumber SubscriberNumber
	 * @param notificationNumber NotificationNumber
	 * @param userInfo UserInfo
	 * @param validity Validity period
	 * @param cosName Subscriber's COS name
	 * @param internal Slamdown/Mcn
	 * @param notificationType Slamdown/Mcn internal/Mcn external
	 */
	public SlamdownList(String subscriberNumber, String notificationNumber, UserInfo userInfo, int validity, String cosName, boolean internal, int notificationType) {

		if (storeType == MsgStoreServerFactory.TYPE.NOSQL) {
			slImpl = new SlamdownListNoSQL(subscriberNumber, notificationNumber, userInfo, validity, cosName, internal, notificationType); 
		} else {
			slImpl = new SlamdownListFS(subscriberNumber, notificationNumber, userInfo, validity, cosName, internal, notificationType);
		}
    }

    public static void setMfsEventManager(IMfsEventManager manager){
    	if (storeType == MsgStoreServerFactory.TYPE.NOSQL) {
    		SlamdownListNoSQL.setMfsEventManager(manager);
    	} else {
    		SlamdownListFS.setMfsEventManager(manager);
    	}
    }
    
    public void updateScheduledEventsIds(AppliEventInfo eventInfo) {
    	slImpl.updateScheduledEventsIds(eventInfo);
    }
    
    public String getSubscriberNumber() {
        return slImpl.getSubscriberNumber();
    }

    public String getNotificationNumber() {
        return slImpl.getNotificationNumber();
    }
    
    public UserInfo getUserInfo() {
        return slImpl.getUserInfo();
    }
    
    public String getNumber() {
        return slImpl.getNumber();
    }

    public Properties getEventProperties() {
    	return slImpl.getEventProperties();
    }    

    public String getOrigDestinationNumber() {
    	return slImpl.getOrigDestinationNumber();
    }

    public int getCurrentState() {
    	return slImpl.getCurrentState();
    }
    public int getCurrentEvent() {
        return slImpl.getCurrentEvent();
    }
    public boolean getInternal() {
        return slImpl.getInternal();
    }
    public int getNotificationType() {
        return slImpl.getNotificationType();
    }
    public String getFilename() {
        return slImpl.getFilename();
    }
    public String getType() {
    	return slImpl.getType();
    }
    
    public void setCurrentState(int currentState) {
    	slImpl.setCurrentState(currentState);
    }

    public void setCurrentEvent(int currentEvent) {
    	slImpl.setCurrentEvent(currentEvent);
    }

    public void setNotificationType(int notificationType) {
    	slImpl.setNotificationType(notificationType);
    }
    
    public String getPreferredLanguage() {
    	return slImpl.getPreferredLanguage();
    }
    
    public String getCosName() {
        return slImpl.getCosName();
    }

    public int getValidity() {
        return slImpl.getValidity();
    }

    public void setPhoneOnLockId(long lockId) {
        slImpl.setPhoneOnLockId(lockId);
    }

    public long getPhoneOnLockId() {
       return slImpl.getPhoneOnLockId();
    }

    /**
     * If the slamdown information list has calls from a single caller
     * @return true if all calls are from single caller
     */
    public boolean isSingleCaller() {
    	return slImpl.isSingleCaller();
    }

    /**
     * Obtains the sorted caller list
     * @return Sorted CallerInfo
     */
    public CallerInfo[] sortCallers() {
    	return slImpl.sortCallers();
    }

    public CallerInfo[] getCallers() {
    	return slImpl.getCallers();
    }


    /**
     * This method recreates a SlamdownList object from persistent storage (if found)
     * @param notificationNumber notificationNumber
     * @return List of SlamdownList
     */
    public static SlamdownList[] recreateSlamdownList(String notificationNumber) {
    	if (storeType == MsgStoreServerFactory.TYPE.NOSQL) {
    		return SlamdownListNoSQL.recreateSlamdownList(notificationNumber);
    	} else {
    		return SlamdownListFS.recreateSlamdownList(notificationNumber);
    	}
    }

    public void renameSlamdownMcnFilesAsHandled() {
    	slImpl.renameSlamdownMcnFilesAsHandled();
    }
    
    /**
     * Retrieves the persistent scheduler-id values from the subscriber (or non subscriber) private/events directory.
     * Values are stored in the SchedulerIds private member.
     */
    public void retrieveSchedulerEventIdsPersistent() {
    	slImpl.retrieveSchedulerEventIdsPersistent();
    }

    /**
     * Update the scheduler-id values for a subscriber (or non subscriber) in the private/events directory.
     * Values stored are taken from the SchedulerIds private member.
     * @return boolean True if the update is successful, false otherwise
     */
    public boolean updateEventIdsPersistent() {
    	return slImpl.updateEventIdsPersistent();
    }

    /**
     * Removes the SLAMDOWNMCN_STATUS_FILE for the given notification number. 
     * @param notificationNumber NotificationNumber
     */
    public static void removeSchedulerIdsPersistent(String notificationNumber) {
    	if (storeType == MsgStoreServerFactory.TYPE.NOSQL) {
    		SlamdownListNoSQL.removeSchedulerIdsPersistent(notificationNumber);
    	} else {
    		SlamdownListFS.removeSchedulerIdsPersistent(notificationNumber);
    	}
    }


    public SchedulerIds getSchedulerIds() {
        return slImpl.getSchedulerIds();
    }
    
    /**
     * Sets the file list for this slamdown list.
     * @param files File list.
     */
    public void setFileList(File[] files) {
        slImpl.setFileList(files);
    }
    
    /**
     * Returns the list of files for this slamdown list.
     * @return Event files. Returns null if there is no event files.
     */
    public File[] getFileList() {
    	return slImpl.getFileList();
    }
}
