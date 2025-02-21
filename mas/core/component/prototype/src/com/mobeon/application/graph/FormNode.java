/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;
import com.mobeon.ecma.ECMAExecutor;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-22
 * Time: 18:22:48
 * To change this template use File | Settings | File Templates.
 */
public class FormNode extends Node implements Gotoable {
    private Node nextSibling = null;
    private Node next = null;
    private Cond cond = null;
    private String id = null;
    private ArrayList children = new ArrayList();

    public Node execute(Traverser traverser) {
        if (id != null)
            logger.debug("Executing " + id);
        else
            logger.debug("Executing");
        traverser.newScope(false);
        traverser.clearDialogGotoables();
        for (Iterator it = children.iterator(); it.hasNext();){
            Node child = (Node) it.next();
            if (child instanceof Gotoable)
                traverser.addDialogGotoable(((Gotoable)child).getName(), child);
        }
        return next;
    }

    public Node load(Traverser traverser) {
        logger.debug("Loading");
        if (id != null)
            traverser.addDocGotoable(id, this);
       
        return null;
    }

    public Node getNextSibling() {
        return nextSibling;
    }

    public void setNextSibling(Node node) {
        this.nextSibling = node;
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
        return id;
    }

    public void setName(String id) {
        this.id = id;
    }

    public void addChild(Node node) {
        children.add(node);
    }
}
