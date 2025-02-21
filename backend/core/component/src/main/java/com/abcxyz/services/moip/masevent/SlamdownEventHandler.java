package com.abcxyz.services.moip.masevent;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.MessageContext;
import com.abcxyz.messaging.mrd.operation.TriggerSendMessageReq;
import com.abcxyz.messaging.mrd.operation.TriggerSendMessageResp;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.LogAgentFactory;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.common.util.FaxPrintStatus;

/**
 * class implements event retry handling on MAS side.
 *
 * @author lmchuzh: setup framework for MAS
 *
 */
public class SlamdownEventHandler extends AbstractEventHandler {

    private static final String PROFILER_CHECKPOINT_NAME_PREFIX = "MAS.BKD.SEH.";
    private static final String PROFILER_CHECKPOINT_NAME_RETRY_SUFFIX = ".Retry.#";    
    private static final String PROFILER_CHECKPOINT_NAME_OK_RESULT_SUFFIX = "(OK)";
    private static final String PROFILER_CHECKPOINT_NAME_FAILED_RESULT_SUFFIX = "(Failed)";
    private static final String PROFILER_CHECKPOINT_NAME_DEPOSIT_EXPIR_SUFFIX = ".Expir.#";
    
    
	private static final LogAgent logger = LogAgentFactory.getLogAgent(SlamdownEventHandler.class);

	public static final String RECIPIENT_ID = "rcpid";
	public static final String OMSA = "omsa";
	public static final String RMSA = "rmsa";
	public static final String OMSGID = "omsgid";
	public static final String RMSGID = "rmsgid";
	public static final String EVENTTYPE = "masevttype";

	public SlamdownEventHandler() {
	}

	public void start(String serviceName) {
		//retrieve MAS scheduler event retry schema from OAM
   	 	ConfigManager localConfig = CommonOamManager.getInstance().getLocalConfig();

   	 	RetryEventInfo info = new RetryEventInfo(serviceName);
        info.setEventRetrySchema(localConfig.getParameter(MoipMessageEntities.MasRetrySchema));
        info.setExpireTimeInMinute(localConfig.getIntValue(MoipMessageEntities.MasExpireTimeInMinute));
   	 	start(info);
	}

	/**
	 * event fired from scheduler. Event will be cancelled automatically from returning of the method
	 *
	 * operations that have I/O, remote connections should be run in a new thread so not to block scheduler's thread
	 *
	 * return result can change the default retry schema
	 */
	@Override
	public int eventFired(AppliEventInfo eventInfo) {
        int result = EventHandleResult.OK;

	    try {
	        String eventType = eventInfo.getEventType();
	        logger.debug("eventFired eventType is " + eventType);
	        EventTypes type = EventTypes.get(eventType);

	        if (eventInfo.getNextEventInfo() == null || eventInfo.isExpire() || eventInfo.isLastExpire()) {

                result = EventHandleResult.STOP_RETRIES;

                String payloadFilename = eventInfo.getEventProperties().getProperty(MfsEventManager.PAYLOAD_FILENAME_KEY);	            
	            /**
	             * For now, only Slamdown and (potentially SendNotification) has payload.
	             * If event expires, MAS has the responsibility to delete payload file.
	             * - Slamdown stores payload under notifNumber private dir.
	             * - SendNotification potentially stores payload under recipient private dir.
	             * Need check both "notifNumber" property and "rcpid" property 
	             */
	            if (payloadFilename != null){
	                //delete payload file
	                String recipient = eventInfo.getEventProperties().getProperty(RECIPIENT_ID);
	                String notifNumber = eventInfo.getEventProperties().getProperty(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY);
	                String storageNumber = (notifNumber != null) ? notifNumber:recipient;
	                logger.debug("eventFired: fire Expir event. Deleting payload file " + payloadFilename + " for " + storageNumber);
	                
	                boolean isRemoved = false;
	                if (storageNumber != null){
	                    isRemoved = removeFile(storageNumber, payloadFilename);
	                } 

	                if (isRemoved) {
	                    logger.debug("Removed the payload file " + payloadFilename + " for " + storageNumber + " (if it existed)");
	                } else {
	                    logger.debug("Failed to remove the payload file " + payloadFilename + " for " + storageNumber);
	                    result = EventHandleResult.OK;
	                }
	            }
	            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    //Event fired is "mas-Expir"; eventType is then "Expir".  
                    //Try to get real event type (slmdw, mcn, etc.) from event property.
                    //If real event type not in properties, use "Expir"
                    String originalEventType = eventInfo.getEventProperties().getProperty(EVENTTYPE);
                    EventTypes originalType = null;
                    if(originalEventType != null) {
                        originalType = EventTypes.get(originalEventType);
                    } 

                    StringBuilder checkPoint = new StringBuilder(PROFILER_CHECKPOINT_NAME_PREFIX);
                    checkPoint.append(originalType != null ? originalType.getPerformanceProfilingName() : eventType);
                    checkPoint.append(PROFILER_CHECKPOINT_NAME_DEPOSIT_EXPIR_SUFFIX + eventInfo.getNumberOfTried());
                    profilerAgentCheckPoint(checkPoint.toString());
                }
	            return result;

	        } else {
	            // Notify NTF, all needed information can be retrieved from event properties
	            renotifyNtf(type, eventInfo);
	            if (logger.isDebugEnabled()) {
	                logger.debug("Event Fired <" + eventType + "> with id <" + eventInfo.getEventId() + ">");
	            }
	        }

	    } catch (Exception e) {
	        String message = "Exception for event " + eventInfo.getEventId();
	        if (eventInfo.getNextEventInfo() == null || eventInfo.isExpire() || eventInfo.isLastExpire()) {
                logger.error(message + ", will not retry", e);
                result = EventHandleResult.STOP_RETRIES;
	        } else {
                logger.debug(message + ", will retry", e);
	        }
	    }
		return result;
	}

	@Override
	public void reportCorruptedEventFail(String eventId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportEventCancelFail(AppliEventInfo eventInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportEventScheduleFail(AppliEventInfo eventInfo) {
		// TODO Auto-generated method stub

	}

	/**
	 * TODO run this in a separate thread
	 *
	 */
	private void renotifyNtf(EventTypes eventType, AppliEventInfo eventInfo) {
		Properties properties = eventInfo.getEventProperties();
		MessageInfo msgInfo = new MessageInfo();
		msgInfo.omsa = new MSA(properties.getProperty(OMSA));
		msgInfo.rmsa = new MSA(properties.getProperty(RMSA));
		msgInfo.omsgid = properties.getProperty(OMSGID);
		msgInfo.rmsgid = properties.getProperty(RMSGID);

		String rcpid = properties.getProperty(RECIPIENT_ID);

		if (logger.isDebugEnabled()) {
            logger.debug("MasEventHandler::renotifyNtf <" + eventType.getName() + "> for:" + rcpid);
        }

		TriggerSendMessageReq req = new TriggerSendMessageReq();
		req.destMsgClass.value = MoipMessageEntities.MESSAGE_SERVICE_NTF;
		req.destRcptID.value = rcpid;
		req.eventType = eventType.getName();
		req.oMsa.value = msgInfo.omsa.getId();
		req.rMsa.value = msgInfo.rmsa.getId();
		req.oMsgID.value = msgInfo.omsgid;
		req.rMsgID.value = msgInfo.rmsgid;

		if (req.eventType.equalsIgnoreCase(MoipMessageEntities.SERVICE_TYPE_SLAMDOWN) ||
		        req.eventType.equalsIgnoreCase(MoipMessageEntities.SERVICE_TYPE_MCN)) {
		    if (req.msgctx.extraValue == null) {
		        req.msgctx.extraValue = new HashMap<String, String>();
		    }
		    req.msgctx.extraValue.put(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY, properties.getProperty(MoipMessageEntities.SLAMDOWN_EVENT_FILE_PROPERTY));
		    req.msgctx.extraValue.put(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY, properties.getProperty(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY));
		} else if (req.eventType.equalsIgnoreCase(MoipMessageEntities.SERVICE_TYPE_FAX)){
            if (req.msgctx.extraValue == null) {
                req.msgctx.extraValue = new HashMap<String, String>();
            }
            req.msgctx.extraValue .put(MessageContext.SERVICE_PROPERTY_PREFIX + MoipMessageEntities.FAX_PRINT_NUMBER_PROPERTY,properties.getProperty(MoipMessageEntities.FAX_PRINT_NUMBER_PROPERTY) );
            req.msgctx.extraValue .put(MessageContext.SERVICE_PROPERTY_PREFIX + MoipMessageEntities.FAX_AUTOPRINT_ENABLE_PROPERTY,properties.getProperty(MoipMessageEntities.FAX_AUTOPRINT_ENABLE_PROPERTY) );
            req.msgctx.extraValue .put(MessageContext.SERVICE_PROPERTY_PREFIX + MoipMessageEntities.FAX_PRINT_FAXMSG_RMSA,properties.getProperty(MoipMessageEntities.FAX_PRINT_FAXMSG_RMSA) );
            req.msgctx.extraValue .put(MessageContext.SERVICE_PROPERTY_PREFIX + MoipMessageEntities.FAX_PRINT_FAXMSG_RMSGID,properties.getProperty(MoipMessageEntities.FAX_PRINT_FAXMSG_RMSGID) );
            req.msgctx.extraValue .put(MessageContext.SERVICE_PROPERTY_PREFIX + MoipMessageEntities.FAX_PRINT_FAXMSG_OMSA,properties.getProperty(MoipMessageEntities.FAX_PRINT_FAXMSG_OMSA) );
            req.msgctx.extraValue .put(MessageContext.SERVICE_PROPERTY_PREFIX + MoipMessageEntities.FAX_PRINT_FAXMSG_OMSGID,properties.getProperty(MoipMessageEntities.FAX_PRINT_FAXMSG_OMSGID) );
        } else if (req.eventType.equalsIgnoreCase(MoipMessageEntities.SERVICE_TYPE_MAILBOX_UPDATE)){
            if (req.msgctx.extraValue == null) {
                req.msgctx.extraValue = new HashMap<String, String>();
            }
            req.msgctx.extraValue .put(MessageContext.SERVICE_PROPERTY_PREFIX + MoipMessageEntities.MAILBOXUPDATE_MAILBOXID_PROPERTY,properties.getProperty(MoipMessageEntities.MAILBOXUPDATE_MAILBOXID_PROPERTY) );
            req.msgctx.extraValue .put(MessageContext.SERVICE_PROPERTY_PREFIX + MoipMessageEntities.MAILBOXUPDATE_USERAGENT_PROPERTY,properties.getProperty(MoipMessageEntities.MAILBOXUPDATE_USERAGENT_PROPERTY) );
            req.msgctx.extraValue .put(MessageContext.SERVICE_PROPERTY_PREFIX + MoipMessageEntities.MAILBOXUPDATE_CALLEDNUMBER_PROPERTY,properties.getProperty(MoipMessageEntities.MAILBOXUPDATE_CALLEDNUMBER_PROPERTY) );
            req.msgctx.extraValue .put(MessageContext.SERVICE_PROPERTY_PREFIX + MoipMessageEntities.MAILBOXUPDATE_FORCE_PROPERTY,properties.getProperty(MoipMessageEntities.MAILBOXUPDATE_FORCE_PROPERTY) );
        } else if (req.eventType.equalsIgnoreCase(MoipMessageEntities.SERVICE_TYPE_MWI_OFF)){
            //do nothing
        } else {
            //generic type retry
            logger.debug("renotifyNtf: generic type retry");
            
            //set all properties to be sent
            if ( properties.size() > 0) {
                if (req.msgctx.extraValue == null) {
                    req.msgctx.extraValue = new HashMap<String, String>();
                }
                String key;
                for (Enumeration<?> e = properties.keys(); e.hasMoreElements();) {
                    key = (String)e.nextElement();
                    req.msgctx.extraValue.put(MessageContext.SERVICE_PROPERTY_PREFIX + key,properties.getProperty(key) );
                }
            }
            
        }

        try {
            //Pass the event to MRD. The latter will handle the cancellation of the event on successful response from NTF
            if (eventInfo.getNextEventInfo() != null) {
                req.retryEventId = new EventID(eventInfo.getNextEventInfo().getEventId());
            }
        } catch (InvalidEventIDException e) {
            logger.warn("SlamdownEventHandler::renotifyNtf Exception:" + e.getMessage());
            e.printStackTrace();
        }


		if (CommonMessagingAccess.getDispacher() != null) {
			TriggerSendMessageResp resp = CommonMessagingAccess.getDispacher() .triggerSendMessage(req);

			if(eventType == EventTypes.FAX_PRINT) {
                try{
                    if(!resp.result.isOk())
                    {
                        String rmsa = properties.getProperty("rmsa");
                        String rmsgid =properties.getProperty("rmsgid");
                        String omsa = properties.getProperty("omsa");
                        String omsgid =properties.getProperty("omsgid");
                        if(rmsa!=null && rmsgid!=null &&omsa!=null &&omsgid!=null)
                        {
                            MessageInfo messageInfo = new MessageInfo(new MSA(omsa), new MSA(rmsa), omsgid, rmsgid);

                            StateFile mfsStateFile = CommonMessagingAccess.getInstance().getStateFile(messageInfo);
                            FaxPrintStatus.changeStatus(mfsStateFile, FaxPrintStatus.done);
                        }

                    }
                }
                catch(MsgStoreException e) {
                    logger.error("CommonMessagingAccess::notifyNtf Unable to set fax as done exception",e);
                }
            }
			
			if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                StringBuilder checkPoint = new StringBuilder(PROFILER_CHECKPOINT_NAME_PREFIX);
                checkPoint.append(eventType.getPerformanceProfilingName());
                checkPoint.append(PROFILER_CHECKPOINT_NAME_RETRY_SUFFIX).append(eventInfo.getNumberOfTried());
                checkPoint.append(resp.result.isOk() ? PROFILER_CHECKPOINT_NAME_OK_RESULT_SUFFIX : PROFILER_CHECKPOINT_NAME_FAILED_RESULT_SUFFIX);
                profilerAgentCheckPoint(checkPoint.toString());
            }

			if (logger.isDebugEnabled()) {
				logger.debug("CommonMessagingAccess::notifyNtf <" + eventType + "> for :" + rcpid + " response: " + resp.result);
			}
		}
	}

    /**
     * Profiling method.
     * This method does not check if profiling is enabled; the caller is responsible for checking.
     * This was done to prevent unnecessary String object creation for check point names when profiling is not enabled (normal live traffic case).
     * Only if profiling is enabled, caller should create check point name and call this method.
     * @param checkPoint - name of profiling check point
     */
    private void profilerAgentCheckPoint(String checkPoint) {
        Object perf = null;
        try {
            perf = CommonOamManager.profilerAgent.enterCheckpoint(checkPoint);
        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }
    }
	/*
	 * Delete payload file
	 * @param notificationNumber
	 * @param payload file name
	 */
    private boolean removeFile(String telephoneNumber, String fileName) {
        boolean isRemoved = false;
        try {
            IMfsEventManager mfsEventManager = MfsEventFactory.getMfsEvenManager();
            mfsEventManager.removeFile(telephoneNumber, fileName);
            isRemoved = true;
        } catch (TrafficEventSenderException e) {
            logger.error("Remove file failed for telephoneNumber=" + telephoneNumber + " fileName=" + fileName + ": " + e.getMessage());
        }
        return isRemoved;
    }

}
