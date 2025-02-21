package com.mobeon.ntf.slamdown.event;

import java.util.Properties;

import com.abcxyz.services.moip.ntf.event.NtfEvent;

/**
 * Container class for a Slamdown event
 */
public class SlamdownEvent extends NtfEvent {

    /** Slamdown properties */
    public static final String RECIPIENT_ID = "rpt";
    public static final String NOTIFICATION_NUMBER = "nnb";
    public static final String CURRENT_STATE = "sta";
    public static final String CURRENT_EVENT = "evt";
    public static final String INTERNAL = "int";
    public static final String NOTIFICATION_TYPE = "ntft";

    /** Private members */
    private String subscriberNumber;
    private String notificationNumber;
    private int currentState;
    private int currentEvent;
    private boolean internal;
    private int notificationType;

    /**
     * Constructor for SlamdownEvent when content from Scheduler (properties)
     * @param props Properties
     */
    public SlamdownEvent(Properties props) {
        if (props != null) {
            subscriberNumber = props.getProperty(RECIPIENT_ID);
            notificationNumber = props.getProperty(NOTIFICATION_NUMBER);

            String prop = props.getProperty(CURRENT_STATE);
            currentState = Integer.parseInt(prop);

            prop = props.getProperty(CURRENT_EVENT);
            currentEvent = Integer.parseInt(prop);

            prop = props.getProperty(INTERNAL);
            internal = Boolean.parseBoolean(prop);

            prop = props.getProperty(NOTIFICATION_TYPE);
            notificationType = Integer.parseInt(prop);

            super.parseMsgInfo(props);
        }
    }

    public Properties getEventProperties() {
        Properties props = new Properties();

        if (subscriberNumber != null) {
            props.put(RECIPIENT_ID, subscriberNumber);
        }
        props.put(NOTIFICATION_NUMBER, notificationNumber );
        props.put(CURRENT_STATE, "" + currentState);
        props.put(CURRENT_EVENT, "" + currentEvent);
        props.put(INTERNAL, internal);
        props.put(NOTIFICATION_TYPE, "" + notificationType);

        super.addMsgProperties(props);
        return props;
    }    

    /**
     * Obtains the subscriber number
     * @return phoneNumber on which the slam down occurred
     */
    public String getSubscriberNumber(){
        return subscriberNumber;
    }

    /** Get the notification number */
    public String getNotificationNumber(){
        return notificationNumber;
    }

    /** Get the state of the SlamdownEvent */
    public int getCurrentState() {
        return this.currentState;
    }

    /** Get current event */
    public int getCurrentEvent() {
        return this.currentEvent;
    }

    /** Get internal */
    public boolean getInternal() {
        return this.internal;
    }

    /** Get notificationType */
    public int getNotificationType() {
        return this.notificationType;
    }
    
}
