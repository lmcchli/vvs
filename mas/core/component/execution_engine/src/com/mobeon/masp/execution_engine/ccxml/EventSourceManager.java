/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml;

import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.execution_engine.ccxml.runtime.Bridge;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.masp.execution_engine.session.ISession;

import java.util.Set;
import java.util.List;

/**
 * @author Mikael Andersson
 */
public interface EventSourceManager extends IEventReceiver {


    void forgetBridge(Bridge bridge);

    CCXMLExecutionContext getExecutionContext();

    void setOwner(CCXMLExecutionContext executionContext);

    void setSession(ISession session);

    Dialog findDialog(String dialogID);

    Connection findConnection(String connectionID);

    Connection findConnectionByCall(Call leg);

    Connection createConnection(Call leg, boolean outbound);

    void shutdownAll();

    void placeCall(Connection connection,
                   CallProperties callProperties);

    void join(Bridge bridge, boolean implicit);

    void setCallManager(CallManager callManager);

    void setEventDispatcher(IEventDispatcher dispatcher);

    void unjoin(Bridge bridge);

    List<Bridge> bridgesOf(Set<? super Connection> conns,Set<? super Dialog> dlgs);

    Bridge findBridge(BridgeParty in, BridgeParty out);

    BridgeParty findParty(String bridgePartyId);

    Dialog createDialog(String src,String mimeType);

    IEventDispatcher getEventDispatcher();

    Connection safeFindConnectionByCall(Call call);

    Connection safeFindConnection(String connectionID);

    void logInvalidEvent(Object ... params);

    /**
     * @return The initiating (first) connection of the session.
     */
    Connection getInitiatingConnection();

    CallManager getCallManager();
    
    public String getConnectionByLegAsString ();
}
