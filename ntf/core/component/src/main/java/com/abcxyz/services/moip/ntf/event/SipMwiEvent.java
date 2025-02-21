package com.abcxyz.services.moip.ntf.event;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.services.moip.ntf.coremgmt.EventSentListener;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.Constants;

/**
 * SipMwi event to be used for keeping SipMwi call information
 */
public class SipMwiEvent extends NtfEvent {

    /** Information to be kept persistently in the event */
    private static LogAgent log = NtfCmnLogger.getLogAgent(SipMwiEvent.class);

    private String subscriberNumber;
    private String notificationNumber;
    private int    currentEvent;
    
    private String reminderTriggerReferenceId = "";
    
    private static final String SUBSCRIBER_NUMBER = "subnumber";
    private static final String NOTIFICATION_NUMBER = "ntfnumber";

    /** Event states */
    public static final int SIPMWI_EVENT_NOTIFICATION = 0; 
    public static final int SIPMWI_EVENT_EXPIRED      = 1; 

    public SipMwiEvent(String schedulerEventId) {
        super(schedulerEventId);
        this.keepReferenceID(null);

        EventID eventId;

        try {
            eventId = new EventID(schedulerEventId);
            
            Properties props = eventId.getEventProperties();
            if (props != null) {
                subscriberNumber = props.getProperty(SUBSCRIBER_NUMBER);
                notificationNumber = props.getProperty(NOTIFICATION_NUMBER);
                parseMsgInfo(props);
            }
            this.setEventServiceTypeKey(eventId.getServiceName());
            this.setEventTypeKey(eventId.getServiceSpecificType());
        } catch (InvalidEventIDException e) {
            log.error("InvalidEventIDException ", e);
            return;
        }
    }

    public SipMwiEvent(Properties props) {
        if (props != null) {
            subscriberNumber = props.getProperty(SUBSCRIBER_NUMBER);
            notificationNumber = props.getProperty(NOTIFICATION_NUMBER);
            super.parseMsgInfo(props);

            // retrieve extra properties
            super.parsingExtraProperties(props);
        }
    }

    public SipMwiEvent(String subscriberNumber, String notificationNumber, NtfEvent ntfEvent) {
        initializeSipMwiEvent(subscriberNumber, notificationNumber);
        super.initialize(ntfEvent);
    }

    public SipMwiEvent(String subscriberNumber, String notificationNumber, MessageInfo msgInfo) {
        initializeSipMwiEvent(subscriberNumber, notificationNumber);
        super.setMsgInfo(msgInfo);
    }

    private void initializeSipMwiEvent(String subscriberNumber, String notificationNumber) {
        this.subscriberNumber = subscriberNumber;
        this.notificationNumber = notificationNumber;
        this.currentEvent = SIPMWI_EVENT_NOTIFICATION;
        EventSentListener listener = NtfEventHandlerRegistry.getEventSentListener(NtfEventTypes.SIPMWI.getName());
        setSentListener(listener);
    }

    public String getRecipient() {
        return subscriberNumber;
    }

    public String getReminderTriggerReferenceId() {
        return reminderTriggerReferenceId;
    }
    
    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    public String getNotificationNumber() {
        return notificationNumber;
    }

    public String getIdentity() {
        return subscriberNumber + " : " + notificationNumber;
    }

    public Properties getEventProperties() {
        Properties props = new Properties();

        if (subscriberNumber != null) {
            props.put(SUBSCRIBER_NUMBER, subscriberNumber);
        }

        if (notificationNumber != null) {
            props.put(NOTIFICATION_NUMBER, notificationNumber);
        }

        super.addMsgProperties(props);
        super.addExtraProperties(props);

        return props;
    }

    /**
     * @return Properties from this NtfEvent that are needed for a reminder notification
     */
    public Properties getReminderProperties(){
        Properties props = getEventProperties();
        props.put(Constants.DEST_RECIPIENT_ID, getRecipient()); 
        //Since the properties are for a reminder notification, set the reminder flag to true.
        props.put(NtfEvent.REMINDER, "1");    
        return props;
    }
      
    /** Get currentEvent */
    public int getCurrentEvent() {
        return this.currentEvent;
    }

    /** Set current Event */
    public void setCurrentEvent(int currentEvent) {
        this.currentEvent = currentEvent;
    }

    public void setReminderTriggerReferenceId(String reminderTriggerReferenceId) {
        if ( reminderTriggerReferenceId == null ) {
            this.reminderTriggerReferenceId = "";
        } else {
            this.reminderTriggerReferenceId = reminderTriggerReferenceId;
        }
    }

    public boolean isReminderSchedulerIdEmpty() {
        boolean result = true;
        if (this.getReminderTriggerReferenceId() != null && this.getReminderTriggerReferenceId().length() > 0) {
            result = false;
        }
        return result;
    }

    public boolean isSchedulerIdEmpty() {
        boolean result = true;
        if (this.getReferenceId() != null && this.getReferenceId().length() > 0) {
            result = false;
        }
        return result;
    }

    public String toString() {
        return "SipMwiEvent: subscriberNumber " + subscriberNumber + ", notificationNumber: " + notificationNumber;
    }
}

