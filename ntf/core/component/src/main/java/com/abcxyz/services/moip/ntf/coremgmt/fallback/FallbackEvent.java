/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt.fallback;

import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.ntf.Constants;

import java.util.Properties;

/**
 * Container class for a Fallback events
 */
public class FallbackEvent extends NtfEvent {

    /** Fallback properties */
    public static final String RECIPIENT_ID = "rpt";

    /** Private members */
    private String subscriberNumber;
    private int currentEvent;
    
    private FallbackInfo fallbackInfo = null; //optional fall back info
    private String fallBackTypeName; // the configured fall back name of the FallbackNotificationType.

    /** Fallback notification types */
    public enum FallbackNotificationTypes {

        /** Current fallback notification types supported
         * name, implemented, profile, notification id, configured name. 
         * */
        FALLBACK_NONE         ("Fallback None",        true,  null,  -1,                        "none"),
        FALLBACK_SMS          ("Fallback SMS",         true,  "SMS", Constants.NTF_SMS,         "sms"),
        FALLBACK_FLS          ("Fallback FLS",         true,  "FLS", Constants.NTF_FLS,         "fls"),
        FALLBACK_FLSSMS       ("Fallback FLSSMS",      true,  "FLSSMS", Constants.NTF_FLSSMS,   "flssms"),
        FALLBACK_OUTDIAL      ("Fallback Outdial",     true,  "ODL", Constants.NTF_ODL,         "outdial"),
        FALLBACK_SIPMWI       ("Fallback SipMwi",      true,  "MWI", Constants.NTF_SIPMWI,         "sipmwi"),

        /** Fall back to notification types not supported yet */
        FALLBACK_MMS          ("Fallback MMS",         false, "",    Constants.NTF_MMS,      "mms"),
        FALLBACK_EMAIL        ("Fallback Email",       false, "",    Constants.NTF_EMAIL,    "email"),
        FALLBACK_SLAMDOWN_MCN ("Fallback SlamdownMcn", false, "",    Constants.NTF_SLAMDOWN, ""),
        FALLBACK_VVM          ("Fallback VVM",         false, "",    Constants.NTF_VVM,      "");

        private String name;
        private boolean implemented;
        private String profile;
        private int notificationTypeId;
        private String configuredName;

        FallbackNotificationTypes(String name, boolean implemented, String profile, int notificationTypeId, String configuredName) {
            this.name = name;
            this.implemented = implemented;
            this.profile = profile;
            this.notificationTypeId = notificationTypeId;
            this.configuredName = configuredName;
        }
        
        public static final int NTF_NO_NOTIF_TYPE = 19;
        
        public String getName() {
            return this.name;
        }

        public boolean isImplemented() {
            return this.implemented;
        }

        public String getProfile() {
            return this.profile;
        }

        public int getNotificationTypeId() {
            return this.notificationTypeId;
        }

        public String getConfiguredName() {
            return this.configuredName;
        }
    }

    /** Event states */
    public static final int FALLBACK_EVENT_PROCESSING = 0; 
    public static final int FALLBACK_EVENT_SCHEDULER_RETRY = 1; 

    public static final String[] FALLBACK_EVENTS_STRING = {
        "FALLBACK_Event_Processing",
        "FALLBACK_Event_Scheduler_Retry"
    };


    /**
     * Constructor for FallbackEvent when triggering fallback 
     * @param originalNotificationType OriginalNotificationType
     * @param ntfEvent NtfEvent
     */
    public FallbackEvent(int originalNotificationType, NtfEvent ntfEvent) {
        super.initialize(ntfEvent);
        this.subscriberNumber = ntfEvent.getRecipient();
        //Set original notification type AFTER doing super.initialize(ntfEvent); otherwise, will end up with original ntfEvent's value.
        setFallbackOriginalNotificationType(originalNotificationType); 
        this.currentEvent = FallbackEvent.FALLBACK_EVENT_PROCESSING;
    }

    /**
     * Constructor for FallbackEvent when content from Scheduler (properties)
     * @param eventKey (subscriberNumber)
     * @param props Properties
     */
    public FallbackEvent(String eventKey, Properties props) {
        // Keep the first part of the eventKey
        String[] keys = eventKey.split("-");
        if (keys.length > 0) {
            eventKey = keys[0];
        }

        if (props != null) {
            subscriberNumber = eventKey;
            super.parseMsgInfo(props);
            super.parsingExtraProperties(props);
        }
    }
    /**
     * Put the private members in Properties
     */
    public Properties getEventProperties() {
        Properties props = new Properties();
        props.put(RECIPIENT_ID, subscriberNumber);
        super.addMsgProperties(props);
        super.addExtraProperties(props);
        return props;
    }
    
    
    public Properties getPersistentProperties() {
        Properties props = getMessageEventProperties();
        addExtraProperties(props);
        return props;
    }

    /**
     * Obtains the subscriber number
     * @return phoneNumber on which the slam down occurred
     */
    public String getSubscriberNumber(){
        return subscriberNumber;
    }

    @Override
    public String getRecipient() {
    	return subscriberNumber;
    }

    /** Get currentEvent */
    public int getCurrentEvent() {
        return this.currentEvent;
    }

    /** Set current Event */
    public void setCurrentEvent(int currentEvent) {
        this.currentEvent = currentEvent;
    }
    
    public FallbackInfo getFallbackInfo() {
        return fallbackInfo;
    }
           
    public void setFallbackInfo(FallbackInfo info) {
        this.fallbackInfo = info;
    }

    public String toString() {
        return "FallbackEvent: " + subscriberNumber + ", original notification type: " + Constants.notifTypeStrings[getFallbackOriginalNotificationType()] + ", MessageInfo: " + getMsgInfo();  
    }

}
