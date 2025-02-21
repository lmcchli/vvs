/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.vvm;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager.FileStatusEnum;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.event.EventRouter;
import com.mobeon.ntf.event.PhoneOnEvent;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.out.ss7.Ss7PhoneOnHandler;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.threads.NtfThread;

public class VvmPhoneOnSender {

    public static final int EVENT_SIZE = 1000;
    private LogAgent log;
    private static VvmPhoneOnReceiver phoneOnReceiver;
    
    /**
     * Constructor
     */
    public VvmPhoneOnSender(ManagedArrayBlockingQueue<Object> queue) {
        log = NtfCmnLogger.getLogAgent(VvmPhoneOnSender.class);
        
        // PhoneOn receiver
        phoneOnReceiver = new VvmPhoneOnReceiver(queue);
        
        // Register to the EventRouter in order to get notified for PhoneOn response from SMSc
        EventRouter.get().register(phoneOnReceiver);
        
        phoneOnReceiver.start();
    }
    
    public synchronized void sendPhoneOnRequest(VvmEvent vvmEvent) {
        
        if(Config.getPhoneOnMethod().equalsIgnoreCase(Config.PHONE_ON_SMS_TYPE_0)) {
            
            UserInfo user = vvmEvent.getUserInfo();
            SMSAddress to = new SMSAddress(Config.getTypeOfNumber(), Config.getNumberingPlanIndicator(), vvmEvent.getSubscriberNumber());
            
            /**
             * The validity period for SMS-Type-0 requests is not the same as the validity period for SMS-Info request. SMS-Type-0
             * has a configured validity at NTF level, SMS-Info's validity can be on a per user/class of service basis.
             */
            int validity = Config.getValidity_smsType0();

            log.debug("Sending PhoneOn request to notification number " + vvmEvent.getSubscriberNumber());

            SMSOut.get().handlePhoneOnRequest(to, validity, vvmEvent.getSubscriberNumber(), user.getPreferredLanguage(), user.getCosName());
            
        } else if (Config.getPhoneOnMethod().equalsIgnoreCase(Config.PHONE_ON_ALERT_SC)) { //HLR_SRI
            
            Ss7PhoneOnHandler.getInstance().requestPhoneOn(vvmEvent.getSubscriberNumber());
        } else {
            log.debug("VvmPhoneOnSender.sendPhoneOnRequest: phoneOnMethod was not sms type 0 nor alert sc");
            EventRouter.get().phoneOn(new PhoneOnEvent(this, vvmEvent.getSubscriberNumber(), PhoneOnEvent.PHONEON_OK, "Phone on was disabled"));
        }
        
        
    }
    
    private class VvmPhoneOnReceiver extends NtfThread implements com.mobeon.ntf.event.PhoneOnEventListener {


        private LogAgent log;
        private BlockingQueue<PhoneOnEvent> phoneOnQueue = new ManagedArrayBlockingQueue<PhoneOnEvent>(EVENT_SIZE);
        private ManagedArrayBlockingQueue<Object> queue;
        private MfsEventManager mfsEventManager;
        private VvmUtil vvmUtil;
        private VvmEventHandler vvmEventHandler;

        VvmPhoneOnReceiver(ManagedArrayBlockingQueue<Object> queue) {
            super("VvmPhoneOnReceiver");
            log = NtfCmnLogger.getLogAgent(VvmPhoneOnReceiver.class);
            this.queue = queue;
            this.mfsEventManager = MfsEventFactory.getMfsEvenManager();
            vvmUtil = new VvmUtil(mfsEventManager);
            vvmEventHandler = (VvmEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.VVM_L3.getName());
        }

        /**
         * Implement PhoneOnEventListener interface
         */
        @Override
        public void phoneOn(PhoneOnEvent ev) {
            
            // before queuing the event, make sure that SIM_SWAP feature is on
            if (!Config.getSimSwapTimeout().equals("0")) {
                try {
                    if (!phoneOnQueue.offer(ev,1,TimeUnit.SECONDS)) {
                        log.warn("Unable to add phone on event for " + ev.getAddress() + "To queue " + " - will be retried..");
                    }   
                } catch ( InterruptedException ie ) {
                    return;
                }
            } else {
                log.debug(" sim swap timeout  : " + Config.getSimSwapTimeout()+ " Phone on is not queued !" ); 
            }
        }

        @SuppressWarnings("null")
        public boolean ntfRun() {
            PhoneOnEvent phoneOnEvent = null;
            long lockId = 0;
            String lockFileName = null;

            try {
                phoneOnEvent = phoneOnQueue.poll(10, TimeUnit.SECONDS);
            } catch (InterruptedException i) {
                log.info("NTF forced shutdown, exiting... ");
                return true; //exit now...
            } catch (OutOfMemoryError me) {
                try {
                    ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                    log.error("NTF out of memory, shutting down... ", me);
                } catch (OutOfMemoryError me2) {;} //ignore second exception
                return true; //exit.
            } catch (Exception e) {
                log.error("Exception in " + getName() + ": ", e);
            }

            if(phoneOnEvent == null) {
                return false; //go back to check management state
            }

            log.debug("Vvm PhoneOn event received: " + phoneOnEvent);

            if (Config.getSimSwapTimeout().compareTo("0") == 0) {
                log.debug(" sim swap timeout  : " + Config.getSimSwapTimeout() + " Phone on Ignored" );
                return false; 
            }

            // Get vvmEvents
            String notificationNumber = phoneOnEvent.getAddress(); 
            File[] files = vvmUtil.getPendingSimSwapNotifications(notificationNumber);
            if (files == null || files.length == 0) {
                log.debug("Event " + phoneOnEvent + " received but nothing found in storage, discard.");
                return false;
            }

            //Get UserInfo
            UserInfo userInfo = UserFactory.findUserByTelephoneNumber(notificationNumber);
            if (userInfo == null) {
                log.debug("Event " + phoneOnEvent + ", but subscriber not found, discard.");
                return false;
            }

            /**
             * Loop through all the pending status files found for the given notification number.
             * Only process PhoneOn event for notification expecting a PhoneOn response.
             */
            ArrayList<VvmEvent> vvmEvents = null;
            for (File file : files) {
                vvmEvents = new ArrayList<VvmEvent>(files.length); 
                String statusFile = file.getName();
                Properties properties = mfsEventManager.getProperties(notificationNumber, statusFile);

                if (properties != null) {
                    Properties eventProperties = null;

                    String eventIdSendPhoneOn = properties.getProperty(VvmEvent.SchedulerIds.SENDING_UNIT_PHONE_ON_EVENT_ID);
                    String eventIdWaitPhoneOn = properties.getProperty(VvmEvent.SchedulerIds.WAITING_PHONE_ON_EVENT_ID);

                    if (!(eventIdSendPhoneOn == null) && !eventIdSendPhoneOn.isEmpty()) {
                        eventProperties = vvmUtil.getEventProperties(eventIdSendPhoneOn);
                    } else if (!(eventIdWaitPhoneOn == null) && !eventIdWaitPhoneOn.isEmpty()) {
                        eventProperties = vvmUtil.getEventProperties(eventIdWaitPhoneOn);
                    } else {
                        log.debug("Event " + phoneOnEvent + " received but no waitingPhoneOnEventId or sendingUnitPhoneOnEventId found, discard.");
                        return false;
                    }

                    VvmEvent vvmEvent = new VvmEvent(eventProperties);
                    try {
                        vvmEvent.initUser(userInfo);
                    } catch (MsgStoreException e) {
                        log.error("Exception while trying to initialize user: " + userInfo.getTelephoneNumber() + " :",e);
                        return false;
                    }

                    vvmEvents.add(vvmEvent);
                }
            }

            if (vvmEvents != null && !vvmEvents.isEmpty()) {

                // Validate if the subscriber's storage is READ-ONLY (using the notification number)
                if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(phoneOnEvent.getAddress())) {
                    log.warn("Storage currently not available for " + phoneOnEvent.getAddress() + ", PhoneOn will retry");
                    return false;
                }

                for (VvmEvent event : vvmEvents) {

                    boolean stored = false;
                    try {
                        lockId = 0;
                        lockFileName = null;
    
                        // Acquire lock file if it's a PHONE_ON response
                        if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_OK || phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_BUSY) {
                            boolean internal = mfsEventManager.isInternal(phoneOnEvent.getAddress());
                            try {
                                lockFileName = event.getNotificationType().getLockFileName();
                                lockId = mfsEventManager.acquireLockFile( phoneOnEvent.getAddress(), 
                                        lockFileName,
                                        Config.getVvmPhoneOnLockFileValidityInSeconds(),
                                        internal);                        
                                if (lockId == 0L) {
                                    log.debug("Vvm PhoneOn lock file not acquired, another NTF instance is processing this PhoneOn event");
                                    continue;
                                }
                            } catch (Exception e) {
                                log.error("Vvm exception while trying to acquire lock file for " + phoneOnEvent.getAddress() + ", PhoneOn will retry", e);
                                return false;
                            }
                        }
    
                        event.setPhoneOnLockId(lockId);
    
                        event.retrieveSchedulerEventIdsPersistent();
    
                        /**
                         * Retrieving stored eventIds from status file (persistent storage).
                         * Status file can be obsolete in which case eventIds still must be cancelled (scheduler cleanup)
                         */
                        FileStatusEnum fileStatus = mfsEventManager.fileExistsValidation(notificationNumber, event.getNotificationType().getStatusFileName(), Config.getVvmStatusFileValidityInMin());
                        if (fileStatus.equals(FileStatusEnum.FILE_EXISTS_AND_INVALID)) {
                            log.info("Obsolete " + event.getNotificationType().getStatusFileName() + " file (and eventIds) for " + notificationNumber + " " + event.getSchedulerIds());
                            vvmEventHandler.cancelAllEvents(event, false);
                            continue;
                        }
    
                        setVvmEventState(event, phoneOnEvent);
    
                        if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_OK || phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_BUSY) {
                            log.debug("PhoneOn received successfully for notification number " + phoneOnEvent.getAddress());
                            log.debug("PhoneOn received successfully: " + phoneOnEvent);
    
                            // Successful PhoneOn response from SMSc
                            event.setCurrentEvent(VvmEvent.VVM_EVENT_PHONE_ON_OK);
                        } else if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_FAILED_TEMPORARY) {
                            log.debug("PhoneOn Response: failed temporary: " + phoneOnEvent);
    
                            // PhoneOn response: failed temporary
                            event.setCurrentEvent(VvmEvent.VVM_EVENT_PHONE_ON_RETRY);
                        } else if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_FAILED) {
                            log.debug("PhoneOn Response: failed: " + phoneOnEvent);
    
                            // PhoneOn response: failed
                            event.setCurrentEvent(VvmEvent.VVM_EVENT_PHONE_ON_FAILED);
                        } else if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_CLIENT_SENT_SUCCESSFULLY) {
                            log.debug("PhoneOn CLIENT sent out successfully: " + phoneOnEvent);
    
                            // PhoneOn CLIENT sent out successfully
                            event.setCurrentEvent(VvmEvent.VVM_EVENT_PHONE_ON_SENT_SUCCESSFULLY);
                        } else if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_CLIENT_FAILED_TEMPORARY) {
                            log.debug("PhoneOn CLIENT failed temporary: " + phoneOnEvent);
    
                            // PhoneOn CLIENT retry
                            event.setCurrentEvent(VvmEvent.VVM_EVENT_PHONE_ON_CLIENT_RETRY);
                        } else if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_CLIENT_FAILED) {
                            log.debug("PhoneOn CLIENT failed: " + phoneOnEvent);
    
                            // PhoneOn CLIENT failed
                            event.setCurrentEvent(VvmEvent.VVM_EVENT_PHONE_ON_CLIENT_FAILED);
                        } else if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_SS7_ERROR) {
                            log.debug("PhoneOn CLIENT failed: " + phoneOnEvent);
    
                            // PhoneOn SS7error, retry
                            event.setCurrentEvent(VvmEvent.VVM_EVENT_PHONE_ON_CLIENT_RETRY);
                        } else {
                            log.debug("PhoneOn failed: " + phoneOnEvent);
    
                            // PhoneOn error, retry
                            event.setCurrentEvent(VvmEvent.VVM_EVENT_PHONE_ON_FAILED);
                        }
    
                        // Put in the worker-queue
                        stored = addToQueue(event, phoneOnEvent);
                        
                    } finally {
                        
                        if(!stored) {
                            //something went wrong (an Exception was thrown or the VVM level-3 queue is full), release lock file now since VvmWorker will not.
                            try {
                                if (lockId !=0 && lockFileName != null) {
                                    boolean internal = mfsEventManager.isInternal(phoneOnEvent.getAddress());
                                    mfsEventManager.releaseLockFile(phoneOnEvent.getAddress(), lockFileName, lockId, internal);
                                }
                            } catch (Exception ie) {
                                log.warn("Exception trying to release lock file " + lockFileName + (phoneOnEvent != null ? " for " + phoneOnEvent.getAddress() : "") + " ", ie);
                            }
                        }
                    }
                }
                

            } else {
                log.debug("Event " + phoneOnEvent + " received but not found in storage, discard.");
            }
            return false;
        }

        

        /**
         * Add the event to the eventQueue and logs a WARNING if the queue must WAIT because the max size is reached.
         * @param vvmEvent VvmEvent
         * @param phoneOnEvent PhoneOnEvent
         */
        private boolean addToQueue(VvmEvent vvmEvent, PhoneOnEvent phoneOnEvent) {
            boolean stored = queue.offer(vvmEvent); 
            if (!stored) {
                log.warn("Event " + phoneOnEvent + " will WAIT a short time in order to be queued: " + vvmEvent.getSubscriberNumber() + " : " + phoneOnEvent.getAddress());
                if (!queue.offer(vvmEvent, 1, TimeUnit.SECONDS)) {
                    log.warn("Event " + phoneOnEvent + " failed after waiting.., will be retried :  " + vvmEvent.getSubscriberNumber() + " : " + phoneOnEvent.getAddress());
                } else {
                    log.warn("Event " + phoneOnEvent + " queud after waiting: " + vvmEvent.getSubscriberNumber() + " : " + phoneOnEvent.getAddress());
                    stored = true;
                }

            }
            
            return stored;
        }

        /**
         * Sets the state of the vvmEvent recreated from IOs based on the slamdownmcn.status file (event ids).
         * This method takes the greater state (STATE_SENDING_UNIT -> STATE_DEACTIVATOR) that a vvmEvent can be into.
         *   
         * @param vvmEvent VvmEvent
         * @param phoneOnEvent PhoneOnEvent
         */
        private void setVvmEventState(VvmEvent vvmEvent, PhoneOnEvent phoneOnEvent) {
            int currentState = -1;

            //TODO: for phoneOn response with notification aggregation, we never want to skip STATE_SENDING_INFO since we could end up not sending the VVM notif
            if (vvmEvent.getSchedulerIds().getSmsInfoEventId() != null && vvmEvent.getSchedulerIds().getSmsInfoEventId().length() > 0) {
                currentState = VvmEvent.STATE_SENDING_INFO;
            }

            if (vvmEvent.getSchedulerIds().getSendingUnitPhoneOnEventId() != null && vvmEvent.getSchedulerIds().getSendingUnitPhoneOnEventId().length() > 0) {
                currentState = VvmEvent.STATE_SENDING_PHONE_ON;
            }
            
            if (vvmEvent.getSchedulerIds().getWaitingPhoneOnEventId() != null && vvmEvent.getSchedulerIds().getWaitingPhoneOnEventId().length() > 0) {
                currentState = VvmEvent.STATE_WAITING_PHONE_ON;
            }

            if (vvmEvent.getSchedulerIds().getDeactivatorEventId() != null && vvmEvent.getSchedulerIds().getDeactivatorEventId().length() > 0) {
                currentState = VvmEvent.STATE_DEACTIVATOR;
            }

            if (currentState < 0) {
                if (phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_SS7_ERROR ||
                    phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_CLIENT_SENT_SUCCESSFULLY ||
                    phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_CLIENT_FAILED ||
                    phoneOnEvent.getResult() == PhoneOnEvent.PHONEON_CLIENT_FAILED_TEMPORARY) {
                    // phoneOnEvent.result is about status on the phoneOn sent, must be handled in the STATE_SENDING_UNIT state.
                    currentState = VvmEvent.STATE_SENDING_PHONE_ON;
                } else {
                    // phoneOnEvent.result is about status on a phoneOn delivered, must be handled in the STATE_WAITING_PHONE_ON state.
                    currentState = VvmEvent.STATE_WAITING_PHONE_ON;;
                }

                log.warn("Receiving PhoneOn response for: " + vvmEvent.getSubscriberNumber() + " : " + phoneOnEvent.getAddress() +
                " while no "+ vvmEvent.getNotificationType().getStatusFileName() + " file found.  Will set the state to " + VvmEvent.STATE_STRING[currentState]);
            }

            vvmEvent.setCurrentState(currentState);
        }


        @Override
        /**
         * The shutdown loop stops after the ntfRun method is finished.
         *
         * @return true if can shutdown now.
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
    }
}
