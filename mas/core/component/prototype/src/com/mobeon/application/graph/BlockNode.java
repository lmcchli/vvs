/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;
import com.mobeon.ecma.ECMAExecutor;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-22
 * Time: 18:23:15
 * To change this template use File | Settings | File Templates.
 */
public class BlockNode extends Node implements Gotoable {
    private Node nextSibling = null;
    private Node next = null;
    private Cond cond = null;
    private String name = null;
    private ArrayList children = new ArrayList();

    public Node execute(Traverser traverser) {
        if (name != null)
            logger.debug("Executing " + name);
        else
            logger.debug("Executing");
        if (isAvailable(traverser.getEcmaExecutor()))  {
            traverser.newScope(false);
            return next;
        }
        else {
            return nextSibling;
        }
    }

    public Node load(Traverser traverser){
        logger.debug("Loading");
        if (name != null)
            traverser.addDocGotoable(name, this);
        return null;
    }

    public Node getNextSibling() {
        return nextSibling;
    }

    public void setNextSibling(Node node) {
       nextSibling = node;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Cond getCond() {
        return cond;
    }

    public void setCond(Cond cond) {
        this.cond = cond;
    }

    private boolean isAvailable(ECMAExecutor exec) {
        if (cond != null)
            return cond.isCond(exec);
        else
           return true; // If no condition, we should be executed
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addChild(Node n){
        children.add(n);
    }

    public ArrayList getChildren() {
        return children;
    }
}
