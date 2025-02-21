/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.executor.SuspendHandler;
import com.mobeon.application.util.Expression;
import com.mobeon.application.vxml.Transformer;
import com.mobeon.application.vxml.ThrowableEvent;
import com.mobeon.ecma.ECMAExecutor;
import com.mobeon.event.types.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: HemPC
 * Date: 2005-mar-19
 * Time: 15:25:20
 * To change this template use File | Settings | File Templates.
 */
public class ReturnNode extends Node{
    Node next = null;
    ThrowableEvent event = null;
    Expression eventExpr = null;
    java.lang.String message = null;
    Expression messageExpr = null;
    ArrayList namelist = new ArrayList();


    public Node execute(Traverser traverser) {
        HashMap values = new HashMap();
        ECMAExecutor ecma = traverser.getEcmaExecutor();
        java.lang.String msg = null;
        if (msg != null)
            msg = message;
        else if (messageExpr != null) {
            msg = (java.lang.String) messageExpr.eval(ecma);
        }

        if (namelist.size() > 0) {
            for (Iterator it = namelist.iterator(); it.hasNext();){
                java.lang.String name = (java.lang.String) it.next();
                Object val = ecma.exec(name);
                values.put(name, val);
            }
        }
        else if (event != null) {
            MASEvent ev = createEvent(event, msg);
            traverser.getDispatcher().fire(ev);
        }
        else if (eventExpr != null) {
            java.lang.String evName = (java.lang.String) eventExpr.eval(ecma);
            ThrowableEvent tev = Transformer.createThrowableEvent(evName);
            MASEvent ev = createEvent(event, msg);
            traverser.getDispatcher().fire(ev);
        }

        SuspendHandler sh = traverser.resume();
        logger.debug("Executing (Calldepth is " + (traverser.getDocScopeCounter() + traverser.getRootScopeCounter()) + ")");

        // Retrieve a new ECMAExecutor handle after resuming
        ecma = traverser.getEcmaExecutor();
        if (sh != null) {
            next = sh.getNext();
            java.lang.String subDialogName = sh.getSubDialogName();
            ecma.exec(subDialogName + " =  new Object();");
            for (Iterator it = values.keySet().iterator(); it.hasNext();){
                java.lang.String name = (java.lang.String) it.next();
                Object val = values.get(name);
                ecma.exec(subDialogName + "." + name + " = " +  val + ";");
                Object obj = ecma.exec(subDialogName + "." + name + ";");
            }
        }
        return next;
    }

    public void setNext(Node child) {
        next = child;
    }

    public Node getNext() {
        return next;
    }

    public ThrowableEvent getEvent() {
        return event;
    }

    public void setEvent(ThrowableEvent event) {
        this.event = event;
    }

    public Expression getEventExpr() {
        return eventExpr;
    }

    public void setEventExpr(Expression eventExpr) {
        this.eventExpr = eventExpr;
    }

    public java.lang.String getMessage() {
        return message;
    }

    public void setMessage(java.lang.String message) {
        this.message = message;
    }

    public Expression getMessageExpr() {
        return messageExpr;
    }

    public void setMessageExpr(Expression messageExpr) {
        this.messageExpr = messageExpr;
    }

    public ArrayList getNamelist() {
        return namelist;
    }

    public void addName(java.lang.String name ) {
        namelist.add(name);
    }

    private MASEvent createEvent(ThrowableEvent e, String msg) {
        if (e instanceof Error) {
            return new MASError(msg);
        }
        else if (e instanceof MASExit) {
            return new MASExit(msg);
        }
        else if (e instanceof MASNoInput) {
            return new MASNoInput(msg);
        }
        else if (e instanceof MASNoMatch) {
            return new MASNoMatch(msg);
        }
        return null;
        // TODO: Add all other MASEvents


    }
}
