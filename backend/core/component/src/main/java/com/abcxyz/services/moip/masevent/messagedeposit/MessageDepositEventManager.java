/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.masevent.messagedeposit;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mrd.oamplugin.DispatcherConfigMgr;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageIDGen;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.LogAgentFactory;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class manages the handling of message deposit events.
 * 
 * All message deposit events are queued to be handled by the MessageDepositWorkers.
 * This is done to release the calling thread (especially since the calling thread can be 
 * that of a TUI session when the message deposit is made).
 */
public class MessageDepositEventManager {

    private static LogAgent logger = LogAgentFactory.getLogAgent(MessageDepositEventManager.class);

    private static final int MSG_DEPOSIT_EVENT_WORKERS_NUM = 10;
    private static final int MSG_DEPOSIT_EVENT_QUEUE_SIZE = 1000;

    private LinkedBlockingQueue<MessageDepositEvent> messageDepositEventQueue = null;
    private MessageDepositEventWorker[] messageDepositEventWorkers = null;

    private MessageDepositEventHandler messageDepositEventHandler = null;

    
    public MessageDepositEventManager() {
        //Initialise the event queue
        messageDepositEventQueue = new LinkedBlockingQueue <MessageDepositEvent>(MSG_DEPOSIT_EVENT_QUEUE_SIZE);
        
        //Initialise the event handler for retry handling
        messageDepositEventHandler = new MessageDepositEventHandler(messageDepositEventQueue);
        
        //Initialise the workers
        messageDepositEventWorkers = new MessageDepositEventWorker[MSG_DEPOSIT_EVENT_WORKERS_NUM];
        for (int i=0; i < MSG_DEPOSIT_EVENT_WORKERS_NUM; i++) {
            messageDepositEventWorkers[i] = new MessageDepositEventWorker("MessageDepositEventWorker-" + i, messageDepositEventQueue, messageDepositEventHandler);
        }
    }
    
    
    /**
     * To be called only after MRD has been started since this will start the retry event handler
     * and MRD is required to process retries of message deposit events.
     * @param serviceName the name of the service to be used by the retry event handler for event scheduling
     */
    public void startRetryEventHandler(String serviceName) {
        messageDepositEventHandler.start(serviceName);
    }

    
    /**
     * Sends an inform event to NTF from MAS for a new message deposit event.
     * Delegates this sending to a worker thread so that the calling thread 
     * does not have to wait for the inform event response.
     * @param msgInfo information about the message (OMSA, RMSA, OMSGID, RMSGID)
     * @param recipientId message recipient id (subscriber's phone number)
     */
    public void informNtf(MessageInfo msgInfo, String recipientId) {
        if (logger.isDebugEnabled()) {
            logger.debug("informNtf <" + msgInfo.toString() + "> for: " + recipientId);
        }
        
        //Schedule back up (retry) event
        //Properties to be kept in retry event, but not sending to NTF
        Properties properties = new Properties();
        properties.setProperty(MessageDepositEventHandler.RECIPIENT_ID, recipientId);
        properties.setProperty(MoipMessageEntities.OMSA, msgInfo.omsa.toString());
        properties.setProperty(MoipMessageEntities.RMSA, msgInfo.rmsa.toString());
        properties.setProperty(MoipMessageEntities.OMSGID, msgInfo.omsgid);
        properties.setProperty(MoipMessageEntities.RMSGID, msgInfo.rmsgid);
        
        String schedulerID = CommonOamManager.getInstance().getMrdOam().getConfigManager().getParameter(DispatcherConfigMgr.SchedulerID);
        AppliEventInfo eventInfo = messageDepositEventHandler.scheduleEvent(MoipMessageIDGen.getRecipientMessageID(msgInfo, schedulerID), EventTypes.DELIVERY.getName(), properties);

        //Need to release calling thread; delegate to worker thread by putting on the queue
        MessageDepositEvent msgDepEvent = new MessageDepositEvent(EventTypes.DELIVERY, recipientId, msgInfo, eventInfo, 0);
        if (!messageDepositEventQueue.offer(msgDepEvent)) {
            logger.warn("informNtf: Not stored in messageDepositEventQueue (full), next eventId:" + eventInfo.getEventId() + ", will retry");
        }
    }
    
}
