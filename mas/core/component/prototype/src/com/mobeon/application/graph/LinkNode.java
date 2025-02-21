package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.application.util.Expression;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-mar-23
 * Time: 14:10:35
 */
public class LinkNode extends Node  {

    Node next = null;
    String nextattrib = null;
    Expression expr = null;
    String event = null;
    Expression eventexpr = null;
    String dtmf = null;
    String message = null;
    Expression messageexpr = null;


    public Node execute(Traverser traverser) {

        // todo: implement
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getNext() {
        return next;
    }


    public String getNextattrib() {
        return nextattrib;
    }

    public void setNextattrib(String nextattrib) {
        this.nextattrib = nextattrib;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Expression getEventexpr() {
        return eventexpr;
    }

    public void setEventexpr(Expression eventexpr) {
        this.eventexpr = eventexpr;
    }

    public String getDtmf() {
        return dtmf;
    }

    public void setDtmf(String dtmf) {
        this.dtmf = dtmf;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Expression getMessageexpr() {
        return messageexpr;
    }

    public void setMessageexpr(Expression messageexpr) {
        this.messageexpr = messageexpr;
    }
}
