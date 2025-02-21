/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.system.mockobjects;

import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.SessionMdcItems;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.MediaMimeTypes;

import jakarta.activation.MimeType;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A mock of ISession used for testing.
 * This class is thread-safe.
 */
public class SessionMock implements ISession {
    private final ILogger log = ILoggerFactory.getILogger(getClass());
    private AtomicReference<CallMediaTypes[]> callMediaTypesArray =
            new AtomicReference<CallMediaTypes[]>();
    private AtomicReference<CallMediaTypes> selectedCallMediaTypes =
            new AtomicReference<CallMediaTypes>();
    private AtomicReference<Id<ISession>> id = new AtomicReference<Id<ISession>>();
    private AtomicReference<SessionMdcItems> sessionMdcItems =
            new AtomicReference<SessionMdcItems>(new SessionMdcItems());

    MediaMimeTypes mediaMimeTypes = new MediaMimeTypes(new MimeType("audio", "pcmu"));

    public SessionMock() throws Exception {
        clearSessionData();
    }

    public void clearSessionData() {
        callMediaTypesArray.set(new CallMediaTypes[] {
                new CallMediaTypes(mediaMimeTypes, null)});
        selectedCallMediaTypes.set(null);
        id.set(null);
        sessionMdcItems.set(new SessionMdcItems());
    }

    public String getId() {
        return id.get() != null ? id.get().toString() : "SID";
    }

    public String getUnprefixedId() {
        return id.get() != null ? id.get().toString() : "SID";
    }

    public void setSessionLogData(String name, Object value) {
        sessionMdcItems.get().setLogData(name, value);
    }

    public void registerSessionInLogger() {
        if (id.get() != null)
            log.registerSessionInfo("session", id.get().toString());
        sessionMdcItems.get().registerMdcItemsInLogger();
    }

    public Id<ISession> getIdentity() {
        return null;
    }

    public void setId(Id<ISession> id) {
        this.id.set(id);
    }

    public void setMdcItems(SessionMdcItems sessionMdcItems) {
        this.sessionMdcItems.set(sessionMdcItems);
    }

    public void dispose() {
    }

    public synchronized void setData(String name, Object value) {
        if (name.equals("callmediatypesarray")) {
            callMediaTypesArray.set((CallMediaTypes[])value);
        } else if (name.equals("selectedcallmediatypes")) {
            selectedCallMediaTypes.set((CallMediaTypes)value);
        }
    }

    public synchronized Object getData(String name) {
        if (name.equals("callmediatypesarray")) {
            return callMediaTypesArray.get();
        } else if (name.equals("selectedcallmediatypes")) {
            return selectedCallMediaTypes.get();
        }
        return null;
    }

    public Map<String, Object> getMap() {
        return null;
    }
}
