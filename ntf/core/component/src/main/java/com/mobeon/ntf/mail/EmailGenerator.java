/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail;

import java.util.concurrent.TimeUnit;

public interface EmailGenerator {
    public NotificationEmail getNextEmail();
    public NotificationEmail getNextEmail(int timeout);
    public void putEmail(NotificationEmail email);
    public void expand(NotificationEmail email);
    public int size();
    public int getSize();
    public boolean isIdle(int time, TimeUnit unit);
    public boolean waitNotEmpty(int timeOut, TimeUnit unit);
}
