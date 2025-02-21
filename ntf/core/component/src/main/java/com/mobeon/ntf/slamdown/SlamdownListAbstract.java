package com.mobeon.ntf.slamdown;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.NotifierPluginHandler;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.trafficeventsender.MfsClient;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.slamdown.event.SlamdownEvent;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.mcd.McdUserInfo;

/**
 * SlamdownList remembers information about slamdown information to one phone.
 */
public class SlamdownListAbstract {

    private static LogAgent log = NtfCmnLogger.getLogAgent(SlamdownListAbstract.class);

	private SlamdownFile _slamdownFile;
    private SchedulerIds schedulerIds = null;

	private String subscriberNumber = null;
	private String destinationNumber = null;
	private String notificationNumber = null;

    /**
     * Since so many slamdown lists are kept in memory, user information is
     * packed into a byte array as follows:<PRE>
     * byte    0        1       2      3        4    5..n<BR>
     *     |state|retry count|smsc|language|validity|cos|</PRE>
     */
    private byte[] _data = null;

    private UserInfo userInfo = null;
    protected int notificationType = SlamdownList.NOTIFICATION_TYPE_SLAMDOWN;
    private boolean internal = true;
    private int currentState = SlamdownList.STATE_SENDING_UNIT;
    private int currentEvent = SlamdownList.EVENT_NEW_NOTIF;
    private String filename;
    protected File[] fileList = null;
    private List<SlamdownInfo> infoList = null;
    private long phoneOnLockId = 0L;

    /** Index to the current state in the _data array. */
    private static final int STATE_IX = 0;
    /** Index to the retry count in the _data array. */
    private static final int RETRY_IX = 1;
    /** Index to the (number of the) preferred language in the _data array. */
    private static final int LANG_IX = 2;
    /** Index to the validity time in the _data array. */
    private static final int VALIDITY_IX = 3;
    /** Index to the (number of the) cosName in the _data array */
    private static final int COSNAME_IX = 4;
    /** The length of the array is the last index +1, in this case COSNAME_IX+1*/
    private static final int DATA_LENGTH=COSNAME_IX+1; 
    private static NumberedStrings _languages = new NumberedStrings();
    private static NumberedStrings _cosNames = new NumberedStrings();

	protected static IMfsEventManager _mfsEventManager;

    private static Object perf;

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
	public SlamdownListAbstract(String subscriberNumber, String notificationNumber, UserInfo userInfo, int validity, String cosName, boolean internal, int notificationType) {

        log.debug("SlamdownList(): subscriberNumber: " + subscriberNumber + " notificationNumber: " + notificationNumber
                + " validity: " + validity +  " cosName: " +cosName  + " internal: " + internal 
                + "notificationType: "  + notificationType);
        this.subscriberNumber = subscriberNumber;
        this.notificationNumber = notificationNumber;
        
        if (userInfo != null) {
            this.destinationNumber = userInfo.getTelephoneNumber();
        }

        _data = new byte[DATA_LENGTH];
        _data[STATE_IX] = 0;
        _data[RETRY_IX] = 0;
        _data[COSNAME_IX] = _cosNames.stringToNumber(cosName);
        if (validity > 255) {
            validity = 255;
        }
        _data[VALIDITY_IX] = (byte) (validity - 128);

        
        if (userInfo != null) {
            _data[LANG_IX] = _languages.stringToNumber(userInfo.getPreferredLanguage());
        }
        this.userInfo = userInfo;
        this.internal = internal;
        this.currentState = SlamdownList.STATE_SENDING_UNIT;
        this.currentEvent = SlamdownList.EVENT_NEW_NOTIF;
        this.notificationType = notificationType;
        this._slamdownFile = new SlamdownFile(notificationNumber);
        this.schedulerIds = new SchedulerIds();
    }

    public static void setMfsEventManager(IMfsEventManager manager){
    	_mfsEventManager = manager;
    }
    
    public void updateScheduledEventsIds(AppliEventInfo eventInfo) {
    	String nextEventId = null;

    	// Get the nextEventInfo value (can be null in case of the last retry on an expiry event)
    	if (eventInfo.getNextEventInfo() != null) {
    	    nextEventId = eventInfo.getNextEventInfo().getEventId();
    	}

    	// Use the current eventInfo to find out which event type it is (state that we are in).
        EventID eventId = null;
        try {
            if (eventInfo.getEventId() != null && eventInfo.getEventId().length() > 0) {
                eventId = new EventID(eventInfo.getEventId());

                if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.SLAMDOWN_SMS_UNIT.getName())){
                    this.schedulerIds.setSmsUnitEventId(nextEventId);
                } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.SLAMDOWN_SMS_TYPE_0.getName())){
                    this.schedulerIds.setSmsType0EventId(nextEventId);
                } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.SLAMDOWN_SMS_INFO.getName())){
                    this.schedulerIds.setSmsInfoEventId(nextEventId);
                } else {
                    log.error("Invalid eventId: " + eventInfo.getEventId());
                }
            }
        } catch (InvalidEventIDException iei) {
            log.error("Invalid eventId: " + eventInfo.getEventId(), iei);
        }
    }
    
    public String getSubscriberNumber() {
        return this.subscriberNumber;
    }

    public String getNotificationNumber() {
        return notificationNumber;
    }
    
    public UserInfo getUserInfo() {
        return this.userInfo;
    }
    
    public String getNumber() {
        return notificationNumber;
    }

    public Properties getEventProperties() {
        Properties props = new Properties();

        props.put(SlamdownEvent.RECIPIENT_ID, getSubscriberNumber());
        props.put(SlamdownEvent.NOTIFICATION_NUMBER, getNotificationNumber());
        props.put(SlamdownEvent.CURRENT_STATE, "" + currentState);
        props.put(SlamdownEvent.CURRENT_EVENT, "" + currentEvent);
        props.put(SlamdownEvent.INTERNAL, "" + internal);
        props.put(SlamdownEvent.NOTIFICATION_TYPE, "" + notificationType);
        
        return props;
    }    

    public String getOrigDestinationNumber() {
        if (destinationNumber!= null){
        	return destinationNumber;
        } else {
            return "";
        }
    }

    public int getCurrentState() {
        return this.currentState;
    }
    public int getCurrentEvent() {
        return this.currentEvent;
    }
    public boolean getInternal() {
        return internal;
    }
    public int getNotificationType() {
        return this.notificationType;
    }
    public String getFilename() {
        return this.filename;
    }
    public String getType() {
        if (internal) {
            if (notificationType == SlamdownList.NOTIFICATION_TYPE_MCN_INTERNAL) {
                return "Mcn";
            } else {
                return "Slamdown";
            }
        } else {
            return "Mcn";
        }
    }
    
    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public void setCurrentEvent(int currentEvent) {
        this.currentEvent = currentEvent;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }
    
    public String getPreferredLanguage() {
        return _languages.numberToString(_data[LANG_IX]);
    }
    
    public String getCosName() {
        return _cosNames.numberToString(_data[COSNAME_IX]);
    }

    public int getValidity() {
        return _data[VALIDITY_IX] + 128;
    }

    public void setPhoneOnLockId(long lockId) {
        this.phoneOnLockId = lockId;
    }

    public long getPhoneOnLockId() {
       return this.phoneOnLockId;
    }

    /**
     * If the slamdown information list has calls from a single caller
     * @return true if all calls are from single caller
     */
    public boolean isSingleCaller() {
    	boolean isSingleCaller = false;
    	Iterator<SlamdownInfo> iterator = _slamdownFile.getSlamdownInfoList().iterator();
		Set<String> set = new HashSet<String>();

		while (iterator.hasNext()) {
			String caller = iterator.next().getCaller();
			set.add(caller);
		}

		if(set.size() == 1){
			isSingleCaller = true;
		}

    	return isSingleCaller;
    }

    /**
     * Obtains the sorted caller list
     * @return Sorted CallerInfo
     */
    public CallerInfo[] sortCallers() {
    	CallerInfo[] tmpCallers = getCallers();
    	Arrays.sort(tmpCallers);
        /*
         * We need to limit the number of callers as per the retention 
         * policy. Note that we keep only the latest callers. So depending 
         * on the sort order, we take the callers at the beginning or 
         * the end of the sorted array.
         */
    	int retentionSize = 0;
    	if (notificationType == SlamdownList.NOTIFICATION_TYPE_SLAMDOWN) {
    	    retentionSize = Config.getSlamdownRetentionNbOfCallers();
    	} else if (notificationType == SlamdownList.NOTIFICATION_TYPE_MCN_INTERNAL ||
    	        notificationType == SlamdownList.NOTIFICATION_TYPE_MCN_EXTERNAL) {
    	    if (Config.isMcnSubscribedEnabled() && userInfo.hasMcnSubscribedService()) {
    	        retentionSize = Config.getMcnSubsRetentionNbOfCallers();
    	    } else {
    	        retentionSize = Config.getMcnRetentionNbOfCallers();
    	    }
    	} else {
    	    log.warn("Invalid notification type value " + notificationType + " for subscriber " +
    	            userInfo.getTelephoneNumber() + ". No Retention policy applied.");
    	}

    	if (retentionSize > 0 && tmpCallers.length > 1 && retentionSize < tmpCallers.length) {
    	    int startIdx = 0;
            // Verify in which order the callers are sorted
    	    if (tmpCallers[0].getCallSec() < tmpCallers[1].getCallSec()) {
    	        // The callers are sorted in ascending order. 
    	        // We will pick the callers at the end of the array
    	        startIdx = tmpCallers.length - retentionSize;
    	    }
    	    int endIdx = startIdx + retentionSize;
    	    tmpCallers = Arrays.copyOfRange(tmpCallers, startIdx, endIdx);
    	}
    	return tmpCallers;
    }

    public CallerInfo[] getCallers() {
    	Collection<CallerInfo> tmpCallerInfoList = getCallersInternal(); 
   	    CallerInfo[] callerInfoList = new CallerInfo[tmpCallerInfoList.size()];  
   	    callerInfoList = tmpCallerInfoList.toArray(new CallerInfo[0]);
   	    return callerInfoList;
    }

    private Collection<CallerInfo> getCallersInternal() {
		Map<String, CallerInfo> list = new TreeMap<String, CallerInfo>();
		
		Iterator<SlamdownInfo> iterator = _slamdownFile.getSlamdownInfoList().iterator();
		
		//For each caller 
		while (iterator.hasNext()) {
			//create a CallerInfo or increment the call count and update date
			SlamdownInfo slamInfo = iterator.next();
			String callerId = slamInfo.getCaller();
			CallerInfo info = list.get(callerId);
			if ( info == null ){
				info = CallerInfo.create(callerId, internal);
				info.setSlamdownInfoProperties(slamInfo.getProperties());
				list.put(callerId, info);
			}
			info.voiceSlamdown(slamInfo.getTimestamp());
		}

        Collection<CallerInfo> tmpCallerInfoList = list.values();
		return tmpCallerInfoList;
	}

    /**
     * Numbered Strings are a way to represent a string in one byte, when there are many instances of a string.
     * Basically a lookup table to lookup up a COS or languages for example as represented by a single byte.
     * These can only be used to represent a small number of strings 2^8 -126 -> +127. 
     * For example COS where there are usually in single digits for most customers, but can be represented in 100's
     * of instances of SL's.
     *
     * Do not use them for representing strings that have more than 256 or more individual strings.
     */
    private static class NumberedStrings {
        private Hashtable<String, Byte> numbers = new Hashtable<String, Byte>();
        private Vector<String> strings = new Vector<String>();

        public byte stringToNumber(String s) {
            Byte i = numbers.get(s);
            if (i == null) {
                synchronized (this) {
                    i = numbers.get(s);
                    if (i == null) {
                        i = new Byte((byte) numbers.size());
                        numbers.put(s, i);
                        strings.add(s);
                    }
                }
            }
            return i.byteValue();
        }

        public String numberToString(byte i) {
            return strings.elementAt(i);
        }
    }

    /**
     * This method recreates a SlamdownList object from persistent storage (if found)
     * @param notificationNumber notificationNumber
     * @return List of SlamdownList
     */
    public static SlamdownList[] recreateSlamdownList(String notificationNumber) {
        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("NTF.recreateSlamdownList");
            }

            ArrayList<SlamdownList> recreatedSlamdownList = new ArrayList<SlamdownList>(2);
            String subscriberNumber = null;

            if (_mfsEventManager == null) {
                _mfsEventManager = MfsEventFactory.getMfsEvenManager();
            }

            boolean internal = _mfsEventManager.isInternal(notificationNumber);
        
            /*
             * Reading slamdown and mcn files from event directory.
             */
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                boolean isAccepted = false;
                    /**
                     * Both PATTERN_FILE_PENDING and PATTERN_FILE_FINAL files must be considered since a retry could
                     * happen (example for SMS-Info) while the file as already been set to  PATTERN_FILE_FINAL.
                     */
                if(!NotifierPluginHandler.get().isHandlingNotificationEvent(NtfEventTypes.SLAMDOWN.getName(), new Properties())) {
                    isAccepted = file.getName().startsWith(MfsClient.EVENT_SLAMDOWNINFORMATION + SlamdownList.PATTERN_FILE_FINAL) ||
                            file.getName().startsWith(MfsClient.EVENT_SLAMDOWNINFORMATION + SlamdownList.PATTERN_FILE_PENDING) ||
                            file.getName().startsWith(MfsClient.EVENT_MISSEDCALLNOTIFICATION + SlamdownList.PATTERN_FILE_FINAL) ||
                            file.getName().startsWith(MfsClient.EVENT_MISSEDCALLNOTIFICATION + SlamdownList.PATTERN_FILE_PENDING);
                } else {
                    log.debug("Slamdown files will not be processed because a Notifier plugin is configured to handle slamdown.");
                    isAccepted = file.getName().startsWith(MfsClient.EVENT_MISSEDCALLNOTIFICATION + SlamdownList.PATTERN_FILE_FINAL) ||
                            file.getName().startsWith(MfsClient.EVENT_MISSEDCALLNOTIFICATION + SlamdownList.PATTERN_FILE_PENDING);
                }

                return isAccepted;
                }
            };
            
            File[] allFiles = _mfsEventManager.getEventFiles(notificationNumber, filter);
            if (allFiles == null || allFiles.length == 0) {
                return null;
            }

            // Separate slamdown and mcn files in two lists.
            ArrayList<File> slamdownFiles = new ArrayList<File>();
            ArrayList<File> mcnFiles = new ArrayList<File>();

            for (File file : allFiles) {
                log.debug(notificationNumber + " (" + internal + ") " + file.getName() + " found.");
                if (file.getName().startsWith(MfsClient.EVENT_SLAMDOWNINFORMATION)) {
                    slamdownFiles.add(file);
                } else {
                    mcnFiles.add(file);
                }
            }

            /**
             * Lookup in MFS
             * 
             * This mfsEventManager.getEventFiles() method encapsulates all the cases of
             * generating the MFS path depending if the phoneNumber:
             *   - exists in MCD (which will lead to a /opt/mfs/internal/<msid>/private/moip/<telephonenumber>/events/ directory 
             *   - is not found in MCD (which will return a /opt/mfs/external/<eid>/private/moip/<telephonenumber>/events/ directory
             * ==> MIO 5.0 MIO5_MFS
             *   - exists in MCD (which will lead to a /opt/mfs/internal/<msid>/private_moip_<telephonenumber>_events/ directory 
             *   - is not found in MCD (which will return a /opt/mfs/external/<eid>/private_moip_<telephonenumber>_events/ directory
             * ==> MIO Using MFS DB (represented as File paths, for convenience only)  
             *   - exists in MCD (msid:<msid>/moip/<telephonenumber>/events/)
             *   - is not found in MCD (eid:<eid>/moip/<telephonenumber>/events/)
             *   
             * No need to keep track of all the final files ("slamdowninformation_*") under the given directory since
             * the SlamdownList class will take them all at SlamdownInfo creation time.
             */
            // Slamdown case
            if (slamdownFiles.size() > 0) {
                // Take the subscriber number from the first file found (since they all contain the same subscriber number)
                subscriberNumber = getSubscriberNumberFromFile(slamdownFiles.get(0));
                if (subscriberNumber == null) {
                    log.warn("Cannot determine subscriber number from " + slamdownFiles.get(0).getPath() + 
                            " - File seems to be corrupted. Skipping Slamdown notification.");
                }
            }

            // MissedCall notification (MCN) case
            if (mcnFiles.size() > 0) {
                // Take the subscriber number from the first MCN file found (only if not already known)
                if (subscriberNumber == null) {
                    subscriberNumber = getSubscriberNumberFromFile(mcnFiles.get(0));
                    if (subscriberNumber == null) {
                        log.warn("Cannot determine subscriber (or non-subcriber) number from " + mcnFiles.get(0).getPath() + 
                                " - File seems to be corrupted. Skipping MCN notification.");
                    }
                }
            }

            if (slamdownFiles.size() == 0 && mcnFiles.size() == 0) {
                log.debug("No Slamdown/MCN file found for notification number " + notificationNumber);
                return null;
            }

            // Recreate the SlamdownList objects
            if (subscriberNumber != null) {
                UserInfo userInfo = UserFactory.findUserByTelephoneNumber(subscriberNumber);
                String cosName = null;
                int validityPeriod;

                if (slamdownFiles.size() > 0) {
                    // Slamdown case
                    int notificationType = SlamdownList.NOTIFICATION_TYPE_SLAMDOWN;
                    if (userInfo != null) {
                        validityPeriod = userInfo.getValidity_slamdown(); 
                        cosName = userInfo.getCosName();
                    } else {
                        /**
                         * Either the subscriber can not be found because of a temporary error on MCD side
                         * or the pending slamdown file(s) belong to a no-longer-subscriber
                         * (for example: PhoneOn response received after the subscriber has been de-provisionned).
                         * In both cases, the MCN template will be used.
                         */
                        log.debug("Slamdown file(s) found for subscriber " + subscriberNumber + " (either temporarily not found subscriber or de-provisionned, will use MCN template.");
                        notificationType = SlamdownList.NOTIFICATION_TYPE_MCN_EXTERNAL;
                        userInfo = new McdUserInfo(notificationNumber, Config.getMcnLanguage());
                        cosName = Constants.DUMMY_MCN_COS;
                        validityPeriod = Config.getValidity_mcn();
                    }

                    SlamdownList sl = new SlamdownList(subscriberNumber, notificationNumber, userInfo, validityPeriod, cosName, internal, notificationType);
                    sl.setFileList(slamdownFiles.toArray(new File[slamdownFiles.size()]));
                    recreatedSlamdownList.add(sl);
                }

                if (mcnFiles.size() > 0) {
                    // MCN case
                    if (userInfo != null) {
                        // MIO subscriber
                        cosName = userInfo.getCosName();
                    } else {
                        // !MIO subscriber or MIO subscriber that has not been found (temporary error)
                        userInfo = new McdUserInfo(notificationNumber, Config.getMcnLanguage());
                        cosName = Constants.DUMMY_MCN_COS;
                    }
                    validityPeriod = Config.getValidity_mcn();
                    int notificationType = internal ? SlamdownList.NOTIFICATION_TYPE_MCN_INTERNAL : SlamdownList.NOTIFICATION_TYPE_MCN_EXTERNAL;

                    SlamdownList sl = new SlamdownList(subscriberNumber, notificationNumber, userInfo, validityPeriod, cosName, internal, notificationType);
                    sl.setFileList(mcnFiles.toArray(new File[mcnFiles.size()]));
                    recreatedSlamdownList.add(sl);
                }
            } else {
                log.warn("No subscriber (or non-subscriber) number found for notification number " + notificationNumber);
                return null;
            }

            return recreatedSlamdownList.toArray(new SlamdownList[recreatedSlamdownList.size()]);
        } finally {
            if (perf != null) {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
        }
    }

    public void renameSlamdownMcnFilesAsHandled() {
        if (fileList != null) {
            String pattern = null;
            if (notificationType == SlamdownList.NOTIFICATION_TYPE_SLAMDOWN) {
                // Slamdown case
                pattern = MfsClient.EVENT_SLAMDOWNINFORMATION;
            } else {
                // Missed call notification case
                pattern = MfsClient.EVENT_MISSEDCALLNOTIFICATION;
            }

            for (int i = 0; i < fileList.length; ++i) {
                String renamedSlamdownFileName = null;
                try {
                    int indexOfDate = fileList[i].getName().indexOf(pattern) + pattern.length();

                    if (fileList[i].getName().substring(indexOfDate).startsWith(SlamdownList.PATTERN_FILE_FINAL)) {
                        // Skip this file as it already contains the PATTERN_FILE_FINAL (possible in case of SMS-Info retry)
                        continue;
                    }

                    String date = fileList[i].getName().substring(indexOfDate + 1);
                    renamedSlamdownFileName = fileList[i].getName().substring(0,indexOfDate) + SlamdownList.PATTERN_FILE_FINAL + "_" + date;
                    File renamedSlamdownFile = new File(fileList[i].getParentFile(), renamedSlamdownFileName);
                    if (!_mfsEventManager.renameFile(fileList[i], renamedSlamdownFile)) {
                        log.error("renameSlamdownMcnFilesAsHandled unable to rename Slamdown file as final.");
                    } else {
                        fileList[i] = renamedSlamdownFile;
                    }
                } catch (Throwable t) {
                    log.error("Exception while renaming " + renamedSlamdownFileName + " file for " + this.subscriberNumber + " : " + this.notificationNumber, t);
                }
            }
        }
    }

    private static String getSubscriberNumberFromFile(File file) {

        String subscriberNumber = null;
        BufferedReader bufReader = null;

        try {
        	Reader reader = _mfsEventManager.retrieveEventsAsReader(file);
        	bufReader = new BufferedReader(reader);
        	String firstLine = bufReader.readLine();
        	if (firstLine != null) {
        		// Slamdown and MCN tags are the same (this method can handle both scenarios) event by using Slamdown tags.
        		int startingIndex = firstLine.indexOf(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY + MfsEventManager.KEY_VALUE_SEPARATOR);
        		startingIndex += (MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY + MfsEventManager.KEY_VALUE_SEPARATOR).length();
        		int endingIndex = firstLine.indexOf(MfsEventManager.PROPERTY_SEPARATOR, startingIndex);
        		subscriberNumber = firstLine.substring(startingIndex, (endingIndex < 0 ? firstLine.length() : endingIndex));
        		log.debug("SubscriberNumber " + subscriberNumber + " found in " + file.getPath() + " file.");
        	}
        } catch (IOException e) {
            log.error("Cannot read file buffer " + file.getPath());
        } catch (TrafficEventSenderException e) {
        	log.error("Cannot access event " + file.getPath() + ". " + e.getMessage());
		} finally {
            try {
                if (bufReader != null) {
                    bufReader.close();
                }
            } catch (IOException e) {}
        }
        return subscriberNumber;
    }
    
    /**
     * Retrieves the persistent scheduler-id values from the subscriber (or non subscriber) private/events directory.
     * Values are stored in the SchedulerIds private member.
     */
    public void retrieveSchedulerEventIdsPersistent() {

        if (_mfsEventManager == null) {
            _mfsEventManager = MfsEventFactory.getMfsEvenManager();
        }

        Properties properties = _mfsEventManager.getProperties(this.getNotificationNumber(), SlamdownList.SLAMDOWNMCN_STATUS_FILE);
        if (properties != null) {
            schedulerIds.setSmsUnitEventId(properties.getProperty(SchedulerIds.SMS_UNIT_EVENT_ID));
            schedulerIds.setSmsType0EventId(properties.getProperty(SchedulerIds.SMS_TYPE0_EVENT_ID));
            schedulerIds.setSmsInfoEventId(properties.getProperty(SchedulerIds.SMS_INFO_EVENT_ID));
            log.debug("retrieveSchedulerEventIdsPersistent: Read the " + SlamdownList.SLAMDOWNMCN_STATUS_FILE + " file for " + this.getNotificationNumber() + " and retrieved " + schedulerIds);
            
        }
    }

    /**
     * Update the scheduler-id values for a subscriber (or non subscriber) in the private/events directory.
     * Values stored are taken from the SchedulerIds private member.
     * @return boolean True if the update is successful, false otherwise
     */
    public boolean updateEventIdsPersistent() {
        boolean result = true;

        if (_mfsEventManager == null) {
            _mfsEventManager = MfsEventFactory.getMfsEvenManager();
        }

        try {
            if (schedulerIds.isEmtpy()) {
                _mfsEventManager.removeFile(this.getNotificationNumber(), SlamdownList.SLAMDOWNMCN_STATUS_FILE);
                log.debug("updateEventIdsPersistent: Removed the " + SlamdownList.SLAMDOWNMCN_STATUS_FILE + " file for " + this.getNotificationNumber() + " (if it existed)");
            } else {
                Properties properties = new Properties();
                properties.setProperty(SchedulerIds.SMS_UNIT_EVENT_ID, schedulerIds.getSmsUnitEventId() != null ? schedulerIds.getSmsUnitEventId() : "" );
                properties.setProperty(SchedulerIds.SMS_TYPE0_EVENT_ID, schedulerIds.getSmsType0EventId() != null ? schedulerIds.getSmsType0EventId() : "" );
                properties.setProperty(SchedulerIds.SMS_INFO_EVENT_ID, schedulerIds.getSmsInfoEventId() != null ? schedulerIds.getSmsInfoEventId() : "" );

                log.debug("updateEventIdsPersistent: Storing the new schedulerIds values for " + this.getNotificationNumber() + "\n" + schedulerIds);
                _mfsEventManager.storeProperties(this.getNotificationNumber(), SlamdownList.SLAMDOWNMCN_STATUS_FILE, properties);
            }
        } catch (TrafficEventSenderException tese) {
            log.error("Exception while SlamdownList.updateEventIdsPersistent", tese);
            result = false;
        }

        return result;
    }

    /**
     * Removes the SLAMDOWNMCN_STATUS_FILE for the given notification number. 
     * @param notificationNumber NotificationNumber
     */
    public static void removeSchedulerIdsPersistent(String notificationNumber) {
        if (_mfsEventManager == null) {
            _mfsEventManager = MfsEventFactory.getMfsEvenManager();
        }

        try {
            _mfsEventManager.removeFile(notificationNumber, SlamdownList.SLAMDOWNMCN_STATUS_FILE);
            log.debug("removeSchedulerIdsPersistent: Removed the " + SlamdownList.SLAMDOWNMCN_STATUS_FILE + " file for " + notificationNumber + " (if it existed)");
        } catch (TrafficEventSenderException tese) {
            log.error("Exception while SlamdownList.removeSchedulerIdsPersistent", tese);
        }
    }

    /**
     * This class represents the whole slamdown file (list of all the SlamdownInfo).
     * In fact, there is no slamdown file anymore since at runtime, this class's methods take
     * the list of all the slamdown final files under the given directory. 
     */
    private class SlamdownFile {
        private String phoneNumber;

        SlamdownFile(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        /**
         * This method returns a list of all the line (SlamdownInfo) contained in a slamdown file.
         * Since multiple slamdown files can be found, it loops through all the slamdown files. 
         */
        public List<SlamdownInfo> getSlamdownInfoList() {

            if (infoList != null) {
                // When the list has already been computed simply return it.
                log.debug("SlamdownInfoList already parsed for number " + phoneNumber);
                return infoList;
            }

            infoList = new ArrayList<SlamdownInfo>();
            if(_mfsEventManager == null){
                _mfsEventManager = MfsEventFactory.getMfsEvenManager();
            }

            String[] fileNames = null;
            int retentionDuration = 0;
            if (notificationType == SlamdownList.NOTIFICATION_TYPE_SLAMDOWN) {
                retentionDuration = Config.getSlamdownRetentionDuration();
                fileNames = getFileNames(retentionDuration);
            } else {
                if (Config.isMcnSubscribedEnabled() && userInfo.hasMcnSubscribedService()) {
                    // MCN Subscribed feature is enabled
                    retentionDuration = Config.getMcnSubsRetentionDuration();
                    fileNames = getFileNames(Config.getMcnSubsRetentionDuration());
                } else {
                    retentionDuration = Config.getMcnRetentionDuration();
                    fileNames = getFileNames(Config.getMcnRetentionDuration());
                }
            }

            // Loop through all the slamdown files
            for (String fileName : fileNames) {
                TrafficEvent[] events;
                try {
                    events = _mfsEventManager.retrieveEvents(phoneNumber, fileName);
                    Date date = null;

                    /**
                     * From the retrievedEvents list, the first event line must be skipped since it contains the subscriber number
                     * and the aggregation eventId (used by MAS only).
                     */
                    for (int i=1; i<events.length; i++) {
                        try {
                            long time = new Long(events[i].getProperties().get(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY)).longValue();
                            if (isEventValid(time, retentionDuration)) {
                                date = new Date(time);
                            } else {
                                log.debug("Date of event (" + new Date(time) + ") in Slamdown/MCN file " + fileName + " for " + phoneNumber +
                                        " is older than the configured duration retention (" + retentionDuration + " minute(s)), this event will be skipped.");
                                continue;
                            }
                        } catch (NumberFormatException nfe) {
                            log.error("Date of event in Slamdown/MCN file " + fileName + " for " + phoneNumber + " cannot be determined, this event will be skipped.");
                            continue;
                        }

                        String caller = events[i].getProperties().get(MoipMessageEntities.SLAMDOWN_CALLING_NUMBER_PROPERTY);
                        if (caller == null) {
                            log.error("Caller of event in Slamdown/MCN file " + fileName + " for " + phoneNumber + " cannot be read, this event will be skipped.");
                            continue;
                        }

                        SlamdownInfo info = new SlamdownInfo(date, caller);
                        info.setProperties(events[i].getProperties());
                        infoList.add(info);
                    }
                } catch (TrafficEventSenderException e1) {
                    log.debug("Slamdown file cannot be read: " + e1.getMessage());
                }
            }

            return infoList;
        }

        /**
         * Returns a list of event files based on a date retention policy and the current
         * notification type. 
         * @param retentionDuration Retention duration in minutes.
         * @return Array of file names.
         */
        private String[] getFileNames(int retentionDuration) {
            ArrayList<String> fileNames = new ArrayList<String>(); 
            File[] fileList = getFileList();
            if (fileList != null) {
                for (File file : fileList ) {
                	try {
						if (isEventValid(_mfsEventManager.getLastModified(file), retentionDuration)) {
							fileNames.add(file.getName());
						}
					} catch (TrafficEventSenderException e) {
						// Continue with next file.
					}
                }
            }
            return fileNames.toArray(new String[fileNames.size()]);
        }

        /**
         * Returns if the given date is still valid based on the retentionDuration
         * @param date Date of the object in milliseconds (file or event)
         * @param retentionDuration Configuration value in minutes
         * @return boolean
         */
        private boolean isEventValid(long date, int retentionDuration) {
            long limitDate = 0;
            boolean result = false;

            if (retentionDuration > 0) {
                limitDate = new Date().getTime() - 60000L * retentionDuration;
            }

            if (date > limitDate) {
                result = true;
            }
            return result;
        }
    }



    
    /**
     * This class represents the information for one line of the SlamdownFile.
     */
    private class SlamdownInfo {
        private Date timestamp;
        private String caller;
        private Map<String, String> properties = null;

        public SlamdownInfo(Date timestamp, String caller){
            this.timestamp = timestamp;
            this.caller = caller;
        }

        public Date getTimestamp(){
            return timestamp;
        }

        public String getCaller(){
            return caller;
        }
        
        public void setProperties(Map<String, String> slamdownInfoProperties) {
            this.properties = slamdownInfoProperties;
        }
        
        public Map<String, String> getProperties() {
            return properties;
        }
    }

    public SchedulerIds getSchedulerIds() {
        return this.schedulerIds;
    }
    
    /**
     * Sets the file list for this slamdown list.
     * @param files File list.
     */
    public void setFileList(File[] files) {
        fileList = files;
    }
    
    /**
     * Returns the list of files for this slamdown list.
     * @return Event files. Returns null if there is no event files.
     */
    public File[] getFileList() {
        if (fileList == null) {
            FileFilter filter = null;
            if (notificationType == SlamdownList.NOTIFICATION_TYPE_SLAMDOWN) {
                filter = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        /**
                         * Both PATTERN_FILE_PENDING and PATTERN_FILE_FINAL files must be considered since a retry could
                         * happen (example for SMS-Info) while the file as already been set to  PATTERN_FILE_FINAL.
                         */
                        return file.getName().startsWith(MfsClient.EVENT_SLAMDOWNINFORMATION + SlamdownList.PATTERN_FILE_FINAL) ||
                               file.getName().startsWith(MfsClient.EVENT_SLAMDOWNINFORMATION + SlamdownList.PATTERN_FILE_PENDING);
                    }
                };
            } else {
                filter = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        /**
                         * Both PATTERN_FILE_PENDING and PATTERN_FILE_FINAL files must be considered since a retry could
                         * happen (example for SMS-Info) while the file as already been set to  PATTERN_FILE_FINAL.
                         */
                        return file.getName().startsWith(MfsClient.EVENT_MISSEDCALLNOTIFICATION + SlamdownList.PATTERN_FILE_FINAL) ||
                               file.getName().startsWith(MfsClient.EVENT_MISSEDCALLNOTIFICATION + SlamdownList.PATTERN_FILE_PENDING);
                    }
                };
            }
            if (_mfsEventManager == null) {
                _mfsEventManager = MfsEventFactory.getMfsEvenManager();
            }
            fileList = _mfsEventManager.getEventFiles(getNotificationNumber(), filter);
        }
        return fileList;
    }
}
