/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule;

import java.util.HashMap;
import java.util.Map;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.NotifierDatabaseException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.ANotifierEventHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.INotifierEventInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.INotifierEventScheduler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.INotifierMessageGenerator;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierProfiler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierUtil;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.NotifierHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.NotifierHelper;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateSmsPlugin;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.NotifierTypeEvent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.NotifierTypeState;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierMdr;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierMdr.NotifierMdrAction;

/**
 * The NotifierEventHandler class handles the scheduling of events and 
 * the call back when a scheduled event is fired.
 */
public class NotifierEventHandler extends ANotifierEventHandler {

    private static INotifierLogger log = TemplateSmsPlugin.getLoggerFactory().getLogger(NotifierEventHandler.class);

    private static NotifierEventHandler _inst = null;
    
    private static Map<String,String> scheduleTypes = new HashMap<String,String>();

    private INotifierUtil notifierUtil = TemplateSmsPlugin.getUtil();
    private INotifierEventScheduler notifierEventScheduler = null;
    private INotifierProfiler notifierProfiler = TemplateSmsPlugin.getProfiler();
    private INotifierMessageGenerator notifierMessageGenerator = TemplateSmsPlugin.getMessageGenerator();
    
    private NotifierEventHandler(INotifierEventScheduler eventScheduler) {
        initScheduling(eventScheduler);
    }

    private void initScheduling(INotifierEventScheduler eventScheduler) {
        notifierEventScheduler = eventScheduler;
    }
    
    public void addnewSchedule(TemplateType notifierType) {
        
        String typeName = notifierType.getCphrTypeName();
        
        if (scheduleTypes.containsKey(typeName.toLowerCase())) {
            //already added.
            return;
        }
        scheduleTypes.put(typeName.toLowerCase(),typeName);
        
        //Scheduler Schema initial
        String serviceName =  typeName + TemplateType.SERVICE_NAME_SEPARATOR + NotifierTypeState.STATE_INITIAL.getName();
        notifierEventScheduler.registerEventService(serviceName, notifierType.getSchedulerInitialSchema(), this);

        // Scheduler schema - Sending
        serviceName =  typeName + TemplateType.SERVICE_NAME_SEPARATOR + NotifierTypeState.STATE_SENDING.getName();
        notifierEventScheduler.registerEventService(serviceName, notifierType.getSchedulerSendingSchema(), this);
        
        //if put phone on later add here...
    }
    
    public static NotifierEventHandler get() {
        if(_inst == null) {
            _inst = new NotifierEventHandler(TemplateSmsPlugin.getEventScheduler());
        }
        return _inst;
    }

    public boolean scheduleInitial(NotifierEvent notifierEvent) {
        boolean result = true;
        String notifierTypeName = notifierEvent.getNotifierTypeName();
        String notifierTypeState = NotifierTypeState.STATE_INITIAL.getName();
        String serviceName =  notifierTypeName + TemplateType.SERVICE_NAME_SEPARATOR + notifierTypeState;

        // Retrieve the current eventId & eventIdWaitingPhoneOn stored
        String previousEventId = notifierEvent.getSchedulerIds().getEventId();
        NotifierTypeState previousState = notifierEvent.getNotifierTypeState();

        // New state
        notifierEvent.setNotifierTypeState(NotifierTypeState.STATE_INITIAL);

        // Schedule event
        INotifierEventInfo eventInfo = 
                notifierEventScheduler.scheduleEvent(serviceName, 
                                                     notifierEvent.getNotificationNumber() + notifierUtil.getUniqueEventSchedulingId(), 
                                                     notifierEvent.getEventProperties());

        if(eventInfo == null) {
            log.error("Unable to schedule event for service " + serviceName);
            notifierEvent.setNotifierTypeState(previousState);
            return false;
        }

        // Store the eventIds + persistent
        notifierEvent.getSchedulerIds().setEventId(eventInfo.getEventId());
        boolean successfullyUpdated = notifierEvent.updateEventIdsPersistent();
        if (!successfullyUpdated) {
            log.debug(serviceName + " scheduled event will be cancelled (unable to store persistently): " + eventInfo.getEventId());
            notifierEventScheduler.cancelEvent(eventInfo.getEventId());

            notifierEvent.getSchedulerIds().setEventId(previousEventId);
            notifierEvent.setNotifierTypeState(previousState);
            result = false;
        } else {
            log.debug(serviceName + " scheduled event: " + eventInfo.getEventId());

            notifierEventScheduler.cancelEvent(previousEventId);
        }

        return result;
    }

        public boolean scheduleNextNotif(NotifierEvent notifierEvent, NotifierEvent nextNotifierEvent) {
        boolean result = true;

        // Set notifierTypeName, State and serviceName according to NEXT notif to be processed
        String notifierTypeName = nextNotifierEvent.getNotifierTypeName();
        NotifierTypeState notifierTypeState = null;
        String notifierTypeStateName = null;
        
        notifierTypeState = NotifierTypeState.STATE_SENDING;
        notifierTypeStateName = NotifierTypeState.STATE_SENDING.getName();
        
        String serviceName = notifierTypeName + TemplateType.SERVICE_NAME_SEPARATOR + notifierTypeStateName;

        // New state
        nextNotifierEvent.setNotifierTypeState(notifierTypeState);

        INotifierEventInfo eventInfo = null;

        // Schedule Sending event
         eventInfo = notifierEventScheduler.scheduleEvent(serviceName,
                    nextNotifierEvent.getNotificationNumber() + notifierUtil.getUniqueEventSchedulingId(),
                    nextNotifierEvent.getEventProperties());
        
        if(eventInfo == null) {
            log.error("Unable to schedule event for service " + serviceName);
            // no need to 'restore' the 'old' notifierEvent since we didn't modify it
            return false;
        }

        // Store the eventId + persistent (this will overwrite the previousEventId and previousEventIdWaitingPhoneOn stored (if any)
        nextNotifierEvent.getSchedulerIds().setEventId(eventInfo.getNextEventId());
        boolean successfullyUpdated = nextNotifierEvent.updateEventIdsPersistent();
        if (!successfullyUpdated) {
            log.debug(serviceName + " scheduled event will be cancelled (unable to store persistently): " + eventInfo.getEventId());
            notifierEventScheduler.cancelEvent(eventInfo.getEventId());
            
            // no need to 'restore' the 'old' notifierEvent since we didn't modify it
            // (as we tried to 'commit' the newly created and modified nextNotifierEvent)
            result = false;
        } else {
            log.debug(serviceName + " scheduled event: " + eventInfo.getEventId());

            // cancel event id and wait phone on event id
            notifierEventScheduler.cancelEvent(notifierEvent.getSchedulerIds().getEventId());
            notifierEvent.getSchedulerIds().setEventId(null);
        }

        return result;
    }
    

    public boolean scheduleSending(NotifierEvent notifierEvent) {
        boolean result = true;
        String eventId = notifierEvent.getSchedulerIds().getEventId();
        String notifierTypeName = notifierEvent.getNotifierTypeName();
        String notifierTypeState = NotifierTypeState.STATE_SENDING.getName();
        String serviceName = notifierTypeName + TemplateType.SERVICE_NAME_SEPARATOR + notifierTypeState;
        

        // In order to keep the retry count, this plug-in does not re-schedule another Sending but keep the current one if present.
        if (eventId != null && !eventId.isEmpty()) {
            NotifierTypeState nts = NotifierHelper.getNotifierTypeStateFromEventId(eventId);
            if (NotifierTypeState.STATE_SENDING.equals(nts)) {
                log.debug(serviceName + " event already scheduled: " + eventId);
                notifierEvent.updateEventIdsPersistent();
                return true;
            }
        }

        // Retrieve the current values stored
        String previousEventId = notifierEvent.getSchedulerIds().getEventId();
        NotifierTypeState previousState = notifierEvent.getNotifierTypeState();
        
        // New state
        notifierEvent.setNotifierTypeState(NotifierTypeState.STATE_SENDING);

        // Schedule event
        INotifierEventInfo eventInfo = 
                notifierEventScheduler.scheduleEvent(serviceName, 
                                                     notifierEvent.getNotificationNumber() + notifierUtil.getUniqueEventSchedulingId(), 
                                                     notifierEvent.getEventProperties());

        if(eventInfo == null) {
            log.error("Unable to schedule event for service " + serviceName);
            notifierEvent.setNotifierTypeState(previousState);
            return false;
        }

        // Store the eventId + persistent (this will overwrite the previousEventId and previousEventIdWaitingPhoneOn stored (if any)
        notifierEvent.getSchedulerIds().setEventId(eventInfo.getEventId());
        boolean successfullyUpdated = notifierEvent.updateEventIdsPersistent();
        if (!successfullyUpdated) {
            log.debug(serviceName + " scheduled event will be cancelled (unable to store persistently): " + eventInfo.getEventId());
            notifierEventScheduler.cancelEvent(eventInfo.getEventId());

            notifierEvent.getSchedulerIds().setEventId(previousEventId);
            notifierEvent.setNotifierTypeState(previousState);
            result = false;
        } else {
            log.debug(serviceName + " scheduled event: " + eventInfo.getEventId());

            notifierEventScheduler.cancelEvent(previousEventId);
        }

        return result;
    }


    @Override
    public int eventFired(INotifierEventInfo eventInfo) {
        
        int result = NOTIFIER_EVENT_HANDLE_RESULT_OK;
        NotifierEvent notifierEvent = null;
        notifierEvent = new NotifierEvent(NotifierHelper.getNotifierType(eventInfo.getEventProperties()), eventInfo.getEventProperties());
        String profilerText = "Retry";
        int profilerNumberOfTries = eventInfo.getNumberOfTried();
        
        //check we have this type registered.  If not register it.
        //this is because a default type could have been registered by another NTF instance or we could have restarted.
        String notifierName = notifierEvent.getNotifierTypeName();
        if (!TemplateType.isTemplateTypeDefined(notifierName)) {
        	if (doesCphrTemplateExist(notifierName)) {        	
        		TemplateType.addTemplateTypeFromDefault(notifierName);
        	}
        }

        try {
            boolean storedInQueue = false;

            log.debug("EventFired: " + notifierEvent.getIdentity()+ " " + eventInfo.getEventType());

            if (!NotifierHandler.get().isStarted()) {
                log.error("Received Notifier event but service is not available");
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.5.NEH.NotStarted." + notifierEvent.getNotifierTypeName() + ".#" + profilerNumberOfTries);
                }
                
                return NOTIFIER_EVENT_HANDLE_RESULT_OK;
            }

            ANotifierDatabaseSubscriberProfile subscriberProfile = null; 
            try {
                subscriberProfile = NotifierHelper.getSubscriberProfile(notifierEvent.getReceiverNumber(), notifierEvent.getNotifierTypeName(), notifierEvent.getEventProperties());
            } catch (NotifierDatabaseException nde) {
                log.error("NotifierDatabaseException while looking up subscriber, will retry: " + nde.getMessage());
                return NOTIFIER_EVENT_HANDLE_RESULT_OK;
            }

            if(subscriberProfile == null) {
                log.error("Received event but subscriber " + notifierEvent.getReceiverNumber() + " not found in database, stopping retries");
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.5.NEH.SubP.Null." + notifierEvent.getNotifierTypeName() + ".#" + profilerNumberOfTries);
                }
                return NOTIFIER_EVENT_HANDLE_RESULT_STOP_RETRIES;
            }
            notifierEvent.setSubscriberProfile(subscriberProfile);
            
            // Validate if the subscriber's storage is READ-ONLY (using the notification number)
            if (!notifierUtil.isFileStorageOperationsAvailable(notifierEvent.getNotificationNumber())) {
                log.warn("Storage currently not available to process Notifier event for " + notifierEvent.getIdentity() + ", will retry");
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.5.NEH.ReadOnly." + notifierEvent.getNotifierTypeName() + ".#" + profilerNumberOfTries);
                }

                return NOTIFIER_EVENT_HANDLE_RESULT_OK;
            }

            boolean retrieveSchedulerEventIds = false;
            retrieveSchedulerEventIds = notifierEvent.retrieveSchedulerEventIdsPersistent();

            if (!retrieveSchedulerEventIds) {
                log.debug("No persistent storage found for " + notifierEvent.getIdentity() + " for event: " + eventInfo.getEventId());
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.5.NEH.CannotRetrieve." + notifierEvent.getNotifierTypeName() + ".#" + profilerNumberOfTries);
                }                
                return NOTIFIER_EVENT_HANDLE_RESULT_STOP_RETRIES;
            }

            /**
             * When retrieving eventIds, the validity period of the file is not considered for fired events.
             * (as opposed to new notification) since there is payload information to handle, inject the event.
             */

            /**
             * Validate that the eventId stored in storage and that the fired eventId match.
             * If it's not the case, that means that an NTF from an other site already handled the notification.
             * In that case, just cancel the scheduler.
             */
            if (!shouldProcessFiredEvent(eventInfo, notifierEvent)) {
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.5.NEH.ShouldNotProcess." + notifierEvent.getNotifierTypeName() + ".#" + profilerNumberOfTries);
                }            	
                return NOTIFIER_EVENT_HANDLE_RESULT_STOP_RETRIES;
            }


            /**
             * Validate if its an expiry event notification
             * Even if the case of an expired event, NOTIFIER_EVENT_HANDLE_RESULT_OK MUST be returned
             * (so that the Scheduler will retry this expired event) instead of NOTIFIER_EVENT_HANDLE_RESULT_STOP_RETRIES
             * since the worker thread that will process this event might die/not process it.
             * If the worker thread process the event successfully, then it MUST cancel the next eventId.
             */
            if (!eventInfo.isNextRetryScheduled() || eventInfo.isExpire() || eventInfo.isLastExpire()) {
                log.debug("EventFired: Expiry event: " + eventInfo.getEventId());
                // Event
                notifierEvent.setNotifierTypeEvent(NotifierTypeEvent.EVENT_SCHEDULER_EXPIRY);
            } else {
                notifierEvent.setNotifierTypeEvent(NotifierTypeEvent.EVENT_SCHEDULER_RETRY);
            }
            
            //set to nextId so when cancelled will cancel correct ID.
            String nextId = eventInfo.getNextEventId();            
            notifierEvent.updateEventId(nextId);

            storedInQueue = NotifierHandler.get().getWorkingQueue().offer(notifierEvent);
            if (storedInQueue) {
                log.debug("EventFired: Stored in workingQueue : " + notifierEvent.getIdentity());
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.5.NEH." + profilerText + ".Queued." + notifierEvent.getNotifierTypeName() + ".#" + profilerNumberOfTries);
                }
            } else {
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.5.NEH." + profilerText + ".NotQueued" + notifierEvent.getNotifierTypeName() + ".#" + profilerNumberOfTries);
                }
                log.warn("EventFired: Not stored in workingQueue (full), will retry : " + notifierEvent.getIdentity());
            }

        } catch (Exception e) {
            String message = "Event fired exception for " + eventInfo.getEventId();
            if (eventInfo.isNextRetryScheduled()) {
                log.warn(message + ", will retry. ", e);
            } else {
                log.error(message + ", will not retry. ", e);
                NotifierMdr.get().generateMdr(notifierEvent, NotifierMdrAction.FAILED);
            }
            if (notifierProfiler.isProfilerEnabled()) {
                NotifierHelper.profilerCheckPoint("NTF.TNP.5.NEH.Excep." + notifierEvent.getNotifierTypeName() + ".#" + profilerNumberOfTries);
            }
        }
        return result;
    }

    /**
     * Compare the received eventId with the persistent eventId stored for the given subscriber.
     * If the two eventIds match, this means that the event fired is the one the plug-in is expecting.
     * If not, it means that this plug-in received an old eventId.
     * 
     * @param firedEventInfo AppliEventInfo
     * @param notifierEvent NotifierEvent
     * @return true if the event should be processed, false otherwise.
     */
    private boolean shouldProcessFiredEvent(INotifierEventInfo firedEventInfo, NotifierEvent notifierEvent) {
        boolean shouldProcessFiredEvent = false;
        String storedEvent = null;

        String state = getStateFromEventInfo(firedEventInfo);
        if (state == null) {
            log.warn("Invalid event id for event " + firedEventInfo);
            return false;
        }

        if (state.equalsIgnoreCase(NotifierTypeState.STATE_INITIAL.getName())) {
            storedEvent = notifierEvent.getSchedulerIds().getEventId();
            notifierEvent.setNotifierTypeState(NotifierTypeState.STATE_INITIAL);
        } else if (state.equalsIgnoreCase(NotifierTypeState.STATE_SENDING.getName())) {
            storedEvent = notifierEvent.getSchedulerIds().getEventId();
            notifierEvent.setNotifierTypeState(NotifierTypeState.STATE_SENDING);
        } else {
            log.debug("Invalid eventInfo received: " + firedEventInfo.getEventId());
        }

        shouldProcessFiredEvent = firedEventInfo.isEventIdsMatching(storedEvent);
        if (!shouldProcessFiredEvent) {
            log.info("EventFired: EventIds not matching: firedEvent: " + firedEventInfo.getEventId() + ", storedEvent: " + storedEvent + ", stop retry");
        }

        return shouldProcessFiredEvent;
    }

    private String getStateFromEventInfo(INotifierEventInfo firedEventInfo) {
        String eventId = firedEventInfo.getEventId();
        if (eventId == null || eventId.isEmpty()) {
            log.warn("Invalid event id for event " + firedEventInfo);
            return null;
        }

        String serviceName = notifierUtil.getEventServiceName(eventId);
        if(serviceName == null) {
            log.warn("Invalid event id for event " + firedEventInfo);
            return null;
        }

        return serviceName.substring(serviceName.indexOf(TemplateType.SERVICE_NAME_SEPARATOR)+1);
    }
    
    private boolean doesCphrTemplateExist(String receivedEventType) {
    	return notifierMessageGenerator.doesCphrTemplateExist(receivedEventType);
	}

    /**
     * This method TRIES to cancel the SmsInfo eventId by updating the persistent storage and invoking the scheduler.cancel.
     * In the plausible case of being unable to update the persistent storage (because of an I/O Exception for example),
     * the method will return false - which would lead the clients of this method not to go forward with their next
     * operation and, instead, wait for a retry of this eventId (by the scheduler).
     * 
     * In the case of a client already backed-up by another eventId type, the client COULD decide to cancel the SmsInfo
     * eventId anyway regardless if the persistent storage operation is successful or not.
     * This operation MUST be considered as a 'best effort' since the eventId COULD be stored on another site
     * (in a Geo-Distributed solution) and therefore, not be cancelled successfully.
     *  
     * In the particular case of cancelling all the events, all the clients SHOULD try to cancel the eventIds anyway
     * since this method is usually used at the end of a Notifier notification process or when a faulty operation
     * has been encountered.
     *  
     * @param notifierEvent NotifierEvent
     * @param tryToCancelAnyway True if the cancel operation SHOULD be tried even if the persistent storage update is not successful 
     * @return True if the cancel operation is successful, false otherwise
     */
    public boolean cancelAllEvents(NotifierEvent notifierEvent, boolean tryToCancelAnyway) {

        if (notifierEvent.getSchedulerIds().isEmtpy()) {
            return true;
        }

        boolean result = true;
        String eventId = notifierEvent.getSchedulerIds().getEventId();

        // First step is to nullify the values on disk
        notifierEvent.getSchedulerIds().nullify();

        boolean successfullyUpdated = notifierEvent.updateEventIdsPersistent();

        /**
         * If the update of the persistent storage is successful, cancel the events.
         * If not, do not cancel and let the scheduler retry later.
         */
        if (successfullyUpdated || tryToCancelAnyway) {
            // Second step is to cancel the scheduled events
            if (eventId != null && !eventId.isEmpty()) {
                log.debug("NotifierEventHandler cancel eventId_initial event: " + eventId);
                notifierEventScheduler.cancelEvent(eventId);
            }

        } else {
            log.warn("Unable to cancel all events and update persistent storage for " + notifierEvent.getIdentity());

            // Revert back to the original eventIds
            notifierEvent.getSchedulerIds().setEventId(eventId);
            result = false;
        }
        return result;
    }

    public boolean cancelEventId(NotifierEvent notifierEvent, boolean tryToCancelAnyway) {
        boolean result = true;

        String eventId = notifierEvent.getSchedulerIds().getEventId();

        // First step is to nullify the values on disk
        notifierEvent.getSchedulerIds().setEventId(null);
        boolean successfullyUpdated = notifierEvent.updateEventIdsPersistent();

        /**
         * If the update of the persistent storage is successful, cancel the events.
         * If not, do not cancel and let the scheduler retry later.
         */
        if (successfullyUpdated || tryToCancelAnyway) {
            // Second step is to cancel the scheduled events
            if (eventId != null && !eventId.isEmpty()) {
                log.debug("NotifierEventHandler cancel eventId: " + eventId);
                notifierEventScheduler.cancelEvent(eventId);
            }

        } else {
            log.warn("Unable to cancel eventId and update persistent storage for " + notifierEvent.getIdentity());

            // Revert back to the original eventIds
            notifierEvent.getSchedulerIds().setEventId(eventId);

            result = false;
        }
        return result;
    }

}
