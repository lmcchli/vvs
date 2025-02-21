/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2012.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.common.cmnaccess;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.abcxyz.messaging.common.data.IDate;
import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.messaging.mrd.util.COSRetentionDaysChangedEvent;
import com.abcxyz.messaging.mrd.util.COSRetentionDaysData;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;


/**
 * Handler for COS Retention days changed events.  These events indicate that a subscriber's COS has changed and that the
 * retention days of messages should be modified.  This handler will re-schedule the expiry time of all of the subscriber's
 * messages according to the new retention days.
 * 
 *  <p>
 *  Implementation is isolated in this handler and uses some mockable interfaces to faciliate unit testing.
 *  </p>
 *  
 *  <p>
 *  For more information about this feature, see tasktop story/task 10164 in the MIO task repository.
 *  </p>
 *
 * @author lmcyvca
 */
public class CosRetentionDaysChangedEventHandler {
    public interface MessageExpirySetter {
        public void setMessageExpiry(Date expiryTime, StateFile stateFile) throws MsgStoreException;
        public void cancelMessageExpiry(StateFile stateFile) throws MsgStoreException;
    }
    
    public interface StateFileFetcher {
        StateFile[] getAllSubscriberMessages(String subscriberMsid) throws MsgStoreException;
    }
    
    private LogAgent logger = null;
    private MessageExpirySetter msgExpirySetter = null;
    private StateFileFetcher stateFileFetcher = null;

    public CosRetentionDaysChangedEventHandler() {
        // Do nothing
    }

    public CosRetentionDaysChangedEventHandler(LogAgent logger, MessageExpirySetter msgExpirySetter, StateFileFetcher stateFileFetcher) {
        this.logger = logger;
        this.msgExpirySetter = msgExpirySetter;
        this.stateFileFetcher = stateFileFetcher;
    }
    
    public void handleCosRetentionDaysChangedEvent(COSRetentionDaysChangedEvent event) throws MsgStoreException {
        if (msgExpirySetter == null || stateFileFetcher == null) {
            return;
        }
        
        StateFile[] stateFiles = stateFileFetcher.getAllSubscriberMessages(event.getSubscriberMsid());
        
        for (StateFile stateFile : stateFiles) {
            String msgClass = stateFile.getC1Attribute(Container1.Message_class);

            if (msgClass != null && (msgClass.equalsIgnoreCase(ServiceName.VOICE) || msgClass.equalsIgnoreCase(ServiceName.FAX)
                    || msgClass.equalsIgnoreCase(ServiceName.VIDEO))) {
            
                String msgState = stateFile.getMsgState();
                String retentionAttributeName = getRetentionAttributeName(msgState, msgClass);
                int currentRetentionDays = getRetentionDays(event.getOldCosData(), retentionAttributeName);
                int newRetentionDays = getRetentionDays(event.getNewCosData(), retentionAttributeName);
        
                if(logger.isDebugEnabled()){
                    logger.debug("Updating retention time for message [" + stateFile.toString() + "] with message state [" + msgState + "] using the COS attribute [" + retentionAttributeName + "] and value new [" + newRetentionDays + "] current [" + currentRetentionDays + "].");
                }
                
                if (newRetentionDays == currentRetentionDays) {
                    if(logger.isDebugEnabled()){
                        logger.debug("New retention time is the same as before. new [" + newRetentionDays + "] current [" + currentRetentionDays + "].");
                    }
                    // Uninteresting case - nothing to do
                    continue;
                }

                // When new retention is infinite, just cancel any existing event
                if (newRetentionDays == DAConstants.ATTR_MSG_RETENTION_VALUE_INFINITE) {
                    msgExpirySetter.cancelMessageExpiry(stateFile);
                    continue;
                }
                
                // A value of "ignored" for new messages is not supported
                if (MoipMessageEntities.MESSAGE_NEW.equalsIgnoreCase(msgState)) {
                    if ((newRetentionDays == DAConstants.ATTR_MSG_RETENTION_VALUE_IGNORED) || (currentRetentionDays == DAConstants.ATTR_MSG_RETENTION_VALUE_IGNORED)) {
                        if (logger != null) {
                            logger.warn("COS attribute " + retentionAttributeName + ": Value " + DAConstants.ATTR_MSG_RETENTION_VALUE_IGNORED + " is not supported for new messages.  Skipping re-scheduling for message: " + stateFile.shortString());
                        }
                        continue;
                    }
                }
                
                Date currentExpiryTime = null;
                
                if (newRetentionDays == DAConstants.ATTR_MSG_RETENTION_VALUE_IGNORED) {
                    // Retention days are now ignored - give some more time to the message by re-scheduling according to the value
                    // for new messages, relative to "now"
                    
                    retentionAttributeName = getRetentionAttributeName(MoipMessageEntities.MESSAGE_NEW, msgClass);
                    newRetentionDays = getRetentionDays(event.getNewCosData(), retentionAttributeName);
                    currentRetentionDays = 0;
                    
                    // When new retention is infinite, just cancel any existing event
                    if (newRetentionDays == DAConstants.ATTR_MSG_RETENTION_VALUE_INFINITE) {
                        msgExpirySetter.cancelMessageExpiry(stateFile);
                        continue;
                    }
                    
                } else if (currentRetentionDays == DAConstants.ATTR_MSG_RETENTION_VALUE_IGNORED) {
                    // Retention days were ignored, and now they are not.  Reschedule relative to "now"
                    currentRetentionDays = 0;
                } else {
                    // both old and new retention days are not ignored so get the current expiry time from the message
                    currentExpiryTime = getExpiryTime(stateFile);
                    
                    if(currentExpiryTime == null || currentExpiryTime.before(Calendar.getInstance().getTime())){
                        if(logger.isDebugEnabled()){
                            logger.debug("Was unable to retrieve the correct expiry time for the message [" + stateFile.toString() + "] or it is in the past expiry time found [" + currentExpiryTime + "] will use now as new value and won't look at the difference between new and current value");
                        }
                        
                        currentExpiryTime = Calendar.getInstance().getTime();
                        currentRetentionDays = 0;
                    }
                }
                
                Date newExpiryTime = calculateNewExpiryTime(currentRetentionDays, newRetentionDays, currentExpiryTime);
                msgExpirySetter.setMessageExpiry(newExpiryTime, stateFile);
            }
        }
    }

    private String getRetentionAttributeName(String messageState, String messageClass){
        String retentionAttributeName = null;
    
            if(MoipMessageEntities.MESSAGE_NEW.equalsIgnoreCase(messageState)) {
                if(ServiceName.VOICE.equalsIgnoreCase(messageClass)) {
                    retentionAttributeName = DAConstants.ATTR_MSG_RETENTION_NEW_VOICE;
                } else if(ServiceName.VIDEO.equalsIgnoreCase(messageClass)) {
                    retentionAttributeName = DAConstants.ATTR_MSG_RETENTION_NEW_VIDEO;
                } else if(ServiceName.FAX.equalsIgnoreCase(messageClass)) {
                    retentionAttributeName = DAConstants.ATTR_MSG_RETENTION_NEW_FAX;
                }
            } else if (MoipMessageEntities.MESSAGE_SAVED.equalsIgnoreCase(messageState)) {
                if(ServiceName.VOICE.equalsIgnoreCase(messageClass)) {
                    retentionAttributeName = DAConstants.ATTR_MSG_RETENTION_SAVED_VOICE;
                } else if(ServiceName.VIDEO.equalsIgnoreCase(messageClass)) {
                    retentionAttributeName = DAConstants.ATTR_MSG_RETENTION_SAVED_VIDEO;
                } else if(ServiceName.FAX.equalsIgnoreCase(messageClass)) {
                    retentionAttributeName = DAConstants.ATTR_MSG_RETENTION_SAVED_FAX;
                }
            } else if (MoipMessageEntities.MESSAGE_READ.equalsIgnoreCase(messageState)) {
                if(ServiceName.VOICE.equalsIgnoreCase(messageClass)) {
                    retentionAttributeName = DAConstants.ATTR_MSG_RETENTION_READ_VOICE;
                } else if(ServiceName.VIDEO.equalsIgnoreCase(messageClass)) {
                    retentionAttributeName = DAConstants.ATTR_MSG_RETENTION_READ_VIDEO;
                } else if(ServiceName.FAX.equalsIgnoreCase(messageClass)) {
                    retentionAttributeName = DAConstants.ATTR_MSG_RETENTION_READ_FAX;
                }
            }
            
        return retentionAttributeName;
    }

    private Date getExpiryTime(StateFile stateFile) {
        Date expiryTime = null;
        
        String expiryTimeAsString = stateFile.getC1Attribute(Container1.Time_of_expiry);
        
        if (expiryTimeAsString != null && !expiryTimeAsString.trim().isEmpty()) {
            try {
                //Using the dateFormatter used to write the Time of Expiry in CMA
                expiryTime = CommonMessagingAccess.DateFormatter.get().parse(expiryTimeAsString);
            } catch (ParseException e) {
                long time = IDate.timeInMillis(expiryTimeAsString);
                if(time > 0){
                    expiryTime = new Date(time);
                }
                   
            }
            
        }
        if(expiryTime == null){
            String event = stateFile.getAttribute(MoipMessageEntities.EXPIRY_EVENT_ID);
            if(event != null && !event.isEmpty()){
                try {
                    EventID eventId = new EventID(event);
                    expiryTime = new Date(eventId.getEventTime());
                } catch (InvalidEventIDException e1) {
                    //Unknow anymore what I can do for that...
                }
            }
        }
        return expiryTime;
    }
    
    private Date calculateNewExpiryTime(int currentRetentionDays, int newRetentionDays, Date currentExpiryTime) {
        Calendar calendar = Calendar.getInstance();
        
        if (currentExpiryTime != null) {
            calendar.setTime(currentExpiryTime);
        }

        int retentionDaysDifference = newRetentionDays - currentRetentionDays;

        calendar.add(Calendar.DAY_OF_MONTH, retentionDaysDifference);

        return calendar.getTime();
    }
    
    private int getRetentionDays(COSRetentionDaysData cosData, String retentionAttributeName) {
        int retentionDays = 0;

        if (cosData != null) {
            String retentionDaysAsString = cosData.getAttributeValue(retentionAttributeName);

            if (retentionDaysAsString != null && retentionDaysAsString.trim().length() > 0) {
                try {
                    retentionDays = Integer.parseInt(retentionDaysAsString);
                } catch (NumberFormatException e) {
                    if (logger != null) {
                        logger.warn("'" + retentionDaysAsString + "'" + " is not a valid number for COS Attribute "
                                + retentionAttributeName, e);
                    }
                }
            }
        }

        return retentionDays;
    }

    public void setStateFileFetcher(StateFileFetcher stateFileFetcher) {
        this.stateFileFetcher = stateFileFetcher;
    }

    public void setMessageExipirySetter(MessageExpirySetter msgExpirySetter) {
        this.msgExpirySetter = msgExpirySetter;
    }
}
