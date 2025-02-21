package com.abcxyz.services.moip.ntf.event;

import java.util.Properties;
import java.util.UUID;

import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.services.moip.ntf.coremgmt.EventSentListener;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;



public class FaxPrintEvent extends NtfEvent {

    /** Information to be kept persistently in the event */
    private String subscriberNumber;
    private String faxprintnumber;
    private String uniqueid;
    String autoprint;


    private int    currentEvent;

    private static final String SUBSCRIBER_NUMBER = "subnumber";
    private static final String AUTOPRINT = "autoprint";
    private static final String FAXPRINT_NUMBER = "ntfnumber";
    private static final String EXPIRY = "expiry";

    /** Event states */
    public static final int FAXPRINT_EVENT_NOTIFICATION = 0;
    public static final int FAXPRINT_EVENT_WAIT = 1;
    public static final int FAXPRINT_EVENT_EXPIRED      = 2;
    public static final int FAXPRINT_EVENT_SENDING = 3;
    public static final int FAXPRINT_EVENT_SEND_OK = 4;
    public static final int FAXPRINT_EVENT_SEND_FAILED = 5;
    public static final int FAXPRINT_EVENT_SEND_RETRY = 6;



    public FaxPrintEvent(String eventKey, Properties props) {


        if (props != null) {
            subscriberNumber = props.getProperty(SUBSCRIBER_NUMBER);
            faxprintnumber = props.getProperty(FAXPRINT_NUMBER);
            autoprint = props.getProperty(AUTOPRINT);
            String expiryStr = props.getProperty(EXPIRY);
            if(expiryStr.equalsIgnoreCase("yes"))
            {
                isExpiry=true;
            }
            else
            {
                isExpiry=false;
            }

            uniqueid=eventKey;
            EventSentListener listener = NtfEventHandlerRegistry.getEventSentListener(NtfEventTypes.FAX_L3.getName());
            setSentListener(listener);
            super.parseMsgInfo(props);

            // retrieve extra properties
            super.parsingExtraProperties(props);
        }
    }


    public FaxPrintEvent(String subscriberNumber, String faxprintnumber,String autoprint, MessageInfo msgInfo) {
        uniqueid=UUID.randomUUID().toString();
        uniqueid = uniqueid.replace("-", "");

        initializeFaxPrintEvent(subscriberNumber, faxprintnumber,autoprint,msgInfo);
    }


    public String getFaxEventUniqueId()
    {
        return uniqueid;
    }

    private void initializeFaxPrintEvent(String subscriberNumber, String faxprintnumber,String autoprint, MessageInfo msgInfo) {

        this.isExpiry=false;
        this.subscriberNumber = subscriberNumber;
        this.faxprintnumber = faxprintnumber;
        this.autoprint =autoprint;
        this.currentEvent = FAXPRINT_EVENT_NOTIFICATION;
        EventSentListener listener = NtfEventHandlerRegistry.getEventSentListener(NtfEventTypes.FAX_L3.getName());
        setSentListener(listener);
        super.setMsgInfo(msgInfo);


    }

    public String getSubscriberNumber() {
        return subscriberNumber;
    }



    public String  getSubscriberIdentity() {
        UserInfo profile = getSubcriberProfile();
        if(profile!=null)
        {
            return profile.getTelephoneNumber();
        }
        return null;
    }
    public String  getInboundFaxNumber() {
        UserInfo profile = getSubcriberProfile();
        if(profile!=null)
        {
            return profile.getInboundFaxNumber();
        }
        return null;
    }

    public String getFaxPrintNumber() {
        return faxprintnumber;
    }
    public boolean isAutoPrint() {
        return autoprint.equalsIgnoreCase("true");
    }

    public Properties getEventProperties() {
        Properties props = new Properties();

        if (subscriberNumber != null) {
            props.put(SUBSCRIBER_NUMBER, subscriberNumber);
        }

        if (faxprintnumber != null) {
            props.put(FAXPRINT_NUMBER, faxprintnumber);
        }
        if (faxprintnumber != null) {
            props.put(AUTOPRINT, autoprint);
        }
        if (isExpiry()) {
            props.put(EXPIRY, "yes");
        }
        else
        {
            props.put(EXPIRY, "no");
        }
        super.addMsgProperties(props);
        super.addExtraProperties(props);
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

    public UserInfo getSubcriberProfile()
    {
        return UserFactory.findUserByTelephoneNumber(subscriberNumber);

    }

    public String toString() {
        return "FaxPrintEvent: subscriberNumber " + subscriberNumber + ", faxprintnumber: " + faxprintnumber+ " currentEvent: "+currentEvent+" uniqueid: "+uniqueid+" AutoPrint: "+autoprint+ " isExpiry: "+isExpiry+" Message Id: "+getMessageId();
    }

}
