/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.application.util.Expression;

/**
 * Created by IntelliJ IDEA.
 * User: HemPC
 * Date: 2005-mar-19
 * Time: 10:36:08
 * To change this template use File | Settings | File Templates.
 */
public class ParamNode extends Node {
    private Node next;
    private String name = null;
    private Expression expr = null;

    public Node execute(Traverser traverser) {
        return next;
    }

    public void setNext(Node child) {
        next = child;
    }

    public Node getNext() {
        return next;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public void setValue(String value) {
        expr = new Expression(value);
    }
}
