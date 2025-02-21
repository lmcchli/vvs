package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.operateandmaintainmanager.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.execution_engine.runapp.ApplicationBasicTestCase;

import java.util.List;
import java.util.ArrayList;

/**
 * @author David Looberger
 */
public class SessionInfoMock implements SessionInfo {

    String service = "";
    String sessionInit = "";
    CallType callType;
    CallState connectionState = CallState.DISCONNECTED;
    CallDirection direction;
    CallActivity outboundActivity = CallActivity.IDLE;
    CallActivity inboundActivity = CallActivity.IDLE;
    String ani = "";
    String dnis = "";
    String rdnis = "";
    String prop = "";
    private static final ILogger log = ILoggerFactory.getILogger(SessionInfoMock.class);
    private String sessionId;
    private List<CallActivity> outboundActivityHistory = new ArrayList<CallActivity>();
    private List<CallActivity> inboundActivityHistory = new ArrayList<CallActivity>();
    private String connectionID;

    public SessionInfoMock(String sessionId, String connectionID) {
        this.sessionId = sessionId;
        this.connectionID = connectionID;
        ApplicationBasicTestCase.setSessionInfoMock(this);
    }

    public void setService(String service) throws IlegalSessionInstanceException {
        this.service = service;
        printInfo();
    }

    public void setSessionInitiator(String sessionInit) throws IlegalSessionInstanceException {
        this.sessionInit = sessionInit;
        printInfo();
    }

    public void setConnetionType(CallType callType) throws IlegalSessionInstanceException {
        this.callType = callType;
        printInfo();
    }

    public void setConnetionState(CallState connectionState) throws IlegalSessionInstanceException {
        this.connectionState = connectionState;
        printInfo();
    }

    public void setDirection(CallDirection direction) throws IlegalSessionInstanceException {
        this.direction = direction;
        printInfo();
    }

    public void setOutboundActivity(CallActivity outboundActivity) throws IlegalSessionInstanceException {
        this.outboundActivity = outboundActivity;
        printInfo();
        outboundActivityHistory.add(outboundActivity);
    }

    public List<CallActivity> getOutboundActivityHistory(){
        return outboundActivityHistory;
    }

    public List<CallActivity> getInboundActivityHistory(){
        return inboundActivityHistory;
    }

    public void setInboundActivity(CallActivity inboundActivity) throws IlegalSessionInstanceException {
        this.inboundActivity = inboundActivity;
        printInfo();
        inboundActivityHistory.add(inboundActivity);
    }

    public void setANI(String ani) throws IlegalSessionInstanceException {
        this.ani = ani;
        printInfo();
    }

    public void setDNIS(String dnis) throws IlegalSessionInstanceException {
        this.dnis = dnis;
        printInfo();
    }

    public void setRDNIS(String rdnis) throws IlegalSessionInstanceException {
        this.rdnis = rdnis;
        printInfo();

    }

    public void setFarEndConProp(String prop) throws IlegalSessionInstanceException {
        this.prop = prop;
        printInfo();

    }

    public String getSessionId() {
        return sessionId;
    }

    public String getConnectionId() {
        return connectionID;
    }

    private void printInfo() {
        String msg = "SessionInfo (" + sessionId + ", "+connectionID + ") " +
                service + " " +
                sessionInit + " " +
                callType + " " +
                connectionState + " " +
                direction + " " +
                outboundActivity + " " +
                inboundActivity + " " +
                ani + " " +
                dnis + " " +
                rdnis + " " +
                prop;
        log.info(msg);

    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public String getService() {
        return service;
    }

    public String getSessionInit() {
        return sessionInit;
    }

    public CallType getCallType() {
        return callType;
    }

    public CallState getConnectionState() {
        return connectionState;
    }

    public CallDirection getDirection() {
        return direction;
    }

    public CallActivity getOutboundActivity() {
        return outboundActivity;
    }

    public CallActivity getInboundActivity() {
        return inboundActivity;
    }

    public String getAni() {
        return ani;
    }

    public String getDnis() {
        return dnis;
    }

    public String getRdnis() {
        return rdnis;
    }

    public String getProp() {
        return prop;
    }


}
