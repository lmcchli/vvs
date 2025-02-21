package com.mobeon.masp.execution_engine.ccxml;

import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Mikael Andersson
 */
public interface BridgeParty {

    public interface EventSender {
        public void sendEvent(CCXMLEvent event);
    }

    static Lock BRIDGE_LOCK = new ReentrantLock();

    String getBridgePartyId();

    String getSessionId();

    boolean join(BridgeParty otherParty, boolean fullDuplex, boolean implicit);

    boolean unjoin(BridgeParty otherParty);

    void sendEvent(CCXMLEvent event);

    void sendEvent(CCXMLEvent event, EventTarget ccxml);

    enum EventTarget {
        CONTEXT,
        CCXML
    }
}
