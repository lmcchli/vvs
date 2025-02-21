/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out;

import com.mobeon.ntf.userinfo.UserInfo;


/**
 * FeedbackHandler handles information about the outcome of an attempt to send
 * a notification through one of the out channels.
 */
public interface FeedbackHandler {
    /**
     * ok is used to tell that this user was successfully notified by one of the
     * delivery interfaces.
     *@param user the user that was successfully notified.
     *@param notifType the delivery interface that successfully notified the user.
     */
    void ok(UserInfo user, int notifType);

    /**
     * ok is used to tell that this user was successfully notified by one of the
     * delivery interfaces.
     *@param user the user that was successfully notified.
     *@param notifType the delivery interface that successfully notified the user.
     *@param sendToMer send to mer in order to write MDR
     */
    void ok(UserInfo user, int notifType, boolean sendToMer);

    /**
     * Failed tells FeedbackHandler that notification of this user failed on one
     * of the delivery interfaces.
     *@param user the user whose delivery failed.
     *@param notifType the delivery interface reporting failure.
     *@param msg a message describing why the delivery failed.
     */
    void failed(UserInfo user, int notifType, String msg);

    /**
     * Expired tells FeedbackHandler that the notification expired before it
     * was delivered to the user.
     *@param user the user whose notification expired.
     *@param notifType the delivery interface reporting expiry.
     */
    void expired(UserInfo user, int notifType);


    /**
     * Retry tells FeedbackHandler that notification of this user failed
     * temporarily on one of the delivery interfaces, and that it may help
     * to retry later.
     *@param user the user
     *@param notifType the delivery interface that failed.
     *@param msg a message describing why the notification failed.
     */
    void retry(UserInfo user, int notifType, String msg);
}
