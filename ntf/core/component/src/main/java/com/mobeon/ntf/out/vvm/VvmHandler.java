/**
 * Copyright (c) 2010 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.vvm;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.mcd.KeyValues;
import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.util.DesignSequenceDiagram;
import com.abcxyz.messaging.mfs.User;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccessException;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryUpdater;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackHandler;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackUtil;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager.FileStatusEnum;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.NtfMain;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.text.TemplateMessageGenerationException;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.out.vvm.VvmEvent.VvmEventTypes;
import com.mobeon.ntf.phonedetection.PhoneStatus;
import com.mobeon.ntf.phonedetection.PhoneStatus.State;

/**
 * VvmHandler controls the reception, storing, formatting and sending of Visual Voice Mail (VVM) notifications.
 */
public class VvmHandler implements com.mobeon.ntf.Constants {

    private LogAgent log;
    private static VvmHandler _inst;
    private VvmWorker[] vvmWorkers;
    private VvmEventHandler vvmEventHandler;
    private VvmEventHandlerSmsInfo vvmEventHandlerSmsInfo;
    private VvmEventHandlerSendingUnitPhoneOn vvmEventHandlerSendingUnitPhoneOn;
    private VvmEventHandlerWaitingPhoneOn vvmEventHandlerWaitingPhoneOn;
    private VvmEventHandlerDeactivator vvmEventHandlerDeactivator;
    private VvmEventHandlerActivityDetected vvmEventHandlerActivityDetected;
    private VvmPhoneOnSender vvmPhoneOnSender;
    private ManagedArrayBlockingQueue<Object> workingQueue;
    private boolean isStarted=false;
    private MfsEventManager mfsEventManager;
    private VvmUtil vvmUtil;
   
    private static final String MOIP_VVM_SYSTEM_ACTIVATED = "MOIPVvmSystemActivated";
    private static final String MOIP_VVM_FIRST_TIME_ACTIVATED = "MOIPVvmFirstTimeActivated";
    
    public VvmHandler() {
        try {
            _inst = this;
            log = NtfCmnLogger.getLogAgent(VvmHandler.class);

            // Create working queue
            workingQueue = new ManagedArrayBlockingQueue<Object>(Config.getVvmQueueSize());
            
            if(mfsEventManager == null) {
                mfsEventManager = MfsEventFactory.getMfsEvenManager();
            }
            
            vvmUtil = new VvmUtil(mfsEventManager);

            // Create VVM event handlers
            vvmEventHandler = (VvmEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.VVM_L3.getName());
            vvmEventHandlerSmsInfo = (VvmEventHandlerSmsInfo)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.VVM_SMS_INFO.getName());
            vvmEventHandlerSendingUnitPhoneOn = (VvmEventHandlerSendingUnitPhoneOn)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.VVM_SENDING_PHONEON.getName());
            vvmEventHandlerWaitingPhoneOn = (VvmEventHandlerWaitingPhoneOn)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.VVM_WAIT_PHONEON.getName());
            vvmEventHandlerDeactivator = (VvmEventHandlerDeactivator)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.VVM_DEACTIVATOR.getName());
            vvmEventHandlerActivityDetected = (VvmEventHandlerActivityDetected)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.VVM_ACTIVATOR.getName());

            int numberOfWorkers = Config.getVvmWorkers();

            // Create VVM workers
            if(numberOfWorkers>0)
            {
                vvmPhoneOnSender = new VvmPhoneOnSender(workingQueue);
                createWorkers(numberOfWorkers);
                isStarted=true;
            }
            
        } catch (Exception e) {log.error("Unable to start vvm service");}
    }

    public static VvmHandler get() {
        return _inst;
    }

    public ManagedArrayBlockingQueue<Object> getWorkingQueue() {
        return workingQueue;
    }

    /**
     * Create the workers
     */
    private void createWorkers(int numberOfWorkers) {
        vvmWorkers = new VvmWorker[numberOfWorkers];

        for (int i = 0; i<numberOfWorkers; i++) {
            vvmWorkers[i] = new VvmWorker(workingQueue, "VvmWorker-" + i, this);
            vvmWorkers[i].setDaemon(true);
            vvmWorkers[i].start();
        }
    }

    /**
     * Handles VVM events originating from NTF
     *
     * @param user UserInfo
     * @param ng NotificationGroup
     * @param userMailbox UserMailbox
     * @param notificationType VvmEventTypes
     * @param otherNotificationSent boolean
     * @return number of SMS notification that will be sent
     */
    public int handleVvm(UserInfo user, NotificationGroup ng, UserMailbox userMailbox, VvmEventTypes notificationType, boolean otherNotificationSent) {
        int count = 0;
        String notificationNumber = ng.getEmail().getReceiver();

        if (!isStarted()) {
            log.error("Received Vvm notification but service is not started");
            ng.retry(user, NTF_VVM, "Vvm service not started");
            return count;
        }
        
        // Check if a notification number has been found
        if (notificationNumber == null) { 
            log.debug("VvmHandler ignoring vvm event, no notification number found ");

            // Inform the feedback handler for CDR generation
            ng.failed(user, NTF_VVM, "Vvm Level-2 Failed No notification number for vvm notification");
            return count;
        }
        
        /**
         * Validate if the subscriber's storage is READ-ONLY (using the notification number).
         * Even if the event is expiry, let the scheduler retry so that the persistent file can be deleted properly.
         */
        if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(notificationNumber)) {
            log.warn("Storage currently not available for " + notificationNumber + " : " + ng.getEmail().getReceiverPhoneNumber() + ", will retry");
            ng.retry(user, NTF_VVM, "Storage currently not available for " + notificationNumber + ", will retry");
            return count;
        }
        
        // Level-2 expiry handling
        NtfEvent ntfEvent = ng.getEmail().getNtfEvent();
        if (ntfEvent.isExpiry()) {
            log.debug("VvmHandler expired for Vvm event " + ntfEvent.getReferenceId());

            // Stop Level-2 scheduling
            ng.expired(user, NTF_VVM, "Vvm Level-2 Expiry");
            return count;
        }
        
        String callerNumber = ng.getEmail().getSenderPhoneNumber();
        VvmEvent vvmEvent = new VvmEvent(ng.getEmail().getReceiverPhoneNumber(), user, ng.getEmail(), userMailbox, notificationType, callerNumber, otherNotificationSent);
        vvmEvent.retrieveSchedulerEventIdsPersistent();
        
        
        /**
         * Retrieving stored eventIds from status file (persistent storage).
         * Status file can be obsolete in which case eventIds still must be cancelled (scheduler cleanup)
         * and keep going with the business logic as if no eventIds were found.   
         */
        FileStatusEnum fileStatus = mfsEventManager.fileExistsValidation(notificationNumber, vvmEvent.getNotificationType().getStatusFileName(), Config.getVvmStatusFileValidityInMin());
        if (fileStatus.equals(FileStatusEnum.FILE_EXISTS_AND_INVALID)) {
            log.info("Obsolete " + vvmEvent.getNotificationType().getStatusFileName() + " file (and eventIds) for " + notificationNumber + " " + vvmEvent.getSchedulerIds());
   
            // Cancel obsolete eventIds
            if (!vvmEventHandler.cancelAllEvents(vvmEvent, false)) {
                String message = "Unable to cancel and update persistent storage for obsolete eventIds for " + ntfEvent.getRecipient() + ", will retry (level-2)";
                log.warn("VvmHandler:handleVvm: " + message);
                ng.retry(user, NTF_VVM, message);
                return count;
            } else {
                log.info("Obsolete eventIds cancelled for " + ntfEvent.getRecipient());
            }
        }
        
        if(!vvmEvent.getSchedulerIds().isEmtpy()) {
            
            //TODO: implement notif aggregation. Only do level-2 retry if notif is still in initial sending state. For now, always do level-2 retry.
            //TODO: We should send VVM notif but skip sim swap feature if there is an existing notif of the same type for this subscriber
            //      that is in one of the sim swap state (schedulerId for sendingInfo is empty). Update state machine in VvmWorker to handle this.
            //@see DP107 Task 12830
            String message = "Attempting to handle a new Vvm notif while one is already scheduled for " 
                                + notificationNumber + ", will retry later (level-2)";
            log.info("VvmHandler:handleVvm: " + message);
            ng.retry(user, NTF_VVM, message);
            return count;
            
        } else {
            log.debug("VvmHandler:handleVvm: No pending Vvm notification for " + notificationNumber);

            // Schedule level-3 event
            boolean successfullyScheduled = vvmEventHandlerSmsInfo.scheduleSmsInfo(vvmEvent);
            if (!successfullyScheduled) {
                String message = "Unable to schedule SmsUnit for " + notificationNumber + ", will retry (level-2)";
                log.info("VvmHandler:handleVvm: " + message);
                ng.retry(user, NTF_VVM, message);
                return count;
            }

        }

        
        // Put in queue        
        if(!workingQueue.offer(vvmEvent,5,TimeUnit.SECONDS)) {
            //level-3 retry already scheduled, do not call ng.retry()
            log.info("handleVvm: queue full or state locked while handling new Vvm notification, will retry (level-3)");
        }
        
        /**
         * If notification number(s) found, a level-3 schedule has been started (Scheduling internal to NTF)
         * In this case, the level-2 schedule (the event between MAS and NTF) must be cancelled.
         * This does not mean though that a CDR event must be generated at this time yet, must wait until
         * the level-3 process completion.
         */
        // Stop level-2 scheduling
        ng.ok(user, NTF_VVM);
        count++;
        
        return count;
    }
    
    
    
    /**
     * Handles VVM events originating from NTF
     *
     * @param user UserInfo
     * @param ng NotificationGroup
     * @param userMailbox UserMailbox
     * @return number of SMS notification that will be sent
     */
    public int handleVvmActivityDetected(UserInfo user, NotificationGroup ng, UserMailbox userMailbox) {
        int count = 0;
        boolean successfullyProcessed = true;
        
        String notificationNumber = ng.getEmail().getReceiver();

        if (!isStarted()) {
            log.error("Received Vvm Activity Detected but service is not started");
            ng.retry(user, NTF_VVM, "Vvm service not started");
            return count;
        }
        
        // Check if a notification number has been found
        if (notificationNumber == null) {
            log.debug("VvmHandler ignoring vvm Activity Detected, no notification number found ");

            // Inform the feedback handler for CDR generation
            ng.failed(user, NTF_VVM, "Vvm Level-2 Failed No notification number for Vvm Activity Detected");
            return count;
        }
        
        /**
         * Validate if the subscriber's storage is READ-ONLY (using the notification number).
         * Even if the event is expiry, let the scheduler retry so that the persistent file can be deleted properly.
         */
        if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(notificationNumber)) { 
            log.warn("Storage currently not available for " + notificationNumber + " : " + ng.getEmail().getReceiverPhoneNumber() + ", will retry");
            ng.retry(user, NTF_VVM, "Storage currently not available for " + notificationNumber + ", will retry");
            return count;
        }
        
        // Level-2 expiry handling
        NtfEvent ntfEvent = ng.getEmail().getNtfEvent();
        if (ntfEvent.isExpiry()) {
            log.debug("VvmHandler expired for Vvm event " + ntfEvent.getReferenceId());

            // Stop Level-2 scheduling
            ng.expired(user, NTF_VVM, "Vvm Level-2 Expiry");
            return count;
        }
        
        // Get pending VVM notifications
        // The validity period of the file is not considered since the processing would be the same (canceling pending events) 
        File[] files = vvmUtil.getPendingSimSwapNotifications(notificationNumber);
        if (files != null) {
            /**
             * Loop through all the pending status files found for the given notification number.
             * Only process Activity Detected for notification in one of the SIM SWAP state (STATE_SENDING_PHONE_ON, STATE_WAITING_PHONE_ON or STATE_DEACTIVATOR).
             */
            for (File file : files) {
                String statusFile = file.getName();
                Properties properties = mfsEventManager.getProperties(notificationNumber, statusFile);
    
                if (properties != null) {
                    Properties eventProperties = null;
                    
                    String eventIdSendPhoneOn = properties.getProperty(VvmEvent.SchedulerIds.SENDING_UNIT_PHONE_ON_EVENT_ID);
                    String eventIdWaitPhoneOn = properties.getProperty(VvmEvent.SchedulerIds.WAITING_PHONE_ON_EVENT_ID);
                    String eventIdDeactivator = properties.getProperty(VvmEvent.SchedulerIds.DEACTIVATOR_EVENT_ID);
                    
                    if (!(eventIdDeactivator == null) && !eventIdDeactivator.isEmpty()) {
                        eventProperties = vvmUtil.getEventProperties(eventIdDeactivator);
                    } else if (!(eventIdSendPhoneOn == null) && !eventIdSendPhoneOn.isEmpty()) {
                        eventProperties = vvmUtil.getEventProperties(eventIdSendPhoneOn);
                    } else if (!(eventIdWaitPhoneOn == null) && !eventIdWaitPhoneOn.isEmpty()) {
                        eventProperties = vvmUtil.getEventProperties(eventIdWaitPhoneOn);
                    } else {
                        log.debug("Vvm Activity Detected notification received for " + statusFile +
                                  " but no deactivatorEventId, waitingPhoneOnEventId or sendingUnitPhoneOnEventId found, discard.");
                        continue;
                    }
                    
                    //TODO: process phoneOnLock..
                    VvmEvent vvmEvent = new VvmEvent(eventProperties);
                    vvmEvent.retrieveSchedulerEventIdsPersistent();
                    
                  //TODO: this should not cancel pending vvm notif if there is notif aggregation
                    if(!vvmEventHandler.cancelAllEvents(vvmEvent, false)) { 
                        log.debug( "VvmHandler:handleVvmActivityDetected: Could not cancel a pending Vvm SimSwap notification for " 
                                    + notificationNumber + ", will retry (level-2)");
                        
                        successfullyProcessed = false;
                    }
                    
                }
            }
        } 
        
        // Update subscriber profile
        if (!user.isVVMSystemActivated()) {
            VvmEvent vvmEvent = new VvmEvent(ng.getEmail().getReceiverPhoneNumber(), user, ng.getEmail(), userMailbox, null, null, false);
            vvmEvent.setCurrentState(VvmEvent.STATE_ACTIVATOR);
            vvmEvent.setCurrentEvent(VvmEvent.VVM_EVENT_ACTIVITY_DETECTED);
            
            vvmEventHandlerActivityDetected.scheduleActivityDetected(vvmEvent);
            
            // Put in queue        
            if(!workingQueue.offer(vvmEvent,5,TimeUnit.SECONDS)) {
                //level-3 retry already scheduled, do not call ng.retry()
                log.info("handleVvm: queue full or state locked while handling new Vvm notification, will retry (level-3)");
            } 
        }
        
        if (!successfullyProcessed) { 
            /**
             * If there are more then one pending notification to be processed, retry for all of them.
             */
            String message = "Could not process VVM activity detected for " + notificationNumber + ", will retry (level-2)";
            ng.retry(user, NTF_VVM, message);
        } else {
            /**
             * If the event has been processed successfully, the level-2 schedule (the event between MAS and NTF) must be cancelled.
             */
            // Stop level-2 scheduling
            ng.ok(user, NTF_VVM);
            count++;
        }
        

        return count;
    }

    protected void handleActivityDetected(VvmEvent vvmEvent) {
        
        boolean successfullyProcessed = true;
        UserInfo user = vvmEvent.getUserInfo();
        
        // Update subscriber profile if needed
        if (!user.isVVMSystemActivated()) {
            String msid = user.getMsid();
            log.debug("VvmHandler:handleVvmActivityDetected: Subscriber " + msid + " will have VVMSystemActivated set to true");
            
            if(!setSubVvmSystemActivatedFlag(msid, null, true, false)) {
                String message = "Could not update VVMSystemActivated for " + vvmEvent.getSubscriberNumber() + ", will retry (level-3)";
                log.debug("VvmHandler:handleVvmActivityDetected: " + message);
                
                successfullyProcessed = false;
            }
        }
        
        if(successfullyProcessed) {
            vvmEventHandler.cancelEvent(vvmEvent.getSchedulerIds().getActivatorEventId());
        }
        
    }

    /**
     * Handles VVM requests from VvmWorkers
     *
     * @param vvmEvent VvmEvent
     */
    public void handleSmsUnitNoScheduling(VvmEvent vvmEvent) {
        
        vvmEvent.setCurrentEvent(VvmEvent.VVM_EVENT_SENDING);
        sendwithRoamingCheck(vvmEvent);
    }

    //send vvm notification if allowed - checking roaming state
    private boolean sendwithRoamingCheck(VvmEvent vvmEvent){
        UserInfo user = vvmEvent.getUserInfo();
        
        if (!user.isVvmNotificationAllowedWhileRoaming() && Config.getCheckRoaming()) {
            log.debug("VvmHandler.isVVMPhoneRoaming() - Notification are not allowed if the user is roaming, checking the roaming status");
            
            // If no filter defined no VVM SMS notification to be sent
            // If roaming make sure notifications are allowed
            PhoneStatus phoneStatus = PhoneStatus.getPhoneStatus(user.getTelephoneNumber()); // PhoneStatus is already initialized by the caller.
            boolean isRoaming = false;
            log.debug("sendwithRoamingCheck - PhoneStatus = " + phoneStatus.toString());
            State roaming = phoneStatus.isRoaming();
            if (roaming == PhoneStatus.State.ERROR ||roaming == PhoneStatus.State.NONE)
            {
                log.warn("sendwithRoamingCheck() - unable to determine roaming status checking failure action.");
                hlrFailAction action = Config.getHLRRoamingFailureAction();
                log.info("endwithRoamingCheck() " + NotificationConfigConstants.HLR_ROAM_FAILURE_ACTION + " " + action );
                switch (action) {
                    case RETRY:
                        vvmEvent.setCurrentEvent(VvmEvent.VVM_EVENT_SMS_UNIT_RETRY);
                        return false; //next scheduled event will retry.
                    case FAIL:
                        log.warn("Failed to detect roaming state, canceling event "+ vvmEvent.toString());
                        return vvmEventHandler.cancelAllEvents(vvmEvent, false);                         
                    case ROAM: //assume subscriber is roaming. 
                        isRoaming=true;
                        break;
                    case HOME: //assume home
                        isRoaming=false;
                        break;
                    default:
                        log.info("sendwithRoamingCheck - unknown failure action: " + action + " assuming retry." );
                        vvmEvent.setCurrentEvent(VvmEvent.VVM_EVENT_SMS_UNIT_RETRY);
                        return false;
                } 
            } else {
                isRoaming = (phoneStatus.isRoaming() == PhoneStatus.State.YES);
            }
            
            
            if(isRoaming){
                log.debug("VvmHandler.isVVMPhoneRoaming() - VVM notification not allowed because subscriber is roaming by setting in sub profile.");
                return vvmEventHandler.cancelAllEvents(vvmEvent, false);
            } else {
                log.debug("VvmHandler.isVVMPhoneRoaming() - VVM notification allowed as not roaming.");
            }
        } else{
            log.debug("VvmHandler.isVVMPhoneRoaming() - Notification are allowed even if the user is roaming or roaming check disabled.");
            
        }
                              
        // Send VVM request
        if(log.isDebugEnabled()) {
            log.debug("Sending VVM request (" + vvmEvent.getSubscriberNumber() + " : " + vvmEvent.getNotificationType().getName() + ") to SMS-Client");
        }
        try {
            return SMSOut.get().handleVvm(vvmEvent);
        } catch (TemplateMessageGenerationException e) {
            log.error("TemplateMessageGenerationException received in handleSmsUnit: ",e);
            return false;
        }     
    }
    
    /**
     * Handles VVM requests from VvmWorkers
     *
     * @param vvmEvent VvmEvent
     */
    public boolean handleSmsUnit(VvmEvent vvmEvent) {

        // Update vvmEvent
        vvmEvent.setCurrentEvent(VvmEvent.VVM_EVENT_SENDING);

        // Start SmsUnit timer
        boolean successfullyScheduled = vvmEventHandlerSmsInfo.scheduleSmsInfo(vvmEvent);
        if (!successfullyScheduled) {
            log.warn("Unable to schedule SmsUnit for " + vvmEvent.getSubscriberNumber() + ", will retry");
            return false;
        }
        
        return sendwithRoamingCheck(vvmEvent);
        
    }
    
    public void handleSendingPhoneOn(VvmEvent vvmEvent) {
        
        vvmEvent.setCurrentState(VvmEvent.STATE_SENDING_PHONE_ON);
        
        //Check if SimSwap feature is enabled and only do the system activated check for deposit, the other event might not trigger IMAP VVM query
        if( isSimSwapConfigActive() && (vvmEvent.getNotificationType().equals(VvmEvent.VvmEventTypes.APPLEVVM_DEPOSIT) || vvmEvent.getNotificationType().equals(VvmEvent.VvmEventTypes.VVM_DEPOSIT)) ) {
            
            // Start SMS-Unit timer
            boolean successfullyScheduled = vvmEventHandlerSendingUnitPhoneOn.scheduleSendingUnit(vvmEvent);
            if (!successfullyScheduled) {
                log.warn("Unable to schedule SmsUnit for " + vvmEvent.getSubscriberNumber() + ", canceling events for VVM SimSwap feature.");
                /**
                 * Cancel all potential scheduled timers.
                 * 
                 * In the case where scheduleSendingUnit fails, cancel all events for Sim Swap feature. There is no guarantee that the VVM Client
                 * will fetch messages again when it receives the same VVM notification a second time.
                 */
                vvmEventHandler.cancelAllEvents(vvmEvent, true);
                return;
            }
            
            //Send the phone On 
            vvmPhoneOnSender.sendPhoneOnRequest(vvmEvent);
            
        } else {
            /**
             * SimSwap feature is disabled
             * Cancel all potential scheduled timers.
             * 
             * In the case where an existing SimSwap timer (sending unit, waiting phone on, deactivator) was already scheduled for a previous notification
             * before the Config was changed, NTF cancels these timers since the feature is now off
             */
            vvmEventHandler.cancelAllEvents(vvmEvent, true);
        }
   
    }
    
    public void handleSendingPhoneOnNoScheduling(VvmEvent vvmEvent) {

        //Check if SimSwap feature is enabled and only do the system activated check for deposit, the other event might not trigger IMAP VVM query
        if( isSimSwapConfigActive() && (vvmEvent.getNotificationType().equals(VvmEvent.VvmEventTypes.APPLEVVM_DEPOSIT) || vvmEvent.getNotificationType().equals(VvmEvent.VvmEventTypes.VVM_DEPOSIT)) ) {
            
            //Send the phone On 
            vvmPhoneOnSender.sendPhoneOnRequest(vvmEvent);
            
        } else {
            /**
             * SimSwap feature is disabled
             * Cancel all potential scheduled timers.
             * 
             * In the case were an existing SimSwap timer (sending unit, waiting phone on, deactivator) was already scheduled for a previous notification
             * before the Config was changed, NTF cancels these timers since the feature is now off
             */
            vvmEventHandler.cancelAllEvents(vvmEvent, true);
        }
   
    }
    
    public boolean handleWaitingPhoneOn(VvmEvent vvmEvent) {
        boolean result = true;
        
        vvmEvent.setCurrentState(VvmEvent.STATE_WAITING_PHONE_ON);
        
        result = vvmEventHandlerWaitingPhoneOn.scheduleWaitingPhoneOn(vvmEvent);
        
        return result;
    }

    
    public boolean handleDeactivator(VvmEvent vvmEvent) {

        vvmEvent.setCurrentState(VvmEvent.STATE_DEACTIVATOR);
        
        // Start Deactivator timer
        boolean successfullyScheduled = vvmEventHandlerDeactivator.scheduleDeactivator(vvmEvent);
        
        //Release VVM lock file
        releaseLockFile(vvmEvent); 
        
        if (!successfullyScheduled) {
            log.warn("Unable to schedule Deactivator for " + vvmEvent.getSubscriberNumber() + ", will retry");
            return false;
        }
        
        return true;
    }
    
    public boolean handleDeactivateVvm(VvmEvent vvmEvent){
        
        String msid = vvmEvent.getUserInfo().getMsid();
        boolean otherNotificationSent = vvmEvent.wasOtherNotificationSent();
        
        if(!setSubVvmSystemActivatedFlag(msid, vvmEvent, false, otherNotificationSent)) {
            log.warn("Unable to update profile for " + vvmEvent.getSubscriberNumber() + ", will retry");
            return false;
        }
            
        vvmEventHandler.cancelAllEvents(vvmEvent, true);
        return true;
    }


    public void releaseLockFile(VvmEvent vvmEvent) {
        log.debug("Releasing lock file for" + vvmEvent.getSubscriberNumber());
        try {
            boolean internal = mfsEventManager.isInternal(vvmEvent.getSubscriberNumber());
            mfsEventManager.releaseLockFile(vvmEvent.getSubscriberNumber(), vvmEvent.getNotificationType().getLockFileName(), vvmEvent.getPhoneOnLockId(), internal);
        } catch (TrafficEventSenderException e) {
            log.warn("Exception trying to release lock file " + vvmEvent.getNotificationType().getLockFileName() + " for " + vvmEvent.getSubscriberNumber(), e);
        }
    }
    
    /**
     * Profiling method
     * @param checkPoint String
     */
    public void profilerAgentCheckPoint(String checkPoint) {
        Object perf = null;
        if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
            try {
                perf = CommonOamManager.profilerAgent.enterCheckpoint(checkPoint);
            } finally {
                CommonOamManager.profilerAgent.exitCheckpoint(perf);
            }
        }
    }
    public boolean isStarted()
    {
        return isStarted;
    }
    
    
    /**
     * Set a subscriber's VVM system deactivated flag in MCD.  If the flag changes, listeners are notified.
     * 
     * @param msid String
     *        The msid of the subscriber for whom to set the flag
     * @param vvmEvent VvmEvent
     *        The ID of the original NtfEvent passed to {@link #subVvmActivatedFlagWasModified(String msid, VvmEvent vvmEvent, boolean otherNotificationsWereSent,
              boolean oldSystemActivated, boolean newSystemActivatedFlag)}
     * @param newSystemActivatedFlag
     *        false to set the sub "VVM system deactivated", true to set it "VVM system activated"
     * @param otherNotificationsWereSent true if any other notifications were sent for the given eventId
     */
    private boolean setSubVvmSystemActivatedFlag(String msid, VvmEvent vvmEvent, boolean newSystemActivatedFlag, boolean otherNotificationsWereSent) {
        boolean result = true;
        boolean oldSystemActivated = getSubVvmSystemActivatedFlag(msid);

        log.debug(MOIP_VVM_SYSTEM_ACTIVATED + ": OLD = " + (oldSystemActivated ? "yes" : "no") + "; NEW = " + (newSystemActivatedFlag ? "yes" : "no"));
        
        if (newSystemActivatedFlag != oldSystemActivated) {
            DirectoryUpdater dirUpdater = DirectoryUpdater.getInstance();
            String newSystemActivatedString = newSystemActivatedFlag ? "yes" : "no";
            String values[] = { newSystemActivatedString };
            KeyValues kv = new KeyValues(MOIP_VVM_SYSTEM_ACTIVATED, values);
            Modification mod = new Modification(Modification.Operation.REPLACE, kv);
            List<Modification> mods = new Vector<Modification>();
            mods.add(mod);

            try {
                URI subMsidURI = new URI("msid", msid, null);
                log.debug("Updating MCD subscriber profile for URI " + subMsidURI + ". Setting " + MOIP_VVM_SYSTEM_ACTIVATED + " = "
                        + newSystemActivatedString);
                dirUpdater.updateProfile("subscriber", subMsidURI, mods);
                log.debug("MCD subscriber profile updated successfully");

                subVvmActivatedFlagWasModified(msid, vvmEvent, otherNotificationsWereSent, oldSystemActivated, newSystemActivatedFlag);
                
            } catch (URISyntaxException e) {
                log.error("Failed to update MCD subscriber profile for msid " + msid, e);
                result = false;
            } catch (DirectoryAccessException e) {
                log.error("Failed to update MCD subscriber profile for msid " + msid, e);
                result = false;
            }
        }
        
        return result;
    }
    

    public boolean handleVvmImapFirstDetected (UserInfo user) {
        return setSubVvmFirstTimeActivatedFlag(user.getMsid());
    }
    
    private boolean setSubVvmFirstTimeActivatedFlag(String msid) {
        boolean result = true;
        boolean currentValue = getSubVvmFirstTimeActivatedFlag(msid);
        
        if (!currentValue) {
            log.debug(MOIP_VVM_FIRST_TIME_ACTIVATED + " will be set to yes");
            DirectoryUpdater dirUpdater = DirectoryUpdater.getInstance();
            String values[] = { "yes" };
            KeyValues kv = new KeyValues(MOIP_VVM_FIRST_TIME_ACTIVATED, values);
            Modification mod = new Modification(Modification.Operation.REPLACE, kv);
            List<Modification> mods = new Vector<Modification>();
            mods.add(mod);
            
            try {
                URI subMsidURI = new URI("msid", msid, null);
                log.debug("Updating MCD subscriber profile for URI " + subMsidURI + ". Setting " + MOIP_VVM_FIRST_TIME_ACTIVATED + " = " + values[0]);
                dirUpdater.updateProfile("subscriber", subMsidURI, mods);
                log.debug("MCD subscriber profile updated successfully");
            } catch (URISyntaxException e) {
                result = false;
                log.error("Failed to update MCD subscriber profile for msid " + msid, e);
            } catch (DirectoryAccessException e) {
                result = false;
                log.error("Failed to update MCD subscriber profile for msid " + msid, e);
            }
        }
        
        return result;
    }
    
    private boolean getSubVvmSystemActivatedFlag(String msid) {
        boolean isVvmSystemActivated = false;
        
        DirectoryUpdater dirUpdater = DirectoryUpdater.getInstance();
        
        if (dirUpdater != null) {
            IDirectoryAccessSubscriber sub = dirUpdater.lookupSubscriber("msid:" + msid);
            
            if (sub != null) {
                boolean[] values = sub.getBooleanAttributes(MOIP_VVM_SYSTEM_ACTIVATED);
                
                if (values != null && values.length > 0) {
                    isVvmSystemActivated = values[0];
                }
            }
        }
        
        return isVvmSystemActivated;
    }

    private boolean getSubVvmFirstTimeActivatedFlag(String msid) {
        boolean isVvmFirstTimeActivatedFlag = false;
        
        DirectoryUpdater dirUpdater = DirectoryUpdater.getInstance();
        
        if (dirUpdater != null) {
            IDirectoryAccessSubscriber sub = dirUpdater.lookupSubscriber("msid:" + msid);
            
            if (sub != null) {
                boolean[] values = sub.getBooleanAttributes(MOIP_VVM_FIRST_TIME_ACTIVATED);
                
                if (values != null && values.length > 0) {
                    isVvmFirstTimeActivatedFlag = values[0];
                }
            }
        }
        
        return isVvmFirstTimeActivatedFlag;
    }
    
    
    /**
     * This callback gets called when the subscriber's VVM System Activated flag changes.
     * 
     * @param msid msid of the subscriber
     * @param vvmEvent VvmEvent
     * @param otherNotificationsWereSent False is NTF should send a fallback notification when System activated is set from true to false
     * @param oldSystemActivated boolean
     * @param newSystemActivatedFlag boolean
     */
    private void subVvmActivatedFlagWasModified(String msid, VvmEvent vvmEvent, boolean otherNotificationsWereSent,
            boolean oldSystemActivated, boolean newSystemActivatedFlag) {
        DesignSequenceDiagram.printFullSequence();
        
        if (oldSystemActivated == true && newSystemActivatedFlag == false) {
            log.debug("VVM System Activated flag was changed from " + oldSystemActivated + " to " + newSystemActivatedFlag
                    + " for msid " + msid + ". Original event: " + vvmEvent);
            
            if(vvmEvent == null) { 
                log.debug("VvmEvent is null, SMS notification will not be sent.");
                return;
            }
 
            /*
             * The original event that eventually led to the VVM system deactivation may not have been received by the
             * subscriber if he had no filters defined. Verify this and send him a fallback notification SMS if required.
             */
            if (!otherNotificationsWereSent) { 
                UserInfo userInfo = vvmEvent.getUserInfo();
                NotificationEmail email = vvmEvent.getNotificationEmail();
                NotificationGroup ng = new NotificationGroup(NtfMain.getEventHandler(), email, log, MerAgent.get());
                
                //FallbackUtil.doLevelTwoScheduledFallback(userInfo, ng);
                FallbackHandler.get().fallback(Constants.NTF_VVM, ng.getEmail().getNtfEvent());
                
            }

        }
    }
    
    public boolean isSimSwapConfigActive() {
        if(Config.getSimSwapTimeout().trim().equals("0")) {
            log.debug("VvmHandler:isSimSwapConfigActive = false");
            return false;
        }
        
        return true;
    }
    
}
