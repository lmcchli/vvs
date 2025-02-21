/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;

import java.io.Serializable;

/**
* This class handles call sessions.
* Sessions can be put and removed.
*
* The class keep track of session positions so when a session is removed there is an "empty"
* entry for antother session to use.
*/

public class SessionInfoImpl implements SessionInfo,SessionInfoRead, Serializable {
    //private ILogger log;
    // State values
    private Long changedTime;
    private Long readTime;
    private Long printTime=(long)0;
    private boolean disposed;

    Integer pos; // the possition in table that this session is assigned to.

    // Object values
    private String sessionId;
    private String connectionId;
    private String service="";
    private String sessionInit="";
    private CallType callType = CallType.VOICE_VIDEO_UNKNOWN;
    private CallState callState = CallState.UNKNOWN;
    private CallDirection callDirection = CallDirection.UNKNOWN;
    private CallActivity inboundActivity = CallActivity.UNKNOWN;
    private CallActivity outboundActivity = CallActivity.UNKNOWN;
    private String ani="";
    private String dnis="";
    private String rdnis="";
    private String farEndConProp="";

    //private SessionInfoFactoryImpl factory;



    public SessionInfoImpl(SessionInfoFactoryImpl factory){
        //this.factory = factory;
        //log = ILoggerFactory.getILogger(SessionInfoImpl.class);
        readTime = Long.MIN_VALUE;
        changedTime = System.currentTimeMillis();

    }

    public Boolean monitoring(){
        return true; //factory.monitoring();
    }

    public void setPos(Integer pos){
        this.pos = pos;
    }

    public Integer getPos(){
        return this.pos;
    }

    public void setRead(){
        readTime = System.currentTimeMillis();
    }


    public void setSessionId(String sessionId){
        this.sessionId = sessionId;
    }

    public void setConnectionId(String connectionId){
        this.connectionId = connectionId;
    }


    public void setService(String service) throws IlegalSessionInstanceException {
        sanityCheck();
        changedTime = System.currentTimeMillis();
        if (service == null)
            service = "";
        this.service = service;
    }

    public void setSessionInitiator(String sessionInit) throws IlegalSessionInstanceException {
        sanityCheck();
        changedTime = System.currentTimeMillis();
        if (sessionInit == null)
            sessionInit = "";
        this.sessionInit = sessionInit;
    }

    public void setConnetionType(CallType callType) throws IlegalSessionInstanceException{
        sanityCheck();
        changedTime = System.currentTimeMillis();
        if (callType == null)
            callType = CallType.VOICE_VIDEO_UNKNOWN;

        this.callType = callType;
    }

    public void setConnetionState(CallState callState) throws IlegalSessionInstanceException{
        sanityCheck();
        changedTime = System.currentTimeMillis();
        if (callState == null)
            callState = CallState.UNKNOWN;

        this.callState = callState;
    }

    public void setDirection(CallDirection direction) throws IlegalSessionInstanceException{
        sanityCheck();
        changedTime = System.currentTimeMillis();
        if (direction == null)
            direction = CallDirection.UNKNOWN;

        this.callDirection = direction;
    }

    public void setOutboundActivity(CallActivity outboundActivity) throws IlegalSessionInstanceException{
        sanityCheck();
        changedTime = System.currentTimeMillis();
        if (outboundActivity == null)
            outboundActivity = CallActivity.UNKNOWN;

        this.outboundActivity = outboundActivity;
    }

    public void setInboundActivity(CallActivity inboundActivity) throws IlegalSessionInstanceException{
        sanityCheck();
        changedTime = System.currentTimeMillis();
        if (inboundActivity == null)
            inboundActivity = CallActivity.UNKNOWN;

        this.inboundActivity = inboundActivity;

    }

    public void setANI(String ani) throws IlegalSessionInstanceException{
        sanityCheck();
        changedTime = System.currentTimeMillis();
        if (ani == null)
            ani = "";

        this.ani = ani;
    }

    public void setDNIS(String dnis) throws IlegalSessionInstanceException{
        sanityCheck();
        changedTime = System.currentTimeMillis();
        if (dnis == null)
            dnis = "";

        this.dnis = dnis;
    }

    public void setRDNIS(String rdnis) throws IlegalSessionInstanceException{
        sanityCheck();
        changedTime = System.currentTimeMillis();
        if (rdnis == null)
            rdnis = "";

        this.rdnis = rdnis;
    }

    public void setFarEndConProp(String prop) throws IlegalSessionInstanceException{
        sanityCheck();
        changedTime = System.currentTimeMillis();
        if (prop == null)
            prop = "";

        this.farEndConProp = prop;
    }


    public String getSessionId() {
        return sessionId;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getKey() {
        return sessionId+connectionId;
    }

    public String getService() {
        return this.service;
    }

    public String getSessionInitiator() {
        return  this.sessionInit;
    }

    public CallType getConnetionType() {
        return  this.callType;
    }

    public CallState getConnetionState() {
        return  this.callState;
    }

    public CallDirection getDirection() {
        return  this.callDirection;
    }

    public CallActivity getOutboundActivity() {
        return this.outboundActivity ;
    }

    public CallActivity gettInboundActivity() {
        return this.inboundActivity;
    }

    public String getANI() {
        return this.ani;
    }

    public String getDNIS() {
        return this.dnis;
    }

    public String getRDNIS() {
        return this.rdnis;
    }

    public String getFarEndConProp() {
        return this.farEndConProp;
    }


    public void setDisposed(){
        changedTime = System.currentTimeMillis();
        disposed = true;
    }

    private void sanityCheck() throws IlegalSessionInstanceException {
        if (disposed) {
            throw new IlegalSessionInstanceException("This session instance is disposed.");
        }
    }

    public Boolean isDisposed(){
        return disposed;
    }

    /**
     * The objcet has been changed since last time.
     * Only used to desice what sessions to send thrue RPC.
     * @return changed
     */
    public Boolean isChanged(){
        return readTime <= changedTime;
    }

    // Returns if changes is printed or not
    public Boolean isPrinted(){
        return printTime >= changedTime;
    }

    // tells the session that chages is printed
    public void setPrinted(){
        printTime = System.currentTimeMillis();
    }

    // This session is disposed. for a long time agon. and should be removed
    // Returns true if time since disposal is longer then 5 sec.
    public Boolean isOld(){
        return (changedTime+5000) < System.currentTimeMillis() ;
    }

}
