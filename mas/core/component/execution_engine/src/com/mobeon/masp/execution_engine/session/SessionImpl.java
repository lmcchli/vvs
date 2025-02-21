/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.session;

import com.mobeon.masp.execution_engine.INeedCloseOnSessionDispose;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGeneratorImpl;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Not implemented yet! Should implement at least a stub implementation. Add some glue to tigh it to the EE runtime classes
 * <p/>
 * TODO: Move this into it's proper location, which is _NOT_ in interface....
 * <p/>
 * Note: the class must be thread-safe since it can be accessed
 * from both callManager and EE for example.
 *
 * @author David Looberger
 */
public class SessionImpl implements ISession {
    private Map<String, Object> map = new HashMap<String, Object>(); // TODO: Change into a proper implementation that fits the rest of the EE runtime
    private Id<ISession> id = IdGeneratorImpl.SESSION_GENERATOR.generateId();
    private static ILogger logger = ILoggerFactory.getILogger(SessionImpl.class);
    private SessionMdcItems sessionMdcItems = new SessionMdcItems();


    public synchronized String getId() {
        return id.toString();
    }

    public String getUnprefixedId() {
        return id.getUnprefixedId();
    }

    public void setId(Id<ISession> id) {
        this.id = id;
    }

    public void setMdcItems(SessionMdcItems sessionMdcItems) {
        this.sessionMdcItems = sessionMdcItems;
    }

    public synchronized void dispose() {
        // TODO: Dispose the data held by the session. For now, simply empty the map
        for (Map.Entry e : map.entrySet()) {
            if (e.getValue() instanceof INeedCloseOnSessionDispose) {
                ((INeedCloseOnSessionDispose) e.getValue()).close();
            }
        }
        map.clear();
    }

    public synchronized void setData(String name, Object value) {
        map.put(name, value);
    }

    public synchronized Object getData(String name) {
        return map.get(name);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setSessionLogData(String name, Object value) {
        sessionMdcItems.setLogData(name, value);
    }

    public void registerSessionInLogger() {
        logger.registerSessionInfo("session", id.toString());
        sessionMdcItems.registerMdcItemsInLogger();
    }

    protected synchronized void finalize() throws Throwable {
        super.finalize();
        unregisterId();

    }

    public Id<ISession> getIdentity() {
        return id;
    }

    private synchronized void unregisterId() {
        if (id != null) {
            id.unregister();
            id = null;
        }
    }
}
