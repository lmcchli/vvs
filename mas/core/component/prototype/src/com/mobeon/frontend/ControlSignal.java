/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.frontend;

import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-19
 * Time: 23:46:18
 * To change this template use File | Settings | File Templates.
 */
public interface ControlSignal {
    public String getToken() throws InterruptedException;
    public String getToken(long timeout, int maxTimeBetweenTokens);
    public void sendToken(String value) throws Exception;
    public void putToken(String value);
    public void putToken(int value);     
    public void setStopRunning(boolean state);
    public void clearQueue();

}
