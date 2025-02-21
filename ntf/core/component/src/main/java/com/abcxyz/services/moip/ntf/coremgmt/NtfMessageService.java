/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mrd.data.EventType;
import com.abcxyz.messaging.mrd.data.MessageContext;
import com.abcxyz.messaging.mrd.data.Reason;
import com.abcxyz.messaging.mrd.data.Result;
import com.abcxyz.messaging.mrd.operation.InformEventReq;
import com.abcxyz.messaging.mrd.operation.InformEventResp;
import com.abcxyz.messaging.mrd.operation.MsgServerOperations;
import com.abcxyz.messaging.mrd.operation.SendMessageReq;
import com.abcxyz.messaging.mrd.operation.SendMessageResp;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventGenerator;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;

/**
 * Class provides NTF message service implementation for handling message requests from remote MRD
 */
public class NtfMessageService extends MsgServerOperations {

    private static LogAgent logger = NtfCmnLogger.getLogAgent(NtfMessageService.class);;
    private ManagedArrayBlockingQueue<Object> ntfMessageServiceQueue;
    private NtfMessageServiceWorker[] ntfMessageServiceWorkers;
    private static final int NTF_MESSAGE_SERVICE_WORKERS = 10;
    private static final int NTF_MESSAGE_SERVICE_QUEUE_SIZE = 1000;
    private static NtfMessageService instance = null;

    private int numOfSendMessageReceived;
    private int numOfExpiredMessageReceived;
    private int numOfInformEventReceived;
    private int numOfExpiredInformEventReceived;
  
    public final static String DELIMITER = ",";
    public final static String EQUAL = ":";

    // Incoming property keys or values from MRD 
    public static final String GENERIC_EVENT_TYPE_VALUE = "sendNotification";
    private static final String SERVICE_TYPE_PROPERTY = "service_type";
    public static final String DATE_KEY = "date";

    // NTF keys or values 
    public static final String SEND_NOTIFICATION_PROPERTY = "sendNotif";

    public NtfMessageService() {
        ntfMessageServiceQueue = new ManagedArrayBlockingQueue<Object>(NTF_MESSAGE_SERVICE_QUEUE_SIZE);
        ntfMessageServiceWorkers = new NtfMessageServiceWorker[NTF_MESSAGE_SERVICE_WORKERS];
        for (int i=0; i<NTF_MESSAGE_SERVICE_WORKERS; i++) {
            ntfMessageServiceWorkers[i] = new NtfMessageServiceWorker("NtfMessageServiceWorker-" + i, ntfMessageServiceQueue);
        }
    }

    public static NtfMessageService get() {
        if (instance == null) {
            instance = new NtfMessageService();
        }
        return instance;
    }

    public ManagedArrayBlockingQueue<Object> getQueue() {
        return ntfMessageServiceQueue;
    }

    /**
     * handling MRD send message request
     * @return SendMessageResp
     */
    public SendMessageResp sendMessage(SendMessageReq smreq) {
        Object perf = null;
        SendMessageResp resp = new SendMessageResp();
        boolean backupCreated = false;

        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("NTF.Trace.1.NMS.SendMessage");
            }

	        if (smreq.validate() != null) {
	            logger.warn("NtfMessageService received invalid sendMessage request return 400 for: " + smreq.toString());
	            resp.transID.value = smreq.transID.value;
	            resp.result.value = Result.FAIL;
	            resp.reason.value = Reason.BAD_REQUEST_400;
	            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    NtfMessageService.profilerAgentCheckPoint("NTF.1.NMS.SendMessage.Invalid");
                }
	            return resp;
	        }

	        //Set the transaction id inside the response
	        resp.transID.value = smreq.transID.value;
	        
	        if (logger.isDebugEnabled()) {
	            logger.debug("NtfMessageService received SendMessage: " + smreq.toString());
	        }

	        if (smreq.eventType.isType(EventType.EXPIRY)) {
	            numOfExpiredMessageReceived ++;
	            if (logger.isDebugEnabled()) {
	                logger.debug("NtfMessageService received SendMessage for expire: " + smreq.toString() + " total expired: " + numOfExpiredMessageReceived);
	            }

	            resp.result.value = Result.OK;
	            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    NtfMessageService.profilerAgentCheckPoint("NTF.1.NMS.SendMessage.Expiry");
                }
	            return resp;
	        }

	        // Construct NTF event, the event is created based on message type
	        MessageInfo msgInfo = new MessageInfo(new MSA(smreq.oMsa.value), new MSA(smreq.rMsa.value), smreq.oMsgID.value, smreq.rMsgID.value);

	        // The request might not contain any extra parameters, if so, the destRcptID still needs to be part of the event
	        if (smreq.extraValue == null) {
	            smreq.extraValue = new HashMap<String, String>();
	        }
	        smreq.extraValue.put(Constants.DEST_RECIPIENT_ID, smreq.destRcptID.value);

            boolean isSendNotification = false;
	        String serviceType = smreq.extraValue.get(MoipMessageEntities.SERVICE_TYPE_CODED);
	        if (serviceType == null) {//always come to here for now,
	            if (smreq.eventType.isType(EventType.DELIVERY)) {//from MRD hand-over, it's always this type
	                serviceType = NtfEventTypes.DEFAULT_NTF.getName();
	            } else {
	                //from MRD trigger send message, event types are the ones defined in common class
	                //this is from one of the special service messages
	                serviceType = smreq.eventType.value;
	                if (serviceType.contains("-")) {
	                    serviceType = serviceType.split("-")[1];
	                    if (GENERIC_EVENT_TYPE_VALUE.equalsIgnoreCase(serviceType)) {
	                        isSendNotification = true;
	                        serviceType = smreq.extraValue.get(MessageContext.SERVICE_PROPERTY_PREFIX + SERVICE_TYPE_PROPERTY);
	                        logger.debug("NtfMessageService.SendNotification: " + serviceType);
	                    }
	                }

	                if (serviceType.equalsIgnoreCase(EventType.DELIVERY)) {
	                    serviceType = NtfEventTypes.DEFAULT_NTF.getName();
	                }
	            }
	        }

	        // Create NTF event
	        NtfEvent ntfEvent = NtfEventGenerator.generateEvent(serviceType, msgInfo, smreq.extraValue, smreq.eventID.value);
	        ntfEvent.setProperty(SEND_NOTIFICATION_PROPERTY, Boolean.toString(isSendNotification));

	        NtfRetryHandling schedulerHandler = NtfEventHandlerRegistry.getEventHandler(ntfEvent.getEventServiceTypeKey());
	        if (schedulerHandler != null) {
	            String backupId = schedulerHandler.scheduleEvent(ntfEvent);
	            ntfEvent.keepReferenceID(backupId);
	            backupCreated = true;
	            logger.debug("NtfMessageService SendMessage scheduled backup (Level-2): " + backupId);
	        } else {
	            logger.error("NtfMessageService Unable to get NtfRetryEventHandler");
	            resp.result.value = Result.FAIL;
	            resp.reason.value = Reason.TEMPORARY_UNAVAILABLE_480;
	            return resp;
	        }

	        boolean storedInQueue = ntfMessageServiceQueue.offer(ntfEvent);
	        if (!storedInQueue) {
	            logger.warn("NtfMessageService.sendMessage: Not stored in ntfMessageServiceQueue (full), eventId:" + ntfEvent.getReferenceId() + ", will retry");
	            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    NtfMessageService.profilerAgentCheckPoint("NTF.1.NMS.SendMessage.NotQueued");
                }
            } else {
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    NtfMessageService.profilerAgentCheckPoint("NTF.1.NMS.SendMessage.Queued");
                }
	        }

	        // Return back to MRD as the event is now in NTF's hand (Level-2)
	        resp.result.value = Result.OK;
	        resp.reason.value = Reason.HAND_OFF_2000;

	        if (logger.isDebugEnabled()) {
	            logger.debug("NtfMessageService send back SendMessageResp: " + resp.toString() + " total handled: " + numOfSendMessageReceived);
	        }

	        numOfSendMessageReceived++;

        } catch (Throwable e) {
            logger.error("NtfMessageService.sendMessage exception: ", e);
            if (backupCreated) {
                resp.result.value = Result.OK;
                resp.reason.value = Reason.HAND_OFF_2000;
            } else {
                resp.result.value = Result.FAIL;
                resp.reason.value = Reason.TEMPORARY_UNAVAILABLE_480;
            }
        } finally {
            if (perf != null) {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
        }

        return resp;
    }

    /**
     * handling parsing of sendNotification EventType attributes
     * @param nvp String  DELIMITER separated list of nvp (ie attributes:values)  
     * @return Hashtable<String,String> - list of attributes and their values
     */
    private Hashtable<String,String> getSendNotificationAttribute(String nvp){
        
        Hashtable<String,String> result = new Hashtable<String,String>();
        String[] tokens = nvp.split(DELIMITER); // delimiters for properties (i.e.: prop1,prop2,prop3...)
            
        for (int i=0; i<tokens.length; i++) {
            int idx = tokens[i].trim().indexOf(EQUAL); // EQUAL for properties nvp (i.e.: prop_name:prop_value)
            if (idx <=0) {
                logger.warn("NtfMessageService received invalid sendNotification eventType property for: " + nvp);
                return null;
            }

            String name = tokens[i].substring(0, idx).toLowerCase().trim();
            String value = tokens[i].substring(idx+1).toLowerCase().trim();
            
            if(value.length()<=0){
                logger.warn("NtfMessageService received invalid sendNotification eventType property for: " + nvp);
                return null;
            }
            result.put(name, value);
        }

        //check for mandatory attribute 
        String serviceType = result.get(SERVICE_TYPE_PROPERTY);
        if (serviceType == null) {
            logger.warn("NtfMessageService received invalid sendNotification " + serviceType + " mandatory property not found for: " + nvp);
            return null;
        }

        // Add date property if not already provided
        String formattedDate = result.get(DATE_KEY);
        if (formattedDate == null || formattedDate.isEmpty()) {
            Long dateInMilliseconds = Calendar.getInstance().getTimeInMillis();
            formattedDate = MfsEventManager.dateFormat.get().format(new Date(dateInMilliseconds));
            logger.debug("No " + DATE_KEY + " property found for event " + serviceType + ", generated date " + formattedDate + " (" + dateInMilliseconds + "ms) will be used");
            result.put(DATE_KEY, Long.toString(dateInMilliseconds));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("NtfMessageService.informEvent req values: " + result.toString());
        }
        return result;
    }     

    /**
     * handling MRD send message request
     * @return InformEventResp
     */
    public InformEventResp informEvent(InformEventReq req) {
        Object perf = null;
        InformEventResp resp = new InformEventResp();
        boolean backupCreated = false;

        try {
            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                perf = CommonOamManager.profilerAgent.enterCheckpoint("NTF.Trace.1.NMS.InformEvent");
            }

            String reqValidateResult = req.validate();
            if (reqValidateResult != null && !reqValidateResult.equalsIgnoreCase(Reason.OK)) {
                logger.warn("NtfMessageService.informEvent received invalid informEvent request return 400 for: " + req.toString());
                resp.informEventResult.value = Result.FAIL;
                resp.reason.value = Reason.BAD_REQUEST_400;
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    NtfMessageService.profilerAgentCheckPoint("NTF.1.NMS.InformEvent.Invalid");
                }
                return resp;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("NtfMessageService.informEvent received informEvent: " + req.toString());
            }

            //Set the transaction id inside the response
            resp.transID.value = req.transID.value;

            if (req.informEventType.value.equalsIgnoreCase(EventType.EXPIRY)) {
                //
                numOfExpiredInformEventReceived ++;
                if (logger.isDebugEnabled()) {
                    logger.debug("NtfMessageService.informEvent received informEvent for expire: " + req.toString() + " total expired: " + numOfExpiredInformEventReceived);
                }

                resp.informEventResult.value = Result.OK;
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    NtfMessageService.profilerAgentCheckPoint("NTF.1.NMS.InformEvent.Expiry");
                }
                return resp;
            }

            String serviceType = null;
            // The request might not contain any extra parameters, if so, the destRcptID still needs to be part of the event
            if (req.extraValue == null) {
                req.extraValue = new HashMap<String, String>();
            }

            // extract serviceType and properties for sendNotification eventType                
            boolean isSendNotification = false;
            if(req.informEventType !=null && req.informEventType.value.equalsIgnoreCase(GENERIC_EVENT_TYPE_VALUE) ){
                resp.informEventResult.value = Result.FAIL;
                resp.reason.value = Reason.BAD_REQUEST_400;

                String desRcpt = req.destRcptID.value;
                if(desRcpt==null || desRcpt.isEmpty()){
                    return resp;
                }

                String nvp = req.informEventProp.value;                    
                Hashtable<String, String> sendNotificationAttributes = getSendNotificationAttribute(nvp); 
                if(sendNotificationAttributes==null){
                    return resp;
                }

                serviceType = sendNotificationAttributes.get(SERVICE_TYPE_PROPERTY);
                sendNotificationAttributes.remove(SERVICE_TYPE_PROPERTY);
                req.extraValue.putAll(sendNotificationAttributes);

                isSendNotification = true;
                logger.debug("NtfMessageService.InformEvent: " + serviceType);
            } else if (req.extraValue != null) {//currently not used feature
                serviceType = req.extraValue.get(MoipMessageEntities.SERVICE_TYPE_CODED);
            }

            if (serviceType == null) {//always come to here for now,
                if (req.informEventType.value.equalsIgnoreCase(EventType.DELIVERY)) {//from MRD hand-over, it's always this type
                    serviceType = NtfEventTypes.DEFAULT_NTF.getName();
                } else {
                    //from MRD trigger send message, event types are the ones defined in common class
                    //this is from one of the special service messages
                    serviceType = req.informEventType.value;
                    if(serviceType.contains("-")){
                        serviceType = serviceType.split("-")[1];
                    }
                    if (serviceType.equalsIgnoreCase(EventType.DELIVERY)) {
                        serviceType = NtfEventTypes.DEFAULT_NTF.getName();
                    } else if (serviceType.equalsIgnoreCase(EventTypes.SUBSCRIBER_ACTIVITY_DETECTED.getName())){
                        // Checking for the case the subscriber has reacted to a notification so we cancel the timeout event for SIM Swap feature
                        if (logger.isDebugEnabled()) {
                            logger.debug("NtfMessageService.informEvent received informEvent for a deposit: " + req.toString() + ". The timeout, if it exist, will be cancelled");
                        }

                        // Get the subscriber ID from the event
                        serviceType = NtfEventTypes.SUBSCRIBER_VVM_SYSTEM_ACTIVITY_DETECTED.getName();

                    } else if(serviceType.equalsIgnoreCase(EventTypes.SUBSCRIBER_VVM_SYSTEM_DEACTIVATED.getName())) {
                        // Checking for a request to send a de-activation email to the subscriber
                        if (logger.isDebugEnabled()) {
                            logger.debug("NtfMessageService.informEvent received informEvent: " + req.toString() + ". The subsriber was de-activated and we want to notify him via an sms.");
                        }
                        // Inject the event in the level 2 queues of events that will take care to send the email
                        serviceType = NtfEventTypes.SUBSCRIBER_VVM_SYSTEM_DEACTIVATED.getName();
                    } else if(serviceType.equalsIgnoreCase(EventTypes.SUBSCRIBER_VVM_IMAP_ACTIVITY_DETECTED.getName())) {
                        // Checking for a request to send a VVM IMAP activation SMS to the subscriber
                        if (logger.isDebugEnabled()) {
                            logger.debug("NtfMessageService.informEvent received informEvent: " + req.toString() + ". The subsriber IMAP activity was detected and we want to notify him via an sms.");
                        }
                        // Inject the event in the level 2 queues of events that will take care to send the sms
                        serviceType = NtfEventTypes.SUBSCRIBER_VVM_IMAP_FIRST_DETECTED.getName();
                    }
                }
            }

            //construct NTF event, the event is created based on message type
            //MessageInfo msgInfo = new MessageInfo();
            //msgInfo.omsa = req.oMsa.value != null ? new MSA(req.oMsa.value): null;
            MessageInfo msgInfo = new MessageInfo(req.oMsa.value != null ? new MSA(req.oMsa.value) : new MSA(""),
                    req.rMsa.value != null ? new MSA(req.rMsa.value) : new MSA(""),
                            req.oMsgID.value != null ? req.oMsgID.value : "",
                                    req.rMsgID.value != null ? req.rMsgID.value : "");
            req.extraValue.put(Constants.DEST_RECIPIENT_ID, req.destRcptID.value);

            // Create NTF event 
            NtfEvent ntfEvent = NtfEventGenerator.generateEvent(serviceType, msgInfo, req.extraValue, req.activeEventID.value);
            ntfEvent.setProperty(SEND_NOTIFICATION_PROPERTY, Boolean.toString(isSendNotification));

            logger.debug("NtfMessageService.informEvent properties: " + ntfEvent.getEventProperties().toString());

            NtfRetryHandling handler = NtfEventHandlerRegistry.getEventHandler(ntfEvent.getEventServiceTypeKey());
            if (handler != null) {
                String backupId = handler.scheduleEvent(ntfEvent);
                ntfEvent.keepReferenceID(backupId);
                backupCreated = true;
                logger.debug("NtfMessageService SendMessage scheduled backup (Level-2): " + backupId);
            } else {
                logger.error("NtfMessageService Unable to get NtfRetryEventHandler");
                resp.informEventResult.value = Result.FAIL;
                resp.reason.value = Reason.TEMPORARY_UNAVAILABLE_480;
                return resp;
            }

            boolean storedInQueue = ntfMessageServiceQueue.offer(ntfEvent);
            if (!storedInQueue) {
                logger.warn("NtfMessageService.sendMessage: Not stored in ntfMessageServiceQueue (full), eventId:" + ntfEvent.getReferenceId() + ", will retry");
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    NtfMessageService.profilerAgentCheckPoint("NTF.1.NMS.InformEvent.NotQueued");
                }
            } else {
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    NtfMessageService.profilerAgentCheckPoint("NTF.1.NMS.InformEvent.Queued");
                }
            }

            // Return back to MRD as the event is now in NTF's hand (Level-2)
            resp.informEventResult.value = Result.OK;
            resp.reason.value = Reason.HAND_OFF_2000;

            if (logger.isDebugEnabled()) {
                logger.debug("NtfMessageService.informEvent send back informEventResp: " + resp.toString() + " total handled: " + numOfInformEventReceived);
            }

            numOfInformEventReceived++;

        } catch (Throwable e) {
            logger.error("NtfMessageService.informEvent exception: ", e);
            if (backupCreated) {
                resp.informEventResult.value = Result.OK;
                resp.reason.value = Reason.HAND_OFF_2000;
            } else {
                resp.informEventResult.value = Result.FAIL;
                resp.reason.value = Reason.TEMPORARY_UNAVAILABLE_480;
            }
        } finally {
            if (perf != null) {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
        }

        return resp;
    }

    /**
     * return this message server class name.
     */
    public String getMsgServerClass() {
        return MoipMessageEntities.MESSAGE_SERVICE_NTF;
    }

    public int getNumOfSendMessageReceived() {
        return numOfSendMessageReceived;
    }

    public int getNumOfExpiredMessageReceived() {
        return numOfExpiredMessageReceived;
    }
    
    public static void profilerAgentCheckPoint(String checkPoint) {
        Object perf = null;
        try {
            perf = CommonOamManager.profilerAgent.enterCheckpoint(checkPoint);
        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }
    }
}
