/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.sip;

import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.SipMwiEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.event.SipMwiEvent;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.util.threads.NtfThread;
import com.abcxyz.messaging.common.message.MSA;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;

/**
 * Does the synchronization of a SIP MWI notification.
 * The workers get their workloads via an ManagedArrayBlockingQueue.
 */
public class SIPWorker extends NtfThread {

    private static LogAgent log = NtfCmnLogger.getLogAgent(SIPWorker.class);
    private ManagedArrayBlockingQueue<Object> queue;
    private SipMwiCallSpec caller;
    private SIPCallListener callListener;
    private boolean merNotification = true;
    private MerAgent mer;
    private SipMwiEventHandler sipMwiEventHandler;

    /**
     * Constructor
     * @param queue ManagedArrayBlockingQueue
     * @param caller SipMwiCallSpec
     * @param threadName ThreadName
     * @param merNotification Mer notify
     */
    public SIPWorker(ManagedArrayBlockingQueue<Object> queue,
                     SipMwiCallSpec caller,
                     String threadName,
                     boolean merNotification)
    {
        super(threadName);
        this.queue = queue;
        this.caller = caller;
        this.callListener = new SIPWorker.WorkerCallListener();
        this.merNotification = merNotification;
        this.mer = MerAgent.get();
        sipMwiEventHandler = (SipMwiEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SIPMWI.getName());
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
     * Do one step of the work.
     * @return False if the work should continue, true if the worker wants to stop.
     */
    public boolean ntfRun()
    {
        SipMwiEvent sipMwiEvent = null;
        UserInfo userInfo = null;

        try {
            // Get an event from the working queue
            Object obj = queue.poll(10, TimeUnit.SECONDS); //timeout to check management status
            if (obj == null) {return false;}

            sipMwiEvent = (SipMwiEvent)obj;
            if ( log.isDebugEnabled() ) {
                log.debug("Handle new event in SIP MWI worker for " + sipMwiEvent.getIdentity());
            }

            userInfo = getUserInfo(sipMwiEvent);
            if (userInfo == null) {
                log.error("No Userinfo found for SipMwiEvent, stops : " + sipMwiEvent.getNotificationNumber());

                // Cancel any pending notification, reminder notification and reminder trigger notification
                sipMwiEventHandler.retrieveSchedulerEventIdsPersistent(sipMwiEvent);
                if (!sipMwiEventHandler.cancelAllEvents(sipMwiEvent, false)) {
                    log.warn("Unable to cancel all events and update persistent storage for " + sipMwiEvent.getIdentity() + ", will retry");
                }
                return false;
            }

            switch (sipMwiEvent.getCurrentEvent()) {
            case SipMwiEvent.SIPMWI_EVENT_EXPIRED:
                // The SipMwi notification retry exhausted all the retries.
                // Now trigger the fallback notification
                
                if ( log.isDebugEnabled() ) {
                    log.debug("Checking if should fallback for " + sipMwiEvent.getIdentity());
                }

                if (sipMwiEvent.isEventServiceType(NtfEventTypes.SIPMWI.getName())) {
                    // This event is either a notification or a reminder notification
                    if ( log.isDebugEnabled() ) {
                        log.debug("SipMwi notification event expired: " + sipMwiEvent.getReferenceId());
                    }

                    if (merNotification) {
                        mer.notificationExpired(sipMwiEvent.getNotificationNumber(), Constants.NTF_SIPMWI);
                    }

                    if (sipMwiEvent.isReminder()) {
                        /**
                         * Since the subscriber has not been notified successfully (in this case, sipmwi expired event),
                         * no need to re-schedule the reminder trigger since the current one respects the reminder schema.
                         */
                        if ( log.isDebugEnabled() ) {
                            log.debug("SipWorker reminder notification event expired: " + sipMwiEvent.getReferenceId());
                        }
                    } else {
                        // A SipMwi notification expired
                        if ( log.isDebugEnabled() ) {
                            log.debug("SipWorker notification event expired: " + sipMwiEvent.getReferenceId());
                        }

                        // Scheduling a reminder notification trigger.
                        if (!sipMwiEventHandler.scheduleReminderTriggerBackup(sipMwiEvent)) {
                            log.warn("Unable to update persistent storage (scheduling reminder) for " + sipMwiEvent.getIdentity() + ", will retry");
                            return false;
                        }
                    }

                    // Cancel the expiry retry
                    if (!sipMwiEventHandler.cancelEvent(sipMwiEvent)) {
                        log.warn("Unable update persistent storage (cancelling retry) for " + sipMwiEvent.getIdentity() + ", will retry");
                        return false;
                    }
                    FallbackHandler.get().fallback(Constants.NTF_SIPMWI, sipMwiEvent);

                } else {
                    // This event is a reminder trigger notification
                    if ( log.isDebugEnabled() ) {
                        log.debug("SipMwi reminder trigger notification event expired: " + sipMwiEvent.getReminderTriggerReferenceId());
                    }

                    /**
                     * Expiry of Reminder trigger, in this case, the reminder trigger must be cancelled
                     * as well as the referenceId since there might be a pending notification retry going on.
                     */
                    if (!sipMwiEventHandler.cancelAllEvents(sipMwiEvent, false)) {
                        log.warn("Unable to update persistent storage (cancelling reminder) for " + sipMwiEvent.getSubscriberNumber() + ", will retry");
                    }
                }
                break;
            case SipMwiEvent.SIPMWI_EVENT_NOTIFICATION:
            default:
                if (sipMwiEvent.isEventServiceType(NtfEventTypes.SIPMWI_REMINDER.getName())) {

                    sipMwiEvent.setEventServiceTypeKey(NtfEventTypes.SIPMWI.getName());
                    sipMwiEvent.setEventTypeKey(NtfEventTypes.EVENT_TYPE_NOTIF.getName());
                    if (!sipMwiEventHandler.scheduleBackup(sipMwiEvent)) {
                        log.warn("Unable to update persistent storage (scheduling retry) for " + sipMwiEvent.getIdentity() + ", reminder will retry");
                        return false;
                    }
                }
                doCall(sipMwiEvent, userInfo);
            	break;
            }
        } catch (OutOfMemoryError me) {
            try {
                ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                log.error("NTF out of memory, shutting down... ", me);
            } catch (OutOfMemoryError me2) {;} //ignore second exception
            return true; //exit.
        } catch (Exception e) {
            log.error("Exception in SIP MWI worker " + NtfUtil.stackTrace(e), e);
            if (sipMwiEvent != null) {
                if (!sipMwiEventHandler.cancelAllEvents(sipMwiEvent, true)) {
                    log.warn("Unable to update persistent storage for " + sipMwiEvent.getIdentity());
                }
            }
        }
        return false;
    }

    /**
     * Get UserInfo from MCD for a Out-dial.
     * @param sipMwiEvent SipMwiEvent
     */
    protected UserInfo getUserInfo(SipMwiEvent sipMwiEvent)
    {
        return UserFactory.findUserByTelephoneNumber(sipMwiEvent.getSubscriberNumber());
    }

    /**
     * Attempt to make a call to user.
     * @param sipMwiEvent SipMwiEvent
     * @param userInfo Information about the call.
     */
    private void doCall(SipMwiEvent sipMwiEvent, UserInfo userInfo)
    {
        SIPInfo sipInfo = new SIPInfo(sipMwiEvent.getSubscriberNumber(), userInfo.getMail(), null, 0);

        MSA rmsa = null;

        if(sipMwiEvent.getMsgInfo() != null && sipMwiEvent.getMsgInfo().rmsa != null){
            rmsa = sipMwiEvent.getMsgInfo().rmsa;
        }
        else{
            String msid = MfsEventManager.getMSID(sipMwiEvent.getRecipient());
            rmsa = new MSA(msid);
        }

        UserMailbox inbox = new UserMailbox(rmsa,
                userInfo.hasMailType(Constants.NTF_EMAIL),
                userInfo.hasMailType(Constants.NTF_FAX),
                userInfo.hasMailType(Constants.NTF_VOICE),
                userInfo.hasMailType(Constants.NTF_VIDEO));

        setCountValues(inbox, sipInfo);

        caller.sendCall(sipMwiEvent, sipInfo, userInfo, callListener);
    }

    /**
     * Get the total count for voice messages
     * Note: Other message class types are out of scope.
     * @param inbox UserMailbox
     * @param sipInfo SIPInfo
     */
    private void setCountValues(UserMailbox inbox, SIPInfo sipInfo) {

		if( inbox.getNewVoiceCount() > Constants.MSG_COUNT_ERR ) {
            sipInfo.setNewVoiceCount(inbox.getNewVoiceCount());
		}
        if( inbox.getOldVoiceCount() > Constants.MSG_COUNT_ERR  && inbox.getSaveVoiceCount() > Constants.MSG_COUNT_ERR) {
            sipInfo.setOldVoiceCount(inbox.getOldVoiceCount()+inbox.getSaveVoiceCount());
        }
        if( inbox.getNewUrgentVoiceCount() > Constants.MSG_COUNT_ERR ) {
        	sipInfo.setNewUrgentVoiceCount(inbox.getNewUrgentVoiceCount());
        }
        if( inbox.getOldUrgentVoiceCount() > Constants.MSG_COUNT_ERR && inbox.getSaveUrgentVoiceCount() > Constants.MSG_COUNT_ERR ) {
          	sipInfo.setOldUrgentVoiceCount(inbox.getOldUrgentVoiceCount()+inbox.getSaveUrgentVoiceCount());
        }

        if( inbox.getNewVideoCount() > Constants.MSG_COUNT_ERR ) {
            sipInfo.setNewVideoCount(inbox.getNewVideoCount());
        }
        if( inbox.getOldVideoCount() > Constants.MSG_COUNT_ERR && inbox.getSaveVideoCount() > Constants.MSG_COUNT_ERR  ) {
            sipInfo.setOldVideoCount(inbox.getOldVideoCount()+inbox.getSaveVideoCount());
        }
        if( inbox.getNewUrgentVideoCount() > Constants.MSG_COUNT_ERR ) {
            sipInfo.setNewUrgentVideoCount(inbox.getNewUrgentVideoCount());
        }
        if( inbox.getOldUrgentVideoCount() > Constants.MSG_COUNT_ERR ) {
            sipInfo.setOldUrgentVideoCount(inbox.getOldUrgentVideoCount());
        }
        if( inbox.getOldUrgentVideoCount() > Constants.MSG_COUNT_ERR && inbox.getSaveUrgentVideoCount() > Constants.MSG_COUNT_ERR ) {
            sipInfo.setOldUrgentVideoCount(inbox.getOldUrgentVideoCount()+inbox.getSaveUrgentVideoCount());
        }
        if( inbox.getNewFaxCount() > Constants.MSG_COUNT_ERR ) {
           sipInfo.setNewFaxCount(inbox.getNewFaxCount());
        }
        if( inbox.getOldFaxCount() > Constants.MSG_COUNT_ERR ) {
           sipInfo.setOldFaxCount(inbox.getOldFaxCount());
        }
        if( inbox.getOldFaxCount() > Constants.MSG_COUNT_ERR && inbox.getSaveFaxCount() > Constants.MSG_COUNT_ERR) {
            sipInfo.setOldFaxCount(inbox.getOldFaxCount()+inbox.getSaveFaxCount());
         }
        if( inbox.getNewUrgentFaxCount() > Constants.MSG_COUNT_ERR ) {
            sipInfo.setNewUrgentFaxCount(inbox.getNewUrgentFaxCount());
        }
        if( inbox.getOldUrgentFaxCount() > Constants.MSG_COUNT_ERR ) {
            sipInfo.setOldUrgentFaxCount(inbox.getOldUrgentFaxCount());
        }
        if( inbox.getOldUrgentFaxCount() > Constants.MSG_COUNT_ERR && inbox.getSaveUrgentFaxCount() > Constants.MSG_COUNT_ERR) {
            sipInfo.setOldUrgentFaxCount(inbox.getOldUrgentFaxCount()+inbox.getSaveUrgentFaxCount());
        }
        if( inbox.getNewEmailCount() > Constants.MSG_COUNT_ERR ) {
            sipInfo.setNewEmailCount(inbox.getNewEmailCount());
        }
        if( inbox.getOldEmailCount() > Constants.MSG_COUNT_ERR ) {
            sipInfo.setOldEmailCount(inbox.getOldEmailCount());
        }
        if( inbox.getOldEmailCount() > Constants.MSG_COUNT_ERR && inbox.getSaveEmailCount() > Constants.MSG_COUNT_ERR) {
            sipInfo.setOldEmailCount(inbox.getOldEmailCount()+ inbox.getSaveEmailCount());
        }
        if( inbox.getNewUrgentEmailCount() > Constants.MSG_COUNT_ERR ) {
            sipInfo.setNewUrgentEmailCount(inbox.getNewUrgentEmailCount());
        }
        if( inbox.getOldUrgentEmailCount() > Constants.MSG_COUNT_ERR ) {
            sipInfo.setOldUrgentEmailCount(inbox.getOldUrgentEmailCount());
        }
        if( inbox.getOldUrgentEmailCount() > Constants.MSG_COUNT_ERR &&  inbox.getSaveUrgentEmailCount() > Constants.MSG_COUNT_ERR ) {
            sipInfo.setOldUrgentEmailCount(inbox.getOldUrgentEmailCount()+inbox.getSaveUrgentEmailCount() );
        }
    }


    /**
     * Class to handle answers from XMP
     */
    private class WorkerCallListener implements SIPCallListener
    {
        public void handleResult(SipMwiEvent sipMwiEvent, UserInfo userInfo, int code, int retryTimeInSecs) {
            sipMwiEventHandler.retrieveSchedulerEventIdsPersistent(sipMwiEvent);

            if (code == Config.getSipMwiOkXmpCode()) {
                // Successful case
                log.debug(sipMwiEvent + " successfully SipMwi notified (code: " + code + ")");

                if (merNotification) {
                    mer.notificationDelivered(userInfo.getTelephoneNumber(), Constants.NTF_SIPMWI);
                }

                if (sipMwiEvent.isReminder()) {
                    if (!sipMwiEventHandler.reScheduleReminderTriggerBackup(sipMwiEvent)) {
                        log.warn("Unable to update persistent storage (re-scheduling reminder) for " + sipMwiEvent.getIdentity() + ", will retry");
                        return;
                    }
                } else {
                    if (!sipMwiEventHandler.scheduleReminderTriggerBackup(sipMwiEvent)) {
                        log.warn("Unable to update persistent storage (scheduling reminder) for " + sipMwiEvent.getIdentity() + ", will retry");
                        return;
                    }
                }

                // Cancel retry event
                if (!sipMwiEventHandler.cancelEvent(sipMwiEvent, false)) {
                    log.warn("Unable to update persistent storage (cancelling retry) for " + sipMwiEvent.getIdentity() + ", will retry");
                    return;
                }

            } else if (retryTimeInSecs > 0)  {
                // Must retry with specific retry time provided by XMP
                log.debug("Rescheduling SipMwi for " + sipMwiEvent + " for " + retryTimeInSecs + " seconds (retry time specified by XMP)");

                // There is already an old request pending, create a new one and cancel the old one
                if (sipMwiEventHandler.scheduleRetryTimeSpecific(sipMwiEvent, retryTimeInSecs)) {
                    log.warn("Unable to update persistent storage (scheduling retry) for " + sipMwiEvent.getIdentity() + ", will retry");
                    return;
                }
            } else if (code == Config.getSipMwiRetryXmpCode()) {
                // Temporary error, nothing to reschedule, the schedule will kick-in later on with configured retry time value
                log.debug("Temporary SipMwi error for " + sipMwiEvent + " (code: " + code + "), retry later.");
            } else {
                // Error from XMP
                String errorMsg = "Failed to send SipMwi for " + sipMwiEvent + " (code: " + code + ")";
                log.debug(errorMsg);

                // Even if the event is not found in the cache, MER must be notified (NTF might just reload)
                // Code 552 used if MWI was not sent because no subscription exist.  In this case we don't want to generate MDRs
                if (code != Config.getSipMwiNotSubscribedXmpCode()) {
                    if (merNotification) {
                        mer.notificationFailed(userInfo.getTelephoneNumber(), Constants.NTF_SIPMWI, errorMsg);
                    }

                    // Schedule the fallback event only if it is a permanent error.
                    // A fallback is not triggered if the answer code is the "not subscribed" answer code.
                    if (FallbackHandler.get() != null) {
                        log.debug("Trying fallback for event " + sipMwiEvent);
                        FallbackHandler.get().fallback(Constants.NTF_SIPMWI, sipMwiEvent);
                    } else {
                        log.debug("Fallback handler null when " + sipMwiEvent);
                    }
                }

                // Schedule a Reminder trigger if not already in Reminder mode
                if (!sipMwiEvent.isReminder()) {
                    sipMwiEventHandler.scheduleReminderTriggerBackup(sipMwiEvent);
                }

                // Cancel retry event
                log.debug("SipMwiWorkerCallListener notified event not found: " + sipMwiEvent + " (code: " + code + ")");
                if (!sipMwiEventHandler.cancelEvent(sipMwiEvent, false)) {
                    log.warn("Unable to update persistent storage (cancelling retry) for " + sipMwiEvent.getIdentity() + ", will retry");
                    return;
                }
            }
        }
    }
}

