/**
 * Copyright (c) 2010 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.vvm;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.sms.SMSClient;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * Container class for a VVM event
 */
public class VvmEvent extends NtfEvent {

    private static LogAgent log = NtfCmnLogger.getLogAgent(VvmEvent.class);

    
    /** VVM properties */
    public static final String RECIPIENT_ID = "rpt";
    public static final String NOTIFICATION_TYPE = "ntft";
    public static final String CALLER = "caller";
    public static final String SERVICE_TYPE_KEY = "stkey";
    public static final String OTHER_NOTIFICATION_SENT = "ons";

    
    /** Private members */
    private String subscriberNumber;
    private VvmEventTypes notificationType;
    private int currentEvent;
    private int currentState;
    private UserInfo userInfo;
    private NotificationEmail email;
    private UserMailbox userMailbox;
    private String callerNumber = null;
    private boolean otherNotificationSent = false;
    private SchedulerIds schedulerIds = null;
    private long phoneOnLockId = 0L;
    
    /**
     * Individual file for each Vvm notification type
     */
    protected static final String VVM_DEPOSIT_STATUS_FILE = "vvmdeposit.status";
    protected static final String VVM_GREETING_STATUS_FILE = "vvmgreeting.status";
    protected static final String VVM_EXPIRY_STATUS_FILE = "vvmexpiry.status";
    protected static final String VVM_LOGOUT_STATUS_FILE = "vvmlogout.status";
    
    protected static final String VVM_DEP_PHONE_ON_LOCK_FILE = "vvmdep_phoneon.lock";
    protected static final String VVM_GRE_PHONE_ON_LOCK_FILE = "vvmgre_phoneon.lock";
    protected static final String VVM_EXP_PHONE_ON_LOCK_FILE = "vvmexp_phoneon.lock";
    protected static final String VVM_LOG_PHONE_ON_LOCK_FILE = "vvmlog_phoneon.lock";

    /** VVM notificationTypes */
    public enum VvmEventTypes {

        VVM_DEPOSIT       ("VVM_Deposit",       "vvmdep",      SMSClient.TYPE_VVM_DEP, VVM_DEPOSIT_STATUS_FILE      , VVM_DEP_PHONE_ON_LOCK_FILE),
        VVM_GREETING      ("VVM_Greeting",      "vvmgre",      SMSClient.TYPE_VVM_GRE, VVM_GREETING_STATUS_FILE     , VVM_GRE_PHONE_ON_LOCK_FILE),
        VVM_EXPIRY        ("VVM_Expiry",        "vvmexp",      SMSClient.TYPE_VVM_EXP, VVM_EXPIRY_STATUS_FILE       , VVM_EXP_PHONE_ON_LOCK_FILE),
        VVM_LOGOUT        ("VVM_Logout",        "vvmlog",      SMSClient.TYPE_VVM_LOG, VVM_LOGOUT_STATUS_FILE       , VVM_LOG_PHONE_ON_LOCK_FILE),
        APPLEVVM_DEPOSIT  ("AppleVVM_Deposit",  "applevvmdep", SMSClient.TYPE_APPLEVVM_DEP, VVM_DEPOSIT_STATUS_FILE , VVM_DEP_PHONE_ON_LOCK_FILE),
        APPLEVVM_GREETING ("AppleVVM_Greeting", "applevvmgre", SMSClient.TYPE_APPLEVVM_GRE, VVM_GREETING_STATUS_FILE, VVM_GRE_PHONE_ON_LOCK_FILE),
        APPLEVVM_EXPIRY   ("AppleVVM_Expiry",   "applevvmexp", SMSClient.TYPE_APPLEVVM_EXP, VVM_EXPIRY_STATUS_FILE  , VVM_EXP_PHONE_ON_LOCK_FILE),
        APPLEVVM_LOGOUT   ("AppleVVM_Logout",   "applevvmlog", SMSClient.TYPE_APPLEVVM_LOG, VVM_LOGOUT_STATUS_FILE  , VVM_LOG_PHONE_ON_LOCK_FILE);
        
        private String name;
        private String template;
        private int smsRequestType;
        private String statusFileName;
        private String lockFileName;

        VvmEventTypes(String name, String template, int smsRequestType, String statusFileName, String lockFileName) {
            this.name = name;
            this.template = template;
            this.smsRequestType = smsRequestType;
            this.statusFileName = statusFileName;
            this.lockFileName = lockFileName;
        }

        public String getName() {
            return name;
        }

        public String getTemplate() {
            return template;
        }

        public int getSmsRequestType() {
            return smsRequestType;
        }
        
        public String getStatusFileName() {
            return statusFileName;
        }
        
        public String getLockFileName() {
            return lockFileName;
        }
    }

    /** Vvm states */
    public static final byte STATE_SENDING_INFO = 0;
    public static final byte STATE_SENDING_PHONE_ON = 1;
    public static final byte STATE_WAITING_PHONE_ON = 2;
    public static final byte STATE_DEACTIVATOR = 3;
    public static final byte STATE_ACTIVATOR = 4;

    public static final String[] STATE_STRING = {
        "Sending Info",
        "Sending Unit PhoneOn",
        "Waiting PhoneOn",
        "Deactivator",
        "Activity Detected"
    };
    
    /** Vvm events */
    //Vvm notifs event
    public static final int VVM_EVENT_SENDING = 0; 
    public static final int VVM_EVENT_SMS_UNIT_SUCCESSFUL = 1; 
    public static final int VVM_EVENT_SMS_UNIT_RETRY = 2; 
    public static final int VVM_EVENT_SMS_UNIT_FAILED = 3; 
    public static final int VVM_EVENT_SCHEDULER_RETRY = 4; 
    public static final int VVM_EVENT_SCHEDULER_EXPIRY = 5;
    //PhoneOn events for Sms-0
    public static final int VVM_EVENT_PHONE_ON_OK = 6;
    public static final int VVM_EVENT_PHONE_ON_RETRY = 7;
    public static final int VVM_EVENT_PHONE_ON_FAILED = 8;
    public static final int VVM_EVENT_PHONE_ON_SENT_SUCCESSFULLY = 9;
    public static final int VVM_EVENT_PHONE_ON_CLIENT_RETRY = 10;
    public static final int VVM_EVENT_PHONE_ON_CLIENT_FAILED = 11;
    //Deactivator events
    // No events for this state. Deactivator events are handled by VVM_EVENT_SCHEDULER_RETRY
    //Activity Detected event 
    public static final int VVM_EVENT_ACTIVITY_DETECTED = 12;

    public static final String[] VVM_EVENTS_STRING = {
        "VVM_Event_Sending",
        "VVM_Event_Sms_Unit_Successful",
        "VVM_Event_Sms_Unit_Retry",
        "VVM_Event_Sms_Unit_Failed",
        "VVM_Event_Scheduler_Retry",
        "VVM_Event_Scheduler_Expiry",
        "VVM_Event_Phone_On_Ok",
        "VVM_Event_Phone_On_Retry",
        "VVM_Event_Phone_On_Failed",
        "VVM_Event_Phone_On_Sent_Successfully",
        "VVM_Event_Phone_On_Client_Retry",
        "VVM_Event_Phone_On_Client_Failed",
        "VVM_Event_Activity_Detected"
    };

    private static IMfsEventManager _mfsEventManager;
    
    /**
     * Constructor for VvmEvent when content from Scheduler (properties)
     * @param props properties
     */
    protected VvmEvent(Properties props) {

        if (props != null) {
            subscriberNumber = props.getProperty(RECIPIENT_ID);
            notificationType = getNotificationType(props);
            
            String prop = props.getProperty(CALLER);
            if(prop != null){
                this.callerNumber = prop;
            }
            
            prop = props.getProperty(SERVICE_TYPE_KEY);
            if(prop != null) {
                String serviceTypeKey = parseEventServiceType(prop);
                if(serviceTypeKey != null) {
                    setEventServiceTypeKey(serviceTypeKey);
                }
            }
            
            prop = props.getProperty(OTHER_NOTIFICATION_SENT);
            if(prop != null){
                this.otherNotificationSent = Boolean.parseBoolean(prop);
            }
            
            super.parseMsgInfo(props);
        }
        
        this.currentEvent = VvmEvent.VVM_EVENT_SENDING;
        this.currentState = STATE_SENDING_INFO;
        this.schedulerIds = new SchedulerIds();
    }
    
    protected void initUser(UserInfo userInfo) throws MsgStoreException {
        setNotificationEmail();
        this.userInfo = userInfo;
        this.userMailbox = email.getUserMailbox();
    }

    /**
     * Constructor for VvmEvent when content from VvmHanlder
     * @param subscriberNumber String
     * @param userInfo UserInfo
     * @param notificationEmail NotificationEmail
     * @param userMailbox UserMailbox
     * @param notificationType VvmEventTypes
     * @param callerNumber String
     * @param otherNotificationSent boolean
     */
    public VvmEvent(String subscriberNumber, UserInfo userInfo, NotificationEmail notificationEmail, UserMailbox userMailbox,
                    VvmEventTypes notificationType, String callerNumber, boolean otherNotificationSent) {
        this.subscriberNumber = subscriberNumber;
        this.notificationType = notificationType;
        this.currentEvent = VvmEvent.VVM_EVENT_SENDING;
        this.currentState = STATE_SENDING_INFO;
        this.userInfo = userInfo;
        this.userMailbox = userMailbox;
        this.callerNumber = callerNumber;
        this.email = notificationEmail;
        this.otherNotificationSent = otherNotificationSent;
        this.schedulerIds = new SchedulerIds();
        setMsgInfo(email.getNtfEvent().getMsgInfo());
    }
       
    
    private void setNotificationEmail() throws MsgStoreException {
        email = new NotificationEmail(this);
        if( this.notificationType == VvmEvent.VvmEventTypes.VVM_DEPOSIT ) {
            /**
             * OMTP VVM DEPOSIT notification needs information specific to the voice message, not only the mailbox
             */
            email.initFallback();
        } else {
            email.init();
        }
                
        
    }

    /**
     * Get the notificationType (VvmEventTypes) from the property string
     * @param props Properties
     * @return VvmEventTypes
     */
    protected static VvmEventTypes getNotificationType(Properties props) {
        VvmEventTypes vvmEventType = null;
        
        if(props == null || props.getProperty(NOTIFICATION_TYPE) == null) {
            return null;
        }
        
        String prop = props.getProperty(NOTIFICATION_TYPE);

        if (prop.equalsIgnoreCase(VvmEventTypes.VVM_DEPOSIT.getTemplate())) {
            vvmEventType = VvmEventTypes.VVM_DEPOSIT;
        } else if (prop.equalsIgnoreCase(VvmEventTypes.VVM_GREETING.getTemplate())) {
            vvmEventType = VvmEventTypes.VVM_GREETING;
        } else if (prop.equalsIgnoreCase(VvmEventTypes.VVM_EXPIRY.getTemplate())) {
            vvmEventType = VvmEventTypes.VVM_EXPIRY;
        } else if (prop.equalsIgnoreCase(VvmEventTypes.VVM_LOGOUT.getTemplate())) {
            vvmEventType = VvmEventTypes.VVM_LOGOUT;
        } else if (prop.equalsIgnoreCase(VvmEventTypes.APPLEVVM_DEPOSIT.getTemplate())) {
            vvmEventType = VvmEventTypes.APPLEVVM_DEPOSIT;
        } else if (prop.equalsIgnoreCase(VvmEventTypes.APPLEVVM_GREETING.getTemplate())) {
            vvmEventType = VvmEventTypes.APPLEVVM_GREETING;
        } else if (prop.equalsIgnoreCase(VvmEventTypes.APPLEVVM_EXPIRY.getTemplate())) {
            vvmEventType = VvmEventTypes.APPLEVVM_EXPIRY;
        } else if (prop.equalsIgnoreCase(VvmEventTypes.APPLEVVM_LOGOUT.getTemplate())) {
            vvmEventType = VvmEventTypes.APPLEVVM_LOGOUT;
        }
        return vvmEventType; 
    }
    
    private String parseEventServiceType(String key) {
        String result = "";
        
        if(key.equalsIgnoreCase(NtfEventTypes.VVM_SMS_INFO.getName())) {
            result = NtfEventTypes.VVM_SMS_INFO.getName();
        } else if(key.equalsIgnoreCase(NtfEventTypes.VVM_SENDING_PHONEON.getName())) {
            result = NtfEventTypes.VVM_SENDING_PHONEON.getName();
        } else if(key.equalsIgnoreCase(NtfEventTypes.VVM_WAIT_PHONEON.getName())) {
            result = NtfEventTypes.VVM_WAIT_PHONEON.getName();
        } else if(key.equalsIgnoreCase(NtfEventTypes.VVM_DEACTIVATOR.getName())) {
            result = NtfEventTypes.VVM_DEACTIVATOR.getName();
        } else {
            log.debug("Invalid EventServiceType: " + key);
        }
        
        return result;
    }

    /**
     * Put the private members in Properties
     */
    public Properties getEventProperties() {
        Properties props = new Properties();
        if(notificationType != null ) {
            props.put(NOTIFICATION_TYPE, "" + notificationType.getTemplate());
        }
        super.addMsgProperties(props);
        if(callerNumber != null) {
            props.put(CALLER, "" + callerNumber);
        }
        props.put(RECIPIENT_ID, "" + subscriberNumber);
        props.put(OTHER_NOTIFICATION_SENT, "" + otherNotificationSent);
        props.put(Constants.DEST_RECIPIENT_ID, "" + subscriberNumber);
        return props;
    }

    /**
     * Obtains the subscriber number
     * @return phoneNumber on which the slam down occurred
     */
    public String getSubscriberNumber(){
        return subscriberNumber;
    }

    /** Get notificationType */
    public VvmEventTypes getNotificationType() {
        return this.notificationType;
    }

    /** Get currentState **/
    public int getCurrentState() {
        return this.currentState;
    }
    
    /** Set currentState **/
    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    /** Get phoneOnLockId **/
    public long getPhoneOnLockId() {
        return this.phoneOnLockId;
    }
    
    /** Set phoneOnLockId **/
    public void setPhoneOnLockId(long lockId) {
        this.phoneOnLockId = lockId;
    }
    
    /** Get currentEvent */
    public int getCurrentEvent() {
        return this.currentEvent;
    }
    
    /** Set current Event */
    public void setCurrentEvent(int currentEvent) {
        this.currentEvent = currentEvent;
    }

    /** Get UserInfo */
    public UserInfo getUserInfo() {
        return this.userInfo;
    }

    /** Get NotificationEmail */
    public NotificationEmail getNotificationEmail() {
        return this.email;
    }

    /** Get UserMailbox */
    public UserMailbox getUserMailbox() {
        return this.userMailbox;
    }

    /** Get caller number */
    public String getCallerNumber() {
        return this.callerNumber;
    }

    public boolean wasOtherNotificationSent() {
        return this.otherNotificationSent;
    }
    
    public String toString() {
        return "VvmEvent: " + subscriberNumber + " , notificationType : " + notificationType.getName();  
    }
    
    public SchedulerIds getSchedulerIds() {
        return this.schedulerIds;
    }
    
    /**
     * Retrieves the persistent scheduler-id values from the subscriber private/events directory.
     * Values are stored in the SchedulerIds private member.
     */
    public void retrieveSchedulerEventIdsPersistent() {
        if (_mfsEventManager == null) {
            _mfsEventManager = MfsEventFactory.getMfsEvenManager();
        }

        Properties properties = _mfsEventManager.getProperties(this.getSubscriberNumber(), this.notificationType.getStatusFileName());
        if (properties != null) {
            schedulerIds.setSmsInfoEventId(properties.getProperty(SchedulerIds.SMS_INFO_EVENT_ID));
            schedulerIds.setSendingUnitPhoneOnEventId(properties.getProperty(SchedulerIds.SENDING_UNIT_PHONE_ON_EVENT_ID));
            schedulerIds.setWaitingPhoneOnEventId(properties.getProperty(SchedulerIds.WAITING_PHONE_ON_EVENT_ID));
            schedulerIds.setDeactivatorEventId(properties.getProperty(SchedulerIds.DEACTIVATOR_EVENT_ID));
            log.debug("Read the " + this.notificationType.getStatusFileName() + " file for " + this.getSubscriberNumber() + " and retrieved " + schedulerIds);
        }
    }
    
    /**
     * Update the scheduler-id values for a subscriber in the private/events directory.
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
                _mfsEventManager.removeFile(this.getSubscriberNumber(), this.notificationType.getStatusFileName());
                log.debug("Removed the " + this.notificationType.getStatusFileName() + " file for " + this.getSubscriberNumber() + " (if it existed)");
            } else {
                Properties properties = new Properties();
                properties.setProperty(SchedulerIds.SMS_INFO_EVENT_ID, schedulerIds.getSmsInfoEventId() != null ? schedulerIds.getSmsInfoEventId() : "" );
                properties.setProperty(SchedulerIds.SENDING_UNIT_PHONE_ON_EVENT_ID, schedulerIds.getSendingUnitPhoneOnEventId() != null ? schedulerIds.getSendingUnitPhoneOnEventId() : "" );
                properties.setProperty(SchedulerIds.WAITING_PHONE_ON_EVENT_ID, schedulerIds.getWaitingPhoneOnEventId() != null ? schedulerIds.getWaitingPhoneOnEventId() : "" );
                properties.setProperty(SchedulerIds.DEACTIVATOR_EVENT_ID, schedulerIds.getDeactivatorEventId() != null ? schedulerIds.getDeactivatorEventId() : "" );

                log.debug("Storing the new schedulerIds values for " + this.getSubscriberNumber() + "\n" + schedulerIds);
                _mfsEventManager.storeProperties(this.getSubscriberNumber(), this.notificationType.getStatusFileName(), properties);
            }
        } catch (TrafficEventSenderException tese) {
            log.error("Exception while VvmEvent.updateEventIdsPersistent", tese);
            result = false;
        }

        return result;
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
                
                if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.VVM_SMS_INFO.getName())){
                    schedulerIds.setSmsInfoEventId(nextEventId);
                } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.VVM_SENDING_PHONEON.getName())){
                    schedulerIds.setSendingUnitPhoneOnEventId(nextEventId);
                } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.VVM_WAIT_PHONEON.getName())){
                    schedulerIds.setWaitingPhoneOnEventId(nextEventId);
                } else if (eventId.getServiceName().equalsIgnoreCase(NtfEventTypes.VVM_DEACTIVATOR.getName())){
                    schedulerIds.setDeactivatorEventId(nextEventId);
                } else {
                    log.error("Invalid eventId: " + eventInfo.getEventId());
                }
            }
        } catch (InvalidEventIDException iei) {
            log.error("Invalid eventId: " + eventInfo.getEventId(), iei);
        }
    }

    
    /**
     * Inner class that encapsulates the various scheduler ids for Vvm.
     * 1) SMS-Sending-Unit:   Timer for sending VVM notification SMS request towards NTF's SMS client;
     * 2) SMS-PhoneOn: Timer for validity period of the SMS-Type-0 PhoneOn request for the SimSwap feature;
     * 3) Deactivator: Timer to wait before deactivating VVM for the SimSwap feature 
     */
    public class SchedulerIds {

        public static final String SMS_INFO_EVENT_ID = "info";
        public static final String SENDING_UNIT_PHONE_ON_EVENT_ID = "unit";
        public static final String WAITING_PHONE_ON_EVENT_ID = "type0";
        public static final String DEACTIVATOR_EVENT_ID = "deact";

        public String smsInfoEventId = null;
        public String sendingUnitPhoneOnEventId = null;
        public String waitingPhoneOnEventId = null;
        public String deactivatorEventId = null;
        public String activatorEventId = null; //for Vvm System Activated event

        /** Default constructor */
        public SchedulerIds() {
        }
   
        /** Constructor */
        public SchedulerIds(String smsInfoEventId, String sendingUnitPhoneOnEventId, String waitingPhoneOnEventId, String deactivatorEventId) {
            this.smsInfoEventId = smsInfoEventId;
            this.sendingUnitPhoneOnEventId = sendingUnitPhoneOnEventId;
            this.waitingPhoneOnEventId = waitingPhoneOnEventId;
            this.deactivatorEventId = deactivatorEventId;
        }

        public String getSmsInfoEventId() {
            return smsInfoEventId;
        }
        public String getSendingUnitPhoneOnEventId() {
            return sendingUnitPhoneOnEventId;
        }
        public String getWaitingPhoneOnEventId() {
            return waitingPhoneOnEventId;
        }
        public String getDeactivatorEventId() {
            return deactivatorEventId;
        }
        public String getActivatorEventId() {
            return activatorEventId;
        }
        public void setSmsInfoEventId(String smsInfoEventId) {
            this.smsInfoEventId = smsInfoEventId;
        }
        public void setSendingUnitPhoneOnEventId(String sendingUnitPhoneOnEventId) {
            this.sendingUnitPhoneOnEventId = sendingUnitPhoneOnEventId;
        }
        public void setWaitingPhoneOnEventId(String waitingPhoneOnEventId) {
            this.waitingPhoneOnEventId = waitingPhoneOnEventId;
        }
        public void setDeactivatorEventId(String deactivatorEventId) {
            this.deactivatorEventId = deactivatorEventId;
        }
        public void setActivatorEventId(String activatorEventId) {
            this.activatorEventId = activatorEventId;
        }

        public boolean isEmtpy() {
            // MUST NOT include activatorEventId since this method is used to determine if status file should be deleted and
            // activatorEventId is not written to vvm status file
            if ((smsInfoEventId != null && smsInfoEventId.length() > 0) ||
                (sendingUnitPhoneOnEventId != null && sendingUnitPhoneOnEventId.length() > 0) ||
                (waitingPhoneOnEventId != null && waitingPhoneOnEventId.length() > 0) ||
                (deactivatorEventId != null && deactivatorEventId.length() > 0)) {
                return false;
            }
            return true;
        }

        public void nullify() {
            this.smsInfoEventId = null;
            this.sendingUnitPhoneOnEventId = null;
            this.waitingPhoneOnEventId = null;
            this.deactivatorEventId = null;
        }

        public String toString() {
            return "SchedulerIds: \n" +
                "smsInfoEventId: " + this.smsInfoEventId + "\n" +
                "sendingUnitPhoneOnEventId: " + this.sendingUnitPhoneOnEventId + "\n" +
                "waitingPhoneOnEventId: " + this.waitingPhoneOnEventId + "\n" +
                "deactivatorEventId: " + this.deactivatorEventId + "\n" +
                "activatorEventId: " + activatorEventId + "\n" ;
        }
    }

}
