package com.mobeon.common.trafficeventsender.mfs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.MsgStoreServer;
import com.abcxyz.messaging.mfs.client.BasicMfs;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.IdGenerationException;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.MfsClient;
import com.mobeon.common.trafficeventsender.MfsConfiguration;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderConfiguration;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException.TrafficEventSenderExceptionCause;

public class MfsEventManagerNoSQL extends MfsEventManager {

    private static BasicMfs mfsClient = BasicMfs.getInstance(CommonOamManager.getInstance().getMfsOam());
    private static final ILogger logger = ILoggerFactory.getILogger(MfsEventManagerNoSQL.class);

    public static final String MOIP_CLASS = "moip";

    private static Boolean eventManagerWorkersStarted = false;
    private static EventManagerWorkerNoSQL[] eventManagerWorkersNoSQL;
    private static Boolean mcnEventManagerWorkersStarted = false;
    private static McnEventManagerWorkerNoSQL[] mcnEventManagerWorkersNoSQL;

    private NtfNotifierEventHandlerNoSQL slamdownNotifierNoSQL;
    private NtfNotifierEventHandlerNoSQL mcnNotifierNoSQL;

    public MfsEventManagerNoSQL() {
        super();
    }

    public MfsEventManagerNoSQL(CommonMessagingAccess cma, IDirectoryAccess da) {
        super(cma, da);
    }

    public void initSlamdownWorkers() {
        if (!eventManagerWorkersStarted) {
            synchronized(eventManagerWorkersStarted) {
                if (!eventManagerWorkersStarted) {
                    ConfigManager localConfig = CommonOamManager.getInstance().getLocalConfig();
                    MfsConfiguration mfsConfiguration = TrafficEventSenderConfiguration.getInstance().getMfsConfiguration();

                    // Create the SlamdownEvent scheduler
                    String slamdownAggregationExpiryIntervalInMin = localConfig.getParameter(MoipMessageEntities.slamdownAggregExpiryIntervalInMin);
                    int slamdownAggregationNumberOfExpiryRetries = localConfig.getIntValue(MoipMessageEntities.slamdownAggregExpiryRetries);

                    RetryEventInfo info = new RetryEventInfo(MfsEventManager.SLAMDOWN_EVENT_SERVICE_NAME);
                    info.setEventRetrySchema(slamdownAggregationExpiryIntervalInMin + "m CONTINUE");
                    slamdownNotifierNoSQL = new NtfNotifierEventHandlerNoSQL(this, MfsEventManager.SLAMDOWN_EVENT_SERVICE_NAME, slamdownAggregationNumberOfExpiryRetries);
                    slamdownNotifierNoSQL.start(info);

                    // Create the EventManagerWorkers
                    numberOfSlamdownWorkers = mfsConfiguration.getSlamdownWorkers();
                    eventManagerWorkersNoSQL = new EventManagerWorkerNoSQL[numberOfSlamdownWorkers];
                    for (int i=0; i<numberOfSlamdownWorkers; i++) {
                        eventManagerWorkersNoSQL[i] = new EventManagerWorkerNoSQL("EventManagerWorkerNoSQL-" + i, slamdownNotifierNoSQL);
                        eventManagerWorkersNoSQL[i].setDaemon(true);
                        eventManagerWorkersNoSQL[i].start();
                    }
                    logger.info("initSlamdownWorkers() : " + numberOfSlamdownWorkers + " eventManagerWorkersNoSQL started");
                    eventManagerWorkersStarted = true;
                }
            }
        }
    }

    public void initMcnWorkers() {
        if (!mcnEventManagerWorkersStarted) {
            synchronized (mcnEventManagerWorkersStarted) {
                if (!mcnEventManagerWorkersStarted) {
                    ConfigManager localConfig = CommonOamManager.getInstance().getLocalConfig();
                    MfsConfiguration mfsConfiguration = TrafficEventSenderConfiguration.getInstance().getMfsConfiguration();

                    // Create the McnEvent scheduler
                    String mcnAggregationExpiryIntervalInMin = localConfig.getParameter(MoipMessageEntities.mcnAggregExpiryIntervalInMin);
                    int mcnAggregationNumberOfExpiryRetries = localConfig.getIntValue(MoipMessageEntities.mcnAggregExpiryRetries);

                    RetryEventInfo info = new RetryEventInfo(MfsEventManager.MCN_EVENT_SERVICE_NAME);
                    info.setEventRetrySchema(mcnAggregationExpiryIntervalInMin + "m CONTINUE");
                    mcnNotifierNoSQL = new NtfNotifierEventHandlerNoSQL(this, MfsEventManager.MCN_EVENT_SERVICE_NAME, mcnAggregationNumberOfExpiryRetries);       
                    mcnNotifierNoSQL.start(info);

                    // Create the McnEventManagerWorkers
                    numberOfMcnWorkers = mfsConfiguration.getMcnWorkers();
                    mcnEventManagerWorkersNoSQL = new McnEventManagerWorkerNoSQL[numberOfMcnWorkers];
                    for (int i=0; i<numberOfMcnWorkers; i++) {
                        mcnEventManagerWorkersNoSQL[i] = new McnEventManagerWorkerNoSQL("McnEventManagerWorkerNoSQL-" + i, mcnNotifierNoSQL);
                        mcnEventManagerWorkersNoSQL[i].setDaemon(true);
                        mcnEventManagerWorkersNoSQL[i].start();
                    }
                    logger.info("initMcnWorkers() : " + numberOfMcnWorkers + " mcnEventManagerWorkersNoSQL started");
                    mcnEventManagerWorkersStarted = true;
                }
            }
        }
    }

    @Override
    public TrafficEvent[] retrieveEvents(String phoneNumber, String name, boolean internal) throws TrafficEventSenderException {
        return retrieveEventsInternal(phoneNumber, name, internal);
    }


    /**
     * 
     * @param uid  The identity from which the MSA must be retrieved.
     * @return MSA, or null if an exception occurred.
     */
    protected static MSA getMSA(String uid) {
        String msid = getMSID(uid);
        MSA msa = null;

        try {
            if (msid != null) {
                msa = new MSA(msid, true);
            } else {
                msa = MsgStoreServer.getMSA(uid, false);
            }
        } catch (MsgStoreException e) {
        }

        return msa;
    }


    /**
     * 
     * @param uid  The identity from which the MSA must be retrieved.
     * @param internal  Indicates if the MSA must be built from the "internal" "msid:"
     *                  or built from the "external" "eid:"
     * @return MSA, or null if the subscriber is not found (when the provided {@code internal}
     *         is true, or null when an exception occurred.
     */
    protected static MSA getMSA(String uid, boolean internal) {
        String msid = null;
        MSA msa = null;

        try {
            if (internal) {
                msid = getMSID(uid);
                if (msid != null) {
                    msa = new MSA(msid, true);
                }
            } else {
                msa = MsgStoreServer.getMSA(uid, false);
            }
        } catch (MsgStoreException e) {
        }

        return msa;
    }


    private TrafficEvent[] retrieveEventsInternal(String telephoneNumber, String fileName, boolean internal) throws TrafficEventSenderException {
        logger.debug("retrieve Events Internal for telephoneNumber=" + telephoneNumber + ", filename=" + fileName);
        String uid = telephoneNumber;//uid is used to access backend DB
        if(telephoneNumber.charAt(0) == '+') {
            uid = telephoneNumber.substring(1);
            logger.debug("remove the prefix +");
        }

        MSA msa = getMSA(telephoneNumber, internal);
        if (msa == null) {
            String msg = "Could not find MSA for phoneNumber " + telephoneNumber + " [" + internal + "]";
            logger.error(msg);
            throw new TrafficEventSenderException(msg);
        }

        String target = msa.toString() + " in private MOIP events for number " + uid + " with name " + fileName;

        ByteArrayInputStream is = null;
        try {
            is = mfsClient.getPrivateFileAsInputStream(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName);
        } catch (Exception e) {
            String msg = "Cannot open event file for " + target;
            logger.error(msg);
            throw new TrafficEventSenderException(msg, TrafficEventSenderExceptionCause.PAYLOAD_FILE_NOT_ACCESSIBLE);
        }

        if (is != null) {
            return retrieveEventsInternal(new InputStreamReader(is), target, fileName);
        } else {
            String msg = "File not found for " + target;
            logger.error(msg);
            throw new TrafficEventSenderException(msg, TrafficEventSenderExceptionCause.PAYLOAD_FILE_DOES_NOT_EXIST);
        }

    }

    @Override
    public void storeEvent(String telephoneNumber, TrafficEvent event)
            throws TrafficEventSenderException {
        logger.debug("store Event for telephoneNumber=" + telephoneNumber + ", eventname=" + event.getName());

        if (event.getName().equals(MfsClient.EVENT_MWIOFF)) {
            MessageInfo msgInfo = new MessageInfo();
            msgInfo.rmsa = new MSA(getMSID(telephoneNumber));
            Properties  properties = new Properties();
            properties.putAll(event.getProperties());
            mfs.notifyNtf(EventTypes.MWI_OFF, msgInfo, telephoneNumber, properties);
        } else if (event.getName().equals(MfsClient.EVENT_MAILBOXUPDATE)) {
            MessageInfo msgInfo = new MessageInfo();
            msgInfo.rmsa = new MSA(getMSID(telephoneNumber));
            Properties  properties = new Properties();
            properties.putAll(event.getProperties());
            mfs.notifyNtf(EventTypes.MAILBOX_UPDATE, msgInfo, telephoneNumber, properties);

        } else if (event.getName().equals(MfsClient.EVENT_MISSEDCALLNOTIFICATION)) {
            // Create the mcn worker thread if not already created.
            initMcnWorkers();

            logger.debug("MfsEventManager: MISSEDCALLNOTIFICATION " );

            try {
                // Store the event in the appropriate worker thread (based on the telephone number)
                int workerId = getWorkerIdFromTelephoneNumber(telephoneNumber, MfsEventManager.MCN_EVENT_SERVICE_NAME);
                logger.debug("Adding McnInformation event for " + telephoneNumber + " to McnEventManagerWorker-" + workerId);
                mcnEventManagerWorkersNoSQL[workerId].enqueue(new IEventManagerWorker.IncomingEventEntry(telephoneNumber, event));

            } catch (IllegalStateException e) {
                logger.error("Cannot store MCN traffic event for " + telephoneNumber + ", ", e);
                throw new TrafficEventSenderException(e);
            }

        } else if (event.getName().equals(MfsClient.EVENT_SLAMDOWNINFORMATION)) {
            // Create Slamdown worker threads (if not already created)
            initSlamdownWorkers();

            try {
                // Store the event in the appropriate worker thread (based on the telephone number)
                int workerId = getWorkerIdFromTelephoneNumber(telephoneNumber, MfsEventManager.SLAMDOWN_EVENT_SERVICE_NAME);
                logger.debug("Adding SlamdownInformation event for " + telephoneNumber + " to EventManagerWorker-" + workerId);
                eventManagerWorkersNoSQL[workerId].enqueue(new IEventManagerWorker.IncomingEventEntry(telephoneNumber, event));

            } catch (IllegalStateException e) {
                logger.error("Cannot store Slamdown traffic event for " + telephoneNumber + ", ", e);
                throw new TrafficEventSenderException(e);
            }
        }
        else if (event.getName().equals(MfsClient.EVENT_DELAYEDEVENT)) {
            MessageInfo msgInfo = new MessageInfo();
            msgInfo.rmsa = new MSA(getMSID(telephoneNumber));
            Properties properties = new Properties();
            properties.putAll(event.getProperties());
            mfs.notifyNtf(EventTypes.DELAYED_EVENT, msgInfo, telephoneNumber, properties);
        }
        // Case - Unknown notification type - just send to NTF
        else {
            String eventName = event.getName();
            logger.debug("Sending SendNotification event to NTF. eventName: " + eventName);
            MessageInfo msgInfo = new MessageInfo();
            String msid = getMSID(telephoneNumber);
            // Could not get subscriber -> verify if non-subscriber
            if (msid != null) {
                msgInfo.rmsa = new MSA(msid);
            } else {
                String eid;
                try {
                    eid = MFSFactory.getEid(telephoneNumber);
                } catch (IdGenerationException ige) {
                    logger.error("Could not generate eid from telephoneNumber " + telephoneNumber);
                    throw new TrafficEventSenderException(ige);
                }
                msgInfo.rmsa = new MSA(eid, false);
            }

            Properties properties = new Properties();
            properties.putAll(event.getProperties());

            // Add date property if not already provided
            String formattedDate = properties.getProperty(DATE_KEY);
            if (formattedDate == null || formattedDate.isEmpty()) {
                Long dateInMilliseconds = Calendar.getInstance().getTimeInMillis();
                formattedDate = MfsEventManager.dateFormat.get().format(new Date(dateInMilliseconds));
                logger.debug("No " + DATE_KEY + " property found in event " + eventName + ", generated date " + formattedDate + " (" + dateInMilliseconds + "ms) will be used");
                properties.setProperty(DATE_KEY, Long.toString(dateInMilliseconds));
            }

            if (event.hasPayload()) {
                String filename = eventName + "_" + formattedDate + "." + PAYLOAD_FILE_EXTENSION;
                String payload = event.getPayload();
                String payloadType = properties.getProperty(PAYLOAD_TYPE_KEY);
                PayloadType type = PayloadType.valueOf(payloadType.toUpperCase());
                logger.debug("Attempting to store payload. eventName: " + eventName + ", telephoneNumber: " + telephoneNumber +
                        ", filename: " + filename + ", payload: " + payload + "payloadType: " + type);
                storePayload(telephoneNumber, filename, type, payload);
                // Place payload filename in properties to send to NTF
                properties.put(PAYLOAD_FILENAME_KEY, filename);
            }

            // Place event name in properties
            properties.put(SERVICE_TYPE_KEY, eventName);

            // Notify NTF
            mfs.notifyNtf(EventTypes.SEND_NOTIFICATOIN_TYPE, msgInfo, telephoneNumber, properties);
        }
    }

    /**
     * This method injects a timed out event to it's appropriate ManagerWorker.
     * It also routes this event to a given workerId based on the subscriber number.
     * @param timeoutEventType String
     * @param properties Properties
     */
    public void injectTimeoutEvent(String timeoutEventType, Properties properties, String nextEventId) {
        String subscriberNumber = properties.getProperty(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY);
        int workerId = getWorkerIdFromTelephoneNumber(subscriberNumber, timeoutEventType);

        try {
            if (timeoutEventType.equalsIgnoreCase(MfsEventManager.SLAMDOWN_EVENT_SERVICE_NAME)) {
                logger.debug("Adding SlamdownTimeout event for " + subscriberNumber + " to EventManagerWorker-" + workerId);
                eventManagerWorkersNoSQL[workerId].enqueue(new IEventManagerWorker.TimeoutEventEntry(properties, nextEventId));
            } else if (timeoutEventType.equalsIgnoreCase(MfsEventManager.MCN_EVENT_SERVICE_NAME)) {
                logger.debug("Adding McnTimeout event for " + subscriberNumber + " to McnEventManagerWorker-" + workerId);
                mcnEventManagerWorkersNoSQL[workerId].enqueue(new IEventManagerWorker.TimeoutEventEntry(properties, nextEventId));
            } else {
                logger.error("Invalid timeoutEventType: " + timeoutEventType + ", Properties: " + properties);
            }
        } catch (IllegalStateException ise) {
            logger.error("Cannot store " + timeoutEventType + " timeout event: ", ise);
        }
    }
    
    
    // Seems these methods are not used... TBD.
    //
    // TODO : public String[] getEventFiles(String telephoneNumber, final String eventName) throws TrafficEventSenderException {
    // TODO : String[] getSendStatusEventFiles(String telephoneNumber, final String eventName, final String order) throws TrafficEventSenderException {
    // TODO : public String getFileNameByExtension(String telephoneNumber, String eventName, final String extension) {

    @Override
    public String[] getEventFiles(String telephoneNumber, final String eventName) throws TrafficEventSenderException {
        throw new TrafficEventSenderException("not implemented in class MfsEventManagerNoSQL");
    }

    public File[] getEventFiles(String telephoneNumber, FileFilter fileFilter) {
        logger.debug("get Event Files for telephoneNumber=" + telephoneNumber);
        String uid = telephoneNumber;
        if(telephoneNumber.charAt(0) == '+') {
            uid = telephoneNumber.substring(1);
            logger.debug("remove the prefix +");
        }

        File[] fileArray = null;
        MSA msa = getMSA(telephoneNumber);
        if (msa == null) {
            String msg = "Could not find MSA for phoneNumber " + telephoneNumber;
            logger.error(msg);
            return null;
        }

        String target = msa.toString() + " in private MOIP events for number " + uid;

        try {
            List<String> files = mfsClient.getPrivateFiles(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileFilter);
            if(files == null || files.size() == 0) {
                return null;
            }
            fileArray = new File[files.size()];

            EventKeys keys = new EventKeys();
            keys.msa = msa;
            keys.internal = msa.isInternal();
            keys.msgClass = MOIP_CLASS;
            keys.telephoneNumber = uid;

            for (int i = 0; i < files.size(); i++) {
                keys.filename = files.get(i);
                String createdPath = createFilePath(keys);
                fileArray[i] = new File(createdPath);
                logger.debug("getEventFiles : createdPath=[" + createdPath + "] fileArray[" + i + "]=[" + fileArray[i].toString()+ "]");
            }
        } catch (Exception e) {
            String msg = "Exception trying to getEventFiles files for " + target; 
            logger.error(msg, e);
        }
        return fileArray;
    }



    @Override
    public long acquireLockFile(String telephoneNumber, String fileName, int validityPeriodInSeconds, boolean internal) throws TrafficEventSenderException {
        logger.debug(" acquire Lock File for telephoneNumber= " + telephoneNumber + ", filename=" + fileName);
        String uid = telephoneNumber;
        if(telephoneNumber.charAt(0) == '+') {
            uid = telephoneNumber.substring(1);
            logger.debug("remove the prefix +");
        }

        MSA msa = getMSA(telephoneNumber, internal);
        if (msa == null) {
            String msg = "Could not find MSA for phoneNumber " + telephoneNumber + " [" + internal + "]";
            logger.error(msg);
            throw new TrafficEventSenderException(msg, TrafficEventSenderExceptionCause.PAYLOAD_FILE_PATH_NOT_ACCESSIBLE);
        }

        String target = msa.toString() + " in private MOIP events for number " + uid + " with name " + fileName;
        long lockId = 0;

        try {
            boolean created = mfsClient.addPrivateIfNotExist(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName, "empty");

            if (created) {
                lockId = mfsClient.getPrivateFileTimeStamp(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName);
                logger.debug("LockFile " + target + " aquired (by creating a new lock file) with lockid = " + lockId);
            } else {

                // File already exists, lookup the last modified date for validity period (if specified)
                if (validityPeriodInSeconds != 0) {
                    long validityPeriodInMilliseconds = validityPeriodInSeconds * 1000;
                    long currentLockId = mfsClient.getPrivateFileTimeStamp(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName);
                    boolean fileExistsAndStillValid = (currentLockId + validityPeriodInMilliseconds) > Calendar.getInstance().getTimeInMillis();
                    if (fileExistsAndStillValid) {
                        logger.debug("LockFile " + target + " not aquired (already a valid lock file found within the validity period)");
                    } else {
                        // Force time stamp. Need to get the content of the existing file, before resetting the content again and getting the new timestamp.
                        ByteBuffer bb = mfsClient.getPrivateFileAsByteBuffer(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName);
                        mfsClient.addPrivate(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName, bb);
                        lockId = mfsClient.getPrivateFileTimeStamp(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName);

                        logger.debug("LockFile " + target + " aquired (by replacing an obsolete lock file) with lockid = " + lockId);
                    }
                } else {
                    logger.debug("LockFile " + target + " not aquired (already a valid lock file found - no validity period verification) for telephone number " + telephoneNumber);
                }

            }

        } catch (Exception e) {
            String msg = "Exception trying to aquire lock file " + target; 
            logger.error(msg, e);
            throw new TrafficEventSenderException(msg);
        }

        return lockId;
    }


    /**
     * Release a lock file (empty file) for a given telephone number.
     * 
     * @param telephoneNumber Subscriber or non-subscriber number
     * @param fileName Lock file name
     * @param internal MIO Subscriber or non-MIO subscriber
     * @param lockId long Provided when the lock was acquired.  A lock cannot be removed if the lockId does not match. 
     * @throws TrafficEventSenderException if either filePath creation fails or IOException
     */
    @Override
    public void releaseLockFile(String telephoneNumber, String fileName, long lockId, boolean internal) throws TrafficEventSenderException {
        if (lockId == 0L) { return; }
        logger.debug(" release Lock File for telephoneNumber= " + telephoneNumber + ", filename=" + fileName);
        String uid = telephoneNumber;
        if(telephoneNumber.charAt(0) == '+') {
            uid = telephoneNumber.substring(1);
            logger.debug("remove the prefix +");
        }

        MSA msa = getMSA(telephoneNumber, internal);
        if (msa == null) {
            String msg = "Could not find MSA for phoneNumber " + telephoneNumber + " [" + internal + "]";
            logger.error(msg);
            throw new TrafficEventSenderException(msg);
        }

        String target = msa.toString() + " in private MOIP events for number " + uid + " with name " + fileName;

        long lastModified;
        try {
            lastModified = mfsClient.getPrivateFileTimeStamp(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName);

            if (lastModified != 0L) {
                if (lastModified == lockId) {
                    mfsClient.deletePrivate(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName);
                    logger.debug("Lock file " + target + " deleted with lastModified = " + lastModified);
                } else {
                    logger.debug("Lock file " + target + " found (" + lastModified + ") does not match the provided lockId (" + lockId + ")");
                }
            }

        } catch (Exception e) {
            String msg = "Failed to delete lock file: " + target; 
            logger.error(msg);
            throw new TrafficEventSenderException(msg);
        }
    }


    @Override
    public void createEmptyFile(String telephoneNumber, String fileName) throws TrafficEventSenderException {
        logger.debug(" create empty File for telephoneNumber= " + telephoneNumber + ", filename=" + fileName);
        String uid = telephoneNumber;
        if(telephoneNumber.charAt(0) == '+') {
            uid = telephoneNumber.substring(1);
            logger.debug("remove the prefix +");
        }

        MSA msa = getMSA(telephoneNumber, true);
        if (msa == null) {
            String msg = "Could not find MSA for phoneNumber " + telephoneNumber;
            logger.error(msg);
            throw new TrafficEventSenderException(msg);
        }

        String target = msa.toString() + " in private MOIP events for number " + uid + " with name " + fileName;

        try {
            mfsClient.addPrivate(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName, "");
        } catch (Exception e) {
            String msg = "Failed to create empty file for " + target;
            logger.error(msg, e);
            throw new TrafficEventSenderException(msg);
        }
    }


    // TODO : Add "append" option for the "addPrivate" method.

    @Override
    public void createPropertiesFile(String telephoneNumber, String prefix, Map<String, String> properties) throws TrafficEventSenderException {
        logger.debug(" create Properties File for telephoneNumber= " + telephoneNumber + ", prefix=" + prefix);
        String uid = telephoneNumber;
        if(telephoneNumber.charAt(0) == '+') {
            uid = telephoneNumber.substring(1);
            logger.debug("remove the prefix +");
        }

        MSA msa = getMSA(telephoneNumber, true);
        if (msa == null) {
            String msg = "Could not find MSA for phoneNumber " + telephoneNumber;
            logger.error(msg);
            throw new TrafficEventSenderException(msg);
        }
        String fileName = prefix + MfsEventManager.TOKEN_SEPARATOR + MfsEventManager.dateFormat.get().format(new Date());
        String target = msa.toString() + " in private MOIP events for number " + uid + " with name " + fileName;

        Set<Map.Entry<String, String>> entries = properties.entrySet();
        StringBuilder buffer=new StringBuilder();
        for (Map.Entry<String, String> property : entries) {
            buffer.append(property.getKey() +
                    MfsEventManager.KEY_VALUE_SEPARATOR +
                    property.getValue() +
                    MfsEventManager.PROPERTY_SEPARATOR);
        }
        buffer.append('\n');

        try {
            mfsClient.addPrivate(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName, buffer.toString());
        } catch (Exception e) {
            String msg = "Failed to create properties file for " + target;
            logger.error(msg, e);
            throw new TrafficEventSenderException(msg);
        }
    }

    public boolean removeFile(String telephoneNumber, String fileName) throws TrafficEventSenderException {
        boolean internal = isInternal(telephoneNumber);
        return deleteFile(telephoneNumber, fileName, internal);
    }

    public boolean removeFile(String telephoneNumber, String fileName, boolean internal) throws TrafficEventSenderException {
        return deleteFile(telephoneNumber, fileName, internal);
    }

    @Override
    public boolean removeFile(String absoluteFilePath) throws TrafficEventSenderException {
        if (absoluteFilePath == null) {
            return true;
        }

        EventKeys keys = extractFromFilePath(absoluteFilePath);
        return deleteFile(keys.telephoneNumber, keys.filename, keys.internal);
    }

    public boolean deleteFile(String telephoneNumber, String fileName, boolean internal) throws TrafficEventSenderException {
        boolean result = true;
        String uid = telephoneNumber;
        logger.debug("delete file for telephoneNumber=" + telephoneNumber + ", filename=" + fileName);
        if(telephoneNumber.charAt(0) == '+') {
            uid = telephoneNumber.substring(1);
            logger.debug("remove the prefix +");
        }

        MSA msa = getMSA(telephoneNumber, internal);
        if (msa == null) {
            String msg = "Could not find MSA for phoneNumber " + telephoneNumber;
            logger.error(msg);
            throw new TrafficEventSenderException(msg);
        }

        String target = msa.toString() + " in private MOIP events for number " + uid + " with name " + fileName;

        try {
            mfsClient.deletePrivate(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName);
        } catch (Exception e) {
            String msg = "Failed to delete file for " + target;
            logger.error(msg, e);
            throw new TrafficEventSenderException(msg);
        }
        return result;
    }


    public FileStatusEnum fileExistsValidation(String telephoneNumber, String fileName, int validityPeriodInMin, boolean internal) {
        boolean result = false;
        logger.debug("check file exist for telephoneNumber=" + telephoneNumber + ", filename=" + fileName);
        String uid = telephoneNumber;
        if(telephoneNumber.charAt(0) == '+') {
            uid = telephoneNumber.substring(1);
            logger.debug("remove the prefix +");
        }

        MSA msa = getMSA(telephoneNumber, internal);
        if (msa == null) {
            String msg = "Could not find MSA for phoneNumber " + telephoneNumber + " [" + internal + "]";
            logger.error(msg);
            return FileStatusEnum.FILE_DOES_NOT_EXIST;
        }

        String target = msa.toString() + " in private MOIP events for number " + uid + " with name " + fileName;


        int maxTries = 5;
        int milliseconds = 10;
        String maxTriesString = System.getProperty("com.abcxyz.vvs.backend.fileexistmaxtries", "5");
        String milliString = System.getProperty("com.abcxyz.vvs.backend.fileexistmilli", "10");
        try {
            maxTries = Integer.parseInt(maxTriesString);
            milliseconds = Integer.parseInt(milliString);
        } catch (NumberFormatException nfex) { ; }

        if (logger.isDebugEnabled()) {
            logger.debug("fileExistsValidation: Checking validity of " + target);
        }

        long lastModified;
        try {
            lastModified = mfsClient.getPrivateFileTimeStamp(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName);

            if (lastModified != 0L) {

                if (validityPeriodInMin != 0) {
                    long validityPeriodInMilliseconds = validityPeriodInMin*60*1000;
                    long currentTime = Calendar.getInstance().getTimeInMillis();

                    result = (lastModified + validityPeriodInMilliseconds) > currentTime;

                    if (logger.isDebugEnabled()) {
                        logger.debug("Checking validity of " + target + " lastModified: " + new Date(lastModified).toString() + " validityPeriodInMilliseconds: " + validityPeriodInMilliseconds + " currentTime: " + new Date(currentTime).toString());
                    }

                    if (result) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(target + " file found and valid within the validity period");
                        }
                        return FileStatusEnum.FILE_EXISTS_AND_VALID;
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.info(target + " file found but older that the validity period lastModified: " + new Date(lastModified).toString() + " validityPeriodInMilliseconds: " + validityPeriodInMilliseconds +" currentTime: "+ new Date(currentTime).toString());
                        }
                        return FileStatusEnum.FILE_EXISTS_AND_INVALID;
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(target + " file found and no validity check performed");
                    }
                    return FileStatusEnum.FILE_EXISTS_NO_VALIDATION;
                }

            } else {
                return FileStatusEnum.FILE_DOES_NOT_EXIST;
            }

        } catch (Exception e) {
            String msg = "Failed to perform fileExistsValidation : " + target; 
            logger.error(msg);
            return FileStatusEnum.FILE_DOES_NOT_EXIST;
        }
    }


    public static boolean renameFile(String telephoneNumber, String oldFileName, String newFileName) {
        logger.debug("rename file for telephoneNumber=" + telephoneNumber + ", oldFileName=" + oldFileName + ", newFileName=" + newFileName );
        String uid = telephoneNumber;
        if(telephoneNumber.charAt(0) == '+') {
            uid = telephoneNumber.substring(1);
            logger.debug("remove the prefix +");
        }
        boolean isRenamed = false;
        boolean internal = isNumberInternal(telephoneNumber);
        MSA msa = getMSA(telephoneNumber, internal);
        if (msa == null) {
            String msg = "Could not find MSA for phoneNumber " + telephoneNumber;
            logger.error(msg);
            return isRenamed;
        }

        String target = msa.toString() + " in private MOIP events for number " + uid + " with name " + oldFileName;

        try {
            mfsClient.renamePrivateArtifact(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, oldFileName, newFileName);
            isRenamed = true;
        } catch (Exception e) {
            String msg = "Failed to rename file " + target + " to " + newFileName;
            logger.error(msg, e);
        }
        return isRenamed;
    }

    public Properties getProperties(String telephoneNumber, String fileName) {
        Properties props = null;
        logger.debug("get Properties for telephoneNumber=" + telephoneNumber + ", filename=" + fileName);
        String uid = telephoneNumber;
        if(telephoneNumber.charAt(0) == '+') {
            uid = telephoneNumber.substring(1);
            logger.debug("remove the prefix +");
        }

        MSA msa = getMSA(telephoneNumber);
        if (msa == null) {
            String msg = "Could not find MSA for phoneNumber " + telephoneNumber;
            logger.error(msg);
            return null;
        }
        String target = msa.toString() + " in private MOIP events for number " + uid + " with name " + fileName;

        ByteArrayInputStream is = null;
        try {
            is = mfsClient.getPrivateFileAsInputStream(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName);
            if (is != null) {
                props = new Properties();
                props.load(is);
            } else {
                logger.debug("Could not get properties of non-existing " + target);
            }
        } catch (Exception e) {
            String msg = "Failed to get properties of " + target;
            logger.error(msg, e);
        }

        return props;
    }


    public void storeProperties(String telephoneNumber, String fileName, Properties props) throws TrafficEventSenderException {
        logger.debug("store Properties for telephoneNumber=" + telephoneNumber + ", fileName=" + fileName);
        String uid = telephoneNumber;
        if(telephoneNumber.charAt(0) == '+') {
            uid = telephoneNumber.substring(1);
            logger.debug("remove the prefix +");
        }

        MSA msa = getMSA(telephoneNumber);
        if (msa == null) {
            String msg = "Could not find MSA for phoneNumber " + telephoneNumber;
            logger.error(msg);
            throw new TrafficEventSenderException(msg);
        }
        String target = msa.toString() + " in private MOIP events for number " + uid + " with name " + fileName;

        try {
            StringWriter sw = new StringWriter();
            props.store(sw, null);
            mfsClient.addPrivate(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, fileName, sw.toString());
        } catch (Exception e) {
            String msg = "Failed to rename file " + target + " to " + fileName;
            logger.error(msg, e);
            throw new TrafficEventSenderException(msg);
        }
    }

    public static File[] getPayloadFiles(String telephoneNumber, final String type) {
        return null;
    }

    public static boolean doesPayloadFileExist(String telephoneNumber, String filename) throws TrafficEventSenderException {
        throw new TrafficEventSenderException("not implemented in class MfsEventManagerNoSQL");
    }

    public static byte[] getBytePayload(String telephoneNumber, String filename) throws TrafficEventSenderException {
        throw new TrafficEventSenderException("not implemented in class MfsEventManagerNoSQL");
    }

    public static String getStringPayload(String telephoneNumber, String filename) throws TrafficEventSenderException {
        throw new TrafficEventSenderException("not implemented in class MfsEventManagerNoSQL");
    }

    public void storePayload(String telephoneNumber, String filename, PayloadType type, String payload) throws TrafficEventSenderException {
        throw new TrafficEventSenderException("not implemented in class MfsEventManagerNoSQL");
    }

    public long getLastModified(File file) throws TrafficEventSenderException{
        if (file != null) {
            EventKeys keys = extractFromFilePath(file.getPath());
            logger.debug("get last modified timestamp for telephoneNumber=" + keys.telephoneNumber + ", fileName=" + keys.filename);
            String uid = keys.telephoneNumber;
            if(keys.telephoneNumber.charAt(0) == '+') {
                uid = keys.telephoneNumber.substring(1);
                logger.debug("remove the prefix +");
            }
            try {
                return mfsClient.getPrivateFileTimeStamp(keys.msa, keys.msgClass, uid, MfsEventManager.EVENTS_DIRECTORY, keys.filename);
            } catch (Exception e) {
                String msg = "Exception while getting the last modified time stamp. " + e.getMessage();
                logger.error(msg);
                throw new TrafficEventSenderException(msg);
            }
        }
        String msg = "Exception while getting the last modified time stamp. The provided file was null.";
        logger.error(msg);
        throw new TrafficEventSenderException(msg);
    }

    @Override
    public Reader retrieveEventsAsReader(File file) throws TrafficEventSenderException {
        if (file != null) {
            InputStreamReader reader = null;
            EventKeys keys = extractFromFilePath(file.getPath());
            logger.debug("retrieveEventsAsReader for telephoneNumber=" + keys.telephoneNumber + ", fileName=" + keys.filename);
            String uid = keys.telephoneNumber;
            if(keys.telephoneNumber.charAt(0) == '+') {
                uid = keys.telephoneNumber.substring(1);
                logger.debug("remove the prefix +");
            }
            try {
                ByteArrayInputStream is = mfsClient.getPrivateFileAsInputStream(keys.msa, keys.msgClass, uid, MfsEventManager.EVENTS_DIRECTORY, keys.filename);
                reader = new InputStreamReader(is);
            } catch (Exception e) {
                String msg = "Exception while getting the event file. " + e.getMessage();
                logger.error(msg);
                throw new TrafficEventSenderException(msg);
            }
            return reader;
        }
        String msg = "Exception while getting the event file. The provided file was null.";
        logger.error(msg);
        throw new TrafficEventSenderException(msg);
    }

    @Override
    public boolean renameFile(File orig, File dest) throws TrafficEventSenderException {
        boolean result = false;
        if (orig != null && dest != null) {
            EventKeys keys = extractFromFilePath(orig.getPath());
            try {
                logger.debug("rename file for telephoneNumber=" + keys.telephoneNumber);
                String uid = keys.telephoneNumber;
                if(keys.telephoneNumber.charAt(0) == '+') {
                    uid = keys.telephoneNumber.substring(1);
                    logger.debug("remove the prefix +");
                }
                mfsClient.renamePrivateArtifact(keys.msa, keys.msgClass, uid, MfsEventManager.EVENTS_DIRECTORY, keys.filename, dest.getName());
                result = true;
            } catch (Exception e) {
                String msg = "Exception while renaming the event file. " + e.getMessage();
                logger.error(msg);
                throw new TrafficEventSenderException(msg);
            }
            return result;
        }
        String msg = "Exception while renaming the event file. One or both provided file were null.";
        logger.error(msg);
        throw new TrafficEventSenderException(msg);
    }

    @Override
    public String[] getSendStatusEventFiles(String telephoneNumber, final String eventName, final String order) throws TrafficEventSenderException {
        logger.debug("getSendStatusEventFiles telephoneNumber=" + telephoneNumber + ", eventName=" + eventName + ",order=" + order);
        String uid = telephoneNumber;
        if(telephoneNumber.charAt(0) == '+') {
            uid = telephoneNumber.substring(1);
            logger.debug("remove the prefix +");
        }
        String[] fileArray = null;
        MSA msa = getMSA(telephoneNumber);
        if (msa == null) {
            String msg = "Could not find MSA for phoneNumber " + telephoneNumber;
            logger.error(msg);
            return null;
        }


        String msg = "Exception trying to getSendStatusEventFiles files for " + msa.toString() + " in private MOIP events for number " + uid;

        try {
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().startsWith(eventName);
                }
            };

            List<String> files = mfsClient.getPrivateFiles(msa, MOIP_CLASS, uid, MfsEventManager.EVENTS_DIRECTORY, filter);
            int fileNumber = files.size();
            fileArray = new String[fileNumber];

            EventKeys keys = new EventKeys();
            keys.msa = msa;
            keys.internal = msa.isInternal();
            keys.msgClass = MOIP_CLASS;
            keys.telephoneNumber = telephoneNumber;

            for (int i = 0; i < fileNumber; i++) {
                keys.filename = files.get(i);
                String createdPath = createFilePath(keys);
                if(order.equalsIgnoreCase("lifo")) {//lifo
                    fileArray[fileNumber-i-1] = createdPath;
                } else {//fifo
                    fileArray[i] = createdPath;
                }
                logger.debug("getSendStatusEventFiles : fileArray[" + i + "]=[" + createdPath + "]");
            }
        } catch (Exception e) {
            logger.error(msg, e);
            throw new TrafficEventSenderException(msg);
        }

        return fileArray;
    }

    /**
     * For testing purpose only.
     * Clears the worker to be able to initialize with a new one.
     */
    static void clearWorker() {

        for (EventManagerWorkerNoSQL eventManagerWorkerNoSQL : eventManagerWorkersNoSQL) {
            eventManagerWorkerNoSQL.stop();
        }
        for (McnEventManagerWorkerNoSQL mcnEventManagerWorkerNoSQL : mcnEventManagerWorkersNoSQL) {
            mcnEventManagerWorkerNoSQL.stop();
        }
    }

    
}
