/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.INotifierMfsEventManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.INotifierMfsEventManager.NotifierFileStatusEnum;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierUtil;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.NotifierConstants;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.NotifierHelper;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateSmsPlugin;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.NotifierTypeEvent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.CPHRNotifTYPE;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.NotifierTypeState;

/**
 * Container class for NotifierEvent
 */
public class NotifierEvent {
	
	//CONSTANTS
	private static final int MAX_ACQUIRE_LOCK_VALIDITY_SECONDS = 2; //max time a lock is valid. Should be short as the lock is transient.
	private static final long ACQUIER_LOCK_SLEEP_TIME_MS = 100; //the sleep time between checks to acquire lock to access the in progress file.
	private static final long MAX_ACQUIRE_LOCK_TRIES = 3; //max times to try to acquire lock before letting the scheduler retry the event later

    /** Private utility members */
    private static INotifierLogger log = TemplateSmsPlugin.getLoggerFactory().getLogger(NotifierEvent.class);
    private static INotifierMfsEventManager notifierMfsEventManager = TemplateSmsPlugin.getMfsEventManager();
    private static INotifierUtil notifierUtil = TemplateSmsPlugin.getUtil();

    
    public static ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(NotifierConstants.DATE_FORMAT);
        }
    };
        
    /** Persistent files */
    public static final String STATUS_FILE = ".status";
    public static final String PHONEON_LOCK_FILE = "phoneon.lock";
    public static final String SENDING = ".sending";
    public static final String SEPARATOR = "_";
    

    /** Notifier properties */
    public static final String CPHR_NOTIFICATION_TYPE = "cphrType";

    /** Notifier properties - Group 1 */
    public static final String SCHEDULER_ID = "sch-id";
    public static final String LATEST_TIMESTAMP = "latesttime";

	


    /** Private members */
    Properties eventProperties = null;
    private NotifierTypeState notifierTypeState = NotifierTypeState.STATE_INITIAL;
    private NotifierTypeEvent notifierTypeEvent = NotifierTypeEvent.EVENT_NEW_NOTIF;
    private String notificationNumber = null;
    private TemplateType notifierType = null;
    private ANotifierDatabaseSubscriberProfile subscriberProfile = null;
    private String validatedSender = null;
    private Boolean acquiredSending = null; 
    
    /** Private members - Persistent */
    private SchedulerIds schedulerIds = new SchedulerIds();
    private String latestTimestamp = null;

	private long progressLockId = 0; //the lock id for the progress file, while we are performing critical op.
	private Object accessExistsLock = new Object();

	;
	

    /**
     * Constructor invoked when mandatory properties must be populated. 
     * For example, when a new incoming signal is first received by this Notifier plug-in.
     * @param notifierType the NotifierType object containing all information for the type
     * @param handledNotification the notification signal that is handled by the notifier type
     * @param receiverNumber the number of the receiver of the message
     * @param notificationNumber the number to which the notification should be sent
     * @param properties the information pertaining to the notification to be sent out
     * @throws NotifierMfsException if retrieving the information from the MFS fails.
     */
    public NotifierEvent(TemplateType notifierType, CPHRNotifTYPE handledNotification, String receiverNumber, String notificationNumber, Properties properties) throws NotifierMfsException {
        this.notifierType = notifierType;
        if(properties != null) {
            eventProperties = properties;
        } else {
            eventProperties = new Properties();
        }

        eventProperties.setProperty(NotifierConstants.NOTIFIER_TYPE_NAME_PROPERTY, notifierType.getCphrTypeName());
        eventProperties.setProperty(NotifierConstants.NOTIFIER_CPHR_TEMPLATE_NAME, notifierType.getCphr_template_name());
        eventProperties.setProperty(CPHR_NOTIFICATION_TYPE, handledNotification.getName());
        eventProperties.setProperty(NotifierConstants.RECEIVER_PHONE_NUMBER_PROPERTY, receiverNumber);
        eventProperties.setProperty(NotifierConstants.NOTIFICATION_PHONE_NUMBER_PROPERTY, notificationNumber);
        
        addMandatoryProperties();
    }

    /**
     * Constructor invoked when all mandatory properties are already known and populated.
     * For example, when an event is received from Scheduler or when the event properties are read from a persistent file.
     * @param properties Event properties
     */
    public NotifierEvent(TemplateType notifierType, Properties properties) {   
        this.notifierType = notifierType;
        
        log.debug("NotifierEvent constructor called with properties: " + properties);
        if (properties != null) {
            eventProperties = properties;
        } else {
            eventProperties = new Properties();
        }

         validatedSender = validateSender(eventProperties.getProperty(NotifierConstants.SENDER_PHONE_NUMBER_PROPERTY), 
                                          eventProperties.getProperty(NotifierConstants.SENDER_DISPLAY_NAME_PROPERTY), 
                                          eventProperties.getProperty(NotifierConstants.SENDER_VISIBILITY_PROPERTY));

        log.debug("NotifierEvent constructor: validatedSender set to: " + validatedSender);        
    }
    
    @SuppressWarnings("unused")
    //Hide default constructor.
	private NotifierEvent() 
    {
    	
    }

    public CPHRNotifTYPE getCphrNotifType() {
        CPHRNotifTYPE cphrNotifType = null;
        try{
            String handledNotif = eventProperties.getProperty(CPHR_NOTIFICATION_TYPE);
            if (handledNotif != null){
                cphrNotifType = CPHRNotifTYPE.valueOf(handledNotif.toUpperCase());                   
            }
        } catch (Throwable t){
            log.debug("Could not recreate handledNotif from properties: " + t.getMessage());
        }
        
        return cphrNotifType;
    }
    
    public Properties getEventProperties() {
        return eventProperties;
    }

    public class SchedulerIds {

        /**
         * The 2 containers (eventId, eventId_waitingPhoneOn) could contain the following event types:
         * 
         * 1) eventId to contain the following event types:                 initial, sending, afterPhoneOn
         */
        public String eventId = null;
        public String eventId_waitingPhoneOn = null;
 
        /** Constructor */
        public SchedulerIds() {
        }

        public String getEventId() {
            return eventId;
        }

        public boolean setEventId(String eventId) {
        	if ((eventId == this.eventId) || (eventId != null && eventId.equals(this.eventId))) {
        		return false; //if same including null checks.
        	}
        	this.eventId = eventId;
        	if (eventId == null) {
        		this.eventId_waitingPhoneOn = null;
        	}
        	return true;
        }
 
        public boolean isEmtpy() {
            if (!isEventIdEmpty() ) {
                return false;
            }
            return true;
        }

        public boolean isEventIdEmpty() {
            if (eventId != null && !eventId.isEmpty()) {
                return false;
            }
            return true;
        }

        public boolean isGroup1Empty() {
            if (!isEventIdEmpty() ) {
                return false;
            }
            return true;
        }

        public void nullify() {
            this.eventId = null;
            this.eventId_waitingPhoneOn = null;
           
        }


        public String toString() {
            return "SchedulerIds: \n" +
                    "eventId: " + (this.eventId != null ? this.eventId : "") + "\n" ;
                   
        }
    }

    /**
     * Retrieve the eventIds from the appropriate persistent file (.status)
     * @return true if the properties have been retrieved successfully, false otherwise.
     */
    public boolean retrieveSchedulerEventIdsPersistent() {
        boolean result = true;

        String filename = getStatusFilename();
        if (filename == null || filename.isEmpty()) {
            log.debug("No filename to retrieveSchedulerEventIdsPersistent for " + this.getIdentity());
            return false;
        }

        Properties properties = notifierMfsEventManager.getProperties(this.getNotificationNumber(), filename);
        if (properties != null) {
                schedulerIds.setEventId(properties.getProperty(SCHEDULER_ID));
                latestTimestamp = properties.getProperty(LATEST_TIMESTAMP);
                log.debug("Read the " + filename + " file for " + this.getIdentity() + " and retrieved eventIds:\n " + schedulerIds);
        } else {
            result = false;
        }

        return result;
    }

    /**
     * Update the scheduler-id values for a subscriber (or non subscriber) in the private/events directory
     * for the given notificationEventType (NotifierEventType).
     * Values stored are taken from the SchedulerIds private member.
     * @return boolean True if the update is successful, false otherwise
     */
     public boolean updateEventIdsPersistent() {
        return updateEventIdsPersistent(this.getStatusFilename());
    }

    private boolean updateEventIdsPersistent( String filename) {
        boolean result = false;
        Properties properties = new Properties();
        properties.put(SCHEDULER_ID, schedulerIds.getEventId() != null ? schedulerIds.getEventId() : "" );
        properties.put(LATEST_TIMESTAMP, latestTimestamp != null ? latestTimestamp : "" );
        if (schedulerIds.isEmtpy()) {
        	notifierMfsEventManager.removeFile(this.getNotificationNumber(),filename);
        	log.debug("Removing empty .status file for: " + this.getIdentity());
        	result=true;
        } else
        {
        	log.debug("Storing schedulerIds values for " + this.getIdentity() + " in " + filename + "\n" + schedulerIds);
        	result = notifierMfsEventManager.storeProperties(this.getNotificationNumber(), filename, properties);
        }
        return result;
    }
    
    public void updateEventId(String nextId) {
    	if (schedulerIds.setEventId(nextId)) { //update if changed.
    		updateEventIdsPersistent();
    	} 

    }	

    public String getPayloadFilename() {
        return eventProperties.getProperty(NotifierConstants.MESSAGE_PAYLOAD_FILE_PROPERTY);
    }
    
    public boolean containsPayloadFile() {
        String payloadFilename = getPayloadFilename();
        if (payloadFilename == null || payloadFilename.isEmpty()){
            return false;
        } else {
            return true;
        }
    }

    public void setPayloadFilename(String payloadFilename) {
        eventProperties.setProperty(NotifierConstants.MESSAGE_PAYLOAD_FILE_PROPERTY, payloadFilename);
    }

    /**
     * Remove the payload file for a subscriber (or non subscriber) in the private/events directory.
     */
    public boolean removePayloadFile() {
        if (containsPayloadFile()){
            boolean isRemoved = notifierMfsEventManager.removeFile(getNotificationNumber(), getPayloadFilename());
            if (isRemoved) {
                log.debug("Removed the payload file for " + this.getIdentity() + " (if it existed)");
            } else {
                log.debug("Failed to remove the payload file for " + this.getIdentity());
            }
            return isRemoved;
        } else {
            log.debug("No payload file to remove");
            return true;
        }
    }



    private String getStatusFilename() {
        StringBuilder statusFilename = new StringBuilder();
        statusFilename.append(this.getNotifierTypeName());
        
        Date date = this.getDate();
        String formattedDate = dateFormat.get().format(date);
        statusFilename.append(NotifierEvent.SEPARATOR).append(formattedDate);
        
        if (this.getValidatedSender() != null && !this.getValidatedSender().isEmpty()) {
            statusFilename.append(NotifierEvent.SEPARATOR).append(this.getValidatedSender());
        }
        
        if (this.getNotifierTypeName() != null && !this.getNotifierTypeName().isEmpty()) {
        	statusFilename.append(NotifierEvent.SEPARATOR).append(this.getNotifierTypeName());
        }
        
        statusFilename.append(STATUS_FILE);
        return statusFilename.toString();
    }

    /**
     * Returns the NotifierType for this notification.
     * The NotifierType can be null if it was removed from the configuration
     * before this NotifierEvent was instantiated.
     * If NotifierEvent.notifierType is null, the notification should not be sent.
     * @return NotifierType for this notification
     */
    public TemplateType getNotifierType() {
        return notifierType;
    }
    
    public String getNotifierTypeName() {
        return eventProperties.getProperty(NotifierConstants.NOTIFIER_TYPE_NAME_PROPERTY);
    }
    
    public String getCphrtemplateName() {
        return eventProperties.getProperty(NotifierConstants.NOTIFIER_CPHR_TEMPLATE_NAME);
    }
    
    
    private Date getDate() {
        String dateInMilliseconds = eventProperties.getProperty(NotifierConstants.DATE_PROPERTY);
        String formattedDate = dateFormat.get().format(new Date(Long.parseLong(dateInMilliseconds)));
        log.debug("GetDate returns " + formattedDate + " (" + dateInMilliseconds + "ms)");
        return new Date(Long.parseLong(dateInMilliseconds));
    }

    public String getValidatedSender() {
        return validatedSender;
    }

    /**
     * This method verifies if there are missing mandatory key/value pairs in NotifierEvent.eventProperties and add them.
     * - sender: MIGHT have to consider the visibility of the sender (if applicable)
     * - visibility
     * - senderPhoneNumber (MUST consider the visibility of the sender)
     * - senderDisplayName (MUST consider the visibility of the sender)
     * - date
     * @throws NotifierMfsException if retrieving the information from the MFS fails. 
     */
    private void addMandatoryProperties() throws NotifierMfsException {
        String visibilityString = null;
        String senderPhoneNumber = null;
        String senderDisplayName = null;
        String dateString = null;

        visibilityString = validateVisibility(this.eventProperties.getProperty(NotifierConstants.SENDER_VISIBILITY_PROPERTY));
        senderPhoneNumber = this.eventProperties.getProperty(NotifierConstants.SENDER_PHONE_NUMBER_PROPERTY);
        senderDisplayName = this.eventProperties.getProperty(NotifierConstants.SENDER_DISPLAY_NAME_PROPERTY);
        validatedSender = validateSender(senderPhoneNumber, senderDisplayName, visibilityString);
        dateString = validateDate(this.eventProperties.getProperty(NotifierConstants.DATE_PROPERTY));
       

        if (senderPhoneNumber == null) {
            senderPhoneNumber = "";
        }
        if (senderDisplayName == null) {
            senderDisplayName = "";
        }
        eventProperties.setProperty(NotifierConstants.SENDER_PHONE_NUMBER_PROPERTY, senderPhoneNumber);
        eventProperties.setProperty(NotifierConstants.SENDER_DISPLAY_NAME_PROPERTY, senderDisplayName);
        eventProperties.setProperty(NotifierConstants.SENDER_VISIBILITY_PROPERTY, visibilityString);
        eventProperties.setProperty(NotifierConstants.DATE_PROPERTY, dateString);

        log.debug("addMandatoryProperties: " + NotifierConstants.SENDER_PHONE_NUMBER_PROPERTY + " property: " + ("".equals(senderPhoneNumber) ? "<none>":senderPhoneNumber) + " will be used");
        log.debug("addMandatoryProperties: " + NotifierConstants.SENDER_DISPLAY_NAME_PROPERTY + " property: " + ("".equals(senderDisplayName) ? "<none>":senderDisplayName) + " will be used");
        log.debug("addMandatoryProperties: " + NotifierConstants.SENDER_VISIBILITY_PROPERTY + " property: " + visibilityString + " will be used");
        log.debug("addMandatoryProperties: " + NotifierConstants.DATE_PROPERTY + " property: " + dateString + " will be used");
        log.debug("addMandatoryProperties: validatedSender set to: " + validatedSender);
        
    }

    private String validateVisibility(String visibility) {
        String isVisible = "false";
        if (visibility == null || "1".equals(visibility) || "true".equalsIgnoreCase(visibility)) {
            isVisible = "true";
        }
        return isVisible;
    }

    private String validateSender(String senderPhoneNumber, String senderDisplayName, String visibilityString) {
        return validateSender(senderPhoneNumber, senderDisplayName, Boolean.parseBoolean(visibilityString));
    }

    private String validateSender(String senderPhoneNumber, String senderDisplayName, boolean visibility) {
        String validatedSender = "unknown";

        if (visibility) {
            validatedSender = getSenderPhoneNumber(senderPhoneNumber);
        } 

        log.debug("validateSender from senderPhoneNumber: " + senderPhoneNumber +
                  ", visibility: " + visibility + " to: " + validatedSender);

        return validatedSender;
    }

    private String getSenderPhoneNumber(String senderPhoneNumber) {
        if (senderPhoneNumber == null || senderPhoneNumber.isEmpty()) {
            return "unknown";
        } else {
            return notifierUtil.getNormalizedTelephoneNumber(senderPhoneNumber); 
        }
    }

    private String validateDate(String dateString) {
        String formattedDate = null;
        String validatedDateString = dateString;

        if (dateString == null || dateString.isEmpty()) {
            Long dateMilliseconds = Calendar.getInstance().getTimeInMillis();
            formattedDate = dateFormat.get().format(new Date(dateMilliseconds));
            validatedDateString = Long.toString(dateMilliseconds);
        }
        log.debug(NotifierConstants.DATE_PROPERTY + " property set to " + validatedDateString + " (" + formattedDate + ")");
        return validatedDateString;
    }


    public String getReceiverNumber() {
        return eventProperties.getProperty(NotifierConstants.RECEIVER_PHONE_NUMBER_PROPERTY);
    }

    public String getNotificationNumber() {
    	if (this.notificationNumber == null)
    		this.notificationNumber = eventProperties.getProperty(NotifierConstants.NOTIFICATION_PHONE_NUMBER_PROPERTY);
    	
    	return this.notificationNumber;
    }

    public String getIdentity() {
        return getReceiverNumber() + ":" + getNotificationNumber();
    }

    public String getStatus() {
        return notifierTypeState.getName() + ":" + notifierTypeEvent.getName();
    }

    public NotifierTypeState getNotifierTypeState() {
        return notifierTypeState;
    }

    public void setNotifierTypeState(NotifierTypeState notifierTypeState) {
        this.notifierTypeState = notifierTypeState;
    }

    public void setNotifierTypeState() {
        String eventId = null;

        if (!this.getSchedulerIds().isEventIdEmpty()) {
            // EventId
            eventId = this.getSchedulerIds().getEventId();
        }  

        NotifierTypeState notifierTypeState = NotifierHelper.getNotifierTypeStateFromEventId(eventId);
        log.debug("NotifierEvent state has been set to " + notifierTypeState.getName());
        this.setNotifierTypeState(notifierTypeState);
    }

    public NotifierTypeEvent getNotifierTypeEvent() {
        return notifierTypeEvent;
    }
    
    public boolean isAlreadySending() {
    	
    	String inProgressFile = getNotifierType().getInProgressFilename();
    	int inProgress_file_validity = getNotifierType().getInProgressFileValidityTime();
    	String notifNumber = getNotificationNumber();
    	
    	NotifierFileStatusEnum status = notifierMfsEventManager.fileExistsValidation(notifNumber, inProgressFile , inProgress_file_validity);
    	switch (status) {
    	case FILE_DOES_NOT_EXIST:
    		return false;
    	case FILE_EXISTS_AND_VALID:
    		return true;
    	case FILE_EXISTS_AND_INVALID:
    		boolean isRemoved = notifierMfsEventManager.removeFile(getNotificationNumber(), inProgressFile);
    		if (isRemoved) {
    			log.warn("isAlreadySending: Removed invalid in Progress file (file timed out): " + inProgress_file_validity + " for notification number: " + notifNumber);
    			return false;
    		} else
    		{
    			if (!notifierUtil.isFileStorageOperationsAvailable(notifNumber)) {
	   				log.warn("isAlreadySending: File system is read only (geo fail over?) for notifNumber:" + notifNumber + " when trying to remove invalid state file ");
	   				return false;
	   			}
    			log.error("isAlreadySending: unable to remove invalid in Progress file (file timed out): " + inProgress_file_validity + " for notification number: " + notifNumber);
    			return false;
    		}
    		
    	case FILE_EXISTS_NO_VALIDATION:
    		return true;
    	case UNABLE_TO_DETERMINE_STATUS:
    		//assume that there is no status file, better to send multiple than miss one.
    		return false;
    	default:
    		log.warn("unknown value returned from removeFile, assuming not sending..");
    		return false;
    	}
    }

    public void setNotifierTypeEvent(NotifierTypeEvent notifierTypeEvent) {
        this.notifierTypeEvent = notifierTypeEvent;
    }

    public SchedulerIds getSchedulerIds() {
        return schedulerIds;
    }

    public ANotifierDatabaseSubscriberProfile getSubscriberProfile() {
        return subscriberProfile;
    }

    public void setSubscriberProfile(ANotifierDatabaseSubscriberProfile subProfile) {
        subscriberProfile = subProfile;
    }

    public String getLatestTimestamp() {
        return latestTimestamp;
    }

    public boolean indicateSending() {
	   	String inProgressFile = getNotifierType().getInProgressFilename();
	   	String notifNumber = getNotificationNumber();
		//this function will create the file with an empty props, there is no function to directly create a file at the moment in the framework;
		if (!isAlreadySending()) {
			boolean sending = notifierMfsEventManager.storeProperties(notifNumber, inProgressFile, new Properties());
			setAquiredSending(sending);
		    return acquiredSending;
		}
		else {
			setAquiredSending(false);
			return false;
		}
		
	}

	private void setAquiredSending(boolean sending) {
		acquiredSending=sending;
		eventProperties.setProperty(NotifierConstants.ACQUIRED_SENDING_FILE, String.valueOf(sending));
	}
	
	private boolean getAquiredSending() {
		if (acquiredSending == null) { //fetch from properties if not already down so.
			String ase = eventProperties.getProperty(NotifierConstants.ACQUIRED_SENDING_FILE);
			if (ase != null ) {
				acquiredSending = Boolean.parseBoolean(ase);
				return acquiredSending;
			} else {
				return false;
			}
		} else {
			return acquiredSending;
		}
	}

	public void setLatestTimestamp(String latestTimestamp) {
	    this.latestTimestamp = latestTimestamp;
	}

	public boolean clearSending() {
		if (getAquiredSending() == false ) { 
			//if we never created the file then don't delete it.
			return true; 
		}
		if (notifierType == null)
		{
			log.warn("Unknown Type, cannot clear sending file if exists.");
			return false;
		}
	   	String inProgressFile = notifierType .getInProgressFilename();
	   	String notifNumber = getNotificationNumber();
	    
	   	//public long acquireLockFile(String telephoneNumber, String lockFileName, int validityPeriodInSeconds);
	    	   		   	
	   	try {
	   		//try to acquire lock, but if failed continue anyway as I am the owner.  Small chance this could cause a double
	   		//notification, but since we are finished sending, unlikely this is an issue.
	   		if (!aquireSendingLock()) {
	   			if (!notifierUtil.isFileStorageOperationsAvailable(notifNumber)) {
	   				log.warn("ClearSending: File system is read only (geo fail over?) for notifNumber:" + notifNumber + " cannot delete: " + inProgressFile );
	   				return false;
	   			}
	   			log.debug("ClearSending: unable to acquire SendingLock, before removing sending file, will delete anyway as I am owner.");
	   		}
	   				
	   		NotifierFileStatusEnum status = notifierMfsEventManager.fileExistsValidation(notifNumber, inProgressFile , 0); 
	   		switch (status) {
	   		case FILE_DOES_NOT_EXIST:
	   			setAquiredSending(false); //indicate we cleared it.
	   			return true; //already deleted.
	   		case FILE_EXISTS_AND_VALID: //fall through
	   		case FILE_EXISTS_AND_INVALID: //fall through	
	   		case FILE_EXISTS_NO_VALIDATION: //fall through - normal case when 0 validity above..
	   		case UNABLE_TO_DETERMINE_STATUS: //fall through - exists but don't know if valid. don't care delete anyway..
	   			break; //try to delete it.
	   		default:
	   			log.error("ClearSending: Unknown status when checking if fileExists: " + status);
	   			return false; //error case...
	   		}
	   		//attempt to delete.
	   		boolean isRemoved = notifierMfsEventManager.removeFile(getNotificationNumber(), notifierType.getInProgressFilename());		
	   		if (!isRemoved) {
	   			log.error("ClearSending: unable to remove in Progress file " + inProgressFile + " for notification number: " + notifNumber);
	   		} else {
	   			setAquiredSending(false); //indicate we cleared it.
	   		}
	   		return isRemoved;
	   	} finally {
	   		releaseSendingLock(); //make sure to release the lock...
	   	}
	}

	public boolean aquireSendingLock() {

	   	String notifNumber = getNotificationNumber();
		if (!notifierUtil.isFileStorageOperationsAvailable(notifNumber)) {
			log.warn("aquireSendingLock: File system is read only (Geo Fail Over?) for notifNumber:" + notifNumber  );
			return false;
		}
		
	   	String inProgressLockFile=getNotifierType().getInProgressLockFileName();
	   
	   	int tries = 0;
	   	while (progressLockId == 0  && tries++ < MAX_ACQUIRE_LOCK_TRIES) {
				synchronized (accessExistsLock)  {
	   			progressLockId = notifierMfsEventManager.acquireLockFile(notifNumber,inProgressLockFile,MAX_ACQUIRE_LOCK_VALIDITY_SECONDS); //should not be locked long...
	   			if (progressLockId != 0)
	   				return true;
	   		}
	   		try {Thread.sleep(ACQUIER_LOCK_SLEEP_TIME_MS); } catch (InterruptedException e) { ; //ignore..}
	   		
			}
	   	}
	   	
	   	return false;
	}

	
	public boolean releaseSendingLock() {
		if (progressLockId == 0 )
			{return true;}

		String notifNumber = getNotificationNumber();
		if (!notifierUtil.isFileStorageOperationsAvailable(notifNumber)) {
			log.warn("releaseSendingLock: File system is read only for notifNumber:" + notifNumber  );
			return false;
		}
		
		String inProgressLockFile=getNotifierType().getInProgressLockFileName();
		synchronized (accessExistsLock)  {
			if (progressLockId == 0 ) //we did not lock
				{return false;}
			if (notifierMfsEventManager.releaseLockFile(notifNumber, inProgressLockFile,progressLockId)) {
				progressLockId=0;
				return true; //Successfully released lock
			}
			return false; //we were unable to release lock.
		}
	}

	
}
