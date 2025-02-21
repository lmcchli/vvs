/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util;

import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.mail.EmailGenerator;
import com.mobeon.ntf.mail.EmailStore;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;

public class EmailQueue implements EmailStore, EmailGenerator {
    ManagedArrayBlockingQueue<Object> fManagedArrayBlockingQueue;
    private static LogAgent log = NtfCmnLogger.getLogAgent(EmailQueue.class);

    public EmailQueue(int queueSize) {
        fManagedArrayBlockingQueue = new ManagedArrayBlockingQueue<Object>(queueSize);
    }

    public void expand(NotificationEmail email){
        fManagedArrayBlockingQueue.offer(email);
    }

    public void expand(NotificationEmail email, int timeout){
        fManagedArrayBlockingQueue.offer(email, timeout);
    }

    public NotificationEmail getNextEmail() {
        return (NotificationEmail) fManagedArrayBlockingQueue.take();
    }

    public NotificationEmail getNextEmail(int timeout) {
        return (NotificationEmail) fManagedArrayBlockingQueue.poll(timeout, TimeUnit.SECONDS);
    }

    public void putEmail(NotificationEmail email) {
        try {
            fManagedArrayBlockingQueue.put(email);
        } catch (Throwable t) {
            log.info("putEmail: queue full or state locked while handling event");
        }
    }

    public boolean putEmailCheckSize(NotificationEmail email) {
        return fManagedArrayBlockingQueue.offer(email);
    }

    public int getSize() {
      return fManagedArrayBlockingQueue.size();
    }

    public int size() {
        return(getSize());
    }

    public boolean isIdle(int time, TimeUnit unit)
    {
        return(fManagedArrayBlockingQueue.isIdle(time, unit));
    }

    public boolean waitNotEmpty(int timeOut, TimeUnit unit) {
        return(fManagedArrayBlockingQueue.waitNotEmpty(timeOut, unit));
    }
}



