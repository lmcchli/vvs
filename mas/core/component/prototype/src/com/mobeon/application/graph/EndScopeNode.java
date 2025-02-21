/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-23
 * Time: 10:26:33
 * To change this template use File | Settings | File Templates.
 */
public class EndScopeNode extends Node{
    private Node next = null;
    public Node execute(Traverser traverser) {
        logger.debug("Executing");
        traverser.leaveScope(false);
        return next;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node child) {
        this.next = child;
    }
}
