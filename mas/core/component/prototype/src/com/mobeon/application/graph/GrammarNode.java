/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.executor.GrammarMatcher;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-28
 * Time: 13:57:52
 * To change this template use File | Settings | File Templates.
 */
public class GrammarNode  extends Node {
    Node next;
    ArrayList rules = new ArrayList();


    public Node execute(Traverser traverser) {
        logger.debug("Executing...");
        traverser.putGrammar(new GrammarMatcher(rules));
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

     public Node getNext() {
        return next;
    }
    public void addRule(String rule) {
        rules.add(rule);
    }

}
