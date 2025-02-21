/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms;


import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierProfiler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierUtil;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.NotifierUtilException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.NtfThread;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.CPHRNotifTYPE;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.NotifierTypeEvent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.NotifierTypeState;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.database.NotifierDatabaseHelper;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule.NotifierEvent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule.NotifierEventHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.send.sms.NotifierSenderSms;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierMdr;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierMdr.NotifierMdrAction;

/**
 * The NotifierWorker threads process the NotifierEvents on the NotifierWorkers' queue.
 * NotifierEvents can be queued, for example:<BR>
 * - after receiving an incoming signal from NTF<BR>
 * - after the result of a sending attempt is received<BR>
 * - scheduler sending retry/expiration event is fired<BR>
 * <p>
 * Hence, the NotifierWorker threads handle all the different stages that can be involved in sending notifications.
 * The handling is implemented using a state machine.
 * For this sample plug-in, the possible states in the state machine is defined in {@link NotifierTypeState} and
 * the possible events in the state machine is defined in {@link NotifierTypeEvent}.
 */
public class NotifierWorker extends NtfThread {

    private static INotifierLogger log = TemplateSmsPlugin.getLoggerFactory().getLogger(NotifierWorker.class);
    private static INotifierProfiler notifierProfiler = TemplateSmsPlugin.getProfiler();
    private static INotifierUtil notifierUtil = TemplateSmsPlugin.getUtil();
    private BlockingQueue<NotifierEvent> notifierWorkerQueue;

    /**
     * Constructor
     * @param workerqueue ManagedArrayBlockingQueue Working queue where work items are found
     * @param threadName String Thread name
     */
    public NotifierWorker(BlockingQueue<NotifierEvent> workerqueue, String threadName) {
        super(TemplateSmsPlugin.getLoggerFactory(),TemplateSmsPlugin.getnotifierServicesManager(), threadName);
        this.notifierWorkerQueue = workerqueue;
    }

    /**
     * Process one work item from queue according to the state machine in this method.
     * @return False if the work should continue, true if the worker wants to stop.
     */
    protected boolean ntfRun() {
        NotifierEvent notifierEvent = null;

        // Get an event from the working queue
        try {
          if (managmentInfo.isAdministrativeStateShutdown()) {
        	  notifierEvent = notifierWorkerQueue.poll();
          } else {
			notifierEvent = notifierWorkerQueue.poll(3,TimeUnit.SECONDS);
          }
		} catch (InterruptedException e1) {
			log.info("Thread interrupted, exiting on request from ManagementInfo: " + getName());
			return false;
		}
        if (notifierEvent == null) { return false; } //check ntf state.

        synchronized (notifierEvent) {
            try {
                String state = notifierEvent.getNotifierTypeState().getName();
                String event = notifierEvent.getNotifierTypeEvent().getName();
                String identity = notifierEvent.getIdentity(); 

                log.debug("Notifier event for " + identity + ", state: " + state + ", event: " + event + ", notifierType: " + notifierEvent.getNotifierType() + " (" + this.getName() + ")");

                /** Initial state */
                if (NotifierTypeState.STATE_INITIAL.equals(notifierEvent.getNotifierTypeState())) {

                    if (NotifierTypeEvent.EVENT_NEW_NOTIF.equals(notifierEvent.getNotifierTypeEvent())) {
                        // notifierEvent.getNotifierType() should not be null here since a new notification is injected 
                        // only if a non-null NotifierType was found (and then was used to instantiate the notifierEvent).
                        processInitial(notifierEvent);

                    } else if (NotifierTypeEvent.EVENT_SCHEDULER_RETRY.equals(notifierEvent.getNotifierTypeEvent())) {
                        if(notifierEvent.getNotifierType() != null) {
                            processInitial(notifierEvent);
                        } else {
                        	cleanupEventFiles(notifierEvent);                       
                        }

                    } else if (NotifierTypeEvent.EVENT_SCHEDULER_EXPIRY.equals(notifierEvent.getNotifierTypeEvent())) {
                        // Regardless if notifierEvent.getNotifierType() is null or not, do the same.
                        // MDR will not be generated if notifierEvent.getNotifierType() is null.
                    	cleanupEventFiles(notifierEvent);                       
                        
                    } else {
                        logInvalidEventReceived(notifierEvent);
                    }
                /** Sending state */
                } else if (NotifierTypeState.STATE_SENDING.equals(notifierEvent.getNotifierTypeState())) {

                    if (NotifierTypeEvent.EVENT_SCHEDULER_RETRY.equals(notifierEvent.getNotifierTypeEvent())) {

                        if (notifierProfiler.isProfilerEnabled()) {
                            NotifierHelper.profilerCheckPoint("NTF.TNP.6.NW.3.Send.SchRet." + notifierEvent.getNotifierTypeName());
                        }
                    	if(notifierEvent.getNotifierType() != null) {
                                processSending(notifierEvent);
                        } else {
                            cleanupEventFiles(notifierEvent);                             
                        }
                    } else if (NotifierTypeEvent.EVENT_SCHEDULER_EXPIRY.equals(notifierEvent.getNotifierTypeEvent())) {

                        if (notifierProfiler.isProfilerEnabled()) {
                            NotifierHelper.profilerCheckPoint("NTF.TNP.6.NW.3.Send.SchExp." + notifierEvent.getNotifierTypeName());
                        }
                    	
                    	// Regardless if notifierEvent.getNotifierType() is null or not, do the same.
                        // MDR will not be generated if notifierEvent.getNotifierType() is null.
                        if (cleanupEventFiles(notifierEvent)) {
                            // Generate MDR
                            NotifierMdr.get().generateMdr(notifierEvent, NotifierMdrAction.EXPIRED);
                        }

                    } else if (NotifierTypeEvent.EVENT_CLIENT_RETRY.equals(notifierEvent.getNotifierTypeEvent())) {
                    	// Since this is the SMSC response and information in the NotifierType is needed to send to SMSC in the first place,
                    	// notifierEvent.getNotifierType() should not be null here 
                    	log.debug("EVENT_CLIENT_RETRY received - scheduler will kick-in and retry later");
                    	// And commit to persistent file
                    	Object perf = null;
                    	try {
                    		if (notifierProfiler.isProfilerEnabled()) {
                    			perf = notifierProfiler.enterProfilerPoint("NTF.TNP.Trace.2.4.NW.Resp.ClientRetry." + notifierEvent.getNotifierTypeName());
                    		}                           
                    		notifierEvent.updateEventIdsPersistent();
                    	} finally {
                    		if (perf != null) {
                    			notifierProfiler.exitProfilerPoint(perf);
                    		}
                    	}

                    } else if (NotifierTypeEvent.EVENT_CLIENT_FAILED.equals(notifierEvent.getNotifierTypeEvent())) {
                        // Since this is the SMSC response and information in the NotifierType is needed to send to SMSC in the first place,
                        // notifierEvent.getNotifierType() should not be null here.
                    	
                        Object perf = null;
                        try {
                            if (notifierProfiler.isProfilerEnabled()) {
                                perf = notifierProfiler.enterProfilerPoint("NTF.TNP.Trace.2.4.NW.Resp.ClientFailed." + notifierEvent.getNotifierTypeName());
                            }

                            if (cleanupEventFiles(notifierEvent)) {
                            // Generate MDR
                            	NotifierMdr.get().generateMdr(notifierEvent, NotifierMdrAction.FAILED);
                            }
                        } finally {
                            if (perf != null) {
                                notifierProfiler.exitProfilerPoint(perf);
                            }
                        }
                            
                        
                    }  else if (NotifierTypeEvent.EVENT_SMS_INFO_RESPONSE_SUCCESSFUL.equals(notifierEvent.getNotifierTypeEvent())) {
                        // Successful notification
                        // Since this is the SMSC response and information in the NotifierType is needed to send to SMSC in the first place,
                        // notifierEvent.getNotifierType() should not be null here.

                        Object perf = null;
                        try {
                            if (notifierProfiler.isProfilerEnabled()) {
                                perf = notifierProfiler.enterProfilerPoint("NTF.TNP.Trace.2.4.NW.Resp.Success." + notifierEvent.getNotifierTypeName());
                            }

                            if (cleanupEventFiles(notifierEvent)) {
                            	// Generate MDR
                            	NotifierMdr.get().generateMdr(notifierEvent, NotifierMdrAction.DELIVERED);
                            }
                            
                        } finally {
                            if (perf != null) {
                                notifierProfiler.exitProfilerPoint(perf);
                            }
                        }
                            
                        
                    } else {
                        if (notifierProfiler.isProfilerEnabled()) {
                            NotifierHelper.profilerCheckPoint("NTF.TNP.6.NW.3.Send.Invalid." + notifierEvent.getNotifierTypeName());
                        }
                     	
                        logInvalidEventReceived(notifierEvent);
                    }
                   
                } else {
                	 log.error("Invalid state " + notifierEvent.getNotifierTypeState().getName() + " for " + identity);
                }

            } catch (Throwable e) {
                log.error("Exception in NotifierWorker for " + notifierEvent.getIdentity(), e);

                // Delete payload
                	notifierEvent.removePayloadFile();
                    NotifierEventHandler.get().cancelAllEvents(notifierEvent, true);
               
                
                // Generate MDR
                NotifierMdr.get().generateMdr(notifierEvent, NotifierMdrAction.FAILED);
                
            }
        }
        return false;
    }

    private void logInvalidEventReceived(NotifierEvent notifierEvent) {
        log.error("Invalid Event " + notifierEvent.getNotifierTypeEvent().getName() +
                " received in state " + notifierEvent.getNotifierTypeState().getName() +
                " for " + notifierEvent.getIdentity());
    }
    
    
    private void processInitial(NotifierEvent notifierEvent) {

        Object perf = null;
        try {
            if (notifierProfiler.isProfilerEnabled()) {
                perf = notifierProfiler.enterProfilerPoint("NTF.TNP.Trace.2.2.NW.Initial");
            }
    	
            processSending(notifierEvent);
            
        } finally {
            if (perf != null) {
                notifierProfiler.exitProfilerPoint(perf);
            }
        }
            
    }

     
    private void processSending(NotifierEvent notifierEvent) {
    	
    	if (checkAndLockBeforeSending(notifierEvent) == false)
    		return;
    	
        log.debug("Sending notifierEvent " + notifierEvent.getNotifierTypeName() + " for " + notifierEvent.getIdentity());
       
        boolean successfullyScheduled = NotifierEventHandler.get().scheduleSending(notifierEvent);
        if (!successfullyScheduled) {
            log.debug("ScheduledSending not successfully scheduled for " + notifierEvent.getIdentity() + ", will retry");
            notifierEvent.clearSending(); //if we failed make sure to release the sending..
            return;
        }
              
        log.debug("Notifier processSending using NotifierSenderSms for " + notifierEvent.getIdentity());
        NotifierSenderSms.get().send(notifierEvent);
    }


    private boolean checkAndLockBeforeSending(NotifierEvent notifierEvent) {    	

    	Properties eventProps = notifierEvent.getEventProperties();
    	CPHRNotifTYPE notifType = notifierEvent.getCphrNotifType();
    	
    	//don't check if enabled in COS/profile for non-subscribers..
    	if (notifType != CPHRNotifTYPE.NONSUBSCRIBER) {
    		ANotifierDatabaseSubscriberProfile subscriberProfile = notifierEvent.getSubscriberProfile();
    		String cphrTemplate = notifierEvent.getCphrtemplateName();
    		if (!NotifierDatabaseHelper.isTempalateEnabledForSub(subscriberProfile,cphrTemplate)) {
    			log.debug("Subscriber " +  notifierEvent.getReceiverNumber() + " has template " + cphrTemplate + " disabled ");
    			NotifierMdr.get().generateMdr(notifierEvent, NotifierMdrAction.DISCARDED); 
				cleanupEventFiles(notifierEvent); 
    			return false;
    		}
    	}
    	
    	  	
    	String SendIfUnread = eventProps.getProperty(TemplateSMSProperties.SendOnlyIfUnread);
    	String SendIfRetry = eventProps.getProperty(TemplateSMSProperties.sendMultipleIfRetry); 
    	

    	if ( notifType == CPHRNotifTYPE.MAILBOXSUBSCRIBER && ( SendIfUnread == null || Boolean.parseBoolean(SendIfUnread) == true) ) {
    		log.debug(TemplateSMSProperties.SendOnlyIfUnread + " is true or not set, checking mailbox...");
    		try {
				if (!TemplateSmsPlugin.getUtil().isNewMessagesInInbox(NotifierDatabaseHelper.getSubscriberMsidIdentity(notifierEvent.getSubscriberProfile()))) {
					NotifierMdr.get().generateMdr(notifierEvent, NotifierMdrAction.DISCARDED); 
					cleanupEventFiles(notifierEvent); 
					log.debug("[" + notifierEvent.getReceiverNumber() +"] No unread messages, discarding. ");
					return false;
				} else
				{
					log.debug ("[" + notifierEvent.getReceiverNumber() +"] has unread messages, continuing with notification.");
				}
			} catch (NotifierUtilException e) {
				log.warn("Unable to get MSID for subscriber [" + notifierEvent.getReceiverNumber() +"] due to exception. ",e);
				NotifierMdr.get().generateMdr(notifierEvent, NotifierMdrAction.FAILED);
				cleanupEventFiles(notifierEvent);
			    return false;
			}
    	} else {
    		if (notifType != CPHRNotifTYPE.MAILBOXSUBSCRIBER) { 
        		log.debug("Skipping mailbox check for notificationType: [" + notifType + "] as not a mailbox type.");
    		} else
    		{
    			log.debug(TemplateSMSProperties.SendOnlyIfUnread + " is false, skipping mailbox check.");
    		}
    	}
    	


    	//only check initial (new) events for sending flag or retried new events, others have already passed and should send according to there retry
    	//schema.
    	
    	if  (NotifierTypeState.STATE_INITIAL.equals(notifierEvent.getNotifierTypeState())) {   		
    		if ( SendIfRetry != null &&  Boolean.parseBoolean(SendIfRetry) == true ) {    		
    			log.debug(TemplateSMSProperties.sendMultipleIfRetry + " is true, will send if other notifications in progress");
    		} else
    		{
    			log.debug(TemplateSMSProperties.sendMultipleIfRetry  + " is false or not set , checking if already sending.");
    			try {
    				if (!notifierEvent.aquireSendingLock()) {
    					if (!notifierUtil .isFileStorageOperationsAvailable(notifierEvent.getNotificationNumber())) {
    		   				log.warn("ntfMain: File system is read only (geo fail over?) for notifNumber:" + notifierEvent.getNotificationNumber() + " when trying to acquire sending lock, will retry.");
    		   				return false;
    		   			}
    					log.debug("unable to aquire sending lock, will retry."); 
    					return false;
    				}
    				if (notifierEvent.isAlreadySending()) {
    					NotifierMdr.get().generateMdr(notifierEvent, NotifierMdrAction.DISCARDED); 
    					cleanupEventFiles(notifierEvent); 
    					log.debug("[" + notifierEvent.getReceiverNumber() +"] Already sending, discarded.");
    					return false;
    				} else
    				{
    					notifierEvent.indicateSending(); //indicate we are taking over the sending of this message.
    					return true;
    				}
    			} finally {
    				notifierEvent.releaseSendingLock(); //make sure to release the lock on the sending file.
    			}
    		}
    	}

		return true;
	}

	private boolean cleanupEventFiles(NotifierEvent notifierEvent) {
    	
        Object perf = null;
        try {
            if (notifierProfiler.isProfilerEnabled()) {
                perf = notifierProfiler.enterProfilerPoint("NTF.TNP.6.NW.1.cleanupEventFiles");
            }
    	
            //note not currently used but may add back in later version.
            if (notifierEvent.containsPayloadFile()) {
            	// Delete payload file
            	if (notifierEvent.removePayloadFile()) {
            		NotifierEventHandler.get().cancelAllEvents(notifierEvent, true);
            		return true;
            	} else {
            		// Cannot delete payload file - return false (retry will kick in later)
            		return false;
            	}
            } else {
                if (!NotifierEventHandler.get().cancelAllEvents(notifierEvent, true)) {
                	return false;
                }
                if (!notifierEvent.clearSending()) {
                	return false;
                }
                return true;
            }
        } finally {
            if (perf != null) {
                notifierProfiler.exitProfilerPoint(perf);
            }
        }     
    }
    
    @Override
	protected boolean shutdown() {
		
		if (isInterrupted()) {
			return true;
		} //exit immediately if interrupted..
		if (notifierWorkerQueue.isEmpty()) {
			//wait a short while see if queue has new items, if not exit.
			try { Thread.sleep(10); } catch (InterruptedException i) {return true;}
			if (notifierWorkerQueue.isEmpty()) {
				return true; //exit now.
			}
			return ntfRun(); //process the remaining items before shutdown.
		} else
		{
			return true;
		}
	}
}
