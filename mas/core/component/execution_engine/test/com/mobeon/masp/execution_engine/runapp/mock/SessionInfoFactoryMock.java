package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.operateandmaintainmanager.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.execution_engine.runapp.ApplicationBasicTestCase;
import com.mobeon.masp.util.test.MASTestSwitches;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author David Looberger
 */
public class SessionInfoFactoryMock implements SessionInfoFactory {

    public static class SessionKey {
        public SessionKey(String id, String connectionId){
            this.sessionID = id;
            this.connectionId = connectionId;
        }

        public boolean equals(Object obj){
            if(obj instanceof SessionKey){
                SessionKey key = (SessionKey) obj;
                return (connectionId.equals(key.connectionId) && sessionID.equals(key.sessionID));
            }
            return false;
        }
        public int hashCode(){
            return sessionID.length()+connectionId.length();
        }
        public String getSessionID() {
            return sessionID;
        }
        public String getConnectionId() {
            return connectionId;
        }
        private String sessionID;
        private String connectionId;
    }

    private Boolean monitoring = Boolean.TRUE;
    private final Map<SessionKey, SessionInfo> sis = new ConcurrentHashMap<SessionKey, SessionInfo>();

    public final static Map<SessionKey, SessionInfo> returnedSessionInfos =  new ConcurrentHashMap<SessionKey, SessionInfo>();

    static final ILogger logger = ILoggerFactory.getILogger(SessionInfoFactoryMock.class);

    public SessionInfo getSessionInstance(String id, String connectionId) {
        SessionKey key = new SessionKey(id, connectionId);

        // Check that EE does not try to get a sessionInfo after it has returned it
        if(returnedSessionInfos.containsKey(key)){
            logger.error("Invoking getSessionInstance for id "+id+","+connectionId+", but it has already been returned");
        }
        if (!sis.containsKey(key)) {
            SessionInfoMock si = new SessionInfoMock(id, connectionId);
            sis.put(key, si);
        }
        return sis.get(key);
    }

    public void returnSessionInstance(SessionInfo sessionInfo) {
        SessionKey key = new SessionKey(sessionInfo.getSessionId(), sessionInfo.getConnectionId());
        // Check that it is not already returned
        if(returnedSessionInfos.containsKey(key)){
            logger.error("Invoking returnSessionInstance for id "+sessionInfo.getSessionId()+","+
                    sessionInfo.getConnectionId()+", but it has already been returned");
        }
        sis.remove(key);
        returnedSessionInfos.put(key, sessionInfo);
    }

    public Boolean monitoring() {
        return monitoring;
    }

    public void setMonitoring(boolean ismonitoring) {
        monitoring = ismonitoring;
    }

    public static void reset() {
        returnedSessionInfos.clear();
    }

    public static Map<SessionKey,SessionInfo> getReturnedSessionInfos() {
        return returnedSessionInfos;
    }
}
