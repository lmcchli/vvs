package com.mobeon.common.trafficeventsender.mfs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.client.BasicMfs;
import com.abcxyz.messaging.mfs.client.PrivateEntryDB;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.abcxyz.services.moip.userinfo.UserFactory;
import com.abcxyz.services.moip.userinfo.UserInfo;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.mfs.NtfNotifierEventHandler.RenameFileResult;
import com.mobeon.common.trafficeventsender.mfs.NtfNotifierEventHandler.RenameFileResultType;

class EventManagerWorkerNoSQL extends EventManagerWorker {

    private static BasicMfs mfsClient = BasicMfs.getInstance(CommonOamManager.getInstance().getMfsOam());
    private static final ILogger logger = ILoggerFactory.getILogger(EventManagerWorkerNoSQL.class);

    private NtfNotifierEventHandlerNoSQL slamdownNotifierNoSQL;
    
    private static FileFilter slamdownFilesFilter = new FileFilter()
    {
    	public boolean accept (File file) {
    		if (file.getName().startsWith("slamdowninformation_")) {
    			return true;
    		}
    		return false;
    	}   	
    };

    private MfsEventManager mfsEventManager = null;

    EventManagerWorkerNoSQL(String threadName, NtfNotifierEventHandlerNoSQL slamdownNotifierNoSQL) {
        super(threadName, null);
        this.slamdownNotifierNoSQL = slamdownNotifierNoSQL;
        mfsEventManager = MfsEventFactory.getMfsEvenManager();
    }


    protected List<BufferInfo> storeEventPerUser(IncomingEventEntry entry) {
        Object perf = null;
        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("BKD.CMA.TrES.EMW.storeEventPerUser");
            }

            logger.debug(this.getName() + " is processing " + entry.getEvent().getName() + " for " + entry.getEvent().getProperties());

            TrafficEvent trafficEvent = entry.getEvent();

            // Find the user's notification numbers
            UserInfo userInfo = UserFactory.findUserByTelephoneNumber(entry.getPhoneNumber());
            if (userInfo == null) {
                logger.warn("User " + entry.getPhoneNumber() + " not found");
                return null;
            }
            String[] notificationNumbers = userInfo.getFilter().getNotifNumbers("SMS", 0, entry.getPhoneNumber());
            int myMaxSlamdownInfoFiles = userInfo.getMaxSlamdownInfoFiles();

            /**
             * Because of a limitation on the SMS-Type-0 response (by notification number) and
             * on MCD side (no lookup by notification possible), the slamdown notifications are stored
             * per notification numbers (as the key), not subscriber number.
             */
            if (notificationNumbers != null && notificationNumbers.length > 0) {

                List<BufferInfo> bufferInfoList = new ArrayList<BufferInfo>();

                for (int i = 0; i < notificationNumbers.length; i++) {
                    String notificationNumber = notificationNumbers[i];			        
                    /**
                     * The <eventFileName> can be stored in either of the following paths: 
                     *    - /opt/mfs/internal/<msid>/private/moip/<notification number>/events/slamdowninformation
                     *    - /opt/mfs/external/<eid>/private/moip/<notification number>/events/slamdowninformation
                     * ==> MIO 5.0 MIO5_MFS   
                     *    - /opt/mfs/internal/<msid>/private_moip_<notification number>_events/slamdowninformation
                     *    - /opt/mfs/external/<eid>/private_moip_<notification number>_events/slamdowninformation
                     * ==> MIO Using MFS DB (represented as File paths, for convenience only)  
                     *   - exists in MCD (msid:<msid>/moip/<telephonenumber>/events/)
                     *   - is not found in MCD (eid:<eid>/moip/<telephonenumber>/events/)
                     *    
                     */

                    // Fill the buffer with the properties.
                    // BufferInfo bufferInfo = new BufferInfo(new StringBuilder(), entry.getPhoneNumber(), notificationNumber, eventFilePath);
                    BufferInfo bufferInfo = new BufferInfo(new StringBuilder(), entry.getPhoneNumber(), notificationNumber, trafficEvent.getName(), myMaxSlamdownInfoFiles);
                    Map<String, String> propertyMap = trafficEvent.getProperties();
                    Set<Map.Entry<String, String>> entries = propertyMap.entrySet();
                    for (Map.Entry<String, String> property : entries) {
                        bufferInfo.buffer.append(property.getKey() + MfsEventManager.KEY_VALUE_SEPARATOR + property.getValue() + MfsEventManager.PROPERTY_SEPARATOR);
                    }
                    bufferInfo.endCallLine();
                    bufferInfoList.add(bufferInfo);
                }
                return bufferInfoList;

            } else {
                logger.debug("No notification number found for " + entry.getPhoneNumber() + ", no notification will be generated.");
                return null;
            }
        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }
    }

    protected void storeEventsToMfs(List<BufferInfo> bufferInfoList){
        Object perf = null;
        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("BKD.CMA.TrES.EMW.storeEventsToMfs");
            }

            if (bufferInfoList == null || bufferInfoList.isEmpty()) {
                logger.debug("bufferInfo is empty, no Slamdown event to store.");
                return;
            }

            for (BufferInfo bufferInfo : bufferInfoList) {

                String notificationNumber = bufferInfo.notificationNumber;
                MSA msa = MfsEventManagerNoSQL.getMSA(notificationNumber);
                //boolean fileExists = mfsClient.privateFileExists(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber, MfsEventManager.EVENTS_DIRECTORY, bufferInfo.eventFilePath);
                String target = msa.toString() + " in private MOIP events for number " + notificationNumber + " with name " + bufferInfo.eventFilePath;		    	
                ByteArrayInputStream is = null;
                try {
                    is = mfsClient.getPrivateFileAsInputStream(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber, MfsEventManager.EVENTS_DIRECTORY, bufferInfo.eventFilePath);
                } catch (Exception e1) {
                    logger.error("Failed to retrieve data from " + target);
                    return;
                }

                int originalCallCount = 0;
                if (is != null) {
                    originalCallCount = countEvents(new InputStreamReader(is), target);
                }
                int bufferCallCount = bufferInfo.numberOfCalls;
                String calledNumber = bufferInfo.calledNumber;

                try {
                    String buffer = bufferInfo.buffer.toString();
                    boolean mustNotify = originalCallCount + bufferCallCount >= eventThreshold;
                    String storedEventId = null;

                    if (!mustNotify) {
                        if (is != null) {
                            logger.debug("Store Slamdown event for " + calledNumber + ", must not notify and file already exists, append.");

                            // Append the new call line(s) to the existing file
                            mfsClient.appendToPrivate(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber, MfsEventManager.EVENTS_DIRECTORY, bufferInfo.eventFilePath, buffer);

                            if(logger.isDebugEnabled()){
                                logger.debug("Event added in file: " + buffer);
                            }
                        } else {
                            logger.debug("Store Slamdown event for " + calledNumber + ", must not notify and file does not exist, create new file.");
                            
                            File[] slamdownFiles = mfsEventManager.getEventFiles(notificationNumber, slamdownFilesFilter);
                            if (null != slamdownFiles) {
                            	logger.debug("storeEventsToMfs::Got a list of slamdowninformation_ files; found " + slamdownFiles.length + " files.");
                            	if (slamdownFiles.length >= bufferInfo.maxSlamdownInfoFiles) {
                            		logger.debug("storeEventsToMfs::At least " + bufferInfo.maxSlamdownInfoFiles + " slamdowninformation_ files found; dropping this new one");
                            		return;
                            	}
                            }

                            /**
                             * When receiving the first Slamdown/Mcn call for a subscriber/non-subscriber, a timeout for notifying NTF
                             * must be started (except if the count has already reached the threshold, no aggregation timer is needed). 
                             */
                            Properties properties = new Properties();
                            properties.setProperty(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY, calledNumber);
                            properties.setProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY, PrivateEntryDB.buildFilename(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber, MfsEventManager.EVENTS_DIRECTORY, bufferInfo.eventFilePath));
                            properties.setProperty(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY, notificationNumber);

                            AppliEventInfo eventInfo = slamdownNotifierNoSQL.scheduleEvent(calculateWhen(), generateUniqueId(calledNumber), EventTypes.INTERNAL_TIMER.getName(), properties);
                            logger.debug("Scheduled event: " + eventInfo.getEventId());

                            // Append the subscriber/non-subscriber number
                            StringBuffer stringBuffer = new StringBuffer();
                            stringBuffer.append(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY + MfsEventManager.KEY_VALUE_SEPARATOR + calledNumber + MfsEventManager.PROPERTY_SEPARATOR);

                            // Append the eventId
                            stringBuffer.append(MoipMessageEntities.SLAMDOWN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR + eventInfo.getEventId() + '\n');

                            // Append the calls
                            stringBuffer.append(buffer);

                            mfsClient.addPrivate(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber, MfsEventManager.EVENTS_DIRECTORY, bufferInfo.eventFilePath, stringBuffer.toString());

                            if(logger.isDebugEnabled()){
                                logger.debug("Event added in file: " + stringBuffer.toString());
                            }
                        }
                    } else {
                        if (is != null) {
                            logger.debug("Store Slamdown event for " + calledNumber + ", must notify and file already exists, cancel eventId and notify.");

                            // Read the eventId stored in the existing file and cancel it
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                            String firstLine = reader.readLine();
                            reader.close();
                            if (firstLine != null) {
                                int indexOf = firstLine.indexOf(MoipMessageEntities.SLAMDOWN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR);
                                storedEventId = firstLine.substring(indexOf + (MoipMessageEntities.SLAMDOWN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR).length());
                            }

                            mfsClient.appendToPrivate(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber, MfsEventManager.EVENTS_DIRECTORY, bufferInfo.eventFilePath, buffer);

                            if(logger.isDebugEnabled()){
                                logger.debug("Event added in file: " + buffer);
                            }

                        } else {
                            logger.debug("Store Slamdown event for " + calledNumber + ", must notify and file does not exist, notify.");

                            File[] slamdownFiles = mfsEventManager.getEventFiles(notificationNumber, slamdownFilesFilter);
                            if (null != slamdownFiles) {
                            	logger.debug("storeEventsToMfs::Got a list of slamdowninformation_ files; found " + slamdownFiles.length + " files.");
                            	if (slamdownFiles.length >= bufferInfo.maxSlamdownInfoFiles) {
                            		logger.debug("storeEventsToMfs::At least " + bufferInfo.maxSlamdownInfoFiles + " slamdowninformation_ files found; dropping this new one");
                            		return;
                            	}
                            }
                            
                            // Create new file
                            StringBuffer stringBuffer = new StringBuffer();

                            // Adding the subscriber/non-subscriber number
                            stringBuffer.append(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY + MfsEventManager.KEY_VALUE_SEPARATOR + calledNumber + MfsEventManager.PROPERTY_SEPARATOR);

                            // Adding an empty eventId
                            stringBuffer.append(MoipMessageEntities.SLAMDOWN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR + '\n');

                            // Adding the calls
                            stringBuffer.append(buffer);

                            mfsClient.addPrivate(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber, MfsEventManager.EVENTS_DIRECTORY, bufferInfo.eventFilePath, stringBuffer.toString());

                            if(logger.isDebugEnabled()){
                                logger.debug("Event added in file: " + stringBuffer.toString());
                            }
                        }

                        /**
                         * When the event threshold has been reached, the event file is renamed with a time stamp
                         * and NTF is notified that an event file is ready with the renamed file.
                         * If the renaming of the file is successful, notify NTF, otherwise wait for the Scheduler to retry the renaming.
                         * No reason to notify NTF without a stamped file since NTF will not look at it.
                         * Even if the file is stored under notification number, NTF still gets notified with subscriber number.
                         */
                        notifyNtf(msa, storedEventId, calledNumber, notificationNumber, bufferInfo.eventFilePath);
                    }

                } catch (IOException ioe) {
                    logger.warn("IOException for " + calledNumber, ioe);
                } catch (Exception e) {
                    logger.warn("Exception for " + calledNumber, e);
                }
            }
        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }
    }


    private void notifyNtf(MSA msa, String storedEventId, String subscriberNumber, String notificationNumber, String filename) {
        Object perf = null;
        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("BKD.CMA.TrES.EMW.notifyNtf");
            }

            /**
             * If the renaming of the file is successful, notify NTF, otherwise wait for the Scheduler to retry the renaming.
             * No reason to notify NTF without a stamped file since NTF will not look at it.
             * Even if the file is stored under notification number, NTF still gets notified with subscriber number.
             */
            RenameFileResult renameFileResult = slamdownNotifierNoSQL.renameFile(msa, notificationNumber, filename);
            if (RenameFileResultType.RENAME_FILE_RESULT_TYPE_OK.equals(renameFileResult.getRenameFileResultType())) {
                MessageInfo msgInfo = new MessageInfo();
                msgInfo.rmsa = msa;
                Properties properties = new Properties();
                // The destination filename in the renameFileResult instance is already correctly formatted and contains
                // all necessary info to be set as the slamdown event file property.
                properties.setProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY, renameFileResult.getDestinationFilename());
                properties.setProperty(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY, notificationNumber);

                MfsEventManager.getMfs().notifyNtf(EventTypes.SLAM_DOWN, msgInfo, subscriberNumber, properties);

                if (storedEventId != null && !storedEventId.isEmpty()) {
                    AppliEventInfo eventInfo = new AppliEventInfo();
                    eventInfo.setEventId(storedEventId);
                    slamdownNotifierNoSQL.cancelEvent(eventInfo);
                }

            } else if (RenameFileResultType.RENAME_FILE_RESULT_TYPE_SOURCE.equals(renameFileResult.getRenameFileResultType())) {
                /**
                 * In a rename-failing scenario, the storedEventId could be:
                 * - null: in the case of a new event (no pending event found), schedule a retry and update persistent storage.
                 * - not null: already scheduled eventId, do not perform anything, will retry. 
                 */
                String destinationFileName = renameFileResult.getDestinationFilename();
                if (storedEventId != null && !storedEventId.isEmpty()) {
                    logger.warn("Unable to rename " + filename + " to " + destinationFileName + ", scheduler will retry.");
                } else {
                    logger.warn("Unable to rename " + filename + " to " + destinationFileName + ", event must be scheduled.");

                    Properties properties = new Properties();
                    properties.setProperty(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY, subscriberNumber);
                    // Build the proper value for the event file property, because the filename does not contain the full path.
                    properties.setProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY, PrivateEntryDB.buildFilename(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber, MfsEventManager.EVENTS_DIRECTORY, filename));
                    properties.setProperty(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY, notificationNumber);

                    AppliEventInfo eventInfo = slamdownNotifierNoSQL.scheduleEvent(calculateWhen(), generateUniqueId(subscriberNumber), EventTypes.INTERNAL_TIMER.getName(), properties);
                    logger.debug("Scheduled event: " + eventInfo.getEventId());

                    //boolean updatedSuccessfully = NtfNotifierEventHandlerNoSQL.updatePersistentFileEventId(filePath, eventInfo.getEventId(), true);
                    boolean updatedSuccessfully = NtfNotifierEventHandlerNoSQL.updatePersistentFileEventId(msa, notificationNumber, filename, eventInfo.getEventId(), true);
                    if (updatedSuccessfully) {
                        logger.debug("Scheduled event " + eventInfo.getEventId() + " for sourceFile " + filename + ", will retry");
                    } else {
                        logger.error("Unable to rename " + filename + " and to schedule event/update storage, dropping the SLAMDOWN event");
                        slamdownNotifierNoSQL.cancelEvent(eventInfo);
                    }
                }

            } else {
                logger.error("Unable to rename " + filename + " and to schedule event/update storage, droping the SLAMDOWN event");

                if (storedEventId != null && !storedEventId.isEmpty()) {
                    AppliEventInfo eventInfo = new AppliEventInfo();
                    eventInfo.setEventId(storedEventId);
                    slamdownNotifierNoSQL.cancelEvent(eventInfo);
                }
            }

        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }
    }

    protected void notifyNtf(TimeoutEventEntry entry) {
        Properties properties = entry.getProperties();
        String subscriberNumber = properties.getProperty(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY);
        String filePath = properties.getProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY);
        String notificationNumber = properties.getProperty(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY);
        PrivateEntryDB keys = PrivateEntryDB.parsePrivateForDB(filePath);
        MSA msa = MfsEventManagerNoSQL.getMSA(notificationNumber);

        // Normally, the artifactname is present in the PrivateEntryDB keys object, but in case it is not,
        // then fallback to the previous implementation.
        if (keys != null && keys.artifactname != null && keys.artifactname.isEmpty() == false) {
            notifyNtf(msa, entry.getNextEventId(), subscriberNumber, notificationNumber, keys.artifactname);
        } else {
            notifyNtf(msa, entry.getNextEventId(), subscriberNumber, notificationNumber, filePath);
        }
    }


    public ILogger getLogger() {
        return logger;
    }
}

