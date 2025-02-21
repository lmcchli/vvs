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
import java.util.Date;

import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.masevent.AbstractEventHandler;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * This class is used by the EventManagerWorker.
 * <p>
 * NtfNotifierEventHandler receives events from the persistent scheduler. It enqueues
 * slam down timeout events to the Event Manager Worker.
 * </p>
 * @author egeobli
 *
 */
class NtfNotifierEventHandler extends AbstractEventHandler {

    private static final ILogger logger = ILoggerFactory.getILogger(NtfNotifierEventHandler.class);

    protected MfsEventManager mfsEventManager;
    protected String timeoutEventType;
    protected int aggregationNumberOfExpiryRetries;

	/**
	 * Constructs a NTF notifier handler associated to an event manager worker.
	 * @param mfsEventManager MfsEventManager
     * @param timeoutEventType String 
     * @param aggregationNumberOfExpiryRetries Number of retries (if the aggregation failed and must retry)
	 */
    NtfNotifierEventHandler(MfsEventManager mfsEventManager, String timeoutEventType, int aggregationNumberOfExpiryRetries) {
        this.mfsEventManager = mfsEventManager;
        this.timeoutEventType = timeoutEventType;
        this.aggregationNumberOfExpiryRetries = aggregationNumberOfExpiryRetries;       
    }

	/* (non-Javadoc)
	 * @see com.abcxyz.messaging.scheduler.handling.EventsStatusListener#eventFired(com.abcxyz.messaging.scheduler.handling.AppliEventInfo)
	 */
	@Override
	public int eventFired(AppliEventInfo eventInfo) {

	    String fileName = eventInfo.getEventProperties().getProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY);
	    int result = EventHandleResult.OK;
	    BufferedReader reader = null;

	    try {
	        if (eventInfo.getNumberOfTried() >= aggregationNumberOfExpiryRetries) {
	            /**
	             * The aggregation file is not deleted in this case since writing (including potential renaming operation)
	             * always failed until this expiration of retries.  The file will stay there until next slamdown/mcn deposit. 
	             */
	            logger.info("Expiry event " + eventInfo.getEventId() + ", will not retry (number of expiry retries reached");
	            return EventHandleResult.STOP_RETRIES;
	        }

	        if (fileName != null && !fileName.isEmpty() && eventInfo.getEventId() != null) {
	            String storedEventId = null;

	            //The first line contains the callednumber and eventid properties. 
	            reader = new BufferedReader(new FileReader(fileName));
	            String line = reader.readLine();
	            reader.close();
	            if (line != null) {
	                boolean isFileContainEventId = false;
	                boolean shouldProcessEvent;

	                if (!line.contains(MfsEventManager.PROPERTY_SEPARATOR + MoipMessageEntities.SLAMDOWN_EVENT_ID)) {
	                    logger.debug("File " + fileName + " does not contain eventid tag, process the fired event.");

	                    //For backward compatibility (eventid not stored in file)
	                    shouldProcessEvent = true;
	                } else {
	                    logger.debug("File " + fileName + " contain eventid tag, compare the stored eventid with the fired one.");

	                    isFileContainEventId = true;
	                    String storedEventIdProperty = line.substring(line.indexOf(MfsEventManager.PROPERTY_SEPARATOR) + 1);
	                    storedEventId = storedEventIdProperty.replace(MoipMessageEntities.SLAMDOWN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR, "");

	                    shouldProcessEvent = CommonMessagingAccess.getInstance().compareEventIds(eventInfo, storedEventId);
	                }

	                if (shouldProcessEvent) {
	                    logger.debug("File " + fileName + " contains same eventId has fired event, inject this event.");

	                    // Update file with next event info
	                    String nextEventId = "";
	                    if (eventInfo.getNextEventInfo() != null) {
	                        nextEventId = eventInfo.getNextEventInfo().getEventId() != null ? eventInfo.getNextEventInfo().getEventId() : "";
	                    }

	                    boolean updatedSuccessfully = updatePersistentFileEventId(fileName, nextEventId, isFileContainEventId);
	                    if (updatedSuccessfully) {
	                        // Inject in EventManagerWorker
	                        mfsEventManager.injectTimeoutEvent(timeoutEventType, eventInfo.getEventProperties(), nextEventId);
	                    }

	                } else {
	                    /**
	                     * Geo-Distributed case.
	                     * Case of an obsolete event.  Event which has been cancelled by another MAS instance (on another site).
	                     * Since the cancellation was ineffective, the event now kicks-in but is obsoleted by the new one stored on disk.
	                     * 
	                     * Limit case.
	                     * There is a possibility that this event is a retry but the eventId has not been stored properly on disk,
	                     * therefore, the event ids will not match and this event will be dropped.  A warn log is used to track this case.
	                     */
	                    logger.info("File " + fileName + " contains eventId " + storedEventId + " while fired event is " + eventInfo.getEventId() + ", do not process this fired event.");
	                    result = EventHandleResult.STOP_RETRIES;
	                }
	            } else {
	                logger.warn("Empty file " + fileName + ", will stop retries.");
	                result = EventHandleResult.STOP_RETRIES;
	            }
	        } else {
	            logger.warn("Invalid event fired, fileName: " + fileName + ", eventInfo.getEventId: " + eventInfo.getEventId() + ", will stop retries.");
	            result = EventHandleResult.STOP_RETRIES;
	        }

	    } catch (FileNotFoundException fnfe) {
	        logger.warn("File " + fileName + " not found, stop retry");
	        result = EventHandleResult.STOP_RETRIES;
	    } catch (Exception e) {
	        String message = "Exception for " + fileName;
	        if (eventInfo.getNextEventInfo() != null) {
                logger.warn(message + ", will retry. ", e);
	        } else {
                logger.error(message + ", will not retry. ", e);
	        }
	    } finally {
	        try {
	            if (reader != null) { reader.close(); }
	        } catch (IOException ioe) { ; }
	    }

	    return result;
	}

    public static boolean updatePersistentFileEventId(String fileName, String nextEventId, boolean isFileContainEventId) {
        int tried = 0;
        boolean fileUpdatedSuccessfully = false;
        BufferedReader reader = null;
        FileWriter fileStream = null;
        Object perf = null;

        int updateSleep = 50;
        String renameSleepString = System.getProperty("com.abcxyz.vvs.backend.updatesleep", "50");
        try {
            updateSleep = Integer.parseInt(renameSleepString);
        } catch (NumberFormatException nfex) { ; }

        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("BKD.CMA.TrES.NtfNotifierEventHandler.updatePersistent");
            }

            while (!fileUpdatedSuccessfully && tried++ < 3) {
                try {
                    // Read original file
                    reader = new BufferedReader(new FileReader(fileName));
                    StringBuffer currentFileContentBuffer = new StringBuffer();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        currentFileContentBuffer.append(line + '\n');
                    }
                    reader.close();

                    // Replace eventId
                    String currentFileContent = currentFileContentBuffer.toString();
                    String newFileContent = null;

                    if (isFileContainEventId) {
                        //The file contains the eventid property; replace the old with the nextEventId as the property value
                        int indexOfEventIdStart = currentFileContent.indexOf(MoipMessageEntities.SLAMDOWN_EVENT_ID);
                        int indexOfEventIdEnd = currentFileContent.indexOf('\n');

                        String eventIdToReplace = currentFileContent.substring(indexOfEventIdStart, indexOfEventIdEnd);
                        newFileContent = currentFileContent.replace(eventIdToReplace, MoipMessageEntities.SLAMDOWN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR + nextEventId);
                    } else {
                        //The file does not contain any eventid property; add the eventid property with the nextEventId.
                        newFileContent = currentFileContent.replaceFirst("\n", MfsEventManager.PROPERTY_SEPARATOR + MoipMessageEntities.SLAMDOWN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR + nextEventId + '\n');
                    }

                    // Update the file
                    fileStream = new FileWriter(new File(fileName), false);
                    fileStream.write(newFileContent);
                    fileStream.close();

                    fileUpdatedSuccessfully = true;

                } catch (IOException ioe) {
                    fileUpdatedSuccessfully = false;
                    logger.debug("IOException ", ioe);
                    try {
                        Thread.sleep(updateSleep);
                    } catch (Exception ex) { ; }
                } finally {
                    try {
                        if (reader != null) { reader.close(); }
                        if (fileStream != null) { fileStream.close(); }
                    } catch (IOException ioe) {
                        fileUpdatedSuccessfully = false;
                    }
                }
            }

        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }

        return fileUpdatedSuccessfully;
	}

    public enum RenameFileResultType {
        RENAME_FILE_RESULT_TYPE_OK,
        RENAME_FILE_RESULT_TYPE_SOURCE,
        RENAME_FILE_RESULT_TYPE_NONE;
    }

    public class RenameFileResult {
        RenameFileResultType renameFileResultType = null;
        File destinationFile = null;
        String destinationFilename = null;

        public RenameFileResult() {
        }

        /**
         *
         * @param file  This must be a full path.
         * @param renameFileResultType
         */
        public void setResponse(File file, RenameFileResultType renameFileResultType) {
            this.destinationFile = file;
            this.renameFileResultType = renameFileResultType;
        }

        /**
         *
         * @param filename  This string representation should be a full path, not just the filename.
         * @param renameFileResultType
         */
        public void setResponse(String filename, RenameFileResultType renameFileResultType) {
            this.destinationFilename = filename;
            this.renameFileResultType = renameFileResultType;
        }
        
        public File getDestinationFile() {
            return this.destinationFile;
        }

        public String getDestinationFilename() {
            return this.destinationFilename;
        }
        
        public RenameFileResultType getRenameFileResultType() {
            return this.renameFileResultType;
        }
    }

    public RenameFileResult renameFile(File sourceFile) {
        int tried = 0;
        boolean fileRenamedSuccessfully = false;
        File destinationFile = null;
        RenameFileResult renameFileResult = new RenameFileResult();
        Object perf = null;

        int renameSleep = 50;
        String renameSleepString = System.getProperty("com.abcxyz.vvs.backend.renamefilesleep", "50");
        try {
            renameSleep = Integer.parseInt(renameSleepString);
        } catch (NumberFormatException nfex) { ; }

        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("BKD.CMA.TrES.NtfNotifierEventHandler.renameFile");
            }

            while (!fileRenamedSuccessfully && tried++ < 3) {
                try {
                    String destinationFileName = sourceFile.getName() + MfsEventManager.TOKEN_SEPARATOR + MfsEventManager.dateFormat.get().format(new Date());
                    destinationFile = new File(sourceFile.getParentFile(), destinationFileName);

                    boolean fileRenamed = sourceFile.renameTo(destinationFile); 
                    if (fileRenamed) {
                        logger.debug("File " + sourceFile + " renamed to " + destinationFile);
                        renameFileResult.setResponse(destinationFile, RenameFileResultType.RENAME_FILE_RESULT_TYPE_OK);
                        fileRenamedSuccessfully = true;
                    } else {
                        /**
                         * Case of not being able to rename file (this scenario has been experienced during performance tests).
                         * Since it's specific/unusual case, best effort scenario (retries) is considered.
                         */
                        if (destinationFile.canRead()) {
                            // destinationFile found, successful
                            logger.debug("sourceFile " + sourceFile + " renamed to destinationFile " + destinationFile + " (.canRead() has been used)");
                            renameFileResult.setResponse(destinationFile, RenameFileResultType.RENAME_FILE_RESULT_TYPE_OK);
                            fileRenamedSuccessfully = true;
                        } else if (sourceFile.canRead()) {
                            // sourceFile found, might retry (if tried < 3)
                            String retryLog = (tried < 3 ? " will retry (count=" + tried + ")" : " will not retry");
                            logger.debug("sourceFile " + sourceFile + " not renamed to destinationFile " + destinationFile + " (.canRead() source file, " + retryLog);
                            renameFileResult.setResponse(destinationFile, RenameFileResultType.RENAME_FILE_RESULT_TYPE_SOURCE);
                            Thread.sleep(renameSleep);
                        } else {
                            // neither sourceFile nor destinationFile found, might retry (if tried < 3)
                            String retryLog = (tried < 3 ? " will retry (count=" + tried + ")" : " will not retry");
                            logger.debug("sourceFile " + sourceFile + " and destinationFile " + destinationFile + " not found" + retryLog);
                            renameFileResult.setResponse(destinationFile, RenameFileResultType.RENAME_FILE_RESULT_TYPE_NONE);
                            Thread.sleep(renameSleep);
                        }
                    }

                } catch (Exception e) {
                    logger.warn("Exception in renameFile", e);
                    renameFileResult.setResponse(destinationFile, RenameFileResultType.RENAME_FILE_RESULT_TYPE_NONE);
                    try {
                        Thread.sleep(renameSleep);
                    } catch (Exception ex) { ; }
                }
            }

        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }

        return renameFileResult;
    }

    /* (non-Javadoc)
	 * @see com.abcxyz.messaging.scheduler.handling.EventsStatusListener#reportCorruptedEventFail(java.lang.String)
	 */
	@Override
	public void reportCorruptedEventFail(String eventId) {
	    logger.debug("Report corrupted event fail: " + eventId);
	}

	/* (non-Javadoc)
	 * @see com.abcxyz.messaging.scheduler.handling.EventsStatusListener#reportEventCancelFail(com.abcxyz.messaging.scheduler.handling.AppliEventInfo)
	 */
	@Override
	public void reportEventCancelFail(AppliEventInfo eventInfo) {
	    logger.debug("Report cancel event fail: " + eventInfo);
	}

	/* (non-Javadoc)
	 * @see com.abcxyz.messaging.scheduler.handling.EventsStatusListener#reportEventScheduleFail(com.abcxyz.messaging.scheduler.handling.AppliEventInfo)
	 */
	@Override
	public void reportEventScheduleFail(AppliEventInfo eventInfo) {
	    logger.debug("Report scheduled event fail: " + eventInfo);
	}
}
