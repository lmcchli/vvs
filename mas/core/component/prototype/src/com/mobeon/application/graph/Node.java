/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * Interface used by the Traverser when iterating and executing the graph.
 */
public abstract class Node {
    public static Logger logger = Logger.getLogger("com.mobeon");

    private Node nextSibling = null;
    private Node parent = null;
    public abstract Node execute(Traverser traverser);
    public abstract void setNext(Node next);
    public abstract Node getNext();

    /**
     * Used when a GOTO node is reached, in order to "load/register" the names and values
     * of the document sub-nodes, according to the Field Interpretation Algorithm.
     * Only some nodes actually performes a task, most simply returns null.
     * @param traverser
     * @return
     */
    public Node load(Traverser traverser) {
        return null;    
    }

    public void setNextSibling(Node sibling) {
        nextSibling = sibling;
    }

    public Node getNextSibling() {
        if (nextSibling != null)
            return nextSibling;
        else {
            if (parent != null)
                return parent.getNextSibling();
            else
                return null;
        }
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent(){
        return parent;
    }
}
