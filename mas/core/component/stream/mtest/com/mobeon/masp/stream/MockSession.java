package com.mobeon.masp.stream;

import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.SessionMdcItems;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import org.apache.log4j.MDC;

class MockSession implements ISession {
    String id;

    public MockSession(String id) {
        this.id = id;
    }

    public String getId() {
        return id;

    }

    public String getUnprefixedId() {
        return null;
    }

    public void setId(Id<ISession> id) {
    }

    public void setMdcItems(SessionMdcItems sessionMdcItems) {
    }

    public void dispose() {
    }

    public void setData(String string, Object object) {
    }

    public Object getData(String string) {
        return id;
    }

    public void setSessionLogData(String string, Object object) {
    }

    public void registerSessionInLogger() {
        MDC.put("session", id);
    }

    public Id<ISession> getIdentity() {
        return null;  
    }
}

