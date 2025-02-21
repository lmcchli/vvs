/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

/**
 * A container of properties related to an outbound call. It shall be used
 * when creating a new outbound call using {@link CallManager#createCall(
 * CallProperties,
        com.mobeon.common.eventnotifier.IEventDispatcher,
        com.mobeon.masp.execution_engine.session.ISession)}.
 * <p>
 * Mandatory parameters in {@link CallManager#createCall(
 * CallProperties,
        com.mobeon.common.eventnotifier.IEventDispatcher,
        com.mobeon.masp.execution_engine.session.ISession)}
 * are:
 * <ul>
 * <li>{@link CallProperties#calledParty}</li>
 * <li>{@link CallProperties#callingParty}</li>
 * <li>{@link CallProperties#maxDurationBeforeConnected}</li>
 * </ul>
 * <p>
 * Default values for optional parameters:
 * <ul>
 * <li>{@link CallProperties#callType}: retrieved from configuration.</li>
 * <li>{@link CallProperties#maxCallDuration}: 0, i.e. no call duration
 * limitation is activated.</li>
 * <li>{@link CallProperties#preventLoopback}: false, i.e. loopback is not
 * prevented.</li>
 * </ul>
 * <p>
 * This class is thread-safe.
 *
 * @see com.mobeon.masp.callmanager.CallingParty
 * @see com.mobeon.masp.callmanager.CalledParty
 *
 * @author Malin Flodin
 */
public class CallProperties {

    private CalledParty calledParty;
    private CallingParty callingParty;
    private DiversionParty diversionParty;
    
    private int maxCallDuration;
    private int maxDurationBeforeConnected;
    private boolean preventLoopback = false;
    private CallType callType = CallType.UNKNOWN;
    private String outboundCallServerHost;
    private int outboundCallServerPort;
    private boolean isOutboundCallServerPortSet = false;

    /**
     * Only invoke this is if isOutboundCallServerPortSet() return true
     * @return the port
     */
    public int getOutboundCallServerPort() {
        return outboundCallServerPort;
    }

    public void setOutboundCallServerPort(int outboundcallserverport) {
        isOutboundCallServerPortSet = true;
        this.outboundCallServerPort = outboundcallserverport;
    }

    public String getOutboundCallServerHost() {
        return outboundCallServerHost;
    }

    public void setOutboundCallServerHost(String outboundCallServerHost) {
        this.outboundCallServerHost = outboundCallServerHost;
    }

    public boolean isOutboundCallServerPortSet() {
        return isOutboundCallServerPortSet;
    }

    public enum CallType {
        VOICE, VIDEO, UNKNOWN
    }

    /**
     * Returns the called party.
     * @return the called party or null if it has not been set.
     */
    public synchronized CalledParty getCalledParty() {
        return calledParty;
    }

    public synchronized void setCalledParty(CalledParty calledParty) {
        this.calledParty = calledParty;
    }

    /**
     * Returns the calling party.
     * @return the calling party or null if it has not been set.
     */
    public synchronized CallingParty getCallingParty() {
        return callingParty;
    }

    public synchronized void setCallingParty(CallingParty callingParty) {
        this.callingParty = callingParty;
    }

    /**
     * Returns the diversion party.
     * @return the diversion party or null if it has not been set.
     */
    public synchronized DiversionParty getDiversionParty() {
        return diversionParty;
    }

    public synchronized void setDiversionParty(DiversionParty diversionParty) {
        this.diversionParty = diversionParty;
    }

    /**
     * Returns the max call duration in milli seconds.
     * @return the max call duration or zero if it has not been set.
     */
    public synchronized int getMaxCallDuration() {
        return maxCallDuration;
    }

    /**
     * Sets the max call duration.
     * @param maxCallDuration The duration in milli seconds.
     */
    public synchronized void setMaxCallDuration(int maxCallDuration) {
        this.maxCallDuration = maxCallDuration;
    }

    /**
     * Returns the max duration before call is connected in milli seconds.
     * @return the max duration before connected or zero if it has not been set.
     */
    public synchronized int getMaxDurationBeforeConnected() {
        return maxDurationBeforeConnected;
    }

    /**
     * Sets the max time before call is connected.
     * @param maxDurationBeforeConnected The time in milli seconds.
     */
    public synchronized void setMaxDurationBeforeConnected(
            int maxDurationBeforeConnected) {
        this.maxDurationBeforeConnected = maxDurationBeforeConnected;
    }

    /**
     * Returns whether or not loopback shall be prevented.
     * @return the loop back prevention or false if it has not been set.
     */
    public synchronized boolean getPreventLoopback() {
        return preventLoopback;
    }

    public synchronized void setPreventLoopback(boolean preventLoopback) {
        this.preventLoopback = preventLoopback;
    }

    /**
     * Returns the call type.
     * @return the call type or {@link CallProperties.CallType#VOICE} if it has
     * not been set.
     */
    public synchronized CallType getCallType() {
        return callType;
    }

    public synchronized void setCallType(CallType callType) {
        this.callType = callType;
    }

    public String toString() {
        return "CallProperties: <CallType=" + getCallType() +
                ">, <CallingParty=" + getCallingParty() +
                ">, <CalledParty=" + getCalledParty() +
                ">, <MaxCallDuration=" + getMaxCallDuration() +
                ">, <MaxDurationBeforeConnected=" + getMaxDurationBeforeConnected() +
                ">, <OutboundCallServer=" + getOutboundCallServerHost() + 
                ":"+ (isOutboundCallServerPortSet ? getOutboundCallServerPort() : "outbound call server port not set")+
                ">, <PreventLoopback=" + getPreventLoopback() + ">";
    }
}
