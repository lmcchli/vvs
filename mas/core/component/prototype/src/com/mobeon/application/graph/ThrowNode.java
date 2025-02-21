/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.application.util.Expression;
import com.mobeon.event.MASEventDispatcher;
import com.mobeon.event.types.*;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-mar-21
 * Time: 14:51:42
 * To change this template use File | Settings | File Templates.
 */
public class ThrowNode extends Node{
    private Node next = null;
    private String event;
    private Expression eventExpression;
    private String message;
    private Expression messageExpression;


    public Node execute(Traverser traverser) {
        logger.debug("Execute");

        String eventSymbol = null;
        Node exceptionHandlerNode = null;
        HashMap handlers = traverser.getExceptionScope().getValues();
        java.lang.String evName = null;
        java.lang.String msg = null;

        if (messageExpression != null) {
           msg = (java.lang.String) eventExpression.eval(traverser.getEcmaExecutor());
        }
        else
            msg = message;

        if (msg == null)
            msg = "";

        if (eventExpression != null) {
            eventSymbol = (java.lang.String) eventExpression.eval(traverser.getEcmaExecutor());
        }
        else
            eventSymbol = event;

        for (Iterator it = handlers.keySet().iterator(); it.hasNext();){
            evName = (java.lang.String)it.next();
            if (eventSymbol.startsWith(evName)) {
                exceptionHandlerNode = (Node) handlers.get(evName);
                break;
            }
            else if (evName.equals(".")){
                exceptionHandlerNode = (Node) handlers.get(evName);
                break;
            }
        }

        // Try to call a default handler if no handler have been found yet,
        // or bail out with an error message otherwise
        if (exceptionHandlerNode == null &&
                !getDetfaultHandler(traverser, eventSymbol, msg)) {
            logger.error("No event handler for event " + eventSymbol + " is installed! Bailing out!");
            return null;
        }
        else
            return exceptionHandlerNode;
    }

    public void setNext(Node child) {
       next = child;
    }

    public Node getNext() {
        return next;
    }

    private boolean getDetfaultHandler(Traverser traverser, java.lang.String eventSymbol, java.lang.String msg) {

        MASEventDispatcher dispatcher = traverser.getDispatcher();
        if (eventSymbol.equals("cancel"))
            dispatcher.fire(new MASCancel(msg));
        else if (eventSymbol.startsWith("error"))
            dispatcher.fire(new MASError(msg));
        else if (eventSymbol.equals("exit"))
            dispatcher.fire(new MASExit(msg));
        else if (eventSymbol.equals("hangup"))
            dispatcher.fire(new MASHangup(msg));
        else if (eventSymbol.equals("help"))
            dispatcher.fire(new MASHelp(msg));
        else if (eventSymbol.equals("noinput"))
            dispatcher.fire(new MASNoInput(msg));
        else if (eventSymbol.equals("timeout"))
            dispatcher.fire(new MASTimeout(msg));
        else if (eventSymbol.equals("transfer"))
            dispatcher.fire(new MASTransfer(msg));
        else
            return false;

        return true;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Expression getEventExpression() {
        return eventExpression;
    }

    public void setEventExpression(Expression eventExpression) {
        this.eventExpression = eventExpression;
    }

    public java.lang.String getMessage() {
        return message;
    }

    public void setMessage(java.lang.String message) {
        this.message = message;
    }

    public Expression getMessageExpression() {
        return messageExpression;
    }

    public void setMessageExpression(Expression messageExpression) {
        this.messageExpression = messageExpression;
    }
}
