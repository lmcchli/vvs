/**
 * Copyright (c) 2009 Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.util.cache.Block;
import com.abcxyz.messaging.mfs.MsgStoreServer;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.Result;
import com.abcxyz.messaging.mrd.operation.SendMessageReq;
import com.abcxyz.messaging.mrd.operation.SendMessageResp;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.FaxPrintEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.common.util.FaxPrintStatus;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.out.fax.FaxPrintOut;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.NtfUtil;
/**
* This class handles FAX Print Event events
*/
public class FaxPrintEventHandler extends NtfRetryEventHandler {

    private static LogAgent logger = NtfCmnLogger.getLogAgent(FaxPrintEventHandler.class);
    /** queue  FaxPrint worker queue*/
    private ManagedArrayBlockingQueue<Object> queue=null;
    /** faxPrintCache  Cache for fax print evenr*/
    private static Block faxPrintCache = new Block(3000);
    private MerAgent mer;

    private MfsEventManager mfsEventManager;

    public FaxPrintEventHandler() {
        queue = new ManagedArrayBlockingQueue<Object>(Config.getFaxWorkerQueueSize());
        RetryEventInfo info = new RetryEventInfo(getEventServiceName());
        info.setEventRetrySchema(Config.getFaxPrintNotifRetrySchema());
        info.setExpireTimeInMinute(Config.getFaxprintExpireTimeInMin());
        mer = MerAgent.get(Config.getInstanceComponentName());
        mfsEventManager = MfsEventFactory.getMfsEvenManager();
        super.init(info);
    }

    public String getEventServiceName() {
        return NtfEventTypes.FAX_L3.getName();
    }

    /**
     * Get fax print worker queue
     */
    public ManagedArrayBlockingQueue<Object> getWorkingQueue() {
        return this.queue;
    }
    /**
     * Reset counters
     */
    public void reset() {

        super.numOfCancelledEvent = new AtomicLong(0);
        super.numOfFiredExpireEvent = new AtomicLong(0);
        super.numOfFiredNotifEvent = new AtomicLong(0);
        super.numOfScheduledEvent = new AtomicLong(0);
    }

    /**
     * Schedule new event with retry schema
     */
    public void scheduleBackup(FaxPrintEvent faxPrintEvent) {

        AppliEventInfo eventInfo = null;
        //Keeping fax print evnt in cahe
        eventInfo = eventHandler.scheduleEvent(faxPrintEvent.getFaxEventUniqueId(), NtfEventTypes.FAX_L3.getName(), faxPrintEvent.getEventProperties());
        faxPrintEvent.keepReferenceID(eventInfo.getEventId());
        keepEventInCache(faxPrintEvent);
        logger.debug("FaxPrintEventHandler Scheduled event: " + eventInfo.getEventId()+" EventKey: "+eventInfo.getEventKey()+" Fax event: "+faxPrintEvent );
    }



    @Override
    public int eventFired(AppliEventInfo eventInfo) {

        int result = EventHandleResult.OK;
        FaxPrintEvent faxPrintEvent = null;

        numOfFiredNotifEvent.incrementAndGet();

        Calendar cal = Calendar.getInstance();
        if (logger.isDebugEnabled()) {
            logger.debug("FaxPrintEventHandler event fired: " + eventInfo.getEventId() + " total: " + numOfFiredNotifEvent + " T: " + cal.getTime()+ " isExpire: "+eventInfo.isExpire());
        }

        if(!FaxPrintOut.get().isStarted())
        {
            if(eventInfo.isExpire())
            {
                logger.error("Fax print service id not active and event expired (FAX PRINT has failled) EventId: "+ eventInfo.getEventId());
                faxPrintEvent = retrieveFaxPrintEvent(eventInfo.getEventKey());
                if(faxPrintEvent==null)
                {
                    faxPrintEvent = new FaxPrintEvent(eventInfo.getEventKey(), eventInfo.getEventProperties());

                }
                handleFaxPrintExpiry(faxPrintEvent);
                return EventHandleResult.STOP_RETRIES;
            }
            else
            {
                logger.error("Fax print service id not active waiting for next rerty for EventId: "+ eventInfo.getEventId());
                return EventHandleResult.OK;

            }
        }

        faxPrintEvent = retrieveFaxPrintEvent(eventInfo.getEventKey());
        if(faxPrintEvent==null)
        {
            //NTF crash so recreate the event and to FAXPRINT_EVENT_WAIT to continue processing
            if (logger.isDebugEnabled())logger.debug("eventFired FaxPrintEvent not found in cache uniqueid: "+eventInfo.getEventKey());
            faxPrintEvent = new FaxPrintEvent(eventInfo.getEventKey(), eventInfo.getEventProperties());
            faxPrintEvent.setCurrentEvent(FaxPrintEvent.FAXPRINT_EVENT_WAIT);
            keepEventInCache(faxPrintEvent);
        }
        // Keep the next eventId in cache
        if (eventInfo.getNextEventInfo() != null) {
            faxPrintEvent.keepReferenceID(eventInfo.getNextEventInfo().getEventId());
        }


        if(eventInfo.isExpire())
        {
            faxPrintEvent.setExpiry();
        }


        if (logger.isDebugEnabled())logger.debug("eventFired Processing event: "+faxPrintEvent);



        //We are already doing fax transmission
        if(faxPrintEvent.getCurrentEvent()!=FaxPrintEvent.FAXPRINT_EVENT_WAIT )
        {
            if(faxPrintEvent.isExpiry())
            {
                //Fax print event expired during fax transmission.
                //We need to reschedule the event and wait for the end of the transmission before doing the expiry
                eventInfo = eventHandler.scheduleEvent(faxPrintEvent.getFaxEventUniqueId(), NtfEventTypes.FAX_L3.getName(), faxPrintEvent.getEventProperties());
                faxPrintEvent.keepReferenceID(eventInfo.getEventId());
                if (logger.isDebugEnabled())logger.debug("eventFired has expired during transmission  resechduling fax event before processing expiry: "+faxPrintEvent);
                result=EventHandleResult.STOP_RETRIES;
            }
            else
            {
                //Fax transmission is occuring will wait for next try
                if (logger.isDebugEnabled())logger.debug("eventFired Fax print is being process will wait for next try: "+faxPrintEvent);
            }
        }
        else
        {
            //We are idle processing next try
            faxPrintEvent.setCurrentEvent(FaxPrintEvent.FAXPRINT_EVENT_NOTIFICATION);
            // Validate if its last event notification (expire event)
            if (faxPrintEvent.isExpiry()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("FaxPrintEventHandler event found to be expired: " + faxPrintEvent.getReferenceId());
                }

                faxPrintEvent.setCurrentEvent(FaxPrintEvent.FAXPRINT_EVENT_EXPIRED);

                // Add to the working queue for processing
                try {
                    queue.put(faxPrintEvent);
                } catch (Throwable t) {
                    return EventHandleResult.ERR_QUEUE_FULL;
                }

                return EventHandleResult.STOP_RETRIES;
            }
            else
            {               
                try {
                    queue.put(faxPrintEvent);
                } catch (Throwable t) {
                    // do nothing
                }
            }
        }
        return result;
    }


    /**
     * Cancel fax event in scheduler and removing the fax event from the cache
     * @param faxPrintEvent FaxPrintEvent
     */
    public void cancelStoredEvent(FaxPrintEvent faxPrintEvent) {
        String eventId = faxPrintEvent.getReferenceId();

        // Cancel future event
        if (eventId != null) {
            if (logger.isDebugEnabled())
                logger.debug("cancelStoredEvent cancel event: " + eventId+ " for " + faxPrintEvent);
            super.cancelEvent(eventId);
        } else {
            if (logger.isDebugEnabled())logger.info("cancelStoredEvent unable to find ReferenceId for " + faxPrintEvent);
        }
        //Remove event from cache
        removeEventFromCache(faxPrintEvent);

    }


    public int getCacheSize() {
        return faxPrintCache.size();
    }

    public FaxPrintEvent retrieveFaxPrintEvent(String id) {
        FaxPrintEvent faxPrintEvent = (FaxPrintEvent)faxPrintCache.get(id);
        return faxPrintEvent;
    }

    public void keepEventInCache(FaxPrintEvent faxPrintEvent) {
        faxPrintCache.putElement(faxPrintEvent.getFaxEventUniqueId(),faxPrintEvent);
    }

    public void removeEventFromCache(FaxPrintEvent faxPrintEvent) {
        faxPrintCache.remove(faxPrintEvent.getFaxEventUniqueId());
    }







    public void handleFaxPrintExpiry(FaxPrintEvent faxPrintEvent)
    {
        if (logger.isDebugEnabled())logger.debug("handleFaxPrintExpiry for "+faxPrintEvent);

        sendFaxDeliveryReceipt(faxPrintEvent.getSubcriberProfile(),faxPrintEvent.getMsgInfo(),faxPrintEvent.getFaxEventUniqueId(),faxPrintEvent.getFaxPrintNumber(),false);
        removeEventFromCache(faxPrintEvent);
        setFaxPrintDone(faxPrintEvent.getMsgInfo());
        mer.faxPrintTimeout(faxPrintEvent.getSubscriberIdentity(),faxPrintEvent.isAutoPrint());
    }

    public void setFaxPrintDone(MessageInfo messageInfo) {
        try
        {
            logger.debug("setFaxPrintDone");

            StateFile mfsStateFile = CommonMessagingAccess.getInstance().getStateFile(messageInfo);
            FaxPrintStatus.changeStatus(mfsStateFile, FaxPrintStatus.done);
        }
        catch(MsgStoreException e)
        {
            logger.error("setFaxPrintDone Unable to set fax as done exception",e);
        }
    }
    public void handleFaxPrintSuccess(FaxPrintEvent faxPrintEvent)
    {
        if (logger.isDebugEnabled())logger.debug("handleFaxPrintSuccess for "+faxPrintEvent);
        sendFaxDeliveryReceipt(faxPrintEvent.getSubcriberProfile(),faxPrintEvent.getMsgInfo(),faxPrintEvent.getFaxEventUniqueId(),faxPrintEvent.getFaxPrintNumber(),true);

        cancelStoredEvent(faxPrintEvent);
        setFaxPrintDone(faxPrintEvent.getMsgInfo());
        mer.faxPrintDelivered(faxPrintEvent.getSubscriberIdentity(),faxPrintEvent.isAutoPrint());
    }
    public void handleFaxPrintFailled(FaxPrintEvent faxPrintEvent)
    {
        if (logger.isDebugEnabled())logger.debug("handleFaxPrintFailled for "+faxPrintEvent);
        sendFaxDeliveryReceipt(faxPrintEvent.getSubcriberProfile(),faxPrintEvent.getMsgInfo(),faxPrintEvent.getFaxEventUniqueId(),faxPrintEvent.getFaxPrintNumber(),false);
        cancelStoredEvent(faxPrintEvent);
        setFaxPrintDone(faxPrintEvent.getMsgInfo());
        mer.faxPrintFailed(faxPrintEvent.getSubscriberIdentity(),faxPrintEvent.isAutoPrint());

    }
    public void handleFaxPrintExpiryInTransmission(FaxPrintEvent faxPrintEvent)
    {
        if (logger.isDebugEnabled())logger.debug("handleFaxPrintExpiryInTransmission for "+faxPrintEvent);
        sendFaxDeliveryReceipt(faxPrintEvent.getSubcriberProfile(),faxPrintEvent.getMsgInfo(),faxPrintEvent.getFaxEventUniqueId(),faxPrintEvent.getFaxPrintNumber(),false);
        cancelStoredEvent(faxPrintEvent);
        setFaxPrintDone(faxPrintEvent.getMsgInfo());
        mer.faxPrintTimeout(faxPrintEvent.getSubscriberIdentity(),faxPrintEvent.isAutoPrint());

    }




    public void sendFaxDeliveryReceipt(UserInfo profile, MessageInfo msgId, String uniqueId, String faxPrintNumber, boolean isSuccess)
    {

        if(profile!=null)
        {
            if(uniqueId==null)
            {
                uniqueId = MsgStoreServer.getAnyMsgId();
            }
            if (logger.isDebugEnabled())logger.debug("sendFaxDeliveryReceipt to : "+profile.getTelephoneNumber() +" uniqueId: "+uniqueId+ " isSuccess: "+isSuccess);

            //Check if NTF should send receipt based on configuration
            if (logger.isDebugEnabled())logger.debug("sendFaxDeliveryReceipt Config.isFaxSuccessNotification() : "+Config.isFaxSuccessNotification() +" isSuccess: "+isSuccess+ " result: "+ ""+((isSuccess && Config.isFaxSuccessNotification()) || !isSuccess));



            if((isSuccess && Config.isFaxSuccessNotification()) || !isSuccess)
            {

                boolean isSMS = profile.isSystemMessageSMS();
                boolean isTUI = profile.isSystemMessageTUI();
                //Check if user has receipt enable
                if (logger.isDebugEnabled())logger.debug("sendFaxDeliveryReceipt to : "+profile.getTelephoneNumber() +" uniqueId: "+uniqueId+ " isSuccess: "+isSuccess+ " TUI: "+isTUI+ " SMS: "+isSMS);
                if(isSMS || isTUI)
                {

                    StateFile mfsStateFile=null;
                    //Extract information from the original message to send the notification
                    if(msgId!=null)
                    {
                        try
                        {
                            mfsStateFile = CommonMessagingAccess.getInstance().getStateFile(msgId);
                        }
                        catch(MsgStoreException e)
                        {
                            logger.info("sendFaxDeliveryReceipt unable to read state file",e);
                        }
                    }

                    if(isSMS)
                    {

                        sendSMSDeliveryReceipt(profile,mfsStateFile, uniqueId, faxPrintNumber,isSuccess);

                    }
                    if(isTUI)
                    {
                        sendTUIDeliveryReceipt(profile,mfsStateFile, uniqueId, faxPrintNumber,isSuccess);
                    }
                }
            }
        }
        else
        {
            logger.error("sendFaxDeliveryReceipt: unable to get subscriber profile will not be able to send receipt");
        }

    }

    private void sendTUIDeliveryReceipt(UserInfo profile, StateFile mfsStateFile, String uniqueId, String faxPrintNumber,
            boolean success) {
        if (logger.isDebugEnabled())logger.debug("sendTUIDeliveryReceipt: to "+profile.getTelephoneNumber() +" uniqueId: "+uniqueId+ " isSuccess: "+success);

        HashMap<String, String> props= new HashMap<String, String> ();
        try
        {
            props.put("msgtype", "fax");

            props.put("operation", "print");
            if(success)
            {
                props.put("status", "success");
            }
            else
            {
                props.put("status", "fail");
            }
            if(faxPrintNumber!=null)
            {
                props.put("recipient", faxPrintNumber);
            }

            if(mfsStateFile!=null)
            {

                String senderVisibility = mfsStateFile.getC1Attribute(Container1.Sender_visibility);
                boolean senderVisible=true;
                if (senderVisibility != null && !senderVisibility.isEmpty()) {
                    if ("0".equals(senderVisibility)) {
                        senderVisible = false;
                    }
                }
                if(senderVisible)
                {
                    String from = mfsStateFile.getC1Attribute(Container1.From);
                    if(from!=null)
                    {
                        props.put("sender", from);
                    }
                }
                String date = mfsStateFile.getC1Attribute(Container1.Date_time);
                if(date !=null)
                {
                    props.put("recvDate", date);

                }
            }
            mfsEventManager.createPropertiesFile(profile.getTelephoneNumber(), "sendstatusinformation", props);
            if (logger.isDebugEnabled())logger.debug("sendTUIDeliveryReceipt delivered  to: "+profile.getTelephoneNumber() +" uniqueId: "+uniqueId+ " isSuccess: "+success+" props: "+props);

            mer.faxprintRecieptNotificationDelivered(profile.getTelephoneNumber(), Constants.NTF_FAX_RECEIPT_TUI);

        }
        catch(TrafficEventSenderException e)
        {
            logger.error("sendTUIDeliveryReceipt Unable to deliver fax receipt to "+profile.getTelephoneNumber() +" uniqueId: "+uniqueId+ " isSuccess: "+success,e);
            mer.faxprintRecieptNotificationFailed(profile.getTelephoneNumber(),null, Constants.NTF_FAX_RECEIPT_TUI);
        }


    }

    private void sendSMSDeliveryReceipt(UserInfo profile,StateFile mfsStateFile, String uniqueId, String faxPrintNumber,boolean success)
    {
        if (logger.isDebugEnabled())logger.debug("sendSMSDeliveryReceipt to "+profile.getTelephoneNumber() +" uniqueId: "+uniqueId+ " isSuccess: "+success);


        SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = MoipMessageEntities.MESSAGE_SERVICE_NTF;
        req.destRcptID.value = profile.getTelephoneNumber();

        //Create dummy message id
        MSA msa = new MSA(profile.getMsid());
        req.oMsa.value = msa.getId();
        req.rMsa.value = msa.getId();
        req.rMsgID.value = MsgStoreServer.getAnyMsgId();
        req.oMsgID.value = MsgStoreServer.getAnyMsgId();

        req.eventType.value = MoipMessageEntities.SERVICE_TYPE_FAX_RECEIPT;
        req.eventID.value = uniqueId;



        HashMap<String, String> props= new HashMap<String, String> ();

        if(faxPrintNumber!=null)
        {
            props.put("faxprintnumber", faxPrintNumber);
        }

        if(mfsStateFile!=null)
        {
            String senderVisibility = mfsStateFile.getC1Attribute(Container1.Sender_visibility);
            if(senderVisibility!=null)
            {
                props.put("orig_sender_visibility", senderVisibility);
            }
            String from = mfsStateFile.getC1Attribute(Container1.From);
            if(from!=null)
            {
                props.put("orig_sender", from);
            }


            String receiver = mfsStateFile.getC1Attribute(Container1.To);
            if(receiver!=null)
            {
                props.put("origreceiver", receiver);

            }
            String date = mfsStateFile.getC1Attribute(Container1.Date_time);
            Date receivedDate=null;
            if (date != null) {
                receivedDate = NtfUtil.stringToDate(date);
            } else {
                receivedDate = new Date();
            }
            props.put("origdate", String.valueOf(receivedDate.getTime()));
            props.put("contenttype", success?Constants.FAX_DELEVERY_RECEIPT_CONTENT_TYPE_SUCCESS: Constants.FAX_DELEVERY_RECEIPT_CONTENT_TYPE_FAIL);

        }
        else
        {
            props.put("origreceiver", profile.getTelephoneNumber());
            props.put("contenttype", success?Constants.FAX_DELEVERY_RECEIPT_CONTENT_TYPE_SUCCESS_DEFAULT: Constants.FAX_DELEVERY_RECEIPT_CONTENT_TYPE_FAIL_DEFAULT);
        }
        req.extraValue = props;
        if (logger.isDebugEnabled())logger.debug("sendSMSDeliveryReceipt sending sms  to: "+profile.getTelephoneNumber() +" uniqueId: "+uniqueId+ " isSuccess: "+success+" props: "+props);

        SendMessageResp resp = NtfMessageService.get().sendMessage(req);
        if(resp.result.value.equalsIgnoreCase(Result.FAIL))
        {
            mer.faxprintRecieptNotificationFailed(profile.getTelephoneNumber(),null, Constants.NTF_FAX_RECEIPT_SMS);
        }
    }
}
