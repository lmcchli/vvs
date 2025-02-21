package com.mobeon.masp.stream.mock;

import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.SessionMdcItems;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;

public class MockCallSession implements ISession {
    public static ISession getSession() {
        return new MockCallSession();
    }
    public String getId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUnprefixedId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setId(Id<ISession> id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMdcItems(SessionMdcItems sessionMdcItems) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setData(String string, Object object) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getData(String string) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSessionLogData(String string, Object object) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void registerSessionInLogger() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Id<ISession> getIdentity() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
