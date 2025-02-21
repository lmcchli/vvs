/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.ecma.ECMAExecutor;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Expression;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-22
 * Time: 18:18:47
 * To change this template use File | Settings | File Templates.
 */
public class VarNode extends Node {
    private String name;
    private Expression expression;
    private Node next;

    public VarNode(String name, Expression expression) {
        this.name = name;
        this.expression = expression;
        next = null;
    }

    public void setNext(Node child) {
        this.next = child;
    }

     public Node getNext() {
        return next;
    }
    
    public Node execute(Traverser traverser) {
        logger.debug("Executing");
        ECMAExecutor ecma = traverser.getEcmaExecutor();
        ecma.putIntoScope(name, expression.eval(ecma));
        return next;
    }
    public Node load(Traverser traverser) {
        logger.debug("Loading");
        ECMAExecutor ecma = traverser.getEcmaExecutor();
        ecma.putIntoScope(name, expression.eval(ecma));
        return null;
    }
   
}
