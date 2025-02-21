/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.common.trafficeventsender.mfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.abcxyz.services.moip.userinfo.UserFactory;
import com.abcxyz.services.moip.userinfo.UserInfo;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.MfsConfiguration;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderConfiguration;
import com.mobeon.common.trafficeventsender.mfs.NtfNotifierEventHandler.RenameFileResult;
import com.mobeon.common.trafficeventsender.mfs.NtfNotifierEventHandler.RenameFileResultType;

/**
 * Worker threads that saves the events into the user's event file.
 * <p>
 * It is important to note that this class is coupled to the MfsEventManager class.
 * Originally it was an inner class, but due to its larger size it has been moved outside of
 * MfsEventManager.
 * </p>
 */
class EventManagerWorker extends Thread implements IEventManagerWorker {

    private static final ILogger logger = ILoggerFactory.getILogger(EventManagerWorker.class);

    /**
	 * Utility class to hold information on buffer content.
	 */
	protected class BufferInfo {
		StringBuilder buffer;
		String calledNumber;
		String notificationNumber;
		int numberOfCalls = 0;
		String eventFilePath;
		int maxSlamdownInfoFiles = 200;
		
		BufferInfo(StringBuilder buffer, String calledNumber, String notificationNumber, String eventFilePath, int maxSlamdownInfoFiles) {
		    this.buffer = buffer;
		    this.calledNumber = calledNumber;
		    this.notificationNumber = notificationNumber;
		    this.eventFilePath = eventFilePath;
		    this.maxSlamdownInfoFiles = maxSlamdownInfoFiles;
		}

		public void endCallLine() {
		    numberOfCalls++;
		    this.buffer.append('\n');
		}
	}

	/** Event queue */
	private BlockingQueue<Object> eventQueue = null;
	protected int eventThreshold = 0;
	protected NtfNotifierEventHandler slamdownNotifier;
	private volatile boolean stop = false;
    int slamdownAggregationWhenInSecs = 100;

	EventManagerWorker(String threadName, NtfNotifierEventHandler slamdownNotifier) {
	    super(threadName);

	    MfsConfiguration mfsConfiguration = TrafficEventSenderConfiguration.getInstance().getMfsConfiguration();
	    eventThreshold = mfsConfiguration.getSlamdownEventThreshold();
	    eventQueue = new ArrayBlockingQueue<Object>(mfsConfiguration.getSlamdownEventQueueSize());

	    slamdownAggregationWhenInSecs = mfsConfiguration.getSlamdownEventTimeout();
	    this.slamdownNotifier = slamdownNotifier;
	}

    public void run() {
		while (!stop) {
		    try {
		        Object entry = eventQueue.take();
		        processQueue(entry);
		    } catch (Exception e) {
		        logger.error("Unexpected exception in EventManagerWorker: ", e);
		    } catch (Throwable t) {
		        logger.error("Throwable Exception in EventManagerWorker: ", t);
		    }
		}
	}

	private void processQueue(Object entry) {
		Object perf = null;
		try {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("BKD.CMA.TrES.EMW.processEntry");
			}

		    if (entry instanceof IncomingEventEntry) {
				// Regroup events per user
		        List<BufferInfo> bufferInfoList = storeEventPerUser((IncomingEventEntry)entry);

		        // Store events.
	            storeEventsToMfs(bufferInfoList);
			} else if (entry instanceof TimeoutEventEntry) {
				notifyNtf((TimeoutEventEntry)entry);
			}

		} finally{
			CommonOamManager.profilerAgent.exitCheckpoint(perf);
		}
	}

	public void enqueue(IncomingEventEntry entry) {
		eventQueue.add(entry);
	}

	public void enqueue(TimeoutEventEntry entry) {
		eventQueue.add(entry);
	}

	protected List<BufferInfo> storeEventPerUser(IncomingEventEntry entry) {
		Object perf = null;
	    try {
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				perf = CommonOamManager.profilerAgent.enterCheckpoint("BKD.CMA.TrES.EMW.storeEventPerUser");
			}

			logger.debug(this.getName() + " is processing " + entry.getEvent().getName() + " for " + entry.getEvent().getProperties());

			TrafficEvent trafficEvent = entry.getEvent();
			String eventFilePath = null;

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
			        *    
			        */
			        eventFilePath = MfsEventManager.generateFilePath(notificationNumber, trafficEvent.getName());
			        if (eventFilePath == null || eventFilePath.isEmpty()) {
			            logger.error("Cannot store Slamdown event for " + entry.getPhoneNumber() + " no store path created.");
			            return null;
			        }
		

			        // Fill the buffer with the properties.
			        BufferInfo bufferInfo = new BufferInfo(new StringBuilder(), entry.getPhoneNumber(), notificationNumber, eventFilePath, myMaxSlamdownInfoFiles);
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
		        String filePath = bufferInfo.eventFilePath;
		        File file = new File(filePath);
		        boolean fileExists = file.exists();

		        int originalCallCount = 0;
		        if (fileExists) {
		            originalCallCount = countEvents(file);
		        }
		        int bufferCallCount = bufferInfo.numberOfCalls;
		        String calledNumber = bufferInfo.calledNumber;
		        String notificationNumber = bufferInfo.notificationNumber;
                
		        FileWriter fileStream = null;
		        try {
		            fileStream = new FileWriter(file, true);
		        } catch (IOException e) {
		            /**
		             * Check if the MOIP private folder is present:
		             * - /opt/mfs/<internal|external>/<msid|eid>/private/moip/
		             * ==> MIO 5.0 MIO5_MFS
		             * - /opt/mfs/<internal|external>/<msid|eid>/
		             */
	                String rootPathName = MfsEventManager.generateRootPath(notificationNumber);
	                if (rootPathName != null) {
	                    File rootPath = new File(rootPathName);
	                    if (!MfsEventManager.validateOrCreateDirectory(rootPath, true)) {
	                        logger.error("Failed to create MOIP private directory for number " + notificationNumber + ", " + rootPath);
	                        return;
	                    }
	                } else {
	                    logger.error("Failed to generate MOIP private directory " + notificationNumber);
	                    return;
	                }

		            /**
		             * Check if the subscriber's event directory is present:
		             * - /opt/mfs/<internal|external>/<msid|eid>/private/moip/<notification number>/events/
		             * ==> MIO 5.0 MIO5_MFS
		             * - /opt/mfs/<internal|external>/<msid|eid>/private_moip_<notification number>_events/
		             */
		            File eventPath = file.getParentFile();
		            if (!MfsEventManager.validateOrCreateDirectory(eventPath, false)) {
		                logger.error("Failed to create subscriber's event directory " + eventPath);
		                return;
		            }

		            // Create the event file
		            try {
		                fileStream = new FileWriter(file, true);
		            } catch (IOException ioe) {
		                logger.error("Cannot write to event file: " + ioe.getMessage());
		                return;
		            }
		        }

		        try {
		            String buffer = bufferInfo.buffer.toString();
		            boolean mustNotify = originalCallCount + bufferCallCount >= eventThreshold;
		            String storedEventId = null;

		            if (!mustNotify) {
		                if (fileExists) {
		                    logger.debug("Store Slamdown event for " + calledNumber + ", must not notify and file already exists, append.");

		                    // Append the new call line(s) to the existing file
		                    fileStream.write(buffer);
		                    
		                    if(logger.isDebugEnabled()){
		                        logger.debug("Event added in file: " + buffer);
		                    }
		                } else {
		                    logger.debug("Store Slamdown event for " + calledNumber + ", must not notify and file does not exist, create new file.");

		                    /**
		                     * When receiving the first Slamdown/Mcn call for a subscriber/non-subscriber, a timeout for notifying NTF
		                     * must be started (except if the count has already reached the threshold, no aggregation timer is needed). 
		                     */
		                    Properties properties = new Properties();
		                    properties.setProperty(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY, calledNumber);
		                    properties.setProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY, filePath);
		                    properties.setProperty(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY, notificationNumber);

		                    AppliEventInfo eventInfo = slamdownNotifier.scheduleEvent(calculateWhen(), generateUniqueId(calledNumber), EventTypes.INTERNAL_TIMER.getName(), properties);
		                    logger.debug("Scheduled event: " + eventInfo.getEventId());

		                    // Append the subscriber/non-subscriber number
		                    StringBuffer stringBuffer = new StringBuffer();
		                    stringBuffer.append(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY + MfsEventManager.KEY_VALUE_SEPARATOR + calledNumber + MfsEventManager.PROPERTY_SEPARATOR);

		                    // Append the eventId
		                    stringBuffer.append(MoipMessageEntities.SLAMDOWN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR + eventInfo.getEventId() + '\n');

		                    // Append the calls
		                    stringBuffer.append(buffer);

		                    fileStream.write(stringBuffer.toString());
		                    fileStream.close();
		                    
		                    if(logger.isDebugEnabled()){
                                logger.debug("Event added in file: " + stringBuffer.toString());
                            }
		                }
		            } else {
		                if (fileExists) {
		                    logger.debug("Store Slamdown event for " + calledNumber + ", must notify and file already exists, cancel eventId and notify.");

		                    // Read the eventId stored in the existing file and cancel it
		                    BufferedReader reader = null;

		                    reader = new BufferedReader(new FileReader(file));
		                    String firstLine = reader.readLine();
		                    reader.close();
		                    if (firstLine != null) {
		                        int indexOf = firstLine.indexOf(MoipMessageEntities.SLAMDOWN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR);
		                        storedEventId = firstLine.substring(indexOf + (MoipMessageEntities.SLAMDOWN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR).length());
		                    }

		                    // Append the calls to the existing file
		                    StringBuffer stringBuffer = new StringBuffer();
		                    stringBuffer.append(buffer);

		                    fileStream.write(stringBuffer.toString());
		                    fileStream.close();

		                    if(logger.isDebugEnabled()){
                                logger.debug("Event added in file: " + stringBuffer.toString());
                            }
		                    
		                } else {
		                    logger.debug("Store Slamdown event for " + calledNumber + ", must notify and file does not exist, notify.");

		                    // Create new file
		                    StringBuffer stringBuffer = new StringBuffer();

		                    // Adding the subscriber/non-subscriber number
		                    stringBuffer.append(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY + MfsEventManager.KEY_VALUE_SEPARATOR + calledNumber + MfsEventManager.PROPERTY_SEPARATOR);

		                    // Adding an empty eventId
		                    stringBuffer.append(MoipMessageEntities.SLAMDOWN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR + '\n');

		                    // Adding the calls
		                    stringBuffer.append(buffer);

		                    fileStream.write(stringBuffer.toString());
                            fileStream.close();
		                    
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
		                notifyNtf(file, storedEventId, calledNumber, notificationNumber);
		            }

		        } catch (IOException ioe) {
	                logger.warn("IOException for " + calledNumber, ioe);
	            } catch (Exception e) {
	                logger.warn("Exception for " + calledNumber, e);
		        } finally {
		            try {
		                fileStream.close();
		            } catch (IOException e) {}
		        }
		    }
		} finally {
		    CommonOamManager.profilerAgent.exitCheckpoint(perf);
		}
	}

    protected int countEvents(File file) {
    	int count = 0; 
    	try {
			count = countEvents(new FileReader(file),file.getPath());
		} catch (FileNotFoundException e) {
			logger.error("Error reading file " + file.getPath() + ": " + e.getMessage());
		}
    	return count;
    }

    
    /**
     * 
     * @param in
     * @param filePath  Only for logging purposes, to identity the event being retrieved.
     * @return
     */
    protected int countEvents(Reader in, String filePath) {
        int count = 0;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(in);
            String line;
            while ((line = reader.readLine()) != null) {
                ++count;
                if(logger.isDebugEnabled()){
                    logger.debug("Content of the file: " + filePath + ", line " + count + " is : " + line);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading file " + filePath + ": " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {}
            }
        }

        /**
         * First line of the file is always the subscriber number and the eventId (for the timeout period) 
         */
        return (count > 0 ? count-1 : 0);
    }

    private void notifyNtf(File sourceFile, String storedEventId, String subscriberNumber, String notificationNumber) {
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
    		RenameFileResult renameFileResult = slamdownNotifier.renameFile(sourceFile);
            if (RenameFileResultType.RENAME_FILE_RESULT_TYPE_OK.equals(renameFileResult.getRenameFileResultType())) {
                MessageInfo msgInfo = new MessageInfo();
                msgInfo.rmsa = new MSA(MfsEventManager.getMSID(subscriberNumber));
                Properties properties = new Properties();
                properties.setProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY, renameFileResult.getDestinationFile().getName());
                properties.setProperty(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY, notificationNumber);

                MfsEventManager.getMfs().notifyNtf(EventTypes.SLAM_DOWN, msgInfo, subscriberNumber, properties);

                if (storedEventId != null && !storedEventId.isEmpty()) {
                    AppliEventInfo eventInfo = new AppliEventInfo();
                    eventInfo.setEventId(storedEventId);
                    slamdownNotifier.cancelEvent(eventInfo);
                }

            } else if (RenameFileResultType.RENAME_FILE_RESULT_TYPE_SOURCE.equals(renameFileResult.getRenameFileResultType())) {
                /**
                 * In a rename-failing scenario, the storedEventId could be:
                 * - null: in the case of a new event (no pending event found), schedule a retry and update persistent storage.
                 * - not null: already scheduled eventId, do not perform anything, will retry. 
                 */
                String destinationFileName = renameFileResult.getDestinationFile().getName();
                if (storedEventId != null && !storedEventId.isEmpty()) {
                    logger.warn("Unable to rename " + sourceFile + " to " + destinationFileName + ", scheduler will retry.");
                } else {
                    logger.warn("Unable to rename " + sourceFile + " to " + destinationFileName + ", event must be scheduled.");

                    String filePath = sourceFile.getPath();
                    Properties properties = new Properties();
                    properties.setProperty(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY, subscriberNumber);
                    properties.setProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY, filePath);
                    properties.setProperty(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY, notificationNumber);

                    AppliEventInfo eventInfo = slamdownNotifier.scheduleEvent(calculateWhen(), generateUniqueId(subscriberNumber), EventTypes.INTERNAL_TIMER.getName(), properties);
                    logger.debug("Scheduled event: " + eventInfo.getEventId());

                    boolean updatedSuccessfully = NtfNotifierEventHandler.updatePersistentFileEventId(filePath, eventInfo.getEventId(), true);
                    if (updatedSuccessfully) {
                        logger.debug("Scheduled event " + eventInfo.getEventId() + " for sourceFile " + sourceFile + ", will retry");
                    } else {
                        logger.error("Unable to rename " + sourceFile + " and to schedule event/update storage, dropping the SLAMDOWN event");
                        slamdownNotifier.cancelEvent(eventInfo);
                    }
                }

            } else {
                logger.error("Unable to rename " + sourceFile + " and to schedule event/update storage, droping the SLAMDOWN event");

                if (storedEventId != null && !storedEventId.isEmpty()) {
                    AppliEventInfo eventInfo = new AppliEventInfo();
                    eventInfo.setEventId(storedEventId);
                    slamdownNotifier.cancelEvent(eventInfo);
                }
            }

    	} finally {
    		CommonOamManager.profilerAgent.exitCheckpoint(perf);
    	}
    }

    protected long calculateWhen() {
        return System.currentTimeMillis() + (slamdownAggregationWhenInSecs * 1000);
    }

    protected String generateUniqueId(String calledNumber) {
        return CommonMessagingAccess.getUniqueId() + "tel" + calledNumber + "tid" + this.getName().substring(this.getName().indexOf("-")+1);        
    }

    protected void notifyNtf(TimeoutEventEntry entry) {
        Properties properties = entry.getProperties();
        String subscriberNumber = properties.getProperty(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY);
        String filePath = properties.getProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY);
        String notificationNumber = properties.getProperty(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY);
        notifyNtf(new File(filePath), entry.getNextEventId(), subscriberNumber, notificationNumber);
    }

	public ILogger getLogger() {
		return logger;
	}
}

