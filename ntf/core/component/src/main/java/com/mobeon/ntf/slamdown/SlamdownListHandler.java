/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.slamdown;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.provisioning.businessrule.ProvisioningConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager.FileStatusEnum;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.NotificationHandler;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * SlamdownHandler controls the reception, storing, formatting and sending of Slamdown/Mcn information.
 */
public class SlamdownListHandler implements com.mobeon.ntf.Constants {
	
    private LogAgent log;
    private static SlamdownListHandler _inst;
    private SlamdownWorker[] slamdownWorkers;
    private SlamdownEventHandler slamdownEventHandler;
    private SlamdownEventHandlerSmsUnit slamdownEventHandlerSmsUnit;
    private SlamdownEventHandlerSmsType0 slamdownEventHandlerSmsType0;
    private SlamdownEventHandlerSmsInfo slamdownEventHandlerSmsInfo;
    private ManagedArrayBlockingQueue<Object> workingQueue;
    private SlamdownPhoneOnSender slamdownPhoneOnSender;
    private SlamdownSender slamdownSender;
    private boolean isStarted=false;
    private MfsEventManager mfsEventManager;

    public SlamdownListHandler() {
        try {
            _inst = this;
            log = NtfCmnLogger.getLogAgent(SlamdownListHandler.class);

            // Create working queue            
            workingQueue = new ManagedArrayBlockingQueue<Object>(Config.getSlamdownQueueSize());
            
            if(mfsEventManager == null) {
                mfsEventManager = MfsEventFactory.getMfsEvenManager();
            }
            // Create Slamdown event handlers
            slamdownEventHandler = (SlamdownEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SLAMDOWN_L3.getName());
            slamdownEventHandlerSmsUnit = (SlamdownEventHandlerSmsUnit)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SLAMDOWN_SMS_UNIT.getName());
            slamdownEventHandlerSmsType0 = (SlamdownEventHandlerSmsType0)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SLAMDOWN_SMS_TYPE_0.getName());
            slamdownEventHandlerSmsInfo = (SlamdownEventHandlerSmsInfo)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SLAMDOWN_SMS_INFO.getName());


            // Create Slamdown workers
            int numberOfWorkers = Config.getSlamdownWorkers();
            if(numberOfWorkers>0)
            {
                slamdownPhoneOnSender = new SlamdownPhoneOnSender(workingQueue);
                slamdownSender = new SlamdownSender(workingQueue);
                createWorkers(numberOfWorkers);
                slamdownPhoneOnSender.start();
                isStarted=true;
            } else {
                log.warn("Slamdown service disable");
            }

        } catch (Exception e) {
            log.error("Unable to start slamdown service");
        }
    }

    public void releaseLockFile(SlamdownList sl) {
        log.debug("Releasing lock file for" + sl.getNotificationNumber());
        try {
            mfsEventManager.releaseLockFile(sl.getNotificationNumber(), SlamdownList.SLAMDOWNMCN_PHONE_ON_LOCK_FILE, sl.getPhoneOnLockId(), sl.getInternal());
        } catch (TrafficEventSenderException e) {
            log.warn("Exception trying to release lock file " + SlamdownList.SLAMDOWNMCN_PHONE_ON_LOCK_FILE + " for " + sl.getSubscriberNumber() + " : " + sl.getNotificationNumber(), e);
        }
    }

    public static SlamdownListHandler get() {
        return _inst;
    }

    public ManagedArrayBlockingQueue<Object> getWorkingQueue() {
        return workingQueue;
    }

    /**
     * Create the workers
     */
    private void createWorkers(int numberOfWorkers) {
        slamdownWorkers = new SlamdownWorker[numberOfWorkers];
        
        for (int i = 0; i<numberOfWorkers; i++) {
            slamdownWorkers[i] = new SlamdownWorker(workingQueue, "SlamdownWorker-" + i, this);
            slamdownWorkers[i].setDaemon(true);
            slamdownWorkers[i].start();
        }
    }
    
    /**
     * Handles incoming slamdown event
     * @param user UserInfo
     * @param ng Notification group
     * @param notificationType Determines if it's a subscriber (Slamdown) or Mcn (MIO subscriber (not VVS) or external)
     * @return number of SMS notification that will be sent
     */
    public int handleSlamdown(UserInfo user, NotificationGroup ng, int notificationType) {
        int count = 0;

        try {
            NtfEvent ntfEvent = ng.getEmail().getNtfEvent();
            String notificationNumber = null;
            int validityPeriod;
            String cosName;
            SlamdownList slamdownList = null;
            boolean internal = true;

            if (!isStarted) {
                log.error("Received Slamdown/Mcn but service is not started");
                ng.retry(user, NTF_SLAM, "Slamdown/Mcn service not started");
                return count;
            }

            // Ignore any events that are not related to Slamdown or Mcn
            if (!(ntfEvent.isEventServiceType(NtfEventTypes.SLAMDOWN.getName()) || ntfEvent.isEventServiceType(NtfEventTypes.MCN.getName()))) {
                log.debug("SlamdownListHandler ignoring none slamdown/mcn event " + ntfEvent.getReferenceId());

                // Inform the feedback handler for CDR generation
                ng.failed(user, NTF_SLAM, "Slamdown Level-2 Failed handleSlamdown() received an unknown type");
                return count;
            }
            
            log.debug("SlamdownListHandler handling: " + ntfEvent.getReferenceId() + " " + ntfEvent.getEventServiceName() +
            		  " notificationType: " + notificationType);

            // Obtain Slamdown/Mcn specific information
            if (notificationType==SlamdownList.NOTIFICATION_TYPE_SLAMDOWN) {
                // Slamdown, get the notification from NTF-Level-2 properties.
                notificationNumber = ntfEvent.getEventProperties().getProperty(MoipMessageEntities.SLAMDOWN_NOTIFICATION_NUMBER_PROPERTY);
                validityPeriod = user.getValidity_slamdown(); 
                cosName = user.getCosName();
            } else {
                // Mcn
                notificationNumber = ntfEvent.getRecipient();
                validityPeriod = Config.getValidity_mcn();
                if (Config.isMcnSubscribedEnabled()) {
                    cosName = user.getCosName();
                    if (cosName == null) {
                        // Note that not having a valid COS name may disable the MCN-Subscribed feature.
                        cosName = Constants.DUMMY_MCN_COS; // set a dummy cosname
                    }

                    if (user.hasMOIPCnService() && user.hasMcnSubscribedService()) {
                        String mcnState = user.getMcnSubscribedState();
                        if (mcnState != null && mcnState.equalsIgnoreCase(ProvisioningConstants.MCN_SUBSCRIBED_DISABLED)) {
                            /**
                             * MCN-Subscribed notification and the subscriber has disabled it.
                             * Do not send this notification.
                             */

                            ng.ok(user, Constants.NTF_SLAM);
                            return ++count;
                        }
                    }
                } else {
                    cosName = Constants.DUMMY_MCN_COS; // set a dummy cosname
                }
                if (notificationType == SlamdownList.NOTIFICATION_TYPE_MCN_EXTERNAL) {
                    internal=false; 
                }
                log.debug("MCN internal = " + internal);
            }

            // Check if a notification number has been found
            if (notificationNumber == null) {
                log.debug("SlamdownListHandler ignoring slamdown/mcn event, no notification number found " + ntfEvent.getReferenceId());

                // Inform the feedback handler for CDR generation
                ng.failed(user, NTF_SLAM, "Slamdown Level-2 Failed No notification number for slamdown/mcn notification");
                return count;
            }

            /**
             * Validate if the subscriber's storage is READ-ONLY (using the notification number).
             * Even if the event is expiry, let the scheduler retry so that the persistent file can be deleted properly.
             */
            if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(notificationNumber)) {
                log.warn("Storage currently not available for " + ntfEvent.getRecipient() + " : " + notificationNumber + ", will retry");
                ng.retry(user, NTF_SLAM, "Storage currently not available for " + ntfEvent.getRecipient() + ", will retry");
                return count;
            }

            // Level-2 expiry handling
            if (ntfEvent.isExpiry()) {
                if (ntfEvent.isEventServiceType(NtfEventTypes.SLAMDOWN.getName())) {
                    log.debug("SlamdownListHandler expired for Slamdown event " + ntfEvent.getReferenceId());
                } else {
                    log.debug("SlamdownListHandler expired for Mcn event " + ntfEvent.getReferenceId());
                }

                // Delete the slamdown file 
                NotificationHandler.deleteFiles(notificationNumber, SlamdownList.PATTERN_FILE_ALL);            

                // Inform the feedback handler for CDR generation
                ng.expired(user, NTF_SLAM, "Slamdown Level-2 Expiry");
                return count;
            }

            log.debug("ntfEvent properties: " + ntfEvent.getEventProperties().toString());
            log.debug("Create SlamdownList: recipient" + ntfEvent.getRecipient() + " notification Number " + notificationNumber );
            slamdownList = new SlamdownList(ntfEvent.getRecipient(), notificationNumber, user, validityPeriod, cosName, internal, notificationType);
            log.debug("slamdownList.retrieveSchedulerEventIdsPersistent()");
            slamdownList.retrieveSchedulerEventIdsPersistent();

            /**
             * Retrieving stored eventIds from status file (persistent storage).
             * Status file can be obsolete based on Config.getSlamdownMcnStatusFileValidityPeriod()
             * in which case obsolete eventIds still must be cancelled (scheduler cleanup)
             * and keep going with the business logic as if no eventIds were found.   
             */
            FileStatusEnum fileStatus = mfsEventManager.fileExistsValidation(notificationNumber, SlamdownList.SLAMDOWNMCN_STATUS_FILE, Config.getSlamdownMcnStatusFileValidityInMin(), internal);
            if (fileStatus.equals(FileStatusEnum.FILE_EXISTS_AND_INVALID)) {
                log.info("Obsolete slamdownmcn.status file (and eventIds) for " + ntfEvent.getRecipient() + " " + slamdownList.getSchedulerIds());

                /**
                 * Cancel obsolete eventIds
                 * 
                 */
                if (!slamdownEventHandler.cancelAllEvents(slamdownList, false)) {
                    String message = "Unable to cancel and update persistent storage for obsolete eventIds for " + ntfEvent.getRecipient() + ", will retry (level-2)";
                    log.warn("SlamdownListHandler:handleSlamdown: " + message);
                    ng.retry(slamdownList.getUserInfo(), Constants.NTF_SLAM, message);
                    return count;
                } else {
                    log.info("Obsolete eventIds cancelled for " + ntfEvent.getRecipient() + " " + slamdownList.getSchedulerIds());
                }
            }

            // Start SmsUnit or SmsInfo timer (depending if the PhoneOn feature is on/off from the configuration or the PhoneOnAlertSc)
            if (isPhoneOnFeatureEnabled(notificationType)) {
                log.debug("SlamdownListHandler:handleSlamdown: PhoneOn configuration is ON");

                if (!slamdownList.getSchedulerIds().isEmtpy()) {
                    if ((slamdownList.getSchedulerIds().getSmsUnitEventId() != null) && (slamdownList.getSchedulerIds().getSmsUnitEventId().length() > 0) ||
                        (slamdownList.getSchedulerIds().getSmsType0EventId() != null) && (slamdownList.getSchedulerIds().getSmsType0EventId().length() > 0)) {
                        /**
                         * Pending Slamdown/Mcn found on disk.
                         * A previous schedule event is already in place to handle a previous notification (found on disk).
                         * No need to perform action. The current notification will be aggregated to the previous one(s).
                         * It will inherit the remaining-scheduled time of the notification stored on disk.
                         */
                        log.info("SlamdownListHandler:handleSlamdown: This notification will be aggregated to the pending one");

                        ng.ok(slamdownList.getUserInfo(), Constants.NTF_SLAM);
                        return ++count;
                    } else {
                        String message = "These is currently a SMS-Info been sent out for " + ntfEvent.getRecipient() + ", will retry (level-2)";
                        log.info("SlamdownListHandler:handleSlamdown: " + message);
                        ng.retry(slamdownList.getUserInfo(), Constants.NTF_SLAM, message);
                        return count;
                    }
                } else {
                    log.debug("SlamdownListHandler:handleSlamdown: No pending Slamdown/Mcn notification for " + ntfEvent.getRecipient());

                    // Start SMS-Unit timer 
                    boolean successfullyScheduled = slamdownEventHandlerSmsUnit.scheduleSmsUnit(slamdownList);
                    if (!successfullyScheduled) {
                        String message = "Unable to schedule SmsUnit for " + ntfEvent.getRecipient() + ", will retry (level-2)";
                        log.info("SlamdownListHandler:handleSlamdown: " + message);
                        ng.retry(slamdownList.getUserInfo(), Constants.NTF_SLAM, message);
                        return count;
                    }
                }
            } else {
                log.debug("SlamdownListHandler:handleSlamdown: PhoneOn configuration is OFF.");

                /**
                 * Check if the subscriber had any events pending (SMS-Unit, SMS-PhoneOn or SMS-Info).
                 * The SMS-Unit and SMS-PhoneOn check is in the eventuality of having a NTF configuration
                 * without PhoneOn configured while, previously, was configured.
                 */
                if (!slamdownList.getSchedulerIds().isEmtpy()) {
                    // Check if the subscriber had either a SMS-Unit or SMS-PhoneOn event pending (even though PhoneOn feature is not configured) 
                    if ((slamdownList.getSchedulerIds().getSmsUnitEventId() != null) && (slamdownList.getSchedulerIds().getSmsUnitEventId().length() > 0) ||
                        (slamdownList.getSchedulerIds().getSmsType0EventId() != null) && (slamdownList.getSchedulerIds().getSmsType0EventId().length() > 0)) {
                        /**
                         * Specific case of a Slamdown/Mcn notification where PhoneOn configuration is OFF but subscriber still have
                         * pending PhoneOn events found on disk, cancel these events.
                         */
                        log.info("SlamdownListHandler:handleSlamdown: " + ntfEvent.getRecipient() + " still have pending SMS-Unit/SMS-PhoneOn event(s), cancel obsoleted event(s)");

                        /**
                         * Cancel SmsType0 timer.
                         * Force the cancellation even if there is an error writing on disk. 
                         */
                        slamdownEventHandler.cancelSmsType0Event(slamdownList, true);
                    }

                    // This is not a if-else statement since subscriber could have 3 eventIds scheduled.
                    if (slamdownList.getSchedulerIds().getSmsInfoEventId() != null && slamdownList.getSchedulerIds().getSmsInfoEventId().length() > 0) {
                        String message = "These is currently a SMS-Info been sent out for " + ntfEvent.getRecipient() + ", will retry (level-2)";
                        log.info("SlamdownListHandler:handleSlamdown: " + message);

                        ng.retry(slamdownList.getUserInfo(), Constants.NTF_SLAM, message);
                        return count;
                    }
                }

                // PhoneOn configuration is OFF, skip the Phone-On sequence and go directly to the SMS-Info (send the Slamdown notification)
                slamdownList.setCurrentState(SlamdownList.STATE_SENDING_INFO);
                slamdownList.setCurrentEvent(SlamdownList.EVENT_NEW_NOTIF_WITHOUT_PHONE_ON_REQ);
                boolean successfullyScheduled = slamdownEventHandlerSmsInfo.scheduleSmsInfo(slamdownList);
                if (!successfullyScheduled) {
                    String message = "SmsInfo not successfully scheduled while receiving a new Slamdown notification for " + ntfEvent.getRecipient() + ", will retry (level-2)";
                    log.warn("SlamdownListHandler:handleSlamdown: " + message);
                    ng.retry(slamdownList.getUserInfo(), Constants.NTF_SLAM, message);
                    return count;
                }
            }

            // Put in queue
            boolean storedInQueue = workingQueue.offer(slamdownList);
            if (storedInQueue) {
                // Stop level-2 scheduling
                ng.ok(slamdownList.getUserInfo(), (slamdownList.getInternal() ? Constants.NTF_SLAM : Constants.NTF_MCNNOTIF));
                count++;
            } else {
                String message = "SlamdownWorkingQueue full while receiving a new Slamdown notification for " + slamdownList.getSubscriberNumber() + ", will retry (level-2)";
                log.warn(message);
                ng.retry(slamdownList.getUserInfo(), Constants.NTF_SLAM, message);
                return count;
            }
        } catch (Exception e) {
            String message = "Slamdown exception for " + user.getTelephoneNumber();
            log.error(message, e);
            ng.failed(user, Constants.NTF_SLAM, message);
        }

        return count;
    }

    /**
     * handleAggregate
     * @param slamdownList SlamdownList
     */
    public void handleAggregate(SlamdownList slamdownList) {
/*        
        // Aggregating state: Must be introduced in a second phase

        // New Slamdown notification received from MAS
        switch (slamdownList.getCurrentState()) {
            case SlamdownList.STATE_IDLE:
                
                // Store event in MFS
                storeEventsToMfs()
                
                // Schedule aggregation expiry timer
                slamdownList.setCurrentState(SlamdownList.STATE_AGGREGATING);
                slamdownEventHandlerAggregate.scheduleAggregate(slamdownList);        
                break;
                
            case slamdownList.STATE_AGGREGATING:
            case slamdownList.STATE_WAITING_PHONE_ON:
                // Store event in MFS
                int count = storeEventsToMfs();

                //If the count reaches the configured threshold number, trigger SMS-Type-0 and cancel aggregation timer.
                //Otherwise, do not perform anything else than storing in MFS, the already-scheduled-timer will expire eventually.
                if (count >= 3) {
                    // Send SMS-Type-0 request
                    slamdownList.setCurrentState(slamdownList.STATE_WAITING_PHONE_ON);
                    phoneOnSender.sendPhoneOnRequest(slamdownList);
                    
                    // Cancel aggregation timer
                    slamdownEventHandler.cancelStoredEvent(slamdownList.getReferenceId());        
                }
                break;
                
            case slamdownList.STATE_SENDING_INFO:
                // Must start a new aggregation timer since too late for the current iteration
                // special case here because we already have one iteration on-going
                // slamdownEventHandlerAggregate.scheduleAggregate(slamdownEvent);        
                break;
                
            default:
                log.error("SlamdownListHandler:handleAggregate: Invalid state " + slamdownEvent.getCurrentState());
        }
*/
    }

    /**
     * handleSmsType0
     * @param slamdownList SlamdownList
     * @return boolean True if the SmsUnit has been successfully scheduled and stored persistently, false otherwise
     */
    public boolean handleSmsType0(SlamdownList slamdownList) {

        // Update slamdownList 
        slamdownList.setCurrentState(SlamdownList.STATE_SENDING_UNIT);

        // Start SMS-Unit timer
        boolean successfullyScheduled = slamdownEventHandlerSmsUnit.scheduleSmsUnit(slamdownList);
        if (!successfullyScheduled) {
            log.warn("Unable to schedule SmsInfo for " + slamdownList.getSubscriberNumber() + ", will retry");
            return false;
        }

        // Send PhoneOn request        
        slamdownPhoneOnSender.sendPhoneOnRequest(slamdownList);
        return true;
    }

    /**
     * handleSmsType0NoScheduling
     * @param slamdownList SlamdownList
     */
    public void handleSmsType0NoScheduling(SlamdownList slamdownList) {

        // SMS-Unit timer already started either at startup (handleSlamdown()) or by SlamdownEventHandler (retry).

        // Send PhoneOn request        
        slamdownPhoneOnSender.sendPhoneOnRequest(slamdownList);
    }

    /**
     * The purpose of this method is to update the scheduler in order to switch
     * from SMS-Type-0 SmsUnit (period waiting from SmsUnit to process the PhoneOn request, then retry)
     * to SMS-Type-0 ValidityTime (period waiting from SMSc to try to get an answer for PhoneOn request, then retry).
     * 
     * No action on the state machine needs to be performed.
     * @param slamdownList SlamdownList
     */
    public boolean handleSmsType0ValidityTimer(SlamdownList slamdownList) {
        boolean result = true;
        
        if (slamdownList.getCurrentEvent() == SlamdownList.EVENT_SMS_INFO_RESPONSE_SUCCESSFUL) {
            log.debug("SlamdownListHanlder: handleSmsType0ValidityTimer() already received EVENT_SMS_INFO_RESPONSE_SUCCESSFUL, discard.");
            return result;
        }

        // Update slamdownList 
        slamdownList.setCurrentState(SlamdownList.STATE_WAITING_PHONE_ON);

        // Start SMS-Type-0 validity timer
        return slamdownEventHandlerSmsType0.scheduleSmsType0(slamdownList);
    }

    /**
     * handleSmsInfo
     * @param slamdownList SlamdownList
     * @return boolean True if SmsInfo has been successfully scheduled and stored, false otherwise
     */
    public boolean handleSmsInfo(SlamdownList slamdownList) {

        // Update slamdownList 
        slamdownList.setCurrentState(SlamdownList.STATE_SENDING_INFO);

        // Start SMS-Info timer
        boolean successfullyScheduled = slamdownEventHandlerSmsInfo.scheduleSmsInfo(slamdownList);
        if (!successfullyScheduled) {
            log.warn("Unable to schedule SmsInfo for " + slamdownList.getSubscriberNumber() + ", will retry");
            return false;
        }

        // Send SMS-Info (slamdown notification)
        slamdownSender.send(slamdownList);
        return true;
    }

    /**
     * handleSmsInfoNoScheduling
     * This method is used when PhoneOn configuration is off.
     * Since initial handleSlamdown method started the scheduler event already,
     * only the SmsInfo must be sent out.
     * @param slamdownList SlamdownList
     */
    public void handleSmsInfoNoScheduling(SlamdownList slamdownList) {
        // Send SMS-Info (slamdown notification)
        slamdownList.setCurrentState(SlamdownList.STATE_SENDING_INFO);
        slamdownSender.send(slamdownList);
    }

    /**
     * Check if PhoneOn feature is enabled
     * @param notificationType NotificationType
     * @return boolean 'true' if the PhoneOn feature must be used (either SMS-Type-0 or HLR-AlertSc), 'false' otherwise.
     */
    public static boolean isPhoneOnFeatureEnabled(int notificationType) {
        boolean slamdownNotification = (notificationType == SlamdownList.NOTIFICATION_TYPE_SLAMDOWN);
        boolean doPhoneOnSmsType0 = slamdownNotification ? Config.getDoSmsType0Slamdown() : Config.getDoSmsType0Mcn();
        boolean doPhoneOnAlertSc = Config.getPhoneOnMethod().equalsIgnoreCase(Config.PHONE_ON_ALERT_SC);
        return doPhoneOnSmsType0 || doPhoneOnAlertSc; 
    }

    /**
     * Profiling method 
     * @param checkPoint CheckPoint
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
        return  isStarted;
    }
}
