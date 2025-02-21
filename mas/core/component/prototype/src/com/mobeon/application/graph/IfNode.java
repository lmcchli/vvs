/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-22
 * Time: 18:24:17
 * To change this template use File | Settings | File Templates.
 */
public class IfNode extends Node {
    private Node truechild = null;
    private Node falsechild = null;
    private Cond cond = null;

    public Node execute(Traverser traverser) {
        logger.debug("Executing (Cond: " + cond.getCond() + ")");
        if (cond != null && cond.isCond(traverser.getEcmaExecutor())) {
            logger.debug("Selecting the TRUE child");
            return truechild;
        }
        else {
            logger.debug("Selecting the FALSE child");
            if (falsechild != null)
                return falsechild;
            else
                return getNextSibling();
        }
    }
    public void setNext(Node child) {
        // Do nothing
    }

    public void setBody(Node node) {
        truechild=node;
    }

    public void setElse(Node node) {
        falsechild=node;
    }

    public Cond getCond() {
        return cond;
    }

    public void setCond(Cond cond) {
        this.cond = cond;
    }

     public Node getNext() {
        return null;  // TODO: How to handle this?
    }
}
