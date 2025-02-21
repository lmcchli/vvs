/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2015.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.abcxyz.services.moip.ntf.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.DelayedEventTriggerHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.Constants;


/**
 * This class is the base class for all kinds of delayed events, which will be fired and processed in the future.
 *
 * @author ewenxie
 * @since vfe_nl33_mfd02  2015-07-14
 */
public abstract class DelayedEvent extends NtfEvent {

    private static LogAgent log = NtfCmnLogger.getLogAgent(DelayedEventTriggerHandler.class);

    protected static final String STATUS_FILE_PREFIX = "dlyd_evt_";
    protected static final String STATUS_FILE_EXTENSION= ".status" ;

    public enum ActionType{ //this can be extended in future to support other types of actions.
        NONE("none"),
        START("start"),
        CANCEL("cancel");

        private final String action;
        ActionType(String action){
            this.action = action;
        }

        public String action(){
            return action;
        }
    }

    public enum DelayedEventType{ //this can be extended in future to support other types of delayed events.
        DELAYEDSMSREMINDER("delayedsmsreminder");

        private final String type;
        DelayedEventType(String type){
            this.type = type;
        }

        public String type(){
            return type;
        }
    }

    public static final String TRIGGER_TIME = "triggertime";
    public static final String TRIGGER_TIME_FORMAT = "yyyyMMdd_HH_mm_ss_SSS";
    public static final String DELAYED_EVENT_TYPE = "delayedeventtype";
    public static final String SUBSCRIBER_NUMBER = "username";
    public static final String ACTION= "action";

    /**
     * when this delayed event should be fired and processed.
     */
    protected Date triggerTime = null;
    protected DelayedEventType delayedEventType;
    protected ActionType action;

    /**
     * Indicates whether eventProperties contains the necessary information for a DelayedEvent.
     */
    protected boolean valid = true;
    protected String subscriberNumber = ""; //the phone number of the subscriber.
    /**
     * The name of the status file containing the eventId for the event to be fired next.
     */
    protected String statusFileName = "DelayedEvent.status";

    public DelayedEvent(Properties eventProperties) {
        if (eventProperties != null) {
            this.eventProperties = eventProperties;
            String triggerTimeStr = eventProperties.getProperty(TRIGGER_TIME);
            if (triggerTimeStr != null) {  //for cancel action, it should be processed now, so there is no triggertime property
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HH_mm_ss_SSS");
                try {
                    triggerTime = formatter.parse(triggerTimeStr);
                } catch (ParseException e) {
                    triggerTime = null;
                    log.warn("Unable to parse property " + TRIGGER_TIME + ", setting it to NULL");
                    valid = false;
                }
            }


            String actionStr = eventProperties.getProperty(ACTION);
            if(actionStr.equals(ActionType.START.action())){
                action = ActionType.START;
            }
            else if(actionStr.equals(ActionType.CANCEL.action())){
                action = ActionType.CANCEL;
            }
            else{
                log.warn("action " + actionStr + " is not supported for DelayedEvent:" + eventProperties);
                action = ActionType.NONE;
                valid = false;
            }

            subscriberNumber = eventProperties.getProperty(SUBSCRIBER_NUMBER);
            if (subscriberNumber == null) {
                subscriberNumber = eventProperties.getProperty(Constants.DEST_RECIPIENT_ID);
                if (subscriberNumber == null) {
                    valid = false;
                    return;
                }
            }

            super.parseMsgInfo(eventProperties);
            super.parsingExtraProperties(eventProperties);
            setEventServiceTypeKey(NtfEventTypes.DELAYED_EVENT.getName());
            setEventTypeKey(NtfEventTypes.DELAYED_EVENT.getName());
            setEventServiceName(NtfEventTypes.DELAYED_EVENT.getName());
        }
    }

    public Date getTriggerTime() {
        return triggerTime;
    }


    public DelayedEventType getDelayedEventType() {
        return delayedEventType;
    }


    public ActionType getAction() {
        return action;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String getRecipient() {
        return subscriberNumber;
    }

    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    /**
     * @return the name of the status file for this event, it should be in the form of
     *          STATUS_FILE_PREFIX+name specific to the concrete type of delayed event+STATUS_FILE_EXTENSION
     */
    abstract public String getStatusFileName();

}
