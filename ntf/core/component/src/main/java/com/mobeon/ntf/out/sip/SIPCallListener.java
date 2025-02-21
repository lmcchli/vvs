/**
 * Copyright (c) 2009 Abcxyz
 * All Rights Reserved
 */

package com.mobeon.ntf.out.sip;

import com.abcxyz.services.moip.ntf.event.SipMwiEvent;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * Handles result from SIPCaller.
 */
public interface SIPCallListener {
    public void handleResult(SipMwiEvent sipMwiEvent, UserInfo userInfo, int code, int retryTime);
}
