/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-mar-01
 * Time: 18:35:35
 * To change this template use File | Settings | File Templates.
 */
public class TransferNode extends Node {
    Node next = null;
    public Node execute(Traverser traverser) {
        return next;
    }

    public void setNext(Node child) {

    }
     public Node getNext() {
        return next;
    }

    private Node parent = null;

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent() {
        return parent;
    }
       private Node sibling = null;

    public Node getNextSibling() {
        return sibling;
    }

    public void setNextSibling(Node sibling) {
        this.sibling = sibling;
    }
}
