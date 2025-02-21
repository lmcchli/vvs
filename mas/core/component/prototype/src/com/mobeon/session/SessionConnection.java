/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.session;

import com.mobeon.frontend.Stream;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-17
 * Time: 16:46:46
 * To change this template use File | Settings | File Templates.
 */
public interface SessionConnection {
    public void setRunning(boolean state);
    public void endConnection();
    public void releaseObjects();
    public Stream getOutputStream(int index);
    public List getInStreams();
    public List getOutStreams();
    public String getCaller();
    public String getCallee();
    public String getURI();
    public SessionServer getServer();
}
