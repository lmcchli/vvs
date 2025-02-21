/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-22
 * Time: 18:20:32
 * To change this template use File | Settings | File Templates.
 */
public class LogNode extends Node{
    private Node next = null;
    public Node execute(Traverser traverser) {
        logger.debug("Executing");
        return next;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node child) {
        this.next = child;
    }
}
