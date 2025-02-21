/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.backend.demo;

import com.mobeon.backend.StorageClient;
import com.mobeon.application.graph.Node;

import java.util.ArrayList;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-16
 * Time: 12:46:17
 * To change this template use File | Settings | File Templates.
 */
public class DemoStorageClient implements StorageClient {
    private static ArrayList messageList;
    public DemoStorageClient() {
        if (messageList == null) {
            messageList = new ArrayList(3);
            messageList.add(0,new String("sound/msg_1.wav"));
            messageList.add(1,new String("sound/msg_2.wav"));
            messageList.add(2,new String("sound/msg_3.wav"));
        }
     }

    public Object getGreeting(String mailboxid,boolean isAdmin) {
        if (isAdmin)
            return new String ("sound/pressOne_UL.wav");
        else
            return new String("sound/welcome_UL.wav");
    }

    public ArrayList getMboxStat(String cnum) {
        ArrayList al = new ArrayList();
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i) != null) {
                al.add(new Integer(i));
            }
        }
        return al;
    }

    public ByteBuffer getMessage(String cnum, int messageId) {
        ByteBuffer message;
        try {
            message = (ByteBuffer) this.messageList.get(messageId);
            return message;
        } catch (IndexOutOfBoundsException e) {
            return null;     
        }

    }

    public String getMessageMediaLocator(String mailboxid, int messageId) {
        String messageLocator;       
        try {
            messageLocator = (String) this.messageList.get(messageId);
            return messageLocator;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void storeMessage(String mailboxid, byte[] msg) {
        messageList.add(ByteBuffer.wrap(msg));
    }

    public void storeMessage(String mailboxid, String mediaLocator) {
        // Build dummy graph

        Node root = null;
        Node current = null;
        Node next = null;
        messageList.add(mediaLocator);
    }

}
