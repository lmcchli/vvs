/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail;

public interface EmailStore {
    public void putEmail(NotificationEmail email);
    public boolean putEmailCheckSize(NotificationEmail email);
    public int getSize();
}
