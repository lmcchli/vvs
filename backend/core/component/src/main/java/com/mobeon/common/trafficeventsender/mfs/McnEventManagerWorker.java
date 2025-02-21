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
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.masevent.EventTypes;
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
 * Worker thread that saves the MCN events into the user's event file.
 */
class McnEventManagerWorker extends Thread implements IEventManagerWorker {

    private static final ILogger logger = ILoggerFactory.getILogger(McnEventManagerWorker.class);

    /**
	 * Utility class to hold information on buffer content.
	 */
	protected class BufferInfo {
		StringBuilder buffer;
		String calledNumber;
		int numberOfCalls = 0;
		String eventFilePath;

		BufferInfo(StringBuilder buffer, String calledNumber, String eventFilePath) {
			this.buffer = buffer;
			this.calledNumber = calledNumber;
			this.eventFilePath = eventFilePath;
		}

		public void endCallLine() {
		    numberOfCalls++;
		    this.buffer.append('\n');
		}
	}

	/** Event queue */
	private BlockingQueue<Object> eventQueue = null;
	protected int eventThreshold = 0;
	private NtfNotifierEventHandler mcnNotifier;
	private volatile boolean stop = false;
    int mcnAggregationWhenInSecs = 100;

	McnEventManagerWorker(String threadName, NtfNotifierEventHandler mcnNotifier) {
	    super(threadName);

	    MfsConfiguration mfsConfiguration = TrafficEventSenderConfiguration.getInstance().getMfsConfiguration();
	    eventThreshold = mfsConfiguration.getMcnEventThreshold();
	    eventQueue = new ArrayBlockingQueue<Object>(mfsConfiguration.getMcnEventQueueSize());

        mcnAggregationWhenInSecs = mfsConfiguration.getMcnEventTimeout();
	    this.mcnNotifier = mcnNotifier;
	}

	public void run() {
	    while (!stop) {
	        try {
	            Object entry = eventQueue.take();
	            processQueue(entry);
	        } catch (Exception e) {
	            logger.error("Exception in McnEventManagerWorker: ", e);
	        } catch (Throwable t) {
	            logger.error("Throwable Exception in McnEventManagerWorker: ", t);
	        }
	    }
	}

	private void processQueue(Object entry) {
        Object perf = null;
        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("BKD.CMA.TrES.McnEMW.processEntry");
            }

			if (entry instanceof IncomingEventEntry) {
				// Regroup events per user
			    BufferInfo bufferInfo = storeEventPerUser((IncomingEventEntry)entry);

				// Store events.
	            storeEventsToMfs(bufferInfo);
			} else if (entry instanceof TimeoutEventEntry) {
				notifyNtf((TimeoutEventEntry)entry);
			}

        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }
    }

	public void enqueue(IncomingEventEntry entry) {
		eventQueue.add(entry);
	}

	public void enqueue(TimeoutEventEntry entry) {
		eventQueue.add(entry);
	}

	protected BufferInfo storeEventPerUser(IncomingEventEntry entry) {
        Object perf = null;
        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("BKD.CMA.TrES.McnEMW.storeEventPerUser");
            }

            logger.debug(this.getName() + " is processing " + entry.getEvent().getName() + " for " + entry.getEvent().getProperties());

            TrafficEvent trafficEvent = entry.getEvent();
            String eventFilePath = null;

    	    /**
    	     * MCN case is for non-subscriber, therefore, the event file should be stored under: 
    	     * /opt/mfs/external/<eid>/private/moip/<b-number>/events/mcninformation_<time stamp>
    	     * ==> MIO 5.0 MIO5_MFS
    	     * /opt/mfs/external/<eid>/private_moip_<b-number>_events/mcninformation_<time stamp>
    	     */
    
            eventFilePath = MfsEventManager.generateFilePath(entry.getPhoneNumber(), trafficEvent.getName(),  (MfsEventManager.getMSID(entry.getPhoneNumber()) != null));
            if (eventFilePath == null) {
                logger.error("Cannot store MCN event for " + entry.getPhoneNumber() + " no store path created. ");
                return null;
            }   

    	    // Fill the buffer with the properties.
            BufferInfo bufferInfo = new BufferInfo(new StringBuilder(), entry.getPhoneNumber(), eventFilePath);
    	    Map<String, String> propertyMap = trafficEvent.getProperties();
    	    Set<Map.Entry<String, String>> entries = propertyMap.entrySet();
    	    for (Map.Entry<String, String> property : entries) {
    	        bufferInfo.buffer.append(property.getKey() + MfsEventManager.KEY_VALUE_SEPARATOR + property.getValue() + MfsEventManager.PROPERTY_SEPARATOR);
    	    }
            bufferInfo.endCallLine();

            return bufferInfo;
        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }
	}

	protected void storeEventsToMfs(BufferInfo bufferInfo){
        Object perf = null;
        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("BKD.CMA.TrES.McnEMW.storeEventsToMfs");
            }

            if (bufferInfo == null) {
                logger.debug("bufferInfo is empty, no MCN event to store.");
                return;
            }

            // Store events to file system
            String filePath = bufferInfo.eventFilePath;
            File file = new File(filePath);
            boolean fileExists = file.exists();

            int originalCallCount = 0;
            if (fileExists) {
                originalCallCount = countEvents(file);
            }
            int bufferCallCount = bufferInfo.numberOfCalls;
            String calledNumber = bufferInfo.calledNumber;

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
                String rootPathName = MfsEventManager.generateRootPath(calledNumber);
                if (rootPathName != null) {
                    File rootPath = new File(rootPathName);
                    if (!MfsEventManager.validateOrCreateDirectory(rootPath, true)) {
                        logger.error("Failed to create MOIP private directory for number " + calledNumber + ", " + rootPath);
                        return;
                    }
                } else {
                    logger.error("Failed to generate MOIP private directory " + calledNumber);
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
                        logger.debug("Store Mcn event for " + calledNumber + ", must not notify and file already exists, append.");

                        // Append the new call line(s) to the existing file
                        fileStream.write(buffer);
                    } else {
                        logger.debug("Store Mcn event for " + calledNumber + ", must not notify and file does not exist, create new file.");

                        /**
                         * When receiving the first Mcn call for a subscriber/non-subscriber, a timeout for notifying NTF
                         * must be started (except if the count has already reached the threshold, no aggregation timer is needed). 
                         */
                        Properties properties = new Properties();
                        properties.setProperty(MoipMessageEntities.MCN_CALLED_NUMBER_PROPERTY, calledNumber);
                        properties.setProperty(MoipMessageEntities.MCN_EVENT_FILE_PROPERTY, filePath);

                        AppliEventInfo eventInfo = mcnNotifier.scheduleEvent(calculateWhen(), generateUniqueId(calledNumber), EventTypes.INTERNAL_TIMER.getName(), properties);
                        logger.debug("Scheduled event: " + eventInfo.getEventId());

                        // Append the subscriber/non-subscriber number
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(MoipMessageEntities.MCN_CALLED_NUMBER_PROPERTY + MfsEventManager.KEY_VALUE_SEPARATOR + calledNumber + MfsEventManager.PROPERTY_SEPARATOR);

                        // Append the eventId
                        stringBuffer.append(MoipMessageEntities.MCN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR + eventInfo.getEventId() + '\n');

                        // Append the calls
                        stringBuffer.append(buffer);

                        fileStream.write(stringBuffer.toString());
                        fileStream.close();
                    }
                } else {
                    if (fileExists) {
                        logger.debug("Store Mcn event for " + calledNumber + ", must notify and file already exists, cancel eventId and notify.");

                        // Read the eventId stored in the existing file and cancel it
                        BufferedReader reader = null;

                        reader = new BufferedReader(new FileReader(file));
                        String firstLine = reader.readLine();
                        reader.close();
                        if (firstLine != null) {
                            int indexOf = firstLine.indexOf(MoipMessageEntities.MCN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR);
                            storedEventId = firstLine.substring(indexOf + (MoipMessageEntities.MCN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR).length());
                        }

                        // Append the calls to the existing file
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(buffer);

                        fileStream.write(stringBuffer.toString());
                        fileStream.close();

                    } else {
                        logger.debug("Store Mcn event for " + calledNumber + ", must notify and file does not exist, notify.");

                        // Create new file
                        StringBuffer stringBuffer = new StringBuffer();

                        // Adding the subscriber/non-subscriber number
                        stringBuffer.append(MoipMessageEntities.MCN_CALLED_NUMBER_PROPERTY + MfsEventManager.KEY_VALUE_SEPARATOR + calledNumber + MfsEventManager.PROPERTY_SEPARATOR);

                        // Adding an empty eventId
                        stringBuffer.append(MoipMessageEntities.MCN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR + '\n');

                        // Adding the calls
                        stringBuffer.append(buffer);

                        fileStream.write(stringBuffer.toString());
                        fileStream.close();
                    }

                    /**
                     * When the event threshold has been reached, the event file is renamed with a time stamp
                     * and NTF is notified that an event file is ready with the renamed file.
                     * If the renaming of the file is successful, notify NTF, otherwise wait for the Scheduler to retry the renaming.
                     * No reason to notify NTF without a stamped file since NTF will not look at it.
                     * Even if the file is stored under notification number, NTF still gets notified with subscriber number.
                     */
                    notifyNtf(file, storedEventId, calledNumber);
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

    
	private void notifyNtf(File sourceFile, String storedEventId, String telephoneNumber) {
	    Object perf = null;
	    try {
	        if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	            perf = CommonOamManager.profilerAgent.enterCheckpoint("BKD.CMA.TrES.McnEMW.notifyNtf");
	        }

            /**
             * If the renaming of the file is successful, notify NTF, otherwise wait for the Scheduler to retry the renaming.
             * No reason to notify NTF without a stamped file since NTF will not look at it.
             * Even if the file is stored under notification number, NTF still gets notified with subscriber number.
             */
            RenameFileResult renameFileResult = mcnNotifier.renameFile(sourceFile);
            if (RenameFileResultType.RENAME_FILE_RESULT_TYPE_OK.equals(renameFileResult.getRenameFileResultType())) {

                // Mcn called telephone number can be internal (MCN-Subscribed feature)
                String msid = MfsEventManager.getMSID(telephoneNumber);
                if (msid == null) {
                    // External telephone number (MCN feature) 
                    try {
                        // Create an external MSA
                        MSA msa = MFSFactory.getGen2MSA(telephoneNumber, false);
                        msid = msa.toString();
                    } catch (Exception e) {
                        logger.error("Unable to create an external MSA, notifyNtf will not be performed.");
                        return;
                    }
                }

                MessageInfo msgInfo = new MessageInfo();
                msgInfo.rmsa = new MSA(msid);
                Properties properties = new Properties();
                properties.setProperty(MoipMessageEntities.MCN_EVENT_FILE_PROPERTY, renameFileResult.getDestinationFile().getName());
                properties.setProperty(MoipMessageEntities.MCN_NOTIFICATION_NUMBER_PROPERTY, telephoneNumber);

                MfsEventManager.getMfs().notifyNtf(EventTypes.MCN, msgInfo, telephoneNumber, properties);

                if (storedEventId != null && !storedEventId.isEmpty()) {
                    AppliEventInfo eventInfo = new AppliEventInfo();
                    eventInfo.setEventId(storedEventId);
                    mcnNotifier.cancelEvent(eventInfo);
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
                    properties.setProperty(MoipMessageEntities.MCN_CALLED_NUMBER_PROPERTY, telephoneNumber);
                    properties.setProperty(MoipMessageEntities.MCN_EVENT_FILE_PROPERTY, filePath);

                    AppliEventInfo eventInfo = mcnNotifier.scheduleEvent(calculateWhen(), generateUniqueId(telephoneNumber), EventTypes.INTERNAL_TIMER.getName(), properties);
                    logger.debug("Scheduled event: " + eventInfo.getEventId());

                    boolean updatedSuccessfully = NtfNotifierEventHandler.updatePersistentFileEventId(filePath, eventInfo.getEventId(), true);
                    if (updatedSuccessfully) {
                        logger.debug("Scheduled event " + eventInfo.getEventId() + " for sourceFile " + sourceFile + ", will retry");
                    } else {
                        logger.error("Unable to rename " + sourceFile + " and to schedule event/update storage, dropping the MCN event");
                        mcnNotifier.cancelEvent(eventInfo);
                    }
                }

            } else {
                logger.error("Unable to rename " + sourceFile + " and to schedule event/update storage, dropping the MCN event");

                if (storedEventId != null && !storedEventId.isEmpty()) {
                    AppliEventInfo eventInfo = new AppliEventInfo();
                    eventInfo.setEventId(storedEventId);
                    mcnNotifier.cancelEvent(eventInfo);
                }
            }

	    } finally {
	        CommonOamManager.profilerAgent.exitCheckpoint(perf);
	    }
	}

	protected long calculateWhen() {
	    return System.currentTimeMillis() + (mcnAggregationWhenInSecs * 1000);
	}

    protected String generateUniqueId(String calledNumber) {
        return CommonMessagingAccess.getUniqueId() + "tel" + calledNumber + "tid" + this.getName().substring(this.getName().indexOf("-")+1);        
    }

	protected void notifyNtf(TimeoutEventEntry entry) {
		Properties properties = entry.getProperties();
		String telephoneNumber = properties.getProperty(MoipMessageEntities.MCN_CALLED_NUMBER_PROPERTY);
		String filePath = properties.getProperty(MoipMessageEntities.MCN_EVENT_FILE_PROPERTY);
		notifyNtf(new File(filePath), entry.getNextEventId(), telephoneNumber);
	}

	public ILogger getLogger() {
		return logger;
	}
}