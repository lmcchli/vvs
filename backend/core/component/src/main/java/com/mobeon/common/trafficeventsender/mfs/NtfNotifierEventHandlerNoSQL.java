package com.mobeon.common.trafficeventsender.mfs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.client.BasicMfs;
import com.abcxyz.messaging.mfs.client.PrivateEntryDB;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * This class is used by the EventManagerWorkerNoSQL.
 * <p>
 * NtfNotifierEventHandlerNoSQL receives events from the persistent scheduler. It enqueues
 * slam down timeout events to the Event Manager Worker.
 * </p>
 *
 */
class NtfNotifierEventHandlerNoSQL extends NtfNotifierEventHandler {

    private static BasicMfs mfsClient = BasicMfs.getInstance(CommonOamManager.getInstance().getMfsOam());
    private static final ILogger logger = ILoggerFactory.getILogger(NtfNotifierEventHandlerNoSQL.class);

    /**
     * Constructs a NTF notifier handler associated to an event manager worker.
     * @param mfsEventManager MfsEventManager
     * @param timeoutEventType String 
     * @param aggregationNumberOfExpiryRetries Number of retries (if the aggregation failed and must retry)
     */
    NtfNotifierEventHandlerNoSQL(MfsEventManager mfsEventManager, String timeoutEventType, int aggregationNumberOfExpiryRetries) {
        super(mfsEventManager, timeoutEventType, aggregationNumberOfExpiryRetries);
    }

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
                PrivateEntryDB entry = PrivateEntryDB.parsePrivateForDB(fileName);
                ByteArrayInputStream is = mfsClient.getPrivateFileAsInputStream(entry.msa, entry.msgclass, entry.key1, entry.key2, entry.artifactname);
                reader = new BufferedReader(new InputStreamReader(is));
                //reader = new BufferedReader(new FileReader(fileName));
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

                        boolean updatedSuccessfully = NtfNotifierEventHandlerNoSQL.updatePersistentFileEventId(entry.msa, entry.key1, entry.artifactname, nextEventId, isFileContainEventId);
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

    public static boolean updatePersistentFileEventId(MSA msa, String notificationNumber, String fileName, String nextEventId, boolean isFileContainEventId) {
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
                    ByteArrayInputStream is = mfsClient.getPrivateFileAsInputStream(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber, MfsEventManager.EVENTS_DIRECTORY, fileName);
                    reader = new BufferedReader(new InputStreamReader(is));
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
                    mfsClient.addPrivate(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber, MfsEventManager.EVENTS_DIRECTORY, fileName, newFileContent);

                    fileUpdatedSuccessfully = true;

                } catch (Exception ioe) {
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


    public RenameFileResult renameFile(MSA msa, String notificationNumber, String sourceFile) {
        int tried = 0;
        boolean fileRenamedSuccessfully = false;
        String destinationFileName = null;
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
                    destinationFileName = sourceFile + MfsEventManager.TOKEN_SEPARATOR + MfsEventManager.dateFormat.get().format(new Date());

                    mfsClient.renamePrivateArtifact(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber, MfsEventManager.EVENTS_DIRECTORY, sourceFile, destinationFileName);
                    logger.debug("File " + sourceFile + " renamed to " + destinationFileName);
                    
                    renameFileResult.setResponse(
                            PrivateEntryDB.buildFilename(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber,  MfsEventManager.EVENTS_DIRECTORY, destinationFileName),
                            RenameFileResultType.RENAME_FILE_RESULT_TYPE_OK);
                    fileRenamedSuccessfully = true;

                } catch (Exception e) {
                    logger.warn("Exception in renameFile", e);
                    renameFileResult.setResponse(
                            PrivateEntryDB.buildFilename(msa, MfsEventManagerNoSQL.MOIP_CLASS, notificationNumber,  MfsEventManager.EVENTS_DIRECTORY, destinationFileName),
                            RenameFileResultType.RENAME_FILE_RESULT_TYPE_NONE);
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
}
