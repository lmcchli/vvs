package com.mobeon.ntf.out.sip;

import com.abcxyz.services.moip.ntf.event.SipMwiEvent;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * Interface for making SIP MWI calls.
 * We go via an interface to simplify testing with mock objects.
 */
public interface SipMwiCallSpec
{
    /**
     * Try to call the given number for the user.
     * @param SipMwiEvent sipMwiEvent.
     * @param SIPInfo SIPInfo 
     * @param user User to call for.
     * @param listener Where to report call status.
     */
    void sendCall(SipMwiEvent sipMwiEvent, SIPInfo sipInfo, UserInfo user, SIPCallListener listener);
}
