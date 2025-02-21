/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt.fallback;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;

/**
 * FallbackHandler
 */
public class FallbackHandler implements com.mobeon.ntf.Constants {

    private LogAgent log;
    private static FallbackHandler _inst;
    private FallbackWorker[] fallbackWorkers;
    private FallbackEventHandler fallbackEventHandler;
    private ManagedArrayBlockingQueue<Object> workingQueue;
    private static boolean isStarted = false;

    private FallbackHandler() {
        try {
            log = NtfCmnLogger.getLogAgent(FallbackHandler.class);

            // Create working queue
            workingQueue = new ManagedArrayBlockingQueue<Object>(Config.getFallbackQueueSize());

            // Create fallback event handlers
            fallbackEventHandler = (FallbackEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.FALLBACK_L3.getName());

            int numberOfWorkers = Config.getFallbackWorkers();
            if (numberOfWorkers > 0) {
                // Create fallback workers
                createWorkers(numberOfWorkers);
                isStarted=true;
            } else {
                log.error("Falback service can't start no worker configured");
            }
        } catch (Exception e) {
            log.error("Falback service can't start : "+e);
        }
    }

    public static FallbackHandler get() {
        if (_inst == null ) {
            _inst = new FallbackHandler();
        }
        return _inst;
    }

    public ManagedArrayBlockingQueue<Object> getWorkingQueue() {
        return workingQueue;
    }

    /**
     * Create the workers
     */
    private void createWorkers(int numberOfWorkers) {
        fallbackWorkers = new FallbackWorker[numberOfWorkers];

        for (int i = 0; i<numberOfWorkers; i++) {
            fallbackWorkers[i] = new FallbackWorker(workingQueue, "FallbackWorker-" + i, this);
            fallbackWorkers[i].setDaemon(true);
            fallbackWorkers[i].start();
        }
    }

    /**
     * Handles fallback events
     *
     * @param originalNotificationType OriginalNotificationType 
     * @param ntfEvent NtfEvent
     */
    public boolean fallback(int originalNotificationType, NtfEvent ntfEvent) {
        return fallback(originalNotificationType, ntfEvent, null);
    }

    public boolean fallback(int originalNotificationType, NtfEvent ntfEvent, FallbackInfo info) {
        boolean storedInQueue = false;
        profilerAgentCheckPoint("NTF.Fallback.1.In (total incoming)");

        if(!isStarted)
        {
            if (log.isDebugEnabled()) {
            log.debug("No fallback will occur service is not started");
            }
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("Create fall back event for:  " + ntfEvent);
        }
        FallbackEvent fallbackEvent = new FallbackEvent(originalNotificationType, ntfEvent);
        if (info != null) {
            //set optional fallback info
            log.debug("Set info for:  " + ntfEvent);
            fallbackEvent.setFallbackInfo(info);
        } else
        {
            if (log.isDebugEnabled()) {
                log.debug("fallback info is null for :  " + ntfEvent);
            }
        }

        // Schedule backup event
        if (log.isDebugEnabled()) {
            log.debug("Shedule backup event for:  " + ntfEvent);
        }
        AppliEventInfo eventInfo = fallbackEventHandler.scheduleBackup(fallbackEvent);
        fallbackEvent.keepReferenceID(eventInfo.getEventId());

        // Put in queue
        if (log.isDebugEnabled()) {
            log.debug("queue:  " + ntfEvent + " fallback info "  + fallbackEvent);
        }
        storedInQueue = workingQueue.offer(fallbackEvent);

        return storedInQueue;
    }

    /**
     * Profiling method
     * @param checkPoint checkPoint string
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

    protected boolean isStarted()
    {
        return isStarted;
    }
}
