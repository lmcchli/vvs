/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-mar-07
 * Time: 15:17:52
 * To change this template use File | Settings | File Templates.
 */
public class EndRecord extends Node {
    private Node next;
    public Node execute(Traverser traverser) {
        logger.debug("Executing");
        traverser.setPromptPlayed(false);
        traverser.setRepromptCount(1);
        ((ArrayList) traverser.getFields().getSymbol("MAS_REPROMPT")).clear();
        return next;
    }

    public void setNext(Node child) {
        next = child;
    }

    public Node getNext() {
        return next;
    }
}
