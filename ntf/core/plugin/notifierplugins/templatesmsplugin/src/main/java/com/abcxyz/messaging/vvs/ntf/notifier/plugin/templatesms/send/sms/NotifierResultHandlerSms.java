/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.send.sms;

import java.util.concurrent.BlockingQueue;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierResultHandlerSms;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierManagementCounter;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateSmsPlugin;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.NotifierTypeEvent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule.NotifierEvent;

/**
 * NotifierResultHandlerSms listens to call-backs for the SMS result for one notification.
 * The result is put on the NotifierWorkers' queue to be processed.
 */
public class NotifierResultHandlerSms extends ANotifierResultHandlerSms {

    private static INotifierLogger log = TemplateSmsPlugin.getLoggerFactory().getLogger(NotifierResultHandlerSms.class);
    private static final String COUNTER_SERVICE_NAME = "ShortMessage";
    private INotifierManagementCounter notifierManagementCounter = TemplateSmsPlugin.getManagementCounter();
    private NotifierEvent notifierEvent = null;
    private BlockingQueue<NotifierEvent> notifierWorkerQueue = null;

    public NotifierResultHandlerSms(NotifierEvent notifierEvent, BlockingQueue<NotifierEvent> notifierWorkerQueue) {
        this.notifierEvent = notifierEvent;
        this.notifierWorkerQueue = notifierWorkerQueue;
    }

    @Override
    public void ok() {
        notifierManagementCounter.incrementSuccessCounter(COUNTER_SERVICE_NAME);
        // Set the event so that it will be handled properly when firing into the NotifierWorker 
        notifierEvent.setNotifierTypeEvent(NotifierTypeEvent.EVENT_SMS_INFO_RESPONSE_SUCCESSFUL);
        boolean storedInQueue = notifierWorkerQueue.offer(notifierEvent);
        if (!storedInQueue) {
            log.warn("NotifierResultHandlerSms.ok: Not stored in workingQueue (full), " + notifierEvent.getIdentity() + ", response dropped.");
        }
    }

    @Override
    public void retry(String errorText) {
        notifierManagementCounter.incrementFailCounter(COUNTER_SERVICE_NAME);
        // Set the event so that it will be handled properly when firing into the NotifierWorker 
        notifierEvent.setNotifierTypeEvent(NotifierTypeEvent.EVENT_CLIENT_RETRY);
        boolean storedInQueue = notifierWorkerQueue.offer(notifierEvent);
        if (!storedInQueue) {
            log.warn("NotifierResultHandlerSms.ok: Not stored in workingQueue (full), " + notifierEvent.getIdentity() + ", response dropped.");
        }
    }

    @Override
    public void failed(String errorText) {
        notifierManagementCounter.incrementFailCounter(COUNTER_SERVICE_NAME);
        // Set the event so that it will be handled properly when firing into the NotifierWorker 
        notifierEvent.setNotifierTypeEvent(NotifierTypeEvent.EVENT_CLIENT_FAILED);
        boolean storedInQueue = notifierWorkerQueue.offer(notifierEvent);
        if (!storedInQueue) {
            log.warn("NotifierResultHandlerSms.ok: Not stored in workingQueue (full), " + notifierEvent.getIdentity() + ", response dropped.");
        }
    }

}
