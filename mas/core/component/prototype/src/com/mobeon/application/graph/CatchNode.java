/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;

import java.util.List;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-22
 * Time: 18:20:12
 * To change this template use File | Settings | File Templates.
 */
public class CatchNode extends Node {
    private Node next = null;
    private Node exceptionHandlerNode = null;
    private List events = null;

    public CatchNode() {
    }

    public CatchNode(List events) {
        this.events = events;
    }

    public Node execute(Traverser traverser) {
        logger.debug("Executing");
        for(Iterator i = events.iterator(); i.hasNext();) {
            String event = (String) i.next();
            traverser.getExceptionScope().addSymbol(event, exceptionHandlerNode);
        }
        return getNextSibling();
    }

    public Node load(Traverser traverser){
        logger.debug("Loading");
        for (Iterator i = events.iterator(); i.hasNext();) {
          String event = (String) i.next();
         traverser.getExceptionScope().addSymbol(event, exceptionHandlerNode);
        }

        return null;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node child) {
        this.next = child;
    }

    public List getEvent() {
        return events;
    }

    public void setEvent(List event) {
        this.events = event;
    }

    public Node getExceptionHandlerNode() {
        return exceptionHandlerNode;
    }

    public void setExceptionHandlerNode(Node exceptionHandlerNode) {
        this.exceptionHandlerNode = exceptionHandlerNode;
    }
}
