/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * TODO: Drop 6! Document
 *
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class SspStatus {

    private static final ILogger log = ILoggerFactory.getILogger(SspStatus.class);

    private static final SspStatus INSTANCE = new SspStatus();

    private final List<SspInstance> sspList = new ArrayList<SspInstance>();
    /**
     * @return The single SspStatus instance.
     */
    public static SspStatus getInstance() {
        return INSTANCE;
    }

    /**
     * Creates the single SspStatus instance.
     */
    private SspStatus() {
    }

    public synchronized void addSpp(SspInstance sspInstance) {
        sspList.add(sspInstance);

        if (log.isDebugEnabled())
            log.debug("SSP instance was added to list of registered SSPs. (" +
                    sspInstance + ").");
    }

    public synchronized void removeSpp(SspInstance sspInstance) {
        sspList.remove(sspInstance);

        if (log.isDebugEnabled())
            log.debug("SSP instance was removed from list of registered SSPs. (" +
                    sspInstance + ").");
    }

    public synchronized SspInstance getRandomSsp() {
        SspInstance sspInstance = null;

        if (sspList.size() > 0) {
            Collections.shuffle(sspList);
            sspInstance = sspList.get(0);
        }

        if (log.isDebugEnabled())
            log.debug("Get random SSP returned this SSP: " + sspInstance + ".");

        return sspInstance;
    }

    public synchronized int getNumberOfRegisteredSsp() {
        return sspList.size();
    }

    public synchronized void clear() {
        for (SspInstance sspInstance : sspList) {
            sspInstance.cancelTimers();
        }
        sspList.clear();
    }
}
