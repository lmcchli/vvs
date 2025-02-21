package com.mobeon.common.trafficeventsender.mfs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.client.BasicMfs;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.mfs.NtfNotifierEventHandler.RenameFileResult;
import com.mobeon.common.trafficeventsender.mfs.NtfNotifierEventHandler.RenameFileResultType;

/**
 * Worker thread that saves the MCN events into the user's event file.
 */
class McnEventManagerWorkerNoSQL extends McnEventManagerWorker {

    private static BasicMfs mfsClient = BasicMfs.getInstance(CommonOamManager.getInstance().getMfsOam());
    private static final ILogger logger = ILoggerFactory.getILogger(McnEventManagerWorkerNoSQL.class);

    private NtfNotifierEventHandlerNoSQL mcnNotifierNoSQL;

    McnEventManagerWorkerNoSQL(String threadName, NtfNotifierEventHandlerNoSQL mcnNotifierNoSQL) {
        super(threadName, null);
        this.mcnNotifierNoSQL = mcnNotifierNoSQL;
    }


    protected BufferInfo storeEventPerUser(IncomingEventEntry entry) {
        Object perf = null;
        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("BKD.CMA.TrES.McnEMW.storeEventPerUser");
            }

            logger.debug(this.getName() + " is processing " + entry.getEvent().getName() + " for " + entry.getEvent().getProperties());

            TrafficEvent trafficEvent = entry.getEvent();
            BufferInfo bufferInfo = new BufferInfo(new StringBuilder(), entry.getPhoneNumber(), trafficEvent.getName());
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

            MSA msa = MfsEventManagerNoSQL.getMSA(bufferInfo.calledNumber);
            String target = msa.toString() + " in private MOIP events for number " + bufferInfo.calledNumber + " with name " + bufferInfo.eventFilePath;		    	
            ByteArrayInputStream is = null;
            try {
                is = mfsClient.getPrivateFileAsInputStream(msa, MfsEventManagerNoSQL.MOIP_CLASS, bufferInfo.calledNumber, MfsEventManager.EVENTS_DIRECTORY, bufferInfo.eventFilePath);
            } catch (Exception e1) {
                logger.error("Failed to retrieve data from " + target);
                return;
            }

            int originalCallCount = 0;
            if (is != null) {
                originalCallCount = countEvents(new InputStreamReader(is), target);
            }
            int bufferCallCount = bufferInfo.numberOfCalls;

            try {
                String buffer = bufferInfo.buffer.toString();
                boolean mustNotify = originalCallCount + bufferCallCount >= eventThreshold;
                String storedEventId = null;

                if (!mustNotify) {
                    if (is != null) {
                        logger.debug("Store Mcn event for " + bufferInfo.calledNumber + ", must not notify and file already exists, append.");

                        // Append the new call line(s) to the existing file
                        mfsClient.appendToPrivate(msa, MfsEventManagerNoSQL.MOIP_CLASS, bufferInfo.calledNumber, MfsEventManager.EVENTS_DIRECTORY, bufferInfo.eventFilePath, buffer);
                    } else {
                        logger.debug("Store Mcn event for " + bufferInfo.calledNumber + ", must not notify and file does not exist, create new file.");

                        /**
                         * When receiving the first Mcn call for a subscriber/non-subscriber, a timeout for notifying NTF
                         * must be started (except if the count has already reached the threshold, no aggregation timer is needed). 
                         */
                        Properties properties = new Properties();
                        properties.setProperty(MoipMessageEntities.MCN_CALLED_NUMBER_PROPERTY, bufferInfo.calledNumber);
                        properties.setProperty(MoipMessageEntities.MCN_EVENT_FILE_PROPERTY, bufferInfo.eventFilePath);

                        AppliEventInfo eventInfo = mcnNotifierNoSQL.scheduleEvent(calculateWhen(), generateUniqueId(bufferInfo.calledNumber), EventTypes.INTERNAL_TIMER.getName(), properties);
                        logger.debug("Scheduled event: " + eventInfo.getEventId());

                        // Append the subscriber/non-subscriber number
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(MoipMessageEntities.MCN_CALLED_NUMBER_PROPERTY + MfsEventManager.KEY_VALUE_SEPARATOR + bufferInfo.calledNumber + MfsEventManager.PROPERTY_SEPARATOR);

                        // Append the eventId
                        stringBuffer.append(MoipMessageEntities.MCN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR + eventInfo.getEventId() + '\n');

                        // Append the calls
                        stringBuffer.append(buffer);

                        mfsClient.appendToPrivate(msa, MfsEventManagerNoSQL.MOIP_CLASS, bufferInfo.calledNumber, MfsEventManager.EVENTS_DIRECTORY, bufferInfo.eventFilePath, stringBuffer.toString());
                    }
                } else {
                    if (is != null) {
                        logger.debug("Store Mcn event for " + bufferInfo.calledNumber + ", must notify and file already exists, cancel eventId and notify.");

                        // Read the eventId stored in the existing file and cancel it
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                        String firstLine = reader.readLine();
                        reader.close();
                        if (firstLine != null) {
                            int indexOf = firstLine.indexOf(MoipMessageEntities.MCN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR);
                            storedEventId = firstLine.substring(indexOf + (MoipMessageEntities.MCN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR).length());
                        }

                        // Append the calls to the existing file
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(buffer);

                        mfsClient.appendToPrivate(msa, MfsEventManagerNoSQL.MOIP_CLASS, bufferInfo.calledNumber, MfsEventManager.EVENTS_DIRECTORY, bufferInfo.eventFilePath, stringBuffer.toString());

                    } else {
                        logger.debug("Store Mcn event for " + bufferInfo.calledNumber + ", must notify and file does not exist, notify.");

                        // Create new file
                        StringBuffer stringBuffer = new StringBuffer();

                        // Adding the subscriber/non-subscriber number
                        stringBuffer.append(MoipMessageEntities.MCN_CALLED_NUMBER_PROPERTY + MfsEventManager.KEY_VALUE_SEPARATOR + bufferInfo.calledNumber + MfsEventManager.PROPERTY_SEPARATOR);

                        // Adding an empty eventId
                        stringBuffer.append(MoipMessageEntities.MCN_EVENT_ID + MfsEventManager.KEY_VALUE_SEPARATOR + '\n');

                        // Adding the calls
                        stringBuffer.append(buffer);

                        mfsClient.appendToPrivate(msa, MfsEventManagerNoSQL.MOIP_CLASS, bufferInfo.calledNumber, MfsEventManager.EVENTS_DIRECTORY, bufferInfo.eventFilePath, stringBuffer.toString());
                    }

                    /**
                     * When the event threshold has been reached, the event file is renamed with a time stamp
                     * and NTF is notified that an event file is ready with the renamed file.
                     * If the renaming of the file is successful, notify NTF, otherwise wait for the Scheduler to retry the renaming.
                     * No reason to notify NTF without a stamped file since NTF will not look at it.
                     * Even if the file is stored under notification number, NTF still gets notified with subscriber number.
                     */
                    notifyNtf(msa, storedEventId, bufferInfo.calledNumber, bufferInfo.eventFilePath);
                }

            } catch (IOException ioe) {
                logger.warn("IOException for " + bufferInfo.calledNumber, ioe);
            } catch (Exception e) {
                logger.warn("Exception for " + bufferInfo.calledNumber, e);
            }
        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }
    }


    private void notifyNtf(MSA msa, String storedEventId, String telephoneNumber, String filename) {
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
            RenameFileResult renameFileResult = mcnNotifierNoSQL.renameFile(msa, telephoneNumber, filename);
            if (RenameFileResultType.RENAME_FILE_RESULT_TYPE_OK.equals(renameFileResult.getRenameFileResultType())) {

                MessageInfo msgInfo = new MessageInfo();
                msgInfo.rmsa = msa;
                Properties properties = new Properties();
                properties.setProperty(MoipMessageEntities.MCN_EVENT_FILE_PROPERTY, renameFileResult.getDestinationFilename());
                properties.setProperty(MoipMessageEntities.MCN_NOTIFICATION_NUMBER_PROPERTY, telephoneNumber);

                MfsEventManager.getMfs().notifyNtf(EventTypes.MCN, msgInfo, telephoneNumber, properties);

                if (storedEventId != null && !storedEventId.isEmpty()) {
                    AppliEventInfo eventInfo = new AppliEventInfo();
                    eventInfo.setEventId(storedEventId);
                    mcnNotifierNoSQL.cancelEvent(eventInfo);
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
                    properties.setProperty(MoipMessageEntities.MCN_CALLED_NUMBER_PROPERTY, telephoneNumber);
                    properties.setProperty(MoipMessageEntities.MCN_EVENT_FILE_PROPERTY, filename);

                    AppliEventInfo eventInfo = mcnNotifierNoSQL.scheduleEvent(calculateWhen(), generateUniqueId(telephoneNumber), EventTypes.INTERNAL_TIMER.getName(), properties);
                    logger.debug("Scheduled event: " + eventInfo.getEventId());

                    boolean updatedSuccessfully = NtfNotifierEventHandlerNoSQL.updatePersistentFileEventId(msa, telephoneNumber, filename, eventInfo.getEventId(), true);
                    if (updatedSuccessfully) {
                        logger.debug("Scheduled event " + eventInfo.getEventId() + " for sourceFile " + filename + ", will retry");
                    } else {
                        logger.error("Unable to rename " + filename + " and to schedule event/update storage, dropping the MCN event");
                        mcnNotifierNoSQL.cancelEvent(eventInfo);
                    }
                }

            } else {
                logger.error("Unable to rename " + filename + " and to schedule event/update storage, dropping the MCN event");

                if (storedEventId != null && !storedEventId.isEmpty()) {
                    AppliEventInfo eventInfo = new AppliEventInfo();
                    eventInfo.setEventId(storedEventId);
                    mcnNotifierNoSQL.cancelEvent(eventInfo);
                }
            }

        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }
    }

    protected void notifyNtf(TimeoutEventEntry entry) {
        Properties properties = entry.getProperties();
        String telephoneNumber = properties.getProperty(MoipMessageEntities.MCN_CALLED_NUMBER_PROPERTY);
        String filePath = properties.getProperty(MoipMessageEntities.MCN_EVENT_FILE_PROPERTY);
        MSA msa = MfsEventManagerNoSQL.getMSA(telephoneNumber);
        notifyNtf(msa, entry.getNextEventId(), telephoneNumber, filePath);
    }


    public ILogger getLogger() {
        return logger;
    }
}