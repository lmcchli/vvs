/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.slamdown;

import com.abcxyz.messaging.common.oam.LogAgent;
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
public class SlamdownEventHandlerSmsInfo extends SlamdownEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(SlamdownEventHandlerSmsInfo.class);
    private SlamdownEventHandler slamdownEventHandler;

	public SlamdownEventHandlerSmsInfo() {
	    RetryEventInfo info = new RetryEventInfo(getEventServiceName());
	    info.setEventRetrySchema(Config.getSlamdownMcnSmsInfoRetrySchema());
	    info.setExpireTimeInMinute(Config.getSlamdownMcnSmsInfoExpireTimeInMin());
        info.setExpireRetryTimerInMinute(Config.getSlamdownMcnExpiryIntervalInMin());        
        info.setMaxExpireTries(Config.getSlamdownMcnExpiryRetries());
	    super.init(info);

	    slamdownEventHandler = (SlamdownEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SLAMDOWN_L3.getName());
	}

	public String getEventServiceName() {
		return NtfEventTypes.SLAMDOWN_SMS_INFO.getName();
	}

    /**
     * Schedule an event for SMS-Info
     * @param slamdownList SlamdownList
     * @return boolean True if the SmsInfo as been scheduled and stored successfully, false otherwise. 
     */
    public boolean scheduleSmsInfo(SlamdownList slamdownList) {
        boolean result = true;
        String smsInfoEventId = slamdownList.getSchedulerIds().getSmsInfoEventId();
        AppliEventInfo eventInfo = null;

        if (smsInfoEventId == null || smsInfoEventId.isEmpty()) {
            eventInfo = eventHandler.scheduleEvent(slamdownList.getNotificationNumber() + NtfEvent.getUniqueId(), NtfEventTypes.SLAMDOWN_SMS_INFO.getName(), slamdownList.getEventProperties());

            log.debug("ScheduleSmsInfo: scheduled event: " + eventInfo.getEventId());
            slamdownList.getSchedulerIds().setSmsInfoEventId(eventInfo.getEventId());

            /**
             * As a SMS-Info event is now scheduled, the SMS-Type-0 timer must be cancelled.
             * In order to perform IO-write only once, all the eventIds will be updated in 1 IO operation.
             * 
             * Cancelling the SMS-Type-0 timer will update the persistent file with the new eventInfo just scheduled.
             */
            boolean successfullyCancelled = slamdownEventHandler.cancelSmsType0Event(slamdownList, false);
            if (!successfullyCancelled) {
                /**
                 * If the update of the persistent storage is not successful while trying to cancel SmsType0,
                 * it means that the storage of the SmsInfo scheduled event as not been stored persistently either,
                 * Cancel the SmsInfo event and let the SmsType0 retry.
                 */
                slamdownEventHandler.cancelEvent(eventInfo.getEventId());
                result = false;
            }

        } else {
            log.debug("ScheduleSmsInfo: event already scheduled: " + smsInfoEventId);

            /**
             * If a smsInfoEventId is already scheduled, still the SMS-Type-0 eventId must be cancelled
             *  
             * Cancelling the SMS-Type-0 timer will update the persistent file
             * (with the new eventInfo just scheduled)
             * 
             * Force the cancellation even if there is an error writing on disk since there is already a SmsInfo scheduled. 
             */
            slamdownEventHandler.cancelSmsType0Event(slamdownList, true);
        }

        return result;
    }
}
