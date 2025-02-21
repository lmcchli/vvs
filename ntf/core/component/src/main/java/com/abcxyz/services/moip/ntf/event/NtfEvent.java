/* **********************************************************************
 * Copyright (c) ABCXYZ 2010. All Rights Reserved.
 * Reproduction in whole or in part is prohibited without the
 * written consent of the copyright owner.
 *
 * ABCXYZ MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ABCXYZ SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * **********************************************************************/

package com.abcxyz.services.moip.ntf.event;

import java.util.Properties;
import java.util.UUID;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mrd.data.ReceiverData;
import com.abcxyz.messaging.mrd.operation.InformEventReq;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.EventProperties;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageIDGen;
import com.abcxyz.services.moip.ntf.coremgmt.EventSentListener;
import com.abcxyz.services.moip.ntf.coremgmt.NtfCmnManager;
import com.mobeon.ntf.Constants;

/**
 * Class keeps one NTF event for exchanging information between scheduler event and NTF framework.
 */
public class NtfEvent {

    private String eventServiceTypeKey = NtfEventTypes.DEFAULT_NTF.getName();
    private String eventTypeKey = NtfEventTypes.EVENT_TYPE_NOTIF.getName();
    
    //Variable to hold EventID.serviceName value.  NtfEvent.eventServiceTypeKey seems to have started out having EventID.serviceName value but
    //in NtfEvent.parsingEvent(), the EventID.serviceType is put as the value for NtfEvent.eventServiceTypeKey (instead of EventID.serviceName).
    //An issue arises when an expiry event fires, e.g., NtfEvent will parse "smsreminder-Expir" to be eventServiceTypeKey=Expir and the serviceName 
    //is not parsed.  Since fixing this issue might break many features which use NtfEvent.eventServiceType and NtfEvent.eventTypeKey (and of course,
    //there is no time to re-test all these features), a new NtfEvent.serviceName variable is introduced to store the EventID.serviceName properly.
    private String eventServiceName = NtfEventTypes.DEFAULT_NTF.getName();

    //When an expiry event fires, the EventID.serviceType is Expir (from e.g. "ntf-Expir") and we do not have the serviceType that is expiring (e.g. Notif, slmdw, etc.).
    //So, this variable contains the type that is being processed and is stored persistently in the event properties .
    private String ntfEventType = "";
    
    private MessageInfo msgInfo;
    protected Properties eventProperties; //properties keep event properties

    private String eventUid;		//event UID
	private int triedNumber = 0;

    //reference is the backup event for next retry.
    private String refId;
    //private EventID eventId;

    protected boolean isExpiry;
    //event status call-back
    EventSentListener listener;

    // If current event is for a fallback scenario
    private int fallback = 0;
    private int fallbackOriginalNotificationType = 0;
    private String fallBackTypeName = null; // the configured fall back name of the FallbackNotificationType.
    
    // If current event is for a reminder notification
    private int reminder = 0;
    
    // If current event is for a roaming user.
    private boolean roaming = false;

    /**
     * construct a NTF event from a scheduler event ID, used in the case the event is from a fired event
     *
     */
    public NtfEvent(String eventId) {
        parsingEvent(eventId);
    }

    /**
     * construct a NTF event from properties, for scheduling
     * @param eventServiceType service type (e.g. ntf, slmdw)
     * @param eventProperties properties from an internal NTF event 
     *                        (as opposed to properties from MRD which would not include MsgInfo values).
     */
    public NtfEvent(String eventServiceType, Properties eventProperties) {
        this(eventServiceType, null, eventProperties, null);
        parseMsgInfo(eventProperties);
        this.eventUid = getMessageId(msgInfo);
    }
    
    /**
     * construct a NTF event from message info, for scheduling
     * @param eventServiceType String
     * @param msgInfo MessageInfo
     * @param eventProperties Properties
     * @param refId String
     */
    public NtfEvent(String eventServiceType, MessageInfo msgInfo, Properties eventProperties, String refId) {
        this (eventServiceType, NtfEventTypes.EVENT_TYPE_NOTIF.getName(), msgInfo, eventProperties, refId);
    }

    /**
     * construct a NTF event from message info, for scheduling
     * @param eventServiceType String
     * @param msgInfo MessageInfo
     * @param eventProperties Properties
     * @param refId String
     */
    public NtfEvent(String eventServiceType, String eventTypeKey, MessageInfo msgInfo, Properties eventProperties, String refId) {
        this.eventServiceTypeKey = eventServiceType;
        this.eventTypeKey = eventTypeKey;
        if(NtfEventTypes.DEFAULT_NTF.getName().equalsIgnoreCase(eventServiceTypeKey)) {
            //event is for a new message deposit notification
            ntfEventType = NtfEventTypes.EVENT_TYPE_NOTIF.getName();
        } else {
            ntfEventType = eventServiceType;
        }
        if(msgInfo != null) {
            this.msgInfo = msgInfo;
            this.eventUid = getMessageId(msgInfo);
        }
        this.eventProperties = eventProperties;
        this.keepReferenceID(refId);
    }

    protected NtfEvent() {

    }

    protected void initialize(NtfEvent ntfEvent) {
        if (ntfEvent != null) {
            this.msgInfo = ntfEvent.getMsgInfo();
            this.fallback = ntfEvent.getFallback();
            this.fallbackOriginalNotificationType = ntfEvent.getFallbackOriginalNotificationType();
            this.eventServiceTypeKey = ntfEvent.getEventServiceTypeKey();
            this.eventTypeKey = ntfEvent.getEventTypeKey();
            this.reminder = ntfEvent.getReminder();
        }
    }

    /**
     *
     * @return Event service type key
     */
    public String getEventServiceTypeKey() {
        return eventServiceTypeKey;
    }

    public void setEventServiceTypeKey(String serviceTypeKey) {
        this.eventServiceTypeKey = serviceTypeKey;
    }

    public String getEventTypeKey() {
        return eventTypeKey;
    }

    public void setEventTypeKey(String typeKey) {
        this.eventTypeKey = typeKey;
    }

    public boolean isEventServiceType(final String serviceType) {
        return eventServiceTypeKey.equalsIgnoreCase(serviceType);
    }

    public boolean isEventType(final String eventType) {
        return eventTypeKey.equalsIgnoreCase(eventType);
    }
    
    public String getEventServiceName(){
        return eventServiceName;
    }

    public void setEventServiceName(String eventServiceName){
        this.eventServiceName = eventServiceName;
    }

    public boolean isEventServiceName(final String serviceName){
        return eventServiceName.equalsIgnoreCase(serviceName);
    }
    
    public void setExpiry() {
    	isExpiry = true;
    }

    public boolean isExpiry() {
    	return isExpiry;
    }

    public String calculateEventUid(String schedulerID) {
        int id = 0;
        try {
            id = Integer.parseInt(schedulerID);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        eventUid = EventID.calculUid(msgInfo.omsgid, msgInfo.rmsgid, id);
        return eventUid;
    }

    public String getEventUid() {
    	return eventUid;
    }

    /**
     * method can be overridden by sub classes
     * @return recipient Subscriber number
     */
    public String getRecipient() {
    	String recipient = getEventProperties().getProperty(Constants.DEST_RECIPIENT_ID);
    	if (recipient==null) {
    	    return "";
    	}
    	return recipient;
    }

    /**
     * method to be overridden by specific NTF event for retrieving event related information
     */
    protected void parsingExtraProperties(Properties properties) {
    	String prop = properties.getProperty(FALLBACK);
        if(prop != null){
        	this.fallback = Integer.parseInt(prop);
        }
        prop = properties.getProperty(FALLBACK_ORIGINAL_NOTIFICATION_TYPE);
        if(prop != null){
        	this.fallbackOriginalNotificationType =  Integer.parseInt(prop);
        }
        
        fallBackTypeName = properties.getProperty(FALLBACK_TYPE_NAME);
        
        prop = properties.getProperty(REMINDER);
        if(prop != null){
            this.reminder = Integer.parseInt(prop);
        }
        
        prop = properties.getProperty(ROAMING);
        if (prop != null) {
            this.roaming = Boolean.parseBoolean(prop);
        }
        
        prop = properties.getProperty(NTF_EVENT_TYPE);
        if (prop != null) {
            this.ntfEventType = prop;
        } else {
            //The event was scheduled with an older NTF jar which did not have this property.
            this.ntfEventType = eventServiceTypeKey;
        }
        
        
    }
    
    /**
     * return scheduler eventId
     *
     */
/*    public EventID getEventId() {
        return eventId;
    }
*/
    /**
     * @return message info
     */
    public MessageInfo getMsgInfo() {
        return msgInfo;
    }

    public void setMsgInfo(MessageInfo msg) {
    	msgInfo = msg;
    }

    public static String getMessageId(MessageInfo msgInfo) {
        String schedulerID = NtfCmnManager.getInstance().getSchedulerID();
        String id = MoipMessageIDGen.getRecipientMessageID(msgInfo, schedulerID);
        return id;
    }

    public String getMessageId() {
        return (getMessageId(msgInfo));
    }

    /**
     *
    * @return a value from the eventProperties.  Null if eventProperties is not set
    */
   public String getProperty(String key) {
	   if (eventProperties != null){
		   return eventProperties.getProperty(key);
	   }
       return "";
   }
   
   /**
    *
    * @return a multi-value property split into an array Null if eventProperties is not set.
    */
   public String[] getMultilineProperty(String key) {
       String attrs = getProperty(key);
       if (attrs != null) {
           return attrs.split(InformEventReq.MULTILINE_DELIMITER);
       }
       return null;
   }

    /**
     * Set the given <key,value> pair in the eventProperties container 
     */
    public void setProperty(String key, String value) {
        eventProperties.put(key, value);
    }
    
  
    /**
     *
     * @return properties provided from the scheduler event
     */
    public Properties getEventProperties() {
        return eventProperties;
    }

    /** NtfEvent properties */
    static public final String OMSA = "omsa";
    static public final String RMSA = "rmsa";
    static public final String OMSGID = "omsg";
    static public final String RMSGID = "rmsg";
    static protected final String FALLBACK = "fallback";
    static protected final String FALLBACK_ORIGINAL_NOTIFICATION_TYPE = "fallbackorigntftype";
    private static final String FALLBACK_TYPE_NAME = "FallbackTName";
    static protected final String REMINDER = "reminder";
    static protected final String ROAMING = "roaming";
    static protected final String NTF_EVENT_TYPE = "ntfevttype";
    static public final String NOTIFIER_HANDLING_TYPE = "pluginh";
    static public final String AUTO_UNLOCK_PIN_LOCKTIME = "locktime";
    static public final String AUTO_UNLOCK_PIN_LOCKED = "pinlock";

    /**
     * @return properties provided from the scheduler event and the MsgInfo properties.
     */
    public Properties getMessageEventProperties() {
        Properties props = null;
        if (eventProperties != null){
            props = (Properties)eventProperties.clone();
        } else {
            props = new Properties();
            props.put(Constants.DEST_RECIPIENT_ID, getRecipient());
        }
        addMsgProperties(props);
        return props;
    }
    
    public Properties getPersistentProperties() {
    	Properties props = getMessageEventProperties();
    	addExtraProperties(props);

        return props;
    }

    protected void addMsgProperties(Properties props) {
    	//add message info
        if (msgInfo != null && !isReminder()) {
            if (msgInfo.omsa != null) {
                props.put(OMSA, msgInfo.omsa.toString());
            }
            if (msgInfo.rmsa != null) {
                props.put(RMSA, msgInfo.rmsa.toString());
            }
            if (msgInfo.omsgid != null) {
                props.put(OMSGID, msgInfo.omsgid);
            }
            if (msgInfo.rmsgid != null) {
                props.put(RMSGID, msgInfo.rmsgid);
            }
        }
    }

    // Somehow addMsgProperties() was created to get MsgInfo stuff EXCEPT for Reminder event.
    // It so happens that Reminder events could very well need to populate the FROM tag and that
    // value is derived from information contained in MsgInfo - so why not get it for Reminder Events???
    protected void addMsgPropertiesEvenForReminder(Properties props) {
        //add message info
        if (msgInfo != null) {
            if (msgInfo.omsa != null) {
                props.put(OMSA, msgInfo.omsa.toString());
            }
            if (msgInfo.rmsa != null) {
                props.put(RMSA, msgInfo.rmsa.toString());
            }
            if (msgInfo.omsgid != null) {
                props.put(OMSGID, msgInfo.omsgid);
            }
            if (msgInfo.rmsgid != null) {
                props.put(RMSGID, msgInfo.rmsgid);
            }
        }
    }

    protected void addExtraProperties(Properties props) {
        props.setProperty(FALLBACK, Integer.toString(this.fallback));
        props.setProperty(FALLBACK_ORIGINAL_NOTIFICATION_TYPE, Integer.toString(this.fallbackOriginalNotificationType));
        if (fallBackTypeName != null ) {props.setProperty(FALLBACK_TYPE_NAME, fallBackTypeName);}
        props.setProperty(REMINDER, Integer.toString(this.reminder));
        props.setProperty(ROAMING, Boolean.toString(this.roaming));
        props.setProperty(NTF_EVENT_TYPE, ntfEventType);
    }

    /**
     * @return Properties from this NtfEvent that are needed for a reminder notification
     */
    public Properties getReminderProperties(){
        Properties props = new Properties();
        props.put(Constants.DEST_RECIPIENT_ID, getRecipient()); 

        //Since the properties are for a reminder notification, set the reminder flag to true.
        props.put(NtfEvent.REMINDER, "1");    

        //We need MsgInfo to get the 'FROM' tag (if only 1 new message, FROM should be usable)
        addMsgPropertiesEvenForReminder(props);
        return props;
    }
    
    /**
     * @return Properties from this NtfEvent that are needed for a auto unlock pin notification
     */
    public Properties getAutoUnlockPinProperties(){
        Properties props = new Properties();
        props.put(Constants.DEST_RECIPIENT_ID, getRecipient()); 
        props.put(AUTO_UNLOCK_PIN_LOCKED, "1"); 
        return props;
    }
      
    public void keepReferenceID(String id) {
        this.refId = id;
    }

    public String getReferenceId() {
        return refId;
    }

    public void setSentListener(EventSentListener listener) {
        this.listener = listener;
    }

    public EventSentListener getSentListener() {
        return listener;
    }

	public void setNumberOfTried(int tried) {
		triedNumber = tried;
	}

	public int getNumberOfTried() {
		return triedNumber;
	}

    public void setFallback() {
        fallback = 1;
    }

    public boolean isFallback() {
        return (fallback == 0 ? false : true);
    }

    public int getFallback() {
        return fallback;
    }

    protected void setFallbackOriginalNotificationType(int notifType){
    	fallbackOriginalNotificationType = notifType;
    }

    public int getFallbackOriginalNotificationType() {
        return fallbackOriginalNotificationType;
    }
    
    
    public String getFallBackName() {
        return fallBackTypeName;   
     }
    
    public void setFallBackName(String fallBackName) {
        this.fallBackTypeName=fallBackName;   
     }

    public void setReminder() {
        reminder = 1;
    }

    public void resetReminder() {
        reminder = 0;
    }

    public boolean isReminder() {
        return (reminder == 0 ? false : true);
    }

    public int getReminder() {
        return reminder;
    }
    
    /**
     * Returns the roaming status of this event.
     * @return Returns true if the event is for a roaming user, false otherwise.
     */
    public boolean isRoaming() {
        return roaming;
    }
    
    /**
     * Sets the roaming status for this event.
     * @param roaming Roaming status.
     */
    public void setRoaming(boolean roaming) {
        this.roaming = roaming;
    }

    public String getNtfEventType() {
        return ntfEventType;
    }
    
    /**
     * Generates a Universal Unique ID.
     * A DASH is prepended since this ID is appended to an existing key    
     */
    static public String getUniqueId() {
        String uniqueId = UUID.randomUUID().toString();
        uniqueId = uniqueId.replace("-", "");
        return "-" + uniqueId;
    }

    /**
     * parsing a scheduler event id for retrieving MFS message
     *
     */
	private static final String TO = "to";
    protected void parsingEvent(String schedulerEventId) {

    	EventID eventId;

        try {
            eventId = new EventID(schedulerEventId);
        } catch (InvalidEventIDException e) {
            e.printStackTrace();
            return;
        }

        eventServiceTypeKey = eventId.getServiceSpecificType();
        eventServiceName = eventId.getServiceName();

        //retrieve message information from event ID
        String[] ids = EventID.calculSid(eventId.getUid());

        String sReminder = eventId.getProperty(REMINDER);
        if (sReminder != null) {
            reminder = Integer.parseInt(sReminder);
        }        
        String to = null; //eventId from MRD SendMessage always contains "To"
        if (ids.length == 3 && reminder == 0) {//event is from MRD
            msgInfo = new MessageInfo();
            msgInfo.omsgid = ids[EventID.UID_OMSGID_POS];
            msgInfo.rmsgid = ids[EventID.UID_RMSGID_POS];
            String receiver = eventId.getProperty(EventProperties.RECEIVER);
            if (receiver != null) {
                ReceiverData rdata = new ReceiverData(receiver);
                to = rdata.rcptID;
                msgInfo.rmsa = new MSA(rdata.rmsa);
            }

            String sender = eventId.getProperty(EventProperties.SENDER);
            if (sender != null) {
                msgInfo.omsa = new MSA(sender);
            }
        } else {//NTF internal event
        	//set message info
        	parseMsgInfo(eventId.getEventProperties());
        }
        eventUid = eventId.getUid();

        //get extra services properties, this will be handled based on specific service information
        this.eventProperties = eventId.getEventProperties();
        if (to != null) {
        	if (eventProperties == null) {
        		eventProperties = new Properties();
        	}
        	eventProperties.setProperty(TO, to);
        }

        String sTried = eventProperties.getProperty(EventProperties.NUM_TRIED);
        if (sTried != null) {
            triedNumber = Integer.parseInt(sTried);
        }

        //keep reference
        this.refId = eventId.toString();

        parsingExtraProperties(eventId.getEventProperties());
   }

    protected void parseMsgInfo(Properties props) {
    	msgInfo = new MessageInfo();

    	if (props.getProperty(OMSA) != null)
    		msgInfo.omsa = new MSA (props.getProperty(OMSA));
    	if (props.getProperty(RMSA) != null)
    		msgInfo.rmsa = new MSA (props.getProperty(RMSA));

    	msgInfo.omsgid = props.getProperty(OMSGID);
    	msgInfo.rmsgid = props.getProperty(RMSGID);
    }

}