/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.frontend;

import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-19
 * Time: 23:46:29
 * To change this template use File | Settings | File Templates.
 */
public interface Stream {
    public void playPrompt(byte[] msg) throws InterruptedException;
    public void playPrompt(String[] messageFiles, boolean interruptable) throws InterruptedException;
    public String recordGetFilename() throws InterruptedException;
    public String record(boolean interruptable) throws InterruptedException;
    public boolean scan() throws InterruptedException;
    public void interrupt(boolean force);
    public void close();

}
