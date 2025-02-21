/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.event.types.MASNoInput;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-mar-01
 * Time: 12:53:11
 * To change this template use File | Settings | File Templates.
 */
public class ValueRetrievalNode extends Node {
    private String varName = null;
    private int tokenTimespan = 0;
    private Node next = null;
    private long timeout = 0;


    public Node execute(Traverser traverser) {
        logger.debug("Executing...");        
        if (varName != null) {
            String value = traverser.getControlSignalQ().getToken(timeout, tokenTimespan);
            if (value == null) {
                traverser.getDispatcher().fire(new MASNoInput(this));
            }
            if (value != null) {
                if (traverser.getGrammar() != null) {
                    value = traverser.getGrammar().match(value, traverser);
                    if (value != null){
                        logger.debug("Storing object " + varName + " with value " + value);
                        traverser.getEcmaExecutor().putIntoScope(varName,value);
                    }
                    else {
                        logger.debug("Object " + varName + " already has a value in the scope!");
                    }
                }
                else {
                    logger.error("NO GRAMMAR!");
                    return null;
                }
            }
        }
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }
    public Node getNext() {
        return next;
    }
    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public int getTokenTimespan() {
        return tokenTimespan;
    }

    public void setTokenTimespan(int tokenTimespan) {
        this.tokenTimespan = tokenTimespan;
    }
}
