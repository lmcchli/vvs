/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.services.moip.ntf.coremgmt;

import java.util.Calendar;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.vvs.ntf.notifier.NotifierIncomingSignalInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.NotifierPluginHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse.NotifierHandlingActions;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse.NotifierHandlingTypes;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.util.threads.NtfThread;

public class NtfMessageServiceWorker extends NtfThread {

    private LogAgent log = NtfCmnLogger.getLogAgent(NtfMessageServiceWorker.class);
    private ManagedArrayBlockingQueue<Object> ntfMessageServiceQueue;

    private static final long SCHEDULED_DELAY_IN_MILLISECONDS = 1000;

    NtfMessageServiceWorker(String threadName, ManagedArrayBlockingQueue<Object> ntfMessageServiceQueue) {
        super(threadName);
        this.ntfMessageServiceQueue = ntfMessageServiceQueue;
        setDaemon(true);
        start();
    }

    public boolean ntfRun() {
        NtfEvent ntfEvent = null;

        // Get an event from the working queue
        Object obj = ntfMessageServiceQueue.take();
        if (obj == null) return false;
        if (!(obj instanceof NtfEvent)) {
            log.error("NtfMessageServiceWorker: Invalid object received: " + obj.getClass().getName());
            return false;
        }
        ntfEvent = (NtfEvent)obj;

        synchronized (ntfEvent) {
            Object perf = null;
            try {
                NtfRetryHandling schedulerHandler = NtfEventHandlerRegistry.getEventHandler(ntfEvent.getEventServiceTypeKey());

                // Step 1) Notification plug-in first (if loaded)
                NotifierIncomingSignalResponse notifierResponse = new NotifierIncomingSignalResponse();
                String notifierHandlingType = ntfEvent.getProperty(NtfEvent.NOTIFIER_HANDLING_TYPE);

                log.debug("NtfMessageServiceWorker for " + ntfEvent.getRecipient() + ", " + ntfEvent.getNtfEventType());

                if (NotifierPluginHandler.hasPlugins() &&
                        (notifierHandlingType == null || notifierHandlingType.isEmpty())) {

                    Object perform = null;
                    try {
                        if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                            perform = CommonOamManager.profilerAgent.enterCheckpoint("NTF.Trace.2.1.NMSW.PluginExecTime");
                        }
                        long time = Calendar.getInstance().getTimeInMillis();
                        notifierResponse = NotifierPluginHandler.get().handleNotification(new NotifierIncomingSignalInfo(ntfEvent));
                        log.debug("NtfMessageServiceWorker: Plug-in-execution-time: " + (Calendar.getInstance().getTimeInMillis() - time) + " ms");
                        log.debug("NtfMessageServiceWorker: Plug-in response: " + notifierResponse);

                    } finally {
                        if (perform != null) {
                            CommonOamManager.profilerAgent.exitCheckpoint(perform);
                        }
                    }

                    // If Notification plug-in returns RETRY, the Legacy engine will not be invoked
                    if (notifierResponse.isAction(NotifierHandlingActions.RETRY)) {
                        log.debug("NtfMessageServiceWorker: Notification plug-in handling action requested to retry (level-2)");
                        if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                            NtfMessageService.profilerAgentCheckPoint("NTF.4.NMSW.P.Action.Retry." + ntfEvent.getNtfEventType());
                        }
                        return false;
                    }
                    
                    if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        NtfMessageService.profilerAgentCheckPoint("NTF.4.NMSW.P.Action.Ok." + ntfEvent.getNtfEventType());
                    }

                    // If Notification plug-in handles all of the notification, no need to invoke Legacy, cancel Level-2 event
                    if (notifierResponse.containsHandlingType(NotifierHandlingTypes.ALL)) {
                        log.debug("NtfMessageServiceWorker: Notification plug-in handling ALL of the notification");

                        log.debug("NtfMessageServiceWorker: Cancel backup (Level-2): " + ntfEvent.getReferenceId());
                        schedulerHandler.cancelEvent(ntfEvent.getReferenceId());
                        if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                            NtfMessageService.profilerAgentCheckPoint("NTF.4.NMSW.P.Handle.All." + ntfEvent.getNtfEventType());
                        }
                        return false;
                    }
                }
                
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    perf = CommonOamManager.profilerAgent.enterCheckpoint("NTF.Trace.3.NMSW.ExecTime");
                }

                /**
                 * If notification is a 'sendNotifcation', it will not be injected in Legacy engine (since not supported anyway in Legacy)
                 * This check is a protection in case of receiving a generic signal ('sendNotification') without having either
                 * a loaded plug-in or having a plug-in that does not recognise the 'sendNotification' notification.
                 */
                boolean isSendNotification = Boolean.parseBoolean(ntfEvent.getProperty(NtfMessageService.SEND_NOTIFICATION_PROPERTY));
                if (isSendNotification) {
                    log.debug("NtfMessageServiceWorker: Notification sendNotification:" + ntfEvent.getNtfEventType() + " will not be injected into Legacy engine");

                    log.debug("NtfMessageServiceWorker: Cancel backup (Level-2): " + ntfEvent.getReferenceId());
                    schedulerHandler.cancelEvent(ntfEvent.getReferenceId());
                    return false;
                }

                // Step 2) Notification Legacy engine second (if applicable)

                /**
                 * Step 2.1) Check Notification plug-in result (if loaded)
                 *   - if Notification plug-in handles ALL part:
                 *     Already handled in previous section
                 *   - if Notification plug-in handles only the SMS part (meaning anything but NONE):
                 *     Cancel original Level-2 and re-schedule a new Level-2 NOTIFIER_HANDLING_TYPE property that states that Notification plug-in already it's part.
                 *   - if Notification plug-in handles NONE:
                 *     Keep the original Level-2 eventId, if Legacy engine asks for a retry, Notification plug-in will be solicited again.
                 */
                if (!notifierResponse.getHandlingType().isEmpty() && !notifierResponse.containsHandlingType(NotifierHandlingTypes.NONE)) {
                    String previousEventId = ntfEvent.getReferenceId();

                    // Set the NotifierHandlingType as 'already handled' by Notification plug-in, so that the Legacy does not consider it
                    notifierHandlingType = notifierResponse.getHandlingType().get(0).getName();
                    ntfEvent.setProperty(NtfEvent.NOTIFIER_HANDLING_TYPE, notifierHandlingType);

                    // Schedule a new backup eventId (NTF level-2) with NOTIFIER_HANDLING_TYPE property
                    String backupId = schedulerHandler.scheduleEvent(ntfEvent, SCHEDULED_DELAY_IN_MILLISECONDS);
                    log.debug("NtfMessageServiceWorker: Scheduled backup (Level-2) with property (" + NtfEvent.NOTIFIER_HANDLING_TYPE + "=" + notifierHandlingType + "): " + backupId);
                    ntfEvent.keepReferenceID(backupId);

                    // Cancel previous eventId (NTF level-2)
                    log.debug("NtfMessageServiceWorker: Cancel previous backup (Level-2): " + previousEventId);
                    schedulerHandler.cancelEvent(previousEventId);
                    
                    if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        NtfMessageService.profilerAgentCheckPoint("NTF.4.NMSW.P.Handle.Partial." + ntfEvent.getNtfEventType());
                    }
                } else {
                    if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        NtfMessageService.profilerAgentCheckPoint("NTF.4.NMSW.P.Handle.None." + ntfEvent.getNtfEventType());
                    }
                }

                // Step 2.2) Send event to NTF (Legacy)
                NtfEventReceiver eventReceiver = NtfEventHandlerRegistry.getNtfEventReceiver(ntfEvent.getNtfEventType());
                if (eventReceiver != null) {
                    eventReceiver.sendEvent(ntfEvent);
                }

            } catch (Throwable t) {
                log.error("NtfMessageServiceWorker exception: ", t);
            } finally {
                if (perf != null) {
                    CommonOamManager.profilerAgent.exitCheckpoint(perf);
                }
            }
            return false;
        }
    }

    @Override
    public boolean shutdown() {
        // TODO Auto-generated method stub
        return false;
    }
}
