/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-mar-21
 * Time: 11:11:37
 * To change this template use File | Settings | File Templates.
 */
public class EndIfNode extends Node{
    Node next = null;

    public Node execute(Traverser traverser) {
        logger.debug("Executing");
        return next;
    }

    public void setNext(Node child) {
        next = child;
    }

    public Node getNext() {
        return next;
    }
}
