/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.session;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-17
 * Time: 16:50:51
 * To change this template use File | Settings | File Templates.
 */
public interface SessionServer {
    public void run() ;
    public  void fail(Exception e, String msg);
    public void removeConnection(String URI);

}
