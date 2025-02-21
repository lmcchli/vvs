/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.backend;

import java.util.ArrayList;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-17
 * Time: 16:32:52
 * To change this template use File | Settings | File Templates.
 */
public interface StorageClient {
    public Object getGreeting(String mailboxid, boolean isAdmin);
    public ArrayList getMboxStat(String mailboxid);
    public ByteBuffer getMessage(String mailboxid, int messageId);
    public String getMessageMediaLocator(String mailboxid, int messageId);    
    public void storeMessage(String mailboxid, byte[] msg);
    public void storeMessage(String mailboxid, String mediaLocator);
}
