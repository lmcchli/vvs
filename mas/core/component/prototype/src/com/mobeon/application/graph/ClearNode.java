/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.ecma.ECMAExecutor;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-mar-16
 * Time: 14:39:26
 * To change this template use File | Settings | File Templates.
 */
public class ClearNode extends Node{
    Node next = null;
    ArrayList list = new ArrayList();
    public Node execute(Traverser traverser) {
        ECMAExecutor ecma = traverser.getEcmaExecutor();
        for (Iterator it = list.iterator(); it.hasNext(); ){
            String name = (String) it.next();
            ecma.delete(name);
        }
        traverser.setPromptPlayed(false);
        traverser.setRepromptCount(1);
        return next;
    }

    public void setNext(Node child) {
        next = child;
    }

    public Node getNext() {
        return next;
    }

    public ArrayList getList() {
        return list;
    }

    public void setList(ArrayList list) {
        this.list = list;
    }
}
