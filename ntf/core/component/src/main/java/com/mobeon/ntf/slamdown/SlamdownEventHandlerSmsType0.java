/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.slamdown;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.EventRetryTimerSchema;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.ntf.Config;

/**
 * This class extends the SlamdownEventHandler because the various slamdown events
 * have their respective scheduler-schema and a handler can only support one schema.
 */
public class SlamdownEventHandlerSmsType0 extends SlamdownEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(SlamdownEventHandlerSmsType0.class);
    private SlamdownEventHandler slamdownEventHandler;

	public SlamdownEventHandlerSmsType0() {
	    RetryEventInfo info = new RetryEventInfo(getEventServiceName());
	    info.setEventRetrySchema(Config.getSlamdownMcnSmsType0RetrySchema());
	    info.setExpireTimeInMinute(Config.getSlamdownMcnSmsType0ExpireTimeInMin());
        info.setExpireRetryTimerInMinute(Config.getSlamdownMcnExpiryIntervalInMin());        
        info.setMaxExpireTries(Config.getSlamdownMcnExpiryRetries());
	    super.init(info);

	    slamdownEventHandler = (SlamdownEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SLAMDOWN_L3.getName());
	}

	public String getEventServiceName() {
		return NtfEventTypes.SLAMDOWN_SMS_TYPE_0.getName();
	}

    /**
     * Schedule an event for SMS-Type-0
     * @param slamdownList SlamdownList
     * @return boolean True if the SmsType0 as been scheduled and stored successfully, false otherwise. 
     */
    public boolean scheduleSmsType0(SlamdownList slamdownList) {
        boolean result = true;
        String smsType0EventId = slamdownList.getSchedulerIds().getSmsType0EventId();
        AppliEventInfo eventInfo = null;

        /**
         * In the case of a validity timer retrying (for example 3 X 24h), in order to keep the retry count,
         * NTF shall not re-schedule another SMS-Type-0 but keep the current one if present.  
         */
        if (smsType0EventId == null || smsType0EventId.isEmpty()) {
            EventRetryTimerSchema retrySchema = new EventRetryTimerSchema(Config.getSlamdownMcnSmsType0RetrySchema());
            long when = retrySchema.getNextRetryTime(0);

            /**
             * Scheduler limitation.
             * An event which has a backup event (scheduled by the Scheduler) cannot be cancelled and re-scheduled.
             * Scheduler will not consider the re-scheduled one.
             * This particular case might happen here since an SMSUnit can either be successful or not depending
             * of the response type from the NTF SMS-Client.
             * To avoid this possibility, a 2 second delay (2000 ms) is introduced in order to make sure the Scheduler
             * will handle this new schedule-event which will have a slightly different ID.
             */
            eventInfo = eventHandler.scheduleEvent(
                    when + 2000,
                    slamdownList.getNotificationNumber() + NtfEvent.getUniqueId(),
                    NtfEventTypes.SLAMDOWN_SMS_TYPE_0.getName(),
                    slamdownList.getEventProperties());

            log.debug("ScheduleSmsType0: scheduled event: " + eventInfo.getEventId());
            slamdownList.getSchedulerIds().setSmsType0EventId(eventInfo.getEventId());

            /**
             * As a PhoneOn event is now scheduled (either SMS-Type-0/SMSc or AlertSc/HLR),
             * the SMS-Unit timer must be cancelled.  In order to perform IO-write only once,
             * both SMS-Unit and SMS-Type-0 eventIds will be updated in 1 IO operation..
             * 
             * Cancelling the SMS-Unit timer will update the persistent file with the new eventInfo just scheduled.
             */
            boolean successfullyCancelled = slamdownEventHandler.cancelSmsUnitEvent(slamdownList, false);
            if (!successfullyCancelled) {
                /**
                 * If the update of the persistent storage is not successful while trying to cancel SmsUnit,
                 * it means that the storage of the SmsType0 scheduled event as not been stored persistently either,
                 * Cancel the SmsType0 event and let the SmsUnit retry.
                 */
                slamdownEventHandler.cancelEvent(eventInfo.getEventId());
                result = false;
            }

        } else {
            log.debug("ScheduleSmsType0: event already scheduled: " + smsType0EventId);

            /**
             * If a smsType0EventId is already scheduled, still the SMS-Unit eventId must be cancelled
             *  
             * Cancelling the SMS-Unit timer will update the persistent file
             * (with the new eventInfo just scheduled)
             * 
             * Force the cancellation even if there is an error writing on disk since there is already a SmsType0 scheduled. 
             */
            slamdownEventHandler.cancelSmsUnitEvent(slamdownList, true);
        }

        return result;
    }
}
