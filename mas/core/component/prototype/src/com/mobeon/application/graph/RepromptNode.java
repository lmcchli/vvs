package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.util.PromptManager;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-mar-21
 * Time: 17:49:54
 */
public class RepromptNode extends  Node {
    Node next;
    Node parant;


    public Node execute(Traverser traverser) {

        logger.debug("Reprompt");

        traverser.setRepromptCount(traverser.getRepromptCount() + 1);
        traverser.setPromptPlayed(false);

        logger.debug("Reprompt count is " + traverser.getRepromptCount());

        return PromptManager.getPromptToReprompt(traverser);
    }



    public void setNext(Node next) {
        this.next = next;
    }

    public Node getNext() {
        return next;
    }
}
