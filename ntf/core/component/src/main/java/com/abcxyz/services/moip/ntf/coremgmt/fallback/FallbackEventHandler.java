/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt.fallback;

import java.util.concurrent.atomic.AtomicLong;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.ntf.Config;

/**
 * This class handles fallback events coming from Scheduler
 */
public class FallbackEventHandler extends NtfRetryEventHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(FallbackEventHandler.class);

    public FallbackEventHandler() {
        RetryEventInfo info = new RetryEventInfo(getEventServiceName());
        info.setEventRetrySchema(Config.getFallbackRetrySchema());
        info.setExpireTimeInMinute(Config.getFallbackExpireTimeInMin());
        super.init(info);
    }

    public String getEventServiceName() {
        return NtfEventTypes.FALLBACK_L3.getName();
    }

    public void reset() {
        super.numOfCancelledEvent = new AtomicLong(0);
        super.numOfFiredExpireEvent = new AtomicLong(0);
        super.numOfFiredNotifEvent = new AtomicLong(0);
        super.numOfScheduledEvent = new AtomicLong(0);
    }

    /**
     * Schedule a backup event for Fallback
     *
     * @param fallbackEvent FallbackEvent
     * @return AppliEventInfo
     */
    public AppliEventInfo scheduleBackup(FallbackEvent fallbackEvent) {
        AppliEventInfo eventInfo = eventHandler.scheduleEvent(fallbackEvent.getSubscriberNumber() + NtfEvent.getUniqueId(), NtfEventTypes.FALLBACK_L3.getName(), fallbackEvent.getEventProperties());
        log.debug("Scheduled event: " + eventInfo.getEventId());
        return eventInfo;
    }

    @Override
    public int eventFired(AppliEventInfo eventInfo) {
        int result = EventHandleResult.OK;

        try {
            FallbackEvent fallbackEvent = null;

            numOfFiredNotifEvent.incrementAndGet();

            fallbackEvent = new FallbackEvent(eventInfo.getEventKey(), eventInfo.getEventProperties());
            if (!FallbackHandler.get().isStarted()) {
                log.error("Received Fallback event but service is not available, will retry for " + fallbackEvent);
                return EventHandleResult.OK;
            }

            log.debug("EventFired: " + fallbackEvent);

            // Validate if its last fallback notification
            if (eventInfo.getNextEventInfo() == null || eventInfo.isExpire() || eventInfo.isLastExpire()) {
                log.info("Subscriber " + fallbackEvent.getSubscriberNumber() + " will not be notified.  Fallback notification expired.  Expiry event: " + fallbackEvent);

                // Profiler
                FallbackHandler.get().profilerAgentCheckPoint("NTF.Fallback.4.Expiry");

                return EventHandleResult.STOP_RETRIES;
            }

            // Profiler
            FallbackHandler.get().profilerAgentCheckPoint("NTF.Fallback.3.Retry");

            // Keep the next eventId in cache
            if (eventInfo.getNextEventInfo() != null) {
                fallbackEvent.keepReferenceID(eventInfo.getNextEventInfo().getEventId());
            }
            fallbackEvent.setCurrentEvent(FallbackEvent.FALLBACK_EVENT_SCHEDULER_RETRY);

            boolean storedInQueue = FallbackHandler.get().getWorkingQueue().offer(fallbackEvent);
            if (storedInQueue) {
                log.debug("EventFired: Stored in workingQueue : " + fallbackEvent);
            } else {
                log.debug("EventFired: Not stored in workingQueue (full), will retry : " + fallbackEvent);
            }
        } catch (Exception e) {
            String message = "Event fired exception for " + eventInfo.getEventId();
            if (eventInfo.getNextEventInfo() != null) {
                log.warn(message + ", will retry. ", e);
            } else {
                log.error(message + ", will not retry. ", e);
            }
        }

        return result;
    }

    public void cancelStoredEvent(String referenceId) {
        if (referenceId != null) {
            log.debug("FallbackEventHandler cancel event: " + referenceId);
            super.cancelEvent(referenceId);
        }
    }
}
