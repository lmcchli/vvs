/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail;

public interface MailMemoryManager {

    public long getMailMemoryUsed();
    
    public int getMailMemoryCount();
    
    public int reserveMemory(int poller, int msgId, int mailSize);
    
    public void releaseMemory(int msgId, int poller);
}

