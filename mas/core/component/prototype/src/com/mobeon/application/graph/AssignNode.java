package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.application.util.Expression;
import com.mobeon.ecma.ECMAExecutor;
import com.mobeon.event.types.MASError;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-mar-09
 * Time: 16:05:04
 */
public class AssignNode extends Node {

    private Expression expr;
    private String name;
    private Node next;


    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Node execute(Traverser traverser) {
        ECMAExecutor ecma = traverser.getEcmaExecutor();
        
        Object obj = expr.eval(ecma);
        ecma.putIntoScope(name, obj);

        return next;
    }

    public void setNext(Node _next) {
        next = _next;
    }

    public Node getNext() {
        return next;
    }
}
