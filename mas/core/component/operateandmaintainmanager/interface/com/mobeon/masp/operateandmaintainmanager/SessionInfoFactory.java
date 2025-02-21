package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public interface SessionInfoFactory {
    public SessionInfo getSessionInstance(String sessionId,String connectionId);
    public void returnSessionInstance(SessionInfo sessionInfo);


}
