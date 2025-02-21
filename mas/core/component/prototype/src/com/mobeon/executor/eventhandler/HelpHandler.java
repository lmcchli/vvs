/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.executor.eventhandler;

import com.mobeon.executor.Traverser;
import com.mobeon.event.types.MASHelp;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-25
 * Time: 10:32:58
 * To change this template use File | Settings | File Templates.
 */
public class HelpHandler {
    static Logger logger = Logger.getLogger("com.mobeon");

    private Traverser traverser;
    public HelpHandler(Traverser traverser){
        this.traverser = traverser;
    }

    public void handle(MASHelp e){
       logger.debug("HELP REQUSTED!!!! (But I don't know how to provide it...)");
    }
}
