/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.masevent.messagedeposit;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mrd.operation.InformEventReq;
import com.abcxyz.messaging.mrd.operation.InformEventResp;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.LogAgentFactory;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * This class does the work of informing NTF of message deposit events.
 */
public class MessageDepositEventWorker extends Thread {
    
    private static final String PROFILER_CHECKPOINT_NAME_DEPOSIT = "MAS.BKD.MDEW.Deposit";
    private static final String PROFILER_CHECKPOINT_NAME_RETRY_SUFFIX = ".Retry#";
    private static final String PROFILER_CHECKPOINT_NAME_OK_RESULT_SUFFIX = "(OK)";
    private static final String PROFILER_CHECKPOINT_NAME_FAILED_RESULT_SUFFIX = "(Failed)";
    
    private LogAgent logger = LogAgentFactory.getLogAgent(MessageDepositEventWorker.class);
    private LinkedBlockingQueue<MessageDepositEvent> messageDepositEventQueue = null;
    private MessageDepositEventHandler messageDepositEventHandler = null;

    
    public MessageDepositEventWorker(String threadName, LinkedBlockingQueue<MessageDepositEvent> messageDepositEventQueue, MessageDepositEventHandler messageDepositEventHandler) {
        super(threadName);
        this.messageDepositEventQueue = messageDepositEventQueue;
        this.messageDepositEventHandler = messageDepositEventHandler;
        setDaemon(true);
        start();
    }

    public boolean doWork() {

        // Get an event from the working queue
		
		try {
				MessageDepositEvent messageDepositEvent = messageDepositEventQueue.take();
				if (messageDepositEvent == null) { 
					return false;
				}
		
				synchronized (messageDepositEvent) {
					try {
						if (CommonMessagingAccess.getDispacher() != null) {
							if (logger.isDebugEnabled()) {
								logger.debug("Sending inform event for: " + messageDepositEvent.toString());
							}
							InformEventReq req = CommonMessagingAccess.constructInformEventReq(messageDepositEvent.getEventType(), messageDepositEvent.getRecipientId(), messageDepositEvent.getMsgInfo(), null);            
							InformEventResp resp = CommonMessagingAccess.getDispacher().informEvent(req);            

							if (resp == null || resp.informEventResult == null || !resp.informEventResult.isOk()) {
								logger.info("Inform event response result not OK for: " + messageDepositEvent.getRecipientId() + " " + messageDepositEvent.getMsgInfo() + ", reason:" + (resp == null ? "null" : resp.reason));
								if (CommonOamManager.profilerAgent.isProfilerEnabled()) {    
		                            profilerAgentCheckPoint(messageDepositEvent, false);
		                        }
							} else {
								messageDepositEventHandler.cancelEvent(messageDepositEvent.getNextEventInfo());
								if (logger.isDebugEnabled()) {
									logger.debug("Inform event response result OK for: " + messageDepositEvent.getRecipientId() + " " + messageDepositEvent.getMsgInfo());
								}
								if (CommonOamManager.profilerAgent.isProfilerEnabled()) {    
		                            profilerAgentCheckPoint(messageDepositEvent, true);
		                        }
							}

							return true;
							
						} else {
							logger.error("Dispatcher is null; failed inform event for: " + messageDepositEvent.getRecipientId() + " " + messageDepositEvent.getMsgInfo());
							return false;
						}
					} catch (Throwable t) {
						logger.error("MessageDepositEventWorker Throwable: ", t);
					}
				}					
		} catch (InterruptedException e) {
			return false;
		}
		return false;
    }
    
   
    /**
     * Profiling method.
     * This method does not check if profiling is enabled; the caller is responsible for checking.
     * This was done to prevent unnecessary String object creation for check point names when profiling is not enabled (normal live traffic case).
     * Only if profiling is enabled, caller should create check point name and call this method.
     * @param messageDepositEvent the processed message deposit event
     * @param isOk boolean indicating if the response for the processed message deposit event was OK (true) or Failed (false)
     */
    private static void profilerAgentCheckPoint(MessageDepositEvent messageDepositEvent, boolean isOk) {
        Object perf = null;        
        StringBuilder checkPoint = new StringBuilder(PROFILER_CHECKPOINT_NAME_DEPOSIT);
        int retryNum = messageDepositEvent.getRetryNumber();
        if(retryNum > 0) {
            checkPoint.append(PROFILER_CHECKPOINT_NAME_RETRY_SUFFIX).append(retryNum);
        }            
        checkPoint.append(isOk ? PROFILER_CHECKPOINT_NAME_OK_RESULT_SUFFIX : PROFILER_CHECKPOINT_NAME_FAILED_RESULT_SUFFIX);            
        try {
            perf = CommonOamManager.profilerAgent.enterCheckpoint(checkPoint.toString());
        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }
    }

           
    /**
     * Main thread loop.
     */
    public void run() {
        while (true) {
            try {
                doWork();
            } catch (Exception e) {
                logger.error("MessageDepositEventWorker Exception: " + e.getMessage(), e);
            } catch (Throwable t) {
                logger.error("MessageDepositEventWorker Throwable: ", t);
            }
        }
    }

}
