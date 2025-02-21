package com.mobeon.masp.execution_engine.dummies;

import com.mobeon.masp.callmanager.*;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.*;

import java.util.Set;
import java.util.Collections;
import java.util.TreeSet;

/**
 * @author Mikael Andersson
 */
public class CallDummy implements InboundCall, OutboundCall {

    DefaultExpectTarget expectTarget;

    public void accept() {
        expectTarget.Call_accept(this);
    }

    public void negotiateEarlyMediaTypes() {
    }

    public CallDummy(DefaultExpectTarget expectTarget) {
        this.expectTarget = expectTarget;
    }

    /**
     * Prevent instantiation by mistake
     */
    protected CallDummy() {
    }

    public RedirectingParty getRedirectingParty() {
        return null;
    }

    public void proxy(RemotePartyAddress uas) {   
    	expectTarget.Call_proxy(this);
    }

    public void reject(String rejectEventTypeName, String reason) {
    }

    public void disconnect() {
    	expectTarget.Call_disconnect(this);
    }

    public CallProperties.CallType getCallType() {
        return null;
    }

    public CallingParty getCallingParty() {
        return null;
    }

    public CalledParty getCalledParty() {
        return null;
    }

    public String getProtocolName() {
        return null;
    }

    public String getProtocolVersion() {
        return null;
    }

    public ISession getSession() {
        return null;
    }

    public void play(Object id, IMediaObject mediaObject, IOutboundMediaStream.PlayOption playOption, long cursor) throws IllegalArgumentException {
    }

    public void play(Object id, IMediaObject mediaObjects[], IOutboundMediaStream.PlayOption playOption, long cursor) throws IllegalArgumentException {
    }

    public void record(Object id, IMediaObject recordMediaObject, RecordingProperties properties) throws IllegalArgumentException {
    }

    public void record(Object id, IMediaObject playMediaObject, IMediaObject recordMediaObject, RecordingProperties properties) throws IllegalArgumentException {
    }

    public void stopPlay(Object id) throws IllegalArgumentException {
    }

    public void stopRecord(Object id) throws IllegalArgumentException {
    }

    public IInboundMediaStream getInboundStream() {
        return null;
    }

    public void sendToken(ControlToken[] tokens) {
    }

    public void dial() {
    }
    public Set<Connection> getFarEndConnections() {
        return Collections.unmodifiableSet(Collections.synchronizedSet(new TreeSet<Connection>()));
    }

    public IOutboundMediaStream getOutboundStream() {
        return null;
    }
    public int getInboundBitRate() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void redirect(RedirectDestination destination, RedirectStatusCode redirectCode) {
        // TODO Auto-generated method stub
        
    }

}
