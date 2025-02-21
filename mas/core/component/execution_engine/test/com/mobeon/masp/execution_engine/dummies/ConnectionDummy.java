package com.mobeon.masp.execution_engine.dummies;

import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.InboundCall.RedirectStatusCode;
import com.mobeon.masp.callmanager.RedirectDestination;
import com.mobeon.masp.callmanager.events.ProgressingEvent;
import com.mobeon.masp.execution_engine.ccxml.BridgeParty;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.mock.MockAction;
import static com.mobeon.masp.execution_engine.mock.MockAction.Action.DELEGATE;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.common.logging.ILogger;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.IMediaStream;
import com.mobeon.masp.stream.IOutboundMediaStream;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Map;

/**
 * @author Mikael Andersson
 */
public class ConnectionDummy implements Connection {

    private DefaultExpectTarget defaultExpectTarget;
    private RuntimeData data;

    public RuntimeData getData() {
        return data;
    }

    public ConnectionDummy(DefaultExpectTarget defaultExpectTarget, RuntimeData data) {
        this.data = data;
        this.defaultExpectTarget = defaultExpectTarget;
    }

    protected ConnectionDummy() {
    }

    public IOutboundMediaStream getOutboundStream() {
        return null;
    }

    public IInboundMediaStream getInboundStream() {
        return null;
    }

    public Call getCall() {
        return null;
    }

    public void sendEvent(CCXMLEvent event) {
    }

    public void sendEvent(String event, Event related, EventTarget context) {

    }

    public String getBridgePartyId() {
        return null;
    }

    public void sendEvent(CCXMLEvent event, EventTarget ccxml) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void sendEvent(String eventName, Event related) {
    }

    public void receiveEvent(AutomatonEvent automatonEvent, Event related) {
    }

    public Map<AutomatonEvent, Transition> getPossibleTransitions() {
        return null;
    }

    public void createCall(CallProperties callProperties) {
    }

    public void accept() {
        defaultExpectTarget.Connection_accept();
    }
    
    public void proxy(String server, int port){    	
    }

    public void reject(String rejectEventTypeName, String reason) {
    }

    public void redirect() {
    }

    public void merge() {
    }

    public void forcedDisconnect() {
    }

    public void disconnect() {
    }

    public void record() {
    }

    public void stopRecording() {
    }

    public void play() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void stopPlaying() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateMediaStream(IMediaStream mediaStream) {
    }

    public void setCall(Call call) {
    }

    public State getState() {
        return null;
    }

    public Connection clone() {
        return null;
    }

    public ScriptableObject getVoiceXMLMirror() {
        return null;
    }

    public ILogger getLog() {
        return null;
    }

    public String getClassName() {
        return null;
    }

    public Object get(String name, Scriptable start) {
        return null;
    }

    public Object get(int index, Scriptable start) {
        return null;
    }

    public boolean has(String name, Scriptable start) {
        return false;
    }

    public boolean has(int index, Scriptable start) {
        return false;
    }

    public void put(String name, Scriptable start, Object value) {
    }

    public void put(int index, Scriptable start, Object value) {
    }

    public void delete(String name) {
    }

    public void delete(int index) {
    }

    public Scriptable getPrototype() {
        return null;
    }

    public void setPrototype(Scriptable prototype) {
    }

    public Scriptable getParentScope() {
        return null;
    }

    public void setParentScope(Scriptable parent) {
    }

    public Object[] getIds() {
        return new Object[0];
    }

    public Object getDefaultValue(Class hint) {
        return null;
    }

    public boolean hasInstance(Scriptable instance) {
        return false;
    }

    public boolean join(Connection conn2, String duplexValue) {
        return false;
    }

    public boolean join(BridgeParty conn2, boolean fullDuplex, boolean implicit) {
        return false;
    }

    public void onJoin(Connection conn2) {
    }

    @MockAction(DELEGATE)
    public String getSessionId() {
        return getData().getExecutionContext().getSessionId();
    }

    public void onJoinError(Connection conn2, String errorMessage) {
    }

    public void onEarlyMedia() {
    }

    public void onConnected() {

    }

    public void onPlayEnded() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onRecordEnded() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getCallManagerWaitTimeout() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getCreateCallAdditionalTimeout() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cleanup() {


    }

    public void onEarlyMediaFailed() {
    }

    public void onAlerting() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onProgressing(ProgressingEvent realEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onProgressing() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean unjoin(BridgeParty otherParty) {
        return false;
    }

    public void compareAndSetOtherParty(BridgeParty otherParty, BridgeParty conn2) {
    }

    public void onUnjoin(Connection conn2) {
    }

    public void onUnjoinError(Connection conn2, String errorMessage) {
    }

    public boolean isInTerminalState() {
        return false;
    }

    public ExecutionContext getExecutionContext() {
        return null;
    }

    @Override
    public void redirect(RedirectDestination destination, RedirectStatusCode redirectCode) {
        // TODO Auto-generated method stub
        
    }
}
