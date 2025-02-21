package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Expression;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-mar-09
 * Time: 10:52:21
 */
public class ExitNode extends Node {

    public List getNamelist() {
        return namelist;
    }

    public void setNamelist(List namelist) {
        this.namelist = namelist;
    }

    List namelist;
    Expression expr;

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }


    public Node execute(Traverser traverser) {
        return null;
    }

    public void setNext(Node child) {

    }

    public Node getNext() {
        return null;
    }
}
