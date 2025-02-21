/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.executor.eventhandler;

import com.mobeon.executor.Traverser;
import com.mobeon.event.types.MASNoMatch;
import com.mobeon.application.graph.Node;
import com.mobeon.application.graph.PromptNode;
import com.mobeon.util.PromptManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-25
 * Time: 10:34:59
 * To change this template use File | Settings | File Templates.
 */
public class NoMatchHandler {
    public static Logger logger = Logger.getLogger("com.mobeon");

    private Traverser traverser;
    public NoMatchHandler(Traverser traverser){
        this.traverser = traverser;
    }

    public void handle(MASNoMatch e){
        logger.debug("No match");
        traverser.setRepromptCount(traverser.getRepromptCount() + 1);
        traverser.setPromptPlayed(false);
        logger.debug("Reprompt count is " + traverser.getRepromptCount());

        traverser.setRetakeNode(PromptManager.getPromptToReprompt(traverser));
        }
    
}
