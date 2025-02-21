/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.executor.eventhandler;

import com.mobeon.executor.Traverser;
import com.mobeon.event.types.MASTimeout;
import com.mobeon.frontend.Stream;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-25
 * Time: 10:57:19
 * To change this template use File | Settings | File Templates.
 */
public class TimeoutHandler {
    static Logger logger = Logger.getLogger("com.mobeon");
    
    private Traverser traverser;
    public TimeoutHandler(Traverser traverser){
        this.traverser = traverser;
    }
    public void handle(MASTimeout e) {
        Stream s;
        s = traverser.getInputStream();
        if (s != null)
             s.interrupt(false);
        s = traverser.getOutputStream();
        if (s != null)
             s.interrupt(false);
    }
}
