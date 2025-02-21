/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon;

import com.mobeon.application.*;
import com.mobeon.application.graph.GraphFactory;
import com.mobeon.session.SessionServer;
import com.mobeon.session.SessionServerFactory;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-09
 * Time: 12:47:45
 * To change this template use File | Settings | File Templates.
 */
public class main {
    static Logger logger = Logger.getLogger(main.class);
    public static void main(String argv[]) {
        // Call the VXML compiler in order to build the executabel graph
        GraphFactory.getInstance().getApplication();
        logger.debug("Application compiled");
        SessionServer ss = SessionServerFactory.create();
        logger.debug("Starting server");
        ss.run();
    }
}
