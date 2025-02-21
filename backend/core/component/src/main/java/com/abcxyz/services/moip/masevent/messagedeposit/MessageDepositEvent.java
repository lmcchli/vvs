/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.masevent.messagedeposit;

import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.masevent.EventTypes;


public class MessageDepositEvent {
    private EventTypes eventType;
    private String recipientId;
    private MessageInfo msgInfo;
    private AppliEventInfo nextEventInfo;
    private int retryNumber = 0;
        
    /**
     * Container for all information about the message deposit event
     * @param eventType the type of event 
     * @param recipientId the recipient's phone number
     * @param msgInfo the information about the deposited message
     * @param nextEventInfo the next retry event to fire for this message deposit event; needs to be cancelled if current try is successful
     * @param retryNumber indicates the retry number for current message deposit event.  0 indicates original attempt, 1 indicates first retry, etc.
     */
    public MessageDepositEvent(EventTypes eventType, String recipientId, MessageInfo msgInfo, AppliEventInfo nextEventInfo, int retryNumber) {
        this.eventType = eventType;
        this.recipientId = recipientId;
        this.msgInfo = msgInfo;
        this.nextEventInfo = nextEventInfo;
        this.retryNumber = retryNumber;        
    }
    
    public EventTypes getEventType() {
        return eventType;
    }
    
    public String getRecipientId() {
        return recipientId;
    }
    
    public MessageInfo getMsgInfo() {
        return msgInfo;
    }
    
    public AppliEventInfo getNextEventInfo() {
        return nextEventInfo;
    }
    
    public int getRetryNumber() {
        return retryNumber;
    }
    
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("eventype=").append(eventType.getName());
        buffer.append(", recipientId=").append(recipientId);
        buffer.append(", msgInfo=").append(msgInfo);
        buffer.append(", nextEventId=").append(nextEventInfo.getEventId());
        buffer.append(", retryNumber=").append(retryNumber);
        return buffer.toString();
    }
    
}
