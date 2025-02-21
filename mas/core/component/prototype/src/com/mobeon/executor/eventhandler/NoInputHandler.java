/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.executor.eventhandler;

import com.mobeon.executor.Traverser;
import com.mobeon.event.types.MASNoInput;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-25
 * Time: 10:34:33
 * To change this template use File | Settings | File Templates.
 */
public class NoInputHandler {
    static Logger logger = Logger.getLogger("com.mobeon");
    
    private Traverser traverser;
    public NoInputHandler(Traverser traverser){
        this.traverser = traverser;
    }

    public void handle(MASNoInput e){
        // todo: Find last prompt, and execut it again
    }
}
