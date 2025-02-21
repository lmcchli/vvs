package com.mobeon.common.trafficeventsender;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.scheduler.SchedulerFactory;
import com.abcxyz.messaging.scheduler.SchedulerManager;
import com.abcxyz.messaging.scheduler.SchedulerStartFailureException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;

/**
 * Handles the TrafficEvents of MFS type
 *
 * @author estberg
 *
 */
public class MfsClient {
    /** Event property name for the telephone number */
    public static final String PROPERTY_USERNAME = "username";

    private static ILogger log = ILoggerFactory.getILogger(MfsClient.class);

    /** The names of the supported events */
    public static final String EVENT_LOGOUTINFORMATION = "logoutinformation";
    public static final String EVENT_LOGININFORMATION = "logininformation";
    public static final String EVENT_SLAMDOWNINFORMATION = "slamdowninformation";
    public static final String EVENT_SENDSTATUSINFORMATION = "sendstatusinformation";
    public static final String EVENT_MISSEDCALLNOTIFICATION = "missedcallnotification";
    public static final String EVENT_MWIOFF = "mwioff";
    public static final String EVENT_MAILBOXUPDATE = "mailboxupdate";
    private static final String GREETING_CHANGED_KEY = "GreetingChanged";
    private static final String EVENT_VVASMS = "vvasms";
    public static final String EVENT_DELAYEDEVENT = "delayedevent";
    private static final String EVENT_AUTOUNLOCKPIN = "autounlockpin";

    /** List of mandatory properties for the slamdown event */
    private static ArrayList<String> slamdownProperties = new ArrayList<String>();

    /** List of mandatory properties for the sendstatus event */
    private static ArrayList<String> sendstatusProperties = new ArrayList<String>();

    /** List of mandatory properties for the MCN event */
    private static ArrayList<String> mcnProperties = new ArrayList<String>();

    /** IMfsEventManager that deals with the MFS */
    protected IMfsEventManager eventManager;

    private ICommonMessagingAccess mfs;

    static {
        // Populate slamdown properties
        slamdownProperties.add(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY);
        slamdownProperties.add(MoipMessageEntities.SLAMDOWN_CALLING_NUMBER_PROPERTY);
        slamdownProperties.add(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY);

        // Populate Mcn properties
        mcnProperties.add(MoipMessageEntities.MCN_CALLED_NUMBER_PROPERTY);
        mcnProperties.add(MoipMessageEntities.MCN_CALLING_NUMBER_PROPERTY);
        mcnProperties.add(MoipMessageEntities.MCN_TIMESTAMP_PROPERTY);
        
        sendstatusProperties.add(MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY);
        sendstatusProperties.add(MoipMessageEntities.SLAMDOWN_CALLING_NUMBER_PROPERTY);
        sendstatusProperties.add(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY);        
    }

    public MfsClient() throws SchedulerStartFailureException {
        super();
        //init the oam to avoid exception when calling initMcnWorkers
        OAMManager mrdOam = CommonOamManager.getInstance().getMrdOam();
        SchedulerManager scheduler = SchedulerFactory.getSchedulerManager();
        try {
            scheduler.init(mrdOam);
        } catch(SchedulerStartFailureException e) {
            log.error("Catch SchedulerStartFailureException.");
            throw e;
        }

        mfs = CommonMessagingAccess.getInstance();
        eventManager = createMfsEventManager();
        ((MfsEventManager)eventManager).initMcnWorkers();
        ((MfsEventManager)eventManager).initSlamdownWorkers();
    }

    /**
     * Constructor for testing purposes
     * @param eventManager
     */
    protected MfsClient(IMfsEventManager eventManager, IDirectoryAccess directoryAccess, ICommonMessagingAccess mfs) {
        super();
        this.eventManager = eventManager;
        this.mfs = mfs;
    }

    /**
     * Sends a TrafficEvent via the Mfs interface.
     *
     * @param trafficEvent
     * @throws TrafficEventSenderException if some error with the event
     */
    public void sendTrafficEvent(TrafficEvent trafficEvent) throws TrafficEventSenderException {
        if (log.isDebugEnabled()) log.debug("In sendTrafficEvent: sending trafficevent " + trafficEvent.getName());

        String eventName = trafficEvent.getName();

        // Verify that the mandatory phone number property isn't null
        String telephoneNumber = getEventPropertyValue(trafficEvent, MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY);
        if(telephoneNumber == null || telephoneNumber.length() <= 0){
        	telephoneNumber = getEventPropertyValue(trafficEvent, PROPERTY_USERNAME);
        }
        if (telephoneNumber == null || telephoneNumber.length() <= 0) {
            //throw new TrafficEventSenderException("Mandatory property " + MoipMessageEntities.SLAMDOWN_CALLED_NUMBER_PROPERTY +
            //        " missing from event " + eventName);
        	log.warn("MfsClient.sendTrafficEvent: telephoneNumber was not populated in this traffic event");
        	return;
        }

        // Handle the specific events
        if (eventName.equals(EVENT_SLAMDOWNINFORMATION)) {
            // in case event has timestamp, we need to preserve the timestamp (case move subscriber)
            if(trafficEvent.getProperties().get(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY)==null ||trafficEvent.getProperties().get(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY).equals("")) {                
            	String timestamp = String.valueOf(System.currentTimeMillis());
            	trafficEvent.setProperty(MoipMessageEntities.SLAMDOWN_TIMESTAMP_PROPERTY, timestamp);
            }
        	if (log.isDebugEnabled()){
        	    log.debug("Event name: " + trafficEvent.getName() + ", properties: " + trafficEvent.getProperties().toString());
        	}

        	validateSlamdownProperties(trafficEvent);
            eventManager.storeEvent(telephoneNumber, trafficEvent);
        }
        else if (eventName.equals(EVENT_MISSEDCALLNOTIFICATION)) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            trafficEvent.setProperty(MoipMessageEntities.MCN_TIMESTAMP_PROPERTY, timestamp);
            validateMcnProperties(trafficEvent);
            eventManager.storeEvent(telephoneNumber, trafficEvent);
        }
        else if (eventName.equals(EVENT_MWIOFF)) {
        	eventManager.storeEvent(telephoneNumber, trafficEvent);
        }
        else if (eventName.equals(EVENT_MAILBOXUPDATE)) {
        	eventManager.storeEvent(telephoneNumber, trafficEvent);
        }
        else if (eventName.equals(EVENT_LOGININFORMATION)) {
            // Login information events should create a login lock file
            eventManager.createLoginFile(telephoneNumber);
        }
        else if (eventName.equals(EVENT_LOGOUTINFORMATION)) {
            // Logout information events should remove the login lock file
            eventManager.removeLoginFile(telephoneNumber);

            // On logout, we should notify NTF to perform a sync message with vvm client
            Properties  properties = new Properties();
            properties.putAll(trafficEvent.getProperties());
            IDirectoryAccessSubscriber subscriber = DirectoryAccess.getInstance().lookupSubscriber(telephoneNumber);
            String msid = subscriber.getSubscriberIdentity(MCDConstants.IDENTITY_SCHEME_MSID);
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.rmsa = new MSA(msid);

            //Not using the logout event anymore for the VVM when the user exit, using more specific event so the update to the phone are
            //not sent as often.
            //CommonMessagingAccess.getInstance().notifyNtf(EventTypes.LOG_OUT, messageInfo, telephoneNumber, properties);

            String greetingChange = properties.getProperty(GREETING_CHANGED_KEY);
            if("true".equals(greetingChange)){
                CommonMessagingAccess.getInstance().notifyNtf(EventTypes.GREETING_CHANGED, messageInfo, telephoneNumber, properties);
            }
        }
        else if (eventName.startsWith(EVENT_VVASMS)){
            eventManager.storeVvaSms(telephoneNumber, trafficEvent);
        }
        else if (eventName.startsWith(EVENT_AUTOUNLOCKPIN)){
            eventManager.storeAutoUnlockPinLockout(telephoneNumber, trafficEvent);
        }
        else {
            log.debug("Unrecognized generic event type; will be stored and dealt with in NTF. eventName: " + eventName);
            eventManager.storeEvent(telephoneNumber, trafficEvent);
        }
    }

    /**
     * Creates the IMfsEventManager that deals with MFS access
     * @return IMfsEventManager interface
     */
    protected IMfsEventManager createMfsEventManager() {
    	MfsEventManager eventManager = MfsEventFactory.getMfsEvenManager();
        return eventManager;
    }

    protected IDirectoryAccess getDirectoryAccess() {
        return DirectoryAccess.getInstance();
    }

    protected ICommonMessagingAccess getMFS() {
        return mfs;
    }

    /**
     * Validates that the slam down traffic event has its mandatory properties
     * @param trafficEvent The TrafficEvent to examine
     * @throws TrafficEventSenderException Thrown if the event is missing some properties.
     */
    private static void validateSlamdownProperties(TrafficEvent trafficEvent) throws TrafficEventSenderException {
        Map<String, String> properties = trafficEvent.getProperties();
        StringBuilder propertyNames = new StringBuilder("Missing propertie(s) for slam down event:");
        boolean error = false;
        for (String key : slamdownProperties) {
            if (!properties.containsKey(key)) {
                propertyNames.append(" " + key);
                log.debug(propertyNames);
                error = true;
            }
        }

        if (error) {
            throw new TrafficEventSenderException(propertyNames.toString());
        }
    }

    /**
     * Validates that the sendstatus traffic event has its mandatory properties
     * @param trafficEvent The TrafficEvent to examine
     * @throws TrafficEventSenderException Thrown if the event is missing some properties.
     */
    private static void validateSendStatusProperties(TrafficEvent trafficEvent) throws TrafficEventSenderException {
        Map<String, String> properties = trafficEvent.getProperties();
        StringBuilder propertyNames = new StringBuilder("Missing propertie(s) for sendStatus event:");
        boolean error = false;
        for (String key : slamdownProperties) {
            if (!properties.containsKey(key)) {
                propertyNames.append(" " + key);
                log.debug("hm71813 "+propertyNames);
                error = true;
            }
        }
        
        if (error) {
            throw new TrafficEventSenderException(propertyNames.toString());
        }
    }
    
    /**
     * Validates that the MCN traffic event has its mandatory properties
     * @param trafficEvent The TrafficEvent to examine
     * @throws TrafficEventSenderException Thrown if the event is missing some properties.
     */
    private static void validateMcnProperties(TrafficEvent trafficEvent) throws TrafficEventSenderException {
        Map<String, String> properties = trafficEvent.getProperties();
        StringBuilder propertyNames = new StringBuilder("Missing propertie(s) for MCN event:");
        boolean error = false;
        for (String key : mcnProperties) {
            if (!properties.containsKey(key)) {
                propertyNames.append(" " + key);
                error = true;
            }
        }

        if (error) {
            throw new TrafficEventSenderException(propertyNames.toString());
        }
    }

    /**
     * Gets one property value from the event
     * @param trafficEvent The TrafficEvent to get the property from
     * @param propertyName The name of the property
     * @return The string value of the property, or null if the property was not found
     */
    protected String getEventPropertyValue(TrafficEvent trafficEvent, String propertyName) {
        Map<String, String> properties = trafficEvent.getProperties();
        String comp = properties.get(propertyName);
        return comp;
    }

    void updateConfiguration() {

        eventManager.updateConfiguration(TrafficEventSenderConfiguration.getInstance().getMfsConfiguration());
    }
}
