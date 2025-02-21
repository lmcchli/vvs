/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.slamdown;

import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationHandler;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.event.PhoneOnEvent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.util.threads.NtfThread;

/**
 * Does the synchronisation of a Slamdown notification.
 * The workers get their workloads via an ManagedArrayBlockingQueue.
 *
 * SlamdownWorkers are used because of Level-3 scheduling.
 * Since NTF could be under heavy load after rebooting
 * (because of MRD/Scheduler sending retries), worker threads
 * are used to handle the traffic.
 */
public class SlamdownWorker extends NtfThread {

    private ManagedArrayBlockingQueue<Object> queue;
    private static LogAgent log =  NtfCmnLogger.getLogAgent(SlamdownWorker.class);
    private SlamdownEventHandler slamdownEventHandler;
    private SlamdownListHandler slamdownListHandler;
    private MerAgent mer;
    private MfsEventManager mfsEventManager;
    private Object ProcessListCounter;
    static int reportCount = 0; // used to report pool size periodically, by one thread only.

    /**
     * Private types for MER notification
     */
    private static final int MER_DELIVERED = 0;
    private static final int MER_EXPIRED = 1;
    private static final int MER_FAILED = 2;

    /**
     * Constructor
     * @param queue ManagedArrayBlockingQueue Working queue where work items are found
     * @param threadName String Thread name
     * @param slamdownListHandler SlamdownListHandler instance
     */
    public SlamdownWorker(ManagedArrayBlockingQueue<Object> queue, String threadName, SlamdownListHandler slamdownListHandler)
    {
        super(threadName);
        this.queue = queue;
        this.slamdownListHandler = slamdownListHandler;
        this.slamdownEventHandler = (SlamdownEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SLAMDOWN_L3.getName());
        this.mer = MerAgent.get();
        this.mfsEventManager = MfsEventFactory.getMfsEvenManager();
    }

    /**
     * The shutdown loop stops after the ntfRun method is finished.
     *
     * @return true always (i.e. this thread has not shutdown activity)
     */
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

    /**
     * Process one work item from queue.
     * @return False if the work should continue, true if the worker wants to stop.
     */
    public boolean ntfRun()
    {
        boolean status = false;

        try {
            // Get an event from the working queue
            Object obj = queue.poll(3, TimeUnit.SECONDS); //timeout in order to check for management status changes, locked, shutdown.
        
            if (reportCount++ >= 100) {
                reportCount = 0;
                log.info("queueSize = " + queue.size());
            }
            
            if (obj == null) {
                //go back to check management state.
                return false;
            }
            
            if (obj instanceof SlamdownList ) {
                status = processList((SlamdownList)obj); 
            } else if (obj instanceof PhoneOnEvent) {
                status = processPhoneOn((PhoneOnEvent)obj);
            } else {             
                log.error("Invalid object received: " + obj.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Unexpected Exception recieved: ",e);
        }
        
        return status;
    }

    @SuppressWarnings("null")
    private boolean processPhoneOn(PhoneOnEvent phoneOnEvent) {
        long lockId = 0L;
        try {

            log.debug("Slamdown/Mcn PhoneOn event received: " + phoneOnEvent);

            // Get slamdownList
            SlamdownList[] slamdownList = SlamdownList.recreateSlamdownList(phoneOnEvent.getAddress());

            if (slamdownList != null && slamdownList.length > 0) {

                // Acquire lock file if it's a PHONE_ON response
                if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_OK || phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_BUSY) {
                    boolean internal = mfsEventManager.isInternal(phoneOnEvent.getAddress());
                    try {
                        lockId = mfsEventManager.acquireLockFile(phoneOnEvent.getAddress(), SlamdownList.SLAMDOWNMCN_PHONE_ON_LOCK_FILE, Config.getSlamdownMcnPhoneOnLockFileValidityInSeconds(), internal);                        
                        if (lockId == 0L) {
                            log.debug("Slamdown/Mcn PhoneOn lock file not acquired, another NTF instance is processing this PhoneOn event");
                            return false;
                        }
                    } catch (Exception e) {
                        log.error("Slamdown/Mcn PhoneOn recieved unexpected exception:",e);
                        return false;
                    }
                }
                
                for (SlamdownList sl : slamdownList) {
                    sl.setPhoneOnLockId(lockId);
                    sl.retrieveSchedulerEventIdsPersistent();
                    setSlamdownListState(sl, phoneOnEvent);

                    if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_OK || phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_BUSY) {
                        log.debug("PhoneOn received successfully for notification number " + phoneOnEvent.getAddress());
                        log.debug("PhoneOn received successfully: " + phoneOnEvent);

                        // Successful PhoneOn response from SMSc
                        sl.setCurrentEvent(SlamdownList.EVENT_PHONE_ON_OK);
                    } else if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_FAILED_TEMPORARY) {
                        log.debug("PhoneOn Response: failed temporary: " + phoneOnEvent);

                        // PhoneOn response: failed temporary
                        sl.setCurrentEvent(SlamdownList.EVENT_PHONE_ON_RETRY);
                    } else if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_FAILED) {
                        log.debug("PhoneOn Response: failed: " + phoneOnEvent);

                        // PhoneOn response: failed
                        sl.setCurrentEvent(SlamdownList.EVENT_PHONE_ON_FAILED);
                    } else if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_CLIENT_SENT_SUCCESSFULLY) {
                        log.debug("PhoneOn CLIENT sent out successfully: " + phoneOnEvent);

                        // PhoneOn CLIENT sent out successfully
                        sl.setCurrentEvent(SlamdownList.EVENT_CLIENT_PHONE_ON_SENT_SUCCESSFULLY);
                    } else if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_CLIENT_FAILED_TEMPORARY) {
                        log.debug("PhoneOn CLIENT failed temporary: " + phoneOnEvent);

                        // PhoneOn CLIENT retry
                        sl.setCurrentEvent(SlamdownList.EVENT_CLIENT_RETRY);
                    } else if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_CLIENT_FAILED) {
                        log.debug("PhoneOn CLIENT failed: " + phoneOnEvent);

                        // PhoneOn CLIENT failed
                        sl.setCurrentEvent(SlamdownList.EVENT_CLIENT_FAILED);
                    } else if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_SS7_ERROR) {
                        log.debug("PhoneOn CLIENT failed: " + phoneOnEvent);

                        // PhoneOn SS7error, retry
                        sl.setCurrentEvent(SlamdownList.EVENT_CLIENT_RETRY);
                    } else {
                        log.debug("PhoneOn failed: " + phoneOnEvent);

                        // PhoneOn error, retry
                        sl.setCurrentEvent(SlamdownList.EVENT_PHONE_ON_FAILED);
                    }

                    //process the list.
                    if (processList(sl) == true) {
                        return true; //exit thread.
                    }
                }
            } else {
                log.debug("Event " + phoneOnEvent + " received but not found in storage, discard.");
            }
        } catch (Exception e) {
            log.error("SlamdownPhoneOnReceiver Exception: ", e);

            try {
                if (phoneOnEvent!=null && lockId !=0) {
                    boolean internal = mfsEventManager.isInternal(phoneOnEvent.getAddress());;
                    mfsEventManager.releaseLockFile(phoneOnEvent.getAddress(), SlamdownList.SLAMDOWNMCN_PHONE_ON_LOCK_FILE, lockId, internal);
                }
            } catch (Exception ie) { 
                log.warn("Unable to release lock file for " + phoneOnEvent.getAddress());
            }

        } catch (Throwable t) {
            log.error("SlamdownPhoneOnReceiver Throwable: ", t);

            try {
                if (phoneOnEvent!=null && lockId !=0) {
                    boolean internal = mfsEventManager.isInternal(phoneOnEvent.getAddress());
                    mfsEventManager.releaseLockFile(phoneOnEvent.getAddress(), SlamdownList.SLAMDOWNMCN_PHONE_ON_LOCK_FILE, lockId, internal);
                }
            } catch (Exception ie) { 
                log.warn("Unable to release lock file for " + phoneOnEvent.getAddress());
            }
        }
        return false;
    }
    
    
    /**
     * Sets the state of the slamdownList recreated from IOs based on the slamdownmcn.status file (event ids).
     * This method takes the greater state (STATE_SENDING_UNIT -> STATE_SENDING_INFO) that a slamdownList can be into.
     *   
     * @param slamdownList SlamdownList
     * @param phoneOnEvent PhoneOnEvent
     */
    private void setSlamdownListState(SlamdownList slamdownList, PhoneOnEvent phoneOnEvent) {
        int currentState = -1;

        if (slamdownList.getSchedulerIds().getSmsUnitEventId() != null && slamdownList.getSchedulerIds().getSmsUnitEventId().length() > 0) {
            currentState = SlamdownList.STATE_SENDING_UNIT;
        }

        if (slamdownList.getSchedulerIds().getSmsType0EventId() != null && slamdownList.getSchedulerIds().getSmsType0EventId().length() > 0) {
            currentState = SlamdownList.STATE_WAITING_PHONE_ON;
        }

        if (slamdownList.getSchedulerIds().getSmsInfoEventId() != null && slamdownList.getSchedulerIds().getSmsInfoEventId().length() > 0) {
            currentState = SlamdownList.STATE_SENDING_INFO;
        }

        if (currentState < 0) {
            if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_SS7_ERROR ||
                phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_CLIENT_SENT_SUCCESSFULLY ||
                phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_CLIENT_FAILED ||
                phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_CLIENT_FAILED_TEMPORARY) {
                // phoneOnEvent.result is about status on the phoneOn sent, must be handled in the STATE_SENDING_UNIT state.
                currentState = SlamdownList.STATE_SENDING_UNIT;
            } else {
                // phoneOnEvent.result is about status on a phoneOn delivered, must be handled in the STATE_SENDING_UNIT state.
                currentState = SlamdownList.STATE_WAITING_PHONE_ON;
            }

            log.warn("Receiving PhoneOn response for: " + slamdownList.getSubscriberNumber() + " : " + phoneOnEvent.getAddress() +
            " while no slamdownmcn.status file found.  Will set the state to " + PhoneOnEvent.responseType[currentState]);
        }

        slamdownList.setCurrentState(currentState);
    }

    /**
     * @param slamdownList -  list to process
     * @return true if thread should exit false otherwise.
     */
    private boolean processList(SlamdownList slamdownList) {
        synchronized (slamdownList) {
            try {
                
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    ProcessListCounter = CommonOamManager.profilerAgent.enterCheckpoint("NTF.SlamDownWorker.processList");
                }
                
                String event = SlamdownList.EVENT_STRING[slamdownList.getCurrentEvent()];
                String state = SlamdownList.STATE_STRING[slamdownList.getCurrentState()];
                String subscriber = slamdownList.getSubscriberNumber() + " : " + slamdownList.getNotificationNumber();

                log.debug("Slamdown for " + subscriber + ", state: " + state + ", event: " + event + " (" + this.getName() + ")");

                /** Idle state */
                if (slamdownList.getCurrentState() == SlamdownList.STATE_SENDING_UNIT) {

                    if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_NEW_NOTIF) {
                        /**
                         * Skipping the aggregation phase
                         * Initial action is to start Aggregation timer in order to accumulate
                         * all new slamdown notifications for a configured period of time.
                         * Once that period completed, PhoneOn request should be sent out to either SMSc or HLR.
                         * This concept must be introduced in a second phase.
                         *
                         * Current behavior is to send out the SMS-Type-0.
                         *
                         * SchedulerSmsType0 already invoked in SlamdownListHandler.
                         */
                        slamdownListHandler.handleSmsType0NoScheduling(slamdownList);
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_SCHEDULER_RETRY) {
                        // Re-send the PhoneOn request (either SMS-Type-0 or AlertSc)
                        slamdownListHandler.handleSmsType0NoScheduling(slamdownList);
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_SCHEDULER_EXPIRY) {
                        // Delete Slamdown/Mcn file(s) first
                        NotificationHandler.deleteFiles(slamdownList.getNotificationNumber(), SlamdownList.PATTERN_FILE_ALL);

                        // Cancel all potential scheduled timers
                        slamdownEventHandler.cancelAllEvents(slamdownList, true);

                        // Generate MDR
                        mer.phoneOnFailed(slamdownList.getSubscriberNumber());
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_PHONE_ON_OK) {
                        // Receiving a PhoneOn response faster than having time to update the slamdownmcn.status file, process it.
                        log.debug("Receiving a PhoneOn response faster than having time to update the slamdownmcn.status file for " + subscriber);

                        /**
                         * PhoneOn 'ON' received, go forward with SMS-Info notification
                         * If the SmsInfo is scheduled, stored and sent successfully, generate the PhoneOn MDR event.
                         * Otherwise, let the SmsType0 event retry.
                         */
                        if (slamdownListHandler.handleSmsInfo(slamdownList)) {
                            // Generate MDR
                            mer.phoneOnDelivered(slamdownList.getSubscriberNumber());
                        }
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_PHONE_ON_RETRY) {
                        // Receiving a PhoneOn response faster than having time to update the slamdownmcn.status file
                        // Do not perform anything, scheduler will kick-in and retry later, even if the current state is SENDING_UNIT.
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_PHONE_ON_FAILED) {
                        // Receiving a PhoneOn response faster than having time to update the slamdownmcn.status file
                        // Sending or not Slamdown notification based on configuration
                        sendSlamdownOrNotBasedOnConfiguration(slamdownList);
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_CLIENT_RETRY) {
                        // Do not perform anything, scheduler will kick-in and retry later
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_CLIENT_FAILED) {
                        // Delete Slamdown/Mcn file(s) first
                        NotificationHandler.deleteFiles(slamdownList.getNotificationNumber(), SlamdownList.PATTERN_FILE_ALL);

                        // Cancel all potential scheduled timers
                        slamdownEventHandler.cancelAllEvents(slamdownList, true);

                        // Generate MDR
                        mer.phoneOnFailed(slamdownList.getSubscriberNumber());
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_CLIENT_PHONE_ON_SENT_SUCCESSFULLY) {
                        /**
                         * Start Validity Timer on SMS-Type-0 (24 hour timer)
                         * If the persistent storage update fails, no SmsType0 timer will be started, the SmsUnit will retry.
                         */
                        if (!slamdownListHandler.handleSmsType0ValidityTimer(slamdownList)) {
                            log.warn("Unable to schedule and store SmsType0 for " + slamdownList.getSubscriberNumber() + ", will retry");
                        }
                    } else {
                        log.error("Invalid Event " + slamdownList.getCurrentEvent() + " received in state " + slamdownList.getCurrentState());
                    }

                    /*
            /** Aggregating state Must be introduced in a second phase
            } else if (slamdownList.getCurrentState() == SlamdownList.STATE_AGGREGATING) {
                if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_NEW_NOTIF) {
                    // Receiving a new notification while aggregating -> aggregate
                    slamdownListHandler.handleAggregate(slamdownList);
                } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_RETRY_SCHEDULER) {
                    // Aggregation period is over, start processing the notification
                    slamdownListHandler.handleSmsType0(slamdownList);
                } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_EXPIRY) {
                    // possible?
                } else {
                    log.error("Invalid Event " + slamdownList.getCurrentEvent() + " received in state " + slamdownList.getCurrentState());
                    slamdownList.getSentListener().sendStatus(slamdownList, EventSentListener.SendStatus.OK);
                }
                     */
                    /** Waiting PhoneOn state */
                } else if (slamdownList.getCurrentState() == SlamdownList.STATE_WAITING_PHONE_ON) {

                    if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_NEW_NOTIF) {
                        // Receiving a new notification while waiting for phone-on -> aggregate
                        slamdownListHandler.handleAggregate(slamdownList);
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_SCHEDULER_RETRY) {
                        // Re-send the SMS-TYPE-0
                        slamdownListHandler.handleSmsType0(slamdownList);
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_SCHEDULER_EXPIRY ||
                            slamdownList.getCurrentEvent() == SlamdownList.EVENT_PHONE_ON_FAILED) {
                        // Sending or not Slamdown notification based on configuration
                        sendSlamdownOrNotBasedOnConfiguration(slamdownList);
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_PHONE_ON_OK) {
                        // PhoneOn 'ON' received, go forward with SMS-Info notification
                        slamdownListHandler.handleSmsInfo(slamdownList);

                        // Generate MDR
                        mer.phoneOnDelivered(slamdownList.getSubscriberNumber());
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_PHONE_ON_RETRY) {
                        // Do not perform anything, scheduler will kick-in and retry later
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_CLIENT_RETRY) {
                        // Do not perform anything, the client (sms-client or SS7 was faster to answer than NTF to update the IO)
                        log.debug("Discard this " + event + " event since already in " + state + "state.");
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_CLIENT_FAILED) {
                        // Client FAILED while pending PHONE-ON, cancel all events
                        // Delete Slamdown/Mcn file(s) first
                        NotificationHandler.deleteFiles(slamdownList.getNotificationNumber(), SlamdownList.PATTERN_FILE_ALL);

                        // Cancel all potential scheduled timers
                        slamdownEventHandler.cancelAllEvents(slamdownList, true);

                        // Generate MDR
                        mer.phoneOnFailed(slamdownList.getSubscriberNumber());
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_CLIENT_PHONE_ON_SENT_SUCCESSFULLY) {
                        // Cancel SmsUnit timer (if present) - Plausible in case of a SMS-Unit sent successfully while a PhoneON is already scheduled
                        slamdownEventHandler.cancelSmsUnitEvent(slamdownList, true);
                    } else {
                        log.error("Invalid Event " + slamdownList.getCurrentEvent() + " received in state " + slamdownList.getCurrentState());
                    }

                    /** SendingInfo state */
                } else if (slamdownList.getCurrentState() == SlamdownList.STATE_SENDING_INFO) {

                    if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_NEW_NOTIF) {
                        // Receiving a new notification while aggregating -> aggregate
                        // Tricky case, refer to notes.
                        slamdownListHandler.handleAggregate(slamdownList);
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_NEW_NOTIF_WITHOUT_PHONE_ON_REQ) {
                        // Send the SMS-Info
                        slamdownListHandler.handleSmsInfoNoScheduling(slamdownList);
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_SCHEDULER_RETRY) {
                        // Re-send the SMS-Info
                        slamdownListHandler.handleSmsInfo(slamdownList);
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_SCHEDULER_EXPIRY) {
                        // Delete Slamdown/Mcn file(s) first
                        NotificationHandler.deleteFiles(slamdownList.getNotificationNumber(), SlamdownList.PATTERN_FILE_FINAL);

                        // Cancel all potential scheduled timers
                        slamdownEventHandler.cancelAllEvents(slamdownList, true);

                        // Generate MDR
                        merSlamdownMcnNotification(slamdownList, MER_EXPIRED, null);
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_CLIENT_RETRY) {
                        // Do not perform anything, scheduler will kick-in and retry later
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_CLIENT_FAILED) {
                        // Delete Slamdown/Mcn file(s) first
                        NotificationHandler.deleteFiles(slamdownList.getNotificationNumber(), SlamdownList.PATTERN_FILE_FINAL);

                        // Cancel all potential scheduled timers
                        slamdownEventHandler.cancelAllEvents(slamdownList, true);

                        // Generate MDR
                        merSlamdownMcnNotification(slamdownList, MER_FAILED, "Unable to send SMS-Type-0 message");
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_PHONE_ON_OK) {
                        // Do not perform anything since already in SENDING_INFO state. Also release lock file
                        log.debug("Discard this " + event + " event since already in " + state + "state.");
                        slamdownListHandler.releaseLockFile(slamdownList);
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_PHONE_ON_RETRY) {
                        // Do not perform anything since already in SENDING_INFO state
                        log.debug("Discard this " + event + " event since already in " + state + "state.");
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_PHONE_ON_FAILED) {
                        // Do not perform anything since already in SENDING_INFO state
                        log.debug("Discard this " + event + " event since already in " + state + "state.");
                    } else if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_SMS_INFO_RESPONSE_SUCCESSFUL) {
                        // Successful Slamdown/Mcn notification

                        // Delete Slamdown/Mcn file(s) first
                        NotificationHandler.deleteFiles(slamdownList.getNotificationNumber(), SlamdownList.PATTERN_FILE_FINAL);

                        /**
                         * Cancel all potential scheduled timers.
                         * In the rare but plausible case of receiving a new Slamdown/Mcn notification while
                         * sending SMS-Info, NTF asked Scheduler (level2) to retry, therefore, all timers can be cancelled.
                         */
                        slamdownEventHandler.cancelAllEvents(slamdownList, true);

                        // Generate MDR
                        merSlamdownMcnNotification(slamdownList, MER_DELIVERED, null);
                    } else {
                        log.error("Invalid Event " + slamdownList.getCurrentEvent() + " received in state " + slamdownList.getCurrentState());
                    }

                } else {
                    log.error("Invalid state in Slamdown worker " + slamdownList.getCurrentState());
                    slamdownListHandler.releaseLockFile(slamdownList);
                }

            } catch (OutOfMemoryError me) {
                try {
                    ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                    log.error("NTF out of memory, shutting down... ", me);
                } catch (OutOfMemoryError me2) {;} //ignore second exception
                return true; //exit.
            } catch (Exception e) {
                log.error("Exception in Slamdown/Mcn worker for " + slamdownList.getSubscriberNumber() + " : " + slamdownList.getNotificationNumber(), e);

                // Delete Slamdown/Mcn file(s) first
                NotificationHandler.deleteFiles(slamdownList.getNotificationNumber(), SlamdownList.PATTERN_FILE_ALL);

                // Release slamdownmcn lock file
                slamdownListHandler.releaseLockFile(slamdownList);

                // Cancel timer
                slamdownEventHandler.cancelAllEvents(slamdownList, true);

                // Generate MDR
                merSlamdownMcnNotification(slamdownList, MER_FAILED, "Error while processing Slamdown or Mcn notification");
            } finally
            {
                if ( ProcessListCounter  != null) {
                    CommonOamManager.profilerAgent.exitCheckpoint(ProcessListCounter);
                }
            }
        }
        return false; 
    }

    /**
     * Send MER notification for Slamdown or MCN, and type.
     * @param slamdownList SlamdownList
     * @param merEvent Delivered, expired or failed
     * @param failedMessage (if MER_FAILED)
     */
    private void merSlamdownMcnNotification(SlamdownList slamdownList, int merEvent, String failedMessage) {
        if (slamdownList.getNotificationType() == SlamdownList.NOTIFICATION_TYPE_SLAMDOWN) {
            // Slamdown MER event
            switch (merEvent) {
            case MER_DELIVERED:
                mer.slamdownInfoDelivered(slamdownList.getSubscriberNumber());
                break;
            case MER_EXPIRED:
                mer.slamdownInfoExpired(slamdownList.getSubscriberNumber());
                break;
            case MER_FAILED:
                mer.slamdownInfoFailed(slamdownList.getSubscriberNumber(), failedMessage);
                break;
            default:
            	break;
            }
        } else if ((slamdownList.getNotificationType() == SlamdownList.NOTIFICATION_TYPE_MCN_EXTERNAL)||
                    (slamdownList.getNotificationType() == SlamdownList.NOTIFICATION_TYPE_MCN_INTERNAL)) {
            // Missed Call Notification (MCN) MER event
            switch (merEvent) {
            case MER_DELIVERED:
                mer.mcnDelivered(slamdownList.getSubscriberNumber());
                break;
            case MER_EXPIRED:
                mer.mcnExpired(slamdownList.getSubscriberNumber());
                break;
            case MER_FAILED:
                mer.mcnFailed(slamdownList.getSubscriberNumber(), failedMessage);
                break;
            default:
                break;
            }
        }
    }

    private void sendSlamdownOrNotBasedOnConfiguration(SlamdownList slamdownList) {

        if (Config.isSlamdownMcnNotificationWhenPhoneOnExpiry()) {
            // Generate MDR
            mer.phoneOnTimeout(slamdownList.getSubscriberNumber());

            // PhoneOn 'ON' failed, go forward with SMS-Info notification
            if (!slamdownListHandler.handleSmsInfo(slamdownList)) {
                // Generate MDR
                merSlamdownMcnNotification(slamdownList, MER_FAILED, "Unable to send Sms-Info message");

                log.warn("Unable to send SmsInfo for " + slamdownList.getSubscriberNumber() + ", deleting files and cancelling events");

                // Delete Slamdown/Mcn file(s) first
                NotificationHandler.deleteFiles(slamdownList.getNotificationNumber(), SlamdownList.PATTERN_FILE_ALL);

                // Cancel all potential scheduled timers
                slamdownEventHandler.cancelAllEvents(slamdownList, true);
            }

        } else {
            // Delete Slamdown/Mcn file(s) first
            NotificationHandler.deleteFiles(slamdownList.getNotificationNumber(), SlamdownList.PATTERN_FILE_ALL);

            // Cancel all potential scheduled timers
            slamdownEventHandler.cancelAllEvents(slamdownList, true);

            // Generate MDR
            mer.phoneOnTimeout(slamdownList.getSubscriberNumber());
        }
    }
}
