/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.client;


/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-09
 * Time: 12:47:45
 * To change this template use File | Settings | File Templates.
 */
public class main {
    public static void main(String argv[]) {
        /*
        DummySessionClient sc[] = new DummySessionClient[5];
        for (int i = 0; i < 5; i++)
            sc[i] = new DummySessionClient();
        for (int i = 0; i < 5 ; i++)
            sc[i].start();
   */
        DummySessionClient sc = new DummySessionClient();
        sc.start();
    }
}
