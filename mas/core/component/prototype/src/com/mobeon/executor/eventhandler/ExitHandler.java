/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.executor.eventhandler;

import com.mobeon.executor.Traverser;
import com.mobeon.event.types.MASExit;
import com.mobeon.frontend.Stream;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-25
 * Time: 10:32:08
 * To change this template use File | Settings | File Templates.
 */
public class ExitHandler {
    static Logger logger = Logger.getLogger("com.mobeon");

    private Traverser traverser;
    public ExitHandler (Traverser traverser){
        this.traverser = traverser;
    }
    public void handle(MASExit e){
        // Terminate connection
        traverser.setStopRunning(true);
        /*
        Stream s = traverser.getInputStream();
        if (s != null)
            s.interrupt(true);
        s = traverser.getOutputStream();
        if (s != null)
            s.interrupt(true);
            */
        if (traverser.getSession() != null)
            traverser.getSession().endConnection();
    }
}
