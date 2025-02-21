package com.mobeon.ntf.slamdown;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.ntf.Config;

/**
 * This class extends the SlamdownEventHandler because the various slamdown events
 * have their respective scheduler-schema and a handler can only support one schema.
 */
public class SlamdownEventHandlerSmsUnit extends SlamdownEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(SlamdownEventHandlerSmsUnit.class);

	public SlamdownEventHandlerSmsUnit() {
		RetryEventInfo info = new RetryEventInfo(getEventServiceName());
		info.setEventRetrySchema(Config.getSlamdownMcnSmsUnitRetrySchema());
        info.setExpireTimeInMinute(Config.getSlamdownMcnSmsUnitExpireTimeInMin());
        info.setExpireRetryTimerInMinute(Config.getSlamdownMcnExpiryIntervalInMin());        
        info.setMaxExpireTries(Config.getSlamdownMcnExpiryRetries());
		super.init(info);
	}

	public String getEventServiceName() {
		return NtfEventTypes.SLAMDOWN_SMS_UNIT.getName();
	}

    /**
     * Schedule an event for SmsUnit.
     * This timer is used to retry if a temporary error occurs internal to NTF (because of a SMSUnit queue full for example)
     * @param slamdownList SlamdownList
     * @return boolean True if the SmsUnit event has been successfully scheduled and stored persistently
     */
    public boolean scheduleSmsUnit(SlamdownList slamdownList) {
        boolean result = true;
        String smsUnitEventId = slamdownList.getSchedulerIds().getSmsUnitEventId();
        AppliEventInfo eventInfo = null;

        if (smsUnitEventId == null || smsUnitEventId.isEmpty()) {
            eventInfo = eventHandler.scheduleEvent(slamdownList.getNotificationNumber() + NtfEvent.getUniqueId(), NtfEventTypes.SLAMDOWN_SMS_UNIT.getName(), slamdownList.getEventProperties());

            log.debug("ScheduleSmsUnit: scheduled event: " + eventInfo.getEventId());
            slamdownList.getSchedulerIds().setSmsUnitEventId(eventInfo.getEventId());

            // Store the eventId
            boolean successfullyCancelled = slamdownList.updateEventIdsPersistent();
            if (!successfullyCancelled) {
                /**
                 * If the update of the persistent storage is not successful after starting a SmsUnit event,
                 * cancel the SmsUnit event and return false.
                 */
                eventHandler.cancelEvent(eventInfo);
                result = false;
            }
        } else {
            log.debug("ScheduleSmsUnit: event already scheduled: " + smsUnitEventId);
        }

        return result;
    }
}
