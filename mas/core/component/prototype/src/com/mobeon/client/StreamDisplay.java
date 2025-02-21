/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.client;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-dec-01
 * Time: 14:44:31
 * To change this template use File | Settings | File Templates.
 */
public class StreamDisplay extends Thread {
    public StreamDisplay(BufferedReader sin, DummySessionClient parent) {
        this.sin = sin;
        this.parent = parent;
        this.start();
    }

    public void run() {
        String msg;
        try {
            while((msg = sin.readLine()) != null) {
              System.out.println("FROM SERVER: " + msg);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("StreamDisplay: END");
        parent.stopRunning();
    }
    private BufferedReader sin;
    private DummySessionClient parent;
}
