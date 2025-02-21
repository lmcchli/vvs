/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.session;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * Container of MDC data for logging.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class SessionMdcItems {

    private Map<String, Object> mdcItems = new HashMap<String, Object>();
    private ILogger logger = ILoggerFactory.getILogger(getClass());


    public synchronized void setLogData(String name, Object value) {
        mdcItems.put(name, value);
    }

    public synchronized void registerMdcItemsInLogger() {
        for (String key : mdcItems.keySet()) {
            logger.registerSessionInfo(key, mdcItems.get(key));
        }
    }

}
