/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-mar-07
 * Time: 10:12:51
 * To change this template use File | Settings | File Templates.
 */
public class RecordNode extends Node {
    Node next= null;
    String name;
    Expression expr = null;
    Cond cond = new Cond("true");
    boolean modal = true;
    boolean beep = false;
    int maxtime =600000; // in ms
    int finalsilencetime;
    boolean dtmfterm = true;
    ArrayList mimetype;
    private ArrayList prompts = new ArrayList();


    public Node execute(Traverser traverser) {
        String token = null;
        logger.debug("Executing");
        if (cond != null && cond.isCond(traverser.getEcmaExecutor())) {
            // Add empty list of prompts to use
            traverser.setRepromptCount(1);
            traverser.getFields().addSymbol("MAS_REPROMPT", prompts);
            traverser.setPromptPlayed(false);
            if (name != null) {
                if (traverser.getEcmaExecutor().getFromScope(name) != null) {
                    // todo: The record has a value in ECMA space. Return child node (?)
                    return next;    // todo: should be nextSibling
                }
                else {
                    return next;
                }
            }
            else {
                logger.error("Record has no name! Record therefor is in-accessable.");
                return next;
            }
        }
        else {
            return getNextSibling();
        }
    }

    public void setNext(Node child) {
        this.next = child;
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

    public Cond getCond() {
        return cond;
    }

    public void setCond(Cond cond) {
        this.cond = cond;
    }

    public boolean isModal() {
        return modal;
    }

    public void setModal(boolean modal) {
        this.modal = modal;
    }

    public boolean isBeep() {
        return beep;
    }

    public void setBeep(boolean beep) {
        this.beep = beep;
    }

    public int getMaxtime() {
        return maxtime;
    }

    public void setMaxtime(int maxtime) {
        this.maxtime = maxtime;
    }

    public int getFinalsilencetime() {
        return finalsilencetime;
    }

    public void setFinalsilencetime(int finalsilencetime) {
        this.finalsilencetime = finalsilencetime;
    }

    public boolean isDtmfterm() {
        return dtmfterm;
    }

    public void setDtmfterm(boolean dtmfterm) {
        this.dtmfterm = dtmfterm;
    }

    public ArrayList getMimetype() {
        return mimetype;
    }

    public void setMimetype(ArrayList mimetype) {
        this.mimetype = mimetype;
    }

    public ArrayList getPrompts() {
        return prompts;
    }

    public void setPrompts(ArrayList prompts) {
        this.prompts = prompts;
    }

    public void addPrompt(PromptNode pn) {
        prompts.add(pn);
    }
}
