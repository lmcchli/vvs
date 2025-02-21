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
 * Time: 17:38:54
 * To change this template use File | Settings | File Templates.
 */
public class ValueNode extends Node{
    Node next = null;
    Expression expr = null;

    public Node execute(Traverser traverser) {
        logger.debug("Executing");
        if (expr != null) {
            Object result = expr.eval(traverser.getEcmaExecutor());
            logger.debug("VALUE = " + result.toString());
        }
        return next;
    }

    public ValueNodeReturn eval(Traverser traverser) {
      logger.debug("Evaluating");
        Node nextnode = next;
        ValueNodeReturn vrn = null;
        if (expr != null) {
            Object objresult = expr.eval(traverser.getEcmaExecutor());
            String result = objresult.toString();

            if (next instanceof ValueNode) {
                vrn = ((ValueNode) next).eval(traverser);
                vrn.setValue(result + vrn.getValue());
            }
            else {
                vrn = new ValueNodeReturn();
                vrn.setNext(next);
                vrn.setValue(result);
            }
        }
        return vrn;
    }

    public void setNext(Node child) {
        next = child;
    }

    public Node getNext() {
        return next;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public class ValueNodeReturn {
        Node next = null;
        String value = null;

        public ValueNodeReturn() {
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
