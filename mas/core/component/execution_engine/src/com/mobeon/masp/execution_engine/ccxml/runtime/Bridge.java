package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.execution_engine.ccxml.BridgeParty;
import com.mobeon.masp.execution_engine.ccxml.EventSourceManager;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;

/**
 * @author Mikael Anderson
 */
public class Bridge {
    private BridgeParty.EventSender bridgeErrorSender;
    private BridgeParty in;
    private BridgeParty out;
    private boolean implicit;
    private Duplex duplex;
    private boolean isRealized;
    private EventSourceManager eventSourceManager;

    public boolean isHomogen(Class<Connection> aClass) {
        return aClass.isInstance(in) && aClass.isInstance(out);
    }

    public static class Key {
        private BridgeParty in;
        private BridgeParty out;

        public Key(BridgeParty in, BridgeParty out) {
            this.in = in;
            this.out = out;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Key key = (Key) o;

            if (!in.equals(key.in)) return false;
            if (!out.equals(key.out)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = in.hashCode();
            result = 29 * result + out.hashCode();
            return result;
        }
    }

    public enum Duplex {
        HALF,
        FULL
    }

    public enum Resolution {
        IN_TO_OUT_HALF,
        OUT_TO_IN_HALF,
        TEARDOWN,
        DO_NOTHING
    }

    public Bridge(EventSourceManager cm, BridgeParty.EventSender bridgeErrorSender, BridgeParty in, BridgeParty out, boolean fullDuplex, boolean implicit) {
        this.bridgeErrorSender = bridgeErrorSender;
        this.in = in;
        this.out = out;
        this.implicit = implicit;
        if (fullDuplex)
            duplex = Duplex.FULL;
        else
            duplex = Duplex.HALF;
        eventSourceManager = cm;
    }

    public Resolution bridgeConflict(Bridge newBridge) {

        if (isRelated(newBridge)) {
            Duplex newDuplex = newBridge.duplex;

            //Full to full is a conflict
            if (newDuplex == Duplex.FULL && duplex == Duplex.FULL) {
                return Resolution.TEARDOWN;
            }
            //Half is a conflict if we're related to the in side of the new bridge
            if (newDuplex == Duplex.HALF && isRelatedToIn(newBridge)) {
                if (duplex == Duplex.FULL) {
                    //The new in is our in, we must join half duplex out to in
                    if (newBridge.getIn() == getIn()) {
                        return Resolution.OUT_TO_IN_HALF;
                    }
                    //The new in is out out (which is an in also,as this is full-duplex)
                    //We must join half duplex in to out
                    if (newBridge.getIn() == getOut()) {
                        return Resolution.IN_TO_OUT_HALF;
                    }
                } else {
                    //For half duplex, we can't do much. If we have a conflict, we must
                    //tear down this bridge.
                    return Resolution.TEARDOWN;
                }
            }
        }
        return Resolution.DO_NOTHING;
    }

    private boolean isRelatedToIn(Bridge newBridge) {
        return newBridge.getIn() == getIn() || newBridge.getIn() == getOut();
    }

    private boolean isRelatedToOut(Bridge newBridge) {
        return newBridge.getOut() == getIn() || newBridge.getOut() == getOut();
    }

    private boolean isRelated(Bridge newBridge) {
        return isRelatedToIn(newBridge) || isRelatedToOut(newBridge);
    }

    public void join(boolean implicit) {
        this.implicit = implicit;
        getConnectionManager().join(this, implicit);
    }

    public void unjoin(boolean implicit) {
        this.implicit = implicit;
        getConnectionManager().unjoin(this);
    }

    public BridgeParty getIn() {
        return in;
    }

    public BridgeParty getOut() {
        return out;
    }

    public Duplex getDuplex() {
        return duplex;
    }


    public static Bridge.Key createKey(BridgeParty in, BridgeParty out) {
        return new Key(in, out);
    }

    public static Key createKey(Bridge bridge) {
        return new Key(bridge.in, bridge.out);
    }

    public Key key() {
        return new Key(in, out);
    }

    private EventSourceManager getConnectionManager() {
        return eventSourceManager;
    }

    public void onJoin() {
        CCXMLEvent event = createJoinEvent(Constants.Event.CONFERENCE_JOINED);
        sendJoinEvent(event);
    }


    private void sendJoinEvent(CCXMLEvent event) {

        if (!implicit) {
            BridgeParty inParty = getConnectionManager().findParty(in.getBridgePartyId());
            BridgeParty outParty = getConnectionManager().findParty(out.getBridgePartyId());

            //Only send one event to each session
            if (inParty.getSessionId().equals(outParty.getSessionId())) {
                inParty.sendEvent(event,BridgeParty.EventTarget.CCXML);
            } else {
                outParty.sendEvent(event,BridgeParty.EventTarget.CCXML);
                event = event.clone();
                inParty.sendEvent(event.clone(), BridgeParty.EventTarget.CCXML);
            }
        }
    }


    public void onUnjoin() {
        CCXMLEvent event = createJoinEvent(Constants.Event.CONFERENCE_UNJOINED);
        sendJoinEvent(event);
    }

    private CCXMLEvent createJoinEvent(String conferenceUnjoined) {
        CCXMLEvent event = new CCXMLEvent(DebugInfo.getInstance());
        event.defineName(conferenceUnjoined);
        event.defineJoinRelated(this);
        return event;
    }

    public void onJoinError(String errorMessage) {

        CCXMLEvent event = createJoinEvent(Constants.Event.ERROR_CONFERENCE_JOIN);
        event.defineReason(errorMessage);

        //Only send an event to the originatng session. This is us
        //since we wouldn't get the message otherwise
        sendJoinErrorEvent(event);

    }

    private void sendJoinErrorEvent(CCXMLEvent event) {
        if (!implicit) {
            bridgeErrorSender.sendEvent(event);
        }
    }

    public void onUnjoinError(String errorMessage) {
        CCXMLEvent event = createJoinEvent(Constants.Event.ERROR_CONFERENCE_UNJOIN);
        event.defineReason(errorMessage);

        //Only send an event to the originatng session. This is us
        //since we wouldn't get the message otherwise
        sendJoinErrorEvent(event);
    }

    public String toString() {
        return "Bridge{" +
               "in=" + in +
               ", out=" + out +
               ", implicit=" + implicit +
               ", duplex=" + duplex +
               '}';
    }
}
