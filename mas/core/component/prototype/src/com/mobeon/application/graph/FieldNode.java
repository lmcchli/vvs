/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.executor.GrammarMatcher;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Expression;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-feb-28
 * Time: 12:38:47
 */
public class FieldNode extends Node implements Gotoable {

    private Node next = null;
    private String name = null;
    private Cond cond = new Cond("true");
    private Expression expr = null;
    private String type = null;
    private String slotname; // Should default to the field name
    private boolean modal = false;
    private Node valueRetrieval = null;
    private ArrayList prompts = new ArrayList();


    public void setNext(Node _next) {
        next = _next;
    }

    public Node getNext() {
        return next;
    }

    public Node execute(Traverser traverser) {
        String token = null;
        if (name != null)
            logger.debug("Executing " +name);
        else
            logger.debug("Executing");
        if (cond != null && cond.isCond(traverser.getEcmaExecutor())) {
            // Add empty list of prompts to use
            traverser.setRepromptCount(1);
            traverser.getFields().addSymbol("MAS_REPROMPT", prompts);
            traverser.setPromptPlayed(false);
            if (name != null) {
                if (traverser.getEcmaExecutor().getFromScope(name) != null) {
                    // todo: The field has a value in ECMA space. Return child node (?)
                    return next;    // todo: should be nextSibling
                }
                else {  // todo: No value yet in ECMA space. Evaluate expression
                    if (expr != null) {
                        Object value = expr.eval(traverser.getEcmaExecutor());
                        traverser.getEcmaExecutor().putIntoScope(name, value);
                        return next;
                    }
                    else {
                        logger.error("No expression! Can not set a value for the field.");
                        return next;
                    }
                }
            }
            else {
                logger.error("Field has no name! Field is in-accessable.");
                return next;
            }
        }
        else {
            return getNextSibling();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cond getCond() {
        return cond;
    }

    public void setCond(Cond cond) {
        this.cond = cond;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSlotname() {
        return slotname;
    }

    public void setSlotname(String slotname) {
        this.slotname = slotname;
    }

    public boolean isModal() {
        return modal;
    }

    public void setModal(boolean modal) {
        this.modal = modal;
    }

    public Node getValueRetrieval() {
        return valueRetrieval;
    }

    public void setValueRetrieval(Node valueRetrieval) {
        this.valueRetrieval = valueRetrieval;
    }

    public void addPrompt(PromptNode pn) {
        prompts.add(pn);
    }

    public ArrayList getPrompts() {
        return prompts;
    }
}
