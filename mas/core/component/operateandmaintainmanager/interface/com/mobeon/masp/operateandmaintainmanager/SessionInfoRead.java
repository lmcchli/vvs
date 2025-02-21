package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public interface SessionInfoRead {
    public String getSessionId();
    public String getService();
    public String getSessionInitiator();
    public CallType getConnetionType();
    public CallState getConnetionState();
    public CallDirection getDirection();
    public CallActivity getOutboundActivity() ;
    public CallActivity gettInboundActivity() ;
    public String getANI() ;
    public String getDNIS() ;
    public String getRDNIS() ;
    public String getFarEndConProp() ;
    public Boolean isDisposed() ;
    public Integer getPos() ;
    public Boolean monitoring();
    public Boolean isPrinted();
    public void setPrinted();
}
