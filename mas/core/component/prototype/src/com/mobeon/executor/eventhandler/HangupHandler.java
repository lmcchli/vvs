/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.executor.eventhandler;

import com.mobeon.executor.Traverser;
import com.mobeon.event.types.MASHangup;
import com.mobeon.frontend.Stream;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-25
 * Time: 10:35:53
 * To change this template use File | Settings | File Templates.
 */
public class HangupHandler {
    static Logger logger = Logger.getLogger("com.mobeon");
    
    private Traverser traverser;
    public HangupHandler(Traverser traverser){
        this.traverser = traverser;
    }
    public void handle(MASHangup e){
        // The user hung up
        traverser.setStopRunning(true);
        Stream s = traverser.getInputStream();
        if (s != null)
            s.interrupt(true);
        s = traverser.getOutputStream();
        if (s != null)
            s.interrupt(true);
        traverser.getSession().endConnection();

    }
}
