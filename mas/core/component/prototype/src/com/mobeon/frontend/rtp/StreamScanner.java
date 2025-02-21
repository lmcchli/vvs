/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.frontend.rtp;

/**
 * Created by IntelliJ IDEA.
 * User: qdalo
 * Date: 2005-feb-10
 * Time: 15:25:44
 * To change this template use File | Settings | File Templates.
 */
public class StreamScanner implements Runnable{
    private RTPStream stream;
    public StreamScanner(RTPStream stream) {
        this.stream = stream;
    }
    public void run() {
        stream.autoScanner();
    }
}
