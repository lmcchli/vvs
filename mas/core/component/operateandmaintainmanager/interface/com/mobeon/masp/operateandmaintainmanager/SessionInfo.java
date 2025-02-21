package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public interface SessionInfo {
    public void setService(String service) throws IlegalSessionInstanceException;
    public void setSessionInitiator(String sessionInit )throws IlegalSessionInstanceException;
    public void setConnetionType(CallType callType)throws IlegalSessionInstanceException;
    public void setConnetionState(CallState connectionState)throws IlegalSessionInstanceException;
    public void setDirection(CallDirection direction)throws IlegalSessionInstanceException;
    public void setOutboundActivity(CallActivity outboundActivity)throws IlegalSessionInstanceException;
    public void setInboundActivity(CallActivity inboundActivity)throws IlegalSessionInstanceException;
    public void setANI(String ani)throws IlegalSessionInstanceException;
    public void setDNIS(String dnis)throws IlegalSessionInstanceException;
    public void setRDNIS(String rdnis)throws IlegalSessionInstanceException;
    public void setFarEndConProp(String prop)throws IlegalSessionInstanceException;

    public String getSessionId();
    public String getConnectionId();


}
