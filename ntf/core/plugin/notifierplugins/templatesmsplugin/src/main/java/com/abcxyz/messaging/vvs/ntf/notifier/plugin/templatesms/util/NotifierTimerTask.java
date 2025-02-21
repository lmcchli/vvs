/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util;

import java.util.TimerTask;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateSmsPlugin;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule.NotifierEvent;
import java.util.concurrent.BlockingQueue;

public class NotifierTimerTask extends TimerTask{

    private static INotifierLogger log = TemplateSmsPlugin.getLoggerFactory().getLogger(NotifierTimerTask.class);
    NotifierEvent notifierEvent;
    BlockingQueue<NotifierEvent> queue;

    public NotifierTimerTask(NotifierEvent notifierEvent, BlockingQueue<NotifierEvent> queue){
        this.notifierEvent = notifierEvent;
        this.queue = queue;
    }

    public void run(){  
        // Inject timed-out event to workingQueue 
        boolean storedInQueue = queue.offer(notifierEvent);
        if (!storedInQueue) {
            log.warn("NotifierTimerTask.run: Not stored in workingQueue (full), will retry");
        }
    }
}
