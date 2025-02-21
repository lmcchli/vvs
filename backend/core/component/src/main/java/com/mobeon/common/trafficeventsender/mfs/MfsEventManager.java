/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.common.trafficeventsender.mfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.identityformatter.IdentityFormatterInvalidIdentityException;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.MsgStoreServer;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.IdGenerationException;
import com.abcxyz.messaging.mfs.message.MfsFileFolder;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.MfsClient;
import com.mobeon.common.trafficeventsender.MfsConfiguration;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderConfiguration;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException.TrafficEventSenderExceptionCause;

/**
 * This class saves and retrieves events to and from MFS.
 * <p>
 * MfsEventManager is an active class. It has a thread to save events to user's event files.<br>
 * Reading events is performed in the calling thread.<br>
 * </p>
 * <b>File Management</b>
 * <p>
 * Received events are saved to event files when calling the
 * {@link MfsEventManager#storeEvent storeEvent} method.
 * </p>
 * <p>
 * Event files are created using the following format:<br>
 * <i>&lt;MFS User's private directory&gt;/&lt;phone number&gt;/events/&lt;event name&gt;_&lt;time stamp&gt;</i><br>
 * </p>
 * <p>
 * The event file is a text file that contains one event per line. Each line has the following format:<br>
 * <i>property1=value1;property2=value2;...;propertyN=valueN</i><br>
 * </p>
 * <b>Notification</b>
 * <p>
 * A notification is sent to NTF when one of the two conditions is reached:
 * <ul>
 * <li>a threshold number (configurable) of events has been received, or;
 * <li>a timeout when some events have been stored but not sent because the previous threshold was not reached.
 * </ul>
 * The notification provides the event name and the file name to NTF.
 * After the notification is sent, the file is not modified anymore.
 * NTF deletes the file when it has consumed the events.<br>
 * MAS creates a new file with a different time stamp to store new events.
 * </p>
 * <b>Retrieving Events</b>
 * <p>
 * Retrieving of events is performed by calling the
 * {@link MfsEventManager#retrieveEvents(String, boolean) retrieveEvents} method.
 * When finished with the file, the calling process should delete the corresponding event file
 * by calling {@link MfsEventManager#removeFile(String, String) remove}.
 * </p>
 */
public class MfsEventManager implements IMfsEventManager {

	public static final String PROPERTY_SEPARATOR = ";";
	public static final String KEY_VALUE_SEPARATOR = "=";
	static final String TOKEN_SEPARATOR = "_";
	static final String EVENTS_DIRECTORY = "events";
	static final String PRIVATE_DIRECTORY = "private";
	public static final String SESSION_ID = "sessionid"; // from TrafficEventManager.SESSIONID

	static final String SLAMDOWN_EVENT_SERVICE_NAME = "slamdowntimeout";
	static final String MCN_EVENT_SERVICE_NAME = "mcntimeout";
	
	static final String ALLOW_DIRECTORY_CREATION = "abcxyz.mfs.userdir.create";
	
	private static final String TEMP_PREFIX = "lockedodl-";

	public static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
	    @Override
	    protected DateFormat initialValue() {
	        return new SimpleDateFormat("yyyyMMdd_HH_mm_ss_SSS");
            }
	};
	private static final ILogger logger = ILoggerFactory.getILogger(MfsEventManager.class);
	
	/** File name for the file indicating the user is currently logged in */
	private static final String FILENAME_LOGGEDIN = "loggedin";
	/** Prefix for out-dial event files. */
	static final String OUTDIAL_EVENT_FILE_PREFIX = "odl-";

	/** MFS interface */
	protected static ICommonMessagingAccess mfs;

	private static IDirectoryAccess directory;

	private static Boolean eventManagerWorkersStarted = false;
	private static EventManagerWorker[] eventManagerWorkers;
	protected int numberOfSlamdownWorkers = 10;
	private NtfNotifierEventHandler slamdownNotifier;

	private static Boolean mcnEventManagerWorkersStarted = false;
	private static McnEventManagerWorker[] mcnEventManagerWorkers;
	protected int numberOfMcnWorkers = 10;
	private NtfNotifierEventHandler mcnNotifier;

    public static final String SERVICE_TYPE_KEY = "service_type";

    /* Constants for payload storage. */
    public static final String PAYLOAD_FILE_EXTENSION = "payload";
    public static final String PAYLOAD_TYPE_KEY = "payload_type";
    public static final String PAYLOAD_FILENAME_KEY = "eventfile";
    public static final String DATE_KEY = "date";

    public enum PayloadType {
        BYTES   ("bytes"),
        STRING     ("string");

        private String key;

        PayloadType(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public enum FileStatusEnum {
        FILE_DOES_NOT_EXIST,
        FILE_EXISTS_NO_VALIDATION,
        FILE_EXISTS_AND_VALID,
        FILE_EXISTS_AND_INVALID
    }

    public MfsEventManager() {
    	this(CommonMessagingAccess.getInstance(), null);
    }

    public MfsEventManager(CommonMessagingAccess cma, IDirectoryAccess da) {
    	synchronized(MfsEventManager.class) {
	    	if (mfs == null) {
	    		mfs = cma;
	    	}

	    	if (directory == null) {
	    		directory = da;
	    	}
    	}
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
                    slamdownNotifier = new NtfNotifierEventHandler(this, MfsEventManager.SLAMDOWN_EVENT_SERVICE_NAME, slamdownAggregationNumberOfExpiryRetries);
                    slamdownNotifier.start(info);

                    // Create the EventManagerWorkers
                    numberOfSlamdownWorkers = mfsConfiguration.getSlamdownWorkers();
                    eventManagerWorkers = new EventManagerWorker[numberOfSlamdownWorkers];
                    for (int i=0; i<numberOfSlamdownWorkers; i++) {
                        eventManagerWorkers[i] = new EventManagerWorker("EventManagerWorker-" + i, slamdownNotifier);
                        eventManagerWorkers[i].setDaemon(true);
                        eventManagerWorkers[i].start();
                    }
                    logger.info("initSlamdownWorkers() : " + numberOfSlamdownWorkers + " eventManagerWorkers started");
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
                    mcnNotifier = new NtfNotifierEventHandler(this, MfsEventManager.MCN_EVENT_SERVICE_NAME, mcnAggregationNumberOfExpiryRetries);       
                    mcnNotifier.start(info);

                    // Create the McnEventManagerWorkers
                    numberOfMcnWorkers = mfsConfiguration.getMcnWorkers();
                    mcnEventManagerWorkers = new McnEventManagerWorker[numberOfMcnWorkers];
                    for (int i=0; i<numberOfMcnWorkers; i++) {
                        mcnEventManagerWorkers[i] = new McnEventManagerWorker("McnEventManagerWorker-" + i, mcnNotifier);
                        mcnEventManagerWorkers[i].setDaemon(true);
                        mcnEventManagerWorkers[i].start();
                    }
                    logger.info("initMcnWorkers() : " + numberOfMcnWorkers + " mcnEventManagerWorkers started");
                    mcnEventManagerWorkersStarted = true;
                }
            }
        }
    }

    @Override
    public TrafficEvent[] retrieveEvents(String filePath, boolean internal) throws TrafficEventSenderException {
        return retrieveEventsInternal(filePath, null);
    }

    @Override
    public TrafficEvent[] retrieveEvents(String phoneNumber, String fileName)
            throws TrafficEventSenderException {
        return retrieveEvents(phoneNumber, fileName, isInternal(phoneNumber));
    }
    
    /* (non-Javadoc)
     * @see com.mobeon.common.trafficeventsender.mfs.IMfsEventManager#retrieveEvents(java.lang.String, java.lang.String)
     */
    @Override
    public TrafficEvent[] retrieveEvents(String phoneNumber, String name, boolean internal)
            throws TrafficEventSenderException {

        String filePath = generateFilePath(phoneNumber, name, internal);
        if (filePath == null) {
            String msg = "Could not generate path for filename: " + name + ", phoneNumber: " + phoneNumber;
            logger.error(msg);
            throw new TrafficEventSenderException(msg, TrafficEventSenderExceptionCause.PAYLOAD_FILE_PATH_NOT_ACCESSIBLE);

        }
        
        return retrieveEventsInternal(filePath, name);
    }

	private TrafficEvent[] retrieveEventsInternal(String filePath, String name)
	        throws TrafficEventSenderException {

	    File file = new File(filePath);

	    if (file.exists()) {
	    	
	    	try {
	    		return retrieveEventsInternal(new FileReader(file), filePath, name);
	    	} catch (FileNotFoundException e) {
	    		String msg = "Cannot open event file " + file.getPath() + " : " + e.getMessage();
	    		logger.error(msg);
	    		throw new TrafficEventSenderException(msg, TrafficEventSenderExceptionCause.PAYLOAD_FILE_NOT_ACCESSIBLE);
	    	}	    	
	    	
	    } else {
	        String msg = "File " + file + " not found.";
	        logger.error(msg);
	        throw new TrafficEventSenderException(msg, TrafficEventSenderExceptionCause.PAYLOAD_FILE_DOES_NOT_EXIST);
	    }
	}
	
	/**
	 * 
	 * @param in
	 * @param filePath  Only for logging purposes, to identity the event being retrieved.
	 * @param name
	 * @return
	 * @throws TrafficEventSenderException
	 */
	protected TrafficEvent[] retrieveEventsInternal(Reader in, String filePath, String name)
	        throws TrafficEventSenderException {

        Vector<TrafficEvent> events = new Vector<TrafficEvent>();
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(in);
            String propertyLine = null;

            // Extract event name from file name.
            int startIdx = name.lastIndexOf(File.separator) + 1;
            int endIdx = name.indexOf(TOKEN_SEPARATOR, startIdx);
            String eventName = null;
            if (endIdx != -1) {
                eventName = name.substring(startIdx, endIdx);
            } else {
                eventName = name.substring(startIdx);
            }

            while ((propertyLine = reader.readLine()) != null) {
                TrafficEvent event = new TrafficEvent();
                String[] properties = propertyLine.split(PROPERTY_SEPARATOR);
                for (String property : properties) {
                    String[] keyVal = property.split(KEY_VALUE_SEPARATOR, 2);
                    event.setProperty(keyVal[0], keyVal[1]);
                }
                event.setName(eventName);

                events.add(event);
            }

            return events.toArray(new TrafficEvent[events.size()]);

        } catch (FileNotFoundException e) {
            String msg = "Cannot open event file " + filePath + " : " + e.getMessage();
            logger.error(msg);
            throw new TrafficEventSenderException(msg, TrafficEventSenderExceptionCause.PAYLOAD_FILE_NOT_ACCESSIBLE);
        } catch (IOException e) {
            String msg = "Cannot read event file " + filePath + " : " + e.getMessage();
            logger.error(msg);
            throw new TrafficEventSenderException(msg, TrafficEventSenderExceptionCause.PAYLOAD_FILE_NOT_ACCESSIBLE);	
        } catch (IndexOutOfBoundsException e) {
            String msg = "Parsing error in event file " + filePath + " : " + e.getMessage();
            logger.error(msg);
            throw new TrafficEventSenderException(msg, TrafficEventSenderExceptionCause.PAYLOAD_FILE_PARSING_ERROR);   		
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {}				
        }			
		
	}
	
	

	/* (non-Javadoc)
	 * @see com.mobeon.common.trafficeventsender.mfs.IMfsEventManager#StoreVvaSms(java.lang.String, com.mobeon.common.trafficeventsender.TrafficEvent)
	 */
	@Override
    public void storeVvaSms(String telephoneNumber, TrafficEvent event) throws TrafficEventSenderException	    {

		MessageInfo msgInfo = new MessageInfo();
		msgInfo.rmsa = new MSA(getMSID(telephoneNumber));
		Properties  properties = new Properties();
		properties.putAll(event.getProperties());

		if (logger.isDebugEnabled()){
			logger.debug("storeVvaSms(): Sending VVA SMS for tel:" + telephoneNumber  + " and event " + event.getName());
		}

		mfs.notifyNtf(EventTypes.VVA_SMS, msgInfo, telephoneNumber, properties);

    }
	
	   /* (non-Javadoc)
     * @see com.mobeon.common.trafficeventsender.mfs.IMfsEventManager#storeAutoUnlockPinLockout(java.lang.String, com.mobeon.common.trafficeventsender.TrafficEvent)
     */
    @Override
    public void storeAutoUnlockPinLockout(String telephoneNumber, TrafficEvent event) throws TrafficEventSenderException      {

        MessageInfo msgInfo = new MessageInfo();
        msgInfo.rmsa = new MSA(getMSID(telephoneNumber));
        Properties  properties = new Properties();
        properties.putAll(event.getProperties());

        /**
         * Validate event.
         * Property 'locktime' is required for event to be processed by NTF
         */
        boolean valid = false;
        String formattedDate = properties.getProperty(MoipMessageEntities.AUTO_UNLOCK_PIN_LOCKTIME);
        if (formattedDate != null && !formattedDate.isEmpty()) {
            try {
                MfsEventManager.dateFormat.get().parse(formattedDate);
                valid = true;
            } catch (ParseException e) {
                //
            }
        }
        
        if(!valid) {
            String message = "AutoUnlockPin event for " + telephoneNumber + " cannot parse mandatory property locktime: " + formattedDate;
            logger.error(message);
            throw new TrafficEventSenderException(message);
        }
       
        if (logger.isDebugEnabled()){
            logger.debug("storeAutoUnlockPinLockout(): Sending auto unlock event for tel:" + telephoneNumber  + " and event " + event.getName());
        }

        mfs.notifyNtf(EventTypes.AUTO_UNLOCK_PIN, msgInfo, telephoneNumber, properties);

    }


	/* (non-Javadoc)
	 * @see com.mobeon.common.trafficeventsender.mfs.IMfsEventManager#storeEvent(java.lang.String, com.mobeon.common.trafficeventsender.TrafficEvent)
	 */
	@Override
	public void storeEvent(String telephoneNumber, TrafficEvent event)
			throws TrafficEventSenderException {
		
		if (logger.isDebugEnabled()){
			logger.debug("MfsEventManager.storeEvent for " + telephoneNumber);
			logger.debug("Event for " + event.getName());
		}

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
		        mcnEventManagerWorkers[workerId].enqueue(new IEventManagerWorker.IncomingEventEntry(telephoneNumber, event));

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
                eventManagerWorkers[workerId].enqueue(new IEventManagerWorker.IncomingEventEntry(telephoneNumber, event));

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
	 * This method hashes a subscriberNumber in order to return a workerId.
	 * The workerId will always return the same value for a given subscriberNumber so that all the events
	 * related to a given subscriber will be processed sequentially by the same worker thread.  
	 * @param telephoneNumber String
	 * @param timeoutEventType String
	 * @return workerId
	 */
	protected int getWorkerIdFromTelephoneNumber(String telephoneNumber, String timeoutEventType) {
	    String number = CommonMessagingAccess.getInstance().denormalizeNumber(telephoneNumber);

	    int numberOfWorkers = 0;
	    if (timeoutEventType.equalsIgnoreCase(MfsEventManager.MCN_EVENT_SERVICE_NAME)) {
	        numberOfWorkers = numberOfMcnWorkers;
	    } else {
	        numberOfWorkers = numberOfSlamdownWorkers;
	    }

	    int workerId = Math.abs(number.hashCode()) % numberOfWorkers;

	    return workerId;
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
	            eventManagerWorkers[workerId].enqueue(new IEventManagerWorker.TimeoutEventEntry(properties, nextEventId));
	        } else if (timeoutEventType.equalsIgnoreCase(MfsEventManager.MCN_EVENT_SERVICE_NAME)) {
	            logger.debug("Adding McnTimeout event for " + subscriberNumber + " to McnEventManagerWorker-" + workerId);
	            mcnEventManagerWorkers[workerId].enqueue(new IEventManagerWorker.TimeoutEventEntry(properties, nextEventId));
	        } else {
	            logger.error("Invalid timeoutEventType: " + timeoutEventType + ", Properties: " + properties);
	        }
	    } catch (IllegalStateException ise) {
	        logger.error("Cannot store " + timeoutEventType + " timeout event: ", ise);
	    }
	}

	@Override
	public String[] getEventFiles(String telephoneNumber, final String eventName) throws TrafficEventSenderException {
		String baseEventFile = generateFilePath(telephoneNumber, eventName, true);

		if (baseEventFile == null) {
			return new String[]{};
		}

		File file = new File(baseEventFile);
		File parentDir = file.getParentFile();

		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().matches(eventName + "_\\d{8}_\\d{4}");
			}
		};

		File[] files = parentDir.listFiles(filter);
		if (files != null) {
			String[] fileList = new String[files.length];
			for (int i = 0; i < files.length; ++i) {
				fileList[i] = files[i].getName();
			}
			return fileList;
		} else {
			return new String[0];
		}

	}

    @Override
    public String[] getSendStatusEventFiles(String telephoneNumber, final String eventName, final String order) throws TrafficEventSenderException {
        if (logger.isDebugEnabled()){
          logger.debug("getSendStatusEventFiles telephoneNumber="+telephoneNumber+",eventName="+eventName+",order="+order);
        }
        String baseEventFile = generateFilePath(telephoneNumber, eventName, true);
        if (logger.isDebugEnabled()){
            logger.debug("baseEventFile="+baseEventFile);
        }
        if (baseEventFile == null) {
            return new String[]{};
        }

        File file = new File(baseEventFile);
        File parentDir = file.getParentFile();

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().startsWith(eventName);
            }
        };

        File[] files = parentDir.listFiles(filter);
        if (logger.isDebugEnabled()){
            logger.debug("files="+files.length);
        }
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File file1, File file2) {
                    if ("lifo".equals(order)) {
                        return (int)(file2.lastModified() - file1.lastModified());
                    }
                    else {
                        return (int)(file1.lastModified() - file2.lastModified());
                    }
                }
            });
            String[] fileList = new String[files.length];
            for (int i = 0; i < files.length; ++i) {
                fileList[i] = files[i].getName();
            }
            return fileList;
        } else {
            return new String[0];
        }

    }
	public boolean isInternal(String telephoneNumber) {
	    return isNumberInternal(telephoneNumber);
	}
	
	/** Static internal method for payload */
    protected static boolean isNumberInternal(String telephoneNumber) {
        String msid = getMSID(telephoneNumber);
        if (msid != null) {
            return true;
        } else {
            return false;
        }
    }

	public static String getMSID(String uid) {
		if(directory == null) {
			directory = DirectoryAccess.getInstance();
		}
		IDirectoryAccessSubscriber subscriber = directory.lookupSubscriber(uid);
		if (subscriber == null) {
			if (logger.isDebugEnabled()) logger.debug("MfsEventManager.getMSID(): uid = " + uid + " returning null");
			return null;
		}
		String msid = subscriber.getSubscriberIdentity(MCDConstants.IDENTITY_SCHEME_MSID);
		if (logger.isDebugEnabled()) logger.debug("MfsEventManager.getMSID(): uid = " + uid + " subscriber msid = " + msid);
		return msid;
	}

    /**
     * Generate root path
     * @param telephoneNumber
     * @return rootPath
     */
    protected static String generateRootPath(String telephoneNumber) {
        boolean internal;
        String rootPath;
        String msid = getMSID(telephoneNumber);

        if (msid != null) {
            // Known subscriber
            internal = true;
        } else {
            // Unknown subscriber
            internal = false;
            try {
                MSA msa = MsgStoreServer.getMSA(telephoneNumber, false);
                msid = msa.toString();
            } catch (Exception e) {}
        }

        if (msid == null) {
            return null;
        }

        rootPath = mfs.getMoipPrivateFolder(msid, internal);

        return rootPath;
    }

	/**
	 * Generate root path<p>
	 * 
	 * Internal number would generate: <p>
	 * <pre>{@literal /opt/mfs/internal/<msid>/}</pre>
	 * External number would generate: <p>
	 * <pre>{@literal /opt/mfs/external/<eid>/}</pre>
	 * 
	 * In versions previous to MiO 5.0, this method would return the
	 * 
	 * Internal number would generate: <p>
	 * <pre>{@literal /opt/mfs/internal/<msid>/private/moip/}</pre>
	 * External number would generate: <p>
	 * <pre>{@literal /opt/mfs/external/<eid>/private/moip/}</pre>
	 * 
	 * @param telephoneNumber
	 * @return rootPath
	 */
	public static String generateRootPath(String telephoneNumber, boolean internal) {
	    String msid = null;
	    String rootPath;
	    
	    if (internal) {
	        msid = getMSID(telephoneNumber);
	    } else {
	        try {
	            MSA msa = MsgStoreServer.getMSA(telephoneNumber, false);
	            msid = msa.toString();
	        } catch (Exception e) {}
	    }

	    if (msid == null) {
	        return null;
	    }

	    rootPath = mfs.getMoipPrivateFolder(msid, internal);

	    return rootPath;
	}
	
	/**
	 * Generate file path.  The phone number provided can either be internal (known to MCD) or external (unknown).
	 * A MCD lookup will be performed to figure out.<p>
	 * 
	 * Internal number would generate:<p>
	 * <pre>{@literal /opt/mfs/internal/<msid>/private_moip_<telephonenumber>_events/<fileName>}</pre>
	 * External number would generate:<p>
	 * <pre>{@literal /opt/mfs/external/<eid>/private_moip_<telephonenumber>_events/<fileName>}</pre>
	 * 
	 * In versions previous to MiO 5.0, this method would return the
	 * 
	 * Internal number would generate:<p>
	 * <pre>{@literal /opt/mfs/internal/<msid>/private/moip/<telephonenumber>/events/<fileName>}</pre>
	 * External number would generate:<p>
	 * <pre>{@literal /opt/mfs/external/<eid>/private/moip/<telephonenumber>/events/<fileName>}</pre>
	 * 
	 * @param telephoneNumber
	 * @param fileName
	 * @return filePath
	 * @throws TrafficEventSenderException
	 */
	protected static String generateFilePath(String telephoneNumber, String fileName)  {
	    String rootPath = generateRootPath(telephoneNumber); 
	    if (rootPath == null) {
	        return null;
	    }

	    // String filePath = rootPath.concat(File.separator + moipFormatedTelephone(telephoneNumber) + File.separator + EVENTS_DIRECTORY + File.separator + fileName);
	    // ==> MIO 5.0 MIO5_MFS: do create one sub-dir level private_moip_<telephonenumber>_events,
	    // instead of not creating any sub dir; too many logics, (e.g. SlamdownList.java) do test based on filename.startsWith()
	    // rootPath = /opt/mfs/internal|external/1/234/1234567890abcdef/ or /opt/mfs/internal|external/12/345/1234567890abcdef/
	    String filePath = rootPath.concat(MfsFileFolder.PREFIX_MOIP + moipFormatedTelephone(telephoneNumber) + 
	    		                          TOKEN_SEPARATOR + EVENTS_DIRECTORY + File.separator + fileName);
	    
	    return filePath;
	}

	/**
	 * Generate file path by providing if the given telephone number is known (Slamdown) or unknown (Missed Call).<p>
	 * 
	 * Internal number would generate: <p>
	 * <pre>{@literal /opt/mfs/internal/<msid>/private_moip_<telephonenumber>_events/<fileName>}</pre>
	 * External number would generate: <p>
	 * <pre>{@literal /opt/mfs/external/<eid>/private_moip_<telephonenumber>_events/<fileName>}</pre>
	 * 
	 * In versions previous to MiO 5.0, this method would return the
	 * 
	 * Internal number would generate: <p>
	 * <pre>{@literal /opt/mfs/internal/<msid>/private/moip/<telephonenumber>/events/<fileName>}</pre>
	 * External number would generate: <p>
	 * <pre>{@literal /opt/mfs/external/<eid>/private/moip/<telephonenumber>/events/<fileName>}</pre>
	 * 
	 * 
	 * @param telephoneNumber
	 * @param fileName
	 * @param internal
	 * @return filePath
	 * @throws TrafficEventSenderException
	 */
	protected static String generateFilePath(String telephoneNumber, String fileName, boolean internal) {
	    String rootPath = generateRootPath(telephoneNumber, internal); 
	    if (rootPath == null) {
	        return null;
	    }  

	    // String filePath = rootPath.concat(File.separator + moipFormatedTelephone(telephoneNumber) + File.separator + EVENTS_DIRECTORY + File.separator + fileName);
	    // ==> MIO 5.0 MIO5_MFS: create one directory level for events:
	    String filePath = rootPath.concat(MfsFileFolder.PREFIX_MOIP + moipFormatedTelephone(telephoneNumber) + TOKEN_SEPARATOR + 
	    		                          EVENTS_DIRECTORY + File.separator + fileName);
	    return filePath;
	}

	
	public static class EventKeys {
		MSA msa;
		boolean internal;
		String msgClass;
		String telephoneNumber;
		String filename;
	}
	
	
	/**
	 * The provided {@code filepath} input will look like this :
	 * This method is used by systems with a DB MFS, not a file system.
	 * 
	 * <pre>
	 * internal/286d8e489cbe2b96/moip/5148988722/slamdowninformation_20211206_20_15_35_717
	 * </pre>
	 * 
	 * @param filepath
	 * @return
	 */
	public static EventKeys extractFromFilePath(String filepath) {
		EventKeys keys = null;
		if (filepath != null) {
			String[] tokens = filepath.split("/");
			if (tokens.length >= 5) {
				keys = new EventKeys();
				if ("internal".equals(tokens[0])) {
					keys.internal = true;
				} else {
					keys.internal = false;
				}
				
				keys.msa = new MSA(tokens[1],keys.internal);
				keys.msgClass = tokens[2];
				keys.telephoneNumber = tokens[3];
				keys.filename = tokens[4];
			}
		}
		return keys;
	}
	
	/**
	 * Create a path that looks like this (to simulate a file system path).
	 * This method is used by systems with a DB MFS, not a file system.
	 * 
	 * <pre>
	 * internal/286d8e489cbe2b96/moip/5148988722/slamdowninformation_20211206_20_15_35_717
	 * </pre>
	 * 
	 * @param keys
	 * @return
	 */
	protected static String createFilePath(EventKeys keys) {
		StringBuffer sb = new StringBuffer();
		if (keys.internal) {
			sb.append("internal");
		} else {
			sb.append("external");
		}
		sb.append(File.separator);
		sb.append(keys.msa.getId()).append(File.separator);
		sb.append(keys.msgClass).append(File.separator);
		sb.append(keys.telephoneNumber).append(File.separator);
		sb.append(keys.filename);
		return sb.toString();
	}

    /**
	 * For testing purposes, File can be wrapped by a subclass
	 * @param path
	 * @return
	 * @deprecated Will be removed in future version. Call <code>new File()</code> instead.
	 */
	protected File createFile(String path) {
	    return new File(path);
	}

    /**
     * Acquire a lock file (empty file) for a given telephone number.
     * 
     * @param telephoneNumber Subscriber or non-subscriber number
     * @param fileName Lock file name
     * @param validityPeriodInSeconds If 0, no validity period validation (file is always considered valid)
     * @param internal MIO Subscriber or non-MIO subscriber
     * @return long lockId which must be provided when unlocking the file, if lock file is not acquire, 0 is returned
     * @throws TrafficEventSenderException if either filePath creation fails or IOException
     *         Thrown to distinguish between a lock file not acquired and a faulty IO access. 
     */
    @Override
    public long acquireLockFile(String telephoneNumber, String fileName, int validityPeriodInSeconds) throws TrafficEventSenderException {
        return acquireLockFile(telephoneNumber, fileName, validityPeriodInSeconds, isInternal(telephoneNumber));
    }

    @Override
    public long acquireLockFile(String telephoneNumber, String fileName, int validityPeriodInSeconds, boolean internal) throws TrafficEventSenderException {

        String path = generateFilePath(telephoneNumber, fileName, internal);
        if (path == null) {
            String ioeMessage = "Unable to generate root path for file name " + fileName + " for telephone number " + telephoneNumber; 
            logger.error(ioeMessage);
            throw new TrafficEventSenderException(ioeMessage);
        }

        long lockId = 0;
        File emptyFile = new File(path);
        try {
            if (emptyFile.createNewFile()) {
                lockId = emptyFile.lastModified();
                logger.debug("LockFile " + path + " aquired (by creating a new lock file) for telephone number " + telephoneNumber + " with lockid = " + lockId);
            } else {
                // File already exists, lookup the last modified date for validity period (if specified)
                if (validityPeriodInSeconds != 0) {
                    long validityPeriodInMilliseconds = validityPeriodInSeconds * 1000;
                    boolean fileExistsAndStillValid = (emptyFile.lastModified() + validityPeriodInMilliseconds) > Calendar.getInstance().getTimeInMillis();
                    if (fileExistsAndStillValid) {
                        logger.debug("LockFile " + path + " not aquired (already a valid lock file found within the validity period) for telephone number " + telephoneNumber);
                    } else {
                        long lastModified = Calendar.getInstance().getTimeInMillis();
                        emptyFile.setLastModified(lastModified);
                        lockId = emptyFile.lastModified();
                        logger.debug("LockFile " + path + " aquired (by replacing an obsolete lock file) for telephone number " + telephoneNumber + " with lockid = " + lockId);
                    }
                } else {
                    logger.debug("LockFile " + path + " not aquired (already a valid lock file found - no validity period verification) for telephone number " + telephoneNumber);
                }
            }
        } catch (IOException ioe) {
            String ioeMessage = "IOException trying to aquire lock file " + path + " for telephone number " + telephoneNumber; 
            logger.error(ioeMessage, ioe);
            throw new TrafficEventSenderException(ioeMessage);
        }

        return lockId;
    }

    /**
     * Release a lock file (empty file) for a given telephone number.
     * 
     * @param telephoneNumber Subscriber or non-subscriber number
     * @param fileName Lock file name
     * @param lockId long Provided when the lock was acquired.  A lock cannot be removed if the lockId does not match. 
     * @throws TrafficEventSenderException if either filePath creation fails or IOException
     */
    public void releaseLockFile(String telephoneNumber, String fileName, long lockId) throws TrafficEventSenderException {
        releaseLockFile(telephoneNumber, fileName, lockId, isInternal(telephoneNumber));
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
        String filePath = generateFilePath(telephoneNumber, fileName, internal);

        if (lockId == 0L) { return; }

        if (filePath == null) {
            String ioeMessage = "Unable to generate root path for file name " + fileName + " for telephone number " + telephoneNumber; 
            logger.error(ioeMessage);
            throw new TrafficEventSenderException(ioeMessage);
        }

        File file = new File(filePath);
        long lastModified = file.lastModified();
        if (lastModified != 0L) {
            if (lastModified == lockId) {
                if (!file.delete()) {
                    String ioeMessage = "Failed to delete lock file: " + filePath + " with lastModified = " + lastModified; 
                    logger.error(ioeMessage);
                    throw new TrafficEventSenderException(ioeMessage);
                } else {
                    logger.debug("Lock file " + filePath + " deleted for telephone number " + telephoneNumber + " with lastModified = " + lastModified);
                }
            } else {
                logger.debug("Lock file " + filePath + " found (" + lastModified + ") does not match the provided lockId (" + lockId + ")");
            }
        }
    }

    @Override
    public void createEmptyFile(String telephoneNumber, String fileName) throws TrafficEventSenderException {
        String path = generateFilePath(telephoneNumber, fileName, true);
        if (path == null) {
            throw new TrafficEventSenderException("No store path for user: " + telephoneNumber);
        }

        File emptyFile = new File(path);
        try {
            if (emptyFile.exists()) {
                emptyFile.setLastModified(Calendar.getInstance().getTimeInMillis());
            }
            if (!emptyFile.exists() && !emptyFile.createNewFile()) {
                throw new TrafficEventSenderException("File not exists and creation failure" + emptyFile);
            }
        } catch (IOException e) {
            
            // ---------------MFS----------------\-PA-\--MfsEventManager--\----MfsEventManager---
            // mfs\internal\1\11\11\24\62\private\moip\151434579001\events\emptyFileToCreate.file
            // MIO 5.0 MIO5_MFS: /mfs/internal/1/111/11111111111111/private_moip_151434579001_events/emptyFileToCreate.file
        	
            // Check if the MOIP private folder is present
            File rootPath = new File(generateRootPath(telephoneNumber, true));
            if (!validateOrCreateDirectory(rootPath, true)) {
                logger.error("Failed to create MOIP private directory for " + telephoneNumber + ", " + fileName + " no store path created. ", e);
                throw new TrafficEventSenderException("Failed to create MOIP private directory " + rootPath);
            }

            // Check if the subscriber's event directory is present
            File eventPath = emptyFile.getParentFile();
            if (!validateOrCreateDirectory(eventPath, false)) {
                logger.error("Failed to create subscriber's event directory for " + telephoneNumber + ", " + fileName, e);
                throw new TrafficEventSenderException("Failed to create subscriber's event directory " + eventPath);
            }

            // Create the empty file
            tryCreateEmptyFile(emptyFile);
        }
    }
    
    @Override
    public void createPropertiesFile(String telephoneNumber, String prefix, Map<String, String> properties) throws TrafficEventSenderException {
        String fileName = null;
        try {
            fileName = MfsEventManager.generateFilePath(telephoneNumber, prefix+MfsEventManager.TOKEN_SEPARATOR +MfsEventManager.dateFormat.get().format(new Date()), true);
            if (fileName == null) {
                logger.error("Cannot create File for " + telephoneNumber + " no store path created. ");
                throw new TrafficEventSenderException("Cannot create File for " + telephoneNumber + " no store path created. ");
            }
        } catch (TrafficEventSenderException e) {
            logger.error("Cannot create File for " + telephoneNumber + " with prefix " + prefix + ", ", e);
            throw e;
        }

        Set<Map.Entry<String, String>> entries = properties.entrySet();
        StringBuilder buffer=new StringBuilder();
        for (Map.Entry<String, String> property : entries) {
            buffer.append(property.getKey() +
                    MfsEventManager.KEY_VALUE_SEPARATOR +
                    property.getValue() +
                    MfsEventManager.PROPERTY_SEPARATOR);
        }
        buffer.append('\n');

        File file = new File(fileName);
        FileWriter fileStream = null;

        //boolean fileExists=file.exists();

        try {
            fileStream = new FileWriter(file, true);
        } catch (IOException e) {

            // Check if the subscriber's event directory is present
            File eventPath = file.getParentFile();
            if (!MfsEventManager.validateOrCreateDirectory(eventPath, false)) {
                logger.error("Failed to create external/non-subscriber's event directory " + eventPath);
                throw new TrafficEventSenderException("Failed to create external/non-subscriber's event directory " + eventPath);
            }

            // Create the event file
            try {
                fileStream = new FileWriter(file, true);
            } catch (IOException ioe) {
                logger.error("Cannot write to event file for " + telephoneNumber + ", ", ioe);
                throw new TrafficEventSenderException("Cannot write to event file: " + ioe.getMessage());
            }
        }

        try {
            fileStream.write(buffer.toString());
            fileStream.close();
        } catch (IOException e) {
            logger.error("Cannot write to event file " + fileName + " for " + telephoneNumber + ", ", e);
            throw new TrafficEventSenderException("Cannot write to event file: " + e.getMessage());
        } finally {
            try {
                if (fileStream != null) {
                    // Make sure the stream is closed.
                    fileStream.close();
                }
            } catch (IOException e) {
                logger.error("Cannot close fileStream for " + telephoneNumber + ", fileName " + fileName + ", ", e);
            }
        }

    }
    /**
     * Create file with retry mecanism 
     * @param emptyFile
     * @throws TrafficEventSenderException
     */
    private void tryCreateEmptyFile(File emptyFile) throws TrafficEventSenderException {
        int retryCount = 0;

        while (retryCount < 3) {
            try {
                if (!emptyFile.createNewFile()) {
                    throw new TrafficEventSenderException("Failed to create empty file: " + emptyFile);
                } else {
                    // File successfully created
                    return;
                }
            } catch (IOException ioe) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ie) {}
                ++retryCount;
            }
        }
        
        throw new TrafficEventSenderException("Failed to create empty file: " + emptyFile);
    }

    /**
     * Validate the presence of the directory or try to create it if not found 
     * @param path To validate/create
     * @param checkPermission If the "abcxyz.mfs.userdir.create" java property must be validate 
     * @return
     */
    public static boolean validateOrCreateDirectory(File path, boolean checkPermission) {
        boolean result = true;
        
        if (path.exists()) {
            return result;
        }

        if (checkPermission) {
            if (isTrueProperty(System.getProperty(ALLOW_DIRECTORY_CREATION))) {
                // Create folder if java property allows to
                if (!path.mkdirs()) {
                    logger.error("Failed to create directory " + path);
                    result = false;
                }
            } else {
                logger.error("System property " + ALLOW_DIRECTORY_CREATION + " not set to allow creation of directory " + path);
                result = false;
            }
        } else {
            if (!path.mkdirs()) {
                logger.error("Failed to create directory " + path);
                result = false;
            }
        }
        
        return result;
    }
    
    /**
     * Returns true if the java property is set to true, false otherwise or if not found.
     * @param property Java property
     * @return
     */
    protected static boolean isTrueProperty(String property) {
        if (property == null) return false;
        property = property.trim();
        return property.equalsIgnoreCase("true") || property.equalsIgnoreCase("yes") || property.equalsIgnoreCase("1");
    }

    @Override
    public String getFileNameByExtension(String telephoneNumber, String eventName, final String extension) {
        
        String filePath = generateFilePath(telephoneNumber, eventName, true);
        if(filePath == null) return null;
        File file = new File(filePath);
        File parentDir = file.getParentFile();

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) { 
                return file.getName().endsWith(extension); 
            }
        };

        File[] files = parentDir.listFiles(filter);
        if (files != null && files.length > 0) {
            String[] fileList = new String[files.length];
            for (int i = 0; i < files.length; ++i) {
                fileList[i] = files[i].getName();
            }
            return fileList[0];
        } else {
            return null;
        }            
    }
    
    @Override
    public String[] getFilesNameStartingWith(String telephoneNumber, final String startingWith) {
        return getFilesNameStartingWith(telephoneNumber, startingWith, false);
    }

    @Override
    public String[] getFilePathsNameStartingWith(String telephoneNumber, final String startingWith) {
        return getFilesNameStartingWith(telephoneNumber, startingWith, true);
    }

    private String[] getFilesNameStartingWith(String telephoneNumber, final String startingWith, boolean parentPath) {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) { 
                return file.getName().startsWith(startingWith); 
            }
        };
        
        return getEventFileNames(telephoneNumber, filter, parentPath);
    }

    /**
     * Returns names of files selected by the specified filter. 
     * @param telephoneNumber Phone number
     * @param fileFilter Filter that is applied on the query.
     * @param fullPath If true returns the full path name of the file.
     * @return filenames if found, null otherwise.
     */
    @Override
    public String[] getEventFileNames(String telephoneNumber, FileFilter fileFilter, boolean fullPath) {
        File[] files = getEventFiles(telephoneNumber, fileFilter);
            if (files != null && files.length > 0) {
                String[] fileList = new String[files.length];
                for (int i = 0; i < files.length; ++i) {
                    if (fullPath) {
                        fileList[i] = files[i].getPath();
                    } else {
                        fileList[i] = files[i].getName();
                    }
                }
                return fileList;
            } else {
                return null;
            }
    }

    @Override
    public File[] getEventFiles(String telephoneNumber, FileFilter fileFilter) {
            String filePath = generateFilePath(telephoneNumber, "");

            if (filePath == null) {
                return null;
            }

            File parentDirectory = new File(filePath);

            File[] files = parentDirectory.listFiles(fileFilter);
            if (files != null && files.length == 0) {
                files = null;
            }
            return files;

    }
    
    @Override
    public boolean removeFile(String telephoneNumber, String fileName) throws TrafficEventSenderException {
        // Generate path
        boolean internal = isNumberInternal(telephoneNumber);
        String filePath = generateFilePath(telephoneNumber, fileName, internal);

        if (filePath == null) {
            return true;
        }

        return deleteFile(filePath);
    }

    @Override
    public boolean removeFile(String telephoneNumber, String fileName, boolean internal) throws TrafficEventSenderException {
        String filePath = generateFilePath(telephoneNumber, fileName, internal);

        if (filePath == null) {
            return true;
        }

        return deleteFile(filePath);
    }

    @Override
    public boolean removeFile(String absoluteFilePath) throws TrafficEventSenderException {
        if (absoluteFilePath == null) {
            return true;
        }

        return deleteFile(absoluteFilePath);
    }

    private boolean deleteFile(String filePath) throws TrafficEventSenderException {
        File file = new File(filePath);
        if (file.exists()) {
            if (!file.delete()) {
                throw new TrafficEventSenderException("Failed to delete file: " + filePath);
            } else {
                // Do NOT systematically delete the directory when it is empty;
                // Better to leave it there and prevent having to re-create it the next time
                // another event (mcn, slamdown, etc.) needs to be created.  Will avoid
                // the extra i/o operations and avoid potential Gluster gfid issues (MiO5.x)
                //performDirectoryCleanup(filePath);
                return true;
            }
        } else {
            return true;
        }
    }    

    
    /**
     * This method removes both 'events' and '<identity>' directories
     * if they are found to be empty under /opt/mfs/external only.
     * Example: opt/mfs/external/e/2c/76/f9c/private/moip/14145193280/events/<files>
     * MIO 5.0 MIO5_MFS opt/mfs/external/e/2c7/e2c76f9cabcdef12/private_moip_14145193280_events/<files>
     * @param filePath Path of the file
     */
    private static void performDirectoryCleanup(String filePath) {
        try {
        	// MIO 5.0 MIO5_MFS: only one dir to clean up
            File file = new File(filePath);
            File directoryEvents = file.getParentFile();
            if (directoryEvents.getName().endsWith(MfsEventManager.EVENTS_DIRECTORY)) {
            	if (!(directoryEvents.delete())) {
            		logger.debug("Directory '" + directoryEvents.getPath() + "' not empty, not deleted");
            	}
            }
            // MIO 5.0 MIO5_MFS: below are pre-MIO5 old logic
        	/********************************************************************************
            File file = new File(filePath);
            File directoryEvents = file.getParentFile();
            File directoryIdentity = directoryEvents.getParentFile();
            File directoryMoip = directoryIdentity.getParentFile();
            File directoryPrivate = directoryMoip.getParentFile();

            // DirectoryCleanup is exclusively performed on MFS 'external'
            if (filePath.contains(File.separator + "external" + File.separator)) {

                if (MfsEventManager.EVENTS_DIRECTORY.equals(directoryEvents.getName()) &&
                    CommonMessagingAccess.MSG_CLASS.equals(directoryMoip.getName()) &&
                    MfsEventManager.PRIVATE_DIRECTORY.equalsIgnoreCase(directoryPrivate.getName())) {

                    if (directoryEvents.delete()) {
                        logger.debug("Empty '" + MfsEventManager.EVENTS_DIRECTORY + "' directory deleted for " + directoryEvents.getPath());
                        if (directoryIdentity.delete()) {
                            logger.debug("Empty identity '" + directoryIdentity.getName() + "' directory deleted for " + directoryIdentity.getPath());
                        } else {
                            logger.debug("Directory '" + directoryIdentity.getPath() + "' not empty, not deleted");
                        }
                    } else {
                        logger.debug("Directory '" + directoryEvents.getPath() + "' not empty, not deleted");
                    }
                }
            }
            ********************************************************************************/
        } catch (Throwable e) {
            logger.debug("performDirectoryCleanup Exception " + e.getMessage() + " for " + filePath);
        }
    }
    
    

    @Override
    public boolean fileExists(String telephoneNumber, String fileName, boolean internal) {
        return fileExists(telephoneNumber, fileName, 0, internal);
    }

    public boolean fileExists(String telephoneNumber, String fileName, int validityPeriodInMin, boolean internal) {
        boolean result = false;
        FileStatusEnum fileStatus = fileExistsValidation(telephoneNumber, fileName, validityPeriodInMin, internal);
        switch (fileStatus) {
            case FILE_DOES_NOT_EXIST:
            case FILE_EXISTS_AND_INVALID:
                result = false;
                break;
            case FILE_EXISTS_AND_VALID:
            case FILE_EXISTS_NO_VALIDATION:
                result = true;
                break;
            default:
                result = false;
        }
        return result;
    }

    public FileStatusEnum fileExistsValidation(String notificationNumber, String fileName, int validityPeriodInMin) {
        return fileExistsValidation(notificationNumber, fileName, validityPeriodInMin, isInternal(notificationNumber));
    }

    public FileStatusEnum fileExistsValidation(String telephoneNumber, String fileName, int validityPeriodInMin, boolean internal) {
        boolean result = false;

        String filePath = generateFilePath(telephoneNumber, fileName, internal);
        if (filePath == null) {
            return FileStatusEnum.FILE_DOES_NOT_EXIST;
        }

        File file = new File(filePath);

        int maxTries = 5;
        int milliseconds = 10;
        String maxTriesString = System.getProperty("com.abcxyz.vvs.backend.fileexistmaxtries", "5");
        String milliString = System.getProperty("com.abcxyz.vvs.backend.fileexistmilli", "10");
        try {
            maxTries = Integer.parseInt(maxTriesString);
            milliseconds = Integer.parseInt(milliString);
        } catch (NumberFormatException nfex) { ; }

        if (logger.isDebugEnabled()) {
            logger.debug("fileExistsValidation: Checking validity of " + file.getAbsolutePath());
        }

        result = file.exists();
        if (!result) {
            return FileStatusEnum.FILE_DOES_NOT_EXIST;
        }

        if (validityPeriodInMin != 0) {
            long validityPeriodInMilliseconds = validityPeriodInMin*60*1000;
            long currentTime = Calendar.getInstance().getTimeInMillis();

            long lastModified = file.lastModified();
            int tries = 0;
            while (lastModified == 0L && tries++ < maxTries) {
                try {
                    Thread.sleep(milliseconds);
                } catch (InterruptedException ie) {;}

                lastModified = file.lastModified();
                result = file.exists();
                if (!result) {
                    return FileStatusEnum.FILE_DOES_NOT_EXIST;
                }
                logger.info("fileExistsValidation: " + file.getAbsolutePath() + ", tries: " + tries + ", lastModified: " + lastModified + ", exist: " + result);
            }

            result = (lastModified + validityPeriodInMilliseconds) > currentTime;

            if (logger.isDebugEnabled()) {
                logger.debug("Checking validity of " + file.getAbsolutePath() + " lastModified: " + new Date(lastModified).toString() + " validityPeriodInMilliseconds: " + validityPeriodInMilliseconds + " currentTime: " + new Date(currentTime).toString());
            }

            if (result) {
                if (logger.isDebugEnabled()) {
                    logger.debug(fileName + " file found and valid within the validity period");
                }
                return FileStatusEnum.FILE_EXISTS_AND_VALID;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.info(fileName + " file found but older that the validity period lastModified: " + new Date(lastModified).toString() + " validityPeriodInMilliseconds: " + validityPeriodInMilliseconds +" currentTime: "+ new Date(currentTime).toString());
                }
                return FileStatusEnum.FILE_EXISTS_AND_INVALID;
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(fileName + " file found and no validity check performed");
            }
            return FileStatusEnum.FILE_EXISTS_NO_VALIDATION;
        }
    }

    public static boolean renameFile(String telephoneNumber, String oldFileName, String newFileName) {
        boolean isRenamed = false;
        boolean internal = isNumberInternal(telephoneNumber);
        String oldFilePath = generateFilePath(telephoneNumber, oldFileName, internal);
        try {
            File oldFile = new File(oldFilePath);
            File newFile = new File(oldFile.getParentFile(), newFileName);
            isRenamed = oldFile.renameTo(newFile);
        } catch(Throwable t) {
            logger.error("Failed to rename " + oldFileName + " to " + newFileName + ": ", t);
        }
        return isRenamed;
    }

    @Override
    public void createLoginFile(String telephoneNumber) throws TrafficEventSenderException {
        createEmptyFile(telephoneNumber, FILENAME_LOGGEDIN);
    }

    @Override
    public void removeLoginFile(String telephoneNumber) throws TrafficEventSenderException {
        removeFile(telephoneNumber, FILENAME_LOGGEDIN, true);
    }

    @Override
    public boolean loginFileExists(String telephoneNumber) {
        return fileExists(telephoneNumber, FILENAME_LOGGEDIN, 0, true);
    }

    @Override
    public boolean loginFileExistsAndValidDate(String telephoneNumber, int validityPeriod) {
        return fileExists(telephoneNumber, FILENAME_LOGGEDIN, validityPeriod, true);
    }

    @Override
    public void updateConfiguration(MfsConfiguration mfsConfiguration) {
        if (mfsConfiguration != null) {
            // Slamdown
            if (!eventManagerWorkersStarted) {
                initSlamdownWorkers();
            }

            // Mcn
            if (!mcnEventManagerWorkersStarted) {
                initMcnWorkers();
            }
        }
    }

    static ILogger getLogger() {
    	return logger;
    }

    public static ICommonMessagingAccess getMfs() {
    	return mfs;
    }

    /**
     * For testing purpose only.
     * Sets the common messaging access object.
     * @param cma Common Messaging access instance.
     */
    static void setCommonMessagingAccess(ICommonMessagingAccess cma) {
    	mfs = cma;
    }

    /**
     * For testing purpose only.
     * Sets the subscriber directory access instance.
     * @param da Subscriber directory access instance.
     */
    public static void setDirectoryAccess(IDirectoryAccess da) {
    	directory = da;
    }

    /**
     * For testing purpose only.
     * Clears the worker to be able to initialize with a new one.
     */
    static void clearWorker() {

        for (EventManagerWorker eventManagerWorker : eventManagerWorkers) {
            eventManagerWorker.stop();
        }
        for (McnEventManagerWorker mcnEventManagerWorker : mcnEventManagerWorkers) {
            mcnEventManagerWorker.stop();
        }
    }
   
    /**
     * This method will first normalized the string passed
     * after it will remove all the non digital character at the beginning of the strin
     *  
     */
    static public String moipFormatedTelephone(String telephone) {
    	try {
			telephone = CommonMessagingAccess.getInstance().normalizeAddressField(telephone);
		} catch (IdentityFormatterInvalidIdentityException e) {
			logger.error("MfsEventManager.moipFormatedTelephone: Unable to format telephone: " + telephone, e);
		}

		StringBuffer sb = new StringBuffer();
		for (int i=0; i< telephone.length(); i++) {			
			if (Character.isDigit(telephone.charAt(i)))			
				sb.append(telephone.charAt(i));
		}
		
		return new String(sb);
    }

	/* (non-Javadoc)
	 * @see com.mobeon.common.trafficeventsender.mfs.IMfsEventManager#getOutdialEvents(java.lang.String)
	 */
	@Override
	public String[] getOutdialEvents(String number) {
		//
		// Read event list for subscriber.
		//
		String[] files = getFilesNameStartingWith(number, OUTDIAL_EVENT_FILE_PREFIX);
		if (files == null) {
			return null;
		}

		//
		// Package event keys in an array
		//
		String[] keys = new String[files.length];
		for (int i = 0; i < files.length; ++i) {
			keys[i] = files[i].substring(OUTDIAL_EVENT_FILE_PREFIX.length());
		}

		return keys;
	}

    public Properties getProperties(String number, String key) {
    	RandomAccessFile fis = null;
    	FileChannel fc = null;
    	FileLock fLock = null;
		FileReader reader = null;
		Properties props = new Properties();
		try {
		    String filename = generateFilePath(number, key);
			File file = new File(filename);
			if (!file.exists()) {
				// nothing to do here - this could happen if the event has been cancelled
				// without the corresponding .cancel file having been accessible
				logger.debug("Could not find file " + filename);
				return props;
			}
			fis = new RandomAccessFile(file, "rw");
			fc = fis.getChannel();
			fLock = fc.lock();
			
			if (fLock != null) {
				reader = new FileReader(fis.getFD());
				props.load(reader);
				logger.debug("Read file " + filename + ": " + props.toString());
				return props;
			} else {
				throw new Exception("Could not obtain exclusive file lock on " + file);
			}
		} catch (Exception ex) {
		    return null;
		} finally {
			try {
				if (fLock != null) fLock.release();
				if (reader != null) reader.close();
				if (fc != null) fc.close();
				if (fis != null) fis.close();
			} catch (IOException e) {
			    logger.error("Cannot close FileReader/FileLock/FileChannel/FileInputStream when retrieving event " + key + " for " + number + ", ", e);
			}
		}
	}

    public void storeProperties_old(String telephoneNumber, String fileName, Properties props) throws TrafficEventSenderException {
        String rootPath = generateFilePath(telephoneNumber, "");
		if (!validateOrCreateDirectory(new File(rootPath), true)) {
			throw new TrafficEventSenderException("Cannot access: " + rootPath);
		}
		File file = new File(rootPath, fileName);
		FileWriter writer = null;
		try {
		    // Temporary file is necessary so the ODL file is not read before
		    // it has been completely written. Otherwise reader threads may be missing
		    // some important attributes and they will abort the event processing.
	        File tempFile = File.createTempFile(TEMP_PREFIX, null, file.getParentFile());
			writer = new FileWriter(tempFile);
			props.store(writer, null);
			writer.close();
			if (tempFile.renameTo(file) == true) {
			    logger.debug("Wrote to file " + rootPath + fileName + ": " + props.toString());
			} else {
			    String msg = "Cannot rename temporary property file from " + tempFile + " to " + file +
                        ". Discarding file."; 
			    logger.error(msg);
			    if (tempFile.delete() == false) {
			        logger.error("Cannot remove temporary file " + tempFile + ". Leaving in directory.");
			    }
			    throw new TrafficEventSenderException(msg);
			}
		} catch (IOException ioe) {
		    logger.error("Exception while storing properties for " + telephoneNumber + ", fileName: " + fileName + ", ", ioe);
			throw new TrafficEventSenderException(ioe);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
                logger.error("Cannot close writer when storing properties for " + telephoneNumber + ", filename: " + fileName, e);
			}
		}
	}

    
    
    public void storeProperties(String telephoneNumber, String fileName, Properties props) throws TrafficEventSenderException {
        String rootPath = generateFilePath(telephoneNumber, "");
		if (!validateOrCreateDirectory(new File(rootPath), true)) {
			throw new TrafficEventSenderException("Cannot access: " + rootPath);
		}
		File file = new File(rootPath, fileName);
		FileOutputStream fos = null;
		FileChannel fc = null;
		FileLock fLock = null;
		try {
			fos = new FileOutputStream(file);
			fc =  fos.getChannel();
			fLock = fc.tryLock();
			if (fLock != null) {
				props.store(fos, null);
				logger.debug("Wrote to file " + rootPath + fileName + ": " + props.toString());
			} else {
				throw new Exception("Could not obtain exclusive file lock on " + rootPath + fileName);
			}
		} catch (Exception e) {
		    logger.error("Exception while storing properties for " + telephoneNumber + ", fileName: " + fileName + ", ", e);
			throw new TrafficEventSenderException(e);
		} finally {
			try {
				if (fLock != null) fLock.release();
				if (fc != null) fc.close();
				if (fos != null) fos.close();
			} catch (IOException e) {
                logger.error("Cannot close FileLock/FileChannel/FileOutputStream when storing properties for " + telephoneNumber + ", filename: " + fileName, e);
			}
		}
	}
    
    
    
	public boolean isStorageOperationsAvailable(String originator, String recipient) {
	    return CommonMessagingAccess.getInstance().isStorageOperationsAvailable(originator, recipient);
	}
	
	/**
     * Returns the payload files stored for the given telephone number.
     * @param telephoneNumber The telephone number to lookup the payload files.
     * @param type The type of payload files to look up (type_timestamp.payload).
     * @return the payload files stored for the given telephone number, or null if the files could not be retrieved. If there are no files, an empty array is returned.
     */
    public static File[] getPayloadFiles(String telephoneNumber, final String type) {
        // .payload file filter
        FileFilter ff = new FileFilter() {
            @Override
            public boolean accept(File file) {
                /**
                 * Both PATTERN_FILE_PENDING and PATTERN_FILE_FINAL files must be considered since a retry could
                 * happen (example for SMS-Info) while the file as already been set to  PATTERN_FILE_FINAL.
                 */
                String filename = file.getName();
                String filePrefix = filename.split("_")[0];
                return (file.getName().endsWith(PAYLOAD_FILE_EXTENSION) && filePrefix.equalsIgnoreCase(type));
            }
        };
        
        // Generate file path
        String filePath = generateFilePath(telephoneNumber, "");
        if (filePath == null) {
            logger.warn("Could not generate file path for number " + telephoneNumber);
            return null;
        }

        File parentDirectory = new File(filePath);
        File[] files = parentDirectory.listFiles(ff);

        if (logger.isDebugEnabled()) {
            StringBuilder debugMsg = new StringBuilder();
            debugMsg.append("Payload files stored under user ").append(telephoneNumber).append(" of type ").append(type).append(": ");
            if (files != null) {
                for(File file : files) {
                    debugMsg.append(file.toString()).append("; ");
                }
            } else {
                debugMsg.append("null");
            }
            logger.debug(debugMsg.toString());
        }
		
        return files;
        
    }
    
    /**
     * Returns the number of payload files stored for the given telephone number.
     * @param telephoneNumber The telephone number to lookup the payload files.
     * @param type The type of payload files to look up (key.type.payload).
     * @return the number of payload files stored for the given telephone number, or -1 if the files could not be retrieved.
     */
    public static int getPayloadFileCount(String telephoneNumber, String type) {
        File[] f = getPayloadFiles(telephoneNumber, type);
        if (f != null) {
            return f.length;
        }
        return -1;
    }
    
    public static boolean doesPayloadFileExist(String telephoneNumber, String filename) throws TrafficEventSenderException {
        // Generate path
        boolean internal = isNumberInternal(telephoneNumber);
        String path = generateFilePath(telephoneNumber, filename, internal);
        if (path == null) {
            logger.error("Could not generate path for filename: " + filename + ", telephoneNumber: " + telephoneNumber);
            throw new TrafficEventSenderException("No store path for filename: " + filename + ", telephoneNumber: " + telephoneNumber);
        }
        
        // Create File object
        File file = new File(path);
        return file.exists();
    }
    
    /**
     * Returns the payload stored in a file, in bytes.
     * @param telephoneNumber The B phone number.
     * @param filename The name of the file to read.
     * @return the payload stored in the file, in bytes.
     * @throws TrafficEventSenderException if the reading of a payload file failed.
     */
    public static byte[] getBytePayload(String telephoneNumber, String filename) throws TrafficEventSenderException {
        // Generate path
        boolean internal = isNumberInternal(telephoneNumber);
        String path = generateFilePath(telephoneNumber, filename, internal);
        if (path == null) {
            logger.error("Could not generate path for filename: " + path + ", telephoneNumber: " + telephoneNumber);
            throw new TrafficEventSenderException("No store path for filename: " + path + ", telephoneNumber: " + telephoneNumber,
                    TrafficEventSenderExceptionCause.PAYLOAD_FILE_PATH_NOT_ACCESSIBLE);
        }
        
        // Create File object
        File file = new File(path);
        if (!file.exists()) {
            logger.error("Payload file does not exist for filename: " + path + ", telephoneNumber: " + telephoneNumber);
            throw new TrafficEventSenderException("File does not exist; cannot retrieve payload from file " + path,
                    TrafficEventSenderExceptionCause.PAYLOAD_FILE_DOES_NOT_EXIST);
        }
        
        // Read payload from file
        FileInputStream fis = null;
        try {
            byte[] payload = null;
            fis = new FileInputStream(file);
            payload = new byte[(int)file.length()];
            int i = 0;
            int data = fis.read();
            while (data != -1)
            {
                payload[i] = (byte)data;
                data = fis.read();
                i++;
            }
            
            return payload;
        } catch (FileNotFoundException fnfe) {
            logger.error("Payload file could not be found, but exists (FNFE): " + path + ", telephoneNumber: " + telephoneNumber);
            throw new TrafficEventSenderException("File not found; cannot retrieve payload from file " + path,
                    TrafficEventSenderExceptionCause.PAYLOAD_FILE_NOT_ACCESSIBLE);
        } catch (IOException ioe) {
            logger.error("Payload file could not be read (IOE): " + path + ", telephoneNumber: " + telephoneNumber);
            throw new TrafficEventSenderException("File could not be read; cannot retrieve payload from file " + path,
                    TrafficEventSenderExceptionCause.PAYLOAD_FILE_NOT_ACCESSIBLE);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Returns the payload stored in a file, in a String.
     * @param telephoneNumber The B phone number.
     * @param filename The name of the file to read.
     * @return the payload stored in the file, in a String.
     * @throws TrafficEventSenderException if the reading of a payload file failed.
     */
    public static String getStringPayload(String telephoneNumber, String filename) throws TrafficEventSenderException {
        // Generate path
        boolean internal = isNumberInternal(telephoneNumber);
        String path = generateFilePath(telephoneNumber, filename, internal);
        if (path == null) {
            logger.error("Could not generate path for filename: " + path + ", telephoneNumber: " + telephoneNumber);
            throw new TrafficEventSenderException("No store path for filename: " + path + ", telephoneNumber: " + telephoneNumber,
                    TrafficEventSenderExceptionCause.PAYLOAD_FILE_PATH_NOT_ACCESSIBLE);
        }
        
        // Create File object
        File file = new File(path);
        if (!file.exists()) {
            logger.error("Payload file does not exist for filename: " + path + ", telephoneNumber: " + telephoneNumber);
            throw new TrafficEventSenderException("File does not exist; cannot retrieve payload from file " + path,
                    TrafficEventSenderExceptionCause.PAYLOAD_FILE_DOES_NOT_EXIST);
        }
        
        // Read payload from file
        String payload = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            int charRead = fis.read();
            while (charRead != -1)
            {
                payload += (char)charRead;
                charRead = fis.read();
            }
        } catch (FileNotFoundException fnfe) {
            logger.error("Payload file could not be found, but exists (FNFE): " + path + ", telephoneNumber: " + telephoneNumber);
            throw new TrafficEventSenderException("File does not exist; cannot retrieve payload from file " + path,
                    TrafficEventSenderExceptionCause.PAYLOAD_FILE_NOT_ACCESSIBLE);
        } catch (IOException ioe) {
            logger.error("Payload file could not be read (IOE): " + path + ", telephoneNumber: " + telephoneNumber);
            throw new TrafficEventSenderException("File does not exist; cannot retrieve payload from file " + path,
                    TrafficEventSenderExceptionCause.PAYLOAD_FILE_NOT_ACCESSIBLE);
        }
        
        return payload;
    }
    
    /**
     * Stores a payload to a file.
     * @param telephoneNumber The B phone number.
     * @param filename The name of the file to store.
     * @param type The type of payload (bytes, String, etc.) to store.
     * @param payload The String representation of the payload to store.
     * @throws TrafficEventSenderException if the storing of the payload into a file failed.
     */
    public void storePayload(String telephoneNumber, String filename, PayloadType type, String payload) throws TrafficEventSenderException {
        // Generate bytes
        byte[] bytesToWrite;
        if (type.equals(PayloadType.STRING)) {
            bytesToWrite = payload.getBytes();
        }
        else if (type.equals(PayloadType.BYTES)) {
            bytesToWrite = stringToByteArray(payload);
        }
        else {
            throw new TrafficEventSenderException("Unable to convert payload to bytes: telephoneNumber: " + telephoneNumber + ", payload: " + payload);
        }
        
        // Generate path
        boolean internal = isInternal(telephoneNumber);
        String path = generateFilePath(telephoneNumber, filename, internal);
        if (path == null) {
            throw new TrafficEventSenderException("No store path for user: " + telephoneNumber);
        }
        
        // Create file
        File file = new File(path);
        try {
            if (file.exists()) {
                file.setLastModified(Calendar.getInstance().getTimeInMillis());
            }
            if (!file.exists() && !file.createNewFile()) {
                throw new TrafficEventSenderException("File not exists and creation failure" + file);
            }
        } catch (IOException e) {
            // Check if the MOIP private folder is present
            File rootPath = new File(generateRootPath(telephoneNumber, internal));
            if (!validateOrCreateDirectory(rootPath, true)) {
                logger.error("Failed to create MOIP private directory for " + telephoneNumber + ", " + filename + " no store path created. ", e);
                throw new TrafficEventSenderException("Failed to create MOIP private directory " + rootPath);
            }
            // Check if the subscriber's event directory is present
            File eventPath = file.getParentFile();
            if (!validateOrCreateDirectory(eventPath, false)) {
                logger.error("Failed to create subscriber's event directory for " + telephoneNumber + ", " + filename, e);
                throw new TrafficEventSenderException("Failed to create subscriber's event directory " + eventPath);
            }
            // Create the empty file
            tryCreateEmptyFile(file);
        }
        
        // Write payload to file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytesToWrite);
        } catch (IOException ioe) {
            logger.error("Exception while storing payload for " + telephoneNumber + ", fileName: " + filename, ioe);
            throw new TrafficEventSenderException("Exception while storing payload for " + telephoneNumber + ", fileName: " + filename + ", payload: " + payload);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                logger.error("Cannot close FileOutputStream when storing payload for " + telephoneNumber + ", filename: " + filename + ", payload: " + payload, e);
                throw new TrafficEventSenderException("Exception while closing file for " + telephoneNumber + ", fileName: " + filename + ", payload: " + payload);
            }
        }
    }

    /**
     * Returns the byte array represented by the string. The string must be of the format
     * <code>"xxyyzz..."</code> where <code>xx</code>, <code>yy</code> and <code>zz</code>
     * are hexadecimal numbers (00, a7, ff, etc.).
     * 
     * If there is a problem in parsing the String, and empty array ([]) is returned.
     * 
     * @param code The hexadecimal code String.
     * @return The array of bytes corresponding to the given String.
     */
    private byte[] stringToByteArray(String code) throws TrafficEventSenderException {
        try {
            byte[] empty = {};
            if (code == null || code.length() == 0) return empty;
            byte[] array = new byte[code.length() / 2];
            for (int i = 0; i < array.length; i++) {
                array[i] = (byte) ((Character.digit(code.charAt(2*i), 16) << 4) + Character.digit(code.charAt(2*i + 1), 16));
            }
            return array;
        } catch (Exception e) {
            throw new TrafficEventSenderException("Unable to convert payload to bytes: payload: " + code);
        }
    }
    
    
    public long getLastModified(File file) throws TrafficEventSenderException {
    	if (file != null) {
    		try {
    			return file.lastModified();	
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
			FileReader reader = null;
			try {
				reader = new FileReader(file);
			} catch (FileNotFoundException e) {
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
		if (orig != null && dest != null) {
			return orig.renameTo(dest);
		}
    	String msg = "Exception while renaming the event file. One or both provided file were null.";
    	logger.error(msg);
    	throw new TrafficEventSenderException(msg);
	}
}
