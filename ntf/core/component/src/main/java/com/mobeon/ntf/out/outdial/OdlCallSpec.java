package com.mobeon.ntf.out.outdial;

import com.mobeon.ntf.userinfo.UserInfo;

/**
 * Interface for making the outcall.
 * We go via an interface to simplify testing with mock objects.
 */
public interface OdlCallSpec
{
    /**
     * Try to call the given number for the user.
     * @param subscriberNumber Mailbox.
     * @param notificationNumber Number to call.
     * @param user User to call for.
     * @param listener Where to report call status.
     */
    void sendCall(String subscriberNumber, String notificationNumber, UserInfo user, OdlCallListener listener);
}
