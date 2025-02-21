/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf;

/****************************************************************
 * Interface for classes interested in knowing when the processing of
 * a notification is completed
 */
public interface NotifCompletedListener {
    
    /****************************************************************
     * notifCompleted is called to notify the listener that a notification is
     * completed and that all about it can be forgotten.
     * @param notifId identifies the notification
     * @param receiver is a reference from when the notification was created,
     * returned to the listener with this call. Intended for routing
     * among iternal components in the receiver
     */
    void notifCompleted(int notifId, int receiver);

    /****************************************************************
     * notifRetry is called to notify the listener that a notification is
     * partly completed, but should be retried for some receivers.
     *@param notifId identifies the notification
     *@param receiver is a reference from when the notification was created,
     * returned to the listener with this call. Intended for routing
     * among iternal components in the receiver
     *@param retryAddresses String with all the mail address that should be
     * retried, separated by comma.
     */
    void notifRetry(int notifId, int receiver, String retryAddresses);


    /**
     * notifrenew is called to make a message unseen.
     * @param notifId identifies the notification
     * @param receiver is a reference from when the notification was created,
     * returned to the listener with this call. Intended for routing
     * among iternal components in the receiver
     */
    void notifRenew(int notifId, int receiver);

    /**
     * notifrenew is called to release memory for a message without changing anyting on the mail.
     * @param notifId identifies the notification
     * @param receiver is a reference from when the notification was created,
     * returned to the listener with this call. Intended for routing
     * among iternal components in the receiver
     */
    void notifRelease(int notifId, int receiver);
}
