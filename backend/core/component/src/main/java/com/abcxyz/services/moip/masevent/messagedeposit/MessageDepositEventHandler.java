/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.masevent.messagedeposit;

import java.util.Properties;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.masevent.AbstractEventHandler;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.LogAgentFactory;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class implements event retry handling on MAS side.
 */
public class MessageDepositEventHandler extends AbstractEventHandler {

    private static final String PROFILER_CHECKPOINT_NAME_DEPOSIT_EXPIR = "MAS.BKD.MDEH.Deposit.Expir.#";

    private static LogAgent logger = LogAgentFactory.getLogAgent(MessageDepositEventHandler.class);

    private static final String CONFIG_PARAM_MSG_DEPOSIT_EVENT_RETRY_SCHEMA = "masMessageDepositEventRetrySchema";
    private static final String CONFIG_PARAM_MSG_DEPOSIT_EVENT_EXPIRE_TIME_IN_MIN = "masMessageDepositEventExpireTimeInMin";

    private static final String DEFAULT_MSG_DEPOSIT_EVENT_RETRY_SCHEMA = "60 CONTINUE";
    private static final int DEFAULT_MSG_DEPOSIT_EVENT_EXPIRE_TIME_IN_MIN = 2880;  // 2 days (in minutes)

    public static final String RECIPIENT_ID = "rcpid";
    
    private LinkedBlockingQueue<MessageDepositEvent>  messageDepositEventQueue = null;

    
    public MessageDepositEventHandler(LinkedBlockingQueue<MessageDepositEvent> messageDepositEventQueue) {
        this.messageDepositEventQueue = messageDepositEventQueue;
    }

    
    public void start(String serviceName) {
        String retrySchema = null;
        int expiryTimeInMin;
        try {
            IGroup backendGroup = CommonOamManager.getInstance().getConfiguration().getGroup(CommonOamManager.BACK_END_CONF);
            retrySchema = backendGroup.getString(CONFIG_PARAM_MSG_DEPOSIT_EVENT_RETRY_SCHEMA);
            expiryTimeInMin = backendGroup.getInteger(CONFIG_PARAM_MSG_DEPOSIT_EVENT_EXPIRE_TIME_IN_MIN);
        } catch (Exception e) {
            logger.error("start: Failed to get retry schema and expiry from " + CommonOamManager.BACK_END_CONF + ", " + e.getMessage(), e);

            retrySchema = DEFAULT_MSG_DEPOSIT_EVENT_RETRY_SCHEMA;
            logger.error("Default masDelivRetrySchema will be used: " + retrySchema);

            expiryTimeInMin = DEFAULT_MSG_DEPOSIT_EVENT_EXPIRE_TIME_IN_MIN;
            logger.error("Default expiryTime will be used: " + expiryTimeInMin);
        }

        RetryEventInfo info = new RetryEventInfo(serviceName);
        info.setEventRetrySchema(retrySchema);
        info.setExpireTimeInMinute(expiryTimeInMin);
        start(info);
    }


    @Override
    public int eventFired(AppliEventInfo eventInfo) {
        int result = EventHandleResult.OK;

        try {
            String eventType = eventInfo.getEventType();
            if (logger.isDebugEnabled()) {
                logger.debug("Event fired: " + eventInfo.getEventId());
            }
            EventTypes type = EventTypes.get(eventType);

            if (eventInfo.getNextEventInfo() == null || eventInfo.isExpire() || eventInfo.isLastExpire()) {
                result = EventHandleResult.STOP_RETRIES;
                logger.warn("Stopping to send message deposit signal to NTF due to expiry for event: " + eventInfo.getEventId());
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    profilerAgentCheckPoint(PROFILER_CHECKPOINT_NAME_DEPOSIT_EXPIR + eventInfo.getNumberOfTried());
                }
            } else {
                // Notify NTF, all needed information can be retrieved from event properties
                informNtf(type, eventInfo);
            }

        } catch (Exception e) {
            String message = "Exception for event " + eventInfo.getEventId();
            if (eventInfo.getNextEventInfo() == null || eventInfo.isExpire() || eventInfo.isLastExpire()) {
                logger.error(message + ", will not retry", e);
                result = EventHandleResult.STOP_RETRIES;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(message + ", will retry", e);
                }
            }
        }
        return result;
    }

    
    /**
     * Retries to send an inform event to NTF from MAS for a message deposit event.
     * Delegates this sending to a worker thread so that the calling thread 
     * does not have to wait for the inform event response.
     * @param eventType the type of event
     * @param eventInfo the current retry event that fired
     */
    private void informNtf(EventTypes eventType, AppliEventInfo eventInfo) {
        Properties properties = eventInfo.getEventProperties();
        String rcpid = properties.getProperty(RECIPIENT_ID);

        if (logger.isDebugEnabled()) {
            logger.debug("informNtf " + eventType.getName() + " for:" + rcpid);
        }

        MessageInfo msgInfo = new MessageInfo(new MSA(properties.getProperty(MoipMessageEntities.OMSA)), 
                new MSA(properties.getProperty(MoipMessageEntities.RMSA)), 
                properties.getProperty(MoipMessageEntities.OMSGID), 
                properties.getProperty(MoipMessageEntities.RMSGID));

        //Release calling thread; delegate to worker thread by putting on the queue
        MessageDepositEvent msgDepEvent = new MessageDepositEvent(eventType, rcpid, msgInfo, eventInfo.getNextEventInfo(), eventInfo.getNumberOfTried());
        if (!messageDepositEventQueue.offer(msgDepEvent)) {
            logger.warn("informNtf: Not stored in messageDepositEventQueue (full), next eventId:" + eventInfo.getNextEventInfo().getEventId() + ", will retry");
        }
    }  
    
    /**
     * Profiling method.
     * This method does not check if profiling is enabled; the caller is responsible for checking.
     * This was done to prevent unnecessary String object creation for check point names when profiling is not enabled (normal live traffic case).
     * Only if profiling is enabled, caller should create check point name and call this method.
     * @param checkPoint - name of profiling check point
     */
    private static void profilerAgentCheckPoint(String checkPoint) {
        Object perf = null;
        try {
            perf = CommonOamManager.profilerAgent.enterCheckpoint(checkPoint);
        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }
    }
}
