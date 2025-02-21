/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.executor.eventhandler;

import com.mobeon.executor.Traverser;
import com.mobeon.executor.Scope;
import com.mobeon.event.types.MASUserInput;
import com.mobeon.event.types.MASExit;
import org.apache.log4j.Logger;

/**
 * Handle the user input
 */
public class UserInputHandler {
    static Logger logger = Logger.getLogger("com.mobeon");

    private Traverser traverser;
    public UserInputHandler(Traverser traverser){
        this.traverser = traverser;
    }

    public void handle(MASUserInput e) {
        traverser.getInputStream().interrupt(false);
        traverser.getOutputStream().interrupt(false);
    }
}
