package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-feb-24
 * Time: 16:03:22
 */
public class PromptNode extends Node{
    private Node next = null;
    private Node nextSibling = null;
    private int count = 1;
    private Cond cond = new Cond("true");
    private long timeout = 0;
    private boolean inField = false;


    public Node execute(Traverser traverser) {
        if (inField && traverser.isPromptPlayed()) {
            logger.debug("Prompt for the field already played! Going to next sibling");
            return getNextSibling();
        }
        logger.debug("Executing");
        logger.debug("Reprompt count : " + traverser.getRepromptCount() + " (count for this node: " + count + ")");
        if (traverser.getRepromptCount() < count ||
                ! cond.isCond(traverser.getEcmaExecutor())) {
            logger.debug("Returning nextSibling");
            return  getNextSibling();
        }

        traverser.setPromptPlayed(true);
        return next;
    }

    public void setNext(Node child) {
        this.next = child;

    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Cond getCond() {
        return cond;
    }

    public void setCond(Cond cond) {
        this.cond = cond;
    }

     public Node getNext() {
        return next;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isInField() {
        return inField;
    }

    public void setInField(boolean inField) {
        this.inField = inField;
    }
}
