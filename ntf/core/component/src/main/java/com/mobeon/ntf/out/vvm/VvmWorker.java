/**
 * Copyright (c) 2010 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.vvm;

import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.util.threads.NtfThread;

/**
 * Does the synchronisation of a Vvm notification.
 * The workers get their workloads via an ManagedArrayBlockingQueue.
 *
 * VvmWorkers are used because of Level-3 scheduling.
 * Since NTF could be under heavy load after rebooting (because of MRD/Scheduler sending retries),
 * worker threads are used to handle the traffic.
 */
public class VvmWorker extends NtfThread {

    private static LogAgent log = NtfCmnLogger.getLogAgent(VvmWorker.class);
    private ManagedArrayBlockingQueue<Object> queue;
    private VvmEventHandler vvmEventHandler;
    private VvmHandler vvmHandler;
    private MerAgent mer;

    /**
     * Constructor
     * @param queue Working queue where work items are found
     * @param threadName The thread name
     * @param vvmHandler The VVM handler
     */
    public VvmWorker(ManagedArrayBlockingQueue<Object> queue, String threadName, VvmHandler vvmHandler)
    {
        super(threadName);
        this.queue = queue;
        this.vvmHandler = vvmHandler;
        this.vvmEventHandler = (VvmEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.VVM_L3.getName());
        this.mer = MerAgent.get();
    }

    /**
     * Process one work item from queue.
     * @return False if the work should continue, true if the worker wants to stop.
     */
    public boolean ntfRun()
    {
        VvmEvent vvmEvent = null;

        // Get an event from the working queue
        Object obj = queue.poll(5, TimeUnit.SECONDS);
        if (obj == null) return false;
        if (!(obj instanceof VvmEvent)) {
            log.error("VVM Worker: Invalid object received: " + obj.getClass().getName());
            return false;
        }
        vvmEvent = (VvmEvent)obj;
        

        synchronized (vvmEvent) {
            
            int state = vvmEvent.getCurrentState();
            int event = vvmEvent.getCurrentEvent();
            
            try {
                if(log.isDebugEnabled()) {
                    log.debug("VVM for " + vvmEvent.getSubscriberNumber() + ", State = " + VvmEvent.STATE_STRING[state] +
                        ", Event: " + VvmEvent.VVM_EVENTS_STRING[event]);
                }

                
                /** Vvm sending state **/
                if(state == VvmEvent.STATE_SENDING_INFO) {
                    
                    if (event == VvmEvent.VVM_EVENT_SENDING) {
                        // Send the VVM notification
                        vvmHandler.handleSmsUnitNoScheduling(vvmEvent);
                    } else if (event == VvmEvent.VVM_EVENT_SMS_UNIT_RETRY) {
                        // Do not perform anything, scheduler will kick-in and retry later
                    } else if (event == VvmEvent.VVM_EVENT_SMS_UNIT_FAILED) {
                        // Receiving a failed response from the SMSc client (no retry)
                        
                        // Cancel all potential scheduled timer
                        vvmEventHandler.cancelAllEvents(vvmEvent, true);
    
                        // Generate MDR
                        mer.vvmFailed(vvmEvent.getSubscriberNumber(), vvmEvent.getCallerNumber());
                    } else if (event == VvmEvent.VVM_EVENT_SMS_UNIT_SUCCESSFUL) {
                        // Receiving a successful response from the SMSc client
                        vvmHandler.handleSendingPhoneOn(vvmEvent);
    
                        // Generate MDR
                        mer.vvmDelivered(vvmEvent.getSubscriberNumber(), vvmEvent.getCallerNumber());
                    } else if (event == VvmEvent.VVM_EVENT_SCHEDULER_RETRY) {
                        // Re-send the VVM notification
                        vvmHandler.handleSmsUnit(vvmEvent);
                    } else if (event == VvmEvent.VVM_EVENT_SCHEDULER_EXPIRY) {
                        // Cancel all potential scheduled timer
                        vvmEventHandler.cancelAllEvents(vvmEvent, true); //TODO: don't cancel pending sim swap feature if there is notification aggregation
                        
                        // Generate MDR
                        mer.vvmTimeout(vvmEvent.getSubscriberNumber(), vvmEvent.getCallerNumber());
                    } else {
                        log.error("Invalid Event " + event + " received in this state " + state);
                    }
                    
                /** Vvm Sending PhoneOn state (for Sim Swap) **/
                } else if(state == VvmEvent.STATE_SENDING_PHONE_ON) {
                    
                    if(event == VvmEvent.VVM_EVENT_SCHEDULER_RETRY) {
                        // Re-send the PhoneOn request (either SMS-Type-0 or HLR_SRI)
                        vvmHandler.handleSendingPhoneOnNoScheduling(vvmEvent);
                        
                    } else if(event == VvmEvent.VVM_EVENT_SCHEDULER_EXPIRY) {
                        // Cancel all potential scheduled timer
                        vvmEventHandler.cancelAllEvents(vvmEvent, true); //TODO: don't cancel pending VVM notif if there is notification aggregation
                        
                    } else if(event == VvmEvent.VVM_EVENT_PHONE_ON_OK) {
                        // Receiving a PhoneOn response faster than having time to update the vvm status file, process it.
                        if(log.isDebugEnabled()) {
                            log.debug("Receiving a PhoneOn response faster than having time to update the " + 
                                    vvmEvent.getNotificationType().getStatusFileName() + " file for " + vvmEvent.getSubscriberNumber());
                        }

                        /**
                         * PhoneOn 'ON' received, go forward with Deactivator scheduling 
                         * If the SmsInfo is scheduled, stored and sent successfully, generate the PhoneOn MDR event.
                         * Otherwise, let the SmsType0 event retry.
                         */
                        if (vvmHandler.handleDeactivator(vvmEvent)) {
                            // Generate MDR
                            //mer.phoneOnDelivered(vvmEvent.getSubscriberNumber()); //TODO: MDR
                        }
                        
                    } else if (event == VvmEvent.VVM_EVENT_PHONE_ON_RETRY) {
                        // Receiving a PhoneOn response faster than having time to update the vvm status file
                        // Do not perform anything, scheduler will kick-in and retry later

                    } else if (event == VvmEvent.VVM_EVENT_PHONE_ON_FAILED) {
                        // Receiving a PhoneOn response faster than having time to update the vvm status file
                        // Cancel all potential scheduled timer
                        vvmEventHandler.cancelAllEvents(vvmEvent, true);
    
                        //TODO: Generate MDR for PhoneOn?
                        
                    } else if (event == VvmEvent.VVM_EVENT_PHONE_ON_CLIENT_RETRY) {
                        // Do not perform anything, scheduler will kick-in and retry later
                        
                    } else if (event == VvmEvent.VVM_EVENT_PHONE_ON_CLIENT_FAILED) {
                        // Cancel all potential scheduled timer
                        vvmEventHandler.cancelAllEvents(vvmEvent, true);
                        
                    } else if (event == VvmEvent.VVM_EVENT_PHONE_ON_SENT_SUCCESSFULLY) {
                        
                        /**
                         * Start Validity Timer on SMS-Type-0 (24 hour timer)
                         * If the persistent storage update fails, no SmsType0 timer will be started, the SendingPhoneOn will retry.
                         */
                        if(!vvmHandler.handleWaitingPhoneOn(vvmEvent)) {
                            log.warn("Unable to schedule and store SmsType0 for " + vvmEvent.getSubscriberNumber() + ", will retry");
                        }
                        
                    } else {
                        log.error("Invalid Event " + event + " received in this state " + state);
                    }
                    
                /** Vvm Waiting PhoneOn state (for Sim Swap) **/
                } else if(state == VvmEvent.STATE_WAITING_PHONE_ON) {
                    
                    if (event == VvmEvent.VVM_EVENT_SCHEDULER_RETRY) {
                        // Re-send the SMS-TYPE-0
                        vvmHandler.handleSendingPhoneOn(vvmEvent);
                        
                    } else if (event == VvmEvent.VVM_EVENT_SCHEDULER_EXPIRY ||
                               event == VvmEvent.VVM_EVENT_PHONE_ON_FAILED) {
                        // Cancel all potential scheduled timer
                        vvmEventHandler.cancelAllEvents(vvmEvent, true);
                        
                    } else if (event == VvmEvent.VVM_EVENT_PHONE_ON_OK) {
                        // PhoneOn 'ON' received, go forward with Deactivator scheduling 
                        vvmHandler.handleDeactivator(vvmEvent);
                        
                    } else if (event == VvmEvent.VVM_EVENT_PHONE_ON_RETRY) {
                        // Do not perform anything, scheduler will kick-in and retry later
                    } else if (event == VvmEvent.VVM_EVENT_PHONE_ON_CLIENT_RETRY) {
                        // Do not perform anything, the client (sms-client or SS7 was faster to answer than NTF to update the IO)
                        log.debug("Discard this " + event + " event since already in " + state + "state.");
                    } else if (event == VvmEvent.VVM_EVENT_PHONE_ON_CLIENT_FAILED) {
                        // Client FAILED while pending PHONE-ON, cancel all events
                        vvmEventHandler.cancelAllEvents(vvmEvent, true);
                        
                    } else if (event == VvmEvent.VVM_EVENT_PHONE_ON_SENT_SUCCESSFULLY) {
                        // Cancel SmsUnit timer (if present) - Plausible in case of a PhoneOn sent successfully while a Waiting-PhoneON is already scheduled
                        vvmEventHandler.cancelSendingUnitPhoneOnEvent(vvmEvent, true);
                        
                    } else {
                        log.error("Invalid Event " + event + " received in state " + state);
                    }
                    
                /** Vvm Deactivator state (for Sim Swap) **/
                } else if(state == VvmEvent.STATE_DEACTIVATOR) {
                    
                    if (event == VvmEvent.VVM_EVENT_SCHEDULER_RETRY) {
                        /**
                         * Vvm SimSwap Deactivator timer expired
                         */
                        vvmHandler.handleDeactivateVvm(vvmEvent);
                        
                    } else if(event == VvmEvent.VVM_EVENT_SCHEDULER_EXPIRY) {
                        // Cancel all potential scheduled timer
                        vvmEventHandler.cancelAllEvents(vvmEvent, true); //TODO: don't cancel pending VVM notif if there is notification aggregation
                        
                    } else {
                        
                        log.error("Invalid Event " + event + " received in this state " + state);
                    }
                    
                /** Vvm System Activated state (for SimSwap) **/
                } else if(state == VvmEvent.STATE_ACTIVATOR) {
                    
                    if (event == VvmEvent.VVM_EVENT_ACTIVITY_DETECTED || event == VvmEvent.VVM_EVENT_SCHEDULER_RETRY) {
                        // Set VvmSystemActivated to yes
                        vvmHandler.handleActivityDetected(vvmEvent);
                        
                    } else if(event == VvmEvent.VVM_EVENT_SCHEDULER_EXPIRY) {
                        // Cancel this event. Only cancel Vvm Activity Detected event.
                        vvmEventHandler.cancelEvent(vvmEvent.getSchedulerIds().getActivatorEventId());
                        
                    } else {
                        
                        log.error("Invalid Event " + event + " received in this state " + state);
                    }
                    
                } else {
                    log.error("Invalid state in Vvm worker " + state);
                    vvmHandler.releaseLockFile(vvmEvent);
                }

            }  catch (OutOfMemoryError me) {
                try {
                    ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                    log.error("NTF out of memory, shutting down... ", me);
                } catch (OutOfMemoryError me2) {;} //ignore second exception
                return true; //exit.
            } catch (Exception e) {
                log.error("Exception in VVM worker for " + vvmEvent.getSubscriberNumber(), e);

                if (state == VvmEvent.STATE_ACTIVATOR) { 
                    //Event in STATE_ACTIVATOR are generated from Vvm Activity Detected level-2 event, they MUST not impact vvm status file
                    vvmEventHandler.cancelEvent(vvmEvent.getSchedulerIds().getActivatorEventId());
                    
                } else {
                    //Release VVM lock file
                    vvmHandler.releaseLockFile(vvmEvent);
                    
                    // Cancel timer
                    vvmEventHandler.cancelAllEvents(vvmEvent, true);
                    
                    // Generate MDR
                    if(state == VvmEvent.STATE_SENDING_INFO) {
                        mer.vvmFailed(vvmEvent.getSubscriberNumber(), vvmEvent.getCallerNumber());
                    }
                }
            }
        }
        return false;
    }
    
    public boolean shutdown() {
        if (isInterrupted()) {
            return true;
        } //exit immediately if interrupted..

        if (queue.size() == 0)
        {
            //give a short time for new items to be queued in workers, to allow other threads to empty there queues.
            if (queue.isIdle(2,TimeUnit.SECONDS)) {
                return true;
            }
            else
            {
                if (queue.waitNotEmpty(2, TimeUnit.SECONDS)) {
                    return(ntfRun());
                } else
                {
                    return true;
                }

            }
        } else {
            return(ntfRun());
        }
    }
}
